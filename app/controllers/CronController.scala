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

import com.mohiva.play.silhouette.api.Silhouette
import env.JWTEnv
import libraries.MailerService
import models.dao.StudyDao
import models.dto.UserNotificationDto
import play.api.{ Configuration, Environment }
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Request, Result }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles cron or scheduled actions (housekeeping, notifications)
 * @param cc injected controller components for the extended abstract controller
 * @param silhouette injected authentication library
 * @param studyDao injected model for study data to retrieve users pending exposure
 * @param notifier injected notification service
 * @param configuration injected config to retrieve shared pass phrase
 * @param ec injected execution context to execute futures
 */
@Singleton
class CronController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  studyDao: StudyDao,
  notifier: MailerService,
  configuration: Configuration)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  /**
   * Passphrase secured endpoint to execute the notifications system
   * @param keyphrase the shared key for access
   * @return list of notified students
   */
  def executeRepromptNotification(keyphrase: String): Action[AnyContent] =
    silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
      val phrase = configuration.underlying.getString("notification.sharedPhrase")
      keyphrase match {
        case p if p == phrase => runNotifications()
        case _ => Future(Unauthorized(Json.toJson("Unauthorised")))
      }
    }

  /**
   * Helper to action the notification sequence
   * @return a future result
   */
  private def runNotifications(): Future[Result] = {
    studyDao.getStudentsWithPendingContent flatMap { students =>
      {
        students.foreach(stud => notifier.notifyStudy(stud))
        Future(Ok(Json.toJson(students.map(stud => UserNotificationDto(stud)))))
      }
    }
  }
}
