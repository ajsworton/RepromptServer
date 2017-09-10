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

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.{ Profile, User }
import models.services.UserService

import scala.concurrent.{ ExecutionContext, Future }

class AuthInfoDaoCredentialsSlick @Inject() (userService: UserService, userDao: UserDao)(implicit executionContext: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] {

  val emptyPassInfo: PasswordInfo = PasswordInfo("", "", None)

  /**
   * Finds the password info which is linked with the specified login info.
   * @param loginInfo the supplied loginInfo
   * @return a future option PasswordInfo
   */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    userService.retrieve(loginInfo).flatMap {
      case None => Future(None)
      case Some(user) => Future(user.profiles.find(p => p.loginInfo == loginInfo).get.passwordInfo)
    }
  }

  /**
   * Adds new password info for the given login info.
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @return a future option PasswordInfo
   */
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    userService.retrieve(loginInfo).flatMap {
      case None => Future(emptyPassInfo)
      case Some(user) => checkAndUpdateUserProfileData(loginInfo, authInfo, user)
    }

  /**
   * Check that the profile exists and has been returned and persist if defined
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @param user the retrieved user form the loginInfo
   * @return
   */
  private def checkAndUpdateUserProfileData(loginInfo: LoginInfo, authInfo: PasswordInfo, user: User) = {
    user.profileFor(loginInfo) match {
      case None => Future(emptyPassInfo)
      case Some(profile) =>
        val updatedUser = getUpdatedUserFromUpdatedProfile(profile, authInfo, user)
        persistUpdatedUser(updatedUser, loginInfo)
    }
  }

  /**
   * Write updated user to backing store and return the new passwordInfo
   * @param updatedUser the updated user
   * @param loginInfo the loginInfo for for the user
   * @return a future passwordInfo
   */
  private def persistUpdatedUser(updatedUser: User, loginInfo: LoginInfo): Future[PasswordInfo] = {
    userDao.update(updatedUser).flatMap {
      case None => Future(emptyPassInfo)
      case Some(user) => Future(user.profileFor(loginInfo).get.passwordInfo.get)
    }
  }

  /**
   * Create a user for update from supplied profile data and updated passwordInfo
   * @param newProfile the updated profile
   * @param authInfo the new passwordInfo
   * @param user the retrieved user
   * @return
   */
  private def getUpdatedUserFromUpdatedProfile(newProfile: Profile, authInfo: PasswordInfo, user: User) = {
    val updatedProfiles: List[Profile] = user.profiles map {
      case profile: Profile if profile == newProfile => profile.copy(passwordInfo = Some(authInfo))
      case profile: Profile => profile
    }
    user.copy(profiles = updatedProfiles)
  }

  /**
   * Updates the password info for the given login info.
   *
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @return a future option PasswordInfo
   */
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    add(loginInfo, authInfo)
  }

  /**
   * Saves the password info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @return a future option PasswordInfo
   */
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    add(loginInfo, authInfo)
  }

  /**
   * Removes the password info for the given login info.
   * @param loginInfo the supplied loginInfo
   * @return a future option PasswordInfo
   */
  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    userDao.delete(loginInfo)
    Future()
  }
}
