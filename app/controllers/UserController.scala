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

import models.dao.UserDaoSlick
import models.dto.UserDto
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject() (
  cc: ControllerComponents,
  userDao: UserDaoSlick
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getAll: Action[AnyContent] = Action async {
    userDao.all().map(users => Ok(Json.toJson(users.map(u => UserDto(u)))))
  }

  def get(id: Int): Action[AnyContent] = Action.async {
    userDao.find(id).map(user => Ok(Json.toJson(UserDto(user.get))))
  }

  //  def create(fname: String, sname: String) = Action {
  //        val user = new UserDao()
  //    implicit request: Request[AnyContent] => Ok(views.html.user())
  //  }

}
