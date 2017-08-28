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

case class ContentAssignedDto(
  id: Option[Int],
  name: String,
  examDate: LocalDate,
  active: Boolean,
  ownerId: Option[Int],
  enabled: Boolean = true,
  cohorts: Option[List[CohortDto]] = None,
  packages: Option[List[ContentPackageDto]] = None
) extends Dto

object ContentAssignedDto {

  implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l.toString),
    d => d.toLocalDate
  )

  def construct(id: Option[Int], name: String, examDate: LocalDate, active: Boolean, ownerId: Option[Int]) =
    new ContentAssignedDto(id = id, name = name, examDate = examDate, active = active, ownerId = ownerId)

  def deconstruct(dto: ContentAssignedDto) = dto match {
    case ContentAssignedDto(id: Option[Int], name: String, examDate: LocalDate, active: Boolean,
      ownerId: Option[Int], _, _, _) => Some(id, name, examDate, active, ownerId)
  }

  def formConstruct(id: Option[Int], name: String, examDate: LocalDate, active: Boolean, ownerId: Option[Int], enabled: Option[Boolean], cohorts: Option[List[CohortDto]], packages: Option[List[ContentPackageDto]]) =
    new ContentAssignedDto(id = id, name = name, examDate = examDate, active = active, ownerId =
      ownerId, enabled = enabled.getOrElse(true), cohorts, packages)

  def formDeconstruct(dto: ContentAssignedDto) = dto match {
    case ContentAssignedDto(id: Option[Int], name: String, examDate: LocalDate, active: Boolean,
      ownerId: Option[Int], enabled: Boolean, cohorts: Option[List[CohortDto]], packages: Option[List[ContentPackageDto]]) => Some(id, name, examDate, active, ownerId, Some(enabled), cohorts, packages)
  }

  def form: Form[ContentAssignedDto] = Form(
    mapping(
      "id" -> optional(number),
      "name" -> nonEmptyText,
      "examDate" -> localDate,
      "active" -> boolean,
      "ownerId" -> optional(number),
      "enabled" -> optional(boolean),
      "cohorts" -> optional(list(mapping(
        "id" -> optional(number),
        "parentId" -> optional(number),
        "ownerId" -> number,
        "name" -> nonEmptyText
      )(CohortDto.construct)(CohortDto.deconstruct))),
      "packages" -> optional(list(mapping(
        "id" -> optional(number),
        "folderId" -> number,
        "ownerId" -> number,
        "name" -> nonEmptyText
      )(ContentPackageDto.construct)(ContentPackageDto.deconstruct)))
    )(ContentAssignedDto.formConstruct)(ContentAssignedDto.formDeconstruct)
  )

  class ContentAssignedTable(tag: Tag) extends Table[ContentAssignedDto](tag, "content_assigned") {

    def id: lifted.Rep[Option[Int]] = column[Option[Int]]("Id", O.PrimaryKey, O.AutoInc)
    def name: lifted.Rep[String] = column[String]("Name")
    def examDate: lifted.Rep[LocalDate] = column[LocalDate]("ExamDate")
    def active: lifted.Rep[Boolean] = column[Boolean]("Active")
    def ownerId: lifted.Rep[Option[Int]] = column[Option[Int]]("OwnerId")
    def pk: PrimaryKey = primaryKey("PRIMARY", id)

    def * : ProvenShape[ContentAssignedDto] = (id, name, examDate, active, ownerId) <>
      ((ContentAssignedDto.construct _).tupled, ContentAssignedDto.deconstruct)
  }

  implicit val getCAResult = GetResult(r =>
    ContentAssignedDto(
      Some(r.nextInt),
      r.nextString(),
      r.nextDate.toLocalDate,
      r.nextBoolean(),
      Some(r.nextInt),
      r.nextBoolean()
    )
  )

  implicit val getSomeCAResult = GetResult(r =>
    Some(ContentAssignedDto(
      Some(r.nextInt),
      r.nextString(),
      r.nextDate.toLocalDate,
      r.nextBoolean(),
      Some(r.nextInt),
      r.nextBoolean()
    ))
  )

  implicit val serializer = Json.format[ContentAssignedDto]
}