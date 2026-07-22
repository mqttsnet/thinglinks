package com.mqttsnet.thinglinks.producttopic.service;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.producttopic.entity.ProductTopic;

import java.util.List;


/**
 * <p>
 * 业务接口
 * 产品Topic信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
public interface ProductTopicService extends SuperService<Long, ProductTopic> {


    /**
     * 初始化产品基础Topic
     *
     * @param productIdentification 产品标识
     * @param productTypeEnum       产品类型枚举
     * @param reInit                是否重新初始化
     */
    void initProductBaseTopics(String productIdentification, ProductTypeEnum productTypeEnum, Boolean reInit);


    /**
     * 根据 ID 列表批量查 topic 模板字符串(北向桥接 TOPIC_IDS 模式用)。
     *
     * <p>fail-soft:入参空 / null 元素 / ID 不存在 / topic 字段空白都过滤,永不抛异常。
     * 结果自动去重,内部限制单次查询上限 500。
     *
     * @return 永不为 null;无命中返回空列表
     * @author mqttsnet
     * @since 2026-05-06
     */
    List<String> findTopicsByIds(List<Long> ids);


}


