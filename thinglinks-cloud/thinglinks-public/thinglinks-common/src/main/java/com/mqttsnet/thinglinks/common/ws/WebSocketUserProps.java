package com.mqttsnet.thinglinks.common.ws;

/**
 * WebSocket {@link jakarta.websocket.EndpointConfig#getUserProperties()} 中
 * 由 {@link WebSocketAuthHeaderCaptor} 写入的 key 常量,业务侧 onOpen 读取时一致使用。
 * 这些 key 跨模块共享(common 写、各业务 endpoint 读),统一在此声明避免散落各处拼错。
 *
 * @author mqttsnet
 */
public final class WebSocketUserProps {

    private WebSocketUserProps() {
    }

    /** 登录用户 ID(网关 TokenContextFilter 解析 token 后写入 X-User-Id header,握手时被捕获)。 */
    public static final String LOGIN_USER_ID = "WS_LOGIN_USER_ID";

    /** 登录用户租户 ID(同上,header 名 X-Tenant-Id)。 */
    public static final String LOGIN_TENANT_ID = "WS_LOGIN_TENANT_ID";

    /** 远端 IP(优先取 X-Forwarded-For 首段,审计 / 限流定位用)。 */
    public static final String REMOTE_ADDR = "WS_REMOTE_ADDR";

    /** User-Agent(审计用,便于区分浏览器 / 自动化客户端)。 */
    public static final String USER_AGENT = "WS_USER_AGENT";
}
