package com.mqttsnet.thinglinks.vo.query.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 表单查询条件 VO
 * 数据桥接-数据源
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "DataSourcePageQuery", description = "数据桥接-数据源 分页查询参数")
public class DataSourcePageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "数据源名称（模糊查询）")
    private String dataSourceName;

    @Schema(description = "业务唯一编码")
    private String dataSourceCode;

    @Schema(description = "方向（10/20/30）")
    private String direction;

    @Schema(description = "协议类型")
    private String sourceType;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "健康状态")
    private String healthStatus;
}
