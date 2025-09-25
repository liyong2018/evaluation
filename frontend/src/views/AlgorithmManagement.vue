<template>
  <div class="algorithm-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>算法管理</h1>
      <p>管理评估算法配置、步骤和公式</p>
    </div>

    <!-- 操作栏 -->
    <el-card class="action-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="12">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索算法名称或描述"
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="12" class="text-right">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建算法
          </el-button>
          <el-button type="success" @click="handleImport">
            <el-icon><Upload /></el-icon>
            导入算法
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 算法列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>算法列表</span>
          <el-button type="text" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading.list"
        :data="filteredAlgorithmList"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configName" label="算法名称" width="200" />
        <el-table-column prop="description" label="描述" min-width="300" />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="info" size="small" @click="handleCopy(row)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button type="success" size="small" @click="handleExport(row)">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 算法详情对话框 -->
    <el-dialog v-model="dialogVisible.detail" title="算法详情" width="80%" :close-on-click-modal="false">
      <div v-if="currentAlgorithmDetail">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="算法名称">{{ currentAlgorithmDetail.algorithm?.configName }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ currentAlgorithmDetail.algorithm?.version }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentAlgorithmDetail.algorithm?.status === 1 ? 'success' : 'danger'">
                {{ currentAlgorithmDetail.algorithm?.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentAlgorithmDetail.algorithm?.createTime }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentAlgorithmDetail.algorithm?.description }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 算法步骤 -->
        <el-card class="detail-card">
          <template #header>
            <span>算法步骤 ({{ currentAlgorithmDetail.stepCount }})</span>
          </template>
          <el-table :data="currentAlgorithmDetail.steps" border>
            <el-table-column prop="stepOrder" label="顺序" width="80" />
            <el-table-column prop="stepName" label="步骤名称" width="200" />
            <el-table-column prop="stepDescription" label="步骤描述" min-width="300" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isEnabled === 1 ? 'success' : 'danger'">
                  {{ row.isEnabled === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 公式配置 -->
        <el-card class="detail-card">
          <template #header>
            <span>公式配置 ({{ currentAlgorithmDetail.formulaCount }})</span>
          </template>
          <el-table :data="currentAlgorithmDetail.formulas" border>
            <el-table-column prop="formulaName" label="公式名称" width="200" />
            <el-table-column prop="formulaType" label="公式类型" width="150" />
            <el-table-column prop="formulaExpression" label="公式表达式" min-width="300" />
            <el-table-column prop="formulaDescription" label="公式描述" min-width="200" />
            <el-table-column label="默认" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault === 1" type="success" size="small">是</el-tag>
                <span v-else>否</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      
      <template #footer>
        <el-button @click="dialogVisible.detail = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑算法对话框 -->
    <el-dialog 
      v-model="dialogVisible.form" 
      :title="formMode === 'create' ? '新建算法' : '编辑算法'" 
      width="70%" 
      :close-on-click-modal="false"
    >
      <el-form
        ref="algorithmFormRef"
        :model="algorithmForm"
        :rules="algorithmRules"
        label-width="120px"
      >
        <!-- 基本信息 -->
        <el-card class="form-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="算法名称" prop="configName">
                <el-input v-model="algorithmForm.configName" placeholder="请输入算法名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="版本" prop="version">
                <el-input v-model="algorithmForm.version" placeholder="请输入版本号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="algorithmForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入算法描述"
            />
          </el-form-item>
        </el-card>

        <!-- 算法步骤 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>算法步骤</span>
              <el-button type="primary" size="small" @click="addStep">
                <el-icon><Plus /></el-icon>
                添加步骤
              </el-button>
            </div>
          </template>
          
          <div v-for="(step, index) in algorithmForm.steps" :key="index" class="step-item">
            <el-card>
              <template #header>
                <div class="step-header">
                  <span>步骤 {{ index + 1 }}</span>
                  <el-button type="danger" size="small" @click="removeStep(index)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item :prop="`steps.${index}.stepName`" label="步骤名称">
                    <el-input v-model="step.stepName" placeholder="请输入步骤名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="启用状态">
                    <el-switch v-model="step.isEnabled" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item :prop="`steps.${index}.stepDescription`" label="步骤描述">
                <el-input
                  v-model="step.stepDescription"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入步骤描述"
                />
              </el-form-item>
              
              <el-form-item :prop="`steps.${index}.stepParams`" label="步骤参数">
                <el-input
                  v-model="step.stepParams"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入步骤参数(JSON格式)"
                />
              </el-form-item>
            </el-card>
          </div>
        </el-card>

        <!-- 公式配置 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>公式配置</span>
              <el-button type="primary" size="small" @click="addFormula">
                <el-icon><Plus /></el-icon>
                添加公式
              </el-button>
            </div>
          </template>
          
          <div v-for="(formula, index) in algorithmForm.formulas" :key="index" class="formula-item">
            <el-card>
              <template #header>
                <div class="formula-header">
                  <span>公式 {{ index + 1 }}</span>
                  <el-button type="danger" size="small" @click="removeFormula(index)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item :prop="`formulas.${index}.formulaName`" label="公式名称">
                    <el-input v-model="formula.formulaName" placeholder="请输入公式名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :prop="`formulas.${index}.formulaType`" label="公式类型">
                    <el-select v-model="formula.formulaType" placeholder="选择公式类型">
                      <el-option label="归一化" value="normalization" />
                      <el-option label="加权" value="weighting" />
                      <el-option label="计算" value="calculation" />
                      <el-option label="分级" value="grading" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="默认公式">
                    <el-switch v-model="formula.isDefault" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item :prop="`formulas.${index}.formulaExpression`" label="公式表达式">
                <el-input
                  v-model="formula.formulaExpression"
                  placeholder="请输入公式表达式"
                  @blur="validateFormula(formula, index)"
                />
              </el-form-item>
              
              <el-form-item :prop="`formulas.${index}.formulaDescription`" label="公式描述">
                <el-input
                  v-model="formula.formulaDescription"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入公式描述"
                />
              </el-form-item>
              
              <el-form-item :prop="`formulas.${index}.variableDefinition`" label="变量定义">
                <el-input
                  v-model="formula.variableDefinition"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入变量定义(JSON格式)"
                />
              </el-form-item>
            </el-card>
          </div>
        </el-card>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible.form = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="loading.save">
          {{ formMode === 'create' ? '创建' : '更新' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 复制算法对话框 -->
    <el-dialog v-model="dialogVisible.copy" title="复制算法" width="400px">
      <el-form :model="copyForm" label-width="100px">
        <el-form-item label="新算法名称">
          <el-input v-model="copyForm.newAlgorithmName" placeholder="请输入新算法名称" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible.copy = false">取消</el-button>
        <el-button type="primary" @click="confirmCopy" :loading="loading.copy">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  Plus,
  Search,
  Upload,
  Refresh,
  View,
  Edit,
  CopyDocument,
  Download,
  Delete
} from '@element-plus/icons-vue'
import { algorithmManagementApi } from '@/api'

// 响应式数据
const algorithmFormRef = ref<FormInstance>()
const algorithmList = ref<any[]>([])
const currentAlgorithmDetail = ref<any>(null)
const searchKeyword = ref('')

const loading = reactive({
  list: false,
  save: false,
  copy: false
})

const dialogVisible = reactive({
  detail: false,
  form: false,
  copy: false
})

const formMode = ref<'create' | 'edit'>('create')

const algorithmForm = reactive({
  id: null as number | null,
  configName: '',
  description: '',
  version: '1.0',
  status: 1,
  steps: [] as any[],
  formulas: [] as any[]
})

const copyForm = reactive({
  sourceAlgorithmId: null as number | null,
  newAlgorithmName: ''
})

const algorithmRules = {
  configName: [{ required: true, message: '请输入算法名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入算法描述', trigger: 'blur' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

// 计算属性
const filteredAlgorithmList = computed(() => {
  if (!searchKeyword.value) return algorithmList.value
  return algorithmList.value.filter(item => 
    item.configName.includes(searchKeyword.value) || 
    item.description.includes(searchKeyword.value)
  )
})

// 获取算法列表
const getAlgorithmList = async () => {
  loading.list = true
  try {
    const response = await algorithmManagementApi.getList()
    if (response.success) {
      algorithmList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取算法列表失败')
    }
  } catch (error) {
    console.error('获取算法列表失败:', error)
    ElMessage.error('获取算法列表失败')
  } finally {
    loading.list = false
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// 刷新列表
const refreshList = () => {
  getAlgorithmList()
}

// 查看详情
const handleDetail = async (row: any) => {
  try {
    const response = await algorithmManagementApi.getDetail(row.id)
    if (response.success) {
      currentAlgorithmDetail.value = response.data
      dialogVisible.detail = true
    } else {
      ElMessage.error(response.message || '获取算法详情失败')
    }
  } catch (error) {
    console.error('获取算法详情失败:', error)
    ElMessage.error('获取算法详情失败')
  }
}

// 新建算法
const handleCreate = () => {
  formMode.value = 'create'
  resetForm()
  dialogVisible.form = true
}

// 编辑算法
const handleEdit = async (row: any) => {
  formMode.value = 'edit'
  try {
    const response = await algorithmManagementApi.getDetail(row.id)
    if (response.success) {
      const detail = response.data
      algorithmForm.id = detail.algorithm.id
      algorithmForm.configName = detail.algorithm.configName
      algorithmForm.description = detail.algorithm.description
      algorithmForm.version = detail.algorithm.version
      algorithmForm.status = detail.algorithm.status
      algorithmForm.steps = detail.steps || []
      algorithmForm.formulas = detail.formulas || []
      
      dialogVisible.form = true
    } else {
      ElMessage.error(response.message || '获取算法详情失败')
    }
  } catch (error) {
    console.error('获取算法详情失败:', error)
    ElMessage.error('获取算法详情失败')
  }
}

// 复制算法
const handleCopy = (row: any) => {
  copyForm.sourceAlgorithmId = row.id
  copyForm.newAlgorithmName = `${row.configName}_副本`
  dialogVisible.copy = true
}

// 确认复制
const confirmCopy = async () => {
  if (!copyForm.newAlgorithmName) {
    ElMessage.error('请输入新算法名称')
    return
  }
  
  loading.copy = true
  try {
    const response = await algorithmManagementApi.copy(
      copyForm.sourceAlgorithmId!,
      copyForm.newAlgorithmName
    )
    
    if (response.success) {
      ElMessage.success('复制算法成功')
      dialogVisible.copy = false
      getAlgorithmList()
    } else {
      ElMessage.error(response.message || '复制算法失败')
    }
  } catch (error) {
    console.error('复制算法失败:', error)
    ElMessage.error('复制算法失败')
  } finally {
    loading.copy = false
  }
}

// 导出算法
const handleExport = async (row: any) => {
  try {
    const response = await algorithmManagementApi.export(row.id)
    if (response.success) {
      // 创建下载链接
      const blob = new Blob([JSON.stringify(response.data, null, 2)], {
        type: 'application/json'
      })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${row.configName}_算法配置.json`
      link.click()
      URL.revokeObjectURL(url)
      
      ElMessage.success('导出算法配置成功')
    } else {
      ElMessage.error(response.message || '导出算法配置失败')
    }
  } catch (error) {
    console.error('导出算法配置失败:', error)
    ElMessage.error('导出算法配置失败')
  }
}

// 删除算法
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除算法 "${row.configName}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await algorithmManagementApi.delete(row.id)
    if (response.success) {
      ElMessage.success('删除算法成功')
      getAlgorithmList()
    } else {
      ElMessage.error(response.message || '删除算法失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除算法失败:', error)
      ElMessage.error('删除算法失败')
    }
  }
}

// 导入算法
const handleImport = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = async (e: any) => {
    const file = e.target.files[0]
    if (!file) return
    
    try {
      const text = await file.text()
      const algorithmData = JSON.parse(text)
      
      const response = await algorithmManagementApi.import(algorithmData)
      if (response.success) {
        ElMessage.success('导入算法配置成功')
        getAlgorithmList()
      } else {
        ElMessage.error(response.message || '导入算法配置失败')
      }
    } catch (error) {
      console.error('导入算法配置失败:', error)
      ElMessage.error('导入算法配置失败')
    }
  }
  input.click()
}

// 添加步骤
const addStep = () => {
  algorithmForm.steps.push({
    stepName: '',
    stepDescription: '',
    stepParams: '',
    stepOrder: algorithmForm.steps.length + 1,
    isEnabled: 1
  })
}

// 删除步骤
const removeStep = (index: number) => {
  algorithmForm.steps.splice(index, 1)
  // 重新排序
  algorithmForm.steps.forEach((step, idx) => {
    step.stepOrder = idx + 1
  })
}

// 添加公式
const addFormula = () => {
  algorithmForm.formulas.push({
    formulaName: '',
    formulaDescription: '',
    formulaExpression: '',
    formulaType: '',
    variableDefinition: '',
    isDefault: 0,
    status: 1,
    creator: 'system'
  })
}

// 删除公式
const removeFormula = (index: number) => {
  algorithmForm.formulas.splice(index, 1)
}

// 验证公式
const validateFormula = async (formula: any, index: number) => {
  if (!formula.formulaExpression) return
  
  try {
    const response = await algorithmManagementApi.validateFormula(formula.formulaExpression)
    if (response.success && response.data) {
      ElMessage.success(`公式 ${index + 1} 验证通过`)
    } else {
      ElMessage.warning(`公式 ${index + 1} 验证失败`)
    }
  } catch (error) {
    console.error('验证公式失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  algorithmForm.id = null
  algorithmForm.configName = ''
  algorithmForm.description = ''
  algorithmForm.version = '1.0'
  algorithmForm.status = 1
  algorithmForm.steps = []
  algorithmForm.formulas = []
  
  algorithmFormRef.value?.resetFields()
}

// 保存算法
const handleSave = async () => {
  if (!algorithmFormRef.value) return
  
  await algorithmFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.save = true
    try {
      const response = formMode.value === 'create' 
        ? await algorithmManagementApi.create(algorithmForm)
        : await algorithmManagementApi.update(algorithmForm)
      
      if (response.success) {
        ElMessage.success(`${formMode.value === 'create' ? '创建' : '更新'}算法成功`)
        dialogVisible.form = false
        getAlgorithmList()
      } else {
        ElMessage.error(response.message || `${formMode.value === 'create' ? '创建' : '更新'}算法失败`)
      }
    } catch (error) {
      console.error(`${formMode.value === 'create' ? '创建' : '更新'}算法失败:`, error)
      ElMessage.error(`${formMode.value === 'create' ? '创建' : '更新'}算法失败`)
    } finally {
      loading.save = false
    }
  })
}

// 组件挂载时获取数据
onMounted(() => {
  getAlgorithmList()
})
</script>

<style scoped>
.algorithm-management {
  max-width: 1400px;
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

.action-card,
.list-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-right {
  text-align: right;
}

.detail-card {
  margin-bottom: 16px;
}

.form-card {
  margin-bottom: 20px;
}

.step-item,
.formula-item {
  margin-bottom: 16px;
}

.step-header,
.formula-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-button + .el-button {
  margin-left: 8px;
}
</style>