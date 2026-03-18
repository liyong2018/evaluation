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
              <span class="org-code" v-if="data.children && data.children.length > 0">（{{ data.children.length }}）</span>
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
                  plain
                  :loading="loading.deleteAll"
                  @click="handleDeleteAll"
                >
                  <el-icon><Delete /></el-icon>
                  全部删除
                </el-button>
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

        <!-- 医疗卫生机构名称 (仅医疗机构数据 - 放在最前面) -->
        <el-table-column v-if="dataType === 'medical'" prop="institutionName" label="医疗机构名称" width="200" show-overflow-tooltip />

        <!-- 区域名称 (仅乡镇数据显示) -->
        <el-table-column v-if="dataType === 'township'" label="区域名称" width="160">
          <template #default="{ row }">
            {{ getRegionName(row) }}
          </template>
        </el-table-column>

        <!-- ========== 乡镇/社区数据列 ========== -->
        <!-- 省份 -->
        <el-table-column label="省份" width="100">
          <template #default="{ row }">
            {{ getFieldValue(row, 'province') }}
          </template>
        </el-table-column>
        <!-- 市 -->
        <el-table-column label="市/州" width="100">
          <template #default="{ row }">
            {{ getFieldValue(row, 'city') }}
          </template>
        </el-table-column>
        <!-- 县 -->
        <el-table-column label="区/县/市" width="100">
          <template #default="{ row }">
            {{ getFieldValue(row, 'county') }}
          </template>
        </el-table-column>
        <!-- 乡镇 -->
        <el-table-column label="街道/乡镇" width="120">
          <template #default="{ row }">
            {{ getFieldValue(row, 'township') }}
          </template>
        </el-table-column>

        <!-- 社区/行政村 (医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="communityName" label="社区/行政村" width="140" show-overflow-tooltip />

        <!-- 社区(行政村) (社区数据) -->
        <el-table-column v-if="dataType === 'community'" prop="communityName" label="社区(行政村)" width="160" />

        <!-- 统一社会信用代码 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="unifiedSocialCreditCode" label="统一社会信用代码" width="200" />
        <!-- 医疗机构地址 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="institutionAddress" label="机构地址" width="250" show-overflow-tooltip />
        <!-- 医疗机构类型 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="institutionTypeLarge" label="机构类型(大类)" width="120">
          <template #default="{ row }">
            {{ formatMedicalInstitutionTypeLarge(row.institutionTypeLarge) }}
          </template>
        </el-table-column>
        <!-- 医院等级 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="hospitalLevel" label="医院等级" width="100">
          <template #default="{ row }">
            {{ formatMedicalHospitalLevel(row.hospitalLevel) }}
          </template>
        </el-table-column>
        <!-- 实有床位数 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="actualHospitalBeds" label="实有床位数" width="100" />

        <!-- ========== 乡镇数据列 ========== -->
        <!-- 管理人员 (仅乡镇数据) -->
        <el-table-column v-if="dataType === 'township'" prop="managementStaff" label="管理人员" width="160" />

        <!-- ========== 社区数据列 ========== -->
        <!-- 应急预案 -->
        <el-table-column v-if="dataType === 'community'" prop="hasEmergencyPlan" label="应急预案" width="100">
          <template #default="{ row }">
            {{ formatYesNo(row.hasEmergencyPlan) }}
          </template>
        </el-table-column>
        <!-- 弱势人群清单 -->
        <el-table-column v-if="dataType === 'community'" prop="hasVulnerableGroupsList" label="弱势人群清单" width="120">
          <template #default="{ row }">
            {{ formatYesNo(row.hasVulnerableGroupsList) }}
          </template>
        </el-table-column>
        <!-- 地质灾害隐患点清单 -->
        <el-table-column v-if="dataType === 'community'" prop="hasDisasterPointsList" label="地质灾害隐患点清单" width="150">
          <template #default="{ row }">
            {{ formatYesNo(row.hasDisasterPointsList) }}
          </template>
        </el-table-column>
        <!-- 灾害类地图 -->
        <el-table-column v-if="dataType === 'community'" prop="hasDisasterMap" label="灾害类地图" width="100">
          <template #default="{ row }">
            {{ formatYesNo(row.hasDisasterMap) }}
          </template>
        </el-table-column>

        <!-- ========== 乡镇/社区数据列 ========== -->
        <!-- 人口数量 -->
        <el-table-column label="人口数量" width="100" v-if="dataType !== 'medical'">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.population : row.residentPopulation }}
          </template>
        </el-table-column>

        <!-- 风险评估 (仅乡镇数据) -->
        <el-table-column v-if="dataType === 'township'" prop="riskAssessment" label="风险评估" width="140">
          <template #default="{ row }">
            {{ formatYesNo(row.riskAssessment) }}
          </template>
        </el-table-column>

        <!-- 资金投入(万元) -->
        <el-table-column v-if="dataType !== 'medical'" prop="fundingAmount" label="资金投入(万元)" width="140">
          <template #default="{ row }">
            <span v-if="dataType === 'township'">{{ formatDecimal(row.fundingAmount) }}</span>
            <span v-else>{{ formatDecimal(row.lastYearFundingAmount) }}</span>
          </template>
        </el-table-column>
        <!-- 物资价值(万元) -->
        <el-table-column v-if="dataType !== 'medical'" prop="materialValue" label="物资价值(万元)" width="140">
          <template #default="{ row }">
            <span v-if="dataType === 'township'">{{ formatDecimal(row.materialValue) }}</span>
            <span v-else>{{ formatDecimal(row.materialsEquipmentValue) }}</span>
          </template>
        </el-table-column>

        <!-- 医疗服务点数 -->
        <el-table-column v-if="dataType === 'community'" prop="medicalServiceCount" label="医疗服务点数" width="110" />

        <!-- 医院床位 -->
        <el-table-column v-if="dataType === 'township'" prop="hospitalBeds" label="医院床位" width="100" />

        <!-- 消防员数量 (仅乡镇数据) -->
        <el-table-column v-if="dataType === 'township'" prop="firefighters" label="消防员数量" width="100" />

        <!-- 志愿者人数 -->
        <el-table-column v-if="dataType !== 'medical'" label="志愿者人数" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.volunteers : row.registeredVolunteerCount }}
          </template>
        </el-table-column>

        <!-- 民兵预备役 -->
        <el-table-column v-if="dataType !== 'medical'" label="民兵预备役" width="100">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.militiaReserve : row.militiaReserveCount }}
          </template>
        </el-table-column>

        <!-- 培训参与人次 -->
        <el-table-column v-if="dataType !== 'medical'" label="培训参与人次" width="130">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.trainingParticipants : row.lastYearTrainingParticipants }}
          </template>
        </el-table-column>

        <!-- 演练参与人次 (仅社区数据) -->
        <el-table-column v-if="dataType === 'community'" prop="lastYearDrillParticipants" label="演练参与人次" width="120" />

        <!-- 避难场所容量 -->
        <el-table-column v-if="dataType !== 'medical'" label="避难场所容量" width="120">
          <template #default="{ row }">
            {{ dataType === 'township' ? row.shelterCapacity : row.emergencyShelterCapacity }}
          </template>
        </el-table-column>

        <!-- 在岗职工人数 (仅医疗机构数据) -->
        <el-table-column v-if="dataType === 'medical'" prop="totalStaff" label="在岗职工人数" width="120" />
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
        <template v-if="dataType !== 'medical'">
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
        </template>
        
        <template v-if="dataType === 'medical'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="机构名称" prop="institutionName">
                <el-input v-model="formData.institutionName" placeholder="请输入医疗机构名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="社会信用代码" prop="unifiedSocialCreditCode">
                <el-input v-model="formData.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="机构地址" prop="institutionAddress">
                <el-input v-model="formData.institutionAddress" placeholder="请输入机构地址" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="机构大类" prop="institutionTypeLarge">
                <el-input v-model="formData.institutionTypeLarge" placeholder="请输入机构大类" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="医院等级" prop="hospitalLevel">
                <el-input v-model="formData.hospitalLevel" placeholder="请输入医院等级" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="实有床位" prop="actualHospitalBeds">
                <el-input-number v-model="formData.actualHospitalBeds" :min="0" placeholder="请输入实有床位" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职工总数" prop="totalStaff">
                <el-input-number v-model="formData.totalStaff" :min="0" placeholder="请输入职工总数" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
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
            :accept="getFileAcceptTypes()"
            drag
            style="width: 100%"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                {{ getFileUploadTip() }}
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
import { surveyDataApi, communityCapacityApi, organizationApi, medicalInstitutionApi, grassrootsOrganizationApi } from '@/api'
import { useGlobalYearStore } from '@/stores/globalYear'
import { useUserStore } from '@/stores/user'
import { useGlobalOrganizationStore } from '@/stores/globalOrganization'

// 修复ResizeObserver错误
const originalError = console.error
console.error = (...args: any[]) => {
  if (args[0]?.includes?.('ResizeObserver loop completed with undelivered notifications')) {
    return
  }
  originalError(...args)
}

// 全局年份 store
const globalYearStore = useGlobalYearStore()
// 用户 store
const userStore = useUserStore()
// 全局组织机构 store
const globalOrganizationStore = useGlobalOrganizationStore()

// 响应式数据
const dataType = ref<'township' | 'community' | 'medical'>('medical')  // 数据类型：township(乡镇)、community(社区) 或 medical(医疗机构)
const tableData = ref<any[]>([])
const selectedRows = ref<any[]>([])
// 代码->名称映射表（一次性从后端加载）
const regionNameMap = ref<Record<string, string>>({})
const orgCodeNameMap = ref<Record<string, string>>({})
// 下拉选项（从后端获取），包含代码与名称
const regionSelectOptions = ref<Array<{ code: string; name: string }>>([])
// 组织机构相关
const selectedOrg = ref<any>(null) // 当前选中的组织机构
const organizationList = ref<any[]>([]) // 组织机构树列表
const orgTreeRef = ref() // 组织机构树引用

const searchForm = reactive({
  keyword: '',
  year: globalYearStore.selectedYear,
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
  batchDelete: false,
  deleteAll: false
})

const dialogVisible = reactive({
  form: false,
  import: false
})

const isEdit = ref(false)
const formRef = ref<FormInstance>()
const uploadRef = ref()
const uploadFile = ref<File | null>(null)

const normalizeOrgCode = (code?: string | number | null) => {
  if (code === null || code === undefined) return undefined
  const raw = String(code).trim()
  if (!raw) return undefined
  const trimmed = raw.replace(/0+$/, '')
  return trimmed || raw
}

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
  shelterCapacity: null,
  // 医疗卫生机构特有字段
  institutionName: '',
  unifiedSocialCreditCode: '',
  institutionAddress: '',
  institutionTypeLarge: '',
  hospitalLevel: '',
  actualHospitalBeds: null,
  totalStaff: null
})

const formRules = {
  regionCode: [{ required: true, message: '请输入地区代码', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入市', trigger: 'blur' }],
  county: [{ required: true, message: '请输入县', trigger: 'blur' }],
  township: [{ required: true, message: '请输入乡镇(街道)', trigger: 'blur' }],
  population: [{ required: true, message: '请输入人口数量', trigger: 'blur' }],
  // 医疗卫生机构校验规则
  institutionName: [{ required: true, message: '请输入医疗机构名称', trigger: 'blur' }],
  unifiedSocialCreditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }]
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

// 判断是否为admin或省级用户
const isUserAdminOrProvincial = computed(() => {
  // 如果是admin，返回true
  if (userStore.isAdmin) {
    return true
  }
  // 如果选中的组织机构是省级(level=1)，也返回true
  if (selectedOrg.value && selectedOrg.value.level === 1) {
    return true
  }
  return false
})

// 收集需要展开的节点
const collectExpandedKeys = (nodes: any[], level: number = 1, keys: string[] = []) => {
  if (!nodes || nodes.length === 0) return keys
  for (const node of nodes) {
    // 如果是admin或省级用户，只展开到2级（市级），区县级节点折叠
    // 否则展开到3级（县级）
    const maxLevel = isUserAdminOrProvincial.value ? 2 : 3
    if (level < maxLevel) {
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

const collectOrgCodeNameMap = (tree: any[], result: Record<string, string> = {}) => {
  for (const node of tree || []) {
    const code = String(node?.code || '').trim()
    const name = String(node?.name || '').trim()
    if (code && name && !/^\d{2,}$/.test(name)) {
      result[code] = name
    }
    if (Array.isArray(node?.children) && node.children.length > 0) {
      collectOrgCodeNameMap(node.children, result)
    }
  }
  return result
}

const normalizeOrgTreeNames = (tree: any[], fallbackNameMap: Record<string, string>) => {
  const patchNode = (node: any) => {
    const normalizedCode = String(node?.code || '').trim()
    const normalizedName = String(node?.name || '').trim()
    if (normalizedCode && (!normalizedName || /^\d{2,}$/.test(normalizedName))) {
      const fallback = fallbackNameMap[normalizedCode]
      if (fallback) {
        node.name = fallback
      }
    }
    if (Array.isArray(node?.children) && node.children.length > 0) {
      node.children.forEach((child: any) => patchNode(child))
    }
  }
  ;(tree || []).forEach((node: any) => patchNode(node))
}

const getOrganizationList = async () => {
  loading.organizations = true
  try {
    const [response, baselineResponse] = await Promise.all([
      organizationApi.getTree({ year: searchForm.year || undefined }),
      organizationApi.getTree({})
    ])
    if (response.success && response.data) {
      organizationList.value = response.data || []
      const fallbackMap = collectOrgCodeNameMap(baselineResponse?.success ? (baselineResponse.data || []) : [])
      normalizeOrgTreeNames(organizationList.value, fallbackMap)
      // 收集需要展开的节点key
      defaultExpandedKeys.value = collectExpandedKeys(organizationList.value)
      console.log('组织机构树形数据 (年份:', searchForm.year, '):', organizationList.value)

      // 优先从全局 store 恢复选中的组织机构
      let targetOrg = selectedOrg.value

      // 如果当前没有选中组织机构，尝试从全局 store 恢复
      if (!targetOrg && globalOrganizationStore.selectedOrganization) {
        const stored = globalOrganizationStore.selectedOrganization
        targetOrg = findOrgNodeByCode(organizationList.value, stored.code)
        if (targetOrg) {
          console.log('从全局 store 恢复组织机构:', targetOrg)
        }
      }

      // 如果还是没有选中的组织机构，或者选中的组织机构不存在，则默认选中第一个
      if (!targetOrg) {
        await selectFirstOrganization()
      } else {
        selectedOrg.value = targetOrg
        await nextTick()
        orgTreeRef.value?.setCurrentKey(targetOrg.code)
        // 同步到全局 store
        globalOrganizationStore.setOrganization({
          code: targetOrg.code,
          name: targetOrg.name,
          level: targetOrg.level
        })
        getDataList()
      }
    }
  } catch (error) {
    console.error('获取组织机构列表失败:', error)
    ElMessage.error('获取组织机构列表失败')
  } finally {
    loading.organizations = false
  }
}

// 选中第一个组织机构
const selectFirstOrganization = async () => {
  if (!organizationList.value || organizationList.value.length === 0) {
    return
  }
  // 默认选中第一个省级节点
  const firstOrg = organizationList.value[0]
  console.log('🎯 选中第一个组织机构:', firstOrg)
  selectedOrg.value = firstOrg
  await nextTick()
  console.log('🎯 调用 setCurrentKey:', firstOrg.code, orgTreeRef.value)
  orgTreeRef.value?.setCurrentKey(firstOrg.code)
  // 保存到全局 store
  globalOrganizationStore.setOrganization({
    code: firstOrg.code,
    name: firstOrg.name,
    level: firstOrg.level
  })
  // 加载数据
  getDataList()
}

// 刷新组织机构树
const refreshOrganizations = () => {
  getOrganizationList()
}

// 处理组织机构节点点击
const handleOrgNodeClick = (data: any) => {
  console.log('选中组织机构:', data)
  selectedOrg.value = data
  // 保存到全局 store（包含完整信息）
  globalOrganizationStore.setOrganization({
    code: data.code,
    name: data.name,
    level: data.level,
    provinceName: data.provinceName,
    cityName: data.cityName,
    countyName: data.countyName,
    townshipName: data.townshipName,
    communityName: data.communityName
  })
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

// 获取字段值（处理不同数据类型的字段名差异）
const getFieldValue = (row: any, fieldName: string) => {
  if (!row) return '-'
  if (dataType.value === 'township' && !['province', 'city', 'county', 'township'].includes(fieldName)) {
    return row[fieldName] || '-'
  }
  let rawValue = row[`${fieldName}Name`] || row[fieldName]
  const addressText = String(row.communityAddress || row.institutionAddress || row.address || '').trim()
  const parsedByAddress = parseDivisionFromAddress(addressText)
  if ((rawValue === null || rawValue === undefined || rawValue === '' || rawValue === '-') && fieldName === 'township') {
    const regionCode = String(row.regionCode || '').trim()
    if (/^\d{9,}$/.test(regionCode)) {
      const townshipCode = regionCode.substring(0, 9)
      const townshipName = getOrgNameByCode(townshipCode)
      if (townshipName) {
        rawValue = townshipName
      } else {
        rawValue = townshipCode
      }
    }
  }
  if ((rawValue === null || rawValue === undefined || rawValue === '' || rawValue === '-') && parsedByAddress) {
    if (fieldName === 'province') rawValue = parsedByAddress.province
    if (fieldName === 'city') rawValue = parsedByAddress.city
    if (fieldName === 'county') rawValue = parsedByAddress.county
    if (fieldName === 'township') rawValue = parsedByAddress.township
  }
  if (!rawValue || rawValue === '-') return '-'
  const normalized = String(rawValue).trim()
  if (!/^\d{2,}$/.test(normalized)) {
    return normalized
  }
  const regionCode = String(row.regionCode || '').trim()
  if (fieldName === 'province' && /^\d{6,}$/.test(regionCode)) {
    const fallback = orgCodeNameMap.value[`${regionCode.substring(0, 2)}0000`]
    if (fallback) return fallback
  }
  if (fieldName === 'city' && /^\d{6,}$/.test(regionCode)) {
    const fallback = orgCodeNameMap.value[`${regionCode.substring(0, 4)}00`]
    if (fallback) return fallback
  }
  if (fieldName === 'county' && /^\d{6,}$/.test(regionCode)) {
    const fallback = orgCodeNameMap.value[regionCode.substring(0, 6)]
    if (fallback) return fallback
  }
  if (fieldName === 'township' && /^\d{9,}$/.test(regionCode)) {
    const fallback = orgCodeNameMap.value[regionCode.substring(0, 9)]
    if (fallback) return fallback
  }
  const cacheName = orgCodeNameMap.value[normalized]
  if (cacheName) {
    return cacheName
  }
  const orgName = getOrgNameByCode(normalized)
  if (orgName) {
    orgCodeNameMap.value[normalized] = orgName
    return orgName
  }
  if (parsedByAddress) {
    if (fieldName === 'province' && parsedByAddress.province) return parsedByAddress.province
    if (fieldName === 'city' && parsedByAddress.city) return parsedByAddress.city
    if (fieldName === 'county' && parsedByAddress.county) return parsedByAddress.county
    if (fieldName === 'township' && parsedByAddress.township) return parsedByAddress.township
  }
  return normalized
}

const getOrgNameByCode = (code: string) => {
  const normalizedCode = String(code || '').trim()
  if (!normalizedCode) return ''
  if (orgCodeNameMap.value[normalizedCode]) {
    return orgCodeNameMap.value[normalizedCode]
  }
  const candidates = new Set<string>([normalizedCode])
  if (/^\d{6}$/.test(normalizedCode)) {
    candidates.add(`${normalizedCode.substring(0, 2)}0000`)
    candidates.add(`${normalizedCode.substring(0, 4)}00`)
    candidates.add(normalizedCode.substring(0, 2))
    candidates.add(normalizedCode.substring(0, 4))
  } else if (/^\d{9}$/.test(normalizedCode)) {
    candidates.add(normalizedCode.substring(0, 6))
    candidates.add(`${normalizedCode.substring(0, 4)}00`)
    candidates.add(`${normalizedCode.substring(0, 2)}0000`)
  } else if (/^\d{12}$/.test(normalizedCode)) {
    candidates.add(normalizedCode.substring(0, 9))
    candidates.add(normalizedCode.substring(0, 6))
  }
  for (const candidate of candidates) {
    const mapped = orgCodeNameMap.value[candidate]
    if (mapped) return mapped
  }
  return ''
}

const cacheNameWithCode = (code: string, name: string) => {
  const normalizedCode = String(code || '').trim()
  const normalizedName = String(name || '').trim()
  if (!normalizedCode || !normalizedName) return
  orgCodeNameMap.value[normalizedCode] = normalizedName
  if (/^\d{12}$/.test(normalizedCode)) {
    orgCodeNameMap.value[normalizedCode.substring(0, 9)] = normalizedName
  }
  if (/^\d{6}$/.test(normalizedCode)) {
    orgCodeNameMap.value[`${normalizedCode.substring(0, 2)}0000`] = orgCodeNameMap.value[`${normalizedCode.substring(0, 2)}0000`] || normalizedName
    orgCodeNameMap.value[`${normalizedCode.substring(0, 4)}00`] = orgCodeNameMap.value[`${normalizedCode.substring(0, 4)}00`] || normalizedName
  }
}

const preloadOrgCodeNameMap = async (rows: any[]) => {
  if ((dataType.value !== 'township' && dataType.value !== 'community' && dataType.value !== 'medical') || !rows?.length) return
  const queue = [...organizationList.value]
  while (queue.length > 0) {
    const node = queue.shift()
    if (!node) continue
    cacheNameWithCode(String(node.code || ''), String(node.name || ''))
    if (Array.isArray(node.children) && node.children.length > 0) {
      queue.push(...node.children)
    }
  }
  const countyCodes = new Set<string>()
  rows.forEach((row: any) => {
    ;[row.countyName, row.county].forEach((value: any) => {
      const code = String(value || '').trim()
      if (/^\d{6,12}$/.test(code)) countyCodes.add(code.substring(0, 6))
    })
    const regionCode = String(row.regionCode || row.orgCode || '').trim()
    if (/^\d{6,}$/.test(regionCode)) {
      countyCodes.add(regionCode.substring(0, 6))
      orgCodeNameMap.value[`${regionCode.substring(0, 2)}0000`] = orgCodeNameMap.value[`${regionCode.substring(0, 2)}0000`] || ''
      orgCodeNameMap.value[`${regionCode.substring(0, 4)}00`] = orgCodeNameMap.value[`${regionCode.substring(0, 4)}00`] || ''
    }
  })
  const year = searchForm.year || undefined
  await Promise.all(Array.from(countyCodes).map(async (countyCode) => {
    try {
      const response = await grassrootsOrganizationApi.getTownshipsByCountyCode(countyCode, year)
      if (response?.success && Array.isArray(response.data)) {
        response.data.forEach((item: any) => {
          cacheNameWithCode(String(item?.code || ''), String(item?.name || item?.townshipName || ''))
          if (item?.provinceName) cacheNameWithCode(`${countyCode.substring(0, 2)}0000`, String(item.provinceName))
          if (item?.cityName) cacheNameWithCode(`${countyCode.substring(0, 4)}00`, String(item.cityName))
          if (item?.countyName) cacheNameWithCode(countyCode, String(item.countyName))
        })
      }
    } catch (error) {
      console.warn('预加载乡镇名称失败:', countyCode, error)
    }
  }))
}

const parseDivisionFromAddress = (address: string) => {
  const text = String(address || '').trim()
  if (!text) return null
  const provinceMatch = text.match(/([^省]+省|[^自治区]+自治区|[^特别行政区]+特别行政区)/)
  const cityMatch = text.match(/([^市]+市|[^州]+州|[^地区]+地区|[^盟]+盟)/)
  const countyMatch = text.match(/([^县]+县|[^区]+区|[^市]+市|[^旗]+旗)/)
  const townshipMatch = text.match(/([^镇]+镇|[^乡]+乡|[^街道]+街道|[^苏木]+苏木)/)
  return {
    province: provinceMatch?.[1] || '',
    city: cityMatch?.[1] || '',
    county: countyMatch?.[1] || '',
    township: townshipMatch?.[1] || ''
  }
}

const formatYesNo = (value: any) => {
  const normalized = String(value ?? '').trim().toLowerCase()
  if (normalized === '是' || normalized === 'yes' || normalized === 'true' || normalized === '1') return '是'
  if (normalized === '否' || normalized === 'no' || normalized === 'false' || normalized === '0' || normalized === '2') return '否'
  return value || '-'
}

const MEDICAL_INSTITUTION_TYPE_LARGE_MAP: Record<string, string> = {
  '1': '医院',
  '2': '基层医疗机构',
  '3': '专业公共卫生机构'
}

const MEDICAL_HOSPITAL_LEVEL_GRADE_MAP: Record<string, string> = {
  '5': '特等',
  '6': '甲等',
  '7': '乙等',
  '8': '丙等',
  '9': '未定等'
}

const MEDICAL_HOSPITAL_LEVEL_CLASS_MAP: Record<string, string> = {
  '1': '三级',
  '2': '二级',
  '3': '一级',
  '4': '未定级'
}

const formatMedicalInstitutionTypeLarge = (value: any) => {
  const raw = String(value ?? '').trim()
  if (!raw) return '-'
  return MEDICAL_INSTITUTION_TYPE_LARGE_MAP[raw] || raw
}

const formatMedicalHospitalLevel = (value: any) => {
  const raw = String(value ?? '').trim()
  if (!raw) return '-'
  if (raw.includes(';')) return raw
  const parts = raw.split(',').map((item: string) => item.trim()).filter(Boolean)
  if (parts.length !== 2) return raw
  const [first, second] = parts
  const firstAsGrade = MEDICAL_HOSPITAL_LEVEL_GRADE_MAP[first]
  const secondAsClass = MEDICAL_HOSPITAL_LEVEL_CLASS_MAP[second]
  if (firstAsGrade && secondAsClass) return `${firstAsGrade};${secondAsClass}`
  const firstAsClass = MEDICAL_HOSPITAL_LEVEL_CLASS_MAP[first]
  const secondAsGrade = MEDICAL_HOSPITAL_LEVEL_GRADE_MAP[second]
  if (firstAsClass && secondAsGrade) return `${secondAsGrade};${firstAsClass}`
  return raw
}

const getImportResultPayload = (response: any) => {
  if (!response || typeof response !== 'object') return response

  if (Object.prototype.hasOwnProperty.call(response, 'success')) {
    return response
  }

  if (response.data && typeof response.data === 'object') {
    if (Object.prototype.hasOwnProperty.call(response.data, 'success')) {
      return response.data
    }
    if (response.data.data && typeof response.data.data === 'object' && Object.prototype.hasOwnProperty.call(response.data.data, 'success')) {
      return response.data.data
    }
  }

  return response
}

const isOperationSuccess = (payload: any) => {
  if (payload === true) return true
  if (!payload || typeof payload !== 'object') return false
  if (payload.success === true) return true
  if (payload.code === 200 || payload.code === '200' || payload.code === 0 || payload.code === '0') return true
  if (payload.data === true) return true
  return false
}

const isRequestTimeoutError = (error: any) => {
  const code = String(error?.code || '')
  const message = String(error?.message || '')
  return code === 'ECONNABORTED' || /timeout/i.test(message)
}

// 格式化数字，保留4位小数
const formatDecimal = (value: any) => {
  if (value === null || value === undefined || value === '') return '-'
  const num = parseFloat(value)
  if (isNaN(num)) return '-'
  return num.toFixed(4)
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
  loading.table = true
  try {
    let response
    let allData: any[] = []

    if (dataType.value === 'township') {
      // 乡镇数据 - 使用分页查询
      const normalizedOrgCode = normalizeOrgCode(selectedOrg.value?.code)
      response = await surveyDataApi.getAll({
        year: searchForm.year || undefined,
        orgCode: normalizedOrgCode,
        page: pagination.currentPage,
        pageSize: pagination.pageSize
      })
      if (response.success) {
        // 新的分页返回格式：{ records: [], total: 0, current: 1, pages: 0, size: 50 }
        if (response.data && typeof response.data === 'object' && 'records' in response.data) {
          allData = response.data.records || []
          pagination.total = response.data.total || 0
          // 后端返回的分页信息
          if (response.data.current) pagination.currentPage = response.data.current
          if (response.data.pages) {
            (pagination as any).pages = response.data.pages
          }
        } else {
          // 兼容旧格式（直接返回数组）
          allData = response.data || []
          pagination.total = allData.length
        }
      }
    } else if (dataType.value === 'community') {
      // 社区数据 - 使用分页查询
      const normalizedOrgCode = normalizeOrgCode(selectedOrg.value?.code)
      response = await communityCapacityApi.getList({
        page: pagination.currentPage,
        size: pagination.pageSize,
        regionCode: normalizedOrgCode || undefined,
        year: searchForm.year || undefined
      })
      if (response.success) {
        // 新的分页返回格式：{ records: [], total: 0, current: 1, pages: 0, size: 50 }
        if (response.data && typeof response.data === 'object' && 'records' in response.data) {
          allData = response.data.records || []
          pagination.total = response.data.total || 0
          if (response.data.current) pagination.currentPage = response.data.current
          if (response.data.pages) {
            (pagination as any).pages = response.data.pages
          }
        } else {
          // 兼容旧格式
          allData = response.data || []
          pagination.total = response.data?.total || allData.length
        }
      }
    } else if (dataType.value === 'medical') {
      // 医疗卫生机构数据 - 使用分页查询
      const year = searchForm.year || new Date().getFullYear()
      const normalizedOrgCode = normalizeOrgCode(selectedOrg.value?.code)
      response = await medicalInstitutionApi.getPage(year, pagination.currentPage, pagination.pageSize, normalizedOrgCode || undefined)
      if (response.success) {
        // 新的分页返回格式：{ records: [], total: 0, current: 1, pages: 0, size: 50 }
        if (response.data && typeof response.data === 'object' && 'records' in response.data) {
          allData = response.data.records || []
          pagination.total = response.data.total || 0
          if (response.data.current) pagination.currentPage = response.data.current
          if (response.data.pages) {
            (pagination as any).pages = response.data.pages
          }
        } else {
          // 兼容旧格式
          allData = response.data || []
          pagination.total = response.data?.total || allData.length
        }
      }
    }

    if (response.success) {
      tableData.value = allData
      await preloadOrgCodeNameMap(tableData.value)
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
  // 更新全局年份 store
  if (searchForm.year) {
    globalYearStore.setYear(searchForm.year)
  }

  // 当年份改变时，刷新组织机构树
  await getOrganizationList()

  if (!searchForm.keyword && !searchForm.selectedRegion) {
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
        // 使用分页查询
        const normalizedOrgCode = normalizeOrgCode(selectedOrg.value?.code)
        response = await surveyDataApi.getAll({
          year: searchForm.year || undefined,
          orgCode: normalizedOrgCode,
          page: pagination.currentPage,
          pageSize: pagination.pageSize
        })
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
        // 如果只有年份过滤，使用 getList（现在调用的是 /page 接口）
        const year = searchForm.year || new Date().getFullYear()
        response = await medicalInstitutionApi.getList(year, selectedOrg.value?.code)
      }
    }

    if (response?.success) {
      let allData: any[] = []

      // 处理数据的多种返回格式
      if (response.data) {
        if (typeof response.data === 'object' && 'records' in response.data) {
          // /page 接口格式：{ records: [], total: number, ... }
          allData = response.data.records || []
          pagination.total = response.data.total || 0
          if (response.data.current) pagination.currentPage = response.data.current
          if (response.data.pages) (pagination as any).pages = response.data.pages
        } else if (Array.isArray(response.data)) {
          // 直接数组格式
          allData = response.data || []
          pagination.total = allData.length
        } else {
          // 对象但不是数组
          allData = [response.data]
          pagination.total = 1
        }
      }

      // 如果选中了组织机构，过滤数据（搜索时）
      if (selectedOrg.value && allData.length > 0) {
        const orgCode = normalizeOrgCode(selectedOrg.value.code)
        const orgCodeValue = orgCode ?? ''
        allData = allData.filter((row: any) => {
          // 根据数据类型过滤
          if (dataType.value === 'township') {
            if (orgCode) {
              return String(row.regionCode || '').startsWith(orgCode)
            }
            return (
              String(row.province || '').includes(selectedOrg.value.name) ||
              String(row.city || '').includes(selectedOrg.value.name) ||
              String(row.county || '').includes(selectedOrg.value.name) ||
              String(row.township || '').includes(selectedOrg.value.name)
            )
          } else if (dataType.value === 'community') {
            // 社区数据：匹配省、市、县、乡镇、社区名称
            return (
              String(row.regionCode || '').startsWith(orgCodeValue) ||
              String(row.provinceName || '').includes(selectedOrg.value.name) ||
              String(row.cityName || '').includes(selectedOrg.value.name) ||
              String(row.countyName || '').includes(selectedOrg.value.name) ||
              String(row.townshipName || '').includes(selectedOrg.value.name) ||
              String(row.communityName || '').includes(selectedOrg.value.name)
            )
          } else if (dataType.value === 'medical') {
            return (
              String(row.orgCode || '').startsWith(orgCodeValue) ||
              String(row.provinceName || '').includes(selectedOrg.value.name) ||
              String(row.cityName || '').includes(selectedOrg.value.name) ||
              String(row.countyName || '').includes(selectedOrg.value.name) ||
              String(row.townshipName || '').includes(selectedOrg.value.name) ||
              String(row.communityName || '').includes(selectedOrg.value.name) ||
              String(row.institutionAddress || '').includes(selectedOrg.value.name)
            )
          }
          return true
        })
      }

      tableData.value = allData
      await preloadOrgCodeNameMap(tableData.value)
      // 对于非分页的搜索结果，更新 total
      if (dataType.value !== 'township' || searchForm.keyword || searchForm.selectedRegion) {
        pagination.total = tableData.value.length
      }
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

const mapCommunityRowToForm = (row: any) => {
  return {
    id: row?.id ?? null,
    regionCode: row?.regionCode ?? '',
    province: row?.provinceName ?? '',
    city: row?.cityName ?? '',
    county: row?.countyName ?? '',
    township: row?.townshipName ?? '',
    population: row?.residentPopulation ?? null,
    fundingAmount: row?.lastYearFundingAmount ?? null,
    materialValue: row?.materialsEquipmentValue ?? null,
    hospitalBeds: row?.medicalServiceCount ?? null,
    volunteers: row?.registeredVolunteerCount ?? null,
    militiaReserve: row?.militiaReserveCount ?? null,
    trainingParticipants: row?.lastYearTrainingParticipants ?? null,
    shelterCapacity: row?.emergencyShelterCapacity ?? null
  }
}

const buildCommunityUpdatePayload = () => {
  const payload: any = {
    regionCode: formData.regionCode,
    provinceName: formData.province,
    cityName: formData.city,
    countyName: formData.county,
    townshipName: formData.township,
    residentPopulation: formData.population,
    lastYearFundingAmount: formData.fundingAmount,
    materialsEquipmentValue: formData.materialValue,
    medicalServiceCount: formData.hospitalBeds,
    registeredVolunteerCount: formData.volunteers,
    militiaReserveCount: formData.militiaReserve,
    lastYearTrainingParticipants: formData.trainingParticipants,
    emergencyShelterCapacity: formData.shelterCapacity
  }

  if (searchForm.year) payload.year = searchForm.year

  return payload
}

const mapMedicalRowToForm = (row: any) => {
  return {
    id: row?.id ?? null,
    institutionName: row?.institutionName ?? '',
    unifiedSocialCreditCode: row?.unifiedSocialCreditCode ?? '',
    institutionAddress: row?.institutionAddress ?? '',
    institutionTypeLarge: row?.institutionTypeLarge ?? '',
    hospitalLevel: row?.hospitalLevel ?? '',
    actualHospitalBeds: row?.actualHospitalBeds ?? null,
    totalStaff: row?.totalStaff ?? null
  }
}

const buildMedicalUpdatePayload = () => {
  return {
    id: formData.id,
    institutionName: formData.institutionName,
    unifiedSocialCreditCode: formData.unifiedSocialCreditCode,
    institutionAddress: formData.institutionAddress,
    institutionTypeLarge: formData.institutionTypeLarge,
    hospitalLevel: formData.hospitalLevel,
    actualHospitalBeds: formData.actualHospitalBeds,
    totalStaff: formData.totalStaff
  }
}

// 显示编辑对话框
const showEditDialog = (row: any) => {
  isEdit.value = true
  resetForm()
  if (dataType.value === 'community') {
    Object.assign(formData, mapCommunityRowToForm(row))
  } else if (dataType.value === 'medical') {
    Object.assign(formData, mapMedicalRowToForm(row))
  } else {
    Object.assign(formData, row)
  }
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
    shelterCapacity: null,
    institutionName: '',
    unifiedSocialCreditCode: '',
    institutionAddress: '',
    institutionTypeLarge: '',
    hospitalLevel: '',
    actualHospitalBeds: null,
    totalStaff: null
  })
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (dataType.value === 'community' && !isEdit.value) {
      ElMessage.warning('社区数据暂不支持新增，请使用导入')
      return
    }

    loading.submit = true
    try {
      let response
      if (dataType.value === 'community') {
        if (!formData.id) {
          ElMessage.error('缺少ID，无法更新')
          return
        }
        response = await communityCapacityApi.update(Number(formData.id), buildCommunityUpdatePayload())
      } else if (dataType.value === 'medical') {
        if (isEdit.value) {
          response = await medicalInstitutionApi.update(buildMedicalUpdatePayload())
        } else {
          ElMessage.warning('医疗卫生机构数据暂不支持新增，请使用导入')
          loading.submit = false
          return
        }
      } else {
        if (isEdit.value) {
          response = await surveyDataApi.update(formData)
        } else {
          response = await surveyDataApi.create(formData)
        }
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

// 全部删除数据
const handleDeleteAll = async () => {
  if (!searchForm.year) {
    ElMessage.warning('请先选择年份')
    return
  }

  const year = searchForm.year
  const orgCode = selectedOrg.value ? normalizeOrgCode(selectedOrg.value.code) : undefined
  const dataTypeText = dataType.value === 'township' ? '乡镇评估数据' : dataType.value === 'community' ? '社区减灾能力数据' : '医疗卫生机构数据'
  const orgText = orgCode ? `和组织机构【${selectedOrg.value?.name}】` : ''

  try {
    await ElMessageBox.confirm(
      `<div style="line-height: 1.8;">
        <p style="color: #E6A23C; font-weight: bold; margin-bottom: 12px;">⚠️ 友情提示</p>
        <p style="margin-bottom: 8px;">您即将删除 <strong>${year}年</strong> ${orgText} 的<strong>所有${dataTypeText}</strong>！</p>
        <p style="color: #F56C6C; margin-bottom: 8px;">此操作将永久删除所有符合条件的数据，无法恢复！</p>
        <p style="color: #909399; font-size: 13px;">建议：删除前请先导出数据作为备份</p>
      </div>`,
      '全部删除确认',
      {
        confirmButtonText: '我已了解风险，确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        distinguishCancelAndClose: true
      }
    )

    loading.deleteAll = true
    let response

    if (dataType.value === 'township') {
      response = await surveyDataApi.deleteAllByYearOrg(year, orgCode)
    } else if (dataType.value === 'community') {
      response = await communityCapacityApi.deleteAllByYearOrg(year, orgCode)
    } else if (dataType.value === 'medical') {
      response = await medicalInstitutionApi.deleteAllByYearOrg(year, orgCode)
    }

    if (response.success || response.data !== undefined) {
      const count = response.data || 0
      ElMessage.success(`成功删除 ${count} 条数据`)
      selectedRows.value = []
      getDataList()
    } else {
      ElMessage.error(response.message || '全部删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('全部删除失败:', error)
      ElMessage.error('全部删除失败: ' + ((error as any)?.message || ''))
    }
  } finally {
    loading.deleteAll = false
  }
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1  // 改变大小时重置到第一页
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
  // 清空上传组件的文件列表
  uploadRef.value?.clearFiles()
}

// 文件选择
const handleFileChange = (file: any) => {
  uploadFile.value = file.raw
}

// 获取接受的文件类型（2024、2025年支持.gpkg）
const getFileAcceptTypes = () => {
  if (searchForm.year === 2024 || searchForm.year === 2025) {
    return '.xlsx,.xls,.csv,.gpkg'
  }
  return '.xlsx,.xls,.csv'
}

// 获取文件上传提示
const getFileUploadTip = () => {
  if (searchForm.year === 2024 || searchForm.year === 2025) {
    return '支持 xlsx/xls/csv/gpkg 格式文件，文件大小不超过 10MB（注：.gpkg文件仅2024、2025年可用）'
  }
  return '支持 xlsx/xls/csv 格式文件，文件大小不超过 10MB'
}

// 判断是否为GPKG文件
const isGpkgFile = (file: File) => {
  return file.name.toLowerCase().endsWith('.gpkg')
}

// 上传前验证
const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10

  // 检查是否为GPKG文件
  if (isGpkgFile(file)) {
    // GPKG文件仅2024、2025年可用
    if (searchForm.year !== 2024 && searchForm.year !== 2025) {
      ElMessage.error('.gpkg文件仅支持2024、2025年数据导入')
      return false
    }
    if (!isLt10M) {
      ElMessage.error('文件大小不能超过 10MB')
      return false
    }
    return true
  }

  // Excel/CSV文件验证
  const isValidType = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                      'application/vnd.ms-excel',
                      'text/csv'].includes(file.type)

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

  // 检查是否为GPKG文件
  if (isGpkgFile(uploadFile.value)) {
    // GPKG文件导入流程
    await handleGpkgImport()
    return
  }

  // Excel文件导入流程
  await handleExcelImport()
}

// 处理GPKG文件导入
const handleGpkgImport = async () => {
  const file = uploadFile.value
  if (!file) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  loading.import = true
  try {
    // 第一步：验证GPKG文件字段
    ElMessage.info({
      message: '正在验证GPKG文件字段...',
      duration: 0
    })

    let validateResponse
    if (dataType.value === 'township') {
      validateResponse = await surveyDataApi.validateGpkg(file, searchForm.year)
    } else if (dataType.value === 'community') {
      validateResponse = await communityCapacityApi.validateGpkg(file, searchForm.year)
    } else if (dataType.value === 'medical') {
      validateResponse = await medicalInstitutionApi.validateGpkg(file, searchForm.year)
    }

    ElMessage.closeAll()

    if (validateResponse.success && validateResponse.data) {
      const validationResult = validateResponse.data
      if (!validationResult.valid) {
        // 构造验证失败消息
        let message = 'GPKG文件验证失败！\n'
        message += `数据类型: ${getDataTypeName(validationResult.dataType)}\n`
        message += `图层名称: ${validationResult.layerName || '未知'}\n`

        if (validationResult.missingFields && validationResult.missingFields.length > 0) {
          message += '\n缺少的必要字段:\n'
          validationResult.missingFields.forEach((field: string) => {
            message += `  - ${field}\n`
          })
        }

        if (validationResult.errors && validationResult.errors.length > 0) {
          message += '\n错误信息:\n'
          validationResult.errors.forEach((error: string) => {
            message += `  - ${error}\n`
          })
        }

        ElMessageBox.alert(
          message,
          'GPKG文件验证失败',
          {
            confirmButtonText: '确定',
            type: 'error',
            dangerouslyUseHTMLString: false
          }
        )
        return
      }
    } else {
      ElMessageBox.alert(
        validateResponse.message || '验证GPKG文件失败',
        '验证失败',
        { type: 'error' }
      )
      return
    }

    // 第二步：导入数据
    ElMessage.info({
      message: '正在导入GPKG数据...',
      duration: 0
    })

    let response
    if (dataType.value === 'township') {
      response = await surveyDataApi.importGpkg(file, searchForm.year)
    } else if (dataType.value === 'community') {
      response = await communityCapacityApi.importGpkg(file, searchForm.year)
    } else if (dataType.value === 'medical') {
      response = await medicalInstitutionApi.importGpkg(file, searchForm.year)
    }

    ElMessage.closeAll()

    const importResult = getImportResultPayload(response)
    if (isOperationSuccess(importResult) || isOperationSuccess(response)) {
      ElMessage.success(importResult?.message || response?.message || '导入成功')
      dialogVisible.import = false
      uploadFile.value = null
      uploadRef.value?.clearFiles()
      await getDataList()
    } else {
      ElMessage.error(importResult?.message || response?.message || '导入失败')
    }
  } catch (error) {
    ElMessage.closeAll()
    console.error('导入GPKG文件失败:', error)
    ElMessage.error('导入失败')
  } finally {
    loading.import = false
  }
}

// 处理Excel文件导入
const handleExcelImport = async () => {
  const file = uploadFile.value
  if (!file) {
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
      const normalizedOrgCode = normalizeOrgCode(selectedOrg.value?.code)
      response = await surveyDataApi.importData(file, searchForm.year, normalizedOrgCode || undefined)
    } else if (dataType.value === 'community') {
      // 导入社区数据
      response = await communityCapacityApi.importData(file, searchForm.year)
    } else if (dataType.value === 'medical') {
      // 导入医疗卫生机构数据
      response = await medicalInstitutionApi.importData(file, searchForm.year)
    }

    const importResult = getImportResultPayload(response)
    if (isOperationSuccess(importResult) || isOperationSuccess(response)) {
      if (importResult.warnings && importResult.warnings.length > 0) {
        const warningTitle = `导入完成！共处理${importResult.totalCount || 0}条数据，新增${importResult.insertCount || 0}条，更新${importResult.updateCount || 0}条。\n\n以下数据的机构地址未能完整解析省市区街道社区信息：`
        const warningMessage = importResult.warnings.slice(0, 10).join('\n') +
          (importResult.warnings.length > 10 ? `\n... 等 ${importResult.warnings.length} 条警告` : '')

        ElMessageBox.alert(warningMessage, warningTitle, {
          confirmButtonText: '确定',
          type: 'warning',
          dangerouslyUseHTMLString: false
        })
      } else {
        ElMessage.success(importResult.message || '导入成功')
      }
      dialogVisible.import = false
      getDataList()
    } else {
      if (importResult?.errors && importResult.errors.length > 0) {
        const errorTitle = '导入失败：地址验证未通过\n\n请确保以下信息在组织机构管理中已维护：'
        const errorMessage = importResult.errors.slice(0, 10).join('\n') +
          (importResult.errors.length > 10 ? `\n... 等 ${importResult.errors.length} 条错误` : '')

        ElMessageBox.alert(errorMessage, errorTitle, {
          confirmButtonText: '确定',
          type: 'error',
          dangerouslyUseHTMLString: false
        })
      } else {
        ElMessage.error(importResult?.message || '导入失败')
      }
    }
  } catch (error) {
    console.error('导入失败:', error)
    if (!isRequestTimeoutError(error)) {
      ElMessage.error('导入失败')
    }
  } finally {
    loading.import = false
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await medicalInstitutionApi.downloadTemplate()

    // 从响应中获取数据
    const blobData = response.data || response

    // 创建blob对象
    const blob = new Blob([blobData], {
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
      // 医疗卫生机构数据导出 - 返回 {data, headers} 格式
      const blobData = response.data || response
      blob = blobData instanceof Blob ? blobData : new Blob([blobData], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
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

// 获取数据类型名称
const getDataTypeName = (dataType: string): string => {
  switch (dataType) {
    case 'township':
      return '乡镇评估数据'
    case 'community':
      return '社区减灾能力数据'
    case 'medical':
      return '医疗卫生机构数据'
    default:
      return '未知数据类型'
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
