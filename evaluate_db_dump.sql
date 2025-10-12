-- MySQL dump 10.13  Distrib 9.3.0, for Linux (x86_64)
--
-- Host: localhost    Database: evaluate_db
-- ------------------------------------------------------
-- Server version	9.3.0

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
-- Current Database: `evaluate_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `evaluate_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `evaluate_db`;

--
-- Table structure for table `algorithm_config`
--

DROP TABLE IF EXISTS `algorithm_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '算法配置名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '算法配置描述',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '1.0' COMMENT '算法版本',
  `status` int DEFAULT '1' COMMENT '状态(1-启用,0-禁用)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `algorithm_config`
--

LOCK TABLES `algorithm_config` WRITE;
/*!40000 ALTER TABLE `algorithm_config` DISABLE KEYS */;
INSERT INTO `algorithm_config` VALUES (1,'默认减灾能力评估算法','标准的减灾能力评估算法流程配置','1.0',1,'2025-09-23 08:38:54');
/*!40000 ALTER TABLE `algorithm_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `algorithm_model`
--

DROP TABLE IF EXISTS `algorithm_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `algorithm_model` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `model_name` varchar(100) NOT NULL COMMENT '模型名称',
  `description` text COMMENT '模型描述',
  `version` varchar(20) DEFAULT '1.0' COMMENT '版本号',
  `status` int DEFAULT '1' COMMENT '状态(1-启用,0-禁用)',
  `formula_ids` text COMMENT '关联公式ID列表(逗号分隔)',
  `execution_order` text COMMENT '执行顺序配置(JSON格式)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_model_name` (`model_name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='算法模型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `algorithm_model`
--

LOCK TABLES `algorithm_model` WRITE;
/*!40000 ALTER TABLE `algorithm_model` DISABLE KEYS */;
INSERT INTO `algorithm_model` VALUES (1,'默认减灾能力评估模型','基于QLExpress动态规则引擎的减灾能力评估模型','1.0',1,NULL,NULL,'system','system','2025-10-11 15:46:01','2025-10-12 09:40:07'),(3,'ee','ee','1.0',1,NULL,NULL,'system','system','2025-10-12 09:40:13','2025-10-12 09:40:13');
/*!40000 ALTER TABLE `algorithm_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `algorithm_step`
--

DROP TABLE IF EXISTS `algorithm_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `algorithm_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `algorithm_config_id` bigint NOT NULL COMMENT '算法配置ID',
  `step_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '步骤名称',
  `step_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '步骤编码',
  `step_order` int NOT NULL COMMENT '执行顺序',
  `input_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '输入数据描述',
  `output_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '输出数据描述',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '步骤描述',
  `status` int DEFAULT '1' COMMENT '状态(1-启用,0-禁用)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_algorithm_step_config` (`algorithm_config_id`),
  KEY `idx_algorithm_step_order` (`step_order`),
  CONSTRAINT `algorithm_step_ibfk_1` FOREIGN KEY (`algorithm_config_id`) REFERENCES `algorithm_config` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `algorithm_step`
--

LOCK TABLES `algorithm_step` WRITE;
/*!40000 ALTER TABLE `algorithm_step` DISABLE KEYS */;
INSERT INTO `algorithm_step` VALUES (1,1,'二级指标计算','SECONDARY_CALCULATION',1,'调查数据','二级指标原始值','根据调查数据计算8个二级指标的原始值',1,'2025-09-23 08:38:55'),(2,1,'属性向量归一化','NORMALIZATION',2,'二级指标原始值','二级指标归一化值','对二级指标进行向量归一化处理',1,'2025-09-23 08:38:55'),(3,1,'二级指标定权','SECONDARY_WEIGHTING',3,'二级指标归一化值,指标权重','二级指标定权值','将归一化值与二级指标权重相乘',1,'2025-09-23 08:38:55'),(4,1,'优劣解算法计算','TOPSIS_CALCULATION',4,'二级指标定权值','一级指标值','基于TOPSIS优劣解算法计算一级指标',1,'2025-09-23 08:38:55'),(5,1,'能力分级计算','GRADING_CALCULATION',5,'一级指标值','能力分级结果','根据均值和标准差计算能力分级',1,'2025-09-23 08:38:55');
/*!40000 ALTER TABLE `algorithm_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dynamic_formula`
--

DROP TABLE IF EXISTS `dynamic_formula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dynamic_formula` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `formula_name` varchar(100) NOT NULL COMMENT '公式名称',
  `formula_code` varchar(50) NOT NULL COMMENT '公式编码',
  `formula_group` varchar(50) NOT NULL COMMENT '公式分组(STEP1,STEP2,STEP3)',
  `ql_expression` text NOT NULL COMMENT 'QLExpress表达式',
  `input_params` text COMMENT '输入参数定义(JSON格式)',
  `output_variable` varchar(50) NOT NULL COMMENT '输出变量名',
  `step_code` varchar(50) NOT NULL COMMENT '所属步骤编码',
  `dependency_formula` varchar(100) DEFAULT NULL COMMENT '依赖的前置公式',
  `sort_order` int DEFAULT '0' COMMENT '执行顺序',
  `description` text COMMENT '公式描述',
  `status` int DEFAULT '1' COMMENT '状态(1-启用,0-禁用)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `formula_code` (`formula_code`),
  KEY `idx_formula_code` (`formula_code`),
  KEY `idx_formula_group` (`formula_group`),
  KEY `idx_step_code` (`step_code`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=1760237173102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态公式表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dynamic_formula`
--

LOCK TABLES `dynamic_formula` WRITE;
/*!40000 ALTER TABLE `dynamic_formula` DISABLE KEYS */;
INSERT INTO `dynamic_formula` VALUES (103,'队伍管理能力计算','TEAM_MANAGEMENT','STEP1','teamManagement = managementStaff * 10000 / population','[{\"name\":\"managementStaff\",\"type\":\"Integer\",\"desc\":\"绠＄悊浜哄憳鏁癨\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','teamManagement','STEP1',NULL,1,'闃熶紞绠＄悊鑳藉姏=(绠＄悊浜哄憳鏁�/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(104,'风险评估能力计算','RISK_ASSESSMENT','STEP1','riskAssessment = riskAssessment == \"鏄痋\" ? 1.0 : 0.0','[{\"name\":\"riskAssessment\",\"type\":\"String\",\"desc\":\"鏄惁寮�灞曢闄╄瘎浼癨\"}]','riskAssessment','STEP1',NULL,2,'椋庨櫓璇勪及鑳藉姏=IF(寮�灞曢闄╄瘎浼�=\"鏄痋\",1,0)',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(105,'财政投入能力计算','FINANCIAL_INPUT','STEP1','financialInput = fundingAmount * 10000 / population','[{\"name\":\"fundingAmount\",\"type\":\"Double\",\"desc\":\"闃茬伨璧勯噾鎶曞叆\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','financialInput','STEP1',NULL,3,'璐㈡斂鎶曞叆鑳藉姏=(闃茬伨璧勯噾鎶曞叆/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(106,'物资储备能力计算','MATERIAL_RESERVE','STEP1','materialReserve = materialValue * 10000 / population','[{\"name\":\"materialValue\",\"type\":\"Double\",\"desc\":\"鐗╄祫鍌ㄥ浠峰�糪\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','materialReserve','STEP1',NULL,4,'鐗╄祫鍌ㄥ鑳藉姏=(鐗╄祫鍌ㄥ浠峰��/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(107,'医疗保障能力计算','MEDICAL_SUPPORT','STEP1','medicalSupport = hospitalBeds * 10000 / population','[{\"name\":\"hospitalBeds\",\"type\":\"Integer\",\"desc\":\"鍖婚櫌搴婁綅鏁癨\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','medicalSupport','STEP1',NULL,5,'鍖荤枟淇濋殰鑳藉姏=(鍖婚櫌搴婁綅鏁�/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(108,'自救互救能力计算','SELF_RESCUE','STEP1','selfRescue = (firefighters + volunteers + militiaReserve) * 10000 / population','[{\"name\":\"firefighters\",\"type\":\"Integer\",\"desc\":\"娑堥槻鍛樻暟閲廫\"},{\"name\":\"volunteers\",\"type\":\"Integer\",\"desc\":\"蹇楁効鑰呬汉鏁癨\"},{\"name\":\"militiaReserve\",\"type\":\"Integer\",\"desc\":\"姘戝叺棰勫褰逛汉鏁癨\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','selfRescue','STEP1',NULL,6,'鑷晳浜掓晳鑳藉姏=(娑堥槻鍛�+蹇楁効鑰�+姘戝叺棰勫褰�)/甯镐綇浜哄彛*10000',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(109,'公众避险能力计算','PUBLIC_AVOIDANCE','STEP1','publicAvoidance = trainingParticipants * 100 / population','[{\"name\":\"trainingParticipants\",\"type\":\"Integer\",\"desc\":\"鍩硅鍙備笌浜烘\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','publicAvoidance','STEP1',NULL,7,'鍏紬閬块櫓鑳藉姏=(鍩硅鍙備笌浜烘/甯镐綇浜哄彛)*100',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(110,'转移安置能力计算','RELOCATION_CAPACITY','STEP1','relocationCapacity = shelterCapacity / population','[{\"name\":\"shelterCapacity\",\"type\":\"Integer\",\"desc\":\"閬块毦鍦烘墍瀹归噺\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','relocationCapacity','STEP1',NULL,8,'杞Щ瀹夌疆鑳藉姏=(閬块毦鍦烘墍瀹归噺/甯镐綇浜哄彛)',1,'system','system','2025-10-12 00:58:43','2025-10-12 12:51:50'),(111,'闃熶紞绠＄悊鑳藉姏璁＄畻','SC_TEAM_MANAGEMENT','SECONDARY_CALCULATION','teamManagement = managementStaff * 10000 / population','[{\"name\":\"managementStaff\",\"type\":\"Integer\",\"desc\":\"绠＄悊浜哄憳鏁癨\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','teamManagement','SECONDARY_CALCULATIO',NULL,1,'闃熶紞绠＄悊鑳藉姏=(绠＄悊浜哄憳鏁�/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(112,'椋庨櫓璇勪及鑳藉姏璁＄畻','SC_RISK_ASSESSMENT','SECONDARY_CALCULATION','riskAssessment = riskAssessment == \"鏄痋\" ? 1.0 : 0.0','[{\"name\":\"riskAssessment\",\"type\":\"String\",\"desc\":\"鏄惁寮�灞曢闄╄瘎浼癨\"}]','riskAssessment','SECONDARY_CALCULATIO',NULL,2,'椋庨櫓璇勪及鑳藉姏=IF(寮�灞曢闄╄瘎浼�=\"鏄痋\",1,0)',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(113,'璐㈡斂鎶曞叆鑳藉姏璁＄畻','SC_FINANCIAL_INPUT','SECONDARY_CALCULATION','financialInput = fundingAmount * 10000 / population','[{\"name\":\"fundingAmount\",\"type\":\"Double\",\"desc\":\"闃茬伨璧勯噾鎶曞叆\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','financialInput','SECONDARY_CALCULATIO',NULL,3,'璐㈡斂鎶曞叆鑳藉姏=(闃茬伨璧勯噾鎶曞叆/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(114,'鐗╄祫鍌ㄥ鑳藉姏璁＄畻','SC_MATERIAL_RESERVE','SECONDARY_CALCULATION','materialReserve = materialValue * 10000 / population','[{\"name\":\"materialValue\",\"type\":\"Double\",\"desc\":\"鐗╄祫鍌ㄥ浠峰�糪\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','materialReserve','SECONDARY_CALCULATIO',NULL,4,'鐗╄祫鍌ㄥ鑳藉姏=(鐗╄祫鍌ㄥ浠峰��/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(115,'鍖荤枟淇濋殰鑳藉姏璁＄畻','SC_MEDICAL_SUPPORT','SECONDARY_CALCULATION','medicalSupport = hospitalBeds * 10000 / population','[{\"name\":\"hospitalBeds\",\"type\":\"Integer\",\"desc\":\"鍖婚櫌搴婁綅鏁癨\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','medicalSupport','SECONDARY_CALCULATIO',NULL,5,'鍖荤枟淇濋殰鑳藉姏=(鍖婚櫌搴婁綅鏁�/甯镐綇浜哄彛)*10000',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(116,'鑷晳浜掓晳鑳藉姏璁＄畻','SC_SELF_RESCUE','SECONDARY_CALCULATION','selfRescue = (firefighters + volunteers + militiaReserve) * 10000 / population','[{\"name\":\"firefighters\",\"type\":\"Integer\",\"desc\":\"娑堥槻鍛樻暟閲廫\"},{\"name\":\"volunteers\",\"type\":\"Integer\",\"desc\":\"蹇楁効鑰呬汉鏁癨\"},{\"name\":\"militiaReserve\",\"type\":\"Integer\",\"desc\":\"姘戝叺棰勫褰逛汉鏁癨\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','selfRescue','SECONDARY_CALCULATIO',NULL,6,'鑷晳浜掓晳鑳藉姏=(娑堥槻鍛�+蹇楁効鑰�+姘戝叺棰勫褰�)/甯镐綇浜哄彛*10000',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(117,'鍏紬閬块櫓鑳藉姏璁＄畻','SC_PUBLIC_AVOIDANCE','SECONDARY_CALCULATION','publicAvoidance = trainingParticipants * 100 / population','[{\"name\":\"trainingParticipants\",\"type\":\"Integer\",\"desc\":\"鍩硅鍙備笌浜烘\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','publicAvoidance','SECONDARY_CALCULATIO',NULL,7,'鍏紬閬块櫓鑳藉姏=(鍩硅鍙備笌浜烘/甯镐綇浜哄彛)*100',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(118,'杞Щ瀹夌疆鑳藉姏璁＄畻','SC_RELOCATION_CAPACITY','SECONDARY_CALCULATION','relocationCapacity = shelterCapacity / population','[{\"name\":\"shelterCapacity\",\"type\":\"Integer\",\"desc\":\"閬块毦鍦烘墍瀹归噺\"},{\"name\":\"population\",\"type\":\"Long\",\"desc\":\"甯镐綇浜哄彛\"}]','relocationCapacity','SECONDARY_CALCULATIO',NULL,8,'杞Щ瀹夌疆鑳藉姏=(閬块毦鍦烘墍瀹归噺/甯镐綇浜哄彛)',1,'system','system','2025-10-12 00:59:20','2025-10-12 00:59:20'),(1760236767205,'测试公式','TEST_FORMULA_001','STEP1','result = management_staff/population','','result','SECONDARY_CALCULATION','',0,'',1,'system','system','2025-10-12 10:39:52','2025-10-12 15:45:39');
/*!40000 ALTER TABLE `dynamic_formula` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `evaluation_model`
--

DROP TABLE IF EXISTS `evaluation_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evaluation_model` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `model_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '????',
  `version` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '1.0' COMMENT '????',
  `status` int DEFAULT '1' COMMENT '??(1-??,0-??)',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '??????',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '???',
  `update_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`id`),
  UNIQUE KEY `model_code` (`model_code`),
  KEY `idx_evaluation_model_code` (`model_code`),
  KEY `idx_evaluation_model_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evaluation_model`
--

LOCK TABLES `evaluation_model` WRITE;
/*!40000 ALTER TABLE `evaluation_model` DISABLE KEYS */;
INSERT INTO `evaluation_model` VALUES (3,'标准减灾能力评估模型','STANDARD_MODEL','基于TOPSIS算法的标准减灾能力评估模型','1.0',1,1,'2025-10-12 09:43:09','2025-10-12 09:43:09',NULL,NULL);
/*!40000 ALTER TABLE `evaluation_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `formula_config`
--

DROP TABLE IF EXISTS `formula_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formula_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `algorithm_step_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '算法步骤ID',
  `formula_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公式名称',
  `formula_expression` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公式表达式',
  `input_variables` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '输入变量列表(JSON格式)',
  `output_variable` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '输出变量名',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公式描述',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` int DEFAULT '1' COMMENT '状态(1-启用,0-禁用)',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_formula_config_step` (`algorithm_step_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `formula_config`
--

LOCK TABLES `formula_config` WRITE;
/*!40000 ALTER TABLE `formula_config` DISABLE KEYS */;
INSERT INTO `formula_config` VALUES (1,'1','队伍管理能力计算','(management_staff/population)*10000','[\"management_staff\",\"population\"]','management_capability','队伍管理能力=(本级灾害管理工作人员总数/常住人口数量)*10000','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(2,'1','风险评估能力计算','IF(risk_assessment=\"是\",1,0)','[\"risk_assessment\"]','risk_assessment_capability','风险评估能力=IF(是否开展风险评估=\"是\",1,0)','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(3,'1','财政投入能力计算','(funding_amount/population)*10000','[\"funding_amount\",\"population\"]','funding_capability','财政投入能力=(防灾减灾救灾资金投入总金额/常住人口数量)*10000','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(4,'1','物资储备能力计算','(material_value/population)*10000','[\"material_value\",\"population\"]','material_capability','物资储备能力=(现有储备物资装备折合金额/常住人口数量)*10000','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(5,'1','医疗保障能力计算','(hospital_beds/population)*10000','[\"hospital_beds\",\"population\"]','medical_capability','医疗保障能力=(实有住院床位数/常住人口数量)*10000','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(6,'1','自救互救能力计算','((firefighters+volunteers+militia_reserve)/population)*10000','[\"firefighters\",\"volunteers\",\"militia_reserve\",\"population\"]','self_rescue_capability','自救互救能力=(消防员数量+志愿者人数+民兵预备役人数)/常住人口数量)*10000','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(7,'1','公众避险能力计算','(training_participants/population)*10000','[\"training_participants\",\"population\"]','public_avoidance_capability','公众避险能力=(应急管理培训和演练参与人次/常住人口数量)*10000','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(8,'1','转移安置能力计算','(shelter_capacity/population)*10000','[\"shelter_capacity\",\"population\"]','relocation_capability','转移安置能力=(本级灾害应急避难场所容量/常住人口数量)*10000','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(9,'2','属性向量归一化公式','value/SQRT(SUMSQ(all_values))','[\"value\",\"all_values\"]','normalized_value','归一化值=原始值/SQRT(SUMSQ(所有原始值))','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(10,'3','二级指标定权公式','normalized_value*weight','[\"normalized_value\",\"weight\"]','weighted_value','定权值=归一化值*权重','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(11,'4','正理想解距离公式','SQRT(SUMSQ(max_values-current_values))','[\"max_values\",\"current_values\"]','positive_distance','正理想解距离=SQRT(SUMSQ(最大值-当前值))','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(12,'4','负理想解距离公式','SQRT(SUMSQ(min_values-current_values))','[\"min_values\",\"current_values\"]','negative_distance','负理想解距离=SQRT(SUMSQ(最小值-当前值))','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(13,'4','TOPSIS得分公式','negative_distance/(negative_distance+positive_distance)','[\"negative_distance\",\"positive_distance\"]','topsis_score','TOPSIS得分=负理想解距离/(负理想解距离+正理想解距离)','2025-09-23 08:38:55',1,'2025-10-11 16:15:59'),(14,'5','能力分级公式','IF(mean<=0.5*stdev,IF(value>=mean+1.5*stdev,\"强\",IF(value>=mean+0.5*stdev,\"较强\",\"中等\")),IF(mean<=1.5*stdev,IF(value>=mean+1.5*stdev,\"强\",IF(value>=mean+0.5*stdev,\"较强\",IF(value>=mean-0.5*stdev,\"中等\",\"较弱\"))),IF(value>=mean+1.5*stdev,\"强\",IF(value>=mean+0.5*stdev,\"较强\",IF(value>=mean-0.5*stdev,\"中等\",IF(value>=mean-1.5*stdev,\"较弱\",\"弱\"))))))','[\"value\",\"mean\",\"stdev\"]','grade','基于均值和标准差的五级分类公式','2025-09-23 08:38:55',1,'2025-10-11 16:15:59');
/*!40000 ALTER TABLE `formula_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `indicator_weight`
--

DROP TABLE IF EXISTS `indicator_weight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `indicator_weight` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_id` bigint NOT NULL COMMENT '权重配置ID',
  `indicator_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标编码',
  `indicator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标名称',
  `indicator_level` int NOT NULL COMMENT '指标级别(1-一级指标,2-二级指标)',
  `weight` decimal(5,4) NOT NULL COMMENT '权重值',
  `parent_id` bigint DEFAULT NULL COMMENT '父指标ID',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_indicator_weight_config` (`config_id`),
  KEY `idx_indicator_weight_parent` (`parent_id`),
  KEY `idx_indicator_weight_level` (`indicator_level`),
  CONSTRAINT `indicator_weight_ibfk_1` FOREIGN KEY (`config_id`) REFERENCES `weight_config` (`id`),
  CONSTRAINT `indicator_weight_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `indicator_weight` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `indicator_weight`
--

LOCK TABLES `indicator_weight` WRITE;
/*!40000 ALTER TABLE `indicator_weight` DISABLE KEYS */;
INSERT INTO `indicator_weight` VALUES (1,1,'L1_MANAGEMENT','灾害管理能力',1,0.3300,NULL,1,'2025-09-23 08:38:54'),(2,1,'L1_PREPARATION','灾害备灾能力',1,0.3200,NULL,2,'2025-09-23 08:38:54'),(3,1,'L1_SELF_RESCUE','自救转移能力',1,0.3500,NULL,3,'2025-09-23 08:38:54'),(4,1,'L2_MANAGEMENT_CAPABILITY','队伍管理能力',2,0.3700,1,1,'2025-09-23 08:38:54'),(5,1,'L2_RISK_ASSESSMENT','风险评估能力',2,0.3100,1,2,'2025-09-23 08:38:54'),(6,1,'L2_FUNDING','财政投入能力',2,0.3200,1,3,'2025-09-23 08:38:54'),(7,1,'L2_MATERIAL','物资储备能力',2,0.5100,2,1,'2025-09-23 08:38:54'),(8,1,'L2_MEDICAL','医疗保障能力',2,0.4900,2,2,'2025-09-23 08:38:54'),(9,1,'L2_SELF_RESCUE','自救互救能力',2,0.3300,3,1,'2025-09-23 08:38:54'),(10,1,'L2_PUBLIC_AVOIDANCE','公众避险能力',2,0.3300,3,2,'2025-09-23 08:38:54'),(11,1,'L2_RELOCATION','转移安置能力',2,0.3400,3,3,'2025-09-23 08:38:54');
/*!40000 ALTER TABLE `indicator_weight` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `model_execution_record`
--

DROP TABLE IF EXISTS `model_execution_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `model_execution_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_id` bigint NOT NULL COMMENT '??ID',
  `execution_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `region_ids` text COLLATE utf8mb4_unicode_ci COMMENT '????ID??(JSON??)',
  `weight_config_id` bigint DEFAULT NULL COMMENT '????ID',
  `execution_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'RUNNING' COMMENT '????(RUNNING/SUCCESS/FAILED)',
  `start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` timestamp NULL DEFAULT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '????',
  `result_summary` text COLLATE utf8mb4_unicode_ci COMMENT '??????(JSON??)',
  `create_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`id`),
  UNIQUE KEY `execution_code` (`execution_code`),
  KEY `weight_config_id` (`weight_config_id`),
  KEY `idx_execution_record_model` (`model_id`),
  KEY `idx_execution_record_status` (`execution_status`),
  CONSTRAINT `model_execution_record_ibfk_1` FOREIGN KEY (`model_id`) REFERENCES `evaluation_model` (`id`),
  CONSTRAINT `model_execution_record_ibfk_2` FOREIGN KEY (`weight_config_id`) REFERENCES `weight_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `model_execution_record`
--

LOCK TABLES `model_execution_record` WRITE;
/*!40000 ALTER TABLE `model_execution_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `model_execution_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `model_step`
--

DROP TABLE IF EXISTS `model_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `model_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_id` bigint NOT NULL COMMENT '??ID',
  `step_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `step_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `step_order` int NOT NULL COMMENT '????',
  `step_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????(CALCULATION/NORMALIZATION/WEIGHTING/TOPSIS/GRADING)',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '????',
  `input_variables` text COLLATE utf8mb4_unicode_ci COMMENT '????(JSON??)',
  `output_variables` text COLLATE utf8mb4_unicode_ci COMMENT '????(JSON??)',
  `depends_on` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????ID(????)',
  `status` int DEFAULT '1' COMMENT '??(1-??,0-??)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_model_step_model_order` (`model_id`,`step_order`),
  CONSTRAINT `model_step_ibfk_1` FOREIGN KEY (`model_id`) REFERENCES `evaluation_model` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `model_step`
--

LOCK TABLES `model_step` WRITE;
/*!40000 ALTER TABLE `model_step` DISABLE KEYS */;
INSERT INTO `model_step` VALUES (15,3,'评估指标赋值','INDICATOR_ASSIGNMENT',1,'CALCULATION','根据调查数据计算8个二级指标的原始值',NULL,NULL,NULL,1,'2025-10-12 09:43:09'),(16,3,'属性向量归一化','VECTOR_NORMALIZATION',2,'NORMALIZATION','对二级指标进行向量归一化处理',NULL,NULL,NULL,1,'2025-10-12 09:43:09'),(17,3,'二级指标定权','SECONDARY_WEIGHTING',3,'WEIGHTING','将归一化值与二级指标权重相乘',NULL,NULL,NULL,1,'2025-10-12 09:43:09'),(18,3,'优劣解计算','TOPSIS_DISTANCE',4,'TOPSIS','TOPSIS方法：计算到正理想解（优）和负理想解（差）的距离',NULL,NULL,NULL,1,'2025-10-12 13:36:29'),(19,3,'能力值计算与分级','CAPABILITY_GRADE',5,'GRADING','计算能力值（贴近度）并进行五级分类（强/较强/中等/较弱/弱）',NULL,NULL,NULL,1,'2025-10-12 13:36:29');
/*!40000 ALTER TABLE `model_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `model_steps`
--

DROP TABLE IF EXISTS `model_steps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `model_steps` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `model_id` bigint NOT NULL COMMENT '关联的模型ID',
  `step_code` varchar(50) NOT NULL COMMENT '步骤代码(STEP1,STEP2,STEP3,STEP4,STEP5)',
  `step_name` varchar(100) NOT NULL COMMENT '步骤名称',
  `step_description` text COMMENT '步骤描述',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `formula_group` varchar(50) DEFAULT NULL COMMENT '公式分组',
  `backend_step_code` varchar(50) DEFAULT NULL COMMENT '后端步骤代码(SECONDARY_CALCULATION,NORMALIZATION等)',
  `status` int DEFAULT '1' COMMENT '状态(1-启用,0-禁用)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_model_id` (`model_id`),
  KEY `idx_step_code` (`step_code`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `model_steps_ibfk_1` FOREIGN KEY (`model_id`) REFERENCES `algorithm_model` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型步骤配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `model_steps`
--

LOCK TABLES `model_steps` WRITE;
/*!40000 ALTER TABLE `model_steps` DISABLE KEYS */;
INSERT INTO `model_steps` VALUES (6,1,'STEP1','综合指标计算','根据调查数据计算8个二级指标的原始值',1,'STEP1','SECONDARY_CALCULATION',1,'system','system','2025-10-12 09:48:27','2025-10-12 09:48:27'),(7,1,'STEP2','属性向量归一化','对二级指标进行向量归一化处理',2,'STEP2','NORMALIZATION',1,'system','system','2025-10-12 09:48:27','2025-10-12 09:48:27'),(8,1,'STEP4','优劣解算法计算','基于TOPSIS优劣解算法计算一级指标',3,'STEP4','TOPSIS_CALCULATION',1,'system','system','2025-10-12 09:48:27','2025-10-12 09:48:27'),(9,1,'STEP3','二级指标定权（修改测试）','将归一化值与二级指标权重相乘',4,'STEP3','SECONDARY_WEIGHTING',1,'system','system','2025-10-12 09:48:27','2025-10-12 09:48:27'),(10,1,'STEP5','能力分级计算','根据均值和标准差计算能力分级',5,'STEP5','GRADING_CALCULATION',1,'system','system','2025-10-12 09:48:27','2025-10-12 09:48:27'),(12,3,'STEP1','aa','aa',1,'STEP1','SECONDARY_CALCULATION',1,'system','system','2025-10-12 10:12:14','2025-10-12 10:12:14'),(13,3,'STEP2','bb','bb',2,'STEP2','NORMALIZATION',1,'system','system','2025-10-12 10:12:14','2025-10-12 10:12:14');
/*!40000 ALTER TABLE `model_steps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `primary_indicator_result`
--

DROP TABLE IF EXISTS `primary_indicator_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `primary_indicator_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `secondary_result_id` bigint NOT NULL COMMENT '二级指标结果ID',
  `survey_id` bigint DEFAULT NULL COMMENT '调查数据ID',
  `algorithm_id` bigint DEFAULT NULL COMMENT '算法配置ID',
  `weight_config_id` bigint DEFAULT NULL COMMENT '权重配置ID',
  `indicator_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '一级指标代码',
  `indicator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '一级指标名称',
  `calculated_value` double DEFAULT NULL COMMENT '计算值',
  `weight_value` double DEFAULT NULL COMMENT '权重值',
  `weighted_value` double DEFAULT NULL COMMENT '加权值',
  `process_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '计算过程数据(JSON格式)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int DEFAULT '0' COMMENT '是否删除(0-未删除，1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='一级指标结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `primary_indicator_result`
--

LOCK TABLES `primary_indicator_result` WRITE;
/*!40000 ALTER TABLE `primary_indicator_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `primary_indicator_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `region`
--

DROP TABLE IF EXISTS `region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) NOT NULL COMMENT '地区代码',
  `name` varchar(100) NOT NULL COMMENT '地区名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父级地区ID',
  `level` int NOT NULL COMMENT '地区级别（1-省，2-市，3-县，4-镇）',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` int DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='地区组织机构表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `region`
--

LOCK TABLES `region` WRITE;
/*!40000 ALTER TABLE `region` DISABLE KEYS */;
INSERT INTO `region` VALUES (1,'510000','四川省',NULL,1,1,1,'2025-09-24 05:38:23'),(2,'511400','眉山市',1,2,1,1,'2025-09-24 05:38:23'),(3,'511425','青神县',2,3,1,1,'2025-09-24 05:38:24'),(4,'511425001','汉阳镇',3,4,1,1,'2025-09-24 05:38:24'),(5,'511425002','西龙镇',3,4,2,1,'2025-09-24 05:38:24'),(6,'110000','北京市',NULL,1,2,1,'2025-09-24 05:38:24'),(7,'310000','上海市',NULL,1,3,1,'2025-09-24 05:38:24'),(8,'440000','广东省',NULL,1,4,1,'2025-09-24 05:38:24'),(9,'110101','东城区',6,2,1,1,'2025-09-24 05:38:24'),(10,'110102','西城区',6,2,2,1,'2025-09-24 05:38:24'),(11,'110105','朝阳区',6,2,3,1,'2025-09-24 05:38:24'),(12,'310101','黄浦区',7,2,1,1,'2025-09-24 05:38:24'),(13,'310104','徐汇区',7,2,2,1,'2025-09-24 05:38:24'),(14,'310105','长宁区',7,2,3,1,'2025-09-24 05:38:24'),(15,'440100','广州市',8,2,1,1,'2025-09-24 05:38:24'),(16,'440300','深圳市',8,2,2,1,'2025-09-24 05:38:24'),(17,'440600','佛山市',8,2,3,1,'2025-09-24 05:38:24'),(18,'440103','荔湾区',15,3,1,1,'2025-09-24 05:38:24'),(19,'440104','越秀区',15,3,2,1,'2025-09-24 05:38:24'),(20,'440105','海珠区',15,3,3,1,'2025-09-24 05:38:24'),(21,'440303','罗湖区',15,3,1,1,'2025-09-24 05:38:24'),(22,'440304','福田区',15,3,2,1,'2025-09-24 05:38:24'),(23,'440305','南山区',15,3,3,1,'2025-09-24 05:38:24');
/*!40000 ALTER TABLE `region` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `primary_result_id` bigint NOT NULL,
  `report_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报告名称',
  `report_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报告类型(PDF/WORD/MAP)',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报告文件路径',
  `map_image_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专题图路径',
  `generate_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_report_primary` (`primary_result_id`),
  KEY `idx_report_time` (`generate_time` DESC),
  CONSTRAINT `report_ibfk_1` FOREIGN KEY (`primary_result_id`) REFERENCES `primary_indicator_result` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report`
--

LOCK TABLES `report` WRITE;
/*!40000 ALTER TABLE `report` DISABLE KEYS */;
/*!40000 ALTER TABLE `report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secondary_indicator_result`
--

DROP TABLE IF EXISTS `secondary_indicator_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `secondary_indicator_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `survey_data_id` bigint NOT NULL,
  `config_id` bigint NOT NULL,
  `management_capability` decimal(10,6) DEFAULT NULL COMMENT '队伍管理能力原始值',
  `risk_assessment_capability` decimal(10,6) DEFAULT NULL COMMENT '风险评估能力原始值',
  `funding_capability` decimal(10,6) DEFAULT NULL COMMENT '财政投入能力原始值',
  `material_capability` decimal(10,6) DEFAULT NULL COMMENT '物资储备能力原始值',
  `medical_capability` decimal(10,6) DEFAULT NULL COMMENT '医疗保障能力原始值',
  `self_rescue_capability` decimal(10,6) DEFAULT NULL COMMENT '自救互救能力原始值',
  `public_avoidance_capability` decimal(10,6) DEFAULT NULL COMMENT '公众避险能力原始值',
  `relocation_capability` decimal(10,6) DEFAULT NULL COMMENT '转移安置能力原始值',
  `management_normalized` decimal(10,6) DEFAULT NULL COMMENT '队伍管理能力归一化值',
  `risk_assessment_normalized` decimal(10,6) DEFAULT NULL COMMENT '风险评估能力归一化值',
  `funding_normalized` decimal(10,6) DEFAULT NULL COMMENT '财政投入能力归一化值',
  `material_normalized` decimal(10,6) DEFAULT NULL COMMENT '物资储备能力归一化值',
  `medical_normalized` decimal(10,6) DEFAULT NULL COMMENT '医疗保障能力归一化值',
  `self_rescue_normalized` decimal(10,6) DEFAULT NULL COMMENT '自救互救能力归一化值',
  `public_avoidance_normalized` decimal(10,6) DEFAULT NULL COMMENT '公众避险能力归一化值',
  `relocation_normalized` decimal(10,6) DEFAULT NULL COMMENT '转移安置能力归一化值',
  `calculate_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_secondary_result_survey` (`survey_data_id`),
  KEY `idx_secondary_result_config` (`config_id`),
  KEY `idx_secondary_result_time` (`calculate_time` DESC),
  CONSTRAINT `secondary_indicator_result_ibfk_1` FOREIGN KEY (`survey_data_id`) REFERENCES `survey_data` (`id`),
  CONSTRAINT `secondary_indicator_result_ibfk_2` FOREIGN KEY (`config_id`) REFERENCES `weight_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secondary_indicator_result`
--

LOCK TABLES `secondary_indicator_result` WRITE;
/*!40000 ALTER TABLE `secondary_indicator_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `secondary_indicator_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `step_algorithm`
--

DROP TABLE IF EXISTS `step_algorithm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `step_algorithm` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `step_id` bigint NOT NULL COMMENT '??ID',
  `algorithm_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `algorithm_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `algorithm_order` int NOT NULL COMMENT '??????',
  `ql_expression` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'QLExpress???',
  `input_params` text COLLATE utf8mb4_unicode_ci COMMENT '??????(JSON??)',
  `output_param` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '????',
  `status` int DEFAULT '1' COMMENT '??(1-??,0-??)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_step_algorithm_step_order` (`step_id`,`algorithm_order`),
  CONSTRAINT `step_algorithm_ibfk_1` FOREIGN KEY (`step_id`) REFERENCES `model_step` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `step_algorithm`
--

LOCK TABLES `step_algorithm` WRITE;
/*!40000 ALTER TABLE `step_algorithm` DISABLE KEYS */;
INSERT INTO `step_algorithm` VALUES (23,15,'队伍管理能力计算','TEAM_MANAGEMENT',1,'(management_staff * 1.0 / population) * 10000',NULL,'teamManagement','è®¡ç®—é˜Ÿä¼ç®¡ç†èƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(24,15,'风险评估能力计算','RISK_ASSESSMENT',2,'riskAssessment != null && riskAssessment.equals(\"是\") ? 1.0 : 0.0',NULL,'riskAssessment','è®¡ç®—é£Žé™©è¯„ä¼°èƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(25,15,'财政投入能力计算','FINANCIAL_INPUT',3,'(funding_amount * 1.0 / population) * 10000',NULL,'financialInput','è®¡ç®—è´¢æ”¿æŠ•å…¥èƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(26,15,'物资储备能力计算','MATERIAL_RESERVE',4,'(material_value * 1.0 / population) * 10000',NULL,'materialReserve','è®¡ç®—ç‰©èµ„å‚¨å¤‡èƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(27,15,'医疗保障能力计算','MEDICAL_SUPPORT',5,'(hospital_beds * 1.0 / population) * 10000',NULL,'medicalSupport','è®¡ç®—åŒ»ç–—ä¿éšœèƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(28,15,'自救互救能力计算','SELF_RESCUE',6,'((firefighters + volunteers + militia_reserve) * 1.0 / population) * 10000',NULL,'selfRescue','è®¡ç®—è‡ªæ•‘äº’æ•‘èƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(29,15,'公众避险能力计算','PUBLIC_AVOIDANCE',7,'(training_participants * 1.0 / population) * 100',NULL,'publicAvoidance','è®¡ç®—å…¬ä¼—é¿é™©èƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(30,15,'转移安置能力计算','RELOCATION_CAPACITY',8,'shelter_capacity * 1.0 / population',NULL,'relocationCapacity','è®¡ç®—è½¬ç§»å®‰ç½®èƒ½åŠ›æŒ‡æ ‡',1,'2025-10-12 10:36:51'),(31,16,'队伍管理能力归一化','TEAM_MANAGEMENT_NORM',1,'@NORMALIZE:teamManagement',NULL,'teamManagementNorm','队伍管理能力（属性向量归一化）=本乡镇队伍管理能力/SQRT(SUMSQ(全部乡镇队伍管理能力))',1,'2025-10-12 13:36:29'),(32,16,'风险评估能力归一化','RISK_ASSESSMENT_NORM',2,'@NORMALIZE:riskAssessment',NULL,'riskAssessmentNorm','风险评估能力（属性向量归一化）=本乡镇风险评估能力/SQRT(SUMSQ(全部乡镇风险评估能力))',1,'2025-10-12 13:36:29'),(33,16,'财政投入能力归一化','FINANCIAL_INPUT_NORM',3,'@NORMALIZE:financialInput',NULL,'financialInputNorm','财政投入能力（属性向量归一化）=本乡镇财政投入能力/SQRT(SUMSQ(全部乡镇财政投入能力))',1,'2025-10-12 13:36:29'),(34,16,'物资储备能力归一化','MATERIAL_RESERVE_NORM',4,'@NORMALIZE:materialReserve',NULL,'materialReserveNorm','物资储备能力（属性向量归一化）=本乡镇物资储备能力/SQRT(SUMSQ(全部乡镇物资储备能力))',1,'2025-10-12 13:36:29'),(35,16,'医疗保障能力归一化','MEDICAL_SUPPORT_NORM',5,'@NORMALIZE:medicalSupport',NULL,'medicalSupportNorm','医疗保障能力（属性向量归一化）=本乡镇医疗保障能力/SQRT(SUMSQ(全部乡镇医疗保障能力))',1,'2025-10-12 13:36:29'),(36,16,'自救互救能力归一化','SELF_RESCUE_NORM',6,'@NORMALIZE:selfRescue',NULL,'selfRescueNorm','自救互救能力（属性向量归一化）=本乡镇自救互救能力/SQRT(SUMSQ(全部乡镇自救互救能力))',1,'2025-10-12 13:36:29'),(37,16,'公众避险能力归一化','PUBLIC_AVOIDANCE_NORM',7,'@NORMALIZE:publicAvoidance',NULL,'publicAvoidanceNorm','公众避险能力（属性向量归一化）=本乡镇公众避险能力/SQRT(SUMSQ(全部乡镇公众避险能力))',1,'2025-10-12 13:36:29'),(38,16,'转移安置能力归一化','RELOCATION_CAPACITY_NORM',8,'@NORMALIZE:relocationCapacity',NULL,'relocationCapacityNorm','转移安置能力（属性向量归一化）=本乡镇转移安置能力/SQRT(SUMSQ(全部乡镇转移安置能力))',1,'2025-10-12 13:36:29'),(39,17,'队伍管理能力定权','TEAM_MANAGEMENT_WEIGHT',1,'teamManagementNorm * weight_L2_MANAGEMENT_CAPABILITY',NULL,'teamManagementWeighted','队伍管理能力（定权）=队伍管理能力（属性向量归一化）*队伍管理能力二级权重指标',1,'2025-10-12 13:36:29'),(40,17,'风险评估能力定权','RISK_ASSESSMENT_WEIGHT',2,'riskAssessmentNorm * weight_L2_RISK_ASSESSMENT',NULL,'riskAssessmentWeighted','风险评估能力（定权）=风险评估能力（属性向量归一化）*风险评估能力二级权重指标',1,'2025-10-12 13:36:29'),(41,17,'财政投入能力定权','FINANCIAL_INPUT_WEIGHT',3,'financialInputNorm * weight_L2_FUNDING',NULL,'financialInputWeighted','财政投入能力（定权）=财政投入能力（属性向量归一化）*财政投入能力二级权重指标',1,'2025-10-12 13:36:29'),(42,17,'物资储备能力定权','MATERIAL_RESERVE_WEIGHT',4,'materialReserveNorm * weight_L2_MATERIAL',NULL,'materialReserveWeighted','物资储备能力（定权）=物资储备能力（属性向量归一化）*物资储备能力二级权重指标',1,'2025-10-12 13:36:29'),(43,17,'医疗保障能力定权','MEDICAL_SUPPORT_WEIGHT',5,'medicalSupportNorm * weight_L2_MEDICAL',NULL,'medicalSupportWeighted','医疗保障能力（定权）=医疗保障能力（属性向量归一化）*医疗保障能力二级权重指标',1,'2025-10-12 13:36:29'),(44,17,'自救互救能力定权','SELF_RESCUE_WEIGHT',6,'selfRescueNorm * weight_L2_SELF_RESCUE',NULL,'selfRescueWeighted','自救互救能力（定权）=自救互救能力（属性向量归一化）*自救互救能力二级权重指标',1,'2025-10-12 13:36:29'),(45,17,'公众避险能力定权','PUBLIC_AVOIDANCE_WEIGHT',7,'publicAvoidanceNorm * weight_L2_PUBLIC_AVOIDANCE',NULL,'publicAvoidanceWeighted','公众避险能力（定权）=公众避险能力（属性向量归一化）*公众避险能力二级权重指标',1,'2025-10-12 13:36:29'),(46,17,'转移安置能力定权','RELOCATION_CAPACITY_WEIGHT',8,'relocationCapacityNorm * weight_L2_RELOCATION',NULL,'relocationCapacityWeighted','转移安置能力（定权）=转移安置能力（属性向量归一化）*转移安置能力二级权重指标',1,'2025-10-12 13:36:29'),(47,17,'队伍管理能力综合定权','TEAM_MANAGEMENT_TOTAL',9,'teamManagementNorm * weight_L1_MANAGEMENT * weight_L2_MANAGEMENT_CAPABILITY',NULL,'teamManagementTotal','队伍管理能力（定权）=队伍管理能力（属性向量归一化）*灾害管理能力一级权重指标*队伍管理能力二级权重指标',1,'2025-10-12 13:36:29'),(48,17,'风险评估能力综合定权','RISK_ASSESSMENT_TOTAL',10,'riskAssessmentNorm * weight_L1_MANAGEMENT * weight_L2_RISK_ASSESSMENT',NULL,'riskAssessmentTotal','风险评估能力（定权）=风险评估能力（属性向量归一化）*灾害管理能力一级权重指标*风险评估能力二级权重指标',1,'2025-10-12 13:36:29'),(49,17,'财政投入能力综合定权','FINANCIAL_INPUT_TOTAL',11,'financialInputNorm * weight_L1_MANAGEMENT * weight_L2_FUNDING',NULL,'financialInputTotal','财政投入能力（定权）=财政投入能力（属性向量归一化）*灾害管理能力一级权重指标*财政投入能力二级权重指标',1,'2025-10-12 13:36:29'),(50,17,'物资储备能力综合定权','MATERIAL_RESERVE_TOTAL',12,'materialReserveNorm * weight_L1_PREPARATION * weight_L2_MATERIAL',NULL,'materialReserveTotal','物资储备能力（定权）=物资储备能力（属性向量归一化）*灾害备灾能力一级权重指标*物资储备能力二级权重指标',1,'2025-10-12 13:36:29'),(51,17,'医疗保障能力综合定权','MEDICAL_SUPPORT_TOTAL',13,'medicalSupportNorm * weight_L1_PREPARATION * weight_L2_MEDICAL',NULL,'medicalSupportTotal','医疗保障能力（定权）=医疗保障能力（属性向量归一化）*灾害备灾能力一级权重指标*医疗保障能力二级权重指标',1,'2025-10-12 13:36:29'),(52,17,'自救互救能力综合定权','SELF_RESCUE_TOTAL',14,'selfRescueNorm * weight_L1_SELF_RESCUE * weight_L2_SELF_RESCUE',NULL,'selfRescueTotal','自救互救能力（定权）=自救互救能力（属性向量归一化）*自救转移能力一级权重指标*自救互救能力二级权重指标',1,'2025-10-12 13:36:29'),(53,17,'公众避险能力综合定权','PUBLIC_AVOIDANCE_TOTAL',15,'publicAvoidanceNorm * weight_L1_SELF_RESCUE * weight_L2_PUBLIC_AVOIDANCE',NULL,'publicAvoidanceTotal','公众避险能力（定权）=公众避险能力（属性向量归一化）*自救转移能力一级权重指标*公众避险能力二级权重指标',1,'2025-10-12 13:36:29'),(54,17,'转移安置能力综合定权','RELOCATION_CAPACITY_TOTAL',16,'relocationCapacityNorm * weight_L1_SELF_RESCUE * weight_L2_RELOCATION',NULL,'relocationCapacityTotal','转移安置能力（定权）=转移安置能力（属性向量归一化）*自救转移能力一级权重指标*转移安置能力二级权重指标',1,'2025-10-12 13:36:29'),(55,18,'灾害管理能力优解','DISASTER_MGMT_POSITIVE',1,'@TOPSIS_POSITIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted',NULL,'disasterMgmtPositive','灾害管理能力（优）=SQRT((队伍管理能力最大值-本乡镇队伍管理能力)^2+(风险评估能力最大值-本乡镇风险评估能力)^2+(财政投入能力最大值-本乡镇财政投入能力)^2)',1,'2025-10-12 13:36:29'),(56,18,'灾害管理能力差解','DISASTER_MGMT_NEGATIVE',2,'@TOPSIS_NEGATIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted',NULL,'disasterMgmtNegative','灾害管理能力（差）=SQRT((队伍管理能力最小值-本乡镇队伍管理能力)^2+(风险评估能力最小值-本乡镇风险评估能力)^2+(财政投入能力最小值-本乡镇财政投入能力)^2)',1,'2025-10-12 13:36:29'),(57,18,'灾害备灾能力优解','DISASTER_PREP_POSITIVE',3,'@TOPSIS_POSITIVE:materialReserveWeighted,medicalSupportWeighted',NULL,'disasterPrepPositive','灾害备灾能力（优）=SQRT((物资储备能力最大值-本乡镇物资储备能力)^2+(医疗保障能力最大值-本乡镇医疗保障能力)^2)',1,'2025-10-12 13:36:29'),(58,18,'灾害备灾能力差解','DISASTER_PREP_NEGATIVE',4,'@TOPSIS_NEGATIVE:materialReserveWeighted,medicalSupportWeighted',NULL,'disasterPrepNegative','灾害备灾能力（差）=SQRT((物资储备能力最小值-本乡镇物资储备能力)^2+(医疗保障能力最小值-本乡镇医疗保障能力)^2)',1,'2025-10-12 13:36:29'),(59,18,'自救转移能力优解','SELF_RESCUE_POSITIVE',5,'@TOPSIS_POSITIVE:selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted',NULL,'selfRescuePositive','自救转移能力（优）=SQRT((自救互救能力最大值-本乡镇自救互救能力)^2+(公众避险能力最大值-本乡镇公众避险能力)^2+(转移安置能力最大值-本乡镇转移安置能力)^2)',1,'2025-10-12 13:36:29'),(60,18,'自救转移能力差解','SELF_RESCUE_NEGATIVE',6,'@TOPSIS_NEGATIVE:selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted',NULL,'selfRescueNegative','自救转移能力（差）=SQRT((自救互救能力最小值-本乡镇自救互救能力)^2+(公众避险能力最小值-本乡镇公众避险能力)^2+(转移安置能力最小值-本乡镇转移安置能力)^2)',1,'2025-10-12 13:36:29'),(61,18,'综合减灾能力优解','TOTAL_POSITIVE',7,'@TOPSIS_POSITIVE:teamManagementTotal,riskAssessmentTotal,financialInputTotal,materialReserveTotal,medicalSupportTotal,selfRescueTotal,publicAvoidanceTotal,relocationCapacityTotal',NULL,'totalPositive','乡镇名称（优）=SQRT((队伍管理能力最大值-本乡镇队伍管理能力)^2+(风险评估能力最大值-本乡镇风险评估能力)^2+(财政投入能力最大值-本乡镇财政投入能力)^2+(物资储备能力最大值-本乡镇物资储备能力)^2+(医疗保障能力最大值-本乡镇医疗保障能力)^2+(自救互救能力最大值-本乡镇自救互救能力)^2+(公众避险能力最大值-本乡镇公众避险能力)^2+(转移安置能力最大值-本乡镇转移安置能力)^2)',1,'2025-10-12 13:36:29'),(62,18,'综合减灾能力差解','TOTAL_NEGATIVE',8,'@TOPSIS_NEGATIVE:teamManagementTotal,riskAssessmentTotal,financialInputTotal,materialReserveTotal,medicalSupportTotal,selfRescueTotal,publicAvoidanceTotal,relocationCapacityTotal',NULL,'totalNegative','乡镇名称（差）=SQRT((队伍管理能力最小值-本乡镇队伍管理能力)^2+(风险评估能力最小值-本乡镇风险评估能力)^2+(财政投入能力最小值-本乡镇财政投入能力)^2+(物资储备能力最小值-本乡镇物资储备能力)^2+(医疗保障能力最小值-本乡镇医疗保障能力)^2+(自救互救能力最小值-本乡镇自救互救能力)^2+(公众避险能力最小值-本乡镇公众避险能力)^2+(转移安置能力最小值-本乡镇转移安置能力)^2)',1,'2025-10-12 13:36:29'),(63,19,'灾害管理能力值','DISASTER_MGMT_SCORE',1,'disasterMgmtNegative / (disasterMgmtNegative + disasterMgmtPositive)',NULL,'disasterMgmtScore','灾害管理能力=灾害管理能力（差）/(灾害管理能力（差）+灾害管理能力（优）)',1,'2025-10-12 13:36:29'),(64,19,'灾害备灾能力值','DISASTER_PREP_SCORE',2,'disasterPrepNegative / (disasterPrepNegative + disasterPrepPositive)',NULL,'disasterPrepScore','灾害备灾能力=灾害备灾能力（差）/(灾害备灾能力（差）+灾害备灾能力（优）)',1,'2025-10-12 13:36:29'),(65,19,'自救转移能力值','SELF_RESCUE_SCORE',3,'selfRescueNegative / (selfRescueNegative + selfRescuePositive)',NULL,'selfRescueScore','自救转移能力=自救转移能力（差）/(自救转移能力（差）+自救转移能力（优）)',1,'2025-10-12 13:36:29'),(66,19,'综合减灾能力值','TOTAL_SCORE',4,'totalNegative / (totalNegative + totalPositive)',NULL,'totalScore','灾害管理能力=灾害管理能力（差）/(灾害管理能力（差）+灾害管理能力（优）)',1,'2025-10-12 13:36:29'),(67,19,'灾害管理能力分级','DISASTER_MGMT_GRADE',5,'@GRADE:disasterMgmtScore',NULL,'disasterMgmtGrade','灾害管理能力（分级）：基于均值μ和标准差σ进行五级分类',1,'2025-10-12 13:36:29'),(68,19,'灾害备灾能力分级','DISASTER_PREP_GRADE',6,'@GRADE:disasterPrepScore',NULL,'disasterPrepGrade','灾害备灾能力（分级）：基于均值μ和标准差σ进行五级分类',1,'2025-10-12 13:36:29'),(69,19,'自救转移能力分级','SELF_RESCUE_GRADE',7,'@GRADE:selfRescueScore',NULL,'selfRescueGrade','自救转移能力（分级）：基于均值μ和标准差σ进行五级分类',1,'2025-10-12 13:36:29'),(70,19,'综合减灾能力分级','TOTAL_GRADE',8,'@GRADE:totalScore',NULL,'totalGrade','乡镇（街道）减灾能力（分级）：基于均值μ和标准差σ进行五级分类',1,'2025-10-12 13:36:29');
/*!40000 ALTER TABLE `step_algorithm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `step_execution_result`
--

DROP TABLE IF EXISTS `step_execution_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `step_execution_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_record_id` bigint NOT NULL COMMENT '????ID',
  `step_id` bigint NOT NULL COMMENT '??ID',
  `region_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `step_input` text COLLATE utf8mb4_unicode_ci COMMENT '??????(JSON??)',
  `step_output` text COLLATE utf8mb4_unicode_ci COMMENT '??????(JSON??)',
  `execution_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `duration_ms` bigint DEFAULT NULL COMMENT '????(??)',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'SUCCESS' COMMENT '????(SUCCESS/FAILED)',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `step_id` (`step_id`),
  KEY `idx_step_result_execution` (`execution_record_id`),
  KEY `idx_step_result_region` (`region_code`),
  CONSTRAINT `step_execution_result_ibfk_1` FOREIGN KEY (`execution_record_id`) REFERENCES `model_execution_record` (`id`) ON DELETE CASCADE,
  CONSTRAINT `step_execution_result_ibfk_2` FOREIGN KEY (`step_id`) REFERENCES `model_step` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `step_execution_result`
--

LOCK TABLES `step_execution_result` WRITE;
/*!40000 ALTER TABLE `step_execution_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `step_execution_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `survey_data`
--

DROP TABLE IF EXISTS `survey_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `survey_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `region_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行政区代码',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省名称',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '市名称',
  `county` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '县名称',
  `township` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '乡镇名称',
  `population` bigint NOT NULL COMMENT '常住人口数量',
  `management_staff` int NOT NULL COMMENT '本级灾害管理工作人员总数',
  `risk_assessment` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否开展风险评估',
  `funding_amount` decimal(15,2) NOT NULL COMMENT '防灾减灾救灾资金投入总金额(万元)',
  `material_value` decimal(15,2) NOT NULL COMMENT '现有储备物资装备折合金额(万元)',
  `hospital_beds` int NOT NULL COMMENT '实有住院床位数',
  `firefighters` int NOT NULL COMMENT '消防员数量',
  `volunteers` int NOT NULL COMMENT '志愿者人数',
  `militia_reserve` int NOT NULL COMMENT '民兵预备役人数',
  `training_participants` int NOT NULL COMMENT '应急管理培训和演练参与人次',
  `shelter_capacity` int NOT NULL COMMENT '本级灾害应急避难场所容量',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_survey_data_region` (`region_code`),
  KEY `idx_survey_data_township` (`township`),
  KEY `idx_survey_data_create_time` (`create_time` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `survey_data`
--

LOCK TABLES `survey_data` WRITE;
/*!40000 ALTER TABLE `survey_data` DISABLE KEYS */;
INSERT INTO `survey_data` VALUES (1,'511425001','四川省','眉山市','青神县','青竹街道',102379,2,'是',20.00,9.00,1010,26,1126,182,280,500,'2025-09-23 08:38:54','2025-09-26 06:54:23',0),(2,'511425102','四川省','眉山市','青神县','汉阳镇',6335,2,'是',70.00,3.00,22,5,322,7,900,1200,'2025-09-23 08:38:54','2025-10-12 09:09:37',0),(3,'511425108','四川省','眉山市','青神县','瑞峰镇',8227,52,'是',63.00,20.00,36,0,373,24,1658,780,'2025-09-23 08:38:54','2025-10-12 09:09:37',0),(4,'511425110','四川省','眉山市','青神县','西龙镇',14051,2,'是',20.00,7.00,2211,0,81,55,320,500,'2025-09-23 08:38:54','2025-10-12 09:09:37',0),(5,'511425112','四川省','眉山市','青神县','高台镇',13786,4,'是',93.00,2.00,28,0,702,348,672,1500,'2025-09-23 08:38:54','2025-10-12 09:09:37',0),(6,'511425217','四川省','眉山市','青神县','白果乡',13523,2,'是',20.00,8.00,34,0,2,65,320,1000,'2025-09-23 08:38:54','2025-10-12 09:09:37',0),(7,'511425218','四川省','眉山市','青神县','罗波乡',9689,12,'是',150.00,10.00,30,0,94,106,300,5000,'2025-09-23 08:38:54','2025-10-12 09:09:37',0);
/*!40000 ALTER TABLE `survey_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weight_config`
--

DROP TABLE IF EXISTS `weight_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weight_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置描述',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weight_config`
--

LOCK TABLES `weight_config` WRITE;
/*!40000 ALTER TABLE `weight_config` DISABLE KEYS */;
INSERT INTO `weight_config` VALUES (1,'默认权重配置','系统默认的减灾能力评估指标权重配置','2025-09-23 08:38:54','2025-09-23 08:47:40',0);
/*!40000 ALTER TABLE `weight_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'evaluate_db'
--

--
-- Dumping routines for database 'evaluate_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-12 16:02:12
