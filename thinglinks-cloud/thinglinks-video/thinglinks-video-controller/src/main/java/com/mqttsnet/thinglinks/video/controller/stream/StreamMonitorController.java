package com.mqttsnet.thinglinks.video.controller.stream;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;
import com.mqttsnet.thinglinks.video.gb28181.session.StreamInfoService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.stream.ActiveStreamResultVO;
import com.mqttsnet.thinglinks.video.vo.result.stream.StreamOverviewResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * 流监控控制器。
 * <p>
 * 提供活跃流列表查询和流概览统计接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/streamMonitor")
@Tag(name = "流监控")
public class StreamMonitorController {

    private final VideoDeviceService videoDeviceService;
    private final StreamInfoService streamInfoService;

    /**
     * 查询所有活跃流列表
     */
    @GetMapping("/list")
    @Operation(summary = "活跃流列表", description = "查询当前所有活跃的视频流")
    public R<List<ActiveStreamResultVO>> list() {
        // 查询所有在线设备
        QueryWrap<VideoDevice> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoDevice::getOnlineStatus, true);
        List<VideoDevice> onlineDevices = videoDeviceService.list(queryWrap);

        List<ActiveStreamResultVO> result = new ArrayList<>();
        for (VideoDevice device : onlineDevices) {
            List<StreamInfo> streams = streamInfoService.getAllStreamInfos(device.getDeviceIdentification());
            if (CollUtil.isNotEmpty(streams)) {
                for (StreamInfo info : streams) {
                    result.add(ActiveStreamResultVO.builder()
                        .deviceIdentification(info.getDeviceIdentification())
                        .channelIdentification(info.getChannelIdentification())
                        .app(info.getApp())
                        .stream(info.getStream())
                        .serverId(info.getServerId())
                        .mediaServerHost(info.getHost())
                        .flvUrl(info.getFlv().getUrl())
                        .callId(info.getCallId())
                        .build());
                }
            }
        }
        return R.success(result);
    }

    /**
     * 流监控概览统计
     */
    @GetMapping("/overview")
    @Operation(summary = "流监控概览", description = "活跃流数量、在线设备数等统计")
    public R<StreamOverviewResultVO> overview() {
        // 在线设备数
        long onlineCount = videoDeviceService.countOnline();

        // 离线设备数
        long offlineCount = videoDeviceService.countOffline();

        // 活跃流数
        QueryWrap<VideoDevice> allOnlineWrap = new QueryWrap<>();
        allOnlineWrap.lambda().eq(VideoDevice::getOnlineStatus, true);
        List<VideoDevice> onlineDevices = videoDeviceService.list(allOnlineWrap);
        int totalStreams = 0;
        for (VideoDevice device : onlineDevices) {
            totalStreams += streamInfoService.getAllStreamInfos(device.getDeviceIdentification()).size();
        }

        return R.success(StreamOverviewResultVO.builder()
            .totalActiveStreams(totalStreams)
            .onlineDeviceCount(onlineCount)
            .offlineDeviceCount(offlineCount)
            .totalChannelCount(onlineCount + offlineCount) // 简化: 后续可查channel表
            .build());
    }
}
