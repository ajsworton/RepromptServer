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

import java.io.File
import java.nio.file.Paths

import models.dto.ContentItemDto
import org.apache.commons.io.FileUtils
import org.scalatest.{ BeforeAndAfter, FunSpec, Matchers }
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile

class FileHelperSpec extends FunSpec with Matchers with BeforeAndAfter {

  val fileHelper = new FileHelper

  val userId = 989898
  val id: Option[Int] = Some(999999)
  val packageId: Int = 999998
  val imageUrl: Option[String] = None
  val name: String = "Content Item Dto"
  val content: String = "Content here."
  val enabled: Boolean = true

  val directory = s"media/$userId/content/$packageId/items/"
  val directoryPath = Paths.get(directory)
  val directoryBase = new File(s"media/$userId")

  val contentItemDto = ContentItemDto(id, packageId, imageUrl, name, content, enabled)
  val fakeImageFile1 = new File(s"$directoryPath/${id.get}.gif")
  val fakeImageFile2 = new File(s"$directoryPath/${id.get}.png")

  val fakeTempFile: TemporaryFile = Files.SingletonTemporaryFileCreator.create()

  before {
    java.nio.file.Files.createDirectories(directoryPath)
    fakeImageFile1.createNewFile()
    fakeImageFile2.createNewFile()
  }

  after {
    FileUtils.forceDelete(directoryBase)
  }

  describe("deleteItemImagesIfExist") {
    it("should remove all image files if they exist in a supplied directory path") {
      //verify exists
      val dir = new java.io.File(directory)
      dir.list should have length 2
      //execute method
      fileHelper.deleteItemImagesIfExist(contentItemDto, userId)
      //verify file deleted
      dir.list should have length 0
    }
  }

  describe("saveImage") {
    it("should persist a provided temporary file to disk") {
      FileUtils.forceDelete(directoryBase)
      java.nio.file.Files.exists(directoryPath) should be(false)
      fileHelper.saveImage(fakeTempFile, "fileName", contentItemDto, userId)
      java.nio.file.Files.exists(directoryPath) should be(true)
    }
  }

  describe("pathToUrl") {
    it("should return the correct url for a supplied path") {
      val url = fileHelper.pathToUrl(Paths.get(fakeImageFile1.toString))
      val expected: String = s"$directory${id.get}.gif"
      url should be(expected)
    }
  }

  describe("deletePackageFolderIfExist") {
    it("should delete the supplied folder and all descendants") {
      val dir = new java.io.File(directory)
      dir.exists should be(true)
      fileHelper.deletePackageFolderIfExist(packageId, userId)
      dir.exists should be(false)
    }
  }

}
