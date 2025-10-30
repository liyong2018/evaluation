# 项目架构与设计说明（概览）

统一而详细的文档已整合到：`docs/PROJECT_GUIDE.md`

本文件仅保留简版概览，便于快速定位；如需完整内容（目录结构、API 清单、执行链路、前端展示策略、迁移说明等），请查看 `docs/PROJECT_GUIDE.md`。

## 总览

- 后端：Spring Boot 2.7 + MyBatis‑Plus，使用 MySQL（可选迁移到 Supabase/PostgreSQL）。
- 计算框架：按“模型 → 步骤 → 算法”执行；算法优先使用 QLExpress，特殊标记以 @ 开头交由 SpecialAlgorithmService 处理。
- 前端：Vue 3 + Element Plus，结果弹窗支持“按步骤列分组 + 行过滤（行政村/乡镇）”。

## 后端核心

- 入口与配置
  - 端口：`server.port: 8081`（见 `src/main/resources/application.yml`）。
  - 数据源：MySQL 连接定义于 `spring.datasource.*`，日志与 MyBatis‑Plus 统一配置。

- 执行服务：`ModelExecutionServiceImpl`（`src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java:1`）
  - executeModel(modelId, regionCodes, weightConfigId)
    - 读取启用步骤（按 `step_order` 升序）。
    - 维护“当前地区集”和“跨步骤输出”：`prevOutputsByRegion: Map<regionCode, Map<k,v>>`。
    - 逐步执行：
      - 聚合步（AGGREGATION 或名称/编码包含“乡镇/聚合”）→ `executeTownshipAggregation(...)`：
        - 基于 `survey_data.township` 将行政村输出分组，均值聚合数值型字段；产出 `TOWNSHIP_...` 虚拟代码行，并附 `/_firstCommunityCode`, `/_townshipName`。
      - 非聚合步 → `executeStep(...)`：对每个 region 构造上下文并执行算法。
    - 结果并入总表：为每个地区保留 `regionCode/regionName` 与本步输出；构建 `stepResults`（含 `regionResults` 与 `outputToAlgorithmName`）。
    - 生成列：`buildColumnsFromStepResults(...)`，每列附 `stepOrder`（用于前端分组）。
  - executeStep(stepId, regionCodes, inputData)
    - 上下文注入：从 `survey_data` 加载常用字段（province/city/county/township 等），再 `putAll(inputData[region])` 注入前一步输出。
    - 执行算法列表（按 `algorithm_order`）：
      - `qlExpression` 以 `@` 开头：解析 `marker:params` 后调用 `SpecialAlgorithmService.executeSpecialAlgorithm(marker, params, currentRegion, ctx, allRegionData)`。
      - 否则交由 `QLExpressService.execute(expr, ctx)`。
    - 数值结果统一格式化为 8 位小数的字符串，输出字段名取 `outputParam`，展示名优先 `algorithmName`。
  - executeTownshipAggregation(step, regionCodes, prevOutputs)
    - 根据 `survey_data.township` 将上一步的行政村输出分组，对每个键做均值（仅数值可聚合），生成 `TOWNSHIP_...` 行。
  - 其他：`resolveRegionName`（兼容 `TOWNSHIP_` 前缀）、`generateResultTable(...)` 提供按步骤平铺的二维表。

- 特殊算法：`SpecialAlgorithmService`
  - 接口位于 `src/main/java/com/evaluate/service/SpecialAlgorithmService.java:1`；实现 `SpecialAlgorithmServiceImpl` 支持 NORMALIZE、TOPSIS_POSITIVE、TOPSIS_NEGATIVE、TOPSIS_SCORE、GRADE 等标记。

- 迁移机制（可选）：`SupabaseMigrationRunner`
  - 路径：`src/main/java/com/evaluate/migration/`。
  - 通过 `migration.enabled=true` 启用，用 `supabase.jdbc.{url,user,password}` 连接 Supabase/Postgres；支持 `truncate/exitOnFinish`。
  - 当前迁移表：`evaluation_result`、`model_execution_record`。

## 前端要点（摘要）

- 请求封装：`frontend/src/utils/request.ts`
  - 默认 `baseURL = http://localhost:8081`（可用 `VITE_API_BASE_URL` 覆盖）；统一处理 `Result` 风格响应，附超时与错误提示。

- 结果弹窗：`frontend/src/components/ResultDialog.vue`
  - 列分组：按列的 `stepOrder` 分组，默认展示“基础信息 + 第一组步骤列”。
  - 行过滤：
    - 选择步骤序号 ≥ 2 或含“乡镇/聚合”等关键词 → 仅显示 `TOWNSHIP_` 行（乡镇结果）。
    - 否则显示行政村行。
  - 功能：列全选/取消、重置为当前步骤、CSV 导出。

- 模型管理：`frontend/src/views/ModelManagement.vue`
  - 管理模型/步骤/算法，当前页面从后端 `/api/model-management/*` 拉取。
  - 步骤的算法可通过 `description` 内的 `|ALGORITHMS|` 标记嵌入 JSON（前端 `parseAlgorithmsFromStep` 做解析）。

## 约定与注意

- 字符编码：为避免 Java 源码字符串异常，新增/修改后端源码时尽量使用 ASCII 文本与英文日志；中文展示交由前端。
- 地区编码：聚合后使用 `TOWNSHIP_` 前缀的虚拟编码；名称落在 `regionName`，前端依此做行过滤。
- 列头命名：优先使用 `algorithmName`，否则回退到 `outputParam`。
- 端口冲突：默认端口 8081，如被占用请终止占用进程或改启动参数 `-Dserver.port=8082`。

## 运行与验证（摘要）

- 后端：`mvn -DskipTests package` → `mvn -DskipTests spring-boot:run`。
- 前端：确保 `.env` 配置的 `VITE_API_BASE_URL` 指向后端端口（默认 8081）。
- 验证社区→乡镇模型：步骤 2（聚合）应只显示 `TOWNSHIP_` 行；步骤 5/6 仅显示能力分/级等目标列。
