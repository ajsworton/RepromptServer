# Users schema
# Profiles schema

# --- !Ups

CREATE TABLE Users (
  Id int(11) NOT NULL AUTO_INCREMENT,
  FirstName varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  SurName varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  Email varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  IsEmailVerified tinyint(1) NOT NULL DEFAULT '0',
  IsEducator tinyint(1) NOT NULL DEFAULT '0',
  IsAdministrator tinyint(1) NOT NULL DEFAULT '0',
  AvatarUrl varchar(255) DEFAULT NULL,
  PRIMARY KEY (Id),
  UNIQUE KEY EMAIL (Email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE Profiles(
  UserId INT NOT NULL,
  ProviderId VARCHAR(255) NOT NULL,
  ProviderKey VARCHAR(255) NOT NULL,
  Confirmed BOOL NOT NULL,
  Email VARCHAR(255),
  FirstName VARCHAR(255),
  LastName VARCHAR(255),
  FullName VARCHAR(255),
  PasswordInfo JSON,
  OAuth1Info JSON,
  OAuth2Info JSON,
  AvatarUrl VARCHAR(255),
  PRIMARY KEY (UserId, ProviderId, ProviderKey),
  FOREIGN KEY (UserId) REFERENCES Users(Id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=INNODB CHARSET=utf8 COLLATE=utf8_unicode_ci;

# --- !Downs

DROP TABLE Users;
DROP TABLE Profiles;