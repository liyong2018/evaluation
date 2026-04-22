import os
import json
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
    model_code = "GOVERNMENT_DISASTER_REDUCTION_2020"
    backup_suffix = datetime.now().strftime("%Y%m%d%H%M%S")
    conn = connect()
    cur = conn.cursor()
    try:
        for table_name in ("evaluation_model", "model_step", "step_algorithm"):
            backup_name = f"{table_name}_bak_2020_gov_{backup_suffix}"
            cur.execute(f"CREATE TABLE `{backup_name}` LIKE `{table_name}`")
            cur.execute(f"INSERT INTO `{backup_name}` SELECT * FROM `{table_name}`")

        cur.execute(
            """
            CREATE TABLE IF NOT EXISTS government_disaster_reduction_capacity_2020 (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                region_code VARCHAR(20) NOT NULL,
                province_name VARCHAR(50) DEFAULT '',
                city_name VARCHAR(50) DEFAULT '',
                county_name VARCHAR(100) DEFAULT '',
                year INT NOT NULL,
                township_count INT DEFAULT 0,
                population BIGINT DEFAULT 0,
                total_households BIGINT DEFAULT 0,
                management_staff BIGINT DEFAULT 0,
                disaster_info_staff BIGINT DEFAULT 0,
                emergency_plan_count BIGINT DEFAULT 0,
                emergency_response_count BIGINT DEFAULT 0,
                training_drill_count BIGINT DEFAULT 0,
                shelter_count BIGINT DEFAULT 0,
                source_record_count BIGINT DEFAULT 0,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                UNIQUE KEY uk_region_year (region_code, year)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """
        )
        extra_columns = [
            ("expert_staff_count", "DECIMAL(20,9) DEFAULT 0"),
            ("disaster_prevention_plan_count", "DECIMAL(20,9) DEFAULT 0"),
            ("education_expenditure", "DECIMAL(20,9) DEFAULT 0"),
            ("science_expenditure", "DECIMAL(20,9) DEFAULT 0"),
            ("agriculture_water_expenditure", "DECIMAL(20,9) DEFAULT 0"),
            ("natural_resources_expenditure", "DECIMAL(20,9) DEFAULT 0"),
            ("grain_reserve_expenditure", "DECIMAL(20,9) DEFAULT 0"),
            ("disaster_emergency_expenditure", "DECIMAL(20,9) DEFAULT 0"),
            ("regional_gdp", "DECIMAL(20,9) DEFAULT 0"),
            ("regional_area", "DECIMAL(20,9) DEFAULT 0"),
            ("standard_flood_dike_length", "DECIMAL(20,9) DEFAULT 0"),
            ("built_flood_dike_length", "DECIMAL(20,9) DEFAULT 0"),
            ("reinforced_reservoir_dam_count", "DECIMAL(20,9) DEFAULT 0"),
            ("reservoir_dam_count", "DECIMAL(20,9) DEFAULT 0"),
            ("reinforced_sluice_count", "DECIMAL(20,9) DEFAULT 0"),
            ("sluice_count", "DECIMAL(20,9) DEFAULT 0"),
            ("geological_hazard_point_count", "DECIMAL(20,9) DEFAULT 0"),
            ("completed_geological_treatment_count", "DECIMAL(20,9) DEFAULT 0"),
            ("seawall_total_length", "DECIMAL(20,9) DEFAULT 0"),
            ("coastline_length", "DECIMAL(20,9) DEFAULT 0"),
            ("forest_fire_project_mileage", "DECIMAL(20,9) DEFAULT 0"),
            ("forest_area", "DECIMAL(20,9) DEFAULT 0"),
            ("meteorological_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("hydrological_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("seismic_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("geological_monitoring_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("ocean_monitoring_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("forest_fire_warning_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("effective_storage_capacity", "DECIMAL(20,9) DEFAULT 0"),
            ("living_material_value", "DECIMAL(20,9) DEFAULT 0"),
            ("rescue_material_value", "DECIMAL(20,9) DEFAULT 0"),
            ("other_material_value", "DECIMAL(20,9) DEFAULT 0"),
            ("fire_truck_count", "DECIMAL(20,9) DEFAULT 0"),
            ("fire_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("forest_fire_team_personnel", "DECIMAL(20,9) DEFAULT 0"),
            ("forest_fire_vehicle_vessel_count", "DECIMAL(20,9) DEFAULT 0"),
            ("aviation_rescue_team_personnel", "DECIMAL(20,9) DEFAULT 0"),
            ("fixed_wing_aircraft_count", "DECIMAL(20,9) DEFAULT 0"),
            ("helicopter_count", "DECIMAL(20,9) DEFAULT 0"),
            ("earthquake_rescue_team_personnel", "DECIMAL(20,9) DEFAULT 0"),
            ("detection_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("search_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("rescue_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("medical_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("communication_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("information_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("logistics_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("vehicle_equipment_total", "DECIMAL(20,9) DEFAULT 0"),
            ("mine_tunnel_rescue_personnel", "DECIMAL(20,9) DEFAULT 0"),
            ("drill_machine_count", "DECIMAL(20,9) DEFAULT 0"),
            ("drainage_equipment_count", "DECIMAL(20,9) DEFAULT 0"),
            ("mobile_drainage_power_equipment_count", "DECIMAL(20,9) DEFAULT 0"),
            ("rapid_fire_suppression_equipment_count", "DECIMAL(20,9) DEFAULT 0"),
            ("detection_prospecting_equipment_count", "DECIMAL(20,9) DEFAULT 0"),
            ("rapid_support_equipment_count", "DECIMAL(20,9) DEFAULT 0"),
            ("large_offroad_crane_count", "DECIMAL(20,9) DEFAULT 0"),
            ("mine_tunnel_satcom_command_vehicle_count", "DECIMAL(20,9) DEFAULT 0"),
            ("hazchem_oilgas_team_personnel", "DECIMAL(20,9) DEFAULT 0"),
            ("aerial_ladder_jet_vehicle_count", "DECIMAL(20,9) DEFAULT 0"),
            ("heavy_foam_fire_truck_count", "DECIMAL(20,9) DEFAULT 0"),
            ("foam_tanker_count", "DECIMAL(20,9) DEFAULT 0"),
            ("turbojet_fire_truck_count", "DECIMAL(20,9) DEFAULT 0"),
            ("foam_supply_truck_count", "DECIMAL(20,9) DEFAULT 0"),
            ("dry_powder_fire_truck_count", "DECIMAL(20,9) DEFAULT 0"),
            ("engineering_leak_blocking_vehicle_count", "DECIMAL(20,9) DEFAULT 0"),
            ("breaking_tools_count", "DECIMAL(20,9) DEFAULT 0"),
            ("leak_blocking_tools_count", "DECIMAL(20,9) DEFAULT 0"),
            ("gas_supply_fire_truck_count", "DECIMAL(20,9) DEFAULT 0"),
            ("long_distance_water_supply_vehicle_count", "DECIMAL(20,9) DEFAULT 0"),
            ("aerial_triphase_jet_fire_truck_count", "DECIMAL(20,9) DEFAULT 0"),
            ("chemical_decon_vehicle_count", "DECIMAL(20,9) DEFAULT 0"),
            ("large_flow_trailer_fire_cannon_count", "DECIMAL(20,9) DEFAULT 0"),
            ("hazchem_oilgas_satcom_command_vehicle_count", "DECIMAL(20,9) DEFAULT 0"),
            ("mine_tunnel_enterprise_count", "DECIMAL(20,9) DEFAULT 0"),
            ("hazchem_oilgas_enterprise_count", "DECIMAL(20,9) DEFAULT 0"),
            ("maritime_rescue_team_personnel", "DECIMAL(20,9) DEFAULT 0"),
            ("inflatable_boat_count", "DECIMAL(20,9) DEFAULT 0"),
            ("assault_boat_count", "DECIMAL(20,9) DEFAULT 0"),
            ("salvage_ship_count", "DECIMAL(20,9) DEFAULT 0"),
            ("maritime_rescue_helicopter_count", "DECIMAL(20,9) DEFAULT 0"),
            ("inflatable_board_count", "DECIMAL(20,9) DEFAULT 0"),
            ("water_robot_count", "DECIMAL(20,9) DEFAULT 0"),
            ("drone_count", "DECIMAL(20,9) DEFAULT 0"),
            ("health_technicians_total", "DECIMAL(20,9) DEFAULT 0"),
            ("transport_ambulance_count", "DECIMAL(20,9) DEFAULT 0"),
            ("monitoring_ambulance_count", "DECIMAL(20,9) DEFAULT 0"),
            ("negative_pressure_ambulance_count", "DECIMAL(20,9) DEFAULT 0"),
            ("emergency_comm_base_station_count", "DECIMAL(20,9) DEFAULT 0"),
            ("emergency_comm_vehicle_count", "DECIMAL(20,9) DEFAULT 0"),
            ("road_total_mileage", "DECIMAL(20,9) DEFAULT 0"),
        ]
        cur.execute(
            """
            SELECT COLUMN_NAME
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'government_disaster_reduction_capacity_2020'
            """
        )
        existing_columns = {r[0] for r in cur.fetchall()}
        obsolete_columns = [
            "funding_amount",
            "material_value",
            "hospital_beds",
            "firefighters",
            "volunteers",
            "militia_reserve",
            "training_participants",
            "shelter_capacity",
        ]
        drop_columns = [name for name in obsolete_columns if name in existing_columns]
        if drop_columns:
            drop_sql = ", ".join([f"DROP COLUMN {name}" for name in drop_columns])
            cur.execute(f"ALTER TABLE government_disaster_reduction_capacity_2020 {drop_sql}")
            existing_columns = existing_columns - set(drop_columns)
        for name, ddl in extra_columns:
            if name not in existing_columns:
                cur.execute(f"ALTER TABLE government_disaster_reduction_capacity_2020 ADD COLUMN {name} {ddl}")

        cur.execute("DELETE FROM government_disaster_reduction_capacity_2020 WHERE year=2020")
        cur.execute(
            """
            INSERT INTO government_disaster_reduction_capacity_2020 (
                region_code, province_name, city_name, county_name, year,
                township_count, population, total_households, management_staff,
                disaster_info_staff, emergency_plan_count, emergency_response_count,
                training_drill_count, shelter_count, source_record_count
            )
            SELECT
                SUBSTRING(region_code,1,6),
                MAX(province),
                MAX(city),
                MAX(county),
                2020,
                COUNT(DISTINCT township),
                SUM(COALESCE(population,0)),
                SUM(COALESCE(total_households,0)),
                SUM(COALESCE(management_staff,0)),
                SUM(COALESCE(disaster_info_staff,0)),
                SUM(COALESCE(emergency_plan_count,0)),
                SUM(COALESCE(emergency_response_count,0)),
                SUM(COALESCE(training_drill_count,0)),
                SUM(COALESCE(shelter_count,0)),
                COUNT(*)
            FROM survey_data
            WHERE year=2020
              AND city='德阳市'
              AND SUBSTRING(region_code,1,6) IN ('510603','510604','510623','510681','510682','510683')
            GROUP BY SUBSTRING(region_code,1,6)
            ORDER BY SUBSTRING(region_code,1,6)
            """
        )
        authoritative_columns = [
            "region_code", "province_name", "city_name", "county_name",
            "management_staff", "population", "expert_staff_count", "disaster_prevention_plan_count",
            "emergency_plan_count", "education_expenditure", "science_expenditure",
            "agriculture_water_expenditure", "natural_resources_expenditure", "grain_reserve_expenditure",
            "disaster_emergency_expenditure", "regional_gdp", "regional_area",
            "standard_flood_dike_length", "built_flood_dike_length", "reinforced_reservoir_dam_count",
            "reservoir_dam_count", "reinforced_sluice_count", "sluice_count", "geological_hazard_point_count",
            "completed_geological_treatment_count", "seawall_total_length", "coastline_length",
            "forest_fire_project_mileage", "forest_area", "meteorological_station_count",
            "hydrological_station_count", "seismic_station_count", "geological_monitoring_station_count",
            "ocean_monitoring_station_count", "forest_fire_warning_station_count", "effective_storage_capacity",
            "living_material_value", "rescue_material_value", "other_material_value", "firefighters",
            "fire_truck_count", "fire_station_count", "forest_fire_team_personnel", "forest_fire_vehicle_vessel_count",
            "aviation_rescue_team_personnel", "fixed_wing_aircraft_count", "helicopter_count",
            "earthquake_rescue_team_personnel", "detection_equipment_total", "search_equipment_total",
            "rescue_equipment_total", "medical_equipment_total", "communication_equipment_total",
            "information_equipment_total", "logistics_equipment_total", "vehicle_equipment_total",
            "mine_tunnel_rescue_personnel", "drill_machine_count", "drainage_equipment_count",
            "mobile_drainage_power_equipment_count", "rapid_fire_suppression_equipment_count",
            "detection_prospecting_equipment_count", "rapid_support_equipment_count", "large_offroad_crane_count",
            "mine_tunnel_satcom_command_vehicle_count", "hazchem_oilgas_team_personnel",
            "aerial_ladder_jet_vehicle_count", "heavy_foam_fire_truck_count", "foam_tanker_count",
            "turbojet_fire_truck_count", "foam_supply_truck_count", "dry_powder_fire_truck_count",
            "engineering_leak_blocking_vehicle_count", "breaking_tools_count", "leak_blocking_tools_count",
            "gas_supply_fire_truck_count", "long_distance_water_supply_vehicle_count",
            "aerial_triphase_jet_fire_truck_count", "chemical_decon_vehicle_count",
            "large_flow_trailer_fire_cannon_count", "hazchem_oilgas_satcom_command_vehicle_count",
            "mine_tunnel_enterprise_count", "hazchem_oilgas_enterprise_count", "maritime_rescue_team_personnel",
            "inflatable_boat_count", "assault_boat_count", "salvage_ship_count", "maritime_rescue_helicopter_count",
            "inflatable_board_count", "water_robot_count", "drone_count", "hospital_beds",
            "health_technicians_total", "transport_ambulance_count", "monitoring_ambulance_count",
            "negative_pressure_ambulance_count", "emergency_comm_base_station_count",
            "emergency_comm_vehicle_count", "shelter_capacity", "road_total_mileage",
        ]
        authoritative_rows = [
            ["510603", "四川省", "德阳市", "旌阳区", 14, 828189, 14, 2, 25, 0, 0, 6152.6837, 0, 0, 2673.7178, 7087481, 647.9296, 0, 0, 0, 0, 0, 0, 39, 34, 0, 0, 55.78, 86.457016, 2, 3, 0, 8, 0, 0, 43354.06, 758.6955, 85.0649, 186.1975, 41, 13, 2, 0, 0, 8, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 102, 1, 0, 2, 0, 0, 2, 0, 27, 17, 1, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 6948, 7410, 31, 12, 7, 0.011402091, 0, 269161, 1663.538],
            ["510604", "四川省", "德阳市", "罗江区", 108, 209088, 6, 1, 14, 0, 0, 203.443, 228.93, 0, 1696.9321, 1483974, 447.88, 0, 0, 0, 0, 0, 0, 99, 15, 0, 0, 2, 90.00816, 2, 7, 0, 11, 0, 0, 9520, 42.9824, 80.3514, 14.0573, 39, 15, 4, 0, 0, 8, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 102, 1, 0, 2, 0, 0, 2, 0, 27, 17, 1, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 1800, 1091, 18, 5, 1, 0.016496291, 0, 18667, 933.126],
            ["510623", "四川省", "德阳市", "中江县", 422, 946019, 3, 1, 33, 13.5, 84, 21.8, 129.8407, 10.88, 6978.0386, 3912730, 2200.413, 0, 0, 0, 0, 0, 0, 201, 40, 0, 0, 72.5, 633.8112, 5, 12, 1, 27, 0, 0, 10419, 268.025, 129.9233, 380.899, 25, 8, 3, 0, 0, 8, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 102, 1, 0, 2, 0, 0, 2, 0, 27, 17, 1, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 5982, 3723, 24, 6, 1, 0.003352144, 0, 170384, 3541.741],
            ["510681", "四川省", "德阳市", "广汉市", 193, 626132, 38, 1, 23, 10, 0, 3202.71, 101.53, 0, 4062.1321, 4289629, 553.969, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 1.5, 33.291912, 2, 8, 0, 0, 0, 0, 3618, 288.4975, 65.834, 6.84, 42, 9, 4, 0, 0, 8, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 102, 1, 0, 2, 0, 0, 2, 0, 27, 17, 1, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4200, 3607, 30, 7, 1, 0.013459815, 0, 149000, 1509.791],
            ["510682", "四川省", "德阳市", "什邡市", 303, 406775, 16, 1, 17, 0.96, 170.5, 100, 249.81, 0, 4125.4, 3726983, 449.43, 0, 0, 0, 0, 0, 0, 154, 0, 0, 0, 334.85, 411.9448, 2, 13, 0, 0, 0, 23, 2886.58, 254.8468, 6.013, 10.3435, 49, 12, 4, 0, 0, 8, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 102, 1, 0, 2, 0, 0, 2, 0, 27, 17, 1, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3200, 2944, 26, 8, 1, 0.009008563, 0, 62100, 1413.862],
            ["510683", "四川省", "德阳市", "绵竹市", 56, 439958, 6, 7, 46, 47, 1205.57, 14262.51, 93.6909, 0, 1783.63, 3540453, 1059.853, 0, 0, 0, 0, 0, 0, 248, 97, 0, 0, 401.6, 641.79486, 2, 35, 0, 70, 0, 11, 1995, 71.877, 190.36, 5.575, 28, 3, 1, 30, 0, 8, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 102, 1, 0, 2, 0, 0, 2, 0, 27, 17, 1, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4059, 3055, 40, 10, 2, 0.00592554, 0, 181000, 2268.093],
        ]
        cur.execute("SHOW COLUMNS FROM government_disaster_reduction_capacity_2020")
        existing_columns = {item[0] for item in cur.fetchall()}
        if "firefighters" not in existing_columns:
            cur.execute(
                "ALTER TABLE government_disaster_reduction_capacity_2020 "
                "ADD COLUMN firefighters BIGINT DEFAULT 0 COMMENT '消防员数量（人）'"
            )
        if "hospital_beds" not in existing_columns:
            cur.execute(
                "ALTER TABLE government_disaster_reduction_capacity_2020 "
                "ADD COLUMN hospital_beds BIGINT DEFAULT 0 COMMENT '实有住院床位数（张）'"
            )
        if "shelter_capacity" not in existing_columns:
            cur.execute(
                "ALTER TABLE government_disaster_reduction_capacity_2020 "
                "ADD COLUMN shelter_capacity DECIMAL(20,6) DEFAULT 0 COMMENT '避难所容纳人数（人）'"
            )
        for row in authoritative_rows:
            if len(row) != len(authoritative_columns):
                raise ValueError("权威数据长度与列定义不一致")

        insert_columns = ["year"] + authoritative_columns
        placeholders = ",".join(["%s"] * len(insert_columns))
        updates = ",".join([f"{column}=VALUES({column})" for column in authoritative_columns] + ["source_record_count=1"])
        upsert_sql = (
            f"INSERT INTO government_disaster_reduction_capacity_2020 ({','.join(insert_columns)}) "
            f"VALUES ({placeholders}) ON DUPLICATE KEY UPDATE {updates}"
        )
        cur.executemany(upsert_sql, [[2020] + row for row in authoritative_rows])

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
                    "政府减灾能力评估（2020）",
                    "基于2020年德阳市区县原始调查数据的政府减灾能力评估模型",
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
                    "政府减灾能力评估（2020）",
                    model_code,
                    "基于2020年德阳市区县原始调查数据的政府减灾能力评估模型",
                    "2020.1",
                    "trae",
                    "trae",
                ),
            )
            model_id = cur.lastrowid

        indicator_source_columns = [
            column
            for column in authoritative_columns
            if column not in {"region_code", "province_name", "city_name", "county_name"}
        ]
        step1_column_label_map = {
            "management_staff": "灾害管理人员总数（人）",
            "population": "区域总人口（人）",
            "expert_staff_count": "正式聘用的专家队伍人员总数（人）",
            "disaster_prevention_plan_count": "2016年（含）以来制定的防灾减灾规划数量（个）",
            "emergency_plan_count": "灾害相关预案总数（个）",
            "education_expenditure": "上一年度教育支出（万元）",
            "science_expenditure": "上一年度科学技术支出（万元）",
            "agriculture_water_expenditure": "上一年度农林水支出（万元）",
            "natural_resources_expenditure": "上一年度自然资源海洋气象等支出（万元）",
            "grain_reserve_expenditure": "上一年度粮油物资储备支出（万元）",
            "disaster_emergency_expenditure": "上一年度灾害防治及应急支出（万元）",
            "regional_gdp": "区域GDP（万元）",
            "regional_area": "区域总面积（平方公里）",
            "standard_flood_dike_length": "已达标防洪堤长度（公里）",
            "built_flood_dike_length": "已建成防洪堤长度（公里）",
            "reinforced_reservoir_dam_count": "除险加固水库（水电站）大坝数量（个）",
            "reservoir_dam_count": "水库（水电站）大坝数量（个）",
            "reinforced_sluice_count": "除险加固水闸工程数量（个）",
            "sluice_count": "水闸工程数量（个）",
            "geological_hazard_point_count": "地质灾害隐患点数量（个）",
            "completed_geological_treatment_count": "已完成的地质防治点数量（个/处）",
            "seawall_total_length": "海堤工程总长度（公里）",
            "coastline_length": "区域海岸线（公里）",
            "forest_fire_project_mileage": "林区防火工程的总里程数（公里）",
            "forest_area": "区域林地面积（平方公里）",
            "meteorological_station_count": "气象站点总数（个）",
            "hydrological_station_count": "水文测站总数（个）",
            "seismic_station_count": "地震台网监测站点总数（个）",
            "geological_monitoring_station_count": "地质灾害监测站点总数（个/处/项）",
            "ocean_monitoring_station_count": "海洋灾害监测站点总数（个）",
            "forest_fire_warning_station_count": "林草防火监测预警站总数（个）",
            "effective_storage_capacity": "有效库容（立方米）",
            "living_material_value": "生活类物资折合金额（万元）",
            "rescue_material_value": "救援类物资折合金额（万元）",
            "other_material_value": "其他物资折合金额（万元）",
            "firefighters": "消防员数量（人）",
            "fire_truck_count": "消防车数量（辆）",
            "fire_station_count": "消防站数量（个）",
            "forest_fire_team_personnel": "森林消防队伍总人数（人）",
            "forest_fire_vehicle_vessel_count": "森林防火车（船）数量（辆/艘）",
            "aviation_rescue_team_personnel": "航空救援队伍总人数（人）",
            "fixed_wing_aircraft_count": "航空护林固定翼飞机数量（架）",
            "helicopter_count": "航空护林直升机数量（架）",
            "earthquake_rescue_team_personnel": "地震救援队伍总人数（人）",
            "detection_equipment_total": "侦检类装备总数量（台）",
            "search_equipment_total": "搜索类装备（含搜救犬）总数量（套/只）",
            "rescue_equipment_total": "营救类装备总数量（套）",
            "medical_equipment_total": "医疗类装备总数量（套）",
            "communication_equipment_total": "通讯类装备总数量（套）",
            "information_equipment_total": "信息类装备总数量（台/套）",
            "logistics_equipment_total": "后勤类装备总数量（顶/套）",
            "vehicle_equipment_total": "车辆类装备总数量（辆）",
            "mine_tunnel_rescue_personnel": "矿山隧道救援总人数（人）",
            "drill_machine_count": "钻机数量（台）",
            "drainage_equipment_count": "排水装备数量（台）",
            "mobile_drainage_power_equipment_count": "可移动排水供电装备数量（台）",
            "rapid_fire_suppression_equipment_count": "快速灭火装备数量（台）",
            "detection_prospecting_equipment_count": "检测探测装备数量（台/种）",
            "rapid_support_equipment_count": "快速支护装备数量（台）",
            "large_offroad_crane_count": "大型越野起重装备（≥10t）数量（台）",
            "mine_tunnel_satcom_command_vehicle_count": "矿山隧道卫星通讯指挥车数量（辆）",
            "hazchem_oilgas_team_personnel": "危化油气队伍总人数（人）",
            "aerial_ladder_jet_vehicle_count": "举高喷射车数量（辆）",
            "heavy_foam_fire_truck_count": "重型泡沫消防车数量（辆）",
            "foam_tanker_count": "泡沫水罐车数量（辆）",
            "turbojet_fire_truck_count": "涡喷消防车数量（辆）",
            "foam_supply_truck_count": "泡沫补给车数量（辆）",
            "dry_powder_fire_truck_count": "干粉消防车数量（辆）",
            "engineering_leak_blocking_vehicle_count": "工程抢险堵漏车数量（辆）",
            "breaking_tools_count": "破拆器材数量（台）",
            "leak_blocking_tools_count": "堵漏器材数量（台）",
            "gas_supply_fire_truck_count": "供气消防车数量（辆）",
            "long_distance_water_supply_vehicle_count": "远程供水车数量（辆）",
            "aerial_triphase_jet_fire_truck_count": "举高三相射流消防车数量（辆）",
            "chemical_decon_vehicle_count": "化学洗消车数量（辆）",
            "large_flow_trailer_fire_cannon_count": "大流量拖车消防炮数量（辆）",
            "hazchem_oilgas_satcom_command_vehicle_count": "危化油气卫星通讯指挥车数量（辆）",
            "mine_tunnel_enterprise_count": "矿山/隧道企业数量（个）",
            "hazchem_oilgas_enterprise_count": "危化/油气企业数量（个）",
            "maritime_rescue_team_personnel": "海事救援队伍总人数（人）",
            "inflatable_boat_count": "橡皮艇/充气船（条）",
            "assault_boat_count": "冲锋舟（条）",
            "salvage_ship_count": "打捞船（条）",
            "maritime_rescue_helicopter_count": "海事救援直升机数量（架）",
            "inflatable_board_count": "充气式浮板（块）",
            "water_robot_count": "水上机器人（台）",
            "drone_count": "无人飞机（架）",
            "hospital_beds": "实有住院床位数（张）",
            "health_technicians_total": "卫生技术人员总数（人）",
            "transport_ambulance_count": "运转型急救车数量（辆）",
            "monitoring_ambulance_count": "监护型急救车数量（辆）",
            "negative_pressure_ambulance_count": "负压急救车数量（辆）",
            "emergency_comm_base_station_count": "应急通讯基站总数（个）",
            "emergency_comm_vehicle_count": "应急通讯车数量（辆）",
            "shelter_capacity": "避难所容纳人数（人）",
            "road_total_mileage": "道路总里程（公里）",
        }
        step1_output_variables = ["indicator_values"] + indicator_source_columns
        secondary_indicator_defs = [
            ("管理队伍人数", "management_staff_count", "management_staff"),
            ("专家队伍人数", "expert_staff_total", "expert_staff_count"),
            ("防灾减灾规划", "disaster_prevention_plan_total", "disaster_prevention_plan_count"),
            ("应急预案数量", "emergency_plan_total", "emergency_plan_count"),
            ("防灾减灾投入", "disaster_investment_ratio", "(regional_gdp == 0 ? 0 : (education_expenditure + science_expenditure + agriculture_water_expenditure + natural_resources_expenditure + grain_reserve_expenditure + disaster_emergency_expenditure) / regional_gdp)"),
            ("工程防洪能力", "flood_control_capability", "((standard_flood_dike_length == 0 ? 0 : built_flood_dike_length / standard_flood_dike_length) + (reservoir_dam_count == 0 ? 0 : reinforced_reservoir_dam_count / reservoir_dam_count) + (sluice_count == 0 ? 0 : reinforced_sluice_count / sluice_count)) / 3"),
            ("地质灾害的防治工程比例", "geo_treatment_ratio", "(geological_hazard_point_count == 0 ? 0 : completed_geological_treatment_count / geological_hazard_point_count)"),
            ("海堤工程长度比例", "seawall_length_ratio", "(coastline_length == 0 ? 0 : seawall_total_length / coastline_length)"),
            ("林区防火阻隔和防火道路网密度", "forest_firebreak_density", "(forest_area == 0 ? 0 : forest_fire_project_mileage / forest_area)"),
            ("气象站点密度", "meteorological_station_density", "(regional_area == 0 ? 0 : meteorological_station_count / regional_area)"),
            ("水文站点密度", "hydrological_station_density", "(regional_area == 0 ? 0 : hydrological_station_count / regional_area)"),
            ("地震台网监测能力", "seismic_monitoring_capacity", "(regional_area == 0 ? 0 : seismic_station_count / regional_area)"),
            ("地质灾害监测站点密度", "geological_monitoring_density", "(geological_hazard_point_count == 0 ? 0 : geological_monitoring_station_count / geological_hazard_point_count)"),
            ("海洋灾害监测站点密度", "ocean_monitoring_density", "(coastline_length == 0 ? 0 : ocean_monitoring_station_count / coastline_length)"),
            ("林草防火监测预警站点密度", "forest_warning_density", "(forest_area == 0 ? 0 : forest_fire_warning_station_count / forest_area)"),
            ("人均储备库容率", "per_capita_storage_ratio", "(population == 0 ? 0 : effective_storage_capacity / population * 10000)"),
            ("人均救援物资储备率", "per_capita_rescue_material_ratio", "(population == 0 ? 0 : (living_material_value + rescue_material_value + other_material_value) / population)"),
            ("综合消防救援能力", "comprehensive_fire_rescue_capability", "((population == 0 ? 0 : firefighters / population * 10000) + (population == 0 ? 0 : fire_station_count / population * 10000) + (population == 0 ? 0 : fire_truck_count / population * 10000)) / 3"),
            ("森林消防救援能力", "forest_fire_rescue_capability", "((population == 0 ? 0 : forest_fire_team_personnel / population * 10000) + (population == 0 ? 0 : forest_fire_vehicle_vessel_count / population * 10000)) / 2"),
            ("航空护林能力", "aviation_forest_rescue_capability", "((population == 0 ? 0 : aviation_rescue_team_personnel / population * 10000) + (population == 0 ? 0 : (fixed_wing_aircraft_count + helicopter_count) / population * 10000)) / 2"),
            ("地震救援能力", "earthquake_rescue_capability", "((population == 0 ? 0 : earthquake_rescue_team_personnel / population * 10000) + (population == 0 ? 0 : (detection_equipment_total + search_equipment_total + rescue_equipment_total + medical_equipment_total + communication_equipment_total + information_equipment_total + logistics_equipment_total + vehicle_equipment_total) / population * 10000)) / 2"),
            ("矿山/隧道救援能力", "mine_tunnel_rescue_capability", "((mine_tunnel_enterprise_count == 0 ? 0 : mine_tunnel_rescue_personnel / mine_tunnel_enterprise_count) + (mine_tunnel_enterprise_count == 0 ? 0 : (drill_machine_count + drainage_equipment_count + mobile_drainage_power_equipment_count + rapid_fire_suppression_equipment_count + detection_prospecting_equipment_count + rapid_support_equipment_count + large_offroad_crane_count + mine_tunnel_satcom_command_vehicle_count) / mine_tunnel_enterprise_count)) / 2"),
            ("危化/油气救援能力", "hazchem_oilgas_rescue_capability", "((hazchem_oilgas_enterprise_count == 0 ? 0 : hazchem_oilgas_team_personnel / hazchem_oilgas_enterprise_count) + (hazchem_oilgas_enterprise_count == 0 ? 0 : (aerial_ladder_jet_vehicle_count + heavy_foam_fire_truck_count + foam_tanker_count + turbojet_fire_truck_count + foam_supply_truck_count + dry_powder_fire_truck_count + engineering_leak_blocking_vehicle_count + breaking_tools_count + leak_blocking_tools_count + gas_supply_fire_truck_count + long_distance_water_supply_vehicle_count + aerial_triphase_jet_fire_truck_count + chemical_decon_vehicle_count + large_flow_trailer_fire_cannon_count + hazchem_oilgas_satcom_command_vehicle_count) / hazchem_oilgas_enterprise_count)) / 2"),
            ("海事救援能力", "maritime_rescue_capability", "((population == 0 ? 0 : maritime_rescue_team_personnel / population * 10000) + (population == 0 ? 0 : (inflatable_boat_count + assault_boat_count + salvage_ship_count + maritime_rescue_helicopter_count + inflatable_board_count + water_robot_count + drone_count) / population * 10000)) / 2"),
            ("医疗救援能力", "medical_rescue_capability", "((population == 0 ? 0 : hospital_beds / population * 10000) + (population == 0 ? 0 : health_technicians_total / population * 10000) + (population == 0 ? 0 : (transport_ambulance_count + monitoring_ambulance_count + negative_pressure_ambulance_count) / population * 10000)) / 3"),
            ("应急通信能力", "emergency_communication_capability", "((regional_area == 0 ? 0 : emergency_comm_base_station_count / regional_area) + (population == 0 ? 0 : emergency_comm_vehicle_count / population * 10000)) / 2"),
            ("应急避难场所容纳率", "shelter_capacity_rate", "(population == 0 ? 0 : shelter_capacity / population)"),
            ("路网密度", "road_network_density", "(regional_area == 0 ? 0 : road_total_mileage / regional_area)"),
        ]
        secondary_output_variables = [item[1] for item in secondary_indicator_defs]
        secondary_weight_mapping = {
            "management_staff_count": 0.20,
            "expert_staff_total": 0.18,
            "disaster_prevention_plan_total": 0.20,
            "emergency_plan_total": 0.20,
            "disaster_investment_ratio": 0.22,
            "flood_control_capability": 0.35,
            "geo_treatment_ratio": 0.36,
            "seawall_length_ratio": 0.00,
            "forest_firebreak_density": 0.29,
            "meteorological_station_density": 0.21,
            "hydrological_station_density": 0.20,
            "seismic_monitoring_capacity": 0.19,
            "geological_monitoring_density": 0.22,
            "ocean_monitoring_density": 0.00,
            "forest_warning_density": 0.18,
            "per_capita_storage_ratio": 0.47,
            "per_capita_rescue_material_ratio": 0.53,
            "comprehensive_fire_rescue_capability": 0.15,
            "forest_fire_rescue_capability": 0.12,
            "aviation_forest_rescue_capability": 0.19,
            "earthquake_rescue_capability": 0.13,
            "mine_tunnel_rescue_capability": 0.10,
            "hazchem_oilgas_rescue_capability": 0.13,
            "maritime_rescue_capability": 0.00,
            "medical_rescue_capability": 0.14,
            "emergency_communication_capability": 0.14,
            "shelter_capacity_rate": 0.49,
            "road_network_density": 0.51,
        }
        primary_capability_defs = [
            ("管理能力", "management_capability", "management_staff_count,expert_staff_total,disaster_prevention_plan_total,emergency_plan_total,disaster_investment_ratio"),
            ("工程设防能力", "engineering_defense_capability", "flood_control_capability,geo_treatment_ratio,seawall_length_ratio,forest_firebreak_density"),
            ("监测预警能力", "monitoring_warning_capability", "meteorological_station_density,hydrological_station_density,seismic_monitoring_capacity,geological_monitoring_density,ocean_monitoring_density,forest_warning_density"),
            ("物资储备能力", "material_reserve_capability", "per_capita_storage_ratio,per_capita_rescue_material_ratio"),
            ("专业队伍救援能力", "professional_rescue_capability", "comprehensive_fire_rescue_capability,forest_fire_rescue_capability,aviation_forest_rescue_capability,earthquake_rescue_capability,mine_tunnel_rescue_capability,hazchem_oilgas_rescue_capability,maritime_rescue_capability,medical_rescue_capability,emergency_communication_capability"),
            ("转移安置能力", "relocation_resettlement_capability", "shelter_capacity_rate,road_network_density"),
        ]
        primary_output_variables = [item[1] for item in primary_capability_defs]
        step_defs = [
            ("评估指标赋值", "indicator_assignment", 1, "CALCULATION", "将原始调查字段映射为评估指标值", "[]", json.dumps(step1_output_variables, ensure_ascii=False)),
            ("二级指标赋值", "secondary_indicator_assignment", 2, "CALCULATION", "按照二级指标口径聚合评估指标", "[\"indicator_values\"]", json.dumps(secondary_output_variables, ensure_ascii=False)),
            ("属性向量归一化", "attribute_vector_normalization", 3, "NORMALIZATION", "对二级指标进行向量归一化", json.dumps(secondary_output_variables, ensure_ascii=False), json.dumps(secondary_output_variables, ensure_ascii=False)),
            ("二级指标定权", "secondary_indicator_weighting", 4, "WEIGHTING", "归一化结果与二级指标权重相乘", json.dumps(secondary_output_variables, ensure_ascii=False), json.dumps(secondary_output_variables, ensure_ascii=False)),
            ("D+与D-", "distance_to_ideal", 5, "TOPSIS", "计算与正负理想解距离", json.dumps(secondary_output_variables, ensure_ascii=False), json.dumps([f"{item[1]}_d_plus" for item in primary_capability_defs] + [f"{item[1]}_d_minus" for item in primary_capability_defs], ensure_ascii=False)),
            ("一级指标能力值", "primary_indicator_value", 6, "CALCULATION", "根据D+和D-计算一级指标能力值", json.dumps([f"{item[1]}_d_plus" for item in primary_capability_defs] + [f"{item[1]}_d_minus" for item in primary_capability_defs], ensure_ascii=False), json.dumps(primary_output_variables, ensure_ascii=False)),
            ("一级指标能力分级", "primary_indicator_level", 7, "GRADING", "按阈值将一级指标能力值映射为等级", json.dumps(primary_output_variables, ensure_ascii=False), json.dumps(primary_output_variables, ensure_ascii=False)),
        ]

        inserted_step_ids = []
        step_id_by_order = {}
        for name, code, order_no, step_type, desc, in_vars, out_vars in step_defs:
            cur.execute(
                """
                INSERT INTO model_step (
                    model_id, step_name, step_code, step_order, step_type, description,
                    input_variables, output_variables, depends_on, status
                ) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,'',1)
                """,
                (model_id, name, code, order_no, step_type, desc, in_vars, out_vars),
            )
            inserted_step_ids.append(cur.lastrowid)
            step_id_by_order[order_no] = cur.lastrowid

        for i in range(1, len(inserted_step_ids)):
            cur.execute(
                "UPDATE model_step SET depends_on=%s WHERE id=%s",
                (str(inserted_step_ids[i - 1]), inserted_step_ids[i]),
            )

        algorithms_by_step_order = {
            1: [
                ("评估指标赋值公式", "EL_INDICATOR_ASSIGNMENT", 1, "(management_staff + disaster_info_staff + emergency_plan_count + emergency_response_count + training_drill_count)", "{\"source\":\"government_disaster_reduction_capacity_2020\"}", "indicator_values", "按字段映射输出评估指标"),
            ],
            2: [
                *[
                    (f"{label}公式", f"EL_SECONDARY_{index:03d}", index, expression, "{\"required\":[\"indicator_values\"]}", output_param, f"二级指标赋值:{label}")
                    for index, (label, output_param, expression) in enumerate(secondary_indicator_defs, start=1)
                ],
            ],
            3: [
                *[
                    (f"{label}归一化", f"EL_NORMALIZE_{index:03d}", index, f"@NORMALIZE:{output_param}", "{\"required\":[\"secondary_indicator_values\"]}", output_param, f"归一化:{label}")
                    for index, (label, output_param, _) in enumerate(secondary_indicator_defs, start=1)
                ],
            ],
            4: [
                *[
                    (f"{label}定权", f"EL_WEIGHTED_{index:03d}", index, f"({output_param} * {secondary_weight_mapping.get(output_param, 1.0)})", "{\"required\":[\"normalized_vector\"]}", output_param, f"定权:{label}")
                    for index, (label, output_param, _) in enumerate(secondary_indicator_defs, start=1)
                ],
            ],
            5: [
                *[
                    (f"{label}D+公式", f"EL_D_PLUS_{index:03d}", index * 2 - 1, f"@TOPSIS_POSITIVE:{indicators}", "{\"required\":[\"weighted_matrix\"]}", f"{output_param}_d_plus", f"计算{label}正理想解距离")
                    for index, (label, output_param, indicators) in enumerate(primary_capability_defs, start=1)
                ],
                *[
                    (f"{label}D-公式", f"EL_D_MINUS_{index:03d}", index * 2, f"@TOPSIS_NEGATIVE:{indicators}", "{\"required\":[\"weighted_matrix\"]}", f"{output_param}_d_minus", f"计算{label}负理想解距离")
                    for index, (label, output_param, indicators) in enumerate(primary_capability_defs, start=1)
                ],
            ],
            6: [
                *[
                    (f"{label}能力值公式", f"EL_PRIMARY_SCORE_{index:03d}", index, f"@TOPSIS_SCORE:{output_param}_d_plus,{output_param}_d_minus", "{\"required\":[\"d_plus\",\"d_minus\"]}", output_param, f"计算{label}能力值")
                    for index, (label, output_param, _) in enumerate(primary_capability_defs, start=1)
                ],
            ],
            7: [
                *[
                    (f"{label}分级公式", f"EL_PRIMARY_GRADE_{index:03d}", index, f"@GRADE:{output_param}", "{\"required\":[\"primary_indicator_values\"]}", output_param, f"{label}能力分级")
                    for index, (label, output_param, _) in enumerate(primary_capability_defs, start=1)
                ],
            ],
        }

        for index, column in enumerate(indicator_source_columns, start=2):
            display_name = step1_column_label_map.get(column, column)
            algorithms_by_step_order[1].append(
                (
                    display_name,
                    f"EL_INDICATOR_COL_{index - 1:03d}",
                    index,
                    column,
                    "{\"source\":\"government_disaster_reduction_capacity_2020\"}",
                    column,
                    f"步骤1字段映射:{column}",
                )
            )

        for step_order, algorithm_defs in algorithms_by_step_order.items():
            step_id = step_id_by_order[step_order]
            for item in algorithm_defs:
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

        cur.execute("SELECT COUNT(*) FROM government_disaster_reduction_capacity_2020 WHERE year=2020")
        data_count = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM model_step WHERE model_id=%s", (model_id,))
        step_count = cur.fetchone()[0]
        cur.execute(
            "SELECT COUNT(*) FROM step_algorithm WHERE step_id IN (SELECT id FROM model_step WHERE model_id=%s)",
            (model_id,),
        )
        algorithm_count = cur.fetchone()[0]

        print(f"backup_suffix={backup_suffix}")
        print(f"model_id={model_id}")
        print(f"government_data_count={data_count}")
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
