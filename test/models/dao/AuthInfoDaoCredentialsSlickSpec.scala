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

import com.mohiva.play.silhouette.api.util.{ PasswordHasher, PasswordInfo }
import libs.{ AppFactory, UserProfileTestData }
import play.api.db.DBApi
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, BeforeAndAfterAll, Matchers }
import org.scalatest.mockito.MockitoSugar
import play.api.db.evolutions.Evolutions

class AuthInfoDaoCredentialsSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with MockitoSugar with AppFactory {

  val authInfoDao: AuthInfoDaoCredentialsSlick = fakeApplication()
    .injector.instanceOf[AuthInfoDaoCredentialsSlick]
  val userDao: UserDaoSlick = fakeApplication().injector.instanceOf[UserDaoSlick]
  val passwordHasher: PasswordHasher = fakeApplication().injector.instanceOf[PasswordHasher]
  val testData = new UserProfileTestData(userDao)
  val newPasswordInfo: PasswordInfo = passwordHasher.hash("12345") // <- That's the exact same
  // code I have on my luggage!

  before {
    testData.before
  }

  after {
    testData.after
  }

  describe("AuthInfoDaoCredentialsSlick") {
    it("should obtain PasswordInfo from backing store from a supplied loginInfo") {
      //find
      for {
        user <- userDao.save(testData.user1Linked)
        passwordInfo <- authInfoDao.find(testData.profile1.loginInfo)
        valid <- passwordInfo should be(testData.profile1.passwordInfo)
        _ <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should add/update/save PasswordInfo to backing store") {
      //add
      val user1 = testData.getUserDoubleProfile
      for {
        user <- userDao.save(user1)
        passInfo <- authInfoDao.add(user1.profiles.head.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find(user1.profiles.head.loginInfo)
        valid <- returned should be(Some(passInfo))
        _ <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should update PasswordInfo in backing store") {
      //update
      val user1 = testData.getUserSingleProfile
      for {
        user <- userDao.save(user1)
        _ <- authInfoDao.update(user1.profiles.head.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find(user1.profiles.head.loginInfo)
        valid <- returned should be(Some(newPasswordInfo))
        _ <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should save PasswordInfo in backing store") {
      //save
      val user1 = testData.getUserSingleProfile
      for {
        user <- userDao.save(user1)
        _ <- authInfoDao.save(user1.profiles.head.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find(user1.profiles.head.loginInfo)
        valid <- returned should be(Some(newPasswordInfo))
        _ <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should remove PasswordInfo from supplied loginInfo") {
      //remove
      val user = userDao.save(testData.user1Linked)
      val add = authInfoDao.add(testData.profile1.loginInfo, newPasswordInfo)

      for {
        user <- user
        add <- add
        remove <- authInfoDao.remove(testData.profile1.loginInfo)
        returned <- authInfoDao.find(testData.profile1.loginInfo)
        valid <- returned should be(None)
        _ <- userDao.delete(user.get.id.get)
        _ <- userDao.delete(user.get.id.get)
      } yield valid
    }

  }
}
