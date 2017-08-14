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
import org.scalatest.{ BeforeAndAfter, FunSpec, Matchers }

class RepromptCalculatorStudyModeSpec extends FunSpec with Matchers with BeforeAndAfter {

  var score: ScoreDto = new ScoreDto(Some(1), 1, 66, Some(LocalDate.of(2017, 6, 1)), 5, None)
  val calc: RepromptCalculator = new RepromptCalculatorStudyMode

  describe("RepromptCalculatorLearningMode") {

    it("should return none if the exam date is in the past") {
      val response = calc.addRepromptDate(score, LocalDate.of(2016, 6, 1))
      response.repromptDate should be(None)
    }

    it("should return none if the exam date matches the score date") {
      val response = calc.addRepromptDate(score, LocalDate.of(2017, 6, 1))
      response.repromptDate should be(None)
    }

    it("should return a date a day later for a streak of 1") {
      score = new ScoreDto(Some(1), 1, 66, Some(LocalDate.of(2017, 6, 1)), 1, None)
      val response = calc.addRepromptDate(score, LocalDate.of(2018, 6, 2))
      response.repromptDate should be(Some(LocalDate.of(2017, 6, 2)))
    }

    it("should return a date two days later for a streak of 2") {
      score = new ScoreDto(Some(1), 1, 66, Some(LocalDate.of(2017, 6, 1)), 2, None)
      val response = calc.addRepromptDate(score, LocalDate.of(2018, 6, 3))
      response.repromptDate should be(Some(LocalDate.of(2017, 6, 3)))
    }

    it("should return a date three days later for a streak of 3") {
      score = new ScoreDto(Some(1), 1, 66, Some(LocalDate.of(2017, 6, 1)), 3, None)
      val response = calc.addRepromptDate(score, LocalDate.of(2018, 6, 4))
      response.repromptDate should be(Some(LocalDate.of(2017, 6, 4)))
    }

    it("should return a date four days later for a streak of 4") {
      score = new ScoreDto(Some(1), 1, 66, Some(LocalDate.of(2017, 6, 1)), 4, None)
      val response = calc.addRepromptDate(score, LocalDate.of(2018, 6, 5))
      response.repromptDate should be(Some(LocalDate.of(2017, 6, 5)))
    }

    it("should return a date one day later for a streak of 4 with a close exam date") {
      score = new ScoreDto(Some(1), 1, 66, Some(LocalDate.of(2017, 6, 1)), 4, None)
      val response = calc.addRepromptDate(score, LocalDate.of(2017, 6, 4))
      response.repromptDate should be(Some(LocalDate.of(2017, 6, 2)))
    }

  }
}
