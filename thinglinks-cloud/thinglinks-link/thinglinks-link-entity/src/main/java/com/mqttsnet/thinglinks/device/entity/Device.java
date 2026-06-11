package com.mqttsnet.thinglinks.device.entity;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;
import java.time.LocalDateTime;

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
 * 设备档案信息表实体。
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "device", autoResultMap = true)
public class Device extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端标识
     */
    @TableField(value = "client_id", condition = EQUAL)
    private String clientId;
    /**
     * 用户名
     */
    @TableField(value = "user_name", condition = EQUAL, typeHandler = EncryptTypeHandler.class)
    private String userName;
    /**
     * 密码
     */
    @TableField(value = "password", condition = EQUAL, typeHandler = EncryptTypeHandler.class)
    private String password;
    /**
     * 证书序列号
     */
    @TableField(value = "cert_serial_number", condition = EQUAL)
    private String certSerialNumber;
    /**
     * 应用ID
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;
    /**
     * 认证方式0-用户名密码，1-ssl证书
     */
    @TableField(value = "auth_mode", condition = EQUAL)
    private Integer authMode;
    /**
     * 加密密钥
     */
    @TableField(value = "encrypt_key", condition = EQUAL, typeHandler = EncryptTypeHandler.class)
    private String encryptKey;
    /**
     * 加密向量
     */
    @TableField(value = "encrypt_vector", condition = EQUAL, typeHandler = EncryptTypeHandler.class)
    private String encryptVector;
    /**
     * 签名密钥
     */
    @TableField(value = "sign_key", condition = EQUAL, typeHandler = EncryptTypeHandler.class)
    private String signKey;
    /**
     * 传输协议的加密方式：0-明文传输、1-SM4、2-AES
     */
    @TableField(value = "encrypt_method", condition = EQUAL)
    private Integer encryptMethod;
    /**
     * 设备标识
     */
    @TableField(value = "device_identification", condition = EQUAL)
    private String deviceIdentification;
    /**
     * 设备名称
     */
    @TableField(value = "device_name", condition = EQUAL, typeHandler = EncryptTypeHandler.class)
    private String deviceName;
    /**
     * 连接实例
     */
    @TableField(value = "connector", condition = LIKE)
    private String connector;
    /**
     * 设备描述
     */
    @TableField(value = "description", condition = LIKE)
    private String description;
    /**
     * 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
     */
    @TableField(value = "device_status", condition = EQUAL)
    private Integer deviceStatus;
    /**
     * 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
     */
    @TableField(value = "connect_status", condition = EQUAL)
    private Integer connectStatus;

    /**
     * 最近一次连接状态事件的因果时钟(HLC,64-bit),作 connect_status 的 event-time LWW CAS 对比基准:
     * 仅当 DB 内本字段严格小于新事件 hlc 才允许覆盖 connect_status,防止异步消费/乱序/抖动重连导致状态回退。
     * 非事件驱动的更新(运维强制下线 / xxl-job 探活补偿等)不更新本字段。
     */
    @TableField(value = "last_status_event_hlc")
    private Long lastStatusEventHlc;

    /**
     * 最新心跳时间
     */
    @TableField(value = "last_heartbeat_time", condition = EQUAL)
    private LocalDateTime lastHeartbeatTime;

    /**
     * 设备标签
     */
    @TableField(value = "device_tags", condition = LIKE)
    private String deviceTags;
    /**
     * 产品标识
     */
    @TableField(value = "product_identification", condition = LIKE)
    private String productIdentification;
    /**
     * 软件版本
     */
    @TableField(value = "sw_version", condition = LIKE)
    private String swVersion;
    /**
     * 固件版本
     */
    @TableField(value = "fw_version", condition = LIKE)
    private String fwVersion;
    /**
     * sdk版本
     */
    @TableField(value = "device_sdk_version", condition = LIKE)
    private String deviceSdkVersion;
    /**
     * 子设备所属网关的 deviceIdentification(业务唯一标识 String,不是网关实体的主键 id),仅 nodeType=SUBDEVICE 时有值。
     * 字段名叫 gatewayId 但语义是 gatewayDeviceIdentification:须用 getDeviceDetailsByIdentification(gatewayId) 换网关详情,
     * 不能用 getDeviceDetails(id) 这类按主键查询的接口。
     */
    @TableField(value = "gateway_id", condition = LIKE)
    private String gatewayId;
    /**
     * 设备类型:0普通设备 || 1网关设备 || 2子设备
     */
    @TableField(value = "node_type", condition = EQUAL)
    private Integer nodeType;
    /**
     * 备注
     */
    @TableField(value = "remark", condition = LIKE)
    private String remark;
    /**
     * 创建人组织
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;
    /**
     * 绑定的产品版本序号(对应 product_version.version_no),数据上报路径的物模型解析依据。
     */
    @TableField(value = "bound_product_version_no", condition = EQUAL)
    private String boundProductVersionNo;
    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;


}
