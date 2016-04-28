DROP DATABASE IF EXISTS `db_techopark`;
CREATE DATABASE `db_techopark` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;
USE `db_techopark`;
-- MySQL dump 10.13  Distrib 5.7.9, for linux-glibc2.5 (x86_64)
--
-- Host: localhost    Database: db_techopark
-- ------------------------------------------------------
-- Server version 5.7.12

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
-- Table structure for table `forum`
--

DROP TABLE IF EXISTS `forum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forum` (
  `fID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` char(40) CHARACTER SET utf8 NOT NULL,
  `short_name` char(40) CHARACTER SET utf8 NOT NULL,
  `user` char(30) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`fID`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  UNIQUE KEY `short_name_UNIQUE` (`short_name`),
  KEY `fk_forum_user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `post` (
  `pID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `parent` int(11) DEFAULT NULL,
  `isApproved` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `isHighlighted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `isEdited` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `isSpam` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `isDeleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `date` datetime NOT NULL,
  `message` text CHARACTER SET utf8 NOT NULL,
  `user` char(30) CHARACTER SET utf8 NOT NULL,
  `forum` char(40) CHARACTER SET utf8 NOT NULL,
  `thread` int(11) unsigned NOT NULL,
  `likes` smallint(5) unsigned NOT NULL DEFAULT '0',
  `dislikes` smallint(5) unsigned NOT NULL DEFAULT '0',
  `points` smallint(6) NOT NULL DEFAULT '0',
  `mpath` char(200) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`pID`),
  KEY `user_date` (`user`,`date`),
  KEY `forum_date` (`forum`,`date`),
  KEY `thread_date` (`thread`,`date`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `thread`
--

DROP TABLE IF EXISTS `thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thread` (
  `tID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `isDeleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `forum` char(40) CHARACTER SET utf8 NOT NULL,
  `isClosed` tinyint(1) unsigned NOT NULL,
  `user` char(30) CHARACTER SET utf8 NOT NULL,
  `date` datetime NOT NULL,
  `message` text COLLATE utf8_unicode_ci NOT NULL,
  `slug` char(50) CHARACTER SET utf8 NOT NULL,
  `dislikes` smallint(5) unsigned DEFAULT '0',
  `likes` smallint(5) unsigned DEFAULT '0',
  `points` smallint(6) DEFAULT '0',
  `posts` smallint(5) unsigned DEFAULT '0',
  `title` char(50) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`tID`),
  KEY `user_date` (`user`,`date`),
  KEY `forum_date` (`forum`,`date`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `uID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `isAnonymous` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `username` char(30) CHARACTER SET utf8 DEFAULT NULL,
  `about` text CHARACTER SET utf8,
  `name` char(50) CHARACTER SET utf8 DEFAULT NULL,
  `email` char(30) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`email`),
  UNIQUE KEY `id` (`uID`),
  KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_thread`
--

DROP TABLE IF EXISTS `user_thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_thread` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user` char(30) NOT NULL,
  `tID` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_tID` (`user`,`tID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_user`
--

DROP TABLE IF EXISTS `user_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `follower` char(30) NOT NULL,
  `followee` char(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `follower_followee` (`follower`,`followee`),
  KEY `followee_follower` (`followee`,`follower`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'db_techopark'
--

--
-- Dumping routines for database 'db_techopark'
--
/*!50003 DROP PROCEDURE IF EXISTS `clear` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `clear`()
BEGIN
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user;
TRUNCATE TABLE forum;
TRUNCATE TABLE thread;
TRUNCATE TABLE post;
TRUNCATE TABLE user_thread;
TRUNCATE TABLE user_user;
SET FOREIGN_KEY_CHECKS = 1;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `insert_post` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_post`(_date DATETIME, threadID INT(11), message TEXT, user CHAR(30), forum CHAR(40), parent INT(11), isApproved TINYINT(1), isHighlighted TINYINT(1), isEdited TINYINT(1), isSpam TINYINT(1), isDeleted TINYINT(1))
BEGIN
DECLARE ID INT(8) ZEROFILL;
DECLARE MATPATH CHAR(200);

INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES (_date, threadID, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted);
SET ID = LAST_INSERT_ID();
SET MATPATH=IF(parent IS NULL, CAST(ID AS CHAR), CONCAT_WS('.', (SELECT mpath FROM post WHERE pID=parent), CAST(ID AS CHAR)));
UPDATE post SET mpath=MATPATH WHERE pID=ID;
CASE isDeleted
  WHEN 0 THEN
    UPDATE thread SET posts=posts+1 WHERE tID = threadID;
  ELSE BEGIN END;
END CASE;

SELECT ID;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `status` */;
ALTER DATABASE `db_techopark` CHARACTER SET utf8 COLLATE utf8_general_ci ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `status`()
BEGIN
DECLARE user INT;
DECLARE thread INT;
DECLARE forum INT;
DECLARE post INT;

SELECT COUNT(*) INTO user FROM user;
SELECT COUNT(*) INTO thread FROM thread;
SELECT COUNT(*) INTO forum FROM forum;
SELECT COUNT(*) INTO post FROM post;

SELECT user, thread, forum, post;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
ALTER DATABASE `db_techopark` CHARACTER SET utf8 COLLATE utf8_unicode_ci ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-04-28 14:52:48
