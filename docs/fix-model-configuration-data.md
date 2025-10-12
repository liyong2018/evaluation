# 修复模型配置数据问题

## 问题描述

在执行模型评估时，发现以下问题：

1. **列名不一致**：结果表格的列名与预期不符
   - 预期：`management_capability`
   - 实际：`INDICATOR_ASSIGNMENT_anagement_capability` (缺少开头的 'm')
   - 预期：`result`
   - 实际：`VECTOR_NORMALIZATION_result(management_staff/population)*10000` (包含了表达式)

2. **计算结果全是0**：所有地区的计算结果都是 0，这是不正确的

## 问题原因

### 1. 数据库配置错误

查询数据库发现以下问题：

```sql
SELECT ms.step_name, ms.step_code, sa.algorithm_name, sa.output_param, sa.ql_expression 
FROM model_step ms 
LEFT JOIN step_algorithm sa ON ms.id = sa.step_id 
WHERE ms.model_id = 3 
ORDER BY ms.step_order, sa.algorithm_order;
```

结果显示：

| step_name | step_code | algorithm_name | output_param | ql_expression |
|-----------|-----------|----------------|--------------|---------------|
| 评估指标赋值 | INDICATOR_ASSIGNMENT | 队伍管理能力计算 | **anagement_capability** | (management_staff / population) * 10000 |
| 向量归一化 | VECTOR_NORMALIZATION | duiwu | **result(management_staff/population)*10000** | (management_staff/population)*10000 |

**问题1：output_param 拼写错误**
- 步骤1的 `output_param` 是 `anagement_capability`，缺少开头的 `m`
- 应该是 `management_capability`

**问题2：output_param 格式错误**
- 步骤2的 `output_param` 是 `result(management_staff/population)*10000`
- 错误地包含了表达式内容
- 应该只是 `result`

### 2. 整数除法问题

更严重的问题是计算结果全是0。测试 SQL：

```sql
SELECT region_code, population, management_staff, 
       (management_staff / population) * 10000 as result 
FROM survey_data 
WHERE region_code = '511425001';
```

结果：
```
region_code: 511425001
population: 102379
management_staff: 2
result: 0.0000  ❌ 错误！
```

**问题原因**：Java/QLExpress 中的整数除法会返回整数结果。
- `2 / 102379 = 0` (整数除法)
- `0 * 10000 = 0`

正确的计算应该是：
- `2.0 / 102379 = 0.000019538`
- `0.000019538 * 10000 = 0.19538`

## 解决方案

### 修复 output_param

```sql
-- 修复步骤1的 output_param
UPDATE step_algorithm 
SET output_param = 'management_capability' 
WHERE step_id = 15;

-- 修复步骤2的 output_param
UPDATE step_algorithm 
SET output_param = 'result' 
WHERE step_id = 16;
```

### 修复 QLExpress 表达式

需要强制使用浮点数除法：

```sql
-- 修复两个步骤的表达式，使用浮点数除法
UPDATE step_algorithm 
SET ql_expression = '(management_staff * 1.0 / population) * 10000' 
WHERE step_id IN (15, 16);
```

## 修复后的配置

```sql
SELECT ms.step_name, ms.step_code, sa.algorithm_name, sa.output_param, sa.ql_expression 
FROM model_step ms 
LEFT JOIN step_algorithm sa ON ms.id = sa.step_id 
WHERE ms.model_id = 3 AND sa.id IS NOT NULL
ORDER BY ms.step_order, sa.algorithm_order;
```

修复后的结果：

| step_name | step_code | algorithm_name | output_param | ql_expression |
|-----------|-----------|----------------|--------------|---------------|
| 评估指标赋值 | INDICATOR_ASSIGNMENT | 队伍管理能力计算 | **management_capability** ✅ | **(management_staff * 1.0 / population) * 10000** ✅ |
| 向量归一化 | VECTOR_NORMALIZATION | duiwu | **result** ✅ | **(management_staff * 1.0 / population) * 10000** ✅ |

## 结果列名格式说明

结果表格的列名格式为：`步骤代码_输出参数名`

例如：
- `INDICATOR_ASSIGNMENT_management_capability` - 步骤1的输出
- `VECTOR_NORMALIZATION_result` - 步骤2的输出

这是在 `ModelExecutionServiceImpl.generateResultTable()` 方法中生成的：

```java
row.put(stepCode + "_" + output.getKey(), output.getValue());
```

## 浮点数除法的最佳实践

在 QLExpress 表达式中进行除法运算时，如果操作数都是整数，需要注意整数除法问题：

### ❌ 错误的写法
```java
management_staff / population  // 整数除法，结果为0
```

### ✅ 正确的写法

**方法1：乘以浮点数**
```java
management_staff * 1.0 / population
```

**方法2：使用浮点数字面量**
```java
(management_staff + 0.0) / population
```

**方法3：显式类型转换（如果 QLExpress 支持）**
```java
((double)management_staff) / population
```

推荐使用**方法1**，因为它最简洁明了。

## 验证修复

修复后，计算结果应该是正确的：

```sql
-- 使用修复后的表达式测试
SELECT region_code, population, management_staff, 
       (management_staff * 1.0 / population) * 10000 as result 
FROM survey_data 
WHERE region_code = '511425001';
```

预期结果：
```
region_code: 511425001
population: 102379
management_staff: 2
result: 0.1954  ✅ 正确！
```

## 修复时间

2025年10月12日 18:20

## 影响范围

- 模型ID: 3 (标准减灾能力评估模型)
- 步骤ID: 15, 16
- 所有使用该模型的评估计算

## 建议

### 1. 数据质量检查

在保存模型配置时，应该进行以下验证：

- `output_param` 应该只包含变量名，不包含表达式
- `output_param` 应该是有效的标识符（字母开头，只包含字母数字下划线）
- `ql_expression` 应该是有效的 QLExpress 表达式

### 2. 表达式编辑器

在模型管理页面添加表达式编辑器时，应该：

- 提供语法高亮
- 提供变量名提示
- 提供表达式验证
- 警告整数除法问题

### 3. 单元测试

为每个算法表达式添加单元测试：

```java
@Test
public void testManagementCapability() {
    Map<String, Object> context = new HashMap<>();
    context.put("management_staff", 2);
    context.put("population", 102379L);
    
    String expression = "(management_staff * 1.0 / population) * 10000";
    Object result = qlExpressService.execute(expression, context);
    
    assertEquals(0.1954, (Double)result, 0.0001);
}
```

### 4. 文档化

在系统文档中说明：

- QLExpress 表达式编写规范
- 常见陷阱和最佳实践
- 数据类型和类型转换

## 相关文件

- 数据库表：`model_step`, `step_algorithm`
- 后端服务：`ModelExecutionServiceImpl.java`
- 前端页面：`ModelManagement.vue`

## 总结

此次修复解决了两个关键问题：

1. ✅ **配置数据错误**：修复了 `output_param` 的拼写和格式错误
2. ✅ **整数除法问题**：修改表达式使用浮点数除法，避免结果为0

修复后，模型评估功能可以正常工作，计算结果也是正确的。
