package com.mqttsnet.thinglinks.video.vo.query.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 级联平台表 分页查询对象
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
@EqualsAndHashCode
@Builder
@Schema(description = "级联平台表分页查询对象")
public class VideoPlatformPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "启用状态")
    private Boolean enable;

    @Schema(description = "上级平台国标编号")
    private String serverGbId;

    @Schema(description = "上级平台地址")
    private String serverIp;

    @Schema(description = "本平台国标编号")
    private String deviceGbId;

    @Schema(description = "传输协议")
    private String transport;

    @Schema(description = "状态")
    private Boolean status;

    @Schema(description = "级联类型")
    private Integer cascadeType;

    @Schema(description = "国标版本")
    private String gbVersion;

    @Schema(description = "创建组织")
    private Long createdOrgId;
}
