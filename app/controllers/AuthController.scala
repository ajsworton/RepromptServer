package controllers

import javax.inject.{Inject, Singleton}

import dao.UserDao
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * @author Alexander Worton.
  */
@Singleton
class AuthController @Inject()(
                                  userDao: UserDao,
                                  cc: ControllerComponents
                                )(implicit ec: ExecutionContext)
  extends AbstractController(cc){

  /**
    * Create an Action to render a JSON response page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/auth`.
    */
  def login(userName: String) = Action {
    Ok(s"All Good, $userName!")
  }

}
