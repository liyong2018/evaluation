/**
 * 后端代码示例：为返回的 columns 添加 stepOrder 字段
 * 
 * 根据算法名称/编码确定所属步骤
 */

// 示例1: 如果您有算法执行记录，其中包含 stepOrder
public Map<String, Object> generateResultWithStepInfo(
        List<AlgorithmExecutionResult> executionResults) {
    
    List<Map<String, Object>> columns = new ArrayList<>();
    List<Map<String, Object>> tableData = new ArrayList<>();
    
    // 基础列
    columns.add(createColumn("regionCode", "地区代码", 150, null));
    columns.add(createColumn("regionName", "地区名称", 120, null));
    
    // 遍历算法执行结果
    for (AlgorithmExecutionResult result : executionResults) {
        String algorithmCode = result.getAlgorithmCode();
        Integer stepOrder = result.getStepOrder();  // 从执行结果中获取步骤序号
        String outputColumnName = result.getOutputColumnName();
        
        // 添加列，包含 stepOrder
        columns.add(createColumn(outputColumnName, outputColumnName, 120, stepOrder));
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("columns", columns);
    response.put("tableData", tableData);
    return response;
}

// 示例2: 根据算法编码映射到步骤
public Map<String, Object> generateResultWithAlgorithmMapping(
        List<String> algorithmCodes, 
        Map<String, Object> calculationResults) {
    
    // 算法编码 -> 步骤序号 映射
    Map<String, Integer> algorithmToStep = buildAlgorithmStepMapping();
    
    List<Map<String, Object>> columns = new ArrayList<>();
    
    // 基础列
    columns.add(createColumn("regionCode", "地区代码", 150, null));
    columns.add(createColumn("regionName", "地区名称", 120, null));
    
    // 遍历算法
    for (String algorithmCode : algorithmCodes) {
        Integer stepOrder = algorithmToStep.get(algorithmCode);
        String outputColumnName = getOutputColumnName(algorithmCode);
        
        columns.add(createColumn(outputColumnName, outputColumnName, 120, stepOrder));
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("columns", columns);
    return response;
}

// 示例3: 从数据库查询算法配置，获取步骤信息
public Map<String, Object> generateResultFromDatabase(
        Long modelId,
        List<String> regionCodes,
        Long weightConfigId) {
    
    // 1. 查询模型的步骤配置
    List<ModelStep> steps = modelStepMapper.selectByModelId(modelId);
    
    // 2. 查询每个步骤的算法配置
    List<Map<String, Object>> columns = new ArrayList<>();
    columns.add(createColumn("regionCode", "地区代码", 150, null));
    columns.add(createColumn("regionName", "地区名称", 120, null));
    
    for (ModelStep step : steps) {
        Integer stepOrder = step.getStepOrder();
        List<AlgorithmConfig> algorithms = algorithmConfigMapper.selectByStepId(step.getId());
        
        for (AlgorithmConfig algorithm : algorithms) {
            String outputParam = algorithm.getOutputParam();
            String outputColumnName = getChineseColumnName(outputParam);
            
            // 添加列，附带 stepOrder
            columns.add(createColumn(outputColumnName, outputColumnName, 120, stepOrder));
        }
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("columns", columns);
    return response;
}

// 辅助方法：创建列对象
private Map<String, Object> createColumn(
        String prop, 
        String label, 
        int width, 
        Integer stepOrder) {
    
    Map<String, Object> column = new HashMap<>();
    column.put("prop", prop);
    column.put("label", label);
    column.put("width", width);
    
    // 关键：添加 stepOrder 字段
    if (stepOrder != null) {
        column.put("stepOrder", stepOrder);
    }
    
    return column;
}

// 辅助方法：构建算法编码到步骤的映射
private Map<String, Integer> buildAlgorithmStepMapping() {
    Map<String, Integer> mapping = new HashMap<>();
    
    // 步骤1: 评估指标赋值
    mapping.put("TEAM_MANAGEMENT", 1);
    mapping.put("RISK_ASSESSMENT", 1);
    mapping.put("FINANCIAL_INPUT", 1);
    mapping.put("MATERIAL_RESERVE", 1);
    mapping.put("MEDICAL_SUPPORT", 1);
    mapping.put("SELF_RESCUE", 1);
    mapping.put("PUBLIC_AVOIDANCE", 1);
    mapping.put("RELOCATION_CAPACITY", 1);
    
    // 步骤2: 属性向量归一化
    mapping.put("TEAM_MANAGEMENT_NORM", 2);
    mapping.put("RISK_ASSESSMENT_NORM", 2);
    mapping.put("FINANCIAL_INPUT_NORM", 2);
    mapping.put("MATERIAL_RESERVE_NORM", 2);
    mapping.put("MEDICAL_SUPPORT_NORM", 2);
    mapping.put("SELF_RESCUE_NORM", 2);
    mapping.put("PUBLIC_AVOIDANCE_NORM", 2);
    mapping.put("RELOCATION_CAPACITY_NORM", 2);
    
    // 步骤3: 二级指标定权
    mapping.put("TEAM_MANAGEMENT_WEIGHTED", 3);
    mapping.put("RISK_ASSESSMENT_WEIGHTED", 3);
    // ... 其他算法
    
    // 步骤4: 优劣解计算
    mapping.put("DISASTER_MGMT_POSITIVE", 4);
    mapping.put("DISASTER_MGMT_NEGATIVE", 4);
    // ... 其他算法
    
    // 步骤5: 能力值计算与分级
    mapping.put("DISASTER_MGMT_SCORE", 5);
    mapping.put("DISASTER_MGMT_GRADE", 5);
    // ... 其他算法
    
    return mapping;
}

// 辅助方法：根据算法编码获取中文列名
private String getChineseColumnName(String algorithmCode) {
    Map<String, String> nameMapping = new HashMap<>();
    nameMapping.put("TEAM_MANAGEMENT", "队伍管理能力计算");
    nameMapping.put("RISK_ASSESSMENT", "风险评估能力计算");
    // ... 其他映射
    
    return nameMapping.getOrDefault(algorithmCode, algorithmCode);
}

/**
 * 完整示例：在Controller中使用
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {
    
    @Autowired
    private ModelExecutionService modelExecutionService;
    
    /**
     * 执行算法步骤
     */
    @PostMapping("/algorithm/{algorithmId}/step/{stepOrder}/execute")
    public Result<Map<String, Object>> executeAlgorithmStep(
            @PathVariable Long algorithmId,
            @PathVariable Integer stepOrder,
            @RequestBody Map<String, Object> request) {
        
        List<String> regionCodes = (List<String>) request.get("regionCodes");
        Long weightConfigId = Long.valueOf(request.get("weightConfigId").toString());
        
        // 执行算法
        Map<String, Object> result = modelExecutionService.executeAlgorithmStep(
                algorithmId, stepOrder, regionCodes, weightConfigId);
        
        // 为每个输出列添加 stepOrder
        List<Map<String, Object>> columns = (List<Map<String, Object>>) result.get("columns");
        if (columns != null) {
            for (Map<String, Object> column : columns) {
                String prop = (String) column.get("prop");
                // 基础列不添加 stepOrder
                if (!"regionCode".equals(prop) && !"regionName".equals(prop)) {
                    column.put("stepOrder", stepOrder);
                }
            }
        }
        
        return Result.success(result);
    }
}

/**
 * 更简单的方式：如果您已经知道每列属于哪个步骤
 */
public Map<String, Object> generateResultSimple() {
    List<Map<String, Object>> columns = new ArrayList<>();
    
    // 基础列
    columns.add(createColumn("regionCode", "地区代码", 150, null));
    columns.add(createColumn("regionName", "地区名称", 120, null));
    
    // 步骤1的列（8个）
    columns.add(createColumn("队伍管理能力计算", "队伍管理能力计算", 120, 1));
    columns.add(createColumn("风险评估能力计算", "风险评估能力计算", 120, 1));
    columns.add(createColumn("财政投入能力计算", "财政投入能力计算", 120, 1));
    columns.add(createColumn("物资储备能力计算", "物资储备能力计算", 120, 1));
    columns.add(createColumn("医疗保障能力计算", "医疗保障能力计算", 120, 1));
    columns.add(createColumn("自救互救能力计算", "自救互救能力计算", 120, 1));
    columns.add(createColumn("公众避险能力计算", "公众避险能力计算", 120, 1));
    columns.add(createColumn("转移安置能力计算", "转移安置能力计算", 120, 1));
    
    // 步骤2的列（8个）
    columns.add(createColumn("队伍管理能力归一化", "队伍管理能力归一化", 120, 2));
    columns.add(createColumn("风险评估能力归一化", "风险评估能力归一化", 120, 2));
    // ... 其他6个
    
    // 步骤3的列（16个）
    columns.add(createColumn("队伍管理能力定权", "队伍管理能力定权", 120, 3));
    columns.add(createColumn("队伍管理能力综合定权", "队伍管理能力综合定权", 120, 3));
    // ... 其他14个
    
    // 步骤4的列（8个）
    columns.add(createColumn("灾害管理能力优解", "灾害管理能力优解", 120, 4));
    columns.add(createColumn("灾害管理能力差解", "灾害管理能力差解", 120, 4));
    // ... 其他6个
    
    // 步骤5的列（8个）
    columns.add(createColumn("灾害管理能力值", "灾害管理能力值", 120, 5));
    columns.add(createColumn("灾害管理能力分级", "灾害管理能力分级", 120, 5));
    // ... 其他6个
    
    Map<String, Object> response = new HashMap<>();
    response.put("columns", columns);
    return response;
}
