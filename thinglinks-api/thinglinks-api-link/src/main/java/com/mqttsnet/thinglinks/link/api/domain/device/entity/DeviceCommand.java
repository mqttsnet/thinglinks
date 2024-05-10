package com.mqttsnet.thinglinks.link.api.domain.device.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 实体类
 * 设备命令下发及响应表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-10-20 17:27:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
public class DeviceCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    private String deviceIdentification;
    /**
     * 命令标识
     */
    @ApiModelProperty(value = "命令标识")
    private String commandIdentification;
    /**
     * 命令类型(0:命名下发、1:命令响应)
     */
    @ApiModelProperty(value = "命令类型", example = "0", required = true, allowableValues = "0,1", notes = "0:命名下发、1:命令响应")
    private Integer commandType;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer status;
    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;


    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
