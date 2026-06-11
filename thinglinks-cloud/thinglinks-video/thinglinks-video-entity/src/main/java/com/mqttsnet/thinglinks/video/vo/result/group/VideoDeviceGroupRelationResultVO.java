package com.mqttsnet.thinglinks.video.vo.result.group;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 表单查询方法返回值VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "设备分组关联表")
public class VideoDeviceGroupRelationResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    private Long id;

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
     * 逻辑删除
     */
    @Schema(description = "逻辑删除")
    private Integer deleted;

}
