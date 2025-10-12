# 动态 QLExpress 表达式系统 - 完整指南

## ✅ 已实现功能

系统现已**完全支持**通过 QLExpress 动态表达式实现所有评估算法，包括复杂的跨区域聚合计算。

### 核心特性

1. **标准 QLExpress 表达式** - 支持简单的单记录计算
2. **特殊标记处理** - 支持跨区域聚合算法
   - `@NORMALIZE` - 向量归一化
   - `@TOPSIS_POSITIVE` - TOPSIS 优解距离
   - `@TOPSIS_NEGATIVE` - TOPSIS 劣解距离
   - `@GRADE` - 能力分级

3. **动态配置** - 所有公式存储在数据库中，无需修改代码即可调整
4. **完整实现** - 步骤1到步骤5全部支持动态表达式

## 📁 新增文件

1. **SpecialAlgorithmService.java** - 特殊算法服务接口
2. **SpecialAlgorithmServiceImpl.java** - 特殊算法实现类
3. **ModelExecutionServiceImpl.java** - 已更新，集成特殊算法支持

## 🔧 系统架构

```
数据库 (step_algorithm 表)
    ↓ 读取算法配置
ModelExecutionServiceImpl
    ├─ 标准表达式 → QLExpressService
    └─ 特殊标记 → SpecialAlgorithmService
         ├─ @NORMALIZE → 归一化算法
         ├─ @TOPSIS_POSITIVE → 优解距离
         ├─ @TOPSIS_NEGATIVE → 劣解距离
         └─ @GRADE → 能力分级
```

## 📝 特殊标记详解

### 1. @NORMALIZE:指标名

**功能**：属性向量归一化

**公式**：`归一化值 = 当前值 / SQRT(SUMSQ(所有区域的值))`

**示例**：
```sql
INSERT INTO step_algorithm (...) VALUES
(..., '@NORMALIZE:teamManagement', 'teamManagementNorm', ...);
```

**说明**：
- 自动收集所有区域的指标值
- 计算平方和的平方根作为分母
- 返回当前区域的归一化值

### 2. @TOPSIS_POSITIVE:指标列表

**功能**：计算到正理想解（优）的距离

**公式**：`优解距离 = SQRT(SUM((最大值 - 当前值)²))`

**示例**：
```sql
INSERT INTO step_algorithm (...) VALUES
(..., '@TOPSIS_POSITIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted', 
'disasterMgmtPositive', ...);
```

**说明**：
- 支持多个指标（逗号分隔）
- 自动找到每个指标的最大值
- 计算欧几里得距离

### 3. @TOPSIS_NEGATIVE:指标列表

**功能**：计算到负理想解（劣）的距离

**公式**：`劣解距离 = SQRT(SUM((最小值 - 当前值)²))`

**示例**：
```sql
INSERT INTO step_algorithm (...) VALUES
(..., '@TOPSIS_NEGATIVE:materialReserveWeighted,medicalSupportWeighted', 
'disasterPrepNegative', ...);
```

**说明**：
- 支持多个指标（逗号分隔）
- 自动找到每个指标的最小值
- 计算欧几里得距离

### 4. @GRADE:分数字段

**功能**：基于均值和标准差的五级分类

**分级规则**：
```
如果 μ <= 0.5σ:
  value >= μ+1.5σ → 强
  value >= μ+0.5σ → 较强
  否则 → 中等

如果 μ <= 1.5σ:
  value >= μ+1.5σ → 强
  value >= μ+0.5σ → 较强
  value >= μ-0.5σ → 中等
  否则 → 较弱

否则:
  value >= μ+1.5σ → 强
  value >= μ+0.5σ → 较强
  value >= μ-0.5σ → 中等
  value >= μ-1.5σ → 较弱
  否则 → 弱
```

**示例**：
```sql
INSERT INTO step_algorithm (...) VALUES
(..., '@GRADE:disasterMgmtScore', 'disasterMgmtGrade', ...);
```

## 🚀 使用方法

### 方法1: 通过前端界面

1. 访问 `http://localhost:5174/model-management`
2. 选择"标准减灾能力评估模型"
3. 点击"查看步骤"
4. 点击"执行步骤"按钮
5. 系统将自动：
   - 读取数据库中的算法配置
   - 执行标准表达式和特殊标记
   - 生成计算结果

### 方法2: 通过API调用

```powershell
# 执行完整模型
$body = @{
    modelId = 1
    regionCodes = @(
        "township_四川省_眉山市_青神县_青竹街道",
        "township_四川省_眉山市_青神县_瑞峰镇"
    )
    weightConfigId = 1
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/evaluation/execute-model" `
    -Method POST -Body $body -ContentType "application/json"
```

## 📊 数据库配置示例

### 步骤1: 评估指标赋值（标准表达式）

```sql
-- 队伍管理能力
INSERT INTO step_algorithm VALUES
(NULL, @step1_id, '队伍管理能力计算', 'TEAM_MANAGEMENT', 1, 
'(managementStaff / population) * 10000', NULL, 'teamManagement', 
'队伍管理能力=(本级灾害管理工作人员总数/常住人口数量)*10000', 1, NOW());

-- 风险评估能力
INSERT INTO step_algorithm VALUES
(NULL, @step1_id, '风险评估能力计算', 'RISK_ASSESSMENT', 2, 
'riskAssessment == "是" ? 1.0 : 0.0', NULL, 'riskAssessment', 
'风险评估能力=IF(是否开展风险评估="是",1,0)', 1, NOW());
```

### 步骤2: 属性向量归一化（特殊标记）

```sql
-- 归一化公式使用 @NORMALIZE 标记
INSERT INTO step_algorithm VALUES
(NULL, @step2_id, '队伍管理能力归一化', 'TEAM_MANAGEMENT_NORM', 1, 
'@NORMALIZE:teamManagement', NULL, 'teamManagementNorm', 
'队伍管理能力（归一化）=本乡镇值/SQRT(SUMSQ(全部乡镇值))', 1, NOW());
```

### 步骤3: 二级指标定权（标准表达式）

```sql
-- 定权公式：归一化值 * 权重
INSERT INTO step_algorithm VALUES
(NULL, @step3_id, '队伍管理能力定权', 'TEAM_MANAGEMENT_WEIGHT', 1, 
'teamManagementNorm * weight_TEAM_MANAGEMENT', NULL, 'teamManagementWeighted', 
'队伍管理能力（定权）=归一化值*权重', 1, NOW());
```

### 步骤4: 优劣解算法（特殊标记）

```sql
-- 优解距离
INSERT INTO step_algorithm VALUES
(NULL, @step4_id, '灾害管理能力优解', 'DISASTER_MGMT_POSITIVE', 1, 
'@TOPSIS_POSITIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted', 
NULL, 'disasterMgmtPositive', '优解距离计算', 1, NOW());

-- 劣解距离
INSERT INTO step_algorithm VALUES
(NULL, @step4_id, '灾害管理能力劣解', 'DISASTER_MGMT_NEGATIVE', 2, 
'@TOPSIS_NEGATIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted', 
NULL, 'disasterMgmtNegative', '劣解距离计算', 1, NOW());
```

### 步骤5: 能力值计算与分级

```sql
-- 能力值计算（标准表达式）
INSERT INTO step_algorithm VALUES
(NULL, @step5_id, '灾害管理能力值', 'DISASTER_MGMT_SCORE', 1, 
'disasterMgmtNegative / (disasterMgmtNegative + disasterMgmtPositive)', 
NULL, 'disasterMgmtScore', '能力值=劣/(劣+优)', 1, NOW());

-- 能力分级（特殊标记）
INSERT INTO step_algorithm VALUES
(NULL, @step5_id, '灾害管理能力分级', 'DISASTER_MGMT_GRADE', 2, 
'@GRADE:disasterMgmtScore', NULL, 'disasterMgmtGrade', 
'基于均值和标准差分级', 1, NOW());
```

## 🔍 执行流程详解

### 两遍扫描机制

系统采用两遍扫描来支持跨区域聚合：

#### 第一遍：准备数据
```java
// 为所有区域加载基础数据和前序步骤结果
for (String regionCode : regionCodes) {
    Map<String, Object> regionContext = new HashMap<>(inputData);
    loadPreviousStepOutputs(regionContext, regionCode, inputData);
    loadSurveyData(regionContext, regionCode);
    allRegionContexts.put(regionCode, regionContext);
}
```

#### 第二遍：执行算法
```java
// 为每个区域执行算法（现在可以访问所有区域的数据）
for (String regionCode : regionCodes) {
    for (StepAlgorithm algorithm : algorithms) {
        if (algorithm.qlExpression.startsWith("@")) {
            // 调用特殊算法服务（可访问 allRegionContexts）
            result = specialAlgorithmService.executeSpecialAlgorithm(...);
        } else {
            // 执行标准表达式
            result = qlExpressService.execute(...);
        }
    }
}
```

## ⚙️ 技术实现细节

### 归一化算法

```java
public Double normalize(String indicatorName, String currentRegionCode, 
                       Map<String, Map<String, Object>> allRegionData) {
    // 1. 收集所有区域的指标值
    List<Double> allValues = collectAllValues(indicatorName, allRegionData);
    
    // 2. 计算平方和的平方根
    double sumSquares = allValues.stream().mapToDouble(v -> v * v).sum();
    double denominator = Math.sqrt(sumSquares);
    
    // 3. 获取当前区域的值
    double currentValue = getCurrentValue(indicatorName, currentRegionCode, allRegionData);
    
    // 4. 返回归一化值
    return currentValue / denominator;
}
```

### TOPSIS 优解距离

```java
public Double calculateTopsisPositive(String indicators, String currentRegionCode,
                                     Map<String, Map<String, Object>> allRegionData) {
    String[] indicatorArray = indicators.split(",");
    double sumSquares = 0.0;
    
    for (String indicator : indicatorArray) {
        // 找到该指标的最大值
        double maxValue = findMaxValue(indicator, allRegionData);
        double currentValue = getCurrentValue(indicator, currentRegionCode, allRegionData);
        
        // 累加平方差
        double diff = maxValue - currentValue;
        sumSquares += diff * diff;
    }
    
    return Math.sqrt(sumSquares);
}
```

## 📈 性能优化

1. **数据预加载**：第一遍扫描时加载所有必要数据，避免重复查询
2. **流式计算**：使用 Java 8 Stream API 进行高效的数据处理
3. **缓存机制**：区域上下文数据在整个步骤执行过程中保持缓存

## 🎯 优势对比

### 之前（硬编码）❌
- ❌ 修改算法需要修改 Java 代码
- ❌ 需要重新编译和部署
- ❌ 难以进行算法对比和A/B测试
- ❌ 不利于非技术人员调整

### 现在（动态表达式）✅
- ✅ 所有算法存储在数据库中
- ✅ 通过 SQL 或界面即可修改
- ✅ 无需重启服务
- ✅ 支持版本管理和回滚
- ✅ 便于算法实验和优化

## 🧪 测试验证

### 1. 编译验证

```powershell
mvn clean compile
# 结果：74 source files compiled successfully ✅
```

### 2. 启动服务

```powershell
mvn spring-boot:run
```

### 3. 测试 API

```powershell
# 测试完整模型执行
POST http://localhost:8081/api/evaluation/execute-model
Body: {
    "modelId": 1,
    "regionCodes": ["township_四川省_眉山市_青神县_青竹街道"],
    "weightConfigId": 1
}
```

### 4. 检查日志

查看以下日志确认特殊算法执行：
```
执行特殊算法: marker=NORMALIZE, params=teamManagement, region=...
归一化计算: indicator=teamManagement, region=...
归一化结果: indicator=teamManagement, value=..., normalized=...
```

## 📚 相关文件

- ✅ `SpecialAlgorithmService.java` - 服务接口
- ✅ `SpecialAlgorithmServiceImpl.java` - 实现类
- ✅ `ModelExecutionServiceImpl.java` - 集成特殊算法
- ✅ `update_steps_2_to_5.sql` - 数据库配置示例
- ✅ `DYNAMIC_QLEXPRESS_GUIDE.md` (本文件) - 使用指南

## 🎓 扩展指南

### 添加新的特殊标记

1. 在 `SpecialAlgorithmService` 接口中添加方法
2. 在 `SpecialAlgorithmServiceImpl` 中实现逻辑
3. 在 `executeSpecialAlgorithm` 方法的 switch 中添加 case
4. 在数据库中使用新标记

示例：添加 `@AVERAGE` 标记

```java
// 1. 接口
Double calculateAverage(String indicators, ...);

// 2. 实现
public Double calculateAverage(String indicators, ...) {
    String[] indicatorArray = indicators.split(",");
    double sum = 0.0;
    for (String indicator : indicatorArray) {
        sum += getCurrentValue(indicator, ...);
    }
    return sum / indicatorArray.length;
}

// 3. Switch case
case "AVERAGE":
    return calculateAverage(params, ...);

// 4. 数据库
'@AVERAGE:indicator1,indicator2,indicator3'
```

## 💡 最佳实践

1. **命名规范**：输出参数使用驼峰命名（如 `teamManagementNorm`）
2. **参数传递**：特殊标记后用冒号分隔参数（如 `@NORMALIZE:teamManagement`）
3. **多参数**：使用逗号分隔（如 `@TOPSIS_POSITIVE:ind1,ind2,ind3`）
4. **错误处理**：所有算法都有日志记录和异常处理
5. **类型安全**：自动进行 Number 到 Double 的转换

## 🔗 总结

系统现已**完全支持动态 QLExpress 表达式**，包括：
- ✅ 标准表达式（步骤1、步骤3、步骤5的计算）
- ✅ 归一化算法（步骤2）
- ✅ TOPSIS 优劣解（步骤4）
- ✅ 能力分级（步骤5）

所有算法都通过数据库配置，无需修改代码即可调整！

**编译状态**：✅ 成功（74个源文件）
**实现状态**：✅ 完成（100%动态化）
**测试状态**：✅ 待验证
