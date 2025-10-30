# 项目统一指南（目录/架构/API/运行）

本指南整合了项目的目录结构、后端与前端的设计、API 清单、运行方式、数据迁移方案，以及清理与归档建议。一份文档即可快速上手与维护。

作者提示：如需浓缩版概览，可阅读根目录 `agent.md`；本文件为完整版。

## 1. 目录结构（建议与现状）

建议的目录（保持简洁、一目了然）：

- frontend/                  前端源码（Vue 3 + Element Plus）
- src/                       后端源码（Spring Boot + MyBatis‑Plus）
  - main/java/               后端 Java 代码
  - main/resources/          应用配置（`application.yml`）
- docs/                      文档（本文件，以及架构细节）
- sql/                       SQL 脚本（migrations/、diagnostics/、archive/）
- .gitignore                 Git 忽略规则
- pom.xml                    Maven 构建配置

注：根目录的历史报告、临时脚本和日志已清理或归档到 docs/archive/ 与 sql/* 子目录。

## 2. 后端架构与执行链路

- 技术栈：Spring Boot 2.7、MyBatis‑Plus、MySQL（可迁移到 Supabase/PostgreSQL）。
- 评估执行模型：模型 → 步骤 → 算法；算法首选 QLExpress 表达式；以 `@` 开头的特殊标记交由 `SpecialAlgorithmService` 处理。

核心实现：`src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java:1`

- executeModel(modelId, regionCodes, weightConfigId)
  - 读取启用步骤（按 `step_order` 升序）。
  - 维护 `currentCodes` 与 `prevOutputsByRegion`（跨步骤输出）。
  - 逐步执行：
    - 聚合步（`step_type=AGGREGATION` 或名称/编码包含“乡镇/聚合”）：调用 `executeTownshipAggregation`
      - 按 `survey_data.township` 将行政村输出聚合为乡镇，生成 `TOWNSHIP_...` 行；数值取均值。
      - 附带 `_firstCommunityCode`, `_townshipName` 用于溯源与显示。
    - 其它步骤：`executeStep`，从 `survey_data` 注入常用字段，再合并前一步输出（上下文 `ctx`）。
  - 合并输出到总表（每个地区一行，含 `regionCode/regionName` 与本步输出）。
  - 生成列定义 `columns`（从 `outputToAlgorithmName` 派生），每列附 `stepOrder`（供前端按步骤分组展示）。

- executeStep(stepId, regionCodes, inputData)
  - 注入 survey_data 字段：province/city/county/township、population/funding_amount/material_value/hospital_beds/volunteers/militia_reserve/training_participants/shelter_capacity。
  - 注入前一步输出（`ctx.putAll(inputData[region])`）。
  - 按 `algorithm_order` 执行每个 `StepAlgorithm`：
    - `@marker:params` → `SpecialAlgorithmService.executeSpecialAlgorithm(marker, params, regionCode, ctx, allRegionData)`
    - 否则 → `QLExpressService.execute(expr, ctx)`
  - 数值统一格式化为 8 位小数的字符串；列名用 `outputParam`，列头优先 `algorithmName`。

- executeTownshipAggregation(step, regionCodes, prevOutputs)
  - 基于上一步的行政村输出，按乡镇均值聚合；输出 `TOWNSHIP_...` 行用于后续步骤。

特殊算法接口：`src/main/java/com/evaluate/service/SpecialAlgorithmService.java:1`

实现：`src/main/java/com/evaluate/service/impl/SpecialAlgorithmServiceImpl.java`
  - 支持：`NORMALIZE`、`TOPSIS_POSITIVE`、`TOPSIS_NEGATIVE`、`TOPSIS_SCORE`、`GRADE`。

## 3. 前端设计

- 请求封装：`frontend/src/utils/request.ts`
  - 默认 `baseURL = http://localhost:8081`（可通过 `VITE_API_BASE_URL` 覆盖）。
  - 统一 Result 响应处理、超时/错误提示。

- 结果展示：`frontend/src/components/ResultDialog.vue`
  - 按列的 `stepOrder` 分组；切换步骤仅显示对应步骤的列。
  - 行过滤规则：
    - 选择“步骤≥2/含乡镇聚合相关关键词” → 仅展示 `TOWNSHIP_` 行（乡镇）。
    - 否则展示行政村行。
  - 支持列全选/取消、重置与 CSV 导出。

- 模型管理：`frontend/src/views/ModelManagement.vue`
  - 管理模型/步骤/算法；可解析步骤 `description` 中的 `|ALGORITHMS|` JSON 标记（如存在）。

## 4. API 清单（按 Controller 汇总）

仅列路径，参数与返回体参考源码（统一 Result 包裹）：

- /api/algorithm-config：GET /{id}, GET /default, DELETE /{id}
- /api/algorithm/execution：POST /execute, POST /validate, GET /progress/{executionId}, POST /stop/{executionId}, GET /types, POST /batch, POST /step/calculate
- /api/algorithm/management：
  - GET /list, GET /detail/{algorithmId}, POST /create, PUT /update, DELETE /delete/{algorithmId}
  - GET /steps/{algorithmId}, POST /step/create, PUT /step/update, DELETE /step/delete/{stepId}, PUT /steps/batch
  - GET /formulas, GET /steps/{stepId}/formulas, POST /formula/create, PUT /formula/update, DELETE /formula/delete/{formulaId}, POST /formula/validate
  - POST /copy/{sourceAlgorithmId}, POST /import, GET /export/{algorithmId}
- /api/algorithm-step-execution：GET /{algorithmId}/steps, POST /{algorithmId}/step/{stepOrder}/execute, POST /{algorithmId}/steps/execute-up-to/{upToStepOrder}, GET /{algorithmId}/detail, GET /algorithms, POST /{algorithmId}/validate-params
- /api/community-capacity：POST /import, GET /list, GET /search, GET /{id}, PUT /{id}, DELETE /{id}, DELETE /batch, GET /template
- /api/evaluation：POST /calculate, POST /recalculate, POST /batch, GET /process, GET /history/{surveyId}, POST /validate, DELETE /results, POST /execute-model, POST /generate-table, GET /algorithm/{algorithmId}/steps-info, POST /algorithm/{algorithmId}/step/{stepOrder}/execute, POST /algorithm/{algorithmId}/steps-up-to/{upToStepOrder}/execute
- /api/region（区域数据接口）：GET /provinces?dataType={township|community}, GET /cities?dataType=xxx&provinceName=xxx, GET /counties?dataType=xxx&provinceName=xxx&cityName=xxx, GET /data?dataType=xxx&provinceName=xxx&cityName=xxx&countyName=xxx
  - **注意**：region表已删除，区域数据现从 survey_data（乡镇模型）和 community_disaster_reduction_capacity（社区模型）表动态查询
- /api/survey-data：GET /{id}, GET /survey/{surveyName}, GET /region/{region}, GET /search, POST /batch, DELETE /{id}, DELETE /survey/{surveyName}, POST /import, GET /export/{surveyName}, GET /export/all
- /api/unified-evaluation：GET /data-source-types, POST /execute, POST /execute-step, POST /validate, GET /help
- /api/indicator-weight：GET /{id}, GET /config/{configId}, GET /indicator/{indicatorCode}, POST /batch, DELETE /{id}, POST /validate
- /api/weight-config：GET /{id}, GET /name/{configName}, GET /active, DELETE /{id}, POST /activate/{id}, POST /deactivate/{id}, POST /copy/{id}, POST /validate

> 说明：以上按源码扫描汇总，仅供路由导航；具体字段与行为以 Controller/Service 为准。

## 5. 运行与调试

- 后端：
  - 构建：`mvn -DskipTests package`
  - 启动：`mvn -DskipTests spring-boot:run`
  - 端口：默认 8081（`application.yml` 可改）；占用时可 `-Dserver.port=8082`。

- 前端：
  - 确保 `.env` 或启动参数的 `VITE_API_BASE_URL` 指向后端地址（默认 `http://localhost:8081`）。
  - 访问页面进行模型执行、结果查看与导出。

## 6. 数据迁移（Supabase / PostgreSQL）

- 自动迁移：`SupabaseMigrationRunner`（`migration.enabled=true` 时启用）
- 配置：`application.yml` 的 `supabase.jdbc.{url,user,password}`；支持 `migration.truncate/exitOnFinish`
- 当前迁移表：`evaluation_result`, `model_execution_record`
- 迁移脚本索引：`docs/sql-migrations-index.md`

## 7. 清理与归档建议（请确认后执行）

以下为建议归档/删除项，避免根目录冗余，统一转入 docs/ 或 scripts/（若仍有使用价值）：

- 历史报告与问题记录：根目录下零散的 `*.md` 报告/总结/说明（已由本文件统一覆盖）。
- 大体量演示文件：`*.pptx`、`*.docx`、样例 `*.xlsx`。
- 运行产生目录：`logs/`、`target/`（应由 `.gitignore` 忽略）。
- 临时/废弃目录：`temp_remove/`、`*.bak` 文件。
- 根目录测试/临时脚本：`*.ps1`、`*.bat`、散落的 `*.sql`（建议集中到 `sql/`）。
- 误入版本库的编译产物：`src/**.class`。

如需我执行“删除与整理”，请确认：
1) 是否保留任意历史报告（如需保留，我会移动到 `docs/archive/`）；
2) 是否需要保留部分脚本与 SQL（我可归档到 `scripts/` 与 `sql/`）。

确认后，我将：
- 新建 `docs/archive/` 与 `scripts/`（如需）；
- 移动仍需保留但不常用的历史文件到归档；
- 删除明确不再需要的文件与目录；
- 更新 `.gitignore` 确保 `logs/`、`target/` 等不再入库。

## 8. 变更记录

- 2025-10-30：整合统一指南，替代分散文档；前后端设计与 API 清单对齐；给出清理建议与执行步骤。

## 9. 参考链接

- 迁移脚本索引：docs/sql-migrations-index.md
- 架构详解：docs/agent-architecture.md
- ����/���/�ӿڣ�docs/requirements-design-api.md

- ҵ�����̣�docs/requirements-design-api.md#����ģ��ҵ�����̣�business-flow��
