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

package controllers

import java.io.{ File, FileInputStream, FileNotFoundException }
import javax.inject._

import org.apache.commons.io.IOUtils
import play.api.Environment
import play.api.mvc.{ AbstractController, ControllerComponents }
import play.libs.ws.WSClient

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class AngularController @Inject() (assets: Assets, cc: ControllerComponents, ws: WSClient,
  environment: Environment)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index = {
    assets.versioned(path = "/public/", "index.html")
  }

  def angular(file: String) = assets.at(path = "/public/", file)

  def media(file: String) = Action {
    try {
      getAndReturnImage(file)
    } catch {
      case _: FileNotFoundException => NotFound("Image not found")
    }
  }

  def getAndReturnImage(file: String) = {
    val image: File = new File("public/media/" + file)
    if (image.exists) {
      val fileType = "image/" + file.split('.').reverse.head
      Ok(IOUtils.toByteArray(new FileInputStream(image))).as(fileType)
    } else {
      NotFound(s"Image not found: ${image.toString}")
    }
  }

}
