# 区域表删除变更记录

## 变更日期
2025-10-30

## 变更原因
原有的 `region` 表设计冗余，区域信息已经存在于业务数据表中（`survey_data` 和 `community_disaster_reduction_capacity`），无需单独维护区域表。

## 变更内容

### 1. 数据库变更
- **删除表**: `region`
- **影响**: 区域数据现在从以下表动态查询：
  - **乡镇模型**: `survey_data` 表的 `province`, `city`, `county`, `township` 字段
  - **社区模型**: `community_disaster_reduction_capacity` 表的 `province_name`, `city_name`, `county_name`, `township_name`, `community_name` 字段

### 2. 后端代码变更

#### 删除的文件
- `src/main/java/com/evaluate/entity/Region.java`
- `src/main/java/com/evaluate/mapper/RegionMapper.java`
- `src/main/java/com/evaluate/service/RegionService.java`
- `src/main/java/com/evaluate/service/impl/RegionServiceImpl.java`
- `src/main/java/com/evaluate/controller/RegionController.java`

#### 修改的文件
- `src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`
  - 移除 `RegionMapper` 依赖
  - 修改区域名称获取逻辑，从数据表中直接查询
  - 第224-249行：根据 modelId 选择不同数据源（survey_data 或 community_disaster_reduction_capacity）

- `src/main/java/com/evaluate/controller/RegionDataController.java`
  - 保持不变，已经实现了基于 dataType 参数从不同表查询区域数据

- `src/main/java/com/evaluate/service/impl/RegionDataServiceImpl.java`
  - 保持不变，已经实现了动态查询逻辑

### 3. 前端代码变更

#### 修改的文件
- `frontend/src/api/index.ts`
  - 注释掉 `regionApi` 相关接口（已废弃）
  - 保留 `regionDataApi`（用于省市县三级联动查询）

- `frontend/src/views/DataManagement.vue`
  - 修改 `loadRegionNameMap` 函数，不再调用 region API
  - 区域名称直接使用数据表中的字段

### 4. API变更

#### 废弃的API端点
以下 `/api/region/*` 端点已被删除：
- `GET /api/region/tree` - 获取地区树形结构
- `GET /api/region/children/{parentId}` - 根据父级ID获取子级地区
- `GET /api/region/level/{level}` - 根据级别获取地区列表
- `GET /api/region/code/{code}` - 根据地区代码获取地区信息
- `POST /api/region/batch` - 根据地区ID列表获取地区信息
- `GET /api/region/all` - 获取所有启用的地区

#### 保留并增强的API端点
以下 `/api/region/*` 端点保留并增强，支持根据数据类型动态查询：
- `GET /api/region/provinces?dataType={township|community}` - 获取省份列表
- `GET /api/region/cities?dataType=xxx&provinceName=xxx` - 获取城市列表
- `GET /api/region/counties?dataType=xxx&provinceName=xxx&cityName=xxx` - 获取区县列表
- `GET /api/region/data?dataType=xxx&provinceName=xxx&cityName=xxx&countyName=xxx` - 获取具体数据

**参数说明**:
- `dataType`: 数据类型
  - `township`: 从 `survey_data` 表查询（乡镇模型）
  - `community`: 从 `community_disaster_reduction_capacity` 表查询（社区模型）

### 5. 数据迁移说明

#### 对现有数据的影响
- **无需数据迁移**: 区域信息已存在于业务数据表中
- **无数据丢失**: 所有区域名称信息都在业务数据中保留

#### 部署步骤
1. 备份数据库（可选）
2. 部署新版本后端代码
3. 删除 `region` 表（如果存在）:
   ```sql
   DROP TABLE IF EXISTS region;
   ```
4. 部署新版本前端代码

### 6. 核心执行流程变更

#### 原流程
```
用户选择模型 + 区域 (从region表)
  ↓
加载区域信息 (region表)
  ↓
加载调查数据 (survey_data或community_disaster_reduction_capacity)
  ↓
执行评估计算
```

#### 新流程
```
用户选择模型 + 数据类型 (township/community)
  ↓
根据数据类型选择省市县 (从对应数据表动态查询)
  ↓
加载调查数据并提取区域信息 (从同一表)
  ↓
执行评估计算
```

**优势**:
- 减少表关联查询
- 数据一致性更好（区域信息与业务数据在同一表）
- 维护成本更低（无需单独维护区域表）

### 7. 模型执行逻辑

`ModelExecutionServiceImpl` 根据 modelId 自动选择数据源：

```java
// 第224-249行
if (modelId != null && (modelId == 4 || modelId == 8)) {
    // 社区模型：从 community_disaster_reduction_capacity 表加载
    QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
    communityQuery.eq("region_code", regionCode);
    List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);
    // ...
} else {
    // 乡镇模型：从 survey_data 表加载
    QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
    dataQuery.eq("region_code", regionCode);
    SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);
    // ...
}
```

## 验证清单

### 功能验证
- [ ] 省市县三级联动查询（乡镇数据）
- [ ] 省市县三级联动查询（社区数据）
- [ ] 评估计算功能（乡镇模型）
- [ ] 评估计算功能（社区模型）
- [ ] 结果展示中的区域名称显示

### 性能验证
- [ ] 区域查询响应时间
- [ ] 评估计算性能（与删除前对比）

## 回退方案

如需回退，执行以下步骤：

1. 恢复 region 表（使用备份或重新创建）
2. 恢复后端代码（回滚到删除前的版本）
3. 恢复前端代码（回滚到删除前的版本）

## 相关文档

- `/docs/PROJECT_GUIDE.md` - 已更新API列表说明
- `/docs/agent-architecture.md` - 架构说明
- `/sql/migrations/` - 数据库迁移脚本目录

## 联系人

如有问题，请联系开发团队。
