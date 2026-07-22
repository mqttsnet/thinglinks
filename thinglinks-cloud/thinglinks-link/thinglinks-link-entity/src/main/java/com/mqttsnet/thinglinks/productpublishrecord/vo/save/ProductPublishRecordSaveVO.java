package com.mqttsnet.thinglinks.productpublishrecord.vo.save;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品发布记录保存 VO(SuperController 范型占位)。
 *
 * <p>记录由 publish / rollback / purge 流程自动生成,不开放外部 save。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductPublishRecordSaveVO", description = "发布记录保存(占位)")
public class ProductPublishRecordSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "备注")
    private String remark;
}
