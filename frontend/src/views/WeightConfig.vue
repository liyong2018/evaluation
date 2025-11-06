<template>
  <div class="weight-config">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>权重配置</h1>
      <p>管理评估指标的权重配置和指标权重设置</p>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="config-tabs">
      <!-- 权重配置管理 -->
      <el-tab-pane label="权重配置" name="config">
        <div class="layout-container">
          <!-- 左侧：组织机构树 -->
          <el-card class="org-tree-panel">
            <template #header>
              <div class="card-header">
                <span>组织机构</span>
                <el-button type="primary" size="small" @click="refreshOrganizations">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            <el-tree
              ref="orgTreeRef"
              v-loading="loading.organizations"
              :data="organizationList"
              :props="{ label: 'name', children: 'children' }"
              node-key="code"
              highlight-current
              :expand-on-click-node="false"
              default-expand-all
              @node-click="handleOrgNodeClick"
            >
              <template #default="{ node, data }">
                <div class="org-tree-node">
                  <span class="org-name">{{ data.name }}</span>
                  <span class="org-code">{{ data.code }}</span>
                </div>
              </template>
            </el-tree>
          </el-card>

          <!-- 右侧：权重配置列表 -->
          <div class="config-panel">
            <!-- 工具栏 -->
            <el-card class="toolbar-card">
              <el-row :gutter="20" justify="space-between">
                <el-col :span="12">
                  <el-input
                    v-model="configSearch"
                    placeholder="搜索配置名称"
                    clearable
                    @keyup.enter="searchConfigs"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="12">
                  <div class="toolbar-actions">
                    <el-button type="primary" @click="showConfigDialog">
                      <el-icon><Plus /></el-icon>
                      新建配置
                    </el-button>
                    <el-button type="success" @click="refreshConfigs">
                      <el-icon><Refresh /></el-icon>
                      刷新
                    </el-button>
                  </div>
                </el-col>
              </el-row>
            </el-card>

            <!-- 当前选中组织机构信息 -->
            <el-card v-if="selectedOrg" class="selected-org-info">
              <div class="org-info-content">
                <el-tag type="primary" size="large">{{ selectedOrg.name }}</el-tag>
                <span class="org-info-code">组织机构代码: {{ selectedOrg.code }}</span>
              </div>
            </el-card>

            <!-- 配置列表 -->
            <el-card class="config-list">
              <el-table
                v-loading="loading.configs"
                :data="configList"
                stripe
                border
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="configName" label="配置名称" width="200" />
                <el-table-column prop="description" label="描述" />
                <el-table-column label="状态" width="70">
                  <template #default="{ row }">
                    <el-tag type="success">
                      激活
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column label="操作" width="350" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" @click="editConfig(row)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button
                      type="success"
                      size="small"
                      @click="activateConfig(row)"
                    >
                      <el-icon><Switch /></el-icon>
                      激活
                    </el-button>
                    <el-button type="info" size="small" @click="copyConfig(row)">
                      <el-icon><CopyDocument /></el-icon>
                      复制
                    </el-button>
                    <el-button type="danger" size="small" @click="deleteConfig(row)">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <!-- 指标权重管理 -->
      <el-tab-pane label="指标权重" name="weights">
        <!-- 配置选择 -->
        <el-card class="weight-toolbar">
          <el-row :gutter="20" align="middle">
            <el-col :span="6">
              <el-select 
                v-model="selectedConfigId" 
                placeholder="选择权重配置"
                @change="loadIndicatorWeights"
              >
                <el-option
                  v-for="config in activeConfigs"
                  :key="config.id"
                  :label="config.configName"
                  :value="config.id"
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-input
                v-model="weightSearch"
                placeholder="搜索指标代码"
                clearable
                @keyup.enter="searchWeights"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="12">
              <div class="toolbar-actions">
                <el-button type="primary" @click="showWeightDialog" :disabled="!selectedConfigId">
                  <el-icon><Plus /></el-icon>
                  添加指标
                </el-button>
                <el-button type="success" @click="batchAddWeights" :disabled="!selectedConfigId">
                  <el-icon><Upload /></el-icon>
                  批量添加
                </el-button>
                <el-button type="warning" @click="validateWeights" :disabled="!selectedConfigId">
                  <el-icon><Check /></el-icon>
                  验证权重
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 权重树形结构 -->
        <el-card class="weight-tree">
          <div v-loading="loading.weights" class="tree-container">
            <el-tree
              :data="treeData"
              :props="treeProps"
              node-key="id"
              default-expand-all
              :expand-on-click-node="false"
              class="weight-tree-component"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <div class="node-content">
                    <div class="node-info">
                      <span class="node-code">{{ data.indicatorCode }}</span>
                      <el-tooltip :content="data.indicatorName" placement="top" :show-after="300">
                        <span class="node-name">{{ data.indicatorName }}</span>
                      </el-tooltip>
                      <el-tag 
                        :type="data.indicatorLevel === 1 ? 'primary' : 'success'" 
                        size="small"
                        class="level-tag"
                      >
                        L{{ data.indicatorLevel }}
                      </el-tag>
                    </div>
                    <div class="node-weight">
                      <el-input-number
                        v-model="data.weight"
                        :min="0"
                        :max="1"
                        :step="0.01"
                        :precision="3"
                        size="small"
                        @change="updateWeight(data)"
                        class="weight-input"
                      />
                    </div>
                    <div class="node-actions">
                      <el-button type="primary" size="small" @click="editWeight(data)">
                        <el-icon><Edit /></el-icon>
                        编辑
                      </el-button>
                      <el-button type="danger" size="small" @click="deleteWeight(data)">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </template>
            </el-tree>
            
            <!-- 权重总计 -->
            <div class="weight-summary" v-if="treeData.length > 0">
              <el-divider />
              <div class="summary-item">
                <span class="summary-label">权重总计：</span>
                <span class="summary-value">{{ totalWeight.toFixed(3) }}</span>
                <el-tag 
                  :type="totalWeight === 1 ? 'success' : 'warning'" 
                  size="small"
                  class="summary-tag"
                >
                  {{ totalWeight === 1 ? '正常' : '异常' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 权重配置对话框 -->
    <el-dialog
      v-model="dialogVisible.config"
      :title="isEditConfig ? '编辑配置' : '新建配置'"
      width="500px"
      @close="resetConfigForm"
    >
      <el-form
        ref="configFormRef"
        :model="configForm"
        :rules="configRules"
        label-width="100px"
      >
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="组织机构" prop="orgcode">
          <el-tree-select
            v-model="configForm.orgcode"
            :data="organizationList"
            placeholder="请选择组织机构（可选）"
            clearable
            filterable
            check-strictly
            :render-after-expand="false"
            style="width: 100%"
            node-key="code"
            :props="{
              value: 'code',
              label: 'name',
              children: 'children'
            }"
          >
            <template #default="{ data }">
              <span>{{ data.name }} <span style="color: #909399; font-size: 12px;">({{ data.code }})</span></span>
            </template>
          </el-tree-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="configForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.config = false">取消</el-button>
        <el-button type="primary" @click="submitConfig" :loading="loading.submit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 指标权重对话框 -->
    <el-dialog
      v-model="dialogVisible.weight"
      :title="isEditWeight ? '编辑指标权重' : '添加指标权重'"
      width="600px"
      @close="resetWeightForm"
    >
      <el-form
        ref="weightFormRef"
        :model="weightForm"
        :rules="weightRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="指标代码" prop="indicatorCode">
              <el-input v-model="weightForm.indicatorCode" placeholder="请输入指标代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指标名称" prop="indicatorName">
              <el-input v-model="weightForm.indicatorName" placeholder="请输入指标名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权重值" prop="weight">
              <el-input-number
                v-model="weightForm.weight"
                :min="0"
                :max="1"
                :step="0.01"
                :precision="3"
                placeholder="请输入权重值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层级" prop="indicatorLevel">
              <el-input-number
                v-model="weightForm.indicatorLevel"
                :min="1"
                :max="5"
                placeholder="请输入层级"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="父级指标" prop="parentId">
          <el-input-number v-model="weightForm.parentId" placeholder="请输入父级指标ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="weightForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入指标描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.weight = false">取消</el-button>
        <el-button type="primary" @click="submitWeight" :loading="loading.submit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  Search,
  Plus,
  Refresh,
  Edit,
  Delete,
  Switch,
  CopyDocument,
  Upload,
  Check
} from '@element-plus/icons-vue'
import { weightConfigApi, indicatorWeightApi, organizationApi } from '@/api'

// 响应式数据
const activeTab = ref('config')
const configList = ref<any[]>([])
const weightList = ref<any[]>([])
const configSearch = ref('')
const weightSearch = ref('')
const selectedConfigId = ref<number | null>(null)
const selectedOrg = ref<any>(null) // 当前选中的组织机构
const organizationList = ref<any[]>([]) // 组织机构列表
const orgTreeRef = ref() // 组织机构树引用

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'indicatorName'
}

const loading = reactive({
  configs: false,
  weights: false,
  submit: false,
  organizations: false
})

const dialogVisible = reactive({
  config: false,
  weight: false
})

const isEditConfig = ref(false)
const isEditWeight = ref(false)
const configFormRef = ref<FormInstance>()
const weightFormRef = ref<FormInstance>()

const configForm = reactive({
  id: null,
  configName: '',
  description: '',
  orgcode: '' // 组织机构编码
})

const weightForm = reactive({
  id: null,
  configId: null,
  indicatorCode: '',
  indicatorName: '',
  weight: 0,
  indicatorLevel: 1,
  parentId: null,
  description: ''
})

const configRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入配置描述', trigger: 'blur' }]
}

const weightRules = {
  indicatorCode: [{ required: true, message: '请输入指标代码', trigger: 'blur' }],
  indicatorName: [{ required: true, message: '请输入指标名称', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入权重值', trigger: 'blur' }],
  indicatorLevel: [{ required: true, message: '请输入层级', trigger: 'blur' }]
}

// 计算属性
const activeConfigs = computed(() => {
  return configList.value // 暂时返回所有配置，因为后端没有isActive字段
})

// 构建树形数据
const treeData = computed(() => {
  if (!weightList.value.length) return []
  
  // 创建节点映射
  const nodeMap = new Map()
  const rootNodes: any[] = []
  
  // 先创建所有节点
  weightList.value.forEach(item => {
    nodeMap.set(item.id, {
      ...item,
      children: []
    })
  })
  
  // 构建树形结构
  weightList.value.forEach(item => {
    const node = nodeMap.get(item.id)
    if (item.parentId && nodeMap.has(item.parentId)) {
      // 有父节点，添加到父节点的children中
      const parentNode = nodeMap.get(item.parentId)
      parentNode.children.push(node)
    } else {
      // 没有父节点或父节点不存在，作为根节点
      rootNodes.push(node)
    }
  })
  
  return rootNodes
})

// 计算权重总计 - 按层级分别计算兄弟节点权重和
const totalWeight = computed(() => {
  if (!weightList.value.length) return 0
  
  // 按层级和父节点分组
  const groups = new Map()
  weightList.value.forEach(item => {
    const key = `${item.indicatorLevel}-${item.parentId || 'root'}`
    if (!groups.has(key)) {
      groups.set(key, [])
    }
    groups.get(key).push(item)
  })
  
  // 检查每组的权重和是否为1
  let allNormal = true
  for (const [key, items] of groups) {
    const sum = items.reduce((s, item) => s + (item.weight || 0), 0)
    if (Math.abs(sum - 1) > 0.001) { // 允许小的浮点误差
      allNormal = false
      break
    }
  }
  
  return allNormal ? 1 : 0
})

// 获取权重配置列表
const getConfigList = async () => {
  console.log('开始获取权重配置列表')
  loading.configs = true
  try {
    // 支持按组织机构过滤
    const orgcode = selectedOrg.value ? selectedOrg.value.code : undefined
    const response = await weightConfigApi.getAll(orgcode)
    console.log('权重配置API响应:', response)
    if (response.success) {
      configList.value = response.data || []
      console.log('权重配置列表:', configList.value)
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

// 获取组织机构列表（树形结构）
const getOrganizationList = async () => {
  loading.organizations = true
  try {
    const response = await organizationApi.getTree()
    if (response.success && response.data) {
      organizationList.value = response.data || []
      console.log('组织机构树形数据:', organizationList.value)
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
  // 加载该组织机构的权重配置
  getConfigList()
}

// 搜索配置
const searchConfigs = async () => {
  if (!configSearch.value) {
    getConfigList()
    return
  }
  
  loading.configs = true
  try {
    const response = await weightConfigApi.getByName(configSearch.value)
    if (response.success) {
      configList.value = response.data ? [response.data] : []
    }
  } catch (error) {
    console.error('搜索配置失败:', error)
    ElMessage.error('搜索配置失败')
  } finally {
    loading.configs = false
  }
}

// 刷新配置列表
const refreshConfigs = () => {
  configSearch.value = ''
  getConfigList()
}

// 显示配置对话框
const showConfigDialog = () => {
  isEditConfig.value = false
  resetConfigForm()
  dialogVisible.config = true
}

// 编辑配置
const editConfig = (row: any) => {
  isEditConfig.value = true
  Object.assign(configForm, row)
  dialogVisible.config = true
}

// 重置配置表单
const resetConfigForm = () => {
  Object.assign(configForm, {
    id: null,
    configName: '',
    description: '',
    orgcode: '' // 重置组织机构编码
  })
  configFormRef.value?.resetFields()
}

// 提交配置
const submitConfig = async () => {
  if (!configFormRef.value) return
  
  await configFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.submit = true
    try {
      let response
      if (isEditConfig.value) {
        response = await weightConfigApi.update(configForm)
      } else {
        response = await weightConfigApi.create(configForm)
      }
      
      if (response.success) {
        ElMessage.success(isEditConfig.value ? '更新成功' : '创建成功')
        dialogVisible.config = false
        getConfigList()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    } catch (error) {
      console.error('提交配置失败:', error)
      ElMessage.error('操作失败')
    } finally {
      loading.submit = false
    }
  })
}

// 激活配置
const activateConfig = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要激活这个配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await weightConfigApi.activate(row.id)
      
    if (response.success) {
      ElMessage.success('激活成功')
      getConfigList()
    } else {
      ElMessage.error(response.message || '激活失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('激活失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 复制配置
const copyConfig = async (row: any) => {
  try {
    const newConfigName = `${row.configName}_副本_${Date.now()}`
    const response = await weightConfigApi.copy(row.id, newConfigName)
    if (response.success) {
      ElMessage.success('复制成功')
      getConfigList()
    } else {
      ElMessage.error(response.message || '复制失败')
    }
  } catch (error) {
    console.error('复制配置失败:', error)
    ElMessage.error('复制失败')
  }
}

// 删除配置
const deleteConfig = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await weightConfigApi.delete(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      getConfigList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除配置失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 加载指标权重
const loadIndicatorWeights = async () => {
  if (!selectedConfigId.value) return
  
  loading.weights = true
  try {
    const response = await indicatorWeightApi.getByConfigId(selectedConfigId.value)
    if (response.success) {
      weightList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取指标权重失败')
    }
  } catch (error) {
    console.error('获取指标权重失败:', error)
    ElMessage.error('获取指标权重失败')
  } finally {
    loading.weights = false
  }
}

// 搜索权重
const searchWeights = async () => {
  if (!weightSearch.value || !selectedConfigId.value) {
    loadIndicatorWeights()
    return
  }
  
  loading.weights = true
  try {
    const response = await indicatorWeightApi.getByIndicatorCode(weightSearch.value)
    if (response.success) {
      weightList.value = response.data ? [response.data] : []
    }
  } catch (error) {
    console.error('搜索权重失败:', error)
    ElMessage.error('搜索权重失败')
  } finally {
    loading.weights = false
  }
}

// 显示权重对话框
const showWeightDialog = () => {
  isEditWeight.value = false
  resetWeightForm()
  weightForm.configId = selectedConfigId.value
  dialogVisible.weight = true
}

// 编辑权重
const editWeight = (row: any) => {
  isEditWeight.value = true
  Object.assign(weightForm, row)
  dialogVisible.weight = true
}

// 重置权重表单
const resetWeightForm = () => {
  Object.assign(weightForm, {
    id: null,
    configId: selectedConfigId.value,
    indicatorCode: '',
    indicatorName: '',
    weight: 0,
    indicatorLevel: 1,
    parentId: null,
    description: ''
  })
  weightFormRef.value?.resetFields()
}

// 提交权重
const submitWeight = async () => {
  if (!weightFormRef.value) return
  
  await weightFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.submit = true
    try {
      let response
      if (isEditWeight.value) {
        response = await indicatorWeightApi.update(weightForm)
      } else {
        response = await indicatorWeightApi.create(weightForm)
      }
      
      if (response.success) {
        ElMessage.success(isEditWeight.value ? '更新成功' : '创建成功')
        dialogVisible.weight = false
        loadIndicatorWeights()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    } catch (error) {
      console.error('提交权重失败:', error)
      ElMessage.error('操作失败')
    } finally {
      loading.submit = false
    }
  })
}

// 更新权重
const updateWeight = async (row: any) => {
  try {
    const response = await indicatorWeightApi.update(row.id, { weight: row.weight })
    if (response.success) {
      ElMessage.success('权重更新成功')
    } else {
      ElMessage.error(response.message || '权重更新失败')
    }
  } catch (error) {
    console.error('更新权重失败:', error)
    ElMessage.error('权重更新失败')
  }
}

// 删除权重
const deleteWeight = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个指标权重吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await indicatorWeightApi.delete(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      loadIndicatorWeights()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除权重失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量添加权重
const batchAddWeights = async () => {
  ElMessage.info('批量添加功能开发中...')
}

// 验证权重
const validateWeights = async () => {
  if (!selectedConfigId.value) return
  
  try {
    const response = await indicatorWeightApi.validate(selectedConfigId.value)
    if (response.success) {
      ElMessage.success('权重验证通过')
    } else {
      ElMessage.error(response.message || '权重验证失败')
    }
  } catch (error) {
    console.error('验证权重失败:', error)
    ElMessage.error('验证权重失败')
  }
}



// 组件挂载时获取数据
onMounted(() => {
  console.log('WeightConfig组件已挂载，开始加载数据')
  getConfigList()
  getOrganizationList() // 获取组织机构列表
})
</script>

<style scoped>
.weight-config {
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

.config-tabs {
  margin-top: 16px;
}

/* 左右布局容器 */
.layout-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 250px);
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

.toolbar-card,
.weight-toolbar {
  margin-bottom: 0;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.config-list,
.weight-tree {
  min-height: 400px;
}

.weight-tree {
  max-height: 800px;
  overflow-y: auto;
}

.el-input-number {
  width: 100%;
}

/* 树形结构样式 */
.tree-container {
  padding: 20px;
  background: linear-gradient(to bottom, #fafbfc, #ffffff);
  border-radius: 8px;
}

.weight-tree-component {
  width: 100%;
}

.tree-node {
  width: 100%;
  padding: 12px 0;
}

.node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 30px;
  padding: 14px 16px;
  border: 1px solid #e8eef5;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  margin: 12px 0;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.node-content::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background: linear-gradient(180deg, #409eff, #79bbff);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.node-content:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateX(2px);
}

.node-content:hover::before {
  opacity: 1;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.node-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  min-width: 100px;
  text-align: center;
  white-space: nowrap;
}

.node-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

.level-tag {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
}

.node-weight {
  margin: 0 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-weight::before {
  content: '权重';
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.weight-input {
  width: 130px;
}

.weight-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #e8eef5 inset;
  transition: all 0.3s ease;
}

.weight-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.weight-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.node-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.node-content:hover .node-actions {
  opacity: 1;
}

.node-actions .el-button {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.node-actions .el-button:hover {
  transform: translateY(-1px);
}

/* 权重总计样式 */
.weight-summary {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef1f5 100%);
  border-radius: 8px;
  border: 1px solid #e8eef5;
}

.summary-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.summary-label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #409eff, #79bbff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  min-width: 80px;
  text-align: center;
}

.summary-tag {
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 4px;
}

/* 树形节点层级样式 */
.el-tree-node__content {
  height: auto !important;
  padding: 6px 0 !important;
}

.el-tree-node__expand-icon {
  font-size: 14px;
  color: #909399;
  transition: all 0.3s ease;
}

.el-tree-node__expand-icon:hover {
  color: #409eff;
}

/* 子节点样式增强 */
.el-tree-node__children .node-content {
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
  border-left: 3px solid #409eff;
  margin-left: 32px;
  margin-top: 16px;
  margin-bottom: 16px;
}

.el-tree-node__children .node-content::before {
  background: linear-gradient(180deg, #79bbff, #409eff);
}

.el-tree-node__children .el-tree-node__children .node-content {
  background: linear-gradient(135deg, #f0fff4 0%, #ffffff 100%);
  border-left-color: #67c23a;
  margin-left: 64px;
  margin-top: 18px;
  margin-bottom: 18px;
}

.el-tree-node__children .el-tree-node__children .node-content::before {
  background: linear-gradient(180deg, #95d475, #67c23a);
}

.el-tree-node__children .el-tree-node__children .el-tree-node__children .node-content {
  background: linear-gradient(135deg, #fef0f0 0%, #ffffff 100%);
  border-left-color: #f56c6c;
  margin-left: 96px;
  margin-top: 20px;
  margin-bottom: 20px;
}

.el-tree-node__children .el-tree-node__children .el-tree-node__children .node-content::before {
  background: linear-gradient(180deg, #f78989, #f56c6c);
}

/* 层级标签颜色增强 */
.level-tag.el-tag--primary {
  background: linear-gradient(135deg, #409eff, #79bbff);
  border-color: #409eff;
  color: white;
  font-weight: 600;
}

.level-tag.el-tag--success {
  background: linear-gradient(135deg, #67c23a, #95d475);
  border-color: #67c23a;
  color: white;
  font-weight: 600;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .node-name {
    max-width: 200px;
  }

  .node-weight {
    margin: 0 12px;
  }

  .weight-input {
    width: 110px;
  }
}

@media (max-width: 768px) {
  .node-content {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .node-info {
    flex-wrap: wrap;
  }

  .node-weight {
    justify-content: space-between;
    margin: 0;
  }

  .node-actions {
    opacity: 1;
    justify-content: flex-end;
  }

  .el-tree-node__children .node-content,
  .el-tree-node__children .el-tree-node__children .node-content,
  .el-tree-node__children .el-tree-node__children .el-tree-node__children .node-content {
    margin-left: 0;
  }
}

/* ========== Optimized tree visuals (overrides) ========== */
:deep(.weight-tree .tree-container) {
  padding: 8px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
}

:deep(.weight-tree .tree-node) {
  padding: 8px 0;
}

:deep(.weight-tree .node-content) {
  padding: 6px 8px;
  border-radius: 10px;
  border-color: #e6edf5;
  margin: 3px 0;
  background: #fff;
  min-height: 30px;
  line-height: 1.15;
}

/* Add vertical rhythm between siblings explicitly */
:deep(.weight-tree .el-tree-node) {
  margin: 1px 0;
}

:deep(.weight-tree .el-tree-node > .el-tree-node__children > .el-tree-node) {
  margin: 3px 0;
}

:deep(.weight-tree .el-tree-node > .el-tree-node__children > .el-tree-node:not(:last-child) .node-content) {
  margin-bottom: 4px;
}

/* Ensure wrapper content gives vertical space */
:deep(.weight-tree .el-tree-node__content) {
  height: auto !important;
  padding: 2px 0 !important;
}

/* Nudge expand icon so rows feel taller */
:deep(.weight-tree .el-tree-node__expand-icon) {
  margin-top: 2px;
}

:deep(.weight-tree .node-content:hover) {
  border-color: #c8e1ff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.12);
  transform: translateX(1px);
}

:deep(.weight-tree .node-code) {
  font-size: 12px;
  color: #337ecc;
  background: rgba(51, 126, 204, 0.08);
  border-color: rgba(51, 126, 204, 0.25);
  padding: 3px 8px;
  border-radius: 6px;
  min-width: 88px;
}

:deep(.weight-tree .node-name) {
  max-width: 360px;
}

:deep(.weight-tree .node-weight) {
  margin: 0 14px;
}

:deep(.weight-tree .weight-input) {
  width: 110px;
}

/* Indentation + subtle connectors per level */
:deep(.weight-tree .el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 28px;
  margin-top: 4px;
  margin-bottom: 4px;
  border-left: 2px solid #409eff;
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
}

:deep(.weight-tree .el-tree-node__children .el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 56px;
  margin-top: 4px;
  margin-bottom: 4px;
  border-left-color: #67c23a;
  background: linear-gradient(135deg, #f6fff8 0%, #ffffff 100%);
}

:deep(.weight-tree .el-tree-node__children .el-tree-node__children .el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 84px;
  margin-top: 4px;
  margin-bottom: 4px;
  border-left-color: #f56c6c;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 100%);
}

/* Slightly tighter gaps and inputs */
:deep(.weight-tree .node-info) {
  gap: 10px;
}

:deep(.weight-tree .node-weight) {
  margin: 0 8px;
}

:deep(.weight-tree .weight-input) {
  width: 92px;
}

@media (max-width: 1200px) {
  :deep(.weight-tree .node-name) { max-width: 220px; }
  :deep(.weight-tree .node-weight) { margin: 0 10px; }
  :deep(.weight-tree .weight-input) { width: 100px; }
}
</style>
