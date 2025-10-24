public class SimpleGradingTest {
    
    public static void main(String[] args) {
        // Test data from Ruifeng Town
        double value = 0.7657391838300;
        double mean = 0.261566249;
        double stdev = 0.275550796;
        
        System.out.println("=== Grading Test ===");
        System.out.println("value = " + value);
        System.out.println("mean = " + mean);
        System.out.println("stdev = " + stdev);
        System.out.println();
        
        // Calculate key thresholds
        double halfStdev = 0.5 * stdev;
        double oneAndHalfStdev = 1.5 * stdev;
        double meanPlusHalf = mean + halfStdev;
        double meanPlusOneAndHalf = mean + oneAndHalfStdev;
        double meanMinusHalf = mean - halfStdev;
        
        System.out.println("=== Key Thresholds ===");
        System.out.println("0.5*stdev = " + halfStdev);
        System.out.println("1.5*stdev = " + oneAndHalfStdev);
        System.out.println("mean + 0.5*stdev = " + meanPlusHalf);
        System.out.println("mean + 1.5*stdev = " + meanPlusOneAndHalf);
        System.out.println("mean - 0.5*stdev = " + meanMinusHalf);
        System.out.println();
        
        // Determine classification scheme
        System.out.println("=== Classification Scheme ===");
        System.out.println("mean = " + mean);
        System.out.println("0.5*stdev = " + halfStdev);
        System.out.println("1.5*stdev = " + oneAndHalfStdev);
        
        if (mean <= halfStdev) {
            System.out.println("mean <= 0.5*stdev -> Use 3-level classification");
        } else if (mean <= oneAndHalfStdev) {
            System.out.println("0.5*stdev < mean <= 1.5*stdev -> Use 4-level classification");
        } else {
            System.out.println("mean > 1.5*stdev -> Use 5-level classification");
        }
        System.out.println();
        
        // Execute grading
        System.out.println("=== Grading Process ===");
        String grade = determineGrade(value, mean, stdev);
        System.out.println("Final grade: " + grade);
    }
    
    private static String determineGrade(double value, double mean, double stdev) {
        double halfStdev = 0.5 * stdev;
        double oneAndHalfStdev = 1.5 * stdev;
        double meanPlusHalf = mean + halfStdev;
        double meanPlusOneAndHalf = mean + oneAndHalfStdev;
        double meanMinusHalf = mean - halfStdev;
        double meanMinusOneAndHalf = mean - oneAndHalfStdev;
        
        value = Math.max(0, value);
        
        if (mean <= halfStdev) {
            System.out.println("Entering 3-level classification");
            if (value >= meanPlusOneAndHalf) {
                System.out.println(value + " >= " + meanPlusOneAndHalf + " -> Strong");
                return "Strong";
            } else if (value >= meanPlusHalf) {
                System.out.println(value + " >= " + meanPlusHalf + " -> Good");
                return "Good";
            } else {
                System.out.println(value + " < " + meanPlusHalf + " -> Medium");
                return "Medium";
            }
        } else if (mean <= oneAndHalfStdev) {
            System.out.println("Entering 4-level classification");
            if (value >= meanPlusOneAndHalf) {
                System.out.println(value + " >= " + meanPlusOneAndHalf + " -> Strong");
                return "Strong";
            } else if (value >= meanPlusHalf) {
                System.out.println(value + " >= " + meanPlusHalf + " -> Good");
                return "Good";
            } else if (value >= meanMinusHalf) {
                System.out.println(value + " >= " + meanMinusHalf + " -> Medium");
                return "Medium";
            } else {
                System.out.println(value + " < " + meanMinusHalf + " -> Weak");
                return "Weak";
            }
        } else {
            System.out.println("Entering 5-level classification");
            if (value >= meanPlusOneAndHalf) {
                System.out.println(value + " >= " + meanPlusOneAndHalf + " -> Strong");
                return "Strong";
            } else if (value >= meanPlusHalf) {
                System.out.println(value + " >= " + meanPlusHalf + " -> Good");
                return "Good";
            } else if (value >= meanMinusHalf) {
                System.out.println(value + " >= " + meanMinusHalf + " -> Medium");
                return "Medium";
            } else if (value >= meanMinusOneAndHalf) {
                System.out.println(value + " >= " + meanMinusOneAndHalf + " -> Weak");
                return "Weak";
            } else {
                System.out.println(value + " < " + meanMinusOneAndHalf + " -> Very Weak");
                return "Very Weak";
            }
        }
    }
}