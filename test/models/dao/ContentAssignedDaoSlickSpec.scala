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

import java.time.LocalDate
import java.util.TimeZone

import libs.{ AppFactory, TestingDbQueries }
import models.dto.ContentAssignedDto
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }

class ContentAssignedDaoSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter with AppFactory {

  val service: ContentAssignedDao = fakeApplication().injector.instanceOf[ContentAssignedDaoSlick]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]
  val examDate: LocalDate = LocalDate.now().plusMonths(2)
  val name = "ContentName"

  val teacherId, cohortId, packageId = 7071
  val studentId, assignedId = 7072
  val otherStudentId = 7073

  val mockContent: ContentAssignedDto = new ContentAssignedDto(id = None, name = name,
    examDate = examDate, active = true, ownerId = Some(1), enabled = true, Some(Nil), Some(Nil))

  before {
    database.insertStudyContent(teacherId, studentId, otherStudentId)
  }

  after {
    database.clearStudyContent(teacherId, studentId, otherStudentId)
  }

  describe("find(assignedContentId: Int)") {
    it("should locate assigned content by Id") {
      for {
        saved <- service.save(mockContent)
        retrieved <- service.find(saved.get.id.get)
        _ <- service.delete(saved.get.id.get)
        assertion = {
          retrieved.isDefined should be(true)
          retrieved.get.examDate should be(examDate)
        }
      } yield assertion
    }
  }

  describe("findByOwner(ownerId: Int)") {
    it("should locate assigned content by ownerId") {
      for {
        saved <- service.save(mockContent)
        retrieved <- service.findByOwner(saved.get.ownerId.get)
        _ <- service.delete(saved.get.id.get)
        assertion = {
          retrieved.size should be > 0
          retrieved.contains(saved.get) should be(true)
        }
      } yield assertion
    }
  }

  describe("update(assigned: ContentAssignedDto)") {
    it("should update assigned content found by ContentAssignedDto") {
      for {
        saved <- service.save(mockContent)
        retrieved <- service.find(saved.get.id.get)
        updated <- service.update(retrieved.get.copy(enabled = !retrieved.get.enabled))
        _ <- service.delete(saved.get.id.get)
        assertion = {
          updated.isDefined should be(true)

        }
      } yield assertion
    }
  }

  describe("deleteByOwner(ownerId: Int)") {
    it("should delete Assigned content by owner id") {
      for {
        saved <- service.save(mockContent)
        before <- service.findByOwner(saved.get.ownerId.get)
        _ <- service.deleteByOwner(saved.get.ownerId.get)
        after <- service.findByOwner(saved.get.id.get)
        assertion = {
          before.size should be > 0
          before.contains(saved.get) should be(true)
          after.size should be(0)
        }
      } yield assertion
    }
  }

  describe("attachCohort(assignedId: Int, cohortId: Int)") {
    it("should attach a cohort that was not previously attached, returning Right(1)") {
      for {
        _ <- service.detachCohort(assignedId, cohortId)
        before <- database.getAssignedCohort(assignedId, cohortId)
        saved <- service.attachCohort(assignedId, cohortId)
        after <- database.getAssignedCohort(assignedId, cohortId)
        assertion = {
          before should be(None)
          saved should be(Right(1))
          after should be(Some(assignedId, cohortId))
        }
      } yield assertion
    }

    it("should have no effect on an already existing cohort, returning Left") {
      for {
        _ <- service.detachCohort(assignedId, cohortId)
        _ <- service.attachCohort(assignedId, cohortId)
        before <- database.getAssignedCohort(assignedId, cohortId)
        saved <- service.attachCohort(assignedId, cohortId)
        after <- database.getAssignedCohort(assignedId, cohortId)
        assertion = {
          before should be(Some(assignedId, cohortId))
          saved should be(Left("Duplicate entry '7072-7071' for key 'PRIMARY'"))
          after should be(Some(assignedId, cohortId))
        }
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with a cohortId less than 1") {
      for {
        detached <- service.attachCohort(assignedId, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with an assignedId less than 1") {
      for {
        detached <- service.attachCohort(0, cohortId)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with assigned and cohortId less than 1") {
      for {
        detached <- service.attachCohort(0, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }
  }

  describe("detachCohort(assignedId: Int, cohortId: Int)") {
    it("should detach a cohort and return a Right(1) with valid data") {
      for {
        _ <- service.detachCohort(assignedId, cohortId)
        _ <- service.attachCohort(assignedId, cohortId)
        before <- database.getAssignedCohort(assignedId, cohortId)
        detached <- service.detachCohort(assignedId, cohortId)
        after <- database.getAssignedCohort(assignedId, cohortId)
        assertion = {
          before should be(Some(assignedId, cohortId))
          detached should be(Right(1))
          after should be(None)
        }
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with a cohortId less than 1") {
      for {
        detached <- service.detachCohort(assignedId, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with an assignedId less than 1") {
      for {
        detached <- service.detachCohort(0, cohortId)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with assigned and cohortId less than 1") {
      for {
        detached <- service.detachCohort(0, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }
  }

  describe("attachPackage(assignedId: Int, packageId: Int)") {
    it("should attach a package that was not previously attached, returning Right(1)") {
      for {
        _ <- service.detachPackage(assignedId, packageId)
        before <- database.getAssignedPackage(assignedId, packageId)
        saved <- service.attachPackage(assignedId, packageId)
        after <- database.getAssignedPackage(assignedId, packageId)
        assertion = {
          before should be(None)
          saved should be(Right(1))
          after should be(Some(assignedId, packageId))
        }
      } yield assertion
    }

    it("should have no effect on an already existing package, returning Left") {
      for {
        _ <- service.detachPackage(assignedId, packageId)
        _ <- service.attachPackage(assignedId, packageId)
        before <- database.getAssignedPackage(assignedId, packageId)
        saved <- service.attachPackage(assignedId, packageId)
        after <- database.getAssignedPackage(assignedId, packageId)
        assertion = {
          before should be(Some(assignedId, packageId))
          saved should be(Left("Duplicate entry '7072-7071' for key 'PRIMARY'"))
          after should be(Some(assignedId, packageId))
        }
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with a packageId less than 1") {
      for {
        detached <- service.attachPackage(assignedId, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with an assignedId less than 1") {
      for {
        detached <- service.attachPackage(0, packageId)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with assigned and packageId less than 1") {
      for {
        detached <- service.attachPackage(0, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }
  }

  describe("detachPackage(assignedId: Int, packageId: Int)") {
    it("should detach a package and return a Right(1) with valid data") {
      for {
        _ <- service.detachPackage(assignedId, packageId)
        _ <- service.attachPackage(assignedId, packageId)
        before <- database.getAssignedPackage(assignedId, packageId)
        detached <- service.detachPackage(assignedId, packageId)
        after <- database.getAssignedPackage(assignedId, packageId)
        assertion = {
          before should be(Some(assignedId, packageId))
          detached should be(Right(1))
          after should be(None)
        }
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with a packageId less than 1") {
      for {
        detached <- service.detachPackage(assignedId, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with an assignedId less than 1") {
      for {
        detached <- service.detachPackage(0, packageId)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }

    it("should should return a Left(Invalid id) if supplied with assigned and packageId less than 1") {
      for {
        detached <- service.detachPackage(0, 0)
        assertion = detached should be(Left("Invalid id"))
      } yield assertion
    }
  }
}
