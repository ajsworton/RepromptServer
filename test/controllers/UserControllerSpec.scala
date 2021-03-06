package controllers

import libs.{ AppFactory, AuthHelper, TestingDbQueries }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }
import play.api.test.FakeRequest
import play.api.mvc._
import play.api.test.Helpers._

import scala.concurrent.Future

/**
 * @author Alexander Worton.
 */
class UserControllerSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val helper: AuthHelper = fakeApplication().injector.instanceOf[AuthHelper]
  val controller: UserController = fakeApplication().injector.instanceOf[UserController]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]

  val teacherId = 9859855
  val studentId = 9859856
  helper.setup()

  var studentFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _
  var educatorFakeRequest: FakeRequest[AnyContentAsEmpty.type] = _

  before {
    database.insertStudyContent(teacherId, studentId, studentId + 1)
    helper.educatorId = teacherId
    helper.studentId = studentId
    helper.setup()
    studentFakeRequest = helper.studentFakeRequest
    educatorFakeRequest = helper.educatorFakeRequest
  }

  after {
    database.clearStudyContent(teacherId, studentId, studentId + 1)
  }

  describe("getAll") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.getAll()(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not an educator") {
      val response: Future[Result] = controller.getAll()(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.getAll()(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.getAll()(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.getAll()(educatorFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

  describe("get(id: Int)") {
    it("should return code 401 If not authenticated") {
      val response: Future[Result] = controller.get(1)(FakeRequest())
      status(response) should be(UNAUTHORIZED)
    }

    it("should return 403 unauthorised if not an educator") {
      val response: Future[Result] = controller.get(1)(studentFakeRequest)
      status(response) should be(FORBIDDEN)
    }

    it("should return 200 OK if an educator") {
      val response: Future[Result] = controller.get(1)(educatorFakeRequest)
      status(response) should be(OK)
    }

    it("should return json") {
      val response: Future[Result] = controller.get(1)(educatorFakeRequest)
      contentType(response) should be(Some("application/json"))
    }

    it("should return content") {
      val response: Future[Result] = controller.get(1)(educatorFakeRequest)
      contentAsString(response).length should be > 0
    }
  }

}
