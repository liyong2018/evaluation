import java.util.*;

public class MedicalSupportTest {

    // Simulated medical support original data (inferred from your logs)
    static List<Double> originalMedicalSupportValues = Arrays.asList(
        98.65304408130,
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

    // Expected normalized values from your logs
    static List<Double> expectedNormalizedValues = Arrays.asList(
        0.06250719020908259,
        0.022003691542529666,
        0.027725570423269917,
        0.9970134001247958,
        0.012868835332992337,
        0.015930351392393648,
        0.019618318700953107
    );

    // System normalization method
    public static double normalizeIndicatorValue(double currentValue, List<Double> allValues) {
        if (allValues == null || allValues.isEmpty()) {
            return 0.0;
        }

        // Calculate sum of squares
        double sumOfSquares = allValues.stream()
            .mapToDouble(value -> value * value)
            .sum();

        // Calculate square root
        double sqrtSumOfSquares = Math.sqrt(sumOfSquares);

        // Avoid division by zero
        if (sqrtSumOfSquares == 0.0) {
            return 0.0;
        }

        // Return normalized value: current value / SQRT(SUMSQ(all values))
        return currentValue / sqrtSumOfSquares;
    }

    public static void main(String[] args) {
        System.out.println("=== Medical Support Normalization Verification ===");

        // Calculate normalized values for first 7 townships (assuming 7 townships)
        List<Double> first7Values = originalMedicalSupportValues.subList(0, 7);

        System.out.println("Original medical support values for first 7 townships:");
        for (int i = 0; i < first7Values.size(); i++) {
            System.out.printf("Township %d: %.8f%n", i+1, first7Values.get(i));
        }

        // Calculate sum of squares
        double sumOfSquares = first7Values.stream()
            .mapToDouble(value -> value * value)
            .sum();
        double sqrtSumOfSquares = Math.sqrt(sumOfSquares);

        System.out.println("\nNormalization calculation:");
        System.out.printf("Sum of squares: %.8f%n", sumOfSquares);
        System.out.printf("Square root: %.8f%n", sqrtSumOfSquares);

        System.out.println("\nCalculation results comparison:");
        System.out.printf("%-10s %-20s %-20s %-15s%n", "Township", "Calculated", "Expected", "Difference");
        System.out.println("-".repeat(70));

        for (int i = 0; i < first7Values.size() && i < expectedNormalizedValues.size(); i++) {
            double currentValue = first7Values.get(i);
            double calculated = normalizeIndicatorValue(currentValue, first7Values);
            double expected = expectedNormalizedValues.get(i);
            double diff = Math.abs(calculated - expected);

            System.out.printf("Township %d %-20.8f %-20.8f %-15.8f%n",
                i+1, calculated, expected, diff);
        }

        // Detailed calculation for first value
        double firstValue = first7Values.get(0);
        double firstNormalized = normalizeIndicatorValue(firstValue, first7Values);
        System.out.println("\nDetailed calculation for first township:");
        System.out.printf("Original value: %.8f%n", firstValue);
        System.out.printf("Square root: %.8f%n", sqrtSumOfSquares);
        System.out.printf("Normalized value: %.8f / %.8f = %.8f%n", firstValue, sqrtSumOfSquares, firstNormalized);

        // Check for abnormal values
        System.out.println("\nData check:");
        System.out.printf("Max value: %.8f%n", Collections.max(first7Values));
        System.out.printf("Min value: %.8f%n", Collections.min(first7Values));
        System.out.printf("Average: %.8f%n", first7Values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }
}