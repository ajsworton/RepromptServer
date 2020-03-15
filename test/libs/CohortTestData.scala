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

import models.dto.{CohortDto, CohortMemberDto}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.duration._
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, ExecutionContext, Future}

class CohortTestData @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit
                                                                                       executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  val ownerId = 1

  val cohortNoId1 = new CohortDto(None, None, ownerId, "cohortNoId1")
  val cohortNoId2 = new CohortDto(None, None, ownerId, "cohortNoId2")
  val cohortNoId3 = new CohortDto(None, None, ownerId, "cohortNoId3")
  val cohortNoId4 = new CohortDto(None, None, ownerId, "cohortNoId4")
  val cohortNoId5 = new CohortDto(None, None, ownerId, "cohortNoId5")

  val cohortsNoIds = List(cohortNoId1, cohortNoId2, cohortNoId3, cohortNoId4, cohortNoId5)

  private def insertCohortDataQueries(teacherId: Int, studentId: Int): DBIO[Unit] = {
    val cohortId: Int = teacherId

    DBIO.seq(
      // Insert some suppliers
      sqlu"INSERT INTO users VALUES($teacherId, 'Testy', 'Teachy', 't@teachyer', 'true', 'true', 'false', NULL)",
      sqlu"INSERT INTO users VALUES($studentId, 'Testy', 'Usery', 't@useyer', 'true', 'false', 'false', NULL)",
      sqlu"""INSERT INTO profiles VALUES($teacherId, 'credentials', 't@teachyer', 'true', NULL, NULL, NULL, NULL, '{"hasher": "bcrypt", "password": "$$2a$$10$$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}', NULL, NULL, NULL)""",
      sqlu"""INSERT INTO profiles VALUES($studentId, 'credentials', 't@useyer', 'true', NULL, NULL, NULL, NULL, '{"hasher": "bcrypt", "password": "$$2a$$10$$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}', NULL, NULL, NULL)""",
      sqlu"INSERT INTO cohorts VALUES($cohortId, $teacherId, 'Test Cohort 1', NULL)",
    )
  }

  private def removeCohortDataQueries(teacherId: Int, studentId: Int): DBIO[Unit] = DBIO.seq(
    sqlu"DELETE FROM users WHERE id = $teacherId",
    sqlu"DELETE FROM users WHERE id = $studentId",
  )

  def insertCohortContent(teacherId: Int, studentId: Int): Unit = {
    val response = db.run(insertCohortDataQueries(teacherId, studentId))
    Await.result(response, 10 seconds)
  }

  def clearCohortContent(teacherId: Int, studentId: Int): Unit = {
    val response = db.run(removeCohortDataQueries(teacherId, studentId))
    Await.result(response, 10 seconds)
  }

  private def getCohortMemberQuery(cohortId: Int, userId: Int) = sql"""
      SELECT cm.cohort_id, cm.user_id
      FROM cohort_members AS cm
      WHERE cm.cohort_id = $cohortId
      AND cm.user_id = $userId
    """.as[CohortMemberDto]

  def getCohortMember(cohortId: Int, userId: Int): Future[Option[CohortMemberDto]] =
    db.run(getCohortMemberQuery(cohortId, userId).headOption)
}
