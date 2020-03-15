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

import javax.inject._

import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import env.JWTEnv
import guards.AuthEducator
import models.dao.{ CohortDao, UserDaoSlick }
import models.dto.UserDto
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents }
import com.mohiva.play.silhouette.api._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles retrieval of userDtos.
 * @param cc injected controller components for the extended abstract controller
 * @param userDao injected model
 * @param silhouette injected authentication library
 * @param cohortDao injected model
 * @param ec injected execution context to execute futures
 */
@Singleton
class UserController @Inject() (
  cc: ControllerComponents,
  userDao: UserDaoSlick,
  silhouette: Silhouette[JWTEnv],
  cohortDao: CohortDao
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) with I18nSupport {

  /**
   * Endpoint to retrieve all users.
   * @return a list of all users
   */
  def getAll: Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      userDao.all().map(users => Ok(Json.toJson(users.map(u => UserDto(u)))))
  }

  /**
   * Endpoin t to retrieve a user by Id
   * @param id the supplied id
   * @return an individual UserDto
   */
  def get(id: Int): Action[AnyContent] = silhouette.SecuredAction(AuthEducator).async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      userDao.find(id).map(user => Ok(Json.toJson(UserDto(user.get))))
  }

}
