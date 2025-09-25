package com.evaluate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.PrimaryIndicatorResult;
import com.evaluate.mapper.PrimaryIndicatorResultMapper;
import com.evaluate.service.IPrimaryIndicatorResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 一级指标结果服务实现类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class PrimaryIndicatorResultServiceImpl extends ServiceImpl<PrimaryIndicatorResultMapper, PrimaryIndicatorResult> implements IPrimaryIndicatorResultService {

    @Override
    public List<PrimaryIndicatorResult> getBySurveyId(Long surveyId) {
        return baseMapper.selectBySurveyId(surveyId);
    }

    @Override
    public List<PrimaryIndicatorResult> getBySurveyIdAndAlgorithmId(Long surveyId, Long algorithmId) {
        return baseMapper.selectBySurveyIdAndAlgorithmId(surveyId, algorithmId);
    }

    @Override
    public List<PrimaryIndicatorResult> getByIndicatorCode(String indicatorCode) {
        return baseMapper.selectByIndicatorCode(indicatorCode);
    }

    @Override
    public List<PrimaryIndicatorResult> getByConditions(Long surveyId, Long algorithmId, Long weightConfigId) {
        return baseMapper.selectByConditions(surveyId, algorithmId, weightConfigId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<PrimaryIndicatorResult> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }
        
        try {
            return baseMapper.batchInsert(resultList) > 0;
        } catch (Exception e) {
            log.error("批量保存一级指标结果失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdate(List<PrimaryIndicatorResult> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }
        
        try {
            return baseMapper.batchUpdate(resultList) > 0;
        } catch (Exception e) {
            log.error("批量更新一级指标结果失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySurveyId(Long surveyId) {
        try {
            return baseMapper.deleteBySurveyId(surveyId) >= 0;
        } catch (Exception e) {
            log.error("根据调查ID删除一级指标结果失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByConditions(Long surveyId, Long algorithmId, Long weightConfigId) {
        try {
            return baseMapper.deleteByConditions(surveyId, algorithmId, weightConfigId) >= 0;
        } catch (Exception e) {
            log.error("根据条件删除一级指标结果失败", e);
            return false;
        }
    }

    @Override
    public int countBySurveyId(Long surveyId) {
        return baseMapper.countBySurveyId(surveyId);
    }

    @Override
    public Double calculateTotalScore(Long surveyId, Long algorithmId, Long weightConfigId) {
        return baseMapper.calculateTotalScore(surveyId, algorithmId, weightConfigId);
    }
}