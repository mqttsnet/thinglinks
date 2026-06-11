package com.mqttsnet.thinglinks.video.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 租户 SIP 配置 Redis 缓存 VO。
 * <p>
 * 存储在全局 Hash 中（参考 DictCacheKeyBuilder 模式），tenantId 在 value 里。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSipConfigCacheVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属租户 ID */
    private Long tenantId;
    /** SIP 服务器编号（设备端 GB28181 配置里的「SIP 服务器编号」值，20 位数字） */
    private String sipId;
    /** SIP 域（SIP 服务器编号前 10 位） */
    private String sipDomain;
    /** SIP 认证密码 */
    private String sipPassword;
    /** SIP 服务器地址（设备端「SIP 服务器 IP / 地址」） */
    private String sipServerAddress;
    /** 绑定 IP（多网卡隔离） */
    private String bindIp;
    /** 是否默认 */
    private Integer isDefault;
    /** 注册有效期 */
    private Integer registerInterval;
    /** 配置名称 */
    private String configName;
}
