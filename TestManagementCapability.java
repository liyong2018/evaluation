import java.util.HashMap;
import java.util.Map;

/**
 * 测试MANAGEMENT_CAPABILITY算法表达式计算
 */
public class TestManagementCapability {

    public static void main(String[] args) {
        System.out.println("=== 测试MANAGEMENT_CAPABILITY算法表达式 ===");

        // 模拟青竹街道的数据
        double management_staff = 2.0;
        double population = 102379.0;

        // 执行算法表达式: (management_staff / population) * 10000
        double result = (management_staff / population) * 10000;

        System.out.println("输入数据:");
        System.out.println("management_staff: " + management_staff);
        System.out.println("population: " + population);
        System.out.println();

        System.out.println("计算表达式: (management_staff / population) * 10000");
        System.out.println("计算过程: (" + management_staff + " / " + population + ") * 10000");
        System.out.println("计算结果: " + result);
        System.out.println("期望结果: 0.19535256");
        boolean isMatch = Math.abs(result - 0.19535256) < 0.000001;
        System.out.println("结果匹配: " + isMatch);

        // 验证其他地区的数据
        System.out.println("\n=== 验证其他地区数据 ===");

        // 汉阳镇
        double management_staff_hy = 2.0;
        double population_hy = 6335.0;
        double result_hy = (management_staff_hy / population_hy) * 10000;
        System.out.println("汉阳镇: (" + management_staff_hy + " / " + population_hy + ") * 10000 = " + result_hy);

        // 瑞峰镇
        double management_staff_rf = 52.0;
        double population_rf = 8227.0;
        double result_rf = (management_staff_rf / population_rf) * 10000;
        System.out.println("瑞峰镇: (" + management_staff_rf + " / " + population_rf + ") * 10000 = " + result_rf);

        System.out.println("\n=== 问题分析 ===");
        System.out.println("算法表达式本身是正确的，问题可能在于:");
        System.out.println("1. QLExpress表达式执行上下文中的变量值不正确");
        System.out.println("2. 计算结果没有正确保存到managementCapability变量中");
        System.out.println("3. 后续的权重计算步骤覆盖了原始计算结果");
    }
}