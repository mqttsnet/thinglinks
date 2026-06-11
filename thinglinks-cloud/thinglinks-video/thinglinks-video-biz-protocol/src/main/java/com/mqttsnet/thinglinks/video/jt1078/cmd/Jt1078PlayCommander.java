package com.mqttsnet.thinglinks.video.jt1078.cmd;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.manager.jt1078.Jt1078ConnectionManager;
import com.mqttsnet.thinglinks.video.jt1078.sdk.Jt1078GatewayAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Description:
 * JT/T 1078 播放指令发送器。
 * 封装实时音视频和历史视频回放的指令发送逻辑，
 * 内部校验终端在线状态后委托给 {@link Jt1078GatewayAdapter} 发送。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Jt1078PlayCommander {

    private final Jt1078GatewayAdapter gatewayAdapter;
    private final Jt1078ConnectionManager connectionManager;

    /**
     * 开始实时播放。
     *
     * @param simNumber      终端SIM卡号
     * @param channelNo      逻辑通道号
     * @param streamType     码流类型（MAIN/SUB）
     * @param mediaServerIp  流媒体服务器IP
     * @param mediaServerPort 流媒体服务器端口
     * @return 是否发送成功
     */
    public boolean startRealPlay(String simNumber, Integer channelNo, String streamType,
                                 String mediaServerIp, Integer mediaServerPort) {
        ArgumentAssert.notBlank(simNumber, "simNumber不能为空");
        ArgumentAssert.notNull(channelNo, "channelNo不能为空");
        checkOnline(simNumber);
        log.info("[JT1078] 发起实时播放: simNumber={}, channelNo={}, streamType={}",
                simNumber, channelNo, streamType);
        return gatewayAdapter.sendRealPlayRequest(simNumber, channelNo, streamType,
                mediaServerIp, mediaServerPort);
    }

    /**
     * 停止实时播放。
     *
     * @param simNumber 终端SIM卡号
     * @param channelNo 逻辑通道号
     * @return 是否发送成功
     */
    public boolean stopRealPlay(String simNumber, Integer channelNo) {
        ArgumentAssert.notBlank(simNumber, "simNumber不能为空");
        ArgumentAssert.notNull(channelNo, "channelNo不能为空");
        log.info("[JT1078] 停止实时播放: simNumber={}, channelNo={}", simNumber, channelNo);
        // controlType=0 表示关闭音视频传输
        return gatewayAdapter.sendRealPlayControl(simNumber, channelNo, 0);
    }

    /**
     * 开始历史视频回放。
     *
     * @param simNumber      终端SIM卡号
     * @param channelNo      逻辑通道号
     * @param startTime      回放起始时间
     * @param endTime        回放结束时间
     * @param mediaServerIp  流媒体服务器IP
     * @param mediaServerPort 流媒体服务器端口
     * @return 是否发送成功
     */
    public boolean startPlayback(String simNumber, Integer channelNo,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 String mediaServerIp, Integer mediaServerPort) {
        ArgumentAssert.notBlank(simNumber, "simNumber不能为空");
        ArgumentAssert.notNull(channelNo, "channelNo不能为空");
        ArgumentAssert.notNull(startTime, "startTime不能为空");
        ArgumentAssert.notNull(endTime, "endTime不能为空");
        checkOnline(simNumber);
        log.info("[JT1078] 发起历史回放: simNumber={}, channelNo={}, startTime={}, endTime={}",
                simNumber, channelNo, startTime, endTime);
        return gatewayAdapter.sendPlaybackRequest(simNumber, channelNo, startTime, endTime,
                mediaServerIp, mediaServerPort);
    }

    /**
     * 停止历史视频回放。
     *
     * @param simNumber 终端SIM卡号
     * @param channelNo 逻辑通道号
     * @return 是否发送成功
     */
    public boolean stopPlayback(String simNumber, Integer channelNo) {
        ArgumentAssert.notBlank(simNumber, "simNumber不能为空");
        ArgumentAssert.notNull(channelNo, "channelNo不能为空");
        log.info("[JT1078] 停止历史回放: simNumber={}, channelNo={}", simNumber, channelNo);
        // controlType=2 表示结束回放
        return gatewayAdapter.sendPlaybackControl(simNumber, channelNo, 2, 0);
    }

    /**
     * 校验终端是否在线。
     *
     * @param simNumber 终端SIM卡号
     */
    private void checkOnline(String simNumber) {
        if (!connectionManager.isOnline(simNumber)) {
            throw BizException.wrap("终端不在线: {}", simNumber);
        }
    }
}
