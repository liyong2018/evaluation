# 修复 QLExpress 表达式变量命名问题

## 问题描述

在执行模型评估时，QLExpress 表达式执行失败，错误信息：

```
执行评估模型失败: 步骤 评估指标赋值 执行失败: 算法 队伍管理能力计算 执行失败: run QlExpress Exception at line 1
```

## 问题原因

数据库中存储的 QLExpress 表达式使用了下划线命名风格（snake_case），例如：

```java
(management_staff / population) * 10000
```

但是，在 `ModelExecutionServiceImpl.addSurveyDataToContext()` 方法中，我们只添加了驼峰命名风格（camelCase）的变量到上下文：

```java
context.put("managementStaff", surveyData.getManagementStaff());
context.put("population", surveyData.getPopulation());
```

这导致 QLExpress 在执行表达式时找不到 `management_staff` 变量，从而抛出异常。

## 命名风格差异

### Java 实体类（驼峰命名）
```java
private Integer managementStaff;  // 驼峰命名
private Long population;
private String riskAssessment;
private Double fundingAmount;
```

### 数据库表（下划线命名）
```sql
management_staff INT
population BIGINT
risk_assessment VARCHAR(50)
funding_amount DECIMAL(10,2)
```

### QLExpress 表达式（下划线命名）
```java
(management_staff / population) * 10000
```

## 解决方案

在 `addSurveyDataToContext()` 方法中同时添加两种命名风格的变量到上下文，以支持不同的表达式编写风格。

### 修改前的代码

```java
private void addSurveyDataToContext(Map<String, Object> context, SurveyData surveyData) {
    context.put("regionCode", surveyData.getRegionCode());
    context.put("province", surveyData.getProvince());
    context.put("city", surveyData.getCity());
    context.put("county", surveyData.getCounty());
    context.put("township", surveyData.getTownship());
    context.put("population", surveyData.getPopulation());
    context.put("managementStaff", surveyData.getManagementStaff());
    // ... 其他字段
}
```

### 修改后的代码

```java
private void addSurveyDataToContext(Map<String, Object> context, SurveyData surveyData) {
    // 地区信息
    context.put("regionCode", surveyData.getRegionCode());
    context.put("region_code", surveyData.getRegionCode());
    context.put("province", surveyData.getProvince());
    context.put("city", surveyData.getCity());
    context.put("county", surveyData.getCounty());
    context.put("township", surveyData.getTownship());
    
    // 人口数据（驼峰和下划线两种命名）
    context.put("population", surveyData.getPopulation());
    
    // 管理人员（驼峰和下划线两种命名）
    context.put("managementStaff", surveyData.getManagementStaff());
    context.put("management_staff", surveyData.getManagementStaff());
    
    // 风险评估（驼峰和下划线两种命名）
    context.put("riskAssessment", surveyData.getRiskAssessment());
    context.put("risk_assessment", surveyData.getRiskAssessment());
    
    // 资金投入（驼峰和下划线两种命名）
    context.put("fundingAmount", surveyData.getFundingAmount());
    context.put("funding_amount", surveyData.getFundingAmount());
    
    // 物资储备（驼峰和下划线两种命名）
    context.put("materialValue", surveyData.getMaterialValue());
    context.put("material_value", surveyData.getMaterialValue());
    
    // 医院床位（驼峰和下划线两种命名）
    context.put("hospitalBeds", surveyData.getHospitalBeds());
    context.put("hospital_beds", surveyData.getHospitalBeds());
    
    // 消防员（驼峰和下划线两种命名）
    context.put("firefighters", surveyData.getFirefighters());
    
    // 志愿者（驼峰和下划线两种命名）
    context.put("volunteers", surveyData.getVolunteers());
    
    // 民兵预备役（驼峰和下划线两种命名）
    context.put("militiaReserve", surveyData.getMilitiaReserve());
    context.put("militia_reserve", surveyData.getMilitiaReserve());
    
    // 培训参与者（驼峰和下划线两种命名）
    context.put("trainingParticipants", surveyData.getTrainingParticipants());
    context.put("training_participants", surveyData.getTrainingParticipants());
    
    // 避难所容量（驼峰和下划线两种命名）
    context.put("shelterCapacity", surveyData.getShelterCapacity());
    context.put("shelter_capacity", surveyData.getShelterCapacity());
}
```

## 支持的变量命名

修复后，QLExpress 表达式可以使用以下两种命名风格：

### 驼峰命名（camelCase）
```java
(managementStaff / population) * 10000
fundingAmount / materialValue
hospitalBeds + trainingParticipants
```

### 下划线命名（snake_case）
```java
(management_staff / population) * 10000
funding_amount / material_value
hospital_beds + training_participants
```

## 影响的变量列表

以下变量现在同时支持两种命名风格：

| 驼峰命名 | 下划线命名 | 说明 |
|---------|-----------|------|
| regionCode | region_code | 行政区代码 |
| managementStaff | management_staff | 灾害管理工作人员总数 |
| riskAssessment | risk_assessment | 是否开展风险评估 |
| fundingAmount | funding_amount | 防灾减灾救灾资金投入 |
| materialValue | material_value | 储备物资装备折合金额 |
| hospitalBeds | hospital_beds | 实有住院床位数 |
| militiaReserve | militia_reserve | 民兵预备役人数 |
| trainingParticipants | training_participants | 培训和演练参与人次 |
| shelterCapacity | shelter_capacity | 应急避难场所容量 |

## 优点

1. **向后兼容**：现有使用下划线命名的表达式可以继续工作
2. **灵活性**：开发者可以选择自己习惯的命名风格
3. **易于迁移**：可以逐步将表达式迁移到驼峰命名风格
4. **减少错误**：避免因命名风格不一致导致的执行错误

## 最佳实践

### 推荐的命名风格

建议在新编写的 QLExpress 表达式中统一使用**驼峰命名**风格：

```java
// 推荐 ✅
(managementStaff / population) * 10000

// 不推荐（虽然仍然支持）❌
(management_staff / population) * 10000
```

原因：
- 与 Java 代码风格一致
- 更符合 QLExpress 的常见用法
- 避免混淆

### 更新现有表达式

如果需要更新数据库中的表达式，可以执行以下 SQL：

```sql
-- 示例：更新队伍管理能力计算表达式
UPDATE step_algorithm 
SET ql_expression = '(managementStaff / population) * 10000'
WHERE algorithm_code = 'MANAGEMENT_CAPABILITY';

-- 批量查找需要更新的表达式
SELECT id, algorithm_name, ql_expression 
FROM step_algorithm 
WHERE ql_expression LIKE '%_%';
```

## 测试验证

修复后，执行以下步骤验证：

1. ✅ 编译成功
2. ✅ 服务启动成功
3. ✅ 使用下划线命名的表达式可以正常执行
4. ✅ 使用驼峰命名的表达式可以正常执行

## 相关文件

- `src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java` - 模型执行服务实现
- `src/main/java/com/evaluate/entity/SurveyData.java` - 调查数据实体类
- 数据库表：`step_algorithm` - 步骤算法配置表

## 修复时间

2025年10月12日 18:09

## 后续改进建议

1. **标准化命名**：在模型管理页面添加表达式编辑器，提供变量名称提示
2. **表达式验证**：在保存表达式前进行语法和变量名称验证
3. **文档化**：在系统中提供可用变量列表和命名规范文档
4. **迁移工具**：提供批量转换工具，将下划线命名转换为驼峰命名

## 备注

此修复确保了无论使用哪种命名风格编写的 QLExpress 表达式都能正常执行，提高了系统的灵活性和健壮性。建议在新开发的功能中统一使用驼峰命名风格。
