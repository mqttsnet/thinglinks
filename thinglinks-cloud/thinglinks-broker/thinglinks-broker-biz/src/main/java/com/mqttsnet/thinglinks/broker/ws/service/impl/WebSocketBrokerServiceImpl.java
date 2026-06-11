package com.mqttsnet.thinglinks.broker.ws.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.broker.common.counter.DownLinkDataReportCounter;
import com.mqttsnet.thinglinks.broker.common.session.WsDeviceSessionRegistry;
import com.mqttsnet.thinglinks.broker.ws.service.WebSocketBrokerService;
import com.mqttsnet.thinglinks.broker.ws.session.WebSocketSubject;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.entity.ws.command.WsCommandBroadcastEvent;
import com.mqttsnet.thinglinks.entity.ws.protocol.WsCommandProtocolEncoder;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * WebSocketBroker API 实现类。
 *
 * <h3>下行命令:RocketMQ 广播</h3>
 * publishMessage 不再做 owner 路由(不存 ip:port、不点对点转发),改为:
 * <ol>
 *   <li>编码 ws 子协议报文(一次性,各节点直推同一份字节)</li>
 *   <li>在线校验:查 Redis session 信息,离线直接抛"设备不在线"(免无谓广播)</li>
 *   <li>RocketMQ 广播({@link BizMqRouteConstant.WebSocket#COMMAND_DOWNLINK} BROADCASTING)</li>
 * </ol>
 * 每个 broker 节点的 {@code WsCommandDownlinkListener} 收到后查本地 {@link WebSocketSubject.Holder},
 * 命中(持有该设备 TCP 的节点)才 {@link #publishLocal} 推 socket,其余节点忽略 —— 恰好一个节点投递。
 *
 * @author ShiHuan Sun
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebSocketBrokerServiceImpl implements WebSocketBrokerService {

    private final DownLinkDataReportCounter downLinkDataReportCounter;
    private final WsDeviceSessionRegistry wsDeviceSessionRegistry;
    private final ObjectProvider<RocketmqTemplate> rocketmqTemplateProvider;

    /**
     * 下行命令入口 ── 编码 + 在线校验 + 广播。
     *
     * @param publishMessageRequestVO 命令参数(含 clientId / topic / payload / reqId)
     * @return 已编码并广播的子协议 JSON
     * @throws BizException 设备不在线 / RocketMQ 不可用
     */
    @Override
    public String publishMessage(PublishWebSocketMessageRequestVO publishMessageRequestVO) throws BizException {
        String clientId = publishMessageRequestVO.getClientId();
        String topic = publishMessageRequestVO.getTopic();
        log.info("[ws-publish] preparing clientId={} topic={}", clientId, topic);

        if (StrUtil.isBlank(clientId)) {
            throw new BizException("clientId 不能为空");
        }

        Long tenantId = parseTenantIdLong(publishMessageRequestVO.getTenantId());
        // 下行数据下发计数(broker 自维护,旁路统计不影响主链路)
        downLinkDataReportCounter.incrementDownLink();

        // 1. 在线校验(查 Redis session 信息,租户用下行请求里的租户;离线直接返回,免无谓广播)
        if (!wsDeviceSessionRegistry.isOnline(tenantId, clientId)) {
            log.warn("[ws-publish] device offline (no session) clientId={}", clientId);
            throw new BizException("设备不在线: " + clientId);
        }

        // 2. 编码为 ws 子协议 JSON(一次性;本地直推 / 跨节点广播都用同一份)
        String json = encodeCommand(publishMessageRequestVO);

        // 3. 本地优先:设备就连在本节点 → 直接推 socket,免 RocketMQ 往返
        //    (单节点部署 / 设备恰在下发入口节点时,这条就够;也不依赖 RocketMQ 是否就绪)
        if (WebSocketSubject.Holder.getSubject().get(clientId) != null) {
            log.info("[ws-publish] deliver locally clientId={}", clientId);
            return publishLocal(clientId, json);
        }

        // 4. 设备连在其它 broker 节点 → RocketMQ 广播,由持有 session 的节点投递
        log.info("[ws-publish] not local, broadcast clientId={}", clientId);
        broadcast(publishMessageRequestVO, clientId, json);
        return json;
    }

    /**
     * 广播下行命令到所有 broker 节点(异步、失败仅 warn)。
     */
    private void broadcast(PublishWebSocketMessageRequestVO vo, String clientId, String encodedJson) {
        RocketmqTemplate rocketmqTemplate = rocketmqTemplateProvider.getIfAvailable();
        if (rocketmqTemplate == null) {
            throw new BizException("RocketmqTemplate not available; cannot broadcast downlink clientId=" + clientId);
        }
        WsCommandBroadcastEvent event = WsCommandBroadcastEvent.builder()
                .tenantId(vo.getTenantId())
                .clientId(clientId)
                .encodedMessage(encodedJson)
                .ts(System.currentTimeMillis())
                .build();
        rocketmqTemplate.asyncSend(BizMqRouteConstant.WebSocket.COMMAND_DOWNLINK, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                if (log.isDebugEnabled()) {
                    log.debug("[ws-publish] broadcast sent clientId={} msgId={}", clientId, sendResult.getMsgId());
                }
            }

            @Override
            public void onException(Throwable e) {
                log.warn("[ws-publish] broadcast failed (non-blocking) clientId={} cause={}",
                        clientId, e.getMessage());
            }
        });
    }

    /**
     * 本节点直推 ── BROADCASTING 消费者命中本地 session 时调用。
     * <p>用非创建式 {@code Holder.getSubject().get(clientId)} 探测:本节点未持有该 session 时返回 null
     * (广播场景下绝大多数节点都是这种情况),据此抛出"未找到",由消费者忽略。
     *
     * @param clientId    客户端 ID
     * @param encodedJson 已编码的子协议 JSON
     * @return 已发送的子协议 JSON
     */
    public String publishLocal(String clientId, String encodedJson) {
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject().get(clientId);
        if (subject == null) {
            throw new BizException("Local ws session not found: " + clientId);
        }
        try {
            subject.notify(encodedJson);
            log.info("[ws-publish] pushed locally clientId={}", clientId);
            return encodedJson;
        } catch (Exception e) {
            log.error("[ws-publish] local push failed clientId={} cause={}", clientId, e.getMessage(), e);
            throw new BizException("Push to local session failed: " + e.getMessage());
        }
    }

    private String encodeCommand(PublishWebSocketMessageRequestVO vo) {
        String messageId = vo.getReqId() == null ? null : String.valueOf(vo.getReqId());
        return messageId == null
                ? WsCommandProtocolEncoder.encodeDown(vo.getTopic(), vo.getPayload())
                : WsCommandProtocolEncoder.encodeDown(vo.getTopic(), vo.getPayload(), messageId);
    }

    private Long parseTenantIdLong(String tenantId) {
        if (StrUtil.isBlank(tenantId)) {
            return 0L;
        }
        try {
            return Long.parseLong(tenantId);
        } catch (NumberFormatException e) {
            return (long) tenantId.hashCode();
        }
    }
}
