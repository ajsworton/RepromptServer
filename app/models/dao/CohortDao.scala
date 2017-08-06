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

import models.dto.CohortDto
import scala.concurrent.Future

trait CohortDao extends Dao[CohortDto] {

  /**
   * locate a cohort by Id
   * @param cohortId the id to match on
   * @return a future cohort
   */
  override def find(cohortId: Int): Future[Option[CohortDto]]

  /**
   * locate a set of cohorts by owner Id
   * @param ownerId the owner Id to match on
   * @return future cohorts
   */
  def findByOwner(ownerId: Int): Future[Seq[CohortDto]]

  /**
   * Save a cohort
   * @param cohort the cohort to save
   * @return a future cohort
   */
  override def save(cohort: CohortDto): Future[Option[CohortDto]]

  /**
   * Update an existing cohort
   * @param cohort the cohort data to update (match by cohort Id)
   * @return
   */
  override def update(cohort: CohortDto): Future[Option[CohortDto]]

  /**
   * Delete a cohort
   * @param cohortId the cohort id to delete
   * @return a future number of affected rows
   */
  def delete(cohortId: Int): Future[Int]

  /**
   * Delete all cohorts for an ownerId
   * @param ownerId the cohort id to delete
   * @return a future number of affected rows
   */
  def deleteByOwner(ownerId: Int): Future[Int]

  /**
   *
   * @param userId
   * @param cohortId
   * @return
   */
  def attach(cohortId: Int, userId: Int): Future[Int]

  /**
   *
   * @param cohortId
   * @param userId
   * @return
   */
  def detach(cohortId: Int, userId: Int): Future[Int]

}
