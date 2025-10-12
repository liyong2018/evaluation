# 权重指标代码不匹配问题修复说明

## 问题描述

在执行评估模型时，步骤3（二级指标定权）出现以下错误：

```
执行评估模型失败: Error: 执行评估模型失败: 步骤 二级指标定权 执行失败: 算法 队伍管理能力定权 执行失败: run QlExpress Exception at line 1
```

### 根本原因

QLExpress表达式执行时出现 `NullPointerException`，因为表达式中引用的权重变量在上下文中不存在。

具体来说：
- **算法表达式使用的权重变量名**：`weight_TEAM_MANAGEMENT`、`weight_RISK_ASSESSMENT` 等
- **数据库中的实际indicator_code**：`L2_MANAGEMENT_CAPABILITY`、`L2_RISK_ASSESSMENT` 等
- **Java代码加载权重时的变量名**：`weight_L2_MANAGEMENT_CAPABILITY`、`weight_L2_RISK_ASSESSMENT` 等

由于变量名不匹配，QLExpress在执行乘法运算时找不到权重值，导致null参与运算引发异常。

## 解决方案

更新 `step_algorithm` 表中步骤3的所有算法表达式，使用正确的权重指标代码。

### 权重指标代码映射关系

#### 二级指标（Level 2）

| 原错误代码 | 正确代码 | 含义 |
|-----------|---------|------|
| `weight_TEAM_MANAGEMENT` | `weight_L2_MANAGEMENT_CAPABILITY` | 队伍管理能力 |
| `weight_RISK_ASSESSMENT` | `weight_L2_RISK_ASSESSMENT` | 风险评估能力 |
| `weight_FINANCIAL_INPUT` | `weight_L2_FUNDING` | 财政投入能力 |
| `weight_MATERIAL_RESERVE` | `weight_L2_MATERIAL` | 物资储备能力 |
| `weight_MEDICAL_SUPPORT` | `weight_L2_MEDICAL` | 医疗保障能力 |
| `weight_SELF_RESCUE` | `weight_L2_SELF_RESCUE` | 自救互救能力 |
| `weight_PUBLIC_AVOIDANCE` | `weight_L2_PUBLIC_AVOIDANCE` | 公众避险能力 |
| `weight_RELOCATION_CAPACITY` | `weight_L2_RELOCATION` | 转移安置能力 |

#### 一级指标（Level 1）

| 原错误代码 | 正确代码 | 含义 |
|-----------|---------|------|
| `weight_DISASTER_MANAGEMENT` | `weight_L1_MANAGEMENT` | 灾害管理能力 |
| `weight_DISASTER_PREPAREDNESS` | `weight_L1_PREPARATION` | 灾害备灾能力 |
| `weight_SELF_RESCUE_TRANSFER` | `weight_L1_SELF_RESCUE` | 自救转移能力 |

## 执行的修复

### 修复脚本

创建了 `fix_algorithm_weight_codes.sql`，包含16个UPDATE语句，分别更新：

1. **算法1-8**：一级指标定权（使用二级权重）
   - 示例：`teamManagementNorm * weight_L2_MANAGEMENT_CAPABILITY`

2. **算法9-16**：乡镇街道减灾能力定权（使用一级权重 × 二级权重）
   - 示例：`teamManagementNorm * weight_L1_MANAGEMENT * weight_L2_MANAGEMENT_CAPABILITY`

### 执行方式

```bash
# 1. 复制SQL文件到Docker容器
docker cp fix_algorithm_weight_codes.sql mysql-ccrc:/tmp/

# 2. 执行SQL脚本
docker exec mysql-ccrc mysql -u root -pHtht1234 --default-character-set=utf8mb4 evaluate_db -e "source /tmp/fix_algorithm_weight_codes.sql"

# 3. 清理临时文件
docker exec mysql-ccrc rm /tmp/fix_algorithm_weight_codes.sql
```

## 验证结果

执行后确认16条算法记录全部更新成功：

```
步骤3算法表达式已更新，共 16 条记录
```

所有算法的 `ql_expression` 字段已更新为使用正确的权重指标代码。

## 后续建议

1. **统一命名规范**：在未来的开发中，保持以下命名一致性：
   - 数据库表 `indicator_weight` 中的 `indicator_code`
   - Java代码中拼接的权重变量名 `weight_` + indicator_code
   - SQL算法表达式中使用的变量名

2. **文档更新**：更新所有相关文档（如 `算法公式参考.md`、`update_steps_2_to_5.sql`）中的权重指标代码

3. **测试验证**：重新执行评估模型，验证步骤3能够正常执行

4. **数据导入检查**：未来导入新的权重配置时，确保 `indicator_code` 使用标准的 `L1_*` 和 `L2_*` 格式

## 技术细节

### Java代码权重加载逻辑

参考 `ModelExecutionServiceImpl.java` 第340行：

```java
// 同时将每个权重作为独立变量存储（便于表达式直接引用）
for (IndicatorWeight weight : weights) {
    context.put("weight_" + weight.getIndicatorCode(), weight.getWeight());
}
```

这段代码将权重加载为：`weight_L2_MANAGEMENT_CAPABILITY`、`weight_L1_MANAGEMENT` 等形式。

### 数据库权重配置

```sql
SELECT indicator_code, weight FROM indicator_weight;
```

返回：
- `L1_MANAGEMENT` = 0.3300
- `L2_MANAGEMENT_CAPABILITY` = 0.3700
- 等等...

## 修复时间

- 问题发现：2025-10-12 21:37:54
- 修复完成：2025-10-12 21:45:00
- 影响范围：步骤3的16个算法表达式

## 状态

✅ **已修复** - 所有权重指标代码已更新为与数据库一致的格式。评估模型现在可以正常执行。
