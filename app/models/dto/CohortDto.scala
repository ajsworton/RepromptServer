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
import play.api.libs.json.{ Json, OFormat }
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }

case class CohortDto(
  id: Option[Int],
  parentId: Option[Int],
  ownerId: Int,
  name: String,
  members: Option[List[UserDto]] = None
) extends Dto

object CohortDto {

  def construct(id: Option[Int], parentId: Option[Int], ownerId: Int, name: String) =
    new CohortDto(id = id, parentId = parentId, ownerId = ownerId, name = name, members = None)

  def deconstruct(dto: CohortDto): Option[(Option[Int], Option[Int], Int, String)] = dto match {
    case CohortDto(id: Option[Int], parentId: Option[Int], ownerId: Int, name: String,
      _: Option[List[UserDto]]
      ) => Some(id, parentId, ownerId, name)
  }

  def cohortForm: Form[CohortDto] = Form(
    mapping(
      "id" -> optional(number),
      "parentId" -> optional(number),
      "ownerId" -> number,
      "name" -> nonEmptyText
    )(CohortDto.construct)(CohortDto.deconstruct)
  )

  class CohortsTable(tag: Tag) extends Table[CohortDto](tag, "cohorts") {

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def parentId: lifted.Rep[Option[Int]] = column[Int]("ParentId")
    def ownerId: lifted.Rep[Int] = column[Int]("OwnerId", O.PrimaryKey)
    def name: lifted.Rep[String] = column[String]("Name")
    def pk: PrimaryKey = primaryKey("PRIMARY", (id, ownerId))

    def * : ProvenShape[CohortDto] = (id, parentId, ownerId, name) <>
      ((CohortDto.construct _).tupled, CohortDto.deconstruct)
  }

  implicit val getCohortResult = GetResult(r =>
    CohortDto(
      Some(r.nextInt),
      Some(r.nextInt),
      r.nextInt,
      r.nextString,
      None
    )
  )

  implicit val getSomeCohortResult = GetResult(r =>
    Some(CohortDto(
      Some(r.nextInt),
      Some(r.nextInt),
      r.nextInt,
      r.nextString,
      None
    ))
  )

  implicit val serializer = Json.format[CohortDto]
}
