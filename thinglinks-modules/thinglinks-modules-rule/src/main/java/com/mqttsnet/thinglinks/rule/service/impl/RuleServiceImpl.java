package com.mqttsnet.thinglinks.rule.service.impl;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.exception.ServiceException;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanUtils;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.RemoteProductPropertiesService;
import com.mqttsnet.thinglinks.link.api.RemoteProductService;
import com.mqttsnet.thinglinks.link.api.RemoteProductServicesService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.rule.api.domain.Rule;
import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import com.mqttsnet.thinglinks.rule.api.domain.model.RuleConditionsModel;
import com.mqttsnet.thinglinks.rule.api.domain.model.RuleModel;
import com.mqttsnet.thinglinks.rule.mapper.RuleMapper;
import com.mqttsnet.thinglinks.rule.service.RuleConditionsService;
import com.mqttsnet.thinglinks.rule.service.RuleService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.rule.service.impl
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-21 18:47
 **/
@Slf4j
@Service
public class RuleServiceImpl implements RuleService {

    @Resource
    private TokenService tokenService;
    @Resource
    private RuleMapper ruleMapper;
    @Resource
    private RuleConditionsService ruleConditionsService;


    @Override
    public int deleteByPrimaryKey(Long id) {

        //删除关联条件
        return ruleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Rule selectByRuleIdentification(String ruleIdentification) {
        return ruleMapper.selectByRuleIdentification(ruleIdentification);
    }

    @Override
    public Rule insert(Rule record) {
        //判断name重复
        Rule ruleQuery = new Rule();
        ruleQuery.setRuleName(record.getRuleName());
        List<Rule> ruleList = ruleMapper.selectRuleList(ruleQuery);
        if (!ruleList.isEmpty()) {
            throw new ServiceException("规则名称重复");
        }
        record.setRuleIdentification(UUID.getUUID());
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        int res = ruleMapper.insert(record);
        if(res > 0){
            return record;
        }
        throw new ServiceException("添加规则失败");
    }

    @Override
    public int insertOrUpdate(Rule record) {
        return ruleMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Rule record) {
        return ruleMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Rule record) {
        return ruleMapper.insertSelective(record);
    }

    @Override
    public Rule selectByPrimaryKey(Long id) {
        return ruleMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Rule record) {
        return ruleMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Rule record) {
        return ruleMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<Rule> list) {
        return ruleMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<Rule> list) {
        return ruleMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<Rule> list) {
        return ruleMapper.batchInsert(list);
    }
    @Override
    public List<Rule> selectRuleList(Rule rule){
        return ruleMapper.selectRuleList(rule);
    }

    @Override
    public RuleModel selectFullRuleById(Long id){
        Rule rule = ruleMapper.selectByPrimaryKey(id);
        RuleModel  ruleModel = new RuleModel();
        BeanUtils.copyProperties(rule,ruleModel);

        List<RuleConditions> ruleConditionsList = ruleConditionsService.selectByRuleId(id);
        log.info("List<RuleConditions>:{}",ruleConditionsList.toString());

        ruleModel.setRuleConditionsModelList(ruleConditionsService.ruleConditionsListToRuleConditionsModelList(ruleConditionsList));
        return ruleModel;
    }
}


