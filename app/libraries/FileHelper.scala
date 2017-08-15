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
import java.nio.file.{ Path, Paths }

import models.dto.ContentItemDto
import play.api.libs.Files

object FileHelper {

  def deleteItemImagesIfExist(item: ContentItemDto, userId: Int): Unit = {
    val directory: File = new File(getItemsDirectoryPath(userId, item.packageId).toString)
    if (directory.exists) {
      val files = directory.listFiles((_, name) => name.startsWith(s"${item.id.get}."))
      files.foreach(f => f.delete)
    }
  }

  def saveImage(tempFile: Files.TemporaryFile, fileName: String, item: ContentItemDto, userId: Int): Path = {
    val filename = getFilename(fileName, item.id.get)
    val filePath = getItemPath(userId, item.packageId, filename)
    ensurePathExists(filePath.getParent)
    tempFile.moveTo(filePath, replace = true)
  }

  def getItemPath(userId: Int, packageId: Int, filename: String): Path = {
    Paths.get(s"media/$userId/content/$packageId/items/$filename")
  }

  def getItemsDirectoryPath(userId: Int, packageId: Int): Path = {
    Paths.get(s"media/$userId/content/$packageId/items")
  }

  def getPackageDirectoryPath(userId: Int, packageId: Int): Path = {
    Paths.get(s"media/$userId/content/$packageId")
  }

  def pathToUrl(filePath: Path): String = {
    val split = filePath.toString.replace('\\', '/').split('/')
    split.mkString("/")
  }

  private def getFilename(fileName: String, itemId: Int): String = {
    val suffix = fileName.split('.').reverse.head
    if (suffix.isEmpty) itemId.toString else s"${itemId.toString}.$suffix"
  }

  private def ensurePathExists(path: Path) = {
    java.nio.file.Files.createDirectories(path)
  }

  def deletePackageFolderIfExist(packageId: Int, userId: Int): Unit = {
    val directory: File = new File(getPackageDirectoryPath(userId, packageId).toString)
    if (directory.exists) {
      deleteFolder(directory)
    }
  }

  def deleteFolder(directory: File): Unit = {
    if (directory.isDirectory) {
      val files = directory.listFiles
      files.foreach(file => {
        if (file.isDirectory) deleteFolder(file)
        else file.delete
      })
    }
    directory.delete
  }
}
