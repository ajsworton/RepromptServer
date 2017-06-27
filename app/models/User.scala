package models

import java.time.{LocalDate, LocalDateTime}

/**
  * @author Alexander Worton.
  */
case class User(
                  id: Int,
                  userName: String,
                  firstName: String,
                  surName: String,
                  email: String,
                  isEmailVerified: Boolean,
                  authHash: String,
                  authResetCode: Option[String],
                  authResetExpiry: Option[LocalDate],
                  authToken: Option[String],
                  authExpire: Option[LocalDateTime],
                  isEducator: Boolean,
                  isAdministrator: Boolean
               )
