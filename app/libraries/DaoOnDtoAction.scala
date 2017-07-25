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

import models.dao.Dao
import models.dto.Dto
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}

import scala.concurrent.Future

object DaoOnDtoAction {

  def saveDto[T <: Dto](dao: Dao[T], dto: T): Future[Result] = {
    implicit val dtoFormat = Json.format[T]
    val saveResponse = dao.save(dto)
    saveResponse flatMap {
      r => Future(Results.Ok(Json.toJson(r.get)))
    }
  }

  def updateDto[T <: Dto](dao: Dao[T], dto: T): Future[Result] = {
    val updateResponse = dao.update(dto)
    updateResponse flatMap {
      r => Future(Results.Ok(Json.toJson(r.get)))
    }
  }

  def validateAndSaveDto[T <: Dto](dao: Dao[T], dto: T): Future[Result] = {
    //check if exists
    if (dto.id.isDefined) {
      val existing = dao.find(dto.id.get)
      existing flatMap {
        case None => DaoOnDtoAction.saveDto(dao, dto)
        case Some(_) => DaoOnDtoAction.updateDto(dao, dto)
      }
    } else {
      // save
      DaoOnDtoAction.saveDto(dao, dto)
    }
  }
}
