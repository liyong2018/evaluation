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
    request.get('/api/survey-data/search', { params: { surveyName: keyword } }),
  
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
  exportData: () => request.get('/api/survey-data/export/all', {
    responseType: 'blob'
  })
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
  })
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
  }) => request.post('/api/algorithm/execution/step/calculate', data)
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