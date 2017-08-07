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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE cohort_members (
  CohortId int(11) NOT NULL,
  UserId int(11) NOT NULL,
  PRIMARY KEY (CohortId, UserId),
  FOREIGN KEY (CohortId) REFERENCES cohorts(Id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (UserId) REFERENCES users(Id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_assigned (
  Id int(11) NOT NULL AUTO_INCREMENT,
  Name varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  ExamDate date DEFAULT NULL,
  Active tinyint(1) NOT NULL DEFAULT '1',
  OwnerId int(11) DEFAULT NULL,
  PRIMARY KEY (Id),
  KEY OwnerId (OwnerId),
  CONSTRAINT content_assigned_ibfk_1 FOREIGN KEY (OwnerId) REFERENCES users (Id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_assigned_cohorts (
  AssignedId int(11) NOT NULL,
  CohortId int(11) NOT NULL,
  PRIMARY KEY (AssignedId,CohortId),
  KEY CohortId (CohortId),
  CONSTRAINT content_assigned_cohorts_ibfk_1 FOREIGN KEY (AssignedId) REFERENCES content_assigned (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_assigned_cohorts_ibfk_2 FOREIGN KEY (CohortId) REFERENCES cohorts (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_folders (
  Id int(11) NOT NULL AUTO_INCREMENT,
  OwnerId int(11) DEFAULT NULL,
  Name varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  ParentId int(11) DEFAULT NULL,
  PRIMARY KEY (Id),
  KEY OwnerId (OwnerId),
  KEY ParentId (ParentId),
  CONSTRAINT content_folders_ibfk_1 FOREIGN KEY (OwnerId) REFERENCES users (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_folders_ibfk_2 FOREIGN KEY (ParentId) REFERENCES content_folders (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_packages (
  Id int(11) NOT NULL AUTO_INCREMENT,
  Name varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  FolderId int(11) DEFAULT NULL,
  OwnerId int(11) DEFAULT NULL,
  PRIMARY KEY (Id),
  UNIQUE KEY FolderId (FolderId,OwnerId,Name),
  KEY OwnerId (OwnerId),
  CONSTRAINT content_packages_ibfk_1 FOREIGN KEY (FolderId) REFERENCES content_folders (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_packages_ibfk_2 FOREIGN KEY (OwnerId) REFERENCES users (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_assigned_packages (
  AssignedId int(11) NOT NULL,
  PackageId int(11) NOT NULL,
  PRIMARY KEY (AssignedId,PackageId),
  KEY PackageId (PackageId),
  CONSTRAINT content_assigned_packages_ibfk_1 FOREIGN KEY (AssignedId) REFERENCES content_assigned (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_assigned_packages_ibfk_2 FOREIGN KEY (PackageId) REFERENCES content_packages (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_items (
  Id int(11) NOT NULL AUTO_INCREMENT,
  PackageId int(11) NOT NULL,
  ImageUrl varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  Content text COLLATE utf8_unicode_ci,
  Name varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (Id),
  KEY PackageId (PackageId),
  CONSTRAINT content_items_ibfk_1 FOREIGN KEY (PackageId) REFERENCES content_packages (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_assessment_questions (
  Id int(11) NOT NULL AUTO_INCREMENT,
  Question text CHARACTER SET utf8 COLLATE utf8_unicode_520_ci,
  Format varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_520_ci DEFAULT NULL,
  ItemId int(11) DEFAULT NULL,
  PRIMARY KEY (Id),
  KEY ItemId (ItemId),
  CONSTRAINT content_assessment_questions_ibfk_1 FOREIGN KEY (ItemId) REFERENCES content_items (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_assessment_answers (
  Id int(11) NOT NULL AUTO_INCREMENT,
  QuestionId int(11) NOT NULL,
  Answer text COLLATE utf8_unicode_ci,
  Correct tinyint(1) NOT NULL DEFAULT '0',
  Sequence int(11) DEFAULT '0',
  PRIMARY KEY (Id),
  KEY QuestionId (QuestionId),
  CONSTRAINT content_assessment_answers_ibfk_1 FOREIGN KEY (QuestionId) REFERENCES content_assessment_questions (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_scores (
  UserId int(11) NOT NULL,
  ContentItemId int(11) NOT NULL,
  LastScore int(3) DEFAULT NULL,
  RepromptDate date DEFAULT NULL,
  Streak int(3) DEFAULT NULL,
  PRIMARY KEY (UserId,ContentItemId),
  KEY ContentId (ContentItemId),
  CONSTRAINT content_scores_ibfk_1 FOREIGN KEY (UserId) REFERENCES users (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_scores_ibfk_2 FOREIGN KEY (ContentItemId) REFERENCES content_items (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

# --- !Downs

DROP TABLE IF EXISTS content_scores;
DROP TABLE IF EXISTS content_assessment_answers;
DROP TABLE IF EXISTS content_assessment_questions;
DROP TABLE IF EXISTS content_assigned_cohorts;
DROP TABLE IF EXISTS content_assigned_packages;
DROP TABLE IF EXISTS content_assigned;
DROP TABLE IF EXISTS content_items;
DROP TABLE IF EXISTS content_packages;
DROP TABLE IF EXISTS content_folders;

DROP TABLE IF EXISTS cohorts;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS cohort_members;