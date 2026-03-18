# 组织机构数据 SQL 生成工具 - 使用说明

## 功能概述

`OrganizationSqlUtil` 是一个用于生成组织机构数据变更 SQL 脚本的工具类。它会从 GeoJSON 文件中读取 2024 年乡镇数据，与数据库中的基准数据进行对比，然后生成包含以下内容的 SQL 脚本：

1. **备份说明** - 如何使用 mysqldump 备份现有数据
2. **新增数据** - 插入 GeoJSON 中有但数据库中还没有的记录
3. **删除数据** - 软删除数据库中有但 GeoJSON 中没有的记录
4. **变更数据** - 更新名称发生变化的记录

## 使用方式

### 方式一：通过 API 生成（推荐）

启动应用后，调用以下 API：

```bash
# 生成 SQL 脚本
curl -X POST "http://localhost:8081/api/organization-import/generate-sql?filePath=frontend/public/zzjg/2024-xzjznl-example.geojson&year=2024" \
  -H "Content-Type: application/json"

# 下载 SQL 文件
curl -O -J "http://localhost:8081/api/organization-import/download-sql?filePath=frontend/public/zzjg/2024-xzjznl-example.geojson&year=2024"
```

### 方式二：通过测试类生成

运行测试方法：

```bash
mvn test -Dtest=OrganizationImportServiceTest#testGenerate2024TownshipChangeSql
```

SQL 文件将保存到 `docs/organization_2024_change.sql`

### 方式三：直接在代码中调用

```java
@Autowired
private OrganizationSqlUtil organizationSqlUtil;

// 生成 SQL
SqlScriptResult result = organizationSqlUtil.generate2024TownshipChangeSql(
    "frontend/public/zzjg/2024-xzjznl-example.geojson",
    2024
);

// 保存 SQL 到文件
Files.writeString(Paths.get("output.sql"), result.getSql());
```

## SQL 脚本结构

生成的 SQL 脚本包含以下几个部分：

### 1. 文件头信息

```sql
-- ============================================
-- 2024 年乡镇组织机构数据变更 SQL
-- 生成时间：2026-03-12 20:03:23
-- 目标年份：2024
-- 基准年份：2020
-- ============================================
```

### 2. 备份说明

```sql
-- 备份 organization 表中的 2020 年乡镇数据
-- mysqldump -u 用户名 -p 数据库名 organization --where="year=2020 AND level=4" > backup_organization_2020_township.sql
```

### 3. 新增数据

为每个新增的乡镇生成两条 INSERT 语句（organization 表和 grassroots_organization 表）：

```sql
-- 新增：512021126 - 南薰镇
INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, '512021126', '南薰镇', 4, 2024, '四川省', '资阳市', '安岳县', '南薰镇', 0, 0, '2026-03-12 20:03:23', '2026-03-12 20:03:23');

INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES
  (NULL, NULL, '512021126', '南薰镇', 4, 2024, '四川省', '资阳市', '安岳县', '南薰镇', 0, 0, '2026-03-12 20:03:23', '2026-03-12 20:03:23');
```

### 4. 删除数据（软删除）

```sql
-- 删除：511902010 - 已撤并街道
UPDATE `organization` SET `is_deleted` = 1 WHERE `code` = '511902010' AND `year` = 2024;
UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '511902010' AND `year` = 2024;
```

### 5. 变更数据

```sql
-- 变更：511902011 - 旧街道名 -> 新街道名
UPDATE `organization` SET `name` = '新街道名', `township_name` = '新街道名', `update_time` = '2026-03-12 20:03:23' WHERE `code` = '511902011' AND `year` = 2024;
UPDATE `grassroots_organization` SET `name` = '新街道名', `township_name` = '新街道名', `update_time` = '2026-03-12 20:03:23' WHERE `code` = '511902011' AND `year` = 2024;
```

## 执行流程

1. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

2. **调用 API 生成 SQL**
   ```bash
   curl -X POST "http://localhost:8081/api/organization-import/generate-sql?filePath=frontend/public/zzjg/2024-xzjznl-example.geojson&year=2024"
   ```

3. **保存 SQL 文件**
   将返回的 SQL 内容保存到文件，例如 `organization_2024_change.sql`

4. **备份数据库**（推荐）
   ```bash
   mysqldump -u root -p evaluation organization --where="year=2020 AND level=4" > backup_organization_2020_township.sql
   mysqldump -u root -p evaluation grassroots_organization --where="year=2020 AND level=4" > backup_grassroots_organization_2020_township.sql
   ```

5. **执行 SQL 脚本**
   ```bash
   mysql -u root -p evaluation < organization_2024_change.sql
   ```

## 统计信息

API 返回的 JSON 包含以下统计信息：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "summary": "新增：3110, 删除：0, 变更：0",
    "addedCount": 3110,
    "removedCount": 0,
    "changedCount": 0,
    "sql": "..."
  }
}
```

## 注意事项

1. **数据备份**：在执行 SQL 脚本前，强烈建议先备份数据库
2. **父级关系**：新增记录的父级 ID 根据区县代码（前 6 位）自动查找 2020 年基准数据
3. **软删除**：删除操作使用软删除（`is_deleted=1`），不会物理删除记录
4. **年份隔离**：2024 年的数据与 2020 年基准数据相互独立
5. **事务**：SQL 脚本中的每条语句都是独立的，建议在事务中执行

## 相关文件

| 文件 | 说明 |
|------|------|
| `src/main/java/com/evaluate/util/OrganizationSqlUtil.java` | SQL 生成工具类 |
| `src/main/java/com/evaluate/controller/OrganizationImportController.java` | API 控制器 |
| `src/test/java/com/evaluate/service/OrganizationImportServiceTest.java` | 测试类 |
| `frontend/public/zzjg/2024-xzjznl-example.geojson` | 示例数据文件 |
