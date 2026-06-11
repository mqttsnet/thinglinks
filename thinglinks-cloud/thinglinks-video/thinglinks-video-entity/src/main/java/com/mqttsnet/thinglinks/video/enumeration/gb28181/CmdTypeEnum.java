package com.mqttsnet.thinglinks.video.empowerment.gb28181;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author mqttsnet
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "CmdTypeEnum", description = "Cmd类型枚举")
public enum CmdTypeEnum {

    CATALOG("Catalog", "设备目录查询"),
    ALARM("Alarm", "报警通知"),
    DEVICE_STATUS("DeviceStatus", "设备状态"),
    RECORD_INFO("RecordInfo", "记录信息"),
    BROADCAST("Broadcast", "广播"),
    CONFIG_DOWNLOAD("ConfigDownload", "下载配置"),
    MOBILE_POSITION("MobilePosition", "移动设备位置订阅");


    private String value;
    private String desc;


    /**
     * 根据value获取对应的枚举
     *
     * @param value 标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<CmdTypeEnum> fromValue(String value) {
        return Stream.of(CmdTypeEnum.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
