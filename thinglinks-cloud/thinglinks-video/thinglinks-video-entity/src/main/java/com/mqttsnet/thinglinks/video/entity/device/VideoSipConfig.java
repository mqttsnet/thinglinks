package com.mqttsnet.thinglinks.video.entity.device;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.mybatis.typehandler.EncryptTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Description:
 * 租户 SIP 服务配置实体。
 * <p>
 * 每个租户可以有多条 SIP 配置（多域接入），通过 {@code isDefault} 标记默认配置。
 * {@code sipId} 全局唯一（跨租户不重复），通过 Redis Hash 校验。
 * {@code sipPassword} 使用 AES 加密存储。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_sip_config", autoResultMap = true)
public class VideoSipConfig extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置名称(如: 北京公安域)
     */
    @TableField(value = "config_name", condition = LIKE)
    private String configName;

    /**
     * SIP 服务器编号（设备端 GB28181 配置中的「SIP 服务器编号」对应值，20 位数字）。
     * <p>全局唯一（跨租户不重复），通过 Redis Hash 校验。
     * <p>注意：这是<b>本平台对外暴露的 SIP 服务器编号</b>，所有接入本配置的 GB28181 设备必须把此值填入设备端
     * 「SIP 服务器编号」字段；不要错填成设备自己的国标编号，否则出站 INVITE 的 From 用户名会与设备自身重叠，
     * 设备会在 200 OK 之后立即回 BYE 拆 dialog。
     */
    @TableField(value = "sip_id", condition = EQUAL)
    private String sipId;

    /**
     * SIP 域
     */
    @TableField(value = "sip_domain", condition = EQUAL)
    private String sipDomain;

    /**
     * SIP 认证密码(AES 加密存储)
     */
    @TableField(value = "sip_password", typeHandler = EncryptTypeHandler.class)
    private String sipPassword;

    /**
     * SIP 接入地址(域名或 IP, 供设备端配置使用, 如 sip-bj.example.com)
     */
    @TableField(value = "sip_server_address", condition = LIKE)
    private String sipServerAddress;

    /**
     * 绑定 IP(多网卡隔离, 逗号分隔, 空=不限制)
     */
    @TableField(value = "bind_ip")
    private String bindIp;

    /**
     * 是否默认(1=是, 出站消息用, 每租户仅一条)
     */
    @TableField(value = "is_default", condition = EQUAL)
    private Integer isDefault;

    /**
     * 注册有效期(秒, 空=用全局默认)
     */
    @TableField(value = "register_interval")
    private Integer registerInterval;

    /**
     * 状态(0=禁用/1=启用)
     */
    @TableField(value = "status", condition = EQUAL)
    private Integer status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建人组织
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
