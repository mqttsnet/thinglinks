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
 * 设备分组表
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
@Schema(description = "设备分组表")
public class VideoDeviceGroupSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分组名称
     */
    @Schema(description = "分组名称")
    private String groupName;
    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    private Long parentId;
    /**
     * 分组类型
     */
    @Schema(description = "分组类型")
    private Integer groupType;
    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;
    /**
     * 分组路径
     */
    @Schema(description = "分组路径")
    private String groupPath;
    /**
     * 分组层级
     */
    @Schema(description = "分组层级")
    private Integer groupLevel;
    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;
    /**
     * 启用状态
     */
    @Schema(description = "启用状态")
    private Boolean enable;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 创建组织
     */
    @Schema(description = "创建组织")
    private Long createdOrgId;

}
