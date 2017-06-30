package controllers

import javax.inject._

import play.api.{Environment, Mode}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._

// Import required for injection
import scala.concurrent.ExecutionContext
import play.libs.ws._


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient, environment: Environment)
                                (implicit ec: ExecutionContext) extends AbstractController(cc)
{

  def index = Assets.versioned(path="/public/dist", "index.html")

  def dist(file: String) = Assets.versioned(path="/public/dist", file)

}
