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

package models.services

import libs.{ AppFactory, TestingDbQueries }
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{ BeforeAndAfterAll, AsyncFunSpec, Matchers }
import org.mockito.mock

class UserServiceImplSpec extends AsyncFunSpec with Matchers with MockitoSugar with BeforeAndAfterAll
  with AppFactory {

  val service: UserServiceImpl = fakeApplication().injector.instanceOf[UserServiceImpl]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]

  val educatorId = 999997
  val studentId = 999998
  val otherStudentId = 999999

  override def beforeAll() = {
    database.insertStudyContent(educatorId, studentId, otherStudentId)
  }

  override def afterAll() = {
    database.clearStudyContent(educatorId, studentId, otherStudentId)
  }

  describe("retrieve(id: Int)") {
    it("should retrieve a user that matches a specified id") {
      service.retrieve(studentId) flatMap {
        r =>
          {
            r.isDefined should be(true)
            r.get.id.get should be(studentId)
          }
      }
    }
  }

  describe("retrieve(loginInfo: LoginInfo)") {
    it("should retrieve a user that matches a provided loginInfo") {
      for {
        inserted <- database.getUser(studentId)
        retrieved <- service.retrieve(inserted.get.profiles.head.loginInfo)
        assertion = {
          inserted.isDefined should be(true)
          retrieved should be(inserted)
        }
      } yield assertion
    }
  }

}
