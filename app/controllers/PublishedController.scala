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
import libraries.FileHelper
import models.dao.{ ContentAssignedDao, ContentItemDao }
import models.dto.{ CohortDto, ContentAssignedDto, ContentPackageDto }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, MultipartFormData, Result, Results }
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

  def get(id: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      val result = publishDao.find(id)
      result flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def getAllPublishedByCurrentUser: Action[AnyContent] = silhouette.SecuredAction(AuthEducator())
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        val user = request.identity
        if (user.id.isDefined) {
          val results = publishDao.findByOwner(user.id.get)
          results flatMap {
            r => Future(Ok(Json.toJson(r)))
          }
        } else {
          Future(Results.Unauthorized(Json.toJson(JsonErrorResponse("Authentication error"))))
        }
    }

  def deletePublishedExam(assignedId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      publishDao.delete(assignedId) flatMap {
        r => Future(Ok(Json.toJson(r)))
      }
  }

  def savePublishedExam: Action[AnyContent] = silhouette.SecuredAction(AuthEducator()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ContentAssignedDto.contentAssignedForm.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => saveOrUpdateExam(formData.copy(ownerId = request.identity.id))
      )
  }

  private def saveOrUpdateExam(assigned: ContentAssignedDto): Future[Result] = {
    assigned.id match {
      case None => saveExam(assigned)
      case Some(id) => updateExam(assigned, id)
    }
  }

  def saveExam(assigned: ContentAssignedDto): Future[Result] = {
    val savedAssigned = publishDao.save(assigned)
    handleCohortAndPackageInsertion(savedAssigned, assigned)
  }

  def updateExam(assigned: ContentAssignedDto, id: Int): Future[Result] = {
    val updatedAssigned = publishDao.update(assigned)
    handleCohortAndPackageInsertion(updatedAssigned, assigned)
  }

  def handleCohortAndPackageInsertion(
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

  def attachAssignedCohorts(cohorts: Option[List[CohortDto]], assignedId: Int): Future[Boolean] = {
    cohorts match {
      case None => Future(false)
      case Some(cohortList) => {
        val responses = cohortList.map(c => c.id match {
          case None => Future(false)
          case Some(id: Int) => {
            val attached = publishDao.attachCohort(assignedId, id)
            attached flatMap {
              _ => Future(true)
            }
          }
        })
        Future.sequence(responses) flatMap {
          _ => Future(true)
        }
      }
    }
  }

  def attachAssignedPackages(packages: Option[List[ContentPackageDto]], assignedId: Int): Future[Boolean] = {
    packages match {
      case None => Future(false)
      case Some(pkgList) => {
        val responses = pkgList.map(c => c.id match {
          case None => Future(false)
          case Some(id: Int) => {
            val attached = publishDao.attachPackage(assignedId, id)
            attached flatMap {
              _ => Future(true)
            }
          }
        })
        Future.sequence(responses) flatMap {
          _ => Future(true)
        }
      }
    }
  }

}
