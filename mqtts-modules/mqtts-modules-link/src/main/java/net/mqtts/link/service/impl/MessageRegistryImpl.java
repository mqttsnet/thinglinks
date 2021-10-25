package net.mqtts.link.service.impl;

import io.github.quickmsg.common.message.MessageRegistry;
import io.github.quickmsg.common.message.RetainMessage;
import io.github.quickmsg.common.message.SessionMessage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 主要提供 session消息 && 保留消息的持久化
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/25$ 17:35$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/25$ 17:35$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
public class MessageRegistryImpl implements MessageRegistry {
    /**
     * 获取连接下线后的session消息
     *
     * @param clientIdentifier 设备id
     * @return {@link SessionMessage}
     */
    @Override
    public List<SessionMessage> getSessionMessage(String clientIdentifier) {
        return null;
    }

    /**
     * 发送连接下线后的session消息
     *
     * @param sessionMessage {@link SessionMessage}
     */
    @Override
    public void saveSessionMessage(SessionMessage sessionMessage) {

    }

    /**
     * 保留Topic保留消息
     *
     * @param retainMessage {@link RetainMessage}
     */
    @Override
    public void saveRetainMessage(RetainMessage retainMessage) {

    }

    /**
     * 保留Topic保留消息
     *
     * @param topic topic
     * @return {@link RetainMessage}
     */
    @Override
    public List<RetainMessage> getRetainMessage(String topic) {
        return null;
    }
}
