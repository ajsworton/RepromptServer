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
import models.services.{ AuthInfoService, UserService }
import views.html.defaultpages.todo

import scala.concurrent.{ ExecutionContext, Future }

class AuthInfoDaoCredentialsSlick @Inject() (userService: UserService)(implicit executionContext: ExecutionContext)
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
      case Some(user) => {
        if (user.profiles.count(p => p.loginInfo == loginInfo) > 0) {
          Future(user.profiles.filter(p => p.loginInfo == loginInfo).head.passwordInfo)
        } else {
          Future(None)
        }
      }
    }
  }

  /**
   * Adds new password info for the given login info.
   * @param loginInfo the supplied loginInfo
   * @param authInfo the supplied authInfo
   * @return a future option PasswordInfo
   */
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {

//    val response = for {
//      user: Option[User] <- userService.retrieve(loginInfo)
//      profile <- getMatchingProfile(user.get.profiles, p => p.loginInfo ==
//        loginInfo)
//      _ <- userService.save(profile.copy(passwordInfo = Some(authInfo)))
//    } yield authInfo
//
//    response


        userService.retrieve(loginInfo).flatMap {
          case None => Future(emptyPassInfo)
          case Some(user) => {
            if (user.profiles.count(p => p.loginInfo == loginInfo) > 0) {
              val profile = user.profiles.filter(p => p.loginInfo == loginInfo).head.copy()
              userService.save(profile.copy(passwordInfo = Some(authInfo))).flatMap {
                case None => Future(emptyPassInfo)
                case Some(_) => Future(authInfo)
              }
            } else {
              Future(emptyPassInfo)
            }
          }
        }
  }

  def getMatchingProfile(profiles: List[Profile], predicate: Profile => Boolean): Option[Profile]
  = {
    profiles.find(predicate)
  }

  /**
   * Updates the password info for the given login info.
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

    userService.retrieve(loginInfo).flatMap {
      case None => Future(Unit)
      case Some(user) => {
        if (user.profiles.count(p => p.loginInfo == loginInfo) > 0) {
          val profile: Profile = user.profiles.filter(p => p.loginInfo == loginInfo).head
          userService.save(profile.copy(passwordInfo = Some(emptyPassInfo)))
          Future(Unit)
        } else {
          Future(Unit)
        }
      }
    }
  }
}
