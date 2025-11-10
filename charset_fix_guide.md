# 数据库中文字符乱码修复指南

## 问题现象
- 数据库列注释显示为乱码：`骞存湯鎬绘埛鏁?鎴?`
- 数据内容可能也存在乱码问题

## 根本原因
数据库客户端连接时使用了错误的字符编码设置

## 解决方案

### 1. 确认数据库字符集正确
```sql
-- 检查数据库字符集
SHOW VARIABLES LIKE 'character_set%';

-- 检查表字符集
SHOW TABLE STATUS LIKE 'survey_data';
```

### 2. 使用正确的字符集连接数据库

#### 命令行连接：
```bash
mysql -h127.0.0.1 -P30314 -uroot -p123456 --default-character-set=utf8mb4
```

#### 应用程序连接：
JDBC URL 中已配置：
```
characterEncoding=utf8mb4
```

### 3. 验证修复效果
```sql
-- 连接后执行
USE evaluate_db;
SET NAMES utf8mb4;
SHOW FULL COLUMNS FROM survey_data WHERE Field = 'total_households';
```

### 4. 如果仍有乱码，执行完全修复脚本：
```sql
-- 完全重建表注释
USE evaluate_db;
SET NAMES utf8mb4;

-- 删除并重建表（如果数据不重要的前提下）
-- 或者逐个修改列注释
ALTER TABLE survey_data MODIFY COLUMN total_households INT NULL COMMENT '年末总户数(户)';
```

### 5. 重启应用
确保Spring Boot应用重启以应用新的JDBC字符集配置

## 测试验证
插入一条包含中文的测试数据：
```sql
INSERT INTO survey_data (region_code, township, main_disaster_types)
VALUES ('TEST001', '测试乡镇', '洪水;地震');
```

## 注意事项
- 确保所有数据库客户端工具（如MySQL Workbench、Navicat等）也配置为UTF-8
- 数据库本身字符集已正确设置为utf8mb4
- 主要是客户端显示和连接的问题