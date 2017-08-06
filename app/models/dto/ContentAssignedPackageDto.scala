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

import models.dto.ContentAssignedDto.ContentAssignedTable
import models.dto.ContentPackageDto.PackageTable
import play.api.data.Form
import play.api.data.Forms.{ mapping, number, optional }
import play.api.libs.json.Json
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }
import slick.model.ForeignKeyAction

case class ContentAssignedPackageDto(assignedId: Option[Int], packageId: Option[Int])

object ContentAssignedPackageDto {

  class ContentAssignedPackageTable(tag: Tag) extends Table[ContentAssignedPackageDto](
    tag, "content_assigned_packages") {
    def assignedId: lifted.Rep[Option[Int]] = column[Int]("AssignedId")
    def packageId: lifted.Rep[Option[Int]] = column[Int]("PackageId")

    def * : ProvenShape[ContentAssignedPackageDto] =
      (assignedId, packageId) <> ((ContentAssignedPackageDto.apply _).tupled, ContentAssignedPackageDto.unapply)

    def pk: PrimaryKey = primaryKey("PRIMARY", (assignedId, packageId))

    def assignedFK = foreignKey("AssignedId", assignedId, TableQuery[ContentAssignedTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade)

    def packagesFK = foreignKey("PackageId", packageId, TableQuery[PackageTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade)

  }

  def cohortMemberForm: Form[ContentAssignedPackageDto] = Form(
    mapping(
      "assignedId" -> optional(number),
      "packageId" -> optional(number)
    )(ContentAssignedPackageDto.apply _)(ContentAssignedPackageDto.unapply)
  )
  implicit val serializer = Json.format[ContentAssignedPackageDto]
}

