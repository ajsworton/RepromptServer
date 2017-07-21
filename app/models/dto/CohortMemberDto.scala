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
import play.api.data.Forms.{ mapping, number, optional }
import play.api.libs.json.Json
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }
import slick.model.ForeignKeyAction

case class CohortMemberDto(CohortId: Option[Int], UserId: Option[Int])

object CohortMemberDto {
  class CohortsMembersTable(tag: Tag) extends Table[CohortMemberDto](tag, "cohort_members") {
    def cohortId: lifted.Rep[Option[Int]] = column[Int]("CohortId")
    def userId: lifted.Rep[Option[Int]] = column[Int]("UserId")

    def * : ProvenShape[CohortMemberDto] =
      (cohortId, userId) <> ((CohortMemberDto.apply _).tupled, CohortMemberDto.unapply)

    def pk: PrimaryKey = primaryKey("PRIMARY", (cohortId, userId))

    def cohortsFK = foreignKey("CohortId", cohortId, TableQuery[CohortsTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade)

    def usersFK = foreignKey("UserId", userId, TableQuery[UsersTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade)

  }

  def cohortMemberForm: Form[CohortMemberDto] = Form(
    mapping(
      "cohortId" -> optional(number),
      "userId" -> optional(number)
    )(CohortMemberDto.apply _)(CohortMemberDto.unapply)
  )
  implicit val cohortMemberDtoFormat = Json.format[CohortMemberDto]
}
