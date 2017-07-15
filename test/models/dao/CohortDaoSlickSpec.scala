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

package models.dao

import libraries.{CohortTestData, UserProfileTestData}
import libs.AppFactory
import models.dto.CohortDto
import org.scalatest.{AsyncFunSpec, BeforeAndAfter, Matchers}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class CohortDaoSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with MockitoSugar with AppFactory {

  val cohortDao: CohortDaoSlick = fakeApplication().injector.instanceOf[CohortDaoSlick]
  val testData = new CohortTestData()


  describe("UserDaoSlick") {

    //find(cohortId: Int): Future[Option[CohortDto]]
    it("should correctly find an existing cohort by id") {
      val returnedCohort = cohortDao.save(testData.cohortNoId1)
      for {
        retCohort <- returnedCohort
        foundCohort <- cohortDao.find(retCohort.get.id.get)
        _ <- cohortDao.delete(retCohort.get.id.get)
        result = retCohort.get.id should be(foundCohort.get.id)
      } yield result
    }


    //findByOwner(ownerId: Int): Future[List[CohortDto]]
    it("should correctly find an existing user by id") {
      testData.cohortsNoIds.foreach(c => cohortDao.save(c))

      cohortDao.findByOwner(testData.ownerId).flatMap {
        result: Seq[CohortDto] => {
          cohortDao.deleteByOwner(testData.ownerId)
          result.size should equal(testData.cohortsNoIds.size)
          result.head.ownerId should equal (testData.ownerId)
        }
      }

    }

    //save(cohort: CohortDto): Future[Option[CohortDto]]
    //this has been tested with "should correctly find an existing cohort by id"


    // update(cohort: CohortDto): Future[Option[User]]
    it("should correctly update an existing cohort") {
      //insert cohort
      val returnedCohort = cohortDao.save(testData.cohortNoId1)

      returnedCohort.flatMap {
        cohort =>
          for {
            changedCohort <- cohortDao.update(cohort.get.copy(name = "Fun"))
            check <- changedCohort.get.name should equal ("Fun")
          } yield check
      }



      //check cohort match

      //remove cohort
    }

    /*

        /**
          * Delete a cohort
          * @param cohortId the cohort id to delete
          * @return a future number of affected rows
          */
        def delete(cohortId: Int): Future[Int]
        it("should correctly find an existing user by id") {

          }
           */


  }

}
