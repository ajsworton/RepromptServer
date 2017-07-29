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

import java.nio.file.{ Path, Paths }
import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import env.JWTEnv
import guards.AuthEducator
import libraries.DaoOnDtoAction
import models.dao.{ ContentFolderDao, ContentItemDao, ContentPackageDao }
import models.dto.{ ContentFolderDto, ContentItemDto, ContentPackageDto, Dto }
import scala.concurrent.duration._
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, MultipartFormData, Result }
import responses.JsonErrorResponse

import scala.concurrent.{ Await, ExecutionContext, Future }
import java.io._

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
          val uploadedFiles = request.request.body.asMultipartFormData
          val userId = request.identity.id.get
          SaveDataWithFile(formData, uploadedFiles, userId)
        }
      )
  }

  def SaveDataWithFile(
    contentItem: ContentItemDto,
    uploadedFiles: Option[MultipartFormData[Files.TemporaryFile]],
    userId: Int): Future[Result] = {

    if (uploadedFiles.isEmpty) {
      daoHelper.validateAndSaveDto[ContentItemDto](itemDao, contentItem)
    } else {
      if (contentItem.id.isDefined) {
        val url = storeImage(uploadedFiles.get, userId, contentItem.id.get)
        writeBackImageUrl(contentItem, url, contentItem.id.get)
      } else {
        val futurePath = for {
          item <- itemDao.save(contentItem)
          path = if (item.isEmpty) None else {
            storeImage(uploadedFiles.get, userId, item.get.id.get)
          }
        } yield (path, item.get.id.get)
        val path = Await.result(futurePath, 10 seconds)
        if (path._1.isEmpty) Future(Ok(Json.toJson(JsonErrorResponse(""))))
        else writeBackImageUrl(contentItem, path._1, path._2)
      }
    }
  }

  def writeBackImageUrl(item: ContentItemDto, newUrl: Option[String], itemId: Int) = {
    val newData = item.copy(id = Some(itemId), imageUrl = newUrl)
    daoHelper.validateAndSaveDto[ContentItemDto](itemDao, newData)
  }

  def ensurePathExists(path: Path) = {
    java.nio.file.Files.createDirectories(path)
  }

  def pathToUrl(filePath: Path): String = {
    val split = filePath.toString.replace('\\', '/').split('/')
    val dropHead = split.tail
    val joined = dropHead.mkString("/")
    joined
  }

  def storeImage(uploadedFiles: MultipartFormData[Files.TemporaryFile], userId: Int,
    itemId: Int): Option[String] = {

    val file = uploadedFiles.file("image")
    if (file.isDefined) {
      val f = file.get.ref
      val filename = getFilename(file.get.filename, itemId)
      val filePath = Paths.get(s"public/media/$userId/content/items/$filename")
      ensurePathExists(filePath.getParent)
      f.moveTo(filePath, replace = true)
      Some(pathToUrl(filePath))
    } else {
      None
    }
  }

  def getFilename(fileName: String, itemId: Int): String = {
    val suffix = fileName.split('.').reverse.head
    if (suffix.isEmpty) itemId.toString else s"${itemId.toString}.$suffix"
  }

  def deleteItem(itemId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator())
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        //delete package with id
        itemDao.delete(itemId) flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
    }

}