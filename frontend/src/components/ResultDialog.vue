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
                <span v-if="index === resultData.columns.length - 1" style="color: red; font-size: 12px;">(第{{ index + 1 }}列)</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 统计信息已移除 -->
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
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

interface StepInfo {
  stepName: string
  description: string
  stepCode: string
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

const visible = ref(false)

// 计算对话框标题
const dialogTitle = computed(() => {
  if (props.stepInfo) {
    return `${props.stepInfo.stepName} - 计算结果`
  }
  return '计算结果'
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
    }
  },
  { immediate: true }
)

// 监听resultData变化
watch(
  () => props.resultData,
  (newData) => {
    console.log('ResultData changed:', newData)
    if (newData && newData.columns) {
      console.log('Columns count:', newData.columns.length)
      console.log('Columns:', newData.columns.map(col => ({ prop: col.prop, label: col.label, width: col.width })))
    }
    if (newData && newData.tableData && newData.tableData.length > 0) {
      console.log('First row data keys:', Object.keys(newData.tableData[0]))
      console.log('Sample row data:', newData.tableData[0])
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