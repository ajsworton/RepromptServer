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

import java.sql.{ Date, Timestamp, Types }
import java.time.{ LocalDate, LocalDateTime, ZoneId }
import java.util.UUID

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{ Json, OFormat }
import slick.jdbc.{ GetResult, PositionedParameters, SetParameter }
import slick.jdbc.MySQLProfile.api._
import slick.{ driver, lifted }
import slick.lifted.{ PrimaryKey, ProvenShape }

case class ScoreDto(
  userId: Option[Int],
  contentItemId: Int,
  score: Int,
  scoreDate: Option[LocalDate],
  streak: Int,
  repromptDate: Option[LocalDate] = None
)

object ScoreDto {

  implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l.toString),
    d => d.toLocalDate
  )

  def construct(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: LocalDate,
    streak: Int, repromptDate: LocalDate) =
    new ScoreDto(userId = userId, contentItemId = contentItemId, score = score,
      scoreDate = Some(scoreDate), streak = streak, repromptDate = Some(repromptDate))

  def deconstruct(dto: ScoreDto) = dto match {
    case ScoreDto(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: Option[LocalDate],
      streak: Int, repromptDate: Option[LocalDate]) =>
      Some((userId, contentItemId, score, scoreDate.orNull, streak, repromptDate.orNull))
  }

  def formConstruct(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: Option[LocalDate],
    streak: Int, repromptDate: Option[LocalDate]) =
    new ScoreDto(userId = userId, contentItemId = contentItemId, score = score,
      scoreDate = scoreDate, streak = streak, repromptDate = repromptDate)

  def formDeconstruct(dto: ScoreDto): Option[(Option[Int], Int, Int, Option[LocalDate], Int, Option[LocalDate])] = dto match {
    case ScoreDto(userId: Option[Int], contentItemId: Int, score: Int, scoreDate: Option[LocalDate],
      streak: Int, repromptDate: Option[LocalDate]) => Some(userId, contentItemId, score,
      scoreDate, streak, repromptDate)
  }

  def form: Form[ScoreDto] = Form(
    mapping(
      "userId" -> optional(number),
      "contentItemId" -> number,
      "score" -> number,
      "scoreDate" -> optional(localDate),
      "streak" -> number,
      "repromptDate" -> optional(localDate)
    )(ScoreDto.formConstruct)(ScoreDto.formDeconstruct)
  )

  class ScoreTable(tag: Tag) extends Table[ScoreDto](tag, "content_scores") {

    def userId: lifted.Rep[Option[Int]] = column[Int]("UserId", O.PrimaryKey).?
    def contentItemId: lifted.Rep[Int] = column[Int]("ContentItemId", O.PrimaryKey)
    def score: lifted.Rep[Int] = column[Int]("Score")
    def scoreDate: lifted.Rep[LocalDate] = column[LocalDate]("ScoreDate", O.PrimaryKey)
    def streak: lifted.Rep[Int] = column[Int]("Streak")
    def repromptDate: lifted.Rep[LocalDate] = column[LocalDate]("RepromptDate")
    def pk: PrimaryKey = primaryKey("PRIMARY", (userId, contentItemId, scoreDate))

    def * : ProvenShape[ScoreDto] = (userId, contentItemId, score, scoreDate, streak, repromptDate) <>
      ((ScoreDto.construct _).tupled, ScoreDto.deconstruct)
  }

  implicit val getScoreResult: GetResult[ScoreDto] = GetResult(r =>
    ScoreDto(
      Some(r.nextInt),
      r.nextInt,
      r.nextInt,
      r.nextDate match {
        case null => None
        case date: Date => Some(date.toLocalDate)
      },
      r.nextInt,
      r.nextDate match {
        case null => None
        case date: Date => Some(date.toLocalDate)
      }
    )
  )

  implicit val getSomeScoreResult: GetResult[Some[ScoreDto]] = GetResult(r =>
    Some(ScoreDto(
      Some(r.nextInt),
      r.nextInt,
      r.nextInt,
      r.nextDate match {
        case null => None
        case date: Date => Some(date.toLocalDate)
      },
      r.nextInt,
      r.nextDate match {
        case null => None
        case date: Date => Some(date.toLocalDate)
      }
    ))
  )

  implicit val serializer: OFormat[ScoreDto] = Json.format[ScoreDto]
}