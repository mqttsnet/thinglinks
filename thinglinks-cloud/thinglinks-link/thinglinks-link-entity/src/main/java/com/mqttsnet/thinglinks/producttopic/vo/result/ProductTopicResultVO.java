package com.mqttsnet.thinglinks.producttopic.vo.result;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 表单查询方法返回值VO
 * 产品Topic信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ProductTopicResultVO", description = "产品Topic信息表")
public class ProductTopicResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    private String productIdentification;

    /**
     * 功能类型
     */
    @Schema(description = "功能类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_TOPIC_FUNCTION_TYPE)
    private Integer functionType;

    /**
     * Topic类型(0:基础Topic,1:自定义Topic)
     */
    @Schema(description = "Topic类型(0:基础Topic,1:自定义Topic)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_TOPIC_TYPE)
    private Integer topicType;
    /**
     * topic
     */
    @Schema(description = "topic")
    private String topic;
    /**
     * 发布者
     */
    @Schema(description = "发布者")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_TOPIC_PUBLISHER)
    private Integer publisher;
    /**
     * 订阅者
     */
    @Schema(description = "订阅者")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_TOPIC_SUBSCRIBER)
    private Integer subscriber;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
