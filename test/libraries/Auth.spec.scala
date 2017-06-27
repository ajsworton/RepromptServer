import org.mindrot.jbcrypt.BCrypt
import org.scalatest.FunSpec

class ExampleSpec extends FunSpec {
  describe("Auth") {

    it("should respond with success when provided with matching passwords") {
      val password = "password"
      val encrypted = BCrypt.hashpw(password, BCrypt.gensalt)
      assert(Auth.login("aUser", password, encrypted))
    }

    it("should respond with failure when provided with non-matching passwords") {
      val password = "password"
      val encrypted = BCrypt.hashpw("somethingElse", BCrypt.gensalt)
      assert(!Auth.login("aUser", password, encrypted))
    }
  }
}