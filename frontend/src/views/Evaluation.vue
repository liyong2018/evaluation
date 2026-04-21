<template>
  <div class="evaluation">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>评估计算</h1>
      <p>配置评估参数并执行减灾能力评估计算</p>
    </div>

    <!-- 评估配置 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>评估配置</span>
          <el-button type="text" @click="resetEvaluationForm">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </template>
      
      <el-form
        ref="evaluationFormRef"
        :model="evaluationForm"
        :rules="evaluationRules"
        label-width="120px"
        class="evaluation-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评估名称" prop="name">
              <el-input
                v-model="evaluationForm.name"
                placeholder="请输入评估名称"
                @blur="generateEvaluationName"
              />
              <el-button type="text" @click="generateEvaluationName" size="small">
                自动生成
              </el-button>
            </el-form-item>

           
          </el-col>
          <el-col :span="12">
            <el-form-item label="评估年份" prop="year">
              <el-select v-model="evaluationForm.year" placeholder="选择评估年份">
                <el-option
                  v-for="y in years"
                  :key="y"
                  :label="String(y)"
                  :value="y"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评估模型" prop="modelId">
              <el-select v-model="evaluationForm.modelId" placeholder="选择评估模型" @change="handleModelChange">
                <el-option
                  v-for="model in evaluationModels"
                  :key="model.id"
                  :label="model.modelName"
                  :value="model.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择行政区划" prop="orgCode">
              <el-tree-select
                v-model="evaluationForm.orgCode"
                :data="regionTreeData"
                placeholder="请选择行政区划"
                clearable
                filterable
                check-strictly
                :render-after-expand="false"
                node-key="code"
                :props="{
                  value: 'code',
                  label: 'name',
                  children: 'children'
                }"
                @change="handleRegionTreeChange"
                style="width: 100%"
              >
                <template #default="{ data }">
                  <span>{{ data.name }} <span style="color: #909399; font-size: 12px;">({{ data.code }})</span></span>
                </template>
              </el-tree-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="描述">
          <el-input
            v-model="evaluationForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入评估描述"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="startEvaluation" :loading="loading.evaluation" :disabled="!evaluationForm.modelId">
            <el-icon><VideoPlay /></el-icon>
            开始评估
          </el-button>
          <el-button
            type="warning"
            @click="startAsyncEvaluation"
            :loading="loading.evaluation"
            :disabled="!evaluationForm.modelId"
          >
            <el-icon><Clock /></el-icon>
            异步开始评估
          </el-button>
          <!-- <el-button type="success" @click="validateParameters">
            <el-icon><Check /></el-icon>
            验证参数
          </el-button>
          <el-button type="info" @click="handlePreviewData">
            <el-icon><View /></el-icon>
            预览数据
          </el-button> -->
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评估进度 -->
    <el-card v-if="evaluationProgress.visible" class="progress-card">
      <template #header>
        <span>评估进度</span>
      </template>
      <div class="progress-content">
        <el-progress
          :percentage="evaluationProgress.percentage"
          :status="evaluationProgress.status"
          :stroke-width="20"
        />
        <div class="progress-info">
          <p>{{ evaluationProgress.message }}</p>
          <p v-if="evaluationProgress.detail">{{ evaluationProgress.detail }}</p>
        </div>
        <div class="progress-actions" v-if="evaluationProgress.status === 'success'">
          <el-button type="primary" @click="viewResults">
            <el-icon><View /></el-icon>
            查看结果
          </el-button>
          <el-button type="success" @click="downloadReport">
            <el-icon><Download /></el-icon>
            下载报告
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 评估历史 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>评估历史</span>
          <el-button type="text" @click="refreshHistory">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 筛选条件（使用评估配置表单的值） -->
      <div class="history-filters">
        <el-row :gutter="16" style="margin-bottom: 16px;">
          <el-col :span="8">
            <el-select
              v-model="filterForm.status"
              placeholder="执行状态（默认全部）"
              clearable
              @change="handleFilterChange"
              style="width: 100%;"
            >
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAILED" />
              <el-option label="进行中" value="RUNNING" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-button type="primary" @click="showAllHistory">
              <el-icon><List /></el-icon>
              显示全部历史
            </el-button>
            <el-button @click="refreshHistory" style="margin-left: 8px;">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </el-col>
          <el-col :span="8" style="text-align: right; color: #666; font-size: 14px;">
            <span v-if="evaluationForm.year">筛选条件：{{ evaluationForm.year }}年</span>
            <span v-if="evaluationForm.orgCode" style="margin-left: 8px;">{{ getCountyName(evaluationForm.orgCode) }}</span>
            <span v-if="evaluationForm.modelId" style="margin-left: 8px;">模型ID：{{ evaluationForm.modelId }}</span>
          </el-col>
        </el-row>
      </div>

      <el-table
        v-loading="loading.history"
        :data="evaluationHistory.records || []"
        stripe
        border
      >
        <el-table-column label="评估名称" min-width="200">
          <template #default="{ row }">
            {{ generateHistoryEvaluationName(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="executionCode" label="执行编号" width="180" />
        <el-table-column label="状态" width="180">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.executionStatus)">
              {{ getStatusText(row.executionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="startTime" label="开始时间" width="200">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="200">
          <template #default="{ row }">
            {{ formatDate(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="执行人" width="200">
          <template #default="{ row }">
            {{ row.createBy || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewEvaluationDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteEvaluationHistory(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="evaluationHistory.total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="evaluationHistory.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 数据预览对话框 -->
    <el-dialog v-model="dialogVisible.preview" title="数据预览" width="80%">
      <el-table :data="previewData" stripe border max-height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="地区名称" width="150" />
        <el-table-column prop="regionName" label="区域" width="120" />
        <el-table-column prop="population" label="人口" width="120" />
        <el-table-column prop="area" label="面积" width="120" />
        <el-table-column prop="gdp" label="GDP" width="120" />
      </el-table>
      <template #footer>
        <el-button @click="dialogVisible.preview = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评估结果详情对话框 -->
    <el-dialog v-model="dialogVisible.evaluationDetail" :title="`评估结果详情 - ${currentExecutionRecord?.executionCode || ''}`" width="1920px" top="5vh">
      <div v-loading="loading.evaluationDetail" style="min-height: 400px;">
        <!-- 执行记录信息 -->
        <el-card v-if="currentExecutionRecord" class="record-info-card" style="margin-bottom: 20px;">
          <el-descriptions title="执行信息" :column="3" border>
            <el-descriptions-item label="执行编号">{{ currentExecutionRecord.executionCode }}</el-descriptions-item>
            <el-descriptions-item label="执行状态">
              <el-tag :type="getStatusType(currentExecutionRecord.executionStatus)">
                {{ getStatusText(currentExecutionRecord.executionStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="模型ID">{{ currentExecutionRecord.modelId }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDate(currentExecutionRecord.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDate(currentExecutionRecord.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="年份">{{ currentExecutionRecord.year || '-' }}</el-descriptions-item>
            <el-descriptions-item label="结果摘要" :span="3">{{ currentExecutionRecord.resultSummary }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 评估结果列表 -->
        <el-table
          v-if="evaluationResults.length > 0"
          :data="evaluationResults"
          stripe
          border
          style="width: 100%;"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="regionCode" label="地区代码" width="120" />
          <el-table-column prop="regionName" label="地区名称" min-width="150" />
          <el-table-column label="灾害管理能力" align="center" min-width="200">
            <el-table-column prop="managementCapabilityScore" label="得分" />
            <el-table-column prop="managementCapabilityLevel" label="等级" />
          </el-table-column>
          <el-table-column label="灾害备灾能力" align="center" min-width="200">
            <el-table-column prop="supportCapabilityScore" label="得分" />
            <el-table-column prop="supportCapabilityLevel" label="等级" />
          </el-table-column>
          <el-table-column label="自救转移能力" align="center" min-width="200">
            <el-table-column prop="selfRescueCapabilityScore" label="得分" />
            <el-table-column prop="selfRescueCapabilityLevel" label="等级" />
          </el-table-column>
          <el-table-column prop="comprehensiveCapabilityScore" label="综合能力得分" min-width="130" />
          <el-table-column prop="comprehensiveCapabilityLevel" label="综合能力等级" min-width="130" />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-else description="暂无评估结果数据" :image-size="100" />

        <!-- 统计信息 -->
        <div v-if="evaluationResults.length > 0" class="statistics-info">
          <el-alert
            :title="`共 ${evaluationResults.length} 条评估结果`"
            type="info"
            :closable="false"
            show-icon
            style="margin-top: 20px;"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible.evaluationDetail = false">关闭</el-button>
        <el-button v-if="evaluationResults.length > 0" type="primary" @click="exportEvaluationResults">
          <el-icon><Download /></el-icon>
          导出结果
        </el-button>
      </template>
    </el-dialog>

    <!-- 计算结果弹窗 -->
    <ResultDialog
      v-model="resultDialogVisible"
      :step-info="currentStepInfo"
      :result-data="currentCalculationResult"
      :formula="currentStepInfo?.formula"
      :evaluation-name="evaluationForm.name"
      :model-id="evaluationForm.modelId"
      @export="handleExportResult"
    />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'AppEvaluation' })
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  Refresh,
  VideoPlay,
  Clock,
  Check,
  View,
  Download,
  Delete,
  Document,
  Filter,
  DataAnalysis,
  List
} from '@element-plus/icons-vue'
import { evaluationApi, surveyDataApi, modelManagementApi, communityCapacityApi, regionDataApi, organizationApi } from '@/api'
import ResultDialog from '@/components/ResultDialog.vue'
import { useUserStore } from '@/stores/user'
import { useGlobalYearStore } from '@/stores/globalYear'
import { useGlobalOrganizationStore } from '@/stores/globalOrganization'

// 处理ResizeObserver警告
const originalError = console.error
console.error = (...args) => {
  if (args[0]?.includes?.('ResizeObserver loop completed with undelivered notifications')) {
    return
  }
  originalError(...args)
}

const router = useRouter()
const userStore = useUserStore()
const globalYearStore = useGlobalYearStore()
const globalOrganizationStore = useGlobalOrganizationStore()

const selectedModel = computed(() => {
  return evaluationModels.value.find(model => model.id === evaluationForm.modelId)
})

const selectedCounty = computed(() => {
  return counties.value.find(county => county.code === evaluationForm.selectedCounty)
})

const isAdministrativeCode = (value: unknown) => {
  return /^\d{6,}$/.test(String(value || '').trim())
}

const findRegionNodeByCode = (nodes: any[], code: string): any => {
  for (const node of nodes) {
    if (node.code === code) return node
    if (node.children) {
      const found = findRegionNodeByCode(node.children, code)
      if (found) return found
    }
  }
  return null
}

const buildCountyFullName = (provinceName: string, cityName: string, countyName: string) => {
  const parts: string[] = []
  const normalizedProvinceName = String(provinceName || '').trim()
  const normalizedCityName = String(cityName || '').trim()
  const normalizedCountyName = String(countyName || '').trim()

  if (normalizedProvinceName && !isAdministrativeCode(normalizedProvinceName)) {
    parts.push(normalizedProvinceName)
  }
  if (
    normalizedCityName &&
    !isAdministrativeCode(normalizedCityName) &&
    !parts.some(part => normalizedCityName.includes(part) || part.includes(normalizedCityName))
  ) {
    parts.push(normalizedCityName)
  }
  if (
    normalizedCountyName &&
    !isAdministrativeCode(normalizedCountyName) &&
    !parts.some(part => normalizedCountyName.includes(part) || part.includes(normalizedCountyName))
  ) {
    parts.push(normalizedCountyName)
  }

  return parts.join('')
}

const getSelectedCountyDisplayName = () => {
  const selectedNode = evaluationForm.orgCode
    ? findRegionNodeByCode(regionTreeData.value, evaluationForm.orgCode)
    : null
  const storedOrg = globalOrganizationStore.selectedOrganization

  const rawCountyNameCandidates = [
    selectedNode?.level === 3 ? (selectedNode.countyName || selectedNode.name) : '',
    storedOrg?.level === 3 ? (storedOrg.countyName || storedOrg.name) : '',
    selectedCounty.value?.name || ''
  ]

  const countyName = rawCountyNameCandidates.find(name => name && !isAdministrativeCode(name)) || ''
  const provinceName = selectedNode?.provinceName || storedOrg?.provinceName || evaluationForm.selectedProvince || ''
  const cityName = selectedNode?.cityName || storedOrg?.cityName || evaluationForm.selectedCity || ''
  return buildCountyFullName(provinceName, cityName, countyName)
}

// 生成评估名称函数
const generateEvaluationName = () => {
  const year = evaluationForm.year || new Date().getFullYear()
  const model = selectedModel.value
  const countyDisplayName = getSelectedCountyDisplayName()

  let name = `${year}年`

  if (countyDisplayName) {
    name += countyDisplayName
  }

  if (model?.modelName) {
    name += `${model.modelName}评估`
  } else {
    name += '减灾能力评估'
  }

  evaluationForm.name = name
}

// 响应式数据
const evaluationFormRef = ref<FormInstance>()
const evaluationModels = ref<any[]>([])
const regionTreeData = ref<any[]>([])
const evaluationHistory = ref<any>({ records: [], total: 0, current: 1, size: 10, pages: 0 })
const previewData = ref<any[]>([])

// 筛选表单（仅状态需要独立选择，其他使用评估配置表单的值）
const filterForm = reactive({
  status: '',
  year: '',
  county: '',
  modelId: null as number | null
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 三级联动数据
const provinces = ref<any[]>([])
const cities = ref<any[]>([])
const counties = ref<any[]>([])

const loading = reactive({
  evaluation: false,
  history: false,
  evaluationDetail: false
})

const dialogVisible = reactive({
  preview: false,
  evaluationDetail: false
})

// 评估结果详情相关数据
const currentExecutionRecord = ref<any>(null)
const evaluationResults = ref<any[]>([])

// 计算结果弹窗相关数据
const resultDialogVisible = ref(false)
const currentStepInfo = ref<any>(null)
const currentCalculationResult = ref<any>(null)

const currentYear = new Date().getFullYear()
const years = ref<number[]>(Array.from({ length: currentYear - 2020 + 1 }, (_, i) => currentYear - i))
const GOVERNMENT_MODEL_KEYWORD = '政府减灾能力'
const ENTERPRISE_MODEL_KEYWORD = '企业减灾能力'
const SOCIAL_ORGANIZATION_MODEL_KEYWORD = '社会组织减灾能力'
const FAMILY_MODEL_KEYWORD = '家庭减灾能力'

const evaluationForm = reactive<any>({
  name: '',
  modelId: null,
  weightConfigId: undefined as number | undefined,
  year: globalYearStore.selectedYear,
  dataType: 'township', // 默认选择乡镇数据
  dataSource: 'REGION',
  regions: [] as string[],
  orgCode: '', // 机构代码
  // 三级联动数据
  selectedProvince: '',
  selectedCity: '',
  selectedCounty: '',
  countyData: [], // 选定县的数据
  parameters: {
    // AHP参数
    crThreshold: 0.1,
    maxIterations: 100,
    // 模糊评价参数
    fuzzyMethod: 'TRIANGULAR',
    operator: 'WEIGHTED_AVERAGE',
    // 灰色关联参数
    resolution: 0.5,
    // 熵权法参数
    normalization: 'MIN_MAX'
  },
  description: ''
})

const findGovernmentModel = (year?: number | string) => {
  const yearText = String(year || '').trim()
  const candidates = evaluationModels.value.filter((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(GOVERNMENT_MODEL_KEYWORD)
  })

  if (!yearText) {
    return candidates[0] || null
  }

  const exactMatch = candidates.find((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(`（${yearText}）`) || modelName.includes(`(${yearText})`) || modelName.includes(yearText)
  })

  return exactMatch || candidates[0] || null
}

const findFamilyModel = (year?: number | string) => {
  const yearText = String(year || '').trim()
  const candidates = evaluationModels.value.filter((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(FAMILY_MODEL_KEYWORD)
  })

  if (!yearText) {
    return candidates[0] || null
  }

  const exactMatch = candidates.find((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(`（${yearText}）`) || modelName.includes(`(${yearText})`) || modelName.includes(yearText)
  })

  return exactMatch || candidates[0] || null
}

const findEnterpriseModel = (year?: number | string) => {
  const yearText = String(year || '').trim()
  const candidates = evaluationModels.value.filter((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(ENTERPRISE_MODEL_KEYWORD)
  })

  if (!yearText) {
    return candidates[0] || null
  }

  const exactMatch = candidates.find((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(`（${yearText}）`) || modelName.includes(`(${yearText})`) || modelName.includes(yearText)
  })

  return exactMatch || candidates[0] || null
}

const findSocialOrganizationModel = (year?: number | string) => {
  const yearText = String(year || '').trim()
  const candidates = evaluationModels.value.filter((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(SOCIAL_ORGANIZATION_MODEL_KEYWORD)
  })

  if (!yearText) {
    return candidates[0] || null
  }

  const exactMatch = candidates.find((model: any) => {
    const modelName = String(model?.modelName || '')
    return modelName.includes(`（${yearText}）`) || modelName.includes(`(${yearText})`) || modelName.includes(yearText)
  })

  return exactMatch || candidates[0] || null
}

const evaluationProgress = reactive({
  visible: false,
  percentage: 0,
  status: 'success' as 'success' | 'exception' | 'warning',
  message: '',
  detail: ''
})

const evaluationRules = {
  name: [{ required: true, message: '请输入评估名称', trigger: 'blur' }],
  year: [{ required: true, message: '请选择评估年份', trigger: 'change' }]
}

// 移除权重配置获取逻辑，年份下拉改为最近六年

// 获取地区树形数据
const getRegionTreeData = async () => {
  try {
    const response: any = await organizationApi.getTree({
      maxLevel: 3,
      year: evaluationForm.year || undefined
    })

    const ok = response?.success === true || response?.code === 200
    if (!ok) {
      ElMessage.error(response?.message || '获取行政区划数据失败')
      return
    }

    regionTreeData.value = response?.data || []
  } catch (error) {
    console.error('获取行政区划数据失败:', error)
    ElMessage.error('获取行政区划数据失败')
  }
}

// 获取评估模型列表
const getEvaluationModels = async () => {
  try {
    const response = await modelManagementApi.getAllModels()
    if (response.success) {
      // 只显示启用的模型
      evaluationModels.value = (response.data || []).filter((model: any) => model.status === 1)
    } else {
      ElMessage.error(response.message || '获取评估模型失败')
    }
  } catch (error) {
    console.error('获取评估模型失败:', error)
    ElMessage.error('获取评估模型失败')
  }
}

// 处理模型变化
const handleModelChange = async (modelId: number) => {
  console.log('模型变化:', modelId)

  // 保存当前选择的组织机构信息
  const storedOrg = globalOrganizationStore.selectedOrganization
  const selectedModel = evaluationModels.value.find((model: any) => model.id === modelId)
  const selectedModelName = String(selectedModel?.modelName || '')
  const isCityLevel = Number(storedOrg?.level) === 2

  // 根据模型ID自动切换数据类型
  if (selectedModelName.includes(GOVERNMENT_MODEL_KEYWORD)) {
    evaluationForm.dataType = 'township'
    evaluationForm.dataSource = 'REGION'
    if (isCityLevel) {
      globalOrganizationStore.setPreferredCapacityModel('government')
    }
    console.log('自动切换到政府减灾能力数据源')
  } else if (selectedModelName.includes(ENTERPRISE_MODEL_KEYWORD)) {
    evaluationForm.dataType = 'township'
    evaluationForm.dataSource = 'REGION'
    if (isCityLevel) {
      globalOrganizationStore.setPreferredCapacityModel('enterprise')
    }
    console.log('自动切换到企业减灾能力数据源')
  } else if (selectedModelName.includes(SOCIAL_ORGANIZATION_MODEL_KEYWORD)) {
    evaluationForm.dataType = 'township'
    evaluationForm.dataSource = 'REGION'
    if (isCityLevel) {
      globalOrganizationStore.setPreferredCapacityModel('social-organization')
    }
    console.log('自动切换到社会组织减灾能力数据源')
  } else if (selectedModelName.includes(FAMILY_MODEL_KEYWORD)) {
    evaluationForm.dataType = 'family'
    evaluationForm.dataSource = 'REGION'
    if (isCityLevel) {
      globalOrganizationStore.setPreferredCapacityModel('family')
    }
    console.log('自动切换到家庭减灾能力数据源')
  } else if (modelId === 3 || modelId === 11 || modelId === 19) {
    // 乡镇模型：乡镇减灾能力TOPSIS评估模型(3) 或 综合减灾能力评估模型(11) 或 乡镇（街道）减灾能力（区县单元）评估模型(19)
    evaluationForm.dataType = 'township'
    console.log('自动切换到乡镇数据类型')
  } else if (modelId === 4 || modelId === 8 || modelId === 17) {
    // 社区模型：社区减灾能力TOPSIS评估模型(4) 或 社区-乡镇减灾能力评估模型(8) 或 社区-区县减灾能力评估模型(17)
    evaluationForm.dataType = 'community'
    console.log('自动切换到社区数据类型')
  }

  // 清空地区选择，让用户重新选择
  evaluationForm.selectedProvince = ''
  evaluationForm.selectedCity = ''
  evaluationForm.selectedCounty = ''
  evaluationForm.regions = []
  evaluationForm.countyData = []

  // 重新加载地区数据（不自动选择）
  resetRegionSelect()
  await getProvinces(false)

  // 恢复之前选择的组织机构（如果有）
  if (storedOrg && (storedOrg.provinceName || storedOrg.name)) {
    console.log('恢复组织机构选择:', storedOrg)
    await restoreOrganizationSelection(storedOrg)
  }

  if (evaluationForm.orgCode) {
    await handleRegionTreeChange(evaluationForm.orgCode)
  }
}

// 恢复组织机构选择
const restoreOrganizationSelection = async (storedOrg: any) => {
  try {
    console.log('尝试恢复组织机构:', storedOrg)

    let targetProvinceName: string | null = storedOrg?.provinceName ?? null

    if (targetProvinceName) {
      const targetNormalized = String(targetProvinceName).replace(/省$/, '')
      const matchedProvince = provinces.value.find((p: any) => {
        const pName = String(p?.name ?? '')
        const pNormalized = pName.replace(/省$/, '')
        return pName === targetProvinceName || pNormalized === targetNormalized
      })
      targetProvinceName = matchedProvince?.name ?? null
    }

    if (!targetProvinceName && storedOrg?.name) {
      const matchedProvince = provinces.value.find((p: any) => String(storedOrg.name).includes(String(p?.name ?? '')))
      targetProvinceName = matchedProvince?.name ?? null
    }

    if (!targetProvinceName) {
      const sichuan = provinces.value.find((p: any) => String(p?.name ?? '').replace(/省$/, '') === '四川')
      targetProvinceName = sichuan?.name ?? null
    }

    if (!targetProvinceName && provinces.value.length > 0) {
      targetProvinceName = (provinces.value as any[])[0].name
    }

    if (!targetProvinceName) {
      console.warn('没有可用的省份，无法恢复组织机构')
      return
    }

    await handleProvinceChange(targetProvinceName)
  } catch (error) {
    console.error('恢复组织机构选择失败:', error)
  }
}

const resetRegionSelect = () => {
  evaluationForm.selectedProvince = ''
  evaluationForm.selectedCity = ''
  evaluationForm.selectedCounty = ''
  evaluationForm.countyData = []
  evaluationForm.regions = []
  evaluationForm.orgCode = ''
  provinces.value = []
  cities.value = []
  counties.value = []
}

// 处理数据类型变化
const handleDataTypeChange = () => {
  console.log('数据类型变化:', evaluationForm.dataType)
  resetRegionSelect()
  // 重新获取省份数据，但不自动选择（让 handleModelChange 来恢复）
  getProvinces(false)
}

// 获取省份列表
const getProvinces = async (autoSelect = true) => {
  try {
    const response = await regionDataApi.getProvinces(evaluationForm.dataType, evaluationForm.year)
    if (response.code === 200) {
      provinces.value = response.data || []
      console.log('获取到省份列表:', provinces.value)

      // 只有在需要自动选择时才恢复组织机构
      if (!autoSelect) {
        return
      }

      const storedOrg = globalOrganizationStore.selectedOrganization
      let targetProvinceName: string | null = null

      if (storedOrg?.provinceName) {
        const storedNormalized = String(storedOrg.provinceName).replace(/省$/, '')
        const matchedProvince = provinces.value.find((p: any) => {
          const provinceName = String(p?.name ?? '')
          const provinceNormalized = provinceName.replace(/省$/, '')
          return provinceName === storedOrg.provinceName || provinceNormalized === storedNormalized
        })
        if (matchedProvince) {
          targetProvinceName = matchedProvince.name
          console.log('从全局 store 恢复省份:', targetProvinceName)
        }
      }

      if (!targetProvinceName && storedOrg?.name) {
        const matchedProvince = provinces.value.find((p: any) => String(storedOrg.name).includes(String(p?.name ?? '')))
        if (matchedProvince) {
          targetProvinceName = matchedProvince.name
          console.log('从全局 store 推断省份:', targetProvinceName)
        }
      }

        // 如果没有找到匹配的省份，使用默认的第一条
      if (!targetProvinceName && provinces.value.length > 0) {
        targetProvinceName = provinces.value[0].name
      }

      if (targetProvinceName) {
        evaluationForm.selectedProvince = targetProvinceName
        // 触发省份变化事件以加载城市数据
        await handleProvinceChange(targetProvinceName)
      }
    } else {
      ElMessage.error(response.message || '获取省份列表失败')
    }
  } catch (error) {
    console.error('获取省份列表失败:', error)
    ElMessage.error('获取省份列表失败')
  }
}

// 处理省份变化
const handleProvinceChange = async (provinceName: string) => {
  console.log('省份变化:', provinceName)
  // 设置选中的省份
  evaluationForm.selectedProvince = provinceName
  // 清空城市和区县选择
  evaluationForm.selectedCity = ''
  evaluationForm.selectedCounty = ''
  evaluationForm.countyData = []
  evaluationForm.regions = []
  evaluationForm.orgCode = ''
  cities.value = []
  counties.value = []

  if (provinceName) {
    // 获取城市列表
    try {
      const response = await regionDataApi.getCities(evaluationForm.dataType, provinceName, evaluationForm.year)
      if (response.code === 200) {
        cities.value = response.data || []
        console.log('获取到城市列表:', cities.value)

        const storedOrg = globalOrganizationStore.selectedOrganization
        let targetCityName: string | null = null

        if (storedOrg) {
          console.log('尝试从全局 store 恢复城市, storedOrg:', storedOrg)
          const cityToMatch = storedOrg.cityName || storedOrg.name
          console.log('要匹配的城市名称:', cityToMatch, '可用城市列表:', cities.value.map((c: any) => c.name))
          if (cityToMatch) {
            const matchedCity = cities.value.find((c: any) => c.name === cityToMatch || String(cityToMatch).includes(String(c.name)))
            if (matchedCity) {
              targetCityName = matchedCity.name
              console.log('从全局 store 恢复城市:', targetCityName)
            } else {
              console.warn('未找到匹配的城市:', cityToMatch)
            }
          }
        }

        if (!targetCityName && storedOrg?.code && cities.value.length > 0) {
          const raw = String(storedOrg.code)
          const countyCodeToFind = /^\d{6,}/.test(raw) ? raw.substring(0, 6) : null
          const countyNameToFind = storedOrg.countyName || storedOrg.name
          if (countyCodeToFind || countyNameToFind) {
            for (const city of cities.value as any[]) {
              try {
                const countiesResp = await regionDataApi.getCounties(evaluationForm.dataType, provinceName, city.name, evaluationForm.year)
                if (countiesResp.code === 200) {
                  const list = countiesResp.data || []
                  const found = list.find((c: any) => {
                    const cCode = c?.code != null ? String(c.code) : ''
                    const cName = c?.name != null ? String(c.name) : ''
                    const cCode6 = /^\d{6,}/.test(cCode) ? cCode.substring(0, 6) : cCode
                    if (countyCodeToFind && cCode6 === countyCodeToFind) return true
                    if (countyNameToFind && (cName === countyNameToFind || String(countyNameToFind).includes(cName))) return true
                    return false
                  })
                  if (found) {
                    targetCityName = city.name
                    console.log('通过区县定位到城市:', targetCityName)
                    break
                  }
                }
              } catch (e) {
                console.warn('通过区县定位城市失败:', city?.name, e)
              }
            }
          }
        }

        // 如果没有找到匹配的城市，使用默认的第一条
        if (!targetCityName && cities.value.length > 0) {
          targetCityName = cities.value[0].name
        }

        if (targetCityName) {
          evaluationForm.selectedCity = targetCityName
          // 触发城市变化事件以加载区县数据
          await handleCityChange(targetCityName)
        }
      } else {
        ElMessage.error(response.message || '获取城市列表失败')
      }
    } catch (error) {
      console.error('获取城市列表失败:', error)
      ElMessage.error('获取城市列表失败')
    }
  }
}

// 处理城市变化
const handleCityChange = async (cityName: string) => {
  console.log('城市变化:', cityName)
  // 清空区县选择
  evaluationForm.selectedCounty = ''
  evaluationForm.countyData = []
  evaluationForm.regions = []
  evaluationForm.orgCode = ''
  counties.value = []

  if (cityName && evaluationForm.selectedProvince) {
    // 获取区县列表
    try {
      console.log('开始获取区县列表:', {
        dataType: evaluationForm.dataType,
        province: evaluationForm.selectedProvince,
        city: cityName,
        year: evaluationForm.year
      })
      const response = await regionDataApi.getCounties(evaluationForm.dataType, evaluationForm.selectedProvince, cityName, evaluationForm.year)
      console.log('区县列表API响应:', response)
      if (response.code === 200) {
        counties.value = response.data || []
        console.log('获取到区县列表:', counties.value)

        const storedOrg = globalOrganizationStore.selectedOrganization
        let targetCountyCode: any = null

        if (storedOrg) {
          console.log('尝试从全局 store 恢复区县, storedOrg:', storedOrg)
          const rawCode = storedOrg.code != null ? String(storedOrg.code) : ''
          const countyCodeToMatch = /^\d{6,}/.test(rawCode) ? rawCode.substring(0, 6) : rawCode
          const countyNameToMatch = storedOrg.countyName || storedOrg.name

          if (countyCodeToMatch) {
            const matchedByCode = counties.value.find((c: any) => {
              const cCode = c?.code != null ? String(c.code) : ''
              const cCode6 = /^\d{6,}/.test(cCode) ? cCode.substring(0, 6) : cCode
              return cCode6 === countyCodeToMatch
            })
            if (matchedByCode) {
              targetCountyCode = matchedByCode.code
              console.log('从全局 store 恢复区县(按code):', targetCountyCode)
            }
          }

          if (!targetCountyCode && countyNameToMatch) {
            console.log('要匹配的区县名称:', countyNameToMatch, '可用区县列表:', counties.value.map((c: any) => c.name))
            // 更健壮的名称匹配逻辑
            const normalizedMatch = countyNameToMatch.trim()
            const matchedByName = counties.value.find((c: any) => {
              const cName = String(c.name || '').trim()
              return cName === normalizedMatch ||
                     normalizedMatch.includes(cName) ||
                     cName.includes(normalizedMatch)
            })
            if (matchedByName) {
              targetCountyCode = matchedByName.code
              console.log('从全局 store 恢复区县(按name):', matchedByName.name, 'code:', targetCountyCode)
            } else {
              console.warn('未找到匹配的区县:', countyNameToMatch)
            }
          }

          // 如果还是没有找到匹配的区县，不要使用默认的第一条，而是保持空选择让用户手动选择
          if (!targetCountyCode) {
            console.warn('无法从全局 store 恢复区县，需要用户手动选择')
          }
        }

        if (targetCountyCode) {
          evaluationForm.selectedCounty = targetCountyCode
          // 触发区县变化事件以加载数据
          await handleCountyChange(targetCountyCode)
        }
      } else {
        console.error('区县列表API返回错误:', response.code, response.message)
        ElMessage.error(response.message || '获取区县列表失败')
      }
    } catch (error) {
      console.error('获取区县列表失败:', error)
      ElMessage.error('获取区县列表失败')
    }
  } else {
    console.warn('handleCityChange 条件不满足:', { cityName, selectedProvince: evaluationForm.selectedProvince })
  }
  console.log('handleCityChange 完成')
}

// 处理区县变化
const handleCountyChange = async (countyCode: string) => {
  console.log('区县变化:', countyCode)
  // 清空之前的数据
  evaluationForm.countyData = []
  evaluationForm.regions = []

  if (countyCode && evaluationForm.selectedProvince && evaluationForm.selectedCity) {
    // 从counties数组中查找区县详细信息，确认数据结构
    const selectedCountyObj = counties.value.find((c: any) => c.code === countyCode)
    console.log('选中的区县对象:', selectedCountyObj)
    console.log('所有区县数据:', counties.value)

    // 获取区县名称和代码
    const countyName = selectedCountyObj?.name || countyCode
    const extractedCountyCode = selectedCountyObj?.code || countyCode

    // 设置机构代码为区县代码（如511425）
    // 如果 selectedCountyObj 中有专门的 code 字段且不是名称，就使用它
    // 否则从返回的县数据中提取区县代码
    evaluationForm.orgCode = extractedCountyCode

    console.log('机构代码设置:', {
      countyName,
      extractedCountyCode,
      orgCode: evaluationForm.orgCode
    })

    // 获取该县的数据
    try {
      const response = await regionDataApi.getDataByCounty(
        evaluationForm.dataType,
        evaluationForm.selectedProvince,
        evaluationForm.selectedCity,
        countyName,
        evaluationForm.year
      )
      if (response.code === 200) {
        evaluationForm.countyData = response.data || []
        console.log('返回的县数据样本:', evaluationForm.countyData[0])

        // 从县数据中提取区县代码（取 regionCode 的前6位，如 511425001 -> 511425）
        if (evaluationForm.countyData.length > 0) {
          const firstItem: any = (evaluationForm.countyData as any[])[0]
          if (firstItem.regionCode) {
            // 提取前6位作为区县代码
            evaluationForm.orgCode = firstItem.regionCode.substring(0, 6)
            console.log('从 regionCode 提取的区县代码:', evaluationForm.orgCode, '（原始值:', firstItem.regionCode, '）')

            // 保存选中的组织机构到全局 store（保留完整信息）
            globalOrganizationStore.setOrganization({
              code: evaluationForm.orgCode,
              name: countyName,
              level: 3, // 区县级别
              provinceName: evaluationForm.selectedProvince,
              cityName: evaluationForm.selectedCity,
              countyName: countyName
            })
            console.log('保存组织机构到全局 store:', {
              code: evaluationForm.orgCode,
              name: countyName,
              level: 3,
              provinceName: evaluationForm.selectedProvince,
              cityName: evaluationForm.selectedCity,
              countyName: countyName
            })
          } else {
            // 如果没有 regionCode，使用 county 字段（但这可能是名称）
            console.warn('未找到 regionCode 字段，使用 county 字段作为备选:', firstItem.county)
            if (firstItem.county && /^\d+$/.test(firstItem.county)) {
              evaluationForm.orgCode = firstItem.county
            } else {
              evaluationForm.orgCode = firstItem.county || countyName
            }

            // 保存选中的组织机构到全局 store（保留完整信息）
            globalOrganizationStore.setOrganization({
              code: evaluationForm.orgCode,
              name: countyName,
              level: 3, // 区县级别
              provinceName: evaluationForm.selectedProvince,
              cityName: evaluationForm.selectedCity,
              countyName: countyName
            })
          }
        }

        // 将数据转换为regions格式用于评估
        evaluationForm.regions = evaluationForm.countyData.map((item: any) => {
          if (evaluationForm.dataType === 'community') {
            return item.regionCode || `${item.provinceName}_${item.cityName}_${item.countyName}_${item.communityName}`
          } else {
            return item.regionCode || `${item.province}_${item.city}_${item.county}_${item.township}`
          }
        })
        console.log('获取到县数据:', {
          county: countyName,
          countyCode: extractedCountyCode,
          orgCode: evaluationForm.orgCode,
          dataType: evaluationForm.dataType,
          dataCount: evaluationForm.countyData.length,
          regions: evaluationForm.regions
        })
        ElMessage.success(`成功获取${countyName}的${evaluationForm.dataType === 'community' ? '社区' : '乡镇'}数据，共${evaluationForm.countyData.length}条`)
      } else {
        ElMessage.error(response.message || '获取县数据失败')
      }
    } catch (error) {
      console.error('获取县数据失败:', error)
      ElMessage.error('获取县数据失败')
    }
  }
}

// 处理树形选择器变化
const handleRegionTreeChange = async (value: string) => {
  console.log('树形选择器变化:', value)
  // 清空之前的数据
  evaluationForm.countyData = []
  evaluationForm.regions = []

  if (!value) {
    // 清空选择
    evaluationForm.orgCode = ''
    globalOrganizationStore.setOrganization(null)
    return
  }

  // 从树形数据中查找选中的节点
  const selectedNode = findRegionNodeByCode(regionTreeData.value, value)
  if (!selectedNode) {
    console.warn('未找到选中的节点:', value)
    return
  }

  console.log('选中的节点:', selectedNode)

  // 设置组织代码
  evaluationForm.orgCode = selectedNode.code

  if (selectedNode.level === 1) {
    evaluationForm.selectedProvince = selectedNode.provinceName || selectedNode.name
    evaluationForm.selectedCity = ''
    evaluationForm.selectedCounty = ''
  } else if (selectedNode.level === 2) {
    evaluationForm.selectedProvince = selectedNode.provinceName || evaluationForm.selectedProvince
    evaluationForm.selectedCity = selectedNode.cityName || selectedNode.name
    evaluationForm.selectedCounty = ''
  } else if (selectedNode.level === 3) {
    evaluationForm.selectedProvince = selectedNode.provinceName || evaluationForm.selectedProvince
    evaluationForm.selectedCity = selectedNode.cityName || evaluationForm.selectedCity
    evaluationForm.selectedCounty = selectedNode.code
  }

  // 根据选择的节点级别保存到全局 store
  const levelInfo = {
    code: selectedNode.code,
    name: selectedNode.name,
    level: selectedNode.level,
    provinceName: selectedNode.provinceName,
    cityName: selectedNode.cityName,
    countyName: selectedNode.level === 3 ? (selectedNode.countyName || selectedNode.name) : selectedNode.countyName,
    townshipName: selectedNode.townshipName,
    communityName: selectedNode.communityName
  }

  globalOrganizationStore.setOrganization(levelInfo)

  const selectedModelName = String(selectedModel.value?.modelName || '')

  // 如果选择的是区县级别（level 3），加载数据
  if (selectedNode.level === 3) {
    await loadCountyData(selectedNode.name, selectedNode.code)
  } else if (selectedNode.level === 2) {
    evaluationForm.regions = [selectedNode.code]
  } else {
    console.log('选择的是非区县级别的节点，暂不加载数据')
  }
}

// 加载区县数据
const loadCountyData = async (countyName: string, countyCode: string) => {
  try {
    let provinceName = evaluationForm.selectedProvince
    let cityName = evaluationForm.selectedCity
    let countyNameToUse = countyName

    if ((!provinceName || !cityName) && countyCode) {
      const parts = String(countyCode).split('_')
      if (parts.length >= 4 && parts[0] === 'county') {
        provinceName = parts[1]
        cityName = parts[2]
        countyNameToUse = parts.slice(3).join('_') || countyNameToUse
      }
    }

    if (!provinceName || !cityName) {
      throw new Error('缺少省/市信息，无法按区县获取数据')
    }

    const response = await regionDataApi.getDataByCounty(
      evaluationForm.dataType,
      provinceName,
      cityName,
      countyNameToUse,
      evaluationForm.year
    )

    if (response.code === 200) {
      evaluationForm.countyData = response.data || []
      console.log('返回的县数据样本:', evaluationForm.countyData[0])

      // 从县数据中提取区县代码（取 regionCode 的前6位，如 511425001 -> 511425）
      if (evaluationForm.countyData.length > 0) {
        const firstItem: any = (evaluationForm.countyData as any[])[0]
        if (firstItem.regionCode) {
          evaluationForm.orgCode = firstItem.regionCode.substring(0, 6)
        }
      }

      // 将数据转换为regions格式用于评估
      evaluationForm.regions = evaluationForm.countyData.map((item: any) => {
        if (evaluationForm.dataType === 'community') {
          return item.regionCode || `${item.provinceName}_${item.cityName}_${item.countyName}_${item.communityName}`
        } else {
          return item.regionCode || item.townshipName
        }
      })

      ElMessage.success(`成功获取${countyName}的${evaluationForm.dataType === 'community' ? '社区' : '乡镇'}数据，共${evaluationForm.countyData.length}条`)
    }
  } catch (error) {
    console.error('获取县数据失败:', error)
    ElMessage.error('获取县数据失败')
  }
}

// 获取评估历史（使用评估配置表单的值作为筛选条件）
const getEvaluationHistory = async (page: number = currentPage.value, size: number = pageSize.value) => {
  loading.history = true
  try {
    // 构建查询参数，直接使用评估表单的值
    const params: any = {
      page,
      size
    }

    // 从评估配置表单获取筛选条件
    if (evaluationForm.year) {
      params.year = evaluationForm.year
    }
    if (evaluationForm.orgCode) {
      params.orgCode = evaluationForm.orgCode
    }
    if (evaluationForm.modelId) {
      params.modelId = evaluationForm.modelId
    }
    // 状态从独立的筛选表单获取
    if (filterForm.status) {
      params.executionStatus = filterForm.status
    }

    console.log('获取评估历史，筛选条件:', params)

    // 调用API
    const result = await evaluationApi.getEvaluationHistoryList(params)

    if (result.success) {
      evaluationHistory.value = result.data || { records: [], total: 0, current: 1, size: 10, pages: 0 }
      // 更新分页状态
      currentPage.value = evaluationHistory.value.current || 1
      pageSize.value = evaluationHistory.value.size || 10
    } else {
      ElMessage.error(result.message || '获取评估历史失败')
      evaluationHistory.value = { records: [], total: 0, current: 1, size: 10, pages: 0 }
    }
  } catch (error) {
    console.error('获取评估历史失败:', error)
    ElMessage.error('获取评估历史失败')
    evaluationHistory.value = { records: [], total: 0, current: 1, size: 10, pages: 0 }
  } finally {
    loading.history = false
  }
}

// 刷新历史记录
const refreshHistory = () => {
  getEvaluationHistory()
}

// 处理筛选条件变化
const handleFilterChange = () => {
  console.log('筛选条件变化:', filterForm)
  // 状态筛选变化时自动刷新
  currentPage.value = 1
  getEvaluationHistory(1, pageSize.value)
}

// 显示全部历史（不清空状态筛选）
const showAllHistory = () => {
  currentPage.value = 1
  getEvaluationHistory(1, pageSize.value)
}

// 清空筛选条件（仅清空状态）
const clearFilters = () => {
  filterForm.status = ''
  currentPage.value = 1
  getEvaluationHistory(1, pageSize.value)
}

// 获取区县名称
const getCountyName = (code: string) => {
  if (!code) return ''
  // 1. 如果传进来的是汉字，直接返回
  if (!isAdministrativeCode(code)) return code

  const selectedNode = findRegionNodeByCode(regionTreeData.value, code)
  const countyName = selectedNode?.level === 3 ? (selectedNode.countyName || selectedNode.name) : ''
  const provinceName = selectedNode?.provinceName || ''
  const cityName = selectedNode?.cityName || ''

  if (countyName && !isAdministrativeCode(countyName)) {
    return buildCountyFullName(provinceName, cityName, countyName)
  }

  const county = counties.value.find((c: any) => c.code === code || (c.code && String(c.code).startsWith(code)))
  return county?.name || code
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

// 处理页面大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  getEvaluationHistory(1, size)
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getEvaluationHistory(page, pageSize.value)
}

// 查看执行记录详情
const viewExecutionDetail = async (row: any) => {
  loading.evaluationDetail = true
  try {
    // 调用后端API获取评估结果详情
    const response = await fetch(`/api/evaluation/history/detail/${row.id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      const result = await response.json()
      if (result.success) {
        // 设置当前执行记录和评估结果
        currentExecutionRecord.value = result.data.executionRecord
        evaluationResults.value = result.data.evaluationResults || []
        const executionResult = result.data.executionResult
        if (executionResult && (executionResult.isMultiStep || executionResult.tableData)) {
          displayModelResults(executionResult)
        } else {
          dialogVisible.evaluationDetail = true
        }
        console.log('获取评估结果详情成功:', {
          executionRecord: result.data.executionRecord,
          evaluationResultsCount: result.data.evaluationResults?.length
        })
      } else {
        ElMessage.error(result.message || '获取评估结果详情失败')
      }
    } else {
      ElMessage.error('获取评估结果详情失败')
    }
  } catch (error) {
    console.error('获取评估结果详情失败:', error)
    ElMessage.error('获取评估结果详情失败')
  } finally {
    loading.evaluationDetail = false
  }
}

// 导出评估结果
const exportEvaluationResults = () => {
  if (evaluationResults.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  try {
    // 创建CSV内容
    let csvContent = 'data:text/csv;charset=utf-8,\n'
    csvContent += `评估结果详情 - ${currentExecutionRecord.value?.executionCode || ''}\n\n`

    // 添加表头
    const headers = [
      'ID',
      '地区代码',
      '地区名称',
      '管理能力得分',
      '管理能力等级',
      '支持能力得分',
      '支持能力等级',
      '自救能力得分',
      '自救能力等级',
      '综合能力得分',
      '综合能力等级',
      '创建时间'
    ]
    csvContent += headers.join(',') + '\n'

    // 添加数据行
    evaluationResults.value.forEach((row: any) => {
      const values = [
        row.id,
        row.regionCode,
        row.regionName,
        row.managementCapabilityScore,
        row.managementCapabilityLevel,
        row.supportCapabilityScore,
        row.supportCapabilityLevel,
        row.selfRescueCapabilityScore,
        row.selfRescueCapabilityLevel,
        row.comprehensiveCapabilityScore,
        row.comprehensiveCapabilityLevel,
        formatDate(row.createTime)
      ]
      csvContent += values.join(',') + '\n'
    })

    // 创建下载链接
    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `评估结果_${currentExecutionRecord.value?.executionCode || 'unknown'}_${new Date().getTime()}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 重置评估表单
const resetEvaluationForm = () => {
  Object.assign(evaluationForm, {
    name: '',
    modelId: null,
    weightConfigId: undefined,
    year: globalYearStore.selectedYear,
    dataType: 'township', // 重置为默认乡镇数据
    dataSource: 'REGION',
    regions: [] as string[],
    parameters: {
      crThreshold: 0.1,
      maxIterations: 100,
      fuzzyMethod: 'TRIANGULAR',
      operator: 'WEIGHTED_AVERAGE',
      resolution: 0.5,
      normalization: 'MIN_MAX'
    },
    description: ''
  })
  evaluationFormRef.value?.resetFields()
  // 重新获取地区树数据
  getRegionTreeData()
}

// 验证参数
const validateParameters = async () => {
  if (!evaluationFormRef.value) return
  
  await evaluationFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const response = await evaluationApi.validateParams(evaluationForm as any)
      if (response.success) {
        ElMessage.success('参数验证通过')
      } else {
        ElMessage.error(response.message || '参数验证失败')
      }
    } catch (error) {
      console.error('验证参数失败:', error)
      ElMessage.error('验证参数失败')
    }
  })
}

// 预览数据
const handlePreviewData = async () => {
  try {
    let response
    if (evaluationForm.dataSource === 'REGION' && evaluationForm.regions.length > 0) {
      // 按地区筛选获取数据
      response = await surveyDataApi.getAll()
    } else {
      response = await surveyDataApi.getAll()
    }
    
    if (response.success) {
      previewData.value = response.data || []
      dialogVisible.preview = true
    } else {
      ElMessage.error(response.message || '获取预览数据失败')
    }
  } catch (error) {
    console.error('预览数据失败:', error)
    ElMessage.error('预览数据失败')
  }
}

// 开始评估
const startEvaluation = async () => {
  if (!evaluationFormRef.value) return

  await evaluationFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 验证必需参数
    if (!evaluationForm.year) {
      ElMessage.error('请选择评估年份')
      return
    }

    if (!evaluationForm.regions || evaluationForm.regions.length === 0) {
      ElMessage.error('请选择评估地区')
      return
    }

    if (!evaluationForm.modelId) {
      ElMessage.error('请选择评估模型')
      return
    }

    await executeModelEvaluation()
  })
}

const startAsyncEvaluation = async () => {
  if (!evaluationFormRef.value) return

  await evaluationFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!evaluationForm.year) {
      ElMessage.error('请选择评估年份')
      return
    }

    if (!evaluationForm.regions || evaluationForm.regions.length === 0) {
      ElMessage.error('请选择评估地区')
      return
    }

    if (!evaluationForm.modelId) {
      ElMessage.error('请选择评估模型')
      return
    }

    loading.evaluation = true
    try {
      const executionRecordId = await submitModelEvaluationTask()
      evaluationProgress.message = '任务已提交，正在后台执行中'
      evaluationProgress.detail = `任务ID：${executionRecordId}`
      ElMessage.success('异步评估任务已提交')
      pollAsyncModelExecutionLite(executionRecordId)
    } catch (error: any) {
      console.error('提交异步评估任务失败:', error)
      evaluationProgress.percentage = 100
      evaluationProgress.status = 'exception'
      evaluationProgress.message = '提交评估任务失败'
      evaluationProgress.detail = error.message || '未知错误'
      ElMessage.error(error.message || '提交异步评估任务失败')
    } finally {
      loading.evaluation = false
    }
  })
}

const asyncExecutionRecordId = ref<number | null>(null)
const asyncExecutionPollTimer = ref<number | null>(null)

const clearAsyncExecutionPoll = () => {
  if (asyncExecutionPollTimer.value != null) {
    window.clearTimeout(asyncExecutionPollTimer.value)
    asyncExecutionPollTimer.value = null
  }
}

const fetchExecutionRecordDetail = async (executionRecordId: number) => {
  const response = await fetch(`/api/evaluation/history/detail/${executionRecordId}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    }
  })

  if (!response.ok) {
    throw new Error('获取执行记录失败')
  }

  const result = await response.json()
  if (!result?.success) {
    throw new Error(result?.message || '获取执行记录失败')
  }

  return result.data
}

const pollAsyncModelExecutionLite = (executionRecordId: number) => {
  clearAsyncExecutionPoll()
  const startedAt = Date.now()
  const tick = async (attempt: number) => {
    try {
      const detail = await fetchExecutionRecordDetail(executionRecordId)
      const record = detail?.executionRecord
      const status = record?.executionStatus

      if (status === 'SUCCESS') {
        evaluationProgress.percentage = 100
        evaluationProgress.status = 'success'
        evaluationProgress.message = '异步评估已完成（可在评估历史查看详情）'
        evaluationProgress.detail = record?.executionCode ? `执行编号：${record.executionCode}` : ''
        await getEvaluationHistory(1, pageSize.value)
        return
      }

      if (status === 'FAILED') {
        evaluationProgress.percentage = 100
        evaluationProgress.status = 'exception'
        evaluationProgress.message = '异步评估执行失败'
        evaluationProgress.detail = record?.errorMessage || '未知错误'
        await getEvaluationHistory(1, pageSize.value)
        return
      }

      const elapsedMs = Date.now() - startedAt
      const nextPercent = Math.min(90, Math.max(10, Math.floor(elapsedMs / 1500) * 5 + 10))
      evaluationProgress.percentage = Math.max(evaluationProgress.percentage, nextPercent)
      evaluationProgress.status = 'warning'
      evaluationProgress.message = '后台评估执行中...'
      evaluationProgress.detail = record?.executionCode ? `执行编号：${record.executionCode}` : `任务ID：${executionRecordId}`

      asyncExecutionPollTimer.value = window.setTimeout(() => tick(attempt + 1), 1500)
    } catch (e: any) {
      if (attempt >= 10) {
        evaluationProgress.status = 'warning'
        evaluationProgress.message = '任务已提交，等待结果中...'
      }
      asyncExecutionPollTimer.value = window.setTimeout(() => tick(attempt + 1), 2000)
    }
  }

  void tick(0)
}

const pollAsyncModelExecution = async (executionRecordId: number) => {
  clearAsyncExecutionPoll()
  const startedAt = Date.now()
  const tick = async (attempt: number) => {
    try {
      const detail = await fetchExecutionRecordDetail(executionRecordId)
      const record = detail?.executionRecord
      const status = record?.executionStatus

      if (status === 'SUCCESS') {
        evaluationProgress.percentage = 100
        evaluationProgress.status = 'success'
        evaluationProgress.message = '评估执行完成'
        evaluationProgress.detail = ''

        currentExecutionRecord.value = record
        evaluationResults.value = detail?.evaluationResults || []
        const executionResult = detail?.executionResult
        if (executionResult && (executionResult.isMultiStep || executionResult.tableData)) {
          displayModelResults(executionResult)
        } else {
          dialogVisible.evaluationDetail = true
        }

        await getEvaluationHistory(1, pageSize.value)
        loading.evaluation = false
        return
      }

      if (status === 'FAILED') {
        evaluationProgress.percentage = 100
        evaluationProgress.status = 'exception'
        evaluationProgress.message = '评估执行失败'
        evaluationProgress.detail = record?.errorMessage || '未知错误'

        await getEvaluationHistory(1, pageSize.value)
        loading.evaluation = false
        return
      }

      const elapsedMs = Date.now() - startedAt
      const nextPercent = Math.min(90, Math.max(10, Math.floor(elapsedMs / 1500) * 5 + 10))
      evaluationProgress.percentage = Math.max(evaluationProgress.percentage, nextPercent)
      evaluationProgress.status = 'warning'
      evaluationProgress.message = '后台评估执行中...'
      evaluationProgress.detail = record?.executionCode ? `执行编号：${record.executionCode}` : ''

      asyncExecutionPollTimer.value = window.setTimeout(() => tick(attempt + 1), 1500)
    } catch (e: any) {
      if (attempt >= 10) {
        evaluationProgress.status = 'warning'
        evaluationProgress.message = '任务已提交，等待结果中...'
      }
      asyncExecutionPollTimer.value = window.setTimeout(() => tick(attempt + 1), 2000)
    }
  }

  await tick(0)
}

const submitModelEvaluationTask = async () => {
  evaluationProgress.visible = true
  evaluationProgress.percentage = 0
  evaluationProgress.status = 'warning'
  evaluationProgress.message = '正在提交评估任务...'
  evaluationProgress.detail = ''

  const regionCodes = evaluationForm.regions.map((regionId: string) => extractRegionCode(regionId))

  evaluationProgress.percentage = 20
  evaluationProgress.detail = '加载模型配置...'

  const response = await evaluationApi.executeModel(
    evaluationForm.modelId,
    regionCodes,
    evaluationForm.weightConfigId ?? null,
    evaluationForm.year,
    evaluationForm.orgCode || globalOrganizationStore.selectedOrganization?.code || '',
    userStore.username || ''
  )

  const executionRecordId = response?.data?.executionRecordId
  if (!response.success || !executionRecordId) {
    throw new Error(response.message || '提交评估任务失败')
  }

  asyncExecutionRecordId.value = Number(executionRecordId)

  evaluationProgress.percentage = 30
  evaluationProgress.status = 'warning'
  evaluationProgress.message = '任务已提交，正在后台执行中'
  evaluationProgress.detail = `任务ID：${asyncExecutionRecordId.value}`

  await getEvaluationHistory(1, pageSize.value)

  return asyncExecutionRecordId.value
}

// 执行模型评估（等待完成并展示结果）
const executeModelEvaluation = async () => {
  loading.evaluation = true

  try {
    const executionRecordId = await submitModelEvaluationTask()
    await pollAsyncModelExecution(executionRecordId)
  } catch (error: any) {
    console.error('执行评估模型失败:', error)
    evaluationProgress.percentage = 100
    evaluationProgress.status = 'exception'
    evaluationProgress.message = '评估执行失败'
    evaluationProgress.detail = error.message || '未知错误'
    ElMessage.error(error.message || '执行评估模型失败')
    loading.evaluation = false
  }
}

// 提取地区代码
const extractRegionCode = (regionId: string): string => {
  // 使用全局映射查找 regionCode
  const regionCodeMap = (window as any).__regionCodeMap
  if (regionCodeMap && regionCodeMap.has(regionId)) {
    return regionCodeMap.get(regionId)
  }
  
  // 如果没有找到映射，尝试移除前缀
  const parts = regionId.split('_')
  if (parts.length > 1) {
    return parts.slice(1).join('_')
  }
  return regionId
}

// 显示模型结果
const displayModelResults = (resultData: any) => {
  console.log('=== displayModelResults 接收的数据 ===')
  console.log('resultData 结构:', {
    hasTableData: !!resultData?.tableData,
    hasColumns: !!resultData?.columns,
    tableDataLength: resultData?.tableData?.length,
    columnsLength: resultData?.columns?.length,
    columnsDetail: resultData?.columns,
    isMultiStep: resultData?.isMultiStep,
    stepResultsListLength: resultData?.stepResultsList?.length,
    rawStepResultsType: typeof resultData?.stepResults
  })
  
  // 优先处理多步骤结果
  if (resultData?.isMultiStep && Array.isArray(resultData?.stepResultsList) && resultData.stepResultsList.length > 0) {
    console.log('✓ 检测到多步骤数据，直接传递给 ResultDialog')
    currentStepInfo.value = {
      stepNumber: 0,
      stepName: '算法步骤执行结果',
      description: `共执行了 ${resultData.stepResultsList.length} 个步骤`,
      stepCode: 'multi_steps',
      formula: '',
      formulaName: '',
      formulaDescription: ''
    }
    
    currentCalculationResult.value = {
      isMultiStep: true,
      stepResults: resultData.stepResultsList
    }
    
    resultDialogVisible.value = true
    return
  }

  // 使用后端返回的 columns（已包含 stepOrder）
  // 如果后端没有返回 columns，则从 tableData 推断
  let columns: any[] = []
  
  if (resultData?.columns && Array.isArray(resultData.columns) && resultData.columns.length > 0) {
    // 直接使用后端返回的 columns（保留 stepOrder 等字段）
    columns = resultData.columns
    console.log('✓ 使用后端返回的 columns:', columns.length)
    console.log('带 stepOrder 的列数量:', columns.filter(c => c.stepOrder !== undefined).length)
  } else if (resultData?.tableData && resultData.tableData.length > 0) {
    // 后端没有返回 columns，从 tableData 推断
    console.log('⚠ 后端未返回 columns，从 tableData 推断')
    const firstRow = resultData.tableData[0]
    Object.keys(firstRow).forEach(key => {
      columns.push({
        prop: key,
        label: key === 'regionCode' ? '地区代码' : key === 'regionName' ? '地区名称' : key,
        width: 120
      })
    })
  } else {
    console.error('❌ 无法获取列配置，resultData:', resultData)
  }

  // 设置弹窗数据并显示
  currentStepInfo.value = {
    stepNumber: 0,
    stepName: '模型评估结果',
    description: '基于配置模型的评估结果',
    stepCode: 'model_result',
    formula: '',
    formulaName: '',
    formulaDescription: ''
  }
  
  currentCalculationResult.value = {
    tableData: resultData?.tableData || resultData || [],
    columns: columns
  }
  
  console.log('✓ 传递给 ResultDialog 的数据:', {
    tableDataLength: currentCalculationResult.value.tableData.length,
    columnsLength: currentCalculationResult.value.columns.length,
    columnsWithStepOrder: (currentCalculationResult.value as any).columns.filter((c: any) => c.stepOrder !== undefined).length
  })
  
  resultDialogVisible.value = true
}

// 查看结果
const viewResults = () => {
  router.push('/results')
}

// 下载报告
const downloadReport = async () => {
  try {
    ElMessage.success('报告下载功能开发中...')
  } catch (error) {
    console.error('下载报告失败:', error)
    ElMessage.error('下载报告失败')
  }
}

// 查看评估详情
const viewEvaluationDetail = (row: any) => {
  // 调用相同的查看执行记录详情方法
  viewExecutionDetail(row)
}

// 删除评估历史记录
const deleteEvaluationHistory = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评估历史记录吗？删除后将同时删除关联的评估结果数据。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await evaluationApi.deleteEvaluationHistory(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      getEvaluationHistory()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评估历史失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUCCESS': 'success',
    'RUNNING': 'warning',
    'FAILED': 'danger',
    'PENDING': 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUCCESS': '成功',
    'RUNNING': '运行中',
    'FAILED': '失败',
    'PENDING': '等待中'
  }
  return statusMap[status] || status
}

// 生成评估历史记录的评估名称
const generateHistoryEvaluationName = (row: any) => {
  const year = row.year || new Date().getFullYear()
  let name = `${year}年`

  // 查找区县名称
  if (row.orgCode) {
    const county = counties.value.find((c: any) => c.code === row.orgCode)
    if (county?.name) {
      name += county.name
    }
  }

  // 查找模型名称
  if (row.modelId) {
    const model = evaluationModels.value.find((m: any) => m.id === row.modelId)
    if (model?.modelName) {
      name += `${model.modelName}评估`
    } else {
      name += '减灾能力评估'
    }
  } else {
    name += '减灾能力评估'
  }

  return name
}

// 处理地区选择
const handleRegionCheck = (
  data: { id: string; children?: Array<{ id: string }> },
  checked: boolean,
  indeterminate: boolean
) => {
  console.log('地区选择事件:', { data: data.id, checked, indeterminate })

  // 获取当前选中的地区列表
  let currentRegions = [...(evaluationForm.regions || [])] as string[]

  if (checked) {
    // 选中节点时：只选择其直系子节点（不包括点击的节点本身）
    // 如果当前节点有子节点，选择所有直接子节点
    if (data.children && data.children.length > 0) {
      data.children.forEach((child) => {
        if (!currentRegions.includes(child.id)) {
          currentRegions.push(child.id)
        }
      })
    }
  } else {
    // 取消选中节点时：只移除其直系子节点（不包括点击的节点本身）
    // 如果当前节点有子节点，移除所有直接子节点
    if (data.children && data.children.length > 0) {
      const childIdsToRemove = data.children.map((child) => child.id)
      currentRegions = currentRegions.filter(id => !childIdsToRemove.includes(id))
    }
  }

  console.log('更新后的地区选择:', currentRegions)
  evaluationForm.regions = currentRegions
}

// 递归查找节点
const findNodeById = (nodes: any[], id: string): any => {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findNodeById(node.children, id)
      if (found) {
        return found
      }
    }
  }
  return null
}

// 生成模拟步骤结果
const generateMockStepResult = (step: any, index: number) => {
  console.log('=== 生成模拟步骤结果 ===', {
    stepName: step.stepName,
    stepIndex: index,
    includesDefinition: step.stepName.includes('定权')
  })
  
  const mockData = []
  const regions = ['青竹街道', '汉阳镇', '瑞峰镇', '西龙镇', '高台镇', '白果乡', '罗波乡']
  
  // 根据步骤类型生成不同的模拟数据
  console.log('步骤名称匹配检查:', {
    stepName: step.stepName,
    includes定权: step.stepName.includes('定权'),
    includes优劣解算: step.stepName.includes('优劣解算'),
    includes分级: step.stepName.includes('分级')
  })
  
  if (step.stepName.includes('定权')) {
    console.log('✅ 匹配到定权步骤，开始生成双表格数据')
    
    // 表格1数据：一级指标权重计算
    const table1Data = []
    // 表格2数据：乡镇减灾能力权重计算
    const table2Data = []
    
    // 权重系数配置
    const indicatorWeights = {
      teamManagement: 0.125,    // 队伍管理能力权重
      riskAssessment: 0.125,    // 风险评估能力权重
      financialInput: 0.125,    // 财政投入能力权重
      materialReserve: 0.125,   // 物资储备能力权重
      medicalSupport: 0.125,    // 医疗保障能力权重
      selfRescueAbility: 0.125, // 自救互救能力权重
      publicAvoidance: 0.125,   // 公众避险能力权重
      transferResettlement: 0.125 // 转移安置能力权重
    }
    
    for (let i = 0; i < regions.length; i++) {
      // 模拟权重系数
      const disasterMgmtWeight = 0.4 // 灾害管理能力一级权重
      const disasterPrepWeight = 0.3 // 灾害备灾能力一级权重
      const selfRescueWeight = 0.3 // 自救转移能力一级权重
      
      // 模拟二级权重指标
      const teamMgmtSecondWeight = 0.35
      const riskAssessSecondWeight = 0.35
      const financialSecondWeight = 0.3
      const materialSecondWeight = 0.5
      const medicalSecondWeight = 0.5
      const selfRescueSecondWeight = 0.4
      const publicAvoidSecondWeight = 0.3
      const transferSecondWeight = 0.3
      
      // 生成属性向量归一化值（模拟数据）
      const teamMgmtNorm = Math.random() * 0.8 + 0.1
      const riskAssessNorm = Math.random() * 0.8 + 0.1
      const financialNorm = Math.random() * 0.8 + 0.1
      const materialNorm = Math.random() * 0.8 + 0.1
      const medicalNorm = Math.random() * 0.8 + 0.1
      const selfRescueNorm = Math.random() * 0.8 + 0.1
      const publicAvoidNorm = Math.random() * 0.8 + 0.1
      const transferNorm = Math.random() * 0.8 + 0.1
      
      // 根据用户提供的公式计算定权值
      const teamMgmtWeighted = teamMgmtNorm * disasterMgmtWeight * teamMgmtSecondWeight
      const riskAssessWeighted = riskAssessNorm * disasterMgmtWeight * riskAssessSecondWeight
      const financialWeighted = financialNorm * disasterMgmtWeight * financialSecondWeight
      const materialWeighted = materialNorm * disasterPrepWeight * materialSecondWeight
      const medicalWeighted = medicalNorm * disasterPrepWeight * medicalSecondWeight
      const selfRescueWeighted = selfRescueNorm * selfRescueWeight * selfRescueSecondWeight
      const publicAvoidWeighted = publicAvoidNorm * selfRescueWeight * publicAvoidSecondWeight
      const transferWeighted = transferNorm * selfRescueWeight * transferSecondWeight
      
      // 表格1：一级指标权重计算（原始定权值）
      const table1Row = {
        regionName: regions[i],
        teamManagement: teamMgmtWeighted.toFixed(8),
        riskAssessment: riskAssessWeighted.toFixed(8),
        financialInput: financialWeighted.toFixed(8),
        materialReserve: materialWeighted.toFixed(8),
        medicalSupport: medicalWeighted.toFixed(8),
        selfRescueAbility: selfRescueWeighted.toFixed(8),
        publicAvoidance: publicAvoidWeighted.toFixed(8),
        transferResettlement: transferWeighted.toFixed(8)
      }
      
      // 表格2：乡镇减灾能力权重计算（定权值乘以对应权重）
      const table2Row = {
        regionName: regions[i],
        teamManagement: (teamMgmtWeighted * indicatorWeights.teamManagement).toFixed(8),
        riskAssessment: (riskAssessWeighted * indicatorWeights.riskAssessment).toFixed(8),
        financialInput: (financialWeighted * indicatorWeights.financialInput).toFixed(8),
        materialReserve: (materialWeighted * indicatorWeights.materialReserve).toFixed(8),
        medicalSupport: (medicalWeighted * indicatorWeights.medicalSupport).toFixed(8),
        selfRescueAbility: (selfRescueWeighted * indicatorWeights.selfRescueAbility).toFixed(8),
        publicAvoidance: (publicAvoidWeighted * indicatorWeights.publicAvoidance).toFixed(8),
        transferResettlement: (transferWeighted * indicatorWeights.transferResettlement).toFixed(8)
      }
      
      table1Data.push(table1Row)
      table2Data.push(table2Row)
    }
    
    // 表格列配置（两个表格使用相同的列配置）
    const tableColumns = [
      { prop: 'regionName', label: '地区', width: 120 },
      { prop: 'teamManagement', label: '队伍管理能力', width: 120 },
      { prop: 'riskAssessment', label: '风险评估能力', width: 120 },
      { prop: 'financialInput', label: '财政投入能力', width: 120 },
      { prop: 'materialReserve', label: '物资储备能力', width: 120 },
      { prop: 'medicalSupport', label: '医疗保障能力', width: 120 },
      { prop: 'selfRescueAbility', label: '自救互救能力', width: 120 },
      { prop: 'publicAvoidance', label: '公众避险能力', width: 120 },
      { prop: 'transferResettlement', label: '转移安置能力', width: 120 }
    ]
    
    // 统计信息已移除
    
    console.log('双表格数据生成完成:', {
      table1DataCount: table1Data.length,
      table2DataCount: table2Data.length,
      columnsCount: tableColumns.length
    })
    
    return {
      isDualTable: true,
      table1Data: table1Data,
      table1Columns: tableColumns,
      // table1Summary已移除
      table2Data: table2Data,
      table2Columns: tableColumns
      // table2Summary已移除
    }
  } else if (step.stepName.includes('优劣解算')) {
    // 步骤4：优劣解算法计算（基于步骤3表2数据增加第4列）
    for (let i = 0; i < regions.length; i++) {
      mockData.push({
        regionName: regions[i],
        // 步骤3表2的8个指标定权值
        teamManagement: (Math.random() * 0.1).toFixed(8),
        riskAssessment: (Math.random() * 0.1).toFixed(8),
        financialInput: (Math.random() * 0.1).toFixed(8),
        materialReserve: (Math.random() * 0.1).toFixed(8),
        medicalSupport: (Math.random() * 0.1).toFixed(8),
        selfRescue: (Math.random() * 0.1).toFixed(8),
        publicAvoidance: (Math.random() * 0.1).toFixed(8),
        relocationCapacity: (Math.random() * 0.1).toFixed(8),
        // 新增第4列：乡镇（街道）减灾能力 - 3个一级指标值
        disasterManagement: (Math.random() * 0.8 + 0.1).toFixed(8), // 灾害管理能力
        disasterPreparedness: (Math.random() * 0.8 + 0.1).toFixed(8), // 灾害备灾能力
        selfRescueTransfer: (Math.random() * 0.8 + 0.1).toFixed(8) // 自救转移能力
      })
    }
    
    return {
      tableData: mockData,
      columns: [
        { prop: 'regionName', label: '地区', width: 100 },
        // 步骤3表2的8个指标定权值列
        { prop: 'teamManagement', label: '队伍管理能力', width: 110 },
        { prop: 'riskAssessment', label: '风险评估能力', width: 110 },
        { prop: 'financialInput', label: '财政投入能力', width: 110 },
        { prop: 'materialReserve', label: '物资储备能力', width: 110 },
        { prop: 'medicalSupport', label: '医疗保障能力', width: 110 },
        { prop: 'selfRescue', label: '自救互救能力', width: 110 },
        { prop: 'publicAvoidance', label: '公众避险能力', width: 110 },
        { prop: 'relocationCapacity', label: '转移安置能力', width: 110 },
        // 新增第4列：乡镇（街道）减灾能力
        { prop: 'disasterManagement', label: '灾害管理能力', width: 120 },
        { prop: 'disasterPreparedness', label: '灾害备灾能力', width: 120 },
        { prop: 'selfRescueTransfer', label: '自救转移能力', width: 120 }
      ]
      // summary统计信息已移除
    }
  } else if (step.stepName.includes('分级')) {
    // 步骤5：减灾能力分级计算
    const grades = ['强', '较强', '中等', '较弱', '弱']
    for (let i = 0; i < regions.length; i++) {
      const abilityValue = Math.random() * 0.8 + 0.1
      mockData.push({
        regionName: regions[i],
        disasterMgmtAbility: (Math.random() * 0.8 + 0.1).toFixed(4), // 灾害管理能力值
        disasterPrepAbility: (Math.random() * 0.8 + 0.1).toFixed(4), // 灾害备灾能力值
        selfRescueAbility: (Math.random() * 0.8 + 0.1).toFixed(4), // 自救转移能力值
        totalAbility: abilityValue.toFixed(4), // 综合减灾能力值
        disasterMgmtGrade: grades[Math.floor(Math.random() * grades.length)], // 灾害管理能力分级
        disasterPrepGrade: grades[Math.floor(Math.random() * grades.length)], // 灾害备灾能力分级
        selfRescueGrade: grades[Math.floor(Math.random() * grades.length)], // 自救转移能力分级
        totalGrade: grades[Math.floor(Math.random() * grades.length)] // 综合减灾能力分级
      })
    }
    
    return {
      tableData: mockData,
      columns: [
        { prop: 'regionName', label: '地区', width: 100 },
        { prop: 'disasterMgmtAbility', label: '灾害管理能力值', width: 120 },
        { prop: 'disasterPrepAbility', label: '灾害备灾能力值', width: 120 },
        { prop: 'selfRescueAbility', label: '自救转移能力值', width: 120 },
        { prop: 'totalAbility', label: '综合减灾能力值', width: 120 },
        { prop: 'disasterMgmtGrade', label: '灾害管理分级', width: 110 },
        { prop: 'disasterPrepGrade', label: '灾害备灾分级', width: 110 },
        { prop: 'selfRescueGrade', label: '自救转移分级', width: 110 },
        { prop: 'totalGrade', label: '综合能力分级', width: 110 }
      ]
      // summary统计信息已移除
    }
  } else if (step.stepName.includes('指标计算')) {
    for (let i = 0; i < regions.length; i++) {
      mockData.push({
        regionName: regions[i],
        indicator1: (Math.random() * 0.8 + 0.2).toFixed(3),
        indicator2: (Math.random() * 0.8 + 0.2).toFixed(3),
        indicator3: (Math.random() * 0.8 + 0.2).toFixed(3),
        total: (Math.random() * 0.8 + 0.2).toFixed(3)
      })
    }
    
    return {
      tableData: mockData,
      columns: [
        { prop: 'regionName', label: '地区', width: 120 },
        { prop: 'indicator1', label: '指标1', width: 100 },
        { prop: 'indicator2', label: '指标2', width: 100 },
        { prop: 'indicator3', label: '指标3', width: 100 },
        { prop: 'total', label: '总分', width: 100 }
      ]
      // summary统计信息已移除
    }
  } else {
    console.log('❌ 未匹配到已知步骤类型，使用通用数据')
    
    // 如果是步骤3（index === 2），强制返回定权数据
    if (index === 2) {
      console.log('🔧 强制为步骤3生成定权数据')
      
      // 强制生成定权数据
      for (let i = 0; i < regions.length; i++) {
        const mockRowData = {
          regionName: regions[i],
          teamManagement: (Math.random() * 0.1).toFixed(6),
          riskAssessment: (Math.random() * 0.1).toFixed(6),
          financialInput: (Math.random() * 0.1).toFixed(6),
          materialReserve: (Math.random() * 0.1).toFixed(6),
          medicalSupport: (Math.random() * 0.1).toFixed(6),
          selfRescueAbility: (Math.random() * 0.1).toFixed(6),
          publicAvoidance: (Math.random() * 0.1).toFixed(6),
          transferResettlement: (Math.random() * 0.1).toFixed(6),
          townshipDisasterReduction: (Math.random() * 0.8).toFixed(6)
        }
        mockData.push(mockRowData)
      }
      
      const forceColumns = [
        { prop: 'regionName', label: '地区', width: 100 },
        { prop: 'teamManagement', label: '队伍管理能力', width: 110 },
        { prop: 'riskAssessment', label: '风险评估能力', width: 110 },
        { prop: 'financialInput', label: '财政投入能力', width: 110 },
        { prop: 'materialReserve', label: '物资储备能力', width: 110 },
        { prop: 'medicalSupport', label: '医疗保障能力', width: 110 },
        { prop: 'selfRescueAbility', label: '自救互救能力', width: 110 },
        { prop: 'publicAvoidance', label: '公众避险能力', width: 110 },
        { prop: 'transferResettlement', label: '转移安置能力', width: 110 },
        { prop: 'townshipDisasterReduction', label: '乡镇（街道）减灾能力', width: 250 }
      ]
      
      console.log('🔧 强制生成的定权数据:', {
        columnsCount: forceColumns.length,
        dataCount: mockData.length,
        hasNinthColumn: mockData[0]?.townshipDisasterReduction !== undefined
      })
      
      return {
        tableData: mockData,
        columns: forceColumns
        // summary统计信息已移除
      }
    }
    
    // 其他步骤的通用模拟数据
    for (let i = 0; i < regions.length; i++) {
      mockData.push({
        regionName: regions[i],
        value: (Math.random() * 100).toFixed(2),
        weight: (Math.random() * 0.3 + 0.1).toFixed(3),
        score: (Math.random() * 90 + 10).toFixed(2)
      })
    }
    
    return {
      tableData: mockData,
      columns: [
        { prop: 'regionName', label: '地区', width: 120 },
        { prop: 'value', label: '数值', width: 100 },
        { prop: 'weight', label: '权重', width: 100 },
        { prop: 'score', label: '得分', width: 100 }
      ]
      // summary统计信息已移除
    }
  }
}

// 处理弹窗导出事件
const handleExportResult = (exportData: any) => {
  try {
    // 转换为CSV格式
    const csvContent = convertToCSV(exportData)
    
    // 创建下载链接
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.setAttribute('href', url)
    link.setAttribute('download', `步骤${exportData.stepNumber}_${exportData.stepName}_计算结果.csv`)
    link.style.visibility = 'hidden'
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 导出步骤结果（保留原方法以兼容其他地方的调用）
const exportStepResult = async (step: any, index: number) => {
  if (!step.calculationResult) {
    ElMessage.warning('请先计算步骤结果')
    return
  }
  
  const exportData = {
    stepNumber: index + 1,
    stepName: step.stepName,
    stepDescription: step.stepDescription,
    formula: step.formula,
    data: step.calculationResult.data,
    columns: step.calculationResult.columns
    // summary已移除
  }
  
  handleExportResult(exportData)
}

// 转换为CSV格式
const convertToCSV = (exportData: any) => {
  let csv = `步骤编号,${exportData.stepNumber}\n`
  csv += `步骤名称,${exportData.stepName}\n`
  csv += `步骤描述,${exportData.stepDescription}\n`
  csv += `计算公式,${exportData.formula || '无'}\n\n`
  
  // 添加表头
  const headers = exportData.columns.map((col: any) => col.label).join(',')
  csv += headers + '\n'
  
  // 添加数据行
  exportData.data.forEach((row: any) => {
    const values = exportData.columns.map((col: any) => row[col.prop] || '').join(',')
    csv += values + '\n'
  })
  
  // 汇总信息已移除
  
  return csv
}

// 设置默认值
const setDefaultValues = async () => {
  // 等待数据加载完成
  await Promise.all([
    getEvaluationModels()
  ])

  // 设置默认权重配置为第一项

  const storedOrg = globalOrganizationStore.selectedOrganization
  const shouldUseCapacityPreset = Number(storedOrg?.level) === 2 || Number(storedOrg?.level) === 3
  const preferredCapacityModel = storedOrg?.preferredCapacityModel
  const targetCapacityModel = shouldUseCapacityPreset
    ? (
      preferredCapacityModel === 'enterprise'
        ? (findEnterpriseModel(evaluationForm.year) || findGovernmentModel(evaluationForm.year) || findSocialOrganizationModel(evaluationForm.year) || findFamilyModel(evaluationForm.year))
        : preferredCapacityModel === 'social-organization'
          ? (findSocialOrganizationModel(evaluationForm.year) || findGovernmentModel(evaluationForm.year) || findEnterpriseModel(evaluationForm.year) || findFamilyModel(evaluationForm.year))
          : preferredCapacityModel === 'family'
            ? (findFamilyModel(evaluationForm.year) || findGovernmentModel(evaluationForm.year) || findEnterpriseModel(evaluationForm.year) || findSocialOrganizationModel(evaluationForm.year))
            : preferredCapacityModel === 'community'
              ? (Number(storedOrg?.level) === 2 ? (evaluationModels.value.find((m: any) => m.id === 17) || evaluationModels.value.find((m: any) => m.id === 4)) : (evaluationModels.value.find((m: any) => m.id === 17) || evaluationModels.value.find((m: any) => m.id === 4) || evaluationModels.value.find((m: any) => m.id === 8)))
            : preferredCapacityModel === 'township'
              ? (Number(storedOrg?.level) === 2 ? (evaluationModels.value.find((m: any) => m.id === 19) || evaluationModels.value.find((m: any) => m.id === 3)) : (evaluationModels.value.find((m: any) => m.id === 3) || evaluationModels.value.find((m: any) => m.id === 11)))
              : (findGovernmentModel(evaluationForm.year) || findEnterpriseModel(evaluationForm.year) || findSocialOrganizationModel(evaluationForm.year) || findFamilyModel(evaluationForm.year))
    )
    : null

  if (targetCapacityModel) {
    evaluationForm.modelId = targetCapacityModel.id
    if (preferredCapacityModel === 'community' || targetCapacityModel.id === 17 || targetCapacityModel.id === 4 || targetCapacityModel.id === 8) {
      evaluationForm.dataType = 'community'
    } else if (preferredCapacityModel === 'family') {
      evaluationForm.dataType = 'family'
    } else {
      evaluationForm.dataType = 'township'
    }
    evaluationForm.dataSource = 'REGION'
  } else if (evaluationModels.value.length > 0) {
    evaluationForm.modelId = evaluationModels.value[0].id
  }

  await getRegionTreeData()

  if (storedOrg?.code) {
    const matchedNode = findRegionNodeByCode(regionTreeData.value, String(storedOrg.code))
    if (matchedNode) {
      await handleRegionTreeChange(matchedNode.code)
      return
    }
  }

  await getProvinces(false)

  if (storedOrg) {
    console.log('从全局 store 恢复组织机构:', storedOrg)
    await restoreOrganizationSelection(storedOrg)
    return
  }

  if (provinces.value.length > 0) {
    await handleProvinceChange((provinces.value as any[])[0].name)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getEvaluationHistory()
  // 设置默认值
  setDefaultValues()
})

// 监听评估表单变化，自动更新筛选条件
watch(
  () => [evaluationForm.year, evaluationForm.selectedCounty, evaluationForm.orgCode, evaluationForm.modelId],
  ([year, countyName, orgCode, modelId], [oldYear, oldCountyName, oldOrgCode, oldModelId]) => {
    console.log('评估表单变化:', { year, countyName, orgCode, modelId })

    // 自动生成评估名称（当年份、模型或区县发生变化时）
    if ((year && year !== oldYear) || (modelId !== undefined && modelId !== oldModelId) || (evaluationForm.selectedCounty && evaluationForm.selectedCounty !== oldCountyName)) {
      generateEvaluationName()
    }

    // 更新筛选条件中的年份
    if (year && year !== oldYear) {
      // 更新全局年份 store
      globalYearStore.setYear(year)

      filterForm.year = String(year)
      currentPage.value = 1
      getEvaluationHistory(1, pageSize.value)
      resetRegionSelect()
      getProvinces()
      getRegionTreeData()
    }

    // 更新筛选条件中的区县（使用机构代码）
    if (orgCode && orgCode !== oldOrgCode) {
      // 根据机构代码查找区县名称
      const selectedCountyObj = counties.value.find((c: any) => c.code === orgCode)
      filterForm.county = selectedCountyObj?.name || ''
      currentPage.value = 1
      getEvaluationHistory(1, pageSize.value)
    }

    // 更新筛选条件中的模型ID
    if (modelId !== undefined && modelId !== oldModelId) {
      filterForm.modelId = modelId ? Number(modelId) : null
      currentPage.value = 1
      getEvaluationHistory(1, pageSize.value)
    }
  },
  { deep: true }
)
</script>

<style scoped>
.evaluation {
  padding: 20px;
  max-width: 1920px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  text-align: left;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  color: #6b7280;
  font-size: 16px;
}

.config-card,
.progress-card,
.history-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.evaluation-form {
  padding: 20px 0;
}

.param-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.algorithm-info {
  margin-bottom: 20px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.algorithm-info h4 {
  color: #1f2937;
  margin-bottom: 8px;
}

.algorithm-info p {
  color: #6b7280;
  margin: 0;
}

.algorithm-steps {
  margin-bottom: 24px;
  position: relative;
}

.steps-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.algorithm-steps h5 {
  color: #374151;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

/* 水平布局容器 */
.steps-horizontal-container {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.step-item-horizontal {
  flex: 0 0 280px;
  min-width: 280px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.step-item-horizontal:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

/* 保留原有垂直布局样式以兼容其他地方 */
.step-item {
  margin-bottom: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.step-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.step-layout {
  display: block;
}

.step-content-wrapper {
  width: 100%;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  position: relative;
}

.step-header:hover {
  background-color: #f8fafc;
  border-radius: 6px;
  padding: 8px;
  margin: -8px;
}

.step-number {
  background: #3b82f6;
  color: white;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}

.step-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.step-content {
  padding-left: 0;
}

.step-description {
  color: #6b7280;
  margin-bottom: 8px;
  line-height: 1.5;
}

.step-parameters {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 12px;
}

.step-parameters strong {
  color: #374151;
}

.step-actions {
  margin-top: 12px;
}

.common-parameters {
  margin-top: 24px;
}

.common-parameters h5 {
  color: #374151;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.progress-content {
  padding: 20px;
}

.progress-info {
  margin-top: 16px;
  text-align: center;
}

.progress-info p {
  margin: 8px 0;
  color: #6b7280;
}

.progress-actions {
  margin-top: 20px;
  text-align: center;
}

.progress-actions .el-button {
  margin: 0 8px;
}

.no-steps {
  text-align: center;
  padding: 40px 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 评估历史筛选区域 */
.history-filters {
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .evaluation {
    padding: 16px;
  }
  
  .step-layout {
    flex-direction: column;
    gap: 16px;
  }
  
  /* 移动端步骤布局调整 */
  .steps-horizontal-container {
    flex-direction: column;
    overflow-x: visible;
  }
  
  .step-item-horizontal {
    flex: none;
    min-width: auto;
    width: 100%;
  }
}
</style>
