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

import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import libraries.UserProfileTestData
import libs.AppFactory
import models.{Profile, User}
import org.scalatest.{AsyncFunSpec, BeforeAndAfter, Matchers}
import org.scalatest.mockito.MockitoSugar

class AuthInfoDaoCredentialsSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with MockitoSugar with AppFactory {

  val authInfoDao: AuthInfoDaoCredentialsSlick = fakeApplication()
    .injector.instanceOf[AuthInfoDaoCredentialsSlick]
  val userDao: UserDaoSlick = fakeApplication().injector.instanceOf[UserDaoSlick]
  val passwordHasher: PasswordHasher = fakeApplication().injector.instanceOf[PasswordHasher]
  val testData = new UserProfileTestData(userDao)

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
        deleted <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should add/update/save PasswordInfo to backing store") {
      //add
      val newPasswordInfo = passwordHasher.hash("password12345") // <- amazingly secure password!
      for {
        user <- userDao.save(testData.user1Linked)
        passInfo <- authInfoDao.add(testData.profile1.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find(testData.profile1.loginInfo)
        valid <- returned should be(Some(newPasswordInfo))
        deleted <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should update PasswordInfo in backing store") {
      //update
      val newPasswordInfo = passwordHasher.hash("password12345") // <- amazingly secure password!
      for {
        user <- userDao.save(testData.user2Linked)
        passInfo <- authInfoDao.update(testData.profile2.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find(testData.profile2.loginInfo)
        valid <- returned should be(newPasswordInfo)
        deleted <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should save PasswordInfo in backing store") {
      //save
      val newPasswordInfo = passwordHasher.hash("password12345") // <- amazingly secure password!
      for {
        user <- userDao.save(testData.user2Linked)
        passInfo <- authInfoDao.save(testData.profile2.loginInfo, newPasswordInfo)
        returned <- authInfoDao.find(testData.profile2.loginInfo)
        valid <- returned should be(newPasswordInfo)
        deleted <- userDao.delete(user.get.id.get)
      } yield valid
    }

    it("should remove PasswordInfo from supplied loginInfo") {
      //remove
      val newPasswordInfo = passwordHasher.hash("password12345") // <- amazingly secure password!
      val emptyPasswordInfo = new PasswordInfo("", "", None)
      for {
        user <- userDao.save(testData.user2Linked)
        passInfo <- authInfoDao.add(testData.profile2.loginInfo, newPasswordInfo)
        remPassInfo <- authInfoDao.remove(testData.profile2.loginInfo)
        returned <- authInfoDao.find(testData.profile2.loginInfo)
        valid <- returned should be(emptyPasswordInfo)
        deleted <- userDao.delete(user.get.id.get)
      } yield valid
    }

  }
}
