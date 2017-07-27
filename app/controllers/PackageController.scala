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

import java.nio.file.Paths
import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import env.JWTEnv
import guards.AuthEducator
import libraries.DaoOnDtoAction
import models.dao.{ ContentFolderDao, ContentItemDao, ContentPackageDao }
import models.dto.{ ContentFolderDto, ContentItemDto, ContentPackageDto, Dto }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result }
import responses.JsonErrorResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class PackageController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  packageDao: ContentPackageDao,
  itemDao: ContentItemDao,
  environment: Environment)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val daoHelper = new DaoOnDtoAction

  def getPackage(packageId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val result = packageDao.find(packageId)
      result flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def savePackage: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentPackageDto.ContentPackageForm.bindFromRequest.fold(
        formError => Future(Ok(Json.toJson(formError.errorsAsJson))),
        formData => daoHelper.validateAndSaveDto[ContentPackageDto](packageDao, formData)
      )
  }

  def deletePackage(packageId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      //delete package with id
      packageDao.delete(packageId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def getItem(itemId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val result = itemDao.find(itemId)
      result flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def saveItem: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentItemDto.ContentItemForm.bindFromRequest.fold(
        formError => Future(Ok(Json.toJson(formError.errorsAsJson))),
        formData => {
          //handle file
          val file = request.request.body.asMultipartFormData.get.file("image")
          val userId = request.identity.id.get
          if (file.isDefined) {
            val f = file.get.ref
            val filename = file.get.filename
            val path = Paths.get(s"/media/$userId/$filename")
            f.moveTo(path, replace = true)
            val newData = formData.copy(imageUrl = Some(path.toString))
            daoHelper.validateAndSaveDto[ContentItemDto](itemDao, newData)
          } else {
            daoHelper.validateAndSaveDto[ContentItemDto](itemDao, formData)
          }
        }
      )
  }

  //  request.body.file("picture").map { picture =>
  //    val filename = picture.filename
  //    val contentType = picture.contentType
  //    picture.ref.moveTo(Paths.get(s"/tmp/picture/$filename"), replace = true)
  //    Ok("File uploaded")
  //  }.getOrElse {
  //    Redirect(routes.ScalaFileUploadController.index).flashing(
  //      "error" -> "Missing file")
  //  }

  def deleteItem(itemId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator())
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        //delete package with id
        itemDao.delete(itemId) flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
    }

}