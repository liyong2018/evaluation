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
              <el-input v-model="evaluationForm.name" placeholder="请输入评估名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权重配置" prop="weightConfigId">
              <el-select v-model="evaluationForm.weightConfigId" placeholder="选择权重配置">
                <el-option
                  v-for="config in weightConfigs"
                  :key="config.id"
                  :label="config.name"
                  :value="config.id"
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
            <el-form-item label="数据类型" prop="dataType">
              <el-radio-group v-model="evaluationForm.dataType" @change="handleDataTypeChange">
                <el-radio label="township">乡镇数据</el-radio>
                <el-radio label="community">社区数据</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评估算法" prop="algorithmId">
              <el-select v-model="evaluationForm.algorithmId" placeholder="选择评估算法">
                <el-option
                  v-for="algorithm in algorithmConfigs"
                  :key="algorithm.id"
                  :label="algorithm.configName"
                  :value="algorithm.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 留空或者可以添加其他配置 -->
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="选择省份" prop="province">
              <el-select
                v-model="evaluationForm.selectedProvince"
                placeholder="请选择省份"
                style="width: 100%"
                @change="handleProvinceChange"
                clearable
              >
                <el-option
                  v-for="province in provinces"
                  :key="province.code"
                  :label="province.name"
                  :value="province.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="选择城市" prop="city">
              <el-select
                v-model="evaluationForm.selectedCity"
                placeholder="请选择城市"
                style="width: 100%"
                @change="handleCityChange"
                :disabled="!evaluationForm.selectedProvince"
                clearable
              >
                <el-option
                  v-for="city in cities"
                  :key="city.code"
                  :label="city.name"
                  :value="city.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="选择区县" prop="county">
              <el-select
                v-model="evaluationForm.selectedCounty"
                placeholder="请选择区县"
                style="width: 100%"
                @change="handleCountyChange"
                :disabled="!evaluationForm.selectedCity"
                clearable
              >
                <el-option
                  v-for="county in counties"
                  :key="county.code"
                  :label="county.name"
                  :value="county.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="算法参数" v-if="selectedAlgorithm">
          <el-card class="param-card">
            <div class="algorithm-info">
              <h4>{{ selectedAlgorithm.configName }}</h4>
              <p>{{ selectedAlgorithm.description }}</p>
            </div>
            
            <!-- 算法步骤和公式展示 -->
            <div class="algorithm-steps">
              <div class="steps-header">
                <h5>算法步骤和公式：</h5>
                <el-button 
                  type="success" 
                  size="small" 
                  @click="viewAllStepsResults"
                  :disabled="!evaluationForm.algorithmId || !evaluationForm.regions || evaluationForm.regions.length === 0"
                >
                  <el-icon><DataAnalysis /></el-icon>
                  查看所有步骤结果
                </el-button>
              </div>
              <div v-if="algorithmSteps.length > 0" class="steps-horizontal-container">
                <div v-for="(step, index) in algorithmSteps" :key="step.id" class="step-item-horizontal">
                  <div class="step-layout">
                    <!-- 步骤信息 -->
                    <div class="step-content-wrapper">
                      <div 
                        class="step-header"
                        @mouseenter="showFormulaTooltip(step, $event)"
                        @mouseleave="hideFormulaTooltip"
                      >
                        <span class="step-number">步骤 {{ index + 1 }}</span>
                        <span class="step-name">{{ step.stepName }}</span>
                      </div>
                      <div class="step-content">
                        <p class="step-description">{{ step.stepDescription }}</p>
                        <div v-if="step.parameters" class="step-parameters">
                          <strong>参数说明：</strong>
                          <span>{{ step.parameters }}</span>
                        </div>
                        <!-- 查看结果按钮移到步骤描述下面 -->
                        <div class="step-actions">
                          <el-button 
                            type="primary" 
                            size="small" 
                            @click="calculateStepResult(step, index)"
                            :loading="step.calculating"
                          >
                            查看结果
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="no-steps">
                <el-empty description="请先选择算法以查看步骤和公式" :image-size="80" />
              </div>
            </div>
            
            <!-- 公式悬停提示框 -->
            <div 
              v-if="formulaTooltip.visible" 
              class="formula-tooltip"
              :style="{
                left: formulaTooltip.x + 'px',
                top: formulaTooltip.y + 'px'
              }"
            >
              <div class="tooltip-header">
                <strong>{{ formulaTooltip.step?.stepName }} - 公式</strong>
              </div>
              <div class="tooltip-content">
                <div v-if="formulaTooltip.step?.formula" class="formula-display">
                  <code class="formula-code">{{ formulaTooltip.step.formula }}</code>
                </div>
                <div v-else class="no-formula">
                  <span class="text-muted">暂无公式</span>
                </div>
                <div v-if="formulaTooltip.step?.formulaDescription" class="formula-description">
                  <p>{{ formulaTooltip.step.formulaDescription }}</p>
                </div>
              </div>
            </div>
            
            <!-- 通用参数配置 -->
            <!-- <div class="common-parameters">
              <h5>通用参数配置：</h5>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="一致性比率阈值">
                    <el-input-number
                      v-model="evaluationForm.parameters.crThreshold"
                      :min="0"
                      :max="1"
                      :step="0.01"
                      :precision="3"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="最大迭代次数">
                    <el-input-number
                      v-model="evaluationForm.parameters.maxIterations"
                      :min="1"
                      :max="1000"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="分辨系数">
                    <el-input-number
                      v-model="evaluationForm.parameters.resolution"
                      :min="0"
                      :max="1"
                      :step="0.01"
                      :precision="3"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="模糊化方法">
                    <el-select v-model="evaluationForm.parameters.fuzzyMethod">
                      <el-option label="三角模糊数" value="TRIANGULAR" />
                      <el-option label="梯形模糊数" value="TRAPEZOIDAL" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="合成算子">
                    <el-select v-model="evaluationForm.parameters.operator">
                      <el-option label="加权平均" value="WEIGHTED_AVERAGE" />
                      <el-option label="最大最小" value="MAX_MIN" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="归一化方法">
                    <el-select v-model="evaluationForm.parameters.normalization">
                      <el-option label="极值标准化" value="MIN_MAX" />
                      <el-option label="Z-score标准化" value="Z_SCORE" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div> -->
          </el-card>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="evaluationForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入评估描述"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="startEvaluation" :loading="loading.evaluation">
            <el-icon><VideoPlay /></el-icon>
            开始评估
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
    <!-- <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>评估历史</span>
          <el-button type="text" @click="refreshHistory">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading.history"
        :data="evaluationHistory"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="评估名称" width="200" />
        <el-table-column prop="algorithm" label="算法" width="120" />
        <el-table-column prop="dataCount" label="数据量" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewEvaluationDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="rerunEvaluation(row)"
              :disabled="row.status === 'RUNNING'"
            >
              <el-icon><Refresh /></el-icon>
              重新计算
            </el-button>
            <el-button type="danger" size="small" @click="deleteEvaluation(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card> -->

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

    <!-- 计算结果弹窗 -->
    <ResultDialog
      v-model="resultDialogVisible"
      :step-info="currentStepInfo"
      :result-data="currentCalculationResult"
      :formula="currentStepInfo?.formula"
      :model-id="evaluationForm.modelId"
      :algorithm-id="evaluationForm.algorithmId"
      @export="handleExportResult"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  Refresh,
  VideoPlay,
  Check,
  View,
  Download,
  Delete
} from '@element-plus/icons-vue'
import { evaluationApi, weightConfigApi, surveyDataApi, regionApi, algorithmConfigApi, algorithmExecutionApi, algorithmManagementApi, modelManagementApi, algorithmStepExecutionApi, communityCapacityApi, regionDataApi } from '@/api'
import ResultDialog from '@/components/ResultDialog.vue'

// 处理ResizeObserver警告
const originalError = console.error
console.error = (...args) => {
  if (args[0]?.includes?.('ResizeObserver loop completed with undelivered notifications')) {
    return
  }
  originalError(...args)
}

const router = useRouter()

// 计算属性
const selectedAlgorithm = computed(() => {
  return algorithmConfigs.value.find(config => config.id === evaluationForm.algorithmId)
})

// 响应式数据
const evaluationFormRef = ref<FormInstance>()
const weightConfigs = ref<any[]>([])
const algorithmConfigs = ref<any[]>([])
const evaluationModels = ref<any[]>([])
const algorithmSteps = ref<any[]>([])
const regionTreeData = ref<any[]>([])
const evaluationHistory = ref<any[]>([])
const previewData = ref<any[]>([])

// 三级联动数据
const provinces = ref<any[]>([])
const cities = ref<any[]>([])
const counties = ref<any[]>([])

const loading = reactive({
  evaluation: false,
  history: false
})

const dialogVisible = reactive({
  preview: false
})

// 计算结果弹窗相关数据
const resultDialogVisible = ref(false)
const currentStepInfo = ref(null)
const currentCalculationResult = ref(null)

// 公式悬停提示框数据
const formulaTooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  step: null
})

const evaluationForm = reactive({
  name: '',
  modelId: null,
  weightConfigId: null,
  algorithmId: null,
  dataType: 'township', // 默认选择乡镇数据
  dataSource: 'REGION',
  regions: [],
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

const evaluationProgress = reactive({
  visible: false,
  percentage: 0,
  status: 'success' as 'success' | 'exception' | 'warning',
  message: '',
  detail: ''
})

const evaluationRules = {
  name: [{ required: true, message: '请输入评估名称', trigger: 'blur' }],
  weightConfigId: [{ required: true, message: '请选择权重配置', trigger: 'change' }]
}

// 获取权重配置列表
const getWeightConfigs = async () => {
  try {
    const response = await weightConfigApi.getActive()
    if (response.success) {
      weightConfigs.value = response.data || []
    }
  } catch (error) {
    console.error('获取权重配置失败:', error)
  }
}

// 获取地区树形数据
const getRegionTreeData = async () => {
  try {
    let response
    let dataKey

    // 根据数据类型选择不同的API
    if (evaluationForm.dataType === 'community') {
      // 社区数据：从community-capacity API获取数据
      response = await communityCapacityApi.getList({ page: 1, size: 1000 })
      dataKey = 'data.data' // 社区数据的嵌套结构
    } else {
      // 乡镇数据：从survey-data API获取数据
      response = await surveyDataApi.getAll()
      dataKey = 'data' // 乡镇数据的直接结构
    }

    if (response.code === 200) {
      // 根据数据类型选择正确的数据路径
      const rawData = dataKey === 'data.data' ? response.data.data : response[dataKey]
      if (!rawData || rawData.length === 0) {
        ElMessage.warning('未找到相关数据')
        return
      }

      // 将数据转换为地区树形结构
      const regionMap = new Map()
      const idToRegionCodeMap = new Map()

      rawData.forEach(item => {
        // 省级
        const provinceName = evaluationForm.dataType === 'community' ? item.provinceName : item.province
        if (provinceName && !regionMap.has(provinceName)) {
          regionMap.set(provinceName, {
            id: `province_${provinceName}`,
            name: provinceName,
            value: `province_${provinceName}`,
            level: 1,
            children: []
          })
        }

        // 市级
        const cityName = evaluationForm.dataType === 'community' ? item.cityName : item.city
        if (cityName && provinceName) {
          const cityKey = `${provinceName}_${cityName}`

          if (!regionMap.has(cityKey)) {
            const cityNode = {
              id: `city_${cityKey}`,
              name: cityName,
              value: `city_${cityKey}`,
              level: 2,
              children: []
            }

            regionMap.set(cityKey, cityNode)

            // 添加到省级节点
            const provinceNode = regionMap.get(provinceName)
            if (provinceNode) {
              provinceNode.children.push(cityNode)
            }
          }
        }

        // 县级
        const countyName = evaluationForm.dataType === 'community' ? item.countyName : item.county
        if (countyName && cityName && provinceName) {
          const countyKey = `${provinceName}_${cityName}_${countyName}`

          if (!regionMap.has(countyKey)) {
            const countyNode = {
              id: `county_${countyKey}`,
              name: countyName,
              value: `county_${countyKey}`,
              level: 3,
              children: []
            }

            regionMap.set(countyKey, countyNode)

            // 添加到市级节点
            const cityNode = regionMap.get(`${provinceName}_${cityName}`)
            if (cityNode) {
              cityNode.children.push(countyNode)
            }
          }
        }

        // 乡镇级/社区级
        if (evaluationForm.dataType === 'community') {
          // 社区级
          if (item.communityName && item.townshipName && countyName && cityName && provinceName) {
            const townshipKey = `${provinceName}_${cityName}_${countyName}_${item.townshipName}`
            const communityKey = `${provinceName}_${cityName}_${countyName}_${item.townshipName}_${item.communityName}`

            if (!regionMap.has(communityKey)) {
              const communityNode = {
                id: `community_${communityKey}`,
                name: item.communityName,
                value: `community_${communityKey}`,
                level: 5,
                regionCode: item.regionCode,
                children: []
              }

              regionMap.set(communityKey, communityNode)

              // 确保乡镇节点存在
              if (!regionMap.has(townshipKey)) {
                const townshipNode = {
                  id: `township_${townshipKey}`,
                  name: item.townshipName,
                  value: `township_${townshipKey}`,
                  level: 4,
                  children: []
                }
                regionMap.set(townshipKey, townshipNode)

                // 添加到县级节点
                const countyNode = regionMap.get(`${provinceName}_${cityName}_${countyName}`)
                if (countyNode) {
                  countyNode.children.push(townshipNode)
                }
              }

              // 添加社区节点到乡镇节点
              const townshipNode = regionMap.get(townshipKey)
              if (townshipNode) {
                townshipNode.children.push(communityNode)
              }

              // 保存映射
              idToRegionCodeMap.set(`community_${communityKey}`, item.regionCode)
            }
          }
        } else {
          // 乡镇级
          if (item.township && countyName && cityName && provinceName) {
            const townshipKey = `${provinceName}_${cityName}_${countyName}_${item.township}`

            if (!regionMap.has(townshipKey)) {
              const townshipNode = {
                id: `township_${townshipKey}`,
                name: item.township,
                value: `township_${townshipKey}`,
                level: 4,
                regionCode: item.regionCode,
                children: []
              }

              regionMap.set(townshipKey, townshipNode)

              // 添加到县级节点
              const countyNode = regionMap.get(`${provinceName}_${cityName}_${countyName}`)
              if (countyNode) {
                countyNode.children.push(townshipNode)
              }

              // 保存映射
              idToRegionCodeMap.set(`township_${townshipKey}`, item.regionCode)
            }
          }
        }
      })

      // 将映射保存到全局变量
      window.__regionCodeMap = idToRegionCodeMap

      // 提取省级节点作为根节点
      const treeData = []
      regionMap.forEach((value, key) => {
        if (value.level === 1) {
          treeData.push(value)
        }
      })

      regionTreeData.value = treeData
      console.log(`成功获取${evaluationForm.dataType === 'community' ? '社区' : '乡镇'}地区树数据:`, {
        dataType: evaluationForm.dataType,
        totalRegions: rawData.length,
        treeNodes: treeData.length
      })
    } else {
      ElMessage.error(response.message || '获取地区数据失败')
    }
  } catch (error) {
    console.error('获取地区数据失败:', error)
    ElMessage.error('获取地区数据失败')
  }
}

// 获取算法配置列表
const getAlgorithmConfigs = async () => {
  try {
    const response = await algorithmConfigApi.getAll()
    if (response.success) {
      algorithmConfigs.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取算法配置失败')
    }
  } catch (error) {
    console.error('获取算法配置失败:', error)
    ElMessage.error('获取算法配置失败')
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
const handleModelChange = (modelId: number) => {
  console.log('模型变化:', modelId)
  // 如果需要，可以清空算法选择
  // evaluationForm.algorithmId = null
}

// 处理数据类型变化
const handleDataTypeChange = () => {
  console.log('数据类型变化:', evaluationForm.dataType)
  // 清空三级联动数据
  evaluationForm.selectedProvince = ''
  evaluationForm.selectedCity = ''
  evaluationForm.selectedCounty = ''
  evaluationForm.countyData = []
  evaluationForm.regions = []
  // 清空下拉框数据
  provinces.value = []
  cities.value = []
  counties.value = []
  // 重新获取省份数据
  getProvinces()
}

// 获取省份列表
const getProvinces = async () => {
  try {
    const response = await regionDataApi.getProvinces(evaluationForm.dataType)
    if (response.code === 200) {
      provinces.value = response.data || []
      console.log('获取到省份列表:', provinces.value)
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
  // 清空城市和区县选择
  evaluationForm.selectedCity = ''
  evaluationForm.selectedCounty = ''
  evaluationForm.countyData = []
  evaluationForm.regions = []
  cities.value = []
  counties.value = []

  if (provinceName) {
    // 获取城市列表
    try {
      const response = await regionDataApi.getCities(evaluationForm.dataType, provinceName)
      if (response.code === 200) {
        cities.value = response.data || []
        console.log('获取到城市列表:', cities.value)
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
  counties.value = []

  if (cityName && evaluationForm.selectedProvince) {
    // 获取区县列表
    try {
      const response = await regionDataApi.getCounties(evaluationForm.dataType, evaluationForm.selectedProvince, cityName)
      if (response.code === 200) {
        counties.value = response.data || []
        console.log('获取到区县列表:', counties.value)
      } else {
        ElMessage.error(response.message || '获取区县列表失败')
      }
    } catch (error) {
      console.error('获取区县列表失败:', error)
      ElMessage.error('获取区县列表失败')
    }
  }
}

// 处理区县变化
const handleCountyChange = async (countyName: string) => {
  console.log('区县变化:', countyName)
  // 清空之前的数据
  evaluationForm.countyData = []
  evaluationForm.regions = []

  if (countyName && evaluationForm.selectedProvince && evaluationForm.selectedCity) {
    // 获取该县的数据
    try {
      const response = await regionDataApi.getDataByCounty(
        evaluationForm.dataType,
        evaluationForm.selectedProvince,
        evaluationForm.selectedCity,
        countyName
      )
      if (response.code === 200) {
        evaluationForm.countyData = response.data || []
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

// 获取算法步骤和公式
const getAlgorithmSteps = async (algorithmId: number) => {
  try {
    const response = await algorithmManagementApi.getAlgorithmStepsAndFormulas(algorithmId)
    if (response.success) {
      const steps = response.data || []
      
      // 处理每个步骤的公式数据
      steps.forEach((step, index) => {
        // 如果步骤有关联的公式配置
        if (step.formulas && step.formulas.length > 0) {
          // 取第一个公式作为主要公式
          const mainFormula = step.formulas[0]
          step.formula = mainFormula.formulaExpression || mainFormula.formula_expression || ''
          step.formulaName = mainFormula.formulaName || mainFormula.formula_name || ''
          step.formulaDescription = mainFormula.description || ''
        } else {
          // 如果没有关联公式，尝试从步骤本身获取公式信息
          step.formula = step.formulaExpression || step.formula_expression || ''
          step.formulaName = step.formulaName || step.formula_name || ''
          step.formulaDescription = step.formulaDescription || step.formula_description || ''
        }
        
        // 根据用户需求调整步骤3-5的逻辑
        if (index === 2) { // 步骤3：定权
          step.stepName = '乡镇减灾能力定权'
          step.stepDescription = '根据不同的逻辑分别计算一级指标公式和乡镇（街道）减灾能力公式'
          step.formula = '一级指标权重计算 + 乡镇减灾能力权重计算'
          step.formulaDescription = '分别计算灾害管理能力、灾害备灾能力、自救转移能力的权重，以及综合减灾能力权重'
        } else if (index === 3) { // 步骤4：优劣解算
          step.stepName = '优劣解算法计算'
          step.stepDescription = '分别计算一级指标优劣和乡镇（街道）减灾能力优劣'
          step.formula = `一级指标优劣计算：
灾害管理能力（优）=SQRT((队伍管理能力最大值-本乡镇队伍管理能力)²+(风险评估能力最大值-本乡镇风险评估能力)²+(财政投入能力最大值-本乡镇财政投入能力)²)
灾害备灾能力（优）=SQRT((物资储备能力最大值-本乡镇物资储备能力)²+(医疗保障能力最大值-本乡镇医疗保障能力)²)
自救转移能力（优）=SQRT((自救互救能力最大值-本乡镇自救互救能力)²+(公众避险能力最大值-本乡镇公众避险能力)²+(转移安置能力最大值-本乡镇转移安置能力)²)

乡镇减灾能力优劣计算：
乡镇名称（优）=SQRT((队伍管理能力最大值-本乡镇队伍管理能力)²+...+所有8个能力指标)
乡镇名称（差）=对应使用最小值计算`
          step.formulaDescription = '使用TOPSIS方法计算各指标与理想解和负理想解的距离'
        } else if (index === 4) { // 步骤5：能力分级
          step.stepName = '减灾能力分级计算'
          step.stepDescription = '分别计算一级指标能力值和乡镇（街道）减灾能力值，并进行分级'
          step.formula = `能力值计算：
灾害管理能力 = 灾害管理能力（差）/(灾害管理能力（差）+灾害管理能力（优）)
灾害备灾能力 = 灾害备灾能力（差）/(灾害备灾能力（差）+灾害备灾能力（优）)
自救转移能力 = 自救转移能力（差）/(自救转移能力（差）+自救转移能力（优）)

分级计算：
均值μ = AVERAGE(能力值)
标准差σ = STDEV.S(能力值)
分级 = IF条件判断（强、较强、中等、较弱、弱）`
          step.formulaDescription = '基于TOPSIS结果计算最终能力值，并使用统计学方法进行五级分类'
        }
      })
      
      algorithmSteps.value = steps
    } else {
      algorithmSteps.value = []
      console.error('获取算法步骤失败:', response.message)
    }
  } catch (error) {
    algorithmSteps.value = []
    console.error('获取算法步骤失败:', error)
  }
}

// 获取评估历史
const getEvaluationHistory = async () => {
  loading.history = true
  try {
    // 暂时使用第一个调查数据的ID，实际应该根据选择的调查数据来获取
    const surveyResponse = await surveyDataApi.getAll()
    if (surveyResponse.success && surveyResponse.data && surveyResponse.data.length > 0) {
      const surveyId = surveyResponse.data[0].id
      const response = await evaluationApi.getHistory(surveyId)
      if (response.success) {
        evaluationHistory.value = response.data || []
      } else {
        ElMessage.error(response.message || '获取评估历史失败')
      }
    } else {
      evaluationHistory.value = []
    }
  } catch (error) {
    console.error('获取评估历史失败:', error)
    ElMessage.error('获取评估历史失败')
  } finally {
    loading.history = false
  }
}

// 刷新历史记录
const refreshHistory = () => {
  getEvaluationHistory()
}

// 重置评估表单
const resetEvaluationForm = () => {
  Object.assign(evaluationForm, {
    name: '',
    modelId: null,
    weightConfigId: null,
    algorithmId: null,
    dataType: 'township', // 重置为默认乡镇数据
    dataSource: 'REGION',
    regions: [],
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
      const response = await evaluationApi.validateParams(evaluationForm)
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
    if (!evaluationForm.weightConfigId) {
      ElMessage.error('请选择权重配置')
      return
    }

    if (!evaluationForm.regions || evaluationForm.regions.length === 0) {
      ElMessage.error('请选择评估地区')
      return
    }

    // 如果选择了模型，使用模型执行
    if (evaluationForm.modelId) {
      await executeModelEvaluation()
    } else if (evaluationForm.algorithmId) {
      // 否则使用算法执行
      await executeAlgorithmEvaluation()
    } else {
      ElMessage.error('请选择评估模型或评估算法')
    }
  })
}

// 执行模型评估
const executeModelEvaluation = async () => {
  loading.evaluation = true
  evaluationProgress.visible = true
  evaluationProgress.percentage = 0
  evaluationProgress.status = 'success'
  evaluationProgress.message = '正在执行评估模型...'

  try {
    // 提取地区代码（从选择的ID中提取region_code）
    const regionCodes = evaluationForm.regions.map((regionId: string) => {
      // regionId格式可能是 "township_province_city_county_township"
      // 我们需要提取实际的region_code
      return extractRegionCode(regionId)
    })

    evaluationProgress.percentage = 20
    evaluationProgress.detail = '加载模型配置...'

    // 调用模型执行 API
    const response = await evaluationApi.executeModel(
      evaluationForm.modelId,
      regionCodes,
      evaluationForm.weightConfigId
    )

    evaluationProgress.percentage = 80

    if (response.success && response.data) {
      evaluationProgress.percentage = 90
      
      console.log('=== execute-model 返回的数据 ===')
      console.log('response.data:', response.data)
      console.log('数据结构:', {
        hasColumns: !!response.data?.columns,
        hasTableData: !!response.data?.tableData,
        columnsLength: response.data?.columns?.length,
        tableDataLength: response.data?.tableData?.length,
        sampleColumns: response.data?.columns?.slice(0, 3)
      })
      
      evaluationProgress.percentage = 100
      evaluationProgress.status = 'success'
      evaluationProgress.message = '评估执行完成'
      evaluationProgress.detail = ''

      // 直接使用 execute-model 返回的数据（已包含 columns 和 tableData）
      displayModelResults(response.data)
      ElMessage.success('评估执行成功')
    } else {
      throw new Error(response.message || '模型执行失败')
    }
  } catch (error: any) {
    console.error('执行评估模型失败:', error)
    evaluationProgress.percentage = 100
    evaluationProgress.status = 'exception'
    evaluationProgress.message = '评估执行失败'
    evaluationProgress.detail = error.message || '未知错误'
    ElMessage.error(error.message || '执行评估模型失败')
  } finally {
    loading.evaluation = false
  }
}

// 执行算法评估（原有逻辑）
const executeAlgorithmEvaluation = async () => {
  // 获取算法步骤
  if (algorithmSteps.value.length === 0) {
    await getAlgorithmSteps(evaluationForm.algorithmId)
  }

  if (algorithmSteps.value.length > 0) {
    // 找到第5步并执行
    const step5 = algorithmSteps.value[4]
    if (step5) {
      await calculateStepResult(step5, 4)
    } else {
      ElMessage.error('未找到第5步的配置')
    }
  } else {
    ElMessage.error('未找到算法步骤配置')
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
    columnsDetail: resultData?.columns
  })
  
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
    columnsWithStepOrder: currentCalculationResult.value.columns.filter(c => c.stepOrder !== undefined).length
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
  router.push(`/results?evaluationId=${row.id}`)
}

// 重新计算
const rerunEvaluation = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要重新计算这个评估吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await evaluationApi.rerun(row.id)
    if (response.success) {
      ElMessage.success('重新计算已开始')
      getEvaluationHistory()
    } else {
      ElMessage.error(response.message || '重新计算失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新计算失败:', error)
      ElMessage.error('重新计算失败')
    }
  }
}

// 删除评估
const deleteEvaluation = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个评估记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await evaluationApi.deleteResult(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      getEvaluationHistory()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评估失败:', error)
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

// 监控执行进度
const monitorExecutionProgress = async (executionId: string) => {
  const checkProgress = async () => {
    try {
      const response = await algorithmExecutionApi.getProgress(executionId)
      if (response.success && response.data) {
        const progress = response.data
        
        evaluationProgress.percentage = progress.percentage || 0
        evaluationProgress.message = progress.message || '执行中...'
        evaluationProgress.detail = progress.detail || ''
        
        if (progress.status === 'SUCCESS') {
          evaluationProgress.status = 'success'
          ElMessage.success('算法执行完成')
        } else if (progress.status === 'FAILED') {
          evaluationProgress.status = 'exception'
          ElMessage.error('算法执行失败')
        } else if (progress.status === 'RUNNING') {
          // 继续监控
          setTimeout(checkProgress, 1000)
        }
      }
    } catch (error) {
      console.error('获取执行进度失败:', error)
    }
  }
  
  // 开始监控
  checkProgress()
}

// 查看所有步骤的结果
const viewAllStepsResults = async () => {
  if (!evaluationForm.algorithmId) {
    ElMessage.warning('请先选择算法')
    return
  }
  
  if (!evaluationForm.regions || evaluationForm.regions.length === 0) {
    ElMessage.warning('请先选择地区')
    return
  }
  
  try {
    // 提取地区代码
    const regionCodes = evaluationForm.regions.map(regionId => extractRegionCode(regionId))
    
    console.log('=== 开始获取所有步骤结果 ===', {
      algorithmId: evaluationForm.algorithmId,
      regionCodes,
      weightConfigId: evaluationForm.weightConfigId
    })
    
    // 首先获取算法步骤信息
    const stepsResponse = await algorithmStepExecutionApi.getAlgorithmSteps(evaluationForm.algorithmId)
    if (!stepsResponse.success || !stepsResponse.data.steps) {
      ElMessage.error('获取算法步骤信息失败')
      return
    }
    
    const algorithmSteps = stepsResponse.data.steps
    console.log('获取到算法步骤:', algorithmSteps)
    
    // 执行所有步骤获取结果
    const stepResultPromises = algorithmSteps.map(async (step: any) => {
      try {
        const response = await algorithmStepExecutionApi.executeStep(
          evaluationForm.algorithmId!,
          step.stepOrder,
          {
            regionCodes,
            weightConfigId: evaluationForm.weightConfigId
          }
        )
        
        if (response.success) {
          return response.data
        } else {
          console.error(`步骤${step.stepOrder}执行失败:`, response.message)
          return null
        }
      } catch (error) {
        console.error(`步骤${step.stepOrder}执行异常:`, error)
        return null
      }
    })
    
    const stepResults = await Promise.all(stepResultPromises)
    const validStepResults = stepResults.filter(result => result !== null)
    
    if (validStepResults.length === 0) {
      ElMessage.error('所有步骤执行失败')
      return
    }
    
    console.log('获取到的步骤结果:', validStepResults)
    console.log('每个步骤结果的详细信息:')
    validStepResults.forEach((result, index) => {
      console.log(`步骤${index + 1}:`, {
        stepOrder: result.stepOrder,
        stepName: result.stepName,
        hasTableData: !!result.tableData,
        tableDataLength: result.tableData?.length,
        keys: Object.keys(result)
      })
    })
    
    // 设置弹窗数据
    currentStepInfo.value = {
      stepNumber: 0,
      stepName: '算法步骤执行结果',
      description: `共执行了 ${validStepResults.length} 个步骤`,
      stepCode: 'multi_steps',
      formula: '',
      formulaName: '',
      formulaDescription: ''
    }
    
    const multiStepData = {
      isMultiStep: true,
      stepResults: validStepResults
    }
    
    console.log('准备传递给ResultDialog的数据:', {
      isMultiStep: multiStepData.isMultiStep,
      stepResultsCount: multiStepData.stepResults.length,
      firstStepKeys: Object.keys(multiStepData.stepResults[0])
    })
    
    currentCalculationResult.value = multiStepData
    
    resultDialogVisible.value = true
    ElMessage.success(`成功获取 ${validStepResults.length} 个步骤的结果`)
    
  } catch (error) {
    console.error('获取步骤结果失败:', error)
    ElMessage.error('获取步骤结果失败')
  }
}

// 计算步骤结果
const calculateStepResult = async (step: any, index: number) => {
  console.log('=== 开始计算步骤结果 ===', {
    stepIndex: index,
    stepName: step.stepName,
    stepId: step.id,
    algorithmId: evaluationForm.algorithmId
  })
  
  if (!evaluationForm.algorithmId) {
    ElMessage.warning('请先选择算法')
    return
  }
  
  // 设置计算状态
  step.calculating = true
  
  try {
    // 准备计算参数
    const calculationParams = {
      algorithmId: evaluationForm.algorithmId,
      stepId: step.id,
      stepIndex: index,
      regions: evaluationForm.regions,
      parameters: evaluationForm.parameters,
      formula: step.formula
    }
    
    // 调用后端API计算步骤结果
    const response = await algorithmExecutionApi.calculateStepResult(calculationParams)
    
    let calculationResult
    if (response.success && response.data) {
      // 检查是否为双表格数据结构
      if (response.data.isDualTable) {
        // 双表格数据结构
        calculationResult = {
          isDualTable: true,
          table1Data: response.data.table1Data || [],
          table1Columns: response.data.table1Columns || [],
          table1Summary: response.data.table1Summary || null,
          table2Data: response.data.table2Data || [],
          table2Columns: response.data.table2Columns || [],
          table2Summary: response.data.table2Summary || null
        }
        console.log('后端返回双表格数据:', {
          table1DataCount: calculationResult.table1Data.length,
          table2DataCount: calculationResult.table2Data.length,
          table1ColumnsCount: calculationResult.table1Columns.length,
          table2ColumnsCount: calculationResult.table2Columns.length
        })
      } else {
        // 单表格数据结构
        calculationResult = {
          tableData: response.data.tableData || [],
          columns: response.data.columns || [],
          summary: response.data.summary || null
        }
      }
      
      ElMessage.success(`步骤 ${index + 1} 计算完成`)
    } else {
      ElMessage.error(response.message || '计算失败')
      return
    }
    
    // 设置弹窗数据并显示
    currentStepInfo.value = {
      stepNumber: index + 1,
      stepName: step.stepName,
      description: step.stepDescription,
      stepCode: step.stepCode || '',
      formula: step.formula,
      formulaName: step.formulaName,
      formulaDescription: step.formulaDescription
    }
    currentCalculationResult.value = calculationResult
    resultDialogVisible.value = true
    
  } catch (error) {
    console.error('计算步骤结果失败:', error)
    console.error('请求错误:', error)
    
    // 如果API不存在，使用模拟数据
    console.log('API调用失败，使用模拟数据', { stepName: step.stepName, index })
    const mockResult = generateMockStepResult(step, index)
    
    console.log('模拟数据生成结果:', {
      tableDataLength: mockResult.tableData?.length,
      columnsLength: mockResult.columns?.length,
      columns: mockResult.columns?.map(col => ({ prop: col.prop, label: col.label })),
      firstRowData: mockResult.tableData?.[0]
    })
    
    // 设置弹窗数据并显示
    currentStepInfo.value = {
      stepNumber: index + 1,
      stepName: step.stepName,
      description: step.stepDescription,
      stepCode: step.stepCode || '',
      formula: step.formula,
      formulaName: step.formulaName,
      formulaDescription: step.formulaDescription
    }
    currentCalculationResult.value = mockResult
    resultDialogVisible.value = true
    
    ElMessage.success(`步骤 ${index + 1} 计算完成（模拟数据）`)
  } finally {
    step.calculating = false
  }
}

// 处理地区选择
const handleRegionCheck = (data: any, checked: boolean, indeterminate: boolean) => {
  console.log('地区选择事件:', { data: data.id, checked, indeterminate })

  // 获取当前选中的地区列表
  let currentRegions = [...(evaluationForm.regions || [])]

  if (checked) {
    // 选中节点时：只选择其直系子节点（不包括点击的节点本身）
    // 如果当前节点有子节点，选择所有直接子节点
    if (data.children && data.children.length > 0) {
      data.children.forEach(child => {
        if (!currentRegions.includes(child.id)) {
          currentRegions.push(child.id)
        }
      })
    }
  } else {
    // 取消选中节点时：只移除其直系子节点（不包括点击的节点本身）
    // 如果当前节点有子节点，移除所有直接子节点
    if (data.children && data.children.length > 0) {
      const childIdsToRemove = data.children.map(child => child.id)
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

// 显示公式悬停提示
const showFormulaTooltip = (step: any, event: MouseEvent) => {
  if (!step.formula) return
  
  formulaTooltip.step = step
  formulaTooltip.x = event.clientX + 10
  formulaTooltip.y = event.clientY + 10
  formulaTooltip.visible = true
}

// 隐藏公式悬停提示
const hideFormulaTooltip = () => {
  formulaTooltip.visible = false
  formulaTooltip.step = null
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

// 监听算法选择变化
watch(() => evaluationForm.algorithmId, (newAlgorithmId) => {
  if (newAlgorithmId) {
    getAlgorithmSteps(newAlgorithmId)
  } else {
    algorithmSteps.value = []
  }
})

// 设置默认值
const setDefaultValues = async () => {
  // 等待数据加载完成
  await Promise.all([
    getWeightConfigs(),
    getEvaluationModels()
  ])

  // 设置默认权重配置为第一项
  if (weightConfigs.value.length > 0) {
    evaluationForm.weightConfigId = weightConfigs.value[0].id
  }

  // 设置默认评估模型为第一项
  if (evaluationModels.value.length > 0) {
    evaluationForm.modelId = evaluationModels.value[0].id
  }

  // 等待省份数据加载完成后再设置默认地区
  await getProvinces()

  // 设置默认地区为四川-眉山-青神县
  if (provinces.value.length > 0) {
    // 查找四川省
    const sichuanProvince = provinces.value.find(p => p.name === '四川')
    if (sichuanProvince) {
      evaluationForm.selectedProvince = sichuanProvince.name
      // 获取四川省下的城市
      await handleProvinceChange(sichuanProvince.name)

      // 查找眉山市
      setTimeout(async () => {
        const meishanCity = cities.value.find(c => c.name === '眉山')
        if (meishanCity) {
          evaluationForm.selectedCity = meishanCity.name
          // 获取眉山市下的区县
          await handleCityChange(meishanCity.name)

          // 查找青神县
          setTimeout(() => {
            const qingshenCounty = counties.value.find(c => c.name === '青神')
            if (qingshenCounty) {
              evaluationForm.selectedCounty = qingshenCounty.name
              // 自动获取青神县的数据
              handleCountyChange(qingshenCounty.name)
            }
          }, 500)
        }
      }, 500)
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getWeightConfigs()
  getAlgorithmConfigs()
  getEvaluationModels()
  getEvaluationHistory()
  // 获取省份数据（替代原来的地区树数据）
  getProvinces()

  // 设置默认值
  setDefaultValues()
})
</script>

<style scoped>
.evaluation {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  text-align: left;
}

.page-header h1 {
  color: #1f2937;
  margin-bottom: 8px;
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

.formula-tooltip {
  position: fixed;
  z-index: 9999;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 400px;
  min-width: 250px;
  pointer-events: none;
}

.tooltip-header {
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  border-radius: 8px 8px 0 0;
}

.tooltip-header strong {
  color: #374151;
  font-size: 14px;
}

.tooltip-content {
  padding: 16px;
}

.formula-display {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
}

.formula-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #dc2626;
  background: transparent;
  border: none;
  word-break: break-all;
  white-space: pre-wrap;
}

.formula-description {
  margin-top: 8px;
}

.formula-description p {
  color: #6b7280;
  font-size: 13px;
  margin: 0;
  line-height: 1.4;
}

.no-formula {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-style: italic;
}

.text-muted {
  color: #9ca3af;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .evaluation {
    padding: 16px;
  }
  
  .step-layout {
    flex-direction: column;
    gap: 16px;
  }
  
  .formula-tooltip {
    max-width: 300px;
    min-width: 200px;
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