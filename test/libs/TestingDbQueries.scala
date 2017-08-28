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

import java.sql.{Date, Types}
import java.time.LocalDate
import javax.inject.Inject

import models.{Profile, User}
import models.dto.{ContentDisabledDto, ContentItemDto, ScoreDto}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.dbio.DBIO
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import slick.jdbc.{JdbcProfile, SetParameter}
import slick.jdbc.MySQLProfile.api._

class TestingDbQueries @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {


  private def insertStudyContentQueries(teacherId: Int, studentId: Int, otherStudentId: Int): DBIO[Unit] = {
    val cohortId:Int = studentId
    val cohortId2:Int = teacherId
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
      sqlu"INSERT INTO profiles VALUES($studentId, 'credentials', 't@usey', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)",
      sqlu"INSERT INTO users VALUES($otherStudentId, 'Test', 'User2', 't@usey2', 1, 0, 0, NULL)",
      sqlu"INSERT INTO profiles VALUES($otherStudentId, 'credentials', 't@usey2', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)",
      sqlu"INSERT INTO cohorts VALUES($cohortId, $teacherId, 'Test Cohort', NULL)",
      sqlu"INSERT INTO cohorts VALUES($cohortId2, $teacherId, 'Test Cohort2', NULL)",
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
      sqlu"INSERT INTO content_assigned VALUES($assigned1Id, 'Test Exam', '2017-10-01', 1, $item1Id)",
      sqlu"INSERT INTO content_assigned VALUES($assigned2Id, 'Test Exam 2', '2017-10-01', 1, $item1Id)",
      sqlu"INSERT INTO content_assigned_cohorts VALUES($assigned1Id, $cohortId)",
      sqlu"INSERT INTO content_assigned_packages VALUES($assigned1Id, $package2Id)",
      sqlu"INSERT INTO content_assigned_cohorts VALUES($assigned2Id, $cohortId)",
      sqlu"INSERT INTO content_assigned_packages VALUES($assigned2Id, $package2Id)",
      sqlu"INSERT INTO content_disabled VALUES($item2Id, $studentId)",
    )
  }

  private def getDisabledQuery(itemId: Int, userId: Int) = sql"""
      SELECT cd.ContentAssignedId, cd.UserId
      FROM content_disabled as cd
      WHERE cd.ContentAssignedId = $itemId
      AND cd.UserId = $userId
    """.as[ContentDisabledDto]

  private def removeStudyContentQueries(teacherId: Int, studentId: Int, otherStudentId: Int):
    DBIO[Unit] = DBIO.seq(
    sqlu"DELETE FROM users WHERE id = $teacherId",
    sqlu"DELETE FROM users WHERE id = $studentId",
    sqlu"DELETE FROM users WHERE id = $otherStudentId",
  )

  def insertStudyContent(teacherId: Int, studentId: Int, otherStudentId: Int) = {
    val response = db.run(insertStudyContentQueries(teacherId, studentId, otherStudentId))
    Await.result(response, 10 seconds)
  }

  def clearStudyContent(teacherId: Int, studentId: Int, otherStudentId: Int) = {
    val response = db.run(removeStudyContentQueries(teacherId, studentId, otherStudentId))
    Await.result(response, 10 seconds)
  }

  def getDisabled(itemId: Int, userId: Int): Future[Option[ContentDisabledDto]] = {
    db.run(getDisabledQuery(itemId, userId).headOption)
  }

  def deleteUser(userId: Int): Future[Int] = {
    db.run(sqlu"DELETE FROM users WHERE id = $userId")
  }

  def getUser(userId: Int): Future[Option[User]] = {
    db.run(getUserQuery(userId).headOption) flatMap {
      case None => Future(None)
      case Some((user, userProfile)) => Future(Some(user.copy(profiles = List(userProfile))))
    }
  }

  private def getUserQuery(userId: Int) = sql"""
  SELECT DISTINCT u.Id, u.FirstName, u.Surname, u.email, u.isEmailVerified, u.isEducator,
  u.isAdministrator, u.avatarUrl,
  p.UserId, p.ProviderId, p.ProviderKey, p.Confirmed, p.Email, p.FirstName, p.LastName, p.FullName,
  p.PasswordInfo, p.OAuth1Info, p.OAuth2Info, p.AvatarUrl

  FROM users AS u

    LEFT JOIN profiles AS p
    ON p.UserId = u.Id

  WHERE u.Id = $userId

  LIMIT 1
    """.as[(User, Profile)]

  private def getAssignedCohortQuery(assignedId: Int, cohortId: Int) = sql"""
      SELECT cac.AssignedId, cac.CohortId
      FROM content_assigned_cohorts as cac
      WHERE cac.AssignedId = $assignedId
      AND cac.CohortId = $cohortId
      LIMIT 1
    """.as[(Int, Int)]

  def getAssignedCohort(assignedId: Int, cohortId: Int): Future[Option[(Int, Int)]] = {
    db.run(getAssignedCohortQuery(assignedId, cohortId).headOption)
  }

  private def getAssignedPackageQuery(assignedId: Int, packageId: Int) = sql"""
      SELECT cap.AssignedId, cap.PackageId
      FROM content_assigned_packages as cap
      WHERE cap.AssignedId = $assignedId
      AND cap.PackageId = $packageId
      LIMIT 1
    """.as[(Int, Int)]

  def getAssignedPackage(assignedId: Int, packageId: Int): Future[Option[(Int, Int)]] = {
    db.run(getAssignedPackageQuery(assignedId, packageId).headOption)
  }

  implicit val optionalLocalDateSetter = SetParameter[Option[LocalDate]] {
    case (Some(l), params) => params.setDate(Date.valueOf(l.toString))
    case (None, params) => params.setNull(Types.DATE)
  }

  private def getScoreDataQuery(score: ScoreDto) = sql"""
      SELECT cs.UserId, cs.ContentItemId, cs.Score, cs.ScoreDate, cs.Streak, cs.RepromptDate
      FROM content_scores as cs
      WHERE cs.UserId = ${score.userId}
      AND cs.ContentItemId = ${score.contentItemId}
      AND cs.ScoreDate = ${score.scoreDate}
      LIMIT 1
    """.as[ScoreDto]

  def getScoreData(score: ScoreDto): Future[Option[ScoreDto]] = {
    db.run(getScoreDataQuery(score).headOption)
  }

  def deleteScoreData(score: ScoreDto): Future[Int] = {
    db.run(sqlu"""DELETE FROM content_scores
      WHERE UserId = ${score.userId}
      AND ContentItemId = ${score.contentItemId}
      AND ScoreDate = ${score.scoreDate}
      """)
  }

}
