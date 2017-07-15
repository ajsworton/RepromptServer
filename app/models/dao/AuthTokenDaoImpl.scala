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
import scala.collection.mutable
import scala.concurrent.Future

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDaoImpl extends AuthTokenDao {

  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]] = Future.successful(tokens.get(id))

  /**
   * Finds expired tokens.
   *
   * @param localDateTime The current date time.
   */
  def findExpired(localDateTime: LocalDateTime): Future[Seq[AuthToken]] = Future.successful {
    tokens.filter {
      case (_, token) =>
        token.expiry.isBefore(localDateTime)
    }.values.toSeq
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] = {
    tokens += (token.tokenId -> token)
    Future.successful(token)
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Unit] = {
    tokens -= id
    Future.successful(())
  }
}
