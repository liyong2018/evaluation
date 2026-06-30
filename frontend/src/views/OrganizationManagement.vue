<template>
  <div class="organization-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>组织机构管理</h1>
      <p>组织机构的创建、配置和管理</p>
    </div>

    <!-- 主操作栏 -->
    <el-card class="action-card">
      <div class="action-bar">
        <el-select
          v-model="treeYear"
          size="default"
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
      </div>
    </el-card>

    <!-- 左右分栏布局 -->
    <div class="content-container">
      <!-- 左侧：组织机构树（省市县） -->
      <el-card class="tree-card">
        <template #header>
          <span>省市区组织机构</span>
        </template>
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
              <div class="tree-node" :class="{ 'is-county': data.level === 3, 'is-deleted': data.isDeleted }">
                <div class="node-info">
                  <el-icon v-if="data.level === 1" color="#409eff"><OfficeBuilding /></el-icon>
                  <el-icon v-else-if="data.level === 2" color="#67c23a"><MapLocation /></el-icon>
                  <el-icon v-else-if="data.level === 3" color="#e6a23c"><Location /></el-icon>
                  <el-icon v-else-if="data.level === 4" color="#f56c6c"><Position /></el-icon>
                  <el-icon v-else color="#909399"><House /></el-icon>
                  <span class="node-name">{{ data.name }}</span>
                  <!-- 数据来源年份标识 -->
                  <el-tag v-if="data.sourceYear !== undefined && data.sourceYear !== null"
                           size="small"
                           :type="getSourceYearTagType(data.sourceYear, treeYear)"
                           class="source-year-tag">
                    {{ getSourceYearLabel(data.sourceYear, treeYear) }}
                  </el-tag>
                  <el-tag v-if="data.changeType"
                           size="small"
                           :type="getChangeTypeTagType(data.changeType)"
                           class="change-type-tag"
                           :title="getChangeTypeTitle(data)">
                    {{ getChangeTypeLabel(data.changeType) }}
                  </el-tag>
                  <el-tag size="small" :type="getLevelTagType(data.level)" class="level-tag">
                    {{ getLevelName(data.level) }}
                  </el-tag>
                  <span class="node-code" v-if="getChildCount(data) > 0">（{{ getChildCount(data) }}）</span>
                </div>
                <div class="node-actions">
                  <el-button size="small" type="primary" @click.stop="showAddChildDialog(data)" v-if="data.level < 3">
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

      <!-- 右侧：子级组织列表 -->
      <el-card class="list-card">
        <template #header>
          <div class="list-header">
            <span>{{ rightPanelTitle }}</span>
            <el-tag v-if="selectedOrganization" type="info">{{ rightPanelCount }} 个组织</el-tag>
          </div>
        </template>
        <div class="grassroots-content" v-loading="rightPanelLoading">
          <div v-if="!selectedOrganization" class="empty-state">
            <el-icon class="empty-icon"><Location /></el-icon>
            <p>请在左侧选择一个组织机构查看下级</p>
          </div>
          <div v-else-if="rightPanelTree.length === 0" class="empty-state">
            <el-icon class="empty-icon"><FolderOpened /></el-icon>
            <p>该组织下暂无下级数据</p>
          </div>
          <el-tree
            v-else
            ref="rightPanelTreeRef"
            :data="rightPanelTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            :default-expand-all="false"
            :expand-on-click-node="false"
            class="grassroots-tree"
            @node-click="handleRightPanelNodeClick"
          >
            <template #default="{ data }">
              <div class="tree-node grassroots-node" :class="{ 'is-deleted': data.isDeleted }">
                <div class="node-info">
                  <el-icon v-if="data.level === 2" color="#67c23a"><MapLocation /></el-icon>
                  <el-icon v-else-if="data.level === 3" color="#e6a23c"><Location /></el-icon>
                  <el-icon v-else-if="data.level === 4" color="#f56c6c"><Position /></el-icon>
                  <el-icon v-else color="#909399"><House /></el-icon>
                  <span class="node-name">{{ data.name }}</span>
                  <!-- 数据来源年份标识 -->
                  <el-tag v-if="data.sourceYear !== undefined && data.sourceYear !== null"
                           size="small"
                           :type="getSourceYearTagType(data.sourceYear, treeYear)"
                           class="source-year-tag">
                    {{ getSourceYearLabel(data.sourceYear, treeYear) }}
                  </el-tag>
                  <el-tag v-if="data.changeType"
                           size="small"
                           :type="getChangeTypeTagType(data.changeType)"
                           class="change-type-tag"
                           :title="getChangeTypeTitle(data)">
                    {{ getChangeTypeLabel(data.changeType) }}
                  </el-tag>
                  <el-tag size="small" :type="getLevelTagType(data.level)" class="level-tag">
                    {{ getLevelName(data.level) }}
                  </el-tag>
                  <span class="node-code" v-if="getChildCount(data) > 0">（{{ getChildCount(data) }}）</span>
                </div>
                <div class="node-actions">
                  <!-- 省市区组织：显示编辑按钮，基层组织显示修改/删除 -->
                  <template v-if="data.level <= 3">
                    <el-button size="small" type="warning" @click.stop="editNode(data)">
                      <el-icon><Edit /></el-icon>
                      修改
                    </el-button>
                    <el-button size="small" type="danger" @click.stop="deleteNode(data)" v-if="treeYear && data.level > 1">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                  <template v-else>
                    <el-button size="small" type="warning" @click.stop="editGrassrootsNode(data)">
                      <el-icon><Edit /></el-icon>
                      修改
                    </el-button>
                    <el-button size="small" type="danger" @click.stop="deleteGrassrootsNode(data)" v-if="treeYear">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </div>
              </div>
            </template>
          </el-tree>
        </div>
      </el-card>
    </div>

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

    <!-- 基层组织编辑对话框 -->
    <el-dialog
      v-model="grassrootsDialogVisible"
      :title="grassrootsDialogMode === 'create' ? '添加基层组织' : '修改基层组织'"
      width="600px"
    >
      <el-form :model="currentGrassrootsOrg" label-width="100px">
        <el-form-item label="组织名称" required>
          <el-input v-model="currentGrassrootsOrg.name" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织编码" required>
          <el-input v-model="currentGrassrootsOrg.code" placeholder="请输入组织编码" :disabled="grassrootsDialogMode === 'edit'" />
          <div class="form-hint" v-if="grassrootsDialogMode === 'edit'">编码不可修改</div>
        </el-form-item>
        <el-form-item label="组织类型">
          <el-tag v-if="currentGrassrootsOrg.level === 4" type="danger">乡镇</el-tag>
          <el-tag v-else type="info">社区</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grassrootsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGrassrootsOrg" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps } from 'element-plus'
import { Plus, Edit, Delete, OfficeBuilding, MapLocation, Location, Position, House, FolderOpened } from '@element-plus/icons-vue'
import type { ElTree } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useGlobalYearStore } from '@/stores/globalYear'

// 用户状态
const userStore = useUserStore()
const globalYearStore = useGlobalYearStore()
const route = useRoute()

// 状态管理
const loading = ref(false)
const saving = ref(false)
const organizationTree = ref<any[]>([])
const orgTreeRef = ref()
const rightPanelTreeRef = ref()

// 根据用户角色确定树展开层级
// admin或省级用户：展开到市级（level=2，区县级折叠）
// 市级用户：展开到区县级（level=3，乡镇折叠）
// 区县用户：展开到乡镇级（level=4，社区折叠）
const treeExpandLevel = computed(() => {
  // admin用户默认展开到市级（省→市展开，区县折叠）
  if (userStore.isAdmin) {
    return 2
  }
  // 非admin用户：展开到区县级
  // TODO: 未来可以根据用户的组织机构级别来确定展开层级
  return 3
})

// 右侧面板状态（支持省/市/区的子级展示）
const rightPanelLoading = ref(false)
const rightPanelTree = ref<any[]>([])
const selectedOrganization = ref<any>(null)
const rightPanelTitle = computed(() => {
  if (!selectedOrganization.value) return '请选择组织机构'
  const level = selectedOrganization.value.level
  if (level === 1) return `${selectedOrganization.value.name} - 市州列表`
  if (level === 2) return `${selectedOrganization.value.name} - 区县列表`
  if (level === 3) return `${selectedOrganization.value.name} - 乡镇社区`
  return selectedOrganization.value.name
})
const rightPanelCount = computed(() => {
  const countNodes = (nodes: any[]): number => {
    if (!nodes || nodes.length === 0) return 0
    return nodes.reduce((sum, node) => sum + 1 + countNodes(node.children || []), 0)
  }
  return countNodes(rightPanelTree.value)
})

// 边界管理状态
const boundaryDialogVisible = ref(false)
const boundarySaving = ref(false)
const boundaryOrgName = ref('')
const currentBoundaryNode = ref<any>(null)
const boundaryForm = ref({
  id: null,
  organizationId: null,
  year: globalYearStore.selectedYear,
  boundaryCoordinates: '',
  filePath: ''
})

const treeYear = ref<number | null>(globalYearStore.selectedYear)
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
  level: 1
})

const parentName = computed(() => {
  // 编辑模式：根据当前编辑节点的级别查找父级名称
  if (dialogMode.value === 'edit' && currentOrg.value.level) {
    const level = currentOrg.value.level
    if (level === 2) {
      // 市级，父级是省级（从selectedOrganization获取，因为点击了省）
      if (selectedOrganization.value && selectedOrganization.value.level === 1) {
        return selectedOrganization.value.name
      }
      // 或从organizationTree中查找省级节点
      const province = organizationTree.value[0]
      return province?.name || '根节点'
    } else if (level === 3) {
      // 县级，父级是市级（从selectedOrganization获取，因为点击了市）
      if (selectedOrganization.value && selectedOrganization.value.level === 2) {
        return selectedOrganization.value.name
      }
      // 或从rightPanelTree中查找市级节点
      const city = rightPanelTree.value.find((n: any) => n.id === currentOrg.value.parentId)
      return city?.name || '未知市'
    }
  }
  // 创建模式：显示selectedNode或selectedOrganization的名称
  if (selectedNode.value) {
    return selectedNode.value.name || '根节点'
  }
  if (selectedOrganization.value) {
    return selectedOrganization.value.name || '根节点'
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

// 获取数据来源年份标签文本
const getSourceYearLabel = (sourceYear: number, currentYear: number | null) => {
  // 只显示年份的后两位，例如 2026 显示为 "26"
  return `${sourceYear % 100}`
}

// 获取数据来源年份标签类型
const getSourceYearTagType = (sourceYear: number, currentYear: number | null) => {
  if (!currentYear) {
    return 'info' // 无年份选择时显示为灰色
  }
  if (sourceYear === currentYear) {
    return 'success' // 本年数据显示为绿色
  }
  if (sourceYear < currentYear) {
    return 'warning' // 顺延数据显示为橙色
  }
  return 'info'
}

const getChangeTypeLabel = (changeType: string) => {
  return changeType
}

const getChangeTypeTitle = (node: any) => {
  if (!node?.changeType) return ''
  const parts = [node.changeType]
  if (node.oldName && node.oldName !== node.name) {
    parts.push(`名称：${node.oldName} -> ${node.name}`)
  }
  if (node.oldCode && node.oldCode !== node.code) {
    parts.push(`代码：${node.oldCode} -> ${node.code}`)
  }
  return parts.join('；')
}

const getChangeTypeTagType = (changeType: string) => {
  const types: Record<string, string> = {
    新增: 'success',
    删除: 'danger',
    修改名称: 'warning',
    修改代码: 'warning',
    修改上级: 'warning',
    下级有变更: 'info'
  }
  return types[changeType] || 'info'
}

const getChildCount = (node: any) => {
  return Array.isArray(node?.children) ? node.children.length : 0
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
      params: {
        year: treeYear.value || undefined,
        includeChangeDetails: true
      }
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

      // 数据加载完成后，根据用户角色展开到相应层级
      await nextTick()
      expandToLevel(treeExpandLevel.value)
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
const handleNodeClick = async (data: any) => {
  selectedNode.value = data
  selectedOrganization.value = data

  // 如果点击的是区县节点（level=3），加载乡镇数据
  if (data.level === 3) {
    await loadGrassrootsTree(data.id, data.code)
  } else {
    // 其他级别直接使用树中的children数据
    rightPanelTree.value = data.children || []
  }
}

// 处理右侧面板节点点击
const handleRightPanelNodeClick = async (data: any) => {
  // 如果点击的是乡镇节点（level=4），加载其下级的社区列表
  if (data.level === 4) {
    // 检查是否已经加载了子级数据
    if (!data.children || data.children.length === 0) {
      try {
        // 加载该乡镇下的社区列表
        const response = await request.get('/api/grassroots-organization/communities/by-township-id/' + data.id, {
          params: { year: treeYear.value || undefined }
        })
        if (response.success && response.data && response.data.length > 0) {
          // 设置子级数据
          data.children = response.data
          // 展开该节点
          const node = rightPanelTreeRef.value?.store?.nodesMap?.[data.id]
          if (node) {
            node.expand()
          }
        } else {
          ElMessage.info('该乡镇下暂无社区数据')
        }
      } catch (error: any) {
        console.error('加载社区列表失败:', error)
        ElMessage.error('加载社区列表失败')
      }
    } else {
      // 已有子级数据，切换展开状态
      const node = rightPanelTreeRef.value?.store?.nodesMap?.[data.id]
      if (node) {
        if (node.expanded) {
          node.collapse()
        } else {
          node.expand()
        }
      }
    }
  }
}

// 加载基层组织的树形数据
const loadGrassrootsTree = async (countyId: number, countyCode: string) => {
  rightPanelLoading.value = true
  try {
    const response = await request.get('/api/grassroots-organization/tree/by-county-id/' + countyId, {
      params: {
        year: treeYear.value || undefined,
        includeChangeDetails: true
      }
    })
    if (response.success) {
      rightPanelTree.value = response.data || []
    } else {
      rightPanelTree.value = []
      ElMessage.warning('加载乡镇社区数据失败')
    }
  } catch (error: any) {
    rightPanelTree.value = []
    console.error('加载基层组织失败:', error)
  } finally {
    rightPanelLoading.value = false
  }
}

// 显示添加子项对话框
const showAddChildDialog = (parent: any) => {
  dialogMode.value = 'create'
  selectedNode.value = parent
  currentOrg.value = {
    name: '',
    code: '',
    parentId: parent.id,
    level: (parent.level || 0) + 1
  }
  dialogVisible.value = true
}

// 编辑节点
const editNode = (node: any) => {
  dialogMode.value = 'edit'
  // 注意：不要设置selectedNode.value = node，因为selectedNode应该保持为左侧树选中的节点
  // 这样parentName才能正确显示父级名称
  currentOrg.value = {
    id: node.id,
    name: node.name,
    code: node.code,
    parentId: node.parentId,
    level: node.level
  }
  dialogVisible.value = true
}

// 保存组织机构
const saveOrg = async () => {
  if (!currentOrg.value.name || !currentOrg.value.code) {
    ElMessage.warning('请填写必填项')
    return
  }

  // 如果没有选择年份，不允许修改（防止误改基准数据）
  if (dialogMode.value === 'edit' && !treeYear.value) {
    ElMessage.warning('请先选择年份后再修改组织机构')
    return
  }

  saving.value = true
  try {
    const url = dialogMode.value === 'create'
      ? '/api/organization'
      : `/api/organization`

    const method = dialogMode.value === 'create' ? 'post' : 'put'

    // 构建请求数据：更新时包含年份
    const requestData = { ...currentOrg.value }
    if (dialogMode.value === 'edit' && treeYear.value) {
      requestData.year = treeYear.value
    }

    const response = await request[method](url, requestData)

    if (response.success) {
      ElMessage.success(dialogMode.value === 'create' ? '创建成功' : '更新成功')
      dialogVisible.value = false
      await loadOrganizationTree()

      // 刷新右侧面板
      if (selectedOrganization.value && (selectedOrganization.value.level === 1 || selectedOrganization.value.level === 2)) {
        const updatedNode = findNodeById(organizationTree.value, selectedOrganization.value.id)
        if (updatedNode) {
          rightPanelTree.value = updatedNode.children || []
        }
      }
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
        // 如果删除的是当前选中的区县，清空右侧面板
        if (selectedOrganization.value && selectedOrganization.value.id === node.id) {
          selectedOrganization.value = null
          rightPanelTree.value = []
        }
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

// 基层组织编辑对话框
const grassrootsDialogVisible = ref(false)
const grassrootsDialogMode = ref<'create' | 'edit'>('edit')
const currentGrassrootsOrg = ref<any>({
  id: null,
  name: '',
  code: '',
  countyId: null,
  parentId: null,
  level: 4
})

// 编辑基层组织节点
const editGrassrootsNode = (node: any) => {
  grassrootsDialogMode.value = 'edit'
  currentGrassrootsOrg.value = {
    id: node.id,
    name: node.name,
    code: node.code,
    countyId: node.countyId,
    parentId: node.parentId,
    level: node.level
  }
  grassrootsDialogVisible.value = true
}

// 删除基层组织节点
const deleteGrassrootsNode = (node: any) => {
  if (!treeYear.value) {
    ElMessage.warning('请先选择年份')
    return
  }

  ElMessageBox.confirm(
    `确定要删除 "${node.name}" 吗？\n\n此操作将删除该组织及其所有子组织在 ${treeYear.value} 年的数据。\n此操作不可恢复！`,
    '确认删除',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const response = await request.delete(`/api/grassroots-organization/${node.id}`, {
        params: { year: treeYear.value }
      })
      if (response.success) {
        ElMessage.success('删除成功')
        // 重新加载左侧组织树（更新sourceYear显示）
        await loadOrganizationTree()
        // 重新加载右侧面板
        if (selectedOrganization.value && selectedOrganization.value.level === 3) {
          await loadGrassrootsTree(selectedOrganization.value.id, selectedOrganization.value.code)
        }
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error: any) {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  })
}

// 保存基层组织
const saveGrassrootsOrg = async () => {
  if (!currentGrassrootsOrg.value.name || !currentGrassrootsOrg.value.code) {
    ElMessage.warning('请填写必填项')
    return
  }

  // 如果没有选择年份，不允许修改（防止误改基准数据）
  if (grassrootsDialogMode.value === 'edit' && !treeYear.value) {
    ElMessage.warning('请先选择年份后再修改基层组织')
    return
  }

  saving.value = true
  try {
    const method = grassrootsDialogMode.value === 'create' ? 'post' : 'put'

    // 构建请求数据：更新时包含年份
    const requestData = { ...currentGrassrootsOrg.value }
    if (grassrootsDialogMode.value === 'edit' && treeYear.value) {
      requestData.year = treeYear.value
    }

    const response = await request[method]('/api/grassroots-organization', requestData)

    if (response.success) {
      ElMessage.success(grassrootsDialogMode.value === 'create' ? '创建成功' : '更新成功')
      grassrootsDialogVisible.value = false
      // 重新加载左侧组织树（更新sourceYear显示）
      await loadOrganizationTree()
      // 重新加载右侧面板
      if (selectedOrganization.value && selectedOrganization.value.level === 3) {
        await loadGrassrootsTree(selectedOrganization.value.id, selectedOrganization.value.code)
      }
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.message || ''))
  } finally {
    saving.value = false
  }
}

// 初始化
onMounted(() => {
  const q = route.query.year
  const raw = Array.isArray(q) ? q[0] : q
  const y = raw != null ? parseInt(String(raw), 10) : NaN
  if (!isNaN(y) && y >= 2020 && y <= 2030) {
    treeYear.value = y
    globalYearStore.setYear(y)
    return
  }
  loadOrganizationTree()
})

watch(
  () => treeYear.value,
  async (year, oldYear) => {
    if (year === oldYear) return
    // 同步到全局年份 store
    if (year != null) {
      globalYearStore.setYear(year)
    }
    selectedNode.value = null
    selectedOrganization.value = null
    rightPanelTree.value = []
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

.action-card {
  margin-bottom: 20px;
}

.action-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.content-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 240px);
  min-height: 400px;
}

.tree-card {
  flex: 0 0 400px;
  display: flex;
  flex-direction: column;
}

.tree-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.list-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  width: 140px;
}

.tree-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.grassroots-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.grassroots-tree {
  background: #fafafa;
  border-radius: 4px;
  padding: 12px;
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

.tree-node.is-county {
  background-color: #fff7e6;
}

.tree-node.is-county:hover {
  background-color: #ffeacc;
}

.grassroots-node {
  padding: 6px 8px;
}

.grassroots-node .node-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.grassroots-node:hover .node-actions {
  opacity: 1;
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

.tree-node.is-deleted .node-name {
  color: #909399;
  text-decoration: line-through;
}

.level-tag {
  margin-left: 8px;
}

.source-year-tag {
  margin-left: 4px;
}

.change-type-tag {
  margin-left: 4px;
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
