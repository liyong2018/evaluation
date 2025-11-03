package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.entity.Organization;
import com.evaluate.entity.SurveyData;

/**
 * 组织机构服务
 */
public interface IOrganizationService extends IService<Organization> {

    /**
     * 根据社区减灾能力数据同步组织机构
     *
     * @param community 社区减灾能力数据
     */
    void syncFromCommunityData(CommunityDisasterReductionCapacity community);

    /**
     * 根据乡镇调查数据同步组织机构
     *
     * @param surveyData 调查数据
     */
    void syncFromSurveyData(SurveyData surveyData);
}
