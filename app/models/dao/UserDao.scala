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

import com.mohiva.play.silhouette.api.LoginInfo
import models.{ Profile, User }
import scala.concurrent.Future

trait UserDao {

  /**
   *
   * @param user
   * @return
   */
  def save(user: User): Future[Option[User]]

  /**
   * Finds and returns an option[User] matching the userId
   * @param userId the user Id to match on
   * @return a future optional user
   */
  def find(userId: Int): Future[Option[User]]

  /**
   * Finds and returns an option[User] matching the loginInfo
   * @param loginInfo the loginInfo to match on
   * @return a future optional user
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Deletes a user and all associated profiles
   * @param userId the user Id to match on
   * @return a future number of affected rows
   */
  def delete(userId: Int): Future[Int]

  /**
   * Deletes a user profile that matches the supplied loginInfo
   * @param loginInfo the loginInfo to match on
   * @return a future number of affected rows
   */
  def delete(loginInfo: LoginInfo): Future[Int]

  /**
   * Confirm the profile associated with the loginInfo
   * @param loginInfo the loginInfo to match on
   * @return a future user
   */
  def confirm(loginInfo: LoginInfo): Future[Int]

  /**
   * Link a profile to an existing user
   * @param user the target user
   * @param profile the profile to link
   * @return a future user
   */
  def link(user: User, profile: Profile): Future[Option[User]]

  /**
   * Update a stored user profile
   * @param profile the profile to update
   * @return a future user
   */
  def update(profile: Profile): Future[Option[User]]

  /**
   * Update a stored user
   * @param user the user to update
   * @return a future user
   */
  def update(user: User): Future[Option[User]]

  /**
   * Check if a duplicate record already exists
   * @param user
   * @return a future boolean
   */
  def checkDuplicate(user: User): Future[Boolean]
}
