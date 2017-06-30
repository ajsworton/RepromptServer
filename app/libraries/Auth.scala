package libraries

import org.mindrot.jbcrypt.BCrypt

object Auth {
    def login(username: String, clearPass: String, hashedPass: String): Boolean = {
        BCrypt.checkpw(clearPass, hashedPass)
    }

    def encrypt(clearPass: String): String = {
      val encrypted = BCrypt.hashpw(clearPass, BCrypt.gensalt())
      val checked = BCrypt.checkpw(clearPass, encrypted)
      encrypted + " :: " + checked
    }
}