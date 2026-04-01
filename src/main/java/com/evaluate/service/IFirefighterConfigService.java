package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.FirefighterConfig;

import java.util.List;

/**
 * 消防员配置服务接口
 *
 * @author System
 * @since 2025-01-01
 */
public interface IFirefighterConfigService extends IService<FirefighterConfig> {

    /**
     * 根据行政区划代码获取消防员数量
     *
     * @param regionCode 行政区划代码
     * @return 消防员数量，如果不存在返回0
     */
    Integer getFirefighterCountByRegionCode(String regionCode);

    /**
     * 根据行政区划代码前缀匹配并汇总消防员数量
     * 用于处理乡镇代码(9位)匹配社区级配置(12位)的场景
     *
     * @param regionCodePrefix 行政区划代码前缀
     * @return 消防员数量总和
     */
    Integer sumFirefighterCountByRegionCodePrefix(String regionCodePrefix);

    /**
     * 根据乡镇名称获取消防员数量
     *
     * @param townshipName 乡镇名称
     * @return 消防员数量，如果不存在返回0
     */
    Integer getFirefighterCountByTownshipName(String townshipName);

    /**
     * 根据地理位置查询消防员配置列表
     *
     * @param provinceName 省名称
     * @param cityName 市名称
     * @param countyName 县名称
     * @param townshipName 乡镇名称（可为空）
     * @return 消防员配置列表
     */
    List<FirefighterConfig> getByLocation(String provinceName, String cityName, String countyName, String townshipName);

    /**
     * 根据行政区划代码列表批量查询消防员配置
     *
     * @param regionCodes 行政区划代码列表
     * @return 消防员配置列表
     */
    List<FirefighterConfig> getByRegionCodes(List<String> regionCodes);

    /**
     * 根据县名称查询所有乡镇的消防员配置
     *
     * @param countyName 县名称
     * @return 消防员配置列表
     */
    List<FirefighterConfig> getByCountyName(String countyName);

    /**
     * 统计指定县的总消防员数量
     *
     * @param countyName 县名称
     * @return 总消防员数量
     */
    Integer sumFirefighterCountByCounty(String countyName);

    /**
     * 添加或更新消防员配置
     *
     * @param firefighterConfig 消防员配置对象
     * @return 操作结果
     */
    boolean saveOrUpdateFirefighterConfig(FirefighterConfig firefighterConfig);

    /**
     * 批量导入消防员配置
     *
     * @param firefighterConfigs 消防员配置列表
     * @return 导入结果
     */
    boolean batchImportFirefighterConfigs(List<FirefighterConfig> firefighterConfigs);

    /**
     * 删除消防员配置
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteById(Long id);

    /**
     * 启用/禁用消防员配置
     *
     * @param id 主键ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 检查是否有任何消防员配置数据
     *
     * @return 如果有数据返回true，否则返回false
     */
    boolean hasAnyData();
}