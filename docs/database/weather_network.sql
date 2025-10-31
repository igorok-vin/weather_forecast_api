CREATE DATABASE  IF NOT EXISTS `weather_network` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `weather_network`;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: weather_network
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `apps_locations`
--

DROP TABLE IF EXISTS `apps_locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `apps_locations` (
  `location_code` varchar(10) DEFAULT NULL,
  `app_id` int NOT NULL,
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `UK65th2eqk6xjbpp7gyi4k5ttrq` (`location_code`),
  CONSTRAINT `FK8x41tpkjewxpjlwxfymsjb42i` FOREIGN KEY (`location_code`) REFERENCES `locations` (`code`),
  CONSTRAINT `FKigr3lvve7oltlest6haiobjc6` FOREIGN KEY (`app_id`) REFERENCES `client_apps` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `apps_locations`
--

LOCK TABLES `apps_locations` WRITE;
/*!40000 ALTER TABLE `apps_locations` DISABLE KEYS */;
INSERT INTO `apps_locations` VALUES ('BFLNY_USA',2),('PARIS_FR',5);
/*!40000 ALTER TABLE `apps_locations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client_apps`
--

DROP TABLE IF EXISTS `client_apps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `client_apps` (
  `id` int NOT NULL AUTO_INCREMENT,
  `client_id` varchar(100) NOT NULL,
  `client_secret` varchar(100) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(50) NOT NULL,
  `role` enum('READER','SYSTEM','UPDATER') NOT NULL,
  `trashed` bit(1) NOT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrd1ekd9e9xo5mggau9dr5lx5` (`client_id`),
  UNIQUE KEY `UK10hf0k2pjfpx2ebqswx4pbqea` (`client_secret`),
  KEY `FKsp3ximjn6j3kghjme8hv9dsui` (`user_id`),
  CONSTRAINT `FKsp3ximjn6j3kghjme8hv9dsui` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client_apps`
--

LOCK TABLES `client_apps` WRITE;
/*!40000 ALTER TABLE `client_apps` DISABLE KEYS */;
INSERT INTO `client_apps` VALUES (1,'gr4TbWEFGFpfZvRrbeyB','$2a$10$yvOFIUF5MZm7U9fcptFu2.aja4xAz6J7GxbOb9JeiuB608UL3U2qK',_binary '','trashed-First App','READER',_binary '',1),(2,'JiaS6z4tKFBlIWNtqxWl','$2a$10$vY7RqaRAd/xBsSW8RuWuRue0qCP8G9W5wEW1ofsinitko9bkDqfq2',_binary '','trashed-three','UPDATER',_binary '',1),(3,'Aq5cWAWjANk8QVYDjmuY','$2a$10$mSWpbX6rbd/.9KbQraZYXewo8zArUb3e18eZ40qXfcgCBNCJcUabS',_binary '','trashed-test_app','READER',_binary '',1),(4,'hnLqPPQqGogt7GtgIJbh','$2a$10$ETCNkBRg.tMcUlONvWtNVeVHfdeJQ8gwzO2wtGl6fAp5.i4fRIJ1W',_binary '','System App','SYSTEM',_binary '\0',1),(5,'gukvoCJfYyC42YJHwUxn','$2a$10$xrEglObq83vE2imuIULCpO3AEOvgqB8GfpzYLMdjZZ0LR7khI1Uze',_binary '','Updater App','UPDATER',_binary '\0',1),(6,'oTwZzfgrhfxngenlKJSe','$2a$10$LU/abOXWOAcpSJqEoOxtt.kXMlTBld9nhYhk/s/FPAyWRPpEWKy.2',_binary '','Reader App','READER',_binary '\0',3);
/*!40000 ALTER TABLE `client_apps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locations`
--

DROP TABLE IF EXISTS `locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `locations` (
  `code` varchar(10) NOT NULL,
  `city_name` varchar(128) NOT NULL,
  `country_code` varchar(10) NOT NULL,
  `country_name` varchar(64) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `region_name` varchar(128) DEFAULT NULL,
  `trashed` bit(1) NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locations`
--

LOCK TABLES `locations` WRITE;
/*!40000 ALTER TABLE `locations` DISABLE KEYS */;
INSERT INTO `locations` VALUES ('BFLNY_USA','Buffalo','US','United States of America',_binary '','New York',_binary '\0'),('BNGL_IN','Bangalore','IN','India',_binary '','Karnataka',_binary '\0'),('DELHI_IN','Delhi','IN','India',_binary '','Delhi',_binary '\0'),('KIEV_UA','KIEV','UA','Ukraine',_binary '','KIEV',_binary '\0'),('LACA_USA','Los Angeles','US','United States of America',_binary '','California',_binary '\0'),('MADRID_ES','Madrid','ES','Spain',_binary '','Community of Madrid',_binary '\0'),('NYC_USA','New York City','US','United States of America',_binary '','New York',_binary '\0'),('PARIS_FR','Paris','FR','France',_binary '','Paris',_binary '\0');
/*!40000 ALTER TABLE `locations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `realtime_weather`
--

DROP TABLE IF EXISTS `realtime_weather`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `realtime_weather` (
  `location_code` varchar(10) NOT NULL,
  `humidity` int NOT NULL,
  `last_updated` varchar(255) DEFAULT NULL,
  `precipitation` int NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `temperature` int NOT NULL,
  `wind_speed` int NOT NULL,
  PRIMARY KEY (`location_code`),
  CONSTRAINT `FKgvl48yx0pq95h8xw589p0mxui` FOREIGN KEY (`location_code`) REFERENCES `locations` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `realtime_weather`
--

LOCK TABLES `realtime_weather` WRITE;
/*!40000 ALTER TABLE `realtime_weather` DISABLE KEYS */;
INSERT INTO `realtime_weather` VALUES ('BNGL_IN',80,'2025-10-17 21:47',90,'Rain',40,35),('DELHI_IN',65,'2025-07-17 00:37',90,'Raining',35,35),('KIEV_UA',65,'2025-07-17 00:35',90,'Raining',35,35),('LACA_USA',55,'2025-10-23 23:05',15,'Cloudy',37,25),('NYC_USA',64,'2025-07-04 20:55',60,'Cloudy',-15,15),('PARIS_FR',28,'2025-07-01 23:18',23,'Sunny',22,2);
/*!40000 ALTER TABLE `realtime_weather` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(50) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(70) NOT NULL,
  `trashed` bit(1) NOT NULL,
  `type` enum('ADMIN','CLIENT') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'igorok.vin@gmail.com',_binary '','Igor Nikolaienko','$2a$12$YlsnwfBtalUAJhWcast4kuGExBRIRpg3eTlC9ngEgXoV/RUdooyka',_binary '\0','CLIENT'),(2,'igor@gmail.com',_binary '','Igor','$2a$12$YlsnwfBtalUAJhWcast4kuGExBRIRpg3eTlC9ngEgXoV/RUdooyka',_binary '\0','ADMIN'),(3,'mark@gmail.com',_binary '','Mark','$2a$12$YlsnwfBtalUAJhWcast4kuGExBRIRpg3eTlC9ngEgXoV/RUdooyka',_binary '\0','ADMIN');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weather_daily`
--

DROP TABLE IF EXISTS `weather_daily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weather_daily` (
  `location_code` varchar(10) NOT NULL,
  `day_of_month` int NOT NULL,
  `month` int NOT NULL,
  `max_temperature` int NOT NULL,
  `min_temperature` int NOT NULL,
  `precipitation` int NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`location_code`,`day_of_month`,`month`),
  CONSTRAINT `FKdb65slqm144d0ol3mhl1wglwh` FOREIGN KEY (`location_code`) REFERENCES `locations` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weather_daily`
--

LOCK TABLES `weather_daily` WRITE;
/*!40000 ALTER TABLE `weather_daily` DISABLE KEYS */;
INSERT INTO `weather_daily` VALUES ('BNGL_IN',15,7,26,24,60,'Cloudy'),('BNGL_IN',16,7,30,28,40,'Sunny'),('BNGL_IN',17,7,25,22,85,'Rainy'),('DELHI_IN',15,7,26,24,60,'Cloudy'),('DELHI_IN',16,7,30,28,40,'Sunny'),('DELHI_IN',17,7,25,22,90,'Rainy'),('KIEV_UA',15,7,26,24,60,'Cloudy'),('KIEV_UA',16,7,30,28,40,'Sunny'),('KIEV_UA',17,7,25,22,90,'Rainy'),('MADRID_ES',8,4,27,25,30,'Sunny'),('PARIS_FR',5,8,23,20,45,'Cloudy');
/*!40000 ALTER TABLE `weather_daily` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weather_hourly`
--

DROP TABLE IF EXISTS `weather_hourly`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weather_hourly` (
  `location_code` varchar(10) NOT NULL,
  `hour_of_day` int NOT NULL,
  `temperature` int NOT NULL,
  `precipitation` int NOT NULL,
  `status` varchar(50) NOT NULL,
  PRIMARY KEY (`location_code`,`hour_of_day`),
  CONSTRAINT `FKb9fne1kfqb2pp4ahjft9an5q9` FOREIGN KEY (`location_code`) REFERENCES `locations` (`code`),
  CONSTRAINT `weather_hourly_chk_1` CHECK (((`precipitation` >= 0) and (`precipitation` <= 100))),
  CONSTRAINT `weather_hourly_chk_2` CHECK (((`temperature` >= -(50)) and (`temperature` <= 50)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weather_hourly`
--

LOCK TABLES `weather_hourly` WRITE;
/*!40000 ALTER TABLE `weather_hourly` DISABLE KEYS */;
INSERT INTO `weather_hourly` VALUES ('BNGL_IN',16,25,95,'Rain'),('BNGL_IN',17,29,75,'Cloudy'),('BNGL_IN',18,32,42,'Sunny'),('DELHI_IN',16,35,35,'Sunny'),('DELHI_IN',17,27,55,'Sunny'),('KIEV_UA',16,24,95,'Rain'),('KIEV_UA',17,27,75,'Cloudy'),('KIEV_UA',18,32,42,'Sunny'),('NYC_USA',5,12,88,'Cloudy'),('NYC_USA',6,13,86,'Cloudy'),('NYC_USA',7,15,25,'Sunny'),('PARIS_FR',6,15,25,'Cloudy'),('PARIS_FR',7,23,33,'Sunny'),('PARIS_FR',8,26,65,'Rain');
/*!40000 ALTER TABLE `weather_hourly` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-30 18:44:26
