package dto

import java.time.LocalDate

import models.User
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{Format, Json, __}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * @author Alexander Worton.
  */
case class UserDto(
                    id: Int,
                    userName: String,
                    firstName: String,
                    surName: String,
                    email: String,
                    isEmailVerified: Boolean,
                    isEducator: Boolean,
                    isAdministrator: Boolean
                  )


object UserDto {

  def apply(user: User) = {
    new UserDto(
      user.id,
      user.userName,
      user.firstName,
      user.surName,
      user.email,
      user.isEmailVerified,
      user.isEducator,
      user.isAdministrator)
  }

  implicit val userDtoFormat = Json.format[UserDto]
}


