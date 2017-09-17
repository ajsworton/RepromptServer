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
import libraries.DaoOnDtoAction
import models.dao.{ ContentFolderDao, ContentPackageDao }
import models.dto.{ ContentFolderDto, ContentPackageDto, Dto }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result, Results }
import responses.JsonErrorResponse

import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles Content requests.
 * @param cc injected controller components for the extended abstract controller
 * @param silhouette injected authentication library
 * @param folderDao injected model
 * @param packageDao injected model
 * @param ec injected execution context to execute futures
 */
@Singleton
class ContentController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  folderDao: ContentFolderDao,
  packageDao: ContentPackageDao)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val daoHelper = new DaoOnDtoAction

  /**
   * Endpoint to retrieve all folders by the current user
   * @return JSON list of users
   */
  def getAllFoldersByCurrentUser: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val user = request.identity
      if (user.id.isDefined) {
        val results = folderDao.findByOwner(user.id.get)
        results flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      } else {
        Future(Ok(Json.toJson(JsonErrorResponse("Authentication error"))))
      }
  }

  /**
   * Endpoint to retrieve a folder by id
   * @param folderId the supplied folder id
   * @return an optional folder
   */
  def getFolder(folderId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (folderId < 1) { Future(BadRequest(Json.toJson("Invalid Id"))) } else {
        val result = folderDao.find(folderId)
        result flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  /**
   * Endpoint to save a supplied folder
   * @return the saved folder with id
   */
  def saveFolder: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentFolderDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => daoHelper.validateAndSaveDto[ContentFolderDto](folderDao, formData)
      )
  }

  /**
   * Endpoint to delete a folder by id
   * @param folderId the supplied id
   * @return a number representing the number of affected rows
   */
  def deleteFolder(folderId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      //delete cohort with id
      folderDao.delete(folderId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

}
