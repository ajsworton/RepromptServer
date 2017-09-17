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

import models.User
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json

/**
 * User security restricted field data object
 * @param id                 The user's Id
 * @param firstName          The user's first name
 * @param surName            The user's last name
 * @param email              The user's email address
 * @param isEmailVerified    The user's email verification status
 * @param isEducator         The user's educator group status
 * @param isAdministrator    The user's administrator status
 * @param avatarUrl          The user's avatarUrl
 */
case class UserDto(
  id: Option[Int],
  firstName: String,
  surName: String,
  name: Option[String],
  email: String,
  isEmailVerified: Boolean,
  isEducator: Boolean,
  isAdministrator: Boolean,
  avatarUrl: Option[String]
)

/**
 * Companion Object for to hold boiler plate for forms, json conversion, slick
 */
object UserDto {

  def apply(user: User): UserDto = {
    new UserDto(
      user.id,
      user.firstName,
      user.surName,
      Some(s"${user.firstName} ${user.surName}"),
      user.email,
      user.isEmailVerified,
      user.isEducator,
      user.isAdministrator,
      user.avatarUrl)
  }

  /**
   * Form definition for data type to bindFromRequest when receiving data
   * @return a form for the dat object
   */
  def form: Form[UserDto] = Form(
    mapping(
      "id" -> optional(number),
      "firstName" -> nonEmptyText,
      "surName" -> nonEmptyText,
      "name" -> optional(text),
      "email" -> nonEmptyText,
      "isEmailVerified" -> boolean,
      "isEducator" -> boolean,
      "isAdministrator" -> boolean,
      "avatarUrl" -> optional(text)
    )(UserDto.apply)(UserDto.unapply)
  )

  /**
   * implicit json conversion formatter
   */
  implicit val serializer = Json.format[UserDto]
}

