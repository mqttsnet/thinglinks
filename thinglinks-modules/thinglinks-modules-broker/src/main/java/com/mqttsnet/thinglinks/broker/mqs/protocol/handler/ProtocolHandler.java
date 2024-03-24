package com.mqttsnet.thinglinks.broker.mqs.protocol.handler;


import com.mqttsnet.thinglinks.link.api.domain.product.enumeration.ProtocolTypeEnum;

/**
 * -----------------------------------------------------------------------------
 * File Name: ProtocolHandler
 * -----------------------------------------------------------------------------
 * Description:
 * 协议接口
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/3/11       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 1373398655@163.com
 * @date 2024/3/11 14:55
 */
public interface ProtocolHandler {

    /**
     * 处理特定协议的消息
     *
     * @param message 消息内容
     */
    void processMessage(String message);

    /**
     * 获取协议类型枚举
     *
     * @return 协议类型
     */
    ProtocolTypeEnum getProtocolTypeEnum();

}
