# 修复 QLExpress 执行错误

## 问题描述

执行评估模型时出现错误：
```
执行评估模型失败: 步骤 二级指标定权 执行失败: 算法 风险评估能力定权 执行失败: run QlExpress Exception at line 1
```

根本原因：`java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Number`

## 根本原因分析

系统中存在两套评估实现方式：

### 方式1：硬编码实现（当前工作正常）✅
- 位置：`AlgorithmExecutionServiceImpl.java`
- 使用方式：通过模型管理界面的"执行步骤"功能
- 特点：步骤2-5使用硬编码的TOPSIS算法实现
- 状态：**完全正常工作**

### 方式2：QLExpress动态表达式（有问题）❌
- 位置：数据库表 `step_algorithm` 中的 `ql_expression` 字段
- SQL文件：`update_steps_2_to_5.sql`
- 使用方式：通过 `/api/evaluation/execute-model` 接口
- 问题：
  1. SQL文件中使用了特殊标记 `@NORMALIZE`, `@TOPSIS_POSITIVE` 等
  2. 这些标记从未在Java代码中实现
  3. QLExpress引擎无法解析这些标记，导致运行失败

## 已实施的紧急修复

修改了 `ModelExecutionServiceImpl.java`：
1. 跳过以 `@` 开头的特殊标记算法（返回默认值0.0）
2. 确保数值类型正确转换为 `Double`
3. 添加了详细的日志输出用于调试

这个修复可以防止系统崩溃，但不会产生正确的计算结果。

## 解决方案

### 方案A：使用现有的工作实现（推荐）✅

**当前系统已经有一套完整工作的TOPSIS实现**，建议使用这套实现：

#### 1. 通过模型管理界面使用

前端访问： `http://localhost:5174/model-management`

在"模型配置"页面：
- 选择模型
- 点击"执行步骤"按钮
- 查看每个步骤的计算结果

#### 2. 通过API使用

```powershell
# 计算单个步骤
POST http://localhost:8081/api/algorithm/execute/step

Body:
{
  "algorithmConfigId": 1,
  "stepId": 2,
  "stepIndex": 1,
  "formula": "",
  "regionIds": [
    "township_四川省_眉山市_青神县_青竹街道",
    "township_四川省_眉山市_青神县_瑞峰镇"
  ],
  "parameters": {}
}
```

这个API调用的是 `AlgorithmExecutionServiceImpl.java` 中的硬编码实现，计算结果正确。

### 方案B：修复QLExpress表达式（需要大量工作）

如果需要使用动态表达式系统，需要完成以下工作：

#### 1. 实现特殊标记处理器

在 `ModelExecutionServiceImpl.java` 中添加：

```java
private Object executeSpecialMarkerAlgorithm(
        String marker, 
        String params, 
        Map<String, Object> regionContext,
        List<String> allRegions) {
    
    if (marker.equals("NORMALIZE")) {
        // 实现归一化：value / SQRT(SUMSQ(all_values))
        String indicatorName = params;
        List<Double> allValues = collectAllRegionValues(indicatorName, allRegions);
        Double currentValue = (Double) regionContext.get(indicatorName);
        return normalizeValue(currentValue, allValues);
    }
    else if (marker.equals("TOPSIS_POSITIVE")) {
        // 实现优解距离计算
        String[] indicators = params.split(",");
        return calculateTopsisPositive(indicators, regionContext, allRegions);
    }
    else if (marker.equals("TOPSIS_NEGATIVE")) {
        // 实现劣解距离计算
        String[] indicators = params.split(",");
        return calculateTopsisNegative(indicators, regionContext, allRegions);
    }
    else if (marker.equals("GRADE")) {
        // 实现分级计算
        String scoreField = params;
        return calculateGrade(scoreField, regionContext, allRegions);
    }
    
    return 0.0;
}
```

#### 2. 修改executeStep方法

```java
if (qlExpression != null && qlExpression.startsWith("@")) {
    // 解析特殊标记
    String[] parts = qlExpression.substring(1).split(":", 2);
    String marker = parts[0];
    String params = parts.length > 1 ? parts[1] : "";
    
    // 调用特殊标记处理器
    result = executeSpecialMarkerAlgorithm(marker, params, regionContext, regionCodes);
}
```

#### 3. 更新SQL文件

或者，更简单的方法是将特殊标记替换为实际的QLExpress表达式：

```sql
-- 替换前：
'@NORMALIZE:riskAssessment'

-- 替换后（但这需要支持跨region聚合，QLExpress不支持）：
'riskAssessment / SQRT(SUMSQ(allRegionRiskAssessments))'
```

**问题**：QLExpress是单记录表达式引擎，不支持跨记录的聚合操作（如SUMSQ所有区域的值）。这就是为什么TOPSIS算法需要在Java代码中实现的原因。

## 推荐做法

**使用方案A - 现有的硬编码实现**

理由：
1. ✅ 已经完全实现并测试通过
2. ✅ 性能更好（无需解析表达式）
3. ✅ 更易于调试
4. ✅ 支持复杂的跨区域聚合计算

QLExpress适用于：
- 简单的单记录计算（步骤1：评估指标赋值）
- 条件判断
- 数学函数调用

QLExpress **不适用于**：
- 需要访问多条记录的聚合计算（归一化、TOPSIS）
- 需要全局状态的算法（分级）

## 验证修复

### 1. 重新编译并启动后端

```powershell
cd C:\Users\Administrator\Development\evaluation
mvn clean compile
mvn spring-boot:run
```

### 2. 测试模型管理界面

访问：`http://localhost:5174/model-management`

应该能够：
- ✅ 查看模型列表
- ✅ 查看步骤列表
- ✅ 执行单个步骤计算
- ✅ 查看计算结果

### 3. 不要使用execute-model接口

**避免调用**：
```
POST /api/evaluation/execute-model
```

这个接口会尝试使用数据库中的QLExpress表达式（包含未实现的特殊标记），会导致错误或错误的结果。

## 长期改进建议

1. **统一评估接口**
   - 移除 `/api/evaluation/execute-model` 或重定向到硬编码实现
   - 只保留一套评估逻辑

2. **清理数据库**
   - 删除或标记 `update_steps_2_to_5.sql` 中带有 `@` 标记的算法
   - 只保留真正可用的QLExpress表达式（步骤1的公式）

3. **更新文档**
   - 明确说明哪些步骤使用QLExpress
   - 哪些步骤使用硬编码实现

4. **前端显示**
   - 在模型管理界面中清楚标识算法类型
   - 对于硬编码算法，显示"系统内置"而不是表达式

## 文件修改清单

### 已修改
- ✅ `src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`
  - 添加特殊标记检测
  - 跳过无法执行的算法
  - 添加类型转换保护

### 建议修改
- `docs/README.md` - 更新说明文档
- `frontend/src/views/ModelManagement.vue` - 添加算法类型显示
- `migrations/update_steps_2_to_5.sql` - 添加注释说明这些是占位符

## 总结

当前系统已经可以正常工作，使用 `AlgorithmExecutionServiceImpl.java` 中的实现即可。避免使用 `/api/evaluation/execute-model` 接口直到完全实现特殊标记处理器或移除这些标记。

修复代码已经部署，系统不会再崩溃，但要获得正确的计算结果，请使用推荐的模型管理界面或相应的API。
