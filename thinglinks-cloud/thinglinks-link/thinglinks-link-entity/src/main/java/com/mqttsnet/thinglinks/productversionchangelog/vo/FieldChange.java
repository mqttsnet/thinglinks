package com.mqttsnet.thinglinks.productversionchangelog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单字段变更明细 —— product_version_change_log.change_detail_json 数组元素。
 * 编辑时 before/after 均有值;新增时 before 为空;删除时 after 为空。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "FieldChange", description = "单字段变更明细")
public class FieldChange implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字段名。 */
    @Schema(description = "字段名")
    private String field;

    /** 字段中文标签(取自字段 {@code @Schema(description)})。 */
    @Schema(description = "字段中文标签")
    private String label;

    /** 变更前值。 */
    @Schema(description = "变更前值")
    private Object before;

    /** 变更后值。 */
    @Schema(description = "变更后值")
    private Object after;
}
