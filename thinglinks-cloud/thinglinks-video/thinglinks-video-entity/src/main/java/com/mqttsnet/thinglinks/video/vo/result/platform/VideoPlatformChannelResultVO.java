package com.mqttsnet.thinglinks.video.vo.result.platform;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 级联平台通道关联表 返回结果对象
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-01 00:00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "级联平台通道关联表返回结果对象")
public class VideoPlatformChannelResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "平台ID")
    private Long platformId;

    @Schema(description = "设备通道ID")
    private Long deviceChannelId;

    @Schema(description = "目录ID")
    private Long catalogId;

    @Schema(description = "设备唯一标识")
    private String deviceIdentification;

    @Schema(description = "通道唯一标识")
    private String channelIdentification;

    @Schema(description = "自定义名称")
    private String customName;

    @Schema(description = "自定义国标编号")
    private String customGbId;

    @Schema(description = "删除标记")
    private Integer deleted;
}
