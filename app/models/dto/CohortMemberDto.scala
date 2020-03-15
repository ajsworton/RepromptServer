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

import models.User.UsersTable
import models.dto.CohortDto.CohortsTable
import play.api.data.Form
import play.api.data.Forms.{mapping, number, optional}
import play.api.libs.json.{Json, OFormat}
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._
import slick.lifted
import slick.lifted.{PrimaryKey, ProvenShape}
import slick.model.ForeignKeyAction

case class CohortMemberDto(cohortId: Option[Int], userId: Option[Int])

/**
  * Companion Object for to hold boiler plate for forms, json conversion, slick
  */
object CohortMemberDto {

  /**
    * Table definition for database mapping via slick
    * @param tag identifies a specific row
    */
  class CohortsMembersTable(tag: Tag) extends Table[CohortMemberDto](tag, "cohort_members") {
    def cohortId: lifted.Rep[Option[Int]] = column[Int]("cohort_id")
    def userId: lifted.Rep[Option[Int]]   = column[Int]("user_id")

    def * : ProvenShape[CohortMemberDto] =
      (cohortId, userId) <> ((CohortMemberDto.apply _).tupled, CohortMemberDto.unapply)

    def pk: PrimaryKey = primaryKey("PRIMARY", (cohortId, userId))

    def cohortsFK = foreignKey("cohort_id", cohortId, TableQuery[CohortsTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade
    )

    def usersFK = foreignKey("user_id", userId, TableQuery[UsersTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade
    )

  }

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getResult: GetResult[CohortMemberDto] = GetResult(r => CohortMemberDto(Some(r.nextInt), Some(r.nextInt)))

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getSomeResult: GetResult[Product with Serializable] = GetResult(r =>
    (r.nextInt, r.nextInt) match {
      case (0, 0)                       => None
      case (cohortId: Int, userId: Int) => CohortMemberDto(Some(cohortId), Some(userId))
  })

  /**
    * Form definition for data type to bindFromRequest when receiving data
    * @return a form for the dat object
    */
  def form: Form[CohortMemberDto] = Form(
    mapping(
      "cohortId" -> optional(number),
      "userId"   -> optional(number)
    )(CohortMemberDto.apply _)(CohortMemberDto.unapply)
  )

  /**
    * implicit json conversion formatter
    */
  implicit val serializer: OFormat[CohortMemberDto] = Json.format[CohortMemberDto]
}
