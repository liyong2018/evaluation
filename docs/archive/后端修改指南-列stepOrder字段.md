# 后端修改指南：在返回的列数据中增加stepOrder字段

## 问题分析

从控制台日志可以看到：

**后端配置的 outputParam（英文）**：
```
步骤 1: teamManagement, riskAssessment, financialInput...
步骤 2: teamManagementNorm, riskAssessmentNorm...
步骤 3: teamManagementWeighted, riskAssessmentWeighted...
步骤 4: disasterMgmtPositive, disasterMgmtNegative...
步骤 5: disasterMgmtScore, disasterPrepScore...
```

**实际返回的列名（中文）**：
```
队伍管理能力归一化, 风险评估能力归一化, 财政投入能力归一化...
队伍管理能力定权, 风险评估能力定权...
灾害管理能力优解, 灾害管理能力差解...
灾害管理能力值, 灾害管理能力分级...
```

**结果**：完全不匹配！所以所有步骤都显示 `✗ 没有匹配到任何列`

## 解决方案

### 推荐方案：在返回数据的 columns 数组中增加 stepOrder 字段

前端已经支持从列数据中读取 `stepOrder` 字段进行分组，这是最简单可靠的方法。

#### 修改位置

需要修改返回计算结果的接口，在 `columns` 数组中为每个列对象增加 `stepOrder` 字段。

**接口**：
- `/api/evaluation/algorithm/{algorithmId}/step/{stepOrder}/execute`
- 或任何返回计算结果的接口

#### 修改前的数据结构

```json
{
  "success": true,
  "data": {
    "tableData": [...],
    "columns": [
      {
        "prop": "regionCode",
        "label": "地区代码",
        "width": 150
      },
      {
        "prop": "队伍管理能力归一化",
        "label": "队伍管理能力归一化",
        "width": 120
      },
      {
        "prop": "风险评估能力归一化",
        "label": "风险评估能力归一化",
        "width": 120
      }
    ]
  }
}
```

#### 修改后的数据结构

```json
{
  "success": true,
  "data": {
    "tableData": [...],
    "columns": [
      {
        "prop": "regionCode",
        "label": "地区代码",
        "width": 150
      },
      {
        "prop": "队伍管理能力归一化",
        "label": "队伍管理能力归一化",
        "width": 120,
        "stepOrder": 2
      },
      {
        "prop": "风险评估能力归一化",
        "label": "风险评估能力归一化",
        "width": 120,
        "stepOrder": 2
      },
      {
        "prop": "队伍管理能力定权",
        "label": "队伍管理能力定权",
        "width": 120,
        "stepOrder": 3
      },
      {
        "prop": "灾害管理能力优解",
        "label": "灾害管理能力优解",
        "width": 120,
        "stepOrder": 4
      },
      {
        "prop": "灾害管理能力值",
        "label": "灾害管理能力值",
        "width": 120,
        "stepOrder": 5
      }
    ]
  }
}
```

#### 实现建议（Java）

假设您有一个算法执行服务：

```java
// 在生成columns的地方添加stepOrder字段
List<Map<String, Object>> columns = new ArrayList<>();

// 示例：步骤2的列
Map<String, Object> column1 = new HashMap<>();
column1.put("prop", "队伍管理能力归一化");
column1.put("label", "队伍管理能力归一化");
column1.put("width", 120);
column1.put("stepOrder", 2);  // ← 添加这个字段
columns.add(column1);

Map<String, Object> column2 = new HashMap<>();
column2.put("prop", "风险评估能力归一化");
column2.put("label", "风险评估能力归一化");
column2.put("width", 120);
column2.put("stepOrder", 2);  // ← 添加这个字段
columns.add(column2);

// 步骤3的列
Map<String, Object> column3 = new HashMap<>();
column3.put("prop", "队伍管理能力定权");
column3.put("label", "队伍管理能力定权");
column3.put("width", 120);
column3.put("stepOrder", 3);  // ← 添加这个字段
columns.add(column3);

// ...以此类推
```

#### 如何确定每列的 stepOrder

根据您的算法模型配置：

**步骤1：评估指标赋值**
- 原始输入数据列（如果有的话）

**步骤2：属性向量归一化**
- 所有以"归一化"结尾的列
- 例如：队伍管理能力归一化, 风险评估能力归一化, ...

**步骤3：二级指标定权**
- 所有以"定权"结尾的列
- 例如：队伍管理能力定权, 风险评估能力定权, ...
- 以及"综合定权"的列

**步骤4：优劣解计算**
- 所有包含"优解"、"差解"的列
- 例如：灾害管理能力优解, 灾害管理能力差解, ...

**步骤5：能力值计算与分级**
- 所有以"值"或"分级"结尾的列
- 例如：灾害管理能力值, 灾害管理能力分级, ...

#### 完整示例代码（伪代码）

```java
public Map<String, Object> generateResultColumns(List<StepResult> stepResults) {
    List<Map<String, Object>> columns = new ArrayList<>();
    
    // 基础列
    columns.add(createColumn("regionCode", "地区代码", 150, null));
    columns.add(createColumn("regionName", "地区名称", 120, null));
    
    // 遍历所有步骤结果
    for (StepResult result : stepResults) {
        int stepOrder = result.getStepOrder();
        List<String> outputColumns = result.getOutputColumns();
        
        for (String columnName : outputColumns) {
            columns.add(createColumn(columnName, columnName, 120, stepOrder));
        }
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("columns", columns);
    return response;
}

private Map<String, Object> createColumn(String prop, String label, int width, Integer stepOrder) {
    Map<String, Object> column = new HashMap<>();
    column.put("prop", prop);
    column.put("label", label);
    column.put("width", width);
    if (stepOrder != null) {
        column.put("stepOrder", stepOrder);
    }
    return column;
}
```

## 前端支持情况

前端已经实现了三层分组策略（按优先级）：

### 1. 最高优先级：列数据中的 stepOrder 字段（推荐）
```javascript
// 前端会检查每个列对象是否有 stepOrder 字段
if (column.stepOrder !== undefined) {
  // 使用 stepOrder 进行分组
}
```

### 2. 次优先级：模型配置的 outputParam 映射
```javascript
// 如果列数据没有 stepOrder，尝试使用模型配置匹配
// 需要 outputParam 与列名完全一致（当前不匹配）
```

### 3. 兜底策略：关键字识别
```javascript
// 如果以上都不行，使用关键字识别
// 但这种方式不够准确
```

## 验证方法

修改后端后，前端控制台应该显示：

```
=== 开始计算列分组 ===
Computing column groups with: {
  allColumnsCount: 50,
  allColumnsList: [
    {prop: 'regionCode', label: '地区代码', stepOrder: undefined},
    {prop: '队伍管理能力归一化', label: '队伍管理能力归一化', stepOrder: 2},
    {prop: '队伍管理能力定权', label: '队伍管理能力定权', stepOrder: 3},
    ...
  ]
}

基础列: regionCode
基础列: regionName

检测到列数据中包含stepOrder字段，使用该字段进行分组
  ✓ 列自带stepOrder: 队伍管理能力归一化 -> 步骤2
  ✓ 列自带stepOrder: 风险评估能力归一化 -> 步骤2
  ...
✓ 添加分组(从列数据): 步骤2 属性向量归一化 共 8 列
✓ 添加分组(从列数据): 步骤3 二级指标定权 共 16 列
✓ 添加分组(从列数据): 步骤4 优劣解计算 共 8 列
✓ 添加分组(从列数据): 步骤5 能力值计算与分级 共 8 列

=== 列分组完成 ===
Final column groups summary: [
  {key: 'base', name: '基础信息', columnCount: 2},
  {key: 'step_2', name: '步骤2 属性向量归一化', columnCount: 8},
  {key: 'step_3', name: '步骤3 二级指标定权', columnCount: 16},
  {key: 'step_4', name: '步骤4 优劣解计算', columnCount: 8},
  {key: 'step_5', name: '步骤5 能力值计算与分级', columnCount: 8}
]
```

## 替代方案（不推荐）

如果实在不想修改后端，可以：

### 方案A：修正 outputParam 配置为中文

在模型管理 > 配置 > 算法配置中，将所有的 `outputParam` 从英文改为中文：

```
步骤2的算法：
- outputParam: "teamManagementNorm" → "队伍管理能力归一化"
- outputParam: "riskAssessmentNorm" → "风险评估能力归一化"
...
```

**缺点**：
- 工作量大（48个算法配置都要改）
- 容易出错
- 不利于国际化

### 方案B：修改后端返回的列名为英文

将所有中文列名改为英文，与 outputParam 匹配。

**缺点**：
- 用户界面会显示英文，体验不好
- 需要额外的国际化处理

## 总结

**最佳实践**：在后端返回的 `columns` 数组中直接添加 `stepOrder` 字段

**优点**：
- ✅ 简单明了，一看就懂
- ✅ 不依赖配置匹配，更可靠
- ✅ 支持中文列名
- ✅ 易于维护和扩展
- ✅ 前端已完全支持

**实施步骤**：
1. 找到生成结果 columns 的代码位置
2. 为每个列对象添加 `stepOrder` 字段
3. 根据列名或算法逻辑确定 stepOrder 值
4. 测试验证

---

**前端支持版本**: 已实现  
**后端需要修改**: 在返回的 columns 数组中添加 stepOrder 字段  
**优先级**: 高（否则列分组功能无法正常工作）
