package com.mqttsnet.thinglinks.video.vo.result.group;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
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
import java.util.List;

/**
 * <p>
 * 表单查询方法返回值VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "设备分组表")
public class VideoDeviceGroupResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    private Long id;

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
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_GROUP_TYPE)
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
     * 子分组列表（用于构建树形结构）
     */
    @Schema(description = "子分组列表")
    private List<VideoDeviceGroupResultVO> children;

    /**
     * 逻辑删除
     */
    @Schema(description = "逻辑删除")
    private Integer deleted;

}
