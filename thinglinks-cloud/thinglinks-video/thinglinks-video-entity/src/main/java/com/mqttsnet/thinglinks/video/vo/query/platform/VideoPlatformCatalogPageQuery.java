package com.mqttsnet.thinglinks.video.vo.query.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 级联平台目录表 分页查询对象
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-01 00:00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(description = "级联平台目录表分页查询对象")
public class VideoPlatformCatalogPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "平台ID")
    private Long platformId;

    @Schema(description = "目录名称")
    private String name;

    @Schema(description = "国标编号")
    private String gbId;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "目录类型")
    private Integer catalogType;

    @Schema(description = "行政区划")
    private String civilCode;
}
