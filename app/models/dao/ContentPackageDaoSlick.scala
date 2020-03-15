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

import models.dto.{AnswerDto, ContentItemDto, ContentPackageDto, QuestionDto}
import models.dto.ContentPackageDto.PackageTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class ContentPackageDaoSlick @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends ContentPackageDao
    with HasDatabaseConfigProvider[JdbcProfile] {

  private val ContentPackages = TableQuery[PackageTable]

  def findContentPackageQuery(packageId: Int) =
    sql"""
          SELECT  cp.id, cp.folder_id, cp.owner_id, cp.name,
                  ci.id, ci.package_id, ci.image_url, ci.name, ci.content, 1,
                  q.id, q.question, q.format, q.item_id,
                  a.id, a.question_id, a.answer, a.correct, a.sequence

          FROM content_packages AS cp

            LEFT JOIN content_items AS ci
            ON cp.id = ci.package_id

            LEFT JOIN content_assessment_questions AS q
            ON ci.id = q.item_id

            LEFT JOIN content_assessment_answers AS a
            ON q.id = a.question_id

          WHERE cp.id = $packageId
          ORDER BY cp.name, ci.name, q.question, a.answer
         """.as[(ContentPackageDto, Option[ContentItemDto], Option[QuestionDto], Option[AnswerDto])]

  def findContentPackageQueryByOwner(ownerId: Int) =
    sql"""
          SELECT  cp.id, cp.folder_id, cp.owner_id, cp.name,
                  ci.id, ci.package_id, ci.image_url, ci.name, ci.content, 1

          FROM content_packages AS cp

              LEFT JOIN content_items AS ci
              ON cp.id = ci.package_id

          WHERE cp.owner_id = $ownerId
          ORDER BY cp.name, ci.name
         """.as[(ContentPackageDto, Option[ContentItemDto])]

  override def find(packageId: Int): Future[Option[ContentPackageDto]] = {
    val result = findContentPackageQuery(packageId)
    val run    = db.run(result)

    run.flatMap(
      r => {
        if (r.nonEmpty) {

          val groupedByQuestion = r.groupBy(_._3)
          val questions = for {
            questions <- groupedByQuestion
            questionData  = questions._2
            answers       = questionData.map(p => p._4.get).toList.filter(m => m.id.get > 0)
            questionsProc = questions._1.map(q => q.copy(answers = Some(answers)))
          } yield questionsProc

          val qList = questions.toList

          val itemsSet = r.map(p => p._2.get).toSet.filter(m => m.id.get > 0)
          val completedItems = itemsSet.toList
            .map(
              item =>
                item.copy(
                  questions = Some(
                    qList
                      .filter(q =>
                        q.isDefined &&
                          q.get.itemId == item.id.get)
                      .map(q => q.get))))
          Future(Some(r.head._1.copy(content = Some(completedItems))))
        } else {
          Future(None)
        }
      }
    )
  }

  override def findByOwner(ownerId: Int): Future[Seq[ContentPackageDto]] = {
    val result = findContentPackageQueryByOwner(ownerId)
    val run    = db.run(result)

    run.flatMap(
      r => {
        val grouped = r.groupBy(_._1)
        val out = for {
          groupMembers <- grouped
          packages     = groupMembers._2
          packagesProc = packages.map(p => p._2.get).toList.filter(m => m.id.get > 0)
          result       = groupMembers._1.copy(content = Some(packagesProc))
        } yield result
        Future(out.toSeq)
      }
    )
  }

  override def save(packageDto: ContentPackageDto): Future[Option[ContentPackageDto]] =
    db.run(
      (ContentPackages returning ContentPackages.map(_.id)
        into ((pkg, returnedId) => Some(pkg.copy(id = returnedId)))) += packageDto)

  override def update(packageDto: ContentPackageDto): Future[Option[ContentPackageDto]] =
    if (packageDto.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(
          ContentPackages
            .filter(_.id === packageDto.id)
            .map(c => (c.name, c.folderId, c.ownerId))
            .update(packageDto.name, packageDto.folderId, packageDto.ownerId))
        read <- find(packageDto.id.get)
      } yield read
    }

  override def delete(packageId: Int): Future[Int] =
    db.run(ContentPackages.filter(_.id === packageId).delete)

  override def deleteByOwner(ownerId: Int): Future[Int] =
    db.run(ContentPackages.filter(_.ownerId === ownerId).delete)
}
