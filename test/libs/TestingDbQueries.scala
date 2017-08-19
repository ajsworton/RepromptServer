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

import models.dto.ContentDisabledDto
import play.api.db.slick.HasDatabaseConfigProvider
import slick.dbio.DBIO
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

class TestingDbQueries @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def insertStudyContentQueries(teacherId: Int, studentId: Int): DBIO[Unit] = {
    val cohortId:Int = studentId
    val cohortFolderId:Int = studentId
    val packageId:Int = teacherId
    val package2Id:Int = studentId
    val item1Id = teacherId
    val item2Id = studentId
    val question1Id = teacherId
    val answer1Id = teacherId
    val question2Id = studentId
    val answer2Id = studentId
    val assigned1Id = teacherId
    val assigned2Id = studentId

    DBIO.seq(
      // Insert some suppliers
      sqlu"INSERT INTO users VALUES($teacherId, 'Testy', 'Teachy', 't@teachy', 1, 1, 0, NULL)",
      sqlu"INSERT INTO users VALUES($studentId, 'Test', 'User', 't@usey', 1, 0, 0, NULL)",
      sqlu"INSERT INTO cohorts VALUES($cohortId, $teacherId, 'Test Cohort', NULL)",
      sqlu"INSERT INTO cohort_members VALUES($cohortId, $studentId)",
      sqlu"INSERT INTO content_folders VALUES($cohortFolderId, $teacherId, 'Folder Name', NULL)",
      sqlu"INSERT INTO content_packages VALUES($packageId, 'Package Name', $cohortFolderId, $teacherId)",
      sqlu"INSERT INTO content_packages VALUES($package2Id, 'Package Name2', $cohortFolderId, $teacherId)",
      sqlu"INSERT INTO content_items VALUES($item1Id, $packageId, 'imageUrl', 'content', 'name')",
      sqlu"INSERT INTO content_items VALUES($item2Id, $package2Id, 'imageUrl', 'content2', 'name2')",
      sqlu"INSERT INTO content_assessment_questions VALUES($question1Id, 'question', 'MCSA', $item1Id)",
      sqlu"INSERT INTO content_assessment_answers VALUES($answer1Id, $question1Id, 'answer', 1, 0)",
      sqlu"INSERT INTO content_assessment_questions VALUES($question2Id, 'question', 'MCSA', $item2Id)",
      sqlu"INSERT INTO content_assessment_answers VALUES($answer2Id, $question2Id, 'answer', 1, 0)",
      sqlu"INSERT INTO content_assigned VALUES($assigned1Id, 'Test Exam', '2017-10-01', 1, $teacherId)",
      sqlu"INSERT INTO content_assigned VALUES($assigned2Id, 'Test Exam 2', '2017-10-01', 1, $teacherId)",
      sqlu"INSERT INTO content_assigned_cohorts VALUES($assigned1Id, $cohortId)",
      sqlu"INSERT INTO content_assigned_packages VALUES($assigned1Id, $packageId)",
      sqlu"INSERT INTO content_assigned_cohorts VALUES($assigned2Id, $cohortId)",
      sqlu"INSERT INTO content_assigned_packages VALUES($assigned2Id, $packageId)",
      sqlu"INSERT INTO content_disabled VALUES($item2Id, $studentId)",
    )
  }

  def getDisabledQuery(itemId: Int, userId: Int) = sql"""
      SELECT cd.ContentAssignedId, cd.UserId
      FROM content_disabled as cd
      WHERE cd.ContentAssignedId = $itemId
      AND cd.userId = $userId
    """.as[ContentDisabledDto]

  def removeStudyContentQueries(teacherId: Int, studentId: Int): DBIO[Unit] = DBIO.seq(
    sqlu"DELETE FROM users WHERE id = $teacherId",
    sqlu"DELETE FROM users WHERE id = $studentId",
  )

  def insertStudyContent(teacherId: Int, studentId: Int) = {
    val response = db.run(insertStudyContentQueries(teacherId, studentId))
    Await.result(response, 10 seconds)
  }

  def clearStudyContent(teacherId: Int, studentId: Int) = {
    val response = db.run(removeStudyContentQueries(teacherId, studentId))
    Await.result(response, 10 seconds)
  }

  def getDisabled(itemId: Int, userId: Int): Future[Option[ContentDisabledDto]] = {
    db.run(getDisabledQuery(itemId, userId).headOption)
  }

}
