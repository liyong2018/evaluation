<template>
  <div class="model-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>评估模型管理</h1>
      <p>评估模型的创建、配置、启用和版本管理</p>
    </div>

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
      <div style="margin-top: 15px">
        <el-table :data="models" style="width: 100%" v-loading="loading" class="model-list">
        <el-table-column prop="modelName" label="模型名称" width="250" />
        <el-table-column prop="modelCode" label="模型编码" width="380" />
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
        <el-table-column label="操作" width="260" fixed="right">
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
      </div>
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

                <el-button
                  type="primary"
                  size="small"
                  @click="showWeightSelectorDialog"
                >
                  <el-icon><Grid /></el-icon>
                  选择权重
                </el-button>

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
                  <li>基础运算：+, -, *, /, ==, &gt;, &lt;, &gt;=, &lt;=, &amp;&amp;, ||</li>
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

    <!-- 权重选择对话框 -->
    <el-dialog
      v-model="weightSelectorDialogVisible"
      title="选择权重"
      width="1920px"
      :close-on-click-modal="false"
    >
      <div class="weight-selector-container">
        <!-- 左侧：组织机构树 -->
        <el-card class="org-tree-card" shadow="never">
          <template #header>
            <span>组织机构</span>
          </template>
          <el-tree
            ref="weightOrgTreeRef"
            v-loading="loadingWeightOrgs"
            :data="weightOrganizationList"
            :props="{ label: 'name', children: 'children' }"
            node-key="code"
            highlight-current
            :expand-on-click-node="false"
            default-expand-all
            @node-click="handleWeightOrgNodeClick"
          >
            <template #default="{ data }">
              <div class="org-tree-node">
                <span class="org-name">{{ data.name }}</span>
                <span class="org-code">{{ data.code }}</span>
              </div>
            </template>
          </el-tree>
        </el-card>

        <!-- 右侧：权重值树 -->
        <el-card class="weight-tree-card" shadow="never">
          <template #header>
            <div class="weight-tree-header">
              <span>权重配置</span>
              <el-tag v-if="selectedWeightOrg" type="primary" size="small">
                {{ selectedWeightOrg.name }}
              </el-tag>
            </div>
          </template>
          <div v-if="selectedWeightOrg" v-loading="loadingWeightData" class="weight-tree-content">
            <el-empty v-if="!weightConfigs.length" description="该组织机构暂无权重配置" />
            <div v-else>
              <!-- 权重配置选择 -->
              <el-select
                v-model="selectedWeightConfigId"
                placeholder="请选择权重配置"
                style="width: 100%; margin-bottom: 16px"
                @change="loadWeightValues"
              >
                <el-option
                  v-for="config in weightConfigs"
                  :key="config.id"
                  :label="config.configName"
                  :value="config.id"
                >
                  <div style="display: flex; justify-content: space-between">
                    <span>{{ config.configName }}</span>
                    <el-tag size="small" type="info">{{ config.description }}</el-tag>
                  </div>
                </el-option>
              </el-select>

              <!-- 权重值树形结构 -->
              <el-tree
                v-if="weightTreeData.length"
                :data="weightTreeData"
                node-key="id"
                default-expand-all
                :expand-on-click-node="false"
                class="weight-values-tree"
              >
                <template #default="{ data }">
                  <div class="weight-tree-node" @click="selectWeightValue(data)">
                    <div class="weight-node-info">
                      <el-tag :type="data.indicatorLevel === 1 ? 'primary' : 'success'" size="small">
                        {{ data.indicatorCode }}
                      </el-tag>
                      <span class="indicator-name">{{ data.indicatorName }}</span>
                    </div>
                    <div class="weight-value-display">
                      <span class="weight-label">权重:</span>
                      <span class="weight-number">{{ data.weight.toFixed(3) }}</span>
                      <el-button
                        type="primary"
                        size="small"
                        @click.stop="selectWeightValue(data)"
                      >
                        选择
                      </el-button>
                    </div>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>
          <el-empty v-else description="请选择左侧组织机构" />
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, QuestionFilled, Grid } from '@element-plus/icons-vue'
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
const weightSelectorDialogVisible = ref(false)

// 权重选择相关
const weightOrganizationList = ref<any[]>([])
const weightOrgTreeRef = ref()
const selectedWeightOrg = ref<any>(null)
const weightConfigs = ref<any[]>([])
const selectedWeightConfigId = ref<number | null>(null)
const weightTreeData = ref<any[]>([])
const loadingWeightOrgs = ref(false)
const loadingWeightData = ref(false)

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
const currentModelInfo = ref<any>(null) // 保存当前模型的完整信息

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

// 乡镇数据表字段 (survey_data) - 使用下划线命名与数据库字段一致
const townshipDataFields = [
  'province', 'city', 'county', 'township',
  'population', 'management_staff', 'risk_assessment',
  'funding_amount', 'material_value', 'hospital_beds',
  'firefighters', 'volunteers', 'militia_reserve'
]

// 社区数据表字段 (community_disaster_reduction_capacity) - 使用下划线命名与数据库字段一致
const communityDataFields = [
  'province_name', 'city_name', 'county_name', 'township_name', 'community_name',
  'resident_population', 'has_emergency_plan', 'has_vulnerable_groups_list',
  'has_disaster_points_list', 'has_disaster_map',
  'last_year_funding_amount', 'materials_equipment_value', 'medical_service_count',
  'registered_volunteer_count', 'militia_reserve_count',
  'last_year_training_participants', 'last_year_drill_participants',
  'emergency_shelter_capacity'
]

// 可用的输入列（从上一步的输出参数获取）
const availableInputColumns = computed(() => {
  if (!selectedStepId.value) {
    return []
  }

  // 找到当前步骤
  const currentStep = currentSteps.value.find(s => s.id === selectedStepId.value)
  if (!currentStep) {
    return []
  }

  // 找到上一步骤（stepOrder 小于当前步骤的最大那个）
  const previousSteps = currentSteps.value
    .filter(s => s.stepOrder < currentStep.stepOrder)
    .sort((a, b) => b.stepOrder - a.stepOrder)

  if (previousSteps.length === 0) {
    // 如果是第一步，根据modelId返回对应数据表字段
    const modelId = currentModelInfo.value?.id

    // modelId=3: 乡镇模型使用survey_data
    // modelId=4,8: 社区模型使用community_disaster_reduction_capacity
    const isTownshipModel = modelId === 3

    return isTownshipModel ? townshipDataFields : communityDataFields
  }

  // 获取上一步骤的输出参数
  const previousStep = previousSteps[0]

  // 从步骤描述中解析算法列表
  if (previousStep.description && previousStep.description.includes('|ALGORITHMS|')) {
    try {
      const algoJson = previousStep.description.split('|ALGORITHMS|')[1]
      const algorithms = JSON.parse(algoJson)
      const outputParams = algorithms
        .filter((algo: any) => algo.outputParam)
        .map((algo: any) => algo.outputParam)
      return outputParams
    } catch (e) {
      return []
    }
  }

  return []
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
  currentModelInfo.value = model // 保存当前模型信息
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
  const functionName = selectedFunction.value.replace(':', '') // 去掉冒号

  if (!textarea) {
    // 如果没有textarea，将整个表达式包裹
    const existingExpression = currentAlgorithm.value.qlExpression || ''
    if (existingExpression) {
      currentAlgorithm.value.qlExpression = `${functionName}(${existingExpression})`
    } else {
      currentAlgorithm.value.qlExpression = `${functionName}()`
    }
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = currentAlgorithm.value.qlExpression || ''
  const selectedText = text.substring(start, end)

  if (selectedText) {
    // 如果有选中文本，将函数包裹在选中文本外面
    currentAlgorithm.value.qlExpression =
      text.substring(0, start) +
      `${functionName}(${selectedText})` +
      text.substring(end)

    await nextTick()
    const newPos = start + functionName.length + selectedText.length + 2
    textarea.setSelectionRange(newPos, newPos)
  } else {
    // 如果没有选中文本，插入函数模板
    currentAlgorithm.value.qlExpression =
      text.substring(0, start) +
      `${functionName}()` +
      text.substring(end)

    await nextTick()
    // 将光标放在括号内
    const newPos = start + functionName.length + 1
    textarea.setSelectionRange(newPos, newPos)
  }

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

// ========== 权重选择相关方法 ==========

// 显示权重选择对话框
const showWeightSelectorDialog = async () => {
  weightSelectorDialogVisible.value = true
  // 加载组织机构树
  await loadWeightOrganizations()
}

// 加载组织机构列表
const loadWeightOrganizations = async () => {
  loadingWeightOrgs.value = true
  try {
    const response = await request.get('/api/organization/tree')
    weightOrganizationList.value = response.data || []
  } catch (error: any) {
    ElMessage.error('加载组织机构失败: ' + (error.message || ''))
  } finally {
    loadingWeightOrgs.value = false
  }
}

// 处理组织机构节点点击
const handleWeightOrgNodeClick = async (data: any) => {
  selectedWeightOrg.value = data
  selectedWeightConfigId.value = null
  weightTreeData.value = []

  // 加载该组织机构的权重配置列表
  await loadWeightConfigs(data.code)
}

// 加载权重配置列表（根据组织机构代码）
const loadWeightConfigs = async (orgcode: string) => {
  loadingWeightData.value = true
  try {
    const response = await request.get('/api/weight-config', {
      params: { orgcode }
    })
    weightConfigs.value = response.data || []

    // 如果只有一个配置，自动选中
    if (weightConfigs.value.length === 1) {
      selectedWeightConfigId.value = weightConfigs.value[0].id
      await loadWeightValues()
    }
  } catch (error: any) {
    ElMessage.error('加载权重配置失败: ' + (error.message || ''))
  } finally {
    loadingWeightData.value = false
  }
}

// 加载权重值列表（根据配置ID）
const loadWeightValues = async () => {
  if (!selectedWeightConfigId.value) return

  loadingWeightData.value = true
  try {
    const response = await request.get(`/api/indicator-weight/config/${selectedWeightConfigId.value}/average-score`)
    const weights = response.data || []

    // 构建树形结构
    weightTreeData.value = buildWeightTree(weights)
  } catch (error: any) {
    ElMessage.error('加载权重值失败: ' + (error.message || ''))
  } finally {
    loadingWeightData.value = false
  }
}

// 构建权重树形结构
const buildWeightTree = (weights: any[]) => {
  const nodeMap = new Map()
  const roots: any[] = []

  // 第一遍：创建所有节点
  weights.forEach((item: any) => {
    item.children = []
    nodeMap.set(item.id, item)
  })

  // 第二遍：建立父子关系
  weights.forEach((item: any) => {
    if (item.parentId !== null && item.parentId !== undefined && nodeMap.has(item.parentId)) {
      const parent = nodeMap.get(item.parentId)
      parent.children.push(item)
    } else {
      // 一级指标（没有父节点）
      roots.push(item)
    }
  })

  return roots
}

// 选择权重值
const selectWeightValue = async (data: any) => {
  const weightValue = data.weight.toFixed(3)

  // 插入到表达式文本框的光标位置
  await nextTick()
  const textarea = expressionInput.value?.$el?.querySelector('textarea')
  if (!textarea) {
    currentAlgorithm.value.qlExpression += weightValue
  } else {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = currentAlgorithm.value.qlExpression || ''

    currentAlgorithm.value.qlExpression =
      text.substring(0, start) +
      weightValue +
      text.substring(end)

    // 重置光标位置
    await nextTick()
    const newPos = start + weightValue.length
    textarea.setSelectionRange(newPos, newPos)
    textarea.focus()
  }

  // 关闭对话框
  weightSelectorDialogVisible.value = false

  ElMessage.success(`已插入权重值: ${weightValue}`)
}

// 初始化
onMounted(() => {
  loadModels()
})
</script>

<style scoped>
.model-management {
    margin: 0 auto;
    max-width: 1920px;
    height:100%
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-card {
  margin-bottom: 20px;
  height:100%;
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

/* 留出标题与表格之间 15px 间距 */
.model-list {
  margin-top: 15px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}

/* ========== 权重选择对话框样式 ========== */
.weight-selector-container {
  display: flex;
  gap: 16px;
  height: 600px;
}

.org-tree-card {
  flex: 0 0 300px;
  overflow-y: auto;
}

.weight-tree-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.weight-tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.weight-tree-content {
  flex: 1;
  overflow-y: auto;
}

.org-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 4px 8px;
}

.org-name {
  font-weight: 500;
  color: #303133;
}

.org-code {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.weight-values-tree {
  margin-top: 8px;
}

.weight-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e8eef5;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  margin: 8px 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.weight-tree-node:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateX(2px);
}

.weight-node-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.indicator-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.weight-value-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.weight-label {
  font-size: 12px;
  color: #606266;
  font-weight: 600;
}

.weight-number {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  min-width: 70px;
  text-align: right;
}

/* 子节点缩进样式 */
.weight-values-tree :deep(.el-tree-node__children > .el-tree-node .weight-tree-node) {
  margin-left: 24px;
  border-left: 3px solid #409eff;
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
}

.weight-values-tree :deep(.el-tree-node__content) {
  height: auto !important;
  padding: 6px 0 !important;
}

.weight-values-tree :deep(.el-tree-node__expand-icon) {
  font-size: 14px;
  color: #909399;
  transition: all 0.3s ease;
}

.weight-values-tree :deep(.el-tree-node__expand-icon:hover) {
  color: #409eff;
}

/* Ensure 15px gap between card header and table */
.header-card :deep(.el-card__body) {
  padding-top: 15px;
}

.header-card :deep(.el-card__header) {
  padding-bottom: 8px;
}

.model-list {
  margin-top: 0; /* rely on card body padding */
}
</style>
