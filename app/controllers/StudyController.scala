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

import java.time.LocalDate
import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import env.JWTEnv
import factories.RepromptCalculatorFactory
import guards.AuthStudent
import models.dao.StudyDao
import models.dto.{ ContentAssignedDto, ScoreDto }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result, Results }
import responses.JsonErrorResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class StudyController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  studyDao: StudyDao,
  environment: Environment)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def getContentItems: Action[AnyContent] = silhouette.SecuredAction(AuthStudent()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      request.identity.id match {
        case None => Future(Results.Unauthorized)
        case Some(id: Int) => returnStudyItems(id)
      }
  }

  def saveStudyScore: Action[AnyContent] = silhouette.SecuredAction(AuthStudent()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ScoreDto.form.bindFromRequest.fold(
        formError => Future(Results.BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          if (formData.score >= 50) {
            handleDataForPersist(formData.copy(userId = request.identity.id, streak =
              formData.streak + 1))
          } else {
            handleDataForPersist(formData.copy(userId = request.identity.id, streak = 0))
          }
        }
      )
  }

  def getContentAssignedStatus: Action[AnyContent] = silhouette.SecuredAction(AuthStudent()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      request.identity.id match {
        case None => Future(Results.Unauthorized)
        case Some(id: Int) => studyDao.getContentAssignedStatusByUserId(id) flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  def disableContent(assignedId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthStudent())
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        request.identity.id match {
          case None => Future(Results.Unauthorized)
          case Some(id: Int) => studyDao.disableContentAssigned(assignedId, id) flatMap {
            r => Future(Ok(Json.toJson(r)))
          }
        }
    }

  def enableContent(assignedId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthStudent())
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        request.identity.id match {
          case None => Future(Results.Unauthorized)
          case Some(id: Int) => studyDao.enableContentAssigned(assignedId, id) flatMap {
            r => Future(Ok(Json.toJson(r)))
          }
        }
    }

  private def handleDataForPersist(scoreData: ScoreDto): Future[Result] = {
    val examDate = studyDao.getExamDateByContentItemId(scoreData.contentItemId)
    examDate flatMap {
      r: Option[LocalDate] =>
        r match {
          case None => Future(Results.InternalServerError("Failed to retrieve examination date"))
          case Some(eDate) => persistStudyData(RepromptCalculatorFactory.getCalculator(scoreData)
            .addRepromptDate(scoreData, eDate))
        }
    }
  }

  private def persistStudyData(scoreData: ScoreDto): Future[Result] = {
    studyDao.saveScoreData(scoreData) flatMap {
      r =>
        if (r.isDefined) { Future(Ok(Json.toJson(r))) }
        else { Future(Results.InternalServerError("Failed to write to database")) }
    }
  }

  private def returnStudyItems(id: Int) = {
    val result = studyDao.getContentItems(id)
    result flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

}
