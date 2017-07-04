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
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import models.dto.UserDto
import env.JWTEnv
import play.api.libs.json.Json
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, MessagesRequest }

import scala.concurrent.ExecutionContext

@Singleton
class AuthController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  //silhouette: Silhouette[JWTEnv],
  //userService: UserService,
  //authInfoRepository: AuthInfoRepository,
  //authTokenService: AuthTokenService,
  avatarService: AvatarService,
  //passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def login: Action[AnyContent] = messagesAction { implicit request: MessagesRequest[AnyContent] =>
    UserDto.userForm.bindFromRequest.fold(
      formWithErrors => {
        Ok("Error with data")
      },
      user => {
        Ok(Json.toJson(user))
      }
    )
  }
}
