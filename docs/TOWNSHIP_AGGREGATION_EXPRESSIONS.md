# 乡镇聚合表达式使用指南

## 📋 概述

乡镇聚合步骤现在支持灵活的SUM()表达式，可以实现复杂的聚合计算逻辑。

**工作原理：**
1. 系统自动按乡镇名称将社区数据分组
2. 对每个乡镇，根据配置的表达式计算聚合结果
3. `SUM(字段名)` 会对该乡镇所有社区的指定字段求和
4. `communityCount` 自动替换为该乡镇的社区数量
5. 支持任意四则运算表达式

## 🎯 支持的表达式类型

### 1. 简单求和
```
SUM(fieldName)
```
对所有社区的某个字段求和

**示例：**
```
SUM(disasterMgmtScore)
```
计算所有社区的灾害管理分值总和

---

### 2. 多字段求和相加
```
SUM(fieldA) + SUM(fieldB)
```

**示例：**
```
SUM(fieldA) + SUM(fieldB)
```
将字段A的总和加上字段B的总和

---

### 3. 复杂四则运算
```
SUM(fieldA) + SUM(fieldB) / SUM(fieldC) * 1000
```

**示例：**
```
SUM(preparednessScore) / SUM(populationCount) * 10000
```
计算每万人的防灾准备分值

---

### 4. 多列平均值
```
(SUM(fieldA) + SUM(fieldB)) / (2 * communityCount)
```

**说明：**
- `communityCount` 是内置变量，表示该乡镇的社区数量
- 先求和再除以字段数和社区数，得到平均值

**示例：**
```
(SUM(disasterMgmtScore) + SUM(preparednessScore)) / (2 * communityCount)
```
计算两个指标的平均分值

---

### 5. 除法与常数运算
```
SUM(fieldA) / SUM(fieldB) * 10000
```

**示例：**
```
SUM(emergencyDrills) / SUM(communityCount) * 100
```
计算应急演练密度（每100个社区的演练次数）

---

## 📝 前端配置方法

### 在 step_algorithm 表中配置

| 字段 | 值 | 说明 |
|-----|-----|-----|
| ql_expression | `SUM(fieldA)+SUM(fieldB)` | SUM表达式 |
| output_param | `totalScore` | 输出字段名 |
| input_params | 可以为空 | 使用表达式时不需要 |

### 配置示例

```sql
-- 示例1：简单求和
INSERT INTO step_algorithm (
    step_id, algorithm_code, algorithm_name,
    ql_expression, output_param, calculation_order
) VALUES (
    51, 'AGG_TOTAL', '总分聚合',
    'SUM(comprehensiveScore)', 'townshipTotalScore', 1
);

-- 示例2：多字段求和
INSERT INTO step_algorithm (
    step_id, algorithm_code, algorithm_name,
    ql_expression, output_param, calculation_order
) VALUES (
    51, 'AGG_MULTI', '多指标聚合',
    'SUM(disasterMgmtScore)+SUM(preparednessScore)+SUM(selfRescueScore)',
    'townshipCombinedScore', 2
);

-- 示例3：比例计算
INSERT INTO step_algorithm (
    step_id, algorithm_code, algorithm_name,
    ql_expression, output_param, calculation_order
) VALUES (
    51, 'AGG_RATIO', '能力密度',
    'SUM(totalCapability)/SUM(population)*10000',
    'townshipCapabilityDensity', 3
);

-- 示例4：平均值计算
INSERT INTO step_algorithm (
    step_id, algorithm_code, algorithm_name,
    ql_expression, output_param, calculation_order
) VALUES (
    51, 'AGG_AVG', '平均能力',
    '(SUM(disasterMgmtScore)+SUM(preparednessScore))/(2*communityCount)',
    'townshipAverageCapability', 4
);
```

---

## 🔧 内置变量

| 变量名 | 说明 | 自动替换 | 示例 |
|-------|------|---------|------|
| `communityCount` | 该乡镇的社区数量 | **系统自动替换** | `SUM(score)/communityCount` |

**重要说明：**
- `communityCount` 是占位符，**不需要**您传递实际值
- 后端会自动根据该乡镇包含的社区数量进行替换
- 例如青竹街道有10个社区，系统会自动将 `communityCount` 替换为 `10`

---

## 📌 实际配置示例（基于用户需求）

### Excel公式 → 系统表达式转换

| Excel公式 | 系统表达式 | 说明 |
|----------|-----------|------|
| `=SUM(AN55:AN60)/$BC$19` | `SUM(fieldAN)/communityCount` | 单列平均 |
| `=(SUM(AO55:AO60)+SUM(AP55:AP60))/(2*$BC$19)` | `(SUM(fieldAO)+SUM(fieldAP))/(2*communityCount)` | 两列平均 |
| `=SUM(AQ55:AQ60)/$BC$19` | `SUM(fieldAQ)/communityCount` | 单列平均 |
| `=SUM(AS55:AS60)/SUM($AR$55:$AR$60)*10000` | `SUM(fieldAS)/SUM(fieldAR)*10000` | 比值（每万）|
| `=SUM(AT55:AT60)/SUM($AR$55:$AR$60)*10000` | `SUM(fieldAT)/SUM(fieldAR)*10000` | 比值（每万）|
| `=SUM(AU55:AU60)/SUM($AR$55:$AR$60)*10000` | `SUM(fieldAU)/SUM(fieldAR)*10000` | 比值（每万）|
| `=(SUM(AV55:AV60)+SUM(AW55:AW60))/SUM($AR$55:$AR$60)*10000` | `(SUM(fieldAV)+SUM(fieldAW))/SUM(fieldAR)*10000` | 多列比值 |

**注意：**
- Excel中的 `$BC$19` 是社区数量单元格，在系统中用 `communityCount` 表示
- Excel中的列名（AN、AO等）对应系统中步骤1的输出字段名
- 确保字段名与步骤1的 `output_param` 完全一致

---

## 💡 使用示例

### 场景1：计算乡镇总分
需求：将所有社区的综合分值汇总到乡镇

**表达式：**
```
SUM(comprehensiveScore)
```

---

### 场景2：计算乡镇平均分
需求：计算乡镇内所有社区的平均综合分值

**表达式：**
```
SUM(comprehensiveScore) / communityCount
```

---

### 场景3：计算多维度总分
需求：将灾害管理、防灾准备、自救能力三个维度的分值加总

**表达式：**
```
SUM(disasterMgmtScore) + SUM(preparednessScore) + SUM(selfRescueScore)
```

---

### 场景4：计算加权平均分
需求：灾害管理权重0.4，防灾准备权重0.3，自救能力权重0.3

**表达式：**
```
(SUM(disasterMgmtScore)*0.4 + SUM(preparednessScore)*0.3 + SUM(selfRescueScore)*0.3) / communityCount
```

---

### 场景5：计算每万人的设施数量
需求：计算避难场所密度

**表达式：**
```
SUM(shelterCount) / SUM(population) * 10000
```

---

### 场景6：计算覆盖率
需求：计算已演练社区占比

**表达式：**
```
SUM(drillCompleted) / communityCount * 100
```

---

## ⚠️ 注意事项

### 1. 字段名必须准确
- 字段名必须与步骤1的 `output_param` 完全一致
- 区分大小写（如果数据库区分）
- 使用驼峰命名或下划线命名，保持一致

### 2. 避免除以零
```
// ❌ 错误：可能除以零
SUM(score) / SUM(count)

// ✓ 正确：使用条件或确保分母不为零
SUM(score) / (SUM(count) + 0.0001)
```

### 3. 括号使用
```
// ❌ 错误：运算顺序不明确
SUM(a) + SUM(b) / SUM(c)

// ✓ 正确：使用括号明确顺序
(SUM(a) + SUM(b)) / SUM(c)
```

### 4. 常数格式
```
// ✓ 支持整数
SUM(score) * 100

// ✓ 支持小数
SUM(score) * 0.5

// ✓ 支持科学计数法
SUM(score) * 1e4
```

---

## 🐛 调试技巧

### 1. 查看日志
执行乡镇聚合时，查看后端日志：
```
INFO - 处理乡镇: 青竹街道, 社区数量: 10
```

### 2. 分步验证
将复杂表达式拆分成多个简单表达式：
```
// 复杂表达式
(SUM(a) + SUM(b)) / (2 * communityCount)

// 拆分为两个简单表达式
表达式1: SUM(a) + SUM(b)  -> 验证总和是否正确
表达式2: 上面的结果 / (2 * communityCount)  -> 验证平均值
```

### 3. 检查中间值
添加临时算法输出中间结果：
```sql
-- 输出总和
INSERT INTO step_algorithm (...) VALUES (..., 'SUM(a)+SUM(b)', 'tempSum', ...);

-- 输出最终结果
INSERT INTO step_algorithm (...) VALUES (..., 'SUM(a)+SUM(b)/(2*communityCount)', 'finalAvg', ...);
```

---

## 📊 完整示例

### 需求：
计算乡镇的综合减灾能力指数，包括：
1. 管理能力总分
2. 准备能力平均分
3. 自救能力密度（每千人）
4. 综合指数（三项加权平均）

### SQL配置：
```sql
-- 步骤2：乡镇聚合
-- 算法1：管理能力总分
INSERT INTO step_algorithm (step_id, algorithm_code, algorithm_name, ql_expression, output_param, calculation_order, status)
VALUES (51, 'TOWN_MGMT_TOTAL', '管理能力总分', 'SUM(disasterMgmtScore)', 'townshipMgmtTotal', 1, 1);

-- 算法2：准备能力平均分
INSERT INTO step_algorithm (step_id, algorithm_code, algorithm_name, ql_expression, output_param, calculation_order, status)
VALUES (51, 'TOWN_PREP_AVG', '准备能力平均分', 'SUM(preparednessScore)/communityCount', 'townshipPrepAvg', 2, 1);

-- 算法3：自救能力密度
INSERT INTO step_algorithm (step_id, algorithm_code, algorithm_name, ql_expression, output_param, calculation_order, status)
VALUES (51, 'TOWN_RESCUE_DENSITY', '自救能力密度', 'SUM(selfRescueScore)/SUM(population)*1000', 'townshipRescueDensity', 3, 1);

-- 算法4：综合指数（权重: 管理0.4, 准备0.3, 自救0.3）
INSERT INTO step_algorithm (step_id, algorithm_code, algorithm_name, ql_expression, output_param, calculation_order, status)
VALUES (51, 'TOWN_COMPOSITE', '综合减灾指数',
    '(SUM(disasterMgmtScore)*0.4 + SUM(preparednessScore)*0.3 + SUM(selfRescueScore)*0.3) / communityCount',
    'townshipCompositeIndex', 4, 1);
```

### 预期结果：
每个乡镇将得到4个输出字段：
- `townshipMgmtTotal` - 管理能力总分
- `townshipPrepAvg` - 准备能力平均分
- `townshipRescueDensity` - 自救能力密度
- `townshipCompositeIndex` - 综合减灾指数

---

## 🔄 兼容性说明

### 旧格式仍然支持
如果不使用SUM()表达式，旧的配置方式仍然有效：

```sql
-- 旧格式：使用 input_params
INSERT INTO step_algorithm (step_id, input_params, output_param, ...)
VALUES (51, 'disasterMgmtScore', 'townshipMgmtAvg', ...);
```
这将计算字段的平均值（总和除以社区数）。

### 迁移建议
如果需要更灵活的计算，建议迁移到SUM()表达式：

```sql
-- 从旧格式
input_params = 'disasterMgmtScore'  -> 平均值

-- 迁移到新格式
ql_expression = 'SUM(disasterMgmtScore)/communityCount'  -> 平均值（明确）
ql_expression = 'SUM(disasterMgmtScore)'  -> 总和（更清晰）
```

---

## 📚 相关文档

- [乡镇聚合问题分析](./TOWNSHIP_AGGREGATION_ISSUE_ANALYSIS.md)
- [步骤1诊断指南](./STEP1_MISSING_COLUMNS_DIAGNOSTIC.md)
- [下一步操作指南](./NEXT_STEPS.md)

---

**更新时间：** 2025-10-31
**文档版本：** 1.0
**作者：** Claude Code
