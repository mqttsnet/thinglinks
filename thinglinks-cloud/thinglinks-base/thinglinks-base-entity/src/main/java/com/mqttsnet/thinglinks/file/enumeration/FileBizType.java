package com.mqttsnet.thinglinks.file.enumeration;

import com.mqttsnet.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

/**
 * 文件 业务类型 枚举
 *
 * @author mqttsnet
 * @date 2026/03/30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "FileBizType", description = "文件业务类型")
public enum FileBizType implements BaseEnum {
    /**
     * 视频截图
     */
    VIDEO_SNAPSHOT("videoSnapshot", "视频截图"),
    /**
     * 视频录像
     */
    VIDEO_RECORD("videoRecord", "视频录像"),
    /**
     * 设备头像
     */
    DEVICE_AVATAR("deviceAvatar", "设备头像"),
    /**
     * 平台Logo
     */
    PLATFORM_LOGO("platformLogo", "平台Logo"),
    /**
     * 告警图片
     */
    ALARM_IMAGE("alarmImage", "告警图片"),
    /**
     * 通道截图
     */
    CHANNEL_SNAPSHOT("channelSnapshot", "通道截图"),
    /**
     * 导入文件
     */
    IMPORT_FILE("importFile", "导入文件"),
    /**
     * 导出文件
     */
    EXPORT_FILE("exportFile", "导出文件"),
    /**
     * 插件扫描报告
     */
    PLUGIN_SCAN_REPORT("pluginScanReport", "插件扫描报告"),
    /**
     * 其他
     */
    OTHER("other", "其他");

    @Schema(description = "编码")
    private String value;

    @Schema(description = "描述")
    private String desc;

    /**
     * 根据 value 匹配
     */
    public static FileBizType match(String val, FileBizType def) {
        return Stream.of(values()).parallel()
                .filter(item -> item.value.equalsIgnoreCase(val) || item.name().equalsIgnoreCase(val))
                .findAny().orElse(def);
    }

    public static FileBizType get(String val) {
        return match(val, null);
    }

    @Override
    @Schema(description = "编码",
            allowableValues = "videoSnapshot,videoRecord,deviceAvatar,platformLogo,alarmImage,channelSnapshot,importFile,exportFile,pluginScanReport,other",
            example = "videoSnapshot")
    public String getCode() {
        return this.value;
    }

    public boolean eq(FileBizType type) {
        return type != null && this.value.equals(type.value);
    }
}
