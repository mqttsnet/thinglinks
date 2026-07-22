package com.mqttsnet.thinglinks.vo.result.linkage;

import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRenderedNotificationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 规则通知模板预览结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleNotificationPreviewResultVO", description = "规则通知模板预览结果")
public class RuleNotificationPreviewResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道预览列表")
    private List<RuleAlarmRenderedNotificationDTO> channels;
}
