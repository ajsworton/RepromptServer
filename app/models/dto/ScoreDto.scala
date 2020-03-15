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

import java.sql.Date
import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}
import slick.jdbc.{GetResult, JdbcType}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{PrimaryKey, ProvenShape}

case class ScoreDto(
    userId: Option[Int],
    contentItemId: Int,
    score: Int,
    scoreDate: Option[LocalDate],
    streak: Int,
    repromptDate: Option[LocalDate] = None
)

/**
  * Companion Object for to hold boiler plate for forms, json conversion, slick
  */
object ScoreDto {

  /**
    * implicit date converter
    */
  implicit val localDateToDate: JdbcType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l.toString),
    d => d.toLocalDate
  )

  def construct(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: Option[LocalDate], streak: Int, repromptDate: Option[LocalDate]) =
    new ScoreDto(userId = userId, contentItemId = contentItemId, score = score, scoreDate = scoreDate, streak = streak, repromptDate = repromptDate)

  def deconstruct(dto: ScoreDto) = dto match {
    case ScoreDto(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: Option[LocalDate], streak: Int, repromptDate: Option[LocalDate]) =>
      Some((userId, contentItemId, score, scoreDate, streak, repromptDate))
  }

  def formConstruct(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: Option[LocalDate], streak: Int, repromptDate: Option[LocalDate]) =
    new ScoreDto(userId = userId, contentItemId = contentItemId, score = score, scoreDate = scoreDate, streak = streak, repromptDate = repromptDate)

  def formDeconstruct(dto: ScoreDto): Option[(Option[Int], Int, Int, Option[LocalDate], Int, Option[LocalDate])] = dto match {
    case ScoreDto(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: Option[LocalDate], streak: Int, repromptDate: Option[LocalDate]) =>
      Some(userId, contentItemId, score, scoreDate, streak, repromptDate)
  }

  /**
    * Form definition for data type to bindFromRequest when receiving data
    * @return a form for the dat object
    */
  def form: Form[ScoreDto] = Form(
    mapping(
      "userId"        -> optional(number),
      "contentItemId" -> number,
      "score"         -> number,
      "scoreDate"     -> optional(localDate),
      "streak"        -> number,
      "repromptDate"  -> optional(localDate)
    )(ScoreDto.formConstruct)(ScoreDto.formDeconstruct)
  )

  /**
    * Table definition for database mapping via slick
    * @param tag identifies a specific row
    */
  class ScoreTable(tag: Tag) extends Table[ScoreDto](tag, "content_scores") {

    def userId: Rep[Option[Int]]             = column[Int]("user_id", O.PrimaryKey).?
    def contentItemId: Rep[Int]              = column[Int]("content_item_id", O.PrimaryKey)
    def score: Rep[Int]                      = column[Int]("score")
    def scoreDate: Rep[Option[LocalDate]]    = column[Option[LocalDate]]("score_date", O.PrimaryKey)
    def streak: Rep[Int]                     = column[Int]("streak")
    def repromptDate: Rep[Option[LocalDate]] = column[Option[LocalDate]]("reprompt_date")
    def pk: PrimaryKey                       = primaryKey("PRIMARY", (userId, contentItemId, scoreDate))

    def * : ProvenShape[ScoreDto] =
      (userId, contentItemId, score, scoreDate, streak, repromptDate) <>
        ((ScoreDto.construct _).tupled, ScoreDto.deconstruct)
  }

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getScoreResult: GetResult[ScoreDto] = GetResult(
    r =>
      ScoreDto(
        Some(r.nextInt),
        r.nextInt,
        r.nextInt,
        r.nextDate match {
          case null       => None
          case date: Date => Some(date.toLocalDate)
        },
        r.nextInt,
        r.nextDate match {
          case null       => None
          case date: Date => Some(date.toLocalDate)
        }
    ))

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getSomeScoreResult: GetResult[Option[ScoreDto]] = GetResult(
    r =>
      Some(
        ScoreDto(
          Some(r.nextInt),
          r.nextInt,
          r.nextInt,
          r.nextDate match {
            case null       => None
            case date: Date => Some(date.toLocalDate)
          },
          r.nextInt,
          r.nextDate match {
            case null       => None
            case date: Date => Some(date.toLocalDate)
          }
        )))

  /**
    * implicit json conversion formatter
    */
  implicit val serializer: OFormat[ScoreDto] = Json.format[ScoreDto]
}
