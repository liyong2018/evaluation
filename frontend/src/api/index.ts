import request from '@/utils/request'

// 工具相关API
export const systemApi = {
  // 获取工具信息
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

  // 批量删除调查数据
  batchDelete: (ids: number[]) => request.delete('/api/survey-data/batch', { data: ids }),

  // 导入Excel文件
  importData: (file: File, year: number) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('year', year.toString())
    return request.post('/api/survey-data/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 检查导入前置条件
  checkImportPrerequisites: (year: number) => request({
    method: 'post',
    url: '/api/survey-data/check-import-prerequisites',
    params: { year }
  }),

  // 导出Excel文件
  exportData: () => request.get('/api/survey-data/export/all')
}

// 权重配置相关API
export const weightConfigApi = {
  // 获取所有权重配置（支持按组织机构过滤）
  getAll: (orgcode?: string) => {
    if (orgcode) {
      return request.get('/api/weight-config', { params: { orgcode } })
    }
    return request.get('/api/weight-config')
  },
  
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

// 专家权重打分记录相关API
export const indicatorWeightScoreApi = {
  // 保存专家打分记录（批量）
  saveScores: (scores: any[]) => request.post('/api/indicator-weight-score/batch', scores),

  // 获取指定配置的所有专家打分记录
  getScoresByConfigId: (configId: number) => request.get(`/api/indicator-weight-score/config/${configId}`),

  // 获取指定配置和指标的所有专家打分记录
  getScoresByConfigIdAndIndicatorCode: (configId: number, indicatorCode: string) =>
    request.get(`/api/indicator-weight-score/config/${configId}/indicator/${indicatorCode}`),

  // 计算指定配置下每个指标的平均权重值
  calculateAverageWeights: (configId: number) =>
    request.get(`/api/indicator-weight-score/config/${configId}/average`),

  // 获取指定配置下的所有专家列表（去重）
  getExpertsByConfigId: (configId: number) =>
    request.get(`/api/indicator-weight-score/config/${configId}/experts`),

  // 获取指定配置的打分统计信息
  getScoreStatistics: (configId: number) =>
    request.get(`/api/indicator-weight-score/config/${configId}/statistics`),

  // 将平均权重应用到正式的 indicator_weight 表
  applyAverageWeights: (configId: number) =>
    request.post(`/api/indicator-weight-score/config/${configId}/apply-average`)
}

// 地区组织机构相关API (已废弃 - 使用 regionDataApi 替代)
// 现在区域数据从 survey_data 和 community_disaster_reduction_capacity 表获取
/*
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
*/

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

  // 执行评估模型（基于模型配置，异步执行）
  executeModel: (modelId: number, regionCodes: string[], weightConfigId: number, year?: number, orgCode?: string, createBy?: string) => {
    const requestBody = {
      modelId,
      regionCodes,
      weightConfigId,
      year,
      orgCode,
      createBy
    };
    // 异步执行，立即返回执行记录ID，无需设置超时
    return request.post('/api/evaluation/execute-model', requestBody);
  },

  // 检查评估数据是否存在
  checkEvaluationData: (params: {
    modelId: number;
    regionCodes: string[];
    year: number;
    orgCode?: string;
  }) => request.get('/api/evaluation/check-data', { params }),

  // 生成评估结果二维表
  generateResultTable: (executionResults: any) =>
    request.post('/api/evaluation/generate-table', executionResults),

  // 获取评估历史列表（支持筛选）
  getEvaluationHistoryList: (params: {
    page?: number;
    size?: number;
    modelId?: number;
    executionStatus?: string;
    year?: number;
    orgCode?: string;
  }) => request.get('/api/evaluation/history', { params }),

  // 删除评估历史记录
  deleteEvaluationHistory: (id: number) => request.delete(`/api/evaluation/history/${id}`)
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
    regions?: Array<string | number>;
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
    year?: number;
    orgCode?: string;
    level?: string;
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
  getTiandituConfig: () => request.get('/api/thematic-map/tianditu-config'),

  // 上传专题图图片
  uploadMapImage: (imageFile: File, year: number, orgCode: string) => {
    const formData = new FormData()
    formData.append('image', imageFile)
    formData.append('year', year.toString())
    formData.append('orgCode', orgCode)
    return request.post('/api/thematic-map/upload-map-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传专题图图片（支持指定级别）
  uploadMapImageWithLevel: (imageFile: File, year: number, orgCode: string, level: string) => {
    const formData = new FormData()
    formData.append('image', imageFile)
    formData.append('year', year.toString())
    formData.append('orgCode', orgCode)
    formData.append('level', level)
    return request.post('/api/thematic-map/upload-map-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 生成包含专题图的报告（OnlyOffice图片替换）
  generateReportWithMap: (imagePath: string, year: number, orgCode: string) => {
    return request({
      url: '/api/thematic-map/generate-report-with-map',
      method: 'POST',
      params: { imagePath, year, orgCode },
      responseType: 'blob'
    })
  }
}

// TOPSIS配置相关API
export const topsisConfigApi = {
  getAll: () => request.get('/api/topsis-config'),

  getByModelAndStep: (modelId: number, stepId: number) =>
    request.get('/api/topsis-config/by-model-step', { params: { modelId, stepId } }),

  getAvailableIndicators: (modelId: number) =>
    request.get('/api/topsis-config/available-indicators', { params: { modelId } }),

  updateStepConfig: (data: {
    stepId: number
    indicators: string[]
    modelId?: number
    algorithmCode?: string
  }) => request.post('/api/topsis-config/update', data),

  validateConfig: (data: { modelId: number; stepId: number; indicators: string[] }) =>
    request.post('/api/topsis-config/validate', data),

  testConfig: (data: {
    config: { modelId: number; stepId: number; indicators: string[] }
    regionCodes: string[]
    weightConfigId: number
  }) => request.post('/api/topsis-config/test', data)
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
  importData: (file: File, year: number) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('year', year.toString())
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
    year?: number;
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

// 医疗卫生机构相关API
export const medicalInstitutionApi = {
  // 导入医疗卫生机构数据
  importData: (file: File, year: number) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('year', year.toString())
    return request.post('/api/medical-institution/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 获取医疗卫生机构数据列表
  getList: (year: number) => request.get('/api/medical-institution/list', { params: { year } }),

  // 搜索医疗卫生机构数据
  search: (institutionName: string) => request.get('/api/medical-institution/search', {
    params: { institutionName }
  }),

  // 根据ID获取医疗卫生机构数据
  getById: (id: number) => request.get(`/api/medical-institution/${id}`),

  // 创建医疗卫生机构数据
  create: (data: any) => request.post('/api/medical-institution', data),

  // 更新医疗卫生机构数据
  update: (data: any) => request.put('/api/medical-institution', data),

  // 删除医疗卫生机构数据
  delete: (id: number) => request.delete(`/api/medical-institution/${id}`),

  // 批量删除医疗卫生机构数据
  batchDelete: (ids: number[]) => request.delete('/api/medical-institution/batch', { data: ids }),

  // 导出医疗卫生机构数据
  exportData: (year: number) => request.get('/api/medical-institution/export', {
    params: { year },
    responseType: 'blob'
  }),

  // 下载导入模板
  downloadTemplate: () => request.get('/api/medical-institution/template', {
    responseType: 'blob'
  })
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

// 模型执行记录API
export const modelExecutionRecordApi = {
  // 获取执行记录列表（分页）
  getList: (params: { 
    current?: number; 
    size?: number; 
    modelId?: number; 
    executionStatus?: string 
  }) => request.get('/api/model-execution-record/list', { params }),
  
  // 根据ID获取执行记录详情
  getById: (id: number) => request.get(`/api/model-execution-record/${id}`),
  
  // 根据执行记录ID获取评估结果
  getResults: (id: number) => request.get(`/api/model-execution-record/${id}/results`),
  
  // 删除执行记录
  delete: (id: number) => request.delete(`/api/model-execution-record/${id}`),
  
  // 获取执行记录统计信息
  getStatistics: () => request.get('/api/model-execution-record/statistics')
}

// 组织机构相关API
export const organizationApi = {
  // 获取所有组织机构
  getAll: (params?: {
    page?: number;
    size?: number;
    code?: string;
    name?: string;
    level?: number;
    parentId?: number;
  }) => request.get('/api/organization/list', { params }),

  // 根据ID获取组织机构
  getById: (id: number) => request.get(`/api/organization/${id}`),

  // 根据编码获取组织机构
  getByCode: (code: string) => request.get(`/api/organization/code/${code}`),

  // 获取组织机构树形结构
  getTree: (params?: {
    parentId?: number;
    maxLevel?: number;
  }) => request.get('/api/organization/tree', { params }),

  // 根据父级ID获取子级组织机构
  getChildrenByParentId: (parentId: number) => request.get(`/api/organization/children/${parentId}`),

  // 搜索组织机构
  search: (params: {
    keyword?: string;
    level?: number;
  }) => request.get('/api/organization/search', { params }),

  // 获取省级组织机构列表
  getProvinces: () => request.get('/api/organization/provinces'),

  // 获取市级组织机构列表
  getCities: (provinceCode?: string) =>
    request.get('/api/organization/cities', { params: { provinceCode } }),

  // 获取县级组织机构列表
  getCounties: (cityCode?: string) =>
    request.get('/api/organization/counties', { params: { cityCode } }),

  // 获取乡镇级组织机构列表
  getTownships: (countyCode?: string) =>
    request.get('/api/organization/townships', { params: { countyCode } }),

  // 获取社区级组织机构列表
  getCommunities: (townshipCode?: string) =>
    request.get('/api/organization/communities', { params: { townshipCode } })
}

// 用户认证相关API
export const userApi = {
  // 用户登录验证
  login: (data: { username: string; password: string }) =>
    request.post('/api/user/login', data),

  // 检查用户名是否存在
  checkExists: (username: string) =>
    request.get(`/api/user/exists/${username}`),

  // 用户注册
  register: (data: { username: string; password: string }) =>
    request.post('/api/user/register', data),

  // 获取用户角色
  getUserRoles: (userId: number) =>
    request.get(`/api/sys/user/${userId}/roles`)
}

// 角色相关API
export const roleApi = {
  // 获取角色组织机构
  getRoleOrganizations: (roleId: number) =>
    request.get(`/api/sys/role/${roleId}/organizations`)
}

// Word模板处理相关API
export const wordTemplateApi = {
  // 生成Word报告
  generateReport: (year?: number, orgCode?: string) => {
    return request({
      url: '/api/word-template/generate-report',
      method: 'POST',
      responseType: 'blob', // 重要：设置响应类型为blob以处理文件下载
      params: { year, orgCode }
    })
  },

  // 获取JSON数据预览 (供前端或OnlyOffice使用)
  previewJson: (year?: number, orgCode?: string) => 
    request.get('/api/word-template/preview-json', { params: { year, orgCode } }),

  // 获取模板预览数据
  previewVariables: (year?: number, orgCode?: string) => 
    request.get('/api/word-template/preview-variables', { params: { year, orgCode } }),

  // 获取完整报告预览数据
  previewReport: (year?: number, orgCode?: string) => request.get('/api/word-template/preview-report', { params: { year, orgCode } }),

  // 获取Word模板的实际内容
  getRealTemplateContent: () => request.get('/api/word-template/real-template-content'),

  // 获取Word模板内容（用于编辑器）
  getTemplateContent: () => request.get('/api/word-template/real-template-content'),

  // 将HTML转换为Word
  convertHtmlToWord: (htmlContent: string) => {
    return request({
      url: '/api/word-template/convert-html-to-word',
      method: 'POST',
      data: { htmlContent },
      responseType: 'blob'
    })
  }
}
