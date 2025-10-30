import request from '@/utils/request'

// 系统相关API
export const systemApi = {
  // 获取系统信息
  getSystemInfo: () => request.get('/'),
  
  // 健康检查
  healthCheck: () => request.get('/health')
}

// 调查数据相关API
export const surveyDataApi = {
  // 获取所有调查数据
  getAll: () => request.get('/api/survey-data'),
  
  // 根据ID获取调查数据
  getById: (id: number) => request.get(`/api/survey-data/${id}`),
  
  // 根据调查名称获取数据
  getBySurveyName: (surveyName: string) => request.get(`/api/survey-data/survey/${surveyName}`),
  
  // 根据地区获取数据
  getByRegion: (region: string) => request.get(`/api/survey-data/region/${region}`),
  
  // 搜索调查数据
  search: (keyword: string) => 
    request.get('/api/survey-data/search', { params: { keyword: keyword } }),
  
  // 创建调查数据
  create: (data: any) => request.post('/api/survey-data', data),
  
  // 批量创建调查数据
  batchCreate: (dataList: any[]) => request.post('/api/survey-data/batch', dataList),
  
  // 更新调查数据
  update: (data: any) => request.put('/api/survey-data', data),
  
  // 删除调查数据
  delete: (id: number) => request.delete(`/api/survey-data/${id}`),
  
  // 根据调查名称删除数据
  deleteBySurveyName: (surveyName: string) => request.delete(`/api/survey-data/survey/${surveyName}`),
  
  // 导入Excel文件
  importData: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/survey-data/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  // 导出Excel文件
  exportData: () => request.get('/api/survey-data/export/all')
}

// 权重配置相关API
export const weightConfigApi = {
  // 获取所有权重配置
  getAll: () => request.get('/api/weight-config'),
  
  // 根据ID获取权重配置
  getById: (id: number) => request.get(`/api/weight-config/${id}`),
  
  // 根据名称获取权重配置
  getByName: (configName: string) => request.get(`/api/weight-config/name/${configName}`),
  
  // 获取激活的权重配置
  getActive: () => request.get('/api/weight-config/active'),
  
  // 创建权重配置
  create: (data: any) => request.post('/api/weight-config', data),
  
  // 更新权重配置
  update: (data: any) => request.put('/api/weight-config', data),
  
  // 删除权重配置
  delete: (id: number) => request.delete(`/api/weight-config/${id}`),
  
  // 激活权重配置
  activate: (id: number) => request.post(`/api/weight-config/activate/${id}`),
  
  // 停用权重配置
  deactivate: (id: number) => request.post(`/api/weight-config/deactivate/${id}`),
  
  // 复制权重配置
  copy: (id: number, newConfigName: string) => 
    request.post(`/api/weight-config/copy/${id}`, null, { params: { newConfigName } }),
  
  // 验证权重配置
  validate: (data: any) => request.post('/api/weight-config/validate', data)
}

// 指标权重相关API
export const indicatorWeightApi = {
  // 获取所有指标权重
  getAll: () => request.get('/api/indicator-weight'),
  
  // 根据ID获取指标权重
  getById: (id: number) => request.get(`/api/indicator-weight/${id}`),
  
  // 根据配置ID获取指标权重
  getByConfigId: (configId: number) => request.get(`/api/indicator-weight/config/${configId}`),
  
  // 根据指标代码获取权重
  getByIndicatorCode: (indicatorCode: string) => request.get(`/api/indicator-weight/indicator/${indicatorCode}`),
  
  // 创建指标权重
  create: (data: any) => request.post('/api/indicator-weight', data),
  
  // 批量创建指标权重
  batchCreate: (dataList: any[]) => request.post('/api/indicator-weight/batch', dataList),
  
  // 更新指标权重
  update: (data: any) => request.put('/api/indicator-weight', data),
  
  // 删除指标权重
  delete: (id: number) => request.delete(`/api/indicator-weight/${id}`),
  
  // 验证权重配置
  validate: (weights: any[]) => request.post('/api/indicator-weight/validate', weights)
}

// 地区组织机构相关API
export const regionApi = {
  // 获取地区树形结构
  getRegionTree: () => request.get('/api/region/tree'),
  
  // 根据父级ID获取子级地区
  getRegionsByParentId: (parentId: number) => request.get(`/api/region/children/${parentId}`),
  
  // 根据级别获取地区列表
  getRegionsByLevel: (level: number) => request.get(`/api/region/level/${level}`),
  
  // 根据地区代码获取地区信息
  getRegionByCode: (code: string) => request.get(`/api/region/code/${code}`),
  
  // 根据地区ID列表获取地区信息
  getRegionsByIds: (ids: number[]) => request.post('/api/region/batch', ids),
  
  // 获取所有启用的地区
  getAllEnabledRegions: () => request.get('/api/region/all')
}

// 评估计算相关API
export const evaluationApi = {
  // 执行评估计算
  calculate: (params: { surveyId: number; algorithmId: number; weightConfigId: number }) => 
    request.post('/api/evaluation/calculate', null, { params }),
  
  // 重新计算评估
  recalculate: (params: { surveyId: number; algorithmId: number; weightConfigId: number }) => 
    request.post('/api/evaluation/recalculate', null, { params }),
  
  // 批量评估计算
  batchCalculate: (data: { surveyIds: number[]; algorithmId: number; weightConfigId: number }) => 
    request.post('/api/evaluation/batch', data.surveyIds, { 
      params: { algorithmId: data.algorithmId, weightConfigId: data.weightConfigId } 
    }),
  
  // 获取算法过程数据
  getProcessData: (params: { surveyId: number; algorithmId: number; weightConfigId: number }) => 
    request.get('/api/evaluation/process', { params }),
  
  // 获取评估历史记录
  getHistory: (surveyId?: number) => {
    if (surveyId) {
      return request.get(`/api/evaluation/history/${surveyId}`)
    } else {
      return request.get('/api/evaluation/history')
    }
  },
  
  // 验证评估参数
  validateParams: (params: { surveyId: number; algorithmId: number; weightConfigId: number }) => 
    request.post('/api/evaluation/validate', null, { params }),
  
  // 删除评估结果
  deleteResults: (params: { surveyId: number; algorithmId: number; weightConfigId: number }) => 
    request.delete('/api/evaluation/results', { params }),

  // 执行评估（用于前端调用）
  execute: (data: any) => request.post('/api/evaluation/calculate', null, { 
    params: { surveyId: 1, algorithmId: 1, weightConfigId: data.weightConfigId || 1 } 
  }),

  // 重新运行评估
  rerun: (id: number) => request.post('/api/evaluation/recalculate', null, { 
    params: { surveyId: id, algorithmId: 1, weightConfigId: 1 } 
  }),

  // 删除评估记录
  deleteResult: (id: number) => request.delete('/api/evaluation/results', { 
    params: { surveyId: id, algorithmId: 1, weightConfigId: 1 } 
  }),

  // 创建评估任务
  create: (data: any) => request.post('/api/evaluation/create', data),

  // 保存步骤结果
  saveStepResult: (data: { evaluationId: number; stepId: number; result: any }) => 
    request.post('/api/evaluation/step-result', data),

  // 完成评估
  finalize: (evaluationId: number) => request.post(`/api/evaluation/finalize/${evaluationId}`),

  // 执行评估模型（基于模型配置）
  executeModel: (modelId: number, regionCodes: string[], weightConfigId: number) => 
    request.post('/api/evaluation/execute-model', regionCodes, {
      params: { modelId, weightConfigId }
    }),

  // 生成评估结果二维表
  generateResultTable: (executionResults: any) => 
    request.post('/api/evaluation/generate-table', executionResults)
}

// 算法执行相关API
export const algorithmExecutionApi = {
  // 执行算法计算
  execute: (data: {
    algorithmId: number;
    surveyId?: number;
    regionIds?: number[];
    weightConfig?: Record<string, number>;
  }) => request.post('/api/algorithm/execution/execute', data),
  
  // 验证算法参数
  validate: (data: {
    algorithmId: number;
    parameters: Record<string, any>;
  }) => request.post('/api/algorithm/execution/validate', data),
  
  // 获取算法执行进度
  getProgress: (executionId: string) => request.get(`/api/algorithm/execution/progress/${executionId}`),
  
  // 停止算法执行
  stop: (executionId: string) => request.post(`/api/algorithm/execution/stop/${executionId}`),
  
  // 获取支持的算法类型
  getSupportedTypes: () => request.get('/api/algorithm/execution/types'),
  
  // 批量执行算法
  batchExecute: (data: {
    algorithmId: number;
    surveyIds: number[];
    regionIds?: number[];
    weightConfig?: Record<string, number>;
  }) => request.post('/api/algorithm/execution/batch', data),
  
  // 计算单个步骤结果
  calculateStepResult: (data: {
    algorithmId: number;
    stepId: number;
    stepIndex: number;
    regions?: number[];
    parameters?: Record<string, any>;
    formula?: string;
  }) => request.post('/api/algorithm/execution/step/calculate', data),

  // 执行单个算法步骤
  executeStep: (data: {
    evaluationId: number;
    stepId: number;
    algorithmId: number;
    regionIds: string[];
    parameters: Record<string, any>;
  }) => request.post('/api/algorithm/execution/step/execute', data)
}

// 算法配置相关API
export const algorithmConfigApi = {
  // 获取所有算法配置
  getAll: () => request.get('/api/algorithm-config'),
  
  // 根据ID获取算法配置
  getById: (id: number) => request.get(`/api/algorithm-config/${id}`),
  
  // 获取默认算法配置
  getDefault: () => request.get('/api/algorithm-config/default'),
  
  // 创建算法配置
  create: (data: any) => request.post('/api/algorithm-config', data),
  
  // 更新算法配置
  update: (data: any) => request.put('/api/algorithm-config', data),
  
  // 删除算法配置
  delete: (id: number) => request.delete(`/api/algorithm-config/${id}`)
}

// 算法管理相关API
export const algorithmManagementApi = {
  // 获取算法列表
  getList: () => request.get('/api/algorithm/management/list'),
  
  // 获取算法详情
  getDetail: (algorithmId: number) => request.get(`/api/algorithm/management/detail/${algorithmId}`),
  
  // 创建算法配置
  create: (data: {
    configName: string;
    description: string;
    version: string;
    steps: any[];
    formulas: any[];
  }) => request.post('/api/algorithm/management/create', data),
  
  // 更新算法配置
  update: (data: {
    id: number;
    configName: string;
    description: string;
    version: string;
    steps: any[];
    formulas: any[];
  }) => request.put('/api/algorithm/management/update', data),
  
  // 删除算法配置
  delete: (algorithmId: number) => request.delete(`/api/algorithm/management/delete/${algorithmId}`),
  
  // 获取算法步骤
  getSteps: (algorithmId: number) => request.get(`/api/algorithm/management/steps/${algorithmId}`),
  
  // 获取算法步骤和公式（用于评估计算页面展示）
  getAlgorithmStepsAndFormulas: (algorithmId: number) => request.get(`/api/algorithm/management/steps/${algorithmId}`),
  
  // 创建算法步骤
  createStep: (step: any) => request.post('/api/algorithm/management/step/create', step),
  
  // 更新算法步骤
  updateStep: (step: any) => request.put('/api/algorithm/management/step/update', step),
  
  // 删除算法步骤
  deleteStep: (stepId: number) => request.delete(`/api/algorithm/management/step/delete/${stepId}`),
  
  // 批量更新算法步骤
  batchUpdateSteps: (steps: any[]) => request.put('/api/algorithm/management/steps/batch', steps),
  
  // 获取公式配置
  getFormulas: (formulaType?: string) => request.get('/api/algorithm/management/formulas', {
    params: { formulaType }
  }),
  
  // 创建公式配置
  createFormula: (formula: any) => request.post('/api/algorithm/management/formula/create', formula),
  
  // 更新公式配置
  updateFormula: (formula: any) => request.put('/api/algorithm/management/formula/update', formula),
  
  // 删除公式配置
  deleteFormula: (formulaId: number) => request.delete(`/api/algorithm/management/formula/delete/${formulaId}`),
  
  // 验证公式表达式
  validateFormula: (expression: string) => request.post('/api/algorithm/management/formula/validate', { expression }),
  
  // 复制算法配置
  copy: (sourceAlgorithmId: number, newAlgorithmName: string) => 
    request.post(`/api/algorithm/management/copy/${sourceAlgorithmId}`, { newAlgorithmName }),
  
  // 导入算法配置
  import: (algorithmData: any) => request.post('/api/algorithm/management/import', algorithmData),
  
  // 导出算法配置
  export: (algorithmId: number) => request.get(`/api/algorithm/management/export/${algorithmId}`)
}

// 专题图相关API
export const thematicMapApi = {
  // 获取专题图数据
  getThematicData: (params: {
    reportId?: number;
    regionIds?: number[];
    surveyId?: number;
    algorithmId?: number;
  }) => request.get('/api/thematic-map/data', { params }),
  
  // 获取地区边界数据
  getRegionBoundaries: (regionIds: number[]) => 
    request.post('/api/thematic-map/boundaries', regionIds),
  
  // 保存专题图图片
  saveMapImage: (data: {
    imageData: string;
    format: string;
    reportId?: number;
    title: string;
    description?: string;
  }) => request.post('/api/thematic-map/save-image', data),
  
  // 获取专题图历史记录
  getMapHistory: (reportId?: number) => {
    if (reportId) {
      return request.get(`/api/thematic-map/history/${reportId}`)
    } else {
      return request.get('/api/thematic-map/history')
    }
  },
  
  // 删除专题图记录
  deleteMapRecord: (id: number) => request.delete(`/api/thematic-map/${id}`),
  
  // 获取专题图配置
  getMapConfig: () => request.get('/api/thematic-map/config'),
  
  // 更新专题图配置
  updateMapConfig: (config: any) => request.put('/api/thematic-map/config', config),
  
  // 获取天地图配置
  getTiandituConfig: () => request.get('/api/thematic-map/tianditu-config')
}

// 算法步骤执行相关API
export const algorithmStepExecutionApi = {
  // 获取算法步骤信息
  getAlgorithmSteps: (algorithmId: number) => 
    request.get(`/api/algorithm-step-execution/${algorithmId}/steps`),
  
  // 执行单个步骤
  executeStep: (algorithmId: number, stepOrder: number, data: {
    regionCodes: string[];
    weightConfigId?: number;
  }) => 
    request.post(`/api/algorithm-step-execution/${algorithmId}/step/${stepOrder}/execute`, data),
  
  // 批量执行步骤（直到指定步骤）
  executeStepsUpTo: (algorithmId: number, upToStepOrder: number, data: {
    regionCodes: string[];
    weightConfigId?: number;
  }) => 
    request.post(`/api/algorithm-step-execution/${algorithmId}/steps/execute-up-to/${upToStepOrder}`, data),
  
  // 获取算法详细信息
  getAlgorithmDetail: (algorithmId: number) => 
    request.get(`/api/algorithm-step-execution/${algorithmId}/detail`),
  
  // 获取算法列表
  getAlgorithms: () => 
    request.get('/api/algorithm-step-execution/algorithms'),
  
  // 验证执行参数
  validateParams: (algorithmId: number, data: {
    regionCodes: string[];
    weightConfigId?: number;
  }) => 
    request.post(`/api/algorithm-step-execution/${algorithmId}/validate-params`, data)
}

// 社区行政村减灾能力相关API
export const communityCapacityApi = {
  // 导入社区行政村减灾能力数据
  importData: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/community-capacity/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 获取社区行政村减灾能力数据列表
  getList: (params: {
    page?: number;
    size?: number;
    regionCode?: string;
    communityName?: string;
  }) => request.get('/api/community-capacity/list', { params }),

  // 搜索社区行政村减灾能力数据
  search: (params: {
    keyword?: string;
    regionCode?: string;
    communityName?: string;
  }) => request.get('/api/community-capacity/search', { params }),

  // 根据ID获取社区行政村减灾能力数据
  getById: (id: number) => request.get(`/api/community-capacity/${id}`),

  // 更新社区行政村减灾能力数据
  update: (id: number, data: any) => request.put(`/api/community-capacity/${id}`, data),

  // 删除社区行政村减灾能力数据
  delete: (id: number) => request.delete(`/api/community-capacity/${id}`),

  // 批量删除社区行政村减灾能力数据
  batchDelete: (ids: number[]) => request.delete('/api/community-capacity/batch', { data: ids }),

  // 下载导入模板
  downloadTemplate: () => request.get('/api/community-capacity/template')
}

// 地区数据相关API（三级联动）
export const regionDataApi = {
  // 根据数据类型获取省份列表
  getProvinces: (dataType: string) =>
    request.get('/api/region/provinces', { params: { dataType } }),

  // 根据省份名称获取城市列表
  getCities: (dataType: string, provinceName: string) =>
    request.get('/api/region/cities', { params: { dataType, provinceName } }),

  // 根据城市名称获取区县列表
  getCounties: (dataType: string, provinceName: string, cityName: string) =>
    request.get('/api/region/counties', { params: { dataType, provinceName, cityName } }),

  // 根据选择的县获取对应的数据
  getDataByCounty: (dataType: string, provinceName: string, cityName: string, countyName: string) =>
    request.get('/api/region/data', { params: { dataType, provinceName, cityName, countyName } })
}

// 模型管理相关API
export const modelManagementApi = {
  // 获取所有评估模型
  getAllModels: () => request.get('/api/model-management/models'),
  
  // 根据ID获取评估模型
  getModelById: (id: number) => request.get(`/api/model-management/models/${id}`),

  // 获取模型详情（包含步骤与算法）
  getModelDetail: (modelId: number) => request.get(`/api/model-management/models/${modelId}/detail`),
  
  // 创建评估模型
  createModel: (data: any) => request.post('/api/model-management/models', data),
  
  // 更新评估模型
  updateModel: (data: any) => request.put('/api/model-management/models', data),
  
  // 删除评估模型
  deleteModel: (id: number) => request.delete(`/api/model-management/models/${id}`),
  
  // 获取模型步骤
  getModelSteps: (modelId: number) => request.get(`/api/model-management/models/${modelId}/steps`),
  
  // 创建模型步骤
  createModelStep: (data: any) => request.post('/api/model-management/steps', data),
  
  // 更新模型步骤
  updateModelStep: (data: any) => request.put('/api/model-management/steps', data),
  
  // 删除模型步骤
  deleteModelStep: (id: number) => request.delete(`/api/model-management/steps/${id}`),
  
  // 获取步骤算法
  getStepAlgorithms: (stepId: number) => request.get(`/api/model-management/steps/${stepId}/algorithms`),
  
  // 创建步骤算法
  createStepAlgorithm: (data: any) => request.post('/api/model-management/algorithms', data),
  
  // 更新步骤算法
  updateStepAlgorithm: (data: any) => request.put('/api/model-management/algorithms', data),
  
  // 删除步骤算法
  deleteStepAlgorithm: (id: number) => request.delete(`/api/model-management/algorithms/${id}`),
  
  // 验证QLExpress表达式
  validateQLExpression: (data: { expression: string; context?: any }) => 
    request.post('/api/model-management/validate-expression', data),
  
  // 测试QLExpress表达式
  testQLExpression: (data: { expression: string; context: any }) => 
    request.post('/api/model-management/test-expression', data)
}
