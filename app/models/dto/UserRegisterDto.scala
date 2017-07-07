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

import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json

/**
 * @param password           The user's Password
 * @param firstName          The user's first name
 * @param surName            The user's last name
 * @param email              The user's email address
 */
case class UserRegisterDto(
  password: String,
  firstName: String,
  surName: String,
  email: String
)

object UserRegisterDto {

  def registerForm: Form[UserRegisterDto] = Form(
    mapping(
      "password" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "surName" -> nonEmptyText,
      "email" -> nonEmptyText
    )(UserRegisterDto.apply)(UserRegisterDto.unapply)
  )

  implicit val userRegisterDtoFormat = Json.format[UserRegisterDto]
}

