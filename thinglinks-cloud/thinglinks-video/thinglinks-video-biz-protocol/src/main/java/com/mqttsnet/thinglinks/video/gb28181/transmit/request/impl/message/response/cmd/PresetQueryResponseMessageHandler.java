package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
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
import java.util.Iterator;

/**
 * 设备预置位查询应答消息处理器。
 * <p>
 * 处理设备对 PresetQuery 查询命令的应答，解析 PresetList 中的预置位信息（PresetID、PresetName）。
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class PresetQueryResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.PRESET_QUERY.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfoResultDTO, Element element) {
        log.info("接收到PresetQuery应答消息");
        if (deviceInfoResultDTO == null) {
            return;
        }
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 预置位查询应答回复200OK: {}", e.getMessage());
        }

        Element presetListElement = element.element("PresetList");
        if (presetListElement == null) {
            log.warn("[预置位查询] PresetList为空, 设备: {}", deviceInfoResultDTO.getDeviceIdentification());
            responseMessageHandler.handMessageEvent(element, null);
            return;
        }

        String numStr = presetListElement.attributeValue("Num");
        int num = 0;
        if (numStr != null) {
            num = Integer.parseInt(numStr);
        }

        JSONArray presetArray = new JSONArray();
        if (num > 0) {
            Iterator<Element> presetIterator = presetListElement.elementIterator();
            while (presetIterator.hasNext()) {
                Element itemElement = presetIterator.next();
                JSONObject presetJson = new JSONObject();
                presetJson.put("PresetID", XmlUtil.getText(itemElement, "PresetID"));
                presetJson.put("PresetName", XmlUtil.getText(itemElement, "PresetName"));
                presetArray.add(presetJson);
            }
        }

        log.info("[预置位查询] 设备: {}, 预置位数量: {}", deviceInfoResultDTO.getDeviceIdentification(), presetArray.size());

        if (log.isDebugEnabled()) {
            log.debug("[预置位查询] 预置位列表: {}", presetArray.toJSONString());
        }

        responseMessageHandler.handMessageEvent(element, presetArray);
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element rootElement) {

    }
}
