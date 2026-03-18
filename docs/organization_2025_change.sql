-- ============================================
-- 2025 年乡镇组织机构数据变更 SQL
-- 生成时间：2026-03-12 23:37:00
-- 目标年份：2025
-- 基准年份：2020
-- ============================================

-- ============================================
-- 第一部分：备份 2020 年基准数据（只读，不执行变更）
-- ============================================

-- 备份 organization 表中的 2020 年乡镇数据
-- 执行以下命令可备份数据到文件：
-- mysqldump -u 用户名 -p 数据库名 organization --where="year=2020 AND level=4" > backup_organization_2020_township.sql

-- 备份 grassroots_organization 表中的 2020 年乡镇数据
-- mysqldump -u 用户名 -p 数据库名 grassroots_organization --where="year=2020 AND level=4" > backup_grassroots_organization_2020_township.sql

-- ============================================
-- 2020 年 organization 表备份数据（乡镇级别）
-- ============================================
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021126', '南薰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132212', '勒乌乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021127', '思贤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021129', '清流镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021122', '卧佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021123', '长河源镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021124', '忠义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922229', '神门乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021125', '护建镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302011', '西山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302012', '搬罾街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021120', '驯龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021121', '华严镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723211', '大兴回族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132211', '金岩乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325203', '祝桑乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325204', '米龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325201', '八角楼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302010', '荆溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325202', '普巴绒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132209', '平等乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325208', '牙衣河乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325205', '八衣绒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021119', '周礼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021115', '兴隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021116', '天林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021117', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021118', '文化镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021111', '两板桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021112', '护龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021113', '李家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021114', '元坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021110', '石羊镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325211', '德差乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325214', '瓦多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325215', '木绒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325213', '柯拉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021108', '永清镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021109', '永顺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922204', '团结乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021104', '龙台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232103', '辖曼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021105', '姚市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232104', '巴西镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021106', '林凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232101', '唐克镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337201', '省母乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021107', '毛家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232102', '红星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021100', '岳阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021101', '鸳大镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232105', '阿西镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337205', '邓坡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021103', '通贤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232106', '铁布镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337204', '巨龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337207', '赤土乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337206', '木拉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337209', '蒙自乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232100', '达扎寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301211', '呷巴乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301214', '孔玉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337210', '各卡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132201', '宜坪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337212', '俄牙同乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132206', '杨河乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132208', '新场乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723222', '莲花湖乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301200', '雅拉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301208', '普沙绒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301209', '吉居乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301204', '麦崩乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301206', '捧塔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503107', '白节镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503108', '天仙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503109', '新乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503103', '上马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503104', '合面镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503106', '丰乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503100', '大渡口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503101', '护国镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503102', '打古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702110', '东岳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781124', '西屏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702111', '梓桐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702112', '北山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781122', '大堰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702113', '金石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781125', '方水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503111', '龙车镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702114', '青宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781112', '永胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781113', '小溪坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781110', '战旗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181200', '龙门乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781111', '双河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702102', '罗江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781118', '马角镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781119', '雁门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781116', '厚坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781117', '二郎庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781114', '河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781115', '重华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311100', '沿滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311102', '兴隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311105', '富全镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311106', '永安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702107', '江陵镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311107', '联络镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702108', '碑庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702109', '磐石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311109', '王井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702103', '蒲家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702104', '复兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702105', '双龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781101', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781102', '三合镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781109', '新安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781107', '武都镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781108', '大康镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781105', '彰明镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781106', '龙凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781103', '含增镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781104', '青莲镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311110', '黄市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311111', '瓦市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311112', '仙市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424115', '热河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424114', '铁炉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424113', '黑龙潭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436105', '候播乃拖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436106', '候古莫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436103', '牛牛坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436104', '拉马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436101', '洪溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436102', '新桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436100', '巴普镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621002', '朝阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621001', '九龙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424103', '麻栗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424102', '乐跃镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424101', '永郎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424109', '巴洞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424106', '茨达镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903203', '唐家乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822109', '三锅镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822108', '姚渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822107', '沙州镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822106', '木鱼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822105', '竹园镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822104', '凉水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822103', '关庄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822102', '房石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822101', '青溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822100', '乔庄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822112', '乐安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822111', '建峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903108', '茶坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903106', '柳林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903107', '下八庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903105', '花丛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903102', '渔溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903100', '明阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903101', '玉山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903119', '九镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903118', '尹家��', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704105', '沉抗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704104', '魏城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704102', '新桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704101', '石马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704106', '忠兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903115', '群乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903113', '兴隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903114', '双胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903111', '上八庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903112', '关公镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704112', '松垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704124', '盐泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704123', '仙鹤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704122', '信义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704121', '小枧镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503002', '罗龙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503001', '南溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011113', '永兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011114', '平坦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433112', '里庄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433113', '惠安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503003', '仙源街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433114', '宏模镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011117', '双桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011118', '富溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433110', '锦屏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433115', '泽远镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011110', '椑木镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433116', '若水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011111', '石子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433117', '棉沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433118', '磨房沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521001', '玉蟾街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423112', '七里坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423113', '将军镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423110', '高庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423111', '瓦屋山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011120', '永福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423114', '中山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304006', '凤凰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321111', '楠木镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304005', '和平街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321112', '长坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304004', '凉高山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321113', '东坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304003', '马冲口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321114', '河坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321110', '富利镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321119', '建兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321115', '定水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321116', '大王镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321117', '黄金镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321118', '流马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423109', '柳江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682120', '师古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423107', '东岳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331103', '盖玉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331102', '河坡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331101', '阿察镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423101', '止戈镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331100', '建设镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423100', '洪川镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304002', '龙井街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423105', '槽渔滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304001', '大安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423106', '中保镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (141, '511423103', '余坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903120', '雪山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226220', '毛日乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321102', '老鸦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321103', '永定镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321108', '石河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321109', '王家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321104', '碑院镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321105', '谢河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321106', '盘龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321107', '铁佛塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433101', '漫水湾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433102', '大桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433103', '复兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433109', '河边镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433104', '泸沽镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433106', '彝海镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433107', '石龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321133', '西水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321134', '桐坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321130', '神坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921001', '普安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321131', '八尔湖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723100', '新宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321132', '石龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723101', '普安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823229', '坭美彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823228', '片马彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823225', '晒经乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823224', '河南乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823227', '小堡藏族彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823220', '马烈乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723102', '回龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682106', '禾丰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723104', '永兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682105', '洛水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723105', '讲治镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682108', '马祖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723106', '甘棠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723107', '任市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723108', '广福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723109', '长岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321122', '双佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321123', '花罐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321124', '大桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321125', '大河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131101', '大塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723110', '八庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321120', '三官镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723111', '灵岩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321121', '伏虎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131104', '西来镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131105', '大兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131103', '朝阳湖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321126', '万年镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321127', '升钟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131106', '甘溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321128', '升水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131107', '成佳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321129', '大坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682111', '蓥华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682110', '马井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002001', '莲花街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682113', '南泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002002', '三贤祠街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002003', '资溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002004', '管理委员会狮子山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002005', '宝莲街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682116', '湔氐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823231', '顺河彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823230', '永利彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322200', '岚安乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181103', '九里镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181102', '罗目镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181101', '高桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324202', '铜鼓乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181100', '绥山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324207', '凤仪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702208', '安云乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181112', '黄湾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181110', '大为镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334201', '哈依乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118104', '宝墩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524114', '向林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334203', '莫坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118103', '永商镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524113', '观兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334204', '亚火乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334205', '绒坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334206', '呷洼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524118', '大石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334207', '奔戈乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524117', '麻城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334208', '村戈乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334209', '禾尼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524119', '黄坭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524110', '赤水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118102', '安西镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524112', '正东镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118101', '兴义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524111', '龙凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181109', '桂花桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181108', '双福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181106', '符溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181104', '龙池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324226', '板桥乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334210', '曲登乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334213', '上木拉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334216', '濯桑乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334218', '藏坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334219', '格木乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334221', '麦洼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324212', '来仪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334222', '德巫乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324210', '福临乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183115', '临济镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324241', '柴井乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183105', '夹关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183106', '火井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183103', '桑园镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183104', '平乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324230', '芭蕉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115001', '柳城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524103', '天池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115002', '公平街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524102', '马岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115003', '涌泉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524105', '两河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115004', '天府街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524104', '水尾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115005', '金马街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524107', '后山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115006', '永宁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524106', '落卜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524109', '摩尼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524108', '分水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524101', '江门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524100', '叙永镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302008', '和平路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302009', '潆溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183122', '大同镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021133', '大平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302001', '中城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021130', '协和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302002', '北城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021131', '朝阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302003', '西城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021132', '乾龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302004', '东南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183120', '南宝山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302005', '舞凤街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302006', '新建街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302007', '华凤街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011102', '高梁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011103', '白合镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011104', '顺河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011107', '双才镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011109', '杨家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183118', '天台山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011100', '田家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011101', '郭北镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021001', '岳城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021002', '石桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781219', '枫顺乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311204', '九洪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426001', '鱼城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426002', '金江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301101', '姑咱镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301102', '新都桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301103', '塔公镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301108', '鱼通镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301104', '沙德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301105', '金汤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301106', '甲根坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921001', '壁州街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301107', '贡嘎山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702001', '东城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702002', '西城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702003', '朝阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702004', '凤西街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (64, '511702005', '凤北街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223107', '土门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223109', '洼底镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223105', '富顺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223100', '凤仪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223102', '叠溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223101', '南新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (52, '510113104', '城厢镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (52, '510113102', '弥牟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (52, '510113108', '清泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (52, '510113106', '姚渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503001', '安富街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503002', '永宁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (137, '510503003', '东升街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602007', '枣山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602006', '中桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (52, '510113111', '福洪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602005', '万盛街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602004', '广福街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602002', '北辰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602001', '浓洄街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621112', '天平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621111', '苟角镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621110', '顾县镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621117', '中和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621116', '裕民镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724231', '川主乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621115', '罗渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621114', '乔家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725111', '双板镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725110', '仁和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621119', '普安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621118', '新场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725114', '演武镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725113', '文兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725112', '金龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422102', '渔门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327103', '虾拉沱镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422101', '红格镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725118', '宏仁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422100', '桐子林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327101', '朱倭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327100', '新都镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436225', '峨曲古乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422105', '惠民镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422104', '新九镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422103', '永兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327104', '上罗柯马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436221', '柳洪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436229', '龙门乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621124', '伏龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621123', '齐福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621122', '西板镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621121', '临溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725100', '文昌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725103', '黎雅镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725102', '许州镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725101', '长卿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725108', '石牛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725107', '玛瑙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725106', '观义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436235', '瓦候乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725105', '卧龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436231', '洒库乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725109', '自强镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402121', '复兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822225', '观音店乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822224', '骑马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (171, '510403002', '玉泉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (171, '510403001', '清香坪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823209', '富乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436202', '合姑洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (171, '510403006', '大宝鼎街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436200', '觉洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223114', '赤不苏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436201', '井叶特西乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (171, '510403004', '陶家渡街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (171, '510403003', '河门口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223111', '渭门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223110', '沙坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223113', '沟口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822220', '七佛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (30, '513223112', '黑虎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436208', '典补乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621102', '坪滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621101', '花园镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621106', '酉溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621105', '白庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621104', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621103', '龙孔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621109', '秦溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621108', '兴隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621107', '同兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436217', '九口乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (122, '513436218', '洛俄依甘乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822207', '蒿溪回族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822203', '茶坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226209', '卡拉脚乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402109', '思蒙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226207', '集沐乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226208', '撒瓦脚乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226205', '河东乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226206', '河西乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402105', '多悦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402104', '尚义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402107', '万胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226201', '庆宁乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402106', '秦家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226202', '咯尔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226200', '沙耳乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402102', '太和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402110', '修文镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822216', '大院回族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226216', '曾达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226217', '独松乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822212', '石坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402116', '三苏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402115', '永寿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226215', '卡撒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (101, '510822210', '曲河乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226212', '二嘎里乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226213', '阿科里乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402112', '松江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226210', '俄热乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402114', '富牛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402113', '崇礼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424218', '金沙傈僳族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424217', '南山傈僳族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304105', '新店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304104', '何市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304103', '三多寨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304102', '团结镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304109', '回龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304108', '庙坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304107', '牛佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304106', '新民镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331207', '赠科乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331206', '登龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331205', '热加乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331203', '麻绒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331202', '章都乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331201', '绒盖乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433230', '新兴乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331200', '金沙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433231', '健美乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (200, '510304100', '大山铺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331209', '麻邛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331214', '沙马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331212', '安孜乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331211', '纳塔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (74, '513331210', '辽西乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682001', '方亭街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (71, '510682002', '皂角街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226104', '马奈镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226102', '安宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226103', '勒乌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (26, '513226101', '观音桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131001', '鹤山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (51, '510131002', '寿安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527101', '腾达镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527100', '筠连镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527102', '巡司镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527104', '沐爱镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527107', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527109', '大雪山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527108', '蒿坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433226', '和爱藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106030', '驷马桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106031', '茶店子街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106035', '九里堤街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106032', '抚琴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823108', '唐家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823107', '皇木镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823109', '富泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823104', '富庄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823103', '宜东镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823106', '大树镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823105', '清溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823100', '富林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823102', '乌斯河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823101', '九襄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106038', '营门口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106036', '五块石街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106041', '沙河源街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321001', '滨江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106042', '天回镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321002', '蜀北街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321003', '满福街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106040', '金泉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321004', '南隆街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106043', '凤凰山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723001', '淙城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503105', '汪家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503104', '大观镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503103', '江南镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503102', '刘家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503109', '裴石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503108', '长兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503107', '仙临镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (190, '511503106', '黄沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106024', '西安路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118004', '花源街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (108, '511113101', '金河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (108, '511113100', '永和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823111', '前域镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (179, '511823110', '安乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118001', '五津街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118003', '花桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (59, '510118002', '普兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106027', '荷花池街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (45, '510106025', '西华街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324105', '土门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324106', '复兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324103', '永乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230201', '宗科乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324104', '日兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230200', '蒲西乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921102', '文井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324101', '新政镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230203', '吾伊乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921101', '新会镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324102', '马鞍镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230202', '石里乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921100', '赤城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230205', '上杜柯乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324100', '金城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324109', '三蛟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324107', '观紫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324108', '先锋镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430100', '天地坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921106', '红江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921105', '天福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430103', '对坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921104', '常乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430102', '芦稿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921103', '明月镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430101', '派来镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921109', '吉祥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421200', '虞丞乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921108', '大石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921107', '宝梵镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421202', '青岗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230210', '上壤塘乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921112', '三凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921111', '任隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921110', '鸣凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921117', '金桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230206', '茸木达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921115', '群利镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921114', '蓬南镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230208', '尕多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921118', '槐花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324127', '双胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324125', '保平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324126', '文星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324124', '杨桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324121', '张公镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324122', '五福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324129', '永光镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324120', '大仪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324116', '大寅镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324117', '二道镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324114', '三河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324115', '瓦子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324112', '义路镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324113', '立山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324110', '回春镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324111', '柳垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826208', '宝盛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324118', '赛金镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324119', '丁字桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429104', '乐安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429103', '九都镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429106', '地洛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429105', '俄里坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722101', '潼川镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429107', '牛角湾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722104', '塔山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722108', '富顺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429100', '特木里镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722109', '三元镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722106', '龙树镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429102', '拖觉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722107', '石安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429101', '龙潭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183002', '文君街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183003', '固驿街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183001', '临邛街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183006', '孔明街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183004', '羊安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (54, '510183005', '高埂街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303211', '佛门乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301001', '榆林街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (82, '513301002', '炉城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324100', '呷尔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324103', '雪洼龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324104', '湾坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324101', '烟袋镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324102', '三垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324130', '思德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324107', '魁多镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324108', '乃渠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324105', '汤古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324106', '乌拉溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303108', '胜观镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303103', '江陵镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722123', '西平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303104', '擦耳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722120', '中新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303107', '长乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722121', '古井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303106', '东观镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722126', '乐安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722127', '建平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722124', '八洞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321125', '东兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321126', '铁厂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321123', '观山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321124', '高山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321121', '来牟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321122', '双古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321120', '留佳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022204', '双河场乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303112', '青居镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303111', '石圭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722112', '新德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303115', '会龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722110', '秋林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303117', '走马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722115', '景福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722116', '紫河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722113', '新生镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722114', '鲁班镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722119', '郪江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303110', '阙家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722118', '观桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185115', '三合镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185114', '施家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185113', '石钟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185112', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185119', '江源镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185118', '踏水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185116', '平武镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722140', '永明镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824127', '黄猫垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722141', '建中镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824128', '河地镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722145', '老马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824120', '运山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824121', '东溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824122', '高坡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321107', '鼎新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824123', '龙山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824125', '亭子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824126', '百利镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321102', '双石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321100', '旭阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185101', '杨家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185107', '三星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185106', '云龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185105', '禾丰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722133', '新鲁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722131', '中太镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722132', '金石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722137', '芦溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722138', '立新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722135', '刘营镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722136', '灵兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321118', '长山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321119', '保华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321116', '度佳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321117', '东佳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321114', '新桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321115', '正紫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321112', '古文镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321113', '河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321110', '乐德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602113', '大安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824105', '白桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824107', '五龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824108', '永宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824109', '鸳溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185132', '海螺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185131', '董家埂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185130', '雷家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824100', '陵江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437237', '巴姑乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824102', '云峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824104', '东青镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437233', '莫红乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602112', '白市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602111', '石笋镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602110', '恒升镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185126', '高明镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602109', '肖溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602108', '龙台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185124', '青龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602107', '花桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602106', '井河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602105', '兴平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425234', '槽元乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185129', '宏缘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602104', '悦来镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185128', '壮溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602103', '浓溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185127', '武庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602102', '协兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722151', '北坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824116', '漓江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437241', '卡哈洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824117', '文昌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824118', '岳东镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824119', '石马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185121', '芦葭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185120', '涌泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824110', '三川镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824111', '龙王镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525122', '白泥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824112', '元坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525121', '黄荆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824113', '唤马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824114', '歧坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824115', '白驿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602101', '官盛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411104', '同德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411103', '福田镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411106', '布德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411105', '技术产业开发区金江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411100', '仁和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812101', '大滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812100', '朝天镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411102', '大田镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812103', '曾家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201200', '梭磨乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411101', '平地镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812102', '羊木镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201201', '白湾乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201202', '党坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201203', '木尔宗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201204', '脚木足乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201206', '龙尔甲乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411107', '前进镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201207', '大藏乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201208', '康山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201209', '草登乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (153, '510725217', '宝石乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437217', '桂花乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437214', '柑子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222102', '古尔沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222103', '薛城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222104', '桃坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222105', '朴头镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812105', '沙河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812104', '中子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222100', '杂谷脑镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222101', '米亚罗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812109', '两河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922001', '集州街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812110', '云雾山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812112', '李家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812111', '水磨沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437220', '山棱岗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437227', '千万贯乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437224', '拉咪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437222', '谷堆乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602123', '东岳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602122', '大龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602121', '穿石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112115', '山泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425202', '内东乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822216', '宝峰彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123107', '龙孔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123106', '芭沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123105', '罗城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123108', '定文镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123103', '清溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123102', '石溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123101', '孝姑镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123100', '玉津镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111107', '踏水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111108', '轸溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111103', '福禄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111104', '牛石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822209', '泗坪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201210', '日部乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111106', '葫芦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111100', '沙湾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111101', '嘉农镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111102', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822203', '安靖乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123118', '双溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123117', '寿保镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123116', '铁炉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437203', '箐口乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123115', '九井镇九井街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225209', '郭元乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225205', '保华乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225203', '白河乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225201', '永和乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123114', '大兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123113', '玉屏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (107, '511123112', '舞雩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621212', '黄龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (96, '511621216', '鱼峰乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781124', '固军镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781125', '黑宝山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425221', '新安傣族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781120', '井溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781121', '鹰背镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781123', '永宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402002', '大石桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402001', '通惠街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (140, '511402003', '苏祠街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112102', '洛带镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328100', '甘孜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112108', '洪安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425219', '树堡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328101', '查龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328102', '来马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725002', '渠南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725001', '渠江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725003', '天星街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703003', '工区街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703001', '城厢街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703009', '创业园街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703006', '普明街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703013', '塘汛街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703011', '石塘街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401203', '四合乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703010', '城郊街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401215', '经久乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401219', '裕隆回族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401212', '大兴乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401211', '开元乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434102', '新民镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434101', '中所镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434104', '普雄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434103', '乃托镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434100', '越城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225213', '玉瓦乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225214', '大录乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434109', '南箐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434106', '竹阿觉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225210', '草地乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434105', '大瑞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434108', '依洛地坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434107', '书古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401228', '马鞍山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401220', '高草回族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434113', '拉普镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434112', '尔觉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121002', '官仓街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434115', '大花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121003', '栖贤街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434114', '马拖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434111', '梅花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434110', '贡莫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121004', '高板街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121005', '白果街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434116', '板桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121006', '淮口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522108', '凤鸣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522109', '榕山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422213', '西秋乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522101', '望龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522102', '白沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422218', '卡拉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422219', '后所乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522104', '先市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527211', '丰乐乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422216', '三桷桠乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522105', '尧坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527210', '高坪苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422217', '倮波乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522106', '九支镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422214', '克尔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422215', '白碉苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121110', '福兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121111', '金龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121112', '赵家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121113', '竹篙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121118', '云合镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121119', '又新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121116', '转龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121117', '土桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422201', '博科乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422202', '宁朗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422209', '李子坪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422207', '屋脚蒙古族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422208', '项脚蒙古族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422206', '牦牛坪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422203', '依吉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422204', '俄亚纳西族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121100', '赵镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434211', '保安藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527205', '乐义乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126108', '华头镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527208', '团林苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102118', '剑峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126107', '木城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102117', '悦来镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102116', '平兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (194, '511527209', '联合苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121109', '三溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126106', '吴场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126102', '甘江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (46, '510121106', '五凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126101', '黄土镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (139, '511424204', '顺龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923001', '盐井街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522124', '石龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522125', '真龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522126', '荔江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522121', '神臂城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522119', '法王寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422223', '麦日乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422224', '东朗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422222', '固增苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422220', '沙湾乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722001', '东乡街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522111', '甘雨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522112', '福宝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522113', '先滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825219', '兴业乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522115', '大桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522116', '车辋镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522117', '白米镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422225', '唐央乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825216', '新华乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422226', '博窝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332208', '格孟乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332207', '呷衣乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332206', '长沙贡马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332204', '德荣马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825210', '乐英乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332202', '正科乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522110', '白鹿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332201', '奔达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722002', '蒲江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332200', '真达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781228', '紫溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322225', '悦中乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603117', '和新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322221', '柏林乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781226', '庙子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332219', '瓦须乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332217', '长须干马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332216', '长沙干马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332215', '长须贡马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332213', '起坞乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421142', '谢安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332211', '宜牛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332210', '新荣乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603110', '双东镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421144', '藕塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421143', '新店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421146', '贵平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603111', '新中镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421145', '板桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322212', '木顶乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322219', '太蓬乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322216', '明德乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781207', '蜂桶乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421128', '曹家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431110', '俄尔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117020', '西园街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102109', '棉竹镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102108', '安谷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102107', '水口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102106', '苏稽镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228208', '洛多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102105', '青平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228207', '龙坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102104', '茅桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421120', '满井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228206', '石碉楼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102103', '白马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102102', '土主镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228204', '瓦钵梁子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102101', '牟子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421121', '黑龙滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421126', '珠嘉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421125', '宝马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781216', '玉带乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322230', '大庙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129001', '晋原街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603108', '德新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129002', '沙渠街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322232', '安化乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129003', '青霞街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781210', '曾家乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322237', '清水乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421117', '方家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421116', '龙马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421111', '北斗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421110', '龙正镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228214', '慈坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603101', '孝泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228213', '晴朗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603100', '黄许镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421112', '禾加镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603103', '柏隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421115', '宝飞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421114', '禄加镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228210', '维古乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117004', '安德街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525214', '大寨苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117006', '犀浦街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117005', '红光街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117008', '安靖街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117007', '德源街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117009', '团结街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421106', '钟祥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421105', '汪洋镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421108', '彰加镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421107', '始建镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421109', '慈航镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525212', '箭竹苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117001', '郫筒街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129207', '高笋乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129208', '茨竹乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129205', '底堡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129206', '杨村乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421102', '文宫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421101', '大化镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921207', '荷叶乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421104', '富加镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421103', '高家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129210', '武圣乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524107', '古河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524106', '老翁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524105', '竹海镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117019', '合作街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524104', '花滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524103', '硐底镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525209', '马嘶苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524102', '双河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524101', '梅硐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524100', '长宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431100', '新城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431101', '城北镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431106', '三岔河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431107', '四开镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431108', '地莫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (176, '510921213', '高升乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431109', '古里镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431102', '竹核镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431103', '谷曲镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431104', '比尔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431105', '解放沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182002', '隆丰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182001', '天彭街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182004', '致和街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182003', '濛阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524119', '梅白镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524113', '铜鼓镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524112', '井江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524110', '龙头镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105005', '少城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105002', '草市街街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105003', '西御河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105008', '府南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105009', '光华街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105007', '草堂街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105011', '金沙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105012', '黄田坝街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (195, '511524120', '铜锣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105013', '苏坡街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105014', '文家街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105019', '康河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (53, '510105018', '蔡桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302003', '新街街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302004', '郭家坳街街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302001', '五星街街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302002', '东兴寺街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302007', '舒坪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302008', '红旗街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302005', '丹桂街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302006', '学苑街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (161, '511324001', '度门街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302009', '高峰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722204', '断石乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (150, '510722201', '忠孝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803001', '永兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803002', '蒙阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126111', '马村镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126110', '新场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922105', '大河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427109', '骑骡沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922106', '光雾山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922103', '长赤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922104', '正直镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922109', '赶场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922108', '下两镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427101', '松新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115101', '和盛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427102', '竹寿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427103', '华弹镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427104', '白鹤滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115105', '万春镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427106', '西瑶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (56, '510115106', '寿安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427108', '大同镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922101', '沙河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132103', '五渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132102', '毛坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132105', '黑竹沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132104', '新林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132106', '红旗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132101', '大堡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (104, '511132100', '沙坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623116', '兴仁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303009', '螺溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623117', '王家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623118', '石滓镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602216', '白马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623119', '三古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623112', '袁市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623113', '丰禾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623114', '八耳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623115', '石永镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303002', '清溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303001', '白塔街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303004', '龙门街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303003', '小龙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303006', '都京街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303005', '青莲街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (154, '511303008', '老君街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802011', '万缘街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325100', '河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325101', '呷拉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802010', '南河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325104', '麻郎措镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325105', '波斯河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325102', '西俄洛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (90, '513325103', '红龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904001', '柔刚街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904002', '凤凰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623110', '九龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623111', '御临镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602208', '彭家乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (91, '511602207', '龙安乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623123', '梁板镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623124', '复盛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623125', '黎家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427112', '石梨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802009', '上西街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427113', '六铁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802007', '河西街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427116', '宁远镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427117', '俱乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802001', '东坝街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802004', '雪峰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802002', '嘉陵街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427110', '跑马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (125, '513427111', '幸福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623120', '两河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623121', '太和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623122', '椿木镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185016', '玉成街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337100', '金珠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185015', '福田街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185014', '草池街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337102', '桑堆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185013', '三岔街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337101', '香格里拉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337104', '噶通镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (77, '513337103', '吉呷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185017', '丹景街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824226', '白山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824228', '彭店乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824229', '桥溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185012', '石板凳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185011', '贾家街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185010', '养马街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311001', '板仓街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (204, '510311002', '邓关街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824223', '月山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185005', '东溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185003', '新市街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185002', '射洪坝街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185009', '石盘街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185008', '赤水街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185007', '石桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185006', '平泉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (197, '510185001', '简城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525001', '彰德街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525003', '永乐街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525002', '金兰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922130', '贵民镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922131', '关路镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922132', '云顶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922133', '公山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922127', '石滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922128', '高桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922125', '兴马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922126', '关门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824208', '白鹤乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (97, '510824209', '浙水乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681001', '双河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922120', '和平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922123', '高塔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681003', '华龙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922121', '侯家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681002', '古桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922122', '仁和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002007', '乐贤街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922116', '八庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922117', '双流镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002005', '牌楼街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922114', '元潭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002006', '壕子口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922115', '赤溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922118', '坪河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922119', '桥亭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002003', '城西街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002004', '玉溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504107', '金龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002001', '城东街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504106', '双加镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504103', '胡市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922112', '关坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922113', '红光镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922110', '杨坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (38, '511922111', '天池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201100', '马尔康镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201102', '松岗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (29, '513201103', '沙尔宗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112005', '西河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112004', '十陵街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112003', '同安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112002', '大面街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112001', '龙泉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112007', '东安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (48, '510112006', '柏合街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328204', '贡隆乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328205', '扎科乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328207', '昔色乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328200', '呷拉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328201', '色西底乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328202', '南多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328203', '生康乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812215', '临溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (98, '510812214', '麻柳乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328208', '卡攻乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328209', '仁果乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703103', '青义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781001', '古东关街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328210', '拖坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703100', '丰谷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328215', '夺多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328216', '泥柯乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328217', '茶扎乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328218', '大德乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703107', '杨家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328212', '庭卡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328213', '下雄乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703106', '吴家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328214', '四通达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (80, '513328219', '卡龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424002', '昌州街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (116, '513424001', '德州街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822107', '五宪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822106', '荥河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703113', '永兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (147, '510703110', '新皂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822103', '牛背山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822102', '龙苍沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822105', '青龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822104', '新添镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822101', '花滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821119', '大两镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821117', '米仓山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821118', '大德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821115', '龙凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821116', '九龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821113', '英萃镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821114', '国华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821111', '高阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821112', '双汇镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821110', '五权镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623105', '观音桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623106', '牟家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623107', '合流镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623108', '坛同镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623101', '城北镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623102', '城南镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623103', '柑子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623109', '高滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821122', '天星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821120', '水磨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821121', '盐河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (93, '511623100', '鼎屏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821108', '三江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821106', '黄洋镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821107', '普济镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821105', '张华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821102', '木门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821103', '白水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727103', '响岩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727100', '龙安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727101', '古城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434236', '申果庄乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981001', '太和街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (130, '513434235', '拉吉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727107', '大桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981002', '平安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727108', '水晶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727109', '江油关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225105', '勿角镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821100', '东河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (139, '511424102', '仁美镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225103', '双河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821101', '嘉川镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225104', '黑河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225101', '漳扎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (139, '511424101', '杨场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (27, '513225102', '南坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (139, '511424104', '张场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (139, '511424105', '齐乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923101', '隆盛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923102', '回马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923103', '天保镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923104', '河边镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108015', '白莲池街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108014', '龙潭街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108013', '青龙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108012', '保和街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923100', '蓬莱镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683110', '广济镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683112', '玉泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683115', '新市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683116', '孝德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923105', '卓筒井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923106', '玉峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683119', '富新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923107', '象山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (175, '510923108', '金元镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821207', '檬子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (102, '510821204', '燕子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011003', '新江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (123, '513433001', '高阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011004', '胜利街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011005', '高桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683124', '清平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683122', '什地镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011001', '东兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (163, '511011002', '西林街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522001', '符阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (133, '510522002', '临港街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825105', '仁义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825106', '新场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825103', '喇叭河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825104', '小河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825101', '始阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825102', '思经镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (183, '511825100', '城厢镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903003', '司城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (62, '511723205', '梅家乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903001', '登科街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (37, '511903002', '文治街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108003', '双水碾街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108002', '猛追湾街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108001', '府青路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108008', '跳蹬河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683106', '汉旺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108006', '双桥子街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683104', '九龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108005', '万年场街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (41, '510108009', '二仙桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683109', '麓棠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322102', '东升镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781106', '竹峪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322101', '渌井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781107', '大竹镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322104', '黄渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781108', '黄钟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322103', '骆市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781109', '官渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781102', '旧院镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781103', '罗文镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781104', '河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781105', '草坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322109', '消水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781100', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781101', '青花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322106', '灵鹫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322105', '小桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322108', '木垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322107', '老林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228107', '扎窝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228106', '知木林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228105', '沙石多镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228104', '木苏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228103', '西尔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228102', '色尔古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228101', '卡龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (24, '513228100', '芦花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603006', '八角井街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781117', '魏家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781118', '白果镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781119', '长坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781113', '八台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603009', '东湖街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781114', '石塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781115', '铁矿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781116', '大沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781110', '白沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781111', '沙滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (65, '511781112', '石窝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (184, '511822001', '严道街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502008', '白沙湾街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502006', '西郊街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502007', '安阜街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603001', '旌阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603004', '旌东街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322122', '青山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322121', '望龙湖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502011', '沙坪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502012', '合江门街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502010', '象鼻街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431231', '补约乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502013', '大观楼街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502014', '双城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431238', '则普乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421001', '文林街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421003', '怀仁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431234', '金曲乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421002', '普宁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (144, '511421004', '视高街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322113', '蓼叶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322115', '回龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322114', '新店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322111', '绿水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322110', '双流镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322117', '西桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322116', '星火镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431244', '日哈乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431245', '哈甘乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411203', '太平乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411205', '中坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411204', '务本乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411201', '啊喇彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411200', '大龙潭彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002110', '交通镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002113', '龙门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431210', '博洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002106', '靖民镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002104', '永安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002105', '全安镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002102', '凌家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002103', '朝阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002100', '白马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (165, '511002101', '史家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322100', '泸桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322102', '兴隆镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322101', '冷碛镇冷碛镇老街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322104', '燕子沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431227', '特布洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322103', '磨西镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431228', '庆恒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322106', '烹坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322105', '得妥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (84, '513322107', '德威镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302103', '荣边镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181002', '峨山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302101', '仲权镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (105, '511181001', '胜利街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (205, '510302104', '飞龙峡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525115', '皇华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525117', '东新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525119', '马蹄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525118', '椒园镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525111', '德耀镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525110', '双沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525113', '石屏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603011', '孝感街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (68, '510603010', '天元街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334100', '高城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334101', '甲洼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334102', '格聂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334103', '木拉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525104', '二郎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334104', '君坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525103', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334105', '拉波镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525106', '石宝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (83, '513334106', '觉吾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525105', '大村镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525108', '茅溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525107', '丹桂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525109', '观文镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (132, '510525101', '龙山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (131, '513431205', '美甘乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232206', '嫩哇乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232205', '麦溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232214', '求吉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325203', '占山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232211', '降扎乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232215', '包座乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (32, '513232210', '占哇乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022116', '高寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022117', '龙门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022118', '盛池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022112', '佛星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022113', '蟠龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325232', '罐垭乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022114', '东山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022115', '通旅镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022110', '劳动镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022111', '中天镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022109', '中和场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325227', '车龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022105', '宝林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022106', '大佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022107', '良安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325226', '祥龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022108', '金顺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022101', '石佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022102', '回澜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022103', '石湍镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022104', '童家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022100', '天池街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325229', '东太乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184003', '三江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184004', '江源街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184001', '崇阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184002', '羊马街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184005', '大划街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184006', '崇庆街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703001', '三里坪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703002', '翠屏街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703004', '管理委员会斌郎街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504009', '特兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802105', '三堆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504006', '鱼塘街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504005', '罗汉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504008', '石洞街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504007', '安宁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504001', '小市街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802100', '荣山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504004', '莲花池街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (135, '510504003', '红星街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802103', '宝轮镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802101', '大石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802102', '盘龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703005', '明月江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703006', '杨柳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304215', '盐溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114116', '军屯镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904110', '三家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304220', '大兴乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904111', '玉丰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904112', '西眉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904113', '磨溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904114', '聚贤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322200', '龙万乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904116', '常理镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923118', '灵山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923115', '得胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425003', '古城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425002', '城南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923119', '土兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425001', '城北街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681108', '溪口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923110', '元山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681106', '高兴镇高兴镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681105', '阳和镇阳和镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681104', '明月镇明月镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923113', '笔山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681103', '永兴镇永兴镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923114', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681102', '禄市镇禄市镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923111', '云台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681101', '天池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923112', '邱家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923106', '岳家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923107', '兰草镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114109', '清流镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923104', '白衣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923105', '涵水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923108', '驷马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326102', '亚卓镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326101', '八美镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326100', '鲜水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326106', '泰宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326105', '仲尼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326104', '玉科镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326107', '瓦日镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923102', '西兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923103', '佛楼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923101', '响滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338102', '白松镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338103', '日雨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338101', '瓦卡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338104', '太阳谷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681109', '庆华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403110', '锦江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529110', '屏山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224105', '红土镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224104', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224106', '小河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403118', '公义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224101', '川主寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224100', '进安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224103', '毛儿盖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224102', '青云镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403121', '黄丰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401007', '海南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401008', '马道街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083108', '石碾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529100', '锦屏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083107', '界市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529101', '新市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401001', '西城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083105', '龙市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529104', '大乘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401003', '北城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401002', '东城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529102', '中都镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401005', '新村街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529103', '龙华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401004', '长安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083111', '石燕桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529109', '书楼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529107', '新安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083115', '普润镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083114', '云顶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083113', '胡家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725215', '报恩乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725216', '安北乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725220', '大义乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725224', '巨光乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083104', '双凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083103', '黄家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083102', '圣灯镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083101', '响石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923130', '粉壁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923135', '岩口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923133', '三十二梁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923134', '江家口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923128', '澌岸镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725231', '望江乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923126', '大寨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923127', '土垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902204', '白庙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923120', '望京镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725236', '拱市乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902203', '大和乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923121', '龙岗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923125', '青云镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923122', '板庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923123', '泥龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904108', '横山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133104', '烟峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423224', '洼里乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904109', '会龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133103', '苏坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133106', '荍坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423222', '沃底乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133105', '劳动镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423223', '大坡蒙古族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133107', '建设镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133109', '梅林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727213', '白马藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727214', '木座藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (168, '510402010', '东华街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727215', '木皮藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727216', '豆叩羌族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329200', '沙堆乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727210', '黄羊关藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727211', '虎牙藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133102', '下溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727212', '泗耳藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133101', '荣丁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435212', '嘎日乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329207', '博美乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727217', '平通羌族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329203', '绕鲁乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435211', '团结乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904100', '安居镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904101', '东禅镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904102', '分水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904103', '石洞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904104', '拦江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329209', '子拖西乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904105', '保石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904106', '白马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435219', '沙岱乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (173, '510904107', '中兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (168, '510402001', '大渡口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727202', '坝子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329210', '和平乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329211', '洛古乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133111', '三河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133110', '雪口山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727200', '高村乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423215', '右所乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (168, '510402009', '瓜子坪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330205', '白垭乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329217', '银多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330206', '汪布顶乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330203', '八帮乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (168, '510402005', '弄弄坪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727206', '锁江羌族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329212', '雄龙西乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727207', '土城藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329213', '麻日乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330202', '岳巴乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727208', '旧堡羌族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (168, '510402002', '炳草岗街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (149, '510727209', '阔达藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329215', '友谊乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330209', '俄南乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330207', '柯洛洞乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330208', '卡松渡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330216', '然姑乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330213', '玉隆乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330211', '俄支乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330219', '年古乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435202', '新茶乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981120', '文升镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981121', '东岳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981122', '瞿河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330223', '亚丁乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330220', '浪多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981110', '青岗镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981111', '洋溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981112', '香山镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981113', '明星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981114', '涪西镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981117', '潼射镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981118', '曹碑镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981119', '官升镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526110', '曹营镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824106', '安顺场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824102', '美罗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981100', '武安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824101', '回隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981101', '大榆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981102', '广兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683001', '剑南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981103', '金华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (70, '510683002', '紫岩街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981104', '沱牌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227107', '八角镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981105', '太乙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722222', '三墩土家族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227106', '宅垄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981106', '金家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722223', '漆树土家族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981107', '复兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722224', '龙泉土家族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227104', '沃日镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981108', '天仙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722225', '渡口土家族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227103', '达维镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (177, '510981109', '仁和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722226', '石铁乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227102', '两河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227101', '四姑娘山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227100', '美兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107001', '浆洗街街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502111', '宗场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502115', '思坡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502112', '宋家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502118', '双谊镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502116', '白花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107007', '火车南站街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124112', '宝五镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124113', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107005', '玉林街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124114', '高凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107004', '望江路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124115', '门坎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107009', '晋阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124110', '集益镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124111', '纯复镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107010', '红牌楼街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107014', '簇锦街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107013', '金花桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107012', '机投桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107011', '簇桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502121', '金秋湖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112110', '石麟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502120', '永兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421101', '丙谷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421100', '攀莲镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421105', '白马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421103', '撒莲镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421102', '得石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421107', '草场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107015', '华兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421106', '普威镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322003', '城南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322002', '朗池街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (162, '511322001', '绥安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526100', '珙泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526101', '巡场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526102', '孝儿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526103', '底洞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526104', '上罗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526105', '洛表镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526106', '洛亥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526107', '王家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526108', '沐滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432208', '且拖乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432202', '贺波洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432203', '鲁基乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432204', '李子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432205', '北山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432210', '沙马拉达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124105', '千佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124106', '王村镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124107', '三江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124108', '东林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124101', '马踏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124102', '竹园镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124103', '研经镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124104', '周坡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231201', '麦昆乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231203', '龙藏乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231204', '求吉玛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502104', '金坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502101', '李庄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502102', '菜坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502108', '牟坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (186, '511502109', '李端镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323102', '革什扎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323103', '东谷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323100', '章谷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323101', '巴底镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323106', '格宗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323107', '半扇门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323104', '墨尔多山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323105', '甲居镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323108', '丹东镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (198, '512022002', '南塔街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428102', '螺髻山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381212', '桥楼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428101', '荞窝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428100', '普基镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231213', '查理乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325115', '莲池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231210', '柯河乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335100', '夏邛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325116', '常林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231211', '垮沙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325113', '仁和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335102', '措拉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325114', '多扶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335101', '中咱镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325111', '双凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231214', '茸安乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335104', '地巫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325112', '高院镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335103', '甲英镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381219', '博树回族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (178, '511827206', '大溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (178, '511827205', '五龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (178, '511827202', '硗碛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (178, '511827201', '蜂桶寨乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231209', '安斗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325110', '鸣龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231208', '四洼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325106', '关文镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303103', '成佳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325107', '凤鸣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325104', '古楼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303101', '建设镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325105', '义兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303108', '莲花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325102', '大全镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303107', '桥头镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325103', '仙林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303106', '五宝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303105', '龙潭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325101', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325108', '青狮镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325109', '槐树镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381235', '鹤峰乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381234', '峰占乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303100', '艾叶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722204', '老君乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722205', '黄石乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222205', '下孟乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222207', '通化乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222202', '甘堡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222203', '蒲溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (28, '513222204', '上孟乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321002', '青阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (203, '510321001', '梧桐街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325001', '南台街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (160, '511325002', '晋城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428213', '特兹乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428212', '西洛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428217', '夹铁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428218', '瓦洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114008', '桂湖街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304109', '大通镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428209', '日都迪萨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304102', '李渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114001', '大丰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304101', '曲水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304104', '龙岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428204', '花山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114003', '三河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304103', '吉安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428203', '黎安乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114004', '新都街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304106', '安福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428206', '大坪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114005', '新繁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304105', '金凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114006', '石板滩街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304108', '世阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (58, '510114007', '斑竹园街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304107', '安平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326201', '麻孜乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326203', '葛卡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326202', '孔色乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326208', '下拖乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326207', '扎拖乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (170, '510411001', '大河中路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304112', '里坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437110', '宝山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304115', '三会镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304114', '金宝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304117', '双桂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304119', '七宝寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326211', '甲斯孔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326210', '木茹乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326216', '龙灯乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304111', '龙蟠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326214', '银恩乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304110', '一立镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326213', '七美乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326219', '沙冲乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (76, '513326218', '色卡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338201', '徐龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425114', '小黑箐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338205', '八日乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425113', '六华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338206', '古学乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425112', '木古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425111', '彰冠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338204', '奔都乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338208', '贡波乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304121', '河西镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129100', '沐溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129101', '永福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (78, '513338210', '茨巫乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823119', '木马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823118', '江口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823117', '羊岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823116', '杨村镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823115', '鹤龄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823114', '白龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823113', '香沉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823112', '金仙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129106', '利店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823111', '公兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129107', '富新镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823110', '王河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129104', '舟坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129105', '黄丹镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129102', '大楠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (111, '511129103', '箭板镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823128', '义兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823127', '姚家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823126', '张王镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437106', '渡口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437107', '马颈子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437104', '金沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437105', '永盛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802202', '龙潭乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437102', '汶水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437103', '黄琅镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802200', '白朝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437100', '锦城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (100, '510802201', '金洞乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437101', '西宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823124', '店子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823123', '涂山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823122', '下寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823121', '汉阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823120', '剑门关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437108', '上田坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (121, '513437109', '瓦岗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811111', '青牛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811112', '射箭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811113', '清水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401111', '巴汝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401110', '琅环镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403001', '凤鸣街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823109', '演圣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823108', '元山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823107', '开封镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823106', '东宝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823105', '武连镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823104', '柳沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823103', '盐店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823101', '龙源镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403006', '谢家街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823100', '普安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403007', '江口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403004', '观音街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (142, '511403003', '青龙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425106', '绿水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425105', '益门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425104', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425103', '通安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425102', '黎溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425101', '鹿厂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725103', '土溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725102', '临巴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425109', '关河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425108', '云甸镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (119, '513425107', '新发镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725105', '文崇镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725104', '三汇镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725107', '贵福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725106', '涌兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725109', '静边镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725108', '岩峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (92, '511681200', '红岩乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725110', '清溪场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725112', '有庆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811100', '元坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725111', '宝城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811101', '卫子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725114', '琅琊镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811102', '王家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725113', '鲜渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (112, '511111002', '铜河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811103', '磨滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725116', '中滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811104', '柏林沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725115', '李渡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811105', '太公镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725118', '三板镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811106', '虎跳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811107', '红岩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (103, '510811108', '昭化镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725119', '丰乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725121', '合力镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133226', '永红乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725120', '李馥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725123', '青龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725125', '卷硐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133223', '高卓营乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725126', '望溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725129', '新市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725128', '龙凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725130', '万寿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725132', '定远镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725131', '渠北镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (63, '511725134', '东安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (143, '511425218', '罗波乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (143, '511425217', '白果乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529210', '屏边彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529211', '清平彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401106', '太和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401105', '佑君镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401108', '阿七镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401107', '安哈镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401109', '樟木箐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (168, '510402100', '银江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401102', '安宁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401101', '礼州镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401104', '黄联关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (110, '511133211', '大竹堡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (127, '513401103', '川兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (191, '511529208', '夏溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705106', '河清镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705105', '秀水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705104', '塔水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705103', '黄土镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705102', '桑枣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705100', '花荄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102009', '绿心街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102008', '海棠街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705108', '界牌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102006', '通江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102004', '大佛街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705116', '千佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705110', '雎水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722109', '峰城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722101', '君塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722102', '清溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722103', '普光镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722104', '天生镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722105', '柏树镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722106', '芭蕉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722107', '南坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722108', '五宝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722110', '土黄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722111', '华景镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722112', '樊哙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722113', '新华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722114', '黄金镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722115', '胡家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722116', '毛坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722118', '大成镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422102', '茶布朗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422103', '雅砻江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422100', '乔瓦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422101', '瓦厂镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722120', '下八镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722122', '塔河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504003', '赵场街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504002', '南岸街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504001', '柏溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422104', '水洛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (124, '513422105', '列瓦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824002', '新棉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421204', '新山傈僳族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421203', '麻陇彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421202', '白坡彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (169, '510421201', '湾丘彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722123', '茶河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722124', '厂溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722125', '红峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722127', '白马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722128', '桃花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (113, '511102010', '全福街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024103', '向义镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024102', '新店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024100', '严陵镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722130', '马渡关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722131', '庙安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722132', '上峡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (66, '511722133', '南坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024109', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024107', '东联镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024106', '高石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024105', '龙会镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024104', '界牌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526205', '玉和苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526209', '罗渡苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024114', '新场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024113', '观英滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024111', '山王镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024119', '小河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024116', '越溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (166, '511024115', '连界镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332106', '阿日扎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332105', '蒙宜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332104', '温波镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332103', '虾扎镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332102', '色须镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332101', '洛须镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (87, '513332100', '尼呷镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323215', '金甲乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323217', '新园乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681114', '金鱼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681116', '南丰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681117', '三星堆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323209', '鲜店乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323208', '平头乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (188, '511526212', '观斗苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182101', '龙门山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182103', '丽春镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182104', '九尺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381117', '洪山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381121', '金垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381120', '水观镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381122', '玉台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (178, '511827102', '陇东镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381124', '木兰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (178, '511827101', '灵关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381127', '五马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (178, '511827100', '穆坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129118', '花水湾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129119', '鹤鸣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129101', '王泗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323222', '石孔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381128', '天宫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129103', '新场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129104', '悦来镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129105', '安仁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129106', '出江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (43, '510129108', '西岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182121', '白鹿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (69, '510604107', '新盛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182123', '葛仙山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (69, '510604106', '调元镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117104', '唐昌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117106', '三道堰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323201', '巴旺乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (69, '510604100', '万安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (69, '510604103', '略坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323209', '梭坡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (69, '510604101', '鄢家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (69, '510604102', '金山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182111', '桂花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (50, '510117115', '友爱镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (75, '513323213', '太平桥乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182107', '丹景山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182106', '通济镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (49, '510182109', '敖平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (69, '510604113', '白马关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335200', '拉哇乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335202', '竹巴龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335205', '昌波乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335204', '苏哇龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803102', '车岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335209', '波密乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803101', '百丈镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335208', '亚日贡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803104', '马岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803106', '蒙顶山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803105', '新店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803108', '红星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803107', '黑竹镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803109', '中峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335210', '莫多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335212', '波戈溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335211', '松多乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335216', '列衣乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335215', '茶洛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (73, '513335217', '德达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428225', '五道箐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (126, '513428227', '大槽乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523109', '迎安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523108', '五矿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523105', '留耕镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523104', '怡乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523101', '红桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523100', '江安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381103', '彭城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381105', '柏垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231102', '麦尔玛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231103', '河支镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231100', '阿坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231101', '贾洛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231104', '各莫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523119', '仁和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (23, '513231105', '安羌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381107', '思依镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523118', '大妙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381106', '飞凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523117', '下长镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381109', '二龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523116', '阳春镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381108', '文成镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523115', '大井镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523114', '四面山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523113', '铁清镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803112', '茅河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381110', '石滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (189, '511523110', '夕佳山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381112', '龙泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803119', '万古镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381111', '老观镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (181, '511803118', '前进镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381114', '望垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381113', '千佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381116', '妙高镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126001', '青衣街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (106, '511126002', '漹城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921125', '陈河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921124', '唱歌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921127', '兴隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336101', '青德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921126', '青峪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336100', '香巴拉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921129', '长坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336103', '热打镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921128', '烟溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921121', '三溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921120', '空山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921123', '杨柏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921122', '春在镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921114', '毛浴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921113', '诺水河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703234', '虎让乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921116', '泥溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703235', '米城乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921115', '两河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921118', '新场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921117', '板桥口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921119', '龙凤场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921110', '永安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921112', '涪阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921111', '铁溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921103', '广纳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921102', '火炬镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233203', '查尔玛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921105', '麻石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233202', '江茸乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921104', '铁佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921107', '洪口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921106', '至诚镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233207', '麦洼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921109', '瓦室镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921108', '沙溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233205', '阿木乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921100', '诺江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (52, '510113003', '大同街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (52, '510113002', '大弯街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502010', '况场街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703217', '龙会乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703200', '石板街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703201', '管理委员会幺塘乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304003', '都尉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304005', '西兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304004', '文峰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304006', '南湖街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723001', '凤灵街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (155, '511304001', '火花街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703203', '安仁乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426111', '大崇镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426112', '松坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327202', '洛秋乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327201', '雅德乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327200', '泥巴乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327205', '仁达乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426110', '鲁吉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327209', '更知乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327208', '充古乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327207', '旦都乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426108', '满银沟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426109', '新街镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426100', '鲹鱼河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426101', '铅锌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426102', '堵格镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426103', '姜州镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426104', '乌东德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426105', '淌塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426106', '铁柳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426107', '嘎吉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327212', '宗麦乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327211', '宗塔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327210', '卡娘乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (85, '513327214', '下罗柯马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322003', '东湖街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322001', '富世街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322002', '邓井关街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724111', '石子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724110', '周家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724113', '妈妈镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724112', '文星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623206', '柏树乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724115', '欧家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724114', '高穴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724117', '清水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623207', '白果乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823210', '秀钟乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724116', '庙坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724119', '高明镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724118', '月华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724120', '童家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (99, '510823228', '樵店乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724121', '天城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681101', '三水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681103', '高坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724126', '永胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681102', '连山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724125', '四合镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681105', '向阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681107', '金轮镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681106', '小汉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726103', '永昌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726104', '通泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726105', '永安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726100', '曲山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726101', '擂鼓镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726106', '禹里镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726107', '桂溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726108', '陈家坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726109', '小坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (143, '511425110', '西龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (143, '511425112', '高台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (171, '510403100', '格里坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903011', '灵泉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903012', '慈音街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903013', '九莲街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (143, '511425102', '汉阳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903014', '南强街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (143, '511425108', '瑞峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903016', '西宁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903017', '杨渡街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903008', '广德街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903009', '富源路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903001', '南津路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903002', '凯旋路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903004', '镇江寺街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903005', '育才路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903006', '介福路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903007', '嘉禾街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622103', '飞龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622102', '烈面镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622101', '中心镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622100', '沿口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622107', '三溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622106', '龙女镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423001', '盐井街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622105', '万善镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622104', '乐善镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (145, '510705204', '高川乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622109', '胜利镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622108', '赛马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622114', '礼安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622113', '万隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622112', '街子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622111', '清平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622118', '鸣钟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902008', '奇章街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622117', '石盘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902009', '时新街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622116', '宝箴塞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902006', '玉堂街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622115', '华封镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902007', '兴文街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902004', '江北街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902005', '宕梁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902002', '西城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623218', '通山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902003', '回风街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622110', '金牛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902001', '东城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623214', '永丰乡街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504101', '南广镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504106', '泥溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504105', '柳嘉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504104', '横江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504103', '观音镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504109', '高场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504108', '商州镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504107', '蕨溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802110', '多营镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504113', '合什镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802112', '望鱼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802111', '碧峰峡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504111', '双龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802114', '八步镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504110', '安边镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802113', '周公山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504119', '樟海镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623132', '东北镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802101', '草坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802107', '上里镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802109', '晏场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (108, '511113201', '共安彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (108, '511113200', '和平彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (108, '511113203', '永胜乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724203', '朝阳乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181111', '龙池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181110', '青城山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422205', '共和乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422203', '红果彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181106', '石羊镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724214', '安吉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181105', '天马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422207', '红宝苗族彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422206', '国胜乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724218', '八渡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025103', '鱼溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025102', '归德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025100', '重龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724220', '杨通乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181103', '聚源镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025109', '罗泉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025108', '龙结镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025106', '球溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025105', '铁��镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422211', '格萨拉彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (172, '510422210', '温泉彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681002', '新丰街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681001', '雒城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (67, '510681003', '金雁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323114', '兴旺镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323113', '银汉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323116', '睦坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230102', '中壤塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230101', '南木达镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323110', '杨家镇杨家镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (31, '513230103', '岗木达镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333100', '色柯镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323112', '福德镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333101', '翁达镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323111', '罗家镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333102', '洛若镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333103', '泥朵镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333104', '甲学镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381001', '保宁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381004', '七里街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381003', '沙溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381006', '河溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (156, '511381005', '江南街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323103', '正源镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323102', '巨龙镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323105', '金溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323101', '锦屏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502106', '通滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502107', '江北镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502108', '方山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502109', '丹林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323107', '河舒镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502103', '黄舣镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323106', '徐家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323109', '龙蚕镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323108', '利溪镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502110', '分水岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430225', '百草坡镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430224', '南瓦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430223', '德溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430227', '丙底镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302200', '新复乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921214', '松溪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (40, '511921216', '胜利乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116112', '永安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116115', '黄水镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429206', '补尔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429209', '拉果乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116111', '黄龙溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430201', '热水河乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430206', '甲依乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324202', '三岩龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430210', '基觉乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324203', '上团乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430214', '青松乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324204', '八窝龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430211', '小银木乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430218', '洛觉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (120, '513430217', '山江乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324209', '子耳彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (115, '513429226', '基只乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324213', '朵洛彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324212', '小金彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (81, '513324216', '洪坝乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336202', '水洼乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336201', '沙贡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336206', '洞松乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336205', '然乌乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336208', '定波乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336209', '正斗乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116108', '彭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826106', '思延镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826105', '龙门镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826104', '大川镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826103', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826102', '双石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826101', '飞仙关镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (88, '513336210', '白依乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021240', '千佛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021241', '拱桥乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184120', '文井江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221109', '绵虒镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426251', '野租乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221111', '灞州镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184113', '道明镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703122', '赵固镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233105', '龙日镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184114', '隆兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703123', '桥湾镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233104', '色地镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233103', '安曲镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184112', '白头镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703125', '大堰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233102', '瓦切镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703120', '双庙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233101', '刷经寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (25, '513233100', '邛溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322104', '骑龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322102', '琵琶镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184109', '街子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322103', '狮市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703127', '罐子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703112', '金垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703113', '渡市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021227', '合义乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703114', '管村镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021221', '白塔寺乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021222', '双龙街乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703110', '河市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723113', '巨龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723116', '岐伯镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723117', '文通镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723114', '高渠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723115', '鹅溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723118', '永泰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723119', '九龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703115', '石梯镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021218', '高升乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703116', '石桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021219', '横庙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703117', '堡子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703118', '平滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703100', '亭子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703101', '福善镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021215', '东胜乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703102', '麻柳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021212', '云峰乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021213', '岳新乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723101', '玉龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723102', '富驿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723100', '云溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723105', '黄甸镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221105', '水磨镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221106', '漩口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723103', '金孔镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221107', '三江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221108', '耿达镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221102', '映秀镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221103', '卧龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703108', '百节镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703109', '赵家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (34, '513221100', '威州镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703104', '大树镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703105', '南岳镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703106', '万家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (60, '511703107', '景市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021205', '来凤乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (196, '512021206', '天马乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723120', '西陵镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426202', '老君滩乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (148, '510781006', '中坝街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426203', '江西街乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (151, '510723121', '嫘祖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603108', '虎城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603107', '龙滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603106', '护安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603105', '观塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603104', '代市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603103', '广兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (118, '513426235', '溜姑乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322119', '福善镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322117', '兜山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322118', '板桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322115', '永年镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603102', '观阁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603101', '桂兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322113', '童寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322114', '古佛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322111', '代寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322126', '长滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322124', '飞龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322125', '怀德镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322122', '安溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322120', '李桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (201, '510322121', '赵化镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622202', '猛山乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025114', '太平镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622201', '真静乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025112', '银山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025110', '发轮镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622203', '双星乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (95, '511622208', '鼓匠乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025119', '明心寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025118', '新桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025116', '水南镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726209', '开坪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083001', '古湖街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726205', '漩坪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726206', '白坭乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726208', '片口乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (164, '511083002', '金鹅街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224221', '下八寨乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025125', '陈家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025124', '高楼镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902109', '大罗镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025123', '双龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025122', '龙江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724001', '竹阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902107', '三江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025121', '公民镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902108', '鼎山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025120', '双河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724003', '白塔街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902105', '曾口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724002', '东柳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902106', '梁永镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025129', '马鞍镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025128', '孟塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902103', '水宁寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902104', '化成镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902101', '清江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902100', '大茅坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112109', '蔡金镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112108', '冠英镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112105', '金山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (143, '511425001', '青竹街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112104', '金粟镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112107', '西坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (167, '511025130', '狮子镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112101', '牛华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (114, '511112100', '竹根镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224208', '岷江乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224207', '镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224209', '大姓乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224203', '安宏乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224201', '十里回族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726212', '青片乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726214', '桃龙藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726210', '坝底乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726211', '白什乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224219', '黄龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726216', '马槽乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (146, '510726217', '都贯乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224214', '燕云乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224210', '白羊乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (33, '513224213', '小姓乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923003', '江口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923001', '同州街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (39, '511923002', '金宝街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623121', '普兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423103', '白乌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623120', '万福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423104', '树河镇树河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623123', '冯店镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423101', '卫城镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623122', '联合镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423102', '梅雨镇街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528100', '古宋镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423109', '梅子坪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423107', '泸沽湖镇格姆街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528101', '僰王山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423108', '官地镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423105', '黄草镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528103', '共乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423106', '平川镇平川街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528105', '莲花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528108', '石海镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528107', '九丝城镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623125', '太安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623124', '积金镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623110', '龙台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623111', '永安镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435103', '吉米镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435102', '海棠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435105', '普昌镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435104', '斯觉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435101', '田坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435100', '新市坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623118', '广福镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623117', '仓山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623119', '会龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623114', '永兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435107', '乌史大桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623113', '玉兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435106', '玉田镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623116', '继光镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623115', '悦来镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (117, '513435108', '苏雄镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107064', '桂溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107063', '石羊街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623101', '南华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107062', '肖家河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623100', '凯江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329100', '如龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329101', '拉日马镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (57, '510107061', '芳草街街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329102', '大盖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329103', '通宵镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329104', '色威镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (89, '513329105', '尤拉西镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623107', '富兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623106', '集凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623109', '兴隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623108', '辑庆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623103', '通济镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623102', '回龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623105', '黄鹿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (72, '510623104', '永太镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903107', '老池镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423114', '盐塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903108', '保升镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423115', '金河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903109', '北固镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423112', '甲米镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423113', '棉桠镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423110', '润盐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423111', '长柏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902127', '凤溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902128', '天马山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704003', '富乐街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528111', '五星镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704004', '游仙街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528110', '周家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (152, '510704001', '涪江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423116', '龙塘镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423117', '兴隆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330106', '麦宿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330107', '打滚镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330105', '错阿镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330102', '竹庆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330103', '阿须镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330100', '更庆镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330101', '马尼干戈镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903100', '龙凤镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903101', '仁里镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902123', '平梁镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902124', '光辉镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903103', '永兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903104', '河沙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (36, '511902122', '枣林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903105', '新桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (174, '510903106', '桂花镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321210', '五灵乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330108', '龚垭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330109', '温拖镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227218', '潘安乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227217', '汗牛乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227216', '窝底乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (79, '513330110', '中扎科镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227213', '抚边乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227211', '木坡乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303003', '长土街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303002', '贡井街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521123', '海潮镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (202, '510303001', '筱溪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321233', '宏观乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423206', '藤桥乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528200', '大坝苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (129, '513423207', '田湾乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321238', '双峰乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528205', '大河苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528207', '仙峰苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (192, '511528206', '麒麟苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321229', '小元乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227209', '结斯乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227208', '日尔乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227204', '沙龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227203', '美沃乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227202', '新桥乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (35, '513227201', '崇德乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525110', '胜天镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525111', '复兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525112', '落润镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525113', '庆岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432100', '光明镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432101', '冕山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432102', '红莫镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432103', '两河口镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432104', '米市镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432105', '洛哈镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (128, '513432106', '尼波镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (157, '511321243', '太霞乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525100', '文江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525101', '庆符镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525102', '沙河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525103', '嘉乐镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525105', '罗场镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525106', '蕉村镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525107', '可久镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525108', '来复镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (187, '511525109', '月江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (109, '511124001', '研城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521118', '方洞镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333201', '克果乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521110', '石桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333202', '然充乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521111', '毗卢镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333203', '康勒乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521112', '奇峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333204', '大章乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521113', '潮河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333205', '大则乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521114', '云锦镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824216', '王岗坪彝族藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333206', '亚龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521115', '立石镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333207', '塔子乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521116', '百和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333208', '年龙乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521117', '天兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824215', '草科藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824212', '新民藏族彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824211', '丰乐乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002110', '伍隍镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002111', '石岭镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002112', '东峰镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002113', '南津镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002116', '丰裕镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002117', '迎接镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002118', '祥符镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521107', '玄滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521108', '太伏镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521109', '云龙镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333210', '霍西乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724102', '团坝镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333211', '旭日乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724101', '乌木镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (86, '513333212', '杨各乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824209', '迎政乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521100', '福集镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521101', '嘉明镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824207', '栗子坪彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521102', '喻寺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521103', '得胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521105', '牛滩镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824203', '永和乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (136, '510521106', '兆雅镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (182, '511824202', '蟹螺藏族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504203', '凤仪乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (193, '511504202', '龙池乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002100', '管理委员会雁江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002101', '管理委员会松涛镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002102', '宝台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724104', '清河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002103', '管理委员会临江镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724103', '杨家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002104', '保和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724106', '石河镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002105', '老君镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724105', '柏林镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002106', '中和镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724108', '石桥铺镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002107', '丹山镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724107', '中华镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002108', '小院镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (199, '512002109', '堪��镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (61, '511724109', '观音镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603004', '新桥街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181007', '蒲阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603003', '龙塘街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181006', '玉堂街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603002', '大佛寺街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (94, '511603001', '奎阁街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181001', '灌口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181003', '银杏街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181002', '幸福街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (44, '510181005', '奎光塔街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (180, '511826001', '芦阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104030', '沙河街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104032', '狮子山街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116017', '怡心街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116018', '成都直管区万安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116019', '成都直管区正兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104035', '成龙路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104037', '三圣街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104036', '柳江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323002', '相如街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (158, '511323001', '周口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116024', '成都直管区太平街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502007', '华阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116025', '成都直管区永兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502009', '泰安街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502003', '大山坪街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502004', '邻玉街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502005', '蓝田街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502006', '茜草街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502001', '南城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (134, '510502002', '北城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116020', '成都直管区兴隆街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116021', '成都直管区煎茶街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116022', '成都直管区新兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116023', '成都直管区籍田街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104017', '锦官驿街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104019', '锦华路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104018', '东湖街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302107', '李家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302108', '双桥镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104022', '春熙路街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116002', '西航港街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116003', '成都直管区华阳街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116004', '中和街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116005', '九江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116006', '黄甲街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802002', '西城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302104', '共兴镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802001', '东城街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302105', '金台镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802004', '青江街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302106', '芦溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802003', '河北街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (185, '511802005', '大兴街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (55, '510116001', '东升街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104023', '书院街街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (47, '510104026', '牛市口街道', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184106', '观胜镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184107', '怀远镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184104', '廖家镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (42, '510184105', '元通镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (159, '511302114', '渔溪镇', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524213', '水潦彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524212', '枧槽苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524215', '石厢子彝族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524211', '白��苗族乡', 4, 2020, '', '', '', '', 1, 0);
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES
  (138, '510524210', '合乐苗族乡', 4, 2020, '', '', '', '', 1, 0);

-- ============================================
-- 第二部分：数据变更 SQL
-- ============================================

-- ============================================
-- 2.1 新增数据（GeoJSON 中有但 2020 年基准没有的）
-- 说明：乡镇/街道数据只插入 grassroots_organization 表
--       organization 表存储省市县数据（来自 2020 年基准，变化概率低）
-- ============================================

-- 新增：513301212 - 吉居乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513301212', '吉居乡', 4, 2025, '510000', '513300', '513301', '吉居乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513301218 - 孔玉乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513301218', '孔玉乡', 4, 2025, '510000', '513300', '513301', '孔玉乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：512072701 - 松涛镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '512072701', '松涛镇', 4, 2025, '510000', '512000', '512002', '松涛镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：512072702 - 狮子山街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '512072702', '狮子山街道', 4, 2025, '510000', '512000', '512002', '狮子山街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513301201 - 雅拉乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513301201', '雅拉乡', 4, 2025, '510000', '513300', '513301', '雅拉乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513301205 - 麦崩乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513301205', '麦崩乡', 4, 2025, '510000', '513300', '513301', '麦崩乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511471701 - 青龙街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511471701', '青龙街道', 4, 2025, '510000', '511400', '511403', '青龙街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511471702 - 锦江镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511471702', '锦江镇', 4, 2025, '510000', '511400', '511403', '锦江镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511471703 - 视高街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511471703', '视高街道', 4, 2025, '510000', '511400', '511421', '视高街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511471704 - 高家镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511471704', '高家镇', 4, 2025, '510000', '511400', '511421', '高家镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511471705 - 贵平镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511471705', '贵平镇', 4, 2025, '510000', '511400', '511421', '贵平镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511471706 - 北斗镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511471706', '北斗镇', 4, 2025, '510000', '511400', '511421', '北斗镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511471707 - 龙马镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511471707', '龙马镇', 4, 2025, '510000', '511400', '511421', '龙马镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173706 - 福田街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173706', '福田街道', 4, 2025, '510000', '510100', '510185', '福田街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173707 - 玉成街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173707', '玉成街道', 4, 2025, '510000', '510100', '510185', '玉成街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173708 - 草池���道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173708', '草池街道', 4, 2025, '510000', '510100', '510185', '草池街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173709 - 石板凳街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173709', '石板凳街道', 4, 2025, '510000', '510100', '510185', '石板凳街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173702 - 石盘街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173702', '石盘街道', 4, 2025, '510000', '510100', '510185', '石盘街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173703 - 养马街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173703', '养马街道', 4, 2025, '510000', '510100', '510185', '养马街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173704 - 贾家街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173704', '贾家街道', 4, 2025, '510000', '510100', '510185', '贾家街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173705 - 丹景街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173705', '丹景街道', 4, 2025, '510000', '510100', '510185', '丹景街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173710 - 高明镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173710', '高明镇', 4, 2025, '510000', '510100', '510185', '高明镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173711 - 武庙镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173711', '武庙镇', 4, 2025, '510000', '510100', '510185', '武庙镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173712 - 壮溪镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173712', '壮溪镇', 4, 2025, '510000', '510100', '510185', '壮溪镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173701 - 三岔街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173701', '三岔街道', 4, 2025, '510000', '510100', '510185', '三岔街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173713 - 海螺镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173713', '海螺镇', 4, 2025, '510000', '510100', '510185', '海螺镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173714 - 董家埂镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173714', '董家埂镇', 4, 2025, '510000', '510100', '510185', '董家埂镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510173715 - 芦葭镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510173715', '芦葭镇', 4, 2025, '510000', '510100', '510185', '芦葭镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402001 - 城北街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402001', '城北街道', 4, 2025, '510000', '513400', '513402', '城北街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402002 - 城南街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402002', '城南街道', 4, 2025, '510000', '513400', '513402', '城南街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510371701 - 红旗街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510371701', '红旗街道', 4, 2025, '510000', '510300', '510302', '红旗街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402003 - 古城街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402003', '古城街道', 4, 2025, '510000', '513400', '513402', '古城街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510371702 - 学苑街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510371702', '学苑街道', 4, 2025, '510000', '510300', '510302', '学苑街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510371703 - 高峰街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510371703', '高峰街道', 4, 2025, '510000', '510300', '510302', '高峰街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510371704 - 丹桂街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510371704', '丹桂街道', 4, 2025, '510000', '510300', '510302', '丹桂街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510371705 - 板仓街道办事处
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510371705', '板仓街道办事处', 4, 2025, '510000', '510300', '510311', '板仓街道办事处', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511621113 - 石垭镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511621113', '石垭镇', 4, 2025, '510000', '511600', '511621', '石垭镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511527106 - 大雪山镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511527106', '大雪山镇', 4, 2025, '510000', '511500', '511527', '大雪山镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510972701 - 西宁街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510972701', '西宁街道', 4, 2025, '510000', '510900', '510903', '西宁街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510972702 - 保升镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510972702', '保升镇', 4, 2025, '510000', '510900', '510903', '保升镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511071702 - 胜利街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511071702', '胜利街道', 4, 2025, '510000', '511000', '511011', '胜利街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511071701 - 高桥街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511071701', '高桥街道', 4, 2025, '510000', '511000', '511011', '高桥街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510773701 - 创业园街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510773701', '创业园街道', 4, 2025, '510000', '510700', '510703', '创业园街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511822205 - 民建彝族乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511822205', '民建彝族乡', 4, 2025, '510000', '511800', '511822', '民建彝族乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511602999 - 枣山街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511602999', '枣山街道', 4, 2025, '510000', '511600', '511602', '枣山街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511072703 - 交通镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511072703', '交通镇', 4, 2025, '510000', '511000', '511002', '交通镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511072702 - 靖民镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511072702', '靖民镇', 4, 2025, '510000', '511000', '511002', '靖民镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511072701 - 壕子口街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511072701', '壕子口街道', 4, 2025, '510000', '511000', '511002', '壕子口街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511171701 - 安谷镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511171701', '安谷镇', 4, 2025, '510000', '511100', '511102', '安谷镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510704400 - 四川省新华劳动教育管理所
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510704400', '四川省新华劳动教育管理所', 4, 2025, '510000', '510700', '510704', '四川省新华劳动教育管理所', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511502009 - 象鼻街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511502009', '象鼻街道', 4, 2025, '510000', '511500', '511502', '象鼻街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511502015 - 大观楼街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511502015', '大观楼街道', 4, 2025, '510000', '511500', '511502', '大观楼街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511771703 - 幺塘乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511771703', '幺塘乡', 4, 2025, '510000', '511700', '511703', '幺塘乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511771704 - 河市镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511771704', '河市镇', 4, 2025, '510000', '511700', '511703', '河市镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511771701 - 斌郎街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511771701', '斌郎街道', 4, 2025, '510000', '511700', '511703', '斌郎街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511771702 - 石板街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511771702', '石板街道', 4, 2025, '510000', '511700', '511703', '石板街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511771705 - 金垭镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511771705', '金垭镇', 4, 2025, '510000', '511700', '511703', '金垭镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513225400 - 九寨沟国营牧场
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513225400', '九寨沟国营牧场', 4, 2025, '510000', '513200', '513225', '九寨沟国营牧场', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513225401 - 九寨沟风景名胜管理局
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513225401', '九寨沟风景名胜管理局', 4, 2025, '510000', '513200', '513225', '九寨沟风景名胜管理局', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510775702 - 松垭镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510775702', '松垭镇', 4, 2025, '510000', '510700', '510704', '松垭镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511403107 - 黄丰镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511403107', '黄丰镇', 4, 2025, '510000', '511400', '511403', '黄丰镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511403103 - 公义镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511403103', '公义镇', 4, 2025, '510000', '511400', '511403', '公义镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510775701 - 塘汛街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510775701', '塘汛街道', 4, 2025, '510000', '510700', '510703', '塘汛街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511133108 - 民主镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511133108', '民主镇', 4, 2025, '510000', '511100', '511133', '民主镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511133100 - 民建镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511133100', '民建镇', 4, 2025, '510000', '511100', '511133', '民建镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722219 - 桃花镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722219', '桃花镇', 4, 2025, '510000', '511700', '511722', '桃花镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722212 - 上峡镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722212', '上峡镇', 4, 2025, '510000', '511700', '511722', '上峡镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722213 - 塔河镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722213', '塔河镇', 4, 2025, '510000', '511700', '511722', '塔河镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722214 - 茶河镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722214', '茶河镇', 4, 2025, '510000', '511700', '511722', '茶河镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722217 - 南坪镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722217', '南坪镇', 4, 2025, '510000', '511700', '511722', '南坪镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722220 - 白马镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722220', '白马镇', 4, 2025, '510000', '511700', '511722', '白马镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722227 - 厂溪镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722227', '厂溪镇', 4, 2025, '510000', '511700', '511722', '厂溪镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722228 - 红峰镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722228', '红峰镇', 4, 2025, '510000', '511700', '511722', '红峰镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722233 - 马渡关镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722233', '马渡关镇', 4, 2025, '510000', '511700', '511722', '马渡关镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722236 - 蒲江街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722236', '蒲江街道', 4, 2025, '510000', '511700', '511722', '蒲江街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722237 - 东乡街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722237', '东乡街道', 4, 2025, '510000', '511700', '511722', '东乡街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511502110 - 宗场镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511502110', '宗场镇', 4, 2025, '510000', '511500', '511502', '宗场镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511502113 - 思坡镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511502113', '思坡镇', 4, 2025, '510000', '511500', '511502', '思坡镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511502119 - 永兴镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511502119', '永兴镇', 4, 2025, '510000', '511500', '511502', '永兴镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511526109 - 沐滩镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511526109', '沐滩镇', 4, 2025, '510000', '511500', '511526', '沐滩镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510971702 - 北固镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510971702', '北固镇', 4, 2025, '510000', '510900', '510903', '北固镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510971703 - 嘉禾街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510971703', '嘉禾街道', 4, 2025, '510000', '510900', '510903', '嘉禾街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510971701 - 新桥镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510971701', '新桥镇', 4, 2025, '510000', '510900', '510903', '新桥镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510971706 - 南强街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510971706', '南强街道', 4, 2025, '510000', '510900', '510903', '南强街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510971707 - 富源路街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510971707', '富源路街道', 4, 2025, '510000', '510900', '510903', '富源路街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510971704 - 九莲街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510971704', '九莲街道', 4, 2025, '510000', '510900', '510903', '九莲街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510971705 - 广德街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510971705', '广德街道', 4, 2025, '510000', '510900', '510903', '广德街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510772701 - 沉抗镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510772701', '沉抗镇', 4, 2025, '510000', '510700', '510704', '沉抗镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：512002400 - 四川大堰劳动教养管理所
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '512002400', '四川大堰劳动教养管理所', 4, 2025, '510000', '512000', '512002', '四川大堰劳动教养管理所', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722207 - 庙安镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722207', '庙安镇', 4, 2025, '510000', '511700', '511722', '庙安镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510824400 - 苍溪县九龙山自然保护区事务中心
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510824400', '苍溪县九龙山自然保护区事务中心', 4, 2025, '510000', '510800', '510824', '苍溪县九龙山自然保护区事务中心', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510824401 - 苍溪县国家森林公园事务中心（苍溪县三溪口国有林场）
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510824401', '苍溪县国家森林公园事务中心（苍溪县三溪口国有林场）', 4, 2025, '510000', '510800', '510824', '苍溪县国家森林公园事务中心（苍溪县三溪口国有林场）', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511722119 - 下八镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511722119', '下八镇', 4, 2025, '510000', '511700', '511722', '下八镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510172704 - 芳草街街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510172704', '芳草街街道', 4, 2025, '510000', '510100', '510107', '芳草街街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510172703 - 肖家河街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510172703', '肖家河街道', 4, 2025, '510000', '510100', '510107', '肖家河街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510172702 - 桂溪街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510172702', '桂溪街道', 4, 2025, '510000', '510100', '510107', '桂溪街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510172701 - 石羊街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510172701', '石羊街道', 4, 2025, '510000', '510100', '510107', '石羊街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510172707 - 西园街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510172707', '西园街道', 4, 2025, '510000', '510100', '510117', '西园街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510172706 - 中和街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510172706', '中和街道', 4, 2025, '510000', '510100', '510116', '中和街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510172705 - 合作街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510172705', '合作街道', 4, 2025, '510000', '510100', '510117', '合作街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513232400 - 白河牧场
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513232400', '白河牧场', 4, 2025, '510000', '513200', '513232', '白河牧场', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：512071701 - 雁江镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '512071701', '雁江镇', 4, 2025, '510000', '512000', '512002', '雁江镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：512071702 - 临江镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '512071702', '临江镇', 4, 2025, '510000', '512000', '512002', '临江镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510671702 - 旌东街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510671702', '旌东街道', 4, 2025, '510000', '510600', '510603', '旌东街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510671701 - 八角井街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510671701', '八角井街道', 4, 2025, '510000', '510600', '510603', '八角井街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511921101 - 民胜镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511921101', '民胜镇', 4, 2025, '510000', '511900', '511921', '民胜镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510723002 - 凤灵街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510723002', '凤灵街道', 4, 2025, '510000', '510700', '510723', '凤灵街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402108 - 云甸镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402108', '云甸镇', 4, 2025, '510000', '513400', '513402', '云甸镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402109 - 关河镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402109', '关河镇', 4, 2025, '510000', '513400', '513402', '关河镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402106 - 绿水镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402106', '绿水镇', 4, 2025, '510000', '513400', '513402', '绿水镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402107 - 新发镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402107', '新发镇', 4, 2025, '510000', '513400', '513402', '新发镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402101 - 鹿厂镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402101', '鹿厂镇', 4, 2025, '510000', '513400', '513402', '鹿厂镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402104 - 太平镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402104', '太平镇', 4, 2025, '510000', '513400', '513402', '太平镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402105 - 益门镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402105', '益门镇', 4, 2025, '510000', '513400', '513402', '益门镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402102 - 黎溪镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402102', '黎溪镇', 4, 2025, '510000', '513400', '513402', '黎溪镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402103 - 通安镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402103', '通安镇', 4, 2025, '510000', '513400', '513402', '通安镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402111 - 彰冠镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402111', '彰冠镇', 4, 2025, '510000', '513400', '513402', '彰冠镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402112 - 木古镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402112', '木古镇', 4, 2025, '510000', '513400', '513402', '木古镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402113 - 六华镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402113', '六华镇', 4, 2025, '510000', '513400', '513402', '六华镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402114 - 小黑箐镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402114', '小黑箐镇', 4, 2025, '510000', '513400', '513402', '小黑箐镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510726110 - 通泉镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510726110', '通泉镇', 4, 2025, '510000', '510700', '510726', '通泉镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511504102 - 观音镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511504102', '观音镇', 4, 2025, '510000', '511500', '511504', '观音镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510973701 - 灵泉街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510973701', '灵泉街道', 4, 2025, '510000', '510900', '510903', '灵泉街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510973704 - 科教园管理办
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510973704', '科教园管理办', 4, 2025, '510000', '510900', '510903', '科教园管理办', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510973705 - 芝溪谷管理办
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510973705', '芝溪谷管理办', 4, 2025, '510000', '510900', '510903', '芝溪谷管理办', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510973702 - 慈音街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510973702', '慈音街道', 4, 2025, '510000', '510900', '510903', '慈音街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510973703 - 杨渡街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510973703', '杨渡街道', 4, 2025, '510000', '510900', '510903', '杨渡街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511504112 - 合什镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511504112', '合什镇', 4, 2025, '510000', '511500', '511504', '合什镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511504118 - 南广镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511504118', '南广镇', 4, 2025, '510000', '511500', '511504', '南广镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511772704 - 安仁乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511772704', '安仁乡', 4, 2025, '510000', '511700', '511703', '安仁乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511772703 - 福善镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511772703', '福善镇', 4, 2025, '510000', '511700', '511703', '福善镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511772702 - 麻柳镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511772702', '麻柳镇', 4, 2025, '510000', '511700', '511703', '麻柳镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511772701 - 亭子镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511772701', '亭子镇', 4, 2025, '510000', '511700', '511703', '亭子镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513429224 - 委只洛乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513429224', '委只洛乡', 4, 2025, '510000', '513400', '513429', '委只洛乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402202 - 内东乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402202', '内东乡', 4, 2025, '510000', '513400', '513402', '内东乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510726207 - 白坭乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510726207', '白坭乡', 4, 2025, '510000', '510700', '510726', '白坭乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402219 - 树堡乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402219', '树堡乡', 4, 2025, '510000', '513400', '513402', '树堡乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402221 - 新安傣族乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402221', '新安傣族乡', 4, 2025, '510000', '513400', '513402', '新安傣族乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510726215 - 白什乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510726215', '白什乡', 4, 2025, '510000', '510700', '510726', '白什乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：513402234 - 槽元乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '513402234', '槽元乡', 4, 2025, '510000', '513400', '513402', '槽元乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171709 - 太平街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171709', '太平街道', 4, 2025, '510000', '510100', '510116', '太平街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171708 - 籍田街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171708', '籍田街道', 4, 2025, '510000', '510100', '510116', '籍田街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171703 - 正兴街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171703', '正兴街道', 4, 2025, '510000', '510100', '510116', '正兴街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171702 - 万安街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171702', '万安街道', 4, 2025, '510000', '510100', '510116', '万安街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171701 - 华阳街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171701', '华阳街道', 4, 2025, '510000', '510100', '510116', '华阳街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171707 - 永兴街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171707', '永兴街道', 4, 2025, '510000', '510100', '510116', '永兴街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171706 - 煎茶街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171706', '煎茶街道', 4, 2025, '510000', '510100', '510116', '煎茶街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171705 - 新兴街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171705', '新兴街道', 4, 2025, '510000', '510100', '510116', '新兴街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510171704 - 兴隆街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510171704', '兴隆街道', 4, 2025, '510000', '510100', '510116', '兴隆街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510704006 - 四川省科学城春雷街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510704006', '四川省科学城春雷街道', 4, 2025, '510000', '510700', '510704', '四川省科学城春雷街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511528203 - 大坝苗族乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511528203', '大坝苗族乡', 4, 2025, '510000', '511500', '511528', '大坝苗族乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511525115 - 落润镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511525115', '落润镇', 4, 2025, '510000', '511500', '511525', '落润镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511525116 - 庆岭镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511525116', '庆岭镇', 4, 2025, '510000', '511500', '511525', '庆岭镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：511504201 - 龙池乡
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '511504201', '龙池乡', 4, 2025, '510000', '511500', '511504', '龙池乡', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510771701 - 永兴镇
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510771701', '永兴镇', 4, 2025, '510000', '510700', '510703', '永兴镇', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- 新增：510771702 - 普明街道
INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '510771702', '普明街道', 4, 2025, '510000', '510700', '510703', '普明街道', 0, 0, '2026-03-12 23:37:00', '2026-03-12 23:37:00');

-- ============================================
-- 2.2 删除数据（2020 年基准有但 GeoJSON 中没有的）
-- ============================================

-- 删除：513301200 - 雅拉乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513301200' AND `year` = 2025;

-- 删除：513301209 - 吉居乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513301209' AND `year` = 2025;

-- 删除：513301204 - 麦崩乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513301204' AND `year` = 2025;

-- 删除：513301206 - 捧塔乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513301206' AND `year` = 2025;

-- 删除：510704105 - 沉抗镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510704105' AND `year` = 2025;

-- 删除：510704112 - 松垭镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510704112' AND `year` = 2025;

-- 删除：512002004 - 管理委员会狮子山街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '512002004' AND `year` = 2025;

-- 删除：511602007 - 枣山街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511602007' AND `year` = 2025;

-- 删除：511527109 - 大雪山镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511527109' AND `year` = 2025;

-- 删除：510185132 - 海螺镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185132' AND `year` = 2025;

-- 删除：510185131 - 董家埂镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185131' AND `year` = 2025;

-- 删除：510185126 - 高明镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185126' AND `year` = 2025;

-- 删除：513425234 - 槽元乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425234' AND `year` = 2025;

-- 删除：510185128 - 壮溪镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185128' AND `year` = 2025;

-- 删除：510185127 - 武庙镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185127' AND `year` = 2025;

-- 删除：510185121 - 芦葭镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185121' AND `year` = 2025;

-- 删除：513425202 - 内东乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425202' AND `year` = 2025;

-- 删除：513425221 - 新安傣族乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425221' AND `year` = 2025;

-- 删除：513425219 - 树堡乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425219' AND `year` = 2025;

-- 删除：510703009 - 创业园街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510703009' AND `year` = 2025;

-- 删除：510703006 - 普明街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510703006' AND `year` = 2025;

-- 删除：510703013 - 塘汛街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510703013' AND `year` = 2025;

-- 删除：511722001 - 东乡街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722001' AND `year` = 2025;

-- 删除：511722002 - 蒲江街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722002' AND `year` = 2025;

-- 删除：511421146 - 贵平镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511421146' AND `year` = 2025;

-- 删除：510117020 - 西园街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510117020' AND `year` = 2025;

-- 删除：511102108 - 安谷镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511102108' AND `year` = 2025;

-- 删除：511421116 - 龙马镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511421116' AND `year` = 2025;

-- 删除：511421111 - 北斗镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511421111' AND `year` = 2025;

-- 删除：511421103 - 高家镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511421103' AND `year` = 2025;

-- 删除：510117019 - 合作街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510117019' AND `year` = 2025;

-- 删除：510302008 - 红旗街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510302008' AND `year` = 2025;

-- 删除：510302005 - 丹桂街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510302005' AND `year` = 2025;

-- 删除：510302006 - 学苑街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510302006' AND `year` = 2025;

-- 删除：510302009 - 高峰街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510302009' AND `year` = 2025;

-- 删除：510185016 - 玉成街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185016' AND `year` = 2025;

-- 删除：510185015 - 福田街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185015' AND `year` = 2025;

-- 删除：510185014 - 草池街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185014' AND `year` = 2025;

-- 删除：510185013 - 三岔街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185013' AND `year` = 2025;

-- 删除：510185017 - 丹景街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185017' AND `year` = 2025;

-- 删除：510185012 - 石板凳街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185012' AND `year` = 2025;

-- 删除：510185011 - 贾家街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185011' AND `year` = 2025;

-- 删除：510185010 - 养马街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185010' AND `year` = 2025;

-- 删除：510311001 - 板仓街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510311001' AND `year` = 2025;

-- 删除：510185009 - 石盘街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510185009' AND `year` = 2025;

-- 删除：511002006 - 壕子口街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511002006' AND `year` = 2025;

-- 删除：510703113 - 永兴镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510703113' AND `year` = 2025;

-- 删除：511011004 - 胜利街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511011004' AND `year` = 2025;

-- 删除：511011005 - 高桥街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511011005' AND `year` = 2025;

-- 删除：510603006 - 八角井街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510603006' AND `year` = 2025;

-- 删除：510603004 - 旌东街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510603004' AND `year` = 2025;

-- 删除：511502012 - 合江门街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511502012' AND `year` = 2025;

-- 删除：511502010 - 象鼻街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511502010' AND `year` = 2025;

-- 删除：511421004 - 视高街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511421004' AND `year` = 2025;

-- 删除：511002110 - 交通镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511002110' AND `year` = 2025;

-- 删除：511002106 - 靖民镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511002106' AND `year` = 2025;

-- 删除：511703004 - 管理委员会斌郎街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703004' AND `year` = 2025;

-- 删除：513425003 - 古城街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425003' AND `year` = 2025;

-- 删除：513425002 - 城南街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425002' AND `year` = 2025;

-- 删除：513425001 - 城北街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425001' AND `year` = 2025;

-- 删除：511403110 - 锦江镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511403110' AND `year` = 2025;

-- 删除：511403118 - 公义镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511403118' AND `year` = 2025;

-- 删除：511403121 - 黄丰镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511403121' AND `year` = 2025;

-- 删除：511502111 - 宗场镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511502111' AND `year` = 2025;

-- 删除：511502115 - 思坡镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511502115' AND `year` = 2025;

-- 删除：511502116 - 白花镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511502116' AND `year` = 2025;

-- 删除：511526108 - 沐滩镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511526108' AND `year` = 2025;

-- 删除：513425114 - 小黑箐镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425114' AND `year` = 2025;

-- 删除：513425113 - 六华镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425113' AND `year` = 2025;

-- 删除：513425112 - 木古镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425112' AND `year` = 2025;

-- 删除：513425111 - 彰冠镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425111' AND `year` = 2025;

-- 删除：511403003 - 青龙街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511403003' AND `year` = 2025;

-- 删除：513425106 - 绿水镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425106' AND `year` = 2025;

-- 删除：513425105 - 益门镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425105' AND `year` = 2025;

-- 删除：513425104 - 太平镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425104' AND `year` = 2025;

-- 删除：513425103 - 通安镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425103' AND `year` = 2025;

-- 删除：513425102 - 黎溪镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425102' AND `year` = 2025;

-- 删除：513425101 - 鹿厂镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425101' AND `year` = 2025;

-- 删除：513425109 - 关河镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425109' AND `year` = 2025;

-- 删除：513425108 - 云甸镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425108' AND `year` = 2025;

-- 删除：513425107 - 新发镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '513425107' AND `year` = 2025;

-- 删除：511722120 - 下八镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722120' AND `year` = 2025;

-- 删除：511722122 - 塔河镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722122' AND `year` = 2025;

-- 删除：511722123 - 茶河镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722123' AND `year` = 2025;

-- 删除：511722124 - 厂溪镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722124' AND `year` = 2025;

-- 删除：511722125 - 红峰镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722125' AND `year` = 2025;

-- 删除：511722127 - 白马镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722127' AND `year` = 2025;

-- 删除：511722128 - 桃花镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722128' AND `year` = 2025;

-- 删除：511722130 - 马渡关镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722130' AND `year` = 2025;

-- 删除：511722131 - 庙安镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722131' AND `year` = 2025;

-- 删除：511722132 - 上峡镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722132' AND `year` = 2025;

-- 删除：511722133 - 南坪镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511722133' AND `year` = 2025;

-- 删除：511703200 - 石板街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703200' AND `year` = 2025;

-- 删除：511703201 - 管理委员会幺塘乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703201' AND `year` = 2025;

-- 删除：510723001 - 凤灵街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510723001' AND `year` = 2025;

-- 删除：511703203 - 安仁乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703203' AND `year` = 2025;

-- 删除：510726103 - 永昌镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510726103' AND `year` = 2025;

-- 删除：510903011 - 灵泉街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903011' AND `year` = 2025;

-- 删除：510903012 - 慈音街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903012' AND `year` = 2025;

-- 删除：510903013 - 九莲街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903013' AND `year` = 2025;

-- 删除：510903014 - 南强街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903014' AND `year` = 2025;

-- 删除：510903016 - 西宁街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903016' AND `year` = 2025;

-- 删除：510903017 - 杨渡街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903017' AND `year` = 2025;

-- 删除：510903008 - 广德街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903008' AND `year` = 2025;

-- 删除：510903009 - 富源路街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903009' AND `year` = 2025;

-- 删除：510903007 - 嘉禾街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903007' AND `year` = 2025;

-- 删除：511504101 - 南广镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511504101' AND `year` = 2025;

-- 删除：511504113 - 合什镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511504113' AND `year` = 2025;

-- 删除：511504111 - 双龙镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511504111' AND `year` = 2025;

-- 删除：511703112 - 金垭镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703112' AND `year` = 2025;

-- 删除：511703110 - 河市镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703110' AND `year` = 2025;

-- 删除：511703100 - 亭子镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703100' AND `year` = 2025;

-- 删除：511703101 - 福善镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703101' AND `year` = 2025;

-- 删除：511703102 - 麻柳镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511703102' AND `year` = 2025;

-- 删除：510726205 - 漩坪乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510726205' AND `year` = 2025;

-- 删除：510726208 - 片口乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510726208' AND `year` = 2025;

-- 删除：510107064 - 桂溪街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510107064' AND `year` = 2025;

-- 删除：510107063 - 石羊街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510107063' AND `year` = 2025;

-- 删除：510107062 - 肖家河街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510107062' AND `year` = 2025;

-- 删除：510107061 - 芳草街街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510107061' AND `year` = 2025;

-- 删除：510903108 - 保升镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903108' AND `year` = 2025;

-- 删除：510903109 - 北固镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903109' AND `year` = 2025;

-- 删除：510903105 - 新桥镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510903105' AND `year` = 2025;

-- 删除：511528200 - 大坝苗族乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511528200' AND `year` = 2025;

-- 删除：511525112 - 落润镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511525112' AND `year` = 2025;

-- 删除：511525113 - 庆岭镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511525113' AND `year` = 2025;

-- 删除：511504203 - 凤仪乡
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511504203' AND `year` = 2025;

-- 删除：512002100 - 管理委员会雁江镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '512002100' AND `year` = 2025;

-- 删除：512002101 - 管理委员会松涛镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '512002101' AND `year` = 2025;

-- 删除：512002103 - 管理委员会临江镇
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '512002103' AND `year` = 2025;

-- 删除：510116018 - 成都直管区万安街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116018' AND `year` = 2025;

-- 删除：510116019 - 成都直管区正兴街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116019' AND `year` = 2025;

-- 删除：510116024 - 成都直管区太平街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116024' AND `year` = 2025;

-- 删除：510116025 - 成都直管区永兴街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116025' AND `year` = 2025;

-- 删除：510116020 - 成都直管区兴隆街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116020' AND `year` = 2025;

-- 删除：510116021 - 成都直管区煎茶街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116021' AND `year` = 2025;

-- 删除：510116022 - 成都直管区新兴街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116022' AND `year` = 2025;

-- 删除：510116023 - 成都直管区籍田街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116023' AND `year` = 2025;

-- 删除：510116003 - 成都直管区华阳街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116003' AND `year` = 2025;

-- 删除：510116004 - 中和街道
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '510116004' AND `year` = 2025;

-- ============================================
-- 2.3 变更数据（名称发生变化的）
-- ============================================

-- 变更：512021117 - 镇 -> 镇子镇
UPDATE `grassroots_organization` SET `name` = '镇子镇', `township_name` = '镇子镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '512021117' AND `year` = 2025;

-- 变更：513301211 - 呷巴乡 -> 普沙绒乡
UPDATE `grassroots_organization` SET `name` = '普沙绒乡', `township_name` = '普沙绒乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513301211' AND `year` = 2025;

-- 变更：513301214 - 孔玉乡 -> 呷巴乡
UPDATE `grassroots_organization` SET `name` = '呷巴乡', `township_name` = '呷巴乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513301214' AND `year` = 2025;

-- 变更：513301208 - 普沙绒乡 -> 捧塔乡
UPDATE `grassroots_organization` SET `name` = '捧塔乡', `township_name` = '捧塔乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513301208' AND `year` = 2025;

-- 变更：511823209 - 富乡 -> 富乡乡
UPDATE `grassroots_organization` SET `name` = '富乡乡', `township_name` = '富乡乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511823209' AND `year` = 2025;

-- 变更：511621104 - 镇 -> 镇裕镇
UPDATE `grassroots_organization` SET `name` = '镇裕镇', `township_name` = '镇裕镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511621104' AND `year` = 2025;

-- 变更：510682002 - 皂角街道 -> 雍城街道
UPDATE `grassroots_organization` SET `name` = '雍城街道', `township_name` = '雍城街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510682002' AND `year` = 2025;

-- 变更：511527107 - 镇 -> 镇舟镇
UPDATE `grassroots_organization` SET `name` = '镇舟镇', `township_name` = '镇舟镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511527107' AND `year` = 2025;

-- 变更：513301001 - 榆林街道 -> 炉城街道
UPDATE `grassroots_organization` SET `name` = '炉城街道', `township_name` = '炉城街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513301001' AND `year` = 2025;

-- 变更：513301002 - 炉城街道 -> 榆林街道
UPDATE `grassroots_organization` SET `name` = '榆林街道', `township_name` = '榆林街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513301002' AND `year` = 2025;

-- 变更：510185112 - 镇 -> 镇金镇
UPDATE `grassroots_organization` SET `name` = '镇金镇', `township_name` = '镇金镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510185112' AND `year` = 2025;

-- 变更：510411105 - 技术产业开发区金江镇 -> 金江镇
UPDATE `grassroots_organization` SET `name` = '金江镇', `township_name` = '金江镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510411105' AND `year` = 2025;

-- 变更：511123115 - 九井镇九井街道 -> 九井镇
UPDATE `grassroots_organization` SET `name` = '九井镇', `township_name` = '九井镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511123115' AND `year` = 2025;

-- 变更：511502013 - 大观楼街道 -> 双城街道
UPDATE `grassroots_organization` SET `name` = '双城街道', `township_name` = '双城街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511502013' AND `year` = 2025;

-- 变更：511502014 - 双城街道 -> 合江门街道
UPDATE `grassroots_organization` SET `name` = '合江门街道', `township_name` = '合江门街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511502014' AND `year` = 2025;

-- 变更：511002105 - 全安镇街道 -> 全安镇
UPDATE `grassroots_organization` SET `name` = '全安镇', `township_name` = '全安镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511002105' AND `year` = 2025;

-- ���更：513322102 - 兴隆镇街道 -> 兴隆镇
UPDATE `grassroots_organization` SET `name` = '兴隆镇', `township_name` = '兴隆镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513322102' AND `year` = 2025;

-- 变更：513322101 - 冷碛镇冷碛镇老街道 -> 冷碛镇
UPDATE `grassroots_organization` SET `name` = '冷碛镇', `township_name` = '冷碛镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513322101' AND `year` = 2025;

-- 变更：510525110 - 双沙镇 -> 白沙场镇
UPDATE `grassroots_organization` SET `name` = '白沙场镇', `township_name` = '白沙场镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510525110' AND `year` = 2025;

-- 变更：511681106 - 高兴镇高兴镇街道 -> 高兴镇
UPDATE `grassroots_organization` SET `name` = '高兴镇', `township_name` = '高兴镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511681106' AND `year` = 2025;

-- 变更：511681105 - 阳和镇阳和镇街道 -> 阳和镇
UPDATE `grassroots_organization` SET `name` = '阳和镇', `township_name` = '阳和镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511681105' AND `year` = 2025;

-- 变更：511681104 - 明月镇明月镇街道 -> 明月镇
UPDATE `grassroots_organization` SET `name` = '明月镇', `township_name` = '明月镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511681104' AND `year` = 2025;

-- 变更：511923114 - 镇 -> 镇龙镇
UPDATE `grassroots_organization` SET `name` = '镇龙镇', `township_name` = '镇龙镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511923114' AND `year` = 2025;

-- 变更：511681103 - 永兴镇永兴镇街道 -> 永兴镇
UPDATE `grassroots_organization` SET `name` = '永兴镇', `township_name` = '永兴镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511681103' AND `year` = 2025;

-- 变更：511681102 - 禄市镇禄市镇街道 -> 禄市镇
UPDATE `grassroots_organization` SET `name` = '禄市镇', `township_name` = '禄市镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511681102' AND `year` = 2025;

-- 变更：513224104 - 镇 -> 镇江关镇
UPDATE `grassroots_organization` SET `name` = '镇江关镇', `township_name` = '镇江关镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513224104' AND `year` = 2025;

-- 变更：511133104 - 烟峰镇 -> 下溪镇
UPDATE `grassroots_organization` SET `name` = '下溪镇', `township_name` = '下溪镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511133104' AND `year` = 2025;

-- 变更：511133102 - 下溪镇 -> 烟峰镇
UPDATE `grassroots_organization` SET `name` = '烟峰镇', `township_name` = '烟峰镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511133102' AND `year` = 2025;

-- 变更：510981110 - 青岗镇街道 -> 青岗镇
UPDATE `grassroots_organization` SET `name` = '青岗镇', `township_name` = '青岗镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510981110' AND `year` = 2025;

-- 变更：510981112 - 香山镇街道 -> 香山镇
UPDATE `grassroots_organization` SET `name` = '香山镇', `township_name` = '香山镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510981112' AND `year` = 2025;

-- 变更：511502118 - 双谊镇 -> 白花镇
UPDATE `grassroots_organization` SET `name` = '白花镇', `township_name` = '白花镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511502118' AND `year` = 2025;

-- 变更：511124113 - 镇 -> 镇阳镇
UPDATE `grassroots_organization` SET `name` = '镇阳镇', `township_name` = '镇阳镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511124113' AND `year` = 2025;

-- 变更：511502120 - 永兴镇 -> 双谊镇
UPDATE `grassroots_organization` SET `name` = '双谊镇', `township_name` = '双谊镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511502120' AND `year` = 2025;

-- 变更：511504003 - 赵场街道 -> 柏溪街道
UPDATE `grassroots_organization` SET `name` = '柏溪街道', `township_name` = '柏溪街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504003' AND `year` = 2025;

-- 变更：511504002 - 南岸街道 -> 赵场街道
UPDATE `grassroots_organization` SET `name` = '赵场街道', `township_name` = '赵场街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504002' AND `year` = 2025;

-- 变更：511504001 - 柏溪街道 -> 南岸街道
UPDATE `grassroots_organization` SET `name` = '南岸街道', `township_name` = '南岸街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504001' AND `year` = 2025;

-- 变更：511024109 - 镇 -> 镇西镇
UPDATE `grassroots_organization` SET `name` = '镇西镇', `township_name` = '镇西镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511024109' AND `year` = 2025;

-- 变更：510726104 - 通泉镇 -> 永安镇
UPDATE `grassroots_organization` SET `name` = '永安镇', `township_name` = '永安镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726104' AND `year` = 2025;

-- 变更：510726105 - 永安镇 -> 永昌镇
UPDATE `grassroots_organization` SET `name` = '永昌镇', `township_name` = '永昌镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726105' AND `year` = 2025;

-- 变更：510726108 - 陈家坝镇 -> 小坝镇
UPDATE `grassroots_organization` SET `name` = '小坝镇', `township_name` = '小坝镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726108' AND `year` = 2025;

-- 变更：510726109 - 小坝镇 -> 陈家坝镇
UPDATE `grassroots_organization` SET `name` = '陈家坝镇', `township_name` = '陈家坝镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726109' AND `year` = 2025;

-- 变更：510623214 - 永丰乡街道 -> 永丰乡
UPDATE `grassroots_organization` SET `name` = '永丰乡', `township_name` = '永丰乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510623214' AND `year` = 2025;

-- 变更：511504106 - 泥溪镇 -> 蕨溪镇
UPDATE `grassroots_organization` SET `name` = '蕨溪镇', `township_name` = '蕨溪镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504106' AND `year` = 2025;

-- 变更：511504105 - 柳嘉镇 -> 泥溪镇
UPDATE `grassroots_organization` SET `name` = '泥溪镇', `township_name` = '泥溪镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504105' AND `year` = 2025;

-- 变更：511504104 - 横江镇 -> 柳嘉镇
UPDATE `grassroots_organization` SET `name` = '柳嘉镇', `township_name` = '柳嘉镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504104' AND `year` = 2025;

-- 变更：511504103 - 观音镇 -> 横江镇
UPDATE `grassroots_organization` SET `name` = '横江镇', `township_name` = '横江镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504103' AND `year` = 2025;

-- 变更：511504109 - 高场镇 -> 安边镇
UPDATE `grassroots_organization` SET `name` = '安边镇', `township_name` = '安边镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504109' AND `year` = 2025;

-- 变更：511504108 - 商州镇 -> 高场镇
UPDATE `grassroots_organization` SET `name` = '高场镇', `township_name` = '高场镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504108' AND `year` = 2025;

-- 变更：511504107 - 蕨溪镇 -> 商州镇
UPDATE `grassroots_organization` SET `name` = '商州镇', `township_name` = '商州镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504107' AND `year` = 2025;

-- 变更：511504110 - 安边镇 -> 双龙镇
UPDATE `grassroots_organization` SET `name` = '双龙镇', `township_name` = '双龙镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504110' AND `year` = 2025;

-- 变更：510681002 - 新丰街道 -> 汉州街道
UPDATE `grassroots_organization` SET `name` = '汉州街道', `township_name` = '汉州街道', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510681002' AND `year` = 2025;

-- 变更：511323114 - 兴旺镇街道 -> 兴旺镇
UPDATE `grassroots_organization` SET `name` = '兴旺镇', `township_name` = '兴旺镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323114' AND `year` = 2025;

-- 变更：511323110 - 杨家镇杨家镇街道 -> 杨家镇
UPDATE `grassroots_organization` SET `name` = '杨家镇', `township_name` = '杨家镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323110' AND `year` = 2025;

-- 变更：511323112 - 福德镇街道 -> 福德镇
UPDATE `grassroots_organization` SET `name` = '福德镇', `township_name` = '福德镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323112' AND `year` = 2025;

-- 变更：511323111 - 罗家镇街道 -> 罗家镇
UPDATE `grassroots_organization` SET `name` = '罗家镇', `township_name` = '罗家镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323111' AND `year` = 2025;

-- 变更：511323103 - 正源镇街道 -> 正源镇
UPDATE `grassroots_organization` SET `name` = '正源镇', `township_name` = '正源镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323103' AND `year` = 2025;

-- 变更：511323102 - 巨龙镇街道 -> 巨龙镇
UPDATE `grassroots_organization` SET `name` = '巨龙镇', `township_name` = '巨龙镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323102' AND `year` = 2025;

-- 变更：511323109 - 龙蚕镇街道 -> 龙蚕镇
UPDATE `grassroots_organization` SET `name` = '龙蚕镇', `township_name` = '龙蚕镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323109' AND `year` = 2025;

-- 变更：511323108 - 利溪镇街道 -> 利溪镇
UPDATE `grassroots_organization` SET `name` = '利溪镇', `township_name` = '利溪镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511323108' AND `year` = 2025;

-- 变更：510726209 - 开坪乡 -> 桃龙藏族乡
UPDATE `grassroots_organization` SET `name` = '桃龙藏族乡', `township_name` = '桃龙藏族乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726209' AND `year` = 2025;

-- 变更：510726206 - 白坭乡 -> 漩坪乡
UPDATE `grassroots_organization` SET `name` = '漩坪乡', `township_name` = '漩坪乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726206' AND `year` = 2025;

-- 变更：513224207 - 镇 -> 镇坪乡
UPDATE `grassroots_organization` SET `name` = '镇坪乡', `township_name` = '镇坪乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513224207' AND `year` = 2025;

-- 变更：510726212 - 青片乡 -> 坝底乡
UPDATE `grassroots_organization` SET `name` = '坝底乡', `township_name` = '坝底乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726212' AND `year` = 2025;

-- 变更：510726214 - 桃龙藏族乡 -> 马槽乡
UPDATE `grassroots_organization` SET `name` = '马槽乡', `township_name` = '马槽乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726214' AND `year` = 2025;

-- 变更：510726210 - 坝底乡 -> 片口乡
UPDATE `grassroots_organization` SET `name` = '片口乡', `township_name` = '片口乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726210' AND `year` = 2025;

-- 变更：510726211 - 白什乡 -> 开坪乡
UPDATE `grassroots_organization` SET `name` = '开坪乡', `township_name` = '开坪乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726211' AND `year` = 2025;

-- 变更：510726216 - 马槽乡 -> 青片乡
UPDATE `grassroots_organization` SET `name` = '青片乡', `township_name` = '青片乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '510726216' AND `year` = 2025;

-- 变更：513423104 - 树河镇树河街道 -> 树河镇
UPDATE `grassroots_organization` SET `name` = '树河镇', `township_name` = '树河镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513423104' AND `year` = 2025;

-- 变更：513423101 - 卫城镇街道 -> 卫城镇
UPDATE `grassroots_organization` SET `name` = '卫城镇', `township_name` = '卫城镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513423101' AND `year` = 2025;

-- 变更：513423102 - 梅雨镇街道 -> 梅雨镇
UPDATE `grassroots_organization` SET `name` = '梅雨镇', `township_name` = '梅雨镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513423102' AND `year` = 2025;

-- 变更：513423107 - 泸沽湖镇格姆街道 -> 泸沽湖镇
UPDATE `grassroots_organization` SET `name` = '泸沽湖镇', `township_name` = '泸沽湖镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513423107' AND `year` = 2025;

-- 变更：513423106 - 平川镇平川街道 -> 平川镇
UPDATE `grassroots_organization` SET `name` = '平川镇', `township_name` = '平川镇', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '513423106' AND `year` = 2025;

-- 变更：511504202 - 龙池乡 -> 凤仪乡
UPDATE `grassroots_organization` SET `name` = '凤仪乡', `township_name` = '凤仪乡', `update_time` = '2026-03-12 23:37:00' WHERE `code` = '511504202' AND `year` = 2025;

