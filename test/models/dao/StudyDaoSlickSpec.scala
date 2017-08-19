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

import libs.{ AppFactory, TestingDbQueries }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }

class StudyDaoSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter with AppFactory {

  val teacherId = 99998
  val studentId = 99999

  val studyDao: StudyDao = fakeApplication().injector.instanceOf[StudyDaoSlick]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]

  before {
    //insert data
    database.insertStudyContent(teacherId, studentId)
  }

  after {
    //clear data
    database.clearStudyContent(teacherId, studentId)
  }

  describe("getContentAssignedStatusByUserId") {
    it("should return all content items with status for both enabled and disabled content") {
      studyDao.getContentAssignedStatusByUserId(studentId) flatMap {
        r =>
          {
            r should have size 2
            r.find(e => e.id.contains(99998)).get.enabled should be(true)
            r.find(e => e.id.contains(99999)).get.enabled should be(false)
          }
      }
    }

    it("should return no content for an unknown student id") {
      studyDao.getContentAssignedStatusByUserId(102345) flatMap {
        r => r should have size 0
      }
    }

    it("should return no content for a negative student id") {
      studyDao.getContentAssignedStatusByUserId(-1) flatMap {
        r => r should have size 0
      }
    }
  }

  describe("disableContentAssigned") {
    it("should write an entry to disable the assigned content for a user") {
      studyDao.disableContentAssigned(99998, 99998) flatMap {
        r => r should be(1)
      }
    }

    it("should return 0 if such an item already exists") {
      studyDao.disableContentAssigned(99999, 99999) flatMap {
        r => r should be(0)
      }
    }
  }

  describe("enableContentItem") {

    it("should remove an entry to enable the content item for a user") {
      for {
        initial <- database.getDisabled(99999, 99999)
        enabled <- studyDao.enableContentAssigned(99999, 99999)
        post <- database.getDisabled(99999, 99999)
        assert = {
          initial.isDefined should be(true)
          post.isDefined should be(false)
        }
      } yield assert
    }

    it("should return 0 if no such item exists") {
      for {
        checkExists <- database.getDisabled(99998, 99998)
        enabled <- studyDao.enableContentAssigned(99998, 99998)
        verify <- database.getDisabled(99998, 99998)
        assert = {
          checkExists.isDefined should be(false)
          enabled should be(0)
          verify.isDefined should be(false)
        }
      } yield assert
    }

    it("should return 1 if such an item exists") {
      for {
        checkExists <- database.getDisabled(99999, 99999)
        enabled <- studyDao.enableContentAssigned(99999, 99999)
        post <- database.getDisabled(99999, 99999)
        assert = {
          checkExists.isDefined should be(true)
          enabled should be(1)
          post.isDefined should be(false)
        }
      } yield assert
    }

  }

}
