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

package models

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import models.dto.Dto
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.ProvenShape

case class User(
  id: Option[Int] = None,
  firstName: String,
  surName: String,
  email: String,
  isEmailVerified: Boolean = false,
  isEducator: Boolean = false,
  isAdministrator: Boolean = false,
  avatarUrl: Option[String] = None,
  profiles: List[Profile] = Nil
) extends Identity with Dto {
  def profileFor(loginInfo: LoginInfo): Option[Profile] = profiles.find(_.loginInfo == loginInfo)
}

object User {

  def apply(profile: Profile) = {
    new User(
      profiles = List(profile),
      firstName = profile.firstName.getOrElse(""),
      surName = profile.lastName.getOrElse(""),
      email = profile.email.getOrElse(""),
      avatarUrl = if (profile.avatarUrl.isDefined) profile.avatarUrl else None
    )
  }

  implicit val getResult = GetResult(r =>
    User(
      Some(r.nextInt),
      r.nextString,
      r.nextString,
      r.nextString,
      r.nextBoolean,
      r.nextBoolean,
      r.nextBoolean,
      Some(r.nextString),
      Nil
    )
  )

  implicit val getOptionResult = GetResult(r =>
    Some(User(
      Some(r.nextInt),
      r.nextString,
      r.nextString,
      r.nextString,
      r.nextBoolean,
      r.nextBoolean,
      r.nextBoolean,
      Some(r.nextString),
      Nil
    ))
  )

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
      l => Date.valueOf(l),
      d => d.toLocalDate
    )

    implicit val localDateTimeToDateTime = MappedColumnType.base[LocalDateTime, Timestamp](
      l => Timestamp.valueOf(l),
      d => d.toLocalDateTime
    )

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def userName: lifted.Rep[String] = column[String]("UserName")
    def firstName: lifted.Rep[String] = column[String]("FirstName")
    def surName: lifted.Rep[String] = column[String]("surName")
    def email: lifted.Rep[String] = column[String]("Email")
    def isEmailVerified: lifted.Rep[Boolean] = column[Boolean]("IsEmailVerified")
    def isEducator: lifted.Rep[Boolean] = column[Boolean]("IsEducator")
    def isAdministrator: lifted.Rep[Boolean] = column[Boolean]("IsAdministrator")
    def avatarUrl: lifted.Rep[Option[String]] = column[Option[String]]("AvatarUrl")

    def * : ProvenShape[User] = (id, firstName, surName, email, isEmailVerified,
      isEducator, isAdministrator, avatarUrl) <> ((constructUser _).tupled, deconstructUser)

    def constructUser(id: Option[Int], firstName: String, surName: String, email: String,
      isEmailVerified: Boolean, isEducator: Boolean, isAdministrator: Boolean,
      avatarUrl: Option[String]) =
      {
        User(id = id, firstName = firstName, surName = surName, email = email,
          isEmailVerified = isEmailVerified, isEducator = isEducator,
          isAdministrator = isAdministrator, avatarUrl = avatarUrl)
      }

    def deconstructUser(user: User) = user match {
      case User(id: Option[Int], firstName: String, surName: String,
        email: String, isEmailVerified: Boolean, isEducator: Boolean, isAdministrator: Boolean,
        avatarUrl: Option[String], profiles: List[Profile]) => {
        Option(id, firstName, surName, email, isEmailVerified, isEducator, isAdministrator,
          avatarUrl)
      }
    }
  }
}