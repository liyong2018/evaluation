#!/usr/bin/env python3
"""校验模型19步骤2：数据库执行记录与接口JSON一致，并校验510603关键指标值。"""

import json
import math
import subprocess
import urllib.request

import pymysql


COUNTY_CODES = ["510603", "510604", "510623", "510681", "510682", "510683"]
METRIC_KEYS = [
    "team_management_capability",
    "risk_assessment_capability",
    "financial_investment_capability",
    "material_reserve_capability",
    "medical_security_capability",
    "self_mutual_rescue_capability",
    "public_evacuation_capability",
    "relocation_settlement_capability",
]

EXPECTED_510603 = {
    "team_management_capability": 0.54335423,
    "financial_investment_capability": 4.00739686,
}

EXPECTED_STEP3_NORMALIZED = {
    "510603": {
        "team_management_capability_norm": 0.28813690,
        "risk_assessment_capability_norm": 0.17313819,
        "financial_investment_capability_norm": 0.17962606,
        "material_reserve_capability_norm": 0.18987715,
        "medical_security_capability_norm": 0.43237602,
        "self_mutual_rescue_capability_norm": 0.34482932,
        "public_evacuation_capability_norm": 0.14315794,
        "relocation_settlement_capability_norm": 0.13458817,
    },
    "510604": {
        "team_management_capability_norm": 0.48188158,
        "risk_assessment_capability_norm": 0.48231354,
        "financial_investment_capability_norm": 0.77460465,
        "material_reserve_capability_norm": 0.67320033,
        "medical_security_capability_norm": 0.44368489,
        "self_mutual_rescue_capability_norm": 0.54860589,
        "public_evacuation_capability_norm": 0.74340763,
        "relocation_settlement_capability_norm": 0.86636290,
    },
    "510623": {
        "team_management_capability_norm": 0.45404719,
        "risk_assessment_capability_norm": 0.0,
        "financial_investment_capability_norm": 0.26034565,
        "material_reserve_capability_norm": 0.20510322,
        "medical_security_capability_norm": 0.32589507,
        "self_mutual_rescue_capability_norm": 0.06896574,
        "public_evacuation_capability_norm": 0.24250762,
        "relocation_settlement_capability_norm": 0.28300089,
    },
    "510681": {
        "team_management_capability_norm": 0.26254976,
        "risk_assessment_capability_norm": 0.37513275,
        "financial_investment_capability_norm": 0.31552710,
        "material_reserve_capability_norm": 0.33367146,
        "medical_security_capability_norm": 0.34571214,
        "self_mutual_rescue_capability_norm": 0.19386449,
        "public_evacuation_capability_norm": 0.40161538,
        "relocation_settlement_capability_norm": 0.27180054,
    },
    "510682": {
        "team_management_capability_norm": 0.27376686,
        "risk_assessment_capability_norm": 0.67523896,
        "financial_investment_capability_norm": 0.33028867,
        "material_reserve_capability_norm": 0.23042504,
        "medical_security_capability_norm": 0.40544035,
        "self_mutual_rescue_capability_norm": 0.30426079,
        "public_evacuation_capability_norm": 0.36061750,
        "relocation_settlement_capability_norm": 0.17162468,
    },
    "510683": {
        "team_management_capability_norm": 0.57855660,
        "risk_assessment_capability_norm": 0.37513275,
        "financial_investment_capability_norm": 0.30214946,
        "material_reserve_capability_norm": 0.55158708,
        "medical_security_capability_norm": 0.47548747,
        "self_mutual_rescue_capability_norm": 0.66724057,
        "public_evacuation_capability_norm": 0.27694957,
        "relocation_settlement_capability_norm": 0.21881792,
    },
}


def extract_step2_rows(execution_result: dict) -> dict:
    step2 = None
    for step in execution_result.get("stepResultsList") or []:
        if int(step.get("stepOrder") or 0) == 2:
            step2 = step
            break
    if not step2:
        raise SystemExit("FAILED: 未找到步骤2结果")

    rows = {}
    for row in step2.get("tableData") or []:
        code = str(row.get("regionCode") or "").strip()
        if code:
            rows[code] = row
    return rows


def fetch_detail_json(execution_id: int) -> dict:
    detail_url = f"http://127.0.0.1:8080/api/evaluation/history/detail/{execution_id}"
    req = urllib.request.Request(detail_url, headers={"User-Agent": "verify-model19/1.0"})
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except Exception:
        # 某些环境下 urllib 会被本地代理返回 503，回退到 curl 直取
        raw = subprocess.check_output(["curl", "-s", detail_url], text=True)
        return json.loads(raw)


def main() -> None:
    tolerance = 1e-8
    failures = []

    conn = pymysql.connect(
        host="127.0.0.1",
        port=30314,
        user="root",
        password="123456",
        database="evaluate_db",
        charset="utf8mb4",
    )

    try:
        with conn.cursor(pymysql.cursors.DictCursor) as cur:
            cur.execute(
                """
                SELECT id, result_detail
                FROM model_execution_record
                WHERE model_id = 19
                  AND execution_status = 'SUCCESS'
                ORDER BY id DESC
                LIMIT 1
                """
            )
            rec = cur.fetchone()
            if not rec or not rec.get("result_detail"):
                raise SystemExit("FAILED: 未找到模型19最新成功执行记录")

            execution_id = int(rec["id"])
            db_execution_result = json.loads(rec["result_detail"])
            db_rows = extract_step2_rows(db_execution_result)
    finally:
        conn.close()

    api_result = fetch_detail_json(execution_id)

    api_execution_result = (api_result.get("data") or {}).get("executionResult")
    if not api_execution_result:
        raise SystemExit("FAILED: 接口返回中缺少 executionResult")
    api_rows = extract_step2_rows(api_execution_result)
    api_step3_rows = {}
    for step in api_execution_result.get("stepResultsList") or []:
        if int(step.get("stepOrder") or 0) == 3:
            for row in step.get("tableData") or []:
                code = str(row.get("regionCode") or "").strip()
                if code:
                    api_step3_rows[code] = row
            break

    for code in COUNTY_CODES:
        db_row = db_rows.get(code)
        api_row = api_rows.get(code)
        if not db_row or not api_row:
            failures.append((code, "步骤2缺失", "db/api缺失"))
            continue

        for metric in METRIC_KEYS:
            db_val = float(db_row.get(metric) or 0.0)
            api_val = float(api_row.get(metric) or 0.0)
            if math.fabs(db_val - api_val) >= tolerance:
                failures.append((code, metric, "db_vs_api", db_val, api_val))

    row_510603 = db_rows.get("510603")
    if not row_510603:
        failures.append(("510603", "步骤2缺失", "db缺失"))
    else:
        for metric, expected in EXPECTED_510603.items():
            actual = float(row_510603.get(metric) or 0.0)
            if math.fabs(actual - expected) >= tolerance:
                failures.append(("510603", metric, "db_vs_expected", actual, expected))

    for code, expected_metrics in EXPECTED_STEP3_NORMALIZED.items():
        row = api_step3_rows.get(code)
        if not row:
            failures.append((code, "step3", "api缺失"))
            continue
        for metric, expected in expected_metrics.items():
            actual = float(row.get(metric) or 0.0)
            if math.fabs(actual - expected) >= tolerance:
                failures.append((code, metric, "step3_vs_expected", actual, expected))

    if failures:
        print("FAILED")
        for item in failures:
            print(item)
        raise SystemExit(1)

    print(f"PASS: executionId={execution_id}")
    print("PASS: 6个区县*8项指标 DB 与 API 全量一致，abs(diff) < 1e-8")
    print("PASS: 510603 队伍管理能力/财政投入能力达到目标值")
    print("PASS: 步骤3归一化值与基准表完全一致")


if __name__ == "__main__":
    main()
