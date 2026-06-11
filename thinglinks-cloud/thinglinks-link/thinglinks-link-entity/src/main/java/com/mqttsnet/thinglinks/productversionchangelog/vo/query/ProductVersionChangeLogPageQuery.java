package com.mqttsnet.thinglinks.productversionchangelog.vo.query;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 产品物模型版本变更日志分页查询参数。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder(toBuilder = true)
@Schema(title = "ProductVersionChangeLogPageQuery", description = "版本变更日志分页查询参数")
public class ProductVersionChangeLogPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    @Schema(description = "产品标识")
    private String productIdentification;

    /**
     * 版本序号(过滤指定快照下的变更日志)。
     */
    @Schema(description = "版本序号(过滤指定快照下的变更日志)")
    private String versionNo;

    /**
     * 变更类型(0-新增,1-编辑,2-删除)。
     */
    @Schema(description = "变更类型")
    private Integer changeType;
}
