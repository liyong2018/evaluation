<template>
  <div class="menu-management">
    <div class="page-header">
      <h1>权限管理</h1>
      <p>系统菜单和权限资源的配置</p>
    </div>

    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="showAddDialog(null)">
              <el-icon><Plus /></el-icon>
              添加一级菜单
            </el-button>
            <el-button @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 0">目录</el-tag>
            <el-tag v-else-if="row.menuType === 1" type="success">菜单</el-tag>
            <el-tag v-else type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="150" />
        <el-table-column prop="perms" label="权限标识" min-width="150" />
        <el-table-column prop="component" label="组件路径" min-width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editMenu(row)">编辑</el-button>
            <el-button type="success" link @click="showAddDialog(row)" v-if="row.menuType !== 2">添加子项</el-button>
            <el-button type="danger" link @click="deleteMenu(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 菜单编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '添加菜单' : '修改菜单'"
      width="600px"
    >
      <el-form ref="formRef" :model="currentMenu" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="currentMenu.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'menuName', value: 'id' }"
            check-strictly
            placeholder="选择上级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="currentMenu.menuType">
            <el-radio :label="0">目录</el-radio>
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="currentMenu.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="图标" v-if="currentMenu.menuType !== 2">
          <el-input v-model="currentMenu.icon" placeholder="请输入Element Plus图标名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="currentMenu.menuType !== 2">
          <el-input v-model="currentMenu.path" placeholder="例如: /system/user" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="currentMenu.menuType === 1">
          <el-input v-model="currentMenu.component" placeholder="例如: system/UserManagement" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="currentMenu.menuType !== 0">
          <el-input v-model="currentMenu.perms" placeholder="例如: sys:user:list" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="currentMenu.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref()
const currentMenu = reactive({
  id: undefined,
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  perms: '',
  icon: '',
  menuType: 1, // 0:目录, 1:菜单, 2:按钮
  sortOrder: 0
})

const rules = {
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入路由路径', trigger: 'blur' }
  ]
}

// 菜单树选项，用于选择父级
const menuTreeOptions = computed(() => {
  const options = [
    { id: 0, menuName: '主类目', children: [] },
    ...tableData.value
  ]
  return options
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/sys/menu/list')
    tableData.value = buildTree(res.data)
  } catch (error) {
    console.error('获取菜单列表失败', error)
  } finally {
    loading.value = false
  }
}

// 将扁平列表转换为树形结构
const buildTree = (list: any[]): any[] => {
  const map: Record<string, any> = {}
  const tree: any[] = []
  
  list.forEach((item) => {
    map[String(item.id)] = { ...item, children: [] }
  })
  
  list.forEach((item) => {
    const node = map[String(item.id)]
    if (item.parentId && map[String(item.parentId)]) {
      map[String(item.parentId)].children.push(node)
    } else {
      tree.push(node)
    }
  })
  
  return tree
}

const showAddDialog = (parent: any) => {
  dialogMode.value = 'create'
  Object.assign(currentMenu, {
    id: undefined,
    parentId: parent ? parent.id : 0,
    menuName: '',
    path: '',
    component: '',
    perms: '',
    icon: '',
    menuType: parent ? 1 : 0,
    sortOrder: 0
  })
  dialogVisible.value = true
}

const editMenu = (row: any) => {
  dialogMode.value = 'edit'
  Object.assign(currentMenu, row)
  dialogVisible.value = true
}

const deleteMenu = (row: any) => {
  ElMessageBox.confirm(`确定要删除菜单 "${row.menuName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/sys/menu/${row.id}`)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除菜单失败', error)
    }
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogMode.value === 'create') {
          await request.post('/api/sys/menu', currentMenu)
          ElMessage.success('添加成功')
        } else {
          await request.put('/api/sys/menu', currentMenu)
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败', error)
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.menu-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
