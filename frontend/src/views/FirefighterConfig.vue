<template>
  <div class="firefighter-config">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>消防员配置</h1>
      <p>配置各个乡镇的消防员信息</p>
    </div>

    <!-- 主容器 -->
    <div class="layout-container">
      <!-- 左侧：组织机构树 -->
      <el-card class="org-tree-panel">
        <template #header>
          <div class="card-header">
            <div class="card-header-left">
              <span>组织机构</span>
              <el-select
                v-model="selectedYear"
                size="small"
                placeholder="年份"
                class="org-year-select"
                @change="handleYearChange"
              >
                <el-option v-for="year in yearOptions" :key="year" :label="`${year}年`" :value="year" />
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

      <!-- 右侧：消防员配置列表 -->
      <div class="config-panel">
        <!-- 当前选中组织机构信息 -->
        <el-card v-if="selectedOrg" class="selected-org-info">
          <div class="org-info-content">
            <el-tag type="primary" size="large">{{ selectedOrg.name }}</el-tag>
            <span class="org-info-code">组织机构代码: {{ selectedOrg.code }}</span>
          </div>
        </el-card>

        <!-- 工具栏 -->
        <el-card class="toolbar-card">
          <el-row :gutter="20" justify="space-between">
            <el-col :span="12">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索乡镇名称"
                clearable
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="12">
              <div class="toolbar-actions">
                <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
                  <el-icon><Delete /></el-icon>
                  批量删除 ({{ selectedRows.length }})
                </el-button>
                <el-button type="primary" @click="showAddDialog">
                  <el-icon><Plus /></el-icon>
                  新增配置
                </el-button>
                <el-button type="success" @click="refreshConfigs">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 配置列表 -->
        <el-card class="config-list">
          <el-table
            v-loading="loading.configs"
            :data="configList"
            stripe
            border
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="provinceName" label="省份" width="120" />
            <el-table-column prop="cityName" label="市" width="120" />
            <el-table-column prop="countyName" label="县" width="120" />
            <el-table-column prop="townshipName" label="乡镇" width="150" />
            <el-table-column prop="regionCode" label="区划代码" width="130" />
            <el-table-column prop="firefighterCount" label="消防员数量(人)" width="140" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="180" />
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
      :title="isEdit ? '编辑消防员配置' : '新增消防员配置'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="所属乡镇" prop="provinceName">
          <el-cascader
            :key="cascaderKey"
            v-model="formData.regionCode"
            :options="grassrootsOrgTree"
            :props="cascaderProps"
            placeholder="请选择省-市-县-乡镇"
            clearable
            filterable
            style="width: 100%"
            @change="handleCascaderChange"
          />
        </el-form-item>
        <el-form-item label="区划代码" prop="regionCode">
          <el-input v-model="formData.regionCode" placeholder="自动填充，也可手动输入" disabled />
        </el-form-item>
        <el-form-item label="消防员数量" prop="firefighterCount">
          <el-input-number
            v-model="formData.firefighterCount"
            :min="0"
            placeholder="请输入消防员数量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.form = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading.submit">
          确定
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
  Delete,
  Edit,
  Refresh
} from '@element-plus/icons-vue'
import { firefighterConfigApi, organizationApi, grassrootsOrganizationApi } from '@/api'
import { useGlobalYearStore } from '@/stores/globalYear'
import { useGlobalOrganizationStore } from '@/stores/globalOrganization'
import { useUserStore } from '@/stores/user'

// 全局年份 store
const globalYearStore = useGlobalYearStore()
// 全局组织机构 store
const globalOrganizationStore = useGlobalOrganizationStore()
// 用户 store
const userStore = useUserStore()

// 响应式数据
const selectedYear = ref<number>(new Date().getFullYear())
const yearOptions = ref<number[]>([])
const organizationList = ref<any[]>([])
const selectedOrg = ref<any>(null)
const orgTreeRef = ref()
const orgTreeRenderKey = computed(() => `orgTree-${selectedYear.value}`)
const defaultExpandedKeys = ref<string[]>([])

const searchKeyword = ref('')
const configList = ref<any[]>([])
const selectedRows = ref<any[]>([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const loading = reactive({
  organizations: false,
  configs: false,
  submit: false
})

const dialogVisible = reactive({
  form: false
})

const isEdit = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  id: null,
  provinceName: '',
  cityName: '',
  countyName: '',
  townshipName: '',
  regionCode: '',
  firefighterCount: 0,
  status: 1,
  remark: ''
})

// 级联选择器相关数据
const cascaderProps = {
  value: 'code',
  label: 'name',
  children: 'children',
  emitPath: false,
  checkStrictly: false
}

const cascaderKey = ref<string>('')

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

const formRules = {
  provinceName: [{ required: true, message: '请选择乡镇', trigger: 'change' }],
  firefighterCount: [{ required: true, message: '请输入消防员数量', trigger: 'blur' }]
}

// 生成年份选项
const generateYearOptions = () => {
  const currentYear = new Date().getFullYear()
  const startYear = 2020
  const years: number[] = []
  for (let year = currentYear; year >= startYear; year--) {
    years.push(year)
  }
  yearOptions.value = years
}

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

// 根据code查找组织节点
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

// 获取组织机构列表
const getOrganizationList = async () => {
  loading.organizations = true
  try {
    const response = await organizationApi.getTree({ year: selectedYear.value })
    if (response.success && response.data) {
      organizationList.value = response.data || []
      defaultExpandedKeys.value = collectExpandedKeys(organizationList.value)
      console.log('组织机构树形数据 (年份:', selectedYear.value, '):', organizationList.value)

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
        targetOrg = organizationList.value[0]
        console.log('默认选中第一个组织机构:', targetOrg)
      }

      if (targetOrg) {
        selectedOrg.value = targetOrg
        await nextTick()
        orgTreeRef.value?.setCurrentKey(targetOrg.code)
        // 同步到全局 store
        globalOrganizationStore.setOrganization({
          code: targetOrg.code,
          name: targetOrg.name,
          level: targetOrg.level
        })
        getConfigList()
      }
    }
  } catch (error) {
    console.error('获取组织机构列表失败:', error)
    ElMessage.error('获取组织机构列表失败')
  } finally {
    loading.organizations = false
  }
}

// 刷新组织机构
const refreshOrganizations = () => {
  getOrganizationList()
}

// 年份变化
const handleYearChange = () => {
  globalYearStore.setYear(selectedYear.value)
  selectedOrg.value = null
  configList.value = []
  getOrganizationList()
}

// 组织机构节点点击
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
  searchKeyword.value = ''
  pagination.currentPage = 1
  getConfigList()
}

// 获取配置列表
const getConfigList = async () => {
  loading.configs = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    if (selectedOrg.value) {
      // 根据组织机构代码前缀过滤（更精确）
      const orgCode = String(selectedOrg.value.code)
      if (selectedOrg.value.level === 1) {
        // 省级 - 用6位省级代码前缀匹配
        params.regionCode = orgCode.substring(0, 6)
      } else if (selectedOrg.value.level === 2) {
        // 市级 - 用6位市级代码前缀匹配
        params.regionCode = orgCode.substring(0, 6)
      } else if (selectedOrg.value.level === 3) {
        // 县级 - 用6位县级代码前缀匹配
        params.regionCode = orgCode.substring(0, 6)
      } else if (selectedOrg.value.level === 4) {
        // 乡镇级 - 用9位乡镇代码精确匹配或前缀匹配
        params.regionCode = orgCode.substring(0, 9)
      }
    }

    const response = await firefighterConfigApi.getList(params)
    if (response.success && response.data) {
      configList.value = response.data.list || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取配置列表失败')
    }
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.configs = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  getConfigList()
}

// 刷新配置
const refreshConfigs = () => {
  getConfigList()
}

// 基层组织树数据（用于表单级联选择器）
const grassrootsOrgTree = ref<any[]>([])

// 获取基层组织树（根据左侧选中的组织机构过滤）
const getGrassrootsOrgTree = async () => {
  try {
    let response
    if (!selectedOrg.value) {
      // 没有选中组织，加载全部
      response = await organizationApi.getTree({ year: selectedYear.value })
    } else {
      const org = selectedOrg.value
      if (org.level === 1) {
        // 省级 - 加载该省下的所有市县乡镇
        response = await grassrootsOrganizationApi.getTreeByCountyCode(org.code, selectedYear.value)
      } else if (org.level === 2) {
        // 市级 - 加载该市下的所有县乡镇
        response = await grassrootsOrganizationApi.getTreeByCountyCode(org.code, selectedYear.value)
      } else if (org.level === 3) {
        // 县级 - 加载该县下的所有乡镇
        response = await grassrootsOrganizationApi.getTreeByCountyId(org.id, selectedYear.value)
      } else {
        // 乡镇级 - 只显示该乡镇
        response = await grassrootsOrganizationApi.getTreeByCountyId(org.parentId, selectedYear.value)
      }
    }

    if (response.success && response.data) {
      grassrootsOrgTree.value = response.data || []
    }
  } catch (error) {
    console.error('获取基层组织树失败:', error)
  }
}

// 级联选择器变化处理
const handleCascaderChange = (value: string) => {
  console.log('级联选择器选中值:', value)
  if (!value) {
    formData.regionCode = ''
    formData.provinceName = ''
    formData.cityName = ''
    formData.countyName = ''
    formData.townshipName = ''
    return
  }

  // 根据选中的code查找对应的组织节点
  const findOrgByCode = (nodes: any[], code: string): any => {
    for (const node of nodes) {
      if (node.code === code) return node
      if (node.children?.length > 0) {
        const found = findOrgByCode(node.children, code)
        if (found) return found
      }
    }
    return null
  }

  const selectedOrg = findOrgByCode(grassrootsOrgTree.value, value)
  if (selectedOrg) {
    formData.regionCode = selectedOrg.code
    formData.provinceName = selectedOrg.provinceName || ''
    formData.cityName = selectedOrg.cityName || ''
    formData.countyName = selectedOrg.countyName || ''
    formData.townshipName = selectedOrg.name || ''
  }
}

// 显示新增对话框
const showAddDialog = async () => {
  isEdit.value = false
  resetForm()
  // 加载基层组织树
  await getGrassrootsOrgTree()
  // 更新级联选择器key以触发重新渲染
  cascaderKey.value = Date.now().toString()
  // 如果选中了组织机构，自动填充部分信息
  if (selectedOrg.value) {
    const org = selectedOrg.value
    formData.provinceName = org.provinceName || ''
    formData.cityName = org.cityName || ''
    formData.countyName = org.countyName || ''
    if (org.level === 4) {
      formData.townshipName = org.name
      formData.regionCode = org.code
    }
  }
  dialogVisible.form = true
}

// 显示编辑对话框
const showEditDialog = async (row: any) => {
  isEdit.value = true
  // 加载基层组织树
  await getGrassrootsOrgTree()
  // 更新级联选择器key
  cascaderKey.value = Date.now().toString()
  Object.assign(formData, {
    id: row.id,
    provinceName: row.provinceName,
    cityName: row.cityName,
    countyName: row.countyName,
    townshipName: row.townshipName,
    regionCode: row.regionCode,
    firefighterCount: row.firefighterCount,
    status: row.status,
    remark: row.remark
  })
  dialogVisible.form = true
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    provinceName: '',
    cityName: '',
    countyName: '',
    townshipName: '',
    regionCode: '',
    firefighterCount: 0,
    status: 1,
    remark: ''
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
        response = await firefighterConfigApi.update(formData)
      } else {
        response = await firefighterConfigApi.create(formData)
      }

      if (response.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.form = false
        getConfigList()
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

// 删除配置
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await firefighterConfigApi.delete(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      getConfigList()
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

// 批量删除
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
        type: 'warning'
      }
    )

    const ids = selectedRows.value.map(row => row.id)
    const response = await firefighterConfigApi.batchDelete(ids)

    if (response.success) {
      ElMessage.success(`成功删除 ${selectedRows.value.length} 条数据`)
      selectedRows.value = []
      getConfigList()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
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
  getConfigList()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  getConfigList()
}

// 组件挂载
onMounted(async () => {
  generateYearOptions()
  // 从全局store获取年份
  if (globalYearStore.selectedYear) {
    selectedYear.value = globalYearStore.selectedYear
  }
  await getOrganizationList()
  await getConfigList()
})
</script>

<style scoped>
.firefighter-config {
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

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.org-year-select {
  width: 100px;
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

/* 右侧配置面板 */
.config-panel {
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

.toolbar-card {
  margin-bottom: 0;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.config-list {
  min-height: 400px;
  flex: 1;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
