# QLExpress动态规则引擎实施指南

## 已完成工作

### 1. 后端基础设施 ✅

- **添加QLExpress依赖** (`pom.xml`)
  - QLExpress 3.3.2版本

- **数据库设计** (`migrations/002_create_model_management.sql`)
  - `evaluation_model` - 评估模型表
  - `model_step` - 模型步骤表
  - `step_algorithm` - 步骤算法表（存储QLExpress表达式）
  - `model_execution_record` - 模型执行记录表
  - `step_execution_result` - 步骤执行结果表

- **实体类**
  - `EvaluationModel.java` - 评估模型实体
  - `ModelStep.java` - 模型步骤实体
  - `StepAlgorithm.java` - 步骤算法实体

- **QLExpress服务**
  - `QLExpressService.java` - 服务接口
  - `QLExpressServiceImpl.java` - 服务实现
  - 支持SQRT, POW, AVERAGE, STDEV, SUMSQ等数学函数

- **Mapper接口**
  - `EvaluationModelMapper.java`
  - `ModelStepMapper.java`
  - `StepAlgorithmMapper.java`

- **控制器**
  - `ModelManagementController.java` - 模型管理API
  - 提供模型、步骤、算法的CRUD操作

- **初始化数据** (`migrations/003_init_model_formulas.sql`)
  - 8个评估指标赋值公式
  - 归一化公式
  - 定权公式

## 需要完成的工作

### 2. 执行数据库迁移 🔄

```powershell
# 连接MySQL并执行迁移
# 方式1: 如果MySQL在PATH中
mysql -u root -pHtht1234 < migrations/002_create_model_management.sql
mysql -u root -pHtht1234 < migrations/003_init_model_formulas.sql

# 方式2: 使用完整路径
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pHtht1234 < migrations/002_create_model_management.sql
```

### 3. 重启后端服务 🔄

```powershell
# 停止当前后端服务
Stop-Job -Name "SpringBootApp"

# 重新编译并启动
mvn clean install
Start-Job -ScriptBlock { Set-Location 'C:\Users\Administrator\Development\evaluation'; mvn spring-boot:run } -Name "SpringBootApp"
```

### 4. 创建前端模型管理页面 📝

创建 `frontend/src/views/ModelManagement.vue`:

```vue
<template>
  <div class="model-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评估模型管理</span>
          <el-button type="primary" @click="showCreateModelDialog">新建模型</el-button>
        </div>
      </template>

      <!-- 模型列表 -->
      <el-table :data="models" style="width: 100%">
        <el-table-column prop="modelName" label="模型名称" />
        <el-table-column prop="modelCode" label="模型编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="version" label="版本" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="viewModel(scope.row)">查看</el-button>
            <el-button size="small" @click="editModel(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 模型详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="模型详情" width="80%">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="步骤管理" name="steps">
          <el-button type="primary" @click="showAddStepDialog">添加步骤</el-button>
          <el-table :data="currentSteps" style="margin-top: 20px">
            <el-table-column prop="stepName" label="步骤名称" />
            <el-table-column prop="stepOrder" label="执行顺序" />
            <el-table-column prop="stepType" label="步骤类型" />
            <el-table-column label="操作">
              <template #default="scope">
                <el-button size="small" @click="viewStepAlgorithms(scope.row)">查看算法</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="算法配置" name="algorithms">
          <el-button type="primary" @click="showAddAlgorithmDialog">添加算法</el-button>
          <el-table :data="currentAlgorithms" style="margin-top: 20px">
            <el-table-column prop="algorithmName" label="算法名称" />
            <el-table-column prop="algorithmOrder" label="执行顺序" />
            <el-table-column label="QLExpress表达式" width="300">
              <template #default="scope">
                <el-input v-model="scope.row.qlExpression" type="textarea" :rows="2" />
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button size="small" type="primary" @click="updateAlgorithm(scope.row)">保存</el-button>
                <el-button size="small" type="danger" @click="deleteAlgorithm(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const models = ref([])
const currentSteps = ref([])
const currentAlgorithms = ref([])
const showDetailDialog = ref(false)
const activeTab = ref('steps')

// 加载模型列表
const loadModels = async () => {
  try {
    const response = await request.get('/api/model-management/models')
    models.value = response.data
  } catch (error) {
    ElMessage.error('加载模型列表失败')
  }
}

// 查看模型详情
const viewModel = async (model: any) => {
  try {
    const response = await request.get(`/api/model-management/models/${model.id}/detail`)
    currentSteps.value = response.data.steps
    showDetailDialog.value = true
  } catch (error) {
    ElMessage.error('加载模型详情失败')
  }
}

// 更新算法
const updateAlgorithm = async (algorithm: any) => {
  try {
    await request.put(`/api/model-management/algorithms/${algorithm.id}`, algorithm)
    ElMessage.success('算法更新成功')
  } catch (error) {
    ElMessage.error('算法更新失败')
  }
}

onMounted(() => {
  loadModels()
})
</script>
```

### 5. 更新路由配置 📝

在 `frontend/src/router/index.ts` 添加:

```typescript
{
  path: '/model-management',
  name: 'ModelManagement',
  component: () => import('@/views/ModelManagement.vue')
}
```

在 `frontend/src/App.vue` 添加菜单项:

```vue
<el-menu-item index="/model-management">模型管理</el-menu-item>
```

### 6. 更新API接口 📝

在 `frontend/src/api/index.ts` 添加:

```typescript
// 模型管理相关API
export const modelManagementApi = {
  // 获取所有模型
  getModels: () => request.get('/api/model-management/models'),
  
  // 获取模型详情
  getModelDetail: (modelId: number) => request.get(`/api/model-management/models/${modelId}/detail`),
  
  // 创建模型
  createModel: (data: any) => request.post('/api/model-management/models', data),
  
  // 添加步骤
  createStep: (modelId: number, data: any) => request.post(`/api/model-management/models/${modelId}/steps`, data),
  
  // 添加算法
  createAlgorithm: (stepId: number, data: any) => request.post(`/api/model-management/steps/${stepId}/algorithms`, data),
  
  // 更新算法
  updateAlgorithm: (algorithmId: number, data: any) => request.put(`/api/model-management/algorithms/${algorithmId}`, data),
  
  // 验证表达式
  validateExpression: (expression: string, context: any) => request.post('/api/model-management/validate-expression', { expression, context })
}
```

## 使用流程

### 创建新模型

1. **新建模型**: 在模型管理页面点击"新建模型"
   - 输入模型名称、编码、描述
   - 保存后获得模型ID

2. **添加步骤**: 为模型添加执行步骤
   - 步骤1: 评估指标赋值
   - 步骤2: 属性向量归一化
   - 步骤3: 二级指标定权
   - 步骤4: 优劣解算法
   - 步骤5: 能力分级

3. **配置算法**: 为每个步骤添加QLExpress表达式
   ```
   示例表达式:
   - 队伍管理能力: (management_staff / population) * 10000
   - 归一化: value / SQRT(SUMSQ(all_values))
   - 定权: normalized_value * weight
   ```

4. **验证表达式**: 系统自动验证QLExpress语法

### 执行评估

1. **选择模型**: 在评估计算页面选择已配置的模型
2. **选择地区**: 选择要评估的地区（从数据库提取数据）
3. **开始评估**: 点击"开始评估"按钮
4. **查看结果**: 系统按步骤顺序执行所有算法，得出最终结果

## QLExpress表达式示例

### 基础运算
```java
// 加减乘除
(management_staff / population) * 10000

// 条件判断
risk_assessment == "是" ? 1 : 0

// 数学函数
SQRT(value)
POW(value, 2)
```

### 统计函数
```java
// 平均值
AVERAGE(value1, value2, value3)

// 标准差
STDEV(all_values)

// 平方和
SUMSQ(all_values)
```

### 复杂表达式
```java
// TOPSIS正理想解距离
SQRT(POW(max1 - current1, 2) + POW(max2 - current2, 2))

// 能力分级
mean <= 0.5 * stdev ? 
  (value >= mean + 1.5 * stdev ? "强" : "中等") : 
  (value >= mean + 1.5 * stdev ? "强" : "较弱")
```

## 注意事项

1. **变量命名**: 使用与数据库字段一致的变量名
2. **表达式验证**: 每次修改表达式后都要点击"验证"按钮
3. **步骤顺序**: 确保步骤的执行顺序正确
4. **依赖关系**: 后续步骤可以使用前面步骤的输出变量

## 下一步优化

1. 实现完整的模型执行引擎
2. 添加模型版本管理
3. 实现模型导入导出功能
4. 添加表达式测试功能
5. 支持自定义函数扩展