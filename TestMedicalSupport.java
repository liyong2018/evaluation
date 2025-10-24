import java.util.*;

public class TestMedicalSupport {

    // 模拟医疗保障能力的原始数据（从您提供的日志中推断）
    static List<Double> originalMedicalSupportValues = Arrays.asList(
        98.65304408130,    // 第一个乡镇的原始值
        34.72770323599,
        0.022003691542529666,
        0.027725570423269917,
        43.75835663060,
        1573.553483737,
        0.9970134001247958,
        20.31045988684,
        0.012868835332992337,
        25.14235007025,
        0.015930351392393648,
        30.96294767261,
        0.019618318700953107
    );

    // 您提供的归一化结果
    static List<Double> expectedNormalizedValues = Arrays.asList(
        0.06250719020908259,
        0.022003691542529666,
        0.027725570423269917,
        0.9970134001247958,
        0.012868835332992337,
        0.015930351392393648,
        0.019618318700953107
    );

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
        System.out.println("=== 医疗保障能力归一化计算验证 ===");

        // 计算前7个乡镇的归一化值（假设有7个乡镇）
        List<Double> first7Values = originalMedicalSupportValues.subList(0, 7);

        System.out.println("前7个乡镇的原始医疗保障能力值:");
        for (int i = 0; i < first7Values.size(); i++) {
            System.out.printf("乡镇%d: %.8f%n", i+1, first7Values.get(i));
        }

        // 计算平方和
        double sumOfSquares = first7Values.stream()
            .mapToDouble(value -> value * value)
            .sum();
        double sqrtSumOfSquares = Math.sqrt(sumOfSquares);

        System.out.println("\n归一化计算:");
        System.out.printf("平方和: %.8f%n", sumOfSquares);
        System.out.printf("平方根: %.8f%n", sqrtSumOfSquares);

        System.out.println("\n计算结果对比:");
        System.out.printf("%-10s %-20s %-20s %-15s%n", "乡镇", "计算结果", "期望结果", "差异");
        System.out.println("-".repeat(70));

        for (int i = 0; i < first7Values.size() && i < expectedNormalizedValues.size(); i++) {
            double currentValue = first7Values.get(i);
            double calculated = normalizeIndicatorValue(currentValue, first7Values);
            double expected = expectedNormalizedValues.get(i);
            double diff = Math.abs(calculated - expected);

            System.out.printf("乡镇%d     %-20.8f %-20.8f %-15.8f%n",
                i+1, calculated, expected, diff);
        }

        // 检查第一个值的详细计算
        double firstValue = first7Values.get(0);
        double firstNormalized = normalizeIndicatorValue(firstValue, first7Values);
        System.out.println("\n第一个乡镇详细计算:");
        System.out.printf("原始值: %.8f%n", firstValue);
        System.out.printf("平方根: %.8f%n", sqrtSumOfSquares);
        System.out.printf("归一化值: %.8f / %.8f = %.8f%n", firstValue, sqrtSumOfSquares, firstNormalized);

        // 验证是否有异常值
        System.out.println("\n数据检查:");
        System.out.printf("最大值: %.8f%n", Collections.max(first7Values));
        System.out.printf("最小值: %.8f%n", Collections.min(first7Values));
        System.out.printf("平均值: %.8f%n", first7Values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }
}