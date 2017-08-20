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

import models.dao.ContentPackageDao
import models.dto.ContentPackageDto
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{ BeforeAndAfter, FunSpec, Matchers }
import libs.AppFactory
import org.mockito.Mockito._
import scala.concurrent.{ ExecutionContext, Future }

class DaoOnDtoActionSpec extends FunSpec with Matchers with MockitoSugar with BeforeAndAfter with AppFactory {

  implicit val ec: ExecutionContext = fakeApplication().injector.instanceOf[ExecutionContext]
  val dto: ContentPackageDto = new ContentPackageDto(Some(1), 1, 1, "Name Here", None)
  val service: ContentPackageDao = mock[ContentPackageDao]
  val action: DaoOnDtoAction = fakeApplication().injector.instanceOf[DaoOnDtoAction]
  //val action: DaoOnDtoAction = new DaoOnDtoAction

  before {
    when(service.save(dto)(ec)).thenReturn(Future(Some(dto)))
  }

  describe("saveDto") {
    it("should call the save action on an injected Dao") {
      action.saveDto(service, dto) should be(Future(dto))
      verify(service, times(1)).save(dto)
    }
  }

}
