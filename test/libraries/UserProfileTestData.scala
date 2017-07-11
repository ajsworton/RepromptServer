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

package libraries

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.dao.UserDaoSlick
import models.{Profile, User}
import libs.AppFactory

class UserProfileTestData(userDao: UserDaoSlick) extends AppFactory {

  val passwordHasher: PasswordHasher = fakeApplication().injector.instanceOf[PasswordHasher]

  val user1Unlinked = User(id = None, firstName = "Bart", surName = "Illiyan", email = "String")
  val user2Unlinked = User(id = None, firstName = "Micvhael", surName = "Grunthy",
              email = "somewhere@over.the.rainbow")

  val profile1 = generateProfile(user1Unlinked)
  val user1Linked = user1Unlinked.copy(profiles = profile1 :: user1Unlinked.profiles)

  val passInfo2 = Some(passwordHasher.hash("password"))
  val profile2 = generateProfile(user2Unlinked, passInfo2)
  val user2Linked = user2Unlinked.copy(profiles = profile2 :: user1Unlinked.profiles)




  def before = {
    userDao.delete(user1Linked.profiles.head.loginInfo)
    userDao.delete(user2Linked.profiles.head.loginInfo)
  }

  def after = {
    userDao.delete(user1Linked.profiles.head.loginInfo)
    userDao.delete(user2Linked.profiles.head.loginInfo)
  }

  def generateProfile(user: User, passwordInfo: Option[PasswordInfo] = None): Profile = {
    Profile(
      loginInfo = LoginInfo(CredentialsProvider.ID, user.email),
      email = Some(user.email),
      firstName = Some(user.firstName),
      lastName = Some(user.surName),
      fullName = Some(s"${user.firstName} ${user.surName}"),
      passwordInfo = passwordInfo
    )
  }
}
