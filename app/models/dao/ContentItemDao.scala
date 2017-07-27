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

import models.dto.ContentItemDto

import scala.concurrent.Future

trait ContentItemDao extends Dao[ContentItemDto] {
  /**
   * locate a item by Id
   * @param itemId the id to match on
   * @return a future cohort
   */
  def find(itemId: Int): Future[Option[ContentItemDto]]

  /**
   * Save a item
   * @param itemDto the item to save
   * @return a future item
   */
  override def save(itemDto: ContentItemDto): Future[Option[ContentItemDto]]

  /**
   * Update an existing cohort
   * @param itemDto the cohort data to update (match by cohort Id)
   * @return
   */
  def update(itemDto: ContentItemDto): Future[Option[ContentItemDto]]

  /**
   * Delete a item
   * @param itemId the item id to delete
   * @return a future number of affected rows
   */
  def delete(itemId: Int): Future[Int]
}
