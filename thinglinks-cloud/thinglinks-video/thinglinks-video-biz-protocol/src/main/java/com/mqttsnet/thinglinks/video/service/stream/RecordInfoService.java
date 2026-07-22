package com.mqttsnet.thinglinks.video.service.stream;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import com.mqttsnet.thinglinks.video.gb28181.cmd.QueryCommander;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.PlaybackEventPublisher;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Description:
 * 设备端录像查询业务服务。
 * 通过 SIP MESSAGE 向设备发起录像查询，
 * 查询结果通过设备异步回复的 MESSAGE 消息返回。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class RecordInfoService {

    private final VideoDeviceService videoDeviceService;
    private final QueryCommander queryCommander;
    private final PlaybackEventPublisher playbackEventPublisher;

    /**
     * 发起设备端录像查询
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param startTime 查询开始时间
     * @param endTime   查询结束时间
     * @param type      录像类型（time/alarm/manual/all）
     */
    public void queryRecordInfo(String deviceIdentification, String channelIdentification,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 String type) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.notNull(startTime, "开始时间不能为空");
        ArgumentAssert.notNull(endTime, "结束时间不能为空");

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线，无法查询录像: " + deviceIdentification);
        }

        String recordType = (type != null && !type.isBlank()) ? type : BizConstant.ALL;

        queryCommander.recordInfoQuery(deviceIdentification, channelIdentification,
                device.getHost(), device.getPort(),
                device.getTransport() != null ? device.getTransport() : "UDP",
                GbProtocolVersionEnum.GB2016,
                startTime.toString(), endTime.toString());

        log.info("发起录像查询: deviceIdentification={}, channelIdentification={}, time={} ~ {}, type={}",
                deviceIdentification, channelIdentification, startTime, endTime, recordType);
    }
}
