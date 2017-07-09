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

  //def find(id: Int): Future[Option[User]] = db.run(Users.filter(_.id === id).result.headOption)

  def find(id: Int): Future[Option[User]] = {
    val result: Future[Option[User]] = for {
      returnedUser <- db.run(Users.filter(_.id === id).result.headOption)
      usersProfiles: Seq[Profile] <- db.run(Profiles.filter(_.userId === returnedUser.get.id).result)
      user <- Future(returnedUser.map(u => u.copy(profiles = usersProfiles.toList)))
      //.filter(_.userId.isDefined)))
    } yield user

    result
  }

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = for {
      p <- Profiles if matchOnLoginInfo(p, loginInfo)
      u <- Users if u.id === p.userId
    } yield u

    db.run(query.result.headOption)
  }

  override def save(user: User): Future[Option[User]] = {

    val returnedUser = db.run(
      (Users returning Users.map(_.id)
        into ((user, returnedId) => user.copy(id = returnedId))
      ) += user)

    returnedUser.flatMap {
      usr: User =>
        {
          val mappedProfiles = usr.profiles.map(p => p.copy(userId = usr.id))
          val usrMapped = usr.copy(profiles = mappedProfiles)
          println(s"User profiles attached: ${user.profiles.size}")

          for {
            _ <- db.run(Profiles ++= usrMapped.profiles)
            read <- find(usrMapped.id.get)
          } yield read
        }
    }
  }

  override def delete(userId: Int): Future[Int] = db.run(Users.filter(_.id === userId).delete)

  override def delete(loginInfo: LoginInfo): Future[Int] = {
    val affected = for {
      id <- db.run(Profiles.filter(p => matchOnLoginInfo(p, loginInfo)).map(_.userId.get).result.headOption)
      affected <- db.run(Users.filter(_.id === id.getOrElse(0)).delete)
    } yield affected
    affected
  }

  def checkDuplicate(user: User): Future[Boolean] = db.run(Users.filter(
    u => matchUserOnDuplicate(u, user)
  ).result.headOption) flatMap {
    case None => Future(false)
    case Some(_) => Future(true)
  }

  def matchUserOnDuplicate(u: User.UsersTable, user: User) = {
    u.id === user.id
  }

  override def confirm(loginInfo: LoginInfo): Future[Int] = {
    val query = for {
      p <- Profiles if matchOnLoginInfo(p, loginInfo)
    } yield p.confirmed

    db.run(query.update(true))
  }

  override def link(user: User, profile: Profile): Future[Option[User]] = {
    if (user.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(Profiles += profile.copy(userId = user.id))
        read <- find(user.id.get)
      } yield read
    }
  }

  override def update(user: User): Future[Option[User]] = {
    if (user.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(Users.update(user))
        read <- find(user.id.get)
      } yield read
    }
  }

  override def update(profile: Profile): Future[Option[User]] = {
    if (profile.userId.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(Profiles.update(profile))
        read <- find(profile.userId.get)
      } yield read
    }
  }

  def matchOnLoginInfo(p: Profile.ProfilesTable, loginInfo: LoginInfo) = {
    p.providerId === loginInfo.providerID && p.providerKey === loginInfo.providerKey
  }
}
