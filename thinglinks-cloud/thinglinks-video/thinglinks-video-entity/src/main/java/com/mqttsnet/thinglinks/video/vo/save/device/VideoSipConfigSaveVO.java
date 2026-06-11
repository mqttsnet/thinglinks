package com.mqttsnet.thinglinks.video.vo.save.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SIP 配置保存参数")
public class VideoSipConfigSaveVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称")
    private String configName;

    @NotBlank(message = "SIP 服务器编号不能为空")
    @Size(min = 20, max = 20, message = "SIP 服务器编号必须为 20 位")
    @Schema(description = "SIP 服务器编号（设备端 GB28181 配置里的「SIP 服务器编号」值，20 位数字，不能填设备自己的国标编号）")
    private String sipId;

    @NotBlank(message = "SIP 域不能为空")
    @Schema(description = "SIP 域（SIP 服务器编号前 10 位，行政区划码）")
    private String sipDomain;

    @Schema(description = "SIP 认证密码（与设备端「SIP 认证密码」一致，AES 加密存储）")
    private String sipPassword;

    @Schema(description = "SIP 服务器地址（设备端「SIP 服务器 IP / 地址」，可以是域名或 IP，集群部署可填 Nginx VIP）")
    private String sipServerAddress;

    @Schema(description = "绑定 IP（多网卡隔离，逗号分隔，留空表示不限制）")
    private String bindIp;

    @Schema(description = "是否默认(0=否/1=是)")
    private Integer isDefault;

    @Schema(description = "注册有效期(秒)")
    private Integer registerInterval;

    @Schema(description = "状态(0=禁用/1=启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
