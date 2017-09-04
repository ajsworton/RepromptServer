// Copyright (C) 2017 Alexander Worton.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package models.dao
import java.time.LocalDate
import javax.inject.Inject

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import models.User
import models.dto.ContentDisabledDto.ContentDisabledTable
import models.dto.ScoreDto.ScoreTable
import models.dto.{ AnswerDto, ContentAssignedDto, ContentDisabledDto, ContentItemDto, ExamHistoricalPoint, ExamHistoryDto, QuestionDto, ScoreDto }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.{ GetResult, JdbcProfile }
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class StudyDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends StudyDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val Scores = TableQuery[ScoreTable]
  private val ContentDisabled = TableQuery[ContentDisabledTable]

  private def findContentItemsQuery(userId: Int) =
    sql"""
          SELECT  ci.Id, ci.PackageId, ci.ImageUrl, ci.Name, ci.Content, 1,
                           cs.UserId, cs.ContentItemId, cs.Score, cs.ScoreDate, cs.Streak, cs.RepromptDate,
                           q.Id, q.Question, q.Format, q.ItemId,
                           a.Id, a.QuestionId, a.Answer, a.Correct, a.Sequence

          FROM content_assigned AS ca

            JOIN content_assigned_cohorts AS cac
            ON cac.AssignedId = ca.Id

            JOIN cohorts
            ON cohorts.Id = cac.CohortId

            JOIN cohort_members AS cm
            ON cm.CohortId = cohorts.Id

            JOIN content_assigned_packages AS cap
            ON cap.AssignedId = ca.Id

            JOIN content_items AS ci
            ON ci.PackageId = cap.PackageId

            JOIN content_assessment_questions AS q
            ON ci.Id = q.ItemId

            JOIN content_assessment_answers AS a
            ON q.Id = a.QuestionId

            LEFT JOIN content_disabled AS cd
            ON cd.ContentAssignedId = ca.Id
            AND cd.UserId = cm.UserId

            LEFT JOIN content_scores AS cs
            ON ci.Id = cs.ContentItemId
            AND cs.UserId = cm.UserId

          WHERE cm.UserId = $userId
          AND (cs.RepromptDate IS NULL OR cs.RepromptDate <= CURDATE())
          AND NOT EXISTS (
            SELECT 1
            FROM content_scores csx
            WHERE csx.ScoreDate > cs.ScoreDate
            AND csx.ContentItemId = cs.ContentItemId
            AND csx.UserId = cs.UserId
            AND csx.ScoreDate = cs.ScoreDate
          )
          AND cd.UserId IS NULL

            ORDER BY ci.Name, q.Question
         """.as[(ContentItemDto, Option[ScoreDto], Option[QuestionDto], Option[AnswerDto])]

  override def getContentItems(userId: Int): Future[List[ContentItemDto]] = {
    val result = findContentItemsQuery(userId)
    val run = db.run(result)

    run.flatMap(
      r => {
        val questions = extractQuestionsWithAnswersFromResultVector(r)
        val contentItems = extractContentItemsFromResultVector(r, userId)
        Future(applyQuestionsToParentContentItems(contentItems, questions))
      }
    )
  }

  private def extractQuestionsWithAnswersFromResultVector(r: Vector[(ContentItemDto, Option[ScoreDto], Option[QuestionDto], Option[AnswerDto])]) = {
    val groupedByQuestion = r.groupBy(_._3)
    for {
      questions <- groupedByQuestion
      questionData = questions._2
      answerList = questionData.map(p => p._4.get).toSet.toList.filter(m => m.id.get > 0)
      questionsProc = questions._1.map(q => q.copy(answers = Some(answerList)))
      if questionsProc.isDefined
    } yield questionsProc.get
  }

  private def extractContentItemsFromResultVector(r: Vector[(ContentItemDto, Option[ScoreDto], Option[QuestionDto], Option[AnswerDto])], userId: Int) = {
    val groupedByContentItem = r.groupBy(_._1)
    for {
      items <- groupedByContentItem
      score = items._2.head._2
      itemData = score match {
        case None => items._1
        case Some(scoreDto) => items._1.copy(score = Some(scoreDto.copy(
          userId = Some(userId),
          contentItemId = items._1.id.get)
        )
        )
      }
    } yield itemData
  }

  private def applyQuestionsToParentContentItems(
    contentItems: Iterable[ContentItemDto],
    questions: Iterable[QuestionDto]) = {
    contentItems.map(item => item.copy(questions =
      Some(questions.toSet.toList.filter(q => q.itemId == item.id.get)))).toList
  }

  override def saveScoreData(scoreData: ScoreDto): Future[Either[String, ScoreDto]] = {
    db.run((Scores += scoreData).asTry) map {
      case Failure(ex) => Left(ex.getMessage)
      case Success(response) => response match {
        case 1 => Right(scoreData)
        case _ => Left("Failed to write")
      }
    }
  }

  implicit val getLocalDate: GetResult[LocalDate] = GetResult(r => r.nextDate.toLocalDate)

  private def findExamDateQuery(contentItemId: Int) =
    sql"""
          SELECT  ca.ExamDate
          FROM content_items as ci, content_assigned_packages as cap, content_assigned as ca

          WHERE ci.PackageId = cap.PackageId
          AND cap.AssignedId = ca.Id
          AND ci.Id = $contentItemId

          LIMIT 1
         """.as[LocalDate]

  override def getExamDateByContentItemId(contentItemId: Int): Future[Option[LocalDate]] = {
    val result = findExamDateQuery(contentItemId)
    db.run(result.headOption)
  }

  override def getContentAssignedStatusByUserId(userId: Int): Future[List[ContentAssignedDto]] = {
    db.run(findContentAssignedWIthStatusQuery(userId)) flatMap {
      r => Future(r.toList)
    }
  }

  private def findContentAssignedWIthStatusQuery(userId: Int) =
    sql"""

      SELECT  DISTINCT ca.Id, ca.Name, ca.ExamDate, ca.active, ca.ownerId, cd.UserId IS NULL AS enabled

      FROM content_assigned AS ca

      JOIN content_assigned_cohorts AS cac
        ON cac.AssignedId = ca.Id

      JOIN cohorts
        ON cohorts.Id = cac.CohortId

      JOIN cohort_members AS cm
        ON cm.CohortId = cohorts.Id

      LEFT JOIN content_disabled AS cd
      ON cd.ContentAssignedId = ca.Id
      AND cd.UserId = cm.UserId

      WHERE cm.UserId = $userId
      AND ca.Active = TRUE

      ORDER BY ca.ExamDate""".as[ContentAssignedDto]

  override def disableContentAssigned(assignedId: Int, userId: Int): Future[Int] = {
    db.run((ContentDisabled += ContentDisabledDto(assignedId, userId)).asTry) map {
      case Failure(_) => 0
      case Success(_) => 1
    }
  }

  override def enableContentAssigned(assignedId: Int, userId: Int): Future[Int] = {
    db.run(ContentDisabled.filter(disabled => disabled.assignedId === assignedId &&
      disabled.userId === userId).delete)
  }

  private def findHistoricalPerformanceQuery(userId: Int) = sql"""
    SELECT  ca.Name, cs.ScoreDate AS PointName, cs.Score AS PointValue

    FROM content_scores AS cs, content_items AS ci, content_assigned_packages AS cap, content_assigned AS ca

    WHERE ca.Id = cap.AssignedId
    AND cap.PackageId = ci.PackageId
    AND ci.Id = cs.ContentItemId
    AND cs.UserId = $userId

    ORDER BY ca.Name ASC, cs.ScoreDate ASC

    """.as[(ExamHistoryDto, ExamHistoricalPoint)]

  override def getHistoricalPerformanceByExam(userId: Int): Future[List[ExamHistoryDto]] = {
    db.run(findHistoricalPerformanceQuery(userId)) flatMap {
      sqlResponse => createAggregatedExamPerformanceDto(sqlResponse)
    }
  }

  private def createAggregatedExamPerformanceDto(r: Vector[(ExamHistoryDto, ExamHistoricalPoint)]) = {
    val dtos = for {
      groupedByDto <- r.groupBy(_._1)
      examPointsGroupedByName = groupedByDto._2.map(_._2).groupBy(_.name)
      averagedExamPoints = examPointsGroupedByName.map(
        e => ExamHistoricalPoint(name = e._1, value = getAverageValue(e._2)))
      dto = groupedByDto._1.copy(series = averagedExamPoints.toList)
    } yield dto
    Future(dtos.toList)
  }

  private def getAverageValue(xs: Iterable[ExamHistoricalPoint]): Int = {
    xs match {
      case Nil => 0
      case _ => xs.map(_.value).sum / xs.size
    }
  }

  private def getStudentsWithPendingContentQuery = sql"""
  SELECT DISTINCT u.Id, u.FirstName, u.Surname, u.email, u.isEmailVerified, u.isEducator,
  u.isAdministrator, u.avatarUrl

  FROM users AS u

    JOIN cohort_members AS cm
    ON u.Id = cm.UserId

    JOIN cohorts AS c
    ON c.Id = cm.CohortId

    LEFT JOIN content_scores AS cs
    ON u.Id = cs.UserId

  WHERE cs.RepromptDate <= CURDATE()
  OR cs.RepromptDate IS NULL
  ORDER BY u.SurName, u.FirstName
    """.as[User]

  override def getStudentsWithPendingContent: Future[Seq[User]] = {
    db.run(getStudentsWithPendingContentQuery)
  }
}
