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
                <el-button size="small" type="danger" @click.stop="deleteNode(data)" :disabled="data.children && data.children.length > 0">
                  <el-icon><Delete /></el-icon>
                  删除
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
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

// 加载组织机构树
const loadOrganizationTree = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/organization/tree')
    if (response.success) {
      // 后端已返回构建好的树形结构，直接使用
      organizationTree.value = response.data || []

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

// 删除节点
const deleteNode = (node: any) => {
  if (node.children && node.children.length > 0) {
    ElMessage.warning('该节点下有子节点，无法删除')
    return
  }

  ElMessageBox.confirm(`确定要删除组织机构 "${node.name}" 吗？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      const response = await request.delete(`/api/organization/${node.id}`)
      if (response.success) {
        ElMessage.success('删除成功')
        await loadOrganizationTree()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error: any) {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  })
}

// 处理Excel文件选择
const handleFileChange: UploadProps['onChange'] = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  // 检查文件类型
  const validTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请选择Excel文件(.xlsx或.xls)')
    return
  }

  // 创建FormData
  const formData = new FormData()
  formData.append('file', file)

  uploading.value = true
  try {
    const response = await request.post('/api/organization/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.success) {
      ElMessage.success(`导入成功，共导入 ${response.data?.count || 0} 条记录`)
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

// 初始化
onMounted(() => {
  loadOrganizationTree()
})
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
