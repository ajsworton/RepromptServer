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

import libs.{ AppFactory, AuthHelper, TestingDbQueries }
import models.dto.{ ContentAssignedDto, ContentPackageDto }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.mvc.{ AnyContentAsEmpty, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.duration._
import scala.concurrent.Future

class PackageControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val controller: PackageController = fakeApplication().injector.instanceOf[PackageController]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]

  val teacherId, contentItem1Id, assigned1Id, packageId = 987895
  val studentId, cohortId, packageId2 = 987896

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

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

  describe("getPackage(packageId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getPackage(packageId)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getPackage(packageId)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getPackage(packageId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getPackage(packageId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.getPackage(packageId)(educatorFakeRequest)
      contentAsString(response).length should be > 0
      val assigned = contentAsJson(response).validate[ContentPackageDto]
      assigned.asOpt.isDefined should be(true)
      assigned.get.name should be("Package Name")
    }
  }

}
