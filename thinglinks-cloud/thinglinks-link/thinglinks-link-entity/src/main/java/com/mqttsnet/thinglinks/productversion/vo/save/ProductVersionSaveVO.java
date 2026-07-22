package com.mqttsnet.thinglinks.productversion.vo.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 产品物模型版本保存 VO(SuperController 范型占位)。
 *
 * <p>实际版本创建由 /productVersion/publish 接口接管,本 VO 仅满足
 * {@link com.mqttsnet.basic.base.controller.SuperController} 范型签名,
 * 默认 save 接口对用户不可用。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionSaveVO", description = "版本保存(占位)")
public class ProductVersionSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "备注")
    private String remark;
}
