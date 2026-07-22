package com.mqttsnet.thinglinks.video.jt1078.sdk;

import com.mqttsnet.thinglinks.video.jt1078.config.Jt1078Config;

import java.time.LocalDateTime;

/**
 * Description:
 * JT/T 1078 网关适配器接口。
 *
 * <p>JT/T 1078 协议的视频传输依赖于 JT808 网关 + 1078 视频服务器的组合架构，
 * 此接口抽象了与网关之间的通信操作，包括实时音视频请求（0x9101）、
 * 实时音视频控制（0x9102）、历史视频回放请求（0x9201）、
 * 历史视频回放控制（0x9202）、语音对讲、远程抓拍（0x8801）、
 * 资源列表查询（0x9003）等核心指令。
 *
 * <p>不同的 JT808 网关（如自研网关、第三方网关）需各自实现此接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface Jt1078GatewayAdapter {

    /**
     * 初始化网关连接。
     *
     * @param config JT/T 1078 配置
     * @return 初始化是否成功
     */
    boolean init(Jt1078Config config);

    /**
     * 销毁网关连接，释放资源。
     */
    void destroy();

    /**
     * 发送实时音视频请求（0x9101）。
     *
     * @param simNumber      终端SIM卡号
     * @param channelNo      逻辑通道号
     * @param streamType     码流类型（MAIN/SUB）
     * @param mediaServerIp  流媒体服务器IP
     * @param mediaServerPort 流媒体服务器端口
     * @return 是否发送成功
     */
    boolean sendRealPlayRequest(String simNumber, Integer channelNo, String streamType,
                                String mediaServerIp, Integer mediaServerPort);

    /**
     * 发送实时音视频控制（0x9102）。
     *
     * @param simNumber   终端SIM卡号
     * @param channelNo   逻辑通道号
     * @param controlType 控制类型（0-关闭音视频传输，1-切换码流，2-暂停，3-恢复，4-关闭双向对讲）
     * @return 是否发送成功
     */
    boolean sendRealPlayControl(String simNumber, Integer channelNo, Integer controlType);

    /**
     * 发送历史视频回放请求（0x9201）。
     *
     * @param simNumber      终端SIM卡号
     * @param channelNo      逻辑通道号
     * @param startTime      回放起始时间
     * @param endTime        回放结束时间
     * @param mediaServerIp  流媒体服务器IP
     * @param mediaServerPort 流媒体服务器端口
     * @return 是否发送成功
     */
    boolean sendPlaybackRequest(String simNumber, Integer channelNo,
                                LocalDateTime startTime, LocalDateTime endTime,
                                String mediaServerIp, Integer mediaServerPort);

    /**
     * 发送历史视频回放控制（0x9202）。
     *
     * @param simNumber   终端SIM卡号
     * @param channelNo   逻辑通道号
     * @param controlType 控制类型（0-正常播放，1-暂停，2-结束，3-快进，4-关键帧快退，5-拖动，6-关键帧播放）
     * @param speed       快进/快退倍数
     * @return 是否发送成功
     */
    boolean sendPlaybackControl(String simNumber, Integer channelNo, Integer controlType, Integer speed);

    /**
     * 发送语音对讲请求（0x9101 音频模式）。
     *
     * @param simNumber      终端SIM卡号
     * @param channelNo      逻辑通道号
     * @param mediaServerIp  流媒体服务器IP
     * @param mediaServerPort 流媒体服务器端口
     * @return 是否发送成功
     */
    boolean sendTalkRequest(String simNumber, Integer channelNo,
                            String mediaServerIp, Integer mediaServerPort);

    /**
     * 发送远程抓拍请求（0x8801）。
     *
     * @param simNumber 终端SIM卡号
     * @param channelNo 逻辑通道号
     * @param count     抓拍张数
     * @return 是否发送成功
     */
    boolean sendCaptureRequest(String simNumber, Integer channelNo, Integer count);

    /**
     * 查询终端音视频资源列表（0x9003）。
     *
     * @param simNumber 终端SIM卡号
     * @return 是否发送成功
     */
    boolean queryResourceList(String simNumber);

    /**
     * 设置设备事件回调。
     *
     * @param callback 设备事件回调
     */
    void setDeviceCallback(Jt1078DeviceCallback callback);
}
