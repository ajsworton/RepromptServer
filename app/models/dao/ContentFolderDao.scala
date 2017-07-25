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

import models.dto.ContentFolderDto

import scala.concurrent.Future

trait ContentFolderDao extends Dao[ContentFolderDto] {
  /**
   * locate a folder by Id
   * @param folderId the id to match on
   * @return a future folder
   */
  def find(folderId: Int): Future[Option[ContentFolderDto]]

  /**
   * locate a set of folders by owner Id
   * @param ownerId the owner Id to match on
   * @return future folders
   */
  def findByOwner(ownerId: Int): Future[Seq[ContentFolderDto]]

  /**
   * Save a folder
   * @param folder the folder to save
   * @return a future folder
   */
  def save(folder: ContentFolderDto): Future[Option[ContentFolderDto]]

  /**
   * Update an existing folder
   * @param folder the folder data to update (match by folder Id)
   * @return
   */
  def update(folder: ContentFolderDto): Future[Option[ContentFolderDto]]

  /**
   * Delete a folder
   * @param folderId the folder id to delete
   * @return a future number of affected rows
   */
  def delete(folderId: Int): Future[Int]

  /**
   * Delete all folders for an ownerId
   * @param ownerId the folder id to delete
   * @return a future number of affected rows
   */
  def deleteByOwner(ownerId: Int): Future[Int]
}
