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

/**
 * This code substantially based on the silhouette framework exemplar
 * accessed 04/07/2017
 * https://github.com/mohiva/play-silhouette-seed/
 */

package models.services

import com.mohiva.play.silhouette.api.services.IdentityService
import models.{Profile, User}

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: Int): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
  * Saves the social profile for a user.
  *
  * If a user exists for this profile then update the user, otherwise create a new user with the
  * given profile.
  *
  * @param profile The social profile to save.
  * @return The user for whom the profile was saved.
  */
  def save(profile: Profile): Future[User]
}
