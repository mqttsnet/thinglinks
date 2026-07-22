package com.mqttsnet.thinglinks.sop.vo.result;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
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
import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询方法返回值VO
 * 回调信息
 * </p>
 *
 * @author zuihou
 * @date 2025-12-17 15:38:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "回调信息")
public class SopNotifyInfoResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "")
    private Long id;

    /**
     * app_id
     */
    @Schema(description = "app_id")
    private String appId;
    /**
     * api_name
     */
    @Schema(description = "api_name")
    private String apiName;
    /**
     * api_version
     */
    @Schema(description = "api_version")
    private String apiVersion;
    /**
     * 回调url
     */
    @Schema(description = "回调url")
    private String notifyUrl;
    /**
     * 最近一次发送时间
     */
    @Schema(description = "最近一次发送时间")
    private LocalDateTime lastSendTime;
    /**
     * 下一次发送时间
     */
    @Schema(description = "下一次发送时间")
    private LocalDateTime nextSendTime;
    /**
     * 最大发送次数
     */
    @Schema(description = "最大发送次数")
    private Integer sendMax;
    /**
     * 已发送次数
     */
    @Schema(description = "已发送次数")
    private Integer sendCnt;
    /**
     * 发送内容
     */
    @Schema(description = "发送内容")
    private String content;
    /**
     * 状态
     * [1-发送成功,2-发送失败,3-重试结束]
     */
    @Schema(description = "状态")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.SopAdmin.NotifyStatusEnum)
    private String notifyStatus;
    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String errorMsg;
    /**
     * 返回结果
     */
    @Schema(description = "返回结果")
    private String resultContent;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;


}
