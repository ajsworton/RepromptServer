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

package dao

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}
import javax.inject._

import slick.jdbc.JdbcProfile
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserDao @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile]
{
  import profile.api._

  private val Users = TableQuery[UsersTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def get(id: Int): Future[User] = db.run(Users.filter(u => u.id === id).result.head)

  def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }

  private class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
    implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
      l => Date.valueOf(l),
      d => d.toLocalDate
    )

    implicit val localDateTimeToDateTime = MappedColumnType.base[LocalDateTime, Timestamp](
      l => Timestamp.valueOf(l),
      d => d.toLocalDateTime
    )

    def id: Rep[Int] = column[Int]("Id", O.PrimaryKey)
    def userName: Rep[String] = column[String]("UserName")
    def firstName: Rep[String] = column[String]("FirstName")
    def surName: Rep[String] = column[String]("UserName")
    def email: Rep[String] = column[String]("UserName")
    def UserName: Rep[String] = column[String]("UserName")
    def isEmailVerified: Rep[Boolean] = column[Boolean]("IsEmailVerified")
    def authHash: Rep[String] = column[String]("AuthHash")
    def authResetCode: Rep[Option[String]] = column[Option[String]]("AuthResetCode")
    def authResetExpiry: Rep[Option[LocalDate]] = column[Option[LocalDate]]("AuthResetExpiry")
    def authToken: Rep[Option[String]] = column[Option[String]]("AuthToken")
    def authExpire: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("AuthExpire")
    def isEducator: Rep[Boolean] = column[Boolean]("IsEducator")
    def isAdministrator: Rep[Boolean] = column[Boolean]("IsAdministrator")

    def * : ProvenShape[User] = (
      id,
      userName,
      firstName,
      surName,
      email,
      isEmailVerified,
      authHash,
      authResetCode,
      authResetExpiry,
      authToken,
      authExpire,
      isEducator,
      isAdministrator) <> (User.tupled, User.unapply _)
  }
}
