<template>
  <div class="topsis-test-panel">
    <div class="test-header">
      <h3>TOPSIS配置测试</h3>
      <p>测试当前配置的TOPSIS算法计算效果</p>
    </div>

    <!-- 测试参数配置 -->
    <el-card class="test-params">
      <template #header>
        <span>测试参数</span>
      </template>
      
      <el-form :model="testParams" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="测试区域">
              <el-select
                v-model="testParams.regionCodes"
                multiple
                placeholder="选择测试区域"
                style="width: 100%"
              >
                <el-option
                  v-for="region in availableRegions"
                  :key="region.code"
                  :label="region.name"
                  :value="region.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权重配置">
              <el-select
                v-model="testParams.weightConfigId"
                placeholder="选择权重配置"
                style="width: 100%"
              >
                <el-option
                  v-for="weight in weightConfigs"
                  :key="weight.id"
                  :label="weight.configName"
                  :value="weight.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="runTest"
            :loading="loading.test"
            :disabled="!canRunTest"
          >
            <el-icon><Play /></el-icon>
            运行测试
          </el-button>
          <el-button @click="resetTest">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 测试结果 -->
    <el-card class="test-results" v-if="testResults">
      <template #header>
        <div class="result-header">
          <span>测试结果</span>
          <el-tag :type="testResults.success ? 'success' : 'danger'">
            {{ testResults.success ? '测试成功' : '测试失败' }}
          </el-tag>
        </div>
      </template>

      <!-- 成功结果 -->
      <div v-if="testResults.success">
        <!-- 计算统计 -->
        <div class="calculation-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="处理区域数" :value="testResults.metrics.processedRegions" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="使用指标数" :value="testResults.metrics.usedIndicators" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="计算时间" :value="testResults.metrics.calculationTime" suffix="ms" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="异常数量" :value="testResults.metrics.anomalies" />
            </el-col>
          </el-row>
        </div>

        <!-- 计算结果表格 -->
        <div class="result-table">
          <h4>TOPSIS计算结果</h4>
          <el-table :data="testResults.data" stripe border max-height="300">
            <el-table-column prop="regionCode" label="区域代码" width="120" />
            <el-table-column prop="regionName" label="区域名称" width="200" />
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
            <el-table-column prop="ranking" label="排名" width="80" />
          </el-table>
        </div>

        <!-- 理想解信息 -->
        <div class="ideal-solutions">
          <h4>理想解信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>正理想解</span>
                </template>
                <div class="solution-values">
                  <div
                    v-for="(value, indicator) in testResults.idealSolutions.positive"
                    :key="indicator"
                    class="solution-item"
                  >
                    <span class="indicator-name">{{ indicator }}:</span>
                    <span class="indicator-value">{{ formatNumber(value) }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>负理想解</span>
                </template>
                <div class="solution-values">
                  <div
                    v-for="(value, indicator) in testResults.idealSolutions.negative"
                    :key="indicator"
                    class="solution-item"
                  >
                    <span class="indicator-name">{{ indicator }}:</span>
                    <span class="indicator-value">{{ formatNumber(value) }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 失败结果 -->
      <div v-else class="error-results">
        <el-alert
          :title="testResults.error"
          type="error"
          :description="testResults.details"
          show-icon
          :closable="false"
        />
        
        <!-- 修复建议 -->
        <div v-if="testResults.suggestions" class="suggestions">
          <h4>修复建议</h4>
          <div class="suggestion-list">
            <div
              v-for="(suggestion, index) in testResults.suggestions"
              :key="index"
              class="suggestion-item"
            >
              <el-icon class="suggestion-icon">
                <InfoFilled />
              </el-icon>
              <div class="suggestion-content">
                <span class="suggestion-text">{{ suggestion.message }}</span>
                <el-button
                  v-if="suggestion.action"
                  type="text"
                  size="small"
                  @click="applySuggestion(suggestion)"
                >
                  {{ suggestion.actionText }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 验证结果 -->
    <el-card class="validation-results" v-if="validationResults">
      <template #header>
        <span>配置验证</span>
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
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Play, Refresh, SuccessFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'

// Props
const props = defineProps<{
  config: any
}>()

// Emits
const emit = defineEmits<{
  testCompleted: [results: any]
}>()

// 响应式数据
const testParams = reactive({
  regionCodes: [] as string[],
  weightConfigId: null as number | null
})

const testResults = ref<any>(null)
const validationResults = ref<any[]>([])
const availableRegions = ref<any[]>([])
const weightConfigs = ref<any[]>([])

const loading = reactive({
  test: false,
  validation: false
})

// 计算属性
const canRunTest = computed(() => {
  return testParams.regionCodes.length > 0 && testParams.weightConfigId !== null
})

// 监听配置变化
watch(() => props.config, (newConfig) => {
  if (newConfig) {
    loadTestData()
    validateConfiguration()
  }
}, { immediate: true })

// 方法
const loadTestData = async () => {
  try {
    // 加载可用区域
    availableRegions.value = [
      { code: 'R001', name: '测试区域1' },
      { code: 'R002', name: '测试区域2' },
      { code: 'R003', name: '测试区域3' }
    ]
    
    // 加载权重配置
    weightConfigs.value = [
      { id: 1, configName: '默认权重配置' },
      { id: 2, configName: '专家权重配置' }
    ]
  } catch (error) {
    console.error('加载测试数据失败:', error)
    ElMessage.error('加载测试数据失败')
  }
}

const validateConfiguration = async () => {
  if (!props.config) return
  
  loading.validation = true
  try {
    // 这里需要调用后端API验证配置
    // const response = await topsisConfigApi.validateConfig(props.config)
    
    // 模拟验证结果
    validationResults.value = [
      { type: 'indicators', passed: true, message: '指标配置有效' },
      { type: 'data', passed: true, message: '数据完整性检查通过' },
      { type: 'algorithm', passed: true, message: '算法参数正确' }
    ]
  } catch (error) {
    console.error('配置验证失败:', error)
    ElMessage.error('配置验证失败')
  } finally {
    loading.validation = false
  }
}

const runTest = async () => {
  if (!canRunTest.value) {
    ElMessage.warning('请完善测试参数')
    return
  }
  
  loading.test = true
  testResults.value = null
  
  try {
    // 这里需要调用后端API运行测试
    // const response = await topsisConfigApi.testConfig({
    //   config: props.config,
    //   regionCodes: testParams.regionCodes,
    //   weightConfigId: testParams.weightConfigId
    // })
    
    // 模拟测试结果
    const mockResults = {
      success: true,
      metrics: {
        processedRegions: testParams.regionCodes.length,
        usedIndicators: props.config.indicators?.length || 0,
        calculationTime: Math.floor(Math.random() * 1000) + 100,
        anomalies: 0
      },
      data: testParams.regionCodes.map((code, index) => ({
        regionCode: code,
        regionName: `区域${index + 1}`,
        positiveDistance: Math.random() * 10,
        negativeDistance: Math.random() * 10,
        comprehensiveScore: Math.random(),
        ranking: index + 1
      })),
      idealSolutions: {
        positive: {
          indicator1: 100,
          indicator2: 95,
          indicator3: 88
        },
        negative: {
          indicator1: 20,
          indicator2: 15,
          indicator3: 12
        }
      }
    }
    
    testResults.value = mockResults
    emit('testCompleted', mockResults)
    ElMessage.success('测试完成')
  } catch (error) {
    console.error('测试运行失败:', error)
    testResults.value = {
      success: false,
      error: '测试运行失败',
      details: error.message || '未知错误',
      suggestions: [
        {
          message: '检查指标配置是否正确',
          actionText: '重新配置指标',
          action: () => {
            // 这里可以触发重新配置指标的操作
            console.log('重新配置指标')
          }
        },
        {
          message: '确认数据完整性',
          actionText: '验证数据',
          action: () => {
            // 这里可以触发数据验证操作
            console.log('验证数据')
          }
        },
        {
          message: '验证权重配置有效性',
          actionText: '检查权重',
          action: () => {
            // 这里可以触发权重检查操作
            console.log('检查权重')
          }
        }
      ]
    }
    ElMessage.error('测试运行失败')
  } finally {
    loading.test = false
  }
}

const resetTest = () => {
  testParams.regionCodes = []
  testParams.weightConfigId = null
  testResults.value = null
  validationResults.value = []
}

const formatNumber = (value: number) => {
  return typeof value === 'number' ? value.toFixed(4) : '-'
}

const getScoreType = (score: number) => {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'warning'
  return 'danger'
}

const applySuggestion = (suggestion: any) => {
  if (suggestion.action) {
    suggestion.action()
    ElMessage.success('建议已应用')
  }
}
</script>

<style scoped>
.topsis-test-panel {
  padding: 16px;
}

.test-header {
  margin-bottom: 20px;
}

.test-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.test-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.test-params,
.test-results,
.validation-results {
  margin-bottom: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calculation-stats {
  margin-bottom: 20px;
}

.result-table {
  margin-bottom: 20px;
}

.result-table h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.ideal-solutions h4 {
  margin: 0 0 16px 0;
  color: #303133;
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

.error-results {
  padding: 16px;
}

.suggestions {
  margin-top: 20px;
}

.suggestions h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.suggestions ul {
  margin: 0;
  padding-left: 20px;
}

.suggestions li {
  margin-bottom: 8px;
  color: #606266;
}

.validation-results {
  background-color: #f9f9f9;
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
}

.success-icon {
  color: #67c23a;
}

.error-icon {
  color: #f56c6c;
}

.validation-text {
  color: #303133;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.suggestion-icon {
  color: #409eff;
  margin-top: 2px;
}

.suggestion-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.suggestion-text {
  color: #303133;
  font-size: 14px;
}
</style>