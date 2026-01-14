<template>
  <div class="organization-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>组织机构管理</h1>
      <p>组织机构的创建、配置和管理</p>
    </div>

    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span>组织机构树</span>
          <div class="header-actions">
            <el-select
              v-model="treeYear"
              size="small"
              clearable
              placeholder="评估年份"
              class="org-year-select"
              @change="loadOrganizationTree"
            >
              <el-option
                v-for="year in yearOptions"
                :key="year"
                :label="year + '年'"
                :value="year"
              />
            </el-select>
            <el-button type="warning" :disabled="!treeYear" @click="copyFromPreviousYear">
              从上一年复制
            </el-button>
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              accept=".xlsx,.xls"
            >
              <el-button type="success">
                <el-icon><Upload /></el-icon>
                导入Excel
              </el-button>
            </el-upload>
            <el-button type="primary" @click="showAddRootDialog">
              <el-icon><Plus /></el-icon>
              添加根节点
            </el-button>
          </div>
        </div>
      </template>

      <!-- 组织机构树 -->
      <div class="tree-container" v-loading="loading">
        <el-tree
          :key="orgTreeRenderKey"
          ref="orgTreeRef"
          :data="organizationTree"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          highlight-current
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <div class="tree-node">
              <div class="node-info">
                <el-icon v-if="data.level === 1" color="#409eff"><OfficeBuilding /></el-icon>
                <el-icon v-else-if="data.level === 2" color="#67c23a"><MapLocation /></el-icon>
                <el-icon v-else-if="data.level === 3" color="#e6a23c"><Location /></el-icon>
                <el-icon v-else-if="data.level === 4" color="#f56c6c"><Position /></el-icon>
                <el-icon v-else color="#909399"><House /></el-icon>
                <span class="node-name">{{ data.name }}</span>
                <el-tag size="small" :type="getLevelTagType(data.level)" class="level-tag">
                  {{ getLevelName(data.level) }}
                </el-tag>
                <span class="node-code">{{ data.code }}</span>
              </div>
              <div class="node-actions">
                <el-button size="small" type="primary" @click.stop="showAddChildDialog(data)">
                  <el-icon><Plus /></el-icon>
                  添加子项
                </el-button>
                <el-button size="small" type="warning" @click.stop="editNode(data)">
                  <el-icon><Edit /></el-icon>
                  修改
                </el-button>
                <el-button size="small" type="info" @click.stop="showBoundaryDialog(data)" v-if="data.level === 2">
                  <el-icon><Location /></el-icon>
                  边界
                </el-button>
                <el-button size="small" type="danger" @click.stop="deleteNode(data)" v-if="treeYear">
                  <el-icon><Delete /></el-icon>
                  删除组织及子组织
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>

    <!-- 创建/编辑组织机构对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '添加组织机构' : '修改组织机构'"
      width="600px"
    >
      <el-form :model="currentOrg" label-width="100px">
        <el-form-item label="机构名称" required>
          <el-input v-model="currentOrg.name" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="机构编码" required>
          <el-input v-model="currentOrg.code" placeholder="请输入机构编码" :disabled="dialogMode === 'edit'" />
          <div class="form-hint" v-if="dialogMode === 'edit'">编码不可修改</div>
        </el-form-item>
        <el-form-item label="上级机构">
          <el-input v-model="parentName" disabled />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="currentOrg.sortOrder" :min="1" :max="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="currentOrg.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOrg" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 边界管理对话框 -->
    <el-dialog
      v-model="boundaryDialogVisible"
      title="边界范围配置"
      width="700px"
    >
      <el-form :model="boundaryForm" label-width="120px">
        <el-form-item label="组织机构">
          <el-input v-model="boundaryOrgName" disabled />
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="boundaryForm.year" placeholder="请选择年份" @change="loadBoundaryData">
            <el-option
              v-for="year in yearOptions"
              :key="year"
              :label="year + '年'"
              :value="year"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="边界坐标">
          <el-input
            v-model="boundaryForm.boundaryCoordinates"
            type="textarea"
            :rows="10"
            placeholder="请输入GeoJSON格式的边界坐标数据"
          />
        </el-form-item>
        <el-form-item label="边界文件">
          <div style="display: flex; width: 100%; gap: 10px;">
            <el-input v-model="boundaryForm.filePath" placeholder="请输入文件路径或URL" style="flex: 1;" />
            <el-upload
              action="/api/organization/boundary/upload"
              :show-file-list="false"
              :on-success="handleBoundaryUploadSuccess"
              :before-upload="beforeBoundaryUpload"
              :headers="uploadHeaders"
              accept=".json"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                上传
              </el-button>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="boundaryDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveBoundary" :loading="boundarySaving">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps } from 'element-plus'
import { Plus, Edit, Delete, Upload, OfficeBuilding, MapLocation, Location, Position, House } from '@element-plus/icons-vue'
import type { ElTree } from 'element-plus'
import request from '@/utils/request'

// 状态管理
const loading = ref(false)
const saving = ref(false)
const organizationTree = ref<any[]>([])
const orgTreeRef = ref()
const uploadRef = ref()

// 边界管理状态
const boundaryDialogVisible = ref(false)
const boundarySaving = ref(false)
const boundaryOrgName = ref('')
const currentBoundaryNode = ref<any>(null)
const boundaryForm = ref({
  id: null,
  organizationId: null,
  year: new Date().getFullYear(),
  boundaryCoordinates: '',
  filePath: ''
})

const treeYear = ref<number | null>(new Date().getFullYear())
const orgTreeRenderKey = computed(() => `orgTree-${treeYear.value ?? 'all'}`)

// 上传相关
const uploadHeaders = computed(() => {
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  return {
    'X-Current-User': user ? user.username : 'admin'
  }
})

const beforeBoundaryUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'application/json' && !rawFile.name.endsWith('.json')) {
    ElMessage.error('请上传 JSON 格式的文件')
    return false
  }
  return true
}

const handleBoundaryUploadSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  if (response.success) {
    boundaryForm.value.filePath = response.data
    ElMessage.success('文件上传成功')
    
    // 读取文件内容并更新边界坐标
    if (uploadFile.raw) {
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const content = e.target?.result as string
          const json = JSON.parse(content)
          boundaryForm.value.boundaryCoordinates = JSON.stringify(json)
          ElMessage.success('边界坐标数据已根据上传文件自动更新')
        } catch (err) {
          console.error('JSON解析失败', err)
          ElMessage.warning('上传的文件内容不是有效的JSON格式，未能更新边界坐标')
        }
      }
      reader.readAsText(uploadFile.raw)
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const isJsonResponse = (res: Response) => {
  const contentType = (res.headers.get('content-type') || '').toLowerCase()
  return contentType.includes('json')
}

const resolveCityBoundaryPath = async (cityName: string, year: number) => {
  const yearPath = `/boundaries/${year}/city/${cityName}.json`
  try {
    const res = await fetch(yearPath)
    if (res.ok && isJsonResponse(res)) return yearPath
  } catch { }

  const commonPath = `/boundaries/city/${cityName}.json`
  try {
    const res = await fetch(commonPath)
    if (res.ok && isJsonResponse(res)) return commonPath
  } catch { }

  return ''
}

// 生成年份列表（从2020年开始）
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let year = currentYear; year >= 2020; year--) {
    years.push(year)
  }
  return years
})

// 显示边界管理对话框
const showBoundaryDialog = async (data: any) => {
  boundaryOrgName.value = data.name
  currentBoundaryNode.value = data
  boundaryForm.value = {
    id: null,
    organizationId: data.id,
    year: new Date().getFullYear(),
    boundaryCoordinates: '',
    filePath: ''
  }
  boundaryDialogVisible.value = true
  await loadBoundaryData()
}

// 加载边界数据
const loadBoundaryData = async () => {
  if (!boundaryForm.value.organizationId || !boundaryForm.value.year) return
  
  try {
    const response = await request.get(`/api/organization/boundary/${boundaryForm.value.organizationId}/${boundaryForm.value.year}`)
    if (response.success && response.data) {
      boundaryForm.value = {
        ...response.data,
        year: boundaryForm.value.year // 保持当前选择的年份
      }

      const node = currentBoundaryNode.value
      if (!boundaryForm.value.filePath) {
        if (node && node.level === 2) {
          const foundPath = await resolveCityBoundaryPath(node.name, boundaryForm.value.year as number)
          if (foundPath) {
            boundaryForm.value.filePath = foundPath
          }
        } else if (node && node.level === 3) {
          boundaryForm.value.filePath = '/region_boundaries.json'
        }
      }
    } else {
      // 如果没有数据，重置表单（除了orgId和year）
      boundaryForm.value.id = null
      boundaryForm.value.boundaryCoordinates = ''
      boundaryForm.value.filePath = ''

      // 尝试加载默认的本地边界数据
      try {
        let coordinates = ''
        let foundPath = ''
        const node = currentBoundaryNode.value
        
        if (node && node.level === 2) { // 市级
          const path = await resolveCityBoundaryPath(node.name, boundaryForm.value.year as number)
          if (path) {
            try {
              const res = await fetch(path)
              if (res.ok && isJsonResponse(res)) {
                const json = await res.json()
                coordinates = JSON.stringify(json)
                foundPath = path
              }
            } catch { }
          }
        } else if (node && node.level === 3) { // 县级
          if (!(window as any).regionBoundariesCache) {
            const res = await fetch('/region_boundaries.json')
            if (res.ok) {
              (window as any).regionBoundariesCache = await res.json()
            }
          }
          if ((window as any).regionBoundariesCache) {
            const feature = (window as any).regionBoundariesCache.features.find((f: any) => f.properties.name === node.name)
            if (feature) {
              coordinates = JSON.stringify(feature.geometry)
              foundPath = '/region_boundaries.json' // 县级统一文件
            }
          }
        }
        
        if (coordinates) {
          boundaryForm.value.boundaryCoordinates = coordinates
          if (foundPath) {
            boundaryForm.value.filePath = foundPath
          }
          ElMessage.info('已自动加载本地默认边界数据')
        }
      } catch (e) {
      }
    }
  } catch (error) {
    // 不提示错误，因为可能就是没有数据
    boundaryForm.value.id = null
    boundaryForm.value.boundaryCoordinates = ''
    boundaryForm.value.filePath = ''
  }
}

// 保存边界配置
const saveBoundary = async () => {
  boundarySaving.value = true
  try {
    const response = await request.post('/api/organization/boundary/save', boundaryForm.value)
    if (response.success) {
      ElMessage.success('保存成功')
      // 刷新数据以获取可能的ID更新
      await loadBoundaryData()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    boundarySaving.value = false
  }
}

// 对话框控制
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const selectedNode = ref<any>(null)

// 表单数据
const currentOrg = ref<any>({
  name: '',
  code: '',
  parentId: null,
  level: 1,
  sortOrder: 1,
  remark: ''
})

const parentName = computed(() => {
  if (selectedNode.value) {
    return selectedNode.value.name || '根节点'
  }
  return '根节点'
})

// 获取级别名称
const getLevelName = (level: number) => {
  const names: Record<number, string> = {
    1: '省级',
    2: '市级',
    3: '县级',
    4: '乡镇级',
    5: '社区级'
  }
  return names[level] || '未知'
}

// 获取级别标签类型
const getLevelTagType = (level: number) => {
  const types: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: 'info'
  }
  return types[level] || ''
}

const findNodeById = (nodes: any[], id: any): any | null => {
  for (const node of nodes || []) {
    if (String(node?.id) === String(id)) return node
    if (node?.children?.length) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 加载组织机构树
const loadOrganizationTree = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/organization/tree', {
      params: { year: treeYear.value || undefined }
    })
    if (response.success) {
      // 后端已返回构建好的树形结构，直接使用
      organizationTree.value = response.data || []

      if (selectedNode.value?.id != null) {
        const exists = findNodeById(organizationTree.value, selectedNode.value.id)
        if (!exists) {
          selectedNode.value = null
        }
      }

      // 数据加载完成后，展开到县级节点
      await nextTick()
      expandToLevel(3)
    }
  } catch (error: any) {
    ElMessage.error('加载组织机构树失败: ' + (error.message || ''))
  } finally {
    loading.value = false
  }
}

// 递归展开到指定层级
const expandToLevel = (level: number) => {
  if (!orgTreeRef.value) return

  // 收集所有需要展开的节点ID（level <= 指定层级）
  const expandKeys: any[] = []
  const collectKeys = (nodes: any[], currentLevel: number) => {
    for (const node of nodes) {
      if (node.level < level && node.children && node.children.length > 0) {
        expandKeys.push(node.id)
        collectKeys(node.children, currentLevel + 1)
      }
    }
  }
  collectKeys(organizationTree.value, 1)

  // 展开节点
  expandKeys.forEach(key => {
    orgTreeRef.value.store.nodesMap[key]?.expand()
  })
}

// 处理节点点击
const handleNodeClick = (data: any) => {
  selectedNode.value = data
}

// 显示添加根节点对话框
const showAddRootDialog = () => {
  dialogMode.value = 'create'
  currentOrg.value = {
    name: '',
    code: '',
    parentId: null,
    level: 1,
    sortOrder: 1,
    remark: ''
  }
  selectedNode.value = null
  dialogVisible.value = true
}

// 显示添加子项对话框
const showAddChildDialog = (parent: any) => {
  dialogMode.value = 'create'
  selectedNode.value = parent
  currentOrg.value = {
    name: '',
    code: '',
    parentId: parent.id,
    level: (parent.level || 0) + 1,
    sortOrder: 1,
    remark: ''
  }
  dialogVisible.value = true
}

// 编辑节点
const editNode = (node: any) => {
  dialogMode.value = 'edit'
  selectedNode.value = node
  currentOrg.value = {
    id: node.id,
    name: node.name,
    code: node.code,
    parentId: node.parentId,
    level: node.level,
    sortOrder: node.sortOrder || 1,
    remark: node.remark || ''
  }
  dialogVisible.value = true
}

// 保存组织机构
const saveOrg = async () => {
  if (!currentOrg.value.name || !currentOrg.value.code) {
    ElMessage.warning('请填写必填项')
    return
  }

  saving.value = true
  try {
    const url = dialogMode.value === 'create'
      ? '/api/organization'
      : `/api/organization`

    const method = dialogMode.value === 'create' ? 'post' : 'put'

    const response = await request[method](url, currentOrg.value)

    if (response.success) {
      ElMessage.success(dialogMode.value === 'create' ? '创建成功' : '更新成功')
      dialogVisible.value = false
      await loadOrganizationTree()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.message || ''))
  } finally {
    saving.value = false
  }
}

// 删除节点（删除当前年份的组织及子组织）
const deleteNode = (node: any) => {
  if (!treeYear.value) {
    ElMessage.warning('请先选择年份')
    return
  }

  ElMessageBox.confirm(
    `确定要删除组织机构 "${node.name}" 及其所有子组织在 ${treeYear.value} 年的数据吗？\n\n此操作将删除：\n- 组织机构记录\n- 边界配置\n\n此操作不可恢复！`,
    '确认删除',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const response = await request.delete(`/api/organization/${node.id}/data`, {
        params: { year: treeYear.value }
      })
      if (response.success) {
        const data = response.data
        ElMessage.success(`删除成功！共删除 ${data.totalDeleted || 0} 条记录（组织记录 ${data.organizationDeleted || 0} 条，边界配置 ${data.boundaryDeleted || 0} 条）`)
        await loadOrganizationTree()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error: any) {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  })
}

// 删除年度数据（已废弃，合并到deleteNode）
const deleteYearData = (node: any) => {
  deleteNode(node)
}

// 处理Excel文件选择
const handleFileChange: UploadProps['onChange'] = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  // 检查是否选择了年份
  if (!treeYear.value) {
    ElMessage.warning('请先选择要导入到的年份')
    return
  }

  // 检查文件类型
  const validTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请选择Excel文件(.xlsx或.xls)')
    return
  }

  // 创建FormData
  const formData = new FormData()
  formData.append('file', file)
  formData.append('year', String(treeYear.value))

  uploading.value = true
  try {
    const response = await request.post('/api/organization/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      params: { year: treeYear.value }
    })

    if (response.success) {
      ElMessage.success(`导入成功，共导入 ${response.data?.count || 0} 条记录到 ${treeYear.value} 年`)
      await loadOrganizationTree()
    } else {
      ElMessage.error(response.message || '导入失败')
    }
  } catch (error: any) {
    ElMessage.error('导入失败: ' + (error.message || ''))
  } finally {
    uploading.value = false
    // 清空文件选择
    uploadRef.value?.clearFiles()
  }
}

const uploading = ref(false)

const copyFromPreviousYear = async () => {
  if (!treeYear.value) {
    ElMessage.warning('请选择目标年份')
    return
  }
  const targetYear = treeYear.value
  const sourceYear = targetYear - 1
  try {
    await ElMessageBox.confirm(`将把 ${sourceYear} 年的边界配置复制到 ${targetYear} 年，是否继续？`, '确认复制', {
      type: 'warning'
    })
    const response = await request.post('/api/organization/copy-from-previous-year', null, {
      params: { targetYear }
    })
    if (response.success) {
      const count = response.data?.count ?? 0
      ElMessage.success(`复制完成：新增 ${count} 条年度配置`)
      await loadOrganizationTree()
    } else {
      ElMessage.error(response.message || '复制失败')
    }
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error('复制失败')
  }
}

// 初始化
onMounted(() => {
  loadOrganizationTree()
})

watch(
  () => treeYear.value,
  async (year, oldYear) => {
    if (year === oldYear) return
    selectedNode.value = null
    await loadOrganizationTree()
  }
)
</script>

<style scoped>
.organization-management {
  margin: 0 auto;
  max-width: 1920px;
  height: 100%;
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

.header-card {
  margin-bottom: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.org-year-select {
  width: 120px;
}

.tree-container {
  min-height: 400px;
  max-height: calc(100vh - 280px);
  padding: 20px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.tree-node:hover {
  background-color: #f5f7fa;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.node-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.level-tag {
  margin-left: 8px;
}

.node-code {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-tree-node__content) {
  height: auto !important;
  padding: 4px 0;
}

:deep(.el-table) {
  font-size: 14px;
}
</style>
