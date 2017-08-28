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

import java.time.LocalDate

import models.User
import models.dto.{ ContentAssignedDto, ContentItemDto, ExamHistoryDto, ScoreDto }

import scala.concurrent.Future

trait StudyDao {

  /**
   * Get all content items for this user based on cohort membership.
   * @param userId the userId to restrict on
   * @return
   */
  def getContentItems(userId: Int): Future[List[ContentItemDto]]

  /**
   * Save the provided score data for a content item
   * @return a future optional scoreDto
   */
  def saveScoreData(scoreData: ScoreDto): Future[Either[String, ScoreDto]]

  /**
   * Retrieve the examination date for a specific content item.
   * @param contentItemId the content item id.
   * @return
   */
  def getExamDateByContentItemId(contentItemId: Int): Future[Option[LocalDate]]

  /**
   * Retrieve all content items that apply to a user included their enabled status
   * @param userId the supplied userId
   * @return the Future List of ContentItemDto
   */
  def getContentAssignedStatusByUserId(userId: Int): Future[List[ContentAssignedDto]]

  /**
   * Write an entry to flag a specified contentItem as disabled
   * @param assignedId the supplied content item
   * @param UserId the supplied userId
   * @return a future int to indicate the number of affected rows
   */
  def disableContentAssigned(assignedId: Int, UserId: Int): Future[Int]

  /**
   * Remove any entry (if exists) that disables the specified contentItem for the user.
   * @param assignedId the supplied content item
   * @param UserId the supplied userId
   * @return a future int to indicate the number of affected rows
   */
  def enableContentAssigned(assignedId: Int, UserId: Int): Future[Int]

  /**
   * Get a users historical assessment performance data
   * @param userId the user's assessment history
   * @return a list of ExamHistoryDto containing all the data
   */
  def getHistoricalPerformanceByExam(userId: Int): Future[List[ExamHistoryDto]]

  /**
   * Get a list of all students with pending content. Primarily to run the notification service.
   * @return a list of all students who have pending content
   */
  def getStudentsWithPendingContent: Future[Seq[User]]

}
