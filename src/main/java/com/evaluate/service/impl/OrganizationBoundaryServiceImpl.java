package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.OrganizationBoundary;
import com.evaluate.mapper.OrganizationBoundaryMapper;
import com.evaluate.service.OrganizationBoundaryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationBoundaryServiceImpl extends ServiceImpl<OrganizationBoundaryMapper, OrganizationBoundary> implements OrganizationBoundaryService {

    @Override
    public OrganizationBoundary getBoundaryByOrgIdAndYear(Long orgId, Integer year) {
        if (orgId == null) {
            return null;
        }

        if (year == null) {
            QueryWrapper<OrganizationBoundary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("organization_id", orgId);
            queryWrapper.orderByDesc("year");
            queryWrapper.last("LIMIT 1");
            return this.getOne(queryWrapper);
        }

        // Use single query instead of loop - much more efficient
        QueryWrapper<OrganizationBoundary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("organization_id", orgId);
        queryWrapper.le("year", year);
        queryWrapper.orderByDesc("year");
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean saveOrUpdateBoundary(OrganizationBoundary boundary) {
        if (boundary.getId() != null) {
            return this.updateById(boundary);
        }
        
        // Check if exists for this year and org
        OrganizationBoundary existing = getBoundaryByOrgIdAndYear(boundary.getOrganizationId(), boundary.getYear());
        if (existing != null) {
            boundary.setId(existing.getId());
            return this.updateById(boundary);
        }
        
        return this.save(boundary);
    }

    @Override
    public List<OrganizationBoundary> getBoundariesByOrgId(Long orgId) {
        QueryWrapper<OrganizationBoundary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("organization_id", orgId);
        queryWrapper.orderByDesc("year");
        return this.list(queryWrapper);
    }
}
