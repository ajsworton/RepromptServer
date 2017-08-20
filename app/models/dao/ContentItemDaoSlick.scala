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

import models.dto.AnswerDto.AnswersTable
import models.dto.{ AnswerDto, ContentItemDto, QuestionDto }
import models.dto.ContentItemDto.ContentItemsTable
import models.dto.QuestionDto.QuestionsTable
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

class ContentItemDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends ContentItemDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val ContentItems = TableQuery[ContentItemsTable]
  private val Questions = TableQuery[QuestionsTable]
  private val Answers = TableQuery[AnswersTable]

  def findContentItemQuery(itemId: Int) =
    sql"""
          SELECT  ci.Id, ci.PackageId, ci.ImageUrl, ci.Name, ci.Content, 1,
                  q.Id, q.Question, q.Format, q.ItemId,
                  a.Id, a.QuestionId, a.Answer, a.Correct, a.Sequence

          FROM content_items AS ci

            LEFT JOIN content_assessment_questions AS q
            ON ci.Id = q.ItemId

            LEFT JOIN content_assessment_answers AS a
            ON q.Id = a.QuestionId

          WHERE ci.Id = $itemId

          ORDER BY ci.Name, q.Question
         """.as[(ContentItemDto, Option[QuestionDto], Option[AnswerDto])]

  def findQuestionQuery(questionId: Int) =
    sql"""
          SELECT  q.Id, q.Question, q.Format, q.ItemId,
                  a.Id, a.QuestionId, a.Answer, a.Correct, a.Sequence

          FROM content_assessment_questions AS q

            LEFT JOIN content_assessment_answers AS a
            ON q.Id = a.QuestionId

          WHERE q.Id = $questionId

          ORDER BY q.Question
         """.as[(QuestionDto, Option[AnswerDto])]

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

  override def findQuestion(questionId: Int): Future[Option[QuestionDto]] = {
    val result = findQuestionQuery(questionId)
    val run = db.run(result)

    run.flatMap(
      r => {
        if (r.nonEmpty) {
          val ans = r.map(p => p._2.get).toList.filter(m => m.id.get > 0)
          Future(Some(r.head._1.copy(answers = Some(ans))))
        } else {
          Future(None)
        }
      })
  }

  override def findAnswer(answerId: Int): Future[Option[AnswerDto]] = {
    db.run(Answers.filter(_.id === answerId).result.headOption)
  }

  override def save(itemDto: ContentItemDto): Future[Option[ContentItemDto]] = {
    db.run((ContentItems returning ContentItems.map(_.id)
      into ((item, returnedId) => Some(item.copy(id = returnedId)))
    ) += itemDto)
  }

  override def saveQuestion(questionDto: QuestionDto): Future[Option[QuestionDto]] = {
    db.run((Questions returning Questions.map(_.id)
      into ((question, returnedId) => Some(question.copy(id = returnedId)))
    ) += questionDto)
  }

  override def saveAnswer(answerDto: AnswerDto): Future[Option[AnswerDto]] = {
    db.run((Answers returning Answers.map(_.id)
      into ((answer, returnedId) => Some(answer.copy(id = returnedId)))
    ) += answerDto)
  }

  override def updateQuestion(questionDto: QuestionDto): Future[Option[QuestionDto]] = {
    if (questionDto.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(Questions.filter(_.id === questionDto.id).map(
          q => (q.question, q.format, q.itemId)
        ).update(questionDto.question, questionDto.format, questionDto.itemId))
        read <- findQuestion(questionDto.id.get)
      } yield read
    }
  }

  override def updateAnswer(answerDto: AnswerDto): Future[Option[AnswerDto]] = {
    if (answerDto.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(Answers.filter(_.id === answerDto.id).map(
          a => (a.questionId, a.answer, a.correct, a.sequence)
        ).update(answerDto.questionId, answerDto.answer, answerDto.correct, answerDto.sequence))
        read <- findAnswer(answerDto.id.get)
      } yield read
    }
  }

  override def deleteQuestion(questionId: Int): Future[Int] = {
    db.run(Questions.filter(_.id === questionId).delete)
  }

  override def deleteAnswer(answerId: Int): Future[Int] = {
    db.run(Answers.filter(_.id === answerId).delete)
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
