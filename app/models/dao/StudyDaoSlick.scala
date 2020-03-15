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

import models.User
import models.dto.ContentDisabledDto.ContentDisabledTable
import models.dto.ScoreDto.ScoreTable
import models.dto.{AnswerDto, ContentAssignedDto, ContentDisabledDto, ContentItemDto, ExamHistoricalPoint, ExamHistoryDto, QuestionDto, ScoreDto}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class StudyDaoSlick @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
    extends StudyDao
    with HasDatabaseConfigProvider[JdbcProfile] {

  private val Scores          = TableQuery[ScoreTable]
  private val ContentDisabled = TableQuery[ContentDisabledTable]

  private def findContentItemsQuery(userId: Int) =
    sql"""
          SELECT  ci.Id, ci.package_id, ci.image_url, ci.name, ci.content, 'true',
                           cs.user_id, cs.content_item_id, cs.score, cs.score_date, cs.streak, cs.reprompt_date,
                           q.id, q.question, q.format, q.item_id,
                           a.id, a.question_id, a.answer, a.correct, a.sequence

          FROM content_assigned AS ca

            JOIN content_assigned_cohorts AS cac
            ON cac.assigned_id = ca.id

            JOIN cohorts
            ON cohorts.Id = cac.cohort_id

            JOIN cohort_members AS cm
            ON cm.cohort_id = cohorts.Id

            JOIN content_assigned_packages AS cap
            ON cap.assigned_id = ca.Id

            JOIN content_items AS ci
            ON ci.package_id = cap.package_id

            JOIN content_assessment_questions AS q
            ON ci.Id = q.item_id

            JOIN content_assessment_answers AS a
            ON q.Id = a.question_id

            LEFT JOIN content_disabled AS cd
            ON cd.content_assigned_id = ca.id
            AND cd.user_id = cm.user_id

            LEFT JOIN content_scores AS cs
            ON ci.Id = cs.content_item_id
            AND cs.user_id = cm.user_id

          WHERE cm.user_id = $userId
          AND (cs.reprompt_date IS NULL OR cs.reprompt_date <= NOW()::date)
          AND NOT EXISTS (
            SELECT 1
            FROM content_scores csx
            WHERE csx.score_date > cs.score_date
            AND csx.content_item_id = cs.content_item_id
            AND csx.user_id = cs.user_id
          )
          AND cd.content_assigned_id IS NULL

            ORDER BY ci.name, q.question
         """.as[(ContentItemDto, Option[ScoreDto], Option[QuestionDto], Option[AnswerDto])]

  override def getContentItems(userId: Int): Future[List[ContentItemDto]] = {
    val result = findContentItemsQuery(userId)
    val run    = db.run(result)

    run.flatMap(
      r => {
        val questions    = extractQuestionsWithAnswersFromResultVector(r)
        val contentItems = extractContentItemsFromResultVector(r, userId)
        Future(applyQuestionsToParentContentItems(contentItems, questions))
      }
    )
  }

  private def extractQuestionsWithAnswersFromResultVector(r: Vector[(ContentItemDto, Option[ScoreDto], Option[QuestionDto], Option[AnswerDto])]) = {
    val groupedByQuestion = r.groupBy(_._3)
    for {
      questions <- groupedByQuestion
      questionData  = questions._2
      answerList    = questionData.map(p => p._4.get).toSet.toList.filter(m => m.id.get > 0)
      questionsProc = questions._1.map(q => q.copy(answers = Some(answerList)))
      if questionsProc.isDefined
    } yield questionsProc.get
  }

  private def extractContentItemsFromResultVector(r: Vector[(ContentItemDto, Option[ScoreDto], Option[QuestionDto], Option[AnswerDto])],
                                                  userId: Int) = {
    val groupedByContentItem = r.groupBy(_._1)
    for {
      items <- groupedByContentItem
      score = items._2.head._2
      itemData = score match {
        case None => items._1
        case Some(scoreDto) =>
          items._1.copy(
            score = Some(
              scoreDto.copy(
                userId = Some(userId),
                contentItemId = items._1.id.get
              )))
      }
    } yield itemData
  }

  private def applyQuestionsToParentContentItems(
      contentItems: Iterable[ContentItemDto],
      questions: Iterable[QuestionDto]
  ) =
    contentItems.map(item => item.copy(questions = Some(questions.toSet.toList.filter(q => q.itemId == item.id.get)))).toList

  override def saveScoreData(scoreData: ScoreDto): Future[Either[String, ScoreDto]] =
    db.run(Scores.insertOrUpdate(scoreData).asTry) map {
      case Failure(ex) => Left(ex.getMessage)
      case Success(response) =>
        response match {
          case 1 => Right(scoreData)
          case _ => Left("Failed to write")
        }
    }

  implicit val getLocalDate: GetResult[LocalDate] = GetResult(r => r.nextDate.toLocalDate)

  private def findExamDateQuery(contentItemId: Int) =
    sql"""
          SELECT  ca.exam_date
          FROM content_items as ci, content_assigned_packages as cap, content_assigned as ca

          WHERE ci.package_id = cap.package_id
          AND cap.assigned_id = ca.id
          AND ci.id = $contentItemId

          LIMIT 1
         """.as[LocalDate]

  override def getExamDateByContentItemId(contentItemId: Int): Future[Option[LocalDate]] = {
    val result = findExamDateQuery(contentItemId)
    db.run(result.headOption)
  }

  override def getContentAssignedStatusByUserId(userId: Int): Future[List[ContentAssignedDto]] =
    db.run(findContentAssignedWIthStatusQuery(userId)) flatMap { r =>
      Future(r.toList)
    }

  private def findContentAssignedWIthStatusQuery(userId: Int) =
    sql"""

      SELECT  DISTINCT ca.id, ca.name, ca.exam_date, ca.active, ca.owner_id, cd.user_id IS NULL AS enabled

      FROM content_assigned AS ca

      JOIN content_assigned_cohorts AS cac
        ON cac.assigned_id = ca.id

      JOIN cohorts
        ON cohorts.id = cac.cohort_id

      JOIN cohort_members AS cm
        ON cm.cohort_id = cohorts.id

      LEFT JOIN content_disabled AS cd
      ON cd.content_assigned_id = ca.id
      AND cd.user_id = cm.user_id

      WHERE cm.user_id = $userId
      AND ca.active = TRUE

      ORDER BY ca.exam_date""".as[ContentAssignedDto]

  override def disableContentAssigned(assignedId: Int, userId: Int): Future[Int] =
    db.run((ContentDisabled += ContentDisabledDto(assignedId, userId)).asTry) map {
      case Failure(_) => 0
      case Success(_) => 1
    }

  override def enableContentAssigned(assignedId: Int, userId: Int): Future[Int] =
    db.run(
      ContentDisabled
        .filter(disabled =>
          disabled.assignedId === assignedId &&
            disabled.userId === userId)
        .delete)

  private def findHistoricalPerformanceQuery(userId: Int) = sql"""
    SELECT  ca.Name, cs.score_date AS PointName, cs.Score AS PointValue

    FROM content_scores AS cs, content_items AS ci, content_assigned_packages AS cap, content_assigned AS ca

    WHERE ca.id = cap.assigned_id
    AND cap.package_id = ci.package_id
    AND ci.id = cs.content_item_id
    AND cs.user_id = $userId

    ORDER BY ca.name, cs.score_date

    """.as[(ExamHistoryDto, ExamHistoricalPoint)]

  override def getHistoricalPerformanceByExam(userId: Int): Future[List[ExamHistoryDto]] =
    db.run(findHistoricalPerformanceQuery(userId)) flatMap { sqlResponse =>
      createAggregatedExamPerformanceDto(sqlResponse)
    }

  private def createAggregatedExamPerformanceDto(r: Vector[(ExamHistoryDto, ExamHistoricalPoint)]) = {
    val dtos = for {
      groupedByDto <- r.groupBy(_._1)
      examPointsGroupedByName = groupedByDto._2.map(_._2).groupBy(_.name)
      averagedExamPoints = examPointsGroupedByName.map(
        e => ExamHistoricalPoint(name = e._1, value = getAverageValue(e._2))
      )
      dto = groupedByDto._1.copy(series = averagedExamPoints.toList)
    } yield dto
    Future(dtos.toList)
  }

  private def getAverageValue(xs: Iterable[ExamHistoricalPoint]): Int =
    xs match {
      case Nil => 0
      case _   => xs.map(_.value).sum / xs.size
    }

  private def getStudentsWithPendingContentQuery = sql"""
  SELECT DISTINCT u.id, u.first_name, u.surname, u.email, u.is_email_verified, u.is_educator,
  u.is_administrator, u.avatar_url

  FROM users AS u

    JOIN cohort_members AS cm
    ON u.id = cm.user_id

    JOIN cohorts AS c
    ON c.id = cm.cohort_id

    LEFT JOIN content_scores AS cs
    ON u.id = cs.user_id

  WHERE cs.reprompt_date <= NOW()::date
  OR cs.reprompt_date IS NULL
  ORDER BY u.surname, u.first_name
    """.as[User]

  override def getStudentsWithPendingContent: Future[Seq[User]] =
    db.run(getStudentsWithPendingContentQuery)
}
