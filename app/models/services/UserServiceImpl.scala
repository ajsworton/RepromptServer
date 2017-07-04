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

/**
 * This code substantially based on the silhouette framework exemplar
 * accessed 04/07/2017
 * https://github.com/mohiva/play-silhouette-seed/
 */

package models.services

import java.time.{ LocalDate, LocalDateTime }
import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User
import models.dao.UserDao

import scala.concurrent.{ ExecutionContext, Future }

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
  override def save(user: User): Future[User] = userDao.save(user)

  //  /**
  //   * Saves the social profile for a user.
  //   *
  //   * If a user exists for this profile then update the user, otherwise create a new user with the
  //   * given profile.
  //   *
  //   * @param profile The social profile to save.
  //   * @return The user for whom the profile was saved.
  //   */
  //  def save(profile: CommonSocialProfile, authHashSupplied: String = ""): Future[User] = {
  //    userDao.find(profile.loginInfo).flatMap {
  //      case Some(user) => // Update user with profile
  //        userDao.save(user.copy(
  //          firstName = profile.firstName,
  //          lastName = profile.lastName,
  //          fullName = profile.fullName,
  //          email = profile.email,
  //          avatarUrl = profile.avatarURL
  //        ))
  //      case None => // Insert a new user
  //        userDao.save(User(
  //          id = None,
  //          userName = profile.loginInfo,
  //          firstName = profile.firstName,
  //          surName = profile.lastName,
  //          email = profile.email,
  //          authHash = authHashSupplied,
  //          avatarUrl = profile.avatarURL,
  //        ))
  //
  //        id: Option[Int],
  //      userName: String,
  //      firstName: String,
  //      surName: String,
  //      email: String,
  //      isEmailVerified: Boolean,
  //      authHash: String,
  //      authResetCode: Option[String],
  //      authResetExpiry: Option[LocalDate],
  //      authToken: Option[String],
  //      authExpire: Option[LocalDateTime],
  //      isEducator: Boolean,
  //      isAdministrator: Boolean,
  //      avatarUrl: Option[String]

  //    }
  //  }
}
