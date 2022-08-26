package com.mqttsnet.thinglinks.link.api.domain.device.model;

import lombok.Data;

/**
 * @Description:  边设备添加子设备详情数据模型
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/25$ 12:54$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/25$ 12:54$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Data
public class DeviceInfos {
    private static final long serialVersionUID = 1L;
    private String nodeId;
    private String name;
    private String description;
    private String manufacturerId;
    private String model;
}
