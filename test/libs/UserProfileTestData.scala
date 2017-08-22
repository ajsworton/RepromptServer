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
import com.mohiva.play.silhouette.api.util.{ PasswordHasher, PasswordInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.dao.UserDao
import models.{ Profile, User }

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

class UserProfileTestData(userDao: UserDao) extends AppFactory {

  val passwordHasher: PasswordHasher = fakeApplication().injector.instanceOf[PasswordHasher]

  val user1Unlinked = User(id = None, firstName = "Bart", surName = "Illiyan", email = "String")
  val user2Unlinked = User(id = None, firstName = "Micvhael", surName = "Grunthy",
    email = "somewhere@over.the.rainbow")
  val user3Unlinked = User(id = None, firstName = "Nate", surName = "Hanak",
    email = "nate@theamerican.com")

  val profile1 = generateProfile(user1Unlinked)
  val user1Linked = user1Unlinked.copy(profiles = profile1 :: user1Unlinked.profiles)

  val passInfo2 = Some(passwordHasher.hash("password"))
  val profile2 = generateProfile(user2Unlinked, passInfo2)
  val user2Linked = user2Unlinked.copy(profiles = profile2 :: user2Unlinked.profiles)

  val passInfo3 = Some(passwordHasher.hash("letmein"))
  val profile31 = generateProfile(user3Unlinked)
  val profile32 = generateProfile(user3Unlinked, passInfo3, LoginInfo(
    CredentialsProvider.ID,
    "54622563"))

  val profiles3 = List(profile31, profile32)
  val user3Linked = user3Unlinked.copy(profiles = profiles3)

  def getUserSingleProfile: User = {
    val user = User(id = None, firstName = getName, surName = getName, email = getEmail)
    val profile = generateProfile(user, Some(passwordHasher.hash(getName)),
      LoginInfo(CredentialsProvider.ID, getName))
    user.copy(profiles = profile :: user.profiles)
  }

  def getUserDoubleProfile: User = {
    val user = User(id = None, firstName = getName, surName = getName, email = getEmail)
    val profile1 = generateProfile(user, Some(passwordHasher.hash(getName)),
      LoginInfo(CredentialsProvider.ID, getEmail))
    val profile2 = generateProfile(user, Some(passwordHasher.hash(getName)),
      LoginInfo(CredentialsProvider.ID, getEmail))
    user.copy(profiles = profile1 :: profile2 :: user.profiles)
  }

  def getName = UserProfileTestData.getRandomName(UserProfileTestData.getRandomNumber(6) + 4)
  def getEmail = UserProfileTestData.getRandomName(UserProfileTestData.getRandomNumber(6) + 4) + "@" +
    UserProfileTestData.getRandomName(UserProfileTestData.getRandomNumber(6) + 4) + ".com"

  def before = {
    userDao.delete(user1Linked.profiles.head.loginInfo)
    userDao.delete(user2Linked.profiles.head.loginInfo)
    userDao.delete(user3Linked.profiles.head.loginInfo)
  }

  def after = {
    userDao.delete(user1Linked.profiles.head.loginInfo)
    userDao.delete(user2Linked.profiles.head.loginInfo)
    userDao.delete(user3Linked.profiles.head.loginInfo)
  }

  def generateProfile(user: User, passwordInfo: Option[PasswordInfo] = None,
    loginInfo: LoginInfo = null): Profile = {

    val login = if (loginInfo == null) LoginInfo(CredentialsProvider.ID, user.email) else loginInfo

    Profile(
      loginInfo = login,
      email = Some(user.email),
      firstName = Some(user.firstName),
      lastName = Some(user.surName),
      fullName = Some(s"${user.firstName} ${user.surName}"),
      passwordInfo = passwordInfo
    )
  }
}
