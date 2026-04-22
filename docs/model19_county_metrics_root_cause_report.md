# 模型19区县指标不一致根因报告

## 1. 现象复现
- 区县 `510603`（旌阳区）在修复前页面/执行记录中出现：
  - `team_management_capability = 0.44929506`
  - `financial_investment_capability = 3.31368283`
- 标准值应为：
  - `team_management_capability = 0.54335423`
  - `financial_investment_capability = 4.00739686`

## 2. 根因定位
- 问题位于模型 `model_id=19` 的步骤2（`COUNTY_DERIVED_AGG`）。
- 原表达式分母采用 `SUM(population)`，即把乡镇人口直接累加作为区县分母。
- 标准口径应使用区县总人口（`countyPopulation`），而不是乡镇累加人口。
- 该口径偏差导致 `队伍管理能力`、`财政投入能力`（以及同类人均指标）被系统性低估。

## 3. 公式修复（前后对比）
- 变更脚本：`sql/fix_model19_county_population_formula.sql`

### 3.1 队伍管理能力
- 修复前：
```sql
(SUM(management_staff)*1.0 + SUM(firefighters)*1.0) / SUM(population) * 10000.0
```
- 修复后：
```sql
(SUM(management_staff)*1.0 + SUM(firefighters)*1.0) / countyPopulation * 10000.0
```

### 3.2 财政投入能力
- 修复前：
```sql
SUM(funding_amount)*1.0 / SUM(population) * 10000.0
```
- 修复后：
```sql
SUM(funding_amount)*1.0 / countyPopulation * 10000.0
```

## 4. 代码链路修复
- 文件：`src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`
- 关键点：
  - 在区县汇总计算时注入 `countyPopulation` 变量到表达式上下文。
  - 扩展聚合表达式计算函数，支持自定义变量替换。
  - 新增区县人口解析逻辑，按优先级取值：
    1. `government_disaster_reduction_capacity_2020.population`
    2. `survey_data` 的 `MAX(population)`（按县名）
    3. 兼容回退：乡镇人口求和

## 5. 字段类型与精度审计结论
- `survey_data.population`：`bigint`
- `survey_data.funding_amount`：`decimal(15,4)`
- `survey_data.material_value`：`decimal(15,4)`
- `survey_data.management_staff/firefighters/...`：`int`
- `government_disaster_reduction_capacity_2020.population`：`bigint`
- 结论：未发现由于列类型导致的截断；计算表达式使用 `*1.0` 保证浮点运算，关键误差来源为分母口径而非精度丢失。

## 6. 回归验证
- 执行脚本：`scripts/verify_model19_county_derived.py`
- 验证内容：
  - 6区县（`510603/510604/510623/510681/510682/510683`）× 8指标
  - 比较 `DB执行记录(step2)` 与 `接口JSON(/api/evaluation/history/detail/{id})`
  - 断言 `abs(diff) < 1e-8`
  - 额外校验 `510603` 两项关键指标精确命中目标值
- 验证结果：通过。

## 7. 可重复执行步骤
1. 应用脚本：`sql/fix_model19_county_population_formula.sql`
2. 触发模型19重算（`year=2020, orgCode=5106`）
3. 运行：`python3 scripts/verify_model19_county_derived.py`
4. 检查输出包含：
   - `PASS: 6个区县*8项指标 DB 与 API 全量一致，abs(diff) < 1e-8`
   - `PASS: 510603 队伍管理能力/财政投入能力达到目标值`
