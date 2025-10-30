<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="90%"
    :before-close="handleClose"
    class="result-dialog"
  >
    <div class="result-content">
      <!-- 姝ラ淇℃伅 -->
      <div class="step-info" v-if="stepInfo">
        <h3>{{ stepInfo.stepName }}</h3>
        <p class="step-description">{{ stepInfo.description }}</p>
      </div>

      <!-- 鍏紡灞曠ず -->
      <div class="formula-section" v-if="formula">
        <h4>璁＄畻鍏紡</h4>
        <div class="formula-code">
          <code>{{ formula }}</code>
        </div>
      </div>

      <!-- 姝ラ鍒嗙粍鎺у埗 -->
      <div v-if="resultData && resultData.isMultiStep" class="step-control-section">
        <div class="step-selector">
          <h4>閫夋嫨鏌ョ湅姝ラ</h4>
          <el-select 
            v-model="selectedStepOrder" 
            placeholder="閫夋嫨姝ラ"
            @change="handleStepChange"
            style="width: 200px;"
          >
            <el-option
              v-for="step in resultData.stepResults"
              :key="step.stepOrder"
              :label="`姝ラ${step.stepOrder}: ${step.stepName}`"
              :value="step.stepOrder"
            />
          </el-select>
        </div>
        
        <!-- 鍒楁樉绀?闅愯棌鎺у埗锛堟寜姝ラ鍒嗙粍锛?-->
        <div class="column-control" v-if="currentStepData">
          <h4>鍒楁樉绀烘帶鍒?/h4>
          <!-- 鍒嗙粍鎶樺彔闈㈡澘 -->
          <el-collapse>
            <el-collapse-item
              v-for="group in columnGroups"
              :key="group.key"
              :title="`${group.name}锛?{group.columns.length}鍒楋級`"
            >
              <div class="group-actions">
                <el-button size="small" @click="selectGroupColumns(group.key)">閫夋嫨璇ョ粍</el-button>
                <el-button size="small" @click="unselectGroupColumns(group.key)">鍙栨秷璇ョ粍</el-button>
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
            <el-button size="small" @click="selectAllColumns">鍏ㄩ€?/el-button>
            <el-button size="small" @click="unselectAllColumns">鍙栨秷鍏ㄩ€?/el-button>
            <el-button size="small" @click="resetColumns">閲嶇疆</el-button>
          </div>
        </div>
      </div>

      <!-- 鍙岃〃鏍兼樉绀?-->
      <div v-if="resultData && resultData.isDualTable" class="dual-table-section">
        <!-- 琛ㄦ牸1锛氫竴绾ф寚鏍囨潈閲嶈绠?-->
        <div class="table-section">
          <h4>涓€绾ф寚鏍囨潈閲嶈绠?/h4>
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
          
          <!-- 缁熻淇℃伅宸茬Щ闄?-->
        </div>

        <!-- 琛ㄦ牸2锛氫埂闀囧噺鐏捐兘鍔涙潈閲嶈绠?-->
        <div class="table-section" style="margin-top: 30px;">
          <h4>涔￠晣鍑忕伨鑳藉姏鏉冮噸璁＄畻</h4>
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
          
          <!-- 缁熻淇℃伅宸茬Щ闄?-->
        </div>
      </div>

      <!-- 鍗曡〃鏍兼樉绀猴紙鍘熸湁閫昏緫锛?-->
  <div v-else-if="resultData && !resultData.isDualTable" class="result-table-section">
    <h4>璁＄畻缁撴灉</h4>
    <!-- 鍒楁樉绀?闅愯棌鎺у埗锛堝崟琛ㄦ牸锛?- 浼樺寲鐗?-->
    <div class="column-control" v-if="resultData">
      <div class="control-header">
        <h4>鍒楁樉绀烘帶鍒?/h4>
        <div class="quick-actions">
          <el-button size="small" @click="selectAllColumns">鍏ㄩ€?/el-button>
          <el-button size="small" @click="unselectAllColumns">鍙栨秷鍏ㄩ€?/el-button>
          <el-button size="small" @click="resetColumns">閲嶇疆</el-button>
        </div>
      </div>
      
      <!-- 鎸夋楠ら€夋嫨锛堜笅鎷夋鏂瑰紡锛?-->
      <div class="step-selector">
        <el-select 
          v-model="selectedGroupKeys" 
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="閫夋嫨瑕佹樉绀虹殑姝ラ"
          style="width: 100%"
          @change="handleGroupSelectionChange"
        >
          <el-option
            v-for="group in columnGroups"
            :key="group.key"
            :label="`${group.name}锛?{group.columns.length}鍒楋級`"
            :value="group.key"
          />
        </el-select>
      </div>
      
      <!-- 璇︾粏鍒楅€夋嫨锛堝彲鎶樺彔锛?-->
      <el-collapse class="detail-collapse">
        <el-collapse-item title="璇︾粏鍒楅€夋嫨">
          <div class="groups-container">
            <div v-for="group in columnGroups" :key="group.key" class="group-section">
              <div class="group-header">
                <span class="group-name">{{ group.name }}锛坽{ group.columns.length }}鍒楋級</span>
                <div class="group-btn">
                  <el-button size="small" text @click="selectGroupColumns(group.key)">閫夋嫨</el-button>
                  <el-button size="small" text @click="unselectGroupColumns(group.key)">鍙栨秷</el-button>
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
              :data="filteredTableData"
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
        
        <!-- 缁熻淇℃伅宸茬Щ闄?-->
      </div>

      <!-- 澶氭楠ょ粨鏋滄樉绀?-->
      <div v-if="resultData && resultData.isMultiStep" class="multi-step-section">
        <div v-if="currentStepData" class="current-step-result">
          <h4>{{ currentStepData.stepName }} - 璁＄畻缁撴灉</h4>
          <p class="step-description">{{ currentStepData.description }}</p>
          
          <div class="table-container">
            <el-table
              :data="filteredTableData"
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
          <el-empty description="璇烽€夋嫨瑕佹煡鐪嬬殑姝ラ" :image-size="80" />
        </div>
      </div>

      <!-- 绌虹姸鎬?-->
      <div v-if="!resultData" class="empty-state">
        <el-empty description="鏆傛棤璁＄畻缁撴灉" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">鍏抽棴</el-button>
        <el-button type="primary" @click="exportResults" :disabled="!resultData">
          瀵煎嚭缁撴灉
        </el-button>
        <el-button type="success" @click="generateThematicMap" :disabled="!resultData">
          鐢熸垚涓撻鍥?        </el-button>
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
  // 鍗曡〃鏍兼暟鎹粨鏋?  tableData?: any[]
  columns?: any[]
  summary?: Record<string, any>
  
  // 鍙岃〃鏍兼暟鎹粨鏋?  isDualTable?: boolean
  table1Data?: any[]
  table1Columns?: any[]
  table1Summary?: Record<string, any>
  table2Data?: any[]
  table2Columns?: any[]
  table2Summary?: Record<string, any>
  
  // 澶氭楠ゆ暟鎹粨鏋?  isMultiStep?: boolean
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
// 鍚庣姝ラ-绠楁硶杈撳嚭鏄犲皠涓庢楠ゅ悕绉?const stepAlgorithmOutputs = ref<Record<number, Set<string>>>({})
const stepOrderNames = ref<Record<number, string>>({})
// 涓嬫媺妗嗛€変腑鐨勫垎缁刱ey
const selectedGroupKeys = ref<string[]>([])
// 鍒楀垎缁?type ColumnItem = { prop: string; label: string; width?: number; formatter?: any }
type ColumnGroup = { key: string; name: string; columns: ColumnItem[] }

// 涓枃姝ラ璇嗗埆瑙勫垯锛氭洿绮剧‘鐨勫尮閰?// 姝ラ1: 鍘熷鏁版嵁 / 鍩虹鏁版嵁
const reStep1 = /(鍘熷|鍩虹|璋冩煡|婧愭暟鎹?/
// 姝ラ2: 灞炴€у悜閲忓綊涓€鍖?const reStep2 = /(褰掍竴鍖東鏍囧噯鍖東normalized)/i
// 姝ラ3: 浜岀骇鎸囨爣瀹氭潈
const reStep3 = /(瀹氭潈|鏉冮噸|weight)/i
// 姝ラ4: 浼樺姡瑙ｈ绠?const reStep4 = /(浼樿В|鍔ｈВ|ideal|solution)/i
// 姝ラ5: 鑳藉姏鍊艰绠椾笌鍒嗙骇
const reStep5 = /(鍊间笌鍒嗙骇|鑳藉姏鍊紎璇勪及绛夌骇|缁煎悎寰楀垎|score|grade)/i

const isBase = (c: ColumnItem) => c.prop === 'region' || c.prop === 'regionCode' || c.prop === 'regionName'

// 鏅鸿兘鍖归厤鍑芥暟锛氬悓鏃舵鏌?label 鍜?prop
const matchesStep = (c: ColumnItem, regex: RegExp): boolean => {
  return regex.test(String(c.label)) || regex.test(c.prop)
}

const isStep1 = (c: ColumnItem) => matchesStep(c, reStep1)
const isStep2 = (c: ColumnItem) => matchesStep(c, reStep2)
const isStep3 = (c: ColumnItem) => matchesStep(c, reStep3)
const isStep4 = (c: ColumnItem) => matchesStep(c, reStep4)
const isStep5 = (c: ColumnItem) => matchesStep(c, reStep5)

// 鍒楀垎缁勮绠楋細浼樺厛浣跨敤鍚庣姝ラ-绠楁硶杈撳嚭鏄犲皠锛屾湭瑕嗙洊鍒椾娇鐢ㄥ叧閿瓧鍏滃簳
const columnGroups = computed<ColumnGroup[]>(() => {
  const base: ColumnItem[] = []
  const assigned = new Set<string>()
  const dynamicStepGroups: ColumnGroup[] = []

  console.log('\n=== 寮€濮嬭绠楀垪鍒嗙粍 ===')
  console.log('Computing column groups with:', {
    allColumnsCount: allColumns.value.length,
    allColumnsList: allColumns.value.map(c => ({ prop: c.prop, label: c.label, stepOrder: (c as any).stepOrder })),
    stepAlgorithmOutputs: stepAlgorithmOutputs.value,
    stepOrderNames: stepOrderNames.value,
    isMultiStep: props.resultData?.isMultiStep
  })

  // 鍩虹鍒椾紭鍏?  allColumns.value.forEach(c => {
    if (isBase(c)) {
      base.push(c)
      assigned.add(c.prop)
      console.log(`鍩虹鍒? ${c.prop}`)
    }
  })

  // 鏂规硶1: 浼樺厛浣跨敤鍒椾腑鑷甫鐨?stepOrder 瀛楁
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
      console.log(`  鉁?鍒楄嚜甯tepOrder: ${c.prop} -> 姝ラ${stepOrder}`)
    }
  })
  
  if (hasStepOrderInColumns) {
    console.log('\n妫€娴嬪埌鍒楁暟鎹腑鍖呭惈stepOrder瀛楁锛屼娇鐢ㄨ瀛楁杩涜鍒嗙粍')
    const orders = Object.keys(stepGroupsFromColumnData)
      .map(n => Number(n))
      .sort((a, b) => a - b)
    
    orders.forEach(order => {
      const cols = stepGroupsFromColumnData[order]
      const title = stepOrderNames.value[order]
        ? `姝ラ${order} ${stepOrderNames.value[order]}`
        : `姝ラ${order}`
      dynamicStepGroups.push({ key: `step_${order}`, name: title, columns: cols })
      console.log(`鉁?娣诲姞鍒嗙粍(浠庡垪鏁版嵁): ${title} 鍏?${cols.length} 鍒梎)
    })
  }

  // 鏂规硶2: 濡傛灉鍒楁暟鎹病鏈塻tepOrder锛屼娇鐢ㄥ悗绔楠よ緭鍑烘槧灏?  if (!hasStepOrderInColumns) {
    const orders = Object.keys(stepAlgorithmOutputs.value)
      .map(n => Number(n))
      .sort((a, b) => a - b)
    
    console.log('\n鍒楁暟鎹腑娌℃湁stepOrder锛屼娇鐢ㄥ悗绔楠ゆ槧灏?', orders)
    
    orders.forEach(order => {
      const outputs = stepAlgorithmOutputs.value[order]
      const cols: ColumnItem[] = []
      
      console.log(`\n姝ラ ${order} 鐨勮緭鍑哄弬鏁?`, Array.from(outputs || []))
      
      allColumns.value.forEach(c => {
        if (!assigned.has(c.prop) && outputs?.has(c.prop)) {
          cols.push(c)
          assigned.add(c.prop)
          console.log(`  鉁?鍖归厤: ${c.prop} -> 姝ラ${order}`)
        }
      })
      
      if (cols.length) {
        const title = stepOrderNames.value[order]
          ? `姝ラ${order} ${stepOrderNames.value[order]}`
          : `姝ラ${order}`
        dynamicStepGroups.push({ key: `step_${order}`, name: title, columns: cols })
        console.log(`鉁?娣诲姞鍔ㄦ€佸垎缁? ${title} 鍏?${cols.length} 鍒梎)
      } else {
        console.log(`鉁?姝ラ${order} 娌℃湁鍖归厤鍒颁换浣曞垪`)
      }
    })
  }

  // 鍏抽敭瀛楀厬搴曞垎缁?  console.log('\n寮€濮嬪叧閿瓧鍏戝簳鍒嗙粍')
  const step1: ColumnItem[] = []  // 鍘熷鏁版嵁
  const step2: ColumnItem[] = []  // 褰掍竴鍖?  const step3: ColumnItem[] = []  // 瀹氭潈
  const step4: ColumnItem[] = []  // 浼樺姡瑙?  const step5: ColumnItem[] = []  // 鍊间笌鍒嗙骇
  const others: ColumnItem[] = []
  
  const unassignedColumns = allColumns.value.filter(c => !assigned.has(c.prop))
  console.log(`鏈垎閰嶇殑鍒楋紙${unassignedColumns.length}锛塦, unassignedColumns.map(c => ({ prop: c.prop, label: c.label })))
  
  allColumns.value.forEach(c => {
    if (assigned.has(c.prop)) return
    
    // 鎸夌収姝ラ椤哄簭鍖归厤锛屼紭鍏堝尮閰嶆洿鍏蜂綋鐨勮鍒?    // 姝ラ5鏈€鍏蜂綋锛屼紭鍏堝尮閰?    if (isStep5(c)) { 
      step5.push(c)
      assigned.add(c.prop)
      console.log(`  鉁?鍏抽敭瀛楀尮閰峓姝ラ5-鍊间笌鍒嗙骇]: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep4(c)) { 
      step4.push(c)
      assigned.add(c.prop)
      console.log(`  鉁?鍏抽敭瀛楀尮閰峓姝ラ4-浼樺姡瑙: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep3(c)) { 
      step3.push(c)
      assigned.add(c.prop)
      console.log(`  鉁?鍏抽敭瀛楀尮閰峓姝ラ3-瀹氭潈]: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep2(c)) { 
      step2.push(c)
      assigned.add(c.prop)
      console.log(`  鉁?鍏抽敭瀛楀尮閰峓姝ラ2-褰掍竴鍖朷: ${c.prop} (${c.label})`)
      return 
    }
    if (isStep1(c)) { 
      step1.push(c)
      assigned.add(c.prop)
      console.log(`  鉁?鍏抽敭瀛楀尮閰峓姝ラ1-鍘熷鏁版嵁]: ${c.prop} (${c.label})`)
      return 
    }
    
    others.push(c)
    console.log(`  ? 鏈尮閰? ${c.prop} (${c.label})`)
  })

  console.log('\n缁勮鏈€缁堝垎缁?)
  const groups: ColumnGroup[] = []
  if (base.length) {
    groups.push({ key: 'base', name: '鍩虹淇℃伅', columns: base })
    console.log(`  + 鍩虹淇℃伅: ${base.length}鍒梎)
  }
  
  // 鍏堟坊鍔犲悗绔楠ゆ槧灏勭殑鍒嗙粍
  dynamicStepGroups.forEach(g => {
    groups.push(g)
    console.log(`  + ${g.name}: ${g.columns.length}鍒梎)
  })
  
  // 鍐嶆坊鍔犲叧閿瓧鍖归厤鐨勫垎缁勶紙鎸夋楠ら『搴忥級
  if (step1.length) {
    const stepName = stepOrderNames.value[1] || '鍘熷鏁版嵁'
    groups.push({ key: 'step_1', name: `姝ラ1 ${stepName}`, columns: step1 })
    console.log(`  + 姝ラ1 ${stepName}(鍏抽敭瀛?: ${step1.length}鍒梎)
  }
  if (step2.length) {
    const stepName = stepOrderNames.value[2] || '灞炴€у悜閲忓綊涓€鍖?
    groups.push({ key: 'step_2', name: `姝ラ2 ${stepName}`, columns: step2 })
    console.log(`  + 姝ラ2 ${stepName}(鍏抽敭瀛?: ${step2.length}鍒梎)
  }
  if (step3.length) {
    const stepName = stepOrderNames.value[3] || '浜岀骇鎸囨爣瀹氭潈'
    groups.push({ key: 'step_3', name: `姝ラ3 ${stepName}`, columns: step3 })
    console.log(`  + 姝ラ3 ${stepName}(鍏抽敭瀛?: ${step3.length}鍒梎)
  }
  if (step4.length) {
    const stepName = stepOrderNames.value[4] || '浼樺姡瑙ｈ绠?
    groups.push({ key: 'step_4', name: `姝ラ4 ${stepName}`, columns: step4 })
    console.log(`  + 姝ラ4 ${stepName}(鍏抽敭瀛?: ${step4.length}鍒梎)
  }
  if (step5.length) {
    const stepName = stepOrderNames.value[5] || '鑳藉姏鍊艰绠椾笌鍒嗙骇'
    groups.push({ key: 'step_5', name: `姝ラ5 ${stepName}`, columns: step5 })
    console.log(`  + 姝ラ5 ${stepName}(鍏抽敭瀛?: ${step5.length}鍒梎)
  }
  
  if (others.length) {
    groups.push({ key: 'others', name: '鍏朵粬杈撳嚭', columns: others })
    console.log(`  + 鍏朵粬杈撳嚭: ${others.length}鍒梎)
  }
  
  console.log('\n=== 鍒楀垎缁勫畬鎴?===')
  console.log('Final column groups summary:', groups.map(g => ({ 
    key: g.key, 
    name: g.name, 
    columnCount: g.columns.length,
    columns: g.columns.map(c => c.prop)
  })))
  
  return groups
})

// 璁＄畻瀵硅瘽妗嗘爣棰?const dialogTitle = computed(() => {
  if (props.resultData?.isMultiStep) {
    return '绠楁硶姝ラ鎵ц缁撴灉'
  }
  if (props.stepInfo) {
    return `${props.stepInfo.stepName} - 璁＄畻缁撴灉`
  }
  return '璁＄畻缁撴灉'
})

// 褰撳墠姝ラ鏁版嵁
const currentStepData = computed(() => {
  if (!props.resultData?.isMultiStep || !props.resultData.stepResults) {
    return null
  }
  const stepResult = props.resultData.stepResults.find(step => step.stepOrder === selectedStepOrder.value)
  if (!stepResult) {
    return null
  }
  
  // 纭繚杩斿洖鐨勬暟鎹寘鍚?tableData 瀛楁
  console.log('Current step result:', {
    stepOrder: stepResult.stepOrder,
    stepName: stepResult.stepName,
    hasTableData: !!stepResult.tableData,
    tableDataLength: stepResult.tableData?.length
  })
  
  return stepResult
})

// 杩囨护鍚庣殑鍒楋紙鎸夊垎缁勯『搴忓睍鐜帮級
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
  // 计算当前可见列涉及到的步骤序号集合
  const visibleStepOrders = computed(() => {
    const stepOrders = new Set<number>()
    filteredColumns.value.forEach((c: any) => {
      if (typeof (c as any).stepOrder === 'number') {
        stepOrders.add((c as any).stepOrder)
      }
    })
    return stepOrders
  })

  // 根据可见列推断显示行层级：步骤1->community，其它->township；混合->all
  const desiredRowLevel = computed<'community' | 'township' | 'all'>(() => {
    const s = visibleStepOrders.value
    if (s.size === 0) return 'all'
    const onlyStep1 = s.size === 1 && s.has(1)
    if (onlyStep1) return 'community'
    if (s.has(2) || s.has(3) || s.has(4) || s.has(5) || s.has(6)) return 'township'
    return 'all'
  })

  // 过滤表格行：优先依据 _regionLevel；没有则用 _rawRegionCode 是否以 TOWNSHIP_ 开头判断
  const filteredTableData = computed(() => {
    const data = (props.resultData?.isMultiStep && currentStepData.value?.tableData)
      ? currentStepData.value.tableData
      : (props.resultData?.tableData || [])

    const level = desiredRowLevel.value
    if (level === 'all') return data

    const isTownship = (row: any) => {
      if (row && typeof row._regionLevel === 'string') return row._regionLevel === 'township'
      const raw = row?._rawRegionCode || row?.regionCode || ''
      return typeof raw === 'string' && raw.startsWith('TOWNSHIP_')
    }

    return data.filter((row: any) => {
      return level === 'township' ? isTownship(row) : !isTownship(row)
    })
  })

// 鐩戝惉props鍙樺寲
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
      
      // 鍒濆鍖栧垪閰嶇疆锛堟敮鎸佸崟琛ㄤ笌澶氭楠わ級
      if (props.resultData?.isMultiStep && props.resultData.stepResults) {
        selectedStepOrder.value = props.resultData.stepResults[0]?.stepOrder || 1
      }
      // 鎷夊彇妯″瀷璇︽儏浠ユ瀯寤哄悗绔楠ゅ垎缁勶紙澶氭楠ゅ拰鍗曡〃鏍兼ā寮忛兘闇€瑕侊級
      if (props.modelId) {
        loadModelDetail(props.modelId)
      } else {
        // 濡傛灉娌℃湁modelId锛岀洿鎺ュ垵濮嬪寲鍒?        initializeColumns()
      }
    }
  },
  { immediate: true }
)

// 鐩戝惉resultData鍙樺寲
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
      // 鍗曡〃鏍煎彉鍖栨椂鍒濆鍖栧垪
      initializeColumns()
    }
  },
  { deep: true, immediate: true }
)

// 鐩戝惉visible鍙樺寲
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 鍏抽棴瀵硅瘽妗?const handleClose = () => {
  visible.value = false
}

// 澶勭悊姝ラ鍒囨崲
const handleStepChange = (stepOrder: number) => {
  selectedStepOrder.value = stepOrder
  initializeColumns()
  autoSelectStepColumns(stepOrder)
}

// 鍒濆鍖栧垪閰嶇疆锛堥€氱敤鍖栵細鏀寔澶氭楠や笌鍗曡〃鏍兼ā寮忥級
const initializeColumns = () => {
  // 澶氭楠ゆā寮?  if (props.resultData?.isMultiStep) {
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
// 根据步骤自动选择列组与可见列（含基础信息）
function autoSelectStepColumns(order: number) {
  try {
    const groups = columnGroups.value
    const baseGroup = groups.find(g => g.key === 'base')
    const stepGroup = groups.find(g => g.key === `step_${order}`)

    const selected: string[] = []
    const visible: string[] = []
    if (baseGroup) {
      selected.push('base')
      baseGroup.columns.forEach((c: any) => visible.push(c.prop))
    }
    if (stepGroup) {
      selected.push(stepGroup.key)
      stepGroup.columns.forEach((c: any) => visible.push(c.prop))
    } else {
      const cols = allColumns.value.filter((c: any) => (c as any).stepOrder === order)
      cols.forEach((c: any) => visible.push(c.prop))
      if (!selected.includes(`step_${order}`)) selected.push(`step_${order}`)
    }
    visibleColumns.value = Array.from(new Set(visible))
    selectedGroupKeys.value = selected
  } catch (e) {
    console.warn('autoSelectStepColumns failed', e)
  }
}
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
      
      // 鍒濆鍖栦笅鎷夋閫夋嫨锛氶粯璁ゅ叏閫?      autoSelectStepColumns(selectedStepOrder.value)
      return
    }

  // 鍗曡〃鏍兼ā寮?  if (props.resultData) {
    const columnsFromProps = props.resultData.columns || []
    const tableData = props.resultData.tableData || []

    if (columnsFromProps.length > 0) {
      // 鐩存帴浣跨敤 props.columns锛屼繚鐣欏凡鏈塴abel/width/formatter/stepOrder
      allColumns.value = columnsFromProps.map(col => ({
        prop: col.prop,
        label: col.label || getColumnLabel(col.prop),
        width: col.width || getColumnWidth(col.prop),
        formatter: col.formatter,
        stepOrder: (col as any).stepOrder  // 淇濈暀 stepOrder 瀛楁
      }))
      visibleColumns.value = allColumns.value.map(col => col.prop)
      console.log('Columns initialized from props (single-table):', {
        totalColumns: allColumns.value.length,
        visibleColumns: visibleColumns.value.length,
        columnsWithStepOrder: allColumns.value.filter(c => (c as any).stepOrder).length
      })
      
      // 鍒濆鍖栦笅鎷夋閫夋嫨锛氶粯璁ゅ叏閫?      setTimeout(() => {
        autoSelectStepColumns(selectedStepOrder.value)
      }, 100)
      return
    }

    if (tableData.length === 0) {
      console.log('No table data available for columns initialization (single-table)')
      allColumns.value = []
      visibleColumns.value = []
      return
    }

    // 浠庤〃鏍兼暟鎹帹鏂?    const firstRow = tableData[0]
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

// 鍔犺浇妯″瀷璇︽儏骞惰В鏋愭瘡姝ョ畻娉曡緭鍑哄弬鏁?const loadModelDetail = async (modelId: number) => {
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
        
        // 鏂规硶1: 浠?description 瀛楁涓殑 |ALGORITHMS| 鏍囪瑙ｆ瀽
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
        
        // 鏂规硶2: 浠?algorithms 瀛楁鐩存帴璇诲彇
        if (algos.length === 0 && Array.isArray(step.algorithms)) {
          algos = step.algorithms
          console.log(`Step ${order} algorithms from algorithms field:`, algos.length)
        }
        
        // 鏂规硶3: 浠?algorithmConfigs 瀛楁璇诲彇
        if (algos.length === 0 && Array.isArray(step.algorithmConfigs)) {
          algos = step.algorithmConfigs
          console.log(`Step ${order} algorithms from algorithmConfigs:`, algos.length)
        }
        
        // 鎻愬彇杈撳嚭鍙傛暟
        algos.forEach(a => {
          const outputParam = a?.outputParam || a?.output_param || a?.outputParameter
          if (outputParam) {
            outputs.add(String(outputParam))
            console.log(`Step ${order} output param: ${outputParam}`)
          }
        })
        
        // 濡傛灉杩樻槸娌℃湁杈撳嚭鍙傛暟锛屽皾璇曞熀浜庢楠ゅ悕绉板拰鍏抽敭瀛楁帹鏂彲鑳界殑鍒楀悕妯″紡
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
      
      // 妯″瀷璇︽儏鍒拌揪鍚庡埛鏂板垪鍒嗙粍
      initializeColumns()
    }
  } catch (error: any) {
    console.warn('鍔犺浇妯″瀷璇︽儏澶辫触锛屼娇鐢ㄥ叧閿瓧鍒嗙粍浣滀负鍥為€€: ', error?.message || error)
    // 鍗充娇澶辫触涔熻鍒濆鍖栧垪
    initializeColumns()
  }
}

// 鑾峰彇鍒楁爣绛?const getColumnLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    'regionCode': '鍦板尯浠ｇ爜',
    'regionName': '鍦板尯鍚嶇О'
  }
  return labelMap[key] || key
}

// 鑾峰彇鍒楀搴?const getColumnWidth = (key: string) => {
  if (key === 'regionCode') return 150
  if (key === 'regionName') return 120
  return 120
}

// 閫変腑鎵€鏈夊垪
const selectAllColumns = () => {
  visibleColumns.value = allColumns.value.map(col => col.prop)
  // 鍚屾鏇存柊涓嬫媺妗嗭細閫変腑鎵€鏈夊垎缁?  autoSelectStepColumns(selectedStepOrder.value)
  console.log('鉁?鍏ㄩ€夛細鏇存柊涓嬫媺妗嗛€変腑', selectedGroupKeys.value)
}

// 鍙栨秷閫変腑鎵€鏈夊垪
const unselectAllColumns = () => {
  // 淇濈暀蹇呴渶鐨勫垪
  visibleColumns.value = allColumns.value
    .filter(col => col.prop === 'regionCode' || col.prop === 'regionName')
    .map(col => col.prop)
  // 鍚屾鏇存柊涓嬫媺妗嗭細鍙€変腑鍩虹淇℃伅鍒嗙粍
  selectedGroupKeys.value = ['base']
  console.log('鉁?鍙栨秷鍏ㄩ€夛細鏇存柊涓嬫媺妗嗛€変腑', selectedGroupKeys.value)
}

// 閲嶇疆鍒楁樉绀?const resetColumns = () => {
  visibleColumns.value = allColumns.value.map(col => col.prop)
  // 鍚屾鏇存柊涓嬫媺妗嗭細閫変腑鎵€鏈夊垎缁?  autoSelectStepColumns(selectedStepOrder.value)
  console.log('鉁?閲嶇疆锛氭洿鏂颁笅鎷夋閫変腑', selectedGroupKeys.value)
}

// 澶勭悊涓嬫媺妗嗛€夋嫨鍙樺寲
const handleGroupSelectionChange = (selectedKeys: string[]) => {
  console.log('閫変腑鐨勫垎缁?', selectedKeys)
  
  // 鑾峰彇鎵€鏈夐€変腑鍒嗙粍鐨勫垪
  const selectedCols = new Set<string>()
  
  // 濮嬬粓鍖呭惈鍩虹鍒?  selectedCols.add('regionCode')
  selectedCols.add('regionName')
  
  selectedKeys.forEach(key => {
    const group = columnGroups.value.find(g => g.key === key)
    if (group) {
      group.columns.forEach(c => selectedCols.add(c.prop))
    }
  })
  
  visibleColumns.value = Array.from(selectedCols)
  console.log('鏇存柊鍚庣殑鍙鍒?', visibleColumns.value.length)
}

// 閫夋嫨/鍙栨秷鏌愪竴鍒嗙粍
const selectGroupColumns = (groupKey: string) => {
  const group = columnGroups.value.find(g => g.key === groupKey)
  if (!group) return
  const current = new Set(visibleColumns.value)
  group.columns.forEach(c => current.add(c.prop))
  visibleColumns.value = Array.from(current)
  
  // 鍚屾鏇存柊涓嬫媺妗嗛€変腑
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
  // 淇濈暀鍩虹鍒?  retain.forEach(r => current.add(r))
  visibleColumns.value = Array.from(current)
  
  // 鍚屾鏇存柊涓嬫媺妗嗛€変腑
  const index = selectedGroupKeys.value.indexOf(groupKey)
  if (index > -1) {
    selectedGroupKeys.value.splice(index, 1)
  }
}

// 瀵煎嚭缁撴灉
const exportResults = () => {
  if (!props.resultData) {
    ElMessage.warning('鏆傛棤鏁版嵁鍙鍑?)
    return
  }

  try {
    // 鏋勫缓CSV鍐呭
    const csvContent = buildCSVContent()
    
    // 鍒涘缓涓嬭浇閾炬帴
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    
    // 鐢熸垚鏂囦欢鍚?    const fileName = `${props.stepInfo?.stepName || '璁＄畻缁撴灉'}_${new Date().toISOString().slice(0, 10)}.csv`
    link.setAttribute('download', fileName)
    
    // 瑙﹀彂涓嬭浇
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('瀵煎嚭鎴愬姛')
    emit('export', props.resultData)
  } catch (error) {
    console.error('瀵煎嚭澶辫触:', error)
    ElMessage.error('瀵煎嚭澶辫触')
  }
}

// 鐢熸垚涓撻鍥?const generateThematicMap = () => {
  if (!props.resultData) {
    ElMessage.warning('鏆傛棤鏁版嵁鍙敓鎴愪笓棰樺浘')
    return
  }

  try {
    // 浠巖esultData涓彁鍙栨暟鎹?    const { tableData, columns, summary } = props.resultData
    
    // 鏋勫缓涓撻鍥炬暟鎹紝鍖归厤ThematicMap.vue鏈熸湜鐨勬暟鎹粨鏋?    const thematicData = {
      id: Date.now(), // 鐢熸垚鍞竴ID
      regionName: props.stepInfo?.stepName || '璇勪及鍖哄煙',
      evaluationTime: new Date().toLocaleString('zh-CN'),
      algorithm: props.stepInfo?.stepCode || 'default',
      totalScore: summary?.鎬诲垎 || summary?.骞冲潎鍒?|| '鏈煡',
      stepInfo: props.stepInfo,
      formula: props.formula,
      resultData: props.resultData,
      tableData: tableData,
      columns: columns,
      summary: summary,
      timestamp: new Date().toISOString(),
      source: 'evaluation_calculation'
    }
    
    console.log('瀛樺偍涓撻鍥炬暟鎹?', thematicData)
    
    // 灏嗘暟鎹瓨鍌ㄥ埌 sessionStorage
    sessionStorage.setItem('thematicMapData', JSON.stringify(thematicData))
    
    // 璺宠浆鍒颁笓棰樺浘椤甸潰
    router.push('/thematic-map')
    
    ElMessage.success('姝ｅ湪璺宠浆鍒颁笓棰樺浘椤甸潰...')
  } catch (error) {
    console.error('鐢熸垚涓撻鍥惧け璐?', error)
    ElMessage.error('鐢熸垚涓撻鍥惧け璐?)
  }
}

// 鏋勫缓CSV鍐呭
const buildCSVContent = (): string => {
  if (!props.resultData) return ''
  
  const lines: string[] = []
  
  // 娣诲姞姝ラ淇℃伅
  if (props.stepInfo) {
    lines.push(`姝ラ鍚嶇О,${props.stepInfo.stepName}`)
    lines.push(`姝ラ鎻忚堪,${props.stepInfo.description}`)
    lines.push('')
  }
  
  // 娣诲姞鍏紡
  if (props.formula) {
    lines.push(`璁＄畻鍏紡,${props.formula}`)
    lines.push('')
  }
  
  if (props.resultData.isDualTable) {
    // 鍙岃〃鏍兼暟鎹鍑?    const { table1Data, table1Columns, table1Summary, table2Data, table2Columns, table2Summary } = props.resultData
    
    // 琛ㄦ牸1
    if (table1Data && table1Columns) {
      lines.push('涓€绾ф寚鏍囨潈閲嶈绠?)
      
      // 琛ㄦ牸1琛ㄥご
      const headers1 = table1Columns.map(col => col.label).join(',')
      lines.push(headers1)
      
      // 琛ㄦ牸1鏁版嵁琛?      table1Data.forEach(row => {
        const values = table1Columns.map(col => {
          const value = row[col.prop]
          return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
        })
        lines.push(values.join(','))
      })
      
      // 琛ㄦ牸1缁熻淇℃伅
      if (table1Summary) {
        lines.push('')
        lines.push('琛ㄦ牸1缁熻淇℃伅')
        Object.entries(table1Summary).forEach(([key, value]) => {
          lines.push(`${key},${value}`)
        })
      }
      
      lines.push('')
      lines.push('')
    }
    
    // 琛ㄦ牸2
    if (table2Data && table2Columns) {
      lines.push('涔￠晣鍑忕伨鑳藉姏鏉冮噸璁＄畻')
      
      // 琛ㄦ牸2琛ㄥご
      const headers2 = table2Columns.map(col => col.label).join(',')
      lines.push(headers2)
      
      // 琛ㄦ牸2鏁版嵁琛?      table2Data.forEach(row => {
        const values = table2Columns.map(col => {
          const value = row[col.prop]
          return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
        })
        lines.push(values.join(','))
      })
      
      // 琛ㄦ牸2缁熻淇℃伅
      if (table2Summary) {
        lines.push('')
        lines.push('琛ㄦ牸2缁熻淇℃伅')
        Object.entries(table2Summary).forEach(([key, value]) => {
          lines.push(`${key},${value}`)
        })
      }
    }
  } else {
    // 鍗曡〃鏍兼暟鎹鍑猴紙鍘熸湁閫昏緫锛?    const { tableData, columns, summary } = props.resultData
    
    if (tableData && columns) {
      // 娣诲姞琛ㄥご
      const headers = columns.map(col => col.label).join(',')
      lines.push(headers)
      
      // 娣诲姞鏁版嵁琛?      tableData.forEach(row => {
        const values = columns.map(col => {
          const value = row[col.prop]
          return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
        })
        lines.push(values.join(','))
      })
      
      // 娣诲姞缁熻淇℃伅
      if (summary) {
        lines.push('')
        lines.push('缁熻淇℃伅')
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

/* 鍝嶅簲寮忚璁?*/
@media (max-width: 768px) {
  .result-dialog {
    .summary-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>



