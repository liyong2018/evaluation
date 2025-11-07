-- ============================================
-- 数据类型转换脚本
-- ============================================
-- 此脚本将 MySQL 导出的数据转换为 PostgreSQL 格式

-- 1. 将 tinyint(1) 转换为 boolean
-- 2. 将 auto_increment 字段的显式插入移除
-- 3. 转换 timestamp 为 timestamptz
-- 4. 转换 decimal 为 numeric

-- 示例转换规则：
-- INSERT INTO `table` (id, ...) VALUES (1, ...) 
-- 转换为：
-- INSERT INTO table (id, ...) VALUES (1, ...)  -- 移除反引号

-- 转换脚本使用 sed 命令：
-- sed -i 's/`//g' data_dump.sql  -- 移除反引号
-- sed -i 's/tinyint(1)/boolean/g' data_dump.sql  -- 转换布尔类型
-- sed -i 's/CURRENT_TIMESTAMP/now()/g' data_dump.sql  -- 转换时间戳

-- 或者使用 Python 脚本来转换
