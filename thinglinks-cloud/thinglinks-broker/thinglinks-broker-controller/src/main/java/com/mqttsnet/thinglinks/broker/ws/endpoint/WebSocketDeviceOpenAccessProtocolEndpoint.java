package com.mqttsnet.thinglinks.broker.ws.endpoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.kafka.producer.KafkaProducerService;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.thinglinks.broker.common.session.WsDeviceSessionRegistry;
import com.mqttsnet.thinglinks.broker.ws.heartbeat.WsHeartbeatTracker;
import com.mqttsnet.thinglinks.broker.ws.session.WebSocketSubject;
import com.mqttsnet.thinglinks.common.cache.broker.ws.WsDeviceSessionInfo;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceAuthModeEnum;
import com.mqttsnet.thinglinks.device.vo.query.DeviceAuthenticationQuery;
import com.mqttsnet.thinglinks.entity.protocol.base.ClientConnectedEvent;
import com.mqttsnet.thinglinks.entity.protocol.base.ClientDisconnectEvent;
import com.mqttsnet.thinglinks.entity.protocol.base.DistErrorEvent;
import com.mqttsnet.thinglinks.entity.protocol.base.DistedEvent;
import com.mqttsnet.thinglinks.entity.protocol.base.PingReqEvent;
import com.mqttsnet.thinglinks.enumeration.QosEnum;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyTenantFacade;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceAuthenticationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceInfoResultVO;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * WebSocket 设备开放接入端点(broker 微服务)。
 *
 * <h3>架构定位</h3>
 * <ul>
 *   <li>broker = 协议接入层 ── 持有 ws session,处理 @OnOpen / @OnMessage / @OnClose</li>
 *   <li>mqs   = 业务处理层 ── 通过 Kafka 消费上行事件,做 DeviceAction 持久化 / 物模型 / 桥接旁路</li>
 * </ul>
 *
 * <h3>连接</h3>
 * 路径 {@code /anyUser/deviceOpenSocket/accessProtocol/socket/{tenantId}/{clientId}};
 * 租户 ID 由路径段直接带入(不再从 clientId 解析),username / password 走 query 参数做账号模式认证。
 * 结构参照 link 的 {@code QueryDeviceShadowEndpoint}:顶层 Configurator、onOpen 带 {@link EndpointConfig}。
 *
 * <h3>报文统一协议</h3>
 * <ul>
 *   <li>业务报文:{@code {"topic":"<ThingLinks Topic>","payload":"<统一消息体JSON字符串>"}} —— topic 与 payload 都在报文体里</li>
 *   <li>心跳报文:{@code {"type":"PING"}}</li>
 * </ul>
 *
 * <h3>本端点职责</h3>
 * <ol>
 *   <li>@OnOpen ── Feign 调 link 设备认证 → 注册本地 session(WebSocketSubject.Holder)+ 写 Redis session 信息(WsDeviceSessionRegistry,多节点共享)→ Kafka 投 ClientConnectedEvent</li>
 *   <li>@OnMessage ── 设备上行报文({@code {topic,payload}})封装为 DistedEvent → Kafka → mqs 消费</li>
 *   <li>@OnClose ── 清除本地 session + Redis session 信息 → Kafka 投 ClientDisconnectEvent</li>
 *   <li>@OnError ── Kafka 投 DistErrorEvent</li>
 * </ol>
 *
 * @author mqttsnet
 */
@ServerEndpoint(
    value = "/anyUser/deviceOpenSocket/accessProtocol/socket/{tenantId}/{clientId}",
    configurator = WsDeviceHandshakeConfigurator.class
)
@Component
@Slf4j
public class WebSocketDeviceOpenAccessProtocolEndpoint {

    /**
     * 上行 topic 兜底值 ── MQTT 多层通配 {@code #}。
     * <p>厂商私有报文(纯文本 / hex / 非约定 JSON)无 topic 时用它占位,交规则脚本(topicPattern={@code #})命中并转换。
     */
    private static final String FALLBACK_TOPIC = "#";

    /**
     * lazy 注入字段 ── jakarta WS 容器每 session 一个实例(非 Spring singleton),
     * 不能用 @Autowired;通过 {@link #initSpringBeans()} 首次回调时填充,后续复用。
     */
    private KafkaProducerService kafkaProducerService;
    private WsDeviceSessionRegistry wsDeviceSessionRegistry;
    private DeviceOpenAnyTenantFacade deviceOpenAnyTenantApi;
    private WsHeartbeatTracker wsHeartbeatTracker;

    /**
     * 首次回调懒加载 Spring bean,null-check 避免重复查 ApplicationContext。
     * <p>每个 @OnXxx 入口都调一次,兜底防 onOpen 被容器异常路径绕过。
     */
    private void initSpringBeans() {
        if (kafkaProducerService == null) {
            kafkaProducerService = SpringUtils.getBean(KafkaProducerService.class);
            wsDeviceSessionRegistry = SpringUtils.getBean(WsDeviceSessionRegistry.class);
            deviceOpenAnyTenantApi = SpringUtils.getBean(DeviceOpenAnyTenantFacade.class);
            wsHeartbeatTracker = SpringUtils.getBean(WsHeartbeatTracker.class);
        }
    }

    /**
     * 连接建立 ── 异步账号认证;通过后注册本地 / 跨节点 session、构建会话上下文、投 CONNECT 事件。
     *
     * @param session  WebSocket 会话
     * @param config   端点配置(承载握手阶段 Configurator 写入的 username / password)
     * @param tenantId 租户 ID(连接路径段直接带入)
     * @param clientId 客户端 ID(设备唯一标识)
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config,
                       @PathParam(CommonIotConstants.TENANT_ID) String tenantId,
                       @PathParam(CommonIotConstants.CLIENT_ID) String clientId) {
        initSpringBeans();
        log.info("WebSocket【DeviceOpenAccessProtocolEndpoint】连接成功, Session ID: {}, tenantId: {}, clientId: {}",
            session.getId(), tenantId, clientId);

        Map<String, Object> userProperties = config.getUserProperties();
        String username = (String) userProperties.get("username");
        String password = (String) userProperties.get("password");

        authenticate(clientId, username, password).thenAccept(authResult -> {
            if (authResult == null || !Boolean.TRUE.equals(authResult.getCertificationResult())) {
                log.info("认证失败,关闭连接。clientId: {}", clientId);
                closeQuietly(session, "认证失败");
                return;
            }
            log.info("认证成功,clientId: {}", clientId);

            // 1. 本节点 session 表(记 session 引用 + observer,后续 close 用)
            WebSocketSubject subject = WebSocketSubject.Holder.getSubject(clientId);
            subject.registerSession(session);

            // 2. 会话信息 ── 鉴权时一次性构建,随会话携带;后续每条上行直接取(clientId / 租户 / 设备标识),
            //    免去重复解析。信息存在即代表已认证,onMsg 凭此放行上行(替代独立的 wsAuthenticated 标记)。
            //    同一份信息既存本地 userProperties(鉴权门),也写 Redis(多节点共享设备渠道信息)。
            //    租户直接用连接路径段的 tenantId;topic 不在此 —— 它是逐条报文级字段({"topic","payload"} 里带)。
            long now = System.currentTimeMillis();
            DeviceInfoResultVO info = authResult.getDeviceInfoResult();
            WsDeviceSessionInfo ctx = WsDeviceSessionInfo.builder()
                .clientId(clientId)
                .tenantId(tenantId)
                .username(username)
                .deviceIdentification(info != null ? info.getDeviceIdentification() : null)
                .productIdentification(info != null ? info.getProductIdentification() : null)
                .protocol(ProtocolTypeEnum.WEBSOCKET.name())
                .channelId(session.getId())
                .connectTime(now)
                .lastActiveTime(now)
                .build();
            session.getUserProperties().put(WsDeviceSessionInfo.SESSION_KEY, ctx);

            // 3. 写 Redis session 信息(多节点共享:在线判断 / 渠道信息查询;不用于下行路由)
            wsDeviceSessionRegistry.save(ctx);

            // 4. Kafka 投连接成功事件
            publishClientConnected(session, ctx);
        }).exceptionally(ex -> {
            log.error("clientId: {} 认证过程中出错: {}", clientId, ex.getMessage());
            closeQuietly(session, "认证出错");
            return null;
        });
    }

    /**
     * 异步账号认证 ── Feign 调 link 设备认证 API,把结果(含 deviceInfoResult)透传回 onOpen 构建上下文。
     */
    private CompletableFuture<DeviceAuthenticationResultVO> authenticate(String clientId, String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("开始认证客户端 - clientId: {}, username: {}", clientId, username);
            DeviceAuthenticationQuery query = new DeviceAuthenticationQuery();
            query.setClientIdentifier(clientId);
            query.setUsername(username);
            query.setPassword(password);
            query.setAuthMode(DeviceAuthModeEnum.ACCOUNT_MODE.getValue());
            query.setProtocolType(ProtocolTypeEnum.WEBSOCKET.getValue());

            ResponseEntity<DeviceAuthenticationResultVO> resp = deviceOpenAnyTenantApi.clientConnectionAuthentication(query);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return resp.getBody();
            }
            log.error("认证失败,clientId: {},状态码: {}", clientId, resp.getStatusCode());
            return null;
        });
    }

    /**
     * 连接关闭 ── 清跨节点 owner、投 DISCONNECT 事件(仅认证通过的会话)、清本地 session。
     */
    @OnClose
    public void onClose(Session session,
                        @PathParam(CommonIotConstants.TENANT_ID) String tenantId,
                        @PathParam(CommonIotConstants.CLIENT_ID) String clientId) {
        initSpringBeans();
        log.info("WebSocket【DeviceOpenAccessProtocolEndpoint】连接关闭, Session ID: {}, tenantId: {}, clientId: {}",
            session.getId(), tenantId, clientId);

        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(clientId);
        // 信息存在 == 该会话曾认证通过(上过线);未认证会话 ctx 为 null
        WsDeviceSessionInfo ctx = (WsDeviceSessionInfo) session.getUserProperties().get(WsDeviceSessionInfo.SESSION_KEY);

        // 1. 清除 Redis session 信息(让多节点立刻知道设备离线;租户用连接路径段的租户)
        try {
            wsDeviceSessionRegistry.remove(parseTenantIdLong(tenantId), clientId);
        } catch (Exception e) {
            log.warn("[ws-session] remove failed (non-blocking) clientId={}", clientId, e);
        }

        // 2. Kafka 断开事件 ── 仅对认证通过(曾上线)的会话发;未认证会话从未发过 CONNECT,不发配对的 DISCONNECT
        if (ctx != null) {
            publishClientDisconnected(session, ctx);
        }

        // 3. 关闭 session + 清本地 subject
        if (subject != null) {
            subject.unregisterSession(session);
        }
        closeSilently(session);
        // 该 clientId 没其它 session 时移除整个 subject(多端复用同 clientId 时保留)
        if (subject != null && subject.getSessionCount() == 0) {
            WebSocketSubject.Holder.removeSubject(clientId);
        }
    }

    /**
     * 接收设备上行报文 ── 只做「鉴权门 + 心跳 + 分发」,具体投递拆到下面私有方法,便于维护。
     */
    @OnMessage
    public String onMsg(Session session, String text) {
        initSpringBeans();
        if (StrUtil.isEmpty(text)) {
            return "";
        }

        // 鉴权门:onOpen 认证是异步的,上下文存在 == 已认证。未认证(认证进行中 / 失败)一律丢弃上行,
        // 杜绝认证空窗期被冒充设备灌数据;clientId / 租户 / 设备标识 全部从上下文直接取,无需重复解析。
        WsDeviceSessionInfo ctx = (WsDeviceSessionInfo) session.getUserProperties().get(WsDeviceSessionInfo.SESSION_KEY);
        if (ctx == null) {
            log.warn("WebSocket onMsg 拒绝未认证会话上行, sessionId={}", session.getId());
            return "";
        }
        log.info("WebSocket onMsg sessionId={} clientId={} text={}", session.getId(), ctx.getClientId(), text);

        // 任何报文都视为活跃信号,先刷新心跳
        touchHeartbeat(ctx);

        String trimmed = text.trim();
        if (isPingMessage(trimmed)) {
            // 心跳:投 PING 事件,经 DevicePingProcessor 平台续命 + 在线校准(与 BifroMQ event 插件对齐)
            publishPing(session, ctx);
        } else {
            // 业务报文:统一协议 {"topic","payload"} → 投 Kafka 给 mqs(转换 / 物模型 / 时序入库 / 桥接)
            publishUplink(session, ctx, trimmed);
        }
        return "";
    }

    @OnError
    public void onError(Session session, Throwable error) {
        initSpringBeans();
        log.warn("WebSocket【DeviceOpenAccessProtocolEndpoint】连接 error, Session ID: {}",
            session == null ? "null" : session.getId(), error);
        if (session == null) {
            return;
        }
        try {
            WsDeviceSessionInfo ctx = (WsDeviceSessionInfo) session.getUserProperties().get(WsDeviceSessionInfo.SESSION_KEY);
            DistErrorEvent event = DistErrorEvent.builder()
                .tenantId(ctx != null ? ctx.getTenantId() : ContextConstants.BUILT_IN_TENANT_ID_STR)
                .clientId(ctx != null ? ctx.getClientId() : "")
                .event(DeviceActionTypeEnum.ERROR.getValue())
                .success("success")
                .errorMessage(error.getMessage())
                .build();
            kafkaProducerService.thingLinksKafkaTemplateSendMsg(
                KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_DISTRIBUTION_ERROR_TOPIC,
                JsonUtil.toJson(event));
        } catch (Exception e) {
            log.error("WebSocket onError 投递异常, Session ID: {}", session.getId(), e);
        } finally {
            closeSilently(session);
        }
    }

    /* ============================ 上行投递(私有,便于维护) ============================ */

    /**
     * 刷新会话存活(任何上行报文都调一次),失败不阻断上行。
     * <p>只更新本地 lastActiveTime + 跨节点广播(超时判活):{@link WsHeartbeatTracker#update}。
     * Redis session 信息的续期不在此(避免每条上行都写 Redis)── 由 {@code WsHeartbeatTimeoutChecker}
     * 每 30s 对本地在线会话统一重写(刷 TTL + 自愈),"本地有 session ⟺ Redis 有 session" 始终成立。
     */
    private void touchHeartbeat(WsDeviceSessionInfo ctx) {
        try {
            wsHeartbeatTracker.update(parseTenantIdLong(ctx.getTenantId()), ctx.getClientId());
        } catch (Exception e) {
            log.warn("[ws-hb] tracker update failed (non-blocking) clientId={} cause={}",
                ctx.getClientId(), e.getMessage());
        }
    }

    /**
     * PING 心跳 → Kafka(THINGLINKS_WEBSOCKET_PING_REQ_TOPIC)。
     */
    private void publishPing(Session session, WsDeviceSessionInfo ctx) {
        try {
            PingReqEvent ping = PingReqEvent.builder()
                .tenantId(ctx.getTenantId())
                .clientId(ctx.getClientId())
                .event(DeviceActionTypeEnum.PING.getValue())
                .success("success")
                .version(session.getProtocolVersion())
                .address(session.getRequestURI().getHost())
                .channelId(session.getId())
                .build();
            kafkaProducerService.thingLinksKafkaTemplateSendMsg(
                KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_PING_REQ_TOPIC,
                JsonUtil.toJson(ping));
        } catch (Exception e) {
            log.error("[ws] PING 投递 Kafka 失败 clientId={}", ctx.getClientId(), e);
        }
    }

    /**
     * 设备上行业务报文 → Kafka(THINGLINKS_WEBSOCKET_DISTRIBUTION_COMPLETED_TOPIC,与 MQTT 主通路对齐)。
     *
     * <p>报文统一协议:{@code {"topic":"<ThingLinks Topic>","payload":"<统一消息体JSON字符串>"}}。
     * topic 决定下游规则脚本 / 物模型 / 时序入库的路由;payload 才是真正的设备数据载荷(下游解码的对象)。
     * 非法报文(非 JSON / 缺 topic|payload)记日志丢弃,不阻断会话。
     */
    private void publishUplink(Session session, WsDeviceSessionInfo ctx, String text) {
        if (StrUtil.isBlank(text)) {
            return;
        }
        // 宽松解析:仅当是标准信封 {topic, payload} 才拆出 topic / payload;否则(纯文本 / hex /
        // 非约定 JSON)把整条原文当 payload、topic 兜底 #,交规则脚本(topicPattern=#)命中并转换为平台标准报文。
        // 绝不因"格式不符"丢弃 —— 厂商私有协议千差万别,转换正是规则脚本的职责。
        String topic = FALLBACK_TOPIC;
        String payload = text;
        Optional<JSONObject> envelope = parseJsonObject(text);
        if (envelope.isPresent()) {
            String envTopic = envelope.get().getString(CommonIotConstants.TOPIC);
            Object envPayload = envelope.get().get(CommonIotConstants.PAYLOAD);
            if (StrUtil.isNotBlank(envTopic) && envPayload != null) {
                topic = envTopic;
                payload = envPayload instanceof String ? (String) envPayload : JSON.toJSONString(envPayload);
            }
        }

        try {
            // payload 用 Base64(下游 decodeBody 统一 Base64.decode),payloadHex 保留原始字节供二进制脚本
            byte[] raw = payload.getBytes(StandardCharsets.UTF_8);
            DistedEvent event = DistedEvent.builder()
                .tenantId(ctx.getTenantId())
                .clientId(ctx.getClientId())
                // 与 MQTT 协议事件命名对齐:下游桥接 / 物模型 / 时序入库都把 ws 设备数据当 PUBLISH 处理
                .event(DeviceActionTypeEnum.PUBLISH.getValue())
                .success("success")
                .messageId(Long.valueOf(SnowflakeIdUtil.nextId()))
                .topic(topic)
                .qos(QosEnum.AT_MOST_ONCE.getValue())
                .payload(Base64.encode(raw))
                .payloadHex(HexUtil.encodeHexStr(raw))
                .encoding(StandardCharsets.UTF_8.name())
                .originalSize(raw.length)
                .build();
            kafkaProducerService.thingLinksKafkaTemplateSendMsg(
                KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_DISTRIBUTION_COMPLETED_TOPIC,
                JsonUtil.toJson(event));
        } catch (Exception e) {
            log.error("WebSocket onMsg 投递异常, sessionId={} clientId={}", session.getId(), ctx.getClientId(), e);
        }
    }

    /* ============================ 生命周期事件投递 ============================ */

    /**
     * 连接成功事件 → Kafka。
     */
    private void publishClientConnected(Session session, WsDeviceSessionInfo ctx) {
        ClientConnectedEvent event = ClientConnectedEvent.builder()
            .tenantId(ctx.getTenantId())
            .clientId(ctx.getClientId())
            .event(DeviceActionTypeEnum.CONNECT.getValue())
            .success("success")
            .version(session.getProtocolVersion())
            .userId(ctx.getUsername())
            .address(session.getRequestURI().getHost())
            .channelId(session.getId())
            .build();
        try {
            kafkaProducerService.thingLinksKafkaTemplateSendMsg(
                KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_CLIENT_CONNECTED_TOPIC,
                JsonUtil.toJson(event));
        } catch (Exception e) {
            log.error("发送连接成功设备事件消息时出错: {}", e.getMessage());
        }
    }

    /**
     * 断开事件 → Kafka。
     */
    private void publishClientDisconnected(Session session, WsDeviceSessionInfo ctx) {
        ClientDisconnectEvent event = ClientDisconnectEvent.builder()
            .tenantId(ctx.getTenantId())
            .clientId(ctx.getClientId())
            .event(DeviceActionTypeEnum.DISCONNECT.getValue())
            .success("success")
            .channelId(session.getId())
            .version(session.getProtocolVersion())
            .address(session.getRequestURI() != null ? session.getRequestURI().getHost() : "")
            .build();
        try {
            kafkaProducerService.thingLinksKafkaTemplateSendMsg(
                KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_CLIENT_DISCONNECTED_TOPIC,
                JsonUtil.toJson(event));
        } catch (Exception e) {
            log.error("发送断开设备事件消息时出错: {}", e.getMessage());
        }
    }

    /* ============================ 工具 ============================ */

    /**
     * 检测是否为 ws 子协议 PING 报文。
     * <p>简化判断:文本是 JSON 且含 {@code "type":"PING"} 即视为 PING(避免引入完整 JSON 解析开销)。
     */
    private boolean isPingMessage(String text) {
        if (text == null || text.length() > 200) {
            return false;
        }
        return text.contains("\"type\"") && text.contains("\"PING\"");
    }

    /**
     * 把字符串 tenantId 转 Long;非法 → hashCode 兜底(保持 owner key 一致性),空 → 0L。
     */
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

    /**
     * 宽松解析为 JSON 对象 ── 非 JSON / 非对象(纯文本、hex、JSON 数组 / 标量)一律返回 empty,
     * 供上行宽松判定:能解析出标准信封 {@code {topic, payload}} 才拆,否则整条原文作 payload。
     */
    private static Optional<JSONObject> parseJsonObject(String text) {
        try {
            return Optional.ofNullable(JSON.parseObject(text));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 认证失败 / 出错时带原因关闭。
     */
    private void closeQuietly(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
        } catch (IOException e) {
            log.error("关闭 WebSocket 连接时出错: {}", e.getMessage());
        }
    }

    /**
     * 清理时静默关闭(对齐 shadow:吞异常,避免抛 RuntimeException 导致容器持有 session 泄漏)。
     */
    private void closeSilently(Session session) {
        if (session == null) {
            return;
        }
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            log.warn("close ws session failed, sessionId={}", session.getId(), e);
        }
    }
}
