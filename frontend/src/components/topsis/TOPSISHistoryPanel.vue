<template>
  <div class="topsis-history-panel">
    <div class="history-header">
      <h3>TOPSIS配置历史记录</h3>
      <p>查看和管理TOPSIS配置的历史变更记录</p>
    </div>

    <!-- 筛选工具栏 -->
    <el-card class="filter-toolbar">
      <el-row :gutter="20" align="middle">
        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="filterHistory"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filterType"
            placeholder="操作类型"
            clearable
            @change="filterHistory"
          >
            <el-option label="全部" value="" />
            <el-option label="创建配置" value="CREATE" />
            <el-option label="更新配置" value="UPDATE" />
            <el-option label="删除配置" value="DELETE" />
            <el-option label="测试配置" value="TEST" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索操作人或备注"
            clearable
            @keyup.enter="filterHistory"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <div class="toolbar-actions">
            <el-button type="primary" @click="filterHistory">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilter">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 历史记录列表 -->
    <el-card class="history-list">
      <el-table
        v-loading="loading.history"
        :data="historyList"
        stripe
        border
      >
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="operationType" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)">
              {{ getOperationTypeName(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="description" label="操作描述" min-width="200" />
        <el-table-column prop="configSnapshot" label="配置快照" width="150">
          <template #default="{ row }">
            <el-button
              type="text"
              size="small"
              @click="viewSnapshot(row)"
              :disabled="!row.configSnapshot"
            >
              <el-icon><View /></el-icon>
              查看快照
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewDetails(row)"
            >
              <el-icon><Document /></el-icon>
              详情
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="restoreConfig(row)"
              :disabled="!canRestore(row)"
            >
              <el-icon><RefreshLeft /></el-icon>
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 配置快照对话框 -->
    <el-dialog
      v-model="dialogVisible.snapshot"
      title="配置快照"
      width="70%"
      @close="resetSnapshot"
    >
      <div v-if="selectedSnapshot">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="快照时间">
            {{ selectedSnapshot.snapshotTime }}
          </el-descriptions-item>
          <el-descriptions-item label="配置版本">
            {{ selectedSnapshot.version }}
          </el-descriptions-item>
          <el-descriptions-item label="模型ID">
            {{ selectedSnapshot.modelId }}
          </el-descriptions-item>
          <el-descriptions-item label="步骤ID">
            {{ selectedSnapshot.stepId }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="snapshot-content">
          <h4>指标配置</h4>
          <div class="indicator-list">
            <el-tag
              v-for="(indicator, index) in selectedSnapshot.indicators"
              :key="indicator"
              :type="getIndicatorTagType(index)"
              class="indicator-tag"
            >
              {{ index + 1 }}. {{ indicator }}
            </el-tag>
          </div>

          <h4>QL表达式</h4>
          <el-input
            v-model="selectedSnapshot.qlExpression"
            type="textarea"
            :rows="3"
            readonly
            class="expression-input"
          />

          <h4>配置参数</h4>
          <el-table :data="selectedSnapshot.parameters" stripe border>
            <el-table-column prop="key" label="参数名" width="200" />
            <el-table-column prop="value" label="参数值" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 操作详情对话框 -->
    <el-dialog
      v-model="dialogVisible.details"
      title="操作详情"
      width="60%"
      @close="resetDetails"
    >
      <div v-if="selectedRecord">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="操作ID">
            {{ selectedRecord.id }}
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTag(selectedRecord.operationType)">
              {{ getOperationTypeName(selectedRecord.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作时间">
            {{ selectedRecord.operationTime }}
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ selectedRecord.operator }}
          </el-descriptions-item>
          <el-descriptions-item label="操作描述">
            {{ selectedRecord.description }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ selectedRecord.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="用户代理">
            {{ selectedRecord.userAgent }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 变更详情 -->
        <div v-if="selectedRecord.changes" class="changes-detail">
          <h4>变更详情</h4>
          <el-table :data="selectedRecord.changes" stripe border>
            <el-table-column prop="field" label="字段" width="150" />
            <el-table-column prop="oldValue" label="原值" />
            <el-table-column prop="newValue" label="新值" />
          </el-table>
        </div>

        <!-- 操作结果 -->
        <div v-if="selectedRecord.result" class="operation-result">
          <h4>操作结果</h4>
          <el-alert
            :title="selectedRecord.result.success ? '操作成功' : '操作失败'"
            :type="selectedRecord.result.success ? 'success' : 'error'"
            :description="selectedRecord.result.message"
            show-icon
            :closable="false"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 恢复确认对话框 -->
    <el-dialog
      v-model="dialogVisible.restore"
      title="恢复配置确认"
      width="50%"
    >
      <div v-if="restoreTarget">
        <el-alert
          title="注意"
          type="warning"
          description="恢复操作将覆盖当前配置，请确认是否继续？"
          show-icon
          :closable="false"
        />
        
        <div class="restore-info">
          <h4>将恢复到以下配置：</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="配置时间">
              {{ restoreTarget.operationTime }}
            </el-descriptions-item>
            <el-descriptions-item label="操作人">
              {{ restoreTarget.operator }}
            </el-descriptions-item>
            <el-descriptions-item label="指标数量">
              {{ restoreTarget.configSnapshot?.indicators?.length || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="配置版本">
              {{ restoreTarget.configSnapshot?.version }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="dialogVisible.restore = false">取消</el-button>
        <el-button type="primary" @click="confirmRestore" :loading="loading.restore">
          确认恢复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  View,
  Document,
  RefreshLeft
} from '@element-plus/icons-vue'

// Props
const props = defineProps<{
  modelId: number | null
  stepId: number | null
}>()

// Emits
const emit = defineEmits<{
  configRestored: [config: any]
}>()

// 响应式数据
const historyList = ref<any[]>([])
const dateRange = ref<string[]>([])
const filterType = ref('')
const searchKeyword = ref('')
const selectedSnapshot = ref<any>(null)
const selectedRecord = ref<any>(null)
const restoreTarget = ref<any>(null)

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const loading = reactive({
  history: false,
  restore: false
})

const dialogVisible = reactive({
  snapshot: false,
  details: false,
  restore: false
})

// 监听props变化
watch([() => props.modelId, () => props.stepId], () => {
  if (props.modelId && props.stepId) {
    loadHistory()
  }
}, { immediate: true })

// 方法
const getOperationTypeTag = (type: string) => {
  const typeMap = {
    'CREATE': 'success',
    'UPDATE': 'primary',
    'DELETE': 'danger',
    'TEST': 'info'
  }
  return typeMap[type] || 'info'
}

const getOperationTypeName = (type: string) => {
  const nameMap = {
    'CREATE': '创建配置',
    'UPDATE': '更新配置',
    'DELETE': '删除配置',
    'TEST': '测试配置'
  }
  return nameMap[type] || type
}

const getIndicatorTagType = (index: number) => {
  const types = ['primary', 'success', 'warning', 'danger', 'info']
  return types[index % types.length]
}

const loadHistory = async () => {
  if (!props.modelId || !props.stepId) return
  
  loading.history = true
  try {
    // 这里需要调用后端API获取历史记录
    // const response = await topsisConfigApi.getHistory({
    //   modelId: props.modelId,
    //   stepId: props.stepId,
    //   page: pagination.currentPage,
    //   size: pagination.pageSize,
    //   dateRange: dateRange.value,
    //   operationType: filterType.value,
    //   keyword: searchKeyword.value
    // })
    
    // 模拟历史记录数据
    const mockHistory = [
      {
        id: 1,
        operationType: 'CREATE',
        operationTime: '2024-01-15 10:30:00',
        operator: '张三',
        description: '创建TOPSIS配置，配置3个指标',
        ipAddress: '192.168.1.100',
        userAgent: 'Mozilla/5.0...',
        configSnapshot: {
          snapshotTime: '2024-01-15 10:30:00',
          version: 'v1.0',
          modelId: props.modelId,
          stepId: props.stepId,
          indicators: ['indicator1', 'indicator2', 'indicator3'],
          qlExpression: '@TOPSIS_POSITIVE:indicator1,indicator2,indicator3',
          parameters: [
            { key: 'algorithm_type', value: 'TOPSIS_POSITIVE', description: '算法类型' },
            { key: 'indicator_count', value: '3', description: '指标数量' }
          ]
        },
        result: {
          success: true,
          message: '配置创建成功'
        }
      },
      {
        id: 2,
        operationType: 'UPDATE',
        operationTime: '2024-01-16 14:20:00',
        operator: '李四',
        description: '更新TOPSIS配置，添加2个新指标',
        ipAddress: '192.168.1.101',
        userAgent: 'Mozilla/5.0...',
        configSnapshot: {
          snapshotTime: '2024-01-16 14:20:00',
          version: 'v1.1',
          modelId: props.modelId,
          stepId: props.stepId,
          indicators: ['indicator1', 'indicator2', 'indicator3', 'indicator4', 'indicator5'],
          qlExpression: '@TOPSIS_POSITIVE:indicator1,indicator2,indicator3,indicator4,indicator5',
          parameters: [
            { key: 'algorithm_type', value: 'TOPSIS_POSITIVE', description: '算法类型' },
            { key: 'indicator_count', value: '5', description: '指标数量' }
          ]
        },
        changes: [
          { field: 'indicators', oldValue: '3个指标', newValue: '5个指标' },
          { field: 'ql_expression', oldValue: '@TOPSIS_POSITIVE:indicator1,indicator2,indicator3', newValue: '@TOPSIS_POSITIVE:indicator1,indicator2,indicator3,indicator4,indicator5' }
        ],
        result: {
          success: true,
          message: '配置更新成功'
        }
      },
      {
        id: 3,
        operationType: 'TEST',
        operationTime: '2024-01-17 09:15:00',
        operator: '王五',
        description: '测试TOPSIS配置，验证计算结果',
        ipAddress: '192.168.1.102',
        userAgent: 'Mozilla/5.0...',
        result: {
          success: true,
          message: '配置测试通过'
        }
      }
    ]
    
    historyList.value = mockHistory
    pagination.total = mockHistory.length
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载历史记录失败')
  } finally {
    loading.history = false
  }
}

const filterHistory = () => {
  loadHistory()
}

const resetFilter = () => {
  dateRange.value = []
  filterType.value = ''
  searchKeyword.value = ''
  pagination.currentPage = 1
  loadHistory()
}

const viewSnapshot = (record: any) => {
  selectedSnapshot.value = record.configSnapshot
  dialogVisible.snapshot = true
}

const viewDetails = (record: any) => {
  selectedRecord.value = record
  dialogVisible.details = true
}

const canRestore = (record: any) => {
  return record.operationType !== 'DELETE' && record.configSnapshot
}

const restoreConfig = (record: any) => {
  if (!canRestore(record)) {
    ElMessage.warning('该记录无法恢复')
    return
  }
  
  restoreTarget.value = record
  dialogVisible.restore = true
}

const confirmRestore = async () => {
  if (!restoreTarget.value) return
  
  loading.restore = true
  try {
    // 这里需要调用后端API恢复配置
    // const response = await topsisConfigApi.restoreConfig({
    //   modelId: props.modelId,
    //   stepId: props.stepId,
    //   configSnapshot: restoreTarget.value.configSnapshot
    // })
    
    ElMessage.success('配置恢复成功')
    dialogVisible.restore = false
    emit('configRestored', restoreTarget.value.configSnapshot)
    loadHistory() // 重新加载历史记录
  } catch (error) {
    console.error('恢复配置失败:', error)
    ElMessage.error('恢复配置失败')
  } finally {
    loading.restore = false
  }
}

const resetSnapshot = () => {
  selectedSnapshot.value = null
}

const resetDetails = () => {
  selectedRecord.value = null
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadHistory()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  loadHistory()
}

// 组件挂载时加载数据
onMounted(() => {
  if (props.modelId && props.stepId) {
    loadHistory()
  }
})
</script>

<style scoped>
.topsis-history-panel {
  padding: 16px;
}

.history-header {
  margin-bottom: 20px;
}

.history-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.history-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-toolbar,
.history-list {
  margin-bottom: 20px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.snapshot-content {
  margin-top: 20px;
}

.snapshot-content h4 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.indicator-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.indicator-tag {
  margin: 2px;
}

.expression-input {
  font-family: 'Courier New', monospace;
  margin-bottom: 20px;
}

.changes-detail,
.operation-result {
  margin-top: 20px;
}

.changes-detail h4,
.operation-result h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.restore-info {
  margin-top: 20px;
}

.restore-info h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}
</style>