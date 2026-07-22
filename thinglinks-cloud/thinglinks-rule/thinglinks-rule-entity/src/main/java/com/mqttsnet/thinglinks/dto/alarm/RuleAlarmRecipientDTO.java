package com.mqttsnet.thinglinks.dto.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 告警通知接收人。
 *
 * <p>PHONE 用于机器人 @ 手机号; EMPLOYEE 用于站内信接收人。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleAlarmRecipientDTO", description = "告警通知接收人")
public class RuleAlarmRecipientDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接收人类型: PHONE/EMPLOYEE/ALL")
    private String type;

    @Schema(description = "接收人值: 手机号、员工ID或all")
    private String value;

    @Schema(description = "前端展示名称")
    private String label;
}
