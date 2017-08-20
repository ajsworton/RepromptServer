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

import models.dao.Dao
import models.dto.{ ContentPackageDto, Dto }
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }

case class MockDto(id: Option[Int]) extends Dto

class MockDaoDto @Inject() (implicit ec: ExecutionContext) extends Dao[MockDto] {

  var foundReturnsValue = true

  var saved = 0
  var found = 0
  var updated = 0

  override def find(id: Int): Future[Option[MockDto]] = {
    found += 1
    if (foundReturnsValue) { Future(Some(MockDto(Some(1)))) }
    else { Future(None) }
  }

  override def save(dto: MockDto): Future[Option[MockDto]] = {
    saved += 1
    Future(Some(MockDto(Some(1))))
  }

  override def update(dto: MockDto): Future[Option[MockDto]] = {
    updated += 1
    Future(Some(MockDto(Some(1))))
  }
}

object MockDto {
  implicit val serializer = Json.format[MockDto]
}
