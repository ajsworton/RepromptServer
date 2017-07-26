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

import libs.AppFactory
import models.User

import scala.concurrent.duration._
import models.dto.{ContentFolderDto, ContentPackageDto}
import org.scalatest.{AsyncFunSpec, BeforeAndAfterAll, Matchers}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Await

class ContentPackageDaoSlickSpec extends AsyncFunSpec with Matchers
  with MockitoSugar with AppFactory with BeforeAndAfterAll {

  var folderDao: ContentFolderDao = fakeApplication().injector
    .instanceOf[ContentFolderDaoSlick]

  var packageDao: ContentPackageDao = fakeApplication().injector
    .instanceOf[ContentPackageDaoSlick]

  var userDao: UserDao = fakeApplication().injector
    .instanceOf[UserDaoSlick]

  var owner: User = new User(None, "Test", "User", "fake@faked.com")

  var folder: ContentFolderDto = _

  var package1: ContentPackageDto = _
  var package2: ContentPackageDto = _
  var package3: ContentPackageDto = _
  var package4: ContentPackageDto = _
  var package5: ContentPackageDto = _
  var packages: List[ContentPackageDto] = Nil

  override def beforeAll {
    //create user
    val futureUser = userDao.save(owner)
    owner = Await.result(futureUser, 10 seconds).get

    //create folder
    val futureFolder = folderDao.save(new ContentFolderDto(None, None, owner.id.get, "folder"))
    folder = Await.result(futureFolder, 10 seconds).get

    package1 = new ContentPackageDto(None, folder.id.get, owner.id.get, "packageNoId1")
    package2 = new ContentPackageDto(None, folder.id.get, owner.id.get, "packageNoId2")
    package3 = new ContentPackageDto(None, folder.id.get, owner.id.get, "packageNoId3")
    package4 = new ContentPackageDto(None, folder.id.get, owner.id.get, "packageNoId4")
    package5 = new ContentPackageDto(None, folder.id.get, owner.id.get, "packageNoId5")
    packages = List(package1, package2, package3, package4, package5)
  }

  override def afterAll(): Unit = {
    userDao.delete(owner.id.get)
  }

  describe("ContentpackageDaoSlick") {


    it("should correctly find an existing package by id") {
      for {
        retpackage <- packageDao.save(package1)
        foundpackage <- packageDao.find(retpackage.get.id.get)
        _ <- packageDao.delete(foundpackage.get.id.get)
        result <- retpackage.get.id should be(foundpackage.get.id)
      } yield result
    }

    it("should correctly not find a package by non existent id") {
      for {
        foundCohort <- packageDao.find(999999999)
        result <- foundCohort should be(None)
      } yield result
    }

    it("should correctly find an existing package by ownerid") {
      var xs: List[Int] = Nil
      packages.foreach(c => {
        val id = packageDao.save(c)
        xs = Await.result(id, 2 seconds).get.id.get :: xs
      })

      val result = for {
        result <- packageDao.findByOwner(owner.id.get)
        test <- result.size should be(packages.size)
      } yield test

      xs.foreach(id => packageDao.delete(id))
      result
    }

    // update
    it("should correctly update an existing package") {
      //insert cohort
      val returnedpackage = packageDao.save(package2)
      val newName = "Salutations"

      for {
        source <- returnedpackage
        _ <- packageDao.update(source.get.copy(name = newName))
        retrieved <- packageDao.find(source.get.id.get)
        result <- retrieved.get.name should be(newName)
      } yield result
    }
  }

}
