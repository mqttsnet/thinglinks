package com.mqttsnet.thinglinks.video.vo.update.device;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "SIP 配置更新参数")
public class VideoSipConfigUpdateVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @NotNull(message = "请填写id", groups = SuperEntity.Update.class)
    private Long id;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "SIP 服务器编号（设备端 GB28181 配置里的「SIP 服务器编号」值，20 位数字，不能填设备自己的国标编号）")
    private String sipId;

    @Schema(description = "SIP 域（SIP 服务器编号前 10 位）")
    private String sipDomain;

    @Schema(description = "SIP 认证密码（与设备端「SIP 认证密码」一致）")
    private String sipPassword;

    @Schema(description = "SIP 服务器地址（设备端「SIP 服务器 IP / 地址」）")
    private String sipServerAddress;

    @Schema(description = "绑定 IP（多网卡隔离，逗号分隔）")
    private String bindIp;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "注册有效期(秒)")
    private Integer registerInterval;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
