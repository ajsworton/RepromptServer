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
import models.dto.{ContentDisabledDto, ScoreDto}
import models.{Profile, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{JdbcProfile, SetParameter}
import slick.sql.SqlStreamingAction

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class TestingDbQueries @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  private def insertStudyContentQueries(teacherId: Int, studentId: Int, otherStudentId: Int): DBIO[Unit] = {
    val cohortId: Int        = teacherId
    val cohortId2: Int       = studentId
    val cohortId3: Int       = teacherId - 1
    val contentFolderId: Int = studentId
    val packageId: Int       = teacherId
    val packageId2: Int      = studentId
    val packageId3: Int      = teacherId - 1
    val item1Id              = teacherId
    val item2Id              = studentId
    val question1Id          = teacherId
    val answer1Id            = teacherId
    val question2Id          = studentId
    val answer2Id            = studentId
    val assigned1Id          = teacherId
    val assigned2Id          = studentId

    DBIO.seq(
      // Insert some suppliers
      sqlu"INSERT INTO users VALUES($teacherId, 'Testy', 'Teachy', 't@teachy', 'true', 'true', 'false', NULL)",
      sqlu"INSERT INTO users VALUES($studentId, 'Test', 'User', 't@usey', 'true', 'false', 'false', NULL)",
      sqlu"""INSERT INTO profiles VALUES($teacherId, 'credentials', 't@teachy', 'true', NULL, NULL, NULL, NULL, '{"hasher": "bcrypt", "password": "$$2a$$10$$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}', NULL, NULL, NULL)""",
      sqlu"""INSERT INTO profiles VALUES($studentId, 'credentials', 't@usey', 'true', NULL, NULL, NULL, NULL, '{"hasher": "bcrypt", "password": "$$2a$$10$$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}', NULL, NULL, NULL)""",
      sqlu"INSERT INTO users VALUES($otherStudentId, 'Test', 'User2', 't@usey2', 'true', 'false', 'false', NULL)",
      sqlu"INSERT INTO profiles VALUES($otherStudentId, 'credentials', 't@usey2', 'true', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)",
      sqlu"INSERT INTO cohorts VALUES($cohortId, $teacherId, 'Test Cohort', NULL)",
      sqlu"INSERT INTO cohorts VALUES($cohortId2, $teacherId, 'Test Cohort2', NULL)",
      sqlu"INSERT INTO cohorts VALUES($cohortId3, $teacherId, 'Test Cohort3', NULL)",
      sqlu"INSERT INTO cohort_members VALUES($cohortId, $studentId)",
      sqlu"INSERT INTO content_folders VALUES($contentFolderId, $teacherId, 'Folder Name', NULL)",
      sqlu"INSERT INTO content_packages VALUES($packageId, 'Package Name', $contentFolderId, $teacherId)",
      sqlu"INSERT INTO content_packages VALUES($packageId2, 'Package Name2', $contentFolderId, $teacherId)",
      sqlu"INSERT INTO content_packages VALUES($packageId3, 'Package Name3', $contentFolderId, $teacherId)",
      sqlu"INSERT INTO content_items VALUES($item1Id, $packageId, 'image/url.png', 'content', 'name')",
      sqlu"INSERT INTO content_items VALUES($item2Id, $packageId2, 'image/url.png', 'content2', 'name2')",
      sqlu"INSERT INTO content_assessment_questions VALUES($question1Id, 'question', 'MCSA', $item1Id)",
      sqlu"INSERT INTO content_assessment_answers VALUES($answer1Id, $question1Id, 'answer', 'true', 0)",
      sqlu"INSERT INTO content_assessment_questions VALUES($question2Id, 'question', 'MCSA', $item2Id)",
      sqlu"INSERT INTO content_assessment_answers VALUES($answer2Id, $question2Id, 'answer', 'true', 0)",
      sqlu"INSERT INTO content_assigned VALUES($assigned1Id, 'Test Exam', '2017-10-01', 'true', $item1Id)",
      sqlu"INSERT INTO content_assigned VALUES($assigned2Id, 'Test Exam 2', '2017-10-01', 'true', $item1Id)",
      sqlu"INSERT INTO content_assigned_cohorts VALUES($assigned1Id, $cohortId)",
      sqlu"INSERT INTO content_assigned_packages VALUES($assigned1Id, $packageId)",
      sqlu"INSERT INTO content_assigned_cohorts VALUES($assigned2Id, $cohortId)",
      sqlu"INSERT INTO content_assigned_packages VALUES($assigned2Id, $packageId2)",
      sqlu"INSERT INTO content_disabled VALUES($item2Id, $studentId)",
    )
  }

  private def getDisabledQuery(itemId: Int, userId: Int) = sql"""
      SELECT cd.content_assigned_id, cd.user_id
      FROM content_disabled as cd
      WHERE cd.content_assigned_id = $itemId
      AND cd.user_id = $userId
    """.as[ContentDisabledDto]

  private def removeStudyContentQueries(teacherId: Int, studentId: Int, otherStudentId: Int): DBIO[Unit] = DBIO.seq(
    sqlu"DELETE FROM users WHERE id = $teacherId",
    sqlu"DELETE FROM users WHERE id = $studentId",
    sqlu"DELETE FROM users WHERE id = $otherStudentId",
  )

  def insertStudyContent(teacherId: Int, studentId: Int, otherStudentId: Int): Unit = {
    val response = db.run(insertStudyContentQueries(teacherId, studentId, otherStudentId))
    Await.result(response, 10 seconds)
  }

  def clearStudyContent(teacherId: Int, studentId: Int, otherStudentId: Int): Unit = {
    val response = db.run(removeStudyContentQueries(teacherId, studentId, otherStudentId))
    Await.result(response, 10 seconds)
  }

  def getDisabled(itemId: Int, userId: Int): Future[Option[ContentDisabledDto]] =
    db.run(getDisabledQuery(itemId, userId).headOption)

  def deleteUser(userId: Int): Future[Int] =
    db.run(sqlu"DELETE FROM users WHERE id = $userId")

  def getUser(userId: Int): Future[Option[User]] =
    db.run(getUserQuery(userId).headOption) flatMap {
      case None                      => Future(None)
      case Some((user, userProfile)) => Future(Some(user.copy(profiles = List(userProfile))))
    }

  private def getUserQuery(userId: Int) = sql"""
  SELECT DISTINCT u.Id, u.first_name, u.surname, u.email, u.is_email_verified, u.is_educator,
  u.is_administrator, u.avatar_url,
  p.user_id, p.provider_id, p.provider_key, p.Confirmed, p.Email, p.first_name, p.last_name, p.full_name,
  p.password_info, p.oauth1_info, p.oauth2_info, p.avatar_url

  FROM users AS u

    LEFT JOIN profiles AS p
    ON p.user_id = u.Id

  WHERE u.Id = $userId

  LIMIT 1
    """.as[(User, Profile)]

  private def getAssignedCohortQuery(assignedId: Int, cohortId: Int) = sql"""
      SELECT cac.assigned_id, cac.cohort_id
      FROM content_assigned_cohorts as cac
      WHERE cac.assigned_id = $assignedId
      AND cac.cohort_id = $cohortId
      LIMIT 1
    """.as[(Int, Int)]

  def getAssignedCohort(assignedId: Int, cohortId: Int): Future[Option[(Int, Int)]] =
    db.run(getAssignedCohortQuery(assignedId, cohortId).headOption)

  private def getAssignedPackageQuery(assignedId: Int, packageId: Int) = sql"""
      SELECT cap.assigned_id, cap.package_id
      FROM content_assigned_packages as cap
      WHERE cap.assigned_id = $assignedId
      AND cap.package_id = $packageId
      LIMIT 1
    """.as[(Int, Int)]

  def getAssignedPackage(assignedId: Int, packageId: Int): Future[Option[(Int, Int)]] =
    db.run(getAssignedPackageQuery(assignedId, packageId).headOption)

  implicit val optionalLocalDateSetter: SetParameter[Option[LocalDate]] = SetParameter[Option[LocalDate]] {
    case (Some(l), params) => params.setDate(Date.valueOf(l.toString))
    case (None, params)    => params.setNull(Types.DATE)
  }

  private def getScoreDataQuery(score: ScoreDto): SqlStreamingAction[Vector[ScoreDto], ScoreDto, Effect] = sql"""
      SELECT cs.user_id, cs.content_item_id, cs.Score, cs.score_date, cs.Streak, cs.reprompt_date
      FROM content_scores as cs
      WHERE cs.user_id = ${score.userId}
      AND cs.content_item_id = ${score.contentItemId}
      AND cs.score_date = ${score.scoreDate}
      LIMIT 1
    """.as[ScoreDto]

  def getScoreData(score: ScoreDto): Future[Option[ScoreDto]] =
    db.run(getScoreDataQuery(score).headOption)

  def deleteScoreData(score: ScoreDto): Future[Int] =
    db.run(sqlu"""DELETE FROM content_scores
      WHERE user_id = ${score.userId}
      AND content_item_id = ${score.contentItemId}
      AND score_date = ${score.scoreDate}
      """)

}
