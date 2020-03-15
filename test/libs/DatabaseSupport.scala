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
// Code sourced from https://stackoverflow.com/questions/46984028/how-to-apply-play-evolutions-when-running-tests-in-play-framework

package libs

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.scalatest.{BeforeAndAfterAll, Suite}
import play.api.Application
import play.api.db.evolutions.Evolutions
import play.api.db.{DBApi, Database}
import play.api.inject.guice.GuiceApplicationBuilder

trait DatabaseSupport extends BeforeAndAfterAll {
  this: Suite =>

  val pg: EmbeddedPostgres = EmbeddedPostgres.start()

  val app: Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.db.user" -> "postgres")
    .configure("slick.dbs.default.db.password" -> "")
    .configure("slick.dbs.default.db.url" -> s"jdbc:postgresql://127.0.0.1:${pg.getPort}/postgres")
    .build()

  private lazy val db = app.injector.instanceOf[DBApi]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    initializeEvolutions(db.database("default"))
  }

  override protected def afterAll(): Unit = {
    cleanupEvolutions(db.database("default"))
    pg.close()
    super.afterAll()
  }

  private def initializeEvolutions(database: Database): Unit = {
    Evolutions.cleanupEvolutions(database)
    Evolutions.applyEvolutions(database)
  }

  private def cleanupEvolutions(database: Database): Unit = {
    Evolutions.cleanupEvolutions(database)
  }

}
