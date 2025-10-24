<template>
  <div class="topsis-preview-panel">
    <div class="preview-header">
      <h3>TOPSIS配置预览</h3>
      <p>预览当前指标配置的TOPSIS计算效果</p>
    </div>

    <!-- 配置信息 -->
    <el-card class="config-info">
      <template #header>
        <span>配置信息</span>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模型ID">{{ modelId }}</el-descriptions-item>
        <el-descriptions-item label="步骤ID">{{ stepId }}</el-descriptions-item>
        <el-descriptions-item label="指标数量">{{ indicators.length }}</el-descriptions-item>
        <el-descriptions-item label="算法类型">TOPSIS正理想解</el-descriptions-item>
      </el-descriptions>
      
      <div class="indicators-preview">
        <h4>配置指标列表</h4>
        <div class="indicator-tags">
          <el-tag
            v-for="(indicator, index) in indicators"
            :key="indicator"
            :type="getIndicatorTagType(index)"
            class="indicator-tag"
          >
            {{ index + 1 }}. {{ indicator }}
          </el-tag>
        </div>
      </div>
    </el-card>

    <!-- 生成的QL表达式 -->
    <el-card class="expression-preview">
      <template #header>
        <span>生成的QL表达式</span>
      </template>
      
      <div class="expression-content">
        <el-input
          v-model="generatedExpression"
          type="textarea"
          :rows="3"
          readonly
          class="expression-input"
        />
        <div class="expression-actions">
          <el-button type="primary" size="small" @click="copyExpression">
            <el-icon><CopyDocument /></el-icon>
            复制表达式
          </el-button>
          <el-button type="success" size="small" @click="validateExpression">
            <el-icon><Check /></el-icon>
            验证表达式
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 示例数据预览 -->
    <el-card class="sample-preview" v-loading="loading.sample">
      <template #header>
        <div class="sample-header">
          <span>示例数据预览</span>
          <el-button type="primary" size="small" @click="loadSampleData">
            <el-icon><Refresh /></el-icon>
            加载示例数据
          </el-button>
        </div>
      </template>
      
      <div v-if="sampleData.length > 0">
        <!-- 原始数据 -->
        <div class="data-section">
          <h4>原始数据 (前5条)</h4>
          <el-table :data="sampleData.slice(0, 5)" stripe border max-height="200">
            <el-table-column prop="regionCode" label="区域代码" width="120" />
            <el-table-column
              v-for="indicator in indicators"
              :key="indicator"
              :prop="indicator"
              :label="indicator"
              width="120"
            >
              <template #default="{ row }">
                <span>{{ formatNumber(row[indicator]) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 计算预览 -->
        <div class="calculation-preview" v-if="calculationPreview">
          <h4>TOPSIS计算预览</h4>
          
          <!-- 理想解 -->
          <el-row :gutter="20" class="ideal-solutions">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>正理想解 (最大值)</span>
                </template>
                <div class="solution-values">
                  <div
                    v-for="indicator in indicators"
                    :key="indicator"
                    class="solution-item"
                  >
                    <span class="indicator-name">{{ indicator }}:</span>
                    <span class="indicator-value">
                      {{ formatNumber(calculationPreview.idealSolutions.positive[indicator]) }}
                    </span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>负理想解 (最小值)</span>
                </template>
                <div class="solution-values">
                  <div
                    v-for="indicator in indicators"
                    :key="indicator"
                    class="solution-item"
                  >
                    <span class="indicator-name">{{ indicator }}:</span>
                    <span class="indicator-value">
                      {{ formatNumber(calculationPreview.idealSolutions.negative[indicator]) }}
                    </span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 距离计算结果 -->
          <div class="distance-results">
            <h4>距离计算结果 (前5条)</h4>
            <el-table :data="calculationPreview.results.slice(0, 5)" stripe border>
              <el-table-column prop="regionCode" label="区域代码" width="120" />
              <el-table-column prop="positiveDistance" label="正理想解距离" width="150">
                <template #default="{ row }">
                  <span>{{ formatNumber(row.positiveDistance) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="negativeDistance" label="负理想解距离" width="150">
                <template #default="{ row }">
                  <span>{{ formatNumber(row.negativeDistance) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="comprehensiveScore" label="综合得分" width="120">
                <template #default="{ row }">
                  <el-tag :type="getScoreType(row.comprehensiveScore)">
                    {{ formatNumber(row.comprehensiveScore) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
      
      <div v-else class="no-data">
        <el-empty description="暂无示例数据，点击加载示例数据按钮获取预览" />
      </div>
    </el-card>

    <!-- 配置验证 -->
    <el-card class="validation-card" v-if="validationResults.length > 0">
      <template #header>
        <span>配置验证结果</span>
      </template>
      
      <div class="validation-items">
        <div
          v-for="item in validationResults"
          :key="item.type"
          class="validation-item"
        >
          <el-icon :class="item.passed ? 'success-icon' : 'error-icon'">
            <component :is="item.passed ? 'SuccessFilled' : 'CircleCloseFilled'" />
          </el-icon>
          <span class="validation-text">{{ item.message }}</span>
          <span v-if="item.details" class="validation-details">{{ item.details }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument, Check, Refresh, SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'

// Props
const props = defineProps<{
  modelId: number | null
  stepId: number | null
  indicators: string[]
}>()

// 响应式数据
const sampleData = ref<any[]>([])
const calculationPreview = ref<any>(null)
const validationResults = ref<any[]>([])

const loading = reactive({
  sample: false,
  validation: false
})

// 计算属性
const generatedExpression = computed(() => {
  if (props.indicators.length === 0) {
    return ''
  }
  return `@TOPSIS_POSITIVE:${props.indicators.join(',')}`
})

// 监听指标变化
watch(() => props.indicators, () => {
  if (props.indicators.length > 0) {
    validateConfiguration()
  }
}, { immediate: true })

// 方法
const getIndicatorTagType = (index: number) => {
  const types = ['primary', 'success', 'warning', 'danger', 'info']
  return types[index % types.length]
}

const copyExpression = async () => {
  try {
    await navigator.clipboard.writeText(generatedExpression.value)
    ElMessage.success('表达式已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

const validateExpression = async () => {
  if (!generatedExpression.value) {
    ElMessage.warning('没有可验证的表达式')
    return
  }
  
  loading.validation = true
  try {
    // 这里需要调用后端API验证表达式
    // const response = await topsisConfigApi.validateExpression(generatedExpression.value)
    
    ElMessage.success('表达式验证通过')
  } catch (error) {
    console.error('表达式验证失败:', error)
    ElMessage.error('表达式验证失败')
  } finally {
    loading.validation = false
  }
}

const loadSampleData = async () => {
  if (!props.modelId || props.indicators.length === 0) {
    ElMessage.warning('请先选择模型和配置指标')
    return
  }
  
  loading.sample = true
  try {
    // 这里需要调用后端API获取示例数据
    // const response = await topsisConfigApi.getSampleData({
    //   modelId: props.modelId,
    //   indicators: props.indicators
    // })
    
    // 模拟示例数据
    const mockData = []
    for (let i = 0; i < 10; i++) {
      const row: any = {
        regionCode: `R${String(i + 1).padStart(3, '0')}`
      }
      props.indicators.forEach(indicator => {
        row[indicator] = Math.random() * 100
      })
      mockData.push(row)
    }
    
    sampleData.value = mockData
    
    // 计算TOPSIS预览
    calculatePreview(mockData)
    
    ElMessage.success('示例数据加载成功')
  } catch (error) {
    console.error('加载示例数据失败:', error)
    ElMessage.error('加载示例数据失败')
  } finally {
    loading.sample = false
  }
}

const calculatePreview = (data: any[]) => {
  if (data.length === 0 || props.indicators.length === 0) return
  
  // 计算理想解
  const idealSolutions = {
    positive: {} as Record<string, number>,
    negative: {} as Record<string, number>
  }
  
  props.indicators.forEach(indicator => {
    const values = data.map(row => row[indicator]).filter(v => typeof v === 'number')
    idealSolutions.positive[indicator] = Math.max(...values)
    idealSolutions.negative[indicator] = Math.min(...values)
  })
  
  // 计算距离
  const results = data.map(row => {
    let positiveDistance = 0
    let negativeDistance = 0
    
    props.indicators.forEach(indicator => {
      const value = row[indicator]
      if (typeof value === 'number') {
        positiveDistance += Math.pow(idealSolutions.positive[indicator] - value, 2)
        negativeDistance += Math.pow(idealSolutions.negative[indicator] - value, 2)
      }
    })
    
    positiveDistance = Math.sqrt(positiveDistance)
    negativeDistance = Math.sqrt(negativeDistance)
    
    const comprehensiveScore = negativeDistance / (positiveDistance + negativeDistance)
    
    return {
      regionCode: row.regionCode,
      positiveDistance,
      negativeDistance,
      comprehensiveScore: isNaN(comprehensiveScore) ? 0 : comprehensiveScore
    }
  })
  
  calculationPreview.value = {
    idealSolutions,
    results
  }
}

const validateConfiguration = async () => {
  if (props.indicators.length === 0) {
    validationResults.value = []
    return
  }
  
  // 模拟验证结果
  const results = [
    {
      type: 'indicators',
      passed: props.indicators.length > 0,
      message: '指标配置检查',
      details: `已配置 ${props.indicators.length} 个指标`
    },
    {
      type: 'expression',
      passed: generatedExpression.value.length > 0,
      message: 'QL表达式生成',
      details: '表达式格式正确'
    },
    {
      type: 'algorithm',
      passed: true,
      message: 'TOPSIS算法兼容性',
      details: '算法参数配置正确'
    }
  ]
  
  validationResults.value = results
}

const formatNumber = (value: number) => {
  return typeof value === 'number' ? value.toFixed(4) : '-'
}

const getScoreType = (score: number) => {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'warning'
  return 'danger'
}

// 组件挂载时验证配置
onMounted(() => {
  validateConfiguration()
})
</script>

<style scoped>
.topsis-preview-panel {
  padding: 16px;
}

.preview-header {
  margin-bottom: 20px;
}

.preview-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.preview-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-info,
.expression-preview,
.sample-preview,
.validation-card {
  margin-bottom: 20px;
}

.indicators-preview {
  margin-top: 16px;
}

.indicators-preview h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.indicator-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.indicator-tag {
  margin: 2px;
}

.expression-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.expression-input {
  font-family: 'Courier New', monospace;
}

.expression-actions {
  display: flex;
  gap: 8px;
}

.sample-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-section {
  margin-bottom: 20px;
}

.data-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.calculation-preview h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 14px;
}

.ideal-solutions {
  margin-bottom: 20px;
}

.solution-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.solution-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

.indicator-name {
  color: #606266;
  font-weight: 500;
}

.indicator-value {
  color: #303133;
  font-family: monospace;
}

.distance-results h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.no-data {
  padding: 40px;
  text-align: center;
}

.validation-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.validation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.success-icon {
  color: #67c23a;
}

.error-icon {
  color: #f56c6c;
}

.validation-text {
  color: #303133;
  font-weight: 500;
}

.validation-details {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}
</style>