package com.mqttsnet.thinglinks.video.isup.cmd;

import com.mqttsnet.thinglinks.video.isup.sdk.IsupSdkAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Description:
 * ISUP 播放命令执行器。
 * 封装实时预览和录像回放的 SDK 调用逻辑。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsupPlayCommander {

    private final IsupSdkAdapter isupSdkAdapter;

    /**
     * 开始实时预览。
     *
     * @param userId     登录句柄
     * @param channelNo  通道号
     * @param streamType 码流类型
     * @return 播放句柄，失败返回 {@code null}
     */
    public Integer startRealPlay(Integer userId, Integer channelNo, String streamType) {
        log.info("ISUP开始实时预览: userId={}, channelNo={}, streamType={}", userId, channelNo, streamType);
        Integer playHandle = isupSdkAdapter.startRealPlay(userId, channelNo, streamType);
        if (playHandle == null) {
            log.error("ISUP实时预览失败: userId={}, channelNo={}", userId, channelNo);
        }
        return playHandle;
    }

    /**
     * 停止实时预览。
     *
     * @param playHandle 播放句柄
     * @return 是否停止成功
     */
    public boolean stopRealPlay(Integer playHandle) {
        log.info("ISUP停止实时预览: playHandle={}", playHandle);
        return isupSdkAdapter.stopRealPlay(playHandle);
    }

    /**
     * 开始录像回放。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @param startTime 回放起始时间
     * @param endTime   回放结束时间
     * @return 回放句柄，失败返回 {@code null}
     */
    public Integer startPlayback(Integer userId, Integer channelNo, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("ISUP开始录像回放: userId={}, channelNo={}, startTime={}, endTime={}",
                userId, channelNo, startTime, endTime);
        Integer playbackHandle = isupSdkAdapter.startPlayback(userId, channelNo, startTime, endTime);
        if (playbackHandle == null) {
            log.error("ISUP录像回放失败: userId={}, channelNo={}", userId, channelNo);
        }
        return playbackHandle;
    }

    /**
     * 停止录像回放。
     *
     * @param playbackHandle 回放句柄
     * @return 是否停止成功
     */
    public boolean stopPlayback(Integer playbackHandle) {
        log.info("ISUP停止录像回放: playbackHandle={}", playbackHandle);
        return isupSdkAdapter.stopPlayback(playbackHandle);
    }
}
