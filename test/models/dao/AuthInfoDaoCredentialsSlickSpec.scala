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

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import libs.{DatabaseSupport, UserProfileTestData}
import org.scalatest.{AsyncFunSpec, BeforeAndAfter, Matchers}
import org.scalatestplus.mockito.MockitoSugar

class AuthInfoDaoCredentialsSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter with MockitoSugar with DatabaseSupport {

  val authInfoDao: AuthInfoRepository = app.injector.instanceOf[AuthInfoRepository]
  val userDao: UserDaoSlick           = app.injector.instanceOf[UserDaoSlick]
  val passwordHasher: PasswordHasher  = app.injector.instanceOf[PasswordHasher]
  val testData                        = new UserProfileTestData(userDao)
  val newPasswordInfo: PasswordInfo   = passwordHasher.hash("12345") // <- That's the exact same
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
        user         <- userDao.save(testData.user1Linked)
        passwordInfo <- authInfoDao.find[PasswordInfo](testData.profile1.loginInfo)
        valid        <- passwordInfo should be(testData.profile1.passwordInfo)
        _            <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should add/update/save PasswordInfo to backing store") {
      //add
      val user1 = testData.getUserDoubleProfile
      for {
        user     <- userDao.save(user1)
        passInfo <- authInfoDao.add[PasswordInfo](user1.profiles.head.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find[PasswordInfo](user1.profiles.head.loginInfo)
        valid    <- returned should be(Some(passInfo))
        _        <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should update PasswordInfo in backing store") {
      //update
      val user1 = testData.getUserSingleProfile
      for {
        user     <- userDao.save(user1)
        _        <- authInfoDao.update[PasswordInfo](user1.profiles.head.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find[PasswordInfo](user1.profiles.head.loginInfo)
        valid    <- returned should be(Some(newPasswordInfo))
        _        <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should save PasswordInfo in backing store") {
      //save
      val user1 = testData.getUserSingleProfile
      for {
        user     <- userDao.save(user1)
        _        <- authInfoDao.save[PasswordInfo](user1.profiles.head.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find[PasswordInfo](user1.profiles.head.loginInfo)
        valid    <- returned should be(Some(newPasswordInfo))
        _        <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should remove PasswordInfo from supplied loginInfo") {
      for {
        user     <- userDao.save(testData.user1Linked)
        add      <- authInfoDao.add[PasswordInfo](testData.profile1.loginInfo, newPasswordInfo)
        remove   <- authInfoDao.remove[PasswordInfo](testData.profile1.loginInfo)
        returned <- authInfoDao.find[PasswordInfo](testData.profile1.loginInfo)
        valid    <- returned should be(None)
        _        <- userDao.delete(user.get.id.get)
        _        <- userDao.delete(user.get.id.get)
      } yield valid
    }

  }
}
