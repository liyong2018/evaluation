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
        
        <!-- 列显�?隐藏控制（按步骤分组�?-->
        <div class="column-control" v-if="currentStepData">
          <h4>列显示控�?/h4>
          <!-- 分组折叠面板 -->
          <el-collapse>
            <el-collapse-item
              v-for="group in columnGroups"
              :key="group.key"
              :title="`${group.name}�?{group.columns.length}列）`"
            >
              <div class="group-actions">
                <el-button size="small" @click="selectGroupColumns(group.key)">选择该组</el-button>
                <el-button size="small" @click="unselectGroupColumns(group.key)">取消该组</el-button>
              </div>
              <div class="column-checkboxes">
                <el-checkbox-group v-model="visibleColumns">
                  <el-checkbox 
                    v-for="column in group.columns" 
                    :key="column.prop"
                    :label="column.prop"
                    :disabled="column.prop === 'regionCode' || column.prop === 'regionName'"
                  >
                    {{ column.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </el-collapse-item>
          </el-collapse>
          <div class="column-actions">
            <el-button size="small" @click="selectAllColumns">全�?/el-button>
            <el-button size="small" @click="unselectAllColumns">取消全�?/el-button>
            <el-button size="small" @click="resetColumns">重置</el-button>
          </div>
        </div>
      </div>

      <!-- 双表格显�?-->
      <div v-if="resultData && resultData.isDualTable" class="dual-table-section">
        <!-- 表格1：一级指标权重计�?-->
        <div class="table-section">
          <h4>一级指标权重计�?/h4>
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
          
          <!-- 统计信息已移�?-->
        </div>

        <!-- 表格2：乡镇减灾能力权重计�?-->
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
          
          <!-- 统计信息已移�?-->
        </div>
      </div>

      <!-- 单表格显示（原有逻辑�?-->
  <div v-else-if="resultData && !resultData.isDualTable" class="result-table-section">
    <h4>计算结果</h4>
    <!-- 列显�?隐藏控制（单表格�?- 优化�?-->
    <div class="column-control" v-if="resultData">
      <div class="control-header">
        <h4>列显示控�?/h4>
        <div class="quick-actions">
          <el-button size="small" @click="selectAllColumns">全�?/el-button>
          <el-button size="small" @click="unselectAllColumns">取消全�?/el-button>
          <el-button size="small" @click="resetColumns">重置</el-button>
        </div>
      </div>
      
      <!-- 按步骤选择（下拉框方式�?-->
      <div class="step-selector">
        <el-select 
          v-model="selectedGroupKeys" 
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择要显示的步骤"
          style="width: 100%"
          @change="handleGroupSelectionChange"
        >
          <el-option
            v-for="group in columnGroups"
            :key="group.key"
            :label="`${group.name}�?{group.columns.length}列）`"
            :value="group.key"
          />
        </el-select>
      </div>
      
      <!-- 详细列选择（可折叠�?-->
      <el-collapse class="detail-collapse">
        <el-collapse-item title="详细列选择">
          <div class="groups-container">
            <div v-for="group in columnGroups" :key="group.key" class="group-section">
              <div class="group-header">
                <span class="group-name">{{ group.name }}（{{ group.columns.length }}列）</span>
                <div class="group-btn">
                  <el-button size="small" text @click="selectGroupColumns(group.key)">选择</el-button>
                  <el-button size="small" text @click="unselectGroupColumns(group.key)">取消</el-button>
                </div>
              </div>
              <div class="column-checkboxes">
                <el-checkbox-group v-model="visibleColumns">
                  <el-checkbox 
                    v-for="column in group.columns" 
                    :key="column.prop"
                    :label="column.prop"
                    :disabled="column.prop === 'regionCode' || column.prop === 'regionName'"
                  >
                    {{ column.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
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
          v-for="(column, index) in filteredColumns"
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
        
        <!-- 统计信息已移�?-->
      </div>

      <!-- 多步骤结果显�?-->
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

      <!-- 空状�?-->
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
          生成专题�?        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { modelManagementApi } from '@/api'

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
  // 单表格数据结�?  tableData?: any[]
  columns?: any[]
  summary?: Record<string, any>
  
  // 双表格数据结�?  isDualTable?: boolean
  table1Data?: any[]
  table1Columns?: any[]
  table1Summary?: Record<string, any>
  table2Data?: any[]
  table2Columns?: any[]
  table2Summary?: Record<string, any>
  
  // 多步骤数据结�?  isMultiStep?: boolean
  stepResults?: StepResult[]
  selectedStep?: number
}

interface Props {
  modelValue: boolean
  stepInfo?: StepInfo
  formula?: string
  resultData?: ResultData
  modelId?: number
  algorithmId?: number
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
// 后端步骤-算法输出映射与步骤名�?const stepAlgorithmOutputs = ref<Record<number, Set<string>>>({})
const stepOrderNames = ref<Record<number, string>>({})
// 下拉框选中的分组key
const selectedGroupKeys = ref<string[]>([])
// 列分�?type ColumnItem = { prop: string; label: string; width?: number; formatter?: any }
type ColumnGroup = { key: string; name: string; columns: ColumnItem[] }

// 中文步骤识别规则：更精确的匹�?// 步骤1: 原始数据 / 基础数据
const reStep1 = /(原始|基础|调查|源数�?/
// 步骤2: 属性向量归一�?const reStep2 = /(归一化|标准化|normalized)/i
// 步骤3: 二级指标定权
const reStep3 = /(定权|权重|weight)/i
// 步骤4: 优劣解计�?const reStep4 = /(优解|劣解|ideal|solution)/i
// 步骤5: 能力值计算与分级
const reStep5 = /(值与分级|能力值|评估等级|综合得分|score|grade)/i

const isBase = (c: ColumnItem) => c.prop === 'region' || c.prop === 'regionCode' || c.prop === 'regionName'

// 智能匹配函数：同时检�?label �?prop
const matchesStep = (c: ColumnItem, regex: RegExp): boolean => {
  return regex.test(String(c.label)) || regex.test(c.prop)
}

const isStep1 = (c: ColumnItem) => matchesStep(c, reStep1)
const isStep2 = (c: ColumnItem) => matchesStep(c, reStep2)
const isStep3 = (c: ColumnItem) => matchesStep(c, reStep3)
const isStep4 = (c: ColumnItem) => matchesStep(c, reStep4)
const isStep5 = (c: ColumnItem) => matchesStep(c, reStep5)

// 列分组计算：优先使用后端步骤-算法输出映射，未覆盖列使用关键字兜底
const columnGroups = computed<ColumnGroup[]>(() => {
  const base: ColumnItem[] = []
  const assigned = new Set<string>()
  const dynamicStepGroups: ColumnGroup[] = []

  console.log('\n=== 开始计算列分组 ===')
  console.log('Computing column groups with:', {
    allColumnsCount: allColumns.value.length,
    allColumnsList: allColumns.value.map(c => ({ prop: c.prop, label: c.label, stepOrder: (c as any).stepOrder })),
    stepAlgorithmOutputs: stepAlgorithmOutputs.value,
    stepOrderNames: stepOrderNames.value,
    isMultiStep: props.resultData?.isMultiStep
  })

  // 基础列优�?  allColumns.value.forEach(c => {
    if (isBase(c)) {
      base.push(c)
      assigned.add(c.prop)
      console.log(`基础�? ${c.prop}`)
    }
  })

  // 方法1: 优先使用列中自带�?stepOrder 字段
  const stepGroupsFromColumnData: Record<number, ColumnItem[]> = {}
  let hasStepOrderInColumns = false
  
  allColumns.value.forEach(c => {
    if (assigned.has(c.prop)) return
    const stepOrder = (c as any).stepOrder
    if (stepOrder !== undefined && stepOrder !== null) {
      hasStepOrderInColumns = true
      if (!stepGroupsFromColumnData[stepOrder]) {
        stepGroupsFromColumnData[stepOrder] = []
      }
      stepGroupsFromColumnData[stepOrder].push(c)
      assigned.add(c.prop)
      console.log(`  �?列自带stepOrder: ${c.prop} -> 步骤${stepOrder}`)
    }
  })
  
  if (hasStepOrderInColumns) {
    console.log('\n检测到列数据中包含stepOrder字段，使用该字段进行分组')
    const orders = Object.keys(stepGroupsFromColumnData)
      .map(n => Number(n))
      .sort((a, b) => a - b)
    
    orders.forEach(order => {
      const cols = stepGroupsFromColumnData[order]
      const title = stepOrderNames.value[order]
        ? `步骤${order} ${stepOrderNames.value[order]}`
        : `步骤${order}`
      dynamicStepGroups.push({ key: `step_${order}`, name: title, columns: cols })
      console.log(`�?添加分组(从列数据): ${title} �?${cols.length} 列`)
    })
  }

  // 方法2: 如果列数据没有stepOrder，使用后端步骤输出映�?  if (!hasStepOrderInColumns) {
    const orders = Object.keys(stepAlgorithmOutputs.value)
      .map(n => Number(n))
      .sort((a, b) => a - b)
    
    console.log('\n列数据中没有stepOrder，使用后端步骤映�?', orders)
    
    orders.forEach(order => {
      const outputs = stepAlgorithmOutputs.value[order]
      const cols: ColumnItem[] = []
      
      console.log(`\n步骤 ${order} 的输出参�?`, Array.from(outputs || []))
      
      allColumns.value.forEach(c => {
        if (!assigned.has(c.prop) && outputs?.has(c.prop)) {
          cols.push(c)
          assigned.add(c.prop)
          console.log(`  �?匹配: ${c.prop} -> 步骤${order}`)
        }
      })
      
      if (cols.length) {
        const title = stepOrderNames.value[order]
          ? `步骤${order} ${stepOrderNames.value[order]}`
          : `步骤${order}`
        dynamicStepGroups.push({ key: `step_${order}`, name: title, columns: cols })
        console.log(`�?添加动态分�? ${title} �?${cols.length} 列`)
      } else {
        console.log(`�?步骤${order} 没有匹配到任何列`)
      }
    })
  }

  // 关键字兑底分�?  console.log('\n开始关键字兑底分组')
  const step1: ColumnItem[] = []  // 原始数据
  const step2: ColumnItem[] = []  // 归一�?  const step3: ColumnItem[] = []  // 定权
  const step4: ColumnItem[] = []  // 优劣�?  const step5: ColumnItem[] = []  // 值与分级
  const others: ColumnItem[] = []
  
  const unassignedColumns = allColumns.value.filter(c => !assigned.has(c.prop))
  console.log(`未分配的列（${unassignedColumns.length}）`, unassignedColumns.map(c => ({ prop: c.prop, label: c.label })))
  
  allColumns.value.forEach(c => {
    if (assigned.has(c.prop)) return
    
    // 按照步骤顺序匹配，优先匹配更具体的规�?    // 步骤5最具体，优先匹�?    if (isStep5(c)) { 
      step5.push(c)
      assigned.add(c.prop)
      console.log(`  �?关键字匹配[步骤5-值与分级]: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep4(c)) { 
      step4.push(c)
      assigned.add(c.prop)
      console.log(`  �?关键字匹配[步骤4-优劣解]: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep3(c)) { 
      step3.push(c)
      assigned.add(c.prop)
      console.log(`  �?关键字匹配[步骤3-定权]: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep2(c)) { 
      step2.push(c)
      assigned.add(c.prop)
      console.log(`  �?关键字匹配[步骤2-归一化]: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep1(c)) { 
      step1.push(c)
      assigned.add(c.prop)
      console.log(`  �?关键字匹配[步骤1-原始数据]: ${c.prop} (${c.label})`)
      return 
    }
    
    others.push(c)
    console.log(`  ? 未匹�? ${c.prop} (${c.label})`)
  })

  console.log('\n组装最终分�?)
  const groups: ColumnGroup[] = []
  if (base.length) {
    groups.push({ key: 'base', name: '基础信息', columns: base })
    console.log(`  + 基础信息: ${base.length}列`)
  }
  
  // 先添加后端步骤映射的分组
  dynamicStepGroups.forEach(g => {
    groups.push(g)
    console.log(`  + ${g.name}: ${g.columns.length}列`)
  })
  
  // 再添加关键字匹配的分组（按步骤顺序）
  if (step1.length) {
    const stepName = stepOrderNames.value[1] || '原始数据'
    groups.push({ key: 'step_1', name: `步骤1 ${stepName}`, columns: step1 })
    console.log(`  + 步骤1 ${stepName}(关键�?: ${step1.length}列`)
  }
  if (step2.length) {
    const stepName = stepOrderNames.value[2] || '属性向量归一�?
    groups.push({ key: 'step_2', name: `步骤2 ${stepName}`, columns: step2 })
    console.log(`  + 步骤2 ${stepName}(关键�?: ${step2.length}列`)
  }
  if (step3.length) {
    const stepName = stepOrderNames.value[3] || '二级指标定权'
    groups.push({ key: 'step_3', name: `步骤3 ${stepName}`, columns: step3 })
    console.log(`  + 步骤3 ${stepName}(关键�?: ${step3.length}列`)
  }
  if (step4.length) {
    const stepName = stepOrderNames.value[4] || '优劣解计�?
    groups.push({ key: 'step_4', name: `步骤4 ${stepName}`, columns: step4 })
    console.log(`  + 步骤4 ${stepName}(关键�?: ${step4.length}列`)
  }
  if (step5.length) {
    const stepName = stepOrderNames.value[5] || '能力值计算与分级'
    groups.push({ key: 'step_5', name: `步骤5 ${stepName}`, columns: step5 })
    console.log(`  + 步骤5 ${stepName}(关键�?: ${step5.length}列`)
  }
  
  if (others.length) {
    groups.push({ key: 'others', name: '其他输出', columns: others })
    console.log(`  + 其他输出: ${others.length}列`)
  }
  
  console.log('\n=== 列分组完�?===')
  console.log('Final column groups summary:', groups.map(g => ({ 
    key: g.key, 
    name: g.name, 
    columnCount: g.columns.length,
    columns: g.columns.map(c => c.prop)
  })))
  
  return groups
})

// 计算对话框标�?const dialogTitle = computed(() => {
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
  
  // 确保返回的数据包�?tableData 字段
  console.log('Current step result:', {
    stepOrder: stepResult.stepOrder,
    stepName: stepResult.stepName,
    hasTableData: !!stepResult.tableData,
    tableDataLength: stepResult.tableData?.length
  })
  
  return stepResult
})

// 过滤后的列（按分组顺序展现）
const filteredColumns = computed(() => {
  const visibleSet = new Set(visibleColumns.value)
  const ordered: ColumnItem[] = []
  columnGroups.value.forEach(group => {
    group.columns.forEach(c => {
      if (visibleSet.has(c.prop)) ordered.push(c)
    })
  })
  return ordered
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
        resultData: props.resultData,
        modelId: props.modelId
      })
      
      // 初始化列配置（支持单表与多步骤）
      if (props.resultData?.isMultiStep && props.resultData.stepResults) {
        selectedStepOrder.value = props.resultData.stepResults[0]?.stepOrder || 1
      }
      // 拉取模型详情以构建后端步骤分组（多步骤和单表格模式都需要）
      if (props.modelId) {
        loadModelDetail(props.modelId)
      } else {
        // 如果没有modelId，直接初始化�?        initializeColumns()
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
      // 单表格变化时初始化列
      initializeColumns()
    }
  },
  { deep: true, immediate: true }
)

// 监听visible变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 关闭对话�?const handleClose = () => {
  visible.value = false
}

// 处理步骤切换
const handleStepChange = (stepOrder: number) => {
  selectedStepOrder.value = stepOrder
  initializeColumns()
}

// 初始化列配置（通用化：支持多步骤与单表格模式）
const initializeColumns = () => {
  // 多步骤模�?  if (props.resultData?.isMultiStep) {
    if (!currentStepData.value?.tableData || currentStepData.value.tableData.length === 0) {
      console.log('No table data available for columns initialization (multi-step)')
      allColumns.value = []
      visibleColumns.value = []
      return
    }

    const firstRow = currentStepData.value.tableData[0]
    const columns: any[] = []
    console.log('Initializing columns from first row (multi-step):', Object.keys(firstRow))
    Object.keys(firstRow).forEach(key => {
      columns.push({
        prop: key,
        label: getColumnLabel(key),
        width: getColumnWidth(key)
      })
    })
      allColumns.value = columns
      visibleColumns.value = columns.map(col => col.prop)
      console.log('Columns initialized (multi-step):', {
        totalColumns: columns.length,
        visibleColumns: visibleColumns.value.length
      })
      
      // 初始化下拉框选择：默认全�?      selectedGroupKeys.value = columnGroups.value.map(g => g.key)
      return
    }

  // 单表格模�?  if (props.resultData) {
    const columnsFromProps = props.resultData.columns || []
    const tableData = props.resultData.tableData || []

    if (columnsFromProps.length > 0) {
      // 直接使用 props.columns，保留已有label/width/formatter/stepOrder
      allColumns.value = columnsFromProps.map(col => ({
        prop: col.prop,
        label: col.label || getColumnLabel(col.prop),
        width: col.width || getColumnWidth(col.prop),
        formatter: col.formatter,
        stepOrder: (col as any).stepOrder  // 保留 stepOrder 字段
      }))
      visibleColumns.value = allColumns.value.map(col => col.prop)
      console.log('Columns initialized from props (single-table):', {
        totalColumns: allColumns.value.length,
        visibleColumns: visibleColumns.value.length,
        columnsWithStepOrder: allColumns.value.filter(c => (c as any).stepOrder).length
      })
      
      // 初始化下拉框选择：默认全�?      setTimeout(() => {
        selectedGroupKeys.value = columnGroups.value.map(g => g.key)
      }, 100)
      return
    }

    if (tableData.length === 0) {
      console.log('No table data available for columns initialization (single-table)')
      allColumns.value = []
      visibleColumns.value = []
      return
    }

    // 从表格数据推�?    const firstRow = tableData[0]
    const columns: any[] = []
    console.log('Initializing columns from first row (single-table):', Object.keys(firstRow))
    Object.keys(firstRow).forEach(key => {
      columns.push({
        prop: key,
        label: getColumnLabel(key),
        width: getColumnWidth(key)
      })
    })
    allColumns.value = columns
    visibleColumns.value = columns.map(col => col.prop)
    console.log('Columns initialized from data (single-table):', {
      totalColumns: columns.length,
      visibleColumns: visibleColumns.value.length
    })
  }
}

// 加载模型详情并解析每步算法输出参�?const loadModelDetail = async (modelId: number) => {
  try {
    console.log('Loading model detail for modelId:', modelId)
    const resp = await modelManagementApi.getModelDetail(modelId)
    if (resp?.success) {
      const steps = resp.data?.steps || []
      const outputsMap: Record<number, Set<string>> = {}
      const namesMap: Record<number, string> = {}
      
      console.log('Model steps received:', steps.length)
      console.log('Model steps detail:', steps)
      
      steps.forEach((step: any) => {
        const order = Number(step.stepOrder ?? step.order ?? step.step_number ?? 0)
        namesMap[order] = step.stepName ?? step.name ?? ''
        
        console.log(`Processing step ${order}: ${namesMap[order]}`)
        console.log('Step details:', {
          description: step.description,
          algorithms: step.algorithms,
          algorithmConfigs: step.algorithmConfigs
        })
        
        const outputs = new Set<string>()
        
        // 方法1: �?description 字段中的 |ALGORITHMS| 标记解析
        let algos: any[] = []
        if (typeof step.description === 'string' && step.description.includes('|ALGORITHMS|')) {
          try {
            const json = step.description.split('|ALGORITHMS|')[1]
            algos = JSON.parse(json)
            console.log(`Step ${order} algorithms from description:`, algos.length)
          } catch (err) {
            console.warn(`Failed to parse algorithms from description for step ${order}:`, err)
          }
        }
        
        // 方法2: �?algorithms 字段直接读取
        if (algos.length === 0 && Array.isArray(step.algorithms)) {
          algos = step.algorithms
          console.log(`Step ${order} algorithms from algorithms field:`, algos.length)
        }
        
        // 方法3: �?algorithmConfigs 字段读取
        if (algos.length === 0 && Array.isArray(step.algorithmConfigs)) {
          algos = step.algorithmConfigs
          console.log(`Step ${order} algorithms from algorithmConfigs:`, algos.length)
        }
        
        // 提取输出参数
        algos.forEach(a => {
          const outputParam = a?.outputParam || a?.output_param || a?.outputParameter
          if (outputParam) {
            outputs.add(String(outputParam))
            console.log(`Step ${order} output param: ${outputParam}`)
          }
        })
        
        // 如果还是没有输出参数，尝试基于步骤名称和关键字推断可能的列名模式
        if (outputs.size === 0) {
          console.log(`Step ${order} has no explicit output params, will rely on keyword matching`)
        }
        
        if (outputs.size > 0) {
          outputsMap[order] = outputs
        }
      })
      
      stepAlgorithmOutputs.value = outputsMap
      stepOrderNames.value = namesMap
      
      console.log('Step algorithm outputs map:', outputsMap)
      console.log('Step order names map:', namesMap)
      
      // 模型详情到达后刷新列分组
      initializeColumns()
    }
  } catch (error: any) {
    console.warn('加载模型详情失败，使用关键字分组作为回退: ', error?.message || error)
    // 即使失败也要初始化列
    initializeColumns()
  }
}

// 获取列标�?const getColumnLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    'regionCode': '地区代码',
    'regionName': '地区名称'
  }
  return labelMap[key] || key
}

// 获取列宽�?const getColumnWidth = (key: string) => {
  if (key === 'regionCode') return 150
  if (key === 'regionName') return 120
  return 120
}

// 选中所有列
const selectAllColumns = () => {
  visibleColumns.value = allColumns.value.map(col => col.prop)
  // 同步更新下拉框：选中所有分�?  selectedGroupKeys.value = columnGroups.value.map(g => g.key)
  console.log('�?全选：更新下拉框选中', selectedGroupKeys.value)
}

// 取消选中所有列
const unselectAllColumns = () => {
  // 保留必需的列
  visibleColumns.value = allColumns.value
    .filter(col => col.prop === 'regionCode' || col.prop === 'regionName')
    .map(col => col.prop)
  // 同步更新下拉框：只选中基础信息分组
  selectedGroupKeys.value = ['base']
  console.log('�?取消全选：更新下拉框选中', selectedGroupKeys.value)
}

// 重置列显�?const resetColumns = () => {
  visibleColumns.value = allColumns.value.map(col => col.prop)
  // 同步更新下拉框：选中所有分�?  selectedGroupKeys.value = columnGroups.value.map(g => g.key)
  console.log('�?重置：更新下拉框选中', selectedGroupKeys.value)
}

// 处理下拉框选择变化
const handleGroupSelectionChange = (selectedKeys: string[]) => {
  console.log('选中的分�?', selectedKeys)
  
  // 获取所有选中分组的列
  const selectedCols = new Set<string>()
  
  // 始终包含基础�?  selectedCols.add('regionCode')
  selectedCols.add('regionName')
  
  selectedKeys.forEach(key => {
    const group = columnGroups.value.find(g => g.key === key)
    if (group) {
      group.columns.forEach(c => selectedCols.add(c.prop))
    }
  })
  
  visibleColumns.value = Array.from(selectedCols)
  console.log('更新后的可见�?', visibleColumns.value.length)
}

// 选择/取消某一分组
const selectGroupColumns = (groupKey: string) => {
  const group = columnGroups.value.find(g => g.key === groupKey)
  if (!group) return
  const current = new Set(visibleColumns.value)
  group.columns.forEach(c => current.add(c.prop))
  visibleColumns.value = Array.from(current)
  
  // 同步更新下拉框选中
  if (!selectedGroupKeys.value.includes(groupKey)) {
    selectedGroupKeys.value.push(groupKey)
  }
}

const unselectGroupColumns = (groupKey: string) => {
  const group = columnGroups.value.find(g => g.key === groupKey)
  if (!group) return
  const retain = new Set(
    allColumns.value
      .filter(col => col.prop === 'regionCode' || col.prop === 'regionName' || col.prop === 'region')
      .map(col => col.prop)
  )
  const current = new Set(visibleColumns.value)
  group.columns.forEach(c => current.delete(c.prop))
  // 保留基础�?  retain.forEach(r => current.add(r))
  visibleColumns.value = Array.from(current)
  
  // 同步更新下拉框选中
  const index = selectedGroupKeys.value.indexOf(groupKey)
  if (index > -1) {
    selectedGroupKeys.value.splice(index, 1)
  }
}

// 导出结果
const exportResults = () => {
  if (!props.resultData) {
    ElMessage.warning('暂无数据可导�?)
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
    
    // 生成文件�?    const fileName = `${props.stepInfo?.stepName || '计算结果'}_${new Date().toISOString().slice(0, 10)}.csv`
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

// 生成专题�?const generateThematicMap = () => {
  if (!props.resultData) {
    ElMessage.warning('暂无数据可生成专题图')
    return
  }

  try {
    // 从resultData中提取数�?    const { tableData, columns, summary } = props.resultData
    
    // 构建专题图数据，匹配ThematicMap.vue期望的数据结�?    const thematicData = {
      id: Date.now(), // 生成唯一ID
      regionName: props.stepInfo?.stepName || '评估区域',
      evaluationTime: new Date().toLocaleString('zh-CN'),
      algorithm: props.stepInfo?.stepCode || 'default',
      totalScore: summary?.总分 || summary?.平均�?|| '未知',
      stepInfo: props.stepInfo,
      formula: props.formula,
      resultData: props.resultData,
      tableData: tableData,
      columns: columns,
      summary: summary,
      timestamp: new Date().toISOString(),
      source: 'evaluation_calculation'
    }
    
    console.log('存储专题图数�?', thematicData)
    
    // 将数据存储到 sessionStorage
    sessionStorage.setItem('thematicMapData', JSON.stringify(thematicData))
    
    // 跳转到专题图页面
    router.push('/thematic-map')
    
    ElMessage.success('正在跳转到专题图页面...')
  } catch (error) {
    console.error('生成专题图失�?', error)
    ElMessage.error('生成专题图失�?)
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
    // 双表格数据导�?    const { table1Data, table1Columns, table1Summary, table2Data, table2Columns, table2Summary } = props.resultData
    
    // 表格1
    if (table1Data && table1Columns) {
      lines.push('一级指标权重计�?)
      
      // 表格1表头
      const headers1 = table1Columns.map(col => col.label).join(',')
      lines.push(headers1)
      
      // 表格1数据�?      table1Data.forEach(row => {
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
      
      // 表格2数据�?      table2Data.forEach(row => {
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
    // 单表格数据导出（原有逻辑�?    const { tableData, columns, summary } = props.resultData
    
    if (tableData && columns) {
      // 添加表头
      const headers = columns.map(col => col.label).join(',')
      lines.push(headers)
      
      // 添加数据�?      tableData.forEach(row => {
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
      .control-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        h4 {
          margin: 0;
        }
        
        .quick-actions {
          display: flex;
          gap: 8px;
        }
      }
      
      .step-selector {
        margin-bottom: 16px;
      }
      
      .detail-collapse {
        margin-top: 12px;
      }
      
      .groups-container {
        .group-section {
          margin-bottom: 16px;
          padding: 12px;
          background-color: white;
          border-radius: 4px;
          border: 1px solid #e0e0e0;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .group-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            padding-bottom: 8px;
            border-bottom: 1px solid #eee;
            
            .group-name {
              font-weight: 500;
              color: #333;
            }
            
            .group-btn {
              display: flex;
              gap: 8px;
            }
          }
        }
      }
      
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

.group-actions {
  margin-bottom: 8px;
}

/* 响应式设�?*/
@media (max-width: 768px) {
  .result-dialog {
    .summary-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
