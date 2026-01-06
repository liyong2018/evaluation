package com.evaluate.service;

import com.evaluate.entity.OrganizationBoundary;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OrganizationBoundaryService extends IService<OrganizationBoundary> {
    
    /**
     * 根据组织机构ID和年份获取边界信息
     * @param orgId 组织机构ID
     * @param year 年份
     * @return 边界信息
     */
    OrganizationBoundary getBoundaryByOrgIdAndYear(Long orgId, Integer year);

    /**
     * 保存或更新边界信息
     * @param boundary 边界信息
     * @return 是否成功
     */
    boolean saveOrUpdateBoundary(OrganizationBoundary boundary);

    /**
     * 获取组织机构的所有边界配置
     * @param orgId 组织机构ID
     * @return 边界列表
     */
    List<OrganizationBoundary> getBoundariesByOrgId(Long orgId);
}
