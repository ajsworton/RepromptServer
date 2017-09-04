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

package controllers

import libs.AppFactory
import models.dto.UserNotificationDto
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.Configuration
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class CronControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val controller: CronController = fakeApplication().injector.instanceOf[CronController]
  val config: Configuration = fakeApplication().injector.instanceOf[Configuration]

  describe("executeRepromptNotification(keyphrase: String)") {
    it("should return unauthorised when an incorrect passphrase is given") {
      val passphrase = config.underlying.getString("notification.sharedPhrase")
      val response: Future[Result] = controller.executeRepromptNotification(s"$passphrase-incorrect")(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return OK when a correct passphrase is given") {
      val passphrase = config.underlying.getString("notification.sharedPhrase")
      val response: Future[Result] = controller.executeRepromptNotification(passphrase)(FakeRequest())
      status(response) should be(OK)
    }

    it("should return a list of all students notified") {
      val passphrase = config.underlying.getString("notification.sharedPhrase")
      val response: Future[Result] = controller.executeRepromptNotification(passphrase)(FakeRequest())
      status(response) should be(OK)
      val users = contentAsJson(response).validate[List[UserNotificationDto]]
      users.isSuccess should be(true)
      users.get.foreach(println)
      users.get.size should be > 0
    }
  }

}
