private Double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("鏃犳硶灏嗗瓧绗︿覆杞崲涓烘暟瀛? {}", value);
                return 0.0;
            }
        }
        log.warn("鏃犳硶杞崲涓篋ouble鐨勭被鍨? {}", value.getClass());
        return 0.0;
    }\n    // 派生社区层9个二级指标（英文字段名），来自步骤1输出或数据库原始列
    private Map<String, Double> deriveCommunityIndicators(Map<String, Object> c) {
        Map<String, Double> r = new HashMap<>();
        double pop = nz(number(c, "RESIDENT_POPULATION"), number(c, "resident_population"));
        if (pop <= 0) pop = 1.0;

        double hasPlan = nz(number(c, "HAS_EMERGENCY_PLAN"), number(c, "has_emergency_plan"));
        double hasVul = nz(number(c, "HAS_VULNERABLE_GROUPS_LIST"), number(c, "has_vulnerable_groups_list"));
        double hasHaz = nz(number(c, "HAS_DISASTER_POINTS_LIST"), number(c, "has_disaster_points_list"));
        double hasMap = nz(number(c, "HAS_DISASTER_MAP"), number(c, "has_disaster_map"));

        double fund = nz(number(c, "LAST_YEAR_FUNDING_AMOUNT"), number(c, "last_year_funding_amount"));
        double material = nz(number(c, "MATERIALS_EQUIPMENT_VALUE"), number(c, "materials_equipment_value"));
  
