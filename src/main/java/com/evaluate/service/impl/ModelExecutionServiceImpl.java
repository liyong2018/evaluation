package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.*;
import com.evaluate.mapper.*;
import com.evaluate.service.ModelExecutionService;
import com.evaluate.service.QLExpressService;
import com.evaluate.service.SpecialAlgorithmService;
import com.evaluate.service.ISurveyDataService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;

/**
 * 妯″瀷鎵ц鏈嶅姟瀹炵幇绫?
 * 璐熻矗鎸夋楠ゆ墽琛孮LExpress琛ㄨ揪寮忓苟鐢熸垚璇勪及缁撴灉
 * 
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class ModelExecutionServiceImpl implements ModelExecutionService {

    @Autowired
    private EvaluationModelMapper evaluationModelMapper;

    @Autowired
    private ModelStepMapper modelStepMapper;

    @Autowired
    private StepAlgorithmMapper stepAlgorithmMapper;

    @Autowired
    private SurveyDataMapper surveyDataMapper;

    @Autowired
    private CommunityDisasterReductionCapacityMapper communityDataMapper;

    @Autowired
    private IndicatorWeightMapper indicatorWeightMapper;

    @Autowired
    private QLExpressService qlExpressService;

    @Autowired
    private SpecialAlgorithmService specialAlgorithmService;

    @Autowired
    private ISurveyDataService surveyDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 鎵ц璇勪及妯″瀷
     * 
     * @param modelId 妯″瀷ID
     * @param regionCodes 鍦板尯浠ｇ爜鍒楄〃
     * @param weightConfigId 鏉冮噸閰嶇疆ID
     * @return 鎵ц缁撴灉锛堝寘鍚瘡涓楠ょ殑杈撳嚭锛?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeModel(Long modelId, List<String> regionCodes, Long weightConfigId) {
        log.info("寮€濮嬫墽琛岃瘎浼版ā鍨? modelId={}, regionCodes={}, weightConfigId={}", 
                modelId, regionCodes, weightConfigId);

        // 1. 楠岃瘉妯″瀷鏄惁瀛樺湪涓斿惎鐢?
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        if (model == null || model.getStatus() == 0) {
            throw new RuntimeException("璇勪及妯″瀷涓嶅瓨鍦ㄦ垨宸茬鐢?);
        }

        // 2. 鑾峰彇妯″瀷鐨勬墍鏈夋楠ゅ苟鎸夐『搴忔帓搴?
        QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("model_id", modelId)
                .eq("status", 1)
                .orderByAsc("step_order");
        List<ModelStep> steps = modelStepMapper.selectList(stepQuery);
        
        if (steps == null || steps.isEmpty()) {
            throw new RuntimeException("璇ユā鍨嬫病鏈夐厤缃楠?);
        }

        // 3. 鍒濆鍖栧叏灞€涓婁笅鏂囷紙瀛樺偍鎵€鏈夋楠ょ殑鎵ц缁撴灉锛?
        Map<String, Object> globalContext = new HashMap<>();
        globalContext.put("modelId", modelId);
        globalContext.put("modelName", model.getModelName());
        globalContext.put("regionCodes", regionCodes);
        globalContext.put("weightConfigId", weightConfigId);

        // 4. 鍔犺浇鍩虹鏁版嵁鍒颁笂涓嬫枃
        loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

        // 5. 鎸夐『搴忔墽琛屾瘡涓楠?
        Map<String, Object> stepResults = new HashMap<>();
        Map<Integer, List<String>> stepOutputParams = new LinkedHashMap<>();  // 璁板綍姣忎釜姝ラ鐨勮緭鍑哄弬鏁板悕绉?
        List<String> currentRegionCodes = new ArrayList<>(regionCodes);  // 褰撳墠浣跨敤鐨勫湴鍖轰唬鐮佸垪琛?
        
        for (ModelStep step : steps) {
            log.info("鎵ц姝ラ: {} - {}, order={}", step.getStepCode(), step.getStepName(), step.getStepOrder());
            
            try {
                Map<String, Object> stepResult;
                
                // 鐗规畩澶勭悊锛氬鏋滄槸AGGREGATION绫诲瀷涓攎odelId=8锛屾墽琛屼埂闀囪仛鍚?
                if ("AGGREGATION".equals(step.getStepType()) && modelId == 8) {
                    log.info("妫€娴嬪埌涔￠晣鑱氬悎姝ラ锛屾墽琛屾寜涔￠晣鍒嗙粍鑱氬悎");
                    stepResult = executeTownshipAggregation(step.getId(), currentRegionCodes, globalContext);
                    
                    // 鏇存柊regionCodes涓轰埂闀囦唬鐮佸垪琛紙鐢ㄤ簬鍚庣画姝ラ锛?
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = 
                            (Map<String, Map<String, Object>>) stepResult.get("regionResults");
                    if (regionResults != null) {
                        currentRegionCodes = new ArrayList<>(regionResults.keySet());
                        log.info("涔￠晣鑱氬悎鍚庯紝鏇存柊regionCodes涓轰埂闀囦唬鐮佸垪琛? {}", currentRegionCodes);
                    }
                } else {
                    // 鎵ц鍗曚釜姝ラ
                    stepResult = executeStep(step.getId(), currentRegionCodes, globalContext);
                }
                
                stepResults.put(step.getStepCode(), stepResult);
                
                // 璁板綍璇ユ楠ょ殑杈撳嚭鍙傛暟锛堢敤浜庡悗闈㈢敓鎴?columns锛?
                @SuppressWarnings("unchecked")
                Map<String, String> outputToAlgorithmName = 
                        (Map<String, String>) stepResult.get("outputToAlgorithmName");
                if (outputToAlgorithmName != null) {
                    stepOutputParams.put(step.getStepOrder(), new ArrayList<>(outputToAlgorithmName.values()));
                    log.debug("姝ラ{} 鐨勮緭鍑哄弬鏁? {}", step.getStepOrder(), outputToAlgorithmName.values());
                }
                
                // 灏嗘楠ょ粨鏋滃悎骞跺埌鍏ㄥ眬涓婁笅鏂囷紙渚涘悗缁楠や娇鐢級
                globalContext.put("step_" + step.getStepCode(), stepResult);
                
                log.info("姝ラ {} 鎵ц瀹屾垚", step.getStepCode());
            } catch (Exception e) {
                log.error("姝ラ {} 鎵ц澶辫触: {}", step.getStepCode(), e.getMessage(), e);
                throw new RuntimeException("姝ラ " + step.getStepName() + " 鎵ц澶辫触: " + e.getMessage(), e);
            }
        }

        // 鐢熸垚浜岀淮琛ㄦ暟鎹?
        List<Map<String, Object>> tableData = generateResultTable(
                Collections.singletonMap("stepResults", stepResults));
        
        // 鐢熸垚 columns 鏁扮粍锛堝寘鍚墍鏈夋楠ょ殑 stepOrder 淇℃伅锛?
        List<Map<String, Object>> columns = generateColumnsWithAllStepsV2(tableData, stepOutputParams);

        // 6. 鏋勫缓鏈€缁堢粨鏋?
        Map<String, Object> result = new HashMap<>();
        result.put("modelId", modelId);
        result.put("modelName", model.getModelName());
        result.put("executionTime", new Date());
        result.put("stepResults", stepResults);
        result.put("tableData", tableData);
        result.put("columns", columns);
        result.put("success", true);

        log.info("璇勪及妯″瀷鎵ц瀹屾垚");
        return result;
    }

    /**
     * 鎵ц鍗曚釜姝ラ
     * 
     * @param stepId 姝ラID
     * @param regionCodes 鍦板尯浠ｇ爜鍒楄〃
     * @param inputData 杈撳叆鏁版嵁锛堝叏灞€涓婁笅鏂囷級
     * @return 姝ラ鎵ц缁撴灉
     */
    @Override
    public Map<String, Object> executeStep(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        log.info("鎵ц姝ラ, stepId={}", stepId);

        // 1. 鑾峰彇姝ラ淇℃伅
        ModelStep step = modelStepMapper.selectById(stepId);
        if (step == null || step.getStatus() == 0) {
            throw new RuntimeException("姝ラ涓嶅瓨鍦ㄦ垨宸茬鐢?);
        }

        // 2. 鑾峰彇璇ユ楠ょ殑鎵€鏈夌畻娉曞苟鎸夐『搴忔帓搴?
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .orderByAsc("algorithm_order");
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);

        if (algorithms == null || algorithms.isEmpty()) {
            log.warn("姝ラ {} 娌℃湁閰嶇疆绠楁硶", step.getStepCode());
            return new HashMap<>();
        }

        // 3. 鍒濆鍖栨楠ょ粨鏋?
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", stepId);
        stepResult.put("stepName", step.getStepName());
        stepResult.put("stepCode", step.getStepCode());

        // 4. 绗竴閬嶏細涓烘墍鏈夊湴鍖哄噯澶囦笂涓嬫枃鏁版嵁
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();

        // 鑾峰彇modelId浠ュ喅瀹氫娇鐢ㄥ摢涓暟鎹簮
        Long modelId = (Long) inputData.get("modelId");

        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(inputData);
            regionContext.put("currentRegionCode", regionCode);

            // 鏍规嵁modelId閫夋嫨涓嶅悓鐨勬暟鎹簮
            if (modelId != null && (modelId == 4 || modelId == 8)) {
                // 绀惧尯妯″瀷(modelId=4)鍜岀ぞ鍖?涔￠晣妯″瀷(modelId=8)锛氫粠community_disaster_reduction_capacity琛ㄥ姞杞芥暟鎹?
                // 浣跨敤selectMaps鐩存帴杩斿洖Map锛宬ey涓烘暟鎹簱瀛楁鍚嶏紝鍙洿鎺ュ尮閰嶇畻娉曡〃杈惧紡涓殑鍙橀噺鍚?
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);

                if (communityDataList != null && !communityDataList.isEmpty()) {
                    Map<String, Object> communityDataMap = communityDataList.get(0);
                    // 鐩存帴灏嗘暟鎹簱瀛楁娣诲姞鍒颁笂涓嬫枃锛屽悓鏃跺鐞嗘暟鍊肩被鍨嬭浆鎹?
                    addMapDataToContext(regionContext, communityDataMap);
                } else {
                    log.warn("鏈壘鍒扮ぞ鍖烘暟鎹? regionCode={}", regionCode);
                }
            } else {
                // 涔￠晣妯″瀷(modelId=3)锛氫粠survey_data琛ㄥ姞杞芥暟鎹?
                QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
                dataQuery.eq("region_code", regionCode);
                SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);

                if (surveyData != null) {
                    addSurveyDataToContext(regionContext, surveyData);
                } else {
                    log.warn("鏈壘鍒拌皟鏌ユ暟鎹? regionCode={}", regionCode);
                }
            }

            // 鍐嶅姞杞藉墠闈㈡楠ょ殑杈撳嚭缁撴灉锛堣绠楃粨鏋滐級锛岃繖鏍蜂細瑕嗙洊鍘熷鏁版嵁涓殑鍚屽悕瀛楁
            loadPreviousStepOutputs(regionContext, regionCode, inputData);

            allRegionContexts.put(regionCode, regionContext);
        }
        
        // 5. 鍒嗙GRADE绠楁硶鍜岄潪GRADE绠楁硶
        List<StepAlgorithm> nonGradeAlgorithms = new ArrayList<>();
        List<StepAlgorithm> gradeAlgorithms = new ArrayList<>();
        
        for (StepAlgorithm algorithm : algorithms) {
            String qlExpression = algorithm.getQlExpression();
            if (qlExpression != null && qlExpression.startsWith("@GRADE")) {
                gradeAlgorithms.add(algorithm);
            } else {
                nonGradeAlgorithms.add(algorithm);
            }
        }
        
        log.info("绠楁硶鍒嗙粍: 闈濭RADE绠楁硶={}, GRADE绠楁硶={}", nonGradeAlgorithms.size(), gradeAlgorithms.size());
        
        // 6. 绗簩閬嶏細涓烘瘡涓湴鍖烘墽琛岄潪GRADE绠楁硶锛堟敮鎸佺壒娈婃爣璁帮級
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToAlgorithmName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            log.info("涓哄湴鍖?{} 鎵ц闈濭RADE绠楁硶", regionCode);
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> algorithmOutputs = new LinkedHashMap<>();
            
            // 鎵ц闈濭RADE绠楁硶
            for (StepAlgorithm algorithm : nonGradeAlgorithms) {
                try {
                    log.debug("鎵ц绠楁硶: {} - {}", algorithm.getAlgorithmCode(), algorithm.getAlgorithmName());
                    
                    Object result;
                    String qlExpression = algorithm.getQlExpression();
                    
                    // 妫€鏌ユ槸鍚︽槸鐗规畩鏍囪
                    if (qlExpression != null && qlExpression.startsWith("@")) {
                        // 瑙ｆ瀽鐗规畩鏍囪: @MARKER:params
                        String[] parts = qlExpression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";
                        
                        log.info("鎵ц鐗规畩鏍囪绠楁硶: marker={}, params={}", marker, params);
                        
                        // 璋冪敤鐗规畩绠楁硶鏈嶅姟
                        result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 纭繚鏁板€肩被鍨嬭浆鎹㈠苟鏍煎紡鍖栦负8浣嶅皬鏁?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    } else {
                        // 鎵ц鏍囧噯QLExpress琛ㄨ揪寮?
                        result = qlExpressService.execute(qlExpression, regionContext);
                        
                        // 纭繚鏁板€肩被鍨嬬殑缁撴灉杞崲涓篋ouble骞舵牸寮忓寲涓?浣嶅皬鏁?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    }
                    
                    // 淇濆瓨绠楁硶杈撳嚭鍒颁笂涓嬫枃锛堜緵鍚庣画绠楁硶浣跨敤锛?
                    String outputParam = algorithm.getOutputParam();
                    if (outputParam != null && !outputParam.isEmpty()) {
                        regionContext.put(outputParam, result);
                        allRegionContexts.put(regionCode, regionContext);  // 鏇存柊鍏ㄥ眬涓婁笅鏂?
                        algorithmOutputs.put(outputParam, result);
                        outputToAlgorithmName.put(outputParam, algorithm.getAlgorithmName());
                    }
                    
                    log.debug("绠楁硶 {} 鎵ц缁撴灉: {}", algorithm.getAlgorithmCode(), result);
                } catch (Exception e) {
                    log.error("绠楁硶 {} 鎵ц澶辫触: {}", algorithm.getAlgorithmCode(), e.getMessage(), e);
                    throw new RuntimeException("绠楁硶 " + algorithm.getAlgorithmName() + " 鎵ц澶辫触: " + e.getMessage(), e);
                }
            }
            
            regionResults.put(regionCode, algorithmOutputs);
        }
        
        // 7. 绗笁閬嶏細涓烘瘡涓湴鍖烘墽琛孏RADE绠楁硶锛堟鏃舵墍鏈夊湴鍖虹殑鍒嗘暟宸茶绠楀畬鎴愶級
        if (!gradeAlgorithms.isEmpty()) {
            log.info("寮€濮嬫墽琛孏RADE绠楁硶锛屾鏃舵墍鏈夊湴鍖虹殑鍒嗘暟宸茶绠楀畬鎴?);
            
            for (String regionCode : regionCodes) {
                log.info("涓哄湴鍖?{} 鎵цGRADE绠楁硶", regionCode);
                Map<String, Object> regionContext = allRegionContexts.get(regionCode);
                Map<String, Object> algorithmOutputs = regionResults.get(regionCode);
                
                for (StepAlgorithm algorithm : gradeAlgorithms) {
                    try {
                        log.debug("鎵цGRADE绠楁硶: {} - {}", algorithm.getAlgorithmCode(), algorithm.getAlgorithmName());
                        
                        String qlExpression = algorithm.getQlExpression();
                        String[] parts = qlExpression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";
                        
                        log.info("鎵ц鐗规畩鏍囪绠楁硶: marker={}, params={}", marker, params);
                        
                        // 璋冪敤鐗规畩绠楁硶鏈嶅姟
                        Object result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 鏍煎紡鍖朑RADE绠楁硶缁撴灉涓?浣嶅皬鏁?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }

                        // 淇濆瓨绠楁硶杈撳嚭鍒颁笂涓嬫枃锛堜緵鍚庣画绠楁硶浣跨敤锛?
                        String outputParam = algorithm.getOutputParam();
                        if (outputParam != null && !outputParam.isEmpty()) {
                            regionContext.put(outputParam, result);
                            allRegionContexts.put(regionCode, regionContext);  // 鏇存柊鍏ㄥ眬涓婁笅鏂?
                            algorithmOutputs.put(outputParam, result);
                            outputToAlgorithmName.put(outputParam, algorithm.getAlgorithmName());
                        }
                        
                        log.debug("GRADE绠楁硶 {} 鎵ц缁撴灉: {}", algorithm.getAlgorithmCode(), result);
                    } catch (Exception e) {
                        log.error("GRADE绠楁硶 {} 鎵ц澶辫触: {}", algorithm.getAlgorithmCode(), e.getMessage(), e);
                        throw new RuntimeException("GRADE绠楁硶 " + algorithm.getAlgorithmName() + " 鎵ц澶辫触: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        // 淇濆瓨杈撳嚭鍙傛暟鍒扮畻娉曞悕绉扮殑鏄犲皠
        if (!outputToAlgorithmName.isEmpty()) {
            stepResult.put("outputToAlgorithmName", outputToAlgorithmName);
        }

        stepResult.put("regionResults", regionResults);
        return stepResult;
    }

    /**
     * 鐢熸垚缁撴灉浜岀淮琛?
     * 
     * @param executionResults 鎵ц缁撴灉
     * @return 浜岀淮琛ㄦ暟鎹?
     */
    @Override
    public List<Map<String, Object>> generateResultTable(Map<String, Object> executionResults) {
        log.info("鐢熸垚缁撴灉浜岀淮琛?);

        List<Map<String, Object>> tableData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> stepResults = 
                (Map<String, Map<String, Object>>) executionResults.get("stepResults");

        if (stepResults == null || stepResults.isEmpty()) {
            return tableData;
        }

        // 鏀堕泦鎵€鏈夊湴鍖轰唬鐮佸拰杈撳嚭鍙橀噺锛屼互鍙婅緭鍑哄弬鏁板埌绠楁硶鍚嶇О鐨勬槧灏?
        Set<String> allRegions = new LinkedHashSet<>();
        Set<String> allOutputs = new LinkedHashSet<>();
        Map<String, String> globalOutputToAlgorithmName = new LinkedHashMap<>();  // 鍏ㄥ眬鐨勮緭鍑哄弬鏁板埌绠楁硶鍚嶇О鏄犲皠

        for (Map.Entry<String, Map<String, Object>> stepEntry : stepResults.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> regionResults = 
                    (Map<String, Map<String, Object>>) stepEntry.getValue().get("regionResults");
            
            // 鑾峰彇杈撳嚭鍙傛暟鍒扮畻娉曞悕绉扮殑鏄犲皠
            @SuppressWarnings("unchecked")
            Map<String, String> outputToAlgorithmName = 
                    (Map<String, String>) stepEntry.getValue().get("outputToAlgorithmName");
            if (outputToAlgorithmName != null) {
                globalOutputToAlgorithmName.putAll(outputToAlgorithmName);
            }
            
            if (regionResults != null) {
                allRegions.addAll(regionResults.keySet());
                
                for (Map<String, Object> outputs : regionResults.values()) {
                    allOutputs.addAll(outputs.keySet());
                }
            }
        }

        // 涓烘瘡涓湴鍖虹敓鎴愪竴琛屾暟鎹?
        for (String regionCode : allRegions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regionCode", regionCode);
            
            // 鑾峰彇鍦板尯鍚嶇О鍜屼埂闀囧悕绉?
            String regionName = regionCode;
            String townshipName = null;
            String communityName = null;
            
            // 妫€鏌ユ槸鍚︽槸涔￠晣铏氭嫙浠ｇ爜锛堜互"TOWNSHIP_"寮€澶达級
            if (regionCode.startsWith("TOWNSHIP_")) {
                // 杩欐槸涔￠晣鑱氬悎鍚庣殑铏氭嫙浠ｇ爜
                townshipName = regionCode.substring("TOWNSHIP_".length());
                regionName = townshipName;
                // 涔￠晣琛岀殑 regionCode 鐩存帴灞曠ず涓枃鍚嶇О锛岄伩鍏嶆樉绀?TOWNSHIP_ 鍓嶇紑
                row.put("regionCode", regionName);
                
                // 浠庢楠ょ粨鏋滀腑鑾峰彇淇濆瓨鐨勪埂闀囦俊鎭?
                for (Map.Entry<String, Map<String, Object>> stepEntry : stepResults.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = 
                            (Map<String, Map<String, Object>>) stepEntry.getValue().get("regionResults");
                    if (regionResults != null && regionResults.containsKey(regionCode)) {
                        Map<String, Object> outputs = regionResults.get(regionCode);
                        if (outputs.containsKey("_townshipName")) {
                            townshipName = (String) outputs.get("_townshipName");
                            regionName = townshipName;
                        }
                        if (outputs.containsKey("_firstCommunityCode")) {
                            String firstCommunityCode = (String) outputs.get("_firstCommunityCode");
                            // 鍙互鐢ㄧ涓€涓ぞ鍖轰唬鐮佹潵鑾峰彇鏇村淇℃伅
                            row.put("_firstCommunityCode", firstCommunityCode);
                        }
                        break;
                    }
                }
                
                log.debug("涔￠晣铏氭嫙浠ｇ爜 {} 鏄犲皠涓? townshipName={}", regionCode, townshipName);
            } else {
                // 杩欐槸鏅€氱殑绀惧尯浠ｇ爜
                // 棣栧厛灏濊瘯浠巆ommunity_disaster_reduction_capacity琛ㄨ幏鍙栫ぞ鍖哄拰涔￠晣淇℃伅
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
                if (communityData != null) {
                    townshipName = communityData.getTownshipName();
                    communityName = communityData.getCommunityName();
                    regionName = communityName != null ? communityName : regionCode;
                } else {
                    // 濡傛灉community琛ㄤ腑娌℃湁鎵惧埌锛屽皾璇曚粠survey_data琛ㄨ幏鍙栧湴鍖哄悕绉?
                    QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                    surveyQuery.eq("region_code", regionCode);
                    SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
                    if (surveyData != null && surveyData.getTownship() != null) {
                        regionName = surveyData.getTownship();
                    } else {
                        // 濡傛灉閮芥病鏈夋壘鍒帮紝浣跨敤regionCode浣滀负regionName
                        regionName = regionCode;
                    }
                }
                
                log.debug("鍦板尯 {} 鏄犲皠涓? regionName={}, townshipName={}, communityName={}", 
                        regionCode, regionName, townshipName, communityName);
            }
            
            row.put("regionName", regionName);
            if (townshipName != null) {
                row.put("townshipName", townshipName);
            }
            if (communityName != null) {
                row.put("communityName", communityName);
            }

            // 鏀堕泦璇ュ湴鍖哄湪鎵€鏈夋楠や腑鐨勮緭鍑?
            for (Map.Entry<String, Map<String, Object>> stepEntry : stepResults.entrySet()) {
                String stepCode = stepEntry.getKey();
                
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> regionResults = 
                        (Map<String, Map<String, Object>>) stepEntry.getValue().get("regionResults");
                
                if (regionResults != null && regionResults.containsKey(regionCode)) {
                    Map<String, Object> outputs = regionResults.get(regionCode);
                    
                    // 灏嗚緭鍑哄彉閲忔坊鍔犲埌琛屾暟鎹紝浣跨敤绠楁硶涓枃鍚嶇О浣滀负鍒楀悕
                    for (Map.Entry<String, Object> output : outputs.entrySet()) {
                        String outputParam = output.getKey();
                        
                        // 璺宠繃鍐呴儴浣跨敤鐨勫瓧娈碉紙浠?_"寮€澶达級
                        if (outputParam.startsWith("_")) {
                            continue;
                        }
                        
                        String columnName;
                        
                        // 浼樺厛浣跨敤绠楁硶鍚嶇О浣滀负鍒楀悕锛屽鏋滄病鏈夊垯浣跨敤鍘熷鐨?stepCode_outputParam 鏍煎紡
                        if (globalOutputToAlgorithmName.containsKey(outputParam)) {
                            columnName = globalOutputToAlgorithmName.get(outputParam);
                        } else {
                            columnName = stepCode + "_" + outputParam;
                        }
                        
                        // 鏍煎紡鍖栨暟鍊间负8浣嶅皬鏁?
                        Object value = output.getValue();
                        if (value != null && value instanceof Number) {
                            double doubleValue = ((Number) value).doubleValue();
                            value = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                        row.put(columnName, value);
                    }
                }
            }

            tableData.add(row);
        }

        log.info("鐢熸垚缁撴灉浜岀淮琛ㄥ畬鎴愶紝鍏?{} 琛屾暟鎹?, tableData.size());
        return tableData;
    }

    /**
     * 鍔犺浇鍩虹鏁版嵁鍒颁笂涓嬫枃
     */
    private void loadBaseDataToContext(Map<String, Object> context, List<String> regionCodes, Long weightConfigId) {
        log.debug("鍔犺浇鍩虹鏁版嵁鍒颁笂涓嬫枃");

        // 鍔犺浇鏉冮噸閰嶇疆
        if (weightConfigId != null) {
            QueryWrapper<IndicatorWeight> weightQuery = new QueryWrapper<>();
            weightQuery.eq("config_id", weightConfigId);
            List<IndicatorWeight> weights = indicatorWeightMapper.selectList(weightQuery);
            
            // 灏嗘潈閲嶈浆鎹负Map渚夸簬鏌ユ壘
            Map<String, Double> weightMap = weights.stream()
                    .collect(Collectors.toMap(
                            IndicatorWeight::getIndicatorCode,
                            IndicatorWeight::getWeight,
                            (v1, v2) -> v1
                    ));
            context.put("weights", weightMap);
            
            // 鍚屾椂灏嗘瘡涓潈閲嶄綔涓虹嫭绔嬪彉閲忓瓨鍌紙渚夸簬琛ㄨ揪寮忕洿鎺ュ紩鐢級
            for (IndicatorWeight weight : weights) {
                // 纭繚鏉冮噸鍊间负Double绫诲瀷
                Double weightValue = weight.getWeight();
                if (weightValue == null) {
                    weightValue = 0.0;
                }
                context.put("weight_" + weight.getIndicatorCode(), weightValue);
                log.debug("鍔犺浇鏉冮噸: weight_{} = {}", weight.getIndicatorCode(), weightValue);
            }
        }
    }

    /**
     * 鍔犺浇鍓嶉潰姝ラ鐨勮緭鍑虹粨鏋滃埌褰撳墠鍖哄煙涓婁笅鏂?
     * 浠?globalContext 涓彁鍙栧墠闈㈡楠ょ殑 regionResults锛屽苟灏嗗綋鍓嶅尯鍩熺殑杈撳嚭鍊兼坊鍔犲埌涓婁笅鏂?
     */
    private void loadPreviousStepOutputs(Map<String, Object> regionContext, String regionCode, Map<String, Object> globalContext) {
        // 閬嶅巻 globalContext 涓墍鏈変互 "step_" 寮€澶寸殑鏉＄洰
        for (Map.Entry<String, Object> entry : globalContext.entrySet()) {
            if (entry.getKey().startsWith("step_") && entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                
                // 鑾峰彇璇ユ楠ょ殑 regionResults
                Object regionResultsObj = stepResult.get("regionResults");
                if (regionResultsObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = (Map<String, Map<String, Object>>) regionResultsObj;
                    
                    // 鑾峰彇褰撳墠鍖哄煙鐨勮緭鍑?
                    Map<String, Object> currentRegionOutputs = regionResults.get(regionCode);
                    if (currentRegionOutputs != null) {
                        // 灏嗗綋鍓嶅尯鍩熺殑鎵€鏈夎緭鍑哄彉閲忔坊鍔犲埌涓婁笅鏂?
                        for (Map.Entry<String, Object> output : currentRegionOutputs.entrySet()) {
                            regionContext.put(output.getKey(), output.getValue());
                            log.debug("浠庡墠闈㈡楠ゅ姞杞藉彉閲? {}={}", output.getKey(), output.getValue());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 灏嗚皟鏌ユ暟鎹坊鍔犲埌涓婁笅鏂?
     * 鍚屾椂娣诲姞椹煎嘲鍛藉悕鍜屼笅鍒掔嚎鍛藉悕锛屼互鏀寔涓嶅悓鐨勮〃杈惧紡椋庢牸
     */
    private void addSurveyDataToContext(Map<String, Object> context, SurveyData surveyData) {
        // 鍦板尯淇℃伅
        context.put("regionCode", surveyData.getRegionCode());
        context.put("region_code", surveyData.getRegionCode());
        context.put("province", surveyData.getProvince());
        context.put("city", surveyData.getCity());
        context.put("county", surveyData.getCounty());
        context.put("township", surveyData.getTownship());
        
        // 浜哄彛鏁版嵁锛堥┘宄板拰涓嬪垝绾夸袱绉嶅懡鍚嶏級
        context.put("population", surveyData.getPopulation());
        
        // 绠＄悊浜哄憳锛堥┘宄板拰涓嬪垝绾夸袱绉嶅懡鍚嶏級
        context.put("managementStaff", surveyData.getManagementStaff());
        context.put("management_staff", surveyData.getManagementStaff());
        
        // 椋庨櫓璇勪及锛堥┘宄板拰涓嬪垝绾夸袱绉嶅懡鍚嶏級
        String riskAssessmentValue = surveyData.getRiskAssessment();
        // 鏍囧噯鍖栭闄╄瘎浼板€硷細濡傛灉鍊兼槸"浣?銆?涓?銆?楂?锛岃浆鎹负"鏄?锛屼互鍖归厤绠楁硶琛ㄨ揪寮?
        String normalizedRiskAssessment = riskAssessmentValue;
        if (riskAssessmentValue != null &&
            (riskAssessmentValue.equals("浣?) ||
             riskAssessmentValue.equals("涓?) ||
             riskAssessmentValue.equals("楂?))) {
            normalizedRiskAssessment = "鏄?;
        }

        context.put("riskAssessment", normalizedRiskAssessment);
        context.put("risk_assessment", normalizedRiskAssessment);
        context.put("鏄惁寮€灞曢闄╄瘎浼?, normalizedRiskAssessment);  // 涓枃鍙橀噺鍚?
        
        // 璧勯噾鎶曞叆锛堥┘宄板拰涓嬪垝绾夸袱绉嶅懡鍚嶏級
        context.put("fundingAmount", surveyData.getFundingAmount());
        context.put("funding_amount", surveyData.getFundingAmount());
        
        // 鐗╄祫鍌ㄥ锛堥┘宄板拰涓嬪垝绾夸袱绉嶅懡鍚嶏級
        context.put("materialValue", surveyData.getMaterialValue());
        context.put("material_value", surveyData.getMaterialValue());
        
        // 鍖婚櫌搴婁綅锛堥┘宄板拰涓嬪垝绾夸袱绉嶅懡鍚嶏級
        context.put("hospitalBeds", surveyData.getHospitalBeds());
        context.put("hospital_beds", surveyData.getHospitalBeds());
        
        // 娑堥槻鍛橈紙椹煎嘲鍜屼笅鍒掔嚎涓ょ鍛藉悕锛?
        context.put("firefighters", surveyData.getFirefighters());
        
        // 蹇楁効鑰咃紙椹煎嘲鍜屼笅鍒掔嚎涓ょ鍛藉悕锛?
        context.put("volunteers", surveyData.getVolunteers());
        
        // 姘戝叺棰勫褰癸紙椹煎嘲鍜屼笅鍒掔嚎涓ょ鍛藉悕锛?
        context.put("militiaReserve", surveyData.getMilitiaReserve());
        context.put("militia_reserve", surveyData.getMilitiaReserve());
        
        // 鍩硅鍙備笌鑰咃紙椹煎嘲鍜屼笅鍒掔嚎涓ょ鍛藉悕锛?
        context.put("trainingParticipants", surveyData.getTrainingParticipants());
        context.put("training_participants", surveyData.getTrainingParticipants());
        
        // 閬块毦鎵€瀹归噺锛堥┘宄板拰涓嬪垝绾夸袱绉嶅懡鍚嶏級
        context.put("shelterCapacity", surveyData.getShelterCapacity());
        context.put("shelter_capacity", surveyData.getShelterCapacity());
    }

    /**
     * 閫氱敤鏂规硶锛氬皢Map鏁版嵁娣诲姞鍒颁笂涓嬫枃
     * 鏁版嵁搴撳瓧娈靛悕鐩存帴浣滀负鍙橀噺鍚嶏紝鏃犻渶鎵嬪姩鏄犲皠
     * 鎵€鏈夋暟鍊肩被鍨嬭浆鎹负Double锛岄伩鍏嶆暣鏁伴櫎娉曠簿搴︿涪澶?
     */
    private void addMapDataToContext(Map<String, Object> context, Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 璺宠繃鏃堕棿瀛楁鍜孖D瀛楁
            if ("create_time".equals(key) || "update_time".equals(key) || "id".equals(key)) {
                continue;
            }

            // 杞崲鏁板€肩被鍨嬩负Double锛岄伩鍏嶆暣鏁伴櫎娉曠簿搴︿涪澶?
            Object contextValue = value;
            if (value != null) {
                if (value instanceof Integer) {
                    contextValue = ((Integer) value).doubleValue();
                } else if (value instanceof Long) {
                    contextValue = ((Long) value).doubleValue();
                } else if (value instanceof java.math.BigDecimal) {
                    contextValue = ((java.math.BigDecimal) value).doubleValue();
                } else if (value instanceof Float) {
                    contextValue = ((Float) value).doubleValue();
                }
            }

            // 鐩存帴浣跨敤鏁版嵁搴撳瓧娈靛悕浣滀负涓婁笅鏂囧彉閲忓悕
            context.put(key, contextValue);
        }

        log.debug("鎴愬姛灏?{} 涓暟鎹簱瀛楁娣诲姞鍒颁笂涓嬫枃", dataMap.size());
    }

    /**
     * 灏嗙ぞ鍖烘暟鎹坊鍔犲埌涓婁笅鏂囷紙宸插簾寮冿紝浣跨敤addMapDataToContext鏇夸唬锛?
     * 鎵€鏈夋暟鍊肩被鍨嬭浆鎹负Double锛岄伩鍏嶆暣鏁伴櫎娉曠簿搴︿涪澶?
     * @deprecated 浣跨敤selectMaps鏌ヨ鍜宎ddMapDataToContext鏂规硶鏇夸唬
     */
    @Deprecated
    private void addCommunityDataToContext(Map<String, Object> context, CommunityDisasterReductionCapacity communityData) {
        // 鍦板尯淇℃伅
        context.put("regionCode", communityData.getRegionCode());
        context.put("region_code", communityData.getRegionCode());
        context.put("province", communityData.getProvinceName());
        context.put("city", communityData.getCityName());
        context.put("county", communityData.getCountyName());
        context.put("township", communityData.getTownshipName());
        context.put("community", communityData.getCommunityName());

        // 浜哄彛鏁版嵁锛堣浆鎹负Double锛?
        context.put("population", communityData.getResidentPopulation() != null ? communityData.getResidentPopulation().doubleValue() : 0.0);
        context.put("residentPopulation", communityData.getResidentPopulation() != null ? communityData.getResidentPopulation().doubleValue() : 0.0);

        // 椋庨櫓璇勪及鐩稿叧锛?涓槸/鍚﹂棶棰橈級
        context.put("hasEmergencyPlan", communityData.getHasEmergencyPlan());
        context.put("hasVulnerableGroupsList", communityData.getHasVulnerableGroupsList());
        context.put("hasDisasterPointsList", communityData.getHasDisasterPointsList());
        context.put("hasDisasterMap", communityData.getHasDisasterMap());

        // 璧勯噾鎶曞叆锛堣浆鎹负Double锛?
        Double fundingAmount = communityData.getLastYearFundingAmount() != null ? communityData.getLastYearFundingAmount().doubleValue() : 0.0;
        context.put("fundingAmount", fundingAmount);
        context.put("funding_amount", fundingAmount);
        context.put("lastYearFundingAmount", fundingAmount);

        // 鐗╄祫鍌ㄥ锛堣浆鎹负Double锛?
        Double materialValue = communityData.getMaterialsEquipmentValue() != null ? communityData.getMaterialsEquipmentValue().doubleValue() : 0.0;
        context.put("materialValue", materialValue);
        context.put("material_value", materialValue);
        context.put("materialsEquipmentValue", materialValue);

        // 鍖荤枟鏈嶅姟锛堣浆鎹负Double锛?
        Double medicalServiceCount = communityData.getMedicalServiceCount() != null ? communityData.getMedicalServiceCount().doubleValue() : 0.0;
        context.put("medicalServiceCount", medicalServiceCount);
        context.put("medical_service_count", medicalServiceCount);

        // 姘戝叺棰勫褰癸紙杞崲涓篋ouble锛?
        Double militiaReserve = communityData.getMilitiaReserveCount() != null ? communityData.getMilitiaReserveCount().doubleValue() : 0.0;
        context.put("militiaReserve", militiaReserve);
        context.put("militia_reserve", militiaReserve);
        context.put("militiaReserveCount", militiaReserve);

        // 蹇楁効鑰咃紙杞崲涓篋ouble锛?
        Double volunteers = communityData.getRegisteredVolunteerCount() != null ? communityData.getRegisteredVolunteerCount().doubleValue() : 0.0;
        context.put("volunteers", volunteers);
        context.put("registeredVolunteerCount", volunteers);

        // 鍩硅鍙備笌鑰咃紙杞崲涓篋ouble锛?
        Double trainingParticipants = communityData.getLastYearTrainingParticipants() != null ? communityData.getLastYearTrainingParticipants().doubleValue() : 0.0;
        context.put("trainingParticipants", trainingParticipants);
        context.put("training_participants", trainingParticipants);
        context.put("lastYearTrainingParticipants", trainingParticipants);

        // 婕旂粌鍙備笌鑰咃紙杞崲涓篋ouble锛?
        Double drillParticipants = communityData.getLastYearDrillParticipants() != null ? communityData.getLastYearDrillParticipants().doubleValue() : 0.0;
        context.put("drillParticipants", drillParticipants);
        context.put("lastYearDrillParticipants", drillParticipants);

        // 閬块毦鎵€瀹归噺锛堣浆鎹负Double锛?
        Double shelterCapacity = communityData.getEmergencyShelterCapacity() != null ? communityData.getEmergencyShelterCapacity().doubleValue() : 0.0;
        context.put("shelterCapacity", shelterCapacity);
        context.put("shelter_capacity", shelterCapacity);
        context.put("emergencyShelterCapacity", shelterCapacity);
    }

    /**
     * 鎵ц绠楁硶鐨勫崟涓楠ゅ苟杩斿洖2D琛ㄦ牸缁撴灉
     *
     * @param algorithmId 绠楁硶ID锛堝搴攁lgorithm_config琛級
     * @param stepOrder 姝ラ椤哄簭锛堜粠1寮€濮嬶級
     * @param regionCodes 鍦板尯浠ｇ爜鍒楄〃
     * @param weightConfigId 鏉冮噸閰嶇疆ID
     * @return 姝ラ鎵ц缁撴灉锛屽寘鍚?D琛ㄦ牸鏁版嵁
     */
    @Override
    public Map<String, Object> executeAlgorithmStep(Long algorithmId, Integer stepOrder, List<String> regionCodes, Long weightConfigId) {
        log.info("鎵ц绠楁硶姝ラ, algorithmId={}, stepOrder={}, regionCodes.size={}", algorithmId, stepOrder, regionCodes.size());

        try {
            // 1. 鑾峰彇绠楁硶閰嶇疆鐨勬墍鏈夋楠?
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            if (algorithmSteps.isEmpty()) {
                throw new RuntimeException("绠楁硶閰嶇疆娌℃湁鎵惧埌浠讳綍姝ラ");
            }

            // 2. 鎵惧埌鎸囧畾椤哄簭鐨勬楠?
            AlgorithmStep targetStep = algorithmSteps.stream()
                    .filter(step -> stepOrder.equals(step.getStepOrder()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("鏈壘鍒版楠ら『搴忎负 " + stepOrder + " 鐨勭畻娉曟楠?));

            // 3. 濡傛灉涓嶆槸绗竴姝ワ紝闇€瑕佸厛鎵ц鍓嶉潰鐨勬墍鏈夋楠ゆ潵鑾峰彇渚濊禆鏁版嵁
            Map<String, Object> globalContext = new HashMap<>();
            globalContext.put("algorithmId", algorithmId);
            globalContext.put("regionCodes", regionCodes);
            globalContext.put("weightConfigId", weightConfigId);

            // 鍔犺浇鍩虹鏁版嵁
            loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

            // 濡傛灉涓嶆槸绗竴姝ワ紝鎵ц鍓嶉潰鐨勬墍鏈夋楠?
            if (stepOrder > 1) {
                executeAlgorithmStepsInternalUpTo(algorithmSteps, stepOrder - 1, regionCodes, globalContext);
            }

            // 4. 鎵ц鐩爣姝ラ
            Map<String, Object> stepExecutionResult = executeAlgorithmStepInternal(targetStep, regionCodes, globalContext);

        // 5. 鐢熸垚璇ユ楠ょ殑2D琛ㄦ牸鏁版嵁
        List<Map<String, Object>> tableData = generateStepResultTable(stepExecutionResult, regionCodes);

        // 鐢熸垚 columns 鏁扮粍锛堝寘鍚?stepOrder 淇℃伅锛?
        List<Map<String, Object>> columns = generateColumnsWithStepOrder(tableData, stepOrder);

        // 6. 鏋勫缓杩斿洖缁撴灉
        Map<String, Object> result = new HashMap<>();
        result.put("stepId", targetStep.getId());
        result.put("stepName", targetStep.getStepName());
        result.put("stepOrder", stepOrder);
        result.put("stepCode", targetStep.getStepCode());
        result.put("description", targetStep.getStepDescription());
        result.put("executionResult", stepExecutionResult);
        result.put("tableData", tableData);
        result.put("columns", columns);
        result.put("success", true);
        result.put("executionTime", new Date());

            log.info("绠楁硶姝ラ {} 鎵ц瀹屾垚锛岀敓鎴?{} 琛岃〃鏍兼暟鎹?, stepOrder, tableData.size());
            return result;

        } catch (Exception e) {
            log.error("鎵ц绠楁硶姝ラ澶辫触", e);
            throw new RuntimeException("鎵ц绠楁硶姝ラ澶辫触: " + e.getMessage(), e);
        }
    }

    /**
     * 鑾峰彇绠楁硶鎵€鏈夋楠ょ殑鍩烘湰淇℃伅
     *
     * @param algorithmId 绠楁硶ID
     * @return 绠楁硶姝ラ鍒楄〃淇℃伅
     */
    @Override
    public Map<String, Object> getAlgorithmStepsInfo(Long algorithmId) {
        log.info("鑾峰彇绠楁硶姝ラ淇℃伅, algorithmId={}", algorithmId);

        try {
            // 鑾峰彇绠楁硶閰嶇疆
            AlgorithmConfig algorithmConfig = algorithmConfigMapper.selectById(algorithmId);
            if (algorithmConfig == null) {
                throw new RuntimeException("绠楁硶閰嶇疆涓嶅瓨鍦?);
            }

            // 鑾峰彇鎵€鏈夋楠?
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            // 杞崲涓虹畝鍖栦俊鎭?
            List<Map<String, Object>> stepsInfo = algorithmSteps.stream().map(step -> {
                Map<String, Object> stepInfo = new HashMap<>();
                stepInfo.put("stepId", step.getId());
                stepInfo.put("stepName", step.getStepName());
                stepInfo.put("stepOrder", step.getStepOrder());
                stepInfo.put("stepCode", step.getStepCode());
                stepInfo.put("description", step.getStepDescription());
                stepInfo.put("status", step.getStatus());
                return stepInfo;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("algorithmId", algorithmId);
            result.put("algorithmName", algorithmConfig.getConfigName());
            result.put("algorithmDescription", algorithmConfig.getDescription());
            result.put("totalSteps", stepsInfo.size());
            result.put("steps", stepsInfo);
            result.put("success", true);

            log.info("鑾峰彇绠楁硶姝ラ淇℃伅瀹屾垚锛屽叡 {} 涓楠?, stepsInfo.size());
            return result;

        } catch (Exception e) {
            log.error("鑾峰彇绠楁硶姝ラ淇℃伅澶辫触", e);
            throw new RuntimeException("鑾峰彇绠楁硶姝ラ淇℃伅澶辫触: " + e.getMessage(), e);
        }
    }

    /**
     * 鎵归噺鎵ц绠楁硶姝ラ锛堢洿鍒版寚瀹氭楠わ級
     *
     * @param algorithmId 绠楁硶ID
     * @param upToStepOrder 鎵ц鍒扮鍑犳锛堝寘鍚姝ラ锛?
     * @param regionCodes 鍦板尯浠ｇ爜鍒楄〃
     * @param weightConfigId 鏉冮噸閰嶇疆ID
     * @return 鎵€鏈夊凡鎵ц姝ラ鐨勭粨鏋?
     */
    @Override
    public Map<String, Object> executeAlgorithmStepsUpTo(Long algorithmId, Integer upToStepOrder, List<String> regionCodes, Long weightConfigId) {
        log.info("鎵归噺鎵ц绠楁硶姝ラ鍒扮{}姝? algorithmId={}", upToStepOrder, algorithmId);

        try {
            // 1. 鑾峰彇绠楁硶閰嶇疆鐨勬墍鏈夋楠?
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            if (algorithmSteps.isEmpty()) {
                throw new RuntimeException("绠楁硶閰嶇疆娌℃湁鎵惧埌浠讳綍姝ラ");
            }

            // 2. 楠岃瘉姝ラ椤哄簭
            boolean hasTargetStep = algorithmSteps.stream()
                    .anyMatch(step -> upToStepOrder.equals(step.getStepOrder()));
            if (!hasTargetStep) {
                throw new RuntimeException("鏈壘鍒版楠ら『搴忎负 " + upToStepOrder + " 鐨勭畻娉曟楠?);
            }

            // 3. 鍒濆鍖栦笂涓嬫枃
            Map<String, Object> globalContext = new HashMap<>();
            globalContext.put("algorithmId", algorithmId);
            globalContext.put("regionCodes", regionCodes);
            globalContext.put("weightConfigId", weightConfigId);

            // 鍔犺浇鍩虹鏁版嵁
            loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

            // 4. 鎵ц鎵€鏈夋楠ょ洿鍒版寚瀹氭楠?
            Map<String, Object> allStepResults = executeAlgorithmStepsInternalUpTo(algorithmSteps, upToStepOrder, regionCodes, globalContext);

            // 5. 涓烘瘡涓楠ょ敓鎴?D琛ㄦ牸
            Map<String, List<Map<String, Object>>> allTableData = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : allStepResults.entrySet()) {
                String stepKey = entry.getKey();
                if (stepKey.startsWith("step_")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                    List<Map<String, Object>> tableData = generateStepResultTable(stepResult, regionCodes);
                    allTableData.put(stepKey, tableData);
                }
            }

            // 6. 鏋勫缓杩斿洖缁撴灉
            Map<String, Object> result = new HashMap<>();
            result.put("algorithmId", algorithmId);
            result.put("executedUpToStep", upToStepOrder);
            result.put("stepResults", allStepResults);
            result.put("tableData", allTableData);
            result.put("success", true);
            result.put("executionTime", new Date());

            log.info("鎵归噺鎵ц绠楁硶姝ラ瀹屾垚锛屾墽琛屽埌绗瑊}姝?, upToStepOrder);
            return result;

        } catch (Exception e) {
            log.error("鎵归噺鎵ц绠楁硶姝ラ澶辫触", e);
            throw new RuntimeException("鎵归噺鎵ц绠楁硶姝ラ澶辫触: " + e.getMessage(), e);
        }
    }

    /**
     * 鍐呴儴鏂规硶锛氭墽琛岀畻娉曟楠ょ洿鍒版寚瀹氶『搴?
     */
    private Map<String, Object> executeAlgorithmStepsInternalUpTo(List<AlgorithmStep> algorithmSteps, Integer upToStepOrder, 
                                                                  List<String> regionCodes, Map<String, Object> globalContext) {
        Map<String, Object> stepResults = new HashMap<>();
        
        for (AlgorithmStep algorithmStep : algorithmSteps) {
            if (algorithmStep.getStepOrder() <= upToStepOrder) {
                log.info("鎵ц绠楁硶姝ラ: {} - {}, order={}", algorithmStep.getStepCode(), algorithmStep.getStepName(), algorithmStep.getStepOrder());
                
                try {
                    Map<String, Object> stepResult = executeAlgorithmStepInternal(algorithmStep, regionCodes, globalContext);
                    stepResults.put("step_" + algorithmStep.getStepCode(), stepResult);
                    
                    // 灏嗘楠ょ粨鏋滃悎骞跺埌鍏ㄥ眬涓婁笅鏂囷紙渚涘悗缁楠や娇鐢級
                    globalContext.put("step_" + algorithmStep.getStepCode(), stepResult);
                    
                    log.info("绠楁硶姝ラ {} 鎵ц瀹屾垚", algorithmStep.getStepCode());
                } catch (Exception e) {
                    log.error("绠楁硶姝ラ {} 鎵ц澶辫触: {}", algorithmStep.getStepCode(), e.getMessage(), e);
                    throw new RuntimeException("绠楁硶姝ラ " + algorithmStep.getStepName() + " 鎵ц澶辫触: " + e.getMessage(), e);
                }
            }
        }
        
        return stepResults;
    }

    /**
     * 鍐呴儴鏂规硶锛氭墽琛屽崟涓畻娉曟楠?
     */
    private Map<String, Object> executeAlgorithmStepInternal(AlgorithmStep algorithmStep, List<String> regionCodes, Map<String, Object> globalContext) {
        // 鑾峰彇璇ユ楠ょ殑鎵€鏈夊叕寮忓苟鎸夐『搴忔帓搴?
        QueryWrapper<FormulaConfig> formulaQuery = new QueryWrapper<>();
        formulaQuery.eq("algorithm_step_id", algorithmStep.getId().toString())
                .eq("status", 1)
                .orderByAsc("id");
        List<FormulaConfig> formulas = formulaConfigMapper.selectList(formulaQuery);

        if (formulas.isEmpty()) {
            log.warn("绠楁硶姝ラ {} 娌℃湁閰嶇疆鍏紡", algorithmStep.getStepCode());
            return new HashMap<>();
        }

        // 鍒濆鍖栨楠ょ粨鏋?
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", algorithmStep.getId());
        stepResult.put("stepName", algorithmStep.getStepName());
        stepResult.put("stepCode", algorithmStep.getStepCode());

        // 绗竴閬嶏細涓烘墍鏈夊湴鍖哄噯澶囦笂涓嬫枃鏁版嵁
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();

        // 鑾峰彇modelId浠ュ喅瀹氫娇鐢ㄥ摢涓暟鎹簮
        Long modelId = (Long) globalContext.get("modelId");

        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(globalContext);
            regionContext.put("currentRegionCode", regionCode);

            // 鏍规嵁modelId閫夋嫨涓嶅悓鐨勬暟鎹簮
            if (modelId != null && modelId == 4) {
                // 绀惧尯妯″瀷(modelId=4)锛氫粠community_disaster_reduction_capacity琛ㄥ姞杞芥暟鎹?
                // 浣跨敤selectMaps鐩存帴杩斿洖Map锛宬ey涓烘暟鎹簱瀛楁鍚嶏紝鍙洿鎺ュ尮閰嶇畻娉曡〃杈惧紡涓殑鍙橀噺鍚?
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);

                if (communityDataList != null && !communityDataList.isEmpty()) {
                    Map<String, Object> communityDataMap = communityDataList.get(0);
                    addMapDataToContext(regionContext, communityDataMap);
                }
            } else {
                // 涔￠晣妯″瀷(modelId=3)锛氫粠survey_data琛ㄥ姞杞芥暟鎹?
                QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
                dataQuery.eq("region_code", regionCode);
                SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);

                if (surveyData != null) {
                    addSurveyDataToContext(regionContext, surveyData);
                }
            }

            // 鍐嶅姞杞藉墠闈㈡楠ょ殑杈撳嚭缁撴灉锛堣绠楃粨鏋滐級锛岃繖鏍蜂細瑕嗙洊鍘熷鏁版嵁涓殑鍚屽悕瀛楁
            loadPreviousStepOutputs(regionContext, regionCode, globalContext);

            allRegionContexts.put(regionCode, regionContext);
        }
        
        // 绗簩閬嶏細涓烘瘡涓湴鍖烘墽琛屽叕寮忥紙鏀寔鐗规畩鏍囪锛?
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToFormulaName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            log.debug("涓哄湴鍖?{} 鎵ц鍏紡", regionCode);
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> formulaOutputs = new LinkedHashMap<>();
            
            // 鎸夐『搴忔墽琛屾瘡涓叕寮?
            for (FormulaConfig formula : formulas) {
                try {
                    log.debug("鎵ц鍏紡: {} - {}", formula.getFormulaName(), formula.getFormulaExpression());
                    
                    Object result;
                    String expression = formula.getFormulaExpression();
                    
                    // 妫€鏌ユ槸鍚︽槸鐗规畩鏍囪
                    if (expression != null && expression.startsWith("@")) {
                        // 瑙ｆ瀽鐗规畩鏍囪: @MARKER:params
                        String[] parts = expression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";
                        
                        log.info("鎵ц鐗规畩鏍囪鍏紡: marker={}, params={}", marker, params);
                        
                        // 璋冪敤鐗规畩绠楁硶鏈嶅姟
                        result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 纭繚鏁板€肩被鍨嬭浆鎹㈠苟鏍煎紡鍖栦负8浣嶅皬鏁?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    } else {
                        // 鎵ц鏍囧噯QLExpress琛ㄨ揪寮?
                        result = qlExpressService.execute(expression, regionContext);
                        
                        // 纭繚鏁板€肩被鍨嬬殑缁撴灉杞崲涓篋ouble骞舵牸寮忓寲涓?浣嶅皬鏁?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    }
                    
                    // 淇濆瓨鍏紡杈撳嚭鍒颁笂涓嬫枃锛堜緵鍚庣画鍏紡浣跨敤锛?
                    String outputParam = formula.getOutputVariable();
                    if (outputParam != null && !outputParam.isEmpty()) {
                        regionContext.put(outputParam, result);
                        allRegionContexts.put(regionCode, regionContext);  // 鏇存柊鍏ㄥ眬涓婁笅鏂?
                        formulaOutputs.put(outputParam, result);
                        outputToFormulaName.put(outputParam, formula.getFormulaName());
                    }
                    
                    log.debug("鍏紡 {} 鎵ц缁撴灉: {}", formula.getFormulaName(), result);
                } catch (Exception e) {
                    log.error("鍏紡 {} 鎵ц澶辫触: {}", formula.getFormulaName(), e.getMessage(), e);
                    throw new RuntimeException("鍏紡 " + formula.getFormulaName() + " 鎵ц澶辫触: " + e.getMessage(), e);
                }
            }
            
            regionResults.put(regionCode, formulaOutputs);
        }
        
        // 淇濆瓨杈撳嚭鍙傛暟鍒板叕寮忓悕绉扮殑鏄犲皠
        if (!outputToFormulaName.isEmpty()) {
            stepResult.put("outputToFormulaName", outputToFormulaName);
        }

        stepResult.put("regionResults", regionResults);
        return stepResult;
    }

    /**
     * 涓哄崟涓楠ょ敓鎴?D琛ㄦ牸鏁版嵁
     */
    private List<Map<String, Object>> generateStepResultTable(Map<String, Object> stepResult, List<String> regionCodes) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> regionResults = 
                (Map<String, Map<String, Object>>) stepResult.get("regionResults");
        
        @SuppressWarnings("unchecked")
        Map<String, String> outputToFormulaName = 
                (Map<String, String>) stepResult.get("outputToFormulaName");
        
        if (regionResults == null) {
            return tableData;
        }
        
        // 涓烘瘡涓湴鍖虹敓鎴愪竴琛屾暟鎹?
        for (String regionCode : regionCodes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regionCode", regionCode);
            
            // 鑾峰彇鍦板尯鍚嶇О - 浼樺厛浠巆ommunity琛紝鐒跺悗survey_data琛?
            String regionName = regionCode;
            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", regionCode);
            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
            if (communityData != null) {
                if (communityData.getCommunityName() != null) {
                    regionName = communityData.getCommunityName();
                } else if (communityData.getTownshipName() != null) {
                    regionName = communityData.getTownshipName();
                }
            } else {
                QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                surveyQuery.eq("region_code", regionCode);
                SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
                if (surveyData != null && surveyData.getTownship() != null) {
                    regionName = surveyData.getTownship();
                }
            }
            row.put("regionName", regionName);
            
            // 娣诲姞璇ュ湴鍖虹殑鎵€鏈夎緭鍑虹粨鏋?
            Map<String, Object> outputs = regionResults.get(regionCode);
            if (outputs != null) {
                for (Map.Entry<String, Object> output : outputs.entrySet()) {
                    String outputParam = output.getKey();
                    String columnName;
                    
                    // 浼樺厛浣跨敤鍏紡鍚嶇О浣滀负鍒楀悕
                    if (outputToFormulaName != null && outputToFormulaName.containsKey(outputParam)) {
                        columnName = outputToFormulaName.get(outputParam);
                    } else {
                        columnName = outputParam;
                    }
                    
                    // 鏍煎紡鍖栨暟鍊间负8浣嶅皬鏁?
                    Object value = output.getValue();
                    if (value != null && value instanceof Number) {
                        double doubleValue = ((Number) value).doubleValue();
                        value = Double.parseDouble(String.format("%.8f", doubleValue));
                    }
                    row.put(columnName, value);
                }
            }
            
            tableData.add(row);
        }
        
        return tableData;
    }

    /**
     * 浠庤〃鏍兼暟鎹拰姝ラ杈撳嚭鍙傛暟鐢熸垚 columns 鏁扮粍锛屾瘡鍒楁爣璁版墍灞炴楠?
     * 
     * @param tableData 琛ㄦ牸鏁版嵁
     * @param stepOutputParams 姝ラ搴忓彿 -> 杈撳嚭鍙傛暟鍚嶇О鍒楄〃鐨勬槧灏?
     * @return columns 鏁扮粍
     */
    private List<Map<String, Object>> generateColumnsWithAllSteps(
            List<Map<String, Object>> tableData, 
            Map<Integer, List<String>> stepOutputParams) {
        
        List<Map<String, Object>> columns = new ArrayList<>();
        
        if (tableData == null || tableData.isEmpty()) {
            log.debug("琛ㄦ牸鏁版嵁涓虹┖锛岃繑鍥炵┖鐨?columns 鏁扮粍");
            return columns;
        }
        
        // 浠庣涓€琛屾暟鎹彁鍙栨墍鏈夊垪鍚?
        Map<String, Object> firstRow = tableData.get(0);
        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region"));
        
        // 鍒涘缓鍙嶅悜鏄犲皠锛氬垪鍚?-> 姝ラ搴忓彿
        Map<String, Integer> columnToStepOrder = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : stepOutputParams.entrySet()) {
            Integer stepOrder = entry.getKey();
            List<String> outputNames = entry.getValue();
            for (String outputName : outputNames) {
                columnToStepOrder.put(outputName, stepOrder);
            }
        }
        
        log.info("寮€濮嬬敓鎴?columns 鏁扮粍锛堝叏妯″瀷锛夛紝鎬诲垪鏁? {}", firstRow.size());
        log.debug("鍒楀悕鍒版楠ゅ簭鍙风殑鏄犲皠: {}", columnToStepOrder);
        
        for (String columnName : firstRow.keySet()) {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", columnName);
            column.put("label", columnName);
            
            // 璁剧疆鍒楀
            if ("regionCode".equals(columnName)) {
                column.put("width", 150);
            } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
                column.put("width", 120);
            } else {
                column.put("width", 120);
                // 闈炲熀纭€鍒楁坊鍔?stepOrder
                Integer stepOrder = columnToStepOrder.get(columnName);
                if (stepOrder != null) {
                    column.put("stepOrder", stepOrder);
                    log.debug("鍒?{} 鏍囪涓烘楠?{}", columnName, stepOrder);
                } else {
                    log.warn("鍒?{} 鏈壘鍒板搴旂殑姝ラ搴忓彿", columnName);
                }
            }
            
            columns.add(column);
        }
        
        log.info("瀹屾垚 columns 鏁扮粍鐢熸垚锛堝叏妯″瀷锛夛紝鍏?{} 鍒楋紝鍏朵腑 {} 鍒楀寘鍚?stepOrder", 
                columns.size(), columns.stream().filter(c -> c.containsKey("stepOrder")).count());
        
        return columns;
    }

    // 鏂扮増锛氭壂鎻忔墍鏈夎锛屽悎骞跺垪锛屽啀鏍规嵁 stepOutputParams 鍙嶆爣璁?stepOrder锛岄伩鍏嶉琛屼笉鍖呭惈鍏ㄩ儴姝ラ鍒楀鑷寸己澶?
    private List<Map<String, Object>> generateColumnsWithAllStepsV2(
            List<Map<String, Object>> tableData,
            Map<Integer, List<String>> stepOutputParams) {
        List<Map<String, Object>> columns = new ArrayList<>();
        if (tableData == null || tableData.isEmpty()) {
            return columns;
        }

        // 鍩虹鍒?
        Set<String> baseColumns = new LinkedHashSet<>(Arrays.asList("regionCode", "regionName", "region"));

        // 鍒楀埌姝ラ搴忓彿
        Map<String, Integer> columnToStepOrder = new HashMap<>();
        for (Map.Entry<Integer, List<String>> e : stepOutputParams.entrySet()) {
            Integer stepOrder = e.getKey();
            for (String name : e.getValue()) {
                columnToStepOrder.put(name, stepOrder);
            }
        }

        // 鏀堕泦鎵€鏈夊垪锛堜繚鐣欓娆″嚭鐜伴『搴忥級
        LinkedHashSet<String> allColumnsOrdered = new LinkedHashSet<>();
        for (Map<String, Object> row : tableData) {
            allColumnsOrdered.addAll(row.keySet());
        }

        for (String columnName : allColumnsOrdered) {
            Map<String, Object> col = new LinkedHashMap<>();
            col.put("prop", columnName);
            col.put("label", columnName);

            if ("regionCode".equals(columnName)) {
                col.put("width", 150);
            } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
                col.put("width", 120);
            } else {
                col.put("width", 120);
                Integer stepOrder = columnToStepOrder.get(columnName);
                if (stepOrder != null) {
                    col.put("stepOrder", stepOrder);
                }
            }

            columns.add(col);
        }

        return columns;
    }

    /**
     * 浠庤〃鏍兼暟鎹敓鎴?columns 鏁扮粍锛屽苟涓洪潪鍩虹鍒楁坊鍔?stepOrder
     * 
     * @param tableData 琛ㄦ牸鏁版嵁
     * @param stepOrder 褰撳墠姝ラ搴忓彿
     * @return columns 鏁扮粍
     */
    private List<Map<String, Object>> generateColumnsWithStepOrder(
            List<Map<String, Object>> tableData, Integer stepOrder) {
        
        List<Map<String, Object>> columns = new ArrayList<>();
        
        if (tableData == null || tableData.isEmpty()) {
            log.debug("琛ㄦ牸鏁版嵁涓虹┖锛岃繑鍥炵┖鐨?columns 鏁扮粍");
            return columns;
        }
        
        // 浠庣涓€琛屾暟鎹彁鍙栨墍鏈夊垪鍚?
        Map<String, Object> firstRow = tableData.get(0);
        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region"));
        
        log.info("寮€濮嬬敓鎴?columns 鏁扮粍锛屾楠ゅ簭鍙? {}, 鍒楁暟: {}", stepOrder, firstRow.size());
        
        for (String columnName : firstRow.keySet()) {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", columnName);
            column.put("label", columnName);  // 浣跨敤涓枃鍚嶇О浣滀负 label
            
            // 璁剧疆鍒楀
            if ("regionCode".equals(columnName)) {
                column.put("width", 150);
            } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
                column.put("width", 120);
            } else {
                column.put("width", 120);
                // 闈炲熀纭€鍒楁坊鍔?stepOrder
                column.put("stepOrder", stepOrder);
                log.debug("鍒?{} 鏍囪涓烘楠?{}", columnName, stepOrder);
            }
            
            columns.add(column);
        }
        
        log.info("瀹屾垚 columns 鏁扮粍鐢熸垚锛屽叡 {} 鍒楋紝鍏朵腑 {} 鍒楀寘鍚?stepOrder", 
                columns.size(), columns.stream().filter(c -> c.containsKey("stepOrder")).count());
        
        return columns;
    }

    @Autowired
    private AlgorithmStepMapper algorithmStepMapper;
    
    @Autowired
    private AlgorithmConfigMapper algorithmConfigMapper;
    
    @Autowired
    private FormulaConfigMapper formulaConfigMapper;

    /**
     * 鎵ц涔￠晣鑱氬悎
     * 鎸変埂闀囧垎缁勶紝瀵圭ぞ鍖烘暟鎹繘琛岃仛鍚堣绠楋紙姹傚拰鍚庨櫎浠ョぞ鍖烘暟閲忥級
     * 
     * @param stepId 姝ラID
     * @param regionCodes 绀惧尯浠ｇ爜鍒楄〃
     * @param inputData 杈撳叆鏁版嵁锛堝寘鍚楠?鐨勭ぞ鍖虹骇鍒绠楃粨鏋滐級
     * @return 涔￠晣绾у埆鐨勮仛鍚堢粨鏋?
     */
    private Map<String, Object> executeTownshipAggregation(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        log.info("寮€濮嬫墽琛屼埂闀囪仛鍚? stepId={}, regionCodes.size={}", stepId, regionCodes.size());
        
        // 1. 鑾峰彇姝ラ淇℃伅
        ModelStep step = modelStepMapper.selectById(stepId);
        if (step == null || step.getStatus() == 0) {
            throw new RuntimeException("姝ラ涓嶅瓨鍦ㄦ垨宸茬鐢?);
        }
        
        // 2. 鑾峰彇璇ユ楠ょ殑鎵€鏈夌畻娉?
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .orderByAsc("algorithm_order");
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);
        
        if (algorithms == null || algorithms.isEmpty()) {
            log.warn("姝ラ {} 娌℃湁閰嶇疆绠楁硶", step.getStepCode());
            return new HashMap<>();
        }
        
        // 3. 鎸変埂闀囧垎缁勬敹闆嗙ぞ鍖烘暟鎹?
        Map<String, List<Map<String, Object>>> townshipGroups = new LinkedHashMap<>();
        Map<String, String> townshipToFirstRegionCode = new HashMap<>();  // 璁板綍姣忎釜涔￠晣鐨勭涓€涓ぞ鍖轰唬鐮侊紙鐢ㄤ簬鍚庣画姝ラ锛?
        
        for (String regionCode : regionCodes) {
            // 鑾峰彇绀惧尯鐨勪埂闀囦俊鎭?
            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", regionCode);
            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
            
            if (communityData == null) {
                log.warn("鏈壘鍒扮ぞ鍖烘暟鎹? regionCode={}", regionCode);
                continue;
            }
            
            String townshipName = communityData.getTownshipName();
            if (townshipName == null || townshipName.isEmpty()) {
                log.warn("绀惧尯 {} 娌℃湁涔￠晣淇℃伅", regionCode);
                continue;
            }
            
            // 鑾峰彇姝ラ1鐨勮緭鍑虹粨鏋滐紙绀惧尯绾у埆鐨勮兘鍔涘€硷級
            Map<String, Object> communityContext = new HashMap<>();
            communityContext.put("currentRegionCode", regionCode);
            
            // 浠巌nputData涓幏鍙栨楠?鐨勭粨鏋?
            // inputData涓寘鍚?"step_XXX" 鐨勯敭锛屽叾鍊兼槸姝ラ鐨勬墽琛岀粨鏋?
            for (Map.Entry<String, Object> entry : inputData.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("step_")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = 
                            (Map<String, Map<String, Object>>) stepResult.get("regionResults");
                    
                    if (regionResults != null && regionResults.containsKey(regionCode)) {
                        // 灏嗚绀惧尯鍦ㄨ繖涓楠ょ殑杈撳嚭娣诲姞鍒颁笂涓嬫枃
                        Map<String, Object> outputs = regionResults.get(regionCode);
                        communityContext.putAll(outputs);
                        log.debug("绀惧尯 {} 浠?{} 鍔犺浇浜?{} 涓緭鍑?, regionCode, key, outputs.size());
                    }
                }
            }
            
            // derive township inputs from community fields (ASCII only)
            Map<String, Double> _derived = deriveTownshipInputsFromCommunity(communityContext);
            if (_derived != null && !_derived.isEmpty()) {
                for (Map.Entry<String, Double> _e : _derived.entrySet()) {
                    communityContext.put(_e.getKey(), _e.getValue());
                }
            }
            // 鎸変埂闀囧垎缁?
            townshipGroups.computeIfAbsent(townshipName, k -> new ArrayList<>()).add(communityContext);
            
            // 璁板綍姣忎釜涔￠晣鐨勭涓€涓ぞ鍖轰唬鐮?
            townshipToFirstRegionCode.putIfAbsent(townshipName, regionCode);
            
            log.debug("绀惧尯 {} 褰掑睘涔￠晣 {}", regionCode, townshipName);
        }
        
        log.info("鎸変埂闀囧垎缁勫畬鎴愶紝鍏?{} 涓埂闀?, townshipGroups.size());
        
        // 4. 瀵规瘡涓埂闀囨墽琛岃仛鍚堣绠?
        Map<String, Map<String, Object>> townshipResults = new LinkedHashMap<>();
        Map<String, String> outputToAlgorithmName = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : townshipGroups.entrySet()) {
            String townshipName = entry.getKey();
            List<Map<String, Object>> communities = entry.getValue();
            int communityCount = communities.size();
            
            log.info("澶勭悊涔￠晣: {}, 绀惧尯鏁伴噺: {}", townshipName, communityCount);
            
            Map<String, Object> townshipOutput = new LinkedHashMap<>();
            
            // 瀵规瘡涓畻娉曟墽琛岃仛鍚?
            for (StepAlgorithm algorithm : algorithms) {
                String qlExpression = algorithm.getQlExpression();
                String outputParam = algorithm.getOutputParam();
                
                if (outputParam == null || outputParam.isEmpty()) {
                    continue;
                }
                
                // 浠庤〃杈惧紡涓彁鍙栬緭鍏ュ瓧娈靛悕锛堜緥濡傦細PLAN_CONSTRUCTION锛?
                String inputField = qlExpression.trim();
                
                // 璁＄畻鑱氬悎鍊硷細姹傚拰鍚庨櫎浠ョぞ鍖烘暟閲?
                double sum = 0.0;
                int validCount = 0;
                
                for (Map<String, Object> community : communities) {
                    Object value = community.get(inputField);
                    if (value != null) {
                        sum += toDouble(value);
                        validCount++;
                    }
                }
                
                // 璁＄畻骞冲潎鍊?
                double average = validCount > 0 ? sum / validCount : 0.0;
                
                // 鏍煎紡鍖栦负8浣嶅皬鏁?
                average = Double.parseDouble(String.format("%.8f", average));
                
                townshipOutput.put(outputParam, average);
                outputToAlgorithmName.put(outputParam, algorithm.getAlgorithmName());
                
                log.debug("涔￠晣 {} 鐨?{} 鑱氬悎缁撴灉: sum={}, count={}, avg={}", 
                        townshipName, outputParam, sum, communityCount, average);
            }
            
            // 浣跨敤"TOWNSHIP_"鍓嶇紑 + 涔￠晣鍚嶇О浣滀负铏氭嫙鐨剅egionCode
            // 杩欐牱鍙互纭繚姣忎釜涔￠晣鏈夊敮涓€鐨勬爣璇嗭紝涓斾笉浼氫笌绀惧尯浠ｇ爜鍐茬獊
            String townshipRegionCode = "TOWNSHIP_" + townshipName;
            townshipResults.put(townshipRegionCode, townshipOutput);
            
            // 鍚屾椂鍦ㄤ笂涓嬫枃涓繚瀛樹埂闀囧悕绉帮紝渚沢enerateResultTable浣跨敤
            townshipOutput.put("_townshipName", townshipName);
            townshipOutput.put("_firstCommunityCode", townshipToFirstRegionCode.get(townshipName));
        }
        
        // 5. 鏋勫缓姝ラ缁撴灉
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", stepId);
        stepResult.put("stepName", step.getStepName());
        stepResult.put("stepCode", step.getStepCode());
        stepResult.put("regionResults", townshipResults);
        stepResult.put("outputToAlgorithmName", outputToAlgorithmName);
        
        log.info("涔￠晣鑱氬悎瀹屾垚锛屽叡 {} 涓埂闀?, townshipResults.size());
        
        return stepResult;
    }
    
    /**
     * 灏嗗璞¤浆鎹负Double
     */
    private Double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("鏃犳硶灏嗗瓧绗︿覆杞崲涓烘暟瀛? {}", value);
                return 0.0;
            }
        }
        log.warn("鏃犳硶杞崲涓篋ouble鐨勭被鍨? {}", value.getClass());
        return 0.0;
    }
}
    // derive 9 township inputs from community context when missing (ASCII only)
    private Map<String, Double> deriveTownshipInputsFromCommunity(Map<String, Object> c) {
        Map<String, Double> r = new HashMap<>();
        Double pop = getNum(c, "RESIDENT_POPULATION", "resident_population");
        if (pop == null || pop <= 0) pop = 1.0;

        double hasPlan = getNumOrZero(c, "HAS_EMERGENCY_PLAN", "has_emergency_plan");
        double hasVul  = getNumOrZero(c, "HAS_VULNERABLE_GROUPS_LIST", "has_vulnerable_groups_list");
        double hasHaz  = getNumOrZero(c, "HAS_DISASTER_POINTS_LIST", "has_disaster_points_list");
        double hasMap  = getNumOrZero(c, "HAS_DISASTER_MAP", "has_disaster_map");

        double fund     = getNumOrZero(c, "LAST_YEAR_FUNDING_AMOUNT", "last_year_funding_amount");
        double material  = getNumOrZero(c, "MATERIALS_EQUIPMENT_VALUE", "materials_equipment_value");
        double medical   = getNumOrZero(c, "MEDICAL_SERVICE_COUNT", "medical_service_count");
        double militia   = getNumOrZero(c, "MILITIA_RESERVE_COUNT", "militia_reserve_count");
        double volunteer = getNumOrZero(c, "REGISTERED_VOLUNTEER_COUNT", "registered_volunteer_count");
        double train     = getNumOrZero(c, "LAST_YEAR_TRAINING_PARTICIPANTS", "last_year_training_participants");
        double drill     = getNumOrZero(c, "LAST_YEAR_DRILL_PARTICIPANTS", "last_year_drill_participants");
        double shelter   = getNumOrZero(c, "EMERGENCY_SHELTER_CAPACITY", "emergency_shelter_capacity");

        double PLAN_CONSTRUCTION   = clamp01(hasPlan);
        double HAZARD_INSPECTION   = clamp01((hasVul + hasHaz) / 2.0);
        double RISK_ASSESSMENT     = clamp01(hasMap);
        double FINANCIAL_INPUT     = (fund / pop) * 10000.0;
        double MATERIAL_RESERVE    = (material / pop) * 10000.0;
        double MEDICAL_SUPPORT     = (medical / pop) * 10000.0;
        double SELF_MUTUAL_AID     = ((militia + volunteer) / pop) * 10000.0;
        double PUBLIC_EVACUATION   = ((train + drill) / pop) * 100.0;
        double RELOCATION_SHELTER  = (shelter / pop);

        r.put("PLAN_CONSTRUCTION", round8(PLAN_CONSTRUCTION));
        r.put("HAZARD_INSPECTION", round8(HAZARD_INSPECTION));
        r.put("RISK_ASSESSMENT", round8(RISK_ASSESSMENT));
        r.put("FINANCIAL_INPUT", round8(FINANCIAL_INPUT));
        r.put("MATERIAL_RESERVE", round8(MATERIAL_RESERVE));
        r.put("MEDICAL_SUPPORT", round8(MEDICAL_SUPPORT));
        r.put("SELF_MUTUAL_AID", round8(SELF_MUTUAL_AID));
        r.put("PUBLIC_EVACUATION", round8(PUBLIC_EVACUATION));
        r.put("RELOCATION_SHELTER", round8(RELOCATION_SHELTER));
        return r;
    }

    private Double getNum(Map<String, Object> m, String k1, String k2) {
        Double v = parseNum(m.get(k1));
        if (v == null && k2 != null) v = parseNum(m.get(k2));
        return v;
    }
    private double getNumOrZero(Map<String, Object> m, String k1, String k2) {
        Double v = getNum(m, k1, k2);
        return v != null ? v : 0.0;
    }
    private Double parseNum(Object o) {
        if (o instanceof Number) return ((Number)o).doubleValue();
        if (o instanceof String) {
            try { return Double.parseDouble((String)o); } catch (Exception ignore) {}
        }
        return null;
    }
    private double clamp01(double v) { return v < 0 ? 0.0 : (v > 1 ? 1.0 : v); }
    private double round8(double v) { return Double.parseDouble(String.format("%.8f", v)); }
