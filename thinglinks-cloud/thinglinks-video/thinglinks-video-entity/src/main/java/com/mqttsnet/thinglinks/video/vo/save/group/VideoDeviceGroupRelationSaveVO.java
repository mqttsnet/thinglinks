package com.mqttsnet.thinglinks.video.vo.save.group;

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
 * 表单保存方法VO
 * 设备分组关联表
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
@Schema(description = "设备分组关联表")
public class VideoDeviceGroupRelationSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分组ID
     */
    @Schema(description = "分组ID")
    private Long groupId;
    /**
     * 设备唯一标识
     */
    @Schema(description = "设备唯一标识")
    private String deviceIdentification;
    /**
     * 通道唯一标识
     */
    @Schema(description = "通道唯一标识")
    private String channelIdentification;
    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 创建组织
     */
    @Schema(description = "创建组织")
    private Long createdOrgId;

}
