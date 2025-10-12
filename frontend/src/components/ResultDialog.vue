<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="90%"
    :before-close="handleClose"
    class="result-dialog"
  >
    <div class="result-content">
      <!-- 步骤信息 -->
      <div class="step-info" v-if="stepInfo">
        <h3>{{ stepInfo.stepName }}</h3>
        <p class="step-description">{{ stepInfo.description }}</p>
      </div>

      <!-- 公式展示 -->
      <div class="formula-section" v-if="formula">
        <h4>计算公式</h4>
        <div class="formula-code">
          <code>{{ formula }}</code>
        </div>
      </div>

      <!-- 步骤分组控制 -->
      <div v-if="resultData && resultData.isMultiStep" class="step-control-section">
        <div class="step-selector">
          <h4>选择查看步骤</h4>
          <el-select 
            v-model="selectedStepOrder" 
            placeholder="选择步骤"
            @change="handleStepChange"
            style="width: 200px;"
          >
            <el-option
              v-for="step in resultData.stepResults"
              :key="step.stepOrder"
              :label="`步骤${step.stepOrder}: ${step.stepName}`"
              :value="step.stepOrder"
            />
          </el-select>
        </div>
        
        <!-- 列显示/隐藏控制 -->
        <div class="column-control" v-if="currentStepData">
          <h4>列显示控制</h4>
          <div class="column-checkboxes">
            <el-checkbox-group v-model="visibleColumns">
              <el-checkbox 
                v-for="column in allColumns" 
                :key="column.prop"
                :label="column.prop"
                :disabled="column.prop === 'regionCode' || column.prop === 'regionName'"
              >
                {{ column.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
          <div class="column-actions">
            <el-button size="small" @click="selectAllColumns">全选</el-button>
            <el-button size="small" @click="unselectAllColumns">取消全选</el-button>
            <el-button size="small" @click="resetColumns">重置</el-button>
          </div>
        </div>
      </div>

      <!-- 双表格显示 -->
      <div v-if="resultData && resultData.isDualTable" class="dual-table-section">
        <!-- 表格1：一级指标权重计算 -->
        <div class="table-section">
          <h4>一级指标权重计算</h4>
          <div class="table-container">
            <el-table
              :data="resultData.table1Data"
              border
              stripe
              size="small"
              max-height="400"
              class="result-table"
              style="width: 100%; min-width: 1200px;"
            >
              <el-table-column
                v-for="column in resultData.table1Columns"
                :key="column.prop"
                :prop="column.prop"
                :label="column.label"
                :width="column.width"
                :show-overflow-tooltip="true"
              />
            </el-table>
          </div>
          
          <!-- 统计信息已移除 -->
        </div>

        <!-- 表格2：乡镇减灾能力权重计算 -->
        <div class="table-section" style="margin-top: 30px;">
          <h4>乡镇减灾能力权重计算</h4>
          <div class="table-container">
            <el-table
              :data="resultData.table2Data"
              border
              stripe
              size="small"
              max-height="400"
              class="result-table"
              style="width: 100%; min-width: 1200px;"
            >
              <el-table-column
                v-for="column in resultData.table2Columns"
                :key="column.prop"
                :prop="column.prop"
                :label="column.label"
                :width="column.width"
                :show-overflow-tooltip="true"
              />
            </el-table>
          </div>
          
          <!-- 统计信息已移除 -->
        </div>
      </div>

      <!-- 单表格显示（原有逻辑） -->
      <div v-else-if="resultData && !resultData.isDualTable" class="result-table-section">
        <h4>计算结果</h4>
        <div class="table-container">
          <el-table
            :data="resultData.tableData"
            border
            stripe
            size="small"
            max-height="400"
            class="result-table"
            style="width: 100%; min-width: 1600px;"
          >
            <el-table-column
              v-for="(column, index) in resultData.columns"
              :key="column.prop"
              :prop="column.prop"
              :label="column.label"
              :width="column.width"
              :formatter="column.formatter"
              :show-overflow-tooltip="true"
            >
              <template #header>
                <span>{{ column.label }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 统计信息已移除 -->
      </div>

      <!-- 多步骤结果显示 -->
      <div v-if="resultData && resultData.isMultiStep" class="multi-step-section">
        <div v-if="currentStepData" class="current-step-result">
          <h4>{{ currentStepData.stepName }} - 计算结果</h4>
          <p class="step-description">{{ currentStepData.description }}</p>
          
          <div class="table-container">
            <el-table
              :data="currentStepData.tableData"
              border
              stripe
              size="small"
              max-height="400"
              class="result-table"
              style="width: 100%; min-width: 1200px;"
            >
              <el-table-column
                v-for="column in filteredColumns"
                :key="column.prop"
                :prop="column.prop"
                :label="column.label"
                :width="column.width || 120"
                :show-overflow-tooltip="true"
              >
                <template #header>
                  <span>{{ column.label }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        
        <div v-else class="no-step-selected">
          <el-empty description="请选择要查看的步骤" :image-size="80" />
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!resultData" class="empty-state">
        <el-empty description="暂无计算结果" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="exportResults" :disabled="!resultData">
          导出结果
        </el-button>
        <el-button type="success" @click="generateThematicMap" :disabled="!resultData">
          生成专题图
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

interface StepInfo {
  stepName: string
  description: string
  stepCode: string
}

interface StepResult {
  stepId: number
  stepName: string
  stepOrder: number
  stepCode: string
  description: string
  executionResult: any
  tableData: any[]
  success: boolean
  executionTime: string
}

interface ResultData {
  // 单表格数据结构
  tableData?: any[]
  columns?: any[]
  summary?: Record<string, any>
  
  // 双表格数据结构
  isDualTable?: boolean
  table1Data?: any[]
  table1Columns?: any[]
  table1Summary?: Record<string, any>
  table2Data?: any[]
  table2Columns?: any[]
  table2Summary?: Record<string, any>
  
  // 多步骤数据结构
  isMultiStep?: boolean
  stepResults?: StepResult[]
  selectedStep?: number
}

interface Props {
  modelValue: boolean
  stepInfo?: StepInfo
  formula?: string
  resultData?: ResultData
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'export', data: any): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  stepInfo: undefined,
  formula: '',
  resultData: undefined
})

const emit = defineEmits<Emits>()
const router = useRouter()

const visible = ref(false)
const selectedStepOrder = ref<number>(1)
const visibleColumns = ref<string[]>([])
const allColumns = ref<any[]>([])

// 计算对话框标题
const dialogTitle = computed(() => {
  if (props.resultData?.isMultiStep) {
    return '算法步骤执行结果'
  }
  if (props.stepInfo) {
    return `${props.stepInfo.stepName} - 计算结果`
  }
  return '计算结果'
})

// 当前步骤数据
const currentStepData = computed(() => {
  if (!props.resultData?.isMultiStep || !props.resultData.stepResults) {
    return null
  }
  const stepResult = props.resultData.stepResults.find(step => step.stepOrder === selectedStepOrder.value)
  if (!stepResult) {
    return null
  }
  
  // 确保返回的数据包含 tableData 字段
  console.log('Current step result:', {
    stepOrder: stepResult.stepOrder,
    stepName: stepResult.stepName,
    hasTableData: !!stepResult.tableData,
    tableDataLength: stepResult.tableData?.length
  })
  
  return stepResult
})

// 过滤后的列
const filteredColumns = computed(() => {
  return allColumns.value.filter(column => visibleColumns.value.includes(column.prop))
})

// 监听props变化
watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal
    if (newVal) {
      console.log('ResultDialog opened with data:', {
        stepInfo: props.stepInfo,
        formula: props.formula,
        resultData: props.resultData
      })
      
      // 初始化多步骤数据
      if (props.resultData?.isMultiStep && props.resultData.stepResults) {
        selectedStepOrder.value = props.resultData.stepResults[0]?.stepOrder || 1
        initializeColumns()
      }
    }
  },
  { immediate: true }
)

// 监听resultData变化
watch(
  () => props.resultData,
  (newData) => {
    console.log('=== ResultDialog: ResultData changed ===')
    console.log('Data type detection:', {
      isMultiStep: !!newData?.isMultiStep,
      isDualTable: !!newData?.isDualTable,
      hasTableData: !!newData?.tableData,
      hasStepResults: !!newData?.stepResults,
      stepResultsLength: newData?.stepResults?.length
    })
    
    if (newData?.isMultiStep) {
      console.log('Multi-step mode detected!')
      console.log('Step results:', newData.stepResults)
    } else if (newData?.isDualTable) {
      console.log('Dual table mode detected!')
    } else if (newData?.tableData) {
      console.log('Single table mode detected!')
      if (newData.columns) {
        console.log('Columns count:', newData.columns.length)
      }
      if (newData.tableData.length > 0) {
        console.log('First row data keys:', Object.keys(newData.tableData[0]))
      }
    }
  },
  { deep: true, immediate: true }
)

// 监听visible变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 处理步骤切换
const handleStepChange = (stepOrder: number) => {
  selectedStepOrder.value = stepOrder
  initializeColumns()
}

// 初始化列配置
const initializeColumns = () => {
  if (!currentStepData.value?.tableData || currentStepData.value.tableData.length === 0) {
    console.log('No table data available for columns initialization')
    allColumns.value = []
    visibleColumns.value = []
    return
  }
  
  // 从表格数据中推断列配置
  const firstRow = currentStepData.value.tableData[0]
  const columns: any[] = []
  
  console.log('Initializing columns from first row:', Object.keys(firstRow))
  
  Object.keys(firstRow).forEach(key => {
    columns.push({
      prop: key,
      label: getColumnLabel(key),
      width: getColumnWidth(key)
    })
  })
  
  allColumns.value = columns
  visibleColumns.value = columns.map(col => col.prop) // 默认全部显示
  
  console.log('Columns initialized:', {
    totalColumns: columns.length,
    visibleColumns: visibleColumns.value.length
  })
}

// 获取列标签
const getColumnLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    'regionCode': '地区代码',
    'regionName': '地区名称'
  }
  return labelMap[key] || key
}

// 获取列宽度
const getColumnWidth = (key: string) => {
  if (key === 'regionCode') return 150
  if (key === 'regionName') return 120
  return 120
}

// 选中所有列
const selectAllColumns = () => {
  visibleColumns.value = allColumns.value.map(col => col.prop)
}

// 取消选中所有列
const unselectAllColumns = () => {
  // 保留必需的列
  visibleColumns.value = allColumns.value
    .filter(col => col.prop === 'regionCode' || col.prop === 'regionName')
    .map(col => col.prop)
}

// 重置列显示
const resetColumns = () => {
  visibleColumns.value = allColumns.value.map(col => col.prop)
}

// 导出结果
const exportResults = () => {
  if (!props.resultData) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  try {
    // 构建CSV内容
    const csvContent = buildCSVContent()
    
    // 创建下载链接
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    
    // 生成文件名
    const fileName = `${props.stepInfo?.stepName || '计算结果'}_${new Date().toISOString().slice(0, 10)}.csv`
    link.setAttribute('download', fileName)
    
    // 触发下载
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('导出成功')
    emit('export', props.resultData)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 生成专题图
const generateThematicMap = () => {
  if (!props.resultData) {
    ElMessage.warning('暂无数据可生成专题图')
    return
  }

  try {
    // 从resultData中提取数据
    const { tableData, columns, summary } = props.resultData
    
    // 构建专题图数据，匹配ThematicMap.vue期望的数据结构
    const thematicData = {
      id: Date.now(), // 生成唯一ID
      regionName: props.stepInfo?.stepName || '评估区域',
      evaluationTime: new Date().toLocaleString('zh-CN'),
      algorithm: props.stepInfo?.stepCode || 'default',
      totalScore: summary?.总分 || summary?.平均分 || '未知',
      stepInfo: props.stepInfo,
      formula: props.formula,
      resultData: props.resultData,
      tableData: tableData,
      columns: columns,
      summary: summary,
      timestamp: new Date().toISOString(),
      source: 'evaluation_calculation'
    }
    
    console.log('存储专题图数据:', thematicData)
    
    // 将数据存储到 sessionStorage
    sessionStorage.setItem('thematicMapData', JSON.stringify(thematicData))
    
    // 跳转到专题图页面
    router.push('/thematic-map')
    
    ElMessage.success('正在跳转到专题图页面...')
  } catch (error) {
    console.error('生成专题图失败:', error)
    ElMessage.error('生成专题图失败')
  }
}

// 构建CSV内容
const buildCSVContent = (): string => {
  if (!props.resultData) return ''
  
  const lines: string[] = []
  
  // 添加步骤信息
  if (props.stepInfo) {
    lines.push(`步骤名称,${props.stepInfo.stepName}`)
    lines.push(`步骤描述,${props.stepInfo.description}`)
    lines.push('')
  }
  
  // 添加公式
  if (props.formula) {
    lines.push(`计算公式,${props.formula}`)
    lines.push('')
  }
  
  if (props.resultData.isDualTable) {
    // 双表格数据导出
    const { table1Data, table1Columns, table1Summary, table2Data, table2Columns, table2Summary } = props.resultData
    
    // 表格1
    if (table1Data && table1Columns) {
      lines.push('一级指标权重计算')
      
      // 表格1表头
      const headers1 = table1Columns.map(col => col.label).join(',')
      lines.push(headers1)
      
      // 表格1数据行
      table1Data.forEach(row => {
        const values = table1Columns.map(col => {
          const value = row[col.prop]
          return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
        })
        lines.push(values.join(','))
      })
      
      // 表格1统计信息
      if (table1Summary) {
        lines.push('')
        lines.push('表格1统计信息')
        Object.entries(table1Summary).forEach(([key, value]) => {
          lines.push(`${key},${value}`)
        })
      }
      
      lines.push('')
      lines.push('')
    }
    
    // 表格2
    if (table2Data && table2Columns) {
      lines.push('乡镇减灾能力权重计算')
      
      // 表格2表头
      const headers2 = table2Columns.map(col => col.label).join(',')
      lines.push(headers2)
      
      // 表格2数据行
      table2Data.forEach(row => {
        const values = table2Columns.map(col => {
          const value = row[col.prop]
          return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
        })
        lines.push(values.join(','))
      })
      
      // 表格2统计信息
      if (table2Summary) {
        lines.push('')
        lines.push('表格2统计信息')
        Object.entries(table2Summary).forEach(([key, value]) => {
          lines.push(`${key},${value}`)
        })
      }
    }
  } else {
    // 单表格数据导出（原有逻辑）
    const { tableData, columns, summary } = props.resultData
    
    if (tableData && columns) {
      // 添加表头
      const headers = columns.map(col => col.label).join(',')
      lines.push(headers)
      
      // 添加数据行
      tableData.forEach(row => {
        const values = columns.map(col => {
          const value = row[col.prop]
          return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
        })
        lines.push(values.join(','))
      })
      
      // 添加统计信息
      if (summary) {
        lines.push('')
        lines.push('统计信息')
        Object.entries(summary).forEach(([key, value]) => {
          lines.push(`${key},${value}`)
        })
      }
    }
  }
  
  return lines.join('\n')
}
</script>

<style scoped>
.result-dialog {
  .result-content {
    max-height: 70vh;
    overflow-y: auto;
  }

  .step-info {
    margin-bottom: 20px;
    padding: 16px;
    background-color: #f8f9fa;
    border-radius: 6px;
    
    h3 {
      margin: 0 0 8px 0;
      color: #409eff;
      font-size: 18px;
    }
    
    .step-description {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }

  .formula-section {
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 12px 0;
      color: #333;
      font-size: 16px;
    }
    
    .formula-code {
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
      border-left: 4px solid #409eff;
      
      code {
        font-family: 'Courier New', Consolas, monospace;
        font-size: 14px;
        color: #e83e8c;
        background: none;
      }
    }
  }

  .dual-table-section {
    .table-section {
      margin-bottom: 20px;
      
      h4 {
        margin: 0 0 12px 0;
        color: #333;
        font-size: 16px;
        padding: 8px 12px;
        background-color: #f0f9ff;
        border-left: 4px solid #409eff;
        border-radius: 4px;
      }
      
      .table-container {
        width: 100%;
        overflow-x: auto;
        overflow-y: hidden;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        margin-bottom: 16px;
      }
      
      .result-table {
        width: 100%;
        min-width: 1200px;
      }
    }
  }

  .result-table-section {
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 12px 0;
      color: #333;
      font-size: 16px;
    }
    
    .table-container {
      width: 100%;
      overflow-x: auto;
      overflow-y: hidden;
      border: 1px solid #ebeef5;
      border-radius: 4px;
    }
    
    .result-table {
      width: 100%;
      min-width: 1600px;
    }
  }

  .summary-section {
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 12px 0;
      color: #333;
      font-size: 16px;
    }
    
    .summary-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 12px;
      
      .summary-item {
        padding: 8px 12px;
        background-color: #f8f9fa;
        border-radius: 4px;
        display: flex;
        justify-content: space-between;
        
        .summary-label {
          font-weight: 500;
          color: #666;
        }
        
        .summary-value {
          font-weight: 600;
          color: #409eff;
        }
      }
    }
  }

  .step-control-section {
    margin-bottom: 20px;
    padding: 16px;
    background-color: #f8f9fa;
    border-radius: 6px;
    border: 1px solid #e9ecef;
    
    .step-selector {
      margin-bottom: 16px;
      
      h4 {
        margin: 0 0 8px 0;
        color: #333;
        font-size: 14px;
        font-weight: 600;
      }
    }
    
    .column-control {
      h4 {
        margin: 0 0 12px 0;
        color: #333;
        font-size: 14px;
        font-weight: 600;
      }
      
      .column-checkboxes {
        max-height: 200px;
        overflow-y: auto;
        margin-bottom: 12px;
        padding: 8px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        background-color: white;
        
        .el-checkbox-group {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 8px;
        }
        
        .el-checkbox {
          margin-right: 0;
        }
      }
      
      .column-actions {
        display: flex;
        gap: 8px;
        
        .el-button {
          flex: 1;
        }
      }
    }
  }
  
  .multi-step-section {
    margin-bottom: 20px;
    
    .current-step-result {
      h4 {
        margin: 0 0 8px 0;
        color: #409eff;
        font-size: 16px;
      }
      
      .step-description {
        margin: 0 0 16px 0;
        color: #666;
        font-size: 14px;
        font-style: italic;
      }
      
      .table-container {
        width: 100%;
        overflow-x: auto;
        overflow-y: hidden;
        border: 1px solid #ebeef5;
        border-radius: 4px;
      }
      
      .result-table {
        width: 100%;
        min-width: 1200px;
      }
    }
    
    .no-step-selected {
      padding: 40px;
      text-align: center;
    }
  }

  .empty-state {
    padding: 40px;
    text-align: center;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .result-dialog {
    .summary-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>