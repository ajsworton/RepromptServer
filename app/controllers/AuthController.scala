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

import java.util.UUID
import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorResult, AvatarService }
import com.mohiva.play.silhouette.api.util.{ PasswordHasher, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.dto.{ UserDto, UserRegisterDto }
import env.JWTEnv
import models.{ Profile, User }
import models.services.{ AuthTokenService, UserService }
import play.api
import play.api.{ Environment, data }
import play.api.i18n.Messages
import responses.JsonErrorResponse
import play.api.libs.json.Json
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, MessagesRequest }
import play.libs.ws.WSClient
import play.api.mvc.Result

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AuthController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  //passwordHasherRegistry: PasswordHasherRegistry,
  passwordHasher: PasswordHasher,
  ws: WSClient,
  environment: Environment,
  mailerClient: MailerClient)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def register: Action[AnyContent] = messagesAction.async {
    implicit request =>
      UserRegisterDto.registerForm.bindFromRequest.fold(
        formError => Future(Ok(Json.toJson(formError.errorsAsJson))),
        formData => {
          handleTokenAuth(formData, request)
        }
      )
  }

  private def handleTokenAuth(formData: UserRegisterDto, request: MessagesRequest[AnyContent]): Future[Result] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, formData.email)
    userService.retrieve(loginInfo).flatMap {
      case Some(_) => Future(Ok(Json.toJson(JsonErrorResponse("Already Authenticated"))))
      case None =>
        val profile = Profile(
          loginInfo = loginInfo, email = Some(formData.email),
          firstName = Some(formData.firstName), lastName = Some(formData.surName),
          fullName = Some(s"${formData.firstName} ${formData.surName}"))
        for {
          avatarUrl <- avatarService.retrieveURL(formData.email)
          user <- userService.save(User(profile.copy(avatarUrl = avatarUrl)))
          _ <- authInfoRepository.add(loginInfo, passwordHasher.hash(formData.password))
          token <- authTokenService.create(user.get.id.get)
          result <- embedToken(loginInfo, request, user.get)
        } yield {
          result
        }
    }
  }

  private def embedToken(loginInfo: LoginInfo, request: MessagesRequest[AnyContent], user: User): Future[Result] = {
    silhouette.env.authenticatorService.create(loginInfo)(request).flatMap { authenticator =>
      silhouette.env.eventBus.publish(LoginEvent(user, request))
      silhouette.env.authenticatorService.init(authenticator)(request).flatMap { v =>
        silhouette.env.authenticatorService.embed(
          v,
          Ok(Json.toJson(JsonErrorResponse("Authenticated"))))(request)
      }
    }
  }

  def login: Action[AnyContent] = messagesAction {
    implicit request: MessagesRequest[AnyContent] =>
      UserDto.userForm.bindFromRequest.fold(
        formError => Ok(Json.toJson(formError.errorsAsJson)),
        formData => Ok(Json.toJson(formData))
      )
  }

}
