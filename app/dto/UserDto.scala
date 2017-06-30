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

package dto

import models.User
import play.api.libs.json.{Format, Json, __}

/**
  * @author Alexander Worton.
  */
case class UserDto(
                    id: Int,
                    userName: String,
                    firstName: String,
                    surName: String,
                    email: String,
                    isEmailVerified: Boolean,
                    isEducator: Boolean,
                    isAdministrator: Boolean
                  )


object UserDto {

  def apply(user: User): UserDto = {
    new UserDto(
      user.id,
      user.userName,
      user.firstName,
      user.surName,
      user.email,
      user.isEmailVerified,
      user.isEducator,
      user.isAdministrator)
  }

  implicit val userDtoFormat = Json.format[UserDto]
}


