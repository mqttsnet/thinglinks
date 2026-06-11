package com.mqttsnet.thinglinks.productversion.vo.save;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本发布请求 VO。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionPublishVO", description = "产品版本发布请求")
public class ProductVersionPublishVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    @NotBlank
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productIdentification;

    /**
     * 发布策略(0-全量,1-灰度,2-影子)。
     */
    @NotNull
    @Schema(description = "发布策略(0-全量,1-灰度,2-影子)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer publishStrategy;

    /**
     * 灰度配置 JSON(仅 publishStrategy=1 时必填)。
     */
    @Schema(description = "灰度配置 JSON")
    private String canaryConfigJson;

    /**
     * 发布说明。
     */
    @Schema(description = "发布说明")
    private String publishRemark;
}
