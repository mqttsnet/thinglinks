package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.gb28181.record.RecordInfo;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.PlaybackEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.RecordQueryCompletedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.ResponseMessageHandler;
import com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 录像信息查询应答消息处理器。
 * <p>
 * 处理设备对 RecordInfo 查询命令的应答，解析 RecordList 中的录像条目信息。
 * 支持多包应答（SumNum 分批上报），完成后发布 RecordQueryCompletedEvent 事件。
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class RecordInfoResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.RECORD_INFO.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private PlaybackEventPublisher playbackEventPublisher;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfoResultDTO, Element element) {
        log.info("接收到RecordInfo应答消息");
        if (deviceInfoResultDTO == null) {
            return;
        }
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 录像查询应答回复200OK: {}", e.getMessage());
        }

        try {
            String sn = XmlUtil.getText(element, "SN");
            String channelIdentification = XmlUtil.getText(element, "DeviceID");
            String name = XmlUtil.getText(element, "Name");
            int sumNum = NumberUtil.parseInt(XmlUtil.getText(element, "SumNum"), 0);

            if (sumNum == 0) {
                log.info("[录像查询] 设备: {}, 通道: {}, 无录像数据", deviceInfoResultDTO.getDeviceIdentification(), channelIdentification);
                publishRecordQueryCompleted(deviceInfoResultDTO.getDeviceIdentification(), channelIdentification, sn, 0);
                responseMessageHandler.handMessageEvent(element, null);
                return;
            }

            Element recordListElement = element.element("RecordList");
            if (recordListElement == null) {
                log.warn("[录像查询] RecordList为空, 设备: {}, 通道: {}", deviceInfoResultDTO.getDeviceIdentification(), channelIdentification);
                publishRecordQueryCompleted(deviceInfoResultDTO.getDeviceIdentification(), channelIdentification, sn, 0);
                responseMessageHandler.handMessageEvent(element, null);
                return;
            }

            // 解析录像列表
            List<RecordInfo.RecordItem> recordList = new ArrayList<>();
            Iterator<Element> recordListIterator = recordListElement.elementIterator();
            while (recordListIterator.hasNext()) {
                Element itemRecord = recordListIterator.next();
                String itemDeviceId = XmlUtil.getText(itemRecord, "DeviceID");
                if (itemDeviceId == null) {
                    log.debug("[录像查询] 录像条目缺少DeviceID，跳过");
                    continue;
                }

                RecordInfo.RecordItem recordItem = RecordInfo.RecordItem.builder()
                        .deviceIdentification(itemDeviceId)
                        .name(XmlUtil.getText(itemRecord, "Name"))
                        .filePath(XmlUtil.getText(itemRecord, "FilePath"))
                        .address(XmlUtil.getText(itemRecord, "Address"))
                        .startTime(XmlUtil.getText(itemRecord, "StartTime"))
                        .endTime(XmlUtil.getText(itemRecord, "EndTime"))
                        .type(XmlUtil.getText(itemRecord, "Type"))
                        .recorderId(XmlUtil.getText(itemRecord, "RecorderID"))
                        .build();

                Integer secrecy = NumberUtil.parseInt(XmlUtil.getText(itemRecord, "Secrecy"), null);
                if (secrecy != null) {
                    recordItem.setSecrecy(secrecy);
                }

                String fileSizeStr = XmlUtil.getText(itemRecord, "FileSize");
                if (StrUtil.isNotBlank(fileSizeStr)) {
                    Long fileSize = NumberUtil.parseLong(fileSizeStr, null);
                    if (fileSize != null) {
                        recordItem.setFileSize(fileSize);
                    } else {
                        log.warn("[录像查询] FileSize解析失败: {}", fileSizeStr);
                    }
                }

                recordList.add(recordItem);
            }

            RecordInfo recordInfo = RecordInfo.builder()
                    .deviceIdentification(deviceInfoResultDTO.getDeviceIdentification())
                    .channelIdentification(channelIdentification)
                    .sn(sn)
                    .sumNum(sumNum)
                    .recordList(recordList)
                    .build();

            log.info("[录像查询] 设备: {}, 通道: {}, 本批{}条, SN={}, SumNum={}",
                    deviceInfoResultDTO.getDeviceIdentification(), channelIdentification, recordList.size(), sn, sumNum);

            if (log.isDebugEnabled()) {
                log.debug("[录像查询] 录像列表: {}", recordList);
            }

            // 发布录像查询完成事件
            publishRecordQueryCompleted(deviceInfoResultDTO.getDeviceIdentification(), channelIdentification, sn, recordList.size());

            responseMessageHandler.handMessageEvent(element, recordInfo);
        } catch (Exception e) {
            log.error("[录像查询] 解析异常, 设备: {}", deviceInfoResultDTO.getDeviceIdentification(), e);
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element rootElement) {

    }

    /**
     * 发布录像查询完成事件
     */
    private void publishRecordQueryCompleted(String deviceIdentification, String channelIdentification, String sn, int recordCount) {
        RecordQueryCompletedEventSource source = RecordQueryCompletedEventSource.builder()
                .deviceIdentification(deviceIdentification)
                .channelIdentification(channelIdentification)
                .sn(sn)
                .recordCount(recordCount)
                .build();
        playbackEventPublisher.publishRecordQueryCompletedEvent(source);
    }
}
