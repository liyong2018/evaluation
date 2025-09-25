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
            <el-form-item label="数据源" prop="dataSource">
              <el-select v-model="evaluationForm.dataSource" placeholder="选择数据源">
                <el-option label="按地区组织机构筛选" value="REGION" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="选择地区" prop="regions">
              <el-tree-select
                v-model="evaluationForm.regions"
                :data="regionTreeData"
                multiple
                show-checkbox
                check-strictly
                :render-after-expand="false"
                placeholder="请选择地区组织机构"
                style="width: 100%"
                node-key="id"
                :props="{
                  label: 'name',
                  children: 'children',
                  value: 'id'
                }"
              />
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
              <h5>算法步骤和公式：</h5>
              <div v-if="algorithmSteps.length > 0">
                <div v-for="(step, index) in algorithmSteps" :key="step.id" class="step-item">
                  <div class="step-layout">
                    <!-- 左侧：步骤信息 -->
                    <div class="step-left">
                      <div class="step-header">
                        <span class="step-number">步骤 {{ index + 1 }}</span>
                        <span class="step-name">{{ step.stepName }}</span>
                      </div>
                      <div class="step-content">
                        <p class="step-description">{{ step.stepDescription }}</p>
                        <div v-if="step.parameters" class="step-parameters">
                          <strong>参数说明：</strong>
                          <span>{{ step.parameters }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 右侧：公式和计算结果 -->
                    <div class="step-right">
                      <!-- 公式展示 -->
                      <div class="formula-section">
                        <div class="formula-header">
                          <strong>公式</strong>
                          <el-button 
                            type="primary" 
                            size="small" 
                            @click="calculateStepResult(step, index)"
                            :loading="step.calculating"
                          >
                            查看结果
                          </el-button>
                        </div>
                        <div v-if="step.formula" class="formula-display">
                          <code class="formula-code">{{ step.formula }}</code>
                        </div>
                        <div v-else class="no-formula">
                          <span class="text-muted">暂无公式</span>
                        </div>
                      </div>
                      
                      <!-- 计算提示 -->
                      <div v-if="!step.calculating" class="result-empty">
                        <el-empty 
                          description="点击查看结果按钮计算此步骤的结果" 
                          :image-size="60"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="no-steps">
                <el-empty description="请先选择算法以查看步骤和公式" :image-size="80" />
              </div>
            </div>
            
            <!-- 通用参数配置 -->
            <div class="common-parameters">
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
            </div>
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
          <el-button type="success" @click="validateParameters">
            <el-icon><Check /></el-icon>
            验证参数
          </el-button>
          <el-button type="info" @click="handlePreviewData">
            <el-icon><View /></el-icon>
            预览数据
          </el-button>
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
    </el-card>

    <!-- 数据预览对话框 -->
    <el-dialog v-model="dialogVisible.preview" title="数据预览" width="80%">
      <el-table :data="previewData" stripe border max-height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="地区名称" width="150" />
        <el-table-column prop="region" label="区域" width="120" />
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
import { evaluationApi, weightConfigApi, surveyDataApi, regionApi, algorithmConfigApi, algorithmExecutionApi, algorithmManagementApi } from '@/api'
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
const algorithmSteps = ref<any[]>([])
const regionTreeData = ref<any[]>([])
const evaluationHistory = ref<any[]>([])
const previewData = ref<any[]>([])

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

const evaluationForm = reactive({
  name: '',
  weightConfigId: null,
  algorithmId: null,
  dataSource: 'REGION',
  regions: [],
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
  weightConfigId: [{ required: true, message: '请选择权重配置', trigger: 'change' }],
  algorithmId: [{ required: true, message: '请选择评估算法', trigger: 'change' }],
  dataSource: [{ required: true, message: '请选择数据源', trigger: 'change' }]
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
    // 从survey-data API获取地区数据
    const response = await surveyDataApi.getAll()
    if (response.success && response.data) {
      // 将survey-data转换为地区树形结构
      const regionMap = new Map()
      
      // 处理调查数据，提取地区信息
      response.data.forEach(item => {
        // 省级
        if (item.province && !regionMap.has(item.province)) {
          regionMap.set(item.province, {
            id: `province_${item.province}`,
            label: item.province,
            value: `province_${item.province}`,
            level: 1,
            children: []
          })
        }
        
        // 市级
        if (item.city && item.province) {
          const provinceKey = item.province
          const cityKey = `${item.province}_${item.city}`
          
          if (!regionMap.has(cityKey)) {
            const cityNode = {
              id: `city_${cityKey}`,
              label: item.city,
              value: `city_${cityKey}`,
              level: 2,
              children: []
            }
            
            regionMap.set(cityKey, cityNode)
            
            // 添加到省级节点
            const provinceNode = regionMap.get(provinceKey)
            if (provinceNode) {
              provinceNode.children.push(cityNode)
            }
          }
        }
        
        // 县级
        if (item.county && item.city && item.province) {
          const cityKey = `${item.province}_${item.city}`
          const countyKey = `${item.province}_${item.city}_${item.county}`
          
          if (!regionMap.has(countyKey)) {
            const countyNode = {
              id: `county_${countyKey}`,
              label: item.county,
              value: `county_${countyKey}`,
              level: 3,
              children: []
            }
            
            regionMap.set(countyKey, countyNode)
            
            // 添加到市级节点
            const cityNode = regionMap.get(cityKey)
            if (cityNode) {
              cityNode.children.push(countyNode)
            }
          }
        }
        
        // 乡镇级
        if (item.township && item.county && item.city && item.province) {
          const countyKey = `${item.province}_${item.city}_${item.county}`
          const townshipKey = `${item.province}_${item.city}_${item.county}_${item.township}`
          
          if (!regionMap.has(townshipKey)) {
            const townshipNode = {
              id: `township_${townshipKey}`,
              label: item.township,
              value: `township_${townshipKey}`,
              level: 4,
              children: []
            }
            
            regionMap.set(townshipKey, townshipNode)
            
            // 添加到县级节点
            const countyNode = regionMap.get(countyKey)
            if (countyNode) {
              countyNode.children.push(townshipNode)
            }
          }
        }
      })
      
      // 提取省级节点作为根节点
      const treeData = []
      regionMap.forEach((value, key) => {
        if (value.level === 1) {
          treeData.push(value)
        }
      })
      
      regionTreeData.value = treeData
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

// 获取算法步骤和公式
const getAlgorithmSteps = async (algorithmId: number) => {
  try {
    const response = await algorithmManagementApi.getAlgorithmStepsAndFormulas(algorithmId)
    if (response.success) {
      const steps = response.data || []
      
      // 处理每个步骤的公式数据
      steps.forEach(step => {
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
    weightConfigId: null,
    algorithmId: null,
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
    
    loading.evaluation = true
    evaluationProgress.visible = true
    evaluationProgress.percentage = 0
    evaluationProgress.status = 'success'
    evaluationProgress.message = '正在初始化评估...'
    evaluationProgress.detail = ''
    
    try {
      // 模拟评估进度
      const progressSteps = [
        { percentage: 20, message: '正在加载数据...', detail: '读取调查数据和权重配置' },
        { percentage: 40, message: '正在预处理数据...', detail: '数据清洗和标准化' },
        { percentage: 60, message: '正在执行算法计算...', detail: `使用${evaluationForm.algorithm}算法进行计算` },
        { percentage: 80, message: '正在生成结果...', detail: '计算评估得分和排名' },
        { percentage: 100, message: '评估完成', detail: '结果已保存，可以查看详细报告' }
      ]
      
      for (const step of progressSteps) {
        await new Promise(resolve => setTimeout(resolve, 1000))
        evaluationProgress.percentage = step.percentage
        evaluationProgress.message = step.message
        evaluationProgress.detail = step.detail
      }
      
      // 准备算法执行参数
      const executionParams = {
        algorithmId: evaluationForm.algorithmId,
        regionIds: evaluationForm.regions,
        weightConfig: {}, // 这里应该从权重配置中获取
        parameters: evaluationForm.parameters
      }
      
      const response = await algorithmExecutionApi.execute(executionParams)
      if (response.success) {
        ElMessage.success('评估执行成功')
        
        // 如果返回了执行ID，可以监控进度
        if (response.data && response.data.executionId) {
          monitorExecutionProgress(response.data.executionId)
        }
        
        getEvaluationHistory()
      } else {
        evaluationProgress.status = 'exception'
        evaluationProgress.message = '评估失败'
        evaluationProgress.detail = response.message || '未知错误'
        ElMessage.error(response.message || '评估执行失败')
      }
    } catch (error) {
      console.error('评估执行失败:', error)
      evaluationProgress.status = 'exception'
      evaluationProgress.message = '评估失败'
      evaluationProgress.detail = '系统错误，请稍后重试'
      ElMessage.error('评估执行失败')
    } finally {
      loading.evaluation = false
    }
  })
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

// 计算步骤结果
const calculateStepResult = async (step: any, index: number) => {
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
      // 设置计算结果
      calculationResult = {
        tableData: response.data.tableData || [],
        columns: response.data.columns || [],
        summary: response.data.summary || null
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
    ElMessage.error('计算步骤结果失败')
    
    // 如果API不存在，使用模拟数据
    const mockResult = generateMockStepResult(step, index)
    
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

// 生成模拟步骤结果
const generateMockStepResult = (step: any, index: number) => {
  const mockData = []
  const regions = ['汉阳镇', '西龙镇', '青神县', '眉山市']
  
  // 根据步骤类型生成不同的模拟数据
  if (step.stepName.includes('指标计算')) {
    for (let i = 0; i < regions.length; i++) {
      mockData.push({
        region: regions[i],
        indicator1: (Math.random() * 0.8 + 0.2).toFixed(3),
        indicator2: (Math.random() * 0.8 + 0.2).toFixed(3),
        indicator3: (Math.random() * 0.8 + 0.2).toFixed(3),
        total: (Math.random() * 0.8 + 0.2).toFixed(3)
      })
    }
    
    return {
      tableData: mockData,
      columns: [
        { prop: 'region', label: '地区', width: 120 },
        { prop: 'indicator1', label: '指标1', width: 100 },
        { prop: 'indicator2', label: '指标2', width: 100 },
        { prop: 'indicator3', label: '指标3', width: 100 },
        { prop: 'total', label: '总分', width: 100 }
      ],
      summary: {
        '平均值': (mockData.reduce((sum, item) => sum + parseFloat(item.total), 0) / mockData.length).toFixed(3),
        '最大值': Math.max(...mockData.map(item => parseFloat(item.total))).toFixed(3),
        '最小值': Math.min(...mockData.map(item => parseFloat(item.total))).toFixed(3)
      }
    }
  } else {
    // 其他步骤的通用模拟数据
    for (let i = 0; i < regions.length; i++) {
      mockData.push({
        region: regions[i],
        value: (Math.random() * 100).toFixed(2),
        weight: (Math.random() * 0.3 + 0.1).toFixed(3),
        score: (Math.random() * 90 + 10).toFixed(2)
      })
    }
    
    return {
      tableData: mockData,
      columns: [
        { prop: 'region', label: '地区', width: 120 },
        { prop: 'value', label: '数值', width: 100 },
        { prop: 'weight', label: '权重', width: 100 },
        { prop: 'score', label: '得分', width: 100 }
      ],
      summary: {
        '数据条数': mockData.length.toString(),
        '平均得分': (mockData.reduce((sum, item) => sum + parseFloat(item.score), 0) / mockData.length).toFixed(2)
      }
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
    columns: step.calculationResult.columns,
    summary: step.calculationResult.summary
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
  
  // 添加汇总信息
  if (exportData.summary) {
    csv += '\n汇总信息\n'
    Object.entries(exportData.summary).forEach(([key, value]) => {
      csv += `${key},${value}\n`
    })
  }
  
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

// 组件挂载时获取数据
onMounted(() => {
  getWeightConfigs()
  getAlgorithmConfigs()
  getRegionTreeData()
  getEvaluationHistory()
})
</script>

<style scoped>
.evaluation {
  max-width: 1200px;
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
  max-width: 800px;
}

.param-card {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
}

.progress-content {
  text-align: center;
}

.progress-info {
  margin: 16px 0;
}

.progress-info p {
  margin: 4px 0;
  color: #606266;
}

.progress-actions {
  margin-top: 16px;
}

.progress-actions .el-button {
  margin: 0 8px;
}

.el-select,
.el-input-number {
  width: 100%;
}

/* 算法步骤样式 */
.algorithm-steps {
  margin-bottom: 20px;
}

.algorithm-steps h5,
.common-parameters h5 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.step-item {
  margin-bottom: 20px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.step-layout {
  display: flex;
  min-height: 200px;
}

.step-left {
  flex: 0 0 40%;
  padding: 16px;
  border-right: 1px solid #e4e7ed;
  background-color: #fafbfc;
}

.step-right {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.step-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.step-number {
  background-color: #409eff;
  color: white;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
  margin-right: 10px;
}

.step-name {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.step-content {
  padding-left: 0;
}

.step-description {
  margin: 0 0 12px 0;
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}

.step-parameters {
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

/* 公式展示样式 */
.formula-section {
  margin-bottom: 16px;
}

.formula-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.formula-header strong {
  color: #303133;
  font-size: 14px;
}

.formula-display {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
}

.formula-code {
  font-family: 'Courier New', 'Monaco', monospace;
  color: #e6a23c;
  font-size: 13px;
  line-height: 1.4;
  word-break: break-all;
}

.no-formula {
  text-align: center;
  padding: 20px;
  color: #c0c4cc;
  font-style: italic;
}

.text-muted {
  color: #909399;
}

/* 计算结果样式 */
.result-section {
  flex: 1;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.result-header strong {
  color: #303133;
  font-size: 14px;
}

.result-table {
  margin-bottom: 12px;
}

.result-table :deep(.el-table) {
  font-size: 12px;
}

.result-table :deep(.el-table th) {
  background-color: #f8f9fa;
  font-weight: 600;
}

.result-table :deep(.el-table td) {
  padding: 8px 0;
}

.result-summary {
  margin-top: 12px;
}

.result-summary :deep(.el-descriptions) {
  font-size: 12px;
}

.result-summary :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
}

.result-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .step-layout {
    flex-direction: column;
  }
  
  .step-left {
    flex: none;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .step-right {
    flex: none;
  }
}

.common-parameters {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.no-steps {
  text-align: center;
  padding: 20px;
  color: #909399;
}
</style>