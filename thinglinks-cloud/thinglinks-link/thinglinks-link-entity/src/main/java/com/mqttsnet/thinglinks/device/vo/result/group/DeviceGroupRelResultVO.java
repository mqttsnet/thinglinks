package com.mqttsnet.thinglinks.device.vo.result.group;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
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
 * 设备分组关系表
 * </p>
 *
 * @author mqttsnet
 * @since 2025-06-23 14:06:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "设备分组关系")
public class DeviceGroupRelResultVO extends SuperEntity<Long> implements Serializable, EchoVO {
    @Serial
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "id")
    private Long id;

    /**
     * 分组ID;#device_group
     */
    @Schema(description = "分组ID")
    private Long groupId;

    /**
     * 分组名称
     */
    @Schema(description = "分组名称")
    private String groupName;

    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    private String deviceIdentification;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID(主键)")
    private Long deviceId;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private LocalDateTime updatedTime;
    /**
     * 最后修改人
     */
    @Schema(description = "最后修改人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long updatedBy;

    /** shadow SuperEntity<Long>.createdBy,挂 @Echo 让 echoService 回填用户昵称到 echoMap.createdBy。 */
    @Schema(description = "创建人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long createdBy;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    @Echo(api = EchoApi.ORG_ID_CLASS)
    private Long createdOrgId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型")
    private Integer nodeType;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态")
    private Integer deviceStatus;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    private String productIdentification;

}
