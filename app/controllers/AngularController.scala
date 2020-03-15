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
import java.nio.file.NoSuchFileException
import javax.inject._

import org.apache.commons.io.IOUtils
import play.api.{ Environment, Play }
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

  /**
   * Endpoint to serve the Angular index
   * @return the angular index html file
   */
  def index = {
    assets.at(path = "/public/", "index.html")
  }

  /**
   * Endpoint to serve the Angular index when an int parameter is supplied
   * @param id
   * @return JSON
   */
  def indexParam(id: Int) = {
    index()
  }

  /**
   * Endpoint to serve the Angular index for webpack bundled packages
   * @param file the remaining uri
   * @return JSON
   */
  def angular(file: String) = assets.at(path = "/public/", file)

  /**
   * Endpoint to return file media previously uploaded
   * @param file the requested file
   * @return JSON
   */
  def media(file: String) = Action {
    try {
      Ok.sendFile(
        content = new File(s"media/$file"),
        inline = true
      )
    } catch {
      case _: FileNotFoundException => NotFound(s"Image not found, ${environment.rootPath}/media/$file")
      case _: NoSuchFileException => NotFound(s"No Such Image, ${environment.rootPath}/media/$file")
    }
  }
}
