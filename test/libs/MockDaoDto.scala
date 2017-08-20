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
import models.dto.Dto

import scala.concurrent.{ ExecutionContext, Future }

case class MockDto(id: Option[Int]) extends Dto

class MockDaoDto @Inject() (ec: ExecutionContext) extends Dao[MockDto] {

  override def find(id: Int)(implicit ec: ExecutionContext): Future[Option[MockDto]] = {
    Future(Some(MockDto(Some(1))))
  }

  override def save(dto: MockDto)(implicit ec: ExecutionContext): Future[Option[MockDto]] = {
    Future(Some(MockDto(Some(1))))
  }

  override def update(dto: MockDto)(implicit ec: ExecutionContext): Future[Option[MockDto]] = {
    Future(Some(MockDto(Some(1))))
  }
}
