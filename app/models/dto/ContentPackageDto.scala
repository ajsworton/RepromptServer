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
  * ContentPackage data object
  * @param id database value
  * @param folderId database value
  * @param ownerId database value
  * @param name database value
  * @param content database value
  */
case class ContentPackageDto(
    id: Option[Int],
    folderId: Int,
    ownerId: Int,
    name: String,
    content: Option[List[ContentItemDto]] = None
) extends Dto

/**
  * Companion Object for to hold boiler plate for forms, json conversion, slick
  */
object ContentPackageDto {

  def construct(id: Option[Int], folderId: Int, ownerId: Int, name: String) =
    new ContentPackageDto(id = id, folderId = folderId, ownerId = ownerId, name = name, content = None)

  def deconstruct(dto: ContentPackageDto): Option[(Option[Int], Int, Int, String)] = dto match {
    case ContentPackageDto(id: Option[Int], folderId: Int, ownerId: Int, name: String, _: Option[List[ContentItemDto]]) =>
      Some(id, folderId, ownerId, name)
  }

  /**
    * Form definition for data type to bindFromRequest when receiving data
    * @return a form for the dat object
    */
  def form: Form[ContentPackageDto] = Form(
    mapping(
      "id"       -> optional(number),
      "folderId" -> number,
      "ownerId"  -> number,
      "name"     -> nonEmptyText
    )(ContentPackageDto.construct)(ContentPackageDto.deconstruct)
  )

  /**
    * Table definition for database mapping via slick
    * @param tag identifies a specific row
    */
  class PackageTable(tag: Tag) extends Table[ContentPackageDto](tag, "content_packages") {

    def id: lifted.Rep[Option[Int]] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def folderId: lifted.Rep[Int]   = column[Int]("folder_id")
    def ownerId: lifted.Rep[Int]    = column[Int]("owner_id")
    def name: lifted.Rep[String]    = column[String]("name")
    def pk: PrimaryKey              = primaryKey("PRIMARY", (id, ownerId))

    def * : ProvenShape[ContentPackageDto] =
      (id, folderId, ownerId, name) <>
        ((ContentPackageDto.construct _).tupled, ContentPackageDto.deconstruct)
  }

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getContentResult: GetResult[ContentPackageDto] = GetResult(
    r =>
      ContentPackageDto(
        Some(r.nextInt),
        r.nextInt,
        r.nextInt,
        r.nextString,
        None
    ))

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getOptionContentResult: GetResult[Some[ContentPackageDto]] = GetResult(
    r =>
      Some(
        ContentPackageDto(
          Some(r.nextInt),
          r.nextInt,
          r.nextInt,
          r.nextString,
          None
        )))

  /**
    * implicit json conversion formatter
    */
  implicit val serializer: OFormat[ContentPackageDto] = Json.format[ContentPackageDto]
}
