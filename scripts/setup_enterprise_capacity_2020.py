import json
import os
from datetime import datetime

import pymysql


def connect():
    return pymysql.connect(
        host=os.getenv("MYSQL_HOST", "127.0.0.1"),
        port=int(os.getenv("MYSQL_PORT", "30314")),
        user=os.getenv("MYSQL_USER", "root"),
        password=os.getenv("MYSQL_PASSWORD", "123456"),
        database=os.getenv("MYSQL_DB", "evaluate_db"),
        charset="utf8mb4",
        autocommit=False,
    )


def run():
    model_code = "ENTERPRISE_DISASTER_REDUCTION_2020"
    backup_suffix = datetime.now().strftime("%Y%m%d%H%M%S")
    conn = connect()
    cur = conn.cursor()
    try:
        for table_name in ("evaluation_model", "model_step", "step_algorithm"):
            backup_name = f"{table_name}_bak_2020_ent_{backup_suffix}"
            cur.execute(f"CREATE TABLE `{backup_name}` LIKE `{table_name}`")
            cur.execute(f"INSERT INTO `{backup_name}` SELECT * FROM `{table_name}`")

        cur.execute("SELECT id FROM evaluation_model WHERE model_code=%s LIMIT 1", (model_code,))
        row = cur.fetchone()
        if row:
            model_id = row[0]
            cur.execute(
                """
                UPDATE evaluation_model
                SET model_name=%s, description=%s, version=%s, status=1, is_default=0, update_by=%s
                WHERE id=%s
                """,
                (
                    "企业减灾能力评估（2020）",
                    "基于2020年企业调查数据的企业减灾能力评估模型",
                    "2020.1",
                    "trae",
                    model_id,
                ),
            )
            cur.execute("SELECT id FROM model_step WHERE model_id=%s", (model_id,))
            step_ids = [r[0] for r in cur.fetchall()]
            if step_ids:
                placeholders = ",".join(["%s"] * len(step_ids))
                cur.execute(f"DELETE FROM step_algorithm WHERE step_id IN ({placeholders})", step_ids)
            cur.execute("DELETE FROM model_step WHERE model_id=%s", (model_id,))
        else:
            cur.execute(
                """
                INSERT INTO evaluation_model (
                    model_name, model_code, description, version, status, is_default, create_by, update_by
                ) VALUES (%s,%s,%s,%s,1,0,%s,%s)
                """,
                (
                    "企业减灾能力评估（2020）",
                    model_code,
                    "基于2020年企业调查数据的企业减灾能力评估模型",
                    "2020.1",
                    "trae",
                    "trae",
                ),
            )
            model_id = cur.lastrowid

        steps = [
            (
                "评估指标赋值",
                "indicator_assignment",
                1,
                "CALCULATION",
                "将企业原始调查字段映射为评估指标值",
                [
                    "large_excavator_count",
                    "large_truck_crane_count",
                    "large_loader_count",
                    "large_crawler_bulldozer_count",
                    "population",
                    "professional_underwriter_count",
                    "last_year_disaster_premium_income",
                    "professional_claim_settler_count",
                    "last_year_claim_payout",
                    "last_year_insurance_reinsurance_income",
                ],
                [
                    "large_excavator_owning_rate",
                    "large_truck_crane_owning_rate",
                    "large_loader_owning_rate",
                    "large_crawler_bulldozer_owning_rate",
                    "insurance_disaster_participation_capacity",
                    "disaster_team_support_capacity",
                    "disaster_insurance_claim_capacity",
                ],
            ),
            (
                "属性向量归一化",
                "attribute_vector_normalization",
                2,
                "NORMALIZATION",
                "对二级指标进行向量归一化",
                [
                    "large_excavator_owning_rate",
                    "large_truck_crane_owning_rate",
                    "large_loader_owning_rate",
                    "large_crawler_bulldozer_owning_rate",
                    "insurance_disaster_participation_capacity",
                    "disaster_team_support_capacity",
                    "disaster_insurance_claim_capacity",
                ],
                [
                    "large_excavator_owning_rate_norm",
                    "large_truck_crane_owning_rate_norm",
                    "large_loader_owning_rate_norm",
                    "large_crawler_bulldozer_owning_rate_norm",
                    "insurance_disaster_participation_capacity_norm",
                    "disaster_team_support_capacity_norm",
                    "disaster_insurance_claim_capacity_norm",
                ],
            ),
            (
                "二级指标定权",
                "secondary_indicator_weighting",
                3,
                "WEIGHTING",
                "归一化指标乘以权重",
                [
                    "large_excavator_owning_rate_norm",
                    "large_truck_crane_owning_rate_norm",
                    "large_loader_owning_rate_norm",
                    "large_crawler_bulldozer_owning_rate_norm",
                    "insurance_disaster_participation_capacity_norm",
                    "disaster_team_support_capacity_norm",
                    "disaster_insurance_claim_capacity_norm",
                ],
                [
                    "w_large_excavator_owning_rate",
                    "w_large_truck_crane_owning_rate",
                    "w_large_loader_owning_rate",
                    "w_large_crawler_bulldozer_owning_rate",
                    "w_insurance_disaster_participation_capacity",
                    "w_disaster_team_support_capacity",
                    "w_disaster_insurance_claim_capacity",
                ],
            ),
            (
                "D+、D-",
                "distance_ideal",
                4,
                "TOPSIS",
                "计算与正负理想解距离",
                [
                    "w_large_excavator_owning_rate",
                    "w_large_truck_crane_owning_rate",
                    "w_large_loader_owning_rate",
                    "w_large_crawler_bulldozer_owning_rate",
                    "w_insurance_disaster_participation_capacity",
                    "w_disaster_team_support_capacity",
                    "w_disaster_insurance_claim_capacity",
                ],
                [
                    "engineering_rescue_capacity_d_plus",
                    "insurance_reinsurance_capacity_d_plus",
                    "engineering_rescue_capacity_d_minus",
                    "insurance_reinsurance_capacity_d_minus",
                ],
            ),
            (
                "一级指标能力值",
                "primary_indicator_value",
                5,
                "CALCULATION",
                "根据D+和D-计算一级指标能力值",
                [
                    "engineering_rescue_capacity_d_plus",
                    "engineering_rescue_capacity_d_minus",
                    "insurance_reinsurance_capacity_d_plus",
                    "insurance_reinsurance_capacity_d_minus",
                ],
                ["engineering_rescue_capacity", "insurance_reinsurance_capacity"],
            ),
            (
                "一级指标能力分级",
                "primary_indicator_level",
                6,
                "GRADING",
                "将一级指标能力值映射为等级",
                ["engineering_rescue_capacity", "insurance_reinsurance_capacity"],
                ["engineering_rescue_capacity_level", "insurance_reinsurance_capacity_level"],
            ),
        ]

        step_id_by_order = {}
        inserted_step_ids = []
        for step_name, step_code, step_order, step_type, desc, input_vars, output_vars in steps:
            cur.execute(
                """
                INSERT INTO model_step (
                    model_id, step_name, step_code, step_order, step_type, description,
                    input_variables, output_variables, depends_on, status
                ) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,'',1)
                """,
                (
                    model_id,
                    step_name,
                    step_code,
                    step_order,
                    step_type,
                    desc,
                    json.dumps(input_vars, ensure_ascii=False),
                    json.dumps(output_vars, ensure_ascii=False),
                ),
            )
            inserted_step_ids.append(cur.lastrowid)
            step_id_by_order[step_order] = cur.lastrowid

        for i in range(1, len(inserted_step_ids)):
            cur.execute(
                "UPDATE model_step SET depends_on=%s WHERE id=%s",
                (str(inserted_step_ids[i - 1]), inserted_step_ids[i]),
            )

        algorithms_by_step_order = {
            1: [
                (
                    "万人大型挖掘机拥有率",
                    "EL_ENTERPRISE_INDICATOR_ASSIGNMENT_001",
                    1,
                    "(population==0?0:large_excavator_count/population*10000)",
                    "{}",
                    "large_excavator_owning_rate",
                    "企业评估指标赋值",
                ),
                (
                    "万人大型汽车式起重机拥有率",
                    "EL_ENTERPRISE_INDICATOR_ASSIGNMENT_002",
                    2,
                    "(population==0?0:large_truck_crane_count/population*10000)",
                    "{}",
                    "large_truck_crane_owning_rate",
                    "企业评估指标赋值",
                ),
                (
                    "万人大型装载机拥有率",
                    "EL_ENTERPRISE_INDICATOR_ASSIGNMENT_003",
                    3,
                    "(population==0?0:large_loader_count/population*10000)",
                    "{}",
                    "large_loader_owning_rate",
                    "企业评估指标赋值",
                ),
                (
                    "万人大型履带式推土机拥有率",
                    "EL_ENTERPRISE_INDICATOR_ASSIGNMENT_004",
                    4,
                    "(population==0?0:large_crawler_bulldozer_count/population*10000)",
                    "{}",
                    "large_crawler_bulldozer_owning_rate",
                    "企业评估指标赋值",
                ),
                (
                    "保险参与救灾能力",
                    "EL_ENTERPRISE_INDICATOR_ASSIGNMENT_005",
                    5,
                    "(last_year_disaster_premium_income==0?0:professional_underwriter_count/last_year_disaster_premium_income*10000)",
                    "{}",
                    "insurance_disaster_participation_capacity",
                    "企业评估指标赋值",
                ),
                (
                    "灾害队伍保障能力",
                    "EL_ENTERPRISE_INDICATOR_ASSIGNMENT_006",
                    6,
                    "(last_year_claim_payout==0?0:professional_claim_settler_count/last_year_claim_payout*10000)",
                    "{}",
                    "disaster_team_support_capacity",
                    "企业评估指标赋值",
                ),
                (
                    "涉灾类保险赔付能力",
                    "EL_ENTERPRISE_INDICATOR_ASSIGNMENT_007",
                    7,
                    "(last_year_insurance_reinsurance_income==0?0:last_year_claim_payout/last_year_insurance_reinsurance_income)",
                    "{}",
                    "disaster_insurance_claim_capacity",
                    "企业评估指标赋值",
                ),
            ],
            2: [
                (
                    "万人大型挖掘机拥有率",
                    "EL_ENTERPRISE_NORMALIZE_001",
                    1,
                    "@NORMALIZE:large_excavator_owning_rate",
                    "{}",
                    "large_excavator_owning_rate_norm",
                    "属性向量归一化",
                ),
                (
                    "万人大型汽车式起重机拥有率",
                    "EL_ENTERPRISE_NORMALIZE_002",
                    2,
                    "@NORMALIZE:large_truck_crane_owning_rate",
                    "{}",
                    "large_truck_crane_owning_rate_norm",
                    "属性向量归一化",
                ),
                (
                    "万人大型装载机拥有率",
                    "EL_ENTERPRISE_NORMALIZE_003",
                    3,
                    "@NORMALIZE:large_loader_owning_rate",
                    "{}",
                    "large_loader_owning_rate_norm",
                    "属性向量归一化",
                ),
                (
                    "万人大型履带式推土机拥有率",
                    "EL_ENTERPRISE_NORMALIZE_004",
                    4,
                    "@NORMALIZE:large_crawler_bulldozer_owning_rate",
                    "{}",
                    "large_crawler_bulldozer_owning_rate_norm",
                    "属性向量归一化",
                ),
                (
                    "保险参与救灾能力",
                    "EL_ENTERPRISE_NORMALIZE_005",
                    5,
                    "@NORMALIZE:insurance_disaster_participation_capacity",
                    "{}",
                    "insurance_disaster_participation_capacity_norm",
                    "属性向量归一化",
                ),
                (
                    "灾害队伍保障能力",
                    "EL_ENTERPRISE_NORMALIZE_006",
                    6,
                    "@NORMALIZE:disaster_team_support_capacity",
                    "{}",
                    "disaster_team_support_capacity_norm",
                    "属性向量归一化",
                ),
                (
                    "涉灾类保险赔付能力",
                    "EL_ENTERPRISE_NORMALIZE_007",
                    7,
                    "@NORMALIZE:disaster_insurance_claim_capacity",
                    "{}",
                    "disaster_insurance_claim_capacity_norm",
                    "属性向量归一化",
                ),
            ],
            3: [
                (
                    "万人大型挖掘机拥有率",
                    "EL_ENTERPRISE_WEIGHTED_001",
                    1,
                    "(large_excavator_owning_rate_norm * 0.26)",
                    "{}",
                    "w_large_excavator_owning_rate",
                    "二级指标定权",
                ),
                (
                    "万人大型汽车式起重机拥有率",
                    "EL_ENTERPRISE_WEIGHTED_002",
                    2,
                    "(large_truck_crane_owning_rate_norm * 0.25)",
                    "{}",
                    "w_large_truck_crane_owning_rate",
                    "二级指标定权",
                ),
                (
                    "万人大型装载机拥有率",
                    "EL_ENTERPRISE_WEIGHTED_003",
                    3,
                    "(large_loader_owning_rate_norm * 0.25)",
                    "{}",
                    "w_large_loader_owning_rate",
                    "二级指标定权",
                ),
                (
                    "万人大型履带式推土机拥有率",
                    "EL_ENTERPRISE_WEIGHTED_004",
                    4,
                    "(large_crawler_bulldozer_owning_rate_norm * 0.24)",
                    "{}",
                    "w_large_crawler_bulldozer_owning_rate",
                    "二级指标定权",
                ),
                (
                    "保险参与救灾能力",
                    "EL_ENTERPRISE_WEIGHTED_005",
                    5,
                    "(insurance_disaster_participation_capacity_norm * 0.32)",
                    "{}",
                    "w_insurance_disaster_participation_capacity",
                    "二级指标定权",
                ),
                (
                    "灾害队伍保障能力",
                    "EL_ENTERPRISE_WEIGHTED_006",
                    6,
                    "(disaster_team_support_capacity_norm * 0.36)",
                    "{}",
                    "w_disaster_team_support_capacity",
                    "二级指标定权",
                ),
                (
                    "涉灾类保险赔付能力",
                    "EL_ENTERPRISE_WEIGHTED_007",
                    7,
                    "(disaster_insurance_claim_capacity_norm * 0.32)",
                    "{}",
                    "w_disaster_insurance_claim_capacity",
                    "二级指标定权",
                ),
            ],
            4: [
                (
                    "大型工程建设等企业应急救援能力D+",
                    "EL_ENTERPRISE_D_PLUS_001",
                    1,
                    "@TOPSIS_POSITIVE:w_large_excavator_owning_rate,w_large_truck_crane_owning_rate,w_large_loader_owning_rate,w_large_crawler_bulldozer_owning_rate",
                    "{}",
                    "engineering_rescue_capacity_d_plus",
                    "正理想解距离",
                ),
                (
                    "保险和再保险企业救灾能力D+",
                    "EL_ENTERPRISE_D_PLUS_002",
                    2,
                    "@TOPSIS_POSITIVE:w_insurance_disaster_participation_capacity,w_disaster_team_support_capacity,w_disaster_insurance_claim_capacity",
                    "{}",
                    "insurance_reinsurance_capacity_d_plus",
                    "正理想解距离",
                ),
                (
                    "大型工程建设等企业应急救援能力D-",
                    "EL_ENTERPRISE_D_MINUS_001",
                    3,
                    "@TOPSIS_NEGATIVE:w_large_excavator_owning_rate,w_large_truck_crane_owning_rate,w_large_loader_owning_rate,w_large_crawler_bulldozer_owning_rate",
                    "{}",
                    "engineering_rescue_capacity_d_minus",
                    "负理想解距离",
                ),
                (
                    "保险和再保险企业救灾能力D-",
                    "EL_ENTERPRISE_D_MINUS_002",
                    4,
                    "@TOPSIS_NEGATIVE:w_insurance_disaster_participation_capacity,w_disaster_team_support_capacity,w_disaster_insurance_claim_capacity",
                    "{}",
                    "insurance_reinsurance_capacity_d_minus",
                    "负理想解距离",
                ),
            ],
            5: [
                (
                    "大型工程建设等企业应急救援能力",
                    "EL_ENTERPRISE_PRIMARY_SCORE_001",
                    1,
                    "@TOPSIS_SCORE:engineering_rescue_capacity_d_plus,engineering_rescue_capacity_d_minus",
                    "{}",
                    "engineering_rescue_capacity",
                    "一级指标能力值计算",
                ),
                (
                    "保险和再保险企业救灾能力",
                    "EL_ENTERPRISE_PRIMARY_SCORE_002",
                    2,
                    "@TOPSIS_SCORE:insurance_reinsurance_capacity_d_plus,insurance_reinsurance_capacity_d_minus",
                    "{}",
                    "insurance_reinsurance_capacity",
                    "一级指标能力值计算",
                ),
            ],
            6: [
                (
                    "大型工程建设等企业应急救援能力",
                    "EL_ENTERPRISE_RESULT_GRADE_001",
                    1,
                    "@GRADE:engineering_rescue_capacity",
                    "{}",
                    "engineering_rescue_capacity_level",
                    "一级指标能力分级",
                ),
                (
                    "保险和再保险企业救灾能力",
                    "EL_ENTERPRISE_RESULT_GRADE_002",
                    2,
                    "@GRADE:insurance_reinsurance_capacity",
                    "{}",
                    "insurance_reinsurance_capacity_level",
                    "一级指标能力分级",
                ),
            ],
        }

        for step_order, defs in algorithms_by_step_order.items():
            step_id = step_id_by_order[step_order]
            for item in defs:
                cur.execute(
                    """
                    INSERT INTO step_algorithm (
                        step_id, algorithm_name, algorithm_code, algorithm_order,
                        ql_expression, input_params, output_param, description, status
                    ) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,1)
                    """,
                    (step_id, *item),
                )

        conn.commit()
        cur.execute("SELECT COUNT(*) FROM model_step WHERE model_id=%s", (model_id,))
        step_count = cur.fetchone()[0]
        cur.execute(
            "SELECT COUNT(*) FROM step_algorithm WHERE step_id IN (SELECT id FROM model_step WHERE model_id=%s)",
            (model_id,),
        )
        algorithm_count = cur.fetchone()[0]
        print(f"backup_suffix={backup_suffix}")
        print(f"model_id={model_id}")
        print(f"model_step_count={step_count}")
        print(f"step_algorithm_count={algorithm_count}")
    except Exception:
        conn.rollback()
        raise
    finally:
        cur.close()
        conn.close()


if __name__ == "__main__":
    run()
