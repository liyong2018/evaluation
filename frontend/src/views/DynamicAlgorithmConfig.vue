<template>
  <div class="dynamic-algorithm-config">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>动态算法配置</h1>
      <p>管理动态规则引擎的算法配置和规则映射</p>
    </div>

    <!-- 操作栏 -->
    <el-card class="action-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索配置名称或描述"
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="8">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="8" class="text-right">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建配置
          </el-button>
          <el-button type="success" @click="handlePerformanceStats">
            <el-icon><DataAnalysis /></el-icon>
            性能统计
          </el-button>
          <el-button type="warning" @click="handleClearCache">
            <el-icon><RefreshRight /></el-icon>
            清理缓存
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 配置列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>算法配置列表</span>
          <el-button type="text" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading.list"
        :data="filteredConfigList"
        stripe
        border
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configName" label="配置名称" width="200" />
        <el-table-column prop="description" label="描述" min-width="300" />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否默认" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="warning" size="small">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="400" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click.stop="handleDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="warning" size="small" @click.stop="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="success" size="small" @click.stop="handleExecute(row)">
              <el-icon><VideoPlay /></el-icon>
              执行
            </el-button>
            <el-button type="info" size="small" @click.stop="handleValidate(row)">
              <el-icon><CircleCheck /></el-icon>
              验证
            </el-button>
            <el-button type="primary" size="small" @click.stop="handleCopy(row)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button type="danger" size="small" @click.stop="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 配置详情对话框 -->
    <el-dialog v-model="dialogVisible.detail" title="算法配置详情" width="90%" :close-on-click-modal="false">
      <div v-if="currentConfig">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="配置名称">{{ currentConfig.configName }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ currentConfig.version }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentConfig.status === 1 ? 'success' : 'danger'">
                {{ currentConfig.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="是否默认">
              <el-tag v-if="currentConfig.isDefault === 1" type="warning" size="small">默认配置</el-tag>
              <span v-else>否</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentConfig.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ currentConfig.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentConfig.description }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 规则映射 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>规则映射 ({{ ruleMappings.length }})</span>
              <el-button type="primary" size="small" @click="handleAddRuleMapping">
                <el-icon><Plus /></el-icon>
                添加规则
              </el-button>
            </div>
          </template>
          <el-table :data="ruleMappings" border v-loading="loading.ruleMappings">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="ruleId" label="规则ID" width="100" />
            <el-table-column prop="ruleName" label="规则名称" width="200" />
            <el-table-column prop="ruleType" label="规则类型" width="150" />
            <el-table-column prop="priority" label="优先级" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="warning" size="small" @click="handleEditRuleMapping(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteRuleMapping(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 执行统计 -->
        <el-card class="detail-card" v-if="configStats">
          <template #header>
            <span>执行统计</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="总执行次数" :value="configStats.totalExecutions || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="成功次数" :value="configStats.successExecutions || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="失败次数" :value="configStats.failedExecutions || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="平均执行时间" :value="configStats.avgExecutionTime || 0" suffix="ms" />
            </el-col>
          </el-row>
        </el-card>
      </div>
    </el-dialog>

    <!-- 创建/编辑配置对话框 -->
    <el-dialog 
      v-model="dialogVisible.form" 
      :title="formMode === 'create' ? '新建算法配置' : '编辑算法配置'" 
      width="60%" 
      :close-on-click-modal="false"
    >
      <el-form ref="configFormRef" :model="configForm" :rules="configFormRules" label-width="120px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="configForm.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="configForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入配置描述" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="configForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否默认" prop="isDefault">
          <el-radio-group v-model="configForm.isDefault">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.form = false">取消</el-button>
        <el-button type="primary" :loading="loading.form" @click="handleSubmitForm">
          {{ formMode === 'create' ? '创建' : '更新' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 规则映射表单对话框 -->
    <el-dialog 
      v-model="dialogVisible.ruleMapping" 
      :title="ruleMappingMode === 'create' ? '添加规则映射' : '编辑规则映射'" 
      width="50%" 
      :close-on-click-modal="false"
    >
      <el-form ref="ruleMappingFormRef" :model="ruleMappingForm" :rules="ruleMappingFormRules" label-width="120px">
        <el-form-item label="规则ID" prop="ruleId">
          <el-input-number v-model="ruleMappingForm.ruleId" :min="1" placeholder="请输入规则ID" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleMappingForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleMappingForm.ruleType" placeholder="请选择规则类型">
            <el-option label="计算规则" value="CALCULATION" />
            <el-option label="验证规则" value="VALIDATION" />
            <el-option label="转换规则" value="TRANSFORMATION" />
            <el-option label="聚合规则" value="AGGREGATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="ruleMappingForm.priority" :min="1" :max="100" placeholder="请输入优先级" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ruleMappingForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.ruleMapping = false">取消</el-button>
        <el-button type="primary" :loading="loading.ruleMapping" @click="handleSubmitRuleMappingForm">
          {{ ruleMappingMode === 'create' ? '添加' : '更新' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 执行配置对话框 -->
    <el-dialog v-model="dialogVisible.execute" title="执行算法配置" width="60%" :close-on-click-modal="false">
      <div v-if="executeConfig">
        <h3>配置信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="配置名称">{{ executeConfig.configName }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ executeConfig.version }}</el-descriptions-item>
        </el-descriptions>
        
        <h3 style="margin-top: 20px;">输入数据</h3>
        <el-form label-width="120px">
          <el-form-item label="输入数据">
            <el-input 
              v-model="executeInputData" 
              type="textarea" 
              :rows="6" 
              placeholder="请输入JSON格式的数据"
            />
          </el-form-item>
        </el-form>
        
        <div v-if="executeResult" style="margin-top: 20px;">
          <h3>执行结果</h3>
          <el-alert 
            :type="executeResult.success ? 'success' : 'error'" 
            :title="executeResult.success ? '执行成功' : '执行失败'"
            show-icon
          />
          <pre style="margin-top: 10px; background: #f5f5f5; padding: 10px; border-radius: 4px;">{{ JSON.stringify(executeResult.data, null, 2) }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible.execute = false">关闭</el-button>
        <el-button type="primary" :loading="loading.execute" @click="handleExecuteSubmit">
          执行
        </el-button>
      </template>
    </el-dialog>

    <!-- 性能统计对话框 -->
    <el-dialog v-model="dialogVisible.performance" title="规则性能统计" width="80%" :close-on-click-modal="false">
      <div v-if="performanceStats">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总规则数" :value="performanceStats.totalRules || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="活跃规则数" :value="performanceStats.activeRules || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="总执行次数" :value="performanceStats.totalExecutions || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="平均响应时间" :value="performanceStats.avgResponseTime || 0" suffix="ms" />
          </el-col>
        </el-row>
        
        <el-divider />
        
        <h3>规则执行日志</h3>
        <el-table :data="executionLogs" border v-loading="loading.logs">
          <el-table-column prop="ruleId" label="规则ID" width="100" />
          <el-table-column prop="ruleName" label="规则名称" width="200" />
          <el-table-column prop="executionTime" label="执行时间" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="执行时间" width="180" />
          <el-table-column prop="message" label="消息" min-width="200" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="dialogVisible.performance = false">关闭</el-button>
        <el-button type="primary" @click="loadPerformanceStats">刷新数据</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, View, Edit, Delete, CopyDocument, 
  VideoPlay, CircleCheck, DataAnalysis, RefreshRight 
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

// 响应式数据
const searchKeyword = ref('')
const statusFilter = ref('')
const configList = ref([])
const currentConfig = ref(null)
const ruleMappings = ref([])
const configStats = ref(null)
const performanceStats = ref(null)
const executionLogs = ref([])
const executeConfig = ref(null)
const executeInputData = ref('{\n  "example": "data"\n}')
const executeResult = ref(null)

// 加载状态
const loading = reactive({
  list: false,
  form: false,
  ruleMapping: false,
  ruleMappings: false,
  execute: false,
  logs: false
})

// 对话框显示状态
const dialogVisible = reactive({
  detail: false,
  form: false,
  ruleMapping: false,
  execute: false,
  performance: false
})

// 表单模式
const formMode = ref('create')
const ruleMappingMode = ref('create')

// 表单数据
const configForm = reactive({
  id: null,
  configName: '',
  version: '1.0.0',
  description: '',
  status: 1,
  isDefault: 0
})

const ruleMappingForm = reactive({
  id: null,
  algorithmConfigId: null,
  ruleId: null,
  ruleName: '',
  ruleType: '',
  priority: 1,
  status: 1
})

// 表单引用
const configFormRef = ref<FormInstance>()
const ruleMappingFormRef = ref<FormInstance>()

// 表单验证规则
const configFormRules: FormRules = {
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入配置描述', trigger: 'blur' }
  ]
}

const ruleMappingFormRules: FormRules = {
  ruleId: [
    { required: true, message: '请输入规则ID', trigger: 'blur' }
  ],
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' }
  ]
}

// 计算属性
const filteredConfigList = computed(() => {
  let filtered = configList.value
  
  if (searchKeyword.value) {
    filtered = filtered.filter(item => 
      item.configName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      item.description.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  if (statusFilter.value !== '') {
    filtered = filtered.filter(item => item.status === statusFilter.value)
  }
  
  return filtered
})

// 方法
const loadConfigList = async () => {
  loading.list = true
  try {
    const result = await request.get('/api/algorithm-config')
    if (result.success) {
      configList.value = result.data || []
    } else {
      ElMessage.error(result.message || '获取配置列表失败')
    }
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.list = false
  }
}

const loadRuleMappings = async (configId: number) => {
  loading.ruleMappings = true
  try {
    const result = await request.get(`/api/algorithm-config/${configId}/rule-mappings`)
    if (result.success) {
      ruleMappings.value = result.data || []
    } else {
      ElMessage.error(result.message || '获取规则映射失败')
    }
  } catch (error) {
    console.error('获取规则映射失败:', error)
    ElMessage.error('获取规则映射失败')
  } finally {
    loading.ruleMappings = false
  }
}

const loadConfigStats = async (configId: number) => {
  try {
    const result = await request.get(`/api/algorithm-config/${configId}/stats`)
    if (result.success) {
      configStats.value = result.data
    }
  } catch (error) {
    console.error('获取配置统计失败:', error)
  }
}

const loadPerformanceStats = async () => {
  loading.logs = true
  try {
    const [statsResult, logsResult] = await Promise.all([
      request.get('/api/algorithm-config/performance-stats'),
      request.get('/api/algorithm-config/rule-logs?limit=50')
    ])
    
    if (statsResult.success) {
      performanceStats.value = statsResult.data
    }
    
    if (logsResult.success) {
      executionLogs.value = logsResult.data || []
    }
  } catch (error) {
    console.error('获取性能统计失败:', error)
    ElMessage.error('获取性能统计失败')
  } finally {
    loading.logs = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const refreshList = () => {
  loadConfigList()
}

const handleCreate = () => {
  formMode.value = 'create'
  Object.assign(configForm, {
    id: null,
    configName: '',
    version: '1.0.0',
    description: '',
    status: 1,
    isDefault: 0
  })
  dialogVisible.form = true
}

const handleEdit = (row: any) => {
  formMode.value = 'edit'
  Object.assign(configForm, { ...row })
  dialogVisible.form = true
}

const handleDetail = async (row: any) => {
  currentConfig.value = row
  dialogVisible.detail = true
  await Promise.all([
    loadRuleMappings(row.id),
    loadConfigStats(row.id)
  ])
}

const handleRowClick = (row: any) => {
  handleDetail(row)
}

const handleExecute = (row: any) => {
  executeConfig.value = row
  executeResult.value = null
  dialogVisible.execute = true
}

const handleValidate = async (row: any) => {
  try {
    const result = await request.post(`/api/algorithm-config/${row.id}/validate`)
    
    if (result.success) {
      ElMessage.success('配置验证通过')
    } else {
      ElMessage.error(result.message || '配置验证失败')
    }
  } catch (error) {
    console.error('验证配置失败:', error)
    ElMessage.error('验证配置失败')
  }
}

const handleCopy = async (row: any) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新配置名称', '复制配置', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: `${row.configName}_copy`
    })
    
    const result = await request.post(`/api/algorithm-config/${row.id}/copy?newConfigName=${encodeURIComponent(newName)}`)
    
    if (result.success) {
      ElMessage.success('配置复制成功')
      loadConfigList()
    } else {
      ElMessage.error(result.message || '配置复制失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('复制配置失败:', error)
      ElMessage.error('复制配置失败')
    }
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个配置吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await request.delete(`/api/algorithm-config/${row.id}`)
    
    if (result.success) {
      ElMessage.success('删除成功')
      loadConfigList()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除配置失败:', error)
      ElMessage.error('删除配置失败')
    }
  }
}

const handleSubmitForm = async () => {
  if (!configFormRef.value) return
  
  try {
    await configFormRef.value.validate()
    loading.form = true
    
    const url = formMode.value === 'create' 
      ? '/api/algorithm-config' 
      : `/api/algorithm-config/${configForm.id}`
    
    let result
    if (formMode.value === 'create') {
      result = await request.post(url, configForm)
    } else {
      result = await request.put(url, configForm)
    }
    
    if (result.success) {
      ElMessage.success(formMode.value === 'create' ? '创建成功' : '更新成功')
      dialogVisible.form = false
      loadConfigList()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.form = false
  }
}

const handleAddRuleMapping = () => {
  ruleMappingMode.value = 'create'
  Object.assign(ruleMappingForm, {
    id: null,
    algorithmConfigId: currentConfig.value?.id,
    ruleId: null,
    ruleName: '',
    ruleType: '',
    priority: 1,
    status: 1
  })
  dialogVisible.ruleMapping = true
}

const handleEditRuleMapping = (row: any) => {
  ruleMappingMode.value = 'edit'
  Object.assign(ruleMappingForm, { ...row })
  dialogVisible.ruleMapping = true
}

const handleDeleteRuleMapping = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个规则映射吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await request.delete(`/api/algorithm-config/rule-mapping/${row.id}`)
    
    if (result.success) {
      ElMessage.success('删除成功')
      loadRuleMappings(currentConfig.value.id)
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除规则映射失败:', error)
      ElMessage.error('删除规则映射失败')
    }
  }
}

const handleSubmitRuleMappingForm = async () => {
  if (!ruleMappingFormRef.value) return
  
  try {
    await ruleMappingFormRef.value.validate()
    loading.ruleMapping = true
    
    const url = ruleMappingMode.value === 'create' 
      ? `/api/algorithm-config/${currentConfig.value.id}/rule-mapping`
      : `/api/algorithm-config/rule-mapping/${ruleMappingForm.id}`
    
    const result = ruleMappingMode.value === 'create' 
      ? await request.post(url, ruleMappingForm)
      : await request.put(url, ruleMappingForm)
    
    if (result.success) {
      ElMessage.success(ruleMappingMode.value === 'create' ? '添加成功' : '更新成功')
      dialogVisible.ruleMapping = false
      loadRuleMappings(currentConfig.value.id)
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('提交规则映射失败:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.ruleMapping = false
  }
}

const handleExecuteSubmit = async () => {
  loading.execute = true
  try {
    let inputData = {}
    try {
      inputData = JSON.parse(executeInputData.value)
    } catch (error) {
      ElMessage.error('输入数据格式错误，请输入有效的JSON')
      return
    }
    
    const result = await request.post(`/api/algorithm-config/${executeConfig.value.id}/execute`, inputData)
    executeResult.value = result
    
    if (result.success) {
      ElMessage.success('执行成功')
    } else {
      ElMessage.error(result.message || '执行失败')
    }
  } catch (error) {
    console.error('执行配置失败:', error)
    ElMessage.error('执行配置失败')
    executeResult.value = {
      success: false,
      data: { error: error.message }
    }
  } finally {
    loading.execute = false
  }
}

const handlePerformanceStats = () => {
  dialogVisible.performance = true
  loadPerformanceStats()
}

const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清理规则缓存吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await request.post('/api/algorithm-config/clear-cache')
    
    if (result.success) {
      ElMessage.success('缓存清理成功')
    } else {
      ElMessage.error(result.message || '缓存清理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理缓存失败:', error)
      ElMessage.error('清理缓存失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadConfigList()
})
</script>

<style scoped>
.dynamic-algorithm-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
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

.action-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-card {
  margin-bottom: 20px;
}

.text-right {
  text-align: right;
}

.el-table {
  cursor: pointer;
}

.el-table .el-table__row:hover {
  background-color: #f5f7fa;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>