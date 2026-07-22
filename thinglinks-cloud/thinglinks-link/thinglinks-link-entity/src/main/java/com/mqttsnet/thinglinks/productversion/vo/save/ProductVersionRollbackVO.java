package com.mqttsnet.thinglinks.productversion.vo.save;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 产品版本回滚请求 VO。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionRollbackVO", description = "产品版本回滚请求")
public class ProductVersionRollbackVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 产品标识。 */
    @NotBlank
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productIdentification;

    /** 目标版本号(回滚到的版本)。 */
    @NotBlank
    @Schema(description = "目标版本号(要回滚到的历史版本)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetVersion;

    /** 回滚说明。 */
    @Schema(description = "回滚说明")
    private String rollbackRemark;
}
