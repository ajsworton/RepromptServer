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

package responses

import libs.AppFactory
import org.scalatest.{ AsyncFunSpec, Matchers }
import play.api.test.FakeRequest

class ApiSecuredErrorHandlerSpec extends AsyncFunSpec with Matchers with AppFactory {

  val errorHandler = fakeApplication().injector.instanceOf[ApiSecuredErrorHandler]

  describe("onNotAuthenticated") {
    it("should return a 401 error response") {
      errorHandler.onNotAuthenticated(FakeRequest()).flatMap {
        r =>
          {
            r.body.contentLength should not be None
            r.body.contentLength.get should be > 0L
            r.header.status should be(401)
          }
      }
    }
  }

  describe("onNotAuthorized") {
    it("should return a 403 error response") {
      errorHandler.onNotAuthorized(FakeRequest()).flatMap {
        r =>
          {
            r.body.contentLength should not be None
            r.body.contentLength.get should be > 0L
            r.header.status should be(403)
          }
      }
    }
  }

}
