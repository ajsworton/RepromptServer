import org.mindrot.jbcrypt.BCrypt

object Auth {
    def login(username: String, clearPass: String, hashedPass: String): Boolean = {
        BCrypt.checkpw(clearPass, hashedPass)
    }
}