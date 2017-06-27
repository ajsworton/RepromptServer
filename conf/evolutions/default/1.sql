# Users schema

# --- !Ups

CREATE TABLE Users (
  Id int(11) NOT NULL AUTO_INCREMENT,
  UserName varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  FirstName varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  SurName varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  Email varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  IsEmailVerified tinyint(1) NOT NULL DEFAULT '0',
  AuthHash binary(60) NOT NULL,
  AuthResetCode binary(1) DEFAULT NULL,
  AuthResetExpiry date DEFAULT NULL,
  AuthToken binary(60) DEFAULT NULL,
  AuthExpire datetime DEFAULT NULL,
  IsEducator tinyint(1) NOT NULL DEFAULT '0',
  IsAdministrator tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (Id),
  UNIQUE KEY USERNAME (UserName),
  UNIQUE KEY EMAIL (Email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

# --- !Downs

DROP TABLE Users;