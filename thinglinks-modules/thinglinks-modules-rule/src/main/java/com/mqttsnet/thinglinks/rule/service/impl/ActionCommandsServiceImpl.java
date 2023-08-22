package com.mqttsnet.thinglinks.rule.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.exception.ServiceException;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanUtils;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.RemoteProductCommandsService;
import com.mqttsnet.thinglinks.link.api.RemoteProductService;
import com.mqttsnet.thinglinks.link.api.RemoteProductServicesService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommands;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import com.mqttsnet.thinglinks.rule.api.domain.model.ActionCommandsModel;
import com.mqttsnet.thinglinks.rule.api.domain.model.RuleConditionsModel;
import com.mqttsnet.thinglinks.rule.service.ActionCommandsService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mqttsnet.thinglinks.rule.mapper.ActionCommandsMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mqttsnet.thinglinks.rule.api.domain.ActionCommands;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.rule.service
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-12-04 21:39
**/
@Service
public class ActionCommandsServiceImpl implements ActionCommandsService {

    @Resource
    private TokenService tokenService;
    @Resource
    private RemoteProductService remoteProductService;

    @Resource
    private RemoteDeviceService remoteDeviceService;

    @Resource
    private RemoteProductServicesService remoteProductServicesService;

    @Resource
    private RemoteProductCommandsService remoteProductCommandsService;
    @Resource
    private ActionCommandsMapper actionCommandsMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return actionCommandsMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ActionCommands record) {
        return actionCommandsMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ActionCommands record) {
        return actionCommandsMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ActionCommands record) {
        return actionCommandsMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ActionCommands record) {
        return actionCommandsMapper.insertSelective(record);
    }

    @Override
    public ActionCommands selectByPrimaryKey(Integer id) {
        return actionCommandsMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ActionCommands record) {
        return actionCommandsMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ActionCommands record) {
        return actionCommandsMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ActionCommands> list) {
        return actionCommandsMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ActionCommands> list) {
        String sysUserName = getSysUserName();
        List<ActionCommands> insertList = list.stream().map(s->{
            s.setUpdateBy(sysUserName);
            return s;
        }).collect(Collectors.toList());
        return actionCommandsMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ActionCommands> list) {

        String sysUserName = getSysUserName();
        List<ActionCommands> insertList = list.stream().map(s->{
            s.setCtreateBy(sysUserName);
            return s;
        }).collect(Collectors.toList());

        return actionCommandsMapper.batchInsert(list);
    }

    @Override
    public List<ActionCommands>   selectByActionCommandsSelective(ActionCommands actionCommands) {
        return actionCommandsMapper.selectByActionCommandsSelective(actionCommands);
    }

    @Override
    public List<ActionCommands> actionCommandsByRuleIdentification(String ruleIdentification) {
        return actionCommandsMapper.actionCommandsByRuleIdentification(ruleIdentification);
    }

    @Override
    public int deleteBatchByIds(Long[] ids){
        return actionCommandsMapper.deleteBatchByIds(ids);
    }



    public List<ActionCommandsModel> actionCommandsToActionCommandsModelList(List<ActionCommands> actionCommandsList){
        List<ActionCommandsModel> actionCommandsModelList = new ArrayList<>();
        List<Long> productServicesIdList = new ArrayList<>();
        List<Long> commandIdList = new ArrayList<>();
        List<String> deviceIdentificationList = new ArrayList<>();
        List<String> productIdentificationList = new ArrayList<>();
        actionCommandsList.stream().forEach(actionCommands -> {
            productServicesIdList.add(actionCommands.getServiceId());
            commandIdList.add(actionCommands.getCommandId());
            deviceIdentificationList.add(actionCommands.getDeviceIdentification());
            productIdentificationList.add(actionCommands.getProductIdentification());
        });
        R<?> productListResponse = remoteProductService.selectProductByProductIdentificationList(productIdentificationList);
        Map<String, Product> productMap = BeanUtils.rDataToBeanList(productListResponse,Product.class).stream().collect(Collectors.toMap(Product::getProductIdentification, s->s));

        R<?> deviceListResponse = remoteDeviceService.selectDeviceByDeviceIdentificationList(deviceIdentificationList);
        Map<String, Device> deviceMap = BeanUtils.rDataToBeanList(deviceListResponse,Device.class).stream().collect(Collectors.toMap(Device::getDeviceIdentification, s->s));

        R<?> productServicesResponse = remoteProductServicesService.selectProductServicesByIdList(productServicesIdList);
        Map<Long, ProductServices> productServicesMap =  BeanUtils.rDataToBeanList(productServicesResponse,ProductServices.class).stream().collect(Collectors.toMap(ProductServices::getId, s->s));

        R<?> productCommandsResponse = remoteProductCommandsService.selectProductCommandsByIdList(commandIdList);
        Map<Long, ProductCommands> productCommandsMap =  BeanUtils.rDataToBeanList(productCommandsResponse,ProductCommands.class).stream().collect(Collectors.toMap(ProductCommands::getId, s->s));

        actionCommandsList.stream().forEach(
                actionCommands -> {
                    ActionCommandsModel actionCommandsModel = new ActionCommandsModel();
                    BeanUtils.copyProperties(actionCommands,actionCommandsModel);
                    actionCommandsModel.setProductName(BeanUtils.getMapBeanVal(productMap,actionCommands.getProductIdentification().toString(), Product.class ,"getProductName"));
                    actionCommandsModel.setDeviceName(BeanUtils.getMapBeanVal(deviceMap,actionCommands.getDeviceIdentification().toString(),Device.class,"getDeviceName"));
                    actionCommandsModel.setServiceName(BeanUtils.getMapBeanVal(productServicesMap,actionCommands.getServiceId(),ProductServices.class,"getServiceName"));
                    actionCommandsModel.setCommandName(BeanUtils.getMapBeanVal(productCommandsMap,actionCommands.getCommandId(),ProductCommands.class,"getName"));

                    actionCommandsModelList.add(actionCommandsModel);
                }
        );
        return actionCommandsModelList;
    }

    private String getSysUserName(){
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        return sysUser.getUserName();
    }

}
