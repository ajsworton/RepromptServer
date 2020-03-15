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

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import env.JWTEnv
import guards.AuthEducator
import libraries.{ DaoOnDtoAction, FileHelper }
import models.dao.{ ContentItemDao, ContentPackageDao }
import models.dto.{ AnswerDto, ContentItemDto, ContentPackageDto, Dto, QuestionDto }

import scala.concurrent.duration._
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, MultipartFormData, Result, Results }
import responses.JsonErrorResponse

import scala.concurrent.{ Await, ExecutionContext, Future }

/**
 * This controller handles package requests.
 * @param cc injected controller components for the extended abstract controller
 * @param silhouette injected authentication library
 * @param packageDao injected model
 * @param itemDao injected model
 * @param fileHelper injected helper
 * @param daoHelper injected helper
 * @param ec injected execution context to execute futures
 */
@Singleton
class PackageController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  packageDao: ContentPackageDao,
  itemDao: ContentItemDao,
  fileHelper: FileHelper,
  daoHelper: DaoOnDtoAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) with I18nSupport {

  /**
   * Endpoint to retrieve a package by Id.
   * @param packageId the supplied id
   * @return an optional package
   */
  def getPackage(packageId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      packageDao.find(packageId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  /**
   * Endpoint to retrieve all packages associated with the current user.
   * @return a list of packages
   */
  def getAllByCurrentUser: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val user = request.identity
      val results = packageDao.findByOwner(user.id.get)
      results flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  /**
   * Endpoint to save a supplied package via POST.
   * @return the package including Id
   */
  def savePackage: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentPackageDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => daoHelper.validateAndSaveDto[ContentPackageDto](packageDao, formData)
      )
  }

  /**
   * Endpoint to delete a package by supplied Id.
   * @param packageId the supplied Id
   * @return a number representing the number of affected rows
   */
  def deletePackage(packageId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (packageId < 1) { Future(BadRequest(Json.toJson("Invalid Id"))) } else {
        //delete media folder
        fileHelper.deletePackageFolderIfExist(packageId, request.identity.id.get)

        //delete package with id
        packageDao.delete(packageId) flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  /**
   * Endpoint to get a content item by Id.
   * @param itemId supplied id
   * @return an optional item
   */
  def getItem(itemId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (itemId < 1) { Future(BadRequest(Json.toJson("Invalid Id"))) } else {
        val result = itemDao.find(itemId)
        result flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  /**
   * Endpoint to save a supplied content item.
   * @return the saved item with id
   */
  def saveItem: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentItemDto.form.bindFromRequest.fold(
        formError =>
          Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          val uploadedFiles = request.body.asMultipartFormData
          val userId = request.identity.id.get
          SaveDataWithFile(formData, uploadedFiles, userId)
        }
      )
  }

  /**
   * Endpoint to delete an item by id.
   * @param itemId the supplied id
   * @return a number representing the number of affected rows
   */
  def deleteItem(itemId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (itemId < 1) { Future(BadRequest(Json.toJson("Invalid Id"))) } else {
        itemDao.find(itemId) flatMap {
          case None => Future(Ok(Json.toJson(0)))
          case Some(item) =>
            fileHelper.deleteItemImagesIfExist(item, request.identity.id.get)
            deleteContentItemById(itemId)
        }
      }
  }

  /**
   * Helper to delete a content item by id.
   * @param itemId the supplied id
   * @return a number representing the number of affected rows
   */
  private def deleteContentItemById(itemId: Int): Future[Result] = {
    itemDao.delete(itemId) flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  /**
   * Endpoint to retrieve a question by Id.
   * @param questionId the supplied id
   * @return an optional question
   */
  def getQuestion(questionId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (questionId < 1) { Future(BadRequest(Json.toJson("Invalid Id"))) } else {
        val result = itemDao.findQuestion(questionId)
        result flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  /**
   * Endpoint to save a supplied question.
   * @return the saved question with id
   */
  def saveQuestion: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      QuestionDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          if (formData.id.isEmpty) saveQuestionData(formData)
          else updateQuestionData(formData)
        }
      )
  }

  /**
   * Endpoint to delete a question by id.
   * @param questionId the supplied id
   * @return
   */
  def deleteQuestion(questionId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (questionId < 1) { Future(BadRequest(Json.toJson("Invalid Id"))) } else {
        itemDao.deleteQuestion(questionId) flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  /**
   * Endpoint to delete an answer by id.
   * @param answerId the supplied id
   * @return a number representing the number of affected rows
   */
  def deleteAnswer(answerId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (answerId < 1) { Future(BadRequest(Json.toJson("Invalid Id"))) } else {
        itemDao.deleteAnswer(answerId) flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  /**
   * Helper to save a supplied question.
   * @param data the supplied question
   * @return a future result
   */
  private def saveQuestionData(data: QuestionDto): Future[Result] = {
    val qResponse = itemDao.saveQuestion(data)
    if (data.answers.isEmpty) {
      qResponse flatMap {
        r => Future(Results.Ok(Json.toJson(r.get)))
      }
    } else {
      qResponse flatMap {
        question =>
          {
            if (question.isDefined) {
              val answerList = data.answers.get.map(ans => ans.copy(questionId = question.get.id))
              val responses: Iterable[Future[Option[AnswerDto]]] = for {
                answer <- answerList
                r = itemDao.saveAnswer(answer)
              } yield r

              Future.sequence(responses) flatMap {
                _ =>
                  itemDao.findQuestion(question.get.id.get) flatMap {
                    r => Future(Results.Ok(Json.toJson(r.get)))
                  }
              }
            } else Future(Results.InternalServerError("Failed to write to database"))
          }
      }

    }
  }

  /**
   * Helper to update a question.
   * @param data the supplied question
   * @return a future result
   */
  private def updateQuestionData(data: QuestionDto): Future[Result] = {
    val qResponse = itemDao.updateQuestion(data)
    if (data.answers.isEmpty) {
      qResponse flatMap {
        r => Future(Results.Ok(Json.toJson(r.get)))
      }
    } else {

      qResponse flatMap {
        question =>
          {
            if (question.isDefined) {
              val answerList = data.answers.get.map(ans => ans.copy(questionId = question.get.id))
              val responses: List[Future[Option[AnswerDto]]] = for {
                answer <- answerList
                r: Future[Option[AnswerDto]] = if (answer.id.isEmpty) itemDao.saveAnswer(answer)
                else itemDao.updateAnswer(answer)
              } yield r

              Future.sequence(responses) flatMap {
                _ =>
                  itemDao.findQuestion(question.get.id.get) flatMap {
                    r => Future(Results.Ok(Json.toJson(r.get)))
                  }
              }
            } else Future(Results.InternalServerError("Failed to write to database"))
          }
      }
    }

  }

  /**
   * Helper to save a content item with an uploaded file.
   * @param contentItem the supplied item
   * @param uploadedFiles the supplied file
   * @param userId the supplied userId
   * @return a future result
   */
  private def SaveDataWithFile(
    contentItem: ContentItemDto,
    uploadedFiles: Option[MultipartFormData[Files.TemporaryFile]],
    userId: Int
  ): Future[Result] = {

    if (uploadedFiles.isEmpty || uploadedFiles.get.file("image").isEmpty) {
      daoHelper.validateAndSaveDto[ContentItemDto](itemDao, contentItem)
    } else {
      if (contentItem.id.isDefined) {
        val url = storeImage(uploadedFiles.get, userId, contentItem)
        writeBackImageUrl(contentItem, url, contentItem.id.get)
      } else {
        val futurePath = for {
          item <- itemDao.save(contentItem)
          path = if (item.isEmpty) None else {
            storeImage(uploadedFiles.get, userId, item.get)
          }
        } yield (path, item.get.id.get)
        val path = Await.result(futurePath, 10 seconds)
        if (path._1.isEmpty) Future(Ok(Json.toJson(JsonErrorResponse(""))))
        else writeBackImageUrl(contentItem, path._1, path._2)
      }
    }
  }

  /**
   * Helper to write the url of a saved image back to the associated content item.
   * @param item the supplied item
   * @param newUrl the supplied newUrl
   * @param itemId the supplied item id
   * @return a future result
   */
  private def writeBackImageUrl(item: ContentItemDto, newUrl: Option[String], itemId: Int): Future[Result] = {
    val newData = item.copy(id = Some(itemId), imageUrl = newUrl)
    daoHelper.validateAndSaveDto[ContentItemDto](itemDao, newData)
  }

  /**
   * Helper to store an uploaded image.
   * @param uploadedFiles the uploaded image file
   * @param userId the supplied user id
   * @param item the associated item
   * @return a future result
   */
  private def storeImage(uploadedFiles: MultipartFormData[Files.TemporaryFile], userId: Int,
    item: ContentItemDto): Option[String] = {
    //remove existing images
    fileHelper.deleteItemImagesIfExist(item, userId)
    val file = uploadedFiles.file("image")
    if (file.isDefined) {
      val filePath = fileHelper.saveImage(file.get.ref, file.get.filename, item, userId)
      Some(fileHelper.pathToUrl(filePath))
    } else {
      None
    }
  }

}
