package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报告Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    // 使用MyBatis Plus的BaseMapper提供的基础CRUD方法
    // 复杂查询通过Service层使用QueryWrapper实现
}