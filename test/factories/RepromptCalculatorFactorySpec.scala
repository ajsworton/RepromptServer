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

package factories

import java.time.LocalDate

import libraries.{ RepromptCalculatorExamMode, RepromptCalculatorStudyMode }
import models.dto.ScoreDto
import org.scalatest.{ FunSpec, Matchers }

class RepromptCalculatorFactorySpec extends FunSpec with Matchers {

  val scoreDto = new ScoreDto(None, 1, 49, None, 0)

  describe("getCalculator") {
    it("should return study mode if provided with a streak of 0") {
      RepromptCalculatorFactory.getCalculator(scoreDto).getClass should be
      (new RepromptCalculatorStudyMode).getClass
    }

    it("should return study mode if provided with a streak of 3") {
      RepromptCalculatorFactory.getCalculator(scoreDto).getClass should be
      (new RepromptCalculatorStudyMode).getClass
    }

    it("should return study mode if provided with a streak of 4") {
      RepromptCalculatorFactory.getCalculator(scoreDto).getClass should be
      (new RepromptCalculatorExamMode).getClass
    }
  }
}
