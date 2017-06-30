package controllers

import java.time.LocalDate
import javax.inject._

import libraries.Auth
import dao.UserDao
import dto.UserDto
import models.User
import play.api.mvc._
import play.api.libs.json._


import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject()(
      cc: ControllerComponents,
      userDao: UserDao
      )(implicit ec: ExecutionContext)
  extends AbstractController(cc){

  //implicit val userFormat = Json.format[User]
  //implicit val userDtoFormat = Json.format[UserDto]


  def getAll = Action.async {
    userDao.all().map(users => Ok(Json.toJson(users.map(u => UserDto(u)))))
  }

  def get(id: Int) = Action.async {
    userDao.get(id).map(user => Ok(Json.toJson(UserDto(user))))
  }

//  def create(fname: String, sname: String) = Action {
//        val user = new UserDao()
//    implicit request: Request[AnyContent] => Ok(views.html.user())
//  }

}
