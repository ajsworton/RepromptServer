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

package libraries

import org.scalatest.{AsyncFunSpec, BeforeAndAfter, Matchers}
import libs.{DatabaseSupport, MockDaoDto, MockDto}
import play.api.libs.json.Json
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}

class DaoOnDtoActionSpec extends AsyncFunSpec with Matchers with BeforeAndAfter with DatabaseSupport {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val dto = MockDto(Some(1))
  var service: MockDaoDto = _
  val action: DaoOnDtoAction = app.injector.instanceOf[DaoOnDtoAction]

  before {
    service = app.injector.instanceOf[MockDaoDto]
  }

  describe("saveDto") {
    it("should call the save action on an injected Dao") {
      val response = action.saveDto(service, dto)
      response map (r => {
        r.header.status should be(200)
        service.saved should be(1)
      })
    }
  }

  describe("updateDto") {
    it("should call the update action on an injected Dao") {
      val response = action.updateDto(service, dto)
      response map (r => {
        r.header.status should be(200)
        service.updated should be(1)
      })
    }
  }

  describe("validateAndSaveDto") {
    it("should call the update action on an injected Dao if find returns a dto") {
      val response = action.validateAndSaveDto(service, dto)
      response map (r => {
        r.header.status should be(200)
        service.updated should be(1)
        service.saved should be(0)
      })
    }

    it("should call the save action on an injected Dao if find returns no dto") {
      service.foundReturnsValue = false

      val response = action.validateAndSaveDto(service, dto)
      response map (r => {
        r.header.status should be(200)
        service.saved should be(1)
        service.updated should be(0)
      })
    }

    it("should call the save action if injected dto is None") {
      val response = action.validateAndSaveDto(service, dto.copy(id = None))
      response map (r => {
        r.header.status should be(200)
        service.saved should be(1)
        service.updated should be(0)
      })
    }

  }

}
