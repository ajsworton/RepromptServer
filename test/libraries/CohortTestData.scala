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

import models.dto.CohortDto

class CohortTestData {

  val ownerId = 1

  val cohortNoId1 = new CohortDto(None, ownerId, "cohortNoId1")
  val cohortNoId2 = new CohortDto(None, ownerId, "cohortNoId2")
  val cohortNoId3 = new CohortDto(None, ownerId, "cohortNoId3")
  val cohortNoId4 = new CohortDto(None, ownerId, "cohortNoId4")
  val cohortNoId5 = new CohortDto(None, ownerId, "cohortNoId5")

  val cohortsNoIds = List(cohortNoId1, cohortNoId2, cohortNoId3, cohortNoId4, cohortNoId5)

//  val cohortWithId1 = new CohortDto(Some(1), 1, "cohortNoId1")
//  val cohortWithId2 = new CohortDto(Some(1), 1, "cohortNoId2")
//  val cohortWithId3 = new CohortDto(Some(1), 1, "cohortNoId3")
//  val cohortWithId4 = new CohortDto(Some(1), 1, "cohortNoId4")
//  val cohortWithId5 = new CohortDto(Some(1), 1, "cohortNoId5")
//
//  val cohortsWithIds = List(cohortWithId1, cohortWithId2, cohortWithId3, cohortWithId4,
//    cohortWithId5)
}
