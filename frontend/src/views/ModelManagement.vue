<template>
  <div class="model-management">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span>评估模型管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建模型
          </el-button>
        </div>
      </template>

      <!-- 模型列表 -->
      <el-table :data="models" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="modelName" label="模型名称" width="200" />
        <el-table-column prop="modelCode" label="模型编码" width="150" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认模型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault" type="warning">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewModelDetail(scope.row)">
              配置
            </el-button>
            <el-button size="small" type="warning" @click="editModel(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteModel(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑模型对话框 -->
    <el-dialog 
      v-model="modelDialogVisible" 
      :title="modelDialogMode === 'create' ? '新建模型' : '编辑模型'"
      width="600px"
    >
      <el-form :model="currentModel" label-width="100px">
        <el-form-item label="模型名称" required>
          <el-input v-model="currentModel.modelName" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型编码" required>
          <el-input v-model="currentModel.modelCode" placeholder="请输入模型编码（英文）" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="currentModel.version" placeholder="例如：1.0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="currentModel.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入模型描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveModel" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 模型详情配置对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="模型配置" 
      width="90%" 
      top="5vh"
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 步骤管理 -->
        <el-tab-pane label="步骤管理" name="steps">
          <div class="tab-header">
            <el-button type="primary" size="small" @click="showAddStepDialog">
              <el-icon><Plus /></el-icon>
              添加步骤
            </el-button>
          </div>
          
          <el-table :data="currentSteps" style="width: 100%; margin-top: 20px">
            <el-table-column prop="stepOrder" label="顺序" width="70" sortable />
            <el-table-column prop="stepName" label="步骤名称" width="150" />
            <el-table-column prop="stepCode" label="步骤编码" width="180" />
            <el-table-column prop="stepType" label="类型" width="140">
              <template #default="scope">
                <el-tag :type="getStepTypeColor(scope.row.stepType)">
                  {{ getStepTypeName(scope.row.stepType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button size="small" type="primary" @click="viewStepAlgorithms(scope.row)">
                  算法配置
                </el-button>
                <el-button size="small" type="danger" @click="deleteStep(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 算法配置 -->
        <el-tab-pane label="算法配置" name="algorithms">
          <div class="tab-header">
            <el-space>
              <span>当前步骤: {{ currentStepName }}</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="showAddAlgorithmDialog"
                :disabled="!selectedStepId"
              >
                <el-icon><Plus /></el-icon>
                添加算法
              </el-button>
            </el-space>
          </div>

          <el-table :data="currentAlgorithms" style="width: 100%; margin-top: 20px">
            <el-table-column prop="algorithmOrder" label="顺序" width="70" sortable />
            <el-table-column prop="algorithmName" label="算法名称" width="150" />
            <el-table-column prop="algorithmCode" label="算法编码" width="150" />
            <el-table-column label="QLExpress表达式" min-width="300">
              <template #default="scope">
                <el-input
                  v-model="scope.row.qlExpression"
                  type="textarea"
                  :rows="2"
                  placeholder="输入QLExpress表达式"
                  @blur="validateExpression(scope.row.qlExpression)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="outputParam" label="输出参数" width="120" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="updateAlgorithm(scope.row)">
                  保存
                </el-button>
                <el-button size="small" type="warning" @click="testExpression(scope.row)">
                  测试
                </el-button>
                <el-button size="small" type="danger" @click="deleteAlgorithm(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 添加步骤对话框 -->
    <el-dialog v-model="stepDialogVisible" title="添加步骤" width="600px">
      <el-form :model="currentStep" label-width="100px">
        <el-form-item label="步骤名称" required>
          <el-input v-model="currentStep.stepName" placeholder="例如：评估指标赋值" />
        </el-form-item>
        <el-form-item label="步骤编码" required>
          <el-input v-model="currentStep.stepCode" placeholder="例如：INDICATOR_ASSIGNMENT" />
        </el-form-item>
        <el-form-item label="执行顺序" required>
          <el-input-number v-model="currentStep.stepOrder" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="步骤类型" required>
          <el-select v-model="currentStep.stepType" placeholder="请选择步骤类型">
            <el-option label="指标计算" value="CALCULATION" />
            <el-option label="归一化" value="NORMALIZATION" />
            <el-option label="定权" value="WEIGHTING" />
            <el-option label="TOPSIS算法" value="TOPSIS" />
            <el-option label="能力分级" value="GRADING" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentStep.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stepDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStep" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加算法对话框 -->
    <el-dialog v-model="algorithmDialogVisible" title="添加算法" width="700px">
      <el-form :model="currentAlgorithm" label-width="120px">
        <el-form-item label="算法名称" required>
          <el-input v-model="currentAlgorithm.algorithmName" placeholder="例如：队伍管理能力计算" />
        </el-form-item>
        <el-form-item label="算法编码" required>
          <el-input v-model="currentAlgorithm.algorithmCode" placeholder="例如：MANAGEMENT_CAPABILITY" />
        </el-form-item>
        <el-form-item label="执行顺序" required>
          <el-input-number v-model="currentAlgorithm.algorithmOrder" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="QLExpress表达式" required>
          <el-input
            v-model="currentAlgorithm.qlExpression"
            type="textarea"
            :rows="4"
            placeholder="例如：(management_staff / population) * 10000"
          />
          <div class="expression-hint">
            支持: +, -, *, /, ==, >, <, >=, <=, &&, ||, ?, :, SQRT(), AVERAGE(), STDEV()等
          </div>
        </el-form-item>
        <el-form-item label="输出参数">
          <el-input v-model="currentAlgorithm.outputParam" placeholder="例如：management_capability" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentAlgorithm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="algorithmDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAlgorithm" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 状态管理
const loading = ref(false)
const saving = ref(false)
const models = ref<any[]>([])
const currentSteps = ref<any[]>([])
const currentAlgorithms = ref<any[]>([])

// 对话框控制
const modelDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const stepDialogVisible = ref(false)
const algorithmDialogVisible = ref(false)

// 表单数据
const modelDialogMode = ref<'create' | 'edit'>('create')
const currentModel = ref<any>({
  modelName: '',
  modelCode: '',
  version: '1.0',
  description: ''
})

const currentStep = ref<any>({
  stepName: '',
  stepCode: '',
  stepOrder: 1,
  stepType: 'CALCULATION',
  description: ''
})

const currentAlgorithm = ref<any>({
  algorithmName: '',
  algorithmCode: '',
  algorithmOrder: 1,
  qlExpression: '',
  outputParam: '',
  description: ''
})

// 其他状态
const activeTab = ref('steps')
const selectedModelId = ref<number | null>(null)
const selectedStepId = ref<number | null>(null)
const currentStepName = ref('')

// 加载模型列表
const loadModels = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/model-management/models')
    if (response.success) {
      models.value = response.data || []
    }
  } catch (error: any) {
    ElMessage.error('加载模型列表失败: ' + (error.message || ''))
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  modelDialogMode.value = 'create'
  currentModel.value = {
    modelName: '',
    modelCode: '',
    version: '1.0',
    description: ''
  }
  modelDialogVisible.value = true
}

// 编辑模型
const editModel = (model: any) => {
  modelDialogMode.value = 'edit'
  currentModel.value = { ...model }
  modelDialogVisible.value = true
}

// 保存模型
const saveModel = async () => {
  if (!currentModel.value.modelName || !currentModel.value.modelCode) {
    ElMessage.warning('请填写必填项')
    return
  }

  saving.value = true
  try {
    const url = modelDialogMode.value === 'create' 
      ? '/api/model-management/models'
      : `/api/model-management/models/${currentModel.value.id}`
    
    const method = modelDialogMode.value === 'create' ? 'post' : 'put'
    const response = await request[method](url, currentModel.value)
    
    if (response.success) {
      ElMessage.success(modelDialogMode.value === 'create' ? '创建成功' : '更新成功')
      modelDialogVisible.value = false
      loadModels()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.message || ''))
  } finally {
    saving.value = false
  }
}

// 删除模型
const deleteModel = (model: any) => {
  ElMessageBox.confirm(`确定要删除模型 "${model.modelName}" 吗？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/model-management/models/${model.id}`)
      ElMessage.success('删除成功')
      loadModels()
    } catch (error: any) {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  })
}

// 查看模型详情
const viewModelDetail = async (model: any) => {
  selectedModelId.value = model.id
  loading.value = true
  
  try {
    const response = await request.get(`/api/model-management/models/${model.id}/detail`)
    if (response.success) {
      currentSteps.value = response.data.steps || []
      detailDialogVisible.value = true
      activeTab.value = 'steps'
    }
  } catch (error: any) {
    ElMessage.error('加载模型详情失败: ' + (error.message || ''))
  } finally {
    loading.value = false
  }
}

// 显示添加步骤对话框
const showAddStepDialog = () => {
  currentStep.value = {
    stepName: '',
    stepCode: '',
    stepOrder: currentSteps.value.length + 1,
    stepType: 'CALCULATION',
    description: ''
  }
  stepDialogVisible.value = true
}

// 保存步骤
const saveStep = async () => {
  if (!currentStep.value.stepName || !currentStep.value.stepCode) {
    ElMessage.warning('请填写必填项')
    return
  }

  saving.value = true
  try {
    const response = await request.post(
      `/api/model-management/models/${selectedModelId.value}/steps`,
      currentStep.value
    )
    
    if (response.success) {
      ElMessage.success('步骤创建成功')
      stepDialogVisible.value = false
      viewModelDetail({ id: selectedModelId.value })
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error: any) {
    ElMessage.error('创建失败: ' + (error.message || ''))
  } finally {
    saving.value = false
  }
}

// 删除步骤
const deleteStep = (step: any) => {
  ElMessageBox.confirm(`确定要删除步骤 "${step.stepName}" 吗？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/model-management/steps/${step.id}`)
      ElMessage.success('删除成功')
      viewModelDetail({ id: selectedModelId.value })
    } catch (error: any) {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  })
}

// 查看步骤算法
const viewStepAlgorithms = async (step: any) => {
  selectedStepId.value = step.id
  currentStepName.value = step.stepName
  activeTab.value = 'algorithms'
  
  loading.value = true
  try {
    // 从步骤描述中解析算法列表（临时方案）
    const response = await request.get(`/api/model-management/models/${selectedModelId.value}/detail`)
    if (response.success) {
      const steps = response.data.steps || []
      const currentStep = steps.find((s: any) => s.id === step.id)
      
      if (currentStep && currentStep.description && currentStep.description.includes('|ALGORITHMS|')) {
        const algoJson = currentStep.description.split('|ALGORITHMS|')[1]
        currentAlgorithms.value = JSON.parse(algoJson)
      } else {
        currentAlgorithms.value = []
      }
    }
  } catch (error: any) {
    ElMessage.error('加载算法列表失败: ' + (error.message || ''))
    currentAlgorithms.value = []
  } finally {
    loading.value = false
  }
}

// 显示添加算法对话框
const showAddAlgorithmDialog = () => {
  currentAlgorithm.value = {
    algorithmName: '',
    algorithmCode: '',
    algorithmOrder: currentAlgorithms.value.length + 1,
    qlExpression: '',
    outputParam: '',
    description: ''
  }
  algorithmDialogVisible.value = true
}

// 保存算法
const saveAlgorithm = async () => {
  if (!currentAlgorithm.value.algorithmName || !currentAlgorithm.value.qlExpression) {
    ElMessage.warning('请填写必填项')
    return
  }

  // 验证表达式
  const isValid = await validateExpression(currentAlgorithm.value.qlExpression)
  if (!isValid) {
    ElMessage.error('QLExpress表达式语法错误，请检查')
    return
  }

  saving.value = true
  try {
    const response = await request.post(
      `/api/model-management/steps/${selectedStepId.value}/algorithms`,
      currentAlgorithm.value
    )
    
    if (response.success) {
      ElMessage.success('算法创建成功')
      algorithmDialogVisible.value = false
      // 重新加载算法列表
      const step = currentSteps.value.find(s => s.id === selectedStepId.value)
      if (step) {
        viewStepAlgorithms(step)
      }
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error: any) {
    ElMessage.error('创建失败: ' + (error.message || ''))
  } finally {
    saving.value = false
  }
}

// 更新算法
const updateAlgorithm = async (algorithm: any) => {
  // 验证表达式
  const isValid = await validateExpression(algorithm.qlExpression)
  if (!isValid) {
    ElMessage.error('QLExpress表达式语法错误，请检查')
    return
  }

  saving.value = true
  try {
    const response = await request.put(
      `/api/model-management/algorithms/${algorithm.id}`,
      algorithm
    )
    
    if (response.success) {
      ElMessage.success('算法更新成功')
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error: any) {
    ElMessage.error('更新失败: ' + (error.message || ''))
  } finally {
    saving.value = false
  }
}

// 删除算法
const deleteAlgorithm = (algorithm: any) => {
  ElMessageBox.confirm(`确定要删除算法 "${algorithm.algorithmName}" 吗？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/model-management/algorithms/${algorithm.id}`)
      ElMessage.success('删除成功')
      // 重新加载算法列表
      const step = currentSteps.value.find(s => s.id === selectedStepId.value)
      if (step) {
        viewStepAlgorithms(step)
      }
    } catch (error: any) {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  })
}

// 验证表达式
const validateExpression = async (expression: string): Promise<boolean> => {
  if (!expression) return false
  
  try {
    const response = await request.post('/api/model-management/validate-expression', {
      expression: expression
    })
    return response.valid === true
  } catch (error) {
    return false
  }
}

// 测试表达式
const testExpression = async (algorithm: any) => {
  try {
    const response = await request.post('/api/model-management/validate-expression', {
      expression: algorithm.qlExpression,
      context: {
        management_staff: 10,
        population: 1000,
        value: 100
      }
    })
    
    if (response.valid) {
      ElMessage.success('表达式验证通过！')
    } else {
      ElMessage.error('表达式验证失败: ' + (response.errorMessage || ''))
    }
  } catch (error: any) {
    ElMessage.error('验证失败: ' + (error.message || ''))
  }
}

// 步骤类型辅助函数
const getStepTypeName = (type: string) => {
  const names: Record<string, string> = {
    'CALCULATION': '指标计算',
    'NORMALIZATION': '归一化',
    'WEIGHTING': '定权',
    'TOPSIS': 'TOPSIS',
    'GRADING': '能力分级'
  }
  return names[type] || type
}

const getStepTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    'CALCULATION': '',
    'NORMALIZATION': 'success',
    'WEIGHTING': 'warning',
    'TOPSIS': 'danger',
    'GRADING': 'info'
  }
  return colors[type] || ''
}

// 初始化
onMounted(() => {
  loadModels()
})
</script>

<style scoped>
.model-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-header {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.expression-hint {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>