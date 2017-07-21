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
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import env.JWTEnv
import guards.AuthEducator
import models.dao.CohortDao
import models.dto.{ CohortDto, CohortMemberDto }
import models.services.{ AuthTokenService, UserService }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result }
import play.libs.ws.WSClient
import responses.JsonErrorResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CohortController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  userService: UserService,
  cohortDao: CohortDao,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasher: PasswordHasher,
  ws: WSClient,
  environment: Environment,
  mailerClient: MailerClient)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  /**
   * Using AuthEducator guard
   * @return
   */
  def getAllByOwner(ownerId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val results = cohortDao.findByOwner(ownerId)
      results flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def get(cohortId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val result = cohortDao.find(cohortId)
      result flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def getAllByCurrentUser: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val user = request.identity
      if (user.id.isDefined) {
        val results = cohortDao.findByOwner(user.id.get)
        results flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      } else {
        Future(Ok(Json.toJson(JsonErrorResponse("Authentication error"))))
      }
  }

  def save: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      CohortDto.cohortForm.bindFromRequest.fold(
        formError => Future(Ok(Json.toJson(formError.errorsAsJson))),
        formData => {
          //check if exists
          if (formData.id.isDefined) {
            val existing = cohortDao.find(formData.id.get)
            existing flatMap {
              case None => saveCohort(formData)
              case Some(_) => updateCohort(formData)
            }
          } else {
            // save
            saveCohort(formData)
          }
        }
      )
  }

  def attach: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      CohortMemberDto.cohortMemberForm.bindFromRequest.fold(
        formError => Future(Ok(Json.toJson(formError.errorsAsJson))),
        formData => {
          if (formData.CohortId.isDefined && formData.UserId.isDefined) {
            saveCohortMember(formData.CohortId.get, formData.UserId.get)
          } else {
            Future(Ok(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
          }
        }
      )
  }

  def detach(cohortId: Int, userId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      if (cohortId > 0 && userId > 0) {
        deleteCohortMember(cohortId, userId)
      } else {
        Future(Ok(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
      }
  }

  def getWithUsers: Action[AnyContent] = Action async {
    val cohortId = 16
    val results = cohortDao.findByOwner(cohortId)
    results flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  private def saveCohort(cohort: CohortDto): Future[Result] = {
    val saveResponse = cohortDao.save(cohort)
    saveResponse flatMap {
      r => Future(Ok(Json.toJson(r.get)))
    }
  }

  private def saveCohortMember(cohortId: Int, userId: Int): Future[Result] = {
    val saveResponse = cohortDao.attach(cohortId, userId)
    saveResponse flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  private def deleteCohortMember(cohortId: Int, userId: Int): Future[Result] = {
    val deleteResponse = cohortDao.detach(cohortId, userId)
    deleteResponse flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  private def updateCohort(cohort: CohortDto): Future[Result] = {
    val updateResponse = cohortDao.update(cohort)
    updateResponse flatMap {
      r => Future(Ok(Json.toJson(r.get)))
    }
  }

  def delete(cohortId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      //delete cohort with id
      cohortDao.delete(cohortId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

}
