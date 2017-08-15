# users schema
# profiles schema
# cohorts schema
# cohort_members schema
# content_assigned schema
# content_assigned_cohorts schema
# content_folders schema
# content_packages schema
# content_assigned_packages schema
# content_items schema
# content_assessment_questions schema
# content_assessment_answers schema
# content_scores schema

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

INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (1, 'Admin', 'User', 'Undefined', '1', '1', '1');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (2, 'Professor', 'Teacher', 't@e', '1', '1', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (3, 'Professor2', 'Teacher2', 't@e2', '1', '1', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (4, 'Test', 'Student', 't@s', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (5, 'Jacob', 'Benson', 'Jacob@Benson', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (6, 'Chelsea', 'Reynolds', 'Chelsea@Reynolds', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (7, 'Eve', 'Hodgson', 'Eve@Hodgson', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (8, 'Christopher', 'Hammond', 'Christopher@Hammond', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (9, 'Freya', 'Williamson', 'Freya@Williamson', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (10, 'Tyler', 'Randall', 'Tyler@Randall', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (11, 'Lucas', 'Fox', 'Lucas@Fox', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (12, 'Daniel', 'Patterson', 'Daniel@Patterson', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (13, 'Zak', 'Graham', 'Zak@Graham', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (14, 'Hollie', 'Ross', 'Hollie@Ross', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (15, 'Alexander', 'Power', 'Alexander@Power', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (16, 'Rachel', 'Morley', 'Rachel@Morley', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (17, 'Elise', 'Gibbs', 'Elise@Gibbs', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (18, 'Toby', 'King', 'Toby@King', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (19, 'Zachary', 'Finch', 'Zachary@Finch', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (20, 'Bradley', 'Warren', 'Bradley@Warren', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (21, 'Reece', 'Schofield', 'Reece@Schofield', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (22, 'Abbie', 'Reed', 'Abbie@Reed', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (23, 'Finley', 'Nixon', 'Finley@Nixon', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (24, 'Shannon', 'Collins', 'Shannon@Collins', '1', '0', '0');
INSERT INTO users (Id, FirstName, SurName, Email, IsEmailVerified, IsEducator, IsAdministrator) VALUES (25, 'Lydia', 'Riley', 'Lydia@Riley', '1', '0', '0');

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

INSERT INTO profiles (UserId, ProviderId, ProviderKey, Confirmed, FirstName) VALUES (1, 'credentials', 'admin', 1 , 'Admin');
INSERT INTO profiles (UserId, ProviderId, ProviderKey, Confirmed, PasswordInfo) VALUES (2, 'credentials', 't@e', 1 , '{"hasher": "bcrypt", "password": "$2a$10$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}');
INSERT INTO profiles (UserId, ProviderId, ProviderKey, Confirmed, PasswordInfo) VALUES (3, 'credentials', 't@e2', 1 , '{"hasher": "bcrypt", "password": "$2a$10$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}');
INSERT INTO profiles (UserId, ProviderId, ProviderKey, Confirmed, PasswordInfo) VALUES (4, 'credentials', 't@s', 1 , '{"hasher": "bcrypt", "password": "$2a$10$1sKWPcIQo8anKCb1XZ4m/.fLWRj6b1XJP6L47LIJNhtvyv13rpEpS"}');

CREATE TABLE cohorts (
  Id int(11) NOT NULL AUTO_INCREMENT,
  OwnerId int(11) DEFAULT NULL,
  Name varchar(255) DEFAULT NULL,
  ParentId int(11) DEFAULT NULL,
  PRIMARY KEY (Id),
  FOREIGN KEY (OwnerId) REFERENCES users(Id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (ParentId) REFERENCES cohorts(Id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO cohorts (Id, OwnerId, Name) VALUES (1, 2, '2017 MSc Computer Science FT');
INSERT INTO cohorts (Id, OwnerId, Name) VALUES (2, 3, '2017 MSc CS FT');

CREATE TABLE cohort_members (
  CohortId int(11) NOT NULL,
  UserId int(11) NOT NULL,
  PRIMARY KEY (CohortId, UserId),
  FOREIGN KEY (CohortId) REFERENCES cohorts(Id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (UserId) REFERENCES users(Id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 4);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 5);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 6);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 7);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 8);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 9);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 10);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 11);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 12);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 20);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 21);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 22);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 23);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 24);
INSERT INTO cohort_members (CohortId, UserId) VALUES (1, 25);

INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 4);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 5);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 6);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 7);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 8);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 9);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 10);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 11);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 12);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 13);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 14);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 15);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 16);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 17);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 18);
INSERT INTO cohort_members (CohortId, UserId) VALUES (2, 19);


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

INSERT INTO content_assigned (Id, Name, ExamDate, Active, OwnerId)
VALUES(1,'2017 MSc SDP','2017-10-01', 1, 2);
INSERT INTO content_assigned (Id, Name, ExamDate, Active, OwnerId)
VALUES(2,'2017 MSc IS','2017-12-25', 1, 3);

CREATE TABLE content_assigned_cohorts (
  AssignedId int(11) NOT NULL,
  CohortId int(11) NOT NULL,
  PRIMARY KEY (AssignedId,CohortId),
  KEY CohortId (CohortId),
  CONSTRAINT content_assigned_cohorts_ibfk_1 FOREIGN KEY (AssignedId) REFERENCES content_assigned (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_assigned_cohorts_ibfk_2 FOREIGN KEY (CohortId) REFERENCES cohorts (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO content_assigned_cohorts (AssignedId, CohortId)
VALUES(1, 1);
INSERT INTO content_assigned_cohorts (AssignedId, CohortId)
VALUES(2, 1);

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

INSERT INTO content_folders (Id, OwnerId, Name, ParentId)
VALUES(1,3,'CS',NULL);
INSERT INTO content_folders (Id, OwnerId, Name, ParentId)
VALUES(2,2,'Computer Science',NULL);
INSERT INTO content_folders (Id, OwnerId, Name, ParentId)
VALUES(3,2,'SDP',2);

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

INSERT INTO content_packages (Id, Name, FolderId, OwnerId)
VALUES('1','SDLC','1','3');
INSERT INTO content_packages (Id, Name, FolderId, OwnerId)
VALUES('2','Design Patterns','3','2');

CREATE TABLE content_assigned_packages (
  AssignedId int(11) NOT NULL,
  PackageId int(11) NOT NULL,
  PRIMARY KEY (AssignedId,PackageId),
  KEY PackageId (PackageId),
  CONSTRAINT content_assigned_packages_ibfk_1 FOREIGN KEY (AssignedId) REFERENCES content_assigned (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_assigned_packages_ibfk_2 FOREIGN KEY (PackageId) REFERENCES content_packages (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO content_assigned_packages (AssignedId, PackageId)
VALUES(1, 2);
INSERT INTO content_assigned_packages (AssignedId, PackageId)
VALUES(2, 1);

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

INSERT INTO content_items(Id,PackageId,ImageUrl,Content,Name) VALUES (1,1,'media/3/content/1/items/1.png','<p>The Software Development Life Cycle outlines the stages involved in creating information systems.</p><p>A methodology is a formalised approach for implementing the SDLC.</p><p><strong>Planning </strong>- The planning phase is all about why the system should be built and consists of planning and project management.</p><p><strong>Analyse </strong>- The analysis phase is about determining <em>what </em>the system should do and <em>where </em>and <em>when </em>it will be used. It includes developing an analysis strategy, gathering requirements and developing a proposal.</p><p><strong>Design </strong>- The design phase is about how the system will be built. It includes developing a design strategy, developing the architecture, designing the database, file structure and programs.</p><p><strong>Implement </strong>- The implementation phase is all about building the system. It involves constructing, installing and supporting the system.</p>','SDLC Overview');
INSERT INTO content_items(Id,PackageId,ImageUrl,Content,Name) VALUES (2,2,'media/2/content/2/items/2.gif','<p>Ensure that only one instance of a class is created and Provide a global access point to the object.</p><p>Singleton pattern should be used when we must ensure that only one instance of a class is created and when the instance must be available through all the code. A special care should be taken in multi-threading environments when multiple threads must access the same resources through the same singleton object.</p><p>There are many common situations when singleton pattern is used:</p><ul><li>Logger Classes</li><li>Configuration Classes</li><li>Accessing resources in shared mode</li><li>Other design patterns implemented as Singletons: Factories and Abstract Factories, Builder, Prototype</li></ul>','Singleton');
INSERT INTO content_items(Id,PackageId,ImageUrl,Content,Name) VALUES (3,2,'media/2/content/2/items/3.gif','<p>Creates objects without exposing the instantiation logic to the client and Refers to the newly created object through a common interface.</p><p>Factory pattern should be used when: - a framework delegate the creation of objects derived from a common super class to the factory - we need flexibility in adding new types of objects that must be created by the class</p><p>Along with singleton pattern the factory is one of the most used patterns. Almost any application has some factories. Here are a some examples in java:</p><ul><li>factories providing an xml parser: javax.xml.parsers.DocumentBuilderFactory or javax.xml.parsers.SAXParserFactory</li><li>java.net.URLConnection - allows users to decide which protocol to use</span></li></ul>','Factory');

CREATE TABLE content_assessment_questions (
  Id int(11) NOT NULL AUTO_INCREMENT,
  Question text CHARACTER SET utf8 COLLATE utf8_unicode_520_ci,
  Format varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_520_ci DEFAULT NULL,
  ItemId int(11) DEFAULT NULL,
  PRIMARY KEY (Id),
  KEY ItemId (ItemId),
  CONSTRAINT content_assessment_questions_ibfk_1 FOREIGN KEY (ItemId) REFERENCES content_items (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (1,'Which phase of the SDLC involves developing the proposal?','MCSA',1);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (2,'Which phase of the SDLC involves installing the system?','MCSA',1);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (3,'Put the phases of the SDLC into the correct order','SORT',1);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (4,'Which of the following is not an advantage of the Singleton Pattern?','MCSA',2);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (5,'Put these sentences in order','SORT',2);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (6,'Which of the following is not a typical use of the singleton?','MCSA',2);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (7,'Which of the following is not an advantage of the factory pattern?','MCSA',3);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (8,'Which of the following is an advantage of the factory pattern?','MCSA',3);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (9,'Which design pattern exhibits a private constructor?','MCSA',2);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (10,'What design pattern would be appropriate to use to implement a logger?','MCSA',2);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (11,'What design pattern would be appropriate to use to implement a configuration class?','MCSA',2);
INSERT INTO content_assessment_questions(Id,Question,Format,ItemId) VALUES (12,'What must classes exhibit to be able to be instantiated by a factory?','MCSA',3);

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

INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (1,1,'Analysis',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (2,1,'Planning',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (3,1,'Design',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (4,1,'Implementation',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (5,2,'Design',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (6,2,'Implementation',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (7,2,'Planning',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (8,2,'Analysis',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (9,3,'Plan',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (10,3,'Analyse',0,1);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (11,3,'Design',0,2);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (12,3,'Implement',0,3);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (13,4,'refers to the newly created object through a common interface',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (14,4,'Ensure that only one instance of a class is created',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (15,4,'Provide a global point of access to the object',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (16,5,'A',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (17,5,'Factory',0,1);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (18,5,'is often implemented as a',0,2);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (19,5,'Singleton',0,3);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (20,6,'Database connection',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (21,6,'Logger class',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (22,6,'Shared resource access',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (23,6,'Configuration class',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (24,7,'creates objects without exposing the instantiation logic to the client',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (25,7,'Defines an instance for creating an object but letting subclasses decide which class to instantiate',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (26,7,'refers to the newly created object through a common interface',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (27,8,'creates objects without exposing the instantiation logic to the client',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (28,8,'Defines an instance for creating an object but letting subclasses decide which class to instantiate',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (29,8,'specifying the kind of objects to create using a prototypical instance',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (30,8,'reuse and share objects that are expensive to create',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (31,9,'Singleton',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (32,9,'Builder',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (33,9,'Prototype',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (34,9,'Object Pool',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (35,10,'Singleton',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (36,10,'Factory',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (37,10,'Builder',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (38,10,'Prototype',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (39,11,'Singleton',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (40,11,'Abstract Factory',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (41,11,'Builder',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (42,11,'Object Pool',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (43,12,'Extend a common interface',1,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (44,12,'Have a private constructor',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (45,12,'Have a protected access modifier',0,0);
INSERT INTO content_assessment_answers(Id,QuestionId,Answer,Correct,Sequence) VALUES (46,12,'Be in separate inheritance hierachies',0,0);

CREATE TABLE content_scores (
  UserId int(11) NOT NULL,
  ContentItemId int(11) NOT NULL,
  Score int(3) DEFAULT NULL,
  ScoreDate date NOT NULL,
  Streak int(3) DEFAULT NULL,
  RepromptDate date DEFAULT NULL,
  PRIMARY KEY (UserId,ContentItemId,ScoreDate),
  KEY ContentId (ContentItemId),
  CONSTRAINT content_scores_ibfk_1 FOREIGN KEY (UserId) REFERENCES users (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_scores_ibfk_2 FOREIGN KEY (ContentItemId) REFERENCES content_items (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE content_disabled (
  ContentItemId int(11) NOT NULL,
  UserId int(11) NOT NULL,
  PRIMARY KEY (ContentItemId,UserId),
  KEY UserId (UserId),
  CONSTRAINT content_disabled_ibfk_1 FOREIGN KEY (ContentItemId) REFERENCES content_items (Id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT content_disabled_ibfk_2 FOREIGN KEY (UserId) REFERENCES users (Id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

# --- !Downs

DROP TABLE IF EXISTS content_disabled;
DROP TABLE IF EXISTS content_scores;
DROP TABLE IF EXISTS content_assessment_answers;
DROP TABLE IF EXISTS content_assessment_questions;
DROP TABLE IF EXISTS content_items;
DROP TABLE IF EXISTS content_assigned_packages;
DROP TABLE IF EXISTS content_packages;
DROP TABLE IF EXISTS content_folders;
DROP TABLE IF EXISTS content_assigned_cohorts;
DROP TABLE IF EXISTS content_assigned;
DROP TABLE IF EXISTS cohort_members;
DROP TABLE IF EXISTS cohorts;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS users;