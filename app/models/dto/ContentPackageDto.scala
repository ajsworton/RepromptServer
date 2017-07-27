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

case class ContentPackageDto(
  id: Option[Int],
  folderId: Int,
  ownerId: Int,
  name: String,
  content: Option[List[ContentItemDto]] = None
) extends Dto

object ContentPackageDto {

  def construct(id: Option[Int], folderId: Int, ownerId: Int, name: String) =
    new ContentPackageDto(id = id, folderId = folderId, ownerId = ownerId, name = name, content = None)

  def deconstruct(dto: ContentPackageDto): Option[(Option[Int], Int, Int, String)] = dto match {
    case ContentPackageDto(id: Option[Int], folderId: Int, ownerId: Int, name: String,
      _: Option[List[ContentItemDto]]
      ) => Some(id, folderId, ownerId, name)
  }

  def ContentPackageForm: Form[ContentPackageDto] = Form(
    mapping(
      "id" -> optional(number),
      "folderId" -> number,
      "ownerId" -> number,
      "name" -> nonEmptyText
    )(ContentPackageDto.construct)(ContentPackageDto.deconstruct)
  )

  class PackageTable(tag: Tag) extends Table[ContentPackageDto](tag, "content_packages") {

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def folderId: lifted.Rep[Int] = column[Int]("FolderId")
    def ownerId: lifted.Rep[Int] = column[Int]("OwnerId")
    def name: lifted.Rep[String] = column[String]("Name")
    def pk: PrimaryKey = primaryKey("PRIMARY", (id, ownerId))

    def * : ProvenShape[ContentPackageDto] = (id, folderId, ownerId, name) <>
      ((ContentPackageDto.construct _).tupled, ContentPackageDto.deconstruct)
  }

  implicit val getContentResult = GetResult(r =>
    ContentPackageDto(
      Some(r.nextInt),
      r.nextInt,
      r.nextInt,
      r.nextString,
      None
    )
  )

  implicit val getOptionContentResult = GetResult(r =>
    Some(ContentPackageDto(
      Some(r.nextInt),
      r.nextInt,
      r.nextInt,
      r.nextString,
      None
    ))
  )

  implicit val serializer = Json.format[ContentPackageDto]
}

