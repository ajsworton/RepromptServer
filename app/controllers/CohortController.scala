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
import models.dao.CohortDao
import models.dto.{ CohortDto, CohortMemberDto }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result, Results }
import responses.JsonErrorResponse

import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles authentication requests.
 * @param cc injected controller components for the extended abstract controller
 * @param silhouette injected authentication library
 * @param cohortDao injected model
 * @param ec injected execution context to execute futures
 */
@Singleton
class CohortController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  cohortDao: CohortDao
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) with I18nSupport {

  val daoHelper = new DaoOnDtoAction

  /**
   * Endpoint to Get all cohortDtos by supplied ownerId.
   * @param ownerId the supplied id
   * @return the matching CohortDto
   */
  def getAllByOwner(ownerId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val results = cohortDao.findByOwner(ownerId)
      results flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  /**
   * Endpoint to Get a specific CohortDto by id.
   * @param cohortId the supplied id
   * @return the matching CohortDto
   */
  def get(cohortId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val result = cohortDao.find(cohortId)
      result flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  /**
   * Endpoint to Get all cohorts owned by current user.
   * @return List[CohortDto]
   */
  def getAllByCurrentUser: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val user = request.identity
      val results = cohortDao.findByOwner(user.id.get)
      results flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  /**
   * Endpoint to Save a cohortDto supplied by POST.
   * @return saved CohortDto
   */
  def save: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      CohortDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          //check if exists
          if (formData.id.isDefined) {
            val existing = cohortDao.find(formData.id.get)
            existing flatMap {
              case None => daoHelper.saveDto(cohortDao, formData)
              case Some(_) => daoHelper.updateDto(cohortDao, formData)
            }
          } else {
            // save
            daoHelper.saveDto(cohortDao, formData)
          }
        }
      )
  }

  /**
   * Endpoint to delete a cohort by id.
   * @param cohortId supplied cohort Id
   * @return a number representing the number of affected rows.
   */
  def delete(cohortId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      //delete cohort with id
      cohortDao.delete(cohortId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  /**
   * Endpoint to attach a user and cohort.
   * @return a number representing the number of affected rows.
   */
  def attach: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      CohortMemberDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          if (formData.cohortId.isDefined && formData.userId.isDefined &&
            formData.cohortId.get > 0 && formData.userId.get > 0) {
            saveCohortMember(formData.cohortId.get, formData.userId.get)
          } else {
            Future(BadRequest(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
          }
        }
      )
  }

  /**
   * Endpoint to detach a user from a cohort
   * @param cohortId the cohort identifier
   * @param userId the user identifier
   * @return a number representing the number of affected rows.
   */
  def detach(cohortId: Int, userId: Int): Action[AnyContent] =
    silhouette.SecuredAction(AuthEducator).async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        if (cohortId > 0 && userId > 0) {
          deleteCohortMember(cohortId, userId)
        } else {
          Future(BadRequest(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
        }
    }

  /**
   * Helper function to persist a cohort member
   * @param cohortId the cohort identifier
   * @param userId the user identifier
   * @return a future result
   */
  private def saveCohortMember(cohortId: Int, userId: Int): Future[Result] = {
    val saveResponse = cohortDao.attach(cohortId, userId)
    saveResponse flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  /**
   * Helper function to remove a cohort member
   * @param cohortId the cohort identifier
   * @param userId the cohort identifier
   * @return a future result
   */
  private def deleteCohortMember(cohortId: Int, userId: Int): Future[Result] = {
    val deleteResponse = cohortDao.detach(cohortId, userId)
    deleteResponse flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

}
