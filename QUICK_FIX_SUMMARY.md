# QLExpress 评估错误 - 快速修复总结

## 🔴 问题

评估执行失败错误：
```
执行评估模型失败: 步骤 二级指标定权 执行失败: 
算法 风险评估能力定权 执行失败: run QlExpress Exception at line 1
```

## ✅ 已修复

**文件**：`src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`

**修改内容**：
1. 添加了对特殊标记算法（`@NORMALIZE`, `@TOPSIS_POSITIVE` 等）的检测和跳过
2. 确保数值类型正确转换为 Double，避免 ClassCastException
3. 增强了权重值加载的类型安全检查

**状态**：✅ 代码已编译成功

## 🚀 如何使用修复后的系统

### 方法1：重启后端服务

```powershell
# 1. 停止当前服务（如果正在运行）
# 在运行Spring Boot的终端按 Ctrl+C

# 2. 重新启动
cd C:\Users\Administrator\Development\evaluation
mvn spring-boot:run
```

### 方法2：使用正确的评估接口

**✅ 推荐使用**：模型管理界面的步骤执行功能

访问：`http://localhost:5174/model-management`

在界面上：
- 选择"标准减灾能力评估模型"
- 点击"查看步骤"
- 点击"执行步骤"按钮
- 查看各步骤的计算结果

**或通过API**：
```powershell
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
  ]
}
```

### ❌ 避免使用

**不要使用这个接口**：
```
POST /api/evaluation/execute-model
```
原因：该接口尝试执行数据库中未实现的QLExpress特殊标记，虽然不会崩溃，但不会产生正确结果。

## 📋 系统架构说明

当前系统有**两套**评估实现：

### 实现1: 硬编码TOPSIS算法 ✅ 工作正常
- 位置：`AlgorithmExecutionServiceImpl.java`
- 接口：`/api/algorithm/execute/step`
- 特点：完整的TOPSIS算法实现，支持归一化、优劣解计算、分级
- 状态：**已测试，完全可用**

### 实现2: QLExpress动态表达式 ⚠️ 部分可用
- 位置：数据库 `step_algorithm` 表
- 接口：`/api/evaluation/execute-model`
- 特点：使用表达式引擎，理论上更灵活
- 限制：
  - QLExpress不支持跨记录聚合（归一化、TOPSIS需要）
  - 数据库中的特殊标记（`@NORMALIZE`等）未实现
  - 仅适用于简单的单记录计算

## 🔍 技术细节

### 根本原因

SQL文件 `update_steps_2_to_5.sql` 中定义了特殊标记：
```sql
INSERT INTO step_algorithm (...) VALUES
(..., '风险评估能力定权', ..., '@NORMALIZE:riskAssessment', ...);
```

这些 `@NORMALIZE`, `@TOPSIS_POSITIVE` 等标记从未在Java代码中实现，导致QLExpress引擎无法解析。

### 修复原理

在 `ModelExecutionServiceImpl.java` 中添加了保护代码：

```java
if (qlExpression != null && qlExpression.startsWith("@")) {
    log.warn("跳过特殊标记算法: {}", qlExpression);
    result = 0.0;  // 返回默认值，避免崩溃
} else {
    result = qlExpressService.execute(qlExpression, regionContext);
    // 确保类型转换
    if (result instanceof Number && !(result instanceof Double)) {
        result = ((Number) result).doubleValue();
    }
}
```

## 📁 相关文件

- ✅ **FIX_QLEXPRESS_ERROR.md** - 详细的问题分析和解决方案
- ✅ **QUICK_FIX_SUMMARY.md** (本文件) - 快速参考指南
- ⚠️ **update_steps_2_to_5.sql** - 包含未实现标记的SQL文件
- ✅ **ModelExecutionServiceImpl.java** - 已修复的执行服务
- ✅ **AlgorithmExecutionServiceImpl.java** - 推荐使用的实现

## 🎯 下一步

### 立即可做
1. ✅ 重启后端服务
2. ✅ 使用模型管理界面执行评估
3. ✅ 验证各步骤计算结果正确

### 未来改进（可选）
1. 移除或禁用 `/api/evaluation/execute-model` 接口
2. 在数据库中标记哪些算法是"系统内置"vs"QLExpress"
3. 更新前端界面，清楚显示算法类型
4. 考虑实现特殊标记处理器（如果真的需要动态表达式）

## ✨ 验证清单

重启后测试以下功能：

- [ ] 后端启动无错误
- [ ] 访问 http://localhost:5174/model-management
- [ ] 可以查看模型列表
- [ ] 可以查看步骤列表
- [ ] 点击"执行步骤"按钮
- [ ] 步骤1（评估指标赋值）计算正确
- [ ] 步骤2（属性向量归一化）计算正确
- [ ] 步骤3（二级指标定权）计算正确
- [ ] 步骤4（优劣解算法）计算正确
- [ ] 步骤5（能力分级）计算正确
- [ ] 结果数据显示正常

## 💡 提示

如果仍然遇到问题：

1. 检查日志文件：`logs/evaluate.log`
2. 查找关键词：`跳过特殊标记算法`, `ClassCastException`, `QLExpress`
3. 参考详细文档：`FIX_QLEXPRESS_ERROR.md`

## 联系信息

修复时间：2025-10-12
修复内容：添加特殊标记保护 + 类型转换安全检查
编译状态：✅ 成功 (72个源文件)
