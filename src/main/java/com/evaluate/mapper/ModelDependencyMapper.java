package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.ModelDependency;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型前置依赖配置Mapper接口
 */
@Mapper
public interface ModelDependencyMapper extends BaseMapper<ModelDependency> {

}
