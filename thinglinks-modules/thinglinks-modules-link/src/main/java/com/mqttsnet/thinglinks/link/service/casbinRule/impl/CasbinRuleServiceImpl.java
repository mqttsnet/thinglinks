package com.mqttsnet.thinglinks.link.service.casbinRule.impl;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.CasbinRule;
import com.mqttsnet.thinglinks.link.mapper.casbinRule.CasbinRuleMapper;
import com.mqttsnet.thinglinks.link.service.casbinRule.CasbinRuleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: thinglinks
 * @description:
 * @packagename: com.mqttsnet.thinglinks.link.service.casbinRule.impl
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-06-16 18:49
 **/
@Service
public class CasbinRuleServiceImpl implements CasbinRuleService {

    @Resource
    private CasbinRuleMapper casbinRuleMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return casbinRuleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(CasbinRule record) {
        return casbinRuleMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(CasbinRule record) {
        return casbinRuleMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(CasbinRule record) {
        return casbinRuleMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(CasbinRule record) {
        return casbinRuleMapper.insertSelective(record);
    }

    @Override
    public CasbinRule selectByPrimaryKey(Integer id) {
        return casbinRuleMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(CasbinRule record) {
        return casbinRuleMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(CasbinRule record) {
        return casbinRuleMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<CasbinRule> list) {
        return casbinRuleMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<CasbinRule> list) {
        return casbinRuleMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<CasbinRule> list) {
        return casbinRuleMapper.batchInsert(list);
    }

    /**
     * ??????CAS????????????
     *
     * @param id CAS??????????????????
     * @return CAS????????????
     */
    @Override
    public CasbinRule selectCasbinRuleById(Long id)
    {
        return casbinRuleMapper.selectCasbinRuleById(id);
    }

    /**
     * ??????CAS??????????????????
     *
     * @param casbinRule CAS????????????
     * @return CAS????????????
     */
    @Override
    public List<CasbinRule> selectCasbinRuleList(CasbinRule casbinRule)
    {
        return casbinRuleMapper.selectCasbinRuleList(casbinRule);
    }

    /**
     * ??????CAS????????????
     *
     * @param casbinRule CAS????????????
     * @return ??????
     */
    @Override
    public int insertCasbinRule(CasbinRule casbinRule)
    {
        return casbinRuleMapper.insertCasbinRule(casbinRule);
    }

    /**
     * ??????CAS????????????
     *
     * @param casbinRule CAS????????????
     * @return ??????
     */
    @Override
    public int updateCasbinRule(CasbinRule casbinRule)
    {
        return casbinRuleMapper.updateCasbinRule(casbinRule);
    }

    /**
     * ????????????CAS????????????
     *
     * @param ids ???????????????CAS??????????????????
     * @return ??????
     */
    @Override
    public int deleteCasbinRuleByIds(Long[] ids)
    {
        return casbinRuleMapper.deleteCasbinRuleByIds(ids);
    }

    /**
     * ??????CAS??????????????????
     *
     * @param id CAS??????????????????
     * @return ??????
     */
    @Override
    public int deleteCasbinRuleById(Long id)
    {
        return casbinRuleMapper.deleteCasbinRuleById(id);
    }

}
