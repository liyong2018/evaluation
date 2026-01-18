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

    private static final int BASELINE_YEAR = 2020;

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

        for (int checkYear = year; checkYear >= BASELINE_YEAR; checkYear--) {
            QueryWrapper<OrganizationBoundary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("organization_id", orgId);
            queryWrapper.eq("year", checkYear);
            queryWrapper.last("LIMIT 1");
            OrganizationBoundary found = this.getOne(queryWrapper);
            if (found != null) {
                return found;
            }
        }

        return null;
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
