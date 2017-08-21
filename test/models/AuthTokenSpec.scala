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

package models

import java.time.LocalDateTime
import java.util.UUID

import org.scalatest.{ FunSpec, Matchers }

class AuthTokenSpec extends FunSpec with Matchers {

  describe("AuthToken") {
    it("should be applyable") {
      val token = AuthToken.apply(UUID.randomUUID(), 1, LocalDateTime.now())
      token.getClass should be(new AuthToken(UUID.randomUUID(), 1, LocalDateTime.now()).getClass)
    }

    it("should be unapplyable") {
      val dateTime = LocalDateTime.now()
      val token = AuthToken.apply(UUID.randomUUID(), 1, dateTime)
      val unapplied = AuthToken.unapply(token)
      unapplied.isDefined should be(true)
      unapplied.get._2 should be(1)
      unapplied.get._3 should be(dateTime)
    }
  }
}
