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
            <el-table-column prop="algorithmOrder" label="执行顺序" width="90" sortable />
            <el-table-column prop="algorithmName" label="算法名称" width="150" />
            <el-table-column prop="algorithmCode" label="算法编码" width="150" />
            <el-table-column label="QLExpress表达式" min-width="250">
              <template #default="scope">
                <el-text class="expression-preview" truncated>
                  {{ scope.row.qlExpression || '-' }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column prop="outputParam" label="输出参数" width="150" />
            <el-table-column label="最后一步" width="90">
              <template #default="scope">
                <el-tag v-if="scope.row.isFinalStep" type="warning" size="small">是</el-tag>
                <el-tag v-else type="info" size="small">否</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editAlgorithm(scope.row)">
                  修改
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

    <!-- 添加/编辑算法对话框 -->
    <el-dialog
      v-model="algorithmDialogVisible"
      :title="algorithmDialogMode === 'create' ? '添加算法' : '编辑算法'"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form :model="currentAlgorithm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="算法名称" required>
              <el-input v-model="currentAlgorithm.algorithmName" placeholder="例如：队伍管理能力计算" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="算法编码" required>
              <el-input v-model="currentAlgorithm.algorithmCode" placeholder="例如：MANAGEMENT_CAPABILITY" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="执行顺序" required>
              <el-input-number v-model="currentAlgorithm.algorithmOrder" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否最后一步">
              <el-switch
                v-model="currentAlgorithm.isFinalStep"
                active-text="是"
                inactive-text="否"
                @change="handleFinalStepChange"
              />
              <el-tooltip content="最后一步将自动使用固定的输出参数" placement="top">
                <el-icon style="margin-left: 8px"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="QLExpress表达式" required>
          <div class="expression-editor">
            <!-- 工具栏 -->
            <div class="expression-toolbar">
              <el-space wrap>
                <span class="toolbar-label">输入列：</span>
                <el-select
                  v-model="selectedInputColumn"
                  placeholder="选择输入列"
                  size="small"
                  style="width: 180px"
                  @change="insertInputColumn"
                  clearable
                >
                  <el-option
                    v-for="col in availableInputColumns"
                    :key="col"
                    :label="col"
                    :value="col"
                  />
                </el-select>

                <el-divider direction="vertical" />
                <span class="toolbar-label">运算符：</span>
                <el-button-group size="small">
                  <el-button @click="insertOperator(' + ')">+</el-button>
                  <el-button @click="insertOperator(' - ')">-</el-button>
                  <el-button @click="insertOperator(' * ')">*</el-button>
                  <el-button @click="insertOperator(' / ')">/</el-button>
                  <el-button @click="insertOperator(' ( ')">(</el-button>
                  <el-button @click="insertOperator(' ) ')">)</el-button>
                </el-button-group>

                <el-divider direction="vertical" />
                <span class="toolbar-label">高级函数：</span>
                <el-select
                  v-model="selectedFunction"
                  placeholder="选择函数"
                  size="small"
                  style="width: 200px"
                  @change="insertFunction"
                  clearable
                >
                  <el-option label="归一化 (@NORMALIZE)" value="@NORMALIZE:" />
                  <el-option label="TOPSIS正理想解 (@TOPSIS_POSITIVE)" value="@TOPSIS_POSITIVE:" />
                  <el-option label="TOPSIS负理想解 (@TOPSIS_NEGATIVE)" value="@TOPSIS_NEGATIVE:" />
                  <el-option label="TOPSIS得分 (@TOPSIS_SCORE)" value="@TOPSIS_SCORE:" />
                  <el-option label="能力分级 (@GRADE)" value="@GRADE:" />
                </el-select>

                <el-button
                  type="success"
                  size="small"
                  @click="validateCurrentExpression"
                  :loading="validating"
                >
                  <el-icon><Check /></el-icon>
                  验证表达式
                </el-button>
              </el-space>
            </div>

            <!-- 表达式输入框 -->
            <el-input
              ref="expressionInput"
              v-model="currentAlgorithm.qlExpression"
              type="textarea"
              :rows="5"
              placeholder="例如：(management_staff / population) * 10000&#10;或使用高级函数：@NORMALIZE:capability_score"
              class="expression-textarea"
            />

            <!-- 表达式提示 -->
            <div class="expression-hints">
              <el-alert
                v-if="expressionValid === true"
                title="表达式验证通过"
                type="success"
                :closable="false"
                show-icon
              />
              <el-alert
                v-else-if="expressionValid === false"
                :title="expressionError || '表达式验证失败'"
                type="error"
                :closable="false"
                show-icon
              />
              <div class="hint-text">
                <strong>提示：</strong>
                <ul>
                  <li>基础运算：+, -, *, /, ==, >, <, >=, <=, &&, ||</li>
                  <li>高级函数：@NORMALIZE:参数, @TOPSIS_POSITIVE:参数1,参数2, @GRADE:参数</li>
                  <li>输入列：来自本模型上一步骤的输出参数</li>
                </ul>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="输出参数" required>
          <el-select
            v-if="currentAlgorithm.isFinalStep"
            v-model="currentAlgorithm.outputParam"
            placeholder="选择输出参数"
            style="width: 100%"
          >
            <el-option
              v-for="param in finalStepOutputParams"
              :key="param.value"
              :label="param.label"
              :value="param.value"
            />
          </el-select>
          <el-input
            v-else
            v-model="currentAlgorithm.outputParam"
            placeholder="例如：management_capability"
          />
          <div class="expression-hint" v-if="currentAlgorithm.isFinalStep">
            最后一步必须使用预定义的输出参数
          </div>
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
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, QuestionFilled } from '@element-plus/icons-vue'
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
const algorithmDialogMode = ref<'create' | 'edit'>('create')

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
  description: '',
  isFinalStep: false
})

// 其他状态
const activeTab = ref('steps')
const selectedModelId = ref<number | null>(null)
const selectedStepId = ref<number | null>(null)
const currentStepName = ref('')

// 算法编辑器状态
const selectedInputColumn = ref('')
const selectedFunction = ref('')
const expressionValid = ref<boolean | null>(null)
const expressionError = ref('')
const validating = ref(false)
const expressionInput = ref<any>(null)

// 最后一步的预定义输出参数
const finalStepOutputParams = [
  { label: '灾害管理能力', value: 'management_capability_score' },
  { label: '灾害备灾能力', value: 'support_capability_score' },
  { label: '自救转移能力', value: 'self_rescue_capability_score' },
  { label: '综合减灾能力', value: 'comprehensive_capability_score' },
  { label: '灾害管理能力级别', value: 'management_capability_level' },
  { label: '灾害备灾能力级别', value: 'support_capability_level' },
  { label: '自救转移能力级别', value: 'self_rescue_capability_level' },
  { label: '综合减灾能力级别', value: 'comprehensive_capability_level' }
]

// 可用的输入列（从上一步的输出参数获取）
const availableInputColumns = computed(() => {
  if (!selectedStepId.value || currentAlgorithms.value.length === 0) {
    return []
  }

  // 获取当前步骤的所有已定义算法的输出参数
  const outputParams = currentAlgorithms.value
    .filter(algo => algo.outputParam)
    .map(algo => algo.outputParam)

  // 获取前序步骤的输出参数（这里简化处理，实际应该查询前序步骤）
  return outputParams
})

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
  algorithmDialogMode.value = 'create'
  currentAlgorithm.value = {
    algorithmName: '',
    algorithmCode: '',
    algorithmOrder: currentAlgorithms.value.length + 1,
    qlExpression: '',
    outputParam: '',
    description: '',
    isFinalStep: false
  }
  expressionValid.value = null
  expressionError.value = ''
  algorithmDialogVisible.value = true
}

// 编辑算法
const editAlgorithm = (algorithm: any) => {
  algorithmDialogMode.value = 'edit'
  currentAlgorithm.value = { ...algorithm }
  expressionValid.value = null
  expressionError.value = ''
  algorithmDialogVisible.value = true
}

// 插入输入列到表达式
const insertInputColumn = async () => {
  if (!selectedInputColumn.value) return

  await nextTick()
  const textarea = expressionInput.value?.$el?.querySelector('textarea')
  if (!textarea) {
    currentAlgorithm.value.qlExpression += selectedInputColumn.value
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = currentAlgorithm.value.qlExpression || ''

  currentAlgorithm.value.qlExpression =
    text.substring(0, start) +
    selectedInputColumn.value +
    text.substring(end)

  // 重置选择
  await nextTick()
  const newPos = start + selectedInputColumn.value.length
  textarea.setSelectionRange(newPos, newPos)
  textarea.focus()

  selectedInputColumn.value = ''
  expressionValid.value = null
}

// 插入运算符到表达式
const insertOperator = async (operator: string) => {
  await nextTick()
  const textarea = expressionInput.value?.$el?.querySelector('textarea')
  if (!textarea) {
    currentAlgorithm.value.qlExpression += operator
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = currentAlgorithm.value.qlExpression || ''

  currentAlgorithm.value.qlExpression =
    text.substring(0, start) +
    operator +
    text.substring(end)

  await nextTick()
  const newPos = start + operator.length
  textarea.setSelectionRange(newPos, newPos)
  textarea.focus()

  expressionValid.value = null
}

// 插入高级函数到表达式
const insertFunction = async () => {
  if (!selectedFunction.value) return

  await nextTick()
  const textarea = expressionInput.value?.$el?.querySelector('textarea')
  const functionTemplate = selectedFunction.value

  if (!textarea) {
    currentAlgorithm.value.qlExpression += functionTemplate
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = currentAlgorithm.value.qlExpression || ''

  currentAlgorithm.value.qlExpression =
    text.substring(0, start) +
    functionTemplate +
    text.substring(end)

  await nextTick()
  const newPos = start + functionTemplate.length
  textarea.setSelectionRange(newPos, newPos)
  textarea.focus()

  selectedFunction.value = ''
  expressionValid.value = null
}

// 处理最后一步切换
const handleFinalStepChange = () => {
  if (currentAlgorithm.value.isFinalStep) {
    // 切换到最后一步，使用预定义参数
    currentAlgorithm.value.outputParam = ''
  } else {
    // 取消最后一步，允许自定义输出
    currentAlgorithm.value.outputParam = ''
  }
}

// 验证当前表达式
const validateCurrentExpression = async () => {
  if (!currentAlgorithm.value.qlExpression) {
    ElMessage.warning('请先输入表达式')
    return
  }

  validating.value = true
  expressionValid.value = null
  expressionError.value = ''

  try {
    const response = await request.post('/api/model-management/validate-expression', {
      expression: currentAlgorithm.value.qlExpression
    })

    if (response.valid === true) {
      expressionValid.value = true
      ElMessage.success('表达式验证通过')
    } else {
      expressionValid.value = false
      expressionError.value = response.errorMessage || '表达式语法错误'
    }
  } catch (error: any) {
    expressionValid.value = false
    expressionError.value = error.message || '验证请求失败'
  } finally {
    validating.value = false
  }
}

// 保存算法
const saveAlgorithm = async () => {
  if (!currentAlgorithm.value.algorithmName || !currentAlgorithm.value.qlExpression) {
    ElMessage.warning('请填写必填项')
    return
  }

  if (!currentAlgorithm.value.outputParam) {
    ElMessage.warning('请设置输出参数')
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
    let response
    if (algorithmDialogMode.value === 'create') {
      response = await request.post(
        `/api/model-management/steps/${selectedStepId.value}/algorithms`,
        currentAlgorithm.value
      )
    } else {
      response = await request.put(
        `/api/model-management/algorithms/${currentAlgorithm.value.id}`,
        currentAlgorithm.value
      )
    }

    if (response.success) {
      ElMessage.success(algorithmDialogMode.value === 'create' ? '算法创建成功' : '算法更新成功')
      algorithmDialogVisible.value = false
      // 重新加载算法列表
      const step = currentSteps.value.find(s => s.id === selectedStepId.value)
      if (step) {
        viewStepAlgorithms(step)
      }
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.message || ''))
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

/* 表达式编辑器样式 */
.expression-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  background-color: #fafafa;
}

.expression-toolbar {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.toolbar-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.expression-textarea {
  margin-bottom: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
}

.expression-textarea :deep(textarea) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.expression-hints {
  margin-top: 12px;
}

.hint-text {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f4f4f5;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.hint-text strong {
  color: #303133;
}

.hint-text ul {
  margin: 4px 0 0 0;
  padding-left: 20px;
}

.hint-text li {
  margin: 2px 0;
  line-height: 1.6;
}

.expression-preview {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 12px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>