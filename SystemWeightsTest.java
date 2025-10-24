import java.util.*;

public class SystemWeightsTest {

    // System default primary weights
    static Map<String, Double> primaryWeights = new HashMap<>();
    static {
        primaryWeights.put("disasterManagement", 0.33);
        primaryWeights.put("disasterPreparedness", 0.32);
        primaryWeights.put("selfRescueTransfer", 0.35);
    }

    // System default secondary weights
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

    // Test normalized data (simulating original data after normalization)
    static Map<String, Map<String, Double>> normalizedData = new HashMap<>();

    static {
        // These values are hypothetical, representing normalized values from original data
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

    // Calculate weighted values: normalized × primary × secondary
    static Map<String, Map<String, Double>> calculateWeightedValues() {
        Map<String, Map<String, Double>> weightedData = new HashMap<>();

        for (String town : normalizedData.keySet()) {
            Map<String, Double> weighted = new HashMap<>();
            Map<String, Double> normalized = normalizedData.get(town);

            // Team management capability
            weighted.put("teamManagement", normalized.get("teamManagement") * primaryWeights.get("disasterManagement") * secondaryWeights.get("teamManagement"));

            // Risk assessment capability
            weighted.put("riskAssessment", normalized.get("riskAssessment") * primaryWeights.get("disasterManagement") * secondaryWeights.get("riskAssessment"));

            // Financial input capability
            weighted.put("financialInput", normalized.get("financialInput") * primaryWeights.get("disasterManagement") * secondaryWeights.get("financialInput"));

            // Material reserve capability
            weighted.put("materialReserve", normalized.get("materialReserve") * primaryWeights.get("disasterPreparedness") * secondaryWeights.get("materialReserve"));

            // Medical support capability
            weighted.put("medicalSupport", normalized.get("medicalSupport") * primaryWeights.get("disasterPreparedness") * secondaryWeights.get("medicalSupport"));

            // Self rescue capability
            weighted.put("selfRescue", normalized.get("selfRescue") * primaryWeights.get("selfRescueTransfer") * secondaryWeights.get("selfRescue"));

            // Public avoidance capability
            weighted.put("publicAvoidance", normalized.get("publicAvoidance") * primaryWeights.get("selfRescueTransfer") * secondaryWeights.get("publicAvoidance"));

            // Relocation capability
            weighted.put("relocationCapacity", normalized.get("relocationCapacity") * primaryWeights.get("selfRescueTransfer") * secondaryWeights.get("relocationCapacity"));

            weightedData.put(town, weighted);
        }

        return weightedData;
    }

    public static void main(String[] args) {
        System.out.println("=== System Default Weights ===");
        System.out.println("Primary weights: " + primaryWeights);
        System.out.println("Secondary weights: " + secondaryWeights);

        Map<String, Map<String, Double>> weightedData = calculateWeightedValues();

        System.out.println("\n=== System Calculated Weighted Values ===");
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