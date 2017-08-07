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
import javax.inject.Inject

import models.dto.{AnswerDto, ContentAssignedDto, ContentItemDto, QuestionDto}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class StudyDaoSlick  @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends StudyDao with HasDatabaseConfigProvider[JdbcProfile] {

  def findContentItemQuery(userId: Int) =
    sql"""
          SELECT  ci.Id, ci.PackageId, ci.ImageUrl, ci.Name, ci.Content,
                  q.Id, q.Question, q.Format, q.ItemId,
                  a.Id, a.QuestionId, a.Answer, a.Correct, a.Sequence

          FROM content_assigned as ca

            LEFT JOIN content_assigned_packages as cap
            ON cap.assignedId = ca.Id

            LEFT JOIN content_items AS ci
            ON ci.PackageId = cp.cap.PackageId

            LEFT JOIN content_assessment_questions AS q
            ON ci.Id = q.ItemId

            LEFT JOIN content_assessment_answers AS a
            ON q.Id = a.QuestionId

          WHERE ci.Id = $userId

          ORDER BY ci.Name, q.Question
         """.as[(ContentItemDto, Option[QuestionDto], Option[AnswerDto])]

  override def getContentItems(userId: Int): Future[List[ContentItemDto]] = {
    val result = findContentItemQuery(userId)
    val run = db.run(result)

    run.flatMap(
      r => {
        val groupedByQuestion = r.groupBy(_._2)
        val item = r.head._1
        val questions = for {
          questions <- groupedByQuestion
          questionData = questions._2
          answers = questionData.map(p => p._3.get).toList.filter(m => m.id.get > 0)
          questionsProc = questions._1.map(q => q.copy(answers = Some(answers)))
        } yield questionsProc

        val culled: Iterable[QuestionDto] = questions.filter(q => q.isDefined && q.get.id.get > 0)
          .map(q => q.get)

        if (culled == Nil) {
          Future(Some(item.copy(questions = None)))
        } else {
          Future(Some(item.copy(questions = Some(culled.toList))))
        }

      }
    )
  }
}
