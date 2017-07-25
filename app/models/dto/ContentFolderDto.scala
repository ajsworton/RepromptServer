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

case class ContentFolderDto(
  id: Option[Int],
  parentId: Option[Int],
  ownerId: Int,
  name: String,
  members: Option[List[ContentPackageDto]] = None
) extends Dto

object ContentFolderDto {

  def construct(id: Option[Int], parentId: Option[Int], ownerId: Int, name: String) =
    new ContentFolderDto(id = id, parentId = parentId, ownerId = ownerId, name = name, members = None)

  def deconstruct(dto: ContentFolderDto): Option[(Option[Int], Option[Int], Int, String)] = dto match {
    case ContentFolderDto(id: Option[Int], parentId: Option[Int], ownerId: Int, name: String,
      _: Option[List[ContentPackageDto]]
      ) => Some(id, parentId, ownerId, name)
  }

  def contentFolderForm: Form[ContentFolderDto] = Form(
    mapping(
      "id" -> optional(number),
      "parentId" -> optional(number),
      "ownerId" -> number,
      "name" -> nonEmptyText
    )(ContentFolderDto.construct)(ContentFolderDto.deconstruct)
  )

  class ContentFoldersTable(tag: Tag) extends Table[ContentFolderDto](tag, "content_folders") {

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def parentId: lifted.Rep[Option[Int]] = column[Int]("ParentId")
    def ownerId: lifted.Rep[Int] = column[Int]("OwnerId", O.PrimaryKey)
    def name: lifted.Rep[String] = column[String]("Name")
    def pk: PrimaryKey = primaryKey("PRIMARY", (id, ownerId))

    def * : ProvenShape[ContentFolderDto] = (id, parentId, ownerId, name) <>
      ((ContentFolderDto.construct _).tupled, ContentFolderDto.deconstruct)
  }

  implicit val getContentResult = GetResult(r =>
    ContentFolderDto(
      Some(r.nextInt),
      Some(r.nextInt),
      r.nextInt,
      r.nextString,
      None
    )
  )

  implicit val getOptionContentResult = GetResult(r =>
    Some(ContentFolderDto(
      Some(r.nextInt),
      Some(r.nextInt),
      r.nextInt,
      r.nextString,
      None
    ))
  )

  implicit val ContentFolderDtoFormat = Json.format[ContentFolderDto]
}

