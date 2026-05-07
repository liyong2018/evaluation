package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/government-capacity")
public class GovernmentDisasterReductionCapacityController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "government_disaster_reduction_capacity_2020";

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) Integer year) {
        try {
            int currentPage = page == null || page < 1 ? 1 : page;
            int pageSize = size == null || size < 1 ? 20 : Math.min(size, 500);

            StringBuilder where = new StringBuilder(" WHERE 1=1 ");
            List<Object> params = new ArrayList<>();
            if (year != null) {
                where.append(" AND year = ? ");
                params.add(year);
            }
            if (StringUtils.hasText(orgCode)) {
                where.append(" AND region_code LIKE ? ");
                params.add(orgCode.trim() + "%");
            }

            List<String> columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION",
                    new Object[]{TABLE_NAME},
                    String.class
            );
            List<String> preferredColumns = buildDisplayColumns();
            List<String> dataColumns = preferredColumns.stream()
                    .filter(columns::contains)
                    .collect(Collectors.toList());

            Long total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM " + TABLE_NAME + where,
                    params.toArray(),
                    Long.class
            );

            String selectColumns = dataColumns.stream()
                    .map(column -> column + " AS " + toCamelCase(column))
                    .collect(Collectors.joining(", "));

            List<Object> listParams = new ArrayList<>(params);
            listParams.add((currentPage - 1) * pageSize);
            listParams.add(pageSize);

            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                    "SELECT " + selectColumns + " FROM " + TABLE_NAME + where +
                            " ORDER BY region_code ASC LIMIT ?, ?",
                    listParams.toArray()
            );

            Map<String, String> labelMap = buildColumnLabelMap();
            List<Map<String, Object>> columnDefs = new ArrayList<>();
            for (String column : dataColumns) {
                String prop = toCamelCase(column);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("prop", prop);
                item.put("label", labelMap.getOrDefault(column, column));
                columnDefs.add(item);
            }

            Map<String, Object> result = new HashMap<>();
            long safeTotal = total == null ? 0L : total;
            result.put("records", records);
            result.put("columns", columnDefs);
            result.put("total", safeTotal);
            result.put("current", currentPage);
            result.put("pages", pageSize == 0 ? 0 : (safeTotal + pageSize - 1) / pageSize);
            result.put("size", pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询政府减灾能力数据失败", e);
            return Result.error("查询政府减灾能力数据失败: " + e.getMessage());
        }
    }

    private String toCamelCase(String value) {
        if (!StringUtils.hasText(value) || !value.contains("_")) {
            return value;
        }
        String[] parts = value.split("_");
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!StringUtils.hasText(parts[i])) {
                continue;
            }
            builder.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        return builder.toString();
    }

    private Map<String, String> buildColumnLabelMap() {
        Map<String, String> map = new HashMap<>();
        map.put("region_code", "行政区代码");
        map.put("province_name", "省名称");
        map.put("city_name", "市名称");
        map.put("county_name", "县名称");
        map.put("management_staff", "灾害管理人员总数（人）");
        map.put("population", "区域总人口（人）");
        map.put("expert_staff_count", "正式聘用的专家队伍人员总数（人）");
        map.put("disaster_prevention_plan_count", "2016年（含）以来制定的防灾减灾规划数量（个）");
        map.put("emergency_plan_count", "灾害相关预案总数（个）");
        map.put("education_expenditure", "上一年度教育支出（万元）");
        map.put("science_expenditure", "上一年度科学技术支出（万元）");
        map.put("agriculture_water_expenditure", "上一年度农林水支出（万元）");
        map.put("natural_resources_expenditure", "上一年度自然资源海洋气象等支出（万元）");
        map.put("grain_reserve_expenditure", "上一年度粮油物资储备支出（万元）");
        map.put("disaster_emergency_expenditure", "上一年度灾害防治及应急支出（万元）");
        map.put("regional_gdp", "区域GDP（万元）");
        map.put("regional_area", "区域总面积（平方公里）");
        map.put("standard_flood_dike_length", "已达标防洪堤长度（公里）");
        map.put("built_flood_dike_length", "已建成防洪堤长度（公里）");
        map.put("reinforced_reservoir_dam_count", "除险加固水库（水电站）大坝数量（个）");
        map.put("reservoir_dam_count", "水库（水电站）大坝数量（个）");
        map.put("reinforced_sluice_count", "除险加固水闸工程数量（个）");
        map.put("sluice_count", "水闸工程数量（个）");
        map.put("geological_hazard_point_count", "地质灾害隐患点数量（个）");
        map.put("completed_geological_treatment_count", "已完成的地质防治点数量（个/处）");
        map.put("seawall_total_length", "海堤工程总长度（公里）");
        map.put("coastline_length", "区域海岸线（公里）");
        map.put("forest_fire_project_mileage", "林区防火工程的总里程数（公里）");
        map.put("forest_area", "区域林地面积（平方公里）");
        map.put("meteorological_station_count", "气象站点总数（个）");
        map.put("hydrological_station_count", "水文测站总数（个）");
        map.put("seismic_station_count", "地震台网监测站点总数（个）");
        map.put("geological_monitoring_station_count", "地质灾害监测站点总数（个/处/项）");
        map.put("ocean_monitoring_station_count", "海洋灾害监测站点总数（个）");
        map.put("forest_fire_warning_station_count", "林草防火监测预警站总数（个）");
        map.put("effective_storage_capacity", "有效库容（立方米）");
        map.put("living_material_value", "生活类物资折合金额（万元）");
        map.put("rescue_material_value", "救援类物资折合金额（万元）");
        map.put("other_material_value", "其他物资折合金额（万元）");
        map.put("fire_truck_count", "消防车数量（辆）");
        map.put("fire_station_count", "消防站数量（个）");
        map.put("forest_fire_team_personnel", "森林消防队伍总人数（人）");
        map.put("forest_fire_vehicle_vessel_count", "森林防火车（船）数量（辆/艘）");
        map.put("aviation_rescue_team_personnel", "航空救援队伍总人数（人）");
        map.put("fixed_wing_aircraft_count", "航空护林固定翼飞机数量（架）");
        map.put("helicopter_count", "航空护林直升机数量（架）");
        map.put("earthquake_rescue_team_personnel", "地震救援队伍总人数（人）");
        map.put("detection_equipment_total", "侦检类装备总数量（台）");
        map.put("search_equipment_total", "搜索类装备（含搜救犬）总数量（套/只）");
        map.put("rescue_equipment_total", "营救类装备总数量（套）");
        map.put("medical_equipment_total", "医疗类装备总数量（套）");
        map.put("communication_equipment_total", "通讯类装备总数量（套）");
        map.put("information_equipment_total", "信息类装备总数量（台/套）");
        map.put("logistics_equipment_total", "后勤类装备总数量（顶/套）");
        map.put("vehicle_equipment_total", "车辆类装备总数量（辆）");
        map.put("mine_tunnel_rescue_personnel", "矿山隧道救援总人数（人）");
        map.put("drill_machine_count", "钻机数量（台）");
        map.put("drainage_equipment_count", "排水装备数量（台）");
        map.put("mobile_drainage_power_equipment_count", "可移动排水供电装备数量（台）");
        map.put("rapid_fire_suppression_equipment_count", "快速灭火装备数量（台）");
        map.put("detection_prospecting_equipment_count", "检测探测装备数量（台/种）");
        map.put("rapid_support_equipment_count", "快速支护装备数量（台）");
        map.put("large_offroad_crane_count", "大型越野起重装备（≥10t）数量（台）");
        map.put("mine_tunnel_satcom_command_vehicle_count", "矿山隧道卫星通讯指挥车数量（辆）");
        map.put("hazchem_oilgas_team_personnel", "危化油气队伍总人数（人）");
        map.put("aerial_ladder_jet_vehicle_count", "举高喷射车数量（辆）");
        map.put("heavy_foam_fire_truck_count", "重型泡沫消防车数量（辆）");
        map.put("foam_tanker_count", "泡沫水罐车数量（辆）");
        map.put("turbojet_fire_truck_count", "涡喷消防车数量（辆）");
        map.put("foam_supply_truck_count", "泡沫补给车数量（辆）");
        map.put("dry_powder_fire_truck_count", "干粉消防车数量（辆）");
        map.put("engineering_leak_blocking_vehicle_count", "工程抢险堵漏车数量（辆）");
        map.put("breaking_tools_count", "破拆器材数量（台）");
        map.put("leak_blocking_tools_count", "堵漏器材数量（台）");
        map.put("gas_supply_fire_truck_count", "供气消防车数量（辆）");
        map.put("long_distance_water_supply_vehicle_count", "远程供水车数量（辆）");
        map.put("aerial_triphase_jet_fire_truck_count", "举高三相射流消防车数量（辆）");
        map.put("chemical_decon_vehicle_count", "化学洗消车数量（辆）");
        map.put("large_flow_trailer_fire_cannon_count", "大流量拖车消防炮数量（辆）");
        map.put("hazchem_oilgas_satcom_command_vehicle_count", "危化油气卫星通讯指挥车数量（辆）");
        map.put("mine_tunnel_enterprise_count", "矿山/隧道企业数量（个）");
        map.put("hazchem_oilgas_enterprise_count", "危化/油气企业数量（个）");
        map.put("maritime_rescue_team_personnel", "海事救援队伍总人数（人）");
        map.put("inflatable_boat_count", "橡皮艇/充气船（条）");
        map.put("assault_boat_count", "冲锋舟（条）");
        map.put("salvage_ship_count", "打捞船（条）");
        map.put("maritime_rescue_helicopter_count", "海事救援直升机数量（架）");
        map.put("inflatable_board_count", "充气式浮板（块）");
        map.put("water_robot_count", "水上机器人（台）");
        map.put("drone_count", "无人飞机（架）");
        map.put("health_technicians_total", "卫生技术人员总数（人）");
        map.put("transport_ambulance_count", "运转型急救车数量（辆）");
        map.put("monitoring_ambulance_count", "监护型急救车数量（辆）");
        map.put("negative_pressure_ambulance_count", "负压急救车数量（辆）");
        map.put("emergency_comm_base_station_count", "应急通讯基站总数（个）");
        map.put("emergency_comm_vehicle_count", "应急通讯车数量（辆）");
        map.put("road_total_mileage", "道路总里程（公里）");
        return map;
    }

    private List<String> buildDisplayColumns() {
        return Arrays.asList(
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
                "living_material_value", "rescue_material_value", "other_material_value",
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
                "inflatable_board_count", "water_robot_count", "drone_count",
                "health_technicians_total", "transport_ambulance_count", "monitoring_ambulance_count",
                "negative_pressure_ambulance_count", "emergency_comm_base_station_count",
                "emergency_comm_vehicle_count", "road_total_mileage"
        );
    }

    // ====================== CRUD ======================

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            int rows = jdbcTemplate.update("DELETE FROM " + TABLE_NAME + " WHERE id = ?", id);
            return Result.success(rows > 0);
        } catch (Exception e) {
            log.error("删除政府减灾能力数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的数据");
            }
            String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
            int rows = jdbcTemplate.update(
                    "DELETE FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ")",
                    ids.toArray());
            return Result.success(rows > 0);
        } catch (Exception e) {
            log.error("批量删除政府减灾能力数据失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-by-year-org")
    public Result<Long> deleteByYearOrg(@RequestParam Integer year,
                                        @RequestParam(required = false) String orgCode) {
        try {
            StringBuilder sql = new StringBuilder("DELETE FROM " + TABLE_NAME + " WHERE year = ?");
            List<Object> params = new ArrayList<>();
            params.add(year);
            if (StringUtils.hasText(orgCode)) {
                sql.append(" AND region_code LIKE ?");
                params.add(orgCode.trim() + "%");
            }
            int rows = jdbcTemplate.update(sql.toString(), params.toArray());
            return Result.success((long) rows);
        } catch (Exception e) {
            log.error("按年份删除政府减灾能力数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "year", required = false) Integer year) {
        log.info("导入政府减灾能力数据，文件名: {}", file != null ? file.getOriginalFilename() : "null");
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return Result.error("Excel文件中没有工作表");
            }

            // 反向映射: label -> column_name
            Map<String, String> labelToColumn = new LinkedHashMap<>();
            Map<String, String> labelMap = buildColumnLabelMap();
            for (Map.Entry<String, String> entry : labelMap.entrySet()) {
                labelToColumn.put(entry.getValue(), entry.getKey());
            }

            // 构建表头列索引映射
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return Result.error("Excel文件中没有表头行");
            }
            Map<Integer, String> colIndexToName = new LinkedHashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String header = ExcelUtil.getCellStringValue(cell);
                if (header != null) {
                    header = header.trim();
                    String colName = labelToColumn.get(header);
                    if (colName != null) {
                        colIndexToName.put(i, colName);
                    }
                }
            }
            if (colIndexToName.isEmpty()) {
                return Result.error("无法识别Excel表头，请检查文件格式");
            }

            int insertCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Map<String, Object> rowValues = new LinkedHashMap<>();
                    for (Map.Entry<Integer, String> entry : colIndexToName.entrySet()) {
                        Cell cell = row.getCell(entry.getKey());
                        String val = ExcelUtil.getCellStringValue(cell);
                        if (val != null && !val.trim().isEmpty()) {
                            rowValues.put(entry.getValue(), val.trim());
                        }
                    }
                    if (rowValues.isEmpty()) continue;

                    // 设置年份
                    if (year != null) {
                        rowValues.put("year", year);
                    }

                    // 插入数据
                    if (!rowValues.containsKey("region_code") || rowValues.get("region_code") == null) {
                        continue;
                    }
                    String columns = String.join(", ", rowValues.keySet());
                    String placeholders = rowValues.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
                    Object[] values = rowValues.values().toArray();
                    jdbcTemplate.update("INSERT INTO " + TABLE_NAME + " (" + columns + ") VALUES (" + placeholders + ")", values);
                    insertCount++;
                } catch (Exception e) {
                    log.error("导入第{}行失败", i + 1, e);
                    errors.add("第" + (i + 1) + "行导入失败: " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("insertCount", insertCount);
            result.put("message", "成功导入" + insertCount + "条数据");
            if (!errors.isEmpty()) {
                result.put("errorMessages", errors);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入政府减灾能力数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode) {
        try {
            StringBuilder where = new StringBuilder(" WHERE 1=1 ");
            List<Object> params = new ArrayList<>();
            if (year != null) {
                where.append(" AND year = ? ");
                params.add(year);
            }
            if (StringUtils.hasText(orgCode)) {
                where.append(" AND region_code LIKE ? ");
                params.add(orgCode.trim() + "%");
            }

            List<String> dataColumns = buildDisplayColumns();
            String selectColumns = String.join(", ", dataColumns);
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                    "SELECT " + selectColumns + " FROM " + TABLE_NAME + where + " ORDER BY region_code ASC",
                    params.toArray());

            Map<String, String> labelMap = buildColumnLabelMap();

            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("政府减灾能力数据");

                // 表头
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < dataColumns.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(labelMap.getOrDefault(dataColumns.get(i), dataColumns.get(i)));
                }

                // 数据行
                for (int r = 0; r < records.size(); r++) {
                    Row dataRow = sheet.createRow(r + 1);
                    Map<String, Object> record = records.get(r);
                    for (int c = 0; c < dataColumns.size(); c++) {
                        Cell cell = dataRow.createCell(c);
                        Object val = record.get(dataColumns.get(c));
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                }

                workbook.write(bos);
                byte[] bytes = bos.toByteArray();
                String fileName = URLEncoder.encode("政府减灾能力数据.xlsx", StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes);
            }
        } catch (Exception e) {
            log.error("导出政府减灾能力数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
