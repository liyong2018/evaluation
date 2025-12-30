# 架构与设计（详细版）

本文档在根目录 `agent.md` 的基础上，补充更细致的信息：后端 API 路由清单、关键数据结构/字段对照、执行链路与前端展示策略，便于研发与测试对齐。

## 后端

### 技术栈
- Spring Boot 2.7、MyBatis‑Plus、MySQL（可迁移到 Supabase/PostgreSQL）。
- QLExpress 作为表达式执行引擎，配合 `SpecialAlgorithmService` 处理特殊标记算法（以 `@` 前缀标记）。

### 模型执行主流程
- 类：`ModelExecutionServiceImpl`（src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java:1）
- executeModel(modelId, regionCodes, weightConfigId)：
  - 读取启用步骤，按 `step_order` 升序。
  - 维护 `currentCodes` 与 `prevOutputsByRegion: Map<String, Map<String,Object>>` 作为跨步骤上下文。
  - 对每个步骤：
    - 聚合步（type=AGGREGATION 或名称/编码包含“乡镇/聚合”）：调用 `executeTownshipAggregation`，按 `survey_data.township` 将上一步的行政村（社区）输出聚合为乡镇（生成 `TOWNSHIP_...` 行），数值做均值；附加 `_firstCommunityCode`, `_townshipName` 字段。
    - 非聚合步：`executeStep`，对每个地区构造上下文（survey_data 字段 + 上一步输出），执行算法列表。
  - 汇总 `stepResults`（含 `regionResults` 与 `outputToAlgorithmName`），并生成 `columns`（列带 `stepOrder`）。

- executeStep(stepId, regionCodes, inputData)：
  - 注入 survey_data 字段：province/city/county/township/population/funding_amount/material_value/hospital_beds/volunteers/militia_reserve/training_participants/shelter_capacity 等。
  - 注入上一步输出：`ctx.putAll(inputData.get(region))`。
  - 对每个 `StepAlgorithm`（按 `algorithm_order`）：
    - 若 `qlExpression` 以 `@` 开头：解析 `marker:params`，构建 `allRegionData`（从 `inputData` 聚合），调用 `SpecialAlgorithmService.executeSpecialAlgorithm(marker, params, regionCode, ctx, allRegionData)`。
    - 否则：`QLExpressService.execute(expr, ctx)`。
  - 数值结果格式化为 `"%.8f"` 字符串；列标识取 `outputParam`，展示名优先 `algorithmName` → `outputToAlgorithmName`。

- executeTownshipAggregation(step, regionCodes, prevOutputs)：
  - 依据 `survey_data.township` 对上一步输出按乡镇名称分组，对每个键做均值聚合（仅数值可聚合），生成 `TOWNSHIP_` 行，并携带 `_firstCommunityCode`, `_townshipName`。

- 重要辅助：
  - `resolveRegionName`：将 `TOWNSHIP_...` 显示为后缀名；其它走 `RegionMapper.selectByCode`。
  - `generateResultTable`：将所有步的 `regionResults` 合并成二维表（如需导出使用）。

### 特殊算法标记（SpecialAlgorithmService）
- 接口：`src/main/java/com/evaluate/service/SpecialAlgorithmService.java:1`
- 实现：`src/main/java/com/evaluate/service/impl/SpecialAlgorithmServiceImpl.java`
- 支持：`NORMALIZE`、`TOPSIS_POSITIVE`、`TOPSIS_NEGATIVE`、`TOPSIS_SCORE`、`GRADE` 等标记。

### API 路由清单（Controller）
按文件分组，路由前缀与方法如下（仅列出路径，参数/返回体按 Controller 源码）：

- AlgorithmConfigController（/api/algorithm-config）
  - GET /{id}, GET /default, DELETE /{id}
- AlgorithmExecutionController（/api/algorithm/execution）
  - POST /execute, POST /validate, GET /progress/{executionId}, POST /stop/{executionId}, GET /types, POST /batch, POST /step/calculate
- AlgorithmManagementController（/api/algorithm/management）
  - GET /list, GET /detail/{algorithmId}, POST /create, PUT /update, DELETE /delete/{algorithmId}
  - GET /steps/{algorithmId}, POST /step/create, PUT /step/update, DELETE /step/delete/{stepId}, PUT /steps/batch
  - GET /formulas, GET /steps/{stepId}/formulas, POST /formula/create, PUT /formula/update, DELETE /formula/delete/{formulaId}, POST /formula/validate
  - POST /copy/{sourceAlgorithmId}, POST /import, GET /export/{algorithmId}
- AlgorithmStepExecutionController（/api/algorithm-step-execution）
  - GET /{algorithmId}/steps, POST /{algorithmId}/step/{stepOrder}/execute, POST /{algorithmId}/steps/execute-up-to/{upToStepOrder}
  - GET /{algorithmId}/detail, GET /algorithms, POST /{algorithmId}/validate-params
- CommunityDisasterReductionCapacityController（/api/community-capacity）
  - POST /import, GET /list, GET /search, GET /{id}, PUT /{id}, DELETE /{id}, DELETE /batch, GET /template
- EvaluationController（/api/evaluation）
  - POST /calculate, POST /recalculate, POST /batch, GET /process, GET /history/{surveyId}, POST /validate, DELETE /results
  - POST /execute-model, POST /generate-table
  - GET /algorithm/{algorithmId}/steps-info
  - POST /algorithm/{algorithmId}/step/{stepOrder}/execute
  - POST /algorithm/{algorithmId}/steps-up-to/{upToStepOrder}/execute
- IndexController
  - GET /, GET /health, GET /hotreload-test
- IndicatorWeightController（/api/indicator-weight）
  - GET /{id}, GET /config/{configId}, GET /indicator/{indicatorCode}, POST /batch, DELETE /{id}, POST /validate
- ModelManagementController（/api/model-management）
  - GET /models, GET /models/{modelId}/detail, POST /models
  - POST /models/{modelId}/steps, POST /steps/{stepId}/algorithms, PUT /algorithms/{algorithmId}
  - POST /validate-expression, DELETE /algorithms/{algorithmId}, DELETE /steps/{stepId}
- RegionController（/api/region）
  - GET /tree, GET /children/{parentId}, GET /level/{level}, GET /code/{code}, POST /batch, GET /all
- RegionDataController（/api/region）
  - GET /provinces, GET /cities, GET /counties, GET /data
- SurveyDataController（/api/survey-data）
  - GET /{id}, GET /survey/{surveyName}, GET /region/{region}, GET /search, POST /batch, DELETE /{id}, DELETE /survey/{surveyName}, POST /import, GET /export/{surveyName}, GET /export/all
- UnifiedEvaluationController（/api/unified-evaluation）
  - GET /data-source-types, POST /execute, POST /execute-step, POST /validate, GET /help
- WeightConfigController（/api/weight-config）
  - GET /{id}, GET /name/{configName}, GET /active, DELETE /{id}
  - POST /activate/{id}, POST /deactivate/{id}, POST /copy/{id}, POST /validate

> 说明：具体请求/响应模型见对应 Controller 方法签名与 DTO；统一使用 `Result<T>` 结构包裹响应（前端 `request.ts` 有统一处理）。

### 字段与表结构对照（节选）

- 结果字段（evaluation_result）：
  - region_code, region_name
  - management_capability_score, support_capability_score, self_rescue_capability_score, comprehensive_capability_score（数值）
  - management_capability_level, support_capability_level, self_rescue_capability_level, comprehensive_capability_level（分级）
  - evaluation_model_id, data_source（community/township 等）、execution_record_id
  - create_by/create_time/update_by/update_time/is_deleted

- 步骤与算法：
  - model_step(step_order, step_type, step_code/name)
  - step_algorithm(algorithm_order, algorithm_name/code, ql_expression, output_param)

- survey_data 关键列（用于上下文）：province/city/county/township，population，funding_amount，material_value，hospital_beds，volunteers，militia_reserve，training_participants，shelter_capacity。

## 前端

- 请求层：`frontend/src/utils/request.ts`，默认 `baseURL = http://localhost:8081`（可通过 `VITE_API_BASE_URL` 覆盖）；响应使用统一的 Result 格式处理，含超时与错误提示。

- 结果展示：`frontend/src/components/ResultDialog.vue`
  - 按列的 `stepOrder` 分组；默认“基础信息 + 第一组步骤列”。
  - 行过滤：步骤≥2 或含“乡镇/聚合”等关键词 → 仅展示 `TOWNSHIP_` 行；否则展示行政村行。
  - 导出 CSV、列全选/取消/重置。

- 模型管理：`frontend/src/views/ModelManagement.vue`
  - 模型/步骤/算法管理页面；支持 `|ALGORITHMS|` 标记的 JSON 嵌入解析。

## 运行与端口

- 后端：8081（配置在 `src/main/resources/application.yml:1`）。若端口占用，可 `-Dserver.port=8082` 启动。
- 前端：请确保 `VITE_API_BASE_URL` 指向后端端口；动态导入失败通常由路径或端口不一致引起。

## 迁移（Supabase）

- 通过 `migration.enabled=true` 启用自动迁移（类：`SupabaseMigrationRunner`）。
- 配置项在 `application.yml` 的 `supabase.jdbc.*` 分组中；支持 `truncate/exitOnFinish`。

