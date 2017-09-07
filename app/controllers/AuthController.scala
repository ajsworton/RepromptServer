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
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import env.JWTEnv
import models.dao.UserDao
import models.dto.{ UserDto, UserLoginDto, UserRegisterDto }
import models.services.{ AuthTokenService, UserService }
import models.{ Profile, User }
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, MessagesActionBuilder, Request, Result }
import play.libs.ws.WSClient
import responses.JsonErrorResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AuthController @Inject() (
  messagesAction: MessagesActionBuilder,
  cc: ControllerComponents,
  silhouette: Silhouette[JWTEnv],
  userService: UserService,
  userDao: UserDao,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasher: PasswordHasher,
  ws: WSClient,
  environment: Environment,
  mailerClient: MailerClient)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def register: Action[AnyContent] = silhouette.UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      UserRegisterDto.form.bindFromRequest.fold(
        formError => Future(BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          handleTokenAuth(formData, request)
        }
      )
  }

  def login: Action[AnyContent] = silhouette.UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      UserLoginDto.form.bindFromRequest.fold(
        formError => Future(BadRequest(Json.toJson(formError.errorsAsJson))),
        formData => {
          CheckRetrieveAndHandleUser(LoginInfo(CredentialsProvider.ID, formData.email), request, formData)
        }
      )
  }

  def profile: Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request: SecuredRequest[JWTEnv, AnyContent] =>
      Future(Ok(Json.toJson(UserDto(request.identity))))
  }

  private def CheckRetrieveAndHandleUser(loginInfo: LoginInfo, request: Request[AnyContent], formData: UserLoginDto) = {
    userService.retrieve(loginInfo).flatMap {
      case None => Future(BadRequest(Json.toJson(JsonErrorResponse("No Matching Record Found"))))
      case Some(user) => HandleUserLogin(request, formData, loginInfo, user)
    }
  }

  private def HandleUserLogin(request: Request[AnyContent], formData: UserLoginDto, loginInfo: LoginInfo, user: User) = {
    if (passwordHasher.matches(
      user.profiles.find(p => p.loginInfo == loginInfo).get.passwordInfo.get, formData.password)) { //user is authenticated
      embedToken(loginInfo, request, user)
    } else { //user not authenticated
      Future(BadRequest(Json.toJson(JsonErrorResponse("No Matching Record Found"))))
    }
  }

  private def handleTokenAuth(formData: UserRegisterDto, request: Request[AnyContent]): Future[Result] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, formData.email)
    userService.retrieve(loginInfo).flatMap {
      case Some(_) => Future(Ok(Json.toJson(JsonErrorResponse("Credentials Already Registered"))))
      case None =>
        val profile = Profile(
          loginInfo = loginInfo, email = Some(formData.email),
          firstName = Some(formData.firstName), lastName = Some(formData.surName),
          fullName = Some(s"${formData.firstName} ${formData.surName}"),
          passwordInfo = Some(passwordHasher.hash(formData.password))
        )
        for {
          avatarUrl <- avatarService.retrieveURL(formData.email)
          user <- userDao.save(User(profile.copy(avatarUrl = avatarUrl)).copy(isEducator =
            formData.isEducator))
          //token <- authTokenService.create(user.get.id.get)
          result <- embedToken(loginInfo, request, user.get)
        } yield result
    }
  }

  private def embedToken(loginInfo: LoginInfo, request: Request[AnyContent], user: User): Future[Result] = {
    silhouette.env.authenticatorService.create(loginInfo)(request).flatMap { authenticator =>
      silhouette.env.eventBus.publish(LoginEvent(user, request))
      silhouette.env.authenticatorService.init(authenticator)(request).flatMap { token =>
        silhouette.env.authenticatorService.embed(
          token,
          Ok(Json.toJson(UserDto(user))))(request)
      }
    }
  }

}
