package com.mqttsnet.thinglinks.video.jt1078.cmd;

import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.jt1078.sdk.Jt1078GatewayAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * JT/T 1078 控制指令发送器。
 * 封装远程抓拍和回放控制（暂停/恢复/倍速/拖拽）等控制类指令的发送逻辑。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Jt1078ControlCommander {

    private final Jt1078GatewayAdapter gatewayAdapter;

    /**
     * 远程抓拍。
     *
     * @param simNumber 终端SIM卡号
     * @param channelNo 逻辑通道号
     * @param count     抓拍张数
     * @return 是否发送成功
     */
    public boolean capture(String simNumber, Integer channelNo, Integer count) {
        ArgumentAssert.notBlank(simNumber, "simNumber不能为空");
        ArgumentAssert.notNull(channelNo, "channelNo不能为空");
        ArgumentAssert.notNull(count, "count不能为空");
        log.info("[JT1078] 远程抓拍: simNumber={}, channelNo={}, count={}", simNumber, channelNo, count);
        return gatewayAdapter.sendCaptureRequest(simNumber, channelNo, count);
    }

    /**
     * 回放控制（暂停/恢复/倍速/拖拽等）。
     *
     * @param simNumber   终端SIM卡号
     * @param channelNo   逻辑通道号
     * @param controlType 控制类型（0-正常播放，1-暂停，2-结束，3-快进，4-关键帧快退，5-拖动，6-关键帧播放）
     * @param speed       快进/快退倍数（controlType=3或4时有效）
     * @return 是否发送成功
     */
    public boolean playbackControl(String simNumber, Integer channelNo,
                                   Integer controlType, Integer speed) {
        ArgumentAssert.notBlank(simNumber, "simNumber不能为空");
        ArgumentAssert.notNull(channelNo, "channelNo不能为空");
        ArgumentAssert.notNull(controlType, "controlType不能为空");
        log.info("[JT1078] 回放控制: simNumber={}, channelNo={}, controlType={}, speed={}",
                simNumber, channelNo, controlType, speed);
        return gatewayAdapter.sendPlaybackControl(simNumber, channelNo, controlType,
                speed != null ? speed : 0);
    }
}
