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

trait RepromptCalculator {

  /**
    * Calculate and add a reprompt date to the ScoreDto based on the score data
    * and the date of the upcoming examination
    * @param score a ScoreDto
    * @return the ScoreDto with the embedded date for next content exposure
    */
  def addRepromptDate(score: ScoreDto, examinationDate: LocalDate): ScoreDto

}
