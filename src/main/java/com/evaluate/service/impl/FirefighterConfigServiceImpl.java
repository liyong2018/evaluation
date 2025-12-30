package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.FirefighterConfig;
import com.evaluate.mapper.FirefighterConfigMapper;
import com.evaluate.service.IFirefighterConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 消防员配置服务实现类
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class FirefighterConfigServiceImpl
        extends ServiceImpl<FirefighterConfigMapper, FirefighterConfig>
        implements IFirefighterConfigService {

    @Override
    public Integer getFirefighterCountByRegionCode(String regionCode) {
        if (regionCode == null || regionCode.trim().isEmpty()) {
            return 0;
        }

        try {
            log.info("开始查询消防员数量，区域代码: {}", regionCode);

            // 首先尝试精确匹配
            Integer count = baseMapper.getFirefighterCountByRegionCode(regionCode.trim());
            log.info("精确匹配查询结果，区域代码: {}, 消防员数量: {}", regionCode, count);

            if (count != null && count > 0) {
                return count;
            }

            // 如果精确匹配没有结果，尝试去掉最后3位（社区代码转乡镇代码）
            if (regionCode.trim().length() >= 3) {
                String townshipCode = regionCode.trim().substring(0, regionCode.trim().length() - 3);
                count = baseMapper.getFirefighterCountByRegionCode(townshipCode);
                log.info("去掉后3位匹配查询结果，原代码: {}, 乡镇代码: {}, 消防员数量: {}", regionCode, townshipCode, count);

                if (count != null && count > 0) {
                    log.debug("通过去掉后3位匹配到消防员数量，原代码: {}, 乡镇代码: {}, 数量: {}", regionCode, townshipCode, count);
                    return count;
                }
            }

            log.info("未找到消防员配置，区域代码: {}, 返回0", regionCode);
            return 0;
        } catch (Exception e) {
            log.error("查询消防员数量失败，区域代码: {}", regionCode, e);
            return 0;
        }
    }

    @Override
    public List<FirefighterConfig> getByLocation(String provinceName, String cityName, String countyName, String townshipName) {
        try {
            return baseMapper.getByLocation(provinceName, cityName, countyName, townshipName);
        } catch (Exception e) {
            log.error("根据地理位置查询消防员配置失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FirefighterConfig> getByRegionCodes(List<String> regionCodes) {
        if (regionCodes == null || regionCodes.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return baseMapper.getByRegionCodes(regionCodes);
        } catch (Exception e) {
            log.error("批量查询消防员配置失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FirefighterConfig> getByCountyName(String countyName) {
        if (countyName == null || countyName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return baseMapper.getByCountyName(countyName.trim());
        } catch (Exception e) {
            log.error("根据县名称查询消防员配置失败，县名: {}", countyName, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Integer sumFirefighterCountByCounty(String countyName) {
        if (countyName == null || countyName.trim().isEmpty()) {
            return 0;
        }

        try {
            Integer sum = baseMapper.sumFirefighterCountByCounty(countyName.trim());
            return sum != null ? sum : 0;
        } catch (Exception e) {
            log.error("统计县级消防员数量失败，县名: {}", countyName, e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateFirefighterConfig(FirefighterConfig firefighterConfig) {
        try {
            // 检查是否已存在相同行政区划代码的配置
            QueryWrapper<FirefighterConfig> wrapper = new QueryWrapper<>();
            wrapper.eq("region_code", firefighterConfig.getRegionCode());
            FirefighterConfig existing = getOne(wrapper);

            if (existing != null) {
                // 更新现有配置
                firefighterConfig.setId(existing.getId());
                firefighterConfig.setUpdatedTime(java.time.LocalDateTime.now());
                return updateById(firefighterConfig);
            } else {
                // 新增配置
                return save(firefighterConfig);
            }
        } catch (Exception e) {
            log.error("保存或更新消防员配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchImportFirefighterConfigs(List<FirefighterConfig> firefighterConfigs) {
        if (firefighterConfigs == null || firefighterConfigs.isEmpty()) {
            return false;
        }

        try {
            int successCount = 0;
            for (FirefighterConfig config : firefighterConfigs) {
                if (saveOrUpdateFirefighterConfig(config)) {
                    successCount++;
                }
            }

            log.info("批量导入消防员配置完成，成功: {}, 总数: {}", successCount, firefighterConfigs.size());
            return successCount == firefighterConfigs.size();
        } catch (Exception e) {
            log.error("批量导入消防员配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        try {
            return removeById(id);
        } catch (Exception e) {
            log.error("删除消防员配置失败，ID: {}", id, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        try {
            FirefighterConfig config = getById(id);
            if (config == null) {
                log.warn("消防员配置不存在，ID: {}", id);
                return false;
            }

            config.setStatus(status);
            return updateById(config);
        } catch (Exception e) {
            log.error("更新消防员配置状态失败，ID: {}, 状态: {}", id, status, e);
            return false;
        }
    }
}