package dao

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}
import javax.inject._

import slick.jdbc.JdbcProfile
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander Worton.
  */
class UserDao @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile]
{
  import profile.api._

  private val Users = TableQuery[UsersTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def get(id: Int): Future[User] = db.run(Users.filter(u => u.id === id).result.head)

  def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }

  private class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
    implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
      l => Date.valueOf(l),
      d => d.toLocalDate
    )

    implicit val localDateTimeToDateTime = MappedColumnType.base[LocalDateTime, Timestamp](
      l => Timestamp.valueOf(l),
      d => d.toLocalDateTime
    )

    def id = column[Int]("Id", O.PrimaryKey)
    def userName = column[String]("UserName")
    def firstName = column[String]("FirstName")
    def surName = column[String]("UserName")
    def email = column[String]("UserName")
    def UserName = column[String]("UserName")
    def isEmailVerified = column[Boolean]("IsEmailVerified")
    def authHash = column[String]("AuthHash")
    def authResetCode = column[Option[String]]("AuthResetCode")
    def authResetExpiry = column[Option[LocalDate]]("AuthResetExpiry")
    def authToken = column[Option[String]]("AuthToken")
    def authExpire = column[Option[LocalDateTime]]("AuthExpire")
    def isEducator = column[Boolean]("IsEducator")
    def isAdministrator = column[Boolean]("IsAdministrator")

    def * = (
      id,
      userName,
      firstName,
      surName,
      email,
      isEmailVerified,
      authHash,
      authResetCode,
      authResetExpiry,
      authToken,
      authExpire,
      isEducator,
      isAdministrator) <> (User.tupled, User.unapply _)
  }
}
