package com.mqttsnet.thinglinks.broker.service;

import com.mqttsnet.thinglinks.broker.api.domain.vo.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.common.core.exception.base.BaseException;

/**
 * -----------------------------------------------------------------------------
 * File Name: MqttBrokerService.java
 * -----------------------------------------------------------------------------
 * Description:
 * MqttBroker API
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-10-31 19:43
 */
public interface MqttBrokerService {


    /**
     * Publishes a message to a specified topic and returns the content if successful.
     *
     * @param publishMessageRequestVO Object containing the required parameters for publishing.
     * @return The content of the published message.
     * @throws BaseException If the publishing fails.
     */
    String publishMessage(PublishMessageRequestVO publishMessageRequestVO);
}
