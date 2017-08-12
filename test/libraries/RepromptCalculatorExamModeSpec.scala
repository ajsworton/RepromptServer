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
import org.scalatest.{FunSpec, BeforeAndAfter, Matchers}

class RepromptCalculatorExamModeSpec extends FunSpec with Matchers with BeforeAndAfter{

  val score: ScoreDto = new ScoreDto(1, 1, 66, LocalDate.of(2017,6,1), 5, None)
  val calc: RepromptCalculator = new RepromptCalculatorExamMode

  describe("RepromptCalculatorStudyMode") {

    it("should return none if the exam date is in the past") {
      val response = calc.addRepromptDate(score, LocalDate.of(2016,6,1))
      response.repromptDate should be (None)
    }

    it("should return none if the exam date is the same as the score date") {
      val response = calc.addRepromptDate(score, LocalDate.of(2017,6,1))
      response.repromptDate should be (None)
    }

    it("should return a date that matches the percentage between the dates if valid") {
      val response = calc.addRepromptDate(score, LocalDate.of(2017,6,11))
      response.repromptDate should be (Some(LocalDate.of(2017,6,3)))
    }

    it("should return a date matching the percentage between the dates if valid across months") {
      val response = calc.addRepromptDate(score, LocalDate.of(2017,7,31))
      response.repromptDate should be (Some(LocalDate.of(2017,6,13)))
    }

    it("should return a date matching the percentage between the dates if valid across years") {
      val response = calc.addRepromptDate(score, LocalDate.of(2018,6,1))
      response.repromptDate should be (Some(LocalDate.of(2017,8,13)))
    }

    it("should return a date matching the percentage between the dates if % distance below one") {
      val response = calc.addRepromptDate(score, LocalDate.of(2017,6,5))
      response.repromptDate should be (Some(LocalDate.of(2017,6,2)))
    }

    it("should return the next day if % distance below one") {
      val response = calc.addRepromptDate(score, LocalDate.of(2017,6,3))
      response.repromptDate should be (Some(LocalDate.of(2017,6,2)))
    }

    it("should return none if the % distance is below one and the next day is the exam") {
      val response = calc.addRepromptDate(score, LocalDate.of(2017,6,2))
      response.repromptDate should be (None)
    }

  }

}
