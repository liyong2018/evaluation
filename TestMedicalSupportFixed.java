import java.util.*;

public class TestMedicalSupportFixed {

    // 从数据库查询得到的医疗保障能力原始数据（按乡镇名称排序）
    static Map<String, Double> originalMedicalSupportValues = new LinkedHashMap<>();
    static {
        originalMedicalSupportValues.put("汉阳镇", 34.7277);
        originalMedicalSupportValues.put("瑞峰镇", 43.7584);
        originalMedicalSupportValues.put("白果乡", 25.1424);
        originalMedicalSupportValues.put("罗波乡", 30.9629);
        originalMedicalSupportValues.put("西龙镇", 1573.5535);
        originalMedicalSupportValues.put("青竹街道", 98.6530);
        originalMedicalSupportValues.put("高台镇", 20.3105);
    }

    // 数据库中的归一化结果（期望值）
    static Map<String, Double> expectedNormalizedValues = new LinkedHashMap<>();
    static {
        expectedNormalizedValues.put("汉阳镇", 0.02200369);
        expectedNormalizedValues.put("瑞峰镇", 0.02772557);
        expectedNormalizedValues.put("白果乡", 0.01593035);
        expectedNormalizedValues.put("罗波乡", 0.01961831);
        expectedNormalizedValues.put("西龙镇", 0.9970134);
        expectedNormalizedValues.put("青竹街道", 0.06250719);
        expectedNormalizedValues.put("高台镇", 0.01286883);
    }

    // 系统的归一化方法
    public static double normalizeIndicatorValue(double currentValue, List<Double> allValues) {
        if (allValues == null || allValues.isEmpty()) {
            return 0.0;
        }

        // 计算平方和
        double sumOfSquares = allValues.stream()
            .mapToDouble(value -> value * value)
            .sum();

        // 计算平方根
        double sqrtSumOfSquares = Math.sqrt(sumOfSquares);

        // 避免除零
        if (sqrtSumOfSquares == 0.0) {
            return 0.0;
        }

        // 返回归一化值：当前值 / SQRT(SUMSQ(所有值))
        return currentValue / sqrtSumOfSquares;
    }

    public static void main(String[] args) {
        System.out.println("=== 医疗保障能力归一化计算验证（数据库实际数据） ===\n");

        List<Double> allValues = new ArrayList<>(originalMedicalSupportValues.values());

        System.out.println("所有乡镇的原始医疗保障能力值:");
        originalMedicalSupportValues.forEach((township, value) -> 
            System.out.printf("%-10s: %.8f%n", township, value)
        );

        // 计算平方和
        double sumOfSquares = allValues.stream()
            .mapToDouble(value -> value * value)
            .sum();
        double sqrtSumOfSquares = Math.sqrt(sumOfSquares);

        System.out.println("\n归一化计算:");
        System.out.printf("平方和: %.8f%n", sumOfSquares);
        System.out.printf("平方根: %.8f%n", sqrtSumOfSquares);

        System.out.println("\n计算结果对比:");
        System.out.printf("%-12s %-20s %-20s %-20s %-15s%n", 
            "乡镇", "原始值", "计算归一化值", "数据库归一化值", "差异");
        System.out.println("-".repeat(95));

        boolean allMatch = true;
        for (String township : originalMedicalSupportValues.keySet()) {
            double originalValue = originalMedicalSupportValues.get(township);
            double calculated = normalizeIndicatorValue(originalValue, allValues);
            double expected = expectedNormalizedValues.get(township);
            double diff = Math.abs(calculated - expected);
            
            String status = diff < 0.0001 ? "✓" : "✗";
            if (diff >= 0.0001) {
                allMatch = false;
            }

            System.out.printf("%-12s %-20.8f %-20.8f %-20.8f %-15.8f %s%n",
                township, originalValue, calculated, expected, diff, status);
        }

        System.out.println("\n验证结果: " + (allMatch ? "✓ 所有计算正确！" : "✗ 存在差异"));

        // 详细检查西龙镇（最大值）
        System.out.println("\n西龙镇详细计算（最大值）:");
        double xiLongValue = originalMedicalSupportValues.get("西龙镇");
        double xiLongNormalized = normalizeIndicatorValue(xiLongValue, allValues);
        System.out.printf("原始值: %.8f%n", xiLongValue);
        System.out.printf("平方根: %.8f%n", sqrtSumOfSquares);
        System.out.printf("归一化值: %.8f / %.8f = %.8f%n", 
            xiLongValue, sqrtSumOfSquares, xiLongNormalized);
        System.out.printf("数据库值: %.8f%n", expectedNormalizedValues.get("西龙镇"));
        System.out.printf("差异: %.10f%n", Math.abs(xiLongNormalized - expectedNormalizedValues.get("西龙镇")));

        // 数据检查
        System.out.println("\n数据统计:");
        System.out.printf("最大值: %.8f (西龙镇)%n", Collections.max(allValues));
        System.out.printf("最小值: %.8f (高台镇)%n", Collections.min(allValues));
        System.out.printf("平均值: %.8f%n", allValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        System.out.printf("乡镇数量: %d%n", allValues.size());
    }
}
