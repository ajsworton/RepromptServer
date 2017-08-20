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

import models.dto.Dto

import scala.concurrent.{ ExecutionContext, Future }

trait Dao[T <: Dto] {

  /**
   * locate a dto by Id
   * @param id the id to match on
   * @return a future dto
   */
  def find(id: Int): Future[Option[T]]

  /**
   * Save a dto
   * @param dto the dto to save
   * @return a future dto
   */
  def save(dto: T): Future[Option[T]]

  /**
   * Update an existing cohort
   * @param dto the data to update (match by Id)
   * @return
   */
  def update(dto: T): Future[Option[T]]
}
