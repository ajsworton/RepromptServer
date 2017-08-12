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

import models.dto.{ContentAssignedDto, ContentItemDto, ScoreDto}

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
  def saveScoreData(scoreData: ScoreDto): Future[Option[ScoreDto]]


  /**
    * Retrieve the examination date for a specific content item.
    * @param contentItemId the content item id.
    * @return
    */
  def getExamDateByContentItemId(contentItemId: Int): Future[Option[LocalDate]]

}
