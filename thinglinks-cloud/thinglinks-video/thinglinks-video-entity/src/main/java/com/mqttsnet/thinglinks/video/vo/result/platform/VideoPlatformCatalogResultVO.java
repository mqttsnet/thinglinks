package com.mqttsnet.thinglinks.video.vo.result.platform;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 级联平台目录表 返回结果对象
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "级联平台目录表返回结果对象")
public class VideoPlatformCatalogResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "平台ID")
    private Long platformId;

    @Schema(description = "目录名称")
    private String name;

    @Schema(description = "国标编号")
    private String gbId;

    @Schema(description = "父级ID")
    private Long parentId;

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_PLATFORM_CATALOG_TYPE)
    @Schema(description = "目录类型")
    private Integer catalogType;

    @Schema(description = "行政区划")
    private String civilCode;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "删除标记")
    private Integer deleted;
}
