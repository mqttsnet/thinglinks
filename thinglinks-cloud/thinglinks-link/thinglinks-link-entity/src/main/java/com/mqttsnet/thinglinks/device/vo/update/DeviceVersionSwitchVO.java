package com.mqttsnet.thinglinks.device.vo.update;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备绑定版本切换请求 ── 把指定设备的 bound_product_version_no 切到目标版本。
 *
 * <p>影子发布的"外部切流"入口:影子发布只在 TD 预建好新版本的 super table、不自动改绑设备;由本请求把目标设备切到
 * 影子/目标版本后,上报热路径按新版本号路由,数据自然落到预建好的表。</p>
 *
 * <p>命中网关标识时,其子设备会一并跟随切换(保持"子设备版本=网关版本"不变式);故本接口面向直连设备与网关,
 * 不建议单独传子设备标识(会与其网关版本不一致)。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "DeviceVersionSwitchVO", description = "设备绑定版本切换请求")
public class DeviceVersionSwitchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 产品标识;必填 ── 收口改绑范围(只切该产品下设备),并据此校验目标版本归属 */
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "productIdentification 不能为空")
    private String productIdentification;

    /** 待切换的设备识别码集合;必填 ── 命中网关会连带其子设备一并切换 */
    @Schema(description = "待切换的设备识别码集合(命中网关会连带其子设备)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "deviceIdentifications 不能为空")
    private List<String> deviceIdentifications;

    /** 目标版本号;必填 ── 须为该产品下 已发布/灰度/影子 状态的版本(这些状态 TD 超表已建好,切过去才有表可写) */
    @Schema(description = "目标版本号(须为该产品下 已发布/灰度/影子 状态的版本)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "targetVersionNo 不能为空")
    private String targetVersionNo;
}
