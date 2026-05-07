package com.evaluate.util;

import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalizes historical Chengdu administrative source codes into functional district codes.
 */
public final class ChengduFunctionalDistrictCodeMapper {

    public static final class Mapping {
        private final String sourceTownshipCode;
        private final String targetTownshipCode;
        private final String countyCode;
        private final String countyName;
        private final String townshipName;

        private Mapping(String sourceTownshipCode, String targetTownshipCode,
                        String countyCode, String countyName, String townshipName) {
            this.sourceTownshipCode = sourceTownshipCode;
            this.targetTownshipCode = targetTownshipCode;
            this.countyCode = countyCode;
            this.countyName = countyName;
            this.townshipName = townshipName;
        }

        public String getSourceTownshipCode() {
            return sourceTownshipCode;
        }

        public String getTargetTownshipCode() {
            return targetTownshipCode;
        }

        public String getCountyCode() {
            return countyCode;
        }

        public String getCountyName() {
            return countyName;
        }

        public String getTownshipName() {
            return townshipName;
        }
    }

    private static final Map<String, Mapping> BY_SOURCE_CODE = new LinkedHashMap<>();
    private static final Map<String, Mapping> BY_TARGET_CODE = new LinkedHashMap<>();

    static {
        add("510116003", "510171701", "510171", "四川天府新区成都直管区", "华阳街道");
        add("510116018", "510171702", "510171", "四川天府新区成都直管区", "万安街道");
        add("510116020", "510171703", "510171", "四川天府新区成都直管区", "兴隆街道");
        add("510116019", "510171704", "510171", "四川天府新区成都直管区", "正兴街道");
        add("510116022", "510171705", "510171", "四川天府新区成都直管区", "新兴街道");
        add("510116021", "510171706", "510171", "四川天府新区成都直管区", "煎茶街道");
        add("510116025", "510171707", "510171", "四川天府新区成都直管区", "永兴街道");
        add("510116023", "510171708", "510171", "四川天府新区成都直管区", "籍田街道");
        add("510116024", "510171709", "510171", "四川天府新区成都直管区", "太平街道");

        add("510107062", "510172701", "510172", "成都高新区", "肖家河街道");
        add("510107061", "510172702", "510172", "成都高新区", "芳草街街道");
        add("510107063", "510172703", "510172", "成都高新区", "石羊街道");
        add("510107064", "510172704", "510172", "成都高新区", "桂溪街道");
        add("510116004", "510172705", "510172", "成都高新区", "中和街道");
        add("510117020", "510172706", "510172", "成都高新区", "西园街道");
        add("510117019", "510172707", "510172", "成都高新区", "合作街道");

        add("510185013", "510173701", "510173", "成都东部新区", "三岔街道");
        add("510185009", "510173702", "510173", "成都东部新区", "石盘街道");
        add("510185010", "510173703", "510173", "成都东部新区", "养马街道");
        add("510185017", "510173704", "510173", "成都东部新区", "丹景街道");
        add("510185015", "510173705", "510173", "成都东部新区", "福田街道");
        add("510185016", "510173706", "510173", "成都东部新区", "玉成街道");
        add("510185014", "510173707", "510173", "成都东部新区", "草池街道");
        add("510185012", "510173708", "510173", "成都东部新区", "石板凳街道");
        add("510185126", "510173709", "510173", "成都东部新区", "高明镇");
        add("510185127", "510173710", "510173", "成都东部新区", "武庙镇");
        add("510185128", "510173711", "510173", "成都东部新区", "壮溪镇");
        add("510185132", "510173712", "510173", "成都东部新区", "海螺镇");
        add("510185131", "510173713", "510173", "成都东部新区", "董家埂镇");
        add("510185121", "510173714", "510173", "成都东部新区", "芦葭镇");
        add("510173715", "510173714", "510173", "成都东部新区", "芦葭镇");

        add("510302008", "510371701", "510371", "自贡高新技术产业开发区", "红旗街道");
        add("510302006", "510371702", "510371", "自贡高新技术产业开发区", "学苑街道");
        add("510302009", "510371703", "510371", "自贡高新技术产业开发区", "高峰街道");
        add("510302005", "510371704", "510371", "自贡高新技术产业开发区", "丹桂街道");
        add("510311001", "510371705", "510371", "自贡高新技术产业开发区", "板仓街道办事处");
    }

    private ChengduFunctionalDistrictCodeMapper() {
    }

    public static Map<String, Mapping> mappingsBySourceCode() {
        return Collections.unmodifiableMap(BY_SOURCE_CODE);
    }

    public static Mapping findByAnyCode(String code) {
        String prefix = townshipPrefix(code);
        if (prefix == null) {
            return null;
        }
        Mapping mapping = BY_SOURCE_CODE.get(prefix);
        return mapping != null ? mapping : BY_TARGET_CODE.get(prefix);
    }

    public static String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            return code;
        }
        String normalized = code.trim();
        Mapping mapping = findByAnyCode(normalized);
        if (mapping == null || normalized.startsWith(mapping.getTargetTownshipCode())) {
            return normalized;
        }
        return mapping.getTargetTownshipCode() + normalized.substring(mapping.getSourceTownshipCode().length());
    }

    private static void add(String sourceTownshipCode, String targetTownshipCode,
                            String countyCode, String countyName, String townshipName) {
        Mapping mapping = new Mapping(sourceTownshipCode, targetTownshipCode, countyCode, countyName, townshipName);
        BY_SOURCE_CODE.put(sourceTownshipCode, mapping);
        BY_TARGET_CODE.put(targetTownshipCode, mapping);
    }

    private static String townshipPrefix(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        String normalized = code.trim();
        return normalized.length() >= 9 ? normalized.substring(0, 9) : normalized;
    }
}
