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

import models.dto.ContentAssignedDto

import scala.concurrent.{ ExecutionContext, Future }

trait ContentAssignedDao extends Dao[ContentAssignedDto] {
  /**
   * locate assigned content by Id
   * @param assignedContentId the id to match on
   * @return a future assigned content
   */
  override def find(assignedContentId: Int): Future[Option[ContentAssignedDto]]

  /**
   * locate assigned content by owner Id
   * @param ownerId the owner Id to match on
   * @return future assigned content
   */
  def findByOwner(ownerId: Int): Future[Seq[ContentAssignedDto]]

  /**
   * Save assigned content
   * @param content the assigned content to save
   * @return a future assigned content
   */
  override def save(content: ContentAssignedDto): Future[Option[ContentAssignedDto]]

  /**
   * Update an existing assigned content
   * @param content the assigned content data to update (match by content Id)
   * @return
   */
  override def update(content: ContentAssignedDto): Future[Option[ContentAssignedDto]]

  /**
   * Delete assigned content
   * @param contentId the assigned content id to delete
   * @return a future number of affected rows
   */
  def delete(contentId: Int): Future[Int]

  /**
   * Delete all assigned content for an ownerId
   * @param ownerId the owner id to delete assigned content
   * @return a future number of affected rows
   */
  def deleteByOwner(ownerId: Int): Future[Int]

  /**
   * Attach a cohort to assigned content
   * @param assignedId
   * @param cohortId
   * @return
   */
  def attachCohort(assignedId: Int, cohortId: Int): Future[Either[String, Int]]

  /**
   * Detach a cohort from assigned content
   * @param assignedId
   * @param cohortId
   * @return
   */
  def detachCohort(assignedId: Int, cohortId: Int): Future[Either[String, Int]]

  /**
   * Attach a package to assigned content
   * @param assignedId
   * @param packageId
   * @return
   */
  def attachPackage(assignedId: Int, packageId: Int): Future[Either[String, Int]]

  /**
   * Detach a package from assigned content
   * @param assignedId
   * @param packageId
   * @return
   */
  def detachPackage(assignedId: Int, packageId: Int): Future[Either[String, Int]]
}
