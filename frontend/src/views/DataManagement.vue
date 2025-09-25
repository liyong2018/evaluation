<template>
  <div class="data-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>数据管理</h1>
      <p>调查数据的导入、查询、编辑和管理</p>
    </div>

    <!-- 操作工具栏 -->
    <el-card class="toolbar-card">
      <el-row :gutter="20" justify="space-between">
        <el-col :span="16">
          <el-row :gutter="12">
            <el-col :span="8">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索地区名称或代码"
                clearable
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-select v-model="searchForm.region" placeholder="选择地区" clearable>
                <el-option label="全部地区" value="" />
                <el-option
                  v-for="region in regionOptions"
                  :key="region"
                  :label="region"
                  :value="region"
                />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="8">
          <div class="toolbar-actions">
            <el-button type="success" @click="showAddDialog">
              <el-icon><Plus /></el-icon>
              新增数据
            </el-button>
            <el-button type="warning" @click="showImportDialog">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button type="info" @click="exportData">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading.table"
        :data="tableData"
        stripe
        border
        style="width: 100%"
        :height="500"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="regionCode" label="地区代码" width="120" />
        <el-table-column prop="province" label="省份" width="100" />
        <el-table-column prop="city" label="市" width="100" />
        <el-table-column prop="county" label="县" width="100" />
        <el-table-column prop="township" label="乡镇(街道)" width="120" />
        <el-table-column prop="population" label="人口数量" width="100" />
        <el-table-column prop="managementStaff" label="管理人员" width="100" />
        <el-table-column prop="riskAssessment" label="风险评估" width="100" />
        <el-table-column prop="fundingAmount" label="资金投入(万元)" width="120" />
        <el-table-column prop="materialValue" label="物资价值(万元)" width="120" />
        <el-table-column prop="hospitalBeds" label="医院床位" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible.form"
      :title="isEdit ? '编辑数据' : '新增数据'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="地区代码" prop="regionCode">
              <el-input v-model="formData.regionCode" placeholder="请输入地区代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="省份" prop="province">
              <el-input v-model="formData.province" placeholder="请输入省份" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="市" prop="city">
              <el-input v-model="formData.city" placeholder="请输入市" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="县" prop="county">
              <el-input v-model="formData.county" placeholder="请输入县" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="乡镇(街道)" prop="township">
              <el-input v-model="formData.township" placeholder="请输入乡镇(街道)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人口数量" prop="population">
              <el-input-number
                v-model="formData.population"
                :min="0"
                placeholder="请输入人口数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="管理人员" prop="managementStaff">
              <el-input-number
                v-model="formData.managementStaff"
                :min="0"
                placeholder="请输入管理人员数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险评估" prop="riskAssessment">
              <el-select v-model="formData.riskAssessment" placeholder="请选择">
                <el-option label="是" value="是" />
                <el-option label="否" value="否" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资金投入(万元)" prop="fundingAmount">
              <el-input-number
                v-model="formData.fundingAmount"
                :min="0"
                :precision="2"
                placeholder="请输入资金投入"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物资价值(万元)" prop="materialValue">
              <el-input-number
                v-model="formData.materialValue"
                :min="0"
                :precision="2"
                placeholder="请输入物资价值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="医院床位" prop="hospitalBeds">
              <el-input-number
                v-model="formData.hospitalBeds"
                :min="0"
                placeholder="请输入医院床位数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.form = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading.submit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="dialogVisible.import" title="批量导入" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        accept=".xlsx,.xls,.csv"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 xlsx/xls/csv 格式文件，文件大小不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="dialogVisible.import = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="loading.import">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  Search,
  Plus,
  Upload,
  Download,
  Edit,
  Delete,
  UploadFilled
} from '@element-plus/icons-vue'
import { surveyDataApi } from '@/api'

// 修复ResizeObserver错误
const originalError = console.error
console.error = (...args: any[]) => {
  if (args[0]?.includes?.('ResizeObserver loop completed with undelivered notifications')) {
    return
  }
  originalError(...args)
}

// 响应式数据
const tableData = ref<any[]>([])
const selectedRows = ref<any[]>([])
const regionOptions = ref<string[]>(['华北地区', '华东地区', '华南地区', '华中地区', '西南地区', '西北地区', '东北地区'])

const searchForm = reactive({
  keyword: '',
  region: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const loading = reactive({
  table: false,
  submit: false,
  import: false
})

const dialogVisible = reactive({
  form: false,
  import: false
})

const isEdit = ref(false)
const formRef = ref<FormInstance>()
const uploadRef = ref()
const uploadFile = ref<File | null>(null)

const formData = reactive({
  id: null,
  regionCode: '',
  province: '',
  city: '',
  county: '',
  township: '',
  population: null,
  managementStaff: null,
  riskAssessment: '',
  fundingAmount: null,
  materialValue: null,
  hospitalBeds: null
})

const formRules = {
  regionCode: [{ required: true, message: '请输入地区代码', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入市', trigger: 'blur' }],
  county: [{ required: true, message: '请输入县', trigger: 'blur' }],
  township: [{ required: true, message: '请输入乡镇(街道)', trigger: 'blur' }],
  population: [{ required: true, message: '请输入人口数量', trigger: 'blur' }]
}

// 获取数据列表
const getDataList = async () => {
  loading.table = true
  try {
    const response = await surveyDataApi.getAll()
    if (response.success) {
      tableData.value = response.data || []
      pagination.total = tableData.value.length
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.table = false
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchForm.keyword && !searchForm.region) {
    getDataList()
    return
  }
  
  loading.table = true
  try {
    let response
    if (searchForm.keyword) {
      response = await surveyDataApi.search(searchForm.keyword)
    } else if (searchForm.region) {
      response = await surveyDataApi.getByRegion(searchForm.region)
    }
    
    if (response?.success) {
      tableData.value = response.data || []
      pagination.total = tableData.value.length
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.table = false
  }
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.form = true
}

// 显示编辑对话框
const showEditDialog = (row: any) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.form = true
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    regionCode: '',
    province: '',
    city: '',
    county: '',
    township: '',
    population: null,
    managementStaff: null,
    riskAssessment: '',
    fundingAmount: null,
    materialValue: null,
    hospitalBeds: null
  })
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.submit = true
    try {
      let response
      if (isEdit.value) {
        response = await surveyDataApi.update(formData)
      } else {
        response = await surveyDataApi.create(formData)
      }
      
      if (response.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.form = false
        getDataList()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    } finally {
      loading.submit = false
    }
  })
}

// 删除数据
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await surveyDataApi.delete(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      getDataList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  getDataList()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  getDataList()
}

// 显示导入对话框
const showImportDialog = () => {
  dialogVisible.import = true
  uploadFile.value = null
}

// 文件选择
const handleFileChange = (file: any) => {
  uploadFile.value = file.raw
}

// 上传前验证
const beforeUpload = (file: File) => {
  const isValidType = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 
                      'application/vnd.ms-excel', 
                      'text/csv'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isValidType) {
    ElMessage.error('只支持 xlsx/xls/csv 格式文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

// 导入数据
const handleImport = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  loading.import = true
  try {
    const response = await surveyDataApi.importData(uploadFile.value)
    if (response.success) {
      ElMessage.success('导入成功')
      dialogVisible.import = false
      getDataList()
    } else {
      ElMessage.error(response.message || '导入失败')
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  } finally {
    loading.import = false
  }
}

// 导出数据
const exportData = async () => {
  try {
    const response = await surveyDataApi.exportData()
    if (response.success) {
      ElMessage.success('导出成功')
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getDataList()
})
</script>

<style scoped>
.data-management {
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

.toolbar-card {
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.table-card {
  min-height: 600px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.el-upload {
  width: 100%;
}

.el-upload__tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
</style>