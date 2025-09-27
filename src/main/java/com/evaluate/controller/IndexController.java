package com.evaluate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 * 处理根路径访问请求
 */
@RestController
public class IndexController {

    /**
     * 系统首页
     * @return 系统信息
     */
    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> result = new HashMap<>();
        result.put("system", "减灾能力评估系统");
        result.put("version", "1.0.0");
        result.put("status", "运行中");
        result.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("description", "基于Java+MyBatis Plus+MySQL的专业减灾能力评估平台");
        
        // API接口列表
        Map<String, String> apis = new HashMap<>();
        apis.put("调查数据管理", "/api/data/survey");
        apis.put("权重配置管理", "/api/data/weight");
        apis.put("指标权重管理", "/api/data/weight/indicator");
        apis.put("评估计算", "/api/evaluation");
        apis.put("算法配置", "/api/algorithm");
        result.put("apis", apis);
        
        return result;
    }
    
    /**
     * 系统健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }
    
    /**
     * 热重载测试接口
     * @return 测试信息
     */
    @GetMapping("/hotreload-test")
    public Map<String, Object> hotReloadTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "热重载功能测试成功！");
        result.put("feature", "Spring Boot DevTools");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("version", "v2.0 - 热重载测试版本");
        return result;
    }
}