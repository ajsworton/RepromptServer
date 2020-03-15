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

import com.mohiva.play.silhouette.api.LoginInfo
import libs.{DatabaseSupport, TestingDbQueries}
import models.Profile
import org.scalatest.{AsyncFunSpec, BeforeAndAfterAll, Matchers}

class UserServiceImplSpec extends AsyncFunSpec with Matchers with BeforeAndAfterAll with DatabaseSupport {

  val service: UserServiceImpl   = app.injector.instanceOf[UserServiceImpl]
  val database: TestingDbQueries = app.injector.instanceOf[TestingDbQueries]

  val educatorId     = 999997
  val studentId      = 999998
  var otherStudentId = 999999

  override def beforeAll() =
    database.insertStudyContent(educatorId, studentId, otherStudentId)

  override def afterAll() =
    database.clearStudyContent(educatorId, studentId, otherStudentId)

  describe("retrieve(id: Int)") {
    it("should retrieve a user that matches a specified id") {
      service.retrieve(studentId) flatMap { r =>
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
        inserted  <- database.getUser(studentId)
        retrieved <- service.retrieve(inserted.get.profiles.head.loginInfo)
        assertion = {
          inserted.isDefined should be(true)
          retrieved.get.email should be(inserted.get.email)
          retrieved.get.firstName should be(inserted.get.firstName)
          retrieved.get.surname should be(inserted.get.surname)
        }
      } yield assertion
    }
  }

  describe("save(user: User)") {
    it("should save a user if none exists") {
      for {
        user  <- database.getUser(otherStudentId)
        _     <- database.deleteUser(otherStudentId)
        saved <- service.save(user.get)
        assertion = {
          if (saved.isDefined) {
            otherStudentId = saved.get.id.get
          }
          saved should be(Some(user.get.copy(id = saved.get.id, profiles = saved.get.profiles.map(p => p.copy(userId = saved.get.id)))))
        }
      } yield assertion
    }

    it("should update a user if one exists") {
      for {
        user    <- database.getUser(otherStudentId)
        _       <- service.save(user.get.copy(firstName = "Badger"))
        altered <- database.getUser(otherStudentId)
        assertion = {
          altered should be(Some(user.get.copy(firstName = "Badger")))
        }
      } yield assertion
    }

  }

  describe("save(profile: Profile)") {
    it("should save a user if none exists") {
      for {
        user  <- database.getUser(otherStudentId)
        _     <- database.deleteUser(otherStudentId)
        saved <- service.save(user.get.profiles.head)
        _     <- database.deleteUser(saved.get.id.get)
        assertion = {
          if (saved.isDefined) {
            otherStudentId = saved.get.id.get
          }
          saved.isDefined should be(true)
          user.get.profiles.head.firstName should be(user.get.profiles.head.firstName)
        }
      } yield assertion
    }

    it("should update a profile if one exists") {
      for {
        user    <- database.getUser(studentId)
        _       <- service.save(user.get.profiles.head.copy(firstName = Some("Bloom")))
        altered <- database.getUser(studentId)
        assertion = {
          altered.isDefined should be(true)
          altered.get.firstName should be("Bloom")
          altered.get.profiles should have size 1
          altered.get.profiles.head.firstName should be(Some("Bloom"))
        }
      } yield assertion
    }
  }

  describe("createUserAndProfile(profile: Profile)") {
    it("should create a user from the supplied profile") {
      val profile = Profile(userId = None,
                            firstName = Some("Samwise"),
                            lastName = Some("Gamgee"),
                            loginInfo = LoginInfo("credentials", "me@newfake1112"),
                            email = Some("me@newfake1112"))

      for {
        created <- service.createUserAndProfile(profile)
        _       <- database.deleteUser(created.get.id.get)
        assertion = {
          created.isDefined should be(true)
          created.get.firstName should be("Samwise")
          created.get.surname should be("Gamgee")
          created.get.email should be("me@newfake1112")
          created.get.profiles.head.loginInfo should be(LoginInfo("credentials", "me@newfake1112"))
        }
      } yield assertion
    }
  }

  describe("getFirstName(firstName: Option[String], fullName: Option[String])") {
    it("should obtain the firstname from the fullname if no firstName is defined") {
      val firstname = None
      val fullname  = Some("Great Name")
      service.getFirstName(firstname, fullname) should be("Great")
    }

    it("should obtain the firstname from the firstname if defined") {
      val firstname = Some("Wall-e")
      val fullname  = Some("Great Name")
      service.getFirstName(firstname, fullname) should be("Wall-e")
    }

    it("should return empty string if defined neither input defined") {
      val firstname = None
      val fullname  = None
      service.getFirstName(firstname, fullname) should be("")
    }
  }

  describe("getsurname(surname: Option[String], fullName: Option[String])") {
    it("should obtain the surname from the fullname if surname undefined") {
      val surname  = None
      val fullname = Some("Great Name")
      service.getsurname(surname, fullname) should be("Name")
    }

    it("should obtain the surname from the surname if defined") {
      val surname  = Some("Naples")
      val fullname = Some("Great Name")
      service.getsurname(surname, fullname) should be("Naples")
    }

    it("should return empty string if defined neither input defined") {
      val surname  = None
      val fullname = None
      service.getsurname(surname, fullname) should be("")
    }
  }

}
