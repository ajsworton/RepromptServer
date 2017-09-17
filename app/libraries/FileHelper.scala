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
import org.apache.commons.io.FileUtils
import play.api.libs.Files

/**
 * Helper class for File actions
 */
class FileHelper {

  /**
   * Helper to delete images after checking for existence.
   * @param item
   * @param userId
   */
  def deleteItemImagesIfExist(item: ContentItemDto, userId: Int): Unit = {
    val directory: File = new File(getItemsDirectoryPath(userId, item.packageId).toString)
    if (directory.exists) {
      val files = directory.listFiles((_, name) => name.startsWith(s"${item.id.get}."))
      files.foreach(f => f.delete)
    }
  }

  /**
   * Helper to save an image.
   * @param tempFile the supplied temporary file
   * @param fileName the supplied name to save the file as
   * @param item the supplied item
   * @param userId the id of the assocated user
   * @return a path for the file
   */
  def saveImage(tempFile: Files.TemporaryFile, fileName: String, item: ContentItemDto, userId: Int): Path = {
    val filename = getFilename(fileName, item.id.get)
    val filePath = getItemPath(userId, item.packageId, filename)
    ensurePathExists(filePath.getParent)
    tempFile.moveTo(filePath, replace = true)
  }

  /**
   * Helper to obtain the path for an item.
   * @param userId the supplied user id
   * @param packageId the supplied package id
   * @param filename the supplied filename
   * @return the path
   */
  private def getItemPath(userId: Int, packageId: Int, filename: String): Path = {
    Paths.get(s"media/$userId/content/$packageId/items/$filename")
  }

  /**
   * Helper to get the path to an item directory.
   * @param userId the supplied user id
   * @param packageId the supplied package id
   * @return the path
   */
  private def getItemsDirectoryPath(userId: Int, packageId: Int): Path = {
    Paths.get(s"media/$userId/content/$packageId/items")
  }

  /**
   * Helper to get the path to a package directory.
   * @param userId the supplied user id
   * @param packageId the supplied package id
   * @return the path
   */
  private def getPackageDirectoryPath(userId: Int, packageId: Int): Path = {
    Paths.get(s"media/$userId/content/$packageId")
  }

  /**
   * Helper to convert a path to a url.
   * @param filePath the supplied path
   * @return the url
   */
  def pathToUrl(filePath: Path): String = {
    val split = filePath.toString.replace('\\', '/').split('/')
    split.mkString("/")
  }

  /**
   * Helper to get a filename from a filename and itemId.
   * @param fileName the supplied filename
   * @param itemId the supplied itemId
   * @return the file name
   */
  private def getFilename(fileName: String, itemId: Int): String = {
    val suffix = fileName.split('.').reverse.head
    if (suffix.isEmpty) itemId.toString else s"${itemId.toString}.$suffix"
  }

  /**
   * Helper to create directories along a path if they don't exist.
   * @param path
   * @return
   */
  private def ensurePathExists(path: Path) = {
    java.nio.file.Files.createDirectories(path)
  }

  /**
   * Helper to remove a directory if it exists.
   * @param packageId the supplied package id
   * @param userId the supplied user id
   */
  def deletePackageFolderIfExist(packageId: Int, userId: Int): Unit = {
    val directory: File = new File(getPackageDirectoryPath(userId, packageId).toString)
    if (directory.exists) {
      deleteFolder(directory)
    }
  }

  /**
   * Helper to delete a directory
   * @param directory the supplied directory
   */
  private def deleteFolder(directory: File): Unit = {
    FileUtils.forceDelete(directory)
  }
}
