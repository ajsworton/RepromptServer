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

case class AnswerDto(
                      id: Option[Int],
                      questionId: Int,
                      answer: String,
                      correct: Boolean
                    )

object AnswerDto {

  def construct(id: Option[Int], questionId: Int, answer: String, correct: Boolean) =
    new AnswerDto(id = id, questionId = questionId, answer = answer, correct = correct)

  def deconstruct(dto: AnswerDto): Option[(Option[Int], Int, String, Boolean)] = dto match {
    case AnswerDto(id: Option[Int], questionId: Int, answer: String, correct: Boolean)
    => Some(id, questionId, answer, correct)
  }

  def ContentPackageForm: Form[AnswerDto] = Form(
    mapping(
      "id" -> optional(number),
      "question" -> number,
      "format" -> nonEmptyText,
      "itemId" -> boolean
    )(AnswerDto.construct)(AnswerDto.deconstruct)
  )

  class AnswersTable(tag: Tag) extends Table[AnswerDto](tag, "content_assessment_answers") {

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc)

    def questionId: lifted.Rep[Int] = column[Int]("QuestionId")

    def answer: lifted.Rep[String] = column[String]("Answer")

    def correct: lifted.Rep[Boolean] = column[Boolean]("Correct")

    def pk: PrimaryKey = primaryKey("PRIMARY", id)

    def * : ProvenShape[AnswerDto] = (id, questionId, answer, correct) <>
      ((AnswerDto.construct _).tupled, AnswerDto.deconstruct)
  }

  implicit val getAnswerResult = GetResult(r =>
    AnswerDto(
      Some(r.nextInt),
      r.nextInt,
      r.nextString,
      r.nextBoolean
    )
  )

  implicit val AnswerDtoFormat = Json.format[AnswerDto]
}
