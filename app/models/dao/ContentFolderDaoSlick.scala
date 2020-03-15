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

import javax.inject.Inject

import models.dto.ContentFolderDto.ContentFoldersTable
import models.dto.{ContentFolderDto, ContentPackageDto}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class ContentFolderDaoSlick @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends ContentFolderDao
    with HasDatabaseConfigProvider[JdbcProfile] {

  private val ContentFolders = TableQuery[ContentFoldersTable]

  def findContentFolderQuery(folderId: Int) =
    sql"""
          SELECT  cf.Id, cf.parent_id, cf.owner_id, cf.Name,
                  cp.Id, cp.folder_id, cp.owner_id, cp.Name

          FROM content_folders AS cf

            LEFT JOIN content_packages AS cp
            ON cf.Id = cp.folder_id

          WHERE cf.Id = $folderId
         """.as[(ContentFolderDto, Option[ContentPackageDto])]

  def findContentFolderQueryByOwner(ownerId: Int) =
    sql"""
          SELECT cf.Id, cf.parent_id, cf.owner_id, cf.Name,
                 cp.Id, cp.folder_id, cp.owner_id, cp.Name

          FROM content_folders AS cf

            LEFT JOIN content_packages AS cp
            ON cf.Id = cp.folder_id

          WHERE cf.owner_id = $ownerId
          ORDER BY cf.Name, cp.Name
         """.as[(ContentFolderDto, Option[ContentPackageDto])]

  override def find(folderId: Int): Future[Option[ContentFolderDto]] = {
    val result = findContentFolderQuery(folderId)
    val run    = db.run(result)

    run.flatMap(
      r => {
        if (r.nonEmpty) {
          val packageList = r.map(p => p._2.get).toList.filter(m => m.id.get > 0)
          Future(Some(r.head._1.copy(members = Some(packageList))))
        } else {
          Future(None)
        }
      }
    )
  }

  override def findByOwner(ownerId: Int): Future[Seq[ContentFolderDto]] = {
    val result = findContentFolderQueryByOwner(ownerId)
    val run    = db.run(result)

    run.flatMap(
      r => {
        val grouped = r.groupBy(_._1)
        val out = for {
          groupMembers <- grouped
          packages     = groupMembers._2
          packagesProc = packages.map(p => p._2.get).toList.filter(m => m.id.get > 0)
          result       = groupMembers._1.copy(members = Some(packagesProc))
        } yield result
        Future(out.toSeq)
      }
    )
  }

  override def save(folder: ContentFolderDto): Future[Option[ContentFolderDto]] =
    db.run(
      (ContentFolders returning ContentFolders.map(_.id)
        into ((folder, returnedId) => Some(folder.copy(id = returnedId)))) += folder)

  override def update(folder: ContentFolderDto): Future[Option[ContentFolderDto]] = {
    val parentId = getFolderParentId(folder)
    if (folder.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(
          ContentFolders
            .filter(_.id === folder.id)
            .map(c => (c.name, c.ownerId, c.parentId))
            .update(folder.name, folder.ownerId, parentId))
        read <- find(folder.id.get)
      } yield read
    }
  }

  private def getFolderParentId(folder: ContentFolderDto): Option[Int] =
    if (folder.parentId.isDefined && folder.parentId.get > 0) {
      folder.parentId
    } else {
      None
    }

  override def delete(folderId: Int): Future[Int] =
    db.run(ContentFolders.filter(_.id === folderId).delete)

  override def deleteByOwner(ownerId: Int): Future[Int] =
    db.run(ContentFolders.filter(_.ownerId === ownerId).delete)

}
