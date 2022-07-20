package com.mqttsnet.thinglinks.link.api.domain.device.entity.model;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: Device Entity class model
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/5/4$ 18:57$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/5/4$ 18:57$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Data
public class DeviceModel extends Device implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 设备位置信息
     */
    private DeviceLocation deviceLocation;

}
