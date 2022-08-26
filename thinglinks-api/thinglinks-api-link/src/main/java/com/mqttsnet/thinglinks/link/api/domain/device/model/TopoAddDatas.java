package com.mqttsnet.thinglinks.link.api.domain.device.model;

import lombok.Data;

import java.util.List;

/**
 * @Description: 边设备添加子设备数据模型
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/25$ 12:52$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/25$ 12:52$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Data
public class TopoAddDatas {
    private static final long serialVersionUID = 1L;
    private Integer mid;
    private List<DeviceInfos> deviceInfos;
}
