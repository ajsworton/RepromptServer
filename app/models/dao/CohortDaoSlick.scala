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

import models.User
import models.dto.{ CohortDto, CohortMemberDto, UserDto }
import models.dto.CohortDto.CohortsTable
import models.dto.CohortMemberDto.CohortsMembersTable
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

class CohortDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CohortDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val Cohorts = TableQuery[CohortsTable]
  private val CohortsMembers = TableQuery[CohortsMembersTable]

  //  override def find(cohortId: Int): Future[Option[CohortDto]] = {
  //    db.run(Cohorts.filter(_.id === cohortId).result.headOption)
  //  }

  def findCohortQuery(cohortId: Int) =
    sql"""
          SELECT cohorts.Id, cohorts.ParentId, cohorts.OwnerId, cohorts.Name,
          users.Id, users.FirstName, users.SurName, users.Email, users.IsEmailVerified,
          users.IsEducator, users.IsAdministrator, users.AvatarUrl
          FROM cohorts

            LEFT JOIN cohort_members
            ON cohorts.Id = cohort_members.CohortId

            LEFT JOIN users
            ON cohort_members.UserId = users.Id

          WHERE cohorts.Id = $cohortId
         """.as[(CohortDto, Option[User])]

  def findCohortQueryByOwner(ownerId: Int) =
    sql"""
          SELECT cohorts.Id, cohorts.ParentId, cohorts.OwnerId, cohorts.Name,
          users.Id, users.FirstName, users.SurName, users.Email, users.IsEmailVerified,
          users.IsEducator, users.IsAdministrator, users.AvatarUrl
          FROM cohorts

            LEFT JOIN cohort_members
            ON cohorts.Id = cohort_members.CohortId

            LEFT JOIN users
            ON cohort_members.UserId = users.Id

          WHERE cohorts.OwnerId = $ownerId
          ORDER BY cohorts.Name, users.SurName, users.FirstName
         """.as[(CohortDto, Option[User])]

  override def find(cohortId: Int): Future[Option[CohortDto]] = {
    val result = findCohortQuery(cohortId)
    val run = db.run(result)

    run.flatMap(
      r => {
        if (r.nonEmpty) {
          val memberList = r.map(p => UserDto(p._2.get)).toList.filter(m => m.id.get > 0)
          Future(Some(r.head._1.copy(members = Some(memberList))))
        } else {
          Future(None)
        }
      })
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
          membersProc = members.map(p => UserDto(p._2.get)).toList.filter(m => m.id.get > 0)
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
    val parentId = getCohortParentId(cohort)
    if (cohort.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(Cohorts.filter(_.id === cohort.id).map(c => (c.name, c.ownerId, c.parentId))
          .update(cohort.name, cohort.ownerId, parentId))
        read <- find(cohort.id.get)
      } yield read
    }
  }

  private def getCohortParentId(cohort: CohortDto): Option[Int] = {
    if (cohort.parentId.isDefined && cohort.parentId.get > 0) {
      cohort.parentId
    } else {
      None
    }
  }

  override def delete(cohortId: Int): Future[Int] = {
    db.run(Cohorts.filter(_.id === cohortId).delete)
  }

  override def deleteByOwner(ownerId: Int): Future[Int] = {
    db.run(Cohorts.filter(_.ownerId === ownerId).delete)
  }

  def attach(cohortId: Int, userId: Int): Future[Int] = {
    if (userId < 1 || cohortId < 1) {
      Future(0)
    } else {
      val insert = new CohortMemberDto(Some(cohortId), Some(userId))
      db.run(CohortsMembers += insert)
    }
  }

  def detach(cohortId: Int, userId: Int): Future[Int] = {
    if (userId < 1 || cohortId < 1) {
      Future(0)
    } else {
      val toDelete = new CohortMemberDto(Some(cohortId), Some(userId))
      db.run(CohortsMembers.filter(cm => cm.cohortId === cohortId && cm.userId === userId).delete)
    }
  }
}
