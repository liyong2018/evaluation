-- 添加缺失的地区数据
-- 基于现有的511425001(汉阳镇)，添加其他相关地区

-- 首先查看现有的青神县(511425)的ID
SET @county_id = (SELECT id FROM region WHERE code = '511425');

-- 添加缺失的镇级地区数据
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('511425102', '西龙镇', @county_id, 4, 2, 1),
('511425108', '青竹街道', @county_id, 4, 3, 1),
('511425110', '南城镇', @county_id, 4, 4, 1),
('511425112', '黑龙镇', @county_id, 4, 5, 1),
('511425217', '瑞峰镇', @county_id, 4, 6, 1),
('511425218', '罗波乡', @county_id, 4, 7, 1)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    parent_id = VALUES(parent_id),
    level = VALUES(level),
    sort = VALUES(sort),
    status = VALUES(status);

-- 验证插入结果
SELECT code, name, level FROM region WHERE code LIKE '511425%' ORDER BY code;

-- 验证特定地区代码
SELECT * FROM region WHERE code = '511425102';