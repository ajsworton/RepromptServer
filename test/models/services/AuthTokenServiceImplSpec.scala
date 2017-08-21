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

package models.services

import java.time.LocalDateTime

import libs.AppFactory
import org.scalatest.{ AsyncFunSpec, Matchers }

import scala.concurrent.duration._

class AuthTokenServiceImplSpec extends AsyncFunSpec with Matchers with AppFactory {

  val service: AuthTokenService = fakeApplication().injector.instanceOf[AuthTokenServiceImpl]

  describe("create") {
    it("should create and return a Future AuthToken") {
      val dateTime = LocalDateTime.now

      for {
        created <- service.create(1, 5 seconds)
        validated <- {
          created.userId should be(1)
          created.expiry should be(dateTime.plusSeconds(5))
        }
      } yield validated

    }
  }

  describe("validate") {
    it("should validate the token and return it when valid") {
      for {
        created <- service.create(1, 1 seconds)
        response <- service.validate(created.tokenId)
        validated <- response.get should be(created)
      } yield validated
    }

    it("should validate the token and return none when invalid") {
      for {
        created <- service.create(1, -1 seconds)
        cleaned <- service.clean
        response <- service.validate(created.tokenId)
        validated <- response should be(None)
      } yield validated
    }
  }

  describe("clean") {
    it("should delete expired tokens and return a list of those deleted") {
      for {
        created <- service.create(1, -1 seconds)
        response <- service.clean
        validated <- response.length should be > 0
      } yield validated
    }
  }

}
