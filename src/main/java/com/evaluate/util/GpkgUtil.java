package com.evaluate.util;

import com.evaluate.dto.GpkgFieldValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * GPKG文件处理工具类
 *
 * @author System
 * @since 2025-01-26
 */
@Slf4j
public class GpkgUtil {

    /**
     * 乡镇评估数据必要字段（GPKG实际字段名）
     */
    private static final Set<String> TOWNSHIP_REQUIRED_FIELDS = new HashSet<>(Arrays.asList(
            "dwmc",            // 乡镇（街道）名称
            "code",            // 行政区划代码
            "nmczrksl"         // 常住人口数量
    ));

    /**
     * 乡镇评估数据推荐字段（GPKG实际字段名）
     */
    private static final Set<String> TOWNSHIP_OPTIONAL_FIELDS = new HashSet<>(Arrays.asList(
            "dzsheng",                       // 省名称
            "dzshi",                         // 市名称
            "dzxian",                        // 县名称
            "dzxiang",                       // 乡名称
            "address",                       // 乡镇（街道）地址
            "dzjh",                          // 地址街号
            "bjzhglgzryzs",                  // 本级灾害管理工作人员总数
            "sfkzxzjdzhfxpg",                // 是否开展乡镇（街道）灾害风险评估
            "syndfzjzjzzjtrzje",             // 上一年度防灾减灾救灾资金投入总金额
            "xycbwzzbzhje",                  // 现有储备物资、装备折合金额
            "syndzzdyjglpyhylcs",            // 上一年度组织的应急管理培训和演练次数
            "syndzzdyjglpyhylcyrc",          // 上一年度组织的应急管理培训和演练参与人次
            "bjzhyjbncssl",                  // 本级灾害应急避难场所数量
            "bjzhyjbncsrl",                  // 本级灾害应急避难场所容量
            "yjgssbsl",                      // 应急供水设备数量
            "yjylsbsl",                      // 应急医疗设备数量
            "yjtxsbsl",                      // 应急通信设备数量
            "yjdyhyjfdsbsl",                 // 其中：应急电源或应急发电设备数量
            "xyjzwzzbsl",                    // 本级储备点救灾物资、装备数量
            "xyjzwzzbcbdsl",                 // 本级救灾物资、装备储备点数量
            "bjzhxxyrs",                     // 本级灾害信息员人数
            "j3nbzhxdzrzhyjyasl",            // 近3年编制或修订自然灾害应急预案数量
            "j3nzdzrzhqdyjxycs",             // 近3年针对自然灾害启动应急响应次数
            "zhs",                           // 年末总户数
            "yxxzjddzyzhlx",                 // 影响乡镇（街道）的主要灾害类型
            "sfyxzjdzhldt"                   // 是否有乡镇（街道）灾害类地图
    ));

    /**
     * 医疗卫生机构数据必要字段（GPKG实际字段名）
     */
    private static final Set<String> MEDICAL_REQUIRED_FIELDS = new HashSet<>(Arrays.asList(
            "dwmc",                  // 医疗卫生机构名称
            "address",               // 医疗卫生机构详细地址
            "dzsheng",               // 地址省
            "dzshi",                 // 地址市
            "dzxian",                // 地址县
            "dzxiang"                // 地址乡
    ));

    /**
     * 医疗卫生机构数据推荐字段（GPKG实际字段名）
     */
    private static final Set<String> MEDICAL_OPTIONAL_FIELDS = new HashSet<>(Arrays.asList(
            "id",                         // 唯一标识
            "code",                       // 行政区划代码
            "fxpc_xzqhbmd_sjgl",         // 行政区划乡-数据管理（备用乡镇代码）
            "dmlx",                       // 代码类型(统一社会信用代码/机构编码)
            "yljglxdl",                   // 医疗机构类型（大类）
            "yydj",                       // 医院等级
            "syzycws",                    // 实有住院床位数
            "zgzgrs"                      // 在岗职工人数
    ));

    /**
     * 社区减灾能力数据必要字段（GPKG实际字段名）
     */
    private static final Set<String> COMMUNITY_REQUIRED_FIELDS = new HashSet<>(Arrays.asList(
            "code",            // 行政区划代码
            "dzsheng",         // 地址省
            "dzshi",           // 地址市
            "dzxian",          // 地址县
            "dzxiang",         // 地址乡
            "dwmc"             // 社区（行政村）名称
    ));

    /**
     * 社区减灾能力数据推荐字段（GPKG实际字段名）
     */
    private static final Set<String> COMMUNITY_OPTIONAL_FIELDS = new HashSet<>(Arrays.asList(
            "dzcun",                       // 地址村
            "nmczrksl",                    // 常住人口数量
            "zero_ss_srs",                 // 其中：0-14岁人数
            "lw_shysrs",                   // 65岁（含）以上人数
            "czryrs",                      // 残障人员人数
            "sqylwsfwzhcwsssl",            // 社区医疗卫生服务站或村卫生室数量
            "sfwqgzhjzsfsq",               // 是否为全国综合减灾示范社区
            "sfwsjzhjzsfsq",               // 是否为省级综合减灾示范社区
            "sfybxqdzzhdyhdqd",            // 是否有本辖区地质灾害等隐患点清单
            "sfybxqrsrqqd",                // 是否有本辖区弱势人群清单
            "sfysqxzczhldt",               // 是否有社区（行政村）灾害类地图
            "sfysqxzcyjya",                // 是否有社区（行政村）应急预案
            "syndfzjzjzzjtrzje",           // 上一年度防灾减灾救灾资金投入总金额
            "djzczyzrs",                   // 登记注册志愿者人数
            "mbybyrs",                     // 民兵预备役人数
            "zhyjbncssl",                  // 本级灾害应急避难场所数量
            "zhyjbncsrl",                  // 本级灾害应急避难场所容量
            "fzjzyjwzcbfs",                // 防灾减灾应急物资储备方式
            "fzjzyjwzcbfs_qtsm",           // 防灾减灾应急物资储备方式-其他项说明
            "xycbwzzbzhje",                // 现有储备物资、装备折合金额（实物储备时填写）
            "zhyjxxjsfs",                  // 灾害预警信息接收方式
            "zhyjxxjsfs_qtsm",             // 灾害预警信息接收方式-其他项说明
            "zhyjxxcdfs",                  // 灾害预警信息传达方式
            "zhyjxxcdfs_qtsm",             // 灾害预警信息传达方式-其他项说明
            "zqxxsbfs",                    // 灾情信息上报方式
            "zqxxsbfs_qtsm",               // 灾情信息上报方式-其他项说明
            "syndzzdfzjzpyhdcs",           // 上一年度组织的防灾减灾培训活动次数
            "pxrc",                        // 上一年度防灾减灾培训活动培训人次
            "syndzzdfzjzylhdcs",           // 上一年度组织的防灾减灾演练活动次数
            "cyyldjmrc",                   // 参与上一年度组织的防灾减灾演练活动的居民人次
            "zhxxyrs",                     // 灾害信息员人数
            "address",                     // 社区（行政村）地址
            "zhs",                         // 总户数
            "tyshxydm",                    // 统一社会信用代码
            "jgbm",                        // 机构编码
            "dwfzr",                       // 单位负责人
            "tjfzr",                       // 统计负责人
            "tbr",                         // 填表人
            "lxdh",                        // 联系电话
            "tbrq",                        // 报出日期
            "id",                          // 唯一标识
            "xgqksm"                      // 修改情况说明
    ));

    /**
     * 验证GPKG文件是否包含必要字段
     *
     * @param file     GPKG文件
     * @param dataType 数据类型 (township/community/medical)
     * @return 验证结果
     */
    public static GpkgFieldValidationResult validateGpkgFields(MultipartFile file, String dataType) {
        GpkgFieldValidationResult result = new GpkgFieldValidationResult();
        result.setDataType(dataType);
        result.setValid(false);

        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".gpkg")) {
            result.addError("文件格式错误，请上传.gpkg格式的文件");
            return result;
        }

        // 创建临时文件
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("gpkg_", ".gpkg");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 创建数据存储
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "geopkg");
            params.put("database", tempFile.toAbsolutePath().toString());

            DataStore dataStore = DataStoreFinder.getDataStore(params);
            if (dataStore == null) {
                result.addError("无法读取GPKG文件，请确保文件格式正确");
                return result;
            }

            try {
                // 获取类型名称
                String[] typeNames = dataStore.getTypeNames();
                if (typeNames == null || typeNames.length == 0) {
                    result.addError("GPKG文件中没有找到任何图层");
                    return result;
                }

                // 使用第一个图层
                String layerName = typeNames[0];
                result.setLayerName(layerName);

                // 获取要素源
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource =
                        dataStore.getFeatureSource(layerName);

                // 获取要素数量
                int featureCount = featureSource.getCount(Query.ALL);
                result.setFeatureCount(featureCount);

                if (featureCount == 0) {
                    result.addError("GPKG文件中没有数据");
                    return result;
                }

                // 获取schema
                SimpleFeatureType schema = featureSource.getSchema();
                Set<String> gpkgFields = new HashSet<>();
                for (AttributeDescriptor descriptor : schema.getAttributeDescriptors()) {
                    gpkgFields.add(descriptor.getLocalName());
                }

                // 根据数据类型验证字段
                Set<String> requiredFields = getRequiredFields(dataType);
                Set<String> optionalFields = getOptionalFields(dataType);

                // 检查必要字段
                for (String requiredField : requiredFields) {
                    if (!gpkgFields.contains(requiredField)) {
                        result.addMissingField(requiredField);
                    }
                }

                // 记录存在推荐字段
                for (String optionalField : optionalFields) {
                    if (gpkgFields.contains(optionalField)) {
                        result.addPresentField(optionalField);
                    }
                }

                // 检查是否有必要字段缺失
                if (!result.hasMissingFields()) {
                    result.setValid(true);
                    result.addWarning("GPKG文件验证通过，包含 " + featureCount + " 条数据");
                } else {
                    result.addError("缺少 " + result.getMissingFields().size() + " 个必要字段，无法导入");
                }

            } finally {
                dataStore.dispose();
            }

        } catch (IOException e) {
            log.error("读取GPKG文件失败", e);
            result.addError("读取GPKG文件失败: " + e.getMessage());
        } finally {
            // 删除临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("删除临时文件失败", e);
                }
            }
        }

        return result;
    }

    /**
     * 获取必要字段集合
     */
    private static Set<String> getRequiredFields(String dataType) {
        switch (dataType) {
            case "township":
                return TOWNSHIP_REQUIRED_FIELDS;
            case "community":
                return COMMUNITY_REQUIRED_FIELDS;
            case "medical":
                return MEDICAL_REQUIRED_FIELDS;
            default:
                return new HashSet<>();
        }
    }

    /**
     * 获取可选字段集合
     */
    private static Set<String> getOptionalFields(String dataType) {
        switch (dataType) {
            case "township":
                return TOWNSHIP_OPTIONAL_FIELDS;
            case "community":
                return COMMUNITY_OPTIONAL_FIELDS;
            case "medical":
                return MEDICAL_OPTIONAL_FIELDS;
            default:
                return new HashSet<>();
        }
    }

    /**
     * 获取友好的字段名称（用于提示）
     */
    public static String getFieldDisplayName(String fieldName) {
        Map<String, String> fieldNames = new HashMap<>();
        // 乡镇字段（GPKG实际字段名）
        fieldNames.put("dwmc", "乡镇（街道）名称");
        fieldNames.put("code", "行政区划代码");
        fieldNames.put("nmczrksl", "常住人口数量");
        fieldNames.put("dzsheng", "省名称");
        fieldNames.put("dzshi", "市名称");
        fieldNames.put("dzxian", "县名称");
        fieldNames.put("dzxiang", "乡名称");
        fieldNames.put("address", "乡镇（街道）地址");
        fieldNames.put("dzjh", "地址街号");
        fieldNames.put("bjzhglgzryzs", "本级灾害管理工作人员总数");
        fieldNames.put("sfkzxzjdzhfxpg", "是否开展乡镇（街道）灾害风险评估");
        fieldNames.put("syndfzjzjzzjtrzje", "上一年度防灾减灾救灾资金投入总金额(万元)");
        fieldNames.put("xycbwzzbzhje", "现有储备物资、装备折合金额(万元)");
        fieldNames.put("syndzzdyjglpyhylcs", "上一年度组织的应急管理培训和演练次数");
        fieldNames.put("syndzzdyjglpyhylcyrc", "上一年度组织的应急管理培训和演练参与人次");
        fieldNames.put("bjzhyjbncssl", "本级灾害应急避难场所数量");
        fieldNames.put("bjzhyjbncsrl", "本级灾害应急避难场所容量");
        fieldNames.put("yjgssbsl", "应急供水设备数量");
        fieldNames.put("yjylsbsl", "应急医疗设备数量");
        fieldNames.put("yjtxsbsl", "应急通信设备数量");
        fieldNames.put("yjdyhyjfdsbsl", "应急电源或应急发电设备数量");
        fieldNames.put("xyjzwzzbsl", "本级储备点救灾物资、装备数量");
        fieldNames.put("xyjzwzzbcbdsl", "本级救灾物资、装备储备点数量");
        fieldNames.put("bjzhxxyrs", "本级灾害信息员人数");
        fieldNames.put("j3nbzhxdzrzhyjyasl", "近3年编制或修订自然灾害应急预案数量");
        fieldNames.put("j3nzdzrzhqdyjxycs", "近3年针对自然灾害启动应急响应次数");
        fieldNames.put("zhs", "年末总户数");
        fieldNames.put("yxxzjddzyzhlx", "影响乡镇（街道）的主要灾害类型");
        fieldNames.put("sfyxzjdzhldt", "是否有乡镇（街道）灾害类地图");
        // 医疗字段（GPKG实际字段名）
        fieldNames.put("dwmc", "医疗卫生机构名称");
        fieldNames.put("address", "医疗卫生机构详细地址");
        fieldNames.put("dzsheng", "地址省");
        fieldNames.put("dzshi", "地址市");
        fieldNames.put("dzxian", "地址县");
        fieldNames.put("dzxiang", "地址乡");
        fieldNames.put("id", "唯一标识");
        fieldNames.put("code", "行政区划代码");
        fieldNames.put("dmlx", "代码类型");
        fieldNames.put("yljglxdl", "医疗机构类型（大类）");
        fieldNames.put("yydj", "医院等级");
        fieldNames.put("syzycws", "实有住院床位数");
        fieldNames.put("zgzgrs", "在岗职工人数");
        // 社区字段（GPKG实际字段名）
        fieldNames.put("code", "行政区划代码");
        fieldNames.put("dzsheng", "地址省");
        fieldNames.put("dzshi", "地址市");
        fieldNames.put("dzxian", "地址县");
        fieldNames.put("dzxiang", "地址乡");
        fieldNames.put("dwmc", "社区（行政村）名称");
        fieldNames.put("dzcun", "地址村");
        fieldNames.put("nmczrksl", "常住人口数量");
        fieldNames.put("zero_ss_srs", "其中：0-14岁人数");
        fieldNames.put("lw_shysrs", "65岁（含）以上人数");
        fieldNames.put("czryrs", "残障人员人数");
        fieldNames.put("sqylwsfwzhcwsssl", "社区医疗卫生服务站或村卫生室数量");
        fieldNames.put("sfwqgzhjzsfsq", "是否为全国综合减灾示范社区");
        fieldNames.put("sfwsjzhjzsfsq", "是否为省级综合减灾示范社区");
        fieldNames.put("sfybxqdzzhdyhdqd", "是否有本辖区地质灾害等隐患点清单");
        fieldNames.put("sfybxqrsrqqd", "是否有本辖区弱势人群清单");
        fieldNames.put("sfysqxzczhldt", "是否有社区（行政村）灾害类地图");
        fieldNames.put("sfysqxzcyjya", "是否有社区（行政村）应急预案");
        fieldNames.put("syndfzjzjzzjtrzje", "上一年度防灾减灾救灾资金投入总金额");
        fieldNames.put("djzczyzrs", "登记注册志愿者人数");
        fieldNames.put("mbybyrs", "民兵预备役人数");
        fieldNames.put("zhyjbncssl", "本级灾害应急避难场所数量");
        fieldNames.put("zhyjbncsrl", "本级灾害应急避难场所容量");
        fieldNames.put("fzjzyjwzcbfs", "防灾减灾应急物资储备方式");
        fieldNames.put("fzjzyjwzcbfs_qtsm", "防灾减灾应急物资储备方式-其他项说明");
        fieldNames.put("xycbwzzbzhje", "现有储备物资、装备折合金额");
        fieldNames.put("zhyjxxjsfs", "灾害预警信息接收方式");
        fieldNames.put("zhyjxxjsfs_qtsm", "灾害预警信息接收方式-其他项说明");
        fieldNames.put("zhyjxxcdfs", "灾害预警信息传达方式");
        fieldNames.put("zhyjxxcdfs_qtsm", "灾害预警信息传达方式-其他项说明");
        fieldNames.put("zqxxsbfs", "灾情信息上报方式");
        fieldNames.put("zqxxsbfs_qtsm", "灾情信息上报方式-其他项说明");
        fieldNames.put("syndzzdfzjzpyhdcs", "上一年度组织的防灾减灾培训活动次数");
        fieldNames.put("pxrc", "上一年度防灾减灾培训活动培训人次");
        fieldNames.put("syndzzdfzjzylhdcs", "上一年度组织的防灾减灾演练活动次数");
        fieldNames.put("cyyldjmrc", "参与上一年度组织的防灾减灾演练活动的居民人次");
        fieldNames.put("zhxxyrs", "灾害信息员人数");
        fieldNames.put("address", "社区（行政村）地址");
        fieldNames.put("zhs", "总户数");
        fieldNames.put("tyshxydm", "统一社会信用代码");
        fieldNames.put("jgbm", "机构编码");
        fieldNames.put("dwfzr", "单位负责人");
        fieldNames.put("tjfzr", "统计负责人");
        fieldNames.put("tbr", "填表人");
        fieldNames.put("lxdh", "联系电话");
        fieldNames.put("tbrq", "报出日期");
        fieldNames.put("id", "唯一标识");
        fieldNames.put("xgqksm", "修改情况说明");

        return fieldNames.getOrDefault(fieldName, fieldName);
    }

    /**
     * 获取字段映射（用于从GPKG属性导入到数据库）
     */
    public static Map<String, String> getFieldMapping(String dataType) {
        Map<String, String> mapping = new HashMap<>();

        switch (dataType) {
            case "township":
                // 乡镇数据字段映射（GPKG字段 → 数据库字段）
                mapping.put("dwmc", "township");                       // 乡镇（街道）名称 → township
                mapping.put("code", "regionCode");                     // 行政区划代码 → regionCode
                mapping.put("nmczrksl", "population");                 // 常住人口数量 → population
                mapping.put("dzsheng", "province");                    // 省名称 → province
                mapping.put("dzshi", "city");                          // 市名称 → city
                mapping.put("dzxian", "county");                       // 县名称 → county
                mapping.put("dzxiang", "address");                     // 乡名称 → address
                mapping.put("address", "townshipAddress");             // 乡镇（街道）地址 → townshipAddress
                mapping.put("dzjh", "contactPhone");                   // 地址街号 → contactPhone
                mapping.put("bjzhglgzryzs", "managementStaff");        // 本级灾害管理工作人员总数 → managementStaff
                mapping.put("sfkzxzjdzhfxpg", "riskAssessment");       // 是否开展乡镇（街道）灾害风险评估 → riskAssessment
                mapping.put("syndfzjzjzzjtrzje", "fundingAmount");     // 上一年度防灾减灾救灾资金投入总金额 → fundingAmount
                mapping.put("xycbwzzbzhje", "materialValue");          // 现有储备物资、装备折合金额 → materialValue
                mapping.put("syndzzdyjglpyhylcs", "trainingDrillCount"); // 培训次数 → trainingDrillCount
                mapping.put("syndzzdyjglpyhylcyrc", "trainingParticipants"); // 培训参与人次 → trainingParticipants
                mapping.put("bjzhyjbncssl", "shelterCount");           // 本级灾害应急避难场所数量 → shelterCount
                mapping.put("bjzhyjbncsrl", "shelterCapacity");        // 本级灾害应急避难场所容量 → shelterCapacity
                mapping.put("yjgssbsl", "emergencyWaterCount");        // 应急供水设备数量 → emergencyWaterCount
                mapping.put("yjylsbsl", "emergencyMedicalCount");      // 应急医疗设备数量 → emergencyMedicalCount
                mapping.put("yjtxsbsl", "emergencyCommunicationCount"); // 应急通信设备数量 → emergencyCommunicationCount
                mapping.put("yjdyhyjfdsbsl", "emergencyPowerCount");   // 应急电源或应急发电设备数量 → emergencyPowerCount
                mapping.put("xyjzwzzbsl", "storageEquipmentCount");    // 本级储备点救灾物资、装备数量 → storageEquipmentCount
                mapping.put("xyjzwzzbcbdsl", "storagePointCount");     // 本级救灾物资、装备储备点数量 → storagePointCount
                mapping.put("bjzhxxyrs", "disasterInfoStaff");         // 本级灾害信息员人数 → disasterInfoStaff
                mapping.put("j3nbzhxdzrzhyjyasl", "emergencyPlanCount"); // 近3年编制或修订自然灾害应急预案数量 → emergencyPlanCount
                mapping.put("j3nzdzrzhqdyjxycs", "emergencyResponseCount"); // 近3年针对自然灾害启动应急响应次数 → emergencyResponseCount
                mapping.put("zhs", "totalHouseholds");                 // 年末总户数 → totalHouseholds
                mapping.put("yxxzjddzyzhlx", "mainDisasterTypes");     // 影响乡镇（街道）的主要灾害类型 → mainDisasterTypes
                mapping.put("sfyxzjdzhldt", "hasDisasterMap");         // 是否有乡镇（街道）灾害类地图 → hasDisasterMap
                mapping.put("tbr", "formFiller");                      // 填表人 → formFiller
                mapping.put("lxdh", "contactPhone");                   // 联系电话 → contactPhone
                mapping.put("dwfzr", "unitLeader");                    // 单位负责人 → unitLeader
                mapping.put("tjfzr", "statisticsLeader");              // 统计负责人 → statisticsLeader
                mapping.put("tbrq", "reportDate");                     // 报出日期 → reportDate
                mapping.put("id", "uniqueId");                         // 唯一标识 → uniqueId
                break;

            case "medical":
                // 医疗数据字段映射（GPKG字段 → 数据库字段）
                // 基本信息
                mapping.put("dwmc", "institutionName");                      // 机构名称
                mapping.put("address", "institutionAddress");                // 机构地址
                mapping.put("dzsheng", "province");                         // 地址省
                mapping.put("dzshi", "city");                               // 地址市
                mapping.put("dzxian", "county");                            // 地址县
                mapping.put("dzxiang", "township");                          // 地址乡
                mapping.put("dzjh", "contactPhone");                        // 地址街号 → 联系电话
                mapping.put("dzcun", "villageName");                         // 地址村
                mapping.put("id", "uniqueCode");                             // 唯一标识
                mapping.put("jgbm", "unifiedSocialCreditCode");              // 机构编码
                mapping.put("code", "orgCode");                              // 行政区划代码 → org_code（优先）
                mapping.put("fxpc_xzqhbmd_sjgl", "townshipCodeFromFxpc");   // 备用乡镇代码
                mapping.put("dmlx", "codeType");                            // 代码类型

                // 机构分类
                mapping.put("ylwsjglbdm", "institutionCategoryCode");        // 医疗卫生机构类别代码
                mapping.put("yljglxdl", "institutionTypeLarge");             // 医疗机构类型（大类）
                mapping.put("yljglxzl", "institutionTypeMedium");             // 医疗机构类型（中类）
                mapping.put("yljglxzkyyfl", "institutionTypeSpecialized");    // 医疗机构类型（专科医院分类）
                mapping.put("yydj", "hospitalLevel");                        // 医院等级
                mapping.put("yljgxz", "institutionNature");                  // 医疗机构性质

                // 场地与设备
                mapping.put("zdmj", "landArea");                            // 占地面积
                mapping.put("fwjzmj", "buildingArea");                       // 房屋建筑面积
                mapping.put("wyyssbts", "equipmentCountAbove10k");            // 万元以上设备台数

                // 人员统计
                mapping.put("zgzgrs", "totalStaff");                         // 在岗职工人数
                mapping.put("wsjsryzs", "healthTechnicalPersonnel");          // 卫生技术人员总数
                mapping.put("zchsrs", "registeredNurses");                   // 注册护士人数
                mapping.put("gqjnrys", "logisticsSkillPersonnel");             // 工勤技能人员数
                mapping.put("aqbwrysl", "securityPersonnelCount");            // 安全保卫人员数量
                mapping.put("yqjjzyrys", "preHospitalEmergencyPersonnel");     // 院前急救专业人员数

                // 诊疗统计
                mapping.put("ndzzlrcs", "annualTotalVisits");                 // 年度总诊疗人次数
                mapping.put("ndryrs", "annualAdmissionCount");                // 年度入院人数
                mapping.put("ndcyrs", "annualDischargeCount");               // 年度出院人数

                // 床位统计
                mapping.put("syzycws", "actualHospitalBeds");                 // 实有住院床位数
                mapping.put("fybfcws", "negativePressureBeds");               // 负压病房床位数
                mapping.put("zzjqhlbfcws", "icuBeds");                       // 重症加强护理病房（ICU）床位数

                // 车辆统计
                mapping.put("jjzhcsl", "emergencyCommandVehicleCount");        // 急救指挥车数量
                mapping.put("yzxjjcsl", "transportAmbulanceCount");           // 运转型急救车数量
                mapping.put("jhxjjcsl", "monitorAmbulanceCount");              // 监护型急救车数量
                mapping.put("fyjjcsl", "negativePressureAmbulanceCount");      // 负压急救车数量
                mapping.put("cxcs", "bloodCollectionVehicleCount");            // 采血车数
                mapping.put("sxcs", "bloodDeliveryVehicleCount");              // 送血车数

                // 应急保障
                mapping.put("yjgdnl", "emergencyPowerSupply");                // 应急供电能力
                mapping.put("yjgdnl_qtsm", "emergencyPowerSupplyOther");       // 应急供电能力-其他说明
                mapping.put("gsfs", "waterSupplyMode");                       // 供水方式
                mapping.put("gnfs", "heatingMode");                           // 供暖方式
                mapping.put("yjtxbzfs", "emergencyCommunicationMode");         // 应急通信保障方式
                mapping.put("yjtxbzfs_qtsm", "emergencyCommunicationModeOther"); // 应急通信保障方式-其他说明

                // 灾害历史与预案
                mapping.put("cjzsgdzrzhlx", "disasterHistoryType");            // 曾经遭受过的自然灾害类型
                mapping.put("cjzsgdzrzhlx_qtsm", "disasterHistoryTypeOther");  // 曾经遭受过的自然灾害类型-其他说明
                mapping.put("yyzrzhyjyalx", "emergencyPlanType");              // 已有自然灾害应急预案类型
                mapping.put("yyzrzhyjyalx_qtsm", "emergencyPlanTypeOther");    // 已有自然灾害应急预案类型-其他说明

                // 负责人信息
                mapping.put("dwfzr", "unitLeader");                          // 单位负责人
                mapping.put("tjfzr", "statisticalLeader");                    // 统计负责人
                mapping.put("tbr", "formFiller");                            // 填表人
                mapping.put("lxdh", "contactPhone");                          // 联系电话
                mapping.put("tbrq", "reportDate");                            // 报出日期
                mapping.put("xgqksm", "fillingInstructions");                 // 修改情况说明
                break;

            case "community":
                // 社区数据字段映射（GPKG字段 → 数据库字段）
                // 基本信息
                mapping.put("code", "regionCode");                      // 行政区划代码 → regionCode
                mapping.put("dzsheng", "provinceName");                 // 地址省 → provinceName
                mapping.put("dzshi", "cityName");                       // 地址市 → cityName
                mapping.put("dzxian", "countyName");                    // 地址县 → countyName
                mapping.put("dzxiang", "townshipName");                 // 地址乡 → townshipName
                mapping.put("dwmc", "communityName");                  // 社区（行政村）名称 → communityName
                mapping.put("dzcun", "villageName");                   // 地址村 → villageName
                mapping.put("address", "communityAddress");             // 社区（行政村）地址 → communityAddress

                // 人口统计
                mapping.put("nmczrksl", "residentPopulation");         // 常住人口数量 → residentPopulation
                mapping.put("zero_ss_srs", "age0To14Count");            // 其中：0-14岁人数 → age0To14Count
                mapping.put("lw_shysrs", "age65PlusCount");             // 65岁（含）以上人数 → age65PlusCount
                mapping.put("czryrs", "disabledPersonCount");          // 残障人员人数 → disabledPersonCount
                mapping.put("zhs", "totalHouseholds");                 // 总户数 → totalHouseholds

                // 医疗服务
                mapping.put("sqylwsfwzhcwsssl", "medicalServiceCount"); // 社区医疗卫生服务站或村卫生室数量 → medicalServiceCount

                // 示范社区标识
                mapping.put("sfwqgzhjzsfsq", "isNationalDemoCommunity"); // 是否为全国综合减灾示范社区 → isNationalDemoCommunity
                mapping.put("sfwsjzhjzsfsq", "isProvincialDemoCommunity"); // 是否为省级综合减灾示范社区 → isProvincialDemoCommunity

                // 隐患清单
                mapping.put("sfybxqdzzhdyhdqd", "hasDisasterPointsList"); // 是否有本辖区地质灾害等隐患点清单 → hasDisasterPointsList
                mapping.put("sfybxqrsrqqd", "hasVulnerableGroupsList");   // 是否有本辖区弱势人群清单 → hasVulnerableGroupsList
                mapping.put("sfysqxzczhldt", "hasDisasterMap");            // 是否有社区（行政村）灾害类地图 → hasDisasterMap
                mapping.put("sfysqxzcyjya", "hasEmergencyPlan");            // 是否有社区（行政村）应急预案 → hasEmergencyPlan

                // 资金投入
                mapping.put("syndfzjzjzzjtrzje", "lastYearFundingAmount"); // 上一年度防灾减灾救灾资金投入总金额 → lastYearFundingAmount
                mapping.put("xycbwzzbzhje", "materialsEquipmentValue");    // 现有储备物资、装备折合金额 → materialsEquipmentValue

                // 人员统计
                mapping.put("djzczyzrs", "registeredVolunteerCount");     // 登记注册志愿者人数 → registeredVolunteerCount
                mapping.put("mbybyrs", "militiaReserveCount");            // 民兵预备役人数 → militiaReserveCount
                mapping.put("zhxxyrs", "disasterInfoStaffCount");         // 灾害信息员人数 → disasterInfoStaffCount

                // 应急避难场所
                mapping.put("zhyjbncssl", "emergencyShelterCount");       // 本级灾害应急避难场所数量 → emergencyShelterCount
                mapping.put("zhyjbncsrl", "emergencyShelterCapacity");    // 本级灾害应急避难场所容量 → emergencyShelterCapacity

                // 物资储备
                mapping.put("fzjzyjwzcbfs", "materialStorageMethod");     // 防灾减灾应急物资储备方式 → materialStorageMethod
                mapping.put("fzjzyjwzcbfs_qtsm", "materialStorageMethodOther"); // 防灾减灾应急物资储备方式-其他项说明 → materialStorageMethodOther

                // 预警信息
                mapping.put("zhyjxxjsfs", "warningReceiveMethod");        // 灾害预警信息接收方式 → warningReceiveMethod
                mapping.put("zhyjxxjsfs_qtsm", "warningReceiveMethodOther"); // 灾害预警信息接收方式-其他项说明 → warningReceiveMethodOther
                mapping.put("zhyjxxcdfs", "warningCommunicationMethod");  // 灾害预警信息传达方式 → warningCommunicationMethod
                mapping.put("zhyjxxcdfs_qtsm", "warningCommunicationMethodOther"); // 灾害预警信息传达方式-其他项说明 → warningCommunicationMethodOther

                // 灾情上报
                mapping.put("zqxxsbfs", "disasterReportMethod");          // 灾情信息上报方式 → disasterReportMethod
                mapping.put("zqxxsbfs_qtsm", "disasterReportMethodOther"); // 灾情信息上报方式-其他项说明 → disasterReportMethodOther

                // 培训演练
                mapping.put("syndzzdfzjzpyhdcs", "lastYearTrainingCount"); // 上一年度组织的防灾减灾培训活动次数 → lastYearTrainingCount
                mapping.put("pxrc", "lastYearTrainingParticipants");      // 上一年度防灾减灾培训活动培训人次 → lastYearTrainingParticipants
                mapping.put("syndzzdfzjzylhdcs", "lastYearDrillCount");    // 上一年度组织的防灾减灾演练活动次数 → lastYearDrillCount
                mapping.put("cyyldjmrc", "lastYearDrillParticipants");     // 参与上一年度组织的防灾减灾演练活动的居民人次 → lastYearDrillParticipants

                // 其他信息
                mapping.put("tyshxydm", "unifiedSocialCreditCode");       // 统一社会信用代码 → unifiedSocialCreditCode
                mapping.put("jgbm", "organizationCode");                 // 机构编码 → organizationCode
                mapping.put("id", "uniqueId");                           // 唯一标识 → uniqueId

                // 负责人信息
                mapping.put("dwfzr", "unitLeader");                      // 单位负责人 → unitLeader
                mapping.put("tjfzr", "statisticsLeader");                // 统计负责人 → statisticsLeader
                mapping.put("tbr", "formFiller");                        // 填表人 → formFiller
                mapping.put("lxdh", "contactPhone");                     // 联系电话 → contactPhone
                mapping.put("tbrq", "reportDate");                       // 报出日期 → reportDate
                mapping.put("xgqksm", "fillInstructions");               // 修改情况说明 → fillInstructions
                break;
        }

        return mapping;
    }
}
