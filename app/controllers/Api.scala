//// Copyright (C) 2017 Alexander Worton.
//// See the LICENCE.txt file distributed with this work for additional
//// information regarding copyright ownership.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
//
//package controllers
//
//import javax.inject.{Inject, Singleton}
//
//import com.mohiva.play.silhouette.api._
//import environments.JWTEnvironment
//import play.api.libs.json.Json
//import play.mvc.Controller
//import play.api.mvc.{Action, AnyContent}
//import play.api.mvc.{AbstractController, ControllerComponents}
//
//import scala.concurrent.ExecutionContext
//import scala.concurrent.Future
//
//@Singleton
//class Api @Inject()(cc: ControllerComponents,
//                    silhouette: Silhouette[JWTEnvironment])
//                   (implicit ec: ExecutionContext)
//  extends AbstractController(cc){
//
//  def login = Action.async { implicit request =>
//    val paramVal = request.body.asFormUrlEncoded.get("param").map(_.head)
//  }
//}
