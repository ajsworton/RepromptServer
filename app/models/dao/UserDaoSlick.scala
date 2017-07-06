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

package models.dao

import java.sql.{ Date, Timestamp }
import java.time.{ LocalDate, LocalDateTime }
import javax.inject._

import com.mohiva.play.silhouette.api.LoginInfo
import models.User.UsersTable
import slick.jdbc.JdbcProfile
import models.{ Profile, User }
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.lifted.{ ProvenShape, Tag }

import scala.concurrent.{ ExecutionContext, Future }

class UserDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends UserDao with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Users = TableQuery[UsersTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def find(id: Int): Future[Option[User]] = db.run(Users.filter(u => u.id === id).result.headOption)

  def find(login: LoginInfo): Future[Option[User]] = db.run(Users.filter(u => u.id === login
    .providerKey.asInstanceOf[Int])
    .result.headOption)

  def save(user: User): Future[User] = {
    db.run(Users returning Users += user)
  }

  override def delete(userId: Int): Unit = ???

  override def delete(loginInfo: LoginInfo): Future[User] = ???

  override def confirm(loginInfo: LoginInfo): Future[User] = ???

  override def link(user: User, profile: Profile): Future[User] = ???

  override def update(profile: Profile): Future[User] = ???
}
