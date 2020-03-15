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

import libs.{AuthHelper, DatabaseSupport, TestingDbQueries}
import models.dto.{UserDto, UserLoginDto, UserRegisterDto}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class AuthControllerSpec extends FunSpec with Matchers with BeforeAndAfter with DatabaseSupport {

  val helper: AuthHelper = app.injector.instanceOf[AuthHelper]
  val controller: AuthController = app.injector.instanceOf[AuthController]
  val database: TestingDbQueries = app.injector.instanceOf[TestingDbQueries]

  val teacherId, contentItem1Id, assigned1Id, packageId, itemId, questionId = 485265
  val studentId, cohortId, packageId2, contentFolderId, folderId = 485266

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  val userRegisterDto: UserRegisterDto = UserRegisterDto(firstName = "FakeFirstName", surname = "Fakesurname",
    email = "fakeFirst.fakeSur@address.com", isEducator = false, password = "12345")

  var userLoginDto: UserLoginDto = _

  before {
    database.insertStudyContent(teacherId, studentId, studentId + 1)
    helper.educatorId = teacherId
    helper.studentId = studentId
    helper.setup()
    studentFakeRequest = helper.studentFakeRequest
    educatorFakeRequest = helper.educatorFakeRequest
    userLoginDto = UserLoginDto(
      email = helper.educatorLoginInfo.providerKey,
      password = "test")
  }

  after {
    database.clearStudyContent(teacherId, studentId, studentId + 1)
  }

  describe("register") {

    it("should return 400 bad request if not all required information is supplied") {
      val response: Future[Result] = controller.register()(FakeRequest())
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if valid post data supplied") {
      val response: Future[Result] = controller.register()(FakeRequest()
        withJsonBody Json.toJson(userRegisterDto))
      val user = contentAsJson(response).validate[UserDto]
      user.isSuccess should be(true)
      database.deleteUser(user.get.id.get)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.register()(FakeRequest()
        withJsonBody Json.toJson(userRegisterDto))
      val user = contentAsJson(response).validate[UserDto]
      user.isSuccess should be(true)
      database.deleteUser(user.get.id.get)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.register()(FakeRequest()
        withJsonBody Json.toJson(userRegisterDto))
      val user = contentAsJson(response).validate[UserDto]
      user.isSuccess should be(true)
      database.deleteUser(user.get.id.get)
      user.get.id.isDefined should be(true)
      user.get.id.get should be > 0
      user.get.firstName should be(userRegisterDto.firstName)
      user.get.surname should be(userRegisterDto.surname)
      user.get.email should be(userRegisterDto.email)
      user.get.isEducator should be(userRegisterDto.isEducator)
    }
  }

  describe("login") {

    it("should return 400 bad request if not all required information is supplied") {
      val response: Future[Result] = controller.login()(FakeRequest())
      status(response) should be(BAD_REQUEST)
    }

    it("should return 400 bad request if valid post data supplied, but no matching user found") {
      val response: Future[Result] = controller.login()(FakeRequest()
        withJsonBody Json.toJson(userLoginDto.copy(password = "none")))
      val content = contentAsString(response)
      content.contains("No Matching Record Found") should be(true)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if valid post data supplied") {
      val response: Future[Result] = controller.login()(FakeRequest()
        withJsonBody Json.toJson(userLoginDto))
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.login()(FakeRequest()
        withJsonBody Json.toJson(userLoginDto))
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.login()(FakeRequest()
        withJsonBody Json.toJson(userLoginDto))
      val user = contentAsJson(response).validate[UserDto]
      user.isSuccess should be(true)
      user.get.id.isDefined should be(true)
      user.get.id.get should be > 0
      user.get.email should be(userLoginDto.email)
      header("X-Auth-Token", response).isDefined should be(true)
    }
  }

  describe("profile") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.profile()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.profile()(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return 200 OK if a student") {
      val response: Future[Result] = controller.profile()(studentFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.profile()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.profile()(educatorFakeRequest)
      val assigned = contentAsJson(response).validate[UserDto]
      assigned.isSuccess should be(true)
      assigned.get.email should be(helper.educator.email)
      assigned.get.isEducator should be(helper.educator.isEducator)
      assigned.get.id should be(helper.educator.id)
    }
  }
}
