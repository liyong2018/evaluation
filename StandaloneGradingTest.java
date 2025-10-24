public class StandaloneGradingTest {
    
    public static void main(String[] args) {
        // 瑞峰镇的测试数据
        double value = 0.7657391838300;
        double mean = 0.261566249;
        double stdev = 0.275550796;
        
        System.out.println("=== 瑞峰镇分级测试 ===");
        System.out.println("value = " + value);
        System.out.println("mean = " + mean);
        System.out.println("stdev = " + stdev);
        System.out.println();
        
        String result = determineGrade(value, mean, stdev);
        System.out.println("最终结果: " + result);
        
        // 验证计算
        System.out.println("\n=== 验证计算 ===");
        double meanPlusOneAndHalf = mean + 1.5 * stdev;
        System.out.println("mean + 1.5*stdev = " + meanPlusOneAndHalf);
        System.out.println("0.7657391838300 >= 0.674892443 ? " + (value >= meanPlusOneAndHalf));
        
        if ("强".equals(result)) {
            System.out.println("✓ 测试通过！");
        } else {
            System.out.println("✗ 测试失败！实际: " + result + ", 预期: 强");
        }
    }
    
    private static String determineGrade(double value, double mean, double stdev) {
        System.out.println("[分级规则调试] ========== 开始分级 ==========");
        
        // 计算关键节点
        double halfStdev = 0.5 * stdev;
        double oneAndHalfStdev = 1.5 * stdev;
        double meanPlusHalf = mean + halfStdev;
        double meanPlusOneAndHalf = mean + oneAndHalfStdev;
        double meanMinusHalf = mean - halfStdev;
        double meanMinusOneAndHalf = mean - oneAndHalfStdev;
        
        System.out.println("[分级规则调试] 输入参数: value=" + value + ", mean=" + mean + ", stdev=" + stdev);
        System.out.println("[分级规则调试] 计算结果: 0.5σ=" + halfStdev + ", 1.5σ=" + oneAndHalfStdev);
        System.out.println("[分级规则调试] 阈值: μ+0.5σ=" + meanPlusHalf + ", μ+1.5σ=" + meanPlusOneAndHalf + 
                          ", μ-0.5σ=" + meanMinusHalf + ", μ-1.5σ=" + meanMinusOneAndHalf);
        
        // 确保值不小于0
        double originalValue = value;
        value = Math.max(0, value);
        if (originalValue != value) {
            System.out.println("[分级规则调试] 值被调整: " + originalValue + " -> " + value);
        }
        
        if (mean <= halfStdev) {
            // 情况1：μ ≤ 0.5σ，分为3级
            System.out.println("[分级规则调试] 进入3级分类: μ (" + mean + ") ≤ 0.5σ (" + halfStdev + ")");
            if (value >= meanPlusOneAndHalf) {
                System.out.println("[分级规则调试] " + value + " >= " + meanPlusOneAndHalf + " → 强");
                return "强";
            } else if (value >= meanPlusHalf) {
                System.out.println("[分级规则调试] " + value + " >= " + meanPlusHalf + " → 较强");
                return "较强";
            } else {
                System.out.println("[分级规则调试] " + value + " < " + meanPlusHalf + " → 中等");
                return "中等";
            }
        } else if (mean <= oneAndHalfStdev) {
            // 情况2：0.5σ < μ ≤ 1.5σ，分为4级
            System.out.println("[分级规则调试] 进入4级分类: 0.5σ (" + halfStdev + ") < μ (" + mean + ") ≤ 1.5σ (" + oneAndHalfStdev + ")");
            
            // 详细比较过程
            System.out.println("[分级规则调试] 比较1: " + value + " >= " + meanPlusOneAndHalf + " ? " + (value >= meanPlusOneAndHalf));
            if (value >= meanPlusOneAndHalf) {
                System.out.println("[分级规则调试] 第1个条件满足 → 强");
                return "强";
            }
            
            System.out.println("[分级规则调试] 比较2: " + value + " >= " + meanPlusHalf + " ? " + (value >= meanPlusHalf));
            if (value >= meanPlusHalf) {
                System.out.println("[分级规则调试] 第2个条件满足 → 较强");
                return "较强";
            }
            
            System.out.println("[分级规则调试] 比较3: " + value + " >= " + meanMinusHalf + " ? " + (value >= meanMinusHalf));
            if (value >= meanMinusHalf) {
                System.out.println("[分级规则调试] 第3个条件满足 → 中等");
                return "中等";
            }
            
            System.out.println("[分级规则调试] 所有条件都不满足 → 较弱");
            return "较弱";
        } else {
            // 情况3：μ > 1.5σ，使用5级分类
            System.out.println("[分级规则调试] 进入5级分类(默认): μ (" + mean + ") > 1.5σ (" + oneAndHalfStdev + ")");
            if (value >= meanPlusOneAndHalf) {
                System.out.println("[分级规则调试] " + value + " >= " + meanPlusOneAndHalf + " → 强");
                return "强";
            } else if (value >= meanPlusHalf) {
                System.out.println("[分级规则调试] " + value + " >= " + meanPlusHalf + " → 较强");
                return "较强";
            } else if (value >= meanMinusHalf) {
                System.out.println("[分级规则调试] " + value + " >= " + meanMinusHalf + " → 中等");
                return "中等";
            } else if (value >= meanMinusOneAndHalf) {
                System.out.println("[分级规则调试] " + value + " >= " + meanMinusOneAndHalf + " → 较弱");
                return "较弱";
            } else {
                System.out.println("[分级规则调试] " + value + " < " + meanMinusOneAndHalf + " → 弱");
                return "弱";
            }
        }
    }
}