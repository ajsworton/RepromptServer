# Users schema
# Profiles schema

# --- !Ups

CREATE TABLE users (
  Id int(11) NOT NULL AUTO_INCREMENT,
  FirstName varchar(255) NOT NULL,
  SurName varchar(255) NOT NULL,
  Email varchar(255) NOT NULL,
  IsEmailVerified tinyint(1) NOT NULL DEFAULT '0',
  IsEducator tinyint(1) NOT NULL DEFAULT '0',
  IsAdministrator tinyint(1) NOT NULL DEFAULT '0',
  AvatarUrl varchar(255) DEFAULT NULL,
  PRIMARY KEY (Id),
  UNIQUE KEY EMAIL (Email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator)
VALUES (1, 'Admin', 'User', 'Undefined', '1', '1', '1');

CREATE TABLE profiles(
  UserId INT NOT NULL,
  ProviderId VARCHAR(255) NOT NULL,
  ProviderKey VARCHAR(255) NOT NULL,
  Confirmed tinyint(1) NOT NULL DEFAULT '0',
  Email VARCHAR(255),
  FirstName VARCHAR(255),
  LastName VARCHAR(255),
  FullName VARCHAR(255),
  PasswordInfo JSON,
  OAuth1Info JSON,
  OAuth2Info JSON,
  AvatarUrl VARCHAR(255),
  PRIMARY KEY (UserId, ProviderId, ProviderKey),
  FOREIGN KEY (UserId) REFERENCES users(Id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO profiles (UserId, ProviderId, ProviderKey, Confirmed, FirstName)
VALUES (1, 'credentials', 'admin', 1 , 'Admin');

CREATE TABLE cohorts (
  Id int(11) NOT NULL AUTO_INCREMENT,
  OwnerId int(11) DEFAULT NULL,
  Name varchar(255) DEFAULT NULL,
  ParentId int(11) DEFAULT NULL,
  PRIMARY KEY (Id),
  FOREIGN KEY (OwnerId) REFERENCES users(Id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (ParentId) REFERENCES cohorts(Id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE cohort_members (
  CohortId int(11) NOT NULL,
  UserId int(11) NOT NULL,
  PRIMARY KEY (CohortId, UserId),
  FOREIGN KEY (CohortId) REFERENCES cohorts(Id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (UserId) REFERENCES users(Id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

# --- !Downs

DROP TABLE IF EXISTS cohort_members;
DROP TABLE IF EXISTS cohorts;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS users;