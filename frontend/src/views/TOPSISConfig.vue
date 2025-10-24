<template>
  <div class="topsis-config">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>TOPSIS配置管理</h1>
      <p>管理TOPSIS优劣解算法的指标列配置和参数设置</p>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="config-tabs">
      <!-- 配置管理 -->
      <el-tab-pane label="配置管理" name="config">
        <!-- 操作工具栏 -->
        <el-card class="toolbar-card">
          <el-row :gutter="20" justify="space-between">
            <el-col :span="12">
              <el-input
                v-model="configSearch"
                placeholder="搜索模型或步骤"
                clearable
                @keyup.enter="searchConfigs"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="12">
              <div class="toolbar-actions">
                <el-button type="success" @click="refreshConfigs">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 配置列表 -->
        <el-card class="config-list">
          <el-table
            v-loading="loading.configs"
            :data="configList"
            stripe
            border
          >
            <el-table-column prop="modelId" label="模型ID" width="80" />
            <el-table-column prop="modelName" label="模型名称" width="200" />
            <el-table-column prop="stepCode" label="步骤代码" width="120" />
            <el-table-column prop="stepName" label="步骤名称" width="200" />
            <el-table-column prop="algorithmCode" label="算法类型" width="150">
              <template #default="{ row }">
                <el-tag :type="getAlgorithmTypeTag(row.algorithmCode)">
                  {{ getAlgorithmTypeName(row.algorithmCode) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="indicators" label="配置指标" min-width="300">
              <template #default="{ row }">
                <div class="indicators-display">
                  <el-tag
                    v-for="indicator in row.indicators"
                    :key="indicator"
                    size="small"
                    class="indicator-tag"
                  >
                    {{ indicator }}
                  </el-tag>
                  <span v-if="!row.indicators || row.indicators.length === 0" class="no-indicators">
                    未配置指标
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.isConfigured ? 'success' : 'warning'">
                  {{ row.isConfigured ? '已配置' : '待配置' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="configureIndicators(row)">
                  <el-icon><Setting /></el-icon>
                  配置指标
                </el-button>
                <el-button 
                  type="success" 
                  size="small" 
                  @click="testConfiguration(row)"
                  :disabled="!row.isConfigured"
                >
                  <el-icon><View /></el-icon>
                  测试配置
                </el-button>
                <el-button 
                  type="info" 
                  size="small" 
                  @click="viewHistory(row)"
                >
                  <el-icon><Clock /></el-icon>
                  历史记录
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 指标配置 -->
      <el-tab-pane label="指标配置" name="indicators">
        <!-- 配置选择 -->
        <el-card class="indicator-toolbar">
          <el-row :gutter="20" align="middle">
            <el-col :span="6">
              <el-select 
                v-model="selectedModelId" 
                placeholder="选择评估模型"
                @change="loadModelSteps"
              >
                <el-option
                  v-for="model in modelList"
                  :key="model.id"
                  :label="model.modelName"
                  :value="model.id"
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-select 
                v-model="selectedStepId" 
                placeholder="选择TOPSIS步骤"
                @change="loadStepConfiguration"
                :disabled="!selectedModelId"
              >
                <el-option
                  v-for="step in topsisSteps"
                  :key="step.id"
                  :label="`${step.stepCode} - ${step.stepName}`"
                  :value="step.id"
                />
              </el-select>
            </el-col>
            <el-col :span="12">
              <div class="toolbar-actions">
                <el-button 
                  type="primary" 
                  @click="loadAvailableIndicators" 
                  :disabled="!selectedModelId"
                >
                  <el-icon><Refresh /></el-icon>
                  加载可用指标
                </el-button>
                <el-button 
                  type="success" 
                  @click="previewConfiguration" 
                  :disabled="!selectedStepId || selectedIndicators.length === 0"
                >
                  <el-icon><View /></el-icon>
                  预览配置
                </el-button>
                <el-button 
                  type="warning" 
                  @click="rollbackConfiguration" 
                  :disabled="!hasUnsavedChanges"
                >
                  <el-icon><RefreshLeft /></el-icon>
                  回滚更改
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 配置验证状态 -->
        <el-card class="validation-status" v-if="selectedStepId">
          <template #header>
            <div class="validation-header">
              <span>配置验证状态</span>
              <div class="validation-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="validateConfiguration"
                  :loading="loading.validation"
                >
                  <el-icon><Check /></el-icon>
                  手动验证
                </el-button>
                <el-button 
                  type="success" 
                  size="small" 
                  @click="quickTestConfiguration"
                  :loading="loading.testing"
                  :disabled="!validationStatus.isValid"
                >
                  <el-icon><Play /></el-icon>
                  快速测试
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="validation-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="validation-item">
                  <el-icon :class="validationStatus.isValid ? 'success-icon' : 'error-icon'">
                    <component :is="validationStatus.isValid ? 'SuccessFilled' : 'CircleCloseFilled'" />
                  </el-icon>
                  <span class="validation-text">
                    {{ validationStatus.isValid ? '配置有效' : '配置无效' }}
                  </span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="validation-item">
                  <el-icon class="info-icon">
                    <InfoFilled />
                  </el-icon>
                  <span class="validation-text">
                    指标数量: {{ selectedIndicators.length }}
                  </span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="validation-item">
                  <el-icon class="info-icon">
                    <Clock />
                  </el-icon>
                  <span class="validation-text">
                    {{ validationStatus.lastValidated ? `最后验证: ${validationStatus.lastValidated}` : '未验证' }}
                  </span>
                </div>
              </el-col>
            </el-row>
            
            <!-- 错误和警告 -->
            <div v-if="validationStatus.errors.length > 0 || validationStatus.warnings.length > 0" class="validation-messages">
              <div v-if="validationStatus.errors.length > 0" class="error-messages">
                <h5>错误信息:</h5>
                <ul>
                  <li v-for="error in validationStatus.errors" :key="error" class="error-item">
                    {{ error }}
                  </li>
                </ul>
              </div>
              <div v-if="validationStatus.warnings.length > 0" class="warning-messages">
                <h5>警告信息:</h5>
                <ul>
                  <li v-for="warning in validationStatus.warnings" :key="warning" class="warning-item">
                    {{ warning }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 指标选择器 -->
        <el-card class="indicator-selector" v-if="selectedStepId">
          <div class="selector-header">
            <h3>选择TOPSIS计算指标</h3>
            <p>从下方可用指标中选择用于TOPSIS计算的指标列</p>
          </div>
          
          <el-row :gutter="20">
            <!-- 可用指标 -->
            <el-col :span="10">
              <div class="indicator-panel">
                <div class="panel-header">
                  <h4>可用指标</h4>
                  <el-input
                    v-model="indicatorSearch"
                    placeholder="搜索指标"
                    size="small"
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                </div>
                <div class="indicator-list" v-loading="loading.indicators">
                  <div
                    v-for="indicator in filteredAvailableIndicators"
                    :key="indicator.name"
                    class="indicator-item"
                    @click="addIndicator(indicator)"
                  >
                    <div class="indicator-info">
                      <span class="indicator-name">{{ indicator.name }}</span>
                      <span class="indicator-type">{{ indicator.dataType }}</span>
                    </div>
                    <div class="indicator-meta">
                      <el-tag size="small" type="info">{{ indicator.sampleCount }} 样本</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>

            <!-- 操作按钮 -->
            <el-col :span="4">
              <div class="operation-buttons">
                <el-button 
                  type="primary" 
                  @click="addAllIndicators"
                  :disabled="availableIndicators.length === 0"
                >
                  全部添加 >>
                </el-button>
                <el-button 
                  type="default" 
                  @click="removeAllIndicators"
                  :disabled="selectedIndicators.length === 0"
                >
                  << 全部移除
                </el-button>
              </div>
            </el-col>

            <!-- 已选指标 -->
            <el-col :span="10">
              <div class="indicator-panel">
                <div class="panel-header">
                  <h4>
                    已选指标 ({{ selectedIndicators.length }})
                    <el-badge 
                      v-if="hasUnsavedChanges" 
                      value="未保存" 
                      type="warning" 
                      class="unsaved-badge"
                    />
                  </h4>
                  <div class="save-actions">
                    <el-button 
                      type="success" 
                      size="small" 
                      @click="saveConfiguration"
                      :disabled="selectedIndicators.length === 0 || !hasUnsavedChanges"
                      :loading="loading.save"
                    >
                      保存配置
                    </el-button>
                    <el-button 
                      type="info" 
                      size="small" 
                      @click="autoSaveConfiguration"
                      :disabled="selectedIndicators.length === 0 || !hasUnsavedChanges"
                      :loading="loading.autoSave"
                    >
                      自动保存
                    </el-button>
                  </div>
                </div>
                <div class="indicator-list">
                  <div
                    v-for="(indicator, index) in selectedIndicators"
                    :key="indicator"
                    class="indicator-item selected"
                  >
                    <div class="indicator-info">
                      <span class="indicator-name">{{ indicator }}</span>
                      <span class="indicator-order">{{ index + 1 }}</span>
                    </div>
                    <div class="indicator-actions">
                      <el-button 
                        type="text" 
                        size="small" 
                        @click="moveIndicatorUp(index)"
                        :disabled="index === 0"
                      >
                        <el-icon><ArrowUp /></el-icon>
                      </el-button>
                      <el-button 
                        type="text" 
                        size="small" 
                        @click="moveIndicatorDown(index)"
                        :disabled="index === selectedIndicators.length - 1"
                      >
                        <el-icon><ArrowDown /></el-icon>
                      </el-button>
                      <el-button 
                        type="text" 
                        size="small" 
                        @click="removeIndicator(index)"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 配置测试对话框 -->
    <el-dialog
      v-model="dialogVisible.test"
      title="TOPSIS配置测试"
      width="90%"
      @close="resetTest"
    >
      <TOPSISTestPanel 
        :config="selectedTestConfig"
        @test-completed="onTestCompleted"
      />
    </el-dialog>

    <!-- 配置预览对话框 -->
    <el-dialog
      v-model="dialogVisible.preview"
      title="TOPSIS配置预览"
      width="80%"
      @close="resetPreview"
    >
      <div v-loading="loading.preview">
        <TOPSISPreviewPanel
          :model-id="selectedModelId"
          :step-id="selectedStepId"
          :indicators="selectedIndicators"
        />
      </div>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog
      v-model="dialogVisible.history"
      title="配置历史记录"
      width="70%"
      @close="resetHistory"
    >
      <TOPSISHistoryPanel
        :model-id="selectedHistoryModelId"
        :step-id="selectedHistoryStepId"
      />
    </el-dialog>

    <!-- 回滚确认对话框 -->
    <el-dialog
      v-model="dialogVisible.rollback"
      title="回滚配置确认"
      width="50%"
    >
      <el-alert
        title="确认回滚"
        type="warning"
        description="此操作将丢弃所有未保存的更改，恢复到上次保存的配置状态。"
        show-icon
        :closable="false"
      />
      
      <div class="rollback-info">
        <h4>当前未保存的更改：</h4>
        <div class="change-summary">
          <p>指标数量：{{ originalIndicators.length }} → {{ selectedIndicators.length }}</p>
          <div v-if="selectedIndicators.length !== originalIndicators.length" class="indicator-changes">
            <div v-if="selectedIndicators.length > originalIndicators.length" class="added-indicators">
              <span class="change-label">新增指标：</span>
              <el-tag
                v-for="indicator in selectedIndicators.filter(i => !originalIndicators.includes(i))"
                :key="indicator"
                type="success"
                size="small"
                class="change-tag"
              >
                + {{ indicator }}
              </el-tag>
            </div>
            <div v-if="selectedIndicators.length < originalIndicators.length" class="removed-indicators">
              <span class="change-label">移除指标：</span>
              <el-tag
                v-for="indicator in originalIndicators.filter(i => !selectedIndicators.includes(i))"
                :key="indicator"
                type="danger"
                size="small"
                class="change-tag"
              >
                - {{ indicator }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="cancelRollback">取消</el-button>
        <el-button type="danger" @click="confirmRollback">
          确认回滚
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Setting,
  View,
  Clock,
  ArrowUp,
  ArrowDown,
  Delete,
  RefreshLeft,
  Check,
  Play,
  SuccessFilled,
  CircleCloseFilled,
  InfoFilled
} from '@element-plus/icons-vue'
import { topsisConfigApi, modelManagementApi } from '@/api'
import TOPSISTestPanel from '@/components/topsis/TOPSISTestPanel.vue'
import TOPSISPreviewPanel from '@/components/topsis/TOPSISPreviewPanel.vue'
import TOPSISHistoryPanel from '@/components/topsis/TOPSISHistoryPanel.vue'

// 响应式数据
const activeTab = ref('config')
const configList = ref<any[]>([])
const modelList = ref<any[]>([])
const topsisSteps = ref<any[]>([])
const availableIndicators = ref<any[]>([])
const selectedIndicators = ref<string[]>([])
const originalIndicators = ref<string[]>([]) // 用于回滚
const configSearch = ref('')
const indicatorSearch = ref('')
const selectedModelId = ref<number | null>(null)
const selectedStepId = ref<number | null>(null)
const selectedHistoryModelId = ref<number | null>(null)
const selectedHistoryStepId = ref<number | null>(null)
const hasUnsavedChanges = ref(false)

const loading = reactive({
  configs: false,
  indicators: false,
  preview: false,
  save: false,
  autoSave: false,
  validation: false,
  testing: false
})

const dialogVisible = reactive({
  test: false,
  preview: false,
  history: false,
  rollback: false
})

const selectedTestConfig = ref<any>(null)
const validationStatus = ref<any>({
  isValid: false,
  errors: [],
  warnings: [],
  lastValidated: null
})

// 计算属性
const filteredAvailableIndicators = computed(() => {
  if (!indicatorSearch.value) {
    return availableIndicators.value
  }
  return availableIndicators.value.filter(indicator =>
    indicator.name.toLowerCase().includes(indicatorSearch.value.toLowerCase())
  )
})

// 检查是否有未保存的更改
const checkUnsavedChanges = () => {
  hasUnsavedChanges.value = JSON.stringify(selectedIndicators.value) !== JSON.stringify(originalIndicators.value)
}

// 自动保存定时器
let autoSaveTimer: NodeJS.Timeout | null = null

// 监听指标变化以检测未保存的更改和触发验证
watch(selectedIndicators, () => {
  checkUnsavedChanges()
  validateConfigurationRealtime()
  
  // 设置自动保存定时器（5秒后自动保存）
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
  
  if (hasUnsavedChanges.value) {
    autoSaveTimer = setTimeout(() => {
      autoSaveConfiguration()
    }, 5000)
  }
}, { deep: true })

// 方法
const getAlgorithmTypeTag = (algorithmCode: string) => {
  const typeMap = {
    'TOPSIS_POSITIVE': 'success',
    'TOPSIS_NEGATIVE': 'warning',
    'TOPSIS': 'primary'
  }
  return typeMap[algorithmCode] || 'info'
}

const getAlgorithmTypeName = (algorithmCode: string) => {
  const nameMap = {
    'TOPSIS_POSITIVE': '正理想解',
    'TOPSIS_NEGATIVE': '负理想解',
    'TOPSIS': 'TOPSIS算法'
  }
  return nameMap[algorithmCode] || algorithmCode
}

// 获取TOPSIS配置列表
const getTOPSISConfigs = async () => {
  loading.configs = true
  try {
    const response = await topsisConfigApi.getAll()
    if (response.success) {
      configList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取TOPSIS配置失败')
    }
  } catch (error) {
    console.error('获取TOPSIS配置失败:', error)
    ElMessage.error('获取TOPSIS配置失败')
    // 使用模拟数据作为后备
    configList.value = [
      {
        id: 1,
        modelId: 1,
        modelName: '乡镇减灾能力评估模型',
        stepId: 4,
        stepCode: 'STEP4',
        stepName: '优劣解算',
        algorithmCode: 'TOPSIS_POSITIVE',
        indicators: ['indicator1', 'indicator2', 'indicator3'],
        isConfigured: true
      },
      {
        id: 2,
        modelId: 2,
        modelName: '社区-行政村减灾能力评估模型',
        stepId: 4,
        stepCode: 'STEP4',
        stepName: '优劣解算',
        algorithmCode: 'TOPSIS_POSITIVE',
        indicators: [],
        isConfigured: false
      }
    ]
  } finally {
    loading.configs = false
  }
}

// 搜索配置
const searchConfigs = () => {
  // 实现搜索逻辑
  getTOPSISConfigs()
}

// 刷新配置
const refreshConfigs = () => {
  configSearch.value = ''
  getTOPSISConfigs()
}

// 配置指标
const configureIndicators = (config: any) => {
  selectedModelId.value = config.modelId
  selectedStepId.value = config.stepId
  selectedIndicators.value = [...(config.indicators || [])]
  activeTab.value = 'indicators'
  loadModelSteps()
  loadStepConfiguration()
}

// 测试配置
const testConfiguration = (config: any) => {
  selectedTestConfig.value = config
  dialogVisible.test = true
}

// 查看历史记录
const viewHistory = (config: any) => {
  selectedHistoryModelId.value = config.modelId
  selectedHistoryStepId.value = config.stepId
  dialogVisible.history = true
}

// 加载模型步骤
const loadModelSteps = async () => {
  if (!selectedModelId.value) return
  
  try {
    const response = await modelManagementApi.getModelSteps(selectedModelId.value)
    if (response.success) {
      // 过滤出TOPSIS相关步骤
      topsisSteps.value = (response.data || []).filter((step: any) => 
        step.algorithmCode && step.algorithmCode.includes('TOPSIS')
      )
    } else {
      ElMessage.error(response.message || '获取模型步骤失败')
    }
  } catch (error) {
    console.error('获取模型步骤失败:', error)
    ElMessage.error('获取模型步骤失败')
    // 使用模拟数据作为后备
    topsisSteps.value = [
      {
        id: 4,
        stepCode: 'STEP4',
        stepName: '优劣解算',
        algorithmCode: 'TOPSIS_POSITIVE'
      }
    ]
  }
}

// 加载步骤配置
const loadStepConfiguration = async () => {
  if (!selectedModelId.value || !selectedStepId.value) return
  
  try {
    const response = await topsisConfigApi.getByModelAndStep(selectedModelId.value, selectedStepId.value)
    if (response.success && response.data) {
      selectedIndicators.value = [...(response.data.indicators || [])]
      originalIndicators.value = [...(response.data.indicators || [])]
      hasUnsavedChanges.value = false
    }
  } catch (error) {
    console.error('获取步骤配置失败:', error)
    ElMessage.error('获取步骤配置失败')
  }
}

// 加载可用指标
const loadAvailableIndicators = async () => {
  if (!selectedModelId.value) return
  
  loading.indicators = true
  try {
    const response = await topsisConfigApi.getAvailableIndicators(selectedModelId.value)
    if (response.success) {
      availableIndicators.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取可用指标失败')
    }
  } catch (error) {
    console.error('获取可用指标失败:', error)
    ElMessage.error('获取可用指标失败')
    // 使用模拟数据作为后备
    availableIndicators.value = [
      { name: 'indicator1', dataType: 'NUMERIC', sampleCount: 100 },
      { name: 'indicator2', dataType: 'NUMERIC', sampleCount: 95 },
      { name: 'indicator3', dataType: 'NUMERIC', sampleCount: 98 },
      { name: 'indicator4', dataType: 'NUMERIC', sampleCount: 102 },
      { name: 'indicator5', dataType: 'NUMERIC', sampleCount: 88 }
    ]
  } finally {
    loading.indicators = false
  }
}

// 添加指标
const addIndicator = (indicator: any) => {
  if (!selectedIndicators.value.includes(indicator.name)) {
    selectedIndicators.value.push(indicator.name)
  }
}

// 移除指标
const removeIndicator = (index: number) => {
  selectedIndicators.value.splice(index, 1)
}

// 添加所有指标
const addAllIndicators = () => {
  availableIndicators.value.forEach(indicator => {
    if (!selectedIndicators.value.includes(indicator.name)) {
      selectedIndicators.value.push(indicator.name)
    }
  })
}

// 移除所有指标
const removeAllIndicators = () => {
  selectedIndicators.value = []
}

// 上移指标
const moveIndicatorUp = (index: number) => {
  if (index > 0) {
    const temp = selectedIndicators.value[index]
    selectedIndicators.value[index] = selectedIndicators.value[index - 1]
    selectedIndicators.value[index - 1] = temp
  }
}

// 下移指标
const moveIndicatorDown = (index: number) => {
  if (index < selectedIndicators.value.length - 1) {
    const temp = selectedIndicators.value[index]
    selectedIndicators.value[index] = selectedIndicators.value[index + 1]
    selectedIndicators.value[index + 1] = temp
  }
}

// 保存配置
const saveConfiguration = async (showMessage = true) => {
  if (!selectedStepId.value || selectedIndicators.value.length === 0) {
    if (showMessage) {
      ElMessage.warning('请选择步骤并配置至少一个指标')
    }
    return false
  }
  
  loading.save = true
  try {
    const response = await topsisConfigApi.updateStepConfig({
      stepId: selectedStepId.value,
      indicators: selectedIndicators.value
    })
    
    if (response.success) {
      originalIndicators.value = [...selectedIndicators.value]
      hasUnsavedChanges.value = false
      if (showMessage) {
        ElMessage.success('配置保存成功')
      }
      getTOPSISConfigs() // 刷新配置列表
      return true
    } else {
      if (showMessage) {
        ElMessage.error(response.message || '保存配置失败')
      }
      return false
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    if (showMessage) {
      ElMessage.error('保存配置失败')
    }
    return false
  } finally {
    loading.save = false
  }
}

// 自动保存配置
const autoSaveConfiguration = async () => {
  if (!hasUnsavedChanges.value || loading.autoSave) return
  
  loading.autoSave = true
  try {
    await saveConfiguration(false) // 不显示成功消息
  } catch (error) {
    console.error('自动保存失败:', error)
  } finally {
    loading.autoSave = false
  }
}

// 回滚配置
const rollbackConfiguration = () => {
  if (!hasUnsavedChanges.value) {
    ElMessage.info('没有需要回滚的更改')
    return
  }
  
  dialogVisible.rollback = true
}

// 确认回滚
const confirmRollback = () => {
  selectedIndicators.value = [...originalIndicators.value]
  hasUnsavedChanges.value = false
  dialogVisible.rollback = false
  ElMessage.success('配置已回滚到上次保存的状态')
}

// 取消回滚
const cancelRollback = () => {
  dialogVisible.rollback = false
}

// 实时验证配置
const validateConfigurationRealtime = async () => {
  if (!selectedModelId.value || !selectedStepId.value || selectedIndicators.value.length === 0) {
    validationStatus.value = {
      isValid: false,
      errors: [],
      warnings: [],
      lastValidated: null
    }
    return
  }
  
  loading.validation = true
  try {
    const response = await topsisConfigApi.validateConfig({
      modelId: selectedModelId.value,
      stepId: selectedStepId.value,
      indicators: selectedIndicators.value
    })
    
    if (response.success) {
      validationStatus.value = {
        isValid: response.data.isValid,
        errors: response.data.errors || [],
        warnings: response.data.warnings || [],
        lastValidated: new Date().toLocaleString()
      }
    }
  } catch (error) {
    console.error('实时验证失败:', error)
    validationStatus.value = {
      isValid: false,
      errors: ['验证服务不可用'],
      warnings: [],
      lastValidated: new Date().toLocaleString()
    }
  } finally {
    loading.validation = false
  }
}

// 手动验证配置
const validateConfiguration = async () => {
  if (!selectedModelId.value || !selectedStepId.value) {
    ElMessage.warning('请选择模型和步骤')
    return
  }
  
  if (selectedIndicators.value.length === 0) {
    ElMessage.warning('请至少选择一个指标')
    return
  }
  
  loading.validation = true
  try {
    const response = await topsisConfigApi.validateConfig({
      modelId: selectedModelId.value,
      stepId: selectedStepId.value,
      indicators: selectedIndicators.value
    })
    
    if (response.success) {
      validationStatus.value = {
        isValid: response.data.isValid,
        errors: response.data.errors || [],
        warnings: response.data.warnings || [],
        lastValidated: new Date().toLocaleString()
      }
      
      if (response.data.isValid) {
        ElMessage.success('配置验证通过')
      } else {
        ElMessage.error(`配置验证失败：${response.data.errors.join(', ')}`)
      }
    } else {
      ElMessage.error(response.message || '验证失败')
    }
  } catch (error) {
    console.error('配置验证失败:', error)
    ElMessage.error('配置验证失败')
  } finally {
    loading.validation = false
  }
}

// 快速测试配置
const quickTestConfiguration = async () => {
  if (!selectedModelId.value || !selectedStepId.value || selectedIndicators.value.length === 0) {
    ElMessage.warning('请完善配置信息')
    return
  }
  
  loading.testing = true
  try {
    const response = await topsisConfigApi.testConfig({
      config: {
        modelId: selectedModelId.value,
        stepId: selectedStepId.value,
        indicators: selectedIndicators.value
      },
      regionCodes: ['TEST_001', 'TEST_002'], // 使用测试区域
      weightConfigId: 1 // 使用默认权重配置
    })
    
    if (response.success) {
      ElMessage.success('配置测试通过')
      // 显示测试结果摘要
      const metrics = response.data.metrics
      ElMessage.info(`测试结果：处理${metrics.processedRegions}个区域，使用${metrics.usedIndicators}个指标，耗时${metrics.calculationTime}ms`)
    } else {
      ElMessage.error(response.message || '配置测试失败')
    }
  } catch (error) {
    console.error('配置测试失败:', error)
    ElMessage.error('配置测试失败')
  } finally {
    loading.testing = false
  }
}

// 预览配置
const previewConfiguration = () => {
  if (!selectedStepId.value || selectedIndicators.value.length === 0) {
    ElMessage.warning('请选择步骤并配置至少一个指标')
    return
  }
  
  dialogVisible.preview = true
}

// 重置测试
const resetTest = () => {
  selectedTestConfig.value = null
}

// 重置预览
const resetPreview = () => {
  // 预览重置逻辑
}

// 重置历史记录
const resetHistory = () => {
  selectedHistoryModelId.value = null
  selectedHistoryStepId.value = null
}

// 测试完成回调
const onTestCompleted = (results: any) => {
  ElMessage.success('测试完成')
  console.log('测试结果:', results)
}

// 获取模型列表
const getModelList = async () => {
  try {
    const response = await modelManagementApi.getAllModels()
    if (response.success) {
      modelList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取模型列表失败')
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
    ElMessage.error('获取模型列表失败')
    // 使用模拟数据作为后备
    modelList.value = [
      { id: 1, modelName: '乡镇减灾能力评估模型' },
      { id: 2, modelName: '社区-行政村减灾能力评估模型' }
    ]
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getTOPSISConfigs()
  getModelList()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
})
</script>

<style scoped>
.topsis-config {
  max-width: 1400px;
  margin: 0 auto;
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

.config-tabs {
  margin-top: 16px;
}

.toolbar-card,
.indicator-toolbar {
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.config-list {
  min-height: 600px;
}

.indicators-display {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.indicator-tag {
  margin: 2px;
}

.no-indicators {
  color: #909399;
  font-style: italic;
}

.indicator-selector {
  margin-top: 16px;
}

.selector-header {
  margin-bottom: 20px;
}

.selector-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.selector-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.indicator-panel {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 400px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid #dcdfe6;
  background-color: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h4 {
  margin: 0;
  color: #303133;
  font-size: 14px;
}

.indicator-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.indicator-item {
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.indicator-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.indicator-item.selected {
  background-color: #e1f3d8;
  border-color: #67c23a;
}

.indicator-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.indicator-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.indicator-type {
  font-size: 12px;
  color: #909399;
}

.indicator-order {
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

.indicator-meta {
  display: flex;
  gap: 4px;
}

.indicator-actions {
  display: flex;
  gap: 4px;
}

.operation-buttons {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 400px;
  gap: 16px;
}

.unsaved-badge {
  margin-left: 8px;
}

.save-actions {
  display: flex;
  gap: 8px;
}

.rollback-info {
  margin-top: 20px;
}

.rollback-info h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.change-summary {
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border-left: 4px solid #e6a23c;
}

.change-summary p {
  margin: 0 0 8px 0;
  font-weight: 500;
}

.indicator-changes {
  margin-top: 12px;
}

.added-indicators,
.removed-indicators {
  margin-bottom: 8px;
}

.change-label {
  font-weight: 500;
  margin-right: 8px;
}

.change-tag {
  margin: 2px;
}

.validation-status {
  margin-bottom: 16px;
}

.validation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.validation-actions {
  display: flex;
  gap: 8px;
}

.validation-content {
  padding: 16px 0;
}

.validation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.success-icon {
  color: #67c23a;
}

.error-icon {
  color: #f56c6c;
}

.info-icon {
  color: #409eff;
}

.validation-text {
  color: #303133;
  font-size: 14px;
}

.validation-messages {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.error-messages,
.warning-messages {
  margin-bottom: 12px;
}

.error-messages h5 {
  color: #f56c6c;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.warning-messages h5 {
  color: #e6a23c;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.error-item {
  color: #f56c6c;
  margin-bottom: 4px;
}

.warning-item {
  color: #e6a23c;
  margin-bottom: 4px;
}
</style>