#!/usr/bin/env python3
"""Generate SQL to insert city-level comprehensive disaster reduction capacity weights (model_id=20) into weight_config + indicator_weight tables."""

# ─── Data from Table A7-1 (Level 1 weights) ───
# Columns: 政府减灾能力, 企业减灾能力, 社会组织减灾能力, 乡镇减灾能力, 社区减灾能力, 家庭减灾能力
level1_data = {
    "四川省": {"orgcode": "51",   "weights": [0.23, 0.15, 0.17, 0.16, 0.15, 0.14]},
    "阿坝州": {"orgcode": "5132", "weights": [0.20, 0.16, 0.15, 0.17, 0.16, 0.16]},
    "巴中市": {"orgcode": "5119", "weights": [0.19, 0.15, 0.17, 0.18, 0.16, 0.15]},
    "成都市": {"orgcode": "5101", "weights": [0.21, 0.16, 0.14, 0.18, 0.17, 0.14]},
    "达州市": {"orgcode": "5117", "weights": [0.19, 0.16, 0.16, 0.18, 0.16, 0.15]},
    "德阳市": {"orgcode": "5106", "weights": [0.20, 0.17, 0.15, 0.18, 0.16, 0.14]},
    "甘孜州": {"orgcode": "5133", "weights": [0.21, 0.18, 0.16, 0.16, 0.15, 0.14]},
    "广安市": {"orgcode": "5116", "weights": [0.20, 0.17, 0.16, 0.18, 0.15, 0.14]},
    "广元市": {"orgcode": "5108", "weights": [0.19, 0.15, 0.16, 0.17, 0.17, 0.16]},
    "乐山市": {"orgcode": "5111", "weights": [0.20, 0.17, 0.16, 0.17, 0.16, 0.14]},
    "凉山州": {"orgcode": "5134", "weights": [0.19, 0.17, 0.15, 0.17, 0.17, 0.15]},
    "泸州市": {"orgcode": "5105", "weights": [0.19, 0.17, 0.16, 0.17, 0.16, 0.15]},
    "眉山市": {"orgcode": "5114", "weights": [0.21, 0.15, 0.16, 0.18, 0.16, 0.14]},
    "绵阳市": {"orgcode": "5107", "weights": [0.20, 0.17, 0.16, 0.17, 0.16, 0.14]},
    "南充市": {"orgcode": "5113", "weights": [0.20, 0.17, 0.16, 0.17, 0.16, 0.14]},
    "内江市": {"orgcode": "5110", "weights": [0.20, 0.17, 0.15, 0.17, 0.16, 0.15]},
    "攀枝花市": {"orgcode": "5104", "weights": [0.19, 0.18, 0.16, 0.17, 0.16, 0.14]},
    "遂宁市": {"orgcode": "5109", "weights": [0.20, 0.18, 0.16, 0.17, 0.15, 0.14]},
    "雅安市": {"orgcode": "5118", "weights": [0.20, 0.17, 0.14, 0.18, 0.17, 0.14]},
    "宜宾市": {"orgcode": "5115", "weights": [0.18, 0.16, 0.16, 0.17, 0.16, 0.17]},
    "资阳市": {"orgcode": "5120", "weights": [0.19, 0.16, 0.16, 0.17, 0.17, 0.15]},
    "自贡市": {"orgcode": "5103", "weights": [0.20, 0.17, 0.16, 0.18, 0.16, 0.13]},
}

# ─── Data from Table A7-2 Part I & Part II (Level 2 weights) ───
# Each entry: list of weights for all 22 second-level indicators, in order:
#  0-5:  Under 政府减灾能力: 管理能力, 工程设防能力, 监测预警能力, 物资储备能力, 专业队伍救援能力, 转移安置能力
#  6-7:  Under 企业减灾能力: 大型企业应急救援能力, 保险和再保险企业减灾能力
#  8-11: Under 社会组织减灾能力: 物资储备能力, 应急运输能力, 应急救援能力, 科普宣传能力
# 12-14: Under 乡镇（街道）减灾能力: 灾害管理能力, 灾害备灾能力, 自救转移能力
# 15-17: Under 社区（行政区）减灾能力: 灾害管理能力, 灾害备灾能力, 自救转移能力
# 18-21: Under 家庭减灾能力: 家庭脆弱性, 防灾物资储备能力, 灾害信息获取能力, 灾害自救互救能力

level2_data = {
    "四川省": [0.18, 0.17, 0.17, 0.16, 0.17, 0.15,  0.54, 0.46,  0.24, 0.25, 0.27, 0.24,  0.33, 0.31, 0.36,  0.33, 0.30, 0.37,  0.27, 0.21, 0.26, 0.26],
    "阿坝州": [0.18, 0.16, 0.16, 0.17, 0.16, 0.17,  0.52, 0.48,  0.25, 0.24, 0.26, 0.25,  0.33, 0.33, 0.34,  0.33, 0.32, 0.35,  0.23, 0.23, 0.28, 0.26],
    "巴中市": [0.17, 0.17, 0.18, 0.16, 0.16, 0.16,  0.51, 0.49,  0.25, 0.24, 0.26, 0.25,  0.33, 0.32, 0.35,  0.33, 0.32, 0.35,  0.24, 0.25, 0.26, 0.25],
    "成都市": [0.18, 0.16, 0.17, 0.14, 0.19, 0.16,  0.60, 0.40,  0.25, 0.24, 0.27, 0.24,  0.33, 0.31, 0.36,  0.33, 0.31, 0.36,  0.24, 0.21, 0.28, 0.27],
    "达州市": [0.17, 0.16, 0.17, 0.16, 0.17, 0.17,  0.47, 0.53,  0.24, 0.25, 0.25, 0.26,  0.33, 0.32, 0.35,  0.33, 0.31, 0.36,  0.22, 0.24, 0.28, 0.26],
    "德阳市": [0.17, 0.16, 0.17, 0.17, 0.16, 0.17,  0.52, 0.48,  0.24, 0.25, 0.26, 0.25,  0.34, 0.32, 0.34,  0.33, 0.32, 0.35,  0.23, 0.23, 0.28, 0.26],
    "甘孜州": [0.17, 0.16, 0.17, 0.16, 0.17, 0.17,  0.54, 0.46,  0.25, 0.24, 0.26, 0.25,  0.31, 0.35, 0.34,  0.33, 0.31, 0.36,  0.25, 0.22, 0.26, 0.27],
    "广安市": [0.17, 0.16, 0.17, 0.16, 0.17, 0.17,  0.53, 0.47,  0.24, 0.27, 0.25, 0.24,  0.34, 0.32, 0.34,  0.35, 0.31, 0.34,  0.24, 0.22, 0.28, 0.26],
    "广元市": [0.17, 0.17, 0.17, 0.16, 0.17, 0.16,  0.52, 0.48,  0.25, 0.26, 0.25, 0.24,  0.35, 0.32, 0.33,  0.34, 0.32, 0.34,  0.23, 0.24, 0.27, 0.26],
    "乐山市": [0.17, 0.16, 0.17, 0.16, 0.17, 0.17,  0.52, 0.48,  0.24, 0.24, 0.25, 0.27,  0.34, 0.33, 0.33,  0.34, 0.33, 0.33,  0.20, 0.23, 0.29, 0.28],
    "凉山州": [0.17, 0.16, 0.17, 0.16, 0.17, 0.17,  0.49, 0.51,  0.25, 0.25, 0.25, 0.25,  0.34, 0.33, 0.33,  0.33, 0.33, 0.34,  0.23, 0.23, 0.28, 0.26],
    "泸州市": [0.17, 0.16, 0.17, 0.16, 0.17, 0.17,  0.52, 0.48,  0.24, 0.26, 0.26, 0.24,  0.33, 0.33, 0.34,  0.34, 0.32, 0.34,  0.21, 0.22, 0.29, 0.28],
    "眉山市": [0.18, 0.16, 0.17, 0.16, 0.16, 0.17,  0.51, 0.49,  0.25, 0.26, 0.25, 0.24,  0.33, 0.32, 0.35,  0.32, 0.31, 0.37,  0.21, 0.20, 0.32, 0.27],
    "绵阳市": [0.17, 0.16, 0.18, 0.16, 0.16, 0.17,  0.50, 0.50,  0.25, 0.25, 0.25, 0.25,  0.34, 0.32, 0.34,  0.34, 0.32, 0.34,  0.23, 0.23, 0.27, 0.27],
    "南充市": [0.18, 0.17, 0.17, 0.16, 0.17, 0.15,  0.54, 0.46,  0.23, 0.25, 0.26, 0.26,  0.35, 0.32, 0.33,  0.34, 0.32, 0.34,  0.25, 0.22, 0.27, 0.26],
    "内江市": [0.17, 0.16, 0.17, 0.17, 0.17, 0.16,  0.51, 0.49,  0.24, 0.25, 0.25, 0.26,  0.33, 0.32, 0.35,  0.34, 0.32, 0.34,  0.24, 0.23, 0.27, 0.26],
    "攀枝花市": [0.18, 0.17, 0.17, 0.16, 0.16, 0.16,  0.53, 0.47,  0.23, 0.25, 0.25, 0.27,  0.34, 0.32, 0.34,  0.33, 0.32, 0.35,  0.25, 0.21, 0.26, 0.28],
    "遂宁市": [0.18, 0.17, 0.17, 0.16, 0.16, 0.16,  0.51, 0.49,  0.24, 0.25, 0.26, 0.25,  0.34, 0.32, 0.34,  0.34, 0.31, 0.35,  0.25, 0.23, 0.27, 0.25],
    "雅安市": [0.17, 0.17, 0.17, 0.15, 0.16, 0.18,  0.48, 0.52,  0.25, 0.24, 0.24, 0.27,  0.33, 0.32, 0.35,  0.33, 0.29, 0.38,  0.25, 0.20, 0.27, 0.28],
    "宜宾市": [0.16, 0.16, 0.17, 0.16, 0.17, 0.18,  0.51, 0.49,  0.24, 0.25, 0.26, 0.25,  0.33, 0.32, 0.35,  0.33, 0.32, 0.35,  0.21, 0.24, 0.27, 0.28],
    "资阳市": [0.18, 0.16, 0.17, 0.16, 0.17, 0.16,  0.50, 0.50,  0.25, 0.25, 0.25, 0.25,  0.34, 0.33, 0.33,  0.34, 0.32, 0.34,  0.25, 0.21, 0.29, 0.25],
    "自贡市": [0.18, 0.16, 0.17, 0.16, 0.17, 0.16,  0.49, 0.51,  0.23, 0.25, 0.26, 0.26,  0.33, 0.33, 0.34,  0.33, 0.31, 0.36,  0.24, 0.21, 0.30, 0.25],
}

# ─── Indicator definitions ───
level1_indicators = [
    ("L1_GOVERNMENT",          "政府减灾能力"),
    ("L1_ENTERPRISE",          "企业减灾能力"),
    ("L1_SOCIAL_ORGANIZATION", "社会组织减灾能力"),
    ("L1_TOWNSHIP",            "乡镇（街道）减灾能力"),
    ("L1_COMMUNITY",           "社区（行政区）减灾能力"),
    ("L1_FAMILY",              "家庭减灾能力"),
]

# (code, name, parent_index_into level1_indicators)
level2_indicators = [
    # Under 政府减灾能力 (parent idx=0)
    ("L2_GOV_MANAGEMENT",    "管理能力",           0),
    ("L2_GOV_ENGINEERING",   "工程设防能力",       0),
    ("L2_GOV_MONITORING",    "监测预警能力",       0),
    ("L2_GOV_MATERIAL",      "物资储备能力",       0),
    ("L2_GOV_RESCUE_TEAM",   "专业队伍救援能力",   0),
    ("L2_GOV_RELOCATION",    "转移安置能力",       0),
    # Under 企业减灾能力 (parent idx=1)
    ("L2_ENT_ENGINEERING_RESCUE", "大型企业应急救援能力",     1),
    ("L2_ENT_INSURANCE",          "保险和再保险企业减灾能力", 1),
    # Under 社会组织减灾能力 (parent idx=2)
    ("L2_SOC_MATERIAL",   "物资储备能力",   2),
    ("L2_SOC_TRANSPORT",  "应急运输能力",   2),
    ("L2_SOC_RESCUE",     "应急救援能力",   2),
    ("L2_SOC_PUBLICITY",  "科普宣传能力",   2),
    # Under 乡镇（街道）减灾能力 (parent idx=3)
    ("L2_TWN_DISASTER_MANAGEMENT",    "灾害管理能力", 3),
    ("L2_TWN_DISASTER_PREPAREDNESS",  "灾害备灾能力", 3),
    ("L2_TWN_SELF_RESCUE_TRANSFER",   "自救转移能力", 3),
    # Under 社区（行政区）减灾能力 (parent idx=4)
    ("L2_COM_DISASTER_MANAGEMENT",    "灾害管理能力", 4),
    ("L2_COM_DISASTER_PREPAREDNESS",  "灾害备灾能力", 4),
    ("L2_COM_SELF_RESCUE_TRANSFER",   "自救转移能力", 4),
    # Under 家庭减灾能力 (parent idx=5)
    ("L2_FAM_VULNERABILITY", "家庭脆弱性",         5),
    ("L2_FAM_MATERIAL",      "防灾物资储备能力",   5),
    ("L2_FAM_INFORMATION",   "灾害信息获取能力",   5),
    ("L2_FAM_SELF_RESCUE",   "灾害自救互救能力",   5),
]

L1_CODES = [c for c, _ in level1_indicators]


def generate_sql():
    lines = [
        "SET NAMES utf8mb4;",
        "",
        "-- =============================================================",
        "-- Insert city-level comprehensive disaster reduction weights",
        "-- Model ID: 20 (2020年市级综合减灾能力评估模型)",
        "-- Year: 2020, Data source: baseline",
        "-- =============================================================",
        "",
    ]

    for city_name, city_info in level1_data.items():
        orgcode = city_info["orgcode"]
        l1_weights = city_info["weights"]
        l2_weights = level2_data[city_name]

        lines.append(f"-- ─── {city_name} (orgcode={orgcode}) ───")

        # Idempotent: delete existing config for this orgcode+model_id+year
        lines.append(
            f"DELETE wc FROM weight_config wc "
            f"LEFT JOIN indicator_weight iw ON iw.config_id = wc.id "
            f"WHERE wc.orgcode = '{orgcode}' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;"
        )
        lines.append(
            f"DELETE FROM indicator_weight WHERE config_id IN "
            f"(SELECT id FROM weight_config WHERE orgcode = '{orgcode}' AND model_id = 20 AND year = 2020);"
        )

        # Insert weight_config
        lines.append(
            f"INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) "
            f"VALUES ('综合减灾能力评估权重', '2020年{city_name}综合减灾能力评估权重', '{orgcode}', 'baseline', 2020, 20, 0);"
        )
        lines.append("SET @config_id = LAST_INSERT_ID();")
        lines.append("")

        # Insert level-1 indicators
        l1_values = []
        for idx, ((code, name), w) in enumerate(zip(level1_indicators, l1_weights)):
            l1_values.append(f"(@config_id, '{code}', '{name}', 1, {w:.2f}, NULL, {idx + 1})")
        lines.append(
            "INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES"
        )
        lines.append(",\n".join(l1_values) + ";")
        lines.append("")

        # Get parent IDs via variables
        for idx, (code, _) in enumerate(level1_indicators):
            var = f"@p{idx}"
            lines.append(
                f"SET {var} = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = '{code}');"
            )
        lines.append("")

        # Insert level-2 indicators
        l2_values = []
        for idx, (code, name, parent_idx) in enumerate(level2_indicators):
            w = l2_weights[idx]
            l2_values.append(f"(@config_id, '{code}', '{name}', 2, {w:.2f}, @p{parent_idx}, {idx + 1})")
        lines.append(
            "INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES"
        )
        lines.append(",\n".join(l2_values) + ";")
        lines.append("")

    return "\n".join(lines)


if __name__ == "__main__":
    sql = generate_sql()
    output_path = "insert_city_comprehensive_weights_2020.sql"
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(sql)
    print(f"Generated {output_path} ({len(sql)} bytes)")
