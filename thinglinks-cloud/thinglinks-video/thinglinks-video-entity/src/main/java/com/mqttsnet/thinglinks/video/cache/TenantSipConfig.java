package com.mqttsnet.thinglinks.video.cache;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

/**
 * Description:
 * 租户 SIP 配置值对象（组合租户业务参数 + 物理层参数）。
 * <p>
 * 供 SipMessageBuilder、Commander 等使用，替代原 SipConfig 全局单例。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSipConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // === 租户级（从 Redis/DB） ===
    private String sipId;
    private String domain;
    private String password;
    private String sipServerAddress;
    private String bindIp;
    private Integer registerInterval;

    // === 物理层（从 SipLayer 运行时） ===
    private String host;
    private Integer port;

    /**
     * 获取本平台"对外身份"地址（用于 Via / Contact / CallId / 发送 Provider 选择）。
     * <p>
     * 选择优先级（从高到低）：
     * <ol>
     *   <li>{@code bindIp} 显式配置（运维强制指定，多网卡精细隔离场景）</li>
     *   <li>{@code sipServerAddress} 即"对外服务地址"——这是 SIP 接入配置里"接入地址"字段，
     *       也是该 SIP 域对外宣告的可达地址。支持逗号分隔多值，当前取第一个；后续 PR
     *       会改走 SipEgressResolver + SipDispatcher 做请求级轮询。</li>
     *   <li>{@code host}——SipLayer 扫到的第一张物理网卡（auto-scan 模式兜底，仅在前两者皆空时使用）</li>
     * </ol>
     * <p>
     * <b>常见部署场景（sipServerAddress 的取值规则）：</b>
     * <ul>
     *   <li><b>本机单网卡直接部署</b>：填本机 LAN IP（如 192.168.1.84）。Provider 精确命中，直接从该卡发包。</li>
     *   <li><b>多网卡物理机 / macOS 带 utun 等虚拟卡</b>：必须填 {@code sipServerAddress}，
     *       让 Via/Contact/CallId 落到正确卡上；SipLayer 扫网卡阶段已过滤 utun/100.64/198.18
     *       等虚拟段兜底，但显式配置仍是最可靠的。</li>
     *   <li><b>云服务器（ECS/EIP）多网卡</b>：填 <b>公网 IP 或 LB IP</b>。云 IaaS 通常把 EIP 以 NAT 方式挂
     *       到内网卡（eth0 只能看到内网 IP），本机网卡列表里不会有公网 IP。此时 SipLayer
     *       {@code getUdpSipProvider} 精确匹配失败 → fallback 到本机任意 Provider（内网卡）→
     *       包从内网卡出去，经云 NAT 源地址改写为公网 IP 发出 → 摄像头收到 Via=公网IP 的 INVITE →
     *       回 200 OK 给公网 IP → 云 NAT 反向映射到内网卡 → 平台监听点收到。整条链路依赖云 NAT
     *       透传，与本地多网卡不同，<b>fallback 是预期行为，不是错误</b>。</li>
     *   <li><b>Docker bridge + 端口映射</b>：填宿主机对外 IP。容器内 Provider 从 {@code eth0}（172.17.x）
     *       发包，Docker 的 iptables MASQUERADE 把源 IP 改为宿主 IP；回包经 {@code -p 5060:5060/udp}
     *       反向映射到容器。</li>
     *   <li><b>K8s NodePort / LB</b>：填节点公网 IP 或 LB IP。注意：SIP 端口可能被 NodePort 映射
     *       （外 30060 → Pod 5060），此时摄像头配置端口应填 30060，平台 {@code port} 字段也应该是 30060，
     *       当前代码 {@code port} 来自 {@code sipConfig.port}（默认 5060），K8s 完整适配留待后续 PR。</li>
     * </ul>
     * <p>
     * 语义修正记录：本字段原本只用 {@code bindIp || host}，导致 macOS utun 等虚拟卡
     * 被扫进 monitorIps 后污染 Via/Contact，设备无法回响应造成点播 10s 超时。
     */
    public String getEffectiveHost() {
        return pickFirstCsv(bindIp)
                .or(() -> pickFirstCsv(sipServerAddress))
                .orElse(host);
    }

    /**
     * 从逗号分隔字符串中选第一个非空元素（自动 trim）。
     * <p>
     * 对以下异常输入全部安全返回 {@link Optional#empty()}：
     * <ul>
     *   <li>{@code null}</li>
     *   <li>空串 / 全空白</li>
     *   <li>{@code ",,,"}</li>
     *   <li>{@code " , \t, "}</li>
     *   <li>{@code ",192.168.1.84"}（返 {@code 192.168.1.84}）</li>
     * </ul>
     */
    private static Optional<String> pickFirstCsv(String csv) {
        if (StrUtil.isBlank(csv)) {
            return Optional.empty();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .findFirst();
    }

    /**
     * 从 CacheVO + 物理参数构建
     */
    public static TenantSipConfig of(TenantSipConfigCacheVO cache, String monitorIp, Integer port) {
        return TenantSipConfig.builder()
                .sipId(cache.getSipId())
                .domain(cache.getSipDomain())
                .password(cache.getSipPassword())
                .sipServerAddress(cache.getSipServerAddress())
                .bindIp(cache.getBindIp())
                .registerInterval(cache.getRegisterInterval())
                .host(monitorIp)
                .port(port)
                .build();
    }
}
