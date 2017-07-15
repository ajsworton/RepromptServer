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

package models.services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.dao.UserDao
import models.{Profile, User}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Handles actions to users.
 *
 * @param userDao The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject() (userDao: UserDao)(implicit ex: ExecutionContext)
  extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  override def retrieve(id: Int): Future[Option[User]] = userDao.find(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDao.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  override def save(user: User): Future[Option[User]] = {
    if (user.id.isDefined) {
      userDao.find(user.id.get).flatMap {
        case None => userDao.save(user)
        case Some(usr) => userDao.update(user)
      }
    } else {
      userDao.save(user)
    }
  }

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the
   * given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  override def save(profile: Profile): Future[Option[User]] = {
    userDao.find(profile.loginInfo).flatMap {
      // user and profile exist, update the profile
      case Some(user) => userDao.update(profile.copy(userId = user.id))
      //no profile exists. Find or Save the user and then save the linked profile.
      case None => {
        if (profile.userId.isEmpty) {
          //write the user
          createUserAndProfile(profile)
        } else {
          //get the user
          val associatedUser = userDao.find(profile.userId.get)
          // write the profile
          associatedUser.flatMap {
            case Some(usr) => userDao.link(usr, profile.copy(userId = usr.id))
            case None => createUserAndProfile(profile)
          }
        }
      }
    }
  }

  def createUserAndProfile(profile: Profile): Future[Option[User]] = {
    val associatedUser = userDao.save(User(
      firstName = getFirstName(profile.firstName, profile.fullName),
      surName = getSurName(profile.lastName, profile.fullName),
      email = profile.email.getOrElse(""),
      avatarUrl = profile.avatarUrl))
    // write the profile
    associatedUser.flatMap((u) => userDao.link(u.get, profile.copy(userId = u.get.id)))
  }

  def getFirstName(firstName: Option[String], fullName: Option[String]): String = firstName match {
    case Some(value) => value
    case None => {
      fullName match {
        case None => ""
        case Some(value) => value.split(" ").head
      }
    }
  }

  def getSurName(surName: Option[String], fullName: Option[String]): String = surName match {
    case Some(value) => value
    case None => {
      fullName match {
        case None => ""
        case Some(value) => value.split(" ").reverse.head
      }
    }
  }
}
