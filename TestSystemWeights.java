import java.util.*;

public class TestSystemWeights {

    // 系统默认一级权重
    static Map<String, Double> primaryWeights = new HashMap<>();
    static {
        primaryWeights.put("disasterManagement", 0.33);
        primaryWeights.put("disasterPreparedness", 0.32);
        primaryWeights.put("selfRescueTransfer", 0.35);
    }

    // 系统默认二级权重
    static Map<String, Double> secondaryWeights = new HashMap<>();
    static {
        secondaryWeights.put("teamManagement", 0.37);
        secondaryWeights.put("riskAssessment", 0.31);
        secondaryWeights.put("financialInput", 0.32);
        secondaryWeights.put("materialReserve", 0.51);
        secondaryWeights.put("medicalSupport", 0.49);
        secondaryWeights.put("selfRescue", 0.33);
        secondaryWeights.put("publicAvoidance", 0.33);
        secondaryWeights.put("relocationCapacity", 0.34);
    }

    // 归一化测试数据（模拟系统获取的原始数据经过归一化后的值）
    static Map<String, Map<String, Double>> normalizedData = new HashMap<>();

    static {
        // 这些值是假设的，代表系统从数据库获取原始数据并归一化后的结果
        normalizedData.put("Qingzhu", new HashMap<>());
        normalizedData.get("Qingzhu").put("teamManagement", 0.01);
        normalizedData.get("Qingzhu").put("riskAssessment", 1.0);
        normalizedData.get("Qingzhu").put("financialInput", 0.01);
        normalizedData.get("Qingzhu").put("materialReserve", 0.02);
        normalizedData.get("Qingzhu").put("medicalSupport", 0.95);
        normalizedData.get("Qingzhu").put("selfRescue", 0.15);
        normalizedData.get("Qingzhu").put("publicAvoidance", 0.01);
        normalizedData.get("Qingzhu").put("relocationCapacity", 0.01);

        normalizedData.put("Hanyang", new HashMap<>());
        normalizedData.get("Hanyang").put("teamManagement", 0.15);
        normalizedData.get("Hanyang").put("riskAssessment", 1.0);
        normalizedData.get("Hanyang").put("financialInput", 0.80);
        normalizedData.get("Hanyang").put("materialReserve", 0.40);
        normalizedData.get("Hanyang").put("medicalSupport", 0.35);
        normalizedData.get("Hanyang").put("selfRescue", 0.60);
        normalizedData.get("Hanyang").put("publicAvoidance", 0.70);
        normalizedData.get("Hanyang").put("relocationCapacity", 0.45);

        normalizedData.put("Ruifeng", new HashMap<>());
        normalizedData.get("Ruifeng").put("teamManagement", 0.95);
        normalizedData.get("Ruifeng").put("riskAssessment", 1.0);
        normalizedData.get("Ruifeng").put("financialInput", 0.50);
        normalizedData.get("Ruifeng").put("materialReserve", 0.90);
        normalizedData.get("Ruifeng").put("medicalSupport", 0.45);
        normalizedData.get("Ruifeng").put("selfRescue", 0.55);
        normalizedData.get("Ruifeng").put("publicAvoidance", 0.80);
        normalizedData.get("Ruifeng").put("relocationCapacity", 0.20);
    }

    // 计算定权值：归一化值 × 一级权重 × 二级权重
    static Map<String, Map<String, Double>> calculateWeightedValues() {
        Map<String, Map<String, Double>> weightedData = new HashMap<>();

        for (String town : normalizedData.keySet()) {
            Map<String, Double> weighted = new HashMap<>();
            Map<String, Double> normalized = normalizedData.get(town);

            // 队伍管理能力 = 归一化值 × 灾害管理能力一级权重 × 队伍管理能力二级权重
            weighted.put("teamManagement", normalized.get("teamManagement") * primaryWeights.get("disasterManagement") * secondaryWeights.get("teamManagement"));

            // 风险评估能力 = 归一化值 × 灾害管理能力一级权重 × 风险评估能力二级权重
            weighted.put("riskAssessment", normalized.get("riskAssessment") * primaryWeights.get("disasterManagement") * secondaryWeights.get("riskAssessment"));

            // 财政投入能力 = 归一化值 × 灾害管理能力一级权重 × 财政投入能力二级权重
            weighted.put("financialInput", normalized.get("financialInput") * primaryWeights.get("disasterManagement") * secondaryWeights.get("financialInput"));

            // 物资储备能力 = 归一化值 × 灾害备灾能力一级权重 × 物资储备能力二级权重
            weighted.put("materialReserve", normalized.get("materialReserve") * primaryWeights.get("disasterPreparedness") * secondaryWeights.get("materialReserve"));

            // 医疗保障能力 = 归一化值 × 灾害备灾能力一级权重 × 医疗保障能力二级权重
            weighted.put("medicalSupport", normalized.get("medicalSupport") * primaryWeights.get("disasterPreparedness") * secondaryWeights.get("medicalSupport"));

            // 自救互救能力 = 归一化值 × 自救转移能力一级权重 × 自救互救能力二级权重
            weighted.put("selfRescue", normalized.get("selfRescue") * primaryWeights.get("selfRescueTransfer") * secondaryWeights.get("selfRescue"));

            // 公众避险能力 = 归一化值 × 自救转移能力一级权重 × 公众避险能力二级权重
            weighted.put("publicAvoidance", normalized.get("publicAvoidance") * primaryWeights.get("selfRescueTransfer") * secondaryWeights.get("publicAvoidance"));

            // 转移安置能力 = 归一化值 × 自救转移能力一级权重 × 转移安置能力二级权重
            weighted.put("relocationCapacity", normalized.get("relocationCapacity") * primaryWeights.get("selfRescueTransfer") * secondaryWeights.get("relocationCapacity"));

            weightedData.put(town, weighted);
        }

        return weightedData;
    }

    public static void main(String[] args) {
        System.out.println("=== 系统默认权重配置 ===");
        System.out.println("一级权重: " + primaryWeights);
        System.out.println("二级权重: " + secondaryWeights);

        Map<String, Map<String, Double>> weightedData = calculateWeightedValues();

        System.out.println("\n=== 系统计算的定权值 ===");
        for (String town : weightedData.keySet()) {
            Map<String, Double> weighted = weightedData.get(town);
            System.out.printf("%-10s: teamMgmt=%.8f, riskAssess=%.8f, financial=%.8f, material=%.8f, medical=%.8f, selfRescue=%.8f, publicAvoid=%.8f, relocation=%.8f%n",
                town,
                weighted.get("teamManagement"),
                weighted.get("riskAssessment"),
                weighted.get("financialInput"),
                weighted.get("materialReserve"),
                weighted.get("medicalSupport"),
                weighted.get("selfRescue"),
                weighted.get("publicAvoidance"),
                weighted.get("relocationCapacity")
            );
        }
    }
}