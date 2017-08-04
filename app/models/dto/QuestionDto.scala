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

package models.dto

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }

case class QuestionDto(
  id: Option[Int],
  question: String,
  format: String,
  itemId: Int,
  answers: Option[List[AnswerDto]] = None
) extends Dto

object QuestionDto {

  def construct(id: Option[Int], question: String, format: String, itemId: Int) =
    new QuestionDto(id = id, question = question, format = format, itemId = itemId, answers = None)

  def deconstruct(dto: QuestionDto): Option[(Option[Int], String, String, Int)] = dto match {
    case QuestionDto(id: Option[Int], question: String, format: String, itemId: Int,
      _: Option[List[AnswerDto]]
      ) => Some(id, question, format, itemId)
  }

  def QuestionForm: Form[QuestionDto] = Form(
    mapping(
      "id" -> optional(number),
      "question" -> nonEmptyText,
      "format" -> nonEmptyText,
      "itemId" -> number,
      "answers" -> optional(list(mapping(
        "id" -> optional(number),
        "questionId" -> optional(number),
        "answer" -> nonEmptyText,
        "correct" -> boolean,
        "sequence" -> number
      )(AnswerDto.construct)(AnswerDto.deconstruct)))
    )(QuestionDto.apply)(QuestionDto.unapply)
  )

  class QuestionsTable(tag: Tag) extends Table[QuestionDto](tag, "content_assessment_questions") {

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def question: lifted.Rep[String] = column[String]("Question")
    def format: lifted.Rep[String] = column[String]("Format")
    def itemId: lifted.Rep[Int] = column[Int]("ItemId")
    def pk: PrimaryKey = primaryKey("PRIMARY", id)

    def * : ProvenShape[QuestionDto] = (id, question, format, itemId) <>
      ((QuestionDto.construct _).tupled, QuestionDto.deconstruct)
  }

  implicit val getQuestionResult = GetResult(r =>
    QuestionDto(
      Some(r.nextInt),
      r.nextString,
      r.nextString,
      r.nextInt,
      None
    )
  )

  implicit val getSomeQuestionResult = GetResult(r =>
    Some(QuestionDto(
      Some(r.nextInt),
      r.nextString,
      r.nextString,
      r.nextInt,
      None
    ))
  )

  implicit val QuestionDtoFormat = Json.format[QuestionDto]
}