package com.mqttsnet.thinglinks.video.vo.query.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(description = "SIP 配置分页查询")
public class VideoSipConfigPageQuery implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置名称（模糊匹配）")
    private String configName;

    @Schema(description = "SIP 服务器编号")
    private String sipId;

    @Schema(description = "SIP 域")
    private String sipDomain;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建组织")
    private Long createdOrgId;
}
