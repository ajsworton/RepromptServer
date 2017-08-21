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

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.test.FakeEnvironment
import env.JWTEnv
import libs.AppFactory
import models.{Profile, User}
import org.scalatest.{AsyncFunSpec, Matchers}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{UNAUTHORIZED, status}

import scala.concurrent.Future

class StudyControllerSpec extends AsyncFunSpec with Matchers with AppFactory {

  val studyController: StudyController = fakeApplication().injector.instanceOf[StudyController]
  val profile: Profile =  Profile(userId = Some(8867),
                          loginInfo = new LoginInfo("credentials", "faked@fake.com"),
                          email = Some("faked@fake.com"),
                          firstName = Some("Test"),
                          lastName = Some("Fake"),
  )
  val identity: User = User(profile)
  implicit val env: FakeEnvironment[JWTEnv] = FakeEnvironment[JWTEnv](Seq(identity.profiles.head.loginInfo -> identity))

  describe("saveStudyScore") {

//    it("should return code 401 If not authenticated") {
//      val request = FakeRequest().headers.add()
//      val response: Future[Result] = studyController.saveStudyScore()(request)
//      status(response) should be(UNAUTHORIZED)
//    }

//    it("should save a study score's dates correctly") {
//      studyController.
//    }
  }


  //{"userId":4,"contentItemId":1,"score":100,"scoreDate":"2017-08-21","streak":7,"repromptDate":"2017-06-17"}
}
