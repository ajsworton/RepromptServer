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

package modules

import com.google.inject.AbstractModule
import models.dao.{
  CohortDao,
  CohortDaoSlick,
  ContentAssignedDao,
  ContentAssignedDaoSlick,
  ContentFolderDao,
  ContentFolderDaoSlick,
  ContentItemDao,
  ContentItemDaoSlick,
  ContentPackageDao,
  ContentPackageDaoSlick,
  ProgressDao,
  ProgressDaoSlick,
  StudyDao,
  StudyDaoSlick,
  UserDao,
  UserDaoSlick
}
import net.codingwell.scalaguice.ScalaModule

/**
 * The base Guice module.
 */
class RepromptModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[UserDao].to[UserDaoSlick]
    bind[CohortDao].to[CohortDaoSlick]
    bind[ContentFolderDao].to[ContentFolderDaoSlick]
    bind[ContentPackageDao].to[ContentPackageDaoSlick]
    bind[ContentItemDao].to[ContentItemDaoSlick]
    bind[ContentAssignedDao].to[ContentAssignedDaoSlick]
    bind[StudyDao].to[StudyDaoSlick]
    bind[ProgressDao].to[ProgressDaoSlick]
  }
}
