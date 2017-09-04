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
import models.dao.{ CohortDao, ContentAssignedDao, ContentPackageDao }
import models.dto.{ ContentAssignedCohortDto, ContentAssignedDto, ContentAssignedPackageDto }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.libs.json.Json
import play.api.mvc.{ AnyContentAsEmpty, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class PublishedControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val controller: PublishedController = fakeApplication().injector.instanceOf[PublishedController]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]
  val cohortDao: CohortDao = fakeApplication().injector.instanceOf[CohortDao]
  val packageDao: ContentPackageDao = fakeApplication().injector.instanceOf[ContentPackageDao]
  val assignedDao: ContentAssignedDao = fakeApplication().injector.instanceOf[ContentAssignedDao]

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  val teacherId, contentItem1Id, assigned1Id, packageId = 989898
  val studentId, cohortId, packageId2 = 989899
  val cohortId3, packageId3 = teacherId - 1

  val assignedDto = ContentAssignedDto(id = None, name = "hnbgarayjtjqa",
    examDate = LocalDate.of(2018, 5, 4), active = true, ownerId = Some(teacherId))

  val assignedCohortDtoExist = ContentAssignedCohortDto(assignedId = Some(assigned1Id), cohortId = Some(cohortId))
  val assignedCohortDtoNew = ContentAssignedCohortDto(assignedId = Some(assigned1Id), cohortId = Some(cohortId3))

  val assignedPackageDtoExist = ContentAssignedPackageDto(assignedId = Some(assigned1Id), packageId = Some(packageId))
  val assignedPackageDtoNew = ContentAssignedPackageDto(assignedId = Some(assigned1Id), packageId = Some(packageId3))

  var contentAssignedDtoNew: ContentAssignedDto = _
  var contentAssignedDtoExist: ContentAssignedDto = _

  before {
    database.insertStudyContent(teacherId, studentId, studentId + 1)
    helper.educatorId = teacherId
    helper.studentId = studentId
    helper.setup()
    studentFakeRequest = helper.studentFakeRequest
    educatorFakeRequest = helper.educatorFakeRequest

    val cohort1Request = cohortDao.find(cohortId)
    val cohort2Request = cohortDao.find(cohortId3)

    val package1Request = packageDao.find(packageId)
    val package2Request = packageDao.find(packageId2)

    val cohort1 = Await.result(cohort1Request, 10 seconds)
    val cohort2 = Await.result(cohort2Request, 10 seconds)

    val package1 = Await.result(package1Request, 10 seconds)
    val package2 = Await.result(package2Request, 10 seconds)
    contentAssignedDtoNew = ContentAssignedDto(id = None, name = "ggtfdahdrtfh",
      examDate = LocalDate.of(2018, 9, 5), active = true, ownerId = Some(teacherId),
      cohorts = Some(List(cohort1.get, cohort2.get)), packages = Some(List(package1.get, package2.get)))
    val contentAssignedDtoExistRequest = assignedDao.find(assigned1Id)
    contentAssignedDtoExist = Await.result(contentAssignedDtoExistRequest, 10 seconds).get
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

    it("should correctly save a new exam with assigned cohorts and packages") {
      val response: Future[Result] = controller.savePublishedExam()(educatorFakeRequest.withJsonBody(Json.toJson(contentAssignedDtoNew)))
      contentAsString(response).length should be > 0
      val saved = contentAsJson(response).validate[ContentAssignedDto]
      saved.asOpt.isDefined should be(true)
      saved.get.id.isDefined should be(true)
      controller.deletePublishedExam(saved.get.id.get)
      saved.get.examDate should be(contentAssignedDtoNew.examDate)
      saved.get.name should be(contentAssignedDtoNew.name)
      saved.get.enabled should be(contentAssignedDtoNew.enabled)
      saved.get.ownerId should be(contentAssignedDtoNew.ownerId)
      saved.get.cohorts.isDefined should be(true)
      saved.get.cohorts.get should have size 2
      saved.get.packages.isDefined should be(true)
      saved.get.packages.get should have size 2
    }

    it("should correctly update an existing exam") {
      val response: Future[Result] = controller.savePublishedExam()(educatorFakeRequest.withJsonBody(Json.toJson(contentAssignedDtoExist.copy(name = "lkjkhgfdsa"))))
      contentAsString(response).length should be > 0
      val updated = contentAsJson(response).validate[ContentAssignedDto]
      updated.asOpt.isDefined should be(true)
      updated.get.id.isDefined should be(true)
      updated.get.examDate should be(contentAssignedDtoExist.examDate)
      updated.get.name should be("lkjkhgfdsa")
      updated.get.enabled should be(contentAssignedDtoExist.enabled)
      updated.get.ownerId should be(contentAssignedDtoExist.ownerId)
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

    it("should return the correct content") {
      val response: Future[Result] = controller.attachCohort()(educatorFakeRequest.withJsonBody(Json.toJson(assignedCohortDtoNew)))
      contentAsString(response).length should be > 0
      val attached = contentAsJson(response).validate[Int]
      attached.isSuccess should be(true)
      attached.get should be(1)
    }
  }

  describe("detachCohort") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.detachCohort(0, 0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.detachCohort(0, 0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 bad request if an educator with invalid data") {
      val response: Future[Result] = controller.detachCohort(0, 0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator with valid data") {
      val response: Future[Result] = controller.detachCohort(assignedCohortDtoExist.assignedId.get, assignedCohortDtoExist.cohortId.get)(educatorFakeRequest)
      val msg = contentAsString(response)
      msg should be("1")
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.detachCohort(assignedCohortDtoExist.assignedId.get, assignedCohortDtoExist.cohortId.get)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.detachCohort(assignedCohortDtoExist.assignedId.get, assignedCohortDtoExist.cohortId.get)(educatorFakeRequest.withJsonBody(Json.toJson(assignedCohortDtoNew)))
      contentAsString(response).length should be > 0
      val detached = contentAsJson(response).validate[Int]
      detached.isSuccess should be(true)
      detached.get should be(1)
    }
  }

  describe("attachPackage") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.attachPackage()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.attachPackage()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 bad request if an educator with invalid post data") {
      val response: Future[Result] = controller.attachPackage()(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator with valid post data") {
      val response: Future[Result] = controller.attachPackage()(educatorFakeRequest.withJsonBody(Json.toJson(assignedPackageDtoNew)))
      val msg = contentAsString(response)
      msg should be("1")
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.attachPackage()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.attachPackage()(educatorFakeRequest.withJsonBody(Json.toJson(assignedPackageDtoNew)))
      contentAsString(response).length should be > 0
      val attached = contentAsJson(response).validate[Int]
      attached.isSuccess should be(true)
      attached.get should be(1)
    }
  }

  describe("detachPackage") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.detachPackage(0, 0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.detachPackage(0, 0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 bad request if an educator with invalid data") {
      val response: Future[Result] = controller.detachPackage(0, 0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator with valid data") {
      val response: Future[Result] = controller.detachPackage(assignedPackageDtoExist.assignedId.get, assignedPackageDtoExist.packageId.get)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.detachPackage(assignedPackageDtoExist.assignedId.get, assignedPackageDtoExist.packageId.get)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return 1 if an educator with existing valid data is supplied") {
      val response: Future[Result] = controller.detachPackage(assignedPackageDtoExist.assignedId.get, assignedPackageDtoExist.packageId.get)(educatorFakeRequest)
      val msg = contentAsString(response)
      msg should be("1")
      status(response) should be(OK)
    }

    it("should return 0 if an educator with new valid data is supplied") {
      val response: Future[Result] = controller.detachPackage(assignedPackageDtoNew.assignedId.get, assignedPackageDtoNew.packageId.get)(educatorFakeRequest)
      val msg = contentAsString(response)
      msg should be("0")
      status(response) should be(OK)
    }
  }
}
