import java.util.*;

/**
 * 测试归一化算法修复效果
 */
public class TestNormalizationFix {

    public static void main(String[] args) {
        System.out.println("=== 测试归一化算法修复效果 ===");

        // 模拟原始数据（基于真实数据）
        Map<String, Map<String, Object>> allRegionData = new HashMap<>();

        // 青竹街道数据
        Map<String, Object> qingzhu = new HashMap<>();
        qingzhu.put("managementCapability", 0.19535256); // (2/102379)*10000
        qingzhu.put("riskAssessmentCapability", 1.0);
        qingzhu.put("fundingCapability", 1.95352563);
        qingzhu.put("materialReserveCapability", 0.87908653);
        qingzhu.put("medicalSupportCapability", 0.0); // 这个在所有地区都为0
        allRegionData.put("511425001", qingzhu);

        // 汉阳镇数据
        Map<String, Object> hanyang = new HashMap<>();
        hanyang.put("managementCapability", 3.15706393); // (2/6335)*10000
        hanyang.put("riskAssessmentCapability", 1.0);
        hanyang.put("fundingCapability", 110.49723757);
        hanyang.put("materialReserveCapability", 4.73559590);
        hanyang.put("medicalSupportCapability", 0.0); // 这个在所有地区都为0
        allRegionData.put("511425102", hanyang);

        // 瑞峰镇数据
        Map<String, Object> ruifeng = new HashMap<>();
        ruifeng.put("managementCapability", 63.20651513); // (52/8227)*10000
        ruifeng.put("riskAssessmentCapability", 1.0);
        ruifeng.put("fundingCapability", 76.57712410);
        ruifeng.put("materialReserveCapability", 24.31019813);
        ruifeng.put("medicalSupportCapability", 0.0); // 这个在所有地区都为0
        allRegionData.put("511425108", ruifeng);

        System.out.println("\n=== 原始数据 ===");
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        // 测试修复前的归一化算法（会返回0）
        System.out.println("\n=== 修复前的归一化结果（分母为0时返回0） ===");
        for (String region : allRegionData.keySet()) {
            double oldResult = oldNormalize("medicalSupportCapability", region, allRegionData);
            System.out.println(region + " medicalSupportCapability: " + oldResult);
        }

        // 测试修复后的归一化算法
        System.out.println("\n=== 修复后的归一化结果（分母为0时返回原值） ===");
        for (String region : allRegionData.keySet()) {
            double newResult = newNormalize("medicalSupportCapability", region, allRegionData);
            System.out.println(region + " medicalSupportCapability: " + newResult);
        }

        // 测试正常的归一化（分母不为0的情况）
        System.out.println("\n=== 正常归一化测试（managementCapability） ===");
        for (String region : allRegionData.keySet()) {
            double result = newNormalize("managementCapability", region, allRegionData);
            System.out.println(region + " managementCapability: " + result);
        }
    }

    // 修复前的归一化算法
    private static double oldNormalize(String indicatorName, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        // 收集所有值
        List<Double> allValues = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            Object value = entry.getValue().get(indicatorName);
            if (value != null) {
                allValues.add(toDouble(value));
            }
        }

        // 计算分母
        double sumSquares = allValues.stream().mapToDouble(v -> v * v).sum();
        double denominator = Math.sqrt(sumSquares);

        // 修复前：分母为0时返回0
        if (denominator == 0) {
            System.out.println("[修复前] 分母为0，返回0: indicator=" + indicatorName);
            return 0.0;
        }

        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        Object currentValue = currentData.get(indicatorName);
        return toDouble(currentValue) / denominator;
    }

    // 修复后的归一化算法
    private static double newNormalize(String indicatorName, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        // 收集所有值
        List<Double> allValues = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            Object value = entry.getValue().get(indicatorName);
            if (value != null) {
                allValues.add(toDouble(value));
            }
        }

        // 计算分母
        double sumSquares = allValues.stream().mapToDouble(v -> v * v).sum();
        double denominator = Math.sqrt(sumSquares);

        // 修复后：分母为0时返回当前区域的值
        if (denominator == 0) {
            System.out.println("[修复后] 分母为0，返回当前值: indicator=" + indicatorName);
            Map<String, Object> currentData = allRegionData.get(currentRegionCode);
            if (currentData != null) {
                Object currentValue = currentData.get(indicatorName);
                if (currentValue != null) {
                    return toDouble(currentValue);
                }
            }
            return 0.0;
        }

        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        Object currentValue = currentData.get(indicatorName);
        return toDouble(currentValue) / denominator;
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}