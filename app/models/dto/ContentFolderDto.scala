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
import play.api.libs.json.{Json, OFormat}
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._
import slick.lifted
import slick.lifted.{PrimaryKey, ProvenShape}

/**
  * ContentFolder data object
  * @param id database value
  * @param parentId database value
  * @param ownerId database value
  * @param name database value
  * @param members database value
  */
case class ContentFolderDto(
    id: Option[Int],
    parentId: Option[Int],
    ownerId: Int,
    name: String,
    members: Option[List[ContentPackageDto]] = None
) extends Dto

/**
  * Companion Object for to hold boiler plate for forms, json conversion, slick
  */
object ContentFolderDto {

  def construct(id: Option[Int], parentId: Option[Int], ownerId: Int, name: String) =
    new ContentFolderDto(id = id, parentId = parentId, ownerId = ownerId, name = name, members = None)

  def deconstruct(dto: ContentFolderDto): Option[(Option[Int], Option[Int], Int, String)] = dto match {
    case ContentFolderDto(id: Option[Int], parentId: Option[Int], ownerId: Int, name: String, _: Option[List[ContentPackageDto]]) =>
      Some(id, parentId, ownerId, name)
  }

  /**
    * Form definition for data type to bindFromRequest when receiving data
    * @return a form for the dat object
    */
  def form: Form[ContentFolderDto] = Form(
    mapping(
      "id"       -> optional(number),
      "parentId" -> optional(number),
      "ownerId"  -> number,
      "name"     -> nonEmptyText
    )(ContentFolderDto.construct)(ContentFolderDto.deconstruct)
  )

  /**
    * Table definition for database mapping via slick
    * @param tag identifies a specific row
    */
  class ContentFoldersTable(tag: Tag) extends Table[ContentFolderDto](tag, "content_folders") {

    def id: lifted.Rep[Option[Int]]       = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def parentId: lifted.Rep[Option[Int]] = column[Int]("parent_id")
    def ownerId: lifted.Rep[Int]          = column[Int]("owner_id", O.PrimaryKey)
    def name: lifted.Rep[String]          = column[String]("name")
    def pk: PrimaryKey                    = primaryKey("PRIMARY", (id, ownerId))

    def * : ProvenShape[ContentFolderDto] =
      (id, parentId, ownerId, name) <>
        ((ContentFolderDto.construct _).tupled, ContentFolderDto.deconstruct)
  }

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getContentResult: GetResult[ContentFolderDto] = GetResult(
    r =>
      ContentFolderDto(
        Some(r.nextInt),
        Some(r.nextInt),
        r.nextInt,
        r.nextString,
        None
    ))

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getOptionContentResult: GetResult[Some[ContentFolderDto]] = GetResult(
    r =>
      Some(
        ContentFolderDto(
          Some(r.nextInt),
          Some(r.nextInt),
          r.nextInt,
          r.nextString,
          None
        )))

  /**
    * implicit json conversion formatter
    */
  implicit val serializer: OFormat[ContentFolderDto] = Json.format[ContentFolderDto]
}
