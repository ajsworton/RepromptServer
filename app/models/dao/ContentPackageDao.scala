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

import models.dto.ContentPackageDto

import scala.concurrent.{ ExecutionContext, Future }

trait ContentPackageDao extends Dao[ContentPackageDto] {
  /**
   * locate a package by Id
   * @param packageId the id to match on
   * @return a future cohort
   */
  def find(packageId: Int): Future[Option[ContentPackageDto]]

  /**
   * locate a set of packages by owner Id
   * @param ownerId the owner Id to match on
   * @return future package
   */
  def findByOwner(ownerId: Int): Future[Seq[ContentPackageDto]]

  /**
   * Save a package
   * @param packageDto the package to save
   * @return a future package
   */
  override def save(packageDto: ContentPackageDto): Future[Option[ContentPackageDto]]

  /**
   * Update an existing cohort
   * @param packageDto the cohort data to update (match by cohort Id)
   * @return
   */
  def update(packageDto: ContentPackageDto): Future[Option[ContentPackageDto]]

  /**
   * Delete a package
   * @param packageId the package id to delete
   * @return a future number of affected rows
   */
  def delete(packageId: Int): Future[Int]

  /**
   * Delete all packages for an ownerId
   * @param ownerId the package id to delete
   * @return a future number of affected rows
   */
  def deleteByOwner(ownerId: Int): Future[Int]
}
