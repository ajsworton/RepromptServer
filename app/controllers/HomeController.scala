package controllers

import javax.inject._

import dao.UserDao
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
      userDao: UserDao,
      cc: ControllerComponents
      )(implicit ec: ExecutionContext)
  extends AbstractController(cc){

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
//  def index() = Action { implicit request: Request[AnyContent] =>
//    Ok(views.html.index())
//  }
  def index = Action.async {
    userDao.all().map { case (users) => Ok(views.html.index(users)) }
  }

}
