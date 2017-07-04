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

import java.time.LocalDateTime
import java.util.UUID

import models.AuthToken

import scala.concurrent.Future

/**
 * Give access to the [[AuthToken]] object.
 */
trait AuthTokenDao {

  /**
   * Finds a token by its ID.
   *
   * @param tokenId The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(tokenId: UUID): Future[Option[AuthToken]]

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current local date time.
   */
  def findExpired(dateTime: LocalDateTime): Future[Seq[AuthToken]]

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken]

  /**
   * Removes the token for the given ID.
   *
   * @param tokenId The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(tokenId: UUID): Future[Unit]
}
