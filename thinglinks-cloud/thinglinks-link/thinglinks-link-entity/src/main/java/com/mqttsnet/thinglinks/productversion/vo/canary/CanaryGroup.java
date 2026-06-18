package com.mqttsnet.thinglinks.productversion.vo.canary;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 灰度命中的分组快照 ── 记录发布那一刻所选分组的 id / 名称 / 当时设备数。
 * 既用于灰度配置({@link CanaryConfigDTO#getGroups()},冻结当时选择),也用于执行结果展示。
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "CanaryGroup", description = "灰度分组快照")
public class CanaryGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分组ID")
    private String groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "发布那一刻该分组的设备数")
    private Integer deviceCount;
}
