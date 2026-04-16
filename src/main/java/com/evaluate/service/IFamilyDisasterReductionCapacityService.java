package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.FamilyDisasterReductionCapacity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 家庭减灾能力服务接口
 *
 * @author System
 * @since 2025-01-01
 */
public interface IFamilyDisasterReductionCapacityService extends IService<FamilyDisasterReductionCapacity> {

    /**
     * 批量导入家庭减灾能力数据
     *
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importFamilyCapacityData(MultipartFile file);
}
