package com.mqttsnet.thinglinks.video.vo.update.platform;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 级联平台目录表 更新对象
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
@Schema(description = "级联平台目录表更新对象")
public class VideoPlatformCatalogUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = SuperEntity.Update.class)
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

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "创建组织")
    private Long createdOrgId;
}
