package com.mqttsnet.thinglinks.video.vo.result.device;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SIP 配置结果")
public class VideoSipConfigResultVO implements Serializable, EchoVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "SIP 服务器编号（设备端 GB28181 配置里的「SIP 服务器编号」值，20 位数字）")
    private String sipId;

    @Schema(description = "SIP 域")
    private String sipDomain;

    @Schema(description = "SIP 认证密码（脱敏）")
    private String sipPassword;

    @Schema(description = "SIP 服务器地址")
    private String sipServerAddress;

    @Schema(description = "绑定 IP")
    private String bindIp;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "注册有效期(秒)")
    private Integer registerInterval;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Echo(api = EchoApi.ORG_ID_CLASS)
    @Schema(description = "创建组织")
    private Long createdOrgId;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    @Schema(description = "最后修改人")
    private Long updatedBy;
}
