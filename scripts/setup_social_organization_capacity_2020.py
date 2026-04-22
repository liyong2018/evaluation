#!/usr/bin/env python3
import pymysql
from datetime import datetime


DB_CONFIG = {
    "host": "127.0.0.1",
    "port": 30314,
    "user": "root",
    "password": "123456",
    "database": "evaluate_db",
    "charset": "utf8mb4",
    "autocommit": False,
}


MODEL_NAME = "社会组织减灾能力评估（2020）"
MODEL_CODE = "SOCIAL_ORGANIZATION_DISASTER_REDUCTION_2020"
MODEL_DESC = "基于2020年社会组织调查数据的社会组织减灾能力评估模型"
MODEL_VERSION = "2020.1"


def build_backup_suffix():
    return datetime.now().strftime("%Y%m%d_%H%M%S")


def backup_tables(cur, suffix):
    cur.execute(f"CREATE TABLE evaluation_model_bak_{suffix} AS SELECT * FROM evaluation_model")
    cur.execute(f"CREATE TABLE model_step_bak_{suffix} AS SELECT * FROM model_step")
    cur.execute(f"CREATE TABLE step_algorithm_bak_{suffix} AS SELECT * FROM step_algorithm")


def upsert_model(cur):
    cur.execute("SELECT id FROM evaluation_model WHERE model_code=%s LIMIT 1", (MODEL_CODE,))
    row = cur.fetchone()
    if row:
        model_id = row[0]
        cur.execute(
            """
            UPDATE evaluation_model
            SET model_name=%s,
                description=%s,
                version=%s,
                status=1,
                is_default=0,
                update_time=NOW(),
                update_by='trae'
            WHERE id=%s
            """,
            (MODEL_NAME, MODEL_DESC, MODEL_VERSION, model_id),
        )
    else:
        cur.execute(
            """
            INSERT INTO evaluation_model (
                model_name, model_code, description, version,
                status, is_default, create_by, update_by
            ) VALUES (%s,%s,%s,%s,1,0,'trae','trae')
            """,
            (MODEL_NAME, MODEL_CODE, MODEL_DESC, MODEL_VERSION),
        )
        model_id = cur.lastrowid
    return model_id


def clear_old_steps(cur, model_id):
    cur.execute("SELECT id FROM model_step WHERE model_id=%s", (model_id,))
    step_ids = [row[0] for row in cur.fetchall()]
    if step_ids:
        placeholders = ",".join(["%s"] * len(step_ids))
        cur.execute(f"DELETE FROM step_algorithm WHERE step_id IN ({placeholders})", tuple(step_ids))
        cur.execute("DELETE FROM model_step WHERE model_id=%s", (model_id,))


def insert_steps(cur, model_id):
    steps = [
        ("评估指标赋值", "SOC_ORG_STEP_1", 1, "CALCULATION", "社会组织评估指标赋值", "[]", "[]", ""),
        ("属性向量归一化", "SOC_ORG_STEP_2", 2, "CALCULATION", "社会组织属性向量归一化", "[]", "[]", "1"),
        ("二级指标定权", "SOC_ORG_STEP_3", 3, "CALCULATION", "社会组织二级指标定权", "[]", "[]", "2"),
        ("D+ 以及 D-", "SOC_ORG_STEP_4", 4, "CALCULATION", "社会组织D+及D-计算", "[]", "[]", "3"),
        ("一级指标能力值", "SOC_ORG_STEP_5", 5, "CALCULATION", "社会组织一级指标能力值计算", "[]", "[]", "4"),
        ("一级指标能力分级", "SOC_ORG_STEP_6", 6, "CALCULATION", "社会组织一级指标能力分级", "[]", "[]", "5"),
    ]
    step_id_by_order = {}
    for s in steps:
        cur.execute(
            """
            INSERT INTO model_step (
                model_id, step_name, step_code, step_order, step_type, description,
                input_variables, output_variables, depends_on, status
            ) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,1)
            """,
            (model_id, *s),
        )
        step_id_by_order[s[2]] = cur.lastrowid
    return step_id_by_order


def insert_algorithms(cur, step_id_by_order):
    algorithms_by_step_order = {
        1: [
            (
                "物资储备能力",
                "SO_DR_IND_001",
                1,
                "(population==0?0:emergency_equipment_material_value/population)",
                "{}",
                "material_reserve_capacity",
                "评估指标赋值",
            ),
            (
                "应急运输能力",
                "SO_DR_IND_002",
                2,
                "(population==0?0:(passenger_vehicle_count+freight_vehicle_count)/population)",
                "{}",
                "emergency_transport_capacity",
                "评估指标赋值",
            ),
            (
                "应急救援能力",
                "SO_DR_IND_003",
                3,
                "(population==0?0:special_operation_vehicle_count/population)",
                "{}",
                "emergency_rescue_capacity",
                "评估指标赋值",
            ),
            (
                "科普宣传能力",
                "SO_DR_IND_004",
                4,
                "(population==0?0:last_year_science_education_audience/population)",
                "{}",
                "science_publicity_capacity",
                "评估指标赋值",
            ),
        ],
        2: [
            (
                "物资储备能力归一化",
                "SO_DR_NORM_001",
                1,
                "@NORMALIZE:material_reserve_capacity",
                "{}",
                "material_reserve_capacity_norm",
                "属性向量归一化",
            ),
            (
                "应急运输能力归一化",
                "SO_DR_NORM_002",
                2,
                "@NORMALIZE:emergency_transport_capacity",
                "{}",
                "emergency_transport_capacity_norm",
                "属性向量归一化",
            ),
            (
                "应急救援能力归一化",
                "SO_DR_NORM_003",
                3,
                "@NORMALIZE:emergency_rescue_capacity",
                "{}",
                "emergency_rescue_capacity_norm",
                "属性向量归一化",
            ),
            (
                "科普宣传能力归一化",
                "SO_DR_NORM_004",
                4,
                "@NORMALIZE:science_publicity_capacity",
                "{}",
                "science_publicity_capacity_norm",
                "属性向量归一化",
            ),
        ],
        3: [
            (
                "物资储备能力定权",
                "SO_DR_WEIGHT_001",
                1,
                "(material_reserve_capacity_norm * 0.25)",
                "{}",
                "weighted_material_reserve_capacity",
                "二级指标定权",
            ),
            (
                "应急运输能力定权",
                "SO_DR_WEIGHT_002",
                2,
                "(emergency_transport_capacity_norm * 0.25)",
                "{}",
                "weighted_emergency_transport_capacity",
                "二级指标定权",
            ),
            (
                "应急救援能力定权",
                "SO_DR_WEIGHT_003",
                3,
                "(emergency_rescue_capacity_norm * 0.25)",
                "{}",
                "weighted_emergency_rescue_capacity",
                "二级指标定权",
            ),
            (
                "科普宣传能力定权",
                "SO_DR_WEIGHT_004",
                4,
                "(science_publicity_capacity_norm * 0.25)",
                "{}",
                "weighted_science_publicity_capacity",
                "二级指标定权",
            ),
        ],
        4: [
            (
                "社会组织减灾能力D+",
                "SO_DR_DPLUS_001",
                1,
                "@TOPSIS_POSITIVE:weighted_material_reserve_capacity,weighted_emergency_transport_capacity,weighted_emergency_rescue_capacity,weighted_science_publicity_capacity",
                "{}",
                "social_org_disaster_reduction_d_plus",
                "D+ 以及 D-",
            ),
            (
                "社会组织减灾能力D-",
                "SO_DR_DMINUS_001",
                2,
                "@TOPSIS_NEGATIVE:weighted_material_reserve_capacity,weighted_emergency_transport_capacity,weighted_emergency_rescue_capacity,weighted_science_publicity_capacity",
                "{}",
                "social_org_disaster_reduction_d_minus",
                "D+ 以及 D-",
            ),
        ],
        5: [
            (
                "社会组织减灾能力",
                "SO_DR_SCORE_001",
                1,
                "@TOPSIS_SCORE:social_org_disaster_reduction_d_plus,social_org_disaster_reduction_d_minus",
                "{}",
                "social_org_disaster_reduction_capacity",
                "一级指标能力值",
            ),
        ],
        6: [
            (
                "社会组织减灾能力",
                "SO_DR_GRADE_001",
                1,
                "@GRADE:social_org_disaster_reduction_capacity",
                "{}",
                "social_org_disaster_reduction_capacity_level",
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


def run():
    conn = pymysql.connect(**DB_CONFIG)
    cur = conn.cursor()
    try:
        backup_suffix = build_backup_suffix()
        backup_tables(cur, backup_suffix)
        model_id = upsert_model(cur)
        clear_old_steps(cur, model_id)
        step_id_by_order = insert_steps(cur, model_id)
        insert_algorithms(cur, step_id_by_order)
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
