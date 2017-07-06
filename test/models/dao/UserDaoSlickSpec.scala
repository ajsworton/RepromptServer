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

import models.services.UserService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future

class UserDaoSlickSpec {

}

class UserSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  "UserDaoSlick" should {
    val userRepository = mock[UserRepository]
    val user1 = userModel(Option(1), "user1@test.com")
    val user2 = userModel(Option(2), "user2@test.com")

    // mock the access and return our own results
    when(userRepository.allUsers) thenReturn Future {Seq(user1, user2)}

    val userService = new UserService(userRepository)

    "find users correctly by id" in {
      val future = userService.getUserById(1)

      whenReady(future) { user =>
        user.get mustBe user1
      }
    }

    "update user correctly" in {
      // TODO test this
    }
  }
