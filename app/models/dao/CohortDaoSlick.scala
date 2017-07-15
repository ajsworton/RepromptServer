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

import javax.inject._

import models.{Profile, User}
import models.dto.CohortDto
import models.dto.CohortDto.CohortsTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class CohortDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  (implicit executionContext: ExecutionContext)
  extends CohortDao  with HasDatabaseConfigProvider[JdbcProfile]{

  private val Cohorts = TableQuery[CohortsTable]

  override def find(cohortId: Int): Future[Option[CohortDto]] = {
    db.run(Cohorts.filter(_.id === cohortId).result.headOption)
  }

  override def findByOwner(ownerId: Int): Future[Seq[CohortDto]] = {
    db.run(Cohorts.filter(_.ownerId === ownerId).result)
  }

  override def save(cohort: CohortDto): Future[Option[CohortDto]] = {
    db.run((Cohorts returning Cohorts.map(_.id)
      into ((cohort, returnedId) => Some(cohort.copy(id = returnedId)))
      ) += cohort)
  }

  override def update(cohort: CohortDto): Future[Option[User]] = ???

  override def delete(cohortId: Int): Future[Int] = {
    db.run(Cohorts.filter(_.id === cohortId).delete)
  }

  override def deleteByOwner(ownerId: Int): Future[Int] = {
    db.run(Cohorts.filter(_.ownerId === ownerId).delete)
  }
}
