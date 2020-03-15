-- MySQL dump 10.13  Distrib 5.7.18, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: reprompt
-- ------------------------------------------------------
-- Server version	5.7.18-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `play_evolutions`
--

DROP TABLE IF EXISTS `play_evolutions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `apply_script` mediumtext COLLATE utf8_unicode_ci,
  `revert_script` mediumtext COLLATE utf8_unicode_ci,
  `state` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `last_problem` mediumtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `play_evolutions`
--

LOCK TABLES `play_evolutions` WRITE;
/*!40000 ALTER TABLE `play_evolutions` DISABLE KEYS */;
INSERT INTO `play_evolutions` VALUES (1,'8a985c2108266c8c168d75d5cc2d76f4ebeb96e8','2017-07-12 08:01:10','CREATE TABLE Users (\nId int(11) NOT NULL AUTO_INCREMENT,\nFirstName varchar(255) COLLATE utf8_unicode_ci NOT NULL,\nsurname varchar(255) COLLATE utf8_unicode_ci NOT NULL,\nEmail varchar(255) COLLATE utf8_unicode_ci NOT NULL,\nIsEmailVerified tinyint(1) NOT NULL DEFAULT \'0\',\nIsEducator tinyint(1) NOT NULL DEFAULT \'0\',\nIsAdministrator tinyint(1) NOT NULL DEFAULT \'0\',\nAvatarUrl varchar(255) DEFAULT NULL,\nPRIMARY KEY (Id),\nUNIQUE KEY EMAIL (Email)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;\n\nINSERT INTO Users (Id, FirstName, surname, Email, IsEmailVerified, IsEducator, IsAdministrator)\nVALUES (1, \'Admin\', \'User\', \'Undefined\', \'1\', \'1\', \'1\');\n\nCREATE TABLE Profiles(\nUserId INT NOT NULL,\nProviderId VARCHAR(255) NOT NULL,\nProviderKey VARCHAR(255) NOT NULL,\nConfirmed tinyint(1) NOT NULL DEFAULT \'0\',\nEmail VARCHAR(255),\nFirstName VARCHAR(255),\nLastName VARCHAR(255),\nFullName VARCHAR(255),\nPasswordInfo JSON,\nOAuth1Info JSON,\nOAuth2Info JSON,\nAvatarUrl VARCHAR(255),\nPRIMARY KEY (UserId, ProviderId, ProviderKey),\nFOREIGN KEY (UserId) REFERENCES Users(Id) ON UPDATE CASCADE ON DELETE CASCADE\n) ENGINE=INNODB CHARSET=utf8 COLLATE=utf8_unicode_ci;\n\nINSERT INTO Profiles (UserId, ProviderId, ProviderKey, Confirmed, FirstName)\nVALUES (1, \'credentials\', \'admin\', 1 , \'Admin\');','DROP TABLE Users;\nDROP TABLE Profiles;','applied','');
/*!40000 ALTER TABLE `play_evolutions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profiles`
--

DROP TABLE IF EXISTS `profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profiles` (
  `UserId` int(11) NOT NULL,
  `ProviderId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `ProviderKey` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `Confirmed` tinyint(1) NOT NULL DEFAULT '0',
  `Email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FirstName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LastName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FullName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PasswordInfo` json DEFAULT NULL,
  `OAuth1Info` json DEFAULT NULL,
  `OAuth2Info` json DEFAULT NULL,
  `AvatarUrl` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`UserId`,`ProviderId`,`ProviderKey`),
  CONSTRAINT `profiles_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `users` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profiles`
--

LOCK TABLES `profiles` WRITE;
/*!40000 ALTER TABLE `profiles` DISABLE KEYS */;
INSERT INTO `profiles` VALUES (1,'credentials','admin',1,NULL,'Admin',NULL,NULL,NULL,NULL,NULL,NULL),(62,'credentials','kesomir@btinternet.com',0,'kesomir@btinternet.com','Alexander','Worton','Alexander Worton','{\"hasher\": \"bcrypt\", \"password\": \"$2a$10$ljjixwrUgH1wWA/ISfBsteRDsL1kUYRVIEemA62G4QUMXPTKGhrfe\"}','null','null','https://secure.gravatar.com/avatar/67e515b51ce3007d07e651b26dff5cdb?d=404'),(63,'credentials','t-worton@ilford-school.co.uk',0,'t-worton@ilford-school.co.uk','Terry','Worton','Terry Worton','{\"hasher\": \"bcrypt\", \"password\": \"$2a$10$LvvTckF8I4VglAgfGJUakOFdVJVyLjYZltygjbw4su5lbGZpfi7iu\"}','null','null',NULL);
/*!40000 ALTER TABLE `profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `FirstName` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `surname` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `Email` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `IsEmailVerified` tinyint(1) NOT NULL DEFAULT '0',
  `IsEducator` tinyint(1) NOT NULL DEFAULT '0',
  `IsAdministrator` tinyint(1) NOT NULL DEFAULT '0',
  `AvatarUrl` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `EMAIL` (`Email`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Admin','User','Undefined',1,1,1,NULL),(62,'Alexander','Worton','kesomir@btinternet.com',0,0,0,'https://secure.gravatar.com/avatar/67e515b51ce3007d07e651b26dff5cdb?d=404'),(63,'Terry','Worton','t-worton@ilford-school.co.uk',0,0,0,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-07-13 16:48:25
