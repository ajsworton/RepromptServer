# --- !Ups

CREATE TABLE users (
  id SERIAL,
  first_name varchar(255) NOT NULL,
  surname varchar(255) NOT NULL,
  email varchar(255) UNIQUE NOT NULL,
  is_email_verified BOOLEAN NOT NULL DEFAULT 'false',
  is_educator BOOLEAN NOT NULL DEFAULT 'false',
  is_administrator BOOLEAN NOT NULL DEFAULT 'false',
  avatar_url varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Admin', 'User', 'Undefined', '1', '1', '1');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Professor', 'Teacher', 't@e', '1', '1', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Professor2', 'Teacher2', 't@e2', '1', '1', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Test', 'Student', 't@s', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Jacob', 'Benson', 'Jacob@Benson', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Chelsea', 'Reynolds', 'Chelsea@Reynolds', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Eve', 'Hodgson', 'Eve@Hodgson', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Christopher', 'Hammond', 'Christopher@Hammond', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Freya', 'Williamson', 'Freya@Williamson', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Tyler', 'Randall', 'Tyler@Randall', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Lucas', 'Fox', 'Lucas@Fox', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Daniel', 'Patterson', 'Daniel@Patterson', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Zak', 'Graham', 'Zak@Graham', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Hollie', 'Ross', 'Hollie@Ross', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Alexander', 'Power', 'Alexander@Power', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Rachel', 'Morley', 'Rachel@Morley', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Elise', 'Gibbs', 'Elise@Gibbs', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Toby', 'King', 'Toby@King', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Zachary', 'Finch', 'Zachary@Finch', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Bradley', 'Warren', 'Bradley@Warren', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Reece', 'Schofield', 'Reece@Schofield', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Abbie', 'Reed', 'Abbie@Reed', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Finley', 'Nixon', 'Finley@Nixon', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Shannon', 'Collins', 'Shannon@Collins', '1', '0', '0');
INSERT INTO users (first_name, surname, email, is_email_verified, is_educator, is_administrator)VALUES ( 'Lydia', 'Riley', 'Lydia@Riley', '1', '0', '0');

CREATE TABLE profiles(
  user_id INT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
  provider_id VARCHAR(255) NOT NULL,
  provider_key VARCHAR(255) NOT NULL,
  confirmed BOOLEAN NOT NULL DEFAULT 'false',
  email VARCHAR(255),
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  full_name VARCHAR(255),
  password_info TEXT,
  oauth1_info TEXT,
  oauth2_info TEXT,
  avatar_url VARCHAR(255),
  PRIMARY KEY (user_id, provider_id, provider_key)
);

INSERT INTO profiles (user_id, provider_id, provider_key, confirmed, first_name) VALUES (1, 'credentials', 'admin', 'true' , 'Admin');
INSERT INTO profiles (user_id, provider_id, provider_key, confirmed, password_info) VALUES (2, 'credentials', 't@e', 'true' , '{"hasher": "bcrypt", "password": "$2a$10$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}');
INSERT INTO profiles (user_id, provider_id, provider_key, confirmed, password_info) VALUES (3, 'credentials', 't@e2', 'true' , '{"hasher": "bcrypt", "password": "$2a$10$SShDo45/naMH3kUAFvZQjuiQ27RgqBYJiBwQiyj2aQGg0CF3.eCFi"}');
INSERT INTO profiles (user_id, provider_id, provider_key, confirmed, password_info) VALUES (4, 'credentials', 't@s', 'true' , '{"hasher": "bcrypt", "password": "$2a$10$1sKWPcIQo8anKCb1XZ4m/.fLWRj6b1XJP6L47LIJNhtvyv13rpEpS"}');

CREATE TABLE cohorts (
  id SERIAL,
  owner_id integer DEFAULT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
  name varchar(255) DEFAULT NULL,
  parent_id integer DEFAULT NULL REFERENCES cohorts(id) ON UPDATE CASCADE ON DELETE CASCADE,
  PRIMARY KEY (id)
);

INSERT INTO cohorts (owner_id, name) VALUES (2, '2017 MSc Computer Science FT');
INSERT INTO cohorts (owner_id, name) VALUES (3, '2017 MSc CS FT');

CREATE TABLE cohort_members (
  cohort_id integer NOT NULL REFERENCES cohorts(id) ON UPDATE CASCADE ON DELETE CASCADE,
  user_id integer NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
  PRIMARY KEY (cohort_id, user_id)
);

INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 4);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 5);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 6);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 7);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 8);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 9);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 10);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 11);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 12);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 20);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 21);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 22);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 23);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 24);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (1, 25);

INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 4);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 5);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 6);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 7);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 8);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 9);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 10);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 11);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 12);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 13);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 14);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 15);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 16);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 17);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 18);
INSERT INTO cohort_members (cohort_id, user_id) VALUES (2, 19);


CREATE TABLE content_assigned (
  id SERIAL,
  name varchar(255) NOT NULL,
  exam_date date DEFAULT NULL,
  active BOOLEAN NOT NULL DEFAULT 'true',
  owner_id integer DEFAULT NULL REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_content_assigned_ownerid ON content_assigned(owner_id);

INSERT INTO content_assigned (name, exam_date, active, owner_id)
VALUES('2017 MSc SDP','2017-10-01', 'true', 2);
INSERT INTO content_assigned (name, exam_date, active, owner_id)
VALUES('2017 MSc IS','2017-12-25', 'true', 3);

CREATE TABLE content_assigned_cohorts (
  assigned_id integer NOT NULL REFERENCES content_assigned (id) ON DELETE CASCADE ON UPDATE CASCADE,
  cohort_id integer NOT NULL REFERENCES cohorts (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (assigned_id,cohort_id)
);

CREATE INDEX idx_content_assigned_cohorts_cohortid ON content_assigned_cohorts(cohort_id);

INSERT INTO content_assigned_cohorts (assigned_id, cohort_id)
VALUES(1, 1);
INSERT INTO content_assigned_cohorts (assigned_id, cohort_id)
VALUES(2, 1);

CREATE TABLE content_folders (
  id SERIAL,
  owner_id integer DEFAULT NULL REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  name varchar(255) DEFAULT NULL,
  parent_id integer DEFAULT NULL REFERENCES content_folders (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_content_folders_ownerid ON content_folders(owner_id);
CREATE INDEX idx_content_folders_parentid ON content_folders(parent_id);

INSERT INTO content_folders (owner_id, name, parent_id) VALUES(3,'CS',NULL);
INSERT INTO content_folders (owner_id, name, parent_id) VALUES(2,'Computer Science',NULL);
INSERT INTO content_folders (owner_id, name, parent_id) VALUES(2,'SDP',2);

CREATE TABLE content_packages (
  id SERIAL,
  name varchar(255) DEFAULT NULL,
  folder_id integer DEFAULT NULL REFERENCES content_folders (id) ON DELETE CASCADE ON UPDATE CASCADE,
  owner_id integer DEFAULT NULL REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (id),
  UNIQUE (folder_id,owner_id,name)
);

CREATE INDEX idx_content_packages_ownerid ON content_packages(owner_id);

INSERT INTO content_packages (name, folder_id, owner_id)
VALUES('SDLC','1','3');
INSERT INTO content_packages (name, folder_id, owner_id)
VALUES('Design Patterns','3','2');

CREATE TABLE content_assigned_packages (
  assigned_id integer NOT NULL REFERENCES content_assigned (id) ON DELETE CASCADE ON UPDATE CASCADE,
  package_id integer NOT NULL REFERENCES content_packages (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (assigned_id,package_id)
);

CREATE INDEX idx_content_assigned_packages_packageid ON content_assigned_packages(package_id);

INSERT INTO content_assigned_packages (assigned_id, package_id)
VALUES(1, 2);
INSERT INTO content_assigned_packages (assigned_id, package_id)
VALUES(2, 1);

CREATE TABLE content_items (
  id SERIAL,
  package_id integer NOT NULL REFERENCES content_packages (id) ON DELETE CASCADE ON UPDATE CASCADE,
  image_url varchar(255) DEFAULT NULL,
  content text,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX idx_content_items_packageid ON content_items(package_id);

INSERT INTO content_items(package_id,image_url,content,name) VALUES (1,'media/3/content/1/items/1.png','<p>The Software Development Life Cycle outlines the stages involved in creating information systems.</p><p>A methodology is a formalised approach for implementing the SDLC.</p><p><strong>Planning </strong>- The planning phase is all about why the system should be built and consists of planning and project management.</p><p><strong>Analyse </strong>- The analysis phase is about determining <em>what </em>the system should do and <em>where </em>and <em>when </em>it will be used. It includes developing an analysis strategy, gathering requirements and developing a proposal.</p><p><strong>Design </strong>- The design phase is about how the system will be built. It includes developing a design strategy, developing the architecture, designing the database, file structure and programs.</p><p><strong>Implement </strong>- The implementation phase is all about building the system. It involves constructing, installing and supporting the system.</p>','SDLC Overview');
INSERT INTO content_items(package_id,image_url,content,name) VALUES (2,'media/2/content/2/items/2.gif','<p>Ensure that only one instance of a class is created and Provide a global access point to the object.</p><p>Singleton pattern should be used when we must ensure that only one instance of a class is created and when the instance must be available through all the code. A special care should be taken in multi-threading environments when multiple threads must access the same resources through the same singleton object.</p><p>There are many common situations when singleton pattern is used:</p><ul><li>Logger Classes</li><li>Configuration Classes</li><li>Accessing resources in shared mode</li><li>Other design patterns implemented as Singletons: Factories and Abstract Factories, Builder, Prototype</li></ul>','Singleton');
INSERT INTO content_items(package_id,image_url,content,name) VALUES (2,'media/2/content/2/items/3.gif','<p>Creates objects without exposing the instantiation logic to the client and Refers to the newly created object through a common interface.</p><p>Factory pattern should be used when: - a framework delegate the creation of objects derived from a common super class to the factory - we need flexibility in adding new types of objects that must be created by the class</p><p>Along with singleton pattern the factory is one of the most used patterns. Almost any application has some factories. Here are a some examples in java:</p><ul><li>factories providing an xml parser: javax.xml.parsers.DocumentBuilderFactory or javax.xml.parsers.SAXParserFactory</li><li>java.net.URLConnection - allows users to decide which protocol to use</span></li></ul>','Factory');

CREATE TABLE content_assessment_questions (
  id SERIAL,
  question text,
  Format varchar(255) DEFAULT NULL,
  item_id integer DEFAULT NULL REFERENCES content_items (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_content_assessment_questions_itemid ON content_assessment_questions(item_id);

INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Which phase of the SDLC involves developing the proposal?','MCSA',1);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Which phase of the SDLC involves installing the system?','MCSA',1);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Put the phases of the SDLC into the correct order','SORT',1);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Which of the following is not an advantage of the Singleton Pattern?','MCSA',2);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Put these sentences in order','SORT',2);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Which of the following is not a typical use of the singleton?','MCSA',2);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Which of the following is not an advantage of the factory pattern?','MCSA',3);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Which of the following is an advantage of the factory pattern?','MCSA',3);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('Which design pattern exhibits a private constructor?','MCSA',2);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('What design pattern would be appropriate to use to implement a logger?','MCSA',2);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('What design pattern would be appropriate to use to implement a configuration class?','MCSA',2);
INSERT INTO content_assessment_questions(question,Format,item_id) VALUES ('What must classes exhibit to be able to be instantiated by a factory?','MCSA',3);

CREATE TABLE content_assessment_answers (
  id SERIAL,
  question_id integer NOT NULL REFERENCES content_assessment_questions (id) ON DELETE CASCADE ON UPDATE CASCADE,
  answer text,
  correct BOOLEAN NOT NULL DEFAULT 'false',
  sequence integer DEFAULT '0',
  PRIMARY KEY (id)
);

CREATE INDEX idx_content_assessment_answers_questionid ON content_assessment_answers(question_id);

INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (1,'Analysis','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (1,'Planning','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (1,'Design','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (1,'Implementation','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (2,'Design','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (2,'Implementation','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (2,'Planning','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (2,'Analysis','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (3,'Plan','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (3,'Analyse','false',1);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (3,'Design','false',2);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (3,'Implement','false',3);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (4,'refers to the newly created object through a common interface','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (4,'Ensure that only one instance of a class is created','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (4,'Provide a global point of access to the object','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (5,'A','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (5,'Factory','false',1);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (5,'is often implemented as a','false',2);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (5,'Singleton','false',3);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (6,'Database connection','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (6,'Logger class','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (6,'Shared resource access','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (6,'Configuration class','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (7,'creates objects without exposing the instantiation logic to the client','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (7,'Defines an instance for creating an object but letting subclasses decide which class to instantiate','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (7,'refers to the newly created object through a common interface','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (8,'creates objects without exposing the instantiation logic to the client','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (8,'Defines an instance for creating an object but letting subclasses decide which class to instantiate','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (8,'specifying the kind of objects to create using a prototypical instance','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (8,'reuse and share objects that are expensive to create','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (9,'Singleton','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (9,'Builder','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (9,'Prototype','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (9,'Object Pool','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (10,'Singleton','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (10,'Factory','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (10,'Builder','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (10,'Prototype','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (11,'Singleton','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (11,'Abstract Factory','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (11,'Builder','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (11,'Object Pool','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (12,'Extend a common interface','true',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (12,'Have a private constructor','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (12,'Have a protected access modifier','false',0);
INSERT INTO content_assessment_answers(question_id,answer,correct,sequence) VALUES (12,'Be in separate inheritance hierachies','false',0);

CREATE TABLE content_scores (
  user_id integer NOT NULL REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  content_item_id integer NOT NULL REFERENCES content_items (id) ON DELETE CASCADE ON UPDATE CASCADE,
  score integer DEFAULT NULL,
  score_date date NOT NULL,
  streak integer DEFAULT NULL,
  reprompt_date date DEFAULT NULL,
  PRIMARY KEY (user_id,content_item_id,score_date)
);

CREATE INDEX idx_content_scores_contentitemid ON content_scores(content_item_id);

INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','1','44','2017-06-15','6','2017-06-16');
INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','1','79','2017-06-17','7','2017-06-17');
INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','1','100','2017-08-20','8','2017-09-14');
INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','2','92','2017-06-17','9','2017-06-17');
INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','2','80','2017-06-22','8','2017-06-16');
INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','3','100','2017-06-17','11','2017-06-17');
INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','3','95','2017-06-18','10','2017-07-30');
INSERT INTO content_scores (user_id, content_item_id, score, score_date, streak, reprompt_date) VALUES('4','3','100','2017-08-20','11','2017-08-28');

CREATE TABLE content_disabled (
  content_assigned_id integer NOT NULL REFERENCES content_assigned (id) ON DELETE CASCADE ON UPDATE CASCADE,
  user_id integer NOT NULL REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (content_assigned_id,user_id)
);

CREATE INDEX idx_content_disabled_userid ON content_disabled(user_id);

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
