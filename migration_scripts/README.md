# 数据库迁移指南：MySQL 到 Supabase (PostgreSQL)

## 迁移状态
✅ **已迁移 (2/18 表)**
- evaluation_result (265 行)
- model_execution_record (17 行)

⏳ **待迁移 (16/18 表)**

## 迁移文件说明

### 01_create_all_tables.sql
包含所有 16 个待迁移表的 PostgreSQL DDL 定义。
在 Supabase SQL Editor 中执行此脚本。

### 02_data_dump.sql
从 MySQL 导出的原始数据（仅核心表）。
格式：MySQL

### 03_convert_data.sql
数据类型转换参考文档。
说明：MySQL 到 PostgreSQL 的数据类型转换规则。

### 04_data_for_pg.sql
已转换为 PostgreSQL 格式的数据。
包含：survey_data, weight_config, indicator_weight, evaluation_model, algorithm_config

## 迁移步骤

### 步骤 1：创建表结构
1. 登录 Supabase Dashboard
2. 进入 SQL Editor
3. 复制并执行 `01_create_all_tables.sql`
4. 确认所有表已创建

### 步骤 2：导入数据
1. 在 Supabase SQL Editor 中
2. 分段复制 `04_data_for_pg.sql` 中的数据插入语句
3. 逐表执行 INSERT 语句
4. 验证数据导入

### 步骤 3：验证数据
执行以下查询验证：
```sql
-- 检查表和数据量
SELECT 'survey_data' as table_name, count(*) as row_count FROM survey_data
UNION ALL
SELECT 'weight_config', count(*) FROM weight_config
UNION ALL
SELECT 'indicator_weight', count(*) FROM indicator_weight
UNION ALL
SELECT 'evaluation_model', count(*) FROM evaluation_model
UNION ALL
SELECT 'algorithm_config', count(*) FROM algorithm_config
UNION ALL
SELECT 'evaluation_result', count(*) FROM evaluation_result
UNION ALL
SELECT 'model_execution_record', count(*) FROM model_execution_record;
```

## 数据类型转换对照表

| MySQL 类型 | PostgreSQL 类型 | 备注 |
|-----------|----------------|------|
| tinyint(1) | boolean | 布尔类型 |
| int | integer | 整数类型 |
| bigint | bigint | 长整数 |
| varchar(n) | varchar(n) | 字符串 |
| text | text | 文本 |
| decimal(m,n) | numeric(m,n) | 精确数值 |
| timestamp | timestamptz | 时间戳 |
| datetime | timestamptz | 日期时间 |
| auto_increment | bigserial | 自增主键 |
| NULL | NULL | 空值 |

## 注意事项

1. **外键关系**：导入数据时需注意表之间的依赖关系
2. **数据一致性**：确保所有相关表数据完整
3. **索引**：导入完成后重新创建必要的索引
4. **序列**：自增列的序列值已重置

## 表依赖关系

```
organization (顶层)
  ↓
evaluation_model
  ↓
model_step
  ↓
step_algorithm
  ↓
step_execution_result
  ↓
report

weight_config
  ↓
indicator_weight

algorithm_config
  ↓
algorithm_step
```

## 迁移后任务

1. 验证所有表数据正确
2. 检查应用连接 Supabase 数据库
3. 更新数据源配置
4. 测试应用功能

## 备份表说明

以下表为备份/临时表，可选择是否迁移：
- step_algorithm_copy1
- step_algorithm_backup_20250121
- evaluation_result_copy1
- indicator_weight_backup_20250121

## 联系支持

如遇到迁移问题，请提供：
1. 错误日志
2. 表结构对比
3. 数据量统计

---
生成时间：2025-11-06
