package com.mqttsnet.thinglinks.productversion.vo.update;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品物模型版本更新 VO(SuperController 范型占位)。
 *
 * <p>实际状态变更由 /productVersion/publish|rollback|purgeHistory 接管,
 * 默认 update 接口仅允许调整 remark。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionUpdateVO", description = "版本更新(仅备注)")
public class ProductVersionUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "备注")
    private String remark;
}
