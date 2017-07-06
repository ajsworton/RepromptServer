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

package controllers.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import env.JWTEnv
import models.dto.UserRegisterDto
import models.services.{ AuthTokenService, UserService }
import play.api.i18n.I18nSupport
import play.api.libs.mailer.{ MailerClient }
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, Request }
import play.api.libs.json.Json
import responses.JsonErrorResponse

import scala.concurrent.ExecutionContext

/**
 * The `Sign Up` controller.
 *
 * @param cc                     The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param ex                     The execution context.
 */
class RegisterController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient
)(
  implicit
  ex: ExecutionContext
) extends AbstractController(cc) with I18nSupport {

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit: Action[AnyContent] = silhouette.UnsecuredAction {
    implicit request: Request[AnyContent] =>
      UserRegisterDto.registerForm.bindFromRequest.fold(
        formError => {
          Ok(Json.toJson(JsonErrorResponse()))
        },
        userRegister => {
          Ok(Json.toJson(userRegister))
        }
      )
  }

}
