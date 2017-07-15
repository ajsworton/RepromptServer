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

import play.api.data
import play.api.data.{Form, format}
import play.api.data.Forms._
import play.api.libs.json.{Json, Reads}
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{PrimaryKey, ProvenShape}

case class CohortDto(
  id: Option[Int],
  ownerId: Int,
  name: String
)

object CohortDto {

  def cohortForm: Form[CohortDto] = Form(
    mapping(
      "id" -> optional(number),
      "ownerId" -> number,
      "name" -> nonEmptyText
    )(CohortDto.apply)(CohortDto.unapply)
  )

  class CohortsTable(tag: Tag) extends Table[CohortDto](tag, "Cohorts") {

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def ownerId: lifted.Rep[Int] = column[Int]("OwnerId", O.PrimaryKey)
    def name: lifted.Rep[String] = column[String]("Name")
    def pk: PrimaryKey = primaryKey("PRIMARY", (id, ownerId))

    def * : ProvenShape[CohortDto] = (id, ownerId, name) <>
      ((CohortDto.apply _).tupled, CohortDto.unapply)
  }

  implicit val cohortDtoFormat = Json.format[CohortDto]
  implicit val cohortDtoSeqFormat = Json.format[List[CohortDto]]
}
