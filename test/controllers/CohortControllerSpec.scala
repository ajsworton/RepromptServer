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

import libs.{ AppFactory, AuthHelper, CohortTestData }
import models.dto.{ CohortDto, CohortMemberDto, ContentItemDto, QuestionDto, ScoreDto }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.libs.json.{ JsError, JsPath, JsSuccess, Json, Reads }
import play.api.mvc.{ Action, AnyContent, AnyContentAsEmpty, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.functional.syntax._

import scala.concurrent.Future

class CohortControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val controller: CohortController = fakeApplication().injector.instanceOf[CohortController]
  val testData: CohortTestData = fakeApplication().injector.instanceOf[CohortTestData]

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  val teacherId, cohortId = 989898
  val studentId = 989899

  val cohortDto = new CohortDto(None, None, teacherId, "TestFishX")

  before {
    testData.insertCohortContent(teacherId, studentId)
    helper.educatorId = teacherId
    helper.studentId = studentId
    helper.setup()
    studentFakeRequest = helper.studentFakeRequest
    educatorFakeRequest = helper.educatorFakeRequest
  }

  after {
    testData.clearCohortContent(teacherId, studentId)
  }

  describe("getAllByOwner(ownerId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getAllByOwner(2)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not an educator") {
      val response: Future[Result] = controller.getAllByOwner(2)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getAllByOwner(2)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getAllByOwner(2)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.getAllByOwner(2)(educatorFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

  describe("get(cohortId: Int)") {
    it("should return code 401 unauthorised If not authenticated") {
      val response: Future[Result] = controller.get(cohortId)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 forbidden if not an educator") {
      val response: Future[Result] = controller.get(cohortId)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.get(cohortId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json when successful") {
      val response: Future[Result] = controller.get(cohortId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return requested content when successful") {
      val response: Future[Result] = controller.get(cohortId)(educatorFakeRequest)
      val json = contentAsJson(response)
      val item = json.validate[CohortDto]
      item.asOpt.isDefined should be(true)
      item.get.id should be(Some(cohortId))
    }
  }

  describe("getAllByCurrentUser") {
    it("should return code 401 unauthorised If not authenticated") {
      val response: Future[Result] = controller.getAllByCurrentUser()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 forbidden if not an educator") {
      val response: Future[Result] = controller.getAllByCurrentUser()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getAllByCurrentUser()(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json when successful") {
      val response: Future[Result] = controller.getAllByCurrentUser()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return requested content when successful") {
      val response: Future[Result] = controller.getAllByCurrentUser()(educatorFakeRequest)
      val json = contentAsJson(response)
      val item = json.validate[List[CohortDto]]
      item.asOpt.isDefined should be(true)
      item.get.head.ownerId should be(helper.educatorId)
    }
  }

  describe("save") {
    it("should return code 401 unauthorised If not authenticated") {
      val response: Future[Result] = controller.save()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 forbidden if not an educator") {
      val response: Future[Result] = controller.save()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
      val cohort = contentAsJson(response).validate[CohortDto]
      controller.delete(cohort.get.id.get)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json when successful") {
      val response: Future[Result] = controller.save()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return requested content when successful") {
      val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
      val cohort = contentAsJson(response).validate[CohortDto]

      cohort.asOpt.isDefined should be(true)
      controller.delete(cohort.get.id.get)(educatorFakeRequest)
      cohort.get.ownerId should be(helper.educatorId)
      cohort.get.name should be(cohortDto.name)
    }

    it("should update requested content if it exists") {
      val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
      val cohort = contentAsJson(response).validate[CohortDto]
      val updated: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohort.get.copy(name = "modified"))))
      val updatedCohort = contentAsJson(updated).validate[CohortDto]
      cohort.asOpt.isDefined should be(true)
      controller.delete(cohort.get.id.get)(educatorFakeRequest)
      updatedCohort.get.ownerId should be(helper.educatorId)
      updatedCohort.get.name should be("modified")
    }
  }

  describe("delete(cohortId: Int)") {
    it("should return code 401 unauthorised If not authenticated") {
      val response: Future[Result] = controller.delete(999998998)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 forbidden if not an educator") {
      val response: Future[Result] = controller.delete(999998998)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response = saveAndDeleteCohort
      status(response) should be(OK)
    }

    it("should return json when successful") {
      val response = saveAndDeleteCohort
      contentType(response) should be(Some("application/json"))
    }

    it("should return 1 if successful") {
      val response = saveAndDeleteCohort
      val cohort = contentAsString(response)
      cohort should be("1")
    }
  }

  describe("attach") {
    it("should return code 401 unauthorised If not authenticated") {
      val response: Future[Result] = controller.attach()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 forbidden if not an educator") {
      val response: Future[Result] = controller.attach()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response = saveCohortAttachUserAndCleanup()
      status(response) should be(OK)
    }

    it("should return json when successful") {
      val response = saveCohortAttachUserAndCleanup()
      contentType(response) should be(Some("application/json"))
    }

    it("should return 1 if successful") {
      val response = saveCohortAttachUserAndCleanup()
      val cohort = contentAsString(response)
      cohort should be("1")
    }

    it("should return BadRequest if invalid id supplied") {
      val response = saveCohortAttachUserAndCleanupWithErrorData()
      status(response) should be(BAD_REQUEST)
    }

    it("should return error response message if invalid id supplied") {
      val response = saveCohortAttachUserAndCleanupWithErrorData()
      contentAsString(response) should be("{\"error\":\"CohortId and UserId must be defined\"}")
    }
  }

  describe("detach(cohortId: Int, userId: Int)") {
    it("should return code 401 unauthorised If not authenticated") {
      val response: Future[Result] = controller.detach(0, 0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 forbidden if not an educator") {
      val response: Future[Result] = controller.detach(0, 0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response = saveCohortAttachUserDetachAndCleanup()
      status(response) should be(OK)
    }

    it("should return json when successful") {
      val response = saveCohortAttachUserDetachAndCleanup()
      contentType(response) should be(Some("application/json"))
    }

    it("should return 1 if successful") {
      val response = saveCohortAttachUserDetachAndCleanup()
      val cohort = contentAsString(response)
      cohort should be("1")
    }

    it("should return BadRequest if invalid id supplied") {
      val response = saveCohortAttachUserDetachAndCleanupWithErrorData()
      status(response) should be(BAD_REQUEST)
    }

    it("should return error response message if invalid id supplied") {
      val response = saveCohortAttachUserDetachAndCleanupWithErrorData()
      contentAsString(response) should be("{\"error\":\"CohortId and UserId must be defined\"}")
    }

  }

  private def saveCohortAttachUserDetachAndCleanupWithErrorData(): Future[Result] = {
    val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
    val cohort = contentAsJson(response).validate[CohortDto]
    cohort.asOpt.isDefined should be(true)
    val cohortMemberDto = new CohortMemberDto(cohort.get.id, Some(helper.studentId))

    val attachRequest = controller.attach()(educatorFakeRequest.withJsonBody(Json.toJson(cohortMemberDto)))
    val attached = contentAsJson(attachRequest).validate[CohortMemberDto]
    val detachRequest = controller.detach(0, 0)(educatorFakeRequest)
    val finished = contentAsString(detachRequest)
    controller.delete(cohort.get.id.get)(educatorFakeRequest)
    detachRequest
  }

  private def saveCohortAttachUserDetachAndCleanup(): Future[Result] = {
    val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
    val cohort = contentAsJson(response).validate[CohortDto]
    cohort.asOpt.isDefined should be(true)
    val cohortMemberDto = new CohortMemberDto(cohort.get.id, Some(helper.studentId))

    val attachRequest = controller.attach()(educatorFakeRequest.withJsonBody(Json.toJson(cohortMemberDto)))
    val attached = contentAsJson(attachRequest).validate[CohortMemberDto]
    val detachRequest = controller.detach(cohortMemberDto.cohortId.get, cohortMemberDto.userId.get)(educatorFakeRequest)
    val finished = contentAsString(detachRequest)
    controller.delete(cohort.get.id.get)(educatorFakeRequest)
    detachRequest
  }

  private def saveCohortAttachUserAndCleanup(): Future[Result] = {
    val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
    val cohort = contentAsJson(response).validate[CohortDto]
    cohort.asOpt.isDefined should be(true)
    val cohortMemberDto = new CohortMemberDto(cohort.get.id, Some(helper.studentId))

    val attachRequest = controller.attach()(educatorFakeRequest.withJsonBody(Json.toJson(cohortMemberDto)))
    val attached = contentAsJson(attachRequest).validate[CohortMemberDto]
    controller.delete(cohort.get.id.get)(educatorFakeRequest)
    attachRequest
  }

  private def saveCohortAttachUserAndCleanupWithErrorData(): Future[Result] = {
    val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
    val cohort = contentAsJson(response).validate[CohortDto]
    cohort.asOpt.isDefined should be(true)
    val cohortMemberDto = new CohortMemberDto(cohort.get.id, Some(helper.studentId))

    val attachRequest = controller.attach()(educatorFakeRequest.withJsonBody(Json.toJson(cohortMemberDto.copy(cohortId = Some(0)))))
    val attached = contentAsJson(attachRequest).validate[CohortMemberDto]
    controller.delete(cohort.get.id.get)(educatorFakeRequest)
    attachRequest
  }

  private def saveAndDeleteCohort: Future[Result] = {
    val response: Future[Result] = controller.save()(educatorFakeRequest.withJsonBody(Json.toJson(cohortDto)))
    val cohort = contentAsJson(response).validate[CohortDto]

    cohort.asOpt.isDefined should be(true)
    controller.delete(cohort.get.id.get)(educatorFakeRequest)
  }

}
