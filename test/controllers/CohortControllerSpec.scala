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
import models.dto.{ CohortDto, ContentItemDto, QuestionDto, ScoreDto }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.libs.json.{ JsError, JsPath, JsSuccess, Json, Reads }
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.functional.syntax._

import scala.concurrent.Future

class CohortControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val controller: CohortController = fakeApplication().injector.instanceOf[CohortController]
  val testData: CohortTestData = fakeApplication().injector.instanceOf[CohortTestData]

  val studentFakeRequest = helper.studentFakeRequest
  val educatorFakeRequest = helper.educatorFakeRequest

  val teacherId, cohortId = 989898
  val studentId = 989899

  before {
    testData.insertCohortContent(teacherId, studentId)
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
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.get(cohortId)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not an educator") {
      val response: Future[Result] = controller.get(cohortId)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.get(cohortId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.get(cohortId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    //    implicit val ContentItemDtoReads: Reads[ContentItemDto] = (
    //      (JsPath \ "id").readNullable[Int] and
    //      (JsPath \ "packageId").read[Int] and
    //      (JsPath \ "imageUrl").readNullable[String] and
    //      (JsPath \ "name").read[String] and
    //      (JsPath \ "content").read[String]
    //    )(ContentItemDto.construct _)

    it("should return content") {
      val response: Future[Result] = controller.get(cohortId)(educatorFakeRequest)
      val json = contentAsJson(response)
      val item = json.validate[CohortDto]
      item.asOpt.isDefined should be(true)
      item.get.id should be(Some(cohortId))
    }
  }

}
