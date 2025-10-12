# 模型执行集成功能完成报告

## 概述

本文档总结了将模型管理功能与评估计算页面集成的完整实现过程。该功能允许用户通过配置好的评估模型（包含多个步骤和QLExpress算法）来执行评估计算，并以二维表格形式展示结果。

## 实施日期

2025年10月12日

## 实现内容

### 1. 后端服务层

#### 1.1 模型执行服务接口 (`ModelExecutionService`)

**文件位置**: `src/main/java/com/evaluate/service/ModelExecutionService.java`

定义了三个核心方法：

- `executeModel()` - 执行完整的评估模型
- `executeStep()` - 执行单个评估步骤
- `generateResultTable()` - 生成结果二维表

#### 1.2 模型执行服务实现 (`ModelExecutionServiceImpl`)

**文件位置**: `src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`

**核心功能**：

1. **模型执行流程**
   - 验证模型存在性和状态
   - 按顺序获取所有启用的步骤
   - 初始化全局上下文（包含模型ID、地区代码、权重配置ID）
   - 加载基础数据（权重配置、地区信息）到上下文
   - 按步骤顺序执行每个步骤
   - 将每个步骤的结果合并到全局上下文，供后续步骤使用
   - 构建最终执行结果

2. **步骤执行流程**
   - 获取步骤信息和关联的算法列表
   - 为每个地区执行该步骤的所有算法
   - 加载该地区的调查数据到上下文
   - 按顺序执行每个算法的QLExpress表达式
   - 将算法输出保存到上下文（供同步骤后续算法使用）
   - 返回该步骤所有地区的执行结果

3. **结果表格生成**
   - 从执行结果中收集所有地区代码
   - 收集所有输出变量名称
   - 为每个地区生成一行数据
   - 包含地区代码、地区名称和所有步骤的输出变量（带步骤前缀）

4. **数据加载**
   - 加载权重配置并转换为Map便于查找
   - 将每个权重作为独立变量存储（便于表达式直接引用）
   - 加载所有地区信息
   - 将调查数据的所有字段添加到上下文

**依赖组件**：
- EvaluationModelMapper - 评估模型数据访问
- ModelStepMapper - 模型步骤数据访问
- StepAlgorithmMapper - 步骤算法数据访问
- SurveyDataMapper - 调查数据访问
- RegionMapper - 地区数据访问
- IndicatorWeightMapper - 指标权重数据访问
- QLExpressService - QLExpress表达式执行服务

### 2. 后端控制器层

#### 2.1 评估控制器更新 (`EvaluationController`)

**文件位置**: `src/main/java/com/evaluate/controller/EvaluationController.java`

**新增API端点**：

1. **执行评估模型**
   - 路径: `POST /api/evaluation/execute-model`
   - 参数:
     - `modelId` (Long) - 模型ID
     - `regionCodes` (List<String>) - 地区代码列表（请求体）
     - `weightConfigId` (Long) - 权重配置ID
   - 返回: 执行结果（包含所有步骤的输出）

2. **生成结果二维表**
   - 路径: `POST /api/evaluation/generate-table`
   - 参数:
     - `executionResults` (Map<String, Object>) - 执行结果（请求体）
   - 返回: 二维表数据列表

### 3. 前端API层

#### 3.1 API接口更新 (`frontend/src/api/index.ts`)

**新增API方法**：

1. **evaluationApi**
   - `executeModel()` - 执行评估模型
   - `generateResultTable()` - 生成结果二维表

2. **modelManagementApi**（新增）
   - `getAllModels()` - 获取所有评估模型
   - `getModelById()` - 根据ID获取评估模型
   - `createModel()` - 创建评估模型
   - `updateModel()` - 更新评估模型
   - `deleteModel()` - 删除评估模型
   - `getModelSteps()` - 获取模型步骤
   - `createModelStep()` - 创建模型步骤
   - `updateModelStep()` - 更新模型步骤
   - `deleteModelStep()` - 删除模型步骤
   - `getStepAlgorithms()` - 获取步骤算法
   - `createStepAlgorithm()` - 创建步骤算法
   - `updateStepAlgorithm()` - 更新步骤算法
   - `deleteStepAlgorithm()` - 删除步骤算法
   - `validateQLExpression()` - 验证QLExpress表达式
   - `testQLExpression()` - 测试QLExpress表达式

### 4. 前端页面层

#### 4.1 评估计算页面更新 (`frontend/src/views/Evaluation.vue`)

**新增功能**：

1. **模型选择**
   - 在评估配置表单中添加"评估模型"下拉选择框
   - 加载所有启用状态的评估模型
   - 支持模型和算法两种评估方式

2. **状态管理**
   - `evaluationModels` - 评估模型列表
   - `evaluationForm.modelId` - 选中的模型ID

3. **执行流程**
   - `startEvaluation()` - 开始评估（支持模型和算法两种方式）
   - `executeModelEvaluation()` - 执行模型评估
     - 提取地区代码
     - 调用模型执行API
     - 显示进度条
     - 生成结果表格
     - 显示结果弹窗
   - `executeAlgorithmEvaluation()` - 执行算法评估（保留原有逻辑）

4. **辅助功能**
   - `getEvaluationModels()` - 获取评估模型列表
   - `handleModelChange()` - 处理模型变化事件
   - `extractRegionCode()` - 从地区ID提取地区代码
   - `displayModelResults()` - 显示模型执行结果

5. **表单更新**
   - 重置表单时包含modelId
   - 组件挂载时加载评估模型列表
   - 验证规则调整（移除algorithmId必填要求）

## 数据流程

### 完整执行流程

```
用户界面
    ↓ 选择模型、地区、权重配置
评估计算页面 (Evaluation.vue)
    ↓ executeModelEvaluation()
评估API (evaluationApi.executeModel)
    ↓ POST /api/evaluation/execute-model
评估控制器 (EvaluationController.executeModel)
    ↓
模型执行服务 (ModelExecutionService.executeModel)
    ↓
┌─────────────────────────────────────────┐
│ 1. 验证模型                              │
│ 2. 获取模型步骤（按顺序）                │
│ 3. 初始化全局上下文                      │
│ 4. 加载基础数据（权重、地区）            │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ 对每个步骤：                             │
│   ├─ 获取步骤算法（按顺序）              │
│   └─ 对每个地区：                        │
│       ├─ 加载调查数据                    │
│       └─ 对每个算法：                    │
│           ├─ 执行QLExpress表达式         │
│           └─ 保存输出到上下文            │
└─────────────────────────────────────────┘
    ↓
返回执行结果
    ↓ evaluationApi.generateResultTable
生成二维表
    ↓
显示结果弹窗 (ResultDialog)
```

## 关键特性

### 1. 上下文传递机制

- **全局上下文**: 存储所有步骤共享的数据
- **步骤上下文**: 每个步骤的结果可被后续步骤使用
- **地区上下文**: 为每个地区创建独立的执行上下文
- **算法上下文**: 同一步骤的算法输出可被后续算法使用

### 2. 数据可用性

表达式中可访问的变量：

**基础变量**:
- `modelId` - 模型ID
- `modelName` - 模型名称
- `currentRegionCode` - 当前地区代码
- `weightConfigId` - 权重配置ID

**权重数据**:
- `weights` - 权重Map（indicatorCode → weight）
- `weight_[indicatorCode]` - 各指标权重独立变量

**地区数据**:
- `regions` - 地区Map（regionCode → Region对象）

**调查数据字段**:
- `regionCode`, `province`, `city`, `county`, `township`
- `population`, `managementStaff`, `riskAssessment`
- `fundingAmount`, `materialValue`, `hospitalBeds`
- `firefighters`, `volunteers`, `militiaReserve`
- `trainingParticipants`, `shelterCapacity`

**步骤输出**:
- `step_[stepCode]` - 前序步骤的完整结果

### 3. 灵活性

- 支持任意数量的步骤和算法
- 步骤之间可以有依赖关系
- 算法输出自动成为可用变量
- 支持复杂的数学计算和逻辑判断

## 使用方法

### 1. 配置模型

1. 在模型管理页面创建评估模型
2. 为模型添加评估步骤（按顺序）
3. 为每个步骤配置算法和QLExpress表达式
4. 启用模型

### 2. 执行评估

1. 打开评估计算页面
2. 选择评估模型
3. 选择权重配置
4. 选择评估地区
5. 点击"开始评估"
6. 查看结果表格

### 3. 结果解读

- 每行代表一个地区
- 每列代表一个步骤的输出变量
- 列名格式: `[stepCode]_[outputParam]`

## 示例场景

### 场景：TOPSIS减灾能力评估

**步骤配置**：

1. **步骤1：数据归一化**
   - 算法：Max-Min归一化
   - 输出：归一化后的各指标值

2. **步骤2：加权规范化**
   - 算法：乘以权重
   - 输入：步骤1的输出、权重配置
   - 输出：加权后的各指标值

3. **步骤3：理想解计算**
   - 算法：计算正理想解和负理想解
   - 输入：步骤2的输出
   - 输出：正理想解、负理想解

4. **步骤4：距离计算**
   - 算法：计算到理想解的欧氏距离
   - 输入：步骤2和步骤3的输出
   - 输出：到正理想解距离、到负理想解距离

5. **步骤5：相对贴近度计算**
   - 算法：计算综合评分
   - 输入：步骤4的输出
   - 输出：最终评分、等级

**执行结果表格**：

| 地区代码 | 地区名称 | STEP1_normalized_X1 | ... | STEP5_score | STEP5_grade |
|---------|---------|-------------------|-----|-------------|-------------|
| 510107  | 青竹街道 | 0.8534           | ... | 0.7234      | 较强         |
| 510108  | 汉阳镇   | 0.7123           | ... | 0.6789      | 中等         |

## 技术亮点

1. **动态执行**: 根据模型配置动态执行评估流程
2. **上下文传递**: 优雅的上下文传递机制支持步骤间数据共享
3. **类型安全**: 使用Java泛型和TypeScript类型系统
4. **错误处理**: 完善的异常捕获和错误信息传递
5. **日志记录**: 详细的日志记录便于调试
6. **事务管理**: 使用@Transactional确保数据一致性
7. **响应式UI**: 进度条和加载状态提供良好的用户体验

## 测试验证

### 后端测试

1. ✅ 编译通过：`mvn clean compile -DskipTests`
2. ✅ 服务启动：后端服务正常启动在端口8081
3. ✅ API可用性：模型管理API正常返回数据

### 前端测试

待完成：
- 浏览器中访问评估计算页面
- 选择模型并执行评估
- 验证结果表格显示

## 后续改进建议

1. **性能优化**
   - 对大量地区的批量处理进行优化
   - 添加缓存机制减少数据库查询
   - 异步执行耗时的步骤

2. **功能增强**
   - 支持步骤的条件执行（if-else逻辑）
   - 支持步骤的循环执行
   - 支持中间结果的持久化和恢复
   - 添加执行历史记录

3. **用户体验**
   - 添加执行进度的实时更新
   - 支持中断和恢复执行
   - 提供更丰富的结果可视化（图表、地图）

4. **安全性**
   - 添加QLExpress表达式的沙箱执行
   - 限制表达式的复杂度和执行时间
   - 添加模型权限管理

## 相关文件

### 后端文件
- `src/main/java/com/evaluate/service/ModelExecutionService.java`
- `src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`
- `src/main/java/com/evaluate/controller/EvaluationController.java`

### 前端文件
- `frontend/src/views/Evaluation.vue`
- `frontend/src/api/index.ts`

### 实体类
- `src/main/java/com/evaluate/entity/EvaluationModel.java`
- `src/main/java/com/evaluate/entity/ModelStep.java`
- `src/main/java/com/evaluate/entity/StepAlgorithm.java`

### Mapper
- `src/main/java/com/evaluate/mapper/EvaluationModelMapper.java`
- `src/main/java/com/evaluate/mapper/ModelStepMapper.java`
- `src/main/java/com/evaluate/mapper/StepAlgorithmMapper.java`

## 结论

模型执行集成功能已成功实现，提供了一个灵活、可扩展的评估模型执行框架。用户可以通过配置模型、步骤和算法来定义复杂的评估流程，系统会自动按配置执行并生成结果。该功能为减灾能力评估系统提供了强大的计算引擎支持。