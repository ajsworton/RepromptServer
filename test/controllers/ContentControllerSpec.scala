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
import models.dto.ContentFolderDto
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ContentControllerSpec extends FunSpec with Matchers with BeforeAndAfter with DatabaseSupport {

  val helper: AuthHelper = app.injector.instanceOf[AuthHelper]
  val controller: ContentController = app.injector.instanceOf[ContentController]
  val database: TestingDbQueries = app.injector.instanceOf[TestingDbQueries]

  val teacherId, contentItem1Id, assigned1Id, packageId, itemId, questionId = 485265
  val studentId, cohortId, packageId2, contentFolderId, folderId = 485266

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  val contentFolderDto = ContentFolderDto(id = None, parentId = None, ownerId = helper.educatorId,
    name = "gbhjfddoklb")

  before {
    database.insertStudyContent(teacherId, studentId, studentId + 1)
    helper.educatorId = teacherId
    helper.studentId = studentId
    helper.setup()
    studentFakeRequest = helper.studentFakeRequest
    educatorFakeRequest = helper.educatorFakeRequest
  }

  after {
    database.clearStudyContent(teacherId, studentId, studentId + 1)
  }

  describe("getAllFoldersByCurrentUser") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getAllFoldersByCurrentUser()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getAllFoldersByCurrentUser()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getAllFoldersByCurrentUser()(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getAllFoldersByCurrentUser()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.getAllFoldersByCurrentUser()(educatorFakeRequest)
      val retrieved = contentAsJson(response).validate[List[ContentFolderDto]]
      retrieved.isSuccess should be(true)
      retrieved.get.size should be > 0
    }
  }

  describe("getFolder(folderId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getFolder(folderId)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getFolder(folderId)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but id is invalid") {
      val response: Future[Result] = controller.getFolder(0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getFolder(folderId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getFolder(folderId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.getFolder(folderId)(educatorFakeRequest)
      val retrieved = contentAsJson(response).validate[ContentFolderDto]
      retrieved.isSuccess should be(true)
      retrieved.get.id should be(Some(folderId))
    }
  }

  describe("saveFolder") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.saveFolder()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.saveFolder()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but post data is invalid") {
      val response: Future[Result] = controller.saveFolder()(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator with valid post data") {
      val response: Future[Result] = controller.saveFolder()(educatorFakeRequest
        withJsonBody Json.toJson(contentFolderDto))
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.saveFolder()(educatorFakeRequest
        withJsonBody Json.toJson(contentFolderDto))
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.saveFolder()(educatorFakeRequest
        withJsonBody Json.toJson(contentFolderDto))
      val saved = contentAsJson(response).validate[ContentFolderDto]
      saved.isSuccess should be(true)
      saved.get.id.isDefined should be(true)
      saved.get.id.get should be > 0
      saved.get should be(contentFolderDto.copy(id = saved.get.id))
    }
  }

}
