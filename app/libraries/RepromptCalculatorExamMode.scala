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
import java.time.temporal.ChronoUnit

import models.dto.ScoreDto

class RepromptCalculatorExamMode(percentage: Int = 20) extends RepromptCalculator {

  /**
   * In study mode, the reprompt date is set as a date in the 10-20% range of
   * the time remaining until the examination date. For the purposes of the initial
   * setup, 20% will be used as the value.
   * This will be configurable via a constructor argument.
   * @param score a ScoreDto
   * @param examinationDate
   */
  override def addRepromptDate(score: ScoreDto, examinationDate: LocalDate): ScoreDto = {
    val daysBetweenDates = ChronoUnit.DAYS.between(score.scoreDate, examinationDate)
    if (daysBetweenDates > 0) {
      score.copy(repromptDate = calcPromptDate(
        daysBetweenDates,
        score.scoreDate,
        examinationDate))
    } else { score.copy(repromptDate = None) }
  }

  /**
   * Perform calculation of the next prompt date. Return an optional date if the date is before
   * the examination date, None otherwise.
   * @param daysBetweenDates the number of days separating the score date and the exam date
   * @param scoreDate the date of the last assessment
   * @param examDate the date of the examination
   * @return an optional date for the next assessment
   */
  private def calcPromptDate(daysBetweenDates: Long, scoreDate: LocalDate, examDate: LocalDate): Option[LocalDate] = {
    val daysToNextExposure = daysBetweenDates * percentage / 100
    val promptDate = scoreDate.plusDays(daysToNextExposure)
    if (promptDate.isBefore(examDate) && promptDate.isAfter(scoreDate)) { Some(promptDate) }
    else if (scoreDate.plusDays(1).isBefore(examDate)) { Some(scoreDate.plusDays(1)) }
    else { None }
  }

}
