# 算法分步执行系统 - 使用指南

## 🎯 功能概述

系统现已支持按步骤执行算法并显示每步的二维结果表，实现了：

1. **分步执行**：按照数据库中配置的步骤数量进行算法拆解
2. **二维表显示**：每个步骤执行后都会生成对应的二维结果表  
3. **动态表达式**：使用 QLExpress 动态表达式避免硬编码
4. **前端交互**：提供按钮界面让用户选择执行特定步骤

## 🚀 主要功能

### 1. 获取算法步骤信息

**端点**: `GET /api/algorithm-step-execution/{algorithmId}/steps`

**功能**: 获取算法的所有步骤信息，用于前端显示步骤按钮

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "algorithmId": 1,
    "algorithmName": "标准减灾能力评估算法",
    "algorithmDescription": "基于多指标的减灾能力综合评估",
    "totalSteps": 5,
    "steps": [
      {
        "stepId": 1,
        "stepName": "评估指标赋值",
        "stepOrder": 1,
        "stepCode": "INDICATOR_ASSIGNMENT",
        "description": "计算基础评估指标值"
      },
      {
        "stepId": 2, 
        "stepName": "属性向量归一化",
        "stepOrder": 2,
        "stepCode": "NORMALIZATION",
        "description": "对指标值进行归一化处理"
      }
    ]
  }
}
```

### 2. 执行单个步骤

**端点**: `POST /api/algorithm-step-execution/{algorithmId}/step/{stepOrder}/execute`

**功能**: 执行指定顺序的算法步骤，并返回该步骤的二维表格结果

**请求体**:
```json
{
  "regionCodes": [
    "township_四川省_眉山市_青神县_青竹街道",
    "township_四川省_眉山市_青神县_瑞峰镇"
  ],
  "weightConfigId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "stepId": 1,
    "stepName": "评估指标赋值",
    "stepOrder": 1,
    "stepCode": "INDICATOR_ASSIGNMENT", 
    "description": "计算基础评估指标值",
    "executionResult": {
      "stepId": 1,
      "stepName": "评估指标赋值",
      "stepCode": "INDICATOR_ASSIGNMENT",
      "regionResults": {
        "township_四川省_眉山市_青神县_青竹街道": {
          "teamManagement": 2.5,
          "riskAssessment": 1.0,
          "financialInput": 150000.0
        }
      }
    },
    "tableData": [
      {
        "regionCode": "township_四川省_眉山市_青神县_青竹街道",
        "regionName": "青竹街道",
        "队伍管理能力": 2.5,
        "风险评估能力": 1.0,
        "资金投入能力": 150000.0
      }
    ],
    "success": true,
    "executionTime": "2025-01-01T10:00:00.000+00:00"
  }
}
```

### 3. 批量执行步骤

**端点**: `POST /api/algorithm-step-execution/{algorithmId}/steps/execute-up-to/{upToStepOrder}`

**功能**: 批量执行算法步骤直到指定步骤，返回所有已执行步骤的结果

**请求体**: 同单个步骤执行

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "algorithmId": 1,
    "executedUpToStep": 3,
    "stepResults": {
      "step_INDICATOR_ASSIGNMENT": { /* 步骤1结果 */ },
      "step_NORMALIZATION": { /* 步骤2结果 */ },
      "step_WEIGHTING": { /* 步骤3结果 */ }
    },
    "tableData": {
      "step_INDICATOR_ASSIGNMENT": [
        { /* 步骤1的二维表数据 */ }
      ],
      "step_NORMALIZATION": [
        { /* 步骤2的二维表数据 */ }
      ],
      "step_WEIGHTING": [
        { /* 步骤3的二维表数据 */ }
      ]
    },
    "success": true,
    "executionTime": "2025-01-01T10:00:00.000+00:00"
  }
}
```

## 🔧 技术实现

### 系统架构

```
前端界面
    ↓ API调用
AlgorithmStepExecutionController 
    ↓ 服务调用
ModelExecutionService
    ├─ 获取算法步骤配置
    ├─ 执行QLExpress表达式  
    ├─ 处理特殊算法标记(@NORMALIZE, @TOPSIS_POSITIVE等)
    └─ 生成2D表格结果
```

### 核心服务方法

1. **getAlgorithmStepsInfo()** - 获取算法步骤信息
2. **executeAlgorithmStep()** - 执行单个步骤
3. **executeAlgorithmStepsUpTo()** - 批量执行到指定步骤
4. **generateStepResultTable()** - 生成步骤的2D表格数据

### 数据依赖处理

系统自动处理步骤之间的数据依赖关系：

- 执行步骤N时，自动先执行步骤1到N-1
- 前面步骤的输出结果作为后续步骤的输入变量
- 支持跨区域聚合计算（如归一化、TOPSIS算法等）

## 📊 前端集成示例

### 1. 获取并显示步骤按钮

```javascript
// 获取算法步骤信息
const getAlgorithmSteps = async (algorithmId) => {
  const response = await fetch(`/api/algorithm-step-execution/${algorithmId}/steps`);
  const result = await response.json();
  
  if (result.code === 200) {
    const { steps } = result.data;
    
    // 渲染步骤按钮
    const buttonsContainer = document.getElementById('step-buttons');
    steps.forEach(step => {
      const button = document.createElement('button');
      button.textContent = `步骤${step.stepOrder}: ${step.stepName}`;
      button.onclick = () => executeStep(algorithmId, step.stepOrder);
      buttonsContainer.appendChild(button);
    });
  }
};
```

### 2. 执行步骤并显示结果表

```javascript
// 执行步骤
const executeStep = async (algorithmId, stepOrder) => {
  const requestBody = {
    regionCodes: ['township_四川省_眉山市_青神县_青竹街道', 'township_四川省_眉山市_青神县_瑞峰镇'],
    weightConfigId: 1
  };
  
  const response = await fetch(`/api/algorithm-step-execution/${algorithmId}/step/${stepOrder}/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestBody)
  });
  
  const result = await response.json();
  
  if (result.code === 200) {
    const { tableData, stepName } = result.data;
    
    // 显示结果表
    displayResultTable(tableData, `步骤${stepOrder}: ${stepName}的执行结果`);
  }
};

// 显示结果表
const displayResultTable = (tableData, title) => {
  const container = document.getElementById('result-container');
  
  // 创建表格标题
  const titleElement = document.createElement('h3');
  titleElement.textContent = title;
  container.appendChild(titleElement);
  
  // 创建表格
  const table = document.createElement('table');
  table.className = 'result-table';
  
  if (tableData.length > 0) {
    // 表头
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    Object.keys(tableData[0]).forEach(key => {
      const th = document.createElement('th');
      th.textContent = key;
      headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);
    
    // 表体
    const tbody = document.createElement('tbody');
    tableData.forEach(row => {
      const tr = document.createElement('tr');
      Object.values(row).forEach(value => {
        const td = document.createElement('td');
        td.textContent = value;
        tr.appendChild(td);
      });
      tbody.appendChild(tr);
    });
    table.appendChild(tbody);
  }
  
  container.appendChild(table);
};
```

## 🎨 前端界面建议

### 布局结构

```html
<div id="algorithm-execution-page">
  <!-- 算法选择区域 -->
  <div id="algorithm-selection">
    <h2>选择算法</h2>
    <select id="algorithm-select">
      <!-- 算法选项 -->
    </select>
  </div>
  
  <!-- 参数配置区域 -->
  <div id="parameter-config">
    <h3>执行参数</h3>
    <div>
      <label>地区选择：</label>
      <select multiple id="region-select">
        <!-- 地区选项 -->
      </select>
    </div>
    <div>
      <label>权重配置：</label>
      <select id="weight-config-select">
        <!-- 权重配置选项 -->
      </select>
    </div>
  </div>
  
  <!-- 步骤按钮区域 -->
  <div id="step-buttons-container">
    <h3>算法步骤</h3>
    <div id="step-buttons">
      <!-- 动态生成的步骤按钮 -->
    </div>
  </div>
  
  <!-- 结果显示区域 -->
  <div id="result-container">
    <h3>执行结果</h3>
    <!-- 动态显示的结果表格 -->
  </div>
</div>
```

### CSS样式建议

```css
.step-button {
  margin: 5px;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.step-button:hover {
  background-color: #0056b3;
}

.step-button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.result-table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

.result-table th, 
.result-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.result-table th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.result-table tr:nth-child(even) {
  background-color: #f9f9f9;
}
```

## 🔍 测试示例

### 使用PowerShell测试API

```powershell
# 1. 获取算法步骤信息
$response = Invoke-RestMethod -Uri "http://localhost:8081/api/algorithm-step-execution/1/steps" -Method GET
Write-Host "算法步骤信息: $($response | ConvertTo-Json -Depth 10)"

# 2. 执行第1步
$body = @{
    regionCodes = @(
        "township_四川省_眉山市_青神县_青竹街道",
        "township_四川省_眉山市_青神县_瑞峰镇"
    )
    weightConfigId = 1
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8081/api/algorithm-step-execution/1/step/1/execute" `
    -Method POST -Body $body -ContentType "application/json"
Write-Host "步骤1执行结果: $($response | ConvertTo-Json -Depth 10)"

# 3. 批量执行到第3步
$response = Invoke-RestMethod -Uri "http://localhost:8081/api/algorithm-step-execution/1/steps/execute-up-to/3" `
    -Method POST -Body $body -ContentType "application/json"
Write-Host "批量执行结果: $($response | ConvertTo-Json -Depth 10)"
```

## 📝 注意事项

1. **步骤顺序**: 步骤顺序从1开始，必须按顺序执行
2. **数据依赖**: 系统会自动处理步骤之间的数据依赖关系
3. **权重配置**: weightConfigId是可选参数，但建议提供以获得完整计算结果
4. **地区代码**: regionCodes必须是有效的地区代码，系统会验证并加载对应的调查数据
5. **表格数据**: tableData中的列名优先使用公式的中文名称，提高可读性

## ✅ 优势特点

1. **动态配置**: 所有算法公式存储在数据库中，支持在线修改
2. **分步执行**: 支持单步和批量执行，满足不同使用场景
3. **结果可视化**: 自动生成二维表格，便于查看和分析
4. **依赖处理**: 自动处理步骤间的数据依赖关系
5. **特殊算法支持**: 支持归一化、TOPSIS等特殊算法标记
6. **错误处理**: 完善的错误处理和日志记录机制

系统现已完全支持按步骤执行算法并显示二维结果表，可以满足用户按需查看各个步骤计算结果的需求！