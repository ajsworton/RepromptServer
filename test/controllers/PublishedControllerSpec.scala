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

import java.time.LocalDate

import com.mohiva.play.silhouette.test.FakeEnvironment
import env.JWTEnv
import libs.{ AppFactory, AuthHelper, TestingDbQueries }
import models.dto.{ CohortDto, ContentAssignedCohortDto, ContentAssignedDto, ContentPackageDto, ScoreDto }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.libs.json.Json
import play.api.mvc.{ AnyContentAsEmpty, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class PublishedControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val controller: PublishedController = fakeApplication().injector.instanceOf[PublishedController]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]

  implicit var env: FakeEnvironment[JWTEnv] = _

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = helper.studentFakeRequest
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = helper.educatorFakeRequest

  val teacherId, contentItem1Id, assigned1Id = 989898
  val studentId, cohortId = 989899
  val cohortId3 = teacherId - 1

  val assignedDto = ContentAssignedDto(id = None, name = "hnbgarayjtjqa",
    examDate = LocalDate.of(2018, 5, 4), active = true, ownerId = Some(teacherId))

  val assignedCohortDtoExist = ContentAssignedCohortDto(assignedId = Some(assigned1Id), cohortId = Some(cohortId))
  val assignedCohortDtoNew = ContentAssignedCohortDto(assignedId = Some(assigned1Id), cohortId = Some(cohortId3))

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

  describe("get(id: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.get(assigned1Id)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.get(assigned1Id)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.get(assigned1Id)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.get(assigned1Id)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.get(assigned1Id)(educatorFakeRequest)
      contentAsString(response).length should be > 0
      val assigned = contentAsJson(response).validate[ContentAssignedDto]
      assigned.asOpt.isDefined should be(true)
      assigned.get.name should be("Test Exam")
      assigned.get.examDate should be(LocalDate.of(2017, 10, 1))
    }
  }

  describe("getAllPublishedByCurrentUser") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getAllPublishedByCurrentUser()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getAllPublishedByCurrentUser()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getAllPublishedByCurrentUser()(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getAllPublishedByCurrentUser()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.getAllPublishedByCurrentUser()(educatorFakeRequest)
      contentAsString(response).length should be > 0
      val assigned = contentAsJson(response).validate[List[ContentAssignedDto]]
      assigned.asOpt.isDefined should be(true)
      assigned.get.size should be(2)
    }
  }

  describe("savePublishedExam") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.savePublishedExam()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.savePublishedExam()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.savePublishedExam()(educatorFakeRequest.withJsonBody(Json.toJson(assignedDto)))
      val saved = contentAsJson(response).validate[ContentAssignedDto]
      saved.asOpt.isDefined should be(true)
      controller.deletePublishedExam(saved.get.id.get)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.savePublishedExam()(educatorFakeRequest.withJsonBody(Json.toJson(assignedDto)))
      val saved = contentAsJson(response).validate[ContentAssignedDto]
      saved.asOpt.isDefined should be(true)
      controller.deletePublishedExam(saved.get.id.get)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.savePublishedExam()(educatorFakeRequest.withJsonBody(Json.toJson(assignedDto)))
      contentAsString(response).length should be > 0
      val saved = contentAsJson(response).validate[ContentAssignedDto]
      saved.asOpt.isDefined should be(true)
      saved.get.id.isDefined should be(true)
      controller.deletePublishedExam(saved.get.id.get)
      saved.get.examDate should be(assignedDto.examDate)
      saved.get.name should be(assignedDto.name)
      saved.get.enabled should be(assignedDto.enabled)
      saved.get.ownerId should be(assignedDto.ownerId)
    }
  }

  describe("deletePublishedExam") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.deletePublishedExam(0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.deletePublishedExam(0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.deletePublishedExam(0)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.deletePublishedExam(0)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.savePublishedExam()(educatorFakeRequest.withJsonBody(Json.toJson(assignedDto)))
      contentAsString(response).length should be > 0
      val saved = contentAsJson(response).validate[ContentAssignedDto]
      saved.asOpt.isDefined should be(true)
      val delete: Future[Result] = controller.deletePublishedExam(saved.get.id.get)(educatorFakeRequest)
      val deleted = contentAsJson(delete).validate[Int]

      deleted.isSuccess should be(true)
      deleted.get should be(1)
    }
  }

  describe("attachCohort") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.attachCohort()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.attachCohort()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 bad request if an educator with invalid post data") {
      val response: Future[Result] = controller.attachCohort()(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator with valid post data") {
      val response: Future[Result] = controller.attachCohort()(educatorFakeRequest.withJsonBody(Json.toJson(assignedCohortDtoNew)))
      val msg = contentAsString(response)
      msg should be("1")
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.attachCohort()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    //    it("should return the correct content") {
    //      val detached = controller.detachCohort(assignedCohortDto.assignedId.get, assignedCohortDto.cohortId.get)(educatorFakeRequest)
    //      status(detached) should be(OK)
    //      val response: Future[Result] = controller.attachCohort()(educatorFakeRequest.withJsonBody(Json.toJson(assignedCohortDto)))
    //      contentAsString(response).length should be > 0
    //      val attached = contentAsJson(response).validate[Int]
    //      attached.isSuccess should be(true)
    //      attached.get should be(1)
    //    }
  }
}
