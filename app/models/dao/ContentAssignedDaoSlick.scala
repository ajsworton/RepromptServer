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

import models.dto.ContentAssignedCohortDto.ContentAssignedCohortTable
import models.dto.ContentAssignedDto.ContentAssignedTable
import models.dto.ContentAssignedPackageDto.ContentAssignedPackageTable
import models.dto.{ CohortDto, ContentAssignedCohortDto, ContentAssignedDto, ContentAssignedPackageDto, ContentPackageDto }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class ContentAssignedDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends ContentAssignedDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val ContentAssigned = TableQuery[ContentAssignedTable]
  private val ContentAssignedCohort = TableQuery[ContentAssignedCohortTable]
  private val ContentAssignedPackage = TableQuery[ContentAssignedPackageTable]

  def findContentQuery(assignedId: Int) =
    sql"""
          SELECT  ca.Id, ca.Name, ca.ExamDate, ca.Active, ca.OwnerId, 1,
          cohorts.Id, cohorts.ParentId, cohorts.OwnerId, cohorts.Name,
          cp.Id, cp.FolderId, cp.OwnerId, cp.Name

          FROM content_assigned AS ca

            LEFT JOIN content_assigned_cohorts AS cac
            ON ca.Id = cac.AssignedId

            LEFT JOIN cohorts AS cohorts
            ON cohorts.Id = cac.CohortId

            LEFT JOIN content_assigned_packages AS cap
            ON ca.Id = cap.AssignedId

            LEFT JOIN content_packages AS cp
            ON cp.Id = cap.PackageId

          WHERE ca.Id = $assignedId
          ORDER BY ca.Name, cohorts.Name, cp.Name
         """.as[(ContentAssignedDto, Option[CohortDto], Option[ContentPackageDto])]

  def findContentQueryByOwner(ownerId: Int) =
    sql"""
          SELECT  ca.Id, ca.Name, ca.ExamDate, ca.Active, ca.OwnerId, 1,
          cohorts.Id, cohorts.ParentId, cohorts.OwnerId, cohorts.Name,
          cp.Id, cp.FolderId, cp.OwnerId, cp.Name

          FROM content_assigned AS ca

            LEFT JOIN content_assigned_cohorts AS cac
            ON ca.Id = cac.AssignedId

            LEFT JOIN cohorts AS cohorts
            ON cohorts.Id = cac.CohortId

            LEFT JOIN content_assigned_packages AS cap
            ON ca.Id = cap.AssignedId

            LEFT JOIN content_packages AS cp
            ON cp.Id = cap.PackageId

          WHERE ca.OwnerId = $ownerId
          ORDER BY ca.Name, cohorts.Name, cp.Name
         """.as[(ContentAssignedDto, Option[CohortDto], Option[ContentPackageDto])]

  override def find(assignedContentId: Int): Future[Option[ContentAssignedDto]] = {
    val result = findContentQuery(assignedContentId)
    val run = db.run(result)

    run.flatMap(
      r => {
        if (r.nonEmpty) {
          val cohortList = r.map(p => p._2.get).toSet.filter(m => m.id.get > 0).toList
          val packageList = r.map(p => p._3.get).toSet.filter(m => m.id.get > 0).toList
          Future(Some(r.head._1.copy(cohorts = Some(cohortList), packages = Some(packageList))))
        } else {
          Future(None)
        }
      })
  }

  override def findByOwner(ownerId: Int): Future[Seq[ContentAssignedDto]] = {
    val result = findContentQueryByOwner(ownerId)
    val run = db.run(result)

    run.flatMap(
      r => {
        val grouped = r.groupBy(_._1)
        val out = for {
          groupContent <- grouped
          content = groupContent._2
          cohortList = content.map(p => p._2.get).toSet.filter(m => m.id.get > 0).toList
          packageList = content.map(p => p._3.get).toSet.filter(m => m.id.get > 0).toList
          result = groupContent._1.copy(cohorts = Some(cohortList), packages = Some(packageList))
        } yield result
        Future(out.toSeq)
      }
    )
  }

  override def save(content: ContentAssignedDto): Future[Option[ContentAssignedDto]] = {
    db.run((ContentAssigned returning ContentAssigned.map(_.id)
      into ((folder, returnedId) => Some(folder.copy(id = returnedId)))
    ) += content)
  }

  override def update(assigned: ContentAssignedDto): Future[Option[ContentAssignedDto]] = {
    if (assigned.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(ContentAssigned.filter(_.id === assigned.id).update(assigned))
        read <- find(assigned.id.get)
      } yield read
    }
  }

  override def delete(contentId: Int): Future[Int] = {
    db.run(ContentAssigned.filter(_.id === contentId).delete)
  }

  override def deleteByOwner(ownerId: Int): Future[Int] = {
    db.run(ContentAssigned.filter(_.ownerId === ownerId).delete)
  }

  override def attachCohort(assignedId: Int, cohortId: Int): Future[Either[String, Int]] = {
    if (cohortId < 1 || assignedId < 1) {
      Future(Left("Invalid id"))
    } else {
      val insert = new ContentAssignedCohortDto(Some(assignedId), Some(cohortId))
      db.run((ContentAssignedCohort += insert).asTry) map {
        case Failure(ex) => Left(ex.getMessage)
        case Success(x) => Right(x)
      }
    }
  }

  override def detachCohort(assignedId: Int, cohortId: Int): Future[Either[String, Int]] = {
    if (cohortId < 1 || assignedId < 1) {
      Future(Left("Invalid id"))
    } else {
      db.run(ContentAssignedCohort.filter(cac => cac.cohortId === cohortId && cac.assignedId === assignedId)
        .delete) flatMap {
        r => Future(Right(r))
      }
    }
  }

  override def attachPackage(assignedId: Int, packageId: Int): Future[Either[String, Int]] = {
    if (packageId < 1 || assignedId < 1) {
      Future(Left("Invalid id"))
    } else {
      val insert = new ContentAssignedPackageDto(Some(assignedId), Some(packageId))
      db.run((ContentAssignedPackage += insert).asTry) map {
        case Failure(ex) => Left(ex.getMessage)
        case Success(x) => Right(x)
      }
    }
  }

  override def detachPackage(assignedId: Int, packageId: Int): Future[Either[String, Int]] = {
    if (packageId < 1 || assignedId < 1) {
      Future(Left("Invalid id"))
    } else {
      db.run(ContentAssignedPackage.filter(cap => cap.packageId === packageId && cap.assignedId === assignedId)
        .delete) flatMap {
        r => Future(Right(r))
      }
    }
  }
}
