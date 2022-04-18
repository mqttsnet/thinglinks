package com.mqttsnet.thinglinks.common.rocketmq.domain;

import lombok.Data;

/**
 * @Description: MQ消息
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/15$ 16:15$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/15$ 16:15$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Data
public class MQMessage {
    private static final long serialVersionUID = 1L;

    /**
     * 主题
     */
    private String topic;

    /**
     * 消息
     */
    private String message;
}
