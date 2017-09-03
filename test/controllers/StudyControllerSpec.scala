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

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.test.FakeEnvironment
import env.JWTEnv
import libs.{ AppFactory, AuthHelper, CohortTestData, TestingDbQueries }
import models.dto.{ CohortDto, ScoreDto }
import models.{ Profile, User }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.libs.json.Json
import play.api.mvc.{ AnyContentAsEmpty, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class StudyControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val controller: StudyController = fakeApplication().injector.instanceOf[StudyController]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  val teacherId, contentItem1Id, assigned1Id = 989898
  val studentId = 989899

  val scoreDto = ScoreDto(userId = Some(studentId), contentItemId = contentItem1Id, score = 54,
    scoreDate = Some(LocalDate.now()), streak = 5)

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

  describe("getContentItems") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getContentItems()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getContentItems()(educatorFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if a student") {
      val response: Future[Result] = controller.getContentItems()(studentFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getContentItems()(studentFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.getContentItems()(studentFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

  describe("saveStudyScore") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.saveStudyScore()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.saveStudyScore()(educatorFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 BadRequest if a form data invalid") {
      val response: Future[Result] = controller.saveStudyScore()(studentFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if a student") {
      val response: Future[Result] = controller.saveStudyScore()(studentFakeRequest
        .withJsonBody(Json.toJson(scoreDto)))
      val extractedScore = contentAsJson(response).validate[ScoreDto]
      database.deleteScoreData(extractedScore.get)
      status(response) should be(OK)
    }

    it("should return 500 Internal server error if a duplicate studyScore is provided") {
      val response: Future[Result] = controller.saveStudyScore()(studentFakeRequest
        .withJsonBody(Json.toJson(scoreDto)))
      val extractedScore = contentAsJson(response).validate[ScoreDto]
      val duplicate: Future[Result] = controller.saveStudyScore()(studentFakeRequest
        .withJsonBody(Json.toJson(scoreDto)))
      status(response) should be(OK)
      status(duplicate) should be(INTERNAL_SERVER_ERROR)
    }

    it("should return json") {
      val response: Future[Result] = controller.saveStudyScore()(studentFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.saveStudyScore()(studentFakeRequest)
      contentAsString(response).length should be > 0
    }

    it("should increment the streak count if score is 50 or more") {
      val response: Future[Result] = controller.saveStudyScore()(studentFakeRequest
        .withJsonBody(Json.toJson(scoreDto)))
      val extractedScore = contentAsJson(response).validate[ScoreDto]
      database.deleteScoreData(extractedScore.get)
      status(response) should be(OK)
      extractedScore.isSuccess should be(true)
      extractedScore.get.streak should be(scoreDto.streak + 1)
    }

    it("should zero the streak count if score is below 50") {
      val response: Future[Result] = controller.saveStudyScore()(studentFakeRequest
        .withJsonBody(Json.toJson(scoreDto.copy(score = 49))))
      val extractedScore = contentAsJson(response).validate[ScoreDto]
      database.deleteScoreData(extractedScore.get)
      status(response) should be(OK)
      extractedScore.isSuccess should be(true)
      extractedScore.get.streak should be(0)
    }
  }

  describe("getContentAssignedStatus") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getContentAssignedStatus()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getContentAssignedStatus()(educatorFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if a student") {
      val response: Future[Result] = controller.getContentAssignedStatus()(studentFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getContentAssignedStatus()(studentFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.getContentAssignedStatus()(studentFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

  describe("disableContent(assignedId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.disableContent(assigned1Id)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.disableContent(assigned1Id)(educatorFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if a student") {
      val response: Future[Result] = controller.disableContent(assigned1Id)(studentFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.disableContent(assigned1Id)(studentFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.disableContent(assigned1Id)(studentFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

  describe("enableContent(assignedId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.enableContent(assigned1Id)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.enableContent(assigned1Id)(educatorFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if a student") {
      val response: Future[Result] = controller.enableContent(assigned1Id)(studentFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.enableContent(assigned1Id)(studentFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.enableContent(assigned1Id)(studentFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

  describe("getHistoricalPerformanceByExam") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getHistoricalPerformanceByExam()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getHistoricalPerformanceByExam()(educatorFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if a student") {
      val response: Future[Result] = controller.getHistoricalPerformanceByExam()(studentFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getHistoricalPerformanceByExam()(studentFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.getHistoricalPerformanceByExam()(studentFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

}
