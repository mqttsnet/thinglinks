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
import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import com.mqttsnet.thinglinks.rule.api.domain.model.RuleConditionsModel;
import com.mqttsnet.thinglinks.rule.mapper.RuleConditionsMapper;
import com.mqttsnet.thinglinks.rule.service.RuleConditionsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.rule.service.impl
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-21 18:47
 **/
@Service
public class RuleConditionsServiceImpl implements RuleConditionsService {

    @Resource
    private RuleConditionsMapper ruleConditionsMapper;

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
        return ruleConditionsMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(RuleConditions record) {
        return ruleConditionsMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(RuleConditions record) {
        return ruleConditionsMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(RuleConditions record) {
        return ruleConditionsMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(RuleConditions record) {
        return ruleConditionsMapper.insertSelective(record);
    }

    @Override
    public RuleConditions selectByPrimaryKey(Long id) {
        return ruleConditionsMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<RuleConditions> selectByRuleId(Long ruleId) {
        return ruleConditionsMapper.selectByRuleId(ruleId);
    }

    @Override
    public int updateByPrimaryKeySelective(RuleConditions record) {
        return ruleConditionsMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(RuleConditions record) {
        return ruleConditionsMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<RuleConditions> list) {
        return ruleConditionsMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<RuleConditions> list) {
        return ruleConditionsMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<RuleConditions> list) {
        return ruleConditionsMapper.batchInsert(list);
    }

    public int deleteBatchByIds(Long[] ids) {
        return ruleConditionsMapper.deleteBatchByIds(ids);
    }

    public List<RuleConditionsModel> ruleConditionsListToRuleConditionsModelList(List<RuleConditions> ruleConditionsList){
        List<RuleConditionsModel> ruleConditionsModelList = new ArrayList<>();
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
        R<?> productListResponse = remoteProductService.selectProductByProductIdentificationList(productIdentificationList);
        List<Product> productList = (List)productListResponse.getData();
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
                    ruleConditionsModelList.add(ruleConditionsModel);
                }
        );
        return ruleConditionsModelList;
    }
}

