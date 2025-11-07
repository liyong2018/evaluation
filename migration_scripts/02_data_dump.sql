-- MySQL dump 10.13  Distrib 8.4.5, for Linux (x86_64)
--
-- Host: localhost    Database: evaluate_db
-- ------------------------------------------------------
-- Server version	8.4.5

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `survey_data`
--

LOCK TABLES `survey_data` WRITE;
/*!40000 ALTER TABLE `survey_data` DISABLE KEYS */;
INSERT INTO `survey_data` (`id`, `region_code`, `province`, `city`, `county`, `township`, `year`, `population`, `management_staff`, `risk_assessment`, `funding_amount`, `material_value`, `hospital_beds`, `firefighters`, `volunteers`, `militia_reserve`, `training_participants`, `shelter_capacity`, `create_time`, `update_time`, `is_deleted`) VALUES (31,'511425001','四川省','眉山市','青神县','青竹街道',2025,2,102379,'是',20.00,9.00,1010,26,1126,182,280,500,'2025-11-06 13:50:36','2025-11-06 13:50:36',0);
INSERT INTO `survey_data` (`id`, `region_code`, `province`, `city`, `county`, `township`, `year`, `population`, `management_staff`, `risk_assessment`, `funding_amount`, `material_value`, `hospital_beds`, `firefighters`, `volunteers`, `militia_reserve`, `training_participants`, `shelter_capacity`, `create_time`, `update_time`, `is_deleted`) VALUES (32,'511425102','四川省','眉山市','青神县','汉阳镇',2025,2,6335,'是',70.00,3.00,22,5,322,7,900,1200,'2025-11-06 13:50:36','2025-11-06 13:50:36',0);
INSERT INTO `survey_data` (`id`, `region_code`, `province`, `city`, `county`, `township`, `year`, `population`, `management_staff`, `risk_assessment`, `funding_amount`, `material_value`, `hospital_beds`, `firefighters`, `volunteers`, `militia_reserve`, `training_participants`, `shelter_capacity`, `create_time`, `update_time`, `is_deleted`) VALUES (33,'511425108','四川省','眉山市','青神县','瑞峰镇',2025,52,8227,'是',63.00,20.00,36,0,373,24,1658,780,'2025-11-06 13:50:36','2025-11-06 13:50:36',0);
INSERT INTO `survey_data` (`id`, `region_code`, `province`, `city`, `county`, `township`, `year`, `population`, `management_staff`, `risk_assessment`, `funding_amount`, `material_value`, `hospital_beds`, `firefighters`, `volunteers`, `militia_reserve`, `training_participants`, `shelter_capacity`, `create_time`, `update_time`, `is_deleted`) VALUES (34,'511425110','四川省','眉山市','青神县','西龙镇',2025,2,14051,'是',20.00,7.00,22,0,81,55,320,500,'2025-11-06 13:50:36','2025-11-06 13:50:36',0);
INSERT INTO `survey_data` (`id`, `region_code`, `province`, `city`, `county`, `township`, `year`, `population`, `management_staff`, `risk_assessment`, `funding_amount`, `material_value`, `hospital_beds`, `firefighters`, `volunteers`, `militia_reserve`, `training_participants`, `shelter_capacity`, `create_time`, `update_time`, `is_deleted`) VALUES (35,'511425112','四川省','眉山市','青神县','高台镇',2025,4,13786,'是',93.00,2.00,28,0,702,348,672,1500,'2025-11-06 13:50:36','2025-11-06 13:50:36',0);
INSERT INTO `survey_data` (`id`, `region_code`, `province`, `city`, `county`, `township`, `year`, `population`, `management_staff`, `risk_assessment`, `funding_amount`, `material_value`, `hospital_beds`, `firefighters`, `volunteers`, `militia_reserve`, `training_participants`, `shelter_capacity`, `create_time`, `update_time`, `is_deleted`) VALUES (36,'511425217','四川省','眉山市','青神县','白果乡',2025,2,13523,'是',20.00,8.00,34,0,2,65,320,1000,'2025-11-06 13:50:36','2025-11-06 13:50:36',0);
INSERT INTO `survey_data` (`id`, `region_code`, `province`, `city`, `county`, `township`, `year`, `population`, `management_staff`, `risk_assessment`, `funding_amount`, `material_value`, `hospital_beds`, `firefighters`, `volunteers`, `militia_reserve`, `training_participants`, `shelter_capacity`, `create_time`, `update_time`, `is_deleted`) VALUES (37,'511425218','四川省','眉山市','青神县','罗波乡',2025,12,9689,'是',150.00,10.00,30,0,94,106,300,5000,'2025-11-06 13:50:36','2025-11-06 13:50:36',0);
/*!40000 ALTER TABLE `survey_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `weight_config`
--

LOCK TABLES `weight_config` WRITE;
/*!40000 ALTER TABLE `weight_config` DISABLE KEYS */;
INSERT INTO `weight_config` (`id`, `config_name`, `description`, `orgcode`, `data_source`, `create_time`, `update_time`, `is_deleted`) VALUES (1,'乡镇街道权重配置','乡镇街道减灾能力评估指标权重配置','511425','township','2025-09-23 08:38:54','2025-10-21 13:34:39',0);
INSERT INTO `weight_config` (`id`, `config_name`, `description`, `orgcode`, `data_source`, `create_time`, `update_time`, `is_deleted`) VALUES (2,'社区-乡镇单元权重配置','社区-乡镇单元减灾能力评估用权重配置','511425','community','2025-10-16 02:19:26','2025-10-21 13:35:49',0);
INSERT INTO `weight_config` (`id`, `config_name`, `description`, `orgcode`, `data_source`, `create_time`, `update_time`, `is_deleted`) VALUES (3,'社区-社区单元权重配置','社区行政村-社区单元减灾能力评估权重配置','511425','community','2025-10-16 06:59:28','2025-10-21 13:36:18',0);
INSERT INTO `weight_config` (`id`, `config_name`, `description`, `orgcode`, `data_source`, `create_time`, `update_time`, `is_deleted`) VALUES (4,'综合模型权重配置','乡镇层面权重0.53，社区层面权重0.47','511425','township','2025-10-19 03:43:56','2025-10-20 13:22:45',0);
INSERT INTO `weight_config` (`id`, `config_name`, `description`, `orgcode`, `data_source`, `create_time`, `update_time`, `is_deleted`) VALUES (9,'乡镇街道权重配置_副本_1762418865079','乡镇街道减灾能力评估指标权重配置(复制)',NULL,'township','2025-11-06 16:47:45','2025-11-06 16:47:49',1);
/*!40000 ALTER TABLE `weight_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `indicator_weight`
--

LOCK TABLES `indicator_weight` WRITE;
/*!40000 ALTER TABLE `indicator_weight` DISABLE KEYS */;
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (1,1,'L1_MANAGEMENT','灾害管理能力',1,0.3300,NULL,1,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (2,1,'L1_PREPARATION','灾害备灾能力',1,0.3200,NULL,2,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (3,1,'L1_SELF_RESCUE','自救转移能力',1,0.3500,NULL,3,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (4,1,'L2_MANAGEMENT_CAPABILITY','队伍管理能力',2,0.3700,1,1,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (5,1,'L2_RISK_ASSESSMENT','风险评估能力',2,0.3100,1,2,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (6,1,'L2_FUNDING','财政投入能力',2,0.3200,1,3,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (7,1,'L2_MATERIAL','物资储备能力',2,0.5100,2,1,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (8,1,'L2_MEDICAL','医疗保障能力',2,0.4900,2,2,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (9,1,'L2_SELF_RESCUE','自救互救能力',2,0.3300,3,1,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (10,1,'L2_PUBLIC_AVOIDANCE','公众避险能力',2,0.3300,3,2,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (11,1,'L2_RELOCATION','转移安置能力',2,0.3400,3,3,'2025-09-23 08:38:54');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (12,2,'L1_MANAGEMENT','灾害管理能力',1,0.3200,NULL,1,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (13,2,'L1_PREPARATION','灾害备灾能力',1,0.3100,NULL,2,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (14,2,'L1_SELF_RESCUE','自救转移能力',1,0.3700,NULL,3,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (15,2,'L2_MANAGEMENT_CAPABILITY','预案建设能力',2,0.2500,12,1,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (16,2,'L2_RISK_ASSESSMENT','风险评估能力',2,0.2300,12,3,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (17,2,'L2_FUNDING','财政投入能力',2,0.2300,12,4,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (18,2,'L2_MATERIAL','物资储备能力',2,0.5200,13,1,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (19,2,'L2_MEDICAL','医疗保障能力',2,0.4800,13,2,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (20,2,'L2_SELF_RESCUE','自救互救能力',2,0.3300,14,1,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (21,2,'L2_PUBLIC_AVOIDANCE','公众避险能力',2,0.3400,14,2,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (22,2,'L2_RELOCATION','转移安置能力',2,0.3300,14,3,'2025-10-16 02:19:26');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (23,2,'L2_HIDDEN_INSPECTION','隐患排查能力',2,0.2900,12,2,'2025-10-16 02:24:23');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (27,3,'L1_MANAGEMENT','灾害管理能力',1,0.3200,NULL,1,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (28,3,'L1_PREPARATION','灾害备灾能力',1,0.3100,NULL,2,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (29,3,'L1_SELF_RESCUE','自救转移能力',1,0.3700,NULL,3,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (30,3,'L2_MANAGEMENT_CAPABILITY','预案建设能力',2,0.2500,27,1,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (31,3,'L2_HIDDEN_INSPECTION','隐患排查能力',2,0.2900,27,2,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (32,3,'L2_RISK_ASSESSMENT','风险评估能力',2,0.2300,27,3,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (33,3,'L2_FUNDING','财政投入能力',2,0.2300,27,4,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (34,3,'L2_MATERIAL','物资储备能力',2,0.5200,28,1,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (35,3,'L2_MEDICAL','医疗保障能力',2,0.4800,28,2,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (36,3,'L2_SELF_RESCUE','自救互救能力',2,0.3300,29,1,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (37,3,'L2_PUBLIC_AVOIDANCE','公众避险能力',2,0.3400,29,2,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (38,3,'L2_RELOCATION','转移安置能力',2,0.3300,29,3,'2025-10-16 09:48:53');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (39,4,'TOWNSHIP_LEVEL','乡镇层面',1,0.5300,NULL,1,'2025-10-19 03:44:38');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (40,4,'COMMUNITY_LEVEL','社区层面',1,0.4700,NULL,2,'2025-10-19 03:44:38');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (41,4,'TOWNSHIP_DISASTER_MGMT','灾害管理能力',2,0.3300,39,1,'2025-10-19 03:45:02');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (42,4,'TOWNSHIP_DISASTER_PREP','灾害备灾能力',2,0.3200,39,2,'2025-10-19 03:45:02');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (43,4,'TOWNSHIP_SELF_RESCUE','自救转移能力',2,0.3500,39,3,'2025-10-19 03:45:02');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (44,4,'COMMUNITY_DISASTER_MGMT','灾害管理能力',2,0.3200,40,4,'2025-10-19 03:45:02');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (45,4,'COMMUNITY_DISASTER_PREP','灾害备灾能力',2,0.3100,40,5,'2025-10-19 03:45:02');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (46,4,'COMMUNITY_SELF_RESCUE','自救转移能力',2,0.3700,40,6,'2025-10-19 03:45:02');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (98,9,'L1_MANAGEMENT','灾害管理能力',1,0.3300,NULL,1,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (99,9,'L1_PREPARATION','灾害备灾能力',1,0.3200,NULL,2,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (100,9,'L1_SELF_RESCUE','自救转移能力',1,0.3500,NULL,3,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (101,9,'L2_MANAGEMENT_CAPABILITY','队伍管理能力',2,0.3700,1,1,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (102,9,'L2_MATERIAL','物资储备能力',2,0.5100,2,1,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (103,9,'L2_SELF_RESCUE','自救互救能力',2,0.3300,3,1,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (104,9,'L2_RISK_ASSESSMENT','风险评估能力',2,0.3100,1,2,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (105,9,'L2_MEDICAL','医疗保障能力',2,0.4900,2,2,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (106,9,'L2_PUBLIC_AVOIDANCE','公众避险能力',2,0.3300,3,2,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (107,9,'L2_FUNDING','财政投入能力',2,0.3200,1,3,'2025-11-06 16:47:45');
INSERT INTO `indicator_weight` (`id`, `config_id`, `indicator_code`, `indicator_name`, `indicator_level`, `weight`, `parent_id`, `sort_order`, `create_time`) VALUES (108,9,'L2_RELOCATION','转移安置能力',2,0.3400,3,3,'2025-11-06 16:47:45');
/*!40000 ALTER TABLE `indicator_weight` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `evaluation_model`
--

LOCK TABLES `evaluation_model` WRITE;
/*!40000 ALTER TABLE `evaluation_model` DISABLE KEYS */;
INSERT INTO `evaluation_model` (`id`, `model_name`, `model_code`, `description`, `version`, `status`, `is_default`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (3,'乡镇减灾能力评估模型','STANDARD_MODEL','基于TOPSIS算法的标准减灾能力评估模型','1.0',1,1,'2025-10-12 09:43:09','2025-10-20 11:20:25',NULL,NULL);
INSERT INTO `evaluation_model` (`id`, `model_name`, `model_code`, `description`, `version`, `status`, `is_default`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (4,'社区-行政村能力评估模型','COMMUNITY_CAPABILITY_MODEL','基于TOPSIS与分级的社区能力评估模型','1.0',1,0,'2025-10-16 02:10:43','2025-10-20 11:21:09',NULL,NULL);
INSERT INTO `evaluation_model` (`id`, `model_name`, `model_code`, `description`, `version`, `status`, `is_default`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (8,'社区-乡镇能力评估模型','COMMUNITY_TOWNSHIP_AGGREGATION_MODEL','先对社区数据进行指标计算，然后按乡镇聚合求平均，再进行归一化和后续评估','1.0',1,0,'2025-10-16 10:56:42','2025-10-16 10:56:42',NULL,NULL);
INSERT INTO `evaluation_model` (`id`, `model_name`, `model_code`, `description`, `version`, `status`, `is_default`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (11,'综合减灾能力评估模型','COMPREHENSIVE_EVALUATION_MODEL','整合标准模型和社区-乡镇模型的结果，通过新的权重体系计算综合评估结果','1.0',1,0,'2025-10-19 03:42:27','2025-10-19 03:42:27',NULL,NULL);
/*!40000 ALTER TABLE `evaluation_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `algorithm_config`
--

LOCK TABLES `algorithm_config` WRITE;
/*!40000 ALTER TABLE `algorithm_config` DISABLE KEYS */;
INSERT INTO `algorithm_config` (`id`, `config_name`, `description`, `version`, `status`, `create_time`) VALUES (1,'默认减灾能力评估算法','标准的减灾能力评估算法流程配置','1.0',1,'2025-09-23 08:38:54');
INSERT INTO `algorithm_config` (`id`, `config_name`, `description`, `version`, `status`, `create_time`) VALUES (4,'Community Village Assessment Algorithm','Community village disaster reduction capability assessment algorithm based on 9 indicators','2.0',1,'2025-10-21 06:43:51');
INSERT INTO `algorithm_config` (`id`, `config_name`, `description`, `version`, `status`, `create_time`) VALUES (8,'Community Township Assessment Algorithm','Community township disaster reduction capability assessment algorithm based on 9 indicators','2.0',1,'2025-10-21 06:43:51');
/*!40000 ALTER TABLE `algorithm_config` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-07  1:22:49
