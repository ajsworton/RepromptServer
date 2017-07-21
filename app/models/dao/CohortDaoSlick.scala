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

import models.User.UsersTable
import models.{ Profile, User }
import models.dto.{ CohortDto, UserDto }
import models.dto.CohortDto.CohortsTable
import models.dto.CohortMembersDto.CohortMembersTable
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.{ GetResult, JdbcProfile }
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery
import slick.sql.SqlStreamingAction

import scala.concurrent.{ ExecutionContext, Future }

class CohortDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CohortDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val Cohorts = TableQuery[CohortsTable]

  //  override def find(cohortId: Int): Future[Option[CohortDto]] = {
  //    db.run(Cohorts.filter(_.id === cohortId).result.headOption)
  //  }

  def findCohortQuery(cohortId: Int) =
    sql"""
          SELECT cohorts.Id, cohorts.ParentId, cohorts.OwnerId, cohorts.Name,
          users.Id, users.FirstName, users.SurName, users.Email, users.IsEmailVerified,
          users.IsEducator, users.IsAdministrator, users.AvatarUrl
          FROM cohorts, cohort_members, users
          WHERE cohorts.Id = cohort_members.CohortId
          AND cohort_members.UserId = users.Id
          AND cohorts.Id = $cohortId
         """.as[(CohortDto, Option[User])]

  def findCohortQueryByOwner(ownerId: Int) =
    sql"""
          SELECT cohorts.Id, cohorts.ParentId, cohorts.OwnerId, cohorts.Name,
          users.Id, users.FirstName, users.SurName, users.Email, users.IsEmailVerified,
          users.IsEducator, users.IsAdministrator, users.AvatarUrl
          FROM cohorts, cohort_members, users
          WHERE cohorts.Id = cohort_members.CohortId
          AND cohort_members.UserId = users.Id
          AND cohorts.OwnerId = $ownerId
         """.as[(CohortDto, Option[User])]

  override def find(cohortId: Int): Future[Option[CohortDto]] = {
    val collect = for {
      res <- findCohortQuery(cohortId)
      collected = res.foldLeft(res.head._1.copy(members = Some(Nil)))((acc: CohortDto, row) => {
        val members: Option[List[UserDto]] = row._2 match {
          case None => acc.members
          case Some(user) => Some(UserDto(user) :: acc.members.get)
        }
        acc.copy(members = members)
      })
    } yield Some(collected)

    db.run(collect)
  }

  override def findByOwner(ownerId: Int): Future[Seq[CohortDto]] = {
    val result = findCohortQueryByOwner(ownerId)
    val run = db.run(result)

    run.flatMap(
      r => {
        val grouped = r.groupBy(_._1)
        val out = for {
          groupMembers <- grouped
          members = groupMembers._2
          membersProc = members.map(p => UserDto(p._2.get)).toList
          result = groupMembers._1.copy(members = Some(membersProc))
        } yield result
        Future(out.toSeq)
      }
    )
  }

  override def save(cohort: CohortDto): Future[Option[CohortDto]] = {
    db.run((Cohorts returning Cohorts.map(_.id)
      into ((cohort, returnedId) => Some(cohort.copy(id = returnedId)))
    ) += cohort)
  }

  override def update(cohort: CohortDto): Future[Option[CohortDto]] = {
    if (cohort.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(Cohorts.filter(_.id === cohort.id).update(cohort))
        read <- find(cohort.id.get)
      } yield read
    }
  }

  override def delete(cohortId: Int): Future[Int] = {
    db.run(Cohorts.filter(_.id === cohortId).delete)
  }

  override def deleteByOwner(ownerId: Int): Future[Int] = {
    db.run(Cohorts.filter(_.ownerId === ownerId).delete)
  }
}
