package com.mqttsnet.thinglinks.rule.service.impl;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanUtils;
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
import org.apache.poi.ss.formula.functions.T;
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
@Service
public class RuleServiceImpl implements RuleService {

    @Resource
    private RuleMapper ruleMapper;
    @Resource
    private RuleConditionsService ruleConditionsService;

    @Resource
    private RemoteProductService remoteProductService;

    @Resource
    private RemoteProductServicesService remoteProductServicesService;

    @Resource
    private RemoteDeviceService remoteDeviceService;

    @Resource
    private RemoteProductPropertiesService remoteProductPropertiesService;

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
    public int insert(Rule record) {
        return ruleMapper.insert(record);
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

        List<Long> productServicesIdList = new ArrayList<>();
        List<Long> productPropertiesIdList = new ArrayList<>();
        List<String> deviceIdentificationList = new ArrayList<>();
        List<String> productIdentificationList = new ArrayList<>();
        ruleConditionsList.stream().forEach(ruleConditions -> {
            productServicesIdList.add(ruleConditions.getServiceId());
            productPropertiesIdList.add(ruleConditions.getPropertiesId());
            deviceIdentificationList.add(ruleConditions.getDeviceIdentification());
            productIdentificationList.add(ruleConditions.getProductIdentification());
        });
        R<?> productListResponse = remoteProductService.selectProductByIdentificationList(productIdentificationList);
        List<Product> productList = (List )productListResponse.getData();
        Map<String,Product> productMap = productList.stream().collect(Collectors.toMap(Product::getProductIdentification, s->s));

        R<?> deviceListResponse = remoteDeviceService.selectDeviceByDeviceIdentificationList(deviceIdentificationList);
        List<Device> deviceList = (List) deviceListResponse.getData();
        Map<String,Device> deviceMap = deviceList.stream().collect(Collectors.toMap(Device::getDeviceIdentification,s->s));

        R<?> productServicesResponse = remoteProductServicesService.selectServicesByServiceIdList(productServicesIdList);
        List<ProductServices> productServicesList = (List) productServicesResponse.getData();
        Map<Long, ProductServices> productServicesMap =  productServicesList.stream().collect(Collectors.toMap(ProductServices::getId,s->s));

        R<?> productPropertiesResponse = remoteProductPropertiesService.selectPropertiesByPropertiesIdList(productPropertiesIdList);
        List<ProductProperties> productPropertiesList = (List) productPropertiesResponse.getData();
        Map<Long, ProductProperties> productPropertiesMap =  productPropertiesList.stream().collect(Collectors.toMap(ProductProperties::getId,s->s));

        ruleConditionsList.stream().forEach(
                ruleConditions -> {
                    RuleConditionsModel ruleConditionsModel = new RuleConditionsModel();
                    BeanUtils.copyProperties(ruleConditions,ruleConditionsModel);

                    ruleConditionsModel.setProductName(productMap.get(ruleConditions.getProductIdentification().toString()).getProductName());
                    ruleConditionsModel.setDeviceName(deviceMap.get(ruleConditions.getDeviceIdentification().toString()).getDeviceName());
                    ruleConditionsModel.setServiceName(productServicesMap.get(ruleConditions.getServiceId()).getServiceName());
                    ruleConditionsModel.setPropertiesName(productPropertiesMap.get(ruleConditions.getPropertiesId()).getName());
                    ruleModel.getRuleConditionsModelList().add(ruleConditionsModel);
                }
        );
        return new RuleModel();
    }
}


