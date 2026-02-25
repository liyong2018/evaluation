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
                <div class="card-header-left">
                  <span>组织机构</span>
                  <el-select
                    v-model="orgYear"
                    size="small"
                    placeholder="年份"
                    class="org-year-select"
                    @change="refreshOrganizations"
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
                <el-tag type="success" size="large" v-if="orgYear">
                  <el-icon><Calendar /></el-icon>
                  {{ orgYear }}年数据
                </el-tag>
              </div>
            </el-card>

            <!-- 配置列表 -->
            <el-card class="config-list">
              <!-- 数据来源提示 -->
              <div v-if="configList.length > 0 && configList.some(c => c.actualOrgcode && c.actualOrgcode !== selectedOrg?.code)" class="data-source-notice">
                <el-alert type="info" :closable="false">
                  <template #title>
                    <span>以下配置的数据来自上级组织机构：</span>
                  </template>
                  <div v-for="config in configList.filter(c => c.actualOrgcode && c.actualOrgcode !== selectedOrg?.code)" :key="config.id" class="data-source-item">
                    <span class="config-name">{{ config.configName }}</span>
                    <el-tag type="warning" size="small">{{ config.actualOrgName || config.actualOrgcode }}</el-tag>
                    <span class="year-label">{{ config.year }}年</span>
                  </div>
                </el-alert>
              </div>
              <el-table
                v-loading="loading.configs"
                :data="configList"
                stripe
                border
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="configName" label="配置名称" width="450" />
                <el-table-column label="状态" width="150">
                  <template #default>
                    <el-tag type="success">
                      激活
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="300" />
                <el-table-column label="操作" width="460" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" @click="editConfig(row)" :disabled="row?._temp">
                      <el-icon><Edit /></el-icon>
                      编辑配置
                    </el-button>
                    <el-button type="primary" size="small" @click="openWeightEditor(row)">
                      <el-icon><Edit /></el-icon>
                      编辑权重
                    </el-button>
                    <el-button type="warning" size="small" @click="openScoreDialog(row)" :disabled="row?._temp">
                      <el-icon><DocumentAdd /></el-icon>
                      打分
                    </el-button>
                    <el-button type="info" size="small" @click="openStatisticsDialog(row)" :disabled="row?._temp">
                      <el-icon><DataLine /></el-icon>
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </div>
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

    <!-- 指标权重编辑对话框 -->
    <el-dialog
      v-model="dialogVisible.weightEditor"
      :title="`编辑权重 - ${currentWeightConfig?.configName || ''}`"
      width="1100px"
      :close-on-click-modal="false"
    >
      <el-card class="weight-editor-card" shadow="never">
        <template #header>
          <div class="card-header-with-tip">
            <span>指标权重编辑</span>
            <el-tag :type="totalWeight === 1 ? 'success' : 'danger'" size="small">
              {{ totalWeight === 1 ? '同层级权重和正常' : '同层级权重和需为1' }}
            </el-tag>
          </div>
        </template>
        <div v-loading="loading.weights" class="tree-container">
          <el-scrollbar height="560px">
            <el-tree
              :data="treeData"
              :props="{ label: 'indicatorName', children: 'children' }"
              node-key="id"
              default-expand-all
              :expand-on-click-node="false"
              class="weight-tree"
            >
              <template #default="{ data }">
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
                        {{ data.indicatorLevel === 1 ? '一级' : '二级' }}
                      </el-tag>
                    </div>
                    <div class="node-weight">
                      <span class="weight-label">权重:</span>
                      <el-input-number
                        v-model="data.weight"
                        :min="0"
                        :max="1"
                        :precision="3"
                        :step="0.01"
                        size="small"
                        class="weight-input"
                      />
                    </div>
                  </div>
                </div>
              </template>
            </el-tree>
          </el-scrollbar>
        </div>
      </el-card>
      <template #footer>
        <el-button @click="dialogVisible.weightEditor = false">取消</el-button>
        <el-button type="primary" @click="saveWeightEditor" :loading="loading.submit">
          保存
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

    <!-- 专家打分对话框 -->
    <el-dialog
      v-model="dialogVisible.score"
      :title="`专家打分 - ${currentScoreConfig?.configName || ''}`"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="score-dialog-content">
        <!-- 专家信息 -->
        <el-card class="expert-info-card" shadow="never">
          <template #header>
            <span>专家信息</span>
          </template>
          <el-form label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="专家姓名" required>
                  <el-input v-model="scoreForm.expertName" placeholder="请输入专家姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话">
                  <el-input v-model="scoreForm.expertPhone" placeholder="请输入联系电话（可选）" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <!-- 指标权重打分 -->
        <el-card class="score-tree-card" shadow="never">
          <template #header>
            <div class="card-header-with-tip">
              <span>指标权重打分</span>
              <el-tag type="warning" size="small">同层级权重总和必须为1</el-tag>
            </div>
          </template>
          <div class="score-tree-container">
            <el-tree
              :data="scoreTreeData"
              :props="{ label: 'indicatorName', children: 'children' }"
              node-key="id"
              default-expand-all
              :expand-on-click-node="false"
              class="score-weight-tree"
            >
              <template #default="{ data }">
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
                      <span class="current-weight-ref">
                        <el-icon><View /></el-icon>
                        参考: {{ data.currentWeight?.toFixed(2) || '0.00' }}
                      </span>
                    </div>
                    <div class="node-weight">
                      <span class="weight-label">打分:</span>
                      <el-input-number
                        v-model="data.weight"
                        :min="0"
                        :max="1"
                        :step="0.01"
                        :precision="3"
                        size="small"
                        class="weight-input"
                      />
                    </div>
                  </div>
                </div>
              </template>
            </el-tree>
          </div>
        </el-card>
      </div>

      <template #footer>
        <el-button @click="dialogVisible.score = false">取消</el-button>
        <el-button type="primary" @click="submitScore" :loading="loading.submit">
          提交打分
        </el-button>
      </template>
    </el-dialog>

    <!-- 统计详情对话框 -->
    <el-dialog
      v-model="dialogVisible.statistics"
      width="1920px"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="statistics-dialog-header">
          <div class="header-title">打分统计详情 - {{ currentScoreConfig?.configName || '' }}</div>
          <div class="header-info">
            <el-tag type="primary" size="small">
              <el-icon><Calendar /></el-icon>
              {{ currentScoreConfig?.year || orgYear }}年数据
            </el-tag>
            <el-tag type="success" size="small">
              <el-icon><Location /></el-icon>
              {{ currentScoreConfig?.actualOrgName || currentScoreConfig?.actualOrgcode || selectedOrg?.name }}
            </el-tag>
            <el-tag v-if="currentScoreConfig?.actualOrgcode && currentScoreConfig?.actualOrgcode !== selectedOrg?.code" type="warning" size="small">
              继承自上级
            </el-tag>
          </div>
        </div>
      </template>
      <div v-if="statisticsData" class="statistics-content-new">
        <!-- 顶部：平均分表格 -->
        <el-card class="average-table-card" shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span>各指标平均权重</span>
              <el-tag type="success" size="small">
                参与专家: {{ statisticsData.experts?.length || 0 }} 人
              </el-tag>
            </div>
          </template>
          <el-table :data="averageScoreTableData" border stripe>
            <el-table-column label="一级指标" width="200">
              <template #default="{ row }">
                <div class="level1-cell">
                  <el-tag type="primary" size="small">{{ row.level1Code }}</el-tag>
                  <span class="indicator-name">{{ row.level1Name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="平均权重" width="120" align="center">
              <template #default="{ row }">
                <span class="weight-value">{{ row.level1Avg?.toFixed(2) || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="二级指标">
              <template #default="{ row }">
                <div v-if="row.children.length > 0" class="level2-list">
                  <div
                    v-for="(child, idx) in row.children"
                    :key="idx"
                    class="level2-item"
                  >
                    <el-tag size="small" type="success">{{ child.indicatorCode }}</el-tag>
                    <span class="indicator-name">{{ child.indicatorName }}</span>
                    <span class="weight-badge">{{ child.avgWeight?.toFixed(2) ?? '-' }}</span>
                    <span class="score-count">({{ child.scoreCount }}人)</span>
                  </div>
                </div>
                <span v-else style="color: #909399">无二级指标</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 底部：左右布局 -->
        <div class="bottom-layout">
          <!-- 左侧：专家列表 -->
          <el-card class="experts-list-card" shadow="never">
            <template #header>
              <span>参与专家</span>
            </template>
            <el-scrollbar height="400px">
              <div
                v-for="(expert, index) in statisticsData.experts"
                :key="index"
                class="expert-item"
                :class="{ active: selectedExpertKey === getExpertKey(expert) }"
                @click="selectExpert(expert)"
              >
                <div class="expert-info">
                  <el-icon class="expert-icon"><User /></el-icon>
                  <div class="expert-details">
                    <div class="expert-name">{{ expert.expert_name }}</div>
                    <div class="expert-phone">{{ expert.expert_phone || '未填写电话' }}</div>
                  </div>
                </div>
                <el-icon v-if="selectedExpertKey === getExpertKey(expert)" class="check-icon">
                  <Check />
                </el-icon>
              </div>
            </el-scrollbar>
          </el-card>

          <!-- 右侧：选中专家的打分详情 -->
          <el-card class="expert-score-card" shadow="never">
            <template #header>
              <span v-if="selectedExpert">
                {{ selectedExpert.expert_name }} 的打分详情
              </span>
              <span v-else style="color: #909399">请选择左侧专家查看打分详情</span>
            </template>
            <div v-if="selectedExpert" class="expert-score-tree-container">
              <el-scrollbar height="400px">
                <el-tree
                  :data="selectedExpertScoreTree"
                  node-key="id"
                  default-expand-all
                  :expand-on-click-node="false"
                  class="expert-score-tree"
                >
                  <template #default="{ data }">
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
                            {{ data.indicatorLevel === 1 ? '一级' : '二级' }}
                          </el-tag>
                        </div>
                        <div class="node-weight">
                          <span class="weight-label">权重:</span>
                          <span class="weight-value-display">{{ data.weight != null ? data.weight.toFixed(2) : '-' }}</span>
                        </div>
                      </div>
                    </div>
                  </template>
                </el-tree>
              </el-scrollbar>
            </div>
            <el-empty v-else description="请选择左侧专家" :image-size="100" />
          </el-card>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible.statistics = false">关闭</el-button>
        <el-button type="primary" @click="applyAverageWeights" :loading="loading.submit">
          应用平均权重到正式配置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  Search,
  Plus,
  Refresh,
  Edit,
  Switch,
  Upload,
  Check,
  DocumentAdd,
  DataLine,
  View,
  User,
  Calendar,
  Location
} from '@element-plus/icons-vue'
import { weightConfigApi, indicatorWeightApi, organizationApi, indicatorWeightScoreApi } from '@/api'
import { useGlobalYearStore } from '@/stores/globalYear'
import { useUserStore } from '@/stores/user'
import { useGlobalOrganizationStore } from '@/stores/globalOrganization'

// 全局年份 store
const globalYearStore = useGlobalYearStore()
// 用户 store
const userStore = useUserStore()
// 全局组织机构 store
const globalOrganizationStore = useGlobalOrganizationStore()

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
const orgYear = ref<number | null>(globalYearStore.selectedYear)
const yearOptions = ref<number[]>([])
const orgTreeRenderKey = computed(() => `orgTree-${orgYear.value ?? 'all'}`)

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'indicatorName'
}

const modelDefinitions = [
  { modelId: 3, modelName: '乡镇减灾能力评估模型' },
  { modelId: 4, modelName: '社区-行政村能力评估模型' },
  { modelId: 8, modelName: '社区-乡镇能力评估模型' },
  { modelId: 11, modelName: '综合减灾能力评估模型' }
]

const tempWeightCache = reactive<Record<string, any[]>>({})

const buildDefaultWeightTemplate = () => {
  return [
    { id: 'L1_DISASTER_MANAGEMENT', indicatorCode: 'L1_DISASTER_MANAGEMENT', indicatorName: '灾害管理能力', indicatorLevel: 1, parentId: null, weight: 0 },
    { id: 'L1_DISASTER_PREPAREDNESS', indicatorCode: 'L1_DISASTER_PREPAREDNESS', indicatorName: '灾害备灾能力', indicatorLevel: 1, parentId: null, weight: 0 },
    { id: 'L1_SELF_RESCUE_TRANSFER', indicatorCode: 'L1_SELF_RESCUE_TRANSFER', indicatorName: '自救转移能力', indicatorLevel: 1, parentId: null, weight: 0 },
    { id: 'L2_MANAGEMENT_CAPABILITY', indicatorCode: 'L2_MANAGEMENT_CAPABILITY', indicatorName: '队伍管理能力', indicatorLevel: 2, parentId: 'L1_DISASTER_MANAGEMENT', weight: 0 },
    { id: 'L2_RISK_ASSESSMENT', indicatorCode: 'L2_RISK_ASSESSMENT', indicatorName: '风险评估能力', indicatorLevel: 2, parentId: 'L1_DISASTER_MANAGEMENT', weight: 0 },
    { id: 'L2_FUNDING', indicatorCode: 'L2_FUNDING', indicatorName: '财政投入能力', indicatorLevel: 2, parentId: 'L1_DISASTER_MANAGEMENT', weight: 0 },
    { id: 'L2_MATERIAL', indicatorCode: 'L2_MATERIAL', indicatorName: '物资储备能力', indicatorLevel: 2, parentId: 'L1_DISASTER_PREPAREDNESS', weight: 0 },
    { id: 'L2_MEDICAL', indicatorCode: 'L2_MEDICAL', indicatorName: '医疗保障能力', indicatorLevel: 2, parentId: 'L1_DISASTER_PREPAREDNESS', weight: 0 },
    { id: 'L2_SELF_RESCUE', indicatorCode: 'L2_SELF_RESCUE', indicatorName: '自救互救能力', indicatorLevel: 2, parentId: 'L1_SELF_RESCUE_TRANSFER', weight: 0 },
    { id: 'L2_PUBLIC_AVOIDANCE', indicatorCode: 'L2_PUBLIC_AVOIDANCE', indicatorName: '公众避险能力', indicatorLevel: 2, parentId: 'L1_SELF_RESCUE_TRANSFER', weight: 0 },
    { id: 'L2_RELOCATION', indicatorCode: 'L2_RELOCATION', indicatorName: '转移安置能力', indicatorLevel: 2, parentId: 'L1_SELF_RESCUE_TRANSFER', weight: 0 }
  ]
}

const buildTempConfigList = (orgcode: string, year: number) => {
  const createTime = `${year}-01-01T00:00:00`
  return modelDefinitions.map(def => ({
    id: null,
    configName: def.modelName,
    description: `${def.modelName}权重配置`,
    orgcode,
    createTime,
    _temp: true,
    _tempKey: `${orgcode}-${year}-${def.modelId}`,
    _modelId: def.modelId
  }))
}

const getOrInitTempWeights = (tempKey: string) => {
  if (!tempWeightCache[tempKey]) {
    tempWeightCache[tempKey] = buildDefaultWeightTemplate()
  }
  return JSON.parse(JSON.stringify(tempWeightCache[tempKey]))
}

const loading = reactive({
  configs: false,
  weights: false,
  submit: false,
  organizations: false
})

const dialogVisible = reactive({
  config: false,
  weight: false,
  weightEditor: false,
  score: false,      // 打分对话框
  statistics: false  // 详情对话框
})

const isEditConfig = ref(false)
const isEditWeight = ref(false)
const configFormRef = ref<FormInstance>()
const weightFormRef = ref<FormInstance>()

const configForm = reactive({
  id: null,
  configName: '',
  description: '',
  orgcode: '', // 组织机构编码
  createTime: null as string | null
})

const weightForm = reactive({
  id: null,
  configId: null as number | null,
  indicatorCode: '',
  indicatorName: '',
  weight: 0,
  indicatorLevel: 1,
  parentId: null,
  description: ''
})

// 打分表单数据
const scoreForm = reactive({
  configId: null,
  orgcode: '',         // 组织机构代码
  expertName: '',
  expertPhone: '',
  scores: [] as any[]  // 存储所有指标的打分
})

// 统计信息数据
const statisticsData = ref<any>(null)
const currentScoreConfig = ref<any>(null)  // 当前正在打分/查看的配置
const currentWeightConfig = ref<any>(null) // 当前正在编辑权重的配置
const selectedExpert = ref<any>(null)  // 选中的专家

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

// 构建平均分表格数据（支持一级指标表头合并）
const averageScoreTableData = computed(() => {
  if (!statisticsData.value?.indicatorStats) return []

  const stats = statisticsData.value.indicatorStats
  const level1Map = new Map()

  // 按一级指标分组（使用 indicatorLevel 判断）
  stats.forEach((stat: any) => {
    if (stat.indicatorLevel === 1) {
      // 一级指标
      if (!level1Map.has(stat.indicatorCode)) {
        level1Map.set(stat.indicatorCode, {
          level1Code: stat.indicatorCode,
          level1Name: stat.indicatorName,
          level1Avg: stat.avgWeight,
          level1Id: stat.id,
          children: []
        })
      } else {
        const group = level1Map.get(stat.indicatorCode)
        group.level1Name = stat.indicatorName
        group.level1Avg = stat.avgWeight
        group.level1Id = stat.id
      }
    } else if (stat.indicatorLevel === 2) {
      // 二级指标，需要找到其父级（一级指标）
      // 先确保父级存在
      const parentIndicator = stats.find((s: any) => s.id === stat.parentId && s.indicatorLevel === 1)
      if (parentIndicator) {
        const parentCode = parentIndicator.indicatorCode
        if (!level1Map.has(parentCode)) {
          level1Map.set(parentCode, {
            level1Code: parentCode,
            level1Name: parentIndicator.indicatorName,
            level1Avg: parentIndicator.avgWeight,
            level1Id: parentIndicator.id,
            children: []
          })
        }

        const group = level1Map.get(parentCode)
        group.children.push({
          indicatorCode: stat.indicatorCode,
          indicatorName: stat.indicatorName,
          avgWeight: stat.avgWeight,
          scoreCount: stat.scoreCount
        })
      }
    }
  })

  return Array.from(level1Map.values())
})

// 构建选中专家的打分树形数据
const selectedExpertScoreTree = computed(() => {
  if (!selectedExpert.value || !statisticsData.value) return []

  const expertName = selectedExpert.value.expert_name
  const expertPhone = selectedExpert.value.expert_phone ?? ''
  const allStats = statisticsData.value.indicatorStats || []

  const nodes: any[] = []
  allStats.forEach((stat: any) => {
    if (stat.id === null || stat.id === undefined) return
    const expertScore = stat.expertScores?.find((s: any) => {
      const sPhone = s.expertPhone ?? ''
      return s.expertName === expertName && sPhone === expertPhone
    })
    nodes.push({
      id: stat.id,
      indicatorCode: stat.indicatorCode,
      indicatorName: stat.indicatorName,
      indicatorLevel: stat.indicatorLevel,
      parentId: stat.parentId,
      weight: expertScore?.weight ?? null,
      createTime: expertScore?.createTime,
      children: []
    })
  })

  // 构建树形结构
  const nodeMap = new Map()
  const roots: any[] = []

  // 第一遍：创建所有节点
  nodes.forEach(item => {
    nodeMap.set(item.id, item)
  })

  // 第二遍：建立父子关系
  nodes.forEach(item => {
    if (item.parentId !== null && item.parentId !== undefined && nodeMap.has(item.parentId)) {
      const parent = nodeMap.get(item.parentId)
      parent.children.push(item)
    } else {
      // 一级指标（没有父节点）
      roots.push(item)
    }
  })

  return roots
})

// 构建树形数据
const treeData = computed(() => {
  if (!weightList.value.length) return []
  
  const nodeMap = new Map<any, any>()
  const rootNodes: any[] = []

  weightList.value.forEach(item => {
    item.children = []
    nodeMap.set(item.id, item)
  })

  weightList.value.forEach(item => {
    const node = nodeMap.get(item.id)
    if (item.parentId !== null && item.parentId !== undefined && nodeMap.has(item.parentId)) {
      const parentNode = nodeMap.get(item.parentId)
      parentNode.children.push(node)
    } else {
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
    const sum = (items as any[]).reduce((s: number, item: any) => s + (item.weight || 0), 0)
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
  // 如果当前年份没有组织机构，不查询权重配置
  if (!organizationList.value || organizationList.value.length === 0) {
    console.log('当前年份没有组织机构，清空权重配置列表')
    configList.value = []
    loading.configs = false
    return
  }

  loading.configs = true
  try {
    const orgcode = selectedOrg.value?.code
    if (!orgcode || !orgYear.value) {
      configList.value = []
      return
    }

    const response = await weightConfigApi.getAll({
      orgcode,
      year: orgYear.value
    })
    console.log('权重配置API响应:', response)
    if (response.success) {
      const list = response.data || []
      configList.value = list.length ? list : buildTempConfigList(orgcode, orgYear.value)
      console.log('权重配置列表（已过滤）:', configList.value)
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

const openWeightEditor = async (row: any) => {
  currentWeightConfig.value = row
  if (row?._temp) {
    selectedConfigId.value = null
    weightList.value = getOrInitTempWeights(row._tempKey)
  } else {
    selectedConfigId.value = row.id
    await loadIndicatorWeights()
  }
  dialogVisible.weightEditor = true
}

const saveWeightEditor = async () => {
  loading.submit = true
  try {
    if (currentWeightConfig.value?._temp) {
      const createResp = await weightConfigApi.create({
        configName: currentWeightConfig.value.configName,
        description: currentWeightConfig.value.description,
        orgcode: currentWeightConfig.value.orgcode,
        createTime: currentWeightConfig.value.createTime
      })
      if (!createResp.success || !createResp.data?.id) {
        ElMessage.error(createResp.message || '保存失败')
        return
      }

      const newConfigId = createResp.data.id as number
      const initResp = await indicatorWeightApi.initDefaultWeights(newConfigId)
      if (!initResp.success) {
        ElMessage.error(initResp.message || '保存失败')
        return
      }

      const weightsResp = await indicatorWeightApi.getByConfigId(newConfigId)
      if (!weightsResp.success) {
        ElMessage.error(weightsResp.message || '保存失败')
        return
      }

      const tempByCode = new Map<string, any>()
      ;(weightList.value || []).forEach((w: any) => tempByCode.set(w.indicatorCode, w))
      const payload = (weightsResp.data || []).map((w: any) => ({
        id: w.id,
        weight: tempByCode.get(w.indicatorCode)?.weight ?? 0
      }))
      const updateResp = await indicatorWeightApi.batchUpdate(payload)
      if (!updateResp.success) {
        ElMessage.error(updateResp.message || '保存失败')
        return
      }

      ElMessage.success('保存成功')
      dialogVisible.weightEditor = false
      delete tempWeightCache[currentWeightConfig.value._tempKey]
      await getConfigList()
      return
    }

    if (!selectedConfigId.value) return
    const payload = (weightList.value || []).map((w: any) => ({ id: w.id, weight: w.weight }))
    const response = await indicatorWeightApi.batchUpdate(payload)
    if (response.success) {
      ElMessage.success('保存成功')
      dialogVisible.weightEditor = false
      await loadIndicatorWeights()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存权重失败:', error)
    ElMessage.error('保存失败')
  } finally {
    loading.submit = false
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

const generateYearOptions = () => {
  const currentYear = new Date().getFullYear()
  const options: number[] = []
  // 倒序生成年份选项：2026, 2025, 2024... 2020
  for (let year = currentYear; year >= 2020; year--) {
    options.push(year)
  }
  yearOptions.value = options
}

const getOrganizationList = async () => {
  loading.organizations = true
  try {
    const response = await organizationApi.getTree({ year: orgYear.value || undefined })
    if (response.success && response.data) {
      organizationList.value = response.data || []
      // 收集需要展开的节点key
      defaultExpandedKeys.value = collectExpandedKeys(organizationList.value)
      console.log('组织机构树形数据 (年份:', orgYear.value, '):', organizationList.value)

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

// 选中第一个组织机构
const selectFirstOrganization = async () => {
  if (!organizationList.value || organizationList.value.length === 0) {
    return
  }
  // 默认选中第一个省级节点
  const firstOrg = organizationList.value[0]
  selectedOrg.value = firstOrg
  await nextTick()
  orgTreeRef.value?.setCurrentKey(firstOrg.code)
  // 保存到全局 store
  globalOrganizationStore.setOrganization({
    code: firstOrg.code,
    name: firstOrg.name,
    level: firstOrg.level
  })
  // 加载配置列表
  getConfigList()
}

// 刷新组织机构树
const refreshOrganizations = async () => {
  // 更新全局年份 store
  if (orgYear.value) {
    globalYearStore.setYear(orgYear.value)
  }
  await getOrganizationList()
  await getConfigList()
}

// 处理组织机构节点点击
const handleOrgNodeClick = (data: any) => {
  console.log('选中组织机构:', data)
  selectedOrg.value = data
  // 保存到全局 store
  globalOrganizationStore.setOrganization({
    code: data.code,
    name: data.name,
    level: data.level
  })
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
    const response = await weightConfigApi.getByName(configSearch.value, { year: orgYear.value || undefined })
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
  if (selectedOrg.value?.code) {
    configForm.orgcode = selectedOrg.value.code
  }
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
    orgcode: '',
    createTime: null
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
        if (orgYear.value) {
          configForm.createTime = `${orgYear.value}-01-01T00:00:00`
        }
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
    const response = await indicatorWeightApi.update({ ...row, weight: row.weight })
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
    const response = await indicatorWeightApi.validate(weightList.value as any[])
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

// ========== 专家打分相关方法 ==========

// 打开打分对话框
const openScoreDialog = async (row: any) => {
  try {
    currentScoreConfig.value = row
    scoreForm.configId = row.id
    scoreForm.orgcode = row.orgcode || ''  // 保存配置的组织机构代码
    scoreForm.expertName = ''
    scoreForm.expertPhone = ''

    // 加载该配置的所有指标权重
    const response = await indicatorWeightApi.getByConfigId(row.id)
    if (response.success && response.data) {
      // 初始化打分数据
      scoreForm.scores = response.data.map((item: any) => ({
        id: item.id,  // 添加 id 字段，用于构建树形结构
        indicatorCode: item.indicatorCode,
        indicatorName: item.indicatorName,
        indicatorLevel: item.indicatorLevel,
        parentId: item.parentId,
        weight: 0,  // 初始权重为0
        currentWeight: item.weight  // 保存当前权重值作为参考
      }))

      dialogVisible.score = true
    } else {
      ElMessage.error('获取指标列表失败')
    }
  } catch (error) {
    console.error('打开打分对话框失败:', error)
    ElMessage.error('打开打分对话框失败')
  }
}

// 提交打分
const submitScore = async () => {
  // 验证专家信息
  if (!scoreForm.expertName) {
    ElMessage.warning('请输入专家姓名')
    return
  }

  const eps = 0.001
  const level1Items = scoreForm.scores.filter((s: any) => s.indicatorLevel === 1)
  const level2Items = scoreForm.scores.filter((s: any) => s.indicatorLevel === 2)

  const level1Sum = level1Items.reduce((sum: number, item: any) => sum + (item.weight || 0), 0)
  if (Math.abs(level1Sum - 1) > eps) {
    ElMessage.warning(`一级指标权重总和必须为1，当前为 ${level1Sum.toFixed(2)}`)
    return
  }

  const byId = new Map<any, any>()
  scoreForm.scores.forEach((s: any) => byId.set(s.id, s))

  for (const parent of level1Items) {
    const children = level2Items.filter((c: any) => c.parentId === parent.id)
    if (!children.length) continue

    const childSum = children.reduce((sum: number, item: any) => sum + (item.weight || 0), 0)
    const parentWeight = parent.weight || 0
    const hasAnyChildWeight = children.some((c: any) => (c.weight || 0) > eps)

    if (parentWeight > eps || hasAnyChildWeight) {
      if (Math.abs(childSum - 1) > eps) {
        ElMessage.warning(`${parent.indicatorName} 下二级指标权重总和必须为1，当前为 ${childSum.toFixed(2)}`)
        return
      }
    } else {
      if (childSum > eps) {
        ElMessage.warning(`${parent.indicatorName} 权重为0时，其二级指标权重应全部为0`)
        return
      }
    }
  }

  loading.submit = true
  try {
    // 构建打分记录
    const scores = scoreForm.scores.map(item => ({
      configId: scoreForm.configId,
      orgcode: scoreForm.orgcode,  // 包含组织机构代码
      indicatorCode: item.indicatorCode,
      weight: item.weight,
      expertName: scoreForm.expertName,
      expertPhone: scoreForm.expertPhone
    }))

    const response = await indicatorWeightScoreApi.saveScores(scores)
    if (response.success) {
      ElMessage.success('打分提交成功')
      dialogVisible.score = false
    } else {
      ElMessage.error(response.message || '打分提交失败')
    }
  } catch (error) {
    console.error('提交打分失败:', error)
    ElMessage.error('提交打分失败')
  } finally {
    loading.submit = false
  }
}

// 打开统计详情对话框
const openStatisticsDialog = async (row: any) => {
  try {
    currentScoreConfig.value = row
    selectedExpert.value = null  // 清空选中的专家
    loading.submit = true

    // 获取打分统计信息
    const response = await indicatorWeightScoreApi.getScoreStatistics(row.id)
    if (response.success) {
      statisticsData.value = response.data
      if (statisticsData.value?.experts?.length === 1) {
        selectedExpert.value = statisticsData.value.experts[0]
      }
      dialogVisible.statistics = true
    } else {
      ElMessage.error(response.message || '获取统计信息失败')
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
    ElMessage.error('获取统计信息失败')
  } finally {
    loading.submit = false
  }
}

// 选择专家
const getExpertKey = (expert: any) => `${expert?.expert_name || ''}__${expert?.expert_phone || ''}`

const selectedExpertKey = computed(() => getExpertKey(selectedExpert.value))

const selectExpert = (expert: any) => {
  if (selectedExpertKey.value === getExpertKey(expert)) {
    selectedExpert.value = null
    return
  }
  selectedExpert.value = expert
}

// 应用平均权重到正式配置
const applyAverageWeights = async () => {
  if (!currentScoreConfig.value) return

  try {
    await ElMessageBox.confirm('确定要将平均权重应用到正式配置吗？这将覆盖当前的权重值。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    loading.submit = true
    const response = await indicatorWeightScoreApi.applyAverageWeights(currentScoreConfig.value.id)
    if (response.success) {
      ElMessage.success('平均权重应用成功')
      dialogVisible.statistics = false
      // 刷新配置列表
      getConfigList()
    } else {
      ElMessage.error(response.message || '应用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('应用平均权重失败:', error)
      ElMessage.error('应用失败')
    }
  } finally {
    loading.submit = false
  }
}

// 构建打分的树形数据
const scoreTreeData = computed(() => {
  if (!scoreForm.scores.length) return []

  const nodeMap = new Map()
  const rootNodes: any[] = []

  // 第一步：清空所有 children 并建立映射
  scoreForm.scores.forEach(item => {
    // 直接使用原对象，不创建新对象，这样修改 weight 会直接影响原数据
    item.children = []  // 每次重新构建树时清空 children
    nodeMap.set(item.id, item)
  })

  console.log('打分数据总数:', scoreForm.scores.length)
  console.log('所有节点ID映射:', Array.from(nodeMap.keys()))

  // 第二步：建立父子关系
  scoreForm.scores.forEach(item => {
    // 使用 !== null && !== undefined 来判断是否有父节点，避免 parentId 为 0 时被误判
    if (item.parentId !== null && item.parentId !== undefined) {
      if (nodeMap.has(item.parentId)) {
        // 有父节点，添加到父节点的 children 中
        const parentNode = nodeMap.get(item.parentId)
        parentNode.children.push(item)
        console.log(`节点 ${item.indicatorCode} (id=${item.id}) 作为子节点添加到父节点 (parentId=${item.parentId})`)
      } else {
        // 父节点不存在，作为根节点
        rootNodes.push(item)
        console.warn(`节点 ${item.indicatorCode} (id=${item.id}) 的父节点不存在 (parentId=${item.parentId})，作为根节点`)
      }
    } else {
      // 没有父节点，作为根节点
      rootNodes.push(item)
      console.log(`节点 ${item.indicatorCode} (id=${item.id}) 没有父节点，作为根节点`)
    }
  })

  console.log('根节点数量:', rootNodes.length)
  console.log('根节点列表:', rootNodes.map(n => `${n.indicatorCode}(id=${n.id})`))

  return rootNodes
})

// 组件挂载时获取数据
onMounted(() => {
  console.log('WeightConfig组件已挂载，开始加载数据')
  generateYearOptions()
  getConfigList()
  getOrganizationList() // 获取组织机构列表
})
</script>

<style scoped>
.weight-config {
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

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.org-year-select {
  width: 110px;
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
  flex-wrap: wrap;
}

/* 数据来源提示 */
.data-source-notice {
  margin-bottom: 16px;
}

.data-source-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
  margin-top: 4px;
}

.data-source-item .config-name {
  font-weight: 500;
}

.data-source-item .year-label {
  color: #909399;
  font-size: 12px;
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
  height: 100%;
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

/* ========== 专家打分对话框样式 ========== */
.score-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.expert-info-card,
.score-tree-card {
  margin-bottom: 0;
}

.card-header-with-tip {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.score-tree-container {
  max-height: 350px;
  overflow-y: auto;
  padding: 16px;
  background: linear-gradient(to bottom, #fafbfc, #ffffff);
  border-radius: 8px;
  border: 1px solid #eef2f7;
}

/* 打分树形结构样式 - 复用指标权重管理的样式 */
.score-weight-tree {
  width: 100%;
}

.score-weight-tree .tree-node {
  width: 100%;
  padding: 0;
}

.score-weight-tree .node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 32px;
  padding: 4px 12px;
  border: 1px solid #e6edf5;
  border-radius: 6px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  margin: 2px 0;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.score-weight-tree .node-content::before {
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

.score-weight-tree .node-content:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateX(2px);
}

.score-weight-tree .node-content:hover::before {
  opacity: 1;
}

.score-weight-tree .node-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.score-weight-tree .node-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  font-weight: 600;
  color: #337ecc;
  background: rgba(51, 126, 204, 0.08);
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid rgba(51, 126, 204, 0.25);
  min-width: 80px;
  text-align: center;
  white-space: nowrap;
}

.score-weight-tree .node-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 250px;
}

.score-weight-tree .level-tag {
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
}

.score-weight-tree .current-weight-ref {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
  background: rgba(103, 194, 58, 0.1);
  padding: 3px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.score-weight-tree .current-weight-ref .el-icon {
  font-size: 14px;
}

.score-weight-tree .node-weight {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.score-weight-tree .weight-label {
  font-size: 12px;
  color: #606266;
  font-weight: 600;
  white-space: nowrap;
}

.score-weight-tree .weight-input {
  width: 130px;
}

.score-weight-tree .weight-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #e8eef5 inset;
  transition: all 0.3s ease;
}

.score-weight-tree .weight-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.score-weight-tree .weight-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

/* 树形节点层级样式 */
.score-weight-tree :deep(.el-tree-node__content) {
  height: auto !important;
  padding: 0 !important;
}

.score-weight-tree :deep(.el-tree-node__expand-icon) {
  font-size: 14px;
  color: #909399;
  transition: all 0.3s ease;
  margin-top: 4px;
}

.score-weight-tree :deep(.el-tree-node__expand-icon:hover) {
  color: #409eff;
}

/* 子节点样式增强 - 不同层级不同颜色 */
.score-weight-tree :deep(.el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 24px;
  margin-top: 2px;
  margin-bottom: 2px;
  border-left: 3px solid #409eff;
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
}

.score-weight-tree :deep(.el-tree-node__children .el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 48px;
  border-left-color: #67c23a;
  background: linear-gradient(135deg, #f6fff8 0%, #ffffff 100%);
}

.score-weight-tree :deep(.el-tree-node__children .el-tree-node__children .el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 72px;
  border-left-color: #e6a23c;
  background: linear-gradient(135deg, #fef9f0 0%, #ffffff 100%);
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .score-weight-tree .node-name {
    max-width: 180px;
  }

  .score-weight-tree .weight-input {
    width: 110px;
  }
}

/* ========== 统计详情对话框样式（新版） ========== */
.statistics-content-new {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 统计对话框头部 */
.statistics-dialog-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.statistics-dialog-header .header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.statistics-dialog-header .header-info {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.statistics-dialog-header .header-info .el-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 顶部平均分表格 */
.average-table-card {
  margin-bottom: 0;
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.level1-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.level1-cell .indicator-name {
  font-weight: 600;
  color: #303133;
}

.weight-value {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
}

.level2-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}

.level2-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  transition: background 0.3s;
}

.level2-item:hover {
  background: #e8eef5;
}

.level2-item .indicator-name {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.weight-badge {
  font-weight: 600;
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.score-count {
  font-size: 12px;
  color: #909399;
}

/* 底部左右布局 */
.bottom-layout {
  display: flex;
  gap: 16px;
  height: 500px;
}

/* 左侧专家列表 */
.experts-list-card {
  width: 300px;
  flex-shrink: 0;
}

.expert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  border: 1px solid #e8eef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.expert-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
  transform: translateX(4px);
}

.expert-item.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.expert-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.expert-icon {
  font-size: 24px;
  color: #409eff;
}

.expert-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.expert-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.expert-phone {
  font-size: 12px;
  color: #909399;
}

.check-icon {
  font-size: 20px;
  color: #67c23a;
}

/* 右侧专家打分详情 */
.expert-score-card {
  flex: 1;
}

.expert-score-tree-container {
  padding: 16px;
}

/* 专家打分树形结构样式 - 复用权重配置树的样式 */
.expert-score-tree {
  width: 100%;
}

.expert-score-tree .tree-node {
  width: 100%;
  padding: 0;
}

.expert-score-tree .node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 32px;
  padding: 4px 12px;
  border: 1px solid #e6edf5;
  border-radius: 6px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  margin: 2px 0;
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;
}

.expert-score-tree .node-content::before {
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

.expert-score-tree .node-content:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateX(2px);
}

.expert-score-tree .node-content:hover::before {
  opacity: 1;
}

.expert-score-tree .node-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.expert-score-tree .node-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  font-weight: 600;
  color: #337ecc;
  background: rgba(51, 126, 204, 0.08);
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid rgba(51, 126, 204, 0.25);
  min-width: 80px;
  text-align: center;
  white-space: nowrap;
}

.expert-score-tree .node-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 250px;
}

.expert-score-tree .level-tag {
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
}

.expert-score-tree .node-weight {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.expert-score-tree .weight-label {
  font-size: 12px;
  color: #606266;
  font-weight: 600;
  white-space: nowrap;
}

.expert-score-tree .weight-value-display {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
  font-family: 'Monaco', 'Menlo', monospace;
}

/* 树形节点层级样式 */
.expert-score-tree :deep(.el-tree-node__content) {
  height: auto !important;
  padding: 0 !important;
}

.expert-score-tree :deep(.el-tree-node__expand-icon) {
  font-size: 14px;
  color: #909399;
  transition: all 0.3s ease;
  margin-top: 4px;
}

.expert-score-tree :deep(.el-tree-node__expand-icon:hover) {
  color: #409eff;
}

/* 子节点样式增强 - 不同层级不同颜色 */
.expert-score-tree :deep(.el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 24px;
  margin-top: 2px;
  margin-bottom: 2px;
  border-left: 3px solid #409eff;
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
}

.expert-score-tree :deep(.el-tree-node__children .el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 48px;
  border-left-color: #67c23a;
  background: linear-gradient(135deg, #f6fff8 0%, #ffffff 100%);
}

.expert-score-tree :deep(.el-tree-node__children .el-tree-node__children .el-tree-node__children > .el-tree-node .node-content) {
  margin-left: 72px;
  border-left-color: #e6a23c;
  background: linear-gradient(135deg, #fef9f0 0%, #ffffff 100%);
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .expert-score-tree .node-name {
    max-width: 180px;
  }

  .expert-score-tree .weight-value-display {
    font-size: 14px;
  }
}

/* 对话框内的卡片样式调整 */
:deep(.statistics-content-new .el-card__header) {
  background: #f5f7fa;
  padding: 12px 20px;
  font-weight: 600;
}
</style>
