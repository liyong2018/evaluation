# 需求 · 设计 · 接口（统一规范）

本文件聚焦三个方面：做什么（需求）、怎么做（设计）、怎样用（接口）。配合 `docs/PROJECT_GUIDE.md`（总览）与 `docs/agent-architecture.md`（架构细节）阅读。

## 一、需求（Requirements）

- 业务目标
  - 支持对社区/乡镇等区域进行分步（Step）评估计算，输出能力得分与分级，支持聚合、归一化、定权、TOPSIS、分级等流程。
  - 评估结果可在前端按步骤查看、导出，并可（按需）写入数据库。

- 角色与范围
  - 管理员/配置人员：维护模型、步骤、算法和权重配置。
  - 评估操作者：选择区域与权重，执行评估并查看结果。

- 功能需求
  - 模型管理
    - 维护模型的步骤顺序与类型（CALCULATION/NORMALIZATION/WEIGHTING/TOPSIS/AGGREGATION/GRADING）。
    - 每步可配置多个算法（顺序、表达式、输出字段、名称）。
  - 评估执行
    - 选择模型、区域、权重配置后执行。
    - 聚合步骤将行政村数据聚合为乡镇（按 survey_data.township 分组）。
    - 特殊算法以 @ 标记（如 @NORMALIZE、@TOPSIS_SCORE、@GRADE）。
  - 结果展示
    - 列按 stepOrder 分组；切换步骤时仅显示对应列。
    - 行按区域层级过滤：步骤≥2（或含“乡镇/聚合”）仅显示 TOWNSHIP_ 行，否则显示行政村行。
    - 支持 CSV 导出。
  - 数据持久化/迁移
    - 评估结果可按固定列写入 evaluation_result（按需接入）。
    - 提供 MySQL → Supabase(PostgreSQL) 迁移 Runner（开关可控）。

- 非功能需求
  - 稳定性：步骤级错误不应导致整体失败（尽量隔离）。
  - 性能：区域列表较大时按批次/就地聚合；列生成与前端渲染可控。
  - 编码：后端源码尽量使用 ASCII 文本，避免源文件中文导致编译异常；中文展示交由前端。

- 数据约束
  - survey_data 包含 province/city/county/township 等行政层级与若干指标字段。
  - 区域编码：乡镇聚合生成虚拟编码 `TOWNSHIP_<标识>`；名称优先取数据库，否则回退编码。

## 二、设计（Design）

- 总体架构
  - 前端：Vue 3 + Element Plus；统一请求封装；结果弹窗负责列分组与行过滤。
  - 后端：Spring Boot + MyBatis‑Plus；按“模型→步骤→算法”执行；QLExpress + 特殊算法服务。

- 后端模块
  - ModelExecutionServiceImpl（核心执行）
    - executeModel：加载步骤；跨步上下文 `prevOutputsByRegion`；识别聚合步；合并输出；生成列（附 stepOrder）。
    - executeStep：注入 survey_data 与上一步输出；执行表达式或特殊标记；格式化数值为 8 位小数。
    - executeTownshipAggregation：按 township 分组，对数值键取均值，生成 `TOWNSHIP_` 行，并附 `_firstCommunityCode`、`_townshipName`。
  - SpecialAlgorithmService：处理 `@NORMALIZE`、`@TOPSIS_POSITIVE`、`@TOPSIS_NEGATIVE`、`@TOPSIS_SCORE`、`@GRADE` 等标记。
  - 迁移 Runner：SupabaseMigrationRunner（通过 `migration.enabled` 开关）。

- 前端模块
  - ResultDialog.vue：
    - 列按 stepOrder 分组；默认“基础列 + 第一组步骤列”。
    - 步骤≥2 或含“乡镇/聚合”等关键词仅显示 TOWNSHIP_ 行；否则显示行政村行。
    - 导出 CSV、列全选/取消、重置。
  - ModelManagement.vue：模型/步骤/算法 CRUD；可解析 `|ALGORITHMS|` JSON 标记（如存在）。
  - request.ts：统一 baseURL、超时处理、Result 结构与错误提示。

- 数据模型（摘要）
  - model_step：step_order、step_type、step_code/name、status
  - step_algorithm：algorithm_order、algorithm_name/code、ql_expression、output_param
  - evaluation_result：region_code/name、四类能力分与级别、evaluation_model_id、data_source、execution_record_id 等
  - survey_data：province/city/county/township 及指标字段

## 三、接口（APIs）

- 统一约定
  - 返回结构：`{ success: boolean, data?: any, message?: string }`
  - 错误处理：`success=false` 时 message 提示；前端 request.ts 统一拦截。

- 评估执行（核心）
  - POST `/api/evaluation/execute-model`
    - 描述：执行指定模型，返回步骤结果、二维表与列定义。
    - 请求（示例）：
      ```json
      {
        "modelId": 8,
        "regionCodes": ["REGION_001", "REGION_002"],
        "weightConfigId": 3
      }
      ```
    - 响应（示例）：
      ```json
      {
        "success": true,
        "data": {
          "stepResults": {
            "STEP1": { "regionResults": {"REGION_001": {"x": 0.123}}, "outputToAlgorithmName": {"x": "指标X"}, "stepOrder": 1 },
            "STEP2": { "regionResults": {"TOWNSHIP_A": {"x": 0.234, "_firstCommunityCode": "REGION_001"}}, "outputToAlgorithmName": {"x": "聚合X"}, "stepOrder": 2 }
          },
          "tableData": [
            { "regionCode": "REGION_001", "regionName": "示例社区", "x": "0.12300000" },
            { "regionCode": "TOWNSHIP_A", "regionName": "A", "x": "0.23400000" }
          ],
          "columns": [
            { "prop": "regionCode", "label": "地区代码" },
            { "prop": "regionName", "label": "地区名称" },
            { "prop": "x", "label": "指标X", "stepOrder": 1 }
          ]
        }
      }
      ```

  - POST `/api/evaluation/generate-table`
    - 描述：从 `stepResults` 生成二维表（可选）。

  - GET `/api/evaluation/algorithm/{algorithmId}/steps-info`
    - 描述：获取算法的步骤信息（前端展示辅助）。

- 模型管理
  - GET `/api/model-management/models`
  - GET `/api/model-management/models/{modelId}/detail`
  - POST `/api/model-management/models`
  - POST `/api/model-management/models/{modelId}/steps`
  - DELETE `/api/model-management/steps/{stepId}`
  - POST `/api/model-management/steps/{stepId}/algorithms`
  - PUT `/api/model-management/algorithms/{algorithmId}`
  - DELETE `/api/model-management/algorithms/{algorithmId}`
  - POST `/api/model-management/validate-expression`

- 步骤级执行（调试/演示）
  - GET `/api/algorithm-step-execution/{algorithmId}/steps`
  - POST `/api/algorithm-step-execution/{algorithmId}/step/{stepOrder}/execute`
  - POST `/api/algorithm-step-execution/{algorithmId}/steps/execute-up-to/{upToStepOrder}`
  - GET `/api/algorithm-step-execution/{algorithmId}/detail`

- 其它（选摘）
  - /api/indicator-weight、/api/weight-config、/api/region、/api/survey-data、/api/algorithm/management、/api/algorithm/execution 等，详见 `docs/PROJECT_GUIDE.md` 的“API 清单”。

## 四、术语与约定

- 区域编码：聚合后行使用 `TOWNSHIP_` 前缀，前端据此过滤乡镇行。
- 列头命名：优先 `algorithmName`，否则回退 `outputParam`。
- 数值格式：统一格式化为 8 位小数字符串，避免科学计数造成展示歧义。
- 编码规范：后端源码尽量使用 ASCII 文本与英文日志；中文展示交由前端。

## 五、变更与待办（Changelog & TODO）

- 2025‑10‑30：首次整理，形成“三合一”规范。
- TODO（可选）：
  - 在 EvaluationController 中新增 `persist=true` 参数路径，按固定列直接写入 evaluation_result。
  - 完善 `getAlgorithmStepsInfo` 的真实实现与示例。
  - 为高频接口补充更详尽的请求/响应 JSON Schema 与示例。

## 六、模型业务流程（Business Flow）

以下流程在三类模型中通用：先“赋值（指标构造）→ 归一化 → 定权 → 优劣解（TOPSIS）→ 能力值 → 分级”。差异主要在数据来源与是否需要分组聚合。

通用记号
- 赋值（Raw）：由原始字段计算得到的指标值（未归一化）。
- 归一化（Norm）：某指标对全体评估单元做向量归一化：`x_norm = x / sqrt(sum(x^2))`。
- 二级权重（w2）：每个二级指标的权重。
- 一级权重（w1）：某个一级能力下的权重。
- 定权值：`x_w = x_norm * w2`；综合定权：`x_w_total = x_norm * w1 * w2`。
- TOPSIS 正/负理想解（D+, D-）：对（同一层级内）一组已定权的指标向量，分别与“坐标最大值向量/最小值向量”计算欧氏距离。
- 能力值：`score = D- / (D+ + D-)`，范围 0–1，越大越好。
- 分级：基于均值 μ 与标准差 σ 的阈值法，划分“强/较强/中等/较弱/弱”（分段阈值见下）。

分级规则（单能力或综合能力均可用）
- 计算 μ=Average(scores)，σ=StdDev(scores)。
- 若 μ ≤ 0.5σ：
  - [μ+1.5σ, +∞)：强；[μ+0.5σ, μ+1.5σ)：较强；其余：中等。
- 若 0.5σ < μ ≤ 1.5σ：
  - [μ+1.5σ, +∞)：强；[μ+0.5σ, μ+1.5σ)：较强；[μ-0.5σ, μ+0.5σ)：中等；其余：较弱。
- 若 μ > 1.5σ：
  - [μ+1.5σ, +∞)：强；[μ+0.5σ, μ+1.5σ)：较强；[μ-0.5σ, μ+0.5σ)：中等；[μ-1.5σ, μ-0.5σ)：较弱；其余：弱。

### 1) 乡镇减灾能力评估（乡镇→乡镇）
输入字段（示例）
- 人口、管理人员数、是否开展风险评估（布尔/枚举）、年度经费、物资金额、床位数、消防员/志愿者/民兵、培训演练人次、避难容量等。

S1 指标赋值（Raw）
- 队伍管理=(管理人员/人口)*10000
- 风险评估=是否评估（是=1，否=0）
- 财政投入=(年度经费/人口)*10000
- 物资储备=(物资金额/人口)*10000
- 医疗保障=(床位数/人口)*10000
- 自救互救=(消防员+志愿者+民兵)/人口*10000
- 公众避险=(（培训+演练）人次/人口)*100
- 转移安置=(避难容量/人口)

S2 归一化（Norm）
- 对每个二级指标，按乡镇维度做向量归一化。

S3 定权（二级与一级）
- 二级定权：`x_w = x_norm * w2`。
- 一级综合：对应一级能力（灾害管理/备灾/自救转移）聚合其二级定权值；或直接用 `x_norm * w1 * w2` 汇入综合矩阵。

S4 优劣解（TOPSIS）
- 先在一级内计算 D+(一级)/D-(一级)；也可直接对“所有二级定权指标”一次性计算 D+/D- 得到综合优/劣（两种做法保持一致性）。

S5 能力值（0–1）
- `score = D- / (D+ + D-)`，分别得到：灾害管理、灾害备灾、自救转移、以及综合能力。

S6 分级
- 按分级规则给出各能力的等级（强/较强/中等/较弱/弱）。

产出（典型）
- 各二级指标的 raw/norm/定权值（可选输出）。
- 一级能力分/等级；综合能力分/等级。

### 2) 社区-行政村评估（社区→社区）
输入字段（示例）
- 预案、弱势清单、隐患清单、风险地图（布尔）、人口、资金、物资、卫生机构数、预备役/志愿者、培训/演练人次、避难容量等。

S1 指标赋值（Raw）
- 预案建设=有(1)/无(0)
- 隐患排查=弱势清单与隐患清单均有(1)，其一(0.5)，均无(0)
- 风险评估=风险地图有(1)/无(0)
- 财政投入=(资金/人口)*10000
- 物资储备=(物资金额/人口)*10000
- 医疗保障=(卫生机构数/人口)*10000
- 自救互救=(预备役+志愿者)/人口*10000
- 公众避险=(演练+培训)/人口*100
- 转移安置=(避难容量/人口)

S2–S6 与乡镇流程相同（社区为评估单元）。

### 3) 社区-乡镇评估（社区→乡镇）
两阶段：先在社区层做 S1–S3，再按乡镇聚合为“乡镇统计指标”，再做 S4–S6。

第一阶段（社区层）
- S1 社区赋值：同“社区-行政村”赋值。
- S2 社区归一化：以社区为集合归一化。
- S3 社区定权：得到社区层定权值。

第二阶段（社区→乡镇聚合）
- 分组：按 `乡镇名称` 将社区归属聚合为乡镇单元。
- 聚合口径：对同名二级指标在该乡镇内做“均值”聚合（或按需改为总量/加权平均，需在模型中明确）。
  - 例如：乡镇财政投入(统计)=Mean(社区财政投入-赋值)；其余指标同口径聚合。
- 得到“乡镇统计指标”（作为新的 Raw）。

第三阶段（乡镇层）
- S2 归一化：对“乡镇统计指标”做向量归一化。
- S3 定权：同上。
- S4–S6：TOPSIS、能力值、分级，得到乡镇层的最终结果。

输出约定
- 行标识：社区层用真实行政区代码；乡镇聚合行使用 `TOWNSHIP_<标识>`（并可带 `_firstCommunityCode`、`_townshipName` 便于回溯）。
- 列分组：列携带 `stepOrder`，前端可按步骤视图展示。
- 结果入库（按需）：映射到 evaluation_result 的固定列（四类能力分+级别、综合能力分+级别、region_code/name、model_id、data_source）。
