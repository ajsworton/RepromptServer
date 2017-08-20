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

package guards

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import libs.AppFactory
import models.User
import org.joda.time.DateTime
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.test.FakeRequest

class AuthEducatorSpec extends AsyncFunSpec with Matchers with BeforeAndAfter with AppFactory {

  val educator = User(id = None, firstName = "", surName = "", email = "em@il", isEducator = true)
  val student = User(id = None, firstName = "", surName = "", email = "em@ail2")

  val authenticator = JWTAuthenticator(id = "", loginInfo = new LoginInfo("", ""),
    lastUsedDateTime = DateTime.now(), expirationDateTime = DateTime.now().plusDays(1),
    idleTimeout = None
  )

  describe("isAuthorized") {
    it("should return true for an educator") {
      AuthEducator.isAuthorized(educator, authenticator)(FakeRequest()) map (r => r should be(true))
    }

    it("should return false for a student") {
      AuthEducator.isAuthorized(student, authenticator)(FakeRequest()) map (r => r should be(false))
    }
  }

}
