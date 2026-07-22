package com.mqttsnet.thinglinks.productpublishrecord.vo.update;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品发布记录更新 VO(SuperController 范型占位)。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductPublishRecordUpdateVO", description = "发布记录更新")
public class ProductPublishRecordUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "备注")
    private String remark;
}
