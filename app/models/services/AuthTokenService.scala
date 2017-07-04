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

import java.util.UUID

import models.AuthToken

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait AuthTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userID The user ID for which the token should be created.
   * @param expiry The duration a token expires.
   * @return The saved auth token.
   */
  def create(userID: Int, expiry: FiniteDuration = 5 minutes): Future[AuthToken]

  /**
   * Validates a token ID.
   *
   * @param tokenId The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(tokenId: UUID): Future[Option[AuthToken]]

  /**
   * Cleans expired tokens.
   *
   * @return The list of deleted tokens.
   */
  def clean: Future[Seq[AuthToken]]
}
