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

import models.User

import scala.concurrent.duration._
import models.dto.ContentFolderDto
import org.scalatest.{AsyncFunSpec, BeforeAndAfterAll, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import libs.DatabaseSupport

import scala.concurrent.{Await, Future}

class ContentFolderDaoSlickSpec extends AsyncFunSpec with Matchers
  with MockitoSugar with DatabaseSupport {

  var folderDao: ContentFolderDao = app.injector
    .instanceOf[ContentFolderDaoSlick]
  var userDao: UserDao = app.injector
    .instanceOf[UserDaoSlick]

  var owner: User = new User(None, "Test", "User", "fake@faked.com")

  var folderNoId1: ContentFolderDto = _
  var folderNoId2: ContentFolderDto = _
  var folderNoId3: ContentFolderDto = _
  var folderNoId4: ContentFolderDto = _
  var folderNoId5: ContentFolderDto = _
  var foldersNoIds: List[ContentFolderDto] = Nil

  override def beforeAll() {
    super.beforeAll()
    //create user
    val futureUser = userDao.save(owner)
    owner = Await.result(futureUser, 10 seconds).get
    folderNoId1 = new ContentFolderDto(None, None, owner.id.get, "folderNoId1")
    folderNoId2 = new ContentFolderDto(None, None, owner.id.get, "folderNoId2")
    folderNoId3 = new ContentFolderDto(None, None, owner.id.get, "folderNoId3")
    folderNoId4 = new ContentFolderDto(None, None, owner.id.get, "folderNoId4")
    folderNoId5 = new ContentFolderDto(None, None, owner.id.get, "folderNoId5")
    foldersNoIds = List(folderNoId1, folderNoId2, folderNoId3, folderNoId4, folderNoId5)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    userDao.delete(owner.id.get)
  }

  describe("ContentFolderDaoSlick") {

    it("should correctly find an existing folder by id") {
      for {
        retFolder <- folderDao.save(folderNoId1)
        foundFolder <- folderDao.find(retFolder.get.id.get)
        _ <- folderDao.delete(foundFolder.get.id.get)
        result <- retFolder.get.id should be(foundFolder.get.id)
      } yield result
    }

    it("should correctly not find a folder by non existent id") {
      for {
        foundCohort <- folderDao.find(999999999)
        result <- foundCohort should be(None)
      } yield result
    }

    it("should correctly find an existing folder by ownerid") {
      var xs: List[Int] = Nil
      foldersNoIds.foreach(c => {
        val id = folderDao.save(c)
        xs = Await.result(id, 2 seconds).get.id.get :: xs
      })

      val result = for {
        result <- folderDao.findByOwner(owner.id.get)
        test <- result.size should be(5)
      } yield test

      xs.foreach(id => folderDao.delete(id))
      result
    }

    // update
    it("should correctly update an existing folder") {
      //insert cohort
      val returnedFolder = folderDao.save(folderNoId2)
      val newName = "Sombrero"

      for {
        source <- returnedFolder
        _ <- folderDao.update(source.get.copy(name = newName))
        retrieved <- folderDao.find(source.get.id.get)
        result <- retrieved.get.name should be(newName)
      } yield result
    }
  }

}
