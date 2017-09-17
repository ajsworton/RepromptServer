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

import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }

case class ContentDisabledDto(
                               assignedId: Int,
                               UserId: Int
                             )

/**
  * Companion Object for to hold boiler plate for forms, json conversion, slick
  */
object ContentDisabledDto {

  /**
    * Table definition for database mapping via slick
    * @param tag identifies a specific row
    */
  class ContentDisabledTable(tag: Tag) extends Table[ContentDisabledDto](tag, "content_disabled") {

    def assignedId: lifted.Rep[Int] = column[Int]("ContentAssignedId", O.PrimaryKey)
    def userId: lifted.Rep[Int] = column[Int]("UserId", O.PrimaryKey)
    def pk: PrimaryKey = primaryKey("PRIMARY", (assignedId, userId))

    def * : ProvenShape[ContentDisabledDto] = (assignedId, userId) <>
      ((ContentDisabledDto.apply _).tupled, ContentDisabledDto.unapply)
  }

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getContentDisabledResult = GetResult(r =>
    ContentDisabledDto(
      r.nextInt,
      r.nextInt,
    )
  )

  /**
    * implicit converter to coerce direct sql query into data object
    */
  implicit val getOptionContentDisabledResult = GetResult(r =>
    Some(ContentDisabledDto(
      r.nextInt,
      r.nextInt,
    ))
  )
}