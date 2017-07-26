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

import java.sql.{ Date, Timestamp }
import java.time.{ LocalDate, LocalDateTime }

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }

case class ScoreDto(
                    userId: Int,
                    contentId: Int,
                    lastScore: Int,
                    repromptDate: LocalDate,
                    streak: Int
                   )

object ScoreDto {


  implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  implicit val localDateTimeToDateTime = MappedColumnType.base[LocalDateTime, Timestamp](
    l => Timestamp.valueOf(l),
    d => d.toLocalDateTime
  )

  def construct(userId: Int, contentId: Int, lastScore: Int, repromptDate: LocalDate, streak: Int) =
    new ScoreDto(userId = userId, contentId = contentId, lastScore = lastScore,
      repromptDate = repromptDate, streak = streak)

  def deconstruct(dto: ScoreDto): Option[(Int, Int, Int, LocalDate, Int)] = dto match {
    case ScoreDto(userId: Int, contentId: Int, lastScore: Int, repromptDate: LocalDate,
      streak: Int) => Some(userId, contentId, lastScore, repromptDate, streak)
  }

  def ScoreForm: Form[ScoreDto] = Form(
    mapping(
      "userId" -> number,
      "contentId" -> number,
      "lastScore" -> number,
      "repromptDate" -> localDate,
      "streak" -> number
    )(ScoreDto.construct)(ScoreDto.deconstruct)
  )

  class ScoreTable(tag: Tag) extends Table[ScoreDto](tag, "content_scores") {

    def userId: lifted.Rep[Int] = column[Int]("UserId", O.PrimaryKey)
    def contentId: lifted.Rep[Int] = column[Int]("ContentId", O.PrimaryKey)
    def lastScore: lifted.Rep[Int] = column[Int]("LastScore")
    def repromptDate: lifted.Rep[LocalDate] = column[LocalDate]("RepromptDate")
    def streak: lifted.Rep[Int] = column[Int]("Streak")
    def pk: PrimaryKey = primaryKey("PRIMARY", (userId, contentId))

    def * : ProvenShape[ScoreDto] = (userId, contentId, lastScore, repromptDate, streak) <>
      ((ScoreDto.construct _).tupled, ScoreDto.deconstruct)
  }

  implicit val getScoreResult = GetResult(r =>
    ScoreDto(
      r.nextInt,
      r.nextInt,
      r.nextInt,
      r.nextDate.toLocalDate,
      r.nextInt
    )
  )

  implicit val serializer = Json.format[ScoreDto]
}