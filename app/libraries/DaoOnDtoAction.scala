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

package libraries

import javax.inject.Inject

import models.dao.Dao
import models.dto.Dto
import play.api.libs.json.{ Json, OFormat }
import play.api.mvc.{ Results, Result }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * DaoOnDtoAction is a helper class to apply common functions between related Daos and Dtos.
 * @param ex
 */
class DaoOnDtoAction @Inject() (implicit ex: ExecutionContext) {

  /**
   * Save a Dto using a supplied Dao.
   * @param dao injected model
   * @param dto injected data object
   * @param ex injected execution context to execute futures
   * @param jsf injected json formatter
   * @tparam T generic type for constraint
   * @return a future result
   */
  def saveDto[T <: Dto](dao: Dao[T], dto: T)(implicit ex: ExecutionContext, jsf: OFormat[T]): Future[Result] = {
    dao.save(dto) flatMap {
      case Some(saveResponse) => Future(Results.Ok(Json.toJson(saveResponse)))
      case _ => Future(Results.InternalServerError(Json.toJson("Unable to save data")))
    }
  }

  /**
   * Update a Dto using a supplied Dao.
   * @param dao injected model
   * @param dto injected data object
   * @param jsf injected json formatter
   * @tparam T generic type for constraint
   * @return a future result
   */
  def updateDto[T <: Dto](dao: Dao[T], dto: T)(implicit jsf: OFormat[T]): Future[Result] = {
    dao.update(dto) flatMap {
      updateResponse => Future(Results.Ok(Json.toJson(updateResponse.get)))
    }
  }

  /**
   * Determine whether to save or update a supplied Dto
   * @param dao injected model
   * @param dto injected data object
   * @param jsf injected json formatter
   * @tparam T generic type for constraint
   * @return a future result
   */
  def validateAndSaveDto[T <: Dto](dao: Dao[T], dto: T)(implicit jsf: OFormat[T]): Future[Result] = {
    //check if exists
    if (dto.id.isDefined) {
      val existing = dao.find(dto.id.get)
      existing flatMap {
        case None => saveDto(dao, dto)
        case Some(_) => updateDto(dao, dto)
      }
    } else {
      // save
      saveDto(dao, dto)
    }
  }
}
