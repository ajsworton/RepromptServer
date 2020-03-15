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

package models.dto

import play.api.libs.json.{ Json, OFormat }
import slick.jdbc.GetResult

case class ExamHistoryDto(name: String, series: List[ExamHistoricalPoint] = Nil)

/**
 * Companion Object for to hold boiler plate for forms, json conversion, slick
 */
object ExamHistoryDto {

  /**
   * implicit converter to coerce direct sql query into data object
   */
  implicit val getExamHistoryDtoResult: GetResult[ExamHistoryDto] = GetResult(r =>
    ExamHistoryDto(
      r.nextString
    ))

  /**
   * implicit json conversion formatter
   */
  implicit val serializer: OFormat[ExamHistoryDto] = Json.format[ExamHistoryDto]
}