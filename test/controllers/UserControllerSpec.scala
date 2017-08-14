package controllers

import libs.AppFactory
import models.dao.CohortDaoSlick
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

import scala.concurrent.Future

/**
 * @author Alexander Worton.
 */
class UserControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with MockitoSugar with AppFactory {

  val userController: UserController = fakeApplication().injector.instanceOf[UserController]
  //.apply(FakeRequest())

  describe("UserController") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = userController.getAll()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    //    it("should return json") {
    //      val response: Future[Result] = userController.getAll()(FakeRequest())
    //
    //      contentType(response) should be ("application/json")
    //    }
    //
    //    it("should return utf-8") {
    //      val response: Future[Result] = userController.getAll()(FakeRequest())
    //
    //      charset(response) should be ("utf-8")
    //    }
    //
    //    it("should return content") {
    //      val response: Future[Result] = userController.getAll()(FakeRequest())
    //
    //      contentAsString(response) should contain ("Hello Bob")
    //    }
  }

}
