package com.mqttsnet.thinglinks.rule.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.ConditionTypeEnum;
import com.mqttsnet.thinglinks.common.core.enums.FieldTypeEnum;
import com.mqttsnet.thinglinks.common.core.enums.OperatorEnum;
import com.mqttsnet.thinglinks.common.core.enums.TriggeringEnum;
import com.mqttsnet.thinglinks.common.core.mqs.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.common.core.utils.CompareUtil;
import com.mqttsnet.thinglinks.common.rocketmq.domain.MQMessage;
import com.mqttsnet.thinglinks.link.api.*;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.rule.api.domain.ActionCommands;
import com.mqttsnet.thinglinks.rule.api.domain.Rule;
import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import com.mqttsnet.thinglinks.rule.service.ActionCommandsService;
import com.mqttsnet.thinglinks.rule.service.RuleConditionsService;
import com.mqttsnet.thinglinks.rule.service.RuleDeviceLinkageService;
import com.mqttsnet.thinglinks.rule.service.RuleService;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.TagsSelectDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @program: thinglinks
 * @description: 规则设备联动业务层接口实现类
 * @packagename: com.mqttsnet.thinglinks.rule.service.impl
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-11-03 18:50
 **/
@Slf4j
@Service
public class RuleDeviceLinkageServiceImpl implements RuleDeviceLinkageService {


    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ActionCommandsService actionCommandsService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RuleConditionsService ruleConditionsService;

    @Resource
    private RemoteProductService remoteProductService;

    @Resource
    private RemoteTdEngineService remoteTdEngineService;

    @Resource
    private RemoteDeviceService remoteDeviceService;

    @Resource
    private RemoteProductServicesService remoteProductServicesService;

    @Resource
    private RemoteProductPropertiesService remoteProductPropertiesService;

    @Resource
    private RemoteProductCommandsService remoteProductCommandsService;

    @Resource
    private RemoteProductCommandsRequestsService remoteProductCommandsRequestsService;

    /**
     * 触发设备联动规则条件
     *
     * @param ruleIdentification 规则标识
     * @return
     */
    @Override
    @Transactional
    public void triggerDeviceLinkageByRuleIdentification(String ruleIdentification) {
        MQMessage mqMessage = new MQMessage();
        mqMessage.setTopic(ConsumerTopicConstant.Rule.THINGLINKS_RULE_TRIGGER);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", ruleIdentification);
        mqMessage.setMessage(jsonObject.toJSONString());

        log.info("topic:{}", mqMessage.getTopic());
        log.info("message:{}", mqMessage.getMessage());

        rocketMQTemplate.convertAndSend(mqMessage.getTopic(), mqMessage.getMessage());
    }

    /**
     * 规则触发条件验证
     *
     * @param ruleIdentification 规则标识
     * @return
     */
    @Override
    public Boolean checkRuleConditions(String ruleIdentification) {
        // 查询规则
        Rule rule = ruleService.selectByRuleIdentification(ruleIdentification);
        if (Objects.isNull(rule)) {
            log.warn("{}规则不存在", ruleIdentification);
        }
        // 查询触发条件
        List<RuleConditions> ruleConditions = ruleConditionsService.selectByRuleId(rule.getId());
        // 存放比较结果
        List<Boolean> flags = new ArrayList<>();
        for (RuleConditions conditions : ruleConditions) {

            log.info("conditions:{}", conditions.toString());

            // 获取属性字段和类型，和设备上报的数据进行比对
            R<ProductProperties> properties = remoteProductService.selectByIdProperties(conditions.getPropertiesId());
            ProductProperties propertiesData = properties.getData();
            if (propertiesData == null) {
                continue;
            }

            log.info("properties:{}", properties.toString());

            // 获取该产品下的所有设备数据
            Map<String, Map<String, Object>> maps = extractedDeviceData(conditions);

            log.info("maps:{}", maps.toString());

            // 属性名称
            String productPropertiesName = propertiesData.getName();
            // 属性类型
            String productPropertiesType = propertiesData.getDatatype();
            // 比较模式
            String comparisonMode = conditions.getComparisonMode();
            // 比较值
            String comparisonValue = conditions.getComparisonValue();
            //  条件类型(0:匹配设备触发、1:指定设备触发、2:按策略定时触发)
            switch (Objects.requireNonNull(ConditionTypeEnum.getBySymbol(conditions.getConditionType()))) {
                case MATCH:
                    R<?> deviceResponse = remoteDeviceService.selectByProductIdentification(conditions.getProductIdentification());
                    List<Map<String, Object>> datas = (List<Map<String, Object>>) deviceResponse.getData();
                    if (CollectionUtils.isEmpty(datas)) {
                        break;
                    }
                    datas.forEach(s -> {

                        String deviceIdentification = s.get("deviceIdentification").toString();
                        if (maps.containsKey(deviceIdentification)) {
                            Map<String, Object> stringObjectMap = maps.get(deviceIdentification);
                            String productPropertiesNameKey = "last(" + productPropertiesName + ")";
                            if (stringObjectMap.containsKey(productPropertiesNameKey)) {
                                // 获取属性实际的值
                                flags.add(compare(comparisonMode, productPropertiesType, stringObjectMap.get(productPropertiesNameKey).toString(), comparisonValue));
                            }
                        }
                    });
                    break;
                case SPECIFY:
                    List<String> deviceDatas = Arrays.asList(conditions.getDeviceIdentification().split(","));
                    deviceDatas.forEach(s -> {
                        if (maps.containsKey(s)) {
                            Map<String, Object> stringObjectMap = maps.get(s);
                            if (stringObjectMap.containsKey(productPropertiesName)) {
                                // 获取属性实际的值
                                flags.add(compare(comparisonMode, productPropertiesType, stringObjectMap.get(productPropertiesName).toString(), comparisonValue));
                            }
                        }
                    });
                    break;
                case STRATEGY:
                    break;
                default:
                    break;
            }
        }
        boolean mark = false;
        if (CollectionUtils.isEmpty(flags)) {
            // 验证条件
            return mark;
        }
        switch (Objects.requireNonNull(TriggeringEnum.getBySymbol(Integer.valueOf(rule.getTriggering())))) {
            case ALL:
                mark = flags.stream().allMatch(s -> s.equals(true));
                break;
            case ANY:
                mark = flags.stream().anyMatch(s -> s.equals(true));
                break;
            default:
                break;
        }
        return mark;
    }

    /**
     * 触发执行动作
     *
     * @param ruleIdentification 规则标识
     * @return
     */
    @Override
    public Boolean execAction(String ruleIdentification) {

        List<ActionCommands> actionCommands = actionCommandsService.actionCommandsByRuleIdentification(ruleIdentification);

        if (actionCommands != null && actionCommands.size() > 0) {
            actionCommands.forEach((command) -> {

                //MQTT消息下发
                String topic = "/v1/devices/" + command.getDeviceIdentification() + "/command";

                Map<String, Object> message = new HashMap<>();

                message.put("msgType", "cloudReq");
                message.put("mid", command.getCommandId());
                message.put("cmd", command.getCommandName());
                message.put("paras", command.getCommandBody());
                message.put("serviceId", command.getServiceId());
                message.put("service", command.getServiceName());
                message.put("deviceId", command.getDeviceIdentification());

            });
        }

        return true;
    }

    /**
     * 获取设备上报数据
     *
     * @param conditions
     * @return
     */
    private Map<String, Map<String, Object>> extractedDeviceData(RuleConditions conditions) {
        Map<String, Map<String, Object>> maps = new HashMap<>();
        // 获取产品信息
        R<Product> productResponse = remoteProductService.selectByProductIdentification(conditions.getProductIdentification());
        Product product = productResponse.getData();
        if (product == null) {
            return maps;
        }
        // 获取服务信息
        R<ProductServices> productServicesResponse = remoteProductService.selectProductServicesById(conditions.getServiceId());
        ProductServices productServices = productServicesResponse.getData();
        if (productServices == null) {
            return maps;
        }
        // 获取超级表名称 产品类型+产品标识+服务名称
        String superName = product.getProductType() + "_" + conditions.getProductIdentification() + "_" + productServices.getServiceName();

        // 查询最新的设备记录
        TagsSelectDao tagsSelectDao = new TagsSelectDao();
        tagsSelectDao.setDataBaseName("thinglinks");
        tagsSelectDao.setStableName(superName);
        tagsSelectDao.setTagsName("device_identification");
        R<Map<String, Map<String, Object>>> lastDataByTags = remoteTdEngineService.getLastDataByTags(tagsSelectDao);
        if (lastDataByTags != null && lastDataByTags.getData() != null) {
            maps = lastDataByTags.getData();
        }
        return maps;
    }


    /**
     * 实现计算
     *
     * @param symbol          运算符
     * @param propertiesType  属性类型
     * @param actualValue     实际值
     * @param comparisonValue 比较值
     * @return
     */
    private boolean compare(String symbol, String propertiesType, String actualValue, String comparisonValue) {
        boolean flag = false;
        FieldTypeEnum bySymbol = FieldTypeEnum.getBySymbol(propertiesType);
        // 判断比较类型
        switch (Objects.requireNonNull(OperatorEnum.getBySymbol(HtmlUtils.htmlUnescape(symbol)))) {
            case eq:
                // 判断属性值类型
                switch (Objects.requireNonNull(bySymbol)) {
                    case INT:
                        flag = Integer.parseInt(actualValue) == Integer.parseInt(comparisonValue);
                        break;
                    case STRING:
                        flag = actualValue.equals(comparisonValue);
                        break;
                    case DECIMAL:
                        flag = Double.parseDouble(actualValue) == Double.parseDouble(comparisonValue);
                        break;
                    case TIMESTAMP:
                        break;
                    case BOOL:
                        break;
                    default:
                        break;
                }
                break;
            case not:
                // 判断属性值类型
                switch (Objects.requireNonNull(bySymbol)) {
                    case INT:
                        flag = Integer.parseInt(actualValue) != Integer.parseInt(comparisonValue);
                        break;
                    case STRING:
                        flag = !actualValue.equals(comparisonValue);
                        break;
                    case DECIMAL:
                        flag = Double.parseDouble(actualValue) != Double.parseDouble(comparisonValue);
                        break;
                    case TIMESTAMP:
                        break;
                    case BOOL:
                        break;
                    default:
                        break;
                }
                break;
            case gt:
                // 判断属性值类型
                switch (Objects.requireNonNull(bySymbol)) {
                    case INT:
                        flag = Integer.parseInt(actualValue) > Integer.parseInt(comparisonValue);
                        break;
                    case DECIMAL:
                        flag = Double.parseDouble(actualValue) > Double.parseDouble(comparisonValue);
                        break;
                    case TIMESTAMP:
                        break;
                    case BOOL:
                        break;
                    default:
                        break;
                }
                break;
            case lt:
                // 判断属性值类型
                switch (Objects.requireNonNull(bySymbol)) {
                    case INT:
                        flag = Integer.parseInt(actualValue) < Integer.parseInt(comparisonValue);
                        break;
                    case DECIMAL:
                        flag = Double.parseDouble(actualValue) < Double.parseDouble(comparisonValue);
                        break;
                    case TIMESTAMP:
                        break;
                    case BOOL:
                        break;
                    default:
                        break;
                }
                break;
            case gte:
                // 判断属性值类型
                switch (Objects.requireNonNull(bySymbol)) {
                    case INT:
                        flag = Integer.parseInt(actualValue) >= Integer.parseInt(comparisonValue);
                        break;
                    case DECIMAL:
                        flag = Double.parseDouble(actualValue) >= Double.parseDouble(comparisonValue);
                        break;
                    case TIMESTAMP:
                        break;
                    case BOOL:
                        break;
                    default:
                        break;
                }
                break;
            case lte:
                // 判断属性值类型
                switch (Objects.requireNonNull(bySymbol)) {
                    case INT:
                        flag = Integer.parseInt(actualValue) <= Integer.parseInt(comparisonValue);
                        break;
                    case DECIMAL:
                        flag = Double.parseDouble(actualValue) <= Double.parseDouble(comparisonValue);
                        break;
                    case TIMESTAMP:
                        break;
                    case BOOL:
                        break;
                    default:
                        break;
                }
                break;
            case between:
                // 判断属性值类型
                switch (Objects.requireNonNull(bySymbol)) {
                    case INT:
                        int[] arrayint = Arrays.stream(comparisonValue.split(",")).mapToInt(Integer::parseInt).toArray();
                        flag = CompareUtil.rangeInDefinedInt(Integer.parseInt(actualValue), arrayint[0], arrayint[1]);
                        break;
                    case DECIMAL:
                        double[] arrayDouble = Arrays.stream(comparisonValue.split(",")).mapToDouble(Double::parseDouble).toArray();
                        flag = CompareUtil.rangeInDefinedDouble(Double.parseDouble(actualValue), arrayDouble[0], arrayDouble[1]);
                        break;
                    case TIMESTAMP:
                        break;
                    case BOOL:
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return flag;
    }


    /**
     * 获取所有可用产品
     *
     * @return
     */
    public R<?> selectAllProduct(String status) {
        R<?> productResponse = remoteProductService.selectAllProduct(status);
        return productResponse;
    }

    /**
     * 根据产品标识获取产品所有设备
     *
     * @param productIdentification
     * @return
     */
    public R<?> selectDeviceByProductIdentification(String productIdentification) {
        R<?> deviceResponse = remoteDeviceService.selectAllByProductIdentification(productIdentification);
        return deviceResponse;
    }


    /**
     * 根据产品标识获取产品所有服务
     *
     * @param productIdentification
     * @return
     */
    public R<?> selectProductServicesByProductIdentification(String productIdentification) {
        R<?> productServicesResponse = remoteProductServicesService.selectAllByProductIdentificationAndStatus(productIdentification, Constants.ENABLE);
        return productServicesResponse;
    }

    /**
     * 根据服务id获取服务所有属性
     *
     * @param serviceId
     * @return
     */
    public R<?> selectProductPropertiesByServiceId(Long serviceId) {
        R<?> propertiesResponse = remoteProductPropertiesService.selectAllByServiceId(serviceId);
        return propertiesResponse;
    }

    /**
     * 根据服务id获取服务所有命令
     *
     * @param serviceId
     * @return
     */
    public R<?> selectProductCommandsByServiceId(Long serviceId) {
        R<?> propertiesResponse = remoteProductCommandsService.selectAllByServiceId(serviceId);
        return propertiesResponse;
    }
}
