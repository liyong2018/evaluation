import java.util.*;

public class VerifyTOPSIS {

    // Your actual weighted data
    static Map<String, Map<String, Double>> actualData = new HashMap<>();

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
        actualData.put("Qingzhu", qingzhu);

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
        actualData.put("Hanyang", hanyang);

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
        actualData.put("Ruifeng", ruifeng);

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
        actualData.put("Xilong", xilong);

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
        actualData.put("Gaotai", gaotai);

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
        actualData.put("Baiguo", baiguo);

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
        actualData.put("Luobo", luobo);
    }

    // Calculate TOPSIS distances using your exact data
    public static void calculateTOPSISDistances() {
        String[] indicators = {"teamManagement", "riskAssessment", "financialInput",
                              "materialReserve", "medicalSupport", "selfRescue",
                              "publicAvoidance", "relocationCapacity"};

        // Calculate max and min values for each indicator
        Map<String, Double> maxValues = new HashMap<>();
        Map<String, Double> minValues = new HashMap<>();

        for (String indicator : indicators) {
            double max = actualData.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .max().orElse(0.0);
            double min = actualData.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .min().orElse(0.0);

            maxValues.put(indicator, max);
            minValues.put(indicator, min);
        }

        System.out.println("=== Max and Min Values from Your Data ===");
        for (String indicator : indicators) {
            System.out.printf("%-20s Max: %.8f  Min: %.8f%n",
                indicator, maxValues.get(indicator), minValues.get(indicator));
        }

        System.out.println("\n=== Expected vs System Results ===");
        System.out.printf("%-10s %-15s %-15s %-15s %-15s%n",
            "Town", "Expected Pos", "Expected Neg", "System Pos", "System Neg");
        System.out.println("-".repeat(80));

        // Expected results from your calculation
        Map<String, Double[]> expectedResults = new HashMap<>();
        expectedResults.put("Qingzhu", new Double[]{0.24959091, 0.10625103});
        expectedResults.put("Hanyang", new Double[]{0.19743867, 0.109435515});
        expectedResults.put("Ruifeng", new Double[]{0.12194812, 0.214626094});
        expectedResults.put("Xilong", new Double[]{0.25254899, 0.02760862});
        expectedResults.put("Gaotai", new Double[]{0.23341046, 0.088356425});
        expectedResults.put("Baiguo", new Double[]{0.24344524, 0.036731531});
        expectedResults.put("Luobo", new Double[]{0.18121838, 0.145444165});

        // System results (from your data)
        Map<String, Double[]> systemResults = new HashMap<>();
        systemResults.put("Qingzhu", new Double[]{0.289425003935, 0.011637250691});
        systemResults.put("Hanyang", new Double[]{0.236012464280, 0.106705642492});
        systemResults.put("Ruifeng", new Double[]{0.181836740017, 0.211622702921});
        systemResults.put("Xilong", new Double[]{0.229273979968, 0.156764172237});
        systemResults.put("Gaotai", new Double[]{0.261344631217, 0.088156717484});
        systemResults.put("Baiguo", new Double[]{0.272273372247, 0.034683795088});
        systemResults.put("Luobo", new Double[]{0.221060548420, 0.144130958086});

        for (String town : actualData.keySet()) {
            Map<String, Double> currentValues = actualData.get(town);

            // Calculate positive ideal distance (to max values)
            double positiveDistance = 0.0;
            for (String indicator : indicators) {
                double currentValue = currentValues.getOrDefault(indicator, 0.0);
                double maxValue = maxValues.get(indicator);
                positiveDistance += Math.pow(maxValue - currentValue, 2);
            }
            positiveDistance = Math.sqrt(positiveDistance);

            // Calculate negative ideal distance (to min values)
            double negativeDistance = 0.0;
            for (String indicator : indicators) {
                double currentValue = currentValues.getOrDefault(indicator, 0.0);
                double minValue = minValues.get(indicator);
                negativeDistance += Math.pow(minValue - currentValue, 2);
            }
            negativeDistance = Math.sqrt(negativeDistance);

            Double[] expected = expectedResults.get(town);
            Double[] system = systemResults.get(town);

            System.out.printf("%-10s %-15.8f %-15.8f %-15.8f %-15.8f%n",
                town, expected[0], expected[1], system[0], system[1]);

            System.out.printf("          Calculated:       %-15.8f %-15.8f%n",
                positiveDistance, negativeDistance);
            System.out.println();
        }
    }

    public static void main(String[] args) {
        calculateTOPSISDistances();
    }
}