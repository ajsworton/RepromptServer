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

import models.dto.{ AnswerDto, ContentItemDto, ContentPackageDto, QuestionDto }
import models.dto.ContentItemDto.ContentItemsTable
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

class ContentItemDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends ContentItemDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val ContentItems = TableQuery[ContentItemsTable]

  def findContentItemQuery(itemId: Int) =
    sql"""
          SELECT  ci.Id, ci.PackageId, ci.ImageUrl, ci.Name, ci.Content,
                  q.Id, q.Question, q.Format, q.ItemId,
                  a.id, a.questionId, a.answer, a.correct

          FROM content_items AS ci

            LEFT JOIN content_assessment_questions AS q
            ON ci.Id = q.ItemId

            LEFT JOIN content_assessment_answers AS a
            ON q.Id = a.QuestionId

          WHERE cp.Id = $itemId
         """.as[(ContentItemDto, Option[QuestionDto], Option[AnswerDto])]

  override def find(itemId: Int): Future[Option[ContentItemDto]] = {
    val result = findContentItemQuery(itemId)
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
        } yield questionsProc.get

        Future(Some(item.copy(questions = Some(questions.toList))))
      }
    )
  }

  override def save(itemDto: ContentItemDto): Future[Option[ContentItemDto]] = {
    db.run((ContentItems returning ContentItems.map(_.id)
      into ((item, returnedId) => Some(item.copy(id = returnedId)))
    ) += itemDto)
  }

  override def update(itemDto: ContentItemDto): Future[Option[ContentItemDto]] = {
    if (itemDto.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(ContentItems.filter(_.id === itemDto.id).map(
          c => (c.name, c.content, c.imageUrl, c.packageId)
        ).update(itemDto.name, itemDto.content, itemDto.imageUrl, itemDto.packageId))
        read <- find(itemDto.id.get)
      } yield read
    }
  }

  override def delete(itemId: Int): Future[Int] = {
    db.run(ContentItems.filter(_.id === itemId).delete)
  }
}
