package com.mqttsnet.thinglinks.video.enumeration.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * GB/T 28181-2016 通道类型枚举（行业编码 - 前端外围设备段）。
 * <p>
 * 定义见标准 Section 7.3.3 DeviceID 编码规则：
 * DeviceID 第 11-13 位为设备/通道类型代码。本枚举收录通道（视频/音频/报警输入输出/
 * 门禁等）常用的子集，131~143 属于前端外围设备段。
 * <p>
 * <b>与 {@link DeviceTypeEnum} 的关系</b>：后者是完整的行业编码表（含 DVR/NVR/
 * 服务器/平台设备等），通道层面只会用到其中一小段。分开枚举可让前端下拉更精准，
 * 字典预置也只需要维护通道相关的值。
 * <p>
 * 同名字典：{@code VIDEO_DEVICE_CHANNEL_TYPE}（见 {@code thinglinks_base.sql}
 * data seed 与 {@code EchoDictType.VIDEO_DEVICE_CHANNEL_TYPE}）。新增类型时，
 * 务必三处一起同步：<b>本枚举</b>、<b>字典 seed SQL</b>、<b>前端 i18n 文案</b>。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-19
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ChannelTypeEnum", description = "GB28181 通道类型枚举")
public enum ChannelTypeEnum {

    /** 摄像机 */
    CAMERA(131, "摄像机"),

    /** 网络摄像机（IPC）/ 在线视频图像信息采集设备 */
    IPC(132, "网络摄像机"),

    /** 显示器 */
    MONITOR(133, "显示器"),

    /** 报警输入设备（红外/烟感/门禁等） */
    ALARM_INPUT(134, "报警输入设备"),

    /** 报警输出设备（警灯/警铃等） */
    ALARM_OUTPUT(135, "报警输出设备"),

    /** 语音输入设备 */
    VOICE_INPUT(136, "语音输入设备"),

    /** 语音输出设备 */
    VOICE_OUTPUT(137, "语音输出设备"),

    /** 移动传输设备 */
    MOBILE_TRANSMISSION(138, "移动传输设备"),

    /** 其他外围设备 */
    OTHER_PERIPHERAL(139, "其他外围设备"),

    /** 报警输出设备（继电器/触发器控制类） */
    ALARM_OUTPUT_RELAY(140, "报警输出设备(继电器)"),

    /** 道闸（控制车辆通行） */
    BARRIER_GATE(141, "道闸"),

    /** 智能门（控制人员通行） */
    SMART_DOOR(142, "智能门"),

    /** 凭证识别单元 */
    VOUCHER_RECOGNITION(143, "凭证识别单元"),
    ;

    /** GB28181 DeviceID 第 11-13 位编码 */
    private Integer value;

    /** 中文描述 */
    private String description;

    /**
     * 根据 value 查找枚举。
     *
     * @param value 通道类型编码
     * @return 匹配的枚举实例，未匹配或 value 为 null 时返回 {@link Optional#empty()}
     */
    public static Optional<ChannelTypeEnum> fromValue(Integer value) {
        if (value == null) {
            return Optional.empty();
        }
        return Stream.of(values())
                .filter(e -> e.getValue().equals(value))
                .findFirst();
    }

    /**
     * 日志友好的描述字符串，格式 "<value>(<description>)"。
     * 非标准值或 null 时降级为原值字符串（不抛异常），便于日志打印和排查。
     *
     * @param value 通道类型编码
     * @return 形如 "131(摄像机)" 的字符串；value 不匹配标准时返回 "<value>"；null 时返回 "null"
     */
    public static String descOf(Integer value) {
        if (value == null) {
            return "null";
        }
        return fromValue(value)
                .map(e -> value + "(" + e.getDescription() + ")")
                .orElse(String.valueOf(value));
    }
}
