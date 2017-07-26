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

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import env.JWTEnv
import guards.AuthEducator
import libraries.DaoOnDtoAction
import models.dao.{ContentFolderDao, ContentPackageDao}
import models.dto.{ContentFolderDto, ContentPackageDto, Dto}
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result}
import responses.JsonErrorResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContentController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  folderDao: ContentFolderDao,
  packageDao: ContentPackageDao,
  environment: Environment)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val daoHelper = new DaoOnDtoAction

  def getAllFoldersByCurrentUser: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
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

  def getFolder(folderId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator())
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        val result = folderDao.find(folderId)
        result flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
    }

  def saveFolder: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentFolderDto.contentFolderForm.bindFromRequest.fold(
        formError => Future(Ok(Json.toJson(formError.errorsAsJson))),
        formData => daoHelper.validateAndSaveDto[ContentFolderDto](folderDao, formData)
      )
  }

  def savePackage: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentPackageDto.ContentPackageForm.bindFromRequest.fold(
        formError => Future(Ok(Json.toJson(formError.errorsAsJson))),
        formData => daoHelper.validateAndSaveDto[ContentPackageDto](packageDao, formData)
      )
  }

  def deletePackage

  def delete(folderId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      //delete cohort with id
      folderDao.delete(folderId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

}
