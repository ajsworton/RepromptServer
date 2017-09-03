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
import models.dto.ScoreDto
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result, Results }
//import com.mohiva.play.silhouette.test._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class StudyController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  studyDao: StudyDao,
  environment: Environment)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  /**
   * Get all content items for the current student
   * @return ContentItems as Json
   */
  def getContentItems: Action[AnyContent] = silhouette.SecuredAction(AuthStudent).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] => returnStudyItems(request.identity.id.get)
  }

  /**
   * Save a provided score by POST
   * @return ScoreDto as JSON
   */
  def saveStudyScore: Action[AnyContent] = silhouette.SecuredAction(AuthStudent).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      ScoreDto.form.bindFromRequest.fold(
        formError => Future(BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => adjustStreakForPersist(request, formData)
      )
  }

  /**
   * Helper method to update the streak based on the score.
   * @param request the request data
   * @param formData the supplied POST data as a scoreDto
   * @return a Future result
   */
  private def adjustStreakForPersist(request: SecuredRequest[JWTEnv, AnyContent], formData: ScoreDto): Future[Result] = {
    if (formData.score >= 50) {
      handleDataForPersist(formData.copy(userId = request.identity.id, streak = formData.streak + 1))
    } else {
      handleDataForPersist(formData.copy(userId = request.identity.id, streak = 0))
    }
  }

  /**
   * Get all the current user's assigned content including status
   * @return List[ContentAssignedDto] as JSON
   */
  def getContentAssignedStatus: Action[AnyContent] = silhouette.SecuredAction(AuthStudent).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      request.identity.id match {
        case None => Future(Results.Unauthorized)
        case Some(id: Int) => studyDao.getContentAssignedStatusByUserId(id) flatMap {
          r => Future(Ok(Json.toJson(r)))
        }
      }
  }

  /**
   * disable a content item for the currently logged in user by assigned content id
   * @param assignedId the supplied assigned content id
   * @return an Integer to indicate number of rows affected
   */
  def disableContent(assignedId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthStudent)
    .async {
      implicit request: SecuredRequest[JWTEnv, AnyContent] =>
        request.identity.id match {
          case None => Future(Results.Unauthorized)
          case Some(id: Int) => applyContentDisableAndGetResult(assignedId, id)
        }
    }

  /**
   * A helper function to apply content disablement and return result
   * @param assignedId the assigned content id
   * @param id the user Id
   * @return a future result
   */
  private def applyContentDisableAndGetResult(assignedId: Int, id: Int) = {
    studyDao.disableContentAssigned(assignedId, id) flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  /**
   * Enable a content item for the currently logged in student
   * @param assignedId the supplied assigned content id
   * @return an Integer to indicate number of rows affected
   */
  def enableContent(assignedId: Int): Action[AnyContent] = silhouette.SecuredAction(AuthStudent).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      request.identity.id match {
        case None => Future(Results.Unauthorized)
        case Some(id: Int) => applyContentEnableAndGetResult(assignedId, id)
      }
  }

  /**
   * Get historical score data for the current user aggregated by examination
   * @return a list of exam aggregated historical performance data as JSON
   */
  def getHistoricalPerformanceByExam: Action[AnyContent] = silhouette.SecuredAction(AuthStudent).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      request.identity.id match {
        case None => Future(Results.Unauthorized)
        case Some(id: Int) => fetchAndReturnHistoricalData(id)
      }
  }

  /**
   * Helper function to retrieve and return historical data
   * @param id the supplied user id
   * @return a future result
   */
  private def fetchAndReturnHistoricalData(id: Int): Future[Result] = {
    studyDao.getHistoricalPerformanceByExam(id) flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  /**
   * Helper function to enable an assigned content item and return a result
   * @param assignedId the supplied assigned content it
   * @param id the user id
   * @return a future result
   */
  private def applyContentEnableAndGetResult(assignedId: Int, id: Int) = {
    studyDao.enableContentAssigned(assignedId, id) flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }

  /**
   * Helper function to validate score data for persistence to backing store
   * @param scoreData the score data to persist
   * @return a future result
   */
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

  /**
   * Helper function to persist score data to backing store and handle response
   * @param scoreData the supplied score data
   * @return a future result
   */
  private def persistStudyData(scoreData: ScoreDto): Future[Result] = {
    studyDao.saveScoreData(scoreData) flatMap {
      case Right(data) => Future(Ok(Json.toJson(data)))
      case Left(msg) => Future(Results.InternalServerError(msg))
    }
  }

  /**
   * Helper function to perform retrieval of study items
   * @param id the user id
   * @return a future result
   */
  private def returnStudyItems(id: Int): Future[Result] = {
    val result = studyDao.getContentItems(id)
    result flatMap { r => Future(Ok(Json.toJson(r))) }
  }

}
