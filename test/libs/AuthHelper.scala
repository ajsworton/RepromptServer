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

package libs

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ LoginInfo, Silhouette }
import models.User
import play.api.mvc.AnyContent
import _root_.play.api.mvc.Request
import controllers.UserController
import env.JWTEnv
import models.dao.UserDao
import play.api.test.FakeRequest
import scala.concurrent.duration._

import scala.concurrent.{ Await, ExecutionContext, Future }

class AuthHelper @Inject() (
  silhouette: Silhouette[JWTEnv],
  userDao: UserDao)(implicit ec: ExecutionContext) {

  val eduSave = userDao.find(2)
  val studSave = userDao.find(4)

  val educator: User = Await.result(eduSave, 10 seconds).get
  val student: User = Await.result(studSave, 10 seconds).get

  val educatorLoginInfo: LoginInfo = educator.profiles.head.loginInfo
  val studentLoginInfo: LoginInfo = student.profiles.head.loginInfo

  val testData = new UserProfileTestData(userDao)

  val educatortokenRequest = setAndGetJWToken(educatorLoginInfo, educator, FakeRequest())
  val educatorJWT = Await.result(educatortokenRequest, 10 seconds)

  val studenttokenRequest = setAndGetJWToken(studentLoginInfo, student, FakeRequest())
  val studentJWT = Await.result(studenttokenRequest, 10 seconds)

  val educatorFakeRequest = FakeRequest().withHeaders(("X-Auth-Token", educatorJWT))
  val studentFakeRequest = FakeRequest().withHeaders(("X-Auth-Token", studentJWT))

  private def setAndGetJWToken(loginInfo: LoginInfo, user: User, request: Request[AnyContent]): Future[String] =
    for {
      authenticator <- silhouette.env.authenticatorService.create(loginInfo)(request)
      token <- silhouette.env.authenticatorService.init(authenticator)(request)
    } yield token

}
