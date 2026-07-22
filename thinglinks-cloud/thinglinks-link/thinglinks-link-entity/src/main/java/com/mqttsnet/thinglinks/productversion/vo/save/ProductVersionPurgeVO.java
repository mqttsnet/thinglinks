package com.mqttsnet.thinglinks.productversion.vo.save;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本历史清理请求 VO。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionPurgeVO", description = "产品版本历史清理请求")
public class ProductVersionPurgeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    @NotBlank
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productIdentification;

    /**
     * 待清理版本序号。
     */
    @NotBlank
    @Schema(description = "待清理版本序号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String versionNo;

    /**
     * 清理说明。
     */
    @Schema(description = "清理说明")
    private String purgeRemark;
}
