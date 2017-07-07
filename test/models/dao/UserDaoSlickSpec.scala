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

import akka.actor.Status.Success
import models.User
import models.services.{ UserService, UserServiceImpl }
import org.mockito.Mockito._
import org.scalatest.{ BeforeAndAfter, FunSpec }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class UserDaoSlickSpec @Inject() (implicit executionContext: ExecutionContext)
  extends FunSpec with BeforeAndAfter with MockitoSugar with ScalaFutures {

  val userRepository = mock[UserDao]
  var userService: UserService = _
  var user1: User = _
  var user2: User = _

  before {
    user1 = User(id = Option(1), firstName = "Barry", surName = "Bear", email = "String")
    user2 = User(id = Option(2), firstName = "Peter", surName = "Pan", email = "String")
    userService = new UserServiceImpl(userRepository)
  }

  describe("UserDaoSlick") {
    it("should find users correctly by id") {
      val future: Future[Option[User]] = userService.retrieve(1)
      //val failedFuture: Future[Option[User]] = userService.retrieve(0)

      //future.map(option => u)
      //      future onComplete {
      //        case scala.util.Success => println(s"Got the callback")
      //        case scala.util.Failure(e) => e.printStackTrace
      //      }

      // assert(opUser.get.get === user1)
      //failedFuture.onComplete(opUser => { assert(opUser.get.isEmpty) })
    }
  }
}
