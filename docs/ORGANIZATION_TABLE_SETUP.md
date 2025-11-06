# 组织机构表部署指南

## 概述

本文档说明如何创建和使用组织机构表（`organization`）。

## 数据库表创建

### 方法1：直接执行SQL文件

```bash
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/migrations/005_create_organization_table.sql
```

### 方法2：手动执行SQL

连接到数据库后执行：

```sql
CREATE TABLE IF NOT EXISTS `organization` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT NULL COMMENT '父级机构ID',
    `code` VARCHAR(32) NOT NULL COMMENT '机构编码（行政区划代码）',
    `name` VARCHAR(128) NOT NULL COMMENT '机构名称',
    `level` TINYINT NOT NULL COMMENT '级别：1省、2市、3县、4乡镇、5社区',
    `data_source` VARCHAR(32) NOT NULL COMMENT '来源：COMMUNITY/TOWNSHIP 等',
    `province_name` VARCHAR(128) NULL COMMENT '省名称',
    `city_name` VARCHAR(128) NULL COMMENT '市名称',
    `county_name` VARCHAR(128) NULL COMMENT '县名称',
    `township_name` VARCHAR(128) NULL COMMENT '乡镇名称',
    `community_name` VARCHAR(128) NULL COMMENT '社区名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_organization_code` (`code`),
    KEY `idx_organization_parent` (`parent_id`),
    KEY `idx_organization_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织机构表';
```

### 验证表是否创建成功

```sql
-- 检查表是否存在
SHOW TABLES LIKE 'organization';

-- 查看表结构
DESC organization;

-- 查看表索引
SHOW INDEX FROM organization;
```

## 功能说明

### 自动数据同步

当您导入乡镇或社区数据时，系统会**自动**将组织机构信息同步到 `organization` 表：

1. **导入乡镇数据** → 自动创建：省/市/县/乡镇层级
2. **导入社区数据** → 自动创建：省/市/县/乡镇/社区层级

相关代码：
- `CommunityDisasterReductionCapacityServiceImpl.java:88` - 社区数据导入时同步
- `SurveyDataServiceImpl.java` - 乡镇数据导入时同步
- `OrganizationServiceImpl.java:36-106` - 组织机构同步逻辑

### API接口

新增的组织机构管理API：

#### 1. 分页查询
```
GET /api/organization/list?page=1&size=10&code=&name=&level=&parentId=
```

#### 2. 根据ID查询
```
GET /api/organization/{id}
```

#### 3. 根据编码查询
```
GET /api/organization/code/{code}
```

#### 4. 获取树形结构
```
GET /api/organization/tree?parentId=&maxLevel=
```

#### 5. 获取子级组织
```
GET /api/organization/children/{parentId}
```

#### 6. 关键词搜索
```
GET /api/organization/search?keyword=&level=
```

#### 7. 按层级查询
```
GET /api/organization/provinces           # 所有省
GET /api/organization/cities?provinceCode=  # 指定省的市
GET /api/organization/counties?cityCode=    # 指定市的县
GET /api/organization/townships?countyCode= # 指定县的乡镇
GET /api/organization/communities?townshipCode= # 指定乡镇的社区
```

## 表结构说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| parent_id | BIGINT | 父级机构ID |
| code | VARCHAR(32) | 机构编码（行政区划代码） |
| name | VARCHAR(128) | 机构名称 |
| level | TINYINT | 级别：1省、2市、3县、4乡镇、5社区 |
| data_source | VARCHAR(32) | 来源：COMMUNITY/TOWNSHIP |
| province_name | VARCHAR(128) | 省名称 |
| city_name | VARCHAR(128) | 市名称 |
| county_name | VARCHAR(128) | 县名称 |
| township_name | VARCHAR(128) | 乡镇名称 |
| community_name | VARCHAR(128) | 社区名称 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 0-否 1-是 |

## 使用场景

1. **统一的组织机构数据源**：所有涉及省市县乡村的功能都从这个表获取数据
2. **层级关系管理**：通过 `parent_id` 和 `level` 字段维护组织层级
3. **行政区划编码管理**：使用标准的行政区划代码（code字段）
4. **数据来源追溯**：记录数据来源（COMMUNITY或TOWNSHIP）

## 注意事项

1. 表使用 `CREATE TABLE IF NOT EXISTS`，多次执行不会报错
2. `code` 字段有唯一索引，同一编码不会重复插入
3. 使用逻辑删除（`is_deleted`），不会物理删除数据
4. 自动维护 `create_time` 和 `update_time`
