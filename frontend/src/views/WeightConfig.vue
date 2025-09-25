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
        <!-- 操作工具栏 -->
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
            <el-table-column prop="configVersion" label="版本" width="100" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag type="success">
                  激活
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="300" fixed="right">
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
                      <span class="node-name">{{ data.indicatorName }}</span>
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
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="configForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述"
          />
        </el-form-item>
        <el-form-item label="版本" prop="configVersion">
          <el-input v-model="configForm.configVersion" placeholder="请输入版本号" />
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
import { weightConfigApi, indicatorWeightApi } from '@/api'

// 响应式数据
const activeTab = ref('config')
const configList = ref<any[]>([])
const weightList = ref<any[]>([])
const configSearch = ref('')
const weightSearch = ref('')
const selectedConfigId = ref<number | null>(null)

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'indicatorName'
}

const loading = reactive({
  configs: false,
  weights: false,
  submit: false
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
  configVersion: ''
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
  description: [{ required: true, message: '请输入配置描述', trigger: 'blur' }],
  configVersion: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
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
    const response = await weightConfigApi.getAll()
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
    configVersion: ''
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
        response = await weightConfigApi.update(configForm.id!, configForm)
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
        response = await indicatorWeightApi.update(weightForm.id!, weightForm)
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

.toolbar-card,
.weight-toolbar {
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.config-list,
.weight-tree {
  min-height: 600px;
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
  padding: 16px;
}

.weight-tree-component {
  width: 100%;
}

.tree-node {
  width: 100%;
  padding: 8px 0;
}

.node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fafafa;
  margin-bottom: 4px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.node-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409eff;
  min-width: 120px;
}

.node-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.level-tag {
  margin-left: 8px;
}

.node-weight {
  margin: 0 16px;
}

.weight-input {
  width: 120px;
}

.node-actions {
  display: flex;
  gap: 8px;
}

/* 权重总计样式 */
.weight-summary {
  margin-top: 16px;
}

.summary-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.summary-label {
  font-weight: 600;
  color: #606266;
}

.summary-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.summary-tag {
  margin-left: 8px;
}

/* 树形节点层级样式 */
.el-tree-node__content {
  height: auto !important;
  padding: 4px 0 !important;
}

.el-tree-node__children .node-content {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}
</style>