package com.mqttsnet.thinglinks.rule.service;

import com.mqttsnet.thinglinks.common.core.domain.R;

/**
 * @program: thinglinks
 * @description: 规则设备联动业务层接口
 * @packagename: com.mqttsnet.thinglinks.rule.service
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-11-03 18:44
 **/
public interface RuleDeviceLinkageService {

    /**
     * 触发设备联动规则条件
     * @param ruleIdentification 规则标识
     * @return
     */
    void triggerDeviceLinkageByRuleIdentification(String ruleIdentification);

    /**
     * 规则触发条件验证
     * @param ruleIdentification 规则标识
     * @return
     */
    Boolean checkRuleConditions(String ruleIdentification);


    /**
     * 执行动作
     * @param ruleIdentification 规则标识
     * @return
     */
    Boolean execAction(String ruleIdentification);

    /**
     * 获取所有产品
     * @return
     */
    R<?> selectAllProduct(String status);
    /**
     * 获取产品设备
     * @param productIdentification
     * @return
     */
    R<?> selectDeviceByProductIdentification(String productIdentification);


    /**
     * 获取产品服务
     * @param productIdentification
     * @return
     */
    R<?> selectProductServicesByProductIdentification(String productIdentification);


    /**
     * 根据产品id获取所有属性
     * @param serviceId
     * @return
     */
    R<?> selectProductPropertiesByServiceId(Long serviceId);

    /**
     * 根据产品id获取所有命令
     * @param serviceId
     * @return
     */
    R<?> selectProductCommandsByServiceId(Long serviceId);
}
