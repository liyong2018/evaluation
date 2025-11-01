# 步骤切换显示API文档

## 📋 概述

评估模型执行API现在支持按步骤独立显示数据，通过单选按钮切换不同步骤的表格展示。

**核心变化：**
- 新增 `stepTables` 数组，包含每个步骤的独立表格数据
- 保留原有 `tableData` 和 `columns`（所有步骤合并的数据），确保向后兼容
- 前端可以使用单选按钮在步骤之间切换，每个步骤显示独立的表格

---

## 🔌 API响应结构

### 完整响应示例

```json
{
  "modelId": 8,
  "modelName": "社区-乡镇能力评估",
  "executionTime": "2025-10-31T15:30:45.123Z",
  "success": true,
  "executionRecordId": 12345,

  // 保留：所有步骤合并的数据（向后兼容）
  "tableData": [
    {
      "currentRegionCode": "330402001001",
      "currentRegionName": "青竹社区",
      "MGMT_CAPABILITY": 0.75,
      "PREP_CAPABILITY": 0.82,
      "SELF_RESCUE_CAPABILITY": 0.68,
      "TOWNSHIP_MGMT": 7.5,
      "TOWNSHIP_PREP": 8.2
    }
  ],

  // 保留：所有步骤合并的列（向后兼容）
  "columns": [
    {
      "prop": "currentRegionCode",
      "label": "地区代码",
      "stepOrder": 0
    },
    {
      "prop": "MGMT_CAPABILITY",
      "label": "管理能力",
      "stepOrder": 1
    },
    {
      "prop": "TOWNSHIP_MGMT",
      "label": "乡镇管理能力",
      "stepOrder": 2
    }
  ],

  // 新增：每个步骤独立的表格数据
  "stepTables": [
    {
      "stepId": 50,
      "stepCode": "STEP_1",
      "stepName": "社区指标计算",
      "stepOrder": 1,
      "tableData": [
        {
          "currentRegionCode": "330402001001",
          "currentRegionName": "青竹社区",
          "MGMT_CAPABILITY": 0.75,
          "PREP_CAPABILITY": 0.82,
          "SELF_RESCUE_CAPABILITY": 0.68
        }
      ],
      "columns": [
        {
          "prop": "currentRegionCode",
          "label": "地区代码",
          "stepOrder": 1
        },
        {
          "prop": "MGMT_CAPABILITY",
          "label": "管理能力",
          "stepOrder": 1
        },
        {
          "prop": "PREP_CAPABILITY",
          "label": "防灾准备能力",
          "stepOrder": 1
        },
        {
          "prop": "SELF_RESCUE_CAPABILITY",
          "label": "自救能力",
          "stepOrder": 1
        }
      ]
    },
    {
      "stepId": 51,
      "stepCode": "STEP_2",
      "stepName": "乡镇聚合",
      "stepOrder": 2,
      "tableData": [
        {
          "currentRegionCode": "330402001",
          "currentRegionName": "青竹街道",
          "TOWNSHIP_MGMT": 7.5,
          "TOWNSHIP_PREP": 8.2,
          "TOWNSHIP_RESCUE": 6.8
        }
      ],
      "columns": [
        {
          "prop": "currentRegionCode",
          "label": "地区代码",
          "stepOrder": 2
        },
        {
          "prop": "TOWNSHIP_MGMT",
          "label": "乡镇管理能力",
          "stepOrder": 2
        },
        {
          "prop": "TOWNSHIP_PREP",
          "label": "乡镇防灾准备",
          "stepOrder": 2
        },
        {
          "prop": "TOWNSHIP_RESCUE",
          "label": "乡镇自救能力",
          "stepOrder": 2
        }
      ]
    }
  ],

  "stepResults": {
    "STEP_1": { /* ... */ },
    "STEP_2": { /* ... */ }
  }
}
```

---

## 📊 stepTables 数组详解

### 数组元素结构

每个 `stepTables` 数组元素包含以下字段：

| 字段 | 类型 | 说明 | 示例 |
|-----|------|------|------|
| `stepId` | Long | 步骤ID | 50 |
| `stepCode` | String | 步骤代码 | "STEP_1" |
| `stepName` | String | 步骤名称 | "社区指标计算" |
| `stepOrder` | Integer | 步骤顺序 | 1 |
| `tableData` | Array | 该步骤的表格数据 | 见下文 |
| `columns` | Array | 该步骤的列定义 | 见下文 |

### tableData 结构

每个步骤的 `tableData` 是一个对象数组，每个对象代表一行数据：

```json
[
  {
    "currentRegionCode": "330402001001",
    "currentRegionName": "青竹社区",
    "MGMT_CAPABILITY": 0.75634521,
    "PREP_CAPABILITY": 0.82156789,
    // ... 其他该步骤的输出字段
  },
  {
    "currentRegionCode": "330402001002",
    "currentRegionName": "竹园社区",
    "MGMT_CAPABILITY": 0.68234567,
    "PREP_CAPABILITY": 0.79456123,
    // ...
  }
]
```

**特点：**
- 仅包含该步骤产生的字段
- 始终包含 `currentRegionCode` 和 `currentRegionName`
- 数值保留8位小数精度

### columns 结构

每个步骤的 `columns` 是一个列定义数组：

```json
[
  {
    "prop": "currentRegionCode",
    "label": "地区代码",
    "stepOrder": 1
  },
  {
    "prop": "currentRegionName",
    "label": "地区名称",
    "stepOrder": 1
  },
  {
    "prop": "MGMT_CAPABILITY",
    "label": "管理能力",
    "stepOrder": 1
  }
]
```

**特点：**
- `prop`：对应 tableData 中的字段名
- `label`：前端表格显示的列标题
- `stepOrder`：该列属于哪个步骤（仅包含当前步骤的列）

---

## 💻 前端实现指南

### Vue 3 实现示例

```vue
<template>
  <div class="evaluation-display">
    <!-- 步骤选择单选按钮 -->
    <div class="step-selector">
      <el-radio-group v-model="selectedStepIndex">
        <el-radio-button
          v-for="(stepTable, index) in stepTables"
          :key="stepTable.stepId"
          :label="index"
        >
          {{ stepTable.stepName }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 当前步骤的表格 -->
    <el-table
      v-if="currentStepTable"
      :data="currentStepTable.tableData"
      stripe
      border
    >
      <el-table-column
        v-for="column in currentStepTable.columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="getColumnWidth(column.prop)"
      />
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

// 模拟API响应数据
const apiResponse = ref({
  stepTables: [],
  tableData: [],
  columns: []
});

// 当前选中的步骤索引
const selectedStepIndex = ref(0);

// 步骤表格数据
const stepTables = computed(() => apiResponse.value.stepTables || []);

// 当前步骤的表格数据
const currentStepTable = computed(() => {
  if (stepTables.value.length === 0) return null;
  return stepTables.value[selectedStepIndex.value];
});

// 获取列宽（可根据字段类型自定义）
const getColumnWidth = (prop) => {
  if (prop === 'currentRegionCode') return 150;
  if (prop === 'currentRegionName') return 200;
  return undefined; // 自动宽度
};

// 调用API获取数据
const fetchEvaluationData = async () => {
  const response = await fetch('/api/model/execute', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      modelId: 8,
      regionCodes: ['330402001001', '330402001002'],
      weightConfigId: 1
    })
  });

  const data = await response.json();
  apiResponse.value = data;

  // 默认选中第一个步骤
  if (data.stepTables && data.stepTables.length > 0) {
    selectedStepIndex.value = 0;
  }
};
</script>

<style scoped>
.evaluation-display {
  padding: 20px;
}

.step-selector {
  margin-bottom: 20px;
}
</style>
```

### React 实现示例

```jsx
import React, { useState, useEffect } from 'react';
import { Radio, Table } from 'antd';

const EvaluationDisplay = () => {
  const [apiResponse, setApiResponse] = useState({
    stepTables: [],
    tableData: [],
    columns: []
  });
  const [selectedStepIndex, setSelectedStepIndex] = useState(0);

  // 获取当前步骤的表格数据
  const currentStepTable = apiResponse.stepTables?.[selectedStepIndex];

  // 转换列定义为Ant Design Table格式
  const getAntdColumns = () => {
    if (!currentStepTable) return [];

    return currentStepTable.columns.map(col => ({
      title: col.label,
      dataIndex: col.prop,
      key: col.prop,
      width: col.prop === 'currentRegionCode' ? 150 :
             col.prop === 'currentRegionName' ? 200 : undefined
    }));
  };

  // 调用API
  useEffect(() => {
    fetchEvaluationData();
  }, []);

  const fetchEvaluationData = async () => {
    const response = await fetch('/api/model/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        modelId: 8,
        regionCodes: ['330402001001', '330402001002'],
        weightConfigId: 1
      })
    });

    const data = await response.json();
    setApiResponse(data);
  };

  return (
    <div className="evaluation-display">
      {/* 步骤选择单选按钮 */}
      <Radio.Group
        value={selectedStepIndex}
        onChange={(e) => setSelectedStepIndex(e.target.value)}
        style={{ marginBottom: 20 }}
      >
        {apiResponse.stepTables?.map((stepTable, index) => (
          <Radio.Button key={stepTable.stepId} value={index}>
            {stepTable.stepName}
          </Radio.Button>
        ))}
      </Radio.Group>

      {/* 当前步骤的表格 */}
      {currentStepTable && (
        <Table
          columns={getAntdColumns()}
          dataSource={currentStepTable.tableData}
          rowKey="currentRegionCode"
          bordered
          pagination={false}
        />
      )}
    </div>
  );
};

export default EvaluationDisplay;
```

---

## 🎯 使用场景

### 场景1：显示社区指标（步骤1）

用户选择"社区指标计算"单选按钮，表格显示：
- 所有社区的行数据
- 步骤1的13个输出列（管理能力、防灾准备能力等）

### 场景2：显示乡镇聚合（步骤2）

用户选择"乡镇聚合"单选按钮，表格显示：
- 所有乡镇的行数据
- 步骤2的聚合输出列（乡镇管理能力、乡镇防灾准备等）

### 场景3：显示最终定权（步骤3）

用户选择"综合定权"单选按钮，表格显示：
- 定权后的最终数据
- 步骤3的输出列（综合能力分值等）

---

## ⚠️ 注意事项

### 1. 向后兼容性

如果前端暂时不实现单选按钮切换，可以继续使用原有的 `tableData` 和 `columns`：

```javascript
// 旧方式（仍然有效）
const tableData = apiResponse.tableData;
const columns = apiResponse.columns;
```

这将显示所有步骤合并的数据，与之前的行为一致。

### 2. 数据一致性

- `stepTables` 中的数据是 `tableData` 的子集
- 每个步骤的 `tableData` 仅包含该步骤的输出字段
- 地区代码字段（`currentRegionCode`、`currentRegionName`）在每个步骤中都存在

### 3. 步骤顺序

- `stepTables` 数组按 `stepOrder` 升序排列
- 第一个元素（索引0）是步骤1，第二个元素（索引1）是步骤2，以此类推
- 可以通过 `stepOrder` 字段进行排序验证

### 4. 空数据处理

如果某个步骤没有产生数据：

```javascript
if (!currentStepTable || !currentStepTable.tableData.length) {
  // 显示空状态
  return <div>该步骤暂无数据</div>;
}
```

---

## 🔍 调试技巧

### 查看API响应

在浏览器控制台打印完整响应：

```javascript
console.log('API Response:', apiResponse);
console.log('Step Tables:', apiResponse.stepTables);
console.log('Step 1 Data:', apiResponse.stepTables?.[0]?.tableData);
console.log('Step 1 Columns:', apiResponse.stepTables?.[0]?.columns);
```

### 验证数据完整性

检查每个步骤的数据是否完整：

```javascript
apiResponse.stepTables.forEach((stepTable, index) => {
  console.log(`步骤${index + 1}: ${stepTable.stepName}`);
  console.log(`  - 数据行数: ${stepTable.tableData.length}`);
  console.log(`  - 列数: ${stepTable.columns.length}`);
  console.log(`  - 列名:`, stepTable.columns.map(c => c.label));
});
```

### 比较合并数据与步骤数据

验证 `stepTables` 数据是否与 `tableData` 一致：

```javascript
// 检查步骤1的第一行数据
const step1FirstRow = apiResponse.stepTables[0].tableData[0];
const mergedFirstRow = apiResponse.tableData[0];

console.log('步骤1第一行:', step1FirstRow);
console.log('合并数据第一行:', mergedFirstRow);
// 步骤1的字段应该存在于合并数据中
```

---

## 📋 常见问题

### Q1: 为什么需要 stepTables，不能直接筛选 tableData 吗？

**A:** 直接筛选 `tableData` 的问题：
1. 列定义(`columns`)包含所有步骤的列，无法只显示当前步骤的列
2. 需要前端编写复杂的筛选逻辑
3. 每次切换步骤都需要重新计算，影响性能

`stepTables` 已经按步骤预处理好数据和列，前端直接使用即可。

### Q2: stepTables 的顺序是固定的吗？

**A:** 是的，`stepTables` 按 `stepOrder` 升序排列：
- 索引0 = 步骤1
- 索引1 = 步骤2
- 索引2 = 步骤3

### Q3: 如何显示步骤名称作为单选按钮标签？

**A:** 使用 `stepName` 字段：

```javascript
stepTables.map(st => (
  <RadioButton key={st.stepId} value={st.stepId}>
    {st.stepName}  {/* "社区指标计算"、"乡镇聚合" 等 */}
  </RadioButton>
))
```

### Q4: 可以同时显示多个步骤吗？

**A:** 可以，使用复选框（Checkbox）代替单选按钮：

```javascript
const [selectedStepIds, setSelectedStepIds] = useState([]);

// 过滤选中的步骤
const selectedSteps = stepTables.filter(st =>
  selectedStepIds.includes(st.stepId)
);

// 显示多个表格
selectedSteps.map(stepTable => (
  <div key={stepTable.stepId}>
    <h3>{stepTable.stepName}</h3>
    <Table data={stepTable.tableData} columns={stepTable.columns} />
  </div>
))
```

---

## 📚 相关文档

- [乡镇聚合表达式使用指南](./TOWNSHIP_AGGREGATION_EXPRESSIONS.md)
- [综合模型使用指南](./comprehensive_model_guide.md)
- [下一步操作指南](./NEXT_STEPS.md)

---

**更新时间：** 2025-11-01
**文档版本：** 1.0
**作者：** Claude Code

**重要提醒：**
- 后端已实现 `stepTables` 结构，前端需要相应实现单选按钮切换逻辑
- 原有 `tableData` 和 `columns` 仍然保留，确保向后兼容
- 建议优先使用 `stepTables` 实现更好的用户体验
