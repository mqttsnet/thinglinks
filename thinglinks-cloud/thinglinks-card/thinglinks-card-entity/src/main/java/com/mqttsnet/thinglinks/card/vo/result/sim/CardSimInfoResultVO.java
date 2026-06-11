package com.mqttsnet.thinglinks.card.vo.result.sim;

import com.mqttsnet.thinglinks.card.vo.result.channel.CardChannelInfoResultVO;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询方法返回值VO
 * 物联网卡信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-06-26 23:45:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "物联网卡信息表")
public class CardSimInfoResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 国际移动用户识别码
     */
    @Schema(description = "国际移动用户识别码")
    private String imsi;
    /**
     * SIM卡唯一识别码
     */
    @Schema(description = "SIM卡唯一识别码")
    private String iccid;
    /**
     * 卡号
     */
    @Schema(description = "卡号")
    private String cardNumber;
    /**
     * 卡类型 0 插拔卡 1 贴片IC卡
     */
    @Schema(description = "卡类型 0 插拔卡 1 贴片IC卡")
    private Integer cardType;
    /**
     * 渠道ID
     */
    @Schema(description = "渠道ID")
    private Long channelId;
    /**
     * 已用流量
     */
    @Schema(description = "已用流量")
    private BigDecimal flowsUsed;
    /**
     * 总流量
     */
    @Schema(description = "总流量")
    private BigDecimal flowsTotal;
    /**
     * 本月剩余流量
     */
    @Schema(description = "本月剩余流量")
    private BigDecimal flowsRest;
    /**
     * 虚拟已用流量
     */
    @Schema(description = "虚拟已用流量")
    private BigDecimal virtualFlowsUsed;
    /**
     * 虚拟剩余流量
     */
    @Schema(description = "虚拟剩余流量")
    private BigDecimal virtualFlowsRest;
    /**
     * 虚拟总流量
     */
    @Schema(description = "虚拟总流量")
    private BigDecimal virtualFlowsTotal;
    /**
     * 是否有短信 0 无 1 有
     */
    @Schema(description = "是否有短信 0 无 1 有")
    private Integer smsFlag;
    /**
     * GPRS开关 0 关 1 开
     */
    @Schema(description = "GPRS开关 0 关 1 开")
    private Integer gprsFlag;
    /**
     * 开卡时间
     */
    @Schema(description = "开卡时间")
    private LocalDateTime openTime;
    /**
     * 最晚激活时间
     */
    @Schema(description = "最晚激活时间")
    private LocalDateTime lastOpenTime;
    /**
     * 激活时间
     */
    @Schema(description = "激活时间")
    private LocalDateTime startTime;
    /**
     * 失效时间
     */
    @Schema(description = "失效时间")
    private LocalDateTime endTime;
    /**
     * 流量到期时间
     */
    @Schema(description = "流量到期时间")
    private LocalDateTime flowsEndTime;
    /**
     * 运营商类型 1 移动 2 电信 3 联通
     */
    @Schema(description = "运营商类型 1 移动 2 电信 3 联通")
    private Integer carrierType;
    /**
     * 已发送短信数量
     */
    @Schema(description = "已发送短信数量")
    private Integer smsCount;
    /**
     * SIM卡状态 1 待激活 2 已激活 3 停机
     */
    @Schema(description = "SIM卡状态 1 待激活 2 已激活 3 停机")
    private Integer status;
    /**
     * 使用类型 1 普卡 2 共享池 3 流量池
     */
    @Schema(description = "使用类型 1 普卡 2 共享池 3 流量池")
    private Integer useType;
    /**
     * APN名称
     */
    @Schema(description = "APN名称")
    private String apnName;
    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;
    /**
     * 获取时间
     */
    @Schema(description = "获取时间")
    private LocalDateTime gainTime;
    /**
     * 在线状态 0 不在线 1 在线
     */
    @Schema(description = "在线状态 0 不在线 1 在线")
    private Integer onlineFlag;
    /**
     * 停卡类型 1 系统停卡 2 人工停卡 0 正常
     */
    @Schema(description = "停卡类型 1 系统停卡 2 人工停卡 0 正常")
    private Integer stopCardType;
    /**
     * 当月流量预警记录
     */
    @Schema(description = "当月流量预警记录")
    private String monthlyWarning;
    /**
     * 关联设备IMEI
     */
    @Schema(description = "关联设备IMEI")
    private String imei;
    /**
     * 流量限制阀值
     */
    @Schema(description = "流量限制阀值")
    private Double limitFlow;
    /**
     * 流量阀值状态 0 未开启 1 开启
     */
    @Schema(description = "流量阀值状态 0 未开启 1 开启")
    private Integer limitFlag;
    /**
     * 流量限制状态 0 未限制 1 已限制
     */
    @Schema(description = "流量限制状态 0 未限制 1 已限制")
    private Integer limitStatus;
    /**
     * 事务ID
     */
    @Schema(description = "事务ID")
    private Long offerId;
    /**
     * API是否可查 0 不可查 1 可查
     */
    @Schema(description = "API是否可查 0 不可查 1 可查")
    private Integer searchableStatus;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


    /**
     * 渠道详情
     */
    @Schema(description = "渠道详情")
    private CardChannelInfoResultVO cardChannelInfoResultVO;

}
