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
import guards.AuthStudent
import models.dao.ProgressDao
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Result, Results }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class DataController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  progressDao: ProgressDao,
  environment: Environment)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def getUserProgress: Action[AnyContent] = silhouette.SecuredAction(AuthStudent()).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      request.identity.id match {
        case None => Future(Results.Unauthorized)
        case Some(id: Int) => returnProgressData(id)
      }
  }

  def returnProgressData(id: Int): Future[Result] = {
    progressDao.getUserProgressData(id) flatMap {
      r => Future(Ok(Json.toJson(r)))
    }
  }
}
