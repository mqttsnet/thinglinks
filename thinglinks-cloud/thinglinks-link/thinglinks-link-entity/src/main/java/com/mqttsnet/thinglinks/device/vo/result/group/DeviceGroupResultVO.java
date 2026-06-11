package com.mqttsnet.thinglinks.device.vo.result.group;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.base.entity.TreeEntity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 表单查询方法返回值VO
 * 设备分组表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-06-19 18:05:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DeviceGroupResultVO", description = "设备分组")
public class DeviceGroupResultVO extends TreeEntity<DeviceGroupResultVO, Long> implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "id")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 分组名称
     */
    @Schema(description = "分组名称")
    private String groupName;
    /**
     * 分组类型
     */
    @Schema(description = "分组类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_DEVICE_GROUP_TYPE)
    private Integer type;
    /**
     * 状态;[0-禁用 1-启用]
     */
    @Schema(description = "状态")
    private Boolean state;
    /**
     * 分组描述
     */
    @Schema(description = "分组描述")
    private String description;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    @Echo(api = EchoApi.ORG_ID_CLASS)
    private Long createdOrgId;

    /** shadow Entity<Long>.createdBy,挂 @Echo 让 echoService 回填用户昵称到 echoMap.createdBy。 */
    @Schema(description = "创建人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long createdBy;

    /** shadow Entity<Long>.updatedBy,同上。 */
    @Schema(description = "最后修改人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long updatedBy;

    @Schema(description = "父节点")
    protected Long parentId;

    @Schema(description = "排序号")
    protected Integer sortValue;


}
