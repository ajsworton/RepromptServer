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
import models.dao.{ ContentAssignedDao, ContentItemDao }
import models.dto.{ CohortDto, ContentAssignedCohortDto, ContentAssignedDto, ContentAssignedPackageDto, ContentPackageDto }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result, Results }
import responses.JsonErrorResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class PublishedController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  publishDao: ContentAssignedDao,
  itemDao: ContentItemDao,
  environment: Environment)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def get(id: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val result = publishDao.find(id)
      result flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def getAllPublishedByCurrentUser: Action[AnyContent] = silhouette.SecuredAction(AuthEducator)
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        val user = request.identity
        val results = publishDao.findByOwner(user.id.get)
        results flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
    }

  def savePublishedExam: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentAssignedDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => saveOrUpdateExam(formData.copy(ownerId = request.identity.id))
      )
  }

  def deletePublishedExam(assignedId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      publishDao.delete(assignedId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def attachCohort: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentAssignedCohortDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          if (formData.cohortId.isDefined && formData.cohortId.get > 0
            && formData.assignedId.isDefined && formData.assignedId.get > 0) {
            handleAttachDetachResult(publishDao.attachCohort(
              formData.assignedId.get,
              formData.cohortId.get))
          } else {
            Future(Results.BadRequest(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
          }
        }
      )
  }

  def detachCohort(cohortId: Int, assignedId: Int): Action[AnyContent] =
    silhouette.SecuredAction(AuthEducator).async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        if (cohortId > 0 && assignedId > 0) {
          handleAttachDetachResult(publishDao.detachCohort(assignedId, cohortId))
        } else {
          Future(Ok(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
        }
    }

  def attachPackage: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentAssignedPackageDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          if (formData.packageId.isDefined && formData.packageId.get > 0
            && formData.assignedId.isDefined && formData.assignedId.get > 0) {
            handleAttachDetachResult(publishDao.attachCohort(
              formData.assignedId.get,
              formData.packageId.get))
          } else {
            Future(Results.BadRequest(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
          }
        }
      )
  }

  def detachPackage(packageId: Int, assignedId: Int): Action[AnyContent] =
    silhouette.SecuredAction(AuthEducator).async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        if (packageId > 0 && assignedId > 0) {
          handleAttachDetachResult(publishDao.detachPackage(assignedId, packageId))
        } else {
          Future(Ok(Json.toJson(JsonErrorResponse("CohortId and UserId must be defined"))))
        }
    }

  private def handleAttachDetachResult(eventualInt: Future[Either[String, Int]]): Future[Result] =
    eventualInt flatMap {
      case Left(msg) => Future(Results.InternalServerError(msg))
      case Right(number: Int) => Future(Ok(Json.toJson(number)))
    }

  private def saveOrUpdateExam(assigned: ContentAssignedDto): Future[Result] = {
    assigned.id match {
      case None => saveExam(assigned)
      case Some(id) => updateExam(assigned, id)
    }
  }

  private def saveExam(assigned: ContentAssignedDto): Future[Result] = {
    val savedAssigned = publishDao.save(assigned)
    handleCohortAndPackageInsertion(savedAssigned, assigned)
  }

  private def updateExam(assigned: ContentAssignedDto, id: Int): Future[Result] = {
    val updatedAssigned = publishDao.update(assigned)
    handleCohortAndPackageInsertion(updatedAssigned, assigned)
  }

  private def handleCohortAndPackageInsertion(
    savedAssigned: Future[Option[ContentAssignedDto]],
    assigned: ContentAssignedDto) = {
    savedAssigned flatMap {
      r =>
        {
          val assignedId = r.get.id.get
          //save assigned_cohorts
          val responses: List[Future[Boolean]] =
            attachAssignedCohorts(assigned.cohorts, assignedId) ::
              attachAssignedPackages(assigned.packages, assignedId) ::
              Nil

          Future.sequence(responses) flatMap {
            _ =>
              {
                publishDao.find(assignedId) flatMap {
                  result => Future(Results.Ok(Json.toJson(result.get)))
                }
              }
          }
        }
    }
  }

  private def attachAssignedCohorts(cohorts: Option[List[CohortDto]], assignedId: Int): Future[Boolean] = {
    cohorts match {
      case None => Future(false)
      case Some(cohortList) =>
        val responses = cohortList.map(c => c.id match {
          case None => Future(false)
          case Some(cohortid: Int) =>
            val attached = publishDao.attachCohort(assignedId, cohortid)
            attached flatMap {
              _ => Future(true)
            }
        })
        Future.sequence(responses) flatMap {
          _ => Future(true)
        }
    }
  }

  private def attachAssignedPackages(packages: Option[List[ContentPackageDto]], assignedId: Int): Future[Boolean] = {
    packages match {
      case None => Future(false)
      case Some(pkgList) =>
        val responses = pkgList.map(c => c.id match {
          case None => Future(false)
          case Some(packageId: Int) =>
            val attached = publishDao.attachPackage(assignedId, packageId)
            attached flatMap {
              _ => Future(true)
            }
        })
        Future.sequence(responses) flatMap {
          _ => Future(true)
        }
    }
  }

}
