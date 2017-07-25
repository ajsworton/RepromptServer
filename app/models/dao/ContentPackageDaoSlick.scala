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

package models.dao

import javax.inject.Inject

import models.dto.CohortDto
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ ExecutionContext, Future }

class ContentPackageDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends ContentPackageDao {

  override def find(packageId: Int): Future[Option[ContentPackageDao]] = ???

  override def findByOwner(ownerId: Int): Future[Seq[CohortDto]] = ???

  override def save(packageDto: CohortDto): Future[Option[CohortDto]] = ???

  override def update(packageDto: CohortDto): Future[Option[CohortDto]] = ???

  override def delete(packageId: Int): Future[Int] = ???

  override def deleteByOwner(ownerId: Int): Future[Int] = ???

  override def attach(packageId: Int, userId: Int): Future[Int] = ???

  override def detach(cohortId: Int, userId: Int): Future[Int] = ???
}
