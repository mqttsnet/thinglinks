package com.mqttsnet.thinglinks.video.controller.stream;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.protocol.DeviceAccessProtocol;
import com.mqttsnet.thinglinks.video.protocol.DeviceAccessProtocolFactory;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.result.stream.PlayResultVO;
import com.mqttsnet.thinglinks.video.vo.save.stream.PlaySaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Description:
 * 实时视频播放控制器。
 * 提供实时点播发起、停止、流信息查询等 REST API。
 * <p>
 *
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/play")
@Tag(name = "实时视频播放")
public class PlayController {

    private final DeviceAccessProtocolFactory deviceAccessProtocolFactory;
    private final VideoDeviceService videoDeviceService;

    /**
     * 按设备 accessProtocol 字段路由到对应协议实现，业务层无需关心 GB28181 / RTSP / ONVIF 差异。
     */
    private DeviceAccessProtocol resolveProtocol(String deviceIdentification) {
        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        return deviceAccessProtocolFactory.getProtocol(device.getAccessProtocol());
    }

    /**
     * 发起实时点播
     *
     * @param saveVO 播放请求参数
     * @return 播放结果（多协议流地址）
     */
    @PostMapping("/start")
    @Operation(summary = "发起实时点播", description = "向指定设备通道发起实时视频点播，按设备接入协议路由，返回多协议流地址")
    @ApiResponse(responseCode = "200", description = "点播发起成功")
    public R<PlayResultVO> play(@Valid @RequestBody PlaySaveVO saveVO) {
        log.info("发起实时点播: deviceIdentification={}, channelIdentification={}",
                saveVO.getDeviceIdentification(), saveVO.getChannelIdentification());
        DeviceAccessProtocol protocol = resolveProtocol(saveVO.getDeviceIdentification());
        StreamInfo streamInfo = protocol.startRealPlay(saveVO.getDeviceIdentification(), saveVO.getChannelIdentification());
        return R.success(convertToResultVO(streamInfo));
    }

    /**
     * 停止播放
     *
     * @param deviceIdentification  设备编号
     * @param channelIdentification 通道编号
     * @return 操作结果
     */
    @DeleteMapping("/stop")
    @Operation(summary = "停止播放", description = "停止指定设备通道的实时视频播放")
    @ApiResponse(responseCode = "200", description = "停止成功")
    public R<Boolean> stop(
            @Parameter(description = "设备编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道编号", required = true) @RequestParam String channelIdentification) {
        log.info("停止播放: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        DeviceAccessProtocol protocol = resolveProtocol(deviceIdentification);
        protocol.stopRealPlay(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 查询流信息
     *
     * @param deviceIdentification  设备编号
     * @param channelIdentification 通道编号
     * @return 流信息
     */
    @GetMapping("/streamInfo")
    @Operation(summary = "查询流信息", description = "查询指定设备通道的当前播放流信息（多协议地址）")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<PlayResultVO> getStreamInfo(
            @Parameter(description = "设备编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道编号", required = true) @RequestParam String channelIdentification) {
        DeviceAccessProtocol protocol = resolveProtocol(deviceIdentification);
        Optional<StreamInfo> streamInfo = protocol.getStreamInfo(deviceIdentification, channelIdentification);
        return streamInfo.map(info -> R.success(convertToResultVO(info)))
                .orElse(R.success(null));
    }

    /**
     * StreamInfo → PlayResultVO 转换
     */
    private PlayResultVO convertToResultVO(StreamInfo streamInfo) {
        return PlayResultVO.builder()
                .deviceIdentification(streamInfo.getDeviceIdentification())
                .channelIdentification(streamInfo.getStream())
                .app(streamInfo.getApp())
                .stream(streamInfo.getStream())
                .mediaServerIp(streamInfo.getHost())
                .flv(streamInfo.getFlv())
                .httpsFlv(streamInfo.getHttps_flv())
                .wsFlv(streamInfo.getWs_flv())
                .hls(streamInfo.getHls())
                .httpsHls(streamInfo.getHttps_hls())
                .rtmp(streamInfo.getRtmp())
                .rtsp(streamInfo.getRtsp())
                .fmp4(streamInfo.getFmp4())
                .ts(streamInfo.getTs())
                .rtc(streamInfo.getRtc())
                .rtcs(streamInfo.getRtcs())
                .callId(streamInfo.getCallId())
                .build();
    }
}
