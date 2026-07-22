package com.mqttsnet.thinglinks.video.isup.sdk;

import java.time.LocalDateTime;

/**
 * Description:
 * ISUP SDK 统一抽象接口。
 * 隔离海康 HCNetSDK 的 native 调用，
 * 提供 Mock 实现用于开发测试环境。
 *
 * <p>所有 ISUP 业务层仅依赖此接口，不直接调用任何 native 方法，
 * 通过 {@link MockIsupSdkAdapter} 实现可在无 SDK 环境下进行开发和测试。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see MockIsupSdkAdapter
 */
public interface IsupSdkAdapter {

    /**
     * 初始化 SDK。
     *
     * @param sdkPath SDK 库文件路径
     * @return 是否初始化成功
     */
    boolean init(String sdkPath);

    /**
     * 销毁 SDK，释放所有资源。
     */
    void destroy();

    /**
     * 启动 ISUP 监听服务。
     *
     * @param ip   监听 IP 地址
     * @param port 监听端口
     * @return 是否启动成功
     */
    boolean startListening(String ip, int port);

    /**
     * 停止 ISUP 监听服务。
     */
    void stopListening();

    // ==================== 设备操作 ====================

    /**
     * 登录设备。
     *
     * @param deviceSerial 设备序列号
     * @param ip           设备 IP
     * @param port         设备端口
     * @param username     用户名
     * @param password     密码
     * @return 用户 ID（登录句柄），失败返回 {@code null}
     */
    Integer login(String deviceSerial, String ip, int port, String username, String password);

    /**
     * 登出设备。
     *
     * @param userId 登录句柄
     * @return 是否登出成功
     */
    boolean logout(Integer userId);

    // ==================== 实时预览 ====================

    /**
     * 开始实时预览。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @param streamType 码流类型（主码流/子码流）
     * @return 播放句柄，失败返回 {@code null}
     */
    Integer startRealPlay(Integer userId, Integer channelNo, String streamType);

    /**
     * 停止实时预览。
     *
     * @param playHandle 播放句柄
     * @return 是否停止成功
     */
    boolean stopRealPlay(Integer playHandle);

    // ==================== 录像回放 ====================

    /**
     * 开始录像回放。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @param startTime 回放起始时间
     * @param endTime   回放结束时间
     * @return 回放句柄，失败返回 {@code null}
     */
    Integer startPlayback(Integer userId, Integer channelNo, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 停止录像回放。
     *
     * @param playbackHandle 回放句柄
     * @return 是否停止成功
     */
    boolean stopPlayback(Integer playbackHandle);

    // ==================== PTZ 控制 ====================

    /**
     * PTZ 云台控制。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @param command   控制命令
     * @param stop      是否停止（0-开始，1-停止）
     * @param speed     速度（1-7）
     * @return 是否执行成功
     */
    boolean ptzControl(Integer userId, Integer channelNo, Integer command, Integer stop, Integer speed);

    /**
     * 预置位操作。
     *
     * @param userId      登录句柄
     * @param channelNo   通道号
     * @param command     操作命令（设置/调用/删除）
     * @param presetIndex 预置位编号
     * @return 是否执行成功
     */
    boolean ptzPreset(Integer userId, Integer channelNo, Integer command, Integer presetIndex);

    // ==================== 告警 ====================

    /**
     * 建立告警上传通道。
     *
     * @param userId 登录句柄
     * @return 告警句柄，失败返回 {@code null}
     */
    Integer setupAlarmChan(Integer userId);

    /**
     * 关闭告警上传通道。
     *
     * @param alarmHandle 告警句柄
     * @return 是否关闭成功
     */
    boolean closeAlarmChan(Integer alarmHandle);

    // ==================== 语音对讲 ====================

    /**
     * 开始语音对讲。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @return 语音对讲句柄，失败返回 {@code null}
     */
    Integer startVoiceTalk(Integer userId, Integer channelNo);

    /**
     * 停止语音对讲。
     *
     * @param voiceHandle 语音对讲句柄
     * @return 是否停止成功
     */
    boolean stopVoiceTalk(Integer voiceHandle);

    // ==================== 抓图 ====================

    /**
     * 设备抓图。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @param savePath  图片保存路径
     * @return 是否抓图成功
     */
    boolean capture(Integer userId, Integer channelNo, String savePath);

    // ==================== 回调注册 ====================

    /**
     * 注册设备事件回调。
     *
     * @param callback 设备事件回调接口
     */
    void setDeviceCallback(IsupDeviceCallback callback);

    /**
     * 注册告警事件回调。
     *
     * @param callback 告警事件回调接口
     */
    void setAlarmCallback(IsupAlarmCallback callback);
}
