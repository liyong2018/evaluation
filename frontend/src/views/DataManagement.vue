<template>
  <div class="data-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>数据管理</h1>
      <p>调查数据的导入、查询、编辑和管理</p>
    </div>

    <!-- 左右布局容器 -->
    <div class="layout-container">
      <!-- 左侧：组织机构树 -->
      <el-card class="org-tree-panel">
        <template #header>
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 12px;">
              <span>组织机构</span>
              <el-select
                v-model="searchForm.year"
                placeholder="选择年份"
                clearable
                size="small"
                style="width: 120px;"
                @change="handleSearch"
              >
                <el-option
                  v-for="year in yearOptions"
                  :key="year"
                  :label="year + '年'"
                  :value="year"
                />
              </el-select>
            </div>
            <el-button type="primary" size="small" @click="refreshOrganizations">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </template>
        <el-tree
          :key="orgTreeRenderKey"
          ref="orgTreeRef"
          v-loading="loading.organizations"
          :data="organizationList"
          :props="{ label: 'name', children: 'children' }"
          node-key="code"
          highlight-current
          :expand-on-click-node="false"
          :default-expanded-keys="defaultExpandedKeys"
          @node-click="handleOrgNodeClick"
        >
          <template #default="{ data }">
            <div class="org-tree-node">
              <span class="org-name">{{ data.name }}</span>
              <span class="org-code">{{ data.code }}</span>
            </div>
          </template>
        </el-tree>
      </el-card>

      <!-- 右侧：数据管理面板 -->
      <div class="data-panel">
        <!-- 当前选中组织机构信息 -->
        <el-card v-if="selectedOrg" class="selected-org-info">
          <div class="org-info-content">
            <el-tag type="primary" size="large">{{ selectedOrg.name }}</el-tag>
            <span class="org-info-code">组织机构代码: {{ selectedOrg.code }}</span>
          </div>
        </el-card>

        <!-- 数据类型切换 -->
        <el-card class="type-switch-card">
          <el-radio-group v-model="dataType" size="large" @change="handleDataTypeChange">
            <el-radio-button label="medical">医疗卫生机构</el-radio-button>
            <el-radio-button label="community">社区数据</el-radio-button>
            <el-radio-button label="township">乡镇数据</el-radio-button>
          </el-radio-group>
          <el-tag
            :type="dataType === 'township' ? 'success' : dataType === 'community' ? 'warning' : 'primary'"
            style="margin-left: 20px"
          >
            当前: {{ getCurrentDataTypeName() }}
          </el-tag>
        </el-card>

        <!-- 操作工具栏 -->
        <el-card class="toolbar-card">
          <el-row :gutter="20" justify="space-between">
            <el-col :span="16">
              <el-input
                v-model="searchForm.keyword"
                :placeholder="getSearchPlaceholder()"
                clearable
                @keyup.enter="handleSearch"
                style="width: 200px; margin-right: 12px;"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="8">
              <div class="toolbar-actions">
                <el-button
                  type="danger"
                  :disabled="selectedRows.length === 0"
                  :loading="loading.batchDelete"
                  @click="handleBatchDelete"
                >
                  <el-icon><Delete /></el-icon>
                  批量删除 ({{ selectedRows.length }})
                </el-button>
                <el-button v-if="dataType !== 'medical'" type="success" @click="showAddDialog">
                  <el-icon><Plus /></el-icon>
                  新增数据
                </el-button>
                <el-button type="warning" @click="showImportDialog">
                  <el-icon><Upload /></el-icon>
                  批量导入
                </el-button>
                <el-button v-if="dataType === 'medical'" type="primary" @click="downloadTemplate">
                  <el-icon><Download /></el-icon>
                  下载模板
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
        <el-table-column label="区域名称" width="160">
          <template #default="{ row }">
            {{ getRegionName(row) }}
          </template>
        </el-table-column>
        <!-- 省份 -->
        <el-table-column label="省份" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.province : row.provinceName }}
          </template>
        </el-table-column>
        <!-- 市 -->
        <el-table-column label="市" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.city : row.cityName }}
          </template>
        </el-table-column>
        <!-- 县 -->
        <el-table-column label="县" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.county : row.countyName }}
          </template>
        </el-table-column>
        <!-- 乡镇(街道) -->
        <el-table-column label="乡镇(街道)" width="120">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.township : row.townshipName }}
          </template>
        </el-table-column>
        <!-- 医疗卫生机构名称 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="institutionName" label="医疗机构名称" width="200" show-overflow-tooltip />
        <!-- 统一社会信用代码 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="unifiedSocialCreditCode" label="统一社会信用代码" width="150" />
        <!-- 医疗机构地址 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="institutionAddress" label="机构地址" width="250" show-overflow-tooltip />
        <!-- 医疗机构类型 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="institutionTypeLarge" label="机构类型(大类)" width="120" />
        <!-- 医院等级 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="hospitalLevel" label="医院等级" width="100" />
        <!-- 实有床位数 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="actualHospitalBeds" label="实有床位数" width="100" />

        <!-- 社区名称 (仅社区数据) -->
        <el-table-column v-if="dataType === 'community'" prop="communityName" label="社区(行政村)" width="140" />
        <!-- 人口数量 -->
        <el-table-column label="人口数量" width="100" v-if="dataType !== 'medical'">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.population : row.residentPopulation }}
          </template>
        </el-table-column>
        <!-- 在岗职工人数 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="totalStaff" label="在岗职工人数" width="100" />
        <!-- 管理人员 (仅乡镇数据) -->
        <el-table-column v-if="dataType === 'township'" prop="managementStaff" label="管理人员" width="100" />
        <!-- 风险评估 (仅乡镇数据) -->
        <el-table-column v-if="dataType === 'township'" prop="riskAssessment" label="风险评估" width="100" />
        <!-- 应急预案 (仅社区数据) -->
        <el-table-column v-if="dataType === 'community'" prop="hasEmergencyPlan" label="应急预案" width="100" />
        <!-- 弱势人群清单 (仅社区数据) -->
        <el-table-column v-if="dataType === 'community'" prop="hasVulnerableGroupsList" label="弱势人群清单" width="120" />
        <!-- 资金投入 -->
        <el-table-column label="资金投入(万元)" width="120">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.fundingAmount : row.lastYearFundingAmount }}
          </template>
        </el-table-column>
        <!-- 物资价值 -->
        <el-table-column label="物资价值(万元)" width="120">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.materialValue : row.materialsEquipmentValue }}
          </template>
        </el-table-column>
        <!-- 医疗设施 -->
        <el-table-column label="医疗设施" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.hospitalBeds : row.medicalServiceCount }}
          </template>
        </el-table-column>
        <!-- 消防员 (仅乡镇数据) -->
        <el-table-column v-if="dataType === 'township'" prop="firefighters" label="消防员数量" width="100" />
        <!-- 志愿者 -->
        <el-table-column label="志愿者人数" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.volunteers : row.registeredVolunteerCount }}
          </template>
        </el-table-column>
        <!-- 民兵预备役 -->
        <el-table-column label="民兵预备役" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.militiaReserve : row.militiaReserveCount }}
          </template>
        </el-table-column>
        <!-- 培训参与人次 -->
        <el-table-column label="培训参与人次" width="120">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.trainingParticipants : row.lastYearTrainingParticipants }}
          </template>
        </el-table-column>
        <!-- 演练参与人次 (仅社区数据) -->
        <el-table-column v-if="dataType === 'community'" prop="lastYearDrillParticipants" label="演练参与人次" width="120" />
        <!-- 避难场所容量 -->
        <el-table-column label="避难场所容量" width="120">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.shelterCapacity : row.emergencyShelterCapacity }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="dataType !== 'medical'" type="primary" size="small" @click="showEditDialog(row)">
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
      </div>
    </div>

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
          <el-col :span="12">
            <el-form-item label="消防员数量" prop="firefighters">
              <el-input-number
                v-model="formData.firefighters"
                :min="0"
                placeholder="请输入消防员数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="志愿者人数" prop="volunteers">
              <el-input-number
                v-model="formData.volunteers"
                :min="0"
                placeholder="请输入志愿者人数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民兵预备役" prop="militiaReserve">
              <el-input-number
                v-model="formData.militiaReserve"
                :min="0"
                placeholder="请输入民兵预备役人数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培训参与人次" prop="trainingParticipants">
              <el-input-number
                v-model="formData.trainingParticipants"
                :min="0"
                placeholder="请输入培训参与人次"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="避难场所容量" prop="shelterCapacity">
              <el-input-number
                v-model="formData.shelterCapacity"
                :min="0"
                placeholder="请输入避难场所容量"
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
      <el-form label-width="100px">
        <el-form-item label="数据年份">
          <span>{{ searchForm.year }}年</span>
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept=".xlsx,.xls,.csv"
            drag
            style="width: 100%"
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
        </el-form-item>
      </el-form>
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
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  Search,
  Plus,
  Upload,
  Download,
  Edit,
  Delete,
  UploadFilled,
  Refresh
} from '@element-plus/icons-vue'
import { surveyDataApi, communityCapacityApi, organizationApi, medicalInstitutionApi } from '@/api'

// 修复ResizeObserver错误
const originalError = console.error
console.error = (...args: any[]) => {
  if (args[0]?.includes?.('ResizeObserver loop completed with undelivered notifications')) {
    return
  }
  originalError(...args)
}

// 响应式数据
const dataType = ref<'township' | 'community' | 'medical'>('medical')  // 数据类型：township(乡镇)、community(社区) 或 medical(医疗机构)
const tableData = ref<any[]>([])
const selectedRows = ref<any[]>([])
// 代码->名称映射表（一次性从后端加载）
const regionNameMap = ref<Record<string, string>>({})
// 下拉选项（从后端获取），包含代码与名称
const regionSelectOptions = ref<Array<{ code: string; name: string }>>([])
// 组织机构相关
const selectedOrg = ref<any>(null) // 当前选中的组织机构
const organizationList = ref<any[]>([]) // 组织机构树列表
const orgTreeRef = ref() // 组织机构树引用

const searchForm = reactive({
  keyword: '',
  year: new Date().getFullYear() as number | null,
  selectedRegion: null as null | { code: string; name: string }
})

const orgTreeRenderKey = computed(() => `orgTree-${searchForm.year ?? 'all'}`)

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const loading = reactive({
  table: false,
  submit: false,
  import: false,
  organizations: false,
  batchDelete: false
})

const dialogVisible = reactive({
  form: false,
  import: false
})

const isEdit = ref(false)
const formRef = ref<FormInstance>()
const uploadRef = ref()
const uploadFile = ref<File | null>(null)

// 年份选项（用于主筛选）
const yearOptions = ref<number[]>([])

// 生成年份选项（从2020年到当前年份）
const generateYearOptions = () => {
  const currentYear = new Date().getFullYear()
  const startYear = 2020
  const years: number[] = []
  for (let year = currentYear; year >= startYear; year--) {
    years.push(year)
  }
  yearOptions.value = years
}

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
  hospitalBeds: null,
  firefighters: null,
  volunteers: null,
  militiaReserve: null,
  trainingParticipants: null,
  shelterCapacity: null
})

const formRules = {
  regionCode: [{ required: true, message: '请输入地区代码', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入市', trigger: 'blur' }],
  county: [{ required: true, message: '请输入县', trigger: 'blur' }],
  township: [{ required: true, message: '请输入乡镇(街道)', trigger: 'blur' }],
  population: [{ required: true, message: '请输入人口数量', trigger: 'blur' }]
}

// 加载地区名称映射 (已废弃 - region表已删除)
// 现在区域名称直接从数据表中获取 (township, communityName等字段)
const loadRegionNameMap = async () => {
  try {
    console.info('[DataManagement] loadRegionNameMap -> skipped (region table removed)')
    // 不再从region表加载，直接使用数据中的名称字段
    regionNameMap.value = {}
    regionSelectOptions.value = []
  } catch (e) {
    console.warn('加载地区名称映射失败:', e)
  }
}

// 获取组织机构列表（树形结构）
const defaultExpandedKeys = ref<string[]>([])

// 收集需要展开的节点（展开到3级）
const collectExpandedKeys = (nodes: any[], level: number = 1, keys: string[] = []) => {
  if (!nodes || nodes.length === 0) return keys
  for (const node of nodes) {
    // 展开到3级（县级），不展开4级（乡镇/街道）和5级（村/社区）
    if (level < 3) {
      keys.push(node.code)
    }
    if (node.children && node.children.length > 0) {
      collectExpandedKeys(node.children, level + 1, keys)
    }
  }
  return keys
}

const findOrgNodeByCode = (tree: any[], code: any): any | null => {
  for (const node of tree || []) {
    if (String(node?.code) === String(code)) return node
    if (node?.children?.length) {
      const found = findOrgNodeByCode(node.children, code)
      if (found) return found
    }
  }
  return null
}

const getOrganizationList = async () => {
  loading.organizations = true
  try {
    // 传递当前选择的年份参数
    const response = await organizationApi.getTree({ year: searchForm.year || undefined })
    if (response.success && response.data) {
      organizationList.value = response.data || []
      // 收集需要展开的节点key
      defaultExpandedKeys.value = collectExpandedKeys(organizationList.value)
      console.log('组织机构树形数据 (年份:', searchForm.year, '):', organizationList.value)
      if (selectedOrg.value) {
        const exists = findOrgNodeByCode(organizationList.value, selectedOrg.value.code)
        if (!exists) {
          selectedOrg.value = null
        }
      }
      await nextTick()
      orgTreeRef.value?.setCurrentKey(selectedOrg.value?.code ?? null)
    }
  } catch (error) {
    console.error('获取组织机构列表失败:', error)
    ElMessage.error('获取组织机构列表失败')
  } finally {
    loading.organizations = false
  }
}

// 刷新组织机构树
const refreshOrganizations = () => {
  getOrganizationList()
}

// 处理组织机构节点点击
const handleOrgNodeClick = (data: any) => {
  console.log('选中组织机构:', data)
  selectedOrg.value = data
  // 清空搜索关键字，保留年份过滤
  searchForm.keyword = ''
  getDataList()
}

// 获取当前数据类型名称
const getCurrentDataTypeName = () => {
  switch (dataType.value) {
    case 'township':
      return '乡镇数据表'
    case 'community':
      return '社区数据表'
    case 'medical':
      return '医疗卫生机构表'
    default:
      return '未知数据类型'
  }
}

// 获取搜索框占位符
const getSearchPlaceholder = () => {
  switch (dataType.value) {
    case 'township':
      return '搜索地区名称或代码'
    case 'community':
      return '搜索社区名称'
    case 'medical':
      return '搜索医疗机构名称'
    default:
      return '请输入搜索关键词'
  }
}

// 根据代码获取地区名称（带鲁棒回退）
const getRegionName = (row?: any) => {
  const code = row?.regionCode
  if (!code && !row) return '-'
  const key = (code != null ? String(code) : '').trim()
  const mapped = key ? regionNameMap.value[key] : ''
  // 回退顺序：映射 -> 行内字段 -> 原始代码 -> '-'
  if (mapped) return mapped
  if (dataType.value === 'township') {
    return row?.township || row?.county || row?.city || row?.province || key || '-'
  } else if (dataType.value === 'community') {
    return row?.communityName || row?.townshipName || row?.countyName || row?.cityName || row?.provinceName || key || '-'
  } else if (dataType.value === 'medical') {
    return row?.institutionName || key || '-'
  }
  return key || '-'
}

// 数据类型切换处理
const handleDataTypeChange = (newType: 'township' | 'community' | 'medical') => {
  console.info('[DataManagement] 切换数据类型:', newType)
  dataType.value = newType
  // 清空搜索条件和表格数据（但保留用户选择的年份）
  searchForm.keyword = ''
  searchForm.selectedRegion = null
  // searchForm.year 保持用户选择的值，不重置
  tableData.value = []
  regionSelectOptions.value = []
  // 重新加载数据
  getDataList()
}

// 获取数据列表
const getDataList = async () => {
  // 如果选择了年份，但该年份没有组织机构，不查询数据
  if (searchForm.year && (!organizationList.value || organizationList.value.length === 0)) {
    console.log('当前年份没有组织机构，清空数据列表')
    tableData.value = []
    pagination.total = 0
    loading.table = false
    return
  }

  loading.table = true
  try {
    let response
    let allData: any[] = []

    if (dataType.value === 'township') {
      // 乡镇数据 - 直接返回数组
      response = await surveyDataApi.getAll()
      if (response.success) {
        allData = response.data || []
      }
    } else if (dataType.value === 'community') {
      // 社区数据 - 使用 search API 支持年份过滤
      const searchParams: any = {}
      if (searchForm.year) searchParams.year = searchForm.year

      response = await communityCapacityApi.search(searchParams)
      if (response.success) {
        allData = response.data || []
      }
    } else if (dataType.value === 'medical') {
      // 医疗卫生机构数据 - 获取指定年份的数据
      const year = searchForm.year || new Date().getFullYear()
      response = await medicalInstitutionApi.getList(year)
      if (response.success) {
        allData = response.data || []
      }
    }

    // 如果选中了组织机构，过滤数据
    if (selectedOrg.value && allData.length > 0) {
      const orgCode = selectedOrg.value.code
      allData = allData.filter((row: any) => {
        // 根据数据类型过滤
        if (dataType.value === 'township') {
          // 乡镇数据：匹配省、市、县、乡镇代码
          return (
            String(row.regionCode || '').startsWith(orgCode) ||
            String(row.province || '').includes(selectedOrg.value.name) ||
            String(row.city || '').includes(selectedOrg.value.name) ||
            String(row.county || '').includes(selectedOrg.value.name) ||
            String(row.township || '').includes(selectedOrg.value.name)
          )
        } else if (dataType.value === 'community') {
          // 社区数据：匹配省、市、县、乡镇、社区名称
          return (
            String(row.regionCode || '').startsWith(orgCode) ||
            String(row.provinceName || '').includes(selectedOrg.value.name) ||
            String(row.cityName || '').includes(selectedOrg.value.name) ||
            String(row.countyName || '').includes(selectedOrg.value.name) ||
            String(row.townshipName || '').includes(selectedOrg.value.name) ||
            String(row.communityName || '').includes(selectedOrg.value.name)
          )
        } else if (dataType.value === 'medical') {
          // 医疗卫生机构数据：按机构地址匹配
          const address = String(row.institutionAddress || '')
          // 匹配逻辑：地址包含选中组织机构的名称
          return address.includes(selectedOrg.value.name)
        }
        return true
      })
    }

    // 对于乡镇数据，应用年份过滤（如果设置了年份）
    if (dataType.value === 'township' && searchForm.year && allData.length > 0) {
      allData = allData.filter((row: any) => row.year === searchForm.year)
    }

    if (response.success) {
      tableData.value = allData
      pagination.total = tableData.value.length
      // 如果下拉选项还未加载成功，基于现有表格构建一个临时选项集
      if (!regionSelectOptions.value?.length && tableData.value?.length && dataType.value !== 'medical') {
        const uniq = new Map<string, string>()
        for (const row of tableData.value) {
          const code = String(row.regionCode || '').trim()
          if (!code) continue
          let name = ''
          if (dataType.value === 'township') {
            name = row.township || row.county || row.city || row.province || code
          } else if (dataType.value === 'community') {
            name = row.communityName || row.townshipName || row.countyName || row.cityName || row.provinceName || code
          }
          if (!uniq.has(code)) uniq.set(code, name)
        }
        regionSelectOptions.value = Array.from(uniq.entries()).map(([code, name]) => ({ code, name }))
      }
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
  // 当年份改变时，刷新组织机构树
  await getOrganizationList()

  // 如果选择了年份，但该年份没有组织机构，清空数据列表
  if (searchForm.year && (!organizationList.value || organizationList.value.length === 0)) {
    console.log('当前年份没有组织机构，清空数据列表')
    tableData.value = []
    pagination.total = 0
    return
  }

  if (!searchForm.keyword && !searchForm.selectedRegion && !searchForm.year) {
    getDataList()
    return
  }

  loading.table = true
  try {
    let response
    if (dataType.value === 'township') {
      // 乡镇数据搜索
      if (searchForm.keyword) {
        response = await surveyDataApi.search(searchForm.keyword)
      } else if (searchForm.selectedRegion) {
        response = await surveyDataApi.getByRegion(searchForm.selectedRegion.name)
      } else {
        // 如果只有年份过滤，使用 getAll 然后在客户端过滤
        response = await surveyDataApi.getAll()
        if (response.success && searchForm.year) {
          tableData.value = (response.data || []).filter((item: any) => item.year === searchForm.year)
          pagination.total = tableData.value.length
          return
        }
      }
    } else if (dataType.value === 'community') {
      // 社区数据搜索
      const searchParams: any = {}
      if (searchForm.keyword) searchParams.keyword = searchForm.keyword
      if (searchForm.selectedRegion) searchParams.communityName = searchForm.selectedRegion.name
      if (searchForm.year) searchParams.year = searchForm.year

      response = await communityCapacityApi.search(searchParams)
    } else if (dataType.value === 'medical') {
      // 医疗卫生机构数据搜索
      if (searchForm.keyword) {
        response = await medicalInstitutionApi.search(searchForm.keyword)
      } else {
        // 如果只有年份过滤，使用 getList
        const year = searchForm.year || new Date().getFullYear()
        response = await medicalInstitutionApi.getList(year)
      }
    }

    if (response?.success) {
      let allData = response.data || []

      // 如果选中了组织机构，过滤数据
      if (selectedOrg.value && allData.length > 0) {
        const orgCode = selectedOrg.value.code
        allData = allData.filter((row: any) => {
          // 根据数据类型过滤
          if (dataType.value === 'township') {
            // 乡镇数据：匹配省、市、县、乡镇代码
            return (
              String(row.regionCode || '').startsWith(orgCode) ||
              String(row.province || '').includes(selectedOrg.value.name) ||
              String(row.city || '').includes(selectedOrg.value.name) ||
              String(row.county || '').includes(selectedOrg.value.name) ||
              String(row.township || '').includes(selectedOrg.value.name)
            )
          } else if (dataType.value === 'community') {
            // 社区数据：匹配省、市、县、乡镇、社区名称
            return (
              String(row.regionCode || '').startsWith(orgCode) ||
              String(row.provinceName || '').includes(selectedOrg.value.name) ||
              String(row.cityName || '').includes(selectedOrg.value.name) ||
              String(row.countyName || '').includes(selectedOrg.value.name) ||
              String(row.townshipName || '').includes(selectedOrg.value.name) ||
              String(row.communityName || '').includes(selectedOrg.value.name)
            )
          } else if (dataType.value === 'medical') {
            // 医疗卫生机构数据：按机构地址匹配
            const address = String(row.institutionAddress || '')
            // 匹配逻辑：地址包含选中组织机构的名称
            return address.includes(selectedOrg.value.name)
          }
          return true
        })
      }

      tableData.value = allData
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
    hospitalBeds: null,
    firefighters: null,
    volunteers: null,
    militiaReserve: null,
    trainingParticipants: null,
    shelterCapacity: null
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

    let response
    // 根据数据类型调用不同的删除接口
    if (dataType.value === 'community') {
      response = await communityCapacityApi.delete(row.id)
    } else if (dataType.value === 'medical') {
      response = await medicalInstitutionApi.delete(row.id)
    } else {
      // 默认为乡镇数据
      response = await surveyDataApi.delete(row.id)
    }

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

// 批量删除数据
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条数据吗？此操作不可恢复！`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )

    loading.batchDelete = true
    const ids = selectedRows.value.map(row => row.id)
    let response

    if (dataType.value === 'township') {
      // 乡镇数据批量删除
      response = await surveyDataApi.batchDelete(ids)
    } else if (dataType.value === 'community') {
      // 社区数据批量删除
      response = await communityCapacityApi.batchDelete(ids)
    } else if (dataType.value === 'medical') {
      // 医疗卫生机构数据批量删除
      response = await medicalInstitutionApi.batchDelete(ids)
    }

    if (response.success) {
      ElMessage.success(`成功删除 ${selectedRows.value.length} 条数据`)
      selectedRows.value = [] // 清空选择
      getDataList() // 重新加载数据
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    loading.batchDelete = false
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
  if (!searchForm.year) {
    ElMessage.warning('请先选择年份')
    return
  }

  if (!uploadFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  // 对于乡镇数据和社区数据，先检查导入前置条件
  if (dataType.value === 'township' || dataType.value === 'community') {
    try {
      ElMessage.info({
        message: '正在检查导入前置条件...',
        duration: 0
      })
      const checkResponse = await surveyDataApi.checkImportPrerequisites(searchForm.year)

      // 关闭检查消息
      ElMessage.closeAll()

      if (checkResponse.success && checkResponse.data) {
        const result = checkResponse.data
        if (!result.canImport) {
          // 显示详细的错误信息
          ElMessageBox.alert(
            result.message || '导入前置条件检查失败，请检查数据完整性',
            '无法导入数据',
            {
              confirmButtonText: '确定',
              type: 'error',
              dangerouslyUseHTMLString: true
            }
          )
          return
        }
      }
    } catch (error) {
      ElMessage.closeAll()
      console.error('检查导入前置条件失败:', error)
      // 检查失败不阻止导入，只记录日志
    }
  }

  loading.import = true
  try {
    let response
    if (dataType.value === 'township') {
      // 导入乡镇数据
      response = await surveyDataApi.importData(uploadFile.value, searchForm.year)
    } else if (dataType.value === 'community') {
      // 导入社区数据
      response = await communityCapacityApi.importData(uploadFile.value, searchForm.year)
    } else if (dataType.value === 'medical') {
      // 导入医疗卫生机构数据
      response = await medicalInstitutionApi.importData(uploadFile.value, searchForm.year)
    }

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

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await medicalInstitutionApi.downloadTemplate()

    // 创建blob对象
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '医疗卫生机构导入模板.xlsx'

    // 触发下载
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

// 导出数据
const exportData = async () => {
  try {
    let response
    if (dataType.value === 'medical') {
      // 导出医疗卫生机构数据
      const year = searchForm.year || new Date().getFullYear()
      response = await medicalInstitutionApi.exportData(year)
    } else {
      // 导出其他类型数据
      response = await surveyDataApi.exportData()
    }

    console.log('导出响应:', response)

    let blob
    let fileName

    if (dataType.value === 'medical') {
      // 医疗卫生机构数据导出 - 直接返回blob
      blob = response
      fileName = `医疗卫生机构数据_${searchForm.year || new Date().getFullYear()}_${new Date().toISOString().slice(0, 10)}.xlsx`
    } else {
      // 其他类型数据导出 - 需要处理响应格式
      if (response && response.success && response.data) {
        console.log('响应数据类型:', typeof response.data)
        console.log('响应数据长度:', response.data.length)

        let byteArray: Uint8Array

        if (typeof response.data === 'string') {
          // 如果是base64字符串，进行解码
          try {
            const byteCharacters = atob(response.data)
            const byteNumbers = new Array(byteCharacters.length)
            for (let i = 0; i < byteCharacters.length; i++) {
              byteNumbers[i] = byteCharacters.charCodeAt(i)
            }
            byteArray = new Uint8Array(byteNumbers)
          } catch (e) {
            console.error('Base64解码失败:', e)
            ElMessage.error('数据格式错误')
            return
          }
        } else if (Array.isArray(response.data)) {
          // 如果是字节数组，直接转换
          byteArray = new Uint8Array(response.data)
        } else {
          console.error('未知的数据格式:', response.data)
          ElMessage.error('数据格式不支持')
          return
        }

        console.log('处理后的字节数组长度:', byteArray.length)

        if (byteArray.length === 0) {
          ElMessage.error('导出的文件为空')
          return
        }

        blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        fileName = `调查数据_${new Date().toISOString().slice(0, 10)}.xlsx`
      } else {
        console.error('导出失败，响应:', response)
        ElMessage.error(response?.message || '导出失败：响应数据为空')
        return
      }
    }

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName

    // 触发下载
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + (error as Error).message)
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  console.info('[DataManagement] onMounted -> start loadRegionNameMap')
  await loadRegionNameMap()
  console.info('[DataManagement] onMounted -> loadRegionNameMap done, start getOrganizationList')
  await getOrganizationList()
  console.info('[DataManagement] onMounted -> getOrganizationList done, start getDataList')
  await getDataList()
  console.info('[DataManagement] onMounted -> getDataList done')
  generateYearOptions()
  // 暴露到 window 便于调试（仅开发时使用）
  try {
    ;(window as any).app = (window as any).app || {}
    ;(window as any).app.regionNameMap = regionNameMap.value
    ;(window as any).app.reloadRegions = loadRegionNameMap
  } catch {}
})
</script>

<style scoped>
.data-management {
  max-width: 1920px;
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

/* 左右布局容器 */
.layout-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 200px);
}

/* 左侧组织机构树面板 */
.org-tree-panel {
  width: 300px;
  flex-shrink: 0;
  height: 100%;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 组织机构树节点样式 */
.org-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.org-name {
  font-weight: 500;
  color: #303133;
}

.org-code {
  font-size: 12px;
  color: #909399;
  font-family: 'Courier New', monospace;
}

/* 右侧数据管理面板 */
.data-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

/* 选中组织机构信息卡片 */
.selected-org-info {
  margin-bottom: 0;
}

.org-info-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.org-info-code {
  color: #606266;
  font-size: 14px;
}

.type-switch-card {
  margin-bottom: 0;
  text-align: center;
}

.type-switch-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.toolbar-card {
  margin-bottom: 0;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.table-card {
  min-height: 400px;
  flex: 1;
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
