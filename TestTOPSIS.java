import java.util.*;

public class TestTOPSIS {

    // Test data: weighted values for 7 towns
    static Map<String, Map<String, Double>> testData = new HashMap<>();

    static {
        // Qingzhu Street
        Map<String, Double> qingzhu = new HashMap<>();
        qingzhu.put("teamManagement", 0.00036933);
        qingzhu.put("riskAssessment", 0.03866577);
        qingzhu.put("financialInput", 0.00095138);
        qingzhu.put("materialReserve", 0.00512831);
        qingzhu.put("medicalSupport", 0.12587605);
        qingzhu.put("selfRescue", 0.01395918);
        qingzhu.put("publicAvoidance", 0.00123692);
        qingzhu.put("relocationCapacity", 0.00101204);
        testData.put("Qingzhu", qingzhu);

        // Hanyang Town
        Map<String, Double> hanyang = new HashMap<>();
        hanyang.put("teamManagement", 0.00596862);
        hanyang.put("riskAssessment", 0.03866577);
        hanyang.put("financialInput", 0.05381293);
        hanyang.put("materialReserve", 0.02762595);
        hanyang.put("medicalSupport", 0.04431071);
        hanyang.put("selfRescue", 0.05648262);
        hanyang.put("publicAvoidance", 0.06425255);
        hanyang.put("relocationCapacity", 0.03925304);
        testData.put("Hanyang", hanyang);

        // Ruifeng Town
        Map<String, Double> ruifeng = new HashMap<>();
        ruifeng.put("teamManagement", 0.11949577);
        ruifeng.put("riskAssessment", 0.03866577);
        ruifeng.put("financialInput", 0.03729360);
        ruifeng.put("materialReserve", 0.14181789);
        ruifeng.put("medicalSupport", 0.05583334);
        ruifeng.put("selfRescue", 0.05169684);
        ruifeng.put("publicAvoidance", 0.09114598);
        ruifeng.put("relocationCapacity", 0.01964680);
        testData.put("Ruifeng", ruifeng);

        // Xilong Town
        Map<String, Double> xilong = new HashMap<>();
        xilong.put("teamManagement", 0.00269100);
        xilong.put("riskAssessment", 0.03866577);
        xilong.put("financialInput", 0.00693199);
        xilong.put("materialReserve", 0.02906252);
        xilong.put("medicalSupport", 0.01997782);
        xilong.put("selfRescue", 0.01036923);
        xilong.put("publicAvoidance", 0.01030000);
        xilong.put("relocationCapacity", 0.00737397);
        testData.put("Xilong", xilong);

        // Gaotai Town
        Map<String, Double> gaotai = new HashMap<>();
        gaotai.put("teamManagement", 0.00548545);
        gaotai.put("riskAssessment", 0.03866577);
        gaotai.put("financialInput", 0.03285337);
        gaotai.put("materialReserve", 0.00846319);
        gaotai.put("medicalSupport", 0.02591507);
        gaotai.put("selfRescue", 0.08159547);
        gaotai.put("publicAvoidance", 0.02204578);
        gaotai.put("relocationCapacity", 0.02254715);
        testData.put("Gaotai", gaotai);

        // Baiguo Township
        Map<String, Double> baiguo = new HashMap<>();
        baiguo.put("teamManagement", 0.00279607);
        baiguo.put("riskAssessment", 0.03866577);
        baiguo.put("financialInput", 0.00720265);
        baiguo.put("materialReserve", 0.03451115);
        baiguo.put("medicalSupport", 0.03208030);
        baiguo.put("selfRescue", 0.00530783);
        baiguo.put("publicAvoidance", 0.01070216);
        baiguo.put("relocationCapacity", 0.01532377);
        testData.put("Baiguo", baiguo);

        // Luobo Township
        Map<String, Double> luobo = new HashMap<>();
        luobo.put("teamManagement", 0.02341494);
        luobo.put("riskAssessment", 0.03866577);
        luobo.put("financialInput", 0.07539587);
        luobo.put("materialReserve", 0.06020930);
        luobo.put("medicalSupport", 0.03950708);
        luobo.put("selfRescue", 0.02211394);
        luobo.put("publicAvoidance", 0.01400351);
        luobo.put("relocationCapacity", 0.10693742);
        testData.put("Luobo", luobo);
    }

    public static void main(String[] args) {
        String[] indicators = {"teamManagement", "riskAssessment", "financialInput",
                              "materialReserve", "medicalSupport", "selfRescue",
                              "publicAvoidance", "relocationCapacity"};

        // Calculate max and min values for each indicator
        Map<String, Double> maxValues = new HashMap<>();
        Map<String, Double> minValues = new HashMap<>();

        for (String indicator : indicators) {
            double max = testData.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .max().orElse(0.0);
            double min = testData.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .min().orElse(0.0);

            maxValues.put(indicator, max);
            minValues.put(indicator, min);
        }

        System.out.println("=== Max and Min Values for Each Indicator ===");
        for (String indicator : indicators) {
            System.out.printf("%-20s Max: %.8f  Min: %.8f%n",
                indicator, maxValues.get(indicator), minValues.get(indicator));
        }

        System.out.println("\n=== TOPSIS Distance Calculation Results ===");
        System.out.printf("%-10s %-20s %-20s %-20s%n", "Town", "Positive Distance", "Negative Distance", "Capability Score");
        System.out.println("-".repeat(75));

        for (Map.Entry<String, Map<String, Double>> entry : testData.entrySet()) {
            String regionName = entry.getKey();
            Map<String, Double> currentValues = entry.getValue();

            // Calculate positive ideal distance (Euclidean distance to max values)
            double positiveDistance = 0.0;
            for (String indicator : indicators) {
                double currentValue = currentValues.getOrDefault(indicator, 0.0);
                double maxValue = maxValues.get(indicator);
                positiveDistance += Math.pow(maxValue - currentValue, 2);
            }
            positiveDistance = Math.sqrt(positiveDistance);

            // Calculate negative ideal distance (Euclidean distance to min values)
            double negativeDistance = 0.0;
            for (String indicator : indicators) {
                double currentValue = currentValues.getOrDefault(indicator, 0.0);
                double minValue = minValues.get(indicator);
                negativeDistance += Math.pow(currentValue - minValue, 2);
            }
            negativeDistance = Math.sqrt(negativeDistance);

            // Calculate comprehensive capability score = negative distance / (negative + positive distance)
            double comprehensiveCapability = 0.0;
            if (negativeDistance + positiveDistance > 0) {
                comprehensiveCapability = negativeDistance / (negativeDistance + positiveDistance);
            }

            System.out.printf("%-10s %-20.8f %-20.8f %-20.8f%n",
                regionName, positiveDistance, negativeDistance, comprehensiveCapability);
        }
    }
}