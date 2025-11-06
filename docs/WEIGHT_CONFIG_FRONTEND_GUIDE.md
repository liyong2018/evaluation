# 权重配置页面组织机构过滤功能修改指南

## 概述
本指南说明如何修改 `frontend/src/views/WeightConfig.vue` 以支持按组织机构过滤权重配置。

## 修改步骤

### 1. 修改 script setup 部分

#### 1.1 更新导入
```typescript
import { weightConfigApi, indicatorWeightApi, organizationApi } from '@/api'
```

#### 1.2 添加组织机构相关响应式数据
在现有的 `configSearch` 后添加：

```typescript
const configSearch = ref('')
const orgcodeFilter = ref('') // 添加组织机构过滤
const organizationList = ref<any[]>([]) // 添加组织机构列表
```

#### 1.3 修改 configForm
在 `configForm` 中添加 `orgcode` 字段：

```typescript
const configForm = reactive({
  id: null,
  configName: '',
  description: '',
  configVersion: '',
  orgcode: '' // 添加组织机构编码字段
})
```

#### 1.4 添加获取组织机构列表的方法
```typescript
// 获取组织机构列表
const getOrganizationList = async () => {
  try {
    const response = await organizationApi.getAll({ page: 1, size: 1000 })
    if (response.success && response.data) {
      organizationList.value = response.data.data || response.data || []
    }
  } catch (error) {
    console.error('获取组织机构列表失败:', error)
  }
}
```

#### 1.5 修改 getConfigList 方法
修改为支持按组织机构过滤：

```typescript
// 获取权重配置列表
const getConfigList = async () => {
  console.log('开始获取权重配置列表')
  loading.configs = true
  try {
    // 支持按组织机构过滤
    const response = await weightConfigApi.getAll(orgcodeFilter.value || undefined)
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
```

#### 1.6 添加按组织机构过滤的方法
```typescript
// 按组织机构过滤
const filterByOrgcode = () => {
  getConfigList()
}

// 清除组织机构过滤
const clearOrgcodeFilter = () => {
  orgcodeFilter.value = ''
  getConfigList()
}
```

#### 1.7 更新 resetConfigForm 方法
```typescript
// 重置配置表单
const resetConfigForm = () => {
  Object.assign(configForm, {
    id: null,
    configName: '',
    description: '',
    configVersion: '',
    orgcode: '' // 添加orgcode重置
  })
  configFormRef.value?.resetFields()
}
```

#### 1.8 更新 onMounted 钩子
```typescript
// 组件挂载时获取数据
onMounted(() => {
  console.log('WeightConfig组件已挂载，开始加载数据')
  getConfigList()
  getOrganizationList() // 添加获取组织机构列表
})
```

### 2. 修改 template 部分

#### 2.1 在工具栏添加组织机构过滤器
在权重配置标签页的工具栏中，修改第一行（搜索框行）：

```vue
<el-row :gutter="20" justify="space-between">
  <el-col :span="8">
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
  <!-- 添加组织机构过滤 -->
  <el-col :span="8">
    <el-select
      v-model="orgcodeFilter"
      placeholder="选择组织机构"
      clearable
      filterable
      @change="filterByOrgcode"
      @clear="clearOrgcodeFilter"
      style="width: 100%"
    >
      <el-option
        v-for="org in organizationList"
        :key="org.code"
        :label="`${org.name} (${org.code})`"
        :value="org.code"
      />
    </el-select>
  </el-col>
  <el-col :span="8">
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
```

#### 2.2 在表格中添加组织机构列
在配置列表的表格中，添加组织机构列：

```vue
<el-table-column prop="id" label="ID" width="80" />
<el-table-column prop="configName" label="配置名称" width="200" />
<!-- 添加组织机构列 -->
<el-table-column prop="orgcode" label="组织机构" width="150">
  <template #default="{ row }">
    <el-tag v-if="row.orgcode" type="info" size="small">
      {{ row.orgcode }}
    </el-tag>
    <span v-else class="text-gray-400">-</span>
  </template>
</el-table-column>
<el-table-column prop="description" label="描述" />
```

#### 2.3 在配置对话框中添加组织机构选择
在配置对话框的表单中添加组织机构选择：

```vue
<el-form-item label="配置名称" prop="configName">
  <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
</el-form-item>
<!-- 添加组织机构选择 -->
<el-form-item label="组织机构" prop="orgcode">
  <el-select
    v-model="configForm.orgcode"
    placeholder="请选择组织机构（可选）"
    clearable
    filterable
    style="width: 100%"
  >
    <el-option
      v-for="org in organizationList"
      :key="org.code"
      :label="`${org.name} (${org.code})`"
      :value="org.code"
    />
  </el-select>
</el-form-item>
<el-form-item label="描述" prop="description">
  <el-input
    v-model="configForm.description"
    type="textarea"
    :rows="3"
    placeholder="请输入配置描述"
  />
</el-form-item>
```

## 测试步骤

1. **启动前端开发服务器**：
   ```bash
   cd frontend
   npm run dev
   ```

2. **测试功能**：
   - 打开权重配置页面
   - 检查工具栏是否显示组织机构选择下拉框
   - 检查下拉框是否正确加载组织机构列表
   - 选择一个组织机构，检查配置列表是否正确过滤
   - 清除过滤条件，检查是否显示所有配置
   - 创建新配置时，检查是否可以选择组织机构
   - 编辑现有配置时，检查组织机构字段是否正确显示
   - 检查表格中组织机构列是否正确显示

## 注意事项

1. **组织机构数据来源**：确保数据库中已有组织机构数据（通过导入乡镇/社区数据自动生成）
2. **SQL迁移**：确保已执行 `006_add_orgcode_to_weight_config.sql` 迁移脚本
3. **后端API**：确保后端已部署最新代码，包含组织机构相关的API
4. **样式调整**：可能需要调整工具栏布局以适应新增的组织机构过滤器

## 完整代码示例

完整的修改后的 WeightConfig.vue 文件可以参考以下结构：

```vue
<template>
  <div class="weight-config">
    <!-- 工具栏：搜索框 + 组织机构过滤 + 操作按钮 -->
    <!-- 表格：配置列表，包含组织机构列 -->
    <!-- 对话框：配置表单，包含组织机构选择 -->
  </div>
</template>

<script setup lang="ts">
// 导入：包含 organizationApi
// 响应式数据：包含 orgcodeFilter 和 organizationList
// 方法：包含 getOrganizationList、filterByOrgcode 等
// 生命周期：onMounted 调用 getOrganizationList
</script>
```

## 相关文件

- 后端实体：`src/main/java/com/evaluate/entity/WeightConfig.java`
- 后端Controller：`src/main/java/com/evaluate/controller/WeightConfigController.java`
- 前端API：`frontend/src/api/index.ts`
- SQL迁移：`sql/migrations/006_add_orgcode_to_weight_config.sql`
