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
import _root_.play.api.mvc.{ AnyContent, AnyContentAsEmpty, Request }
import env.JWTEnv
import models.dao.UserDao
import play.api.test.FakeRequest

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

class AuthHelper @Inject() (
  silhouette: Silhouette[JWTEnv],
  userDao: UserDao)(implicit ec: ExecutionContext) {

  var educatorId = 2
  var studentId = 4

  var eduSave: Future[Option[User]] = _
  var studSave: Future[Option[User]] = _

  var educator: User = _
  var student: User = _

  var educatorLoginInfo: LoginInfo = _
  var studentLoginInfo: LoginInfo = _

  val testData = new UserProfileTestData(userDao)

  var educatorTokenRequest: Future[String] = _
  var educatorJWT: String = _

  var studentTokenRequest: Future[String] = _
  var studentJWT: String = _

  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  def setup(): Unit = {
    eduSave = userDao.find(educatorId)
    studSave = userDao.find(studentId)

    educator = Await.result(eduSave, 10 seconds).get
    student = Await.result(studSave, 10 seconds).get

    educatorLoginInfo = educator.profiles.head.loginInfo
    studentLoginInfo = student.profiles.head.loginInfo

    educatorTokenRequest = setAndGetJWToken(educatorLoginInfo, educator, FakeRequest())
    educatorJWT = Await.result(educatorTokenRequest, 10 seconds)

    studentTokenRequest = setAndGetJWToken(studentLoginInfo, student, FakeRequest())
    studentJWT = Await.result(studentTokenRequest, 10 seconds)

    educatorFakeRequest = FakeRequest().withHeaders(("X-Auth-Token", educatorJWT))
    studentFakeRequest = FakeRequest().withHeaders(("X-Auth-Token", studentJWT))

  }

  private def setAndGetJWToken(loginInfo: LoginInfo, user: User, request: Request[AnyContent]): Future[String] =
    for {
      authenticator <- silhouette.env.authenticatorService.create(loginInfo)(request)
      token <- silhouette.env.authenticatorService.init(authenticator)(request)
    } yield token

}
