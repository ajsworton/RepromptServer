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

/**
 * User registration data object
 * @param password           The user's Password
 * @param firstName          The user's first name
 * @param surName            The user's last name
 * @param email              The user's email address
 */
case class UserRegisterDto(
  password: String,
  firstName: String,
  surName: String,
  email: String,
  isEducator: Boolean
)

/**
 * Companion Object for to hold boiler plate for forms, json conversion, slick
 */
object UserRegisterDto {

  /**
   * Form definition for data type to bindFromRequest when receiving data
   * @return a form for the dat object
   */
  def form: Form[UserRegisterDto] = Form(
    mapping(
      "password" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "surName" -> nonEmptyText,
      "email" -> nonEmptyText,
      "isEducator" -> boolean
    )(UserRegisterDto.apply)(UserRegisterDto.unapply)
  )

  /**
   * implicit json conversion formatter
   */
  implicit val serializer = Json.format[UserRegisterDto]
}

