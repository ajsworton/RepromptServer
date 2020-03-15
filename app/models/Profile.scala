// Copyright (C) 2017 Alexander Worton.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package models

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info, SocialProfile}
import play.api.libs.json.{JsSuccess, Json, OFormat}
import slick.jdbc.{GetResult, JdbcType}
import slick.jdbc.PostgresProfile.api._
import slick.lifted
import slick.lifted.{PrimaryKey, ProvenShape}

case class Profile(
    userId: Option[Int] = None,
    loginInfo: LoginInfo,
    confirmed: Boolean = false,
    email: Option[String] = None,
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    fullName: Option[String] = None,
    passwordInfo: Option[PasswordInfo] = None,
    oauth1Info: Option[OAuth1Info] = None,
    oauth2Info: Option[OAuth2Info] = None,
    avatarUrl: Option[String] = None
) extends SocialProfile

/**
  * Companion Object for to hold boiler plate for forms, json conversion, slick
  */
object Profile {

  implicit val getResult = GetResult(
    r =>
      Profile(
        Some(r.nextInt),
        LoginInfo(r.nextString, r.nextString),
        r.nextBoolean,
        r.nextString match { case null => None; case "" => None; case str => Some(str) },
        r.nextString match { case null => None; case "" => None; case str => Some(str) },
        r.nextString match { case null => None; case "" => None; case str => Some(str) },
        r.nextString match { case null => None; case "" => None; case str => Some(str) },
    ))

  class ProfilesTable(tag: Tag) extends Table[Profile](tag, "profiles") {

    implicit val oAuth1JsonFormat: OFormat[OAuth1Info] = Json.format[OAuth1Info]
//    implicit val oAuth1ColumnType: JdbcType[OAuth1Info] = MappedColumnType.base[OAuth1Info, String](
//      authInfo => Json.stringify(Json.toJson(authInfo)),
//      column => Json.parse(column).as[OAuth1Info]
//    )
    implicit val oAuth2JsonFormat: OFormat[OAuth2Info] = Json.format[OAuth2Info]
//    implicit val oAuth2ColumnType: JdbcType[OAuth2Info] = MappedColumnType.base[OAuth2Info, String](
//      authInfo => Json.stringify(Json.toJson(authInfo)),
//      column => Json.parse(column).as[OAuth2Info]
//    )
    implicit val passwordInfoJsonFormat: OFormat[PasswordInfo] = Json.format[PasswordInfo]
//    implicit val passwordInfoColumnType: JdbcType[PasswordInfo] = MappedColumnType.base[PasswordInfo, String](
//      authInfo => Json.stringify(Json.toJson(authInfo)),
//      column => Json.parse(column).as[PasswordInfo]
//    )

    def userId: lifted.Rep[Option[Int]]          = column[Int]("user_id", O.PrimaryKey)
    def providerId: lifted.Rep[String]           = column[String]("provider_id", O.PrimaryKey)
    def providerKey: lifted.Rep[String]          = column[String]("provider_key", O.PrimaryKey)
    def confirmed: lifted.Rep[Boolean]           = column[Boolean]("confirmed")
    def email: lifted.Rep[Option[String]]        = column[Option[String]]("email")
    def firstName: lifted.Rep[Option[String]]    = column[Option[String]]("first_name")
    def lastName: lifted.Rep[Option[String]]     = column[Option[String]]("last_name")
    def fullName: lifted.Rep[Option[String]]     = column[Option[String]]("full_name")
    def passwordInfo: lifted.Rep[Option[String]] = column[Option[String]]("password_info")
    def oauth1Info: lifted.Rep[Option[String]]   = column[Option[String]]("oauth1_info")
    def oauth2Info: lifted.Rep[Option[String]]   = column[Option[String]]("oauth2_info")
    def avatarUrl: lifted.Rep[Option[String]]    = column[Option[String]]("avatar_url")
    def pk: PrimaryKey                           = primaryKey("PRIMARY", (userId, providerId, providerKey))

    def * : ProvenShape[Profile] =
      (userId, providerId, providerKey, confirmed, email, firstName, lastName, fullName, passwordInfo, oauth1Info, oauth2Info, avatarUrl) <>
        ((constructProfile _).tupled, deconstructProfile)

    def constructProfile(userId: Option[Int],
                         providerId: String,
                         providerKey: String,
                         confirmed: Boolean,
                         email: Option[String],
                         firstName: Option[String],
                         lastName: Option[String],
                         fullName: Option[String],
                         passwordInfo: Option[String],
                         oAuth1Info: Option[String],
                         oAuth2Info: Option[String],
                         avatarUrl: Option[String]): Profile = {

      val parsedPasswordInfo: Option[PasswordInfo] = parsePasswordInfo(passwordInfo)
      val parsedOauth1Info: Option[OAuth1Info]     = parseOauth1Info(oAuth1Info)
      val parsedOauth2Info: Option[OAuth2Info]     = parseOauth2Info(oAuth2Info)

      Profile(userId,
              LoginInfo(providerId, providerKey),
              confirmed,
              email,
              firstName,
              lastName,
              fullName,
              parsedPasswordInfo,
              parsedOauth1Info,
              parsedOauth2Info,
              avatarUrl)
    }

    def deconstructProfile(profile: Profile) = profile match {
      case Profile(userId: Option[Int],
                   LoginInfo(providerId: String, providerKey: String),
                   confirmed: Boolean,
                   email: Option[String],
                   firstName: Option[String],
                   lastName: Option[String],
                   fullName: Option[String],
                   passwordInfo: Option[PasswordInfo],
                   oauth1Info: Option[OAuth1Info],
                   oauth2Info: Option[OAuth2Info],
                   avatarUrl: Option[String]) => {
        Option(
          userId,
          providerId,
          providerKey,
          confirmed,
          email,
          firstName,
          lastName,
          fullName,
          Some(Json.toJson(passwordInfo).toString()),
          Some(Json.toJson(oauth1Info).toString()),
          Some(Json.toJson(oauth2Info).toString()),
          avatarUrl
        )
      }
    }

    def parsePasswordInfo(passwordInfo: Option[String]): Option[PasswordInfo] =
      passwordInfo match {
        case None => None
        case Some(authInfo) =>
          Json.parse(authInfo).validate[PasswordInfo] match {
            case s: JsSuccess[PasswordInfo] => Some(s.get)
            case _                          => Option.empty[PasswordInfo]
          }
      }

    def parseOauth1Info(oAuth1Info: Option[String]): Option[OAuth1Info] =
      oAuth1Info match {
        case None => None
        case Some(authInfo) =>
          Json.parse(authInfo).validate[OAuth1Info] match {
            case s: JsSuccess[OAuth1Info] => Some(s.get)
            case _                        => Option.empty[OAuth1Info]
          }
      }

    def parseOauth2Info(oAuth2Info: Option[String]): Option[OAuth2Info] =
      oAuth2Info match {
        case None => None
        case Some(authInfo) =>
          Json.parse(authInfo).validate[OAuth2Info] match {
            case s: JsSuccess[OAuth2Info] => Some(s.get)
            case _                        => Option.empty[OAuth2Info]
          }
      }

  }
}
