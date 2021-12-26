package com.mqttsnet.thinglinks.link.api.domain.device.entity;

import com.mqttsnet.thinglinks.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @Description: 设备位置
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 2:03$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 2:03$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@ApiModel(value = "设备位置")
@Data
public class DeviceLocation extends BaseEntity implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    private String deviceIdentification;

    /**
     * 纬度
     */
    @ApiModelProperty(value = "纬度")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @ApiModelProperty(value = "经度")
    private BigDecimal longitude;

    /**
     * 位置名称
     */
    @ApiModelProperty(value = "位置名称")
    private String fullName;

    /**
     * 省,直辖市编码
     */
    @ApiModelProperty(value = "省,直辖市编码")
    private String provinceCode;

    /**
     * 市编码
     */
    @ApiModelProperty(value = "市编码")
    private String cityCode;

    /**
     * 区县
     */
    @ApiModelProperty(value = "区县")
    private String regionCode;


    private static final long serialVersionUID = 1L;
}