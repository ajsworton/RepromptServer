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

package libs

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import models.dao.UserDao
import models.{Profile, User}

import scala.concurrent.Future
import scala.util.Random

object UserProfileTestData {
  val r = new Random()

  def getRandomNumber(limit: Int) = r.nextInt(limit)

  def getRandomChar = (r.nextInt(25) + 97).asInstanceOf[Char]

  def getRandomName(length: Int) = {
    var word = ""
    for (n <- 1 to length) {
      word = word + getRandomChar
    }
    word
  }
}

class UserProfileTestData(userDao: UserDao) {

  val passwordHasher: PasswordHasher = new BCryptPasswordHasher

  val user1Unlinked: User = User(id = None, firstName = "Bart", surname = "Illiyan", email = "String",
    isEducator = true)
  val user2Unlinked: User = User(id = None, firstName = "Micvhael", surname = "Grunthy",
    email = "somewhere@over.the.rainbow")
  val user3Unlinked: User = User(id = None, firstName = "Nate", surname = "Hanak",
    email = "nate@theamerican.com")

  val profile1: Profile = generateProfile(user1Unlinked)
  val user1Linked: User = user1Unlinked.copy(profiles = profile1 :: user1Unlinked.profiles)

  val passInfo2: Option[PasswordInfo] = Some(passwordHasher.hash("password"))
  val profile2: Profile = generateProfile(user2Unlinked, passInfo2)
  val user2Linked: User = user2Unlinked.copy(profiles = profile2 :: user2Unlinked.profiles)

  val passInfo3: Option[PasswordInfo] = Some(passwordHasher.hash("letmein"))
  val profile31: Profile = generateProfile(user3Unlinked)
  val profile32: Profile = generateProfile(user3Unlinked, passInfo3, Some(LoginInfo(
    CredentialsProvider.ID,
    "54622563")))

  val profiles3 = List(profile31, profile32)
  val user3Linked: User = user3Unlinked.copy(profiles = profiles3)

  def getUserSingleProfile: User = {
    val user = User(id = None, firstName = getName, surname = getName, email = getEmail)
    val profile = generateProfile(user, Some(passwordHasher.hash(getName)),
      Some(LoginInfo(CredentialsProvider.ID, getName)))
    user.copy(profiles = profile :: user.profiles)
  }

  def getUserDoubleProfile: User = {
    val user = User(id = None, firstName = getName, surname = getName, email = getEmail)
    val profile1 = generateProfile(user, Some(passwordHasher.hash(getName)),
      Some(LoginInfo(CredentialsProvider.ID, getEmail)))
    val profile2 = generateProfile(user, Some(passwordHasher.hash(getName)),
      Some(LoginInfo(CredentialsProvider.ID, getEmail)))
    user.copy(profiles = profile1 :: profile2 :: user.profiles)
  }

  def getName: String = UserProfileTestData.getRandomName(UserProfileTestData.getRandomNumber(6) + 4)
  def getEmail: String = UserProfileTestData.getRandomName(UserProfileTestData.getRandomNumber(6) + 4) + "@" +
    UserProfileTestData.getRandomName(UserProfileTestData.getRandomNumber(6) + 4) + ".com"

  def before: Future[Int] = {
    userDao.delete(user1Linked.profiles.head.loginInfo)
    userDao.delete(user2Linked.profiles.head.loginInfo)
    userDao.delete(user3Linked.profiles.head.loginInfo)
  }

  def after: Future[Int] = {
    userDao.delete(user1Linked.profiles.head.loginInfo)
    userDao.delete(user2Linked.profiles.head.loginInfo)
    userDao.delete(user3Linked.profiles.head.loginInfo)
  }

  private def generateProfile(user: User, passwordInfo: Option[PasswordInfo] = None,
    loginInfo: Option[LoginInfo] = None): Profile = {

    val login: LoginInfo = loginInfo.fold(LoginInfo(CredentialsProvider.ID, user.email))(identity)

    Profile(
      loginInfo = login,
      email = Some(user.email),
      firstName = Some(user.firstName),
      lastName = Some(user.surname),
      fullName = Some(s"${user.firstName} ${user.surname}"),
      passwordInfo = passwordInfo
    )
  }
}
