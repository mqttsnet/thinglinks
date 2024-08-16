package com.mqttsnet.thinglinks.link.api.domain.cache.device;

import cn.hutool.core.map.MapUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 子设备档案缓存VO
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-14 17:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "DeviceInfoCacheVO", description = "子设备档案缓存VO")
public class DeviceInfoCacheVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 边设备主键
     */
    @ApiModelProperty(value = "边设备主键")
    private Long did;

    /**
     * 边设备唯一标识
     */
    @ApiModelProperty(value = "边设备唯一标识")
    private String edgeDevicesIdentification;

    /**
     * 应用ID
     */
    @ApiModelProperty(value = "应用ID")
    private String appId;

    /**
     * 设备节点ID
     */
    @ApiModelProperty(value = "设备节点ID")
    private String nodeId;

    /**
     * 设备名称
     */
    @ApiModelProperty(value = "设备名称")
    private String nodeName;

    /**
     * 子设备唯一标识
     */
    @ApiModelProperty(value = "子设备唯一标识")
    private String deviceId;

    /**
     * 设备描述
     */
    @ApiModelProperty(value = "设备描述")
    private String description;

    /**
     * 厂商ID
     */
    @ApiModelProperty(value = "厂商ID")
    private String manufacturerId;

    /**
     * 设备型号
     */
    @ApiModelProperty(value = "设备型号")
    private String model;

    /**
     * 子设备连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT
     */
    @ApiModelProperty(value = "子设备连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT")
    private String connectStatus;

    /**
     * 是否支持设备影子TRUE:1、FALSE :0
     */
    @ApiModelProperty(value = "是否支持设备影子TRUE:1、FALSE :0")
    private Boolean shadowEnable;

    /**
     * 设备影子数据表名
     */
    @ApiModelProperty(value = "设备影子数据表名")
    private String shadowTableName;

    /**
     * 状态(字典值：0启用  1停用)
     */
    @ApiModelProperty(value = "状态(字典值：0启用  1停用)")
    private String status;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 父设备基础信息（包含产品）
     */
    @ApiModelProperty(value = "父设备基础信息（包含产品）")
    private DeviceCacheVO deviceCacheVO;

}
