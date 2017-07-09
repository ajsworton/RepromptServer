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

import java.io.File
import javax.inject.Inject

import akka.actor.Status.Success
import com.google.inject.Guice
import models.User
import models.services.{UserService, UserServiceImpl}
import org.mockito.Mockito._
import org.scalatest.{AsyncFunSpec, BeforeAndAfter, Matchers, TestSuite}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{FakeApplicationFactory, PlaySpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration}
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.bind
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import libs.AppFactory

class UserDaoSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with MockitoSugar with AppFactory{

  var user1: User = _
  val userDao: UserDaoSlick = fakeApplication().injector.instanceOf[UserDaoSlick]

  before {
    user1 = User(id = Option(1), firstName = "Barry", surName = "Bear", email = "String")
    //    user2 = User(id = Option(2), firstName = "Peter", surName = "Pan", email = "String")
  }

  describe("UserDaoSlick") {
    it("should correctly insert a user by id") {
      //tests save
      val returnedUser = userDao.save(user1)
      returnedUser map {
        result =>
          {
            result should not be None
            result match {
              case Some(usr) => {
                //also tests delete
                userDao.delete(usr.id.get)
                usr.id.isDefined should be(true)
                usr.id.get should be > 0
              }
            }
          }
      }

    }

  }
}
