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

import javax.inject._

import com.mohiva.play.silhouette.api.LoginInfo
import models.Profile.ProfilesTable
import models.User.UsersTable
import models.{ Profile, User }
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

class UserDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends UserDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val Users = TableQuery[UsersTable]
  private val Profiles = TableQuery[ProfilesTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def find(id: Int): Future[Option[User]] = db.run(Users.filter(_.id === id).result.headOption)

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = for {
      p <- Profiles if matchOnLoginInfo(p, loginInfo)
      u <- Users if u.id === p.userId
    } yield (u)

    db.run(query.result.headOption)

    //    val response = db.run(query.result).flatMap((u, p) => (u, p).groupBy(u))
    //    db.run(Users.filter(u => u.id === login
    //      .providerKey.asInstanceOf[Int])
    //      .result.headOption)
  }

  override def save(user: User): Future[Option[User]] = {
    val userId = db.run(Users returning Users.map(_.id) += user)
    userId.flatMap {
      case None => Future(None)
      case Some(idVal) => find(idVal)
    }
  }

  override def delete(userId: Int): Future[Int] = db.run(Users.filter(_.id === userId).delete)

  override def delete(loginInfo: LoginInfo): Future[Int] = {
    db.run(Profiles.filter(p => matchOnLoginInfo(p, loginInfo)).delete)
  }

  override def confirm(loginInfo: LoginInfo): Future[Int] = {
    val query = for {
      p <- Profiles if matchOnLoginInfo(p, loginInfo)
    } yield p.confirmed

    db.run(query.update(true))
  }

  override def link(user: User, profile: Profile): Future[Option[User]] = {
    if (!user.id.isDefined) {
      Future(None)
    } else {
      db.run(Profiles += profile.copy(userId = user.id))
      find(user.id.get)
    }
  }

  override def update(profile: Profile): Future[Option[User]] = {
    if (!profile.userId.isDefined) {
      Future(None)
    } else {
      db.run(Profiles.update(profile))
      find(profile.userId.get)
    }
  }

  def matchOnLoginInfo(p: Profile.ProfilesTable, loginInfo: LoginInfo) = {
    p.providerId === loginInfo.providerID && p.providerKey === loginInfo.providerKey
  }
}
