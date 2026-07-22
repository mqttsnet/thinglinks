package com.mqttsnet.thinglinks.video.protocol;

import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Description:
 * 统一设备接入协议抽象接口。
 *
 * <p>所有视频设备接入协议（GB28181、ISUP、JT/T 1078）必须实现此接口，
 * 业务层（PlayService 等）仅依赖此接口，不直接依赖具体协议实现，
 * 实现协议间完全隔离。
 *
 * <p>设计原则：
 * <ul>
 *     <li>每种协议一个独立实现，放在独立的包中（gb28181/、isup/、jt1078/）</li>
 *     <li>修改任何一种协议的实现不影响其他协议</li>
 *     <li>新增协议只需新增实现类并注册到 {@link DeviceAccessProtocolFactory}</li>
 *     <li>不支持的操作抛出 {@link UnsupportedOperationException}</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see DeviceAccessProtocolFactory
 * @see AccessProtocolEnum
 * @see Gb28181AccessProtocol
 */
public interface DeviceAccessProtocol {

    /**
     * 获取协议类型。
     *
     * @return 当前实现对应的协议枚举值
     */
    AccessProtocolEnum getProtocolType();

    // ========== 实时预览 ==========

    /**
     * 发起实时视频播放。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @return 流信息（包含多协议 URL），流尚未就绪时仅包含基本信息
     */
    StreamInfo startRealPlay(String deviceIdentification, String channelIdentification);

    /**
     * 停止实时播放。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     */
    void stopRealPlay(String deviceIdentification, String channelIdentification);

    /**
     * 获取当前流信息。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @return 流信息，不存在时返回 {@link Optional#empty()}
     */
    Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification);

    // ========== 录像回放 ==========

    /**
     * 发起录像回放。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @param startTime 回放起始时间
     * @param endTime   回放结束时间
     * @return 流信息
     */
    StreamInfo startPlayback(String deviceIdentification, String channelIdentification, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 停止录像回放。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     */
    void stopPlayback(String deviceIdentification, String channelIdentification);

    /**
     * 回放暂停。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     */
    default void pausePlayback(String deviceIdentification, String channelIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support pausePlayback");
    }

    /**
     * 回放恢复。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     */
    default void resumePlayback(String deviceIdentification, String channelIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support resumePlayback");
    }

    /**
     * 设置回放倍速。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @param speed     倍速值（如 0.5、1.0、2.0、4.0）
     */
    default void setPlaybackSpeed(String deviceIdentification, String channelIdentification, Double speed) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support setPlaybackSpeed");
    }

    /**
     * 回放拖拽定位。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification    设备标识
     * @param channelIdentification   通道标识
     * @param seekSeconds 相对起始时间的秒数偏移
     */
    default void seekPlayback(String deviceIdentification, String channelIdentification, Long seekSeconds) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support seekPlayback");
    }

    // ========== PTZ 控制 ==========

    /**
     * PTZ 方向控制。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @param direction 方向（UP / DOWN / LEFT / RIGHT / LEFT_UP / RIGHT_UP / LEFT_DOWN / RIGHT_DOWN）
     * @param speed     速度（1-255）
     */
    default void ptzDirection(String deviceIdentification, String channelIdentification, String direction, Integer speed) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support ptzDirection");
    }

    /**
     * PTZ 停止。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     */
    default void ptzStop(String deviceIdentification, String channelIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support ptzStop");
    }

    /**
     * PTZ 变倍控制。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @param zoomType  变倍类型（ZOOM_IN / ZOOM_OUT）
     * @param speed     速度（1-255）
     */
    default void ptzZoom(String deviceIdentification, String channelIdentification, String zoomType, Integer speed) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support ptzZoom");
    }

    /**
     * 预置位操作。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @param command   操作指令（SET / CALL / DELETE）
     * @param presetId  预置位编号
     */
    default void ptzPreset(String deviceIdentification, String channelIdentification, String command, Integer presetId) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support ptzPreset");
    }

    // ========== 设备控制 ==========

    /**
     * 远程重启设备。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification 设备标识
     */
    default void reboot(String deviceIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support reboot");
    }

    /**
     * 布防 / 撤防。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification 设备标识
     * @param enable   {@code true} 布防，{@code false} 撤防
     */
    default void guard(String deviceIdentification, Boolean enable) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support guard");
    }

    /**
     * 开始 / 停止录像。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @param start     {@code true} 开始录像，{@code false} 停止录像
     */
    default void recordControl(String deviceIdentification, String channelIdentification, Boolean start) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support recordControl");
    }

    // ========== 设备查询 ==========

    /**
     * 查询设备目录（通道列表）。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification 设备标识
     */
    default void queryCatalog(String deviceIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support queryCatalog");
    }

    /**
     * 查询设备信息。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification 设备标识
     */
    default void queryDeviceInfo(String deviceIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support queryDeviceInfo");
    }

    // ========== 告警 ==========

    /**
     * 订阅设备告警。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification 设备标识
     */
    default void subscribeAlarm(String deviceIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support subscribeAlarm");
    }

    // ========== 语音广播 ==========

    /**
     * 发起语音广播。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @return 语音广播流信息
     */
    default StreamInfo startBroadcast(String deviceIdentification, String channelIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support startBroadcast");
    }

    /**
     * 停止语音广播。
     * 默认抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     */
    default void stopBroadcast(String deviceIdentification, String channelIdentification) {
        throw new UnsupportedOperationException(getProtocolType().getValue() + " does not support stopBroadcast");
    }

    // ========== 能力查询 ==========

    /**
     * 是否支持实时播放。
     *
     * @return 默认返回 {@code true}
     */
    default boolean supportsRealPlay() {
        return true;
    }

    /**
     * 是否支持录像回放。
     *
     * @return 默认返回 {@code true}
     */
    default boolean supportsPlayback() {
        return true;
    }

    /**
     * 是否支持 PTZ 控制。
     *
     * @return 默认返回 {@code false}
     */
    default boolean supportsPtz() {
        return false;
    }

    /**
     * 是否支持语音广播。
     *
     * @return 默认返回 {@code false}
     */
    default boolean supportsBroadcast() {
        return false;
    }
}
