public class GradingTest {
    
    public static void main(String[] args) {
        // 瑞峰镇的数据
        double value = 0.7657391838300;
        double mean = 0.261566249;
        double stdev = 0.275550796;
        
        System.out.println("=== 瑞峰镇灾害管理能力分级测试 ===");
        System.out.println("value = " + value);
        System.out.println("mean (μ) = " + mean);
        System.out.println("stdev (σ) = " + stdev);
        System.out.println();
        
        // 计算关键节点
        double halfStdev = 0.5 * stdev;
        double oneAndHalfStdev = 1.5 * stdev;
        double meanPlusHalf = mean + halfStdev;
        double meanPlusOneAndHalf = mean + oneAndHalfStdev;
        double meanMinusHalf = mean - halfStdev;
        
        System.out.println("=== 计算关键节点 ===");
        System.out.println("0.5σ = " + halfStdev);
        System.out.println("1.5σ = " + oneAndHalfStdev);
        System.out.println("μ + 0.5σ = " + meanPlusHalf);
        System.out.println("μ + 1.5σ = " + meanPlusOneAndHalf);
        System.out.println("μ - 0.5σ = " + meanMinusHalf);
        System.out.println();
        
        // 判断分级方案
        System.out.println("=== 分级方案判断 ===");
        System.out.println("μ = " + mean);
        System.out.println("0.5σ = " + halfStdev);
        System.out.println("1.5σ = " + oneAndHalfStdev);
        
        if (mean <= halfStdev) {
            System.out.println("μ ≤ 0.5σ → 使用3级分类");
        } else if (mean <= oneAndHalfStdev) {
            System.out.println("0.5σ < μ ≤ 1.5σ → 使用4级分类");
        } else {
            System.out.println("μ > 1.5σ → 使用5级分类");
        }
        System.out.println();
        
        // 执行分级
        System.out.println("=== 执行分级 ===");
        System.out.println("value = " + value);
        
        String grade = determineGrade(value, mean, stdev);
        System.out.println("最终等级: " + grade);
    }
    
    private static String determineGrade(double value, double mean, double stdev) {
        // 计算关键节点
        double halfStdev = 0.5 * stdev;
        double oneAndHalfStdev = 1.5 * stdev;
        double meanPlusHalf = mean + halfStdev;
        double meanPlusOneAndHalf = mean + oneAndHalfStdev;
        double meanMinusHalf = mean - halfStdev;
        double meanMinusOneAndHalf = mean - oneAndHalfStdev;
        
        // 确保值不小于0
        value = Math.max(0, value);
        
        if (mean <= halfStdev) {
            // 情况1：μ ≤ 0.5σ，分为3级
            System.out.println("进入3级分类");
            if (value >= meanPlusOneAndHalf) {
                System.out.println(value + " >= " + meanPlusOneAndHalf + " → 强");
                return "强";
            } else if (value >= meanPlusHalf) {
                System.out.println(value + " >= " + meanPlusHalf + " → 较强");
                return "较强";
            } else {
                System.out.println(value + " < " + meanPlusHalf + " → 中等");
                return "中等";
            }
        } else if (mean <= oneAndHalfStdev) {
            // 情况2：0.5σ < μ ≤ 1.5σ，分为4级
            System.out.println("进入4级分类");
            if (value >= meanPlusOneAndHalf) {
                System.out.println(value + " >= " + meanPlusOneAndHalf + " → 强");
                return "强";
            } else if (value >= meanPlusHalf) {
                System.out.println(value + " >= " + meanPlusHalf + " → 较强");
                return "较强";
            } else if (value >= meanMinusHalf) {
                System.out.println(value + " >= " + meanMinusHalf + " → 中等");
                return "中等";
            } else {
                System.out.println(value + " < " + meanMinusHalf + " → 较弱");
                return "较弱";
            }
        } else {
            // 情况3：μ > 1.5σ，使用5级分类
            System.out.println("进入5级分类");
            if (value >= meanPlusOneAndHalf) {
                System.out.println(value + " >= " + meanPlusOneAndHalf + " → 强");
                return "强";
            } else if (value >= meanPlusHalf) {
                System.out.println(value + " >= " + meanPlusHalf + " → 较强");
                return "较强";
            } else if (value >= meanMinusHalf) {
                System.out.println(value + " >= " + meanMinusHalf + " → 中等");
                return "中等";
            } else if (value >= meanMinusOneAndHalf) {
                System.out.println(value + " >= " + meanMinusOneAndHalf + " → 较弱");
                return "较弱";
            } else {
                System.out.println(value + " < " + meanMinusOneAndHalf + " → 弱");
                return "弱";
            }
        }
    }
}