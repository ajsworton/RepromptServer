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
import models.Profile
import models.services.{ AuthInfoService, UserService }

import scala.concurrent.{ ExecutionContext, Future }

class AuthInfoDaoCredentialsSlick @Inject() (userService: UserService)
        (implicit executionContext: ExecutionContext) extends AuthInfoService {
  /**
   * Finds the password info which is linked with the specified login info.
   * @param loginInfo the supplied loginInfo
   * @return a future option PasswordInfo
   */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    userService.retrieve(loginInfo).flatMap {
      case None => Future(None)
      case Some(user) => Future(user.profiles.filter(p => p.loginInfo == loginInfo).head.passwordInfo)
    }
  }

  /**
   * Adds new password info for the given login info.
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @return a future option PasswordInfo
   */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[Option[PasswordInfo]] = {
    userService.retrieve(loginInfo).flatMap {
      case None => Future(None)
      case Some(user) => {
        val profile = user.profiles.filter(p => p.loginInfo == loginInfo).head.copy()
        userService.save(profile.copy(passwordInfo = Some(authInfo))).flatMap {
          case None => Future(None)
          case Some(_) => Future(Some(authInfo))
        }
      }
    }
  }

  /**
   * Updates the password info for the given login info.
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @return a future option PasswordInfo
   */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[Option[PasswordInfo]] = ???

  /**
   * Saves the password info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @return a future option PasswordInfo
   */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[Option[PasswordInfo]] = ???

  /**
   * Removes the password info for the given login info.
   * @param loginInfo the supplied loginInfo
   * @return a future option PasswordInfo
   */
  def remove(loginInfo: LoginInfo): Future[PasswordInfo] = ???
}
