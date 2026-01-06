<template>
  <div class="role-management">
    <div class="page-header">
      <h1>角色管理</h1>
      <p>系统角色的创建、配置和权限分配</p>
    </div>

    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-input
              v-model="searchQuery"
              placeholder="请输入角色名称"
              style="width: 200px"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDialog">
              <el-icon><Plus /></el-icon>
              添加角色
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editRole(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteRole(row)" v-if="row.roleCode !== 'ROLE_ADMIN'">删除</el-button>
            <el-button type="warning" link @click="assignPerms(row)">分配权限</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 角色编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '添加角色' : '修改角色'"
      width="500px"
    >
      <el-form ref="formRef" :model="currentRole" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="currentRole.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="currentRole.roleCode" :disabled="dialogMode === 'edit'" placeholder="例如: ROLE_USER" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="currentRole.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permDialogVisible"
      title="分配数据权限"
      width="500px"
    >
      <div class="perm-tree-container" style="max-height: 400px; overflow-y: auto;">
        <el-tree
          ref="treeRef"
          :data="permTreeData"
          :props="{ label: 'name', children: 'children' }"
          show-checkbox
          node-key="id"
          default-expand-all
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPerms">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')

const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const currentRoleId = ref<number | null>(null)
const permTreeData = ref([])
const treeRef = ref<InstanceType<typeof ElTree>>()

const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref()
const currentRole = reactive({
  id: undefined,
  roleName: '',
  roleCode: '',
  description: ''
})

const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z_]+$/, message: '编码格式应为 ROLE_XXX', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/sys/role/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        roleName: searchQuery.value
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取角色列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadData()
}

const showAddDialog = () => {
  dialogMode.value = 'create'
  Object.assign(currentRole, {
    id: undefined,
    roleName: '',
    roleCode: '',
    description: ''
  })
  dialogVisible.value = true
}

const editRole = (row: any) => {
  dialogMode.value = 'edit'
  Object.assign(currentRole, row)
  dialogVisible.value = true
}

const deleteRole = (row: any) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/sys/role/${row.id}`)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除角色失败', error)
    }
  })
}

const assignPerms = async (row: any) => {
  currentRoleId.value = row.id
  permDialogVisible.value = true
  try {
    // 加载组织机构树
    const treeRes = await request.get('/api/organization/tree')
    permTreeData.value = treeRes.data
    
    // 加载已分配的权限
    const permRes = await request.get(`/api/sys/role/${row.id}/organizations`)
    const checkedKeys = permRes.data
    
    await nextTick()
    if (treeRef.value) {
      treeRef.value.setCheckedKeys(checkedKeys)
    }
  } catch (error) {
    console.error('加载权限数据失败', error)
    ElMessage.error('加载权限数据失败')
  }
}

const submitPerms = async () => {
  if (!currentRoleId.value || !treeRef.value) return
  
  try {
    const checkedKeys = treeRef.value.getCheckedKeys()
    
    await request.post(`/api/sys/role/${currentRoleId.value}/organizations`, checkedKeys)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch (error) {
    console.error('保存权限失败', error)
    ElMessage.error('保存权限失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogMode.value === 'create') {
          await request.post('/api/sys/role', currentRole)
          ElMessage.success('添加成功')
        } else {
          await request.put('/api/sys/role', currentRole)
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
.role-management {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
