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

import akka.stream.Materializer
import libs.{AppFactory, AuthHelper, TestingDbQueries}
import models.dto.{AnswerDto, ContentItemDto, ContentPackageDto, QuestionDto}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFunSpec, BeforeAndAfter, Matchers}
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{AnyContentAsEmpty, MultipartFormData, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.Files.TemporaryFileCreator

import scala.concurrent.Future

class PackageControllerSpec extends AsyncFunSpec with Matchers with MockitoSugar with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val fileCreator = fakeApplication().injector.instanceOf[TemporaryFileCreator]
  implicit val mat: Materializer = fakeApplication().injector.instanceOf[Materializer]
  val controller: PackageController = fakeApplication().injector.instanceOf[PackageController]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]

  val teacherId, contentItem1Id, assigned1Id, packageId, itemId, questionId = 987895
  val studentId, cohortId, packageId2, contentFolderId = 987896

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  //multipart data
  //val file = new File("file")
  val file: TemporaryFile = fileCreator.create("image", "jpg")
  val filePart = FilePart[TemporaryFile](key = "image", filename = "fileName.jpg", contentType = None, ref = file)

  val fakePackage = ContentPackageDto(id = None, folderId = contentFolderId, ownerId = teacherId,
    name = "splablopghi")

  val fakeContentItem = ContentItemDto(id = None, packageId = packageId, imageUrl = Some("url"),
    name = "ContentItemName", content = "Content here")

  val fakeAnswer = AnswerDto(id = None, questionId = None, answer = "ans", correct = true)
  val fakeQuestion = QuestionDto(id = None, question = "q", format = "Sort", itemId = itemId)


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
      assigned.isSuccess should be(true)
      assigned.get.name should be("Package Name")
    }
  }

  describe("getAllByCurrentUser") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getAllByCurrentUser()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getAllByCurrentUser()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getAllByCurrentUser()(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getAllByCurrentUser()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.getAllByCurrentUser()(educatorFakeRequest)
      contentAsString(response).length should be > 0
      val assigned = contentAsJson(response).validate[List[ContentPackageDto]]
      assigned.isSuccess should be(true)
      assigned.get.exists(p => p.name == "Package Name") should be(true)
    }
  }

  describe("savePackage") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.savePackage()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.savePackage()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad request if an educator but invalid post data") {
      val response: Future[Result] = controller.savePackage()(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.savePackage()(educatorFakeRequest
        .withJsonBody(Json.toJson(fakePackage)))
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.savePackage()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.savePackage()(educatorFakeRequest
        .withJsonBody(Json.toJson(fakePackage)))
      contentAsString(response).length should be > 0
      val saved = contentAsJson(response).validate[ContentPackageDto]
      saved.isSuccess should be(true)
      saved.get.id.isDefined should be(true)
      saved.get.name should be(fakePackage.name)
      saved.get.folderId should be(fakePackage.folderId)
      saved.get.ownerId should be(fakePackage.ownerId)
    }
  }

  describe("deletePackage(packageId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.deletePackage(packageId)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.deletePackage(packageId)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad request if an educator but invalid id") {
      val response: Future[Result] = controller.deletePackage(0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.deletePackage(packageId)(educatorFakeRequest
        .withJsonBody(Json.toJson(fakePackage)))
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.deletePackage(packageId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.deletePackage(packageId)(educatorFakeRequest)
      contentAsString(response).length should be > 0
      val saved = contentAsString(response)
      saved.contains("1") should be(true)
    }
  }

  describe("getItem(itemId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getItem(itemId)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getItem(itemId)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but itemId is invalid") {
      val response: Future[Result] = controller.getItem(0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getItem(itemId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getItem(itemId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.getItem(itemId)(educatorFakeRequest)
      contentAsString(response).length should be > 0
      val retrieved = contentAsJson(response).validate[ContentItemDto]
      retrieved.isSuccess should be(true)
      retrieved.get.imageUrl should be(Some("imageUrl"))
    }
  }

  describe("saveItem") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.saveItem()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.saveItem()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but post data is invalid") {
      val response: Future[Result] = controller.saveItem()(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.saveItem()(educatorFakeRequest
        withJsonBody Json.toJson(fakeContentItem))
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.saveItem()(educatorFakeRequest
        withJsonBody Json.toJson(fakeContentItem))
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.saveItem()(educatorFakeRequest
        withJsonBody Json.toJson(fakeContentItem))
      contentAsString(response).length should be > 0
      val saved = contentAsJson(response).validate[ContentItemDto]
      saved.isSuccess should be(true)
      saved.get.imageUrl should be(fakeContentItem.imageUrl)
      saved.get.name should be(fakeContentItem.name)
      saved.get.packageId should be(fakeContentItem.packageId)
      saved.get.content should be(fakeContentItem.content)
    }

    it("should correctly handle a multipart image") {

      val params: Map[String, Seq[String]] = Map("name" -> Seq("Name"),
        "content" -> Seq("Content"), "packageId" -> Seq(fakeContentItem.packageId.toString),
      )

      val fakeMultipartRequest = educatorFakeRequest.withMethod("POST")
        .withMultipartFormDataBody(MultipartFormData[TemporaryFile](dataParts = params, files = Seq(filePart),  badParts = Nil))

      val response = controller.saveItem()(fakeMultipartRequest)

      val content = contentAsString(response)
      status(response) should be(OK)
      content.length should be > 0

      val saved = contentAsJson(response).validate[ContentItemDto]
      saved.isSuccess should be(true)
      controller.deleteItem(saved.get.id.get)(educatorFakeRequest)
      saved.get.name.toString should be(params("name").head)
      saved.get.content.toString should be(params("content").head)
      saved.get.packageId.toString should be(params("packageId").head)
    }
  }

  describe("deleteItem(itemId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.deleteItem(0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.deleteItem(0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but id is invalid") {
      val response: Future[Result] = controller.deleteItem(0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.deleteItem(itemId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.deleteItem(itemId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.deleteItem(itemId)(educatorFakeRequest)
      val deleted = contentAsString(response)
      deleted.length should be > 0
      deleted.contains("1") should be(true)
    }
  }

  describe("getQuestion(questionId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getQuestion(0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.getQuestion(0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but itemId is invalid") {
      val response: Future[Result] = controller.getQuestion(0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getQuestion(questionId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getQuestion(questionId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.getQuestion(questionId)(educatorFakeRequest)
      contentAsString(response).length should be > 0
      val retrieved = contentAsJson(response).validate[QuestionDto]
      retrieved.isSuccess should be(true)
      retrieved.get.format should be("MCSA")
      retrieved.get.question should be("question")
    }
  }

  describe("saveQuestion") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.saveQuestion()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.saveQuestion()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but post data is invalid") {
      val response: Future[Result] = controller.saveQuestion()(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.saveQuestion()(educatorFakeRequest
        withJsonBody Json.toJson(fakeQuestion))
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.saveQuestion()(educatorFakeRequest
        withJsonBody Json.toJson(fakeQuestion))
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.saveQuestion()(educatorFakeRequest
        withJsonBody Json.toJson(fakeQuestion))
      contentAsString(response).length should be > 0
      val saved = contentAsJson(response).validate[QuestionDto]
      saved.isSuccess should be(true)
      saved.get.question should be(fakeQuestion.question)
      saved.get.format should be(fakeQuestion.format)
      saved.get.itemId should be(fakeQuestion.itemId)
    }

    it("should update an existing question") {
      val existingRequest = controller.getQuestion(questionId)(educatorFakeRequest)
      val existing = contentAsJson(existingRequest).validate[QuestionDto]
      existing.isSuccess should be(true)
      val fakeAnswer = AnswerDto(id = None, questionId = existing.get.id, answer = "ans", correct = true)
      val updatedQuestion = existing.get.copy(answers = Some(List(fakeAnswer)))
      val saveRequest = controller.saveQuestion()(educatorFakeRequest
        withJsonBody Json.toJson(updatedQuestion))
      val updated = contentAsJson(saveRequest).validate[QuestionDto]
      updated.isSuccess should be(true)
      updated.get.question should be(existing.get.question)
      updated.get.format should be(existing.get.format)
      updated.get.itemId should be(existing.get.itemId)
      updated.get.answers.isDefined should be(true)
      updated.get.answers.get.size should be(existing.get.answers.get.size + 1)
    }

    it("should save a new question with answers") {
      val newQuestion = fakeQuestion.copy(answers = Some(List(fakeAnswer)))
      val saveRequest = controller.saveQuestion()(educatorFakeRequest
        withJsonBody Json.toJson(newQuestion))
      val updated = contentAsJson(saveRequest).validate[QuestionDto]
      updated.isSuccess should be(true)
      updated.get.question should be(newQuestion.question)
      updated.get.format should be(newQuestion.format)
      updated.get.itemId should be(newQuestion.itemId)
      updated.get.answers.isDefined should be(true)
      updated.get.answers.get.size should be(newQuestion.answers.get.size)
      updated.get.answers.get.head.questionId should be(updated.get.id)
    }

  }

  describe("deleteQuestion(questionId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.deleteQuestion(0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.deleteQuestion(0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but id is invalid") {
      val response: Future[Result] = controller.deleteQuestion(0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.deleteQuestion(questionId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.deleteQuestion(questionId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.deleteQuestion(questionId)(educatorFakeRequest)
      val deleted = contentAsString(response)
      deleted.length should be > 0
      deleted.contains("1") should be(true)
    }
  }

  describe("deleteAnswer(answerId: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.deleteAnswer(0)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not a student") {
      val response: Future[Result] = controller.deleteAnswer(0)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 400 Bad Request if an educator, but id is invalid") {
      val response: Future[Result] = controller.deleteAnswer(0)(educatorFakeRequest)
      status(response) should be(BAD_REQUEST)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.deleteAnswer(questionId)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.deleteAnswer(questionId)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return the correct content") {
      val response: Future[Result] = controller.deleteAnswer(questionId)(educatorFakeRequest)
      val deleted = contentAsString(response)
      deleted.length should be > 0
      deleted.contains("1") should be(true)
    }
  }

}
