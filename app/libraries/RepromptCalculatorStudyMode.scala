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
import java.time.LocalDate

import models.dto.ScoreDto

class RepromptCalculatorStudyMode extends RepromptCalculator {

  /**
   * add the calculated reprompt date to the scoreDto and return it.
   * @param score a supplied ScoreDto.
   * @param examinationDate the upcoming examination date.
   * @return the ScoreDto with the embedded date for next content exposure.
   */
  override def addRepromptDate(score: ScoreDto, examinationDate: LocalDate): ScoreDto = {
    score.copy(repromptDate = getRepromptDate(score, examinationDate))
  }

  /**
   * Calculate and retrieve the date for next prompting, or none if the examination occurs first.
   * @param score the scoreDto.
   * @param examinationDate the upcoming examination date.
   * @return an optional date for next exposure
   */
  private def getRepromptDate(score: ScoreDto, examinationDate: LocalDate): Option[LocalDate] = {
    val dayInterval = score.streak
    val repromptDate = score.scoreDate.plusDays(dayInterval)
    val tomorrow = score.scoreDate.plusDays(1)
    selectDate(repromptDate, tomorrow, examinationDate)
  }

  /**
   * Logic to determine which date to use
   * @param repromptDate the ideal calculated reprompt date
   * @param tomorrow the date after the score date
   * @param examDate the date of the pending exam
   * @return an optional date for next exposure
   */
  private def selectDate(repromptDate: LocalDate, tomorrow: LocalDate, examDate: LocalDate): Option[LocalDate] = {
    if (repromptDate.isBefore(examDate)) { Some(repromptDate) }
    else if (tomorrow.isBefore(examDate)) { Some(tomorrow) }
    else { None }
  }
}
