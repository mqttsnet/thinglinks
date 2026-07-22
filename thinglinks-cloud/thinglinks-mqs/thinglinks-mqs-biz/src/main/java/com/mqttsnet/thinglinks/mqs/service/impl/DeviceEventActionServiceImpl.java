package com.mqttsnet.thinglinks.mqs.service.impl;

import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.cache.utils.CachePlusUtil;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import com.mqttsnet.thinglinks.mqs.service.DeviceEventActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * Description:
 * 设备事件动作Service Impl
 * ============================================================================
 *
 * @author mqttsnet
 * @version 1.0.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/3/2      mqttsnet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/3/2 13:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceEventActionServiceImpl implements DeviceEventActionService {
    @Autowired
    private DeviceOpenInnerFacade deviceOpenInnerApi;

    @Autowired
    private CachePlusUtil cachePlusOpsUtil;

    /**
     * 保存设备事件动作
     *
     * @param eventMessage 事件消息
     * @param actionType   动作类型
     * @param describable  描述
     */
    @Override
    public void saveDeviceEventAction(String eventMessage, DeviceActionTypeEnum actionType, String describable) {
        JSONObject map = JSON.parseObject(eventMessage);
        String clientId = String.valueOf(map.get(CommonIotConstants.CLIENT_ID));

        Optional<DeviceCacheVO> deviceCacheVOOptional = cachePlusOpsUtil.getObjectFromCache(DeviceCacheKeyBuilder.build(clientId).getKey(), DeviceCacheVO.class);
        if (deviceCacheVOOptional.isEmpty()) {
            return;
        }

        // save device action
        DeviceActionSaveVO deviceActionSaveVO = new DeviceActionSaveVO();
        deviceActionSaveVO.setDeviceIdentification(deviceCacheVOOptional.get().getDeviceIdentification());
        deviceActionSaveVO.setActionType(actionType.getValue());
        deviceActionSaveVO.setMessage(eventMessage);
        deviceActionSaveVO.setStatus(DeviceActionStatusEnum.SUCCESSFUL.getValue());
        deviceActionSaveVO.setRemark(describable);
        R<DeviceAction> deviceActionR = deviceOpenInnerApi.saveDeviceAction(deviceActionSaveVO);
        if (Boolean.TRUE.equals(deviceActionR.getIsSuccess())) {
            log.info("Save device action success: deviceAction={}", deviceActionR.getData());
        } else {
            log.error("Save device action failed: deviceAction={}", deviceActionR.getData());
        }
    }

    /**
     * {@inheritDoc}
     * <p>直接取 event 字段建 {@link DeviceActionSaveVO} 走 facade;deviceId 缺失或 facade 失败仅告警不抛。
     */
    @Override
    public void save(CommonDeviceEvent event) {
        if (event == null || event.getActionType() == null) {
            return;
        }
        String deviceId = event.getDeviceIdentification();
        if (deviceId == null || deviceId.isEmpty()) {
            log.warn("[DeviceEventAction] deviceIdentification missing, skip persist clientId={} action={}",
                event.getClientId(), event.getActionType());
            return;
        }
        try {
            DeviceActionSaveVO vo = new DeviceActionSaveVO();
            vo.setDeviceIdentification(deviceId);
            vo.setActionType(event.getActionType().getValue());
            vo.setMessage(event.getRawMessage());
            vo.setStatus(DeviceActionStatusEnum.SUCCESSFUL.getValue());
            vo.setRemark(event.getActionType().getDesc());
            R<DeviceAction> r = deviceOpenInnerApi.saveDeviceAction(vo);
            if (!Boolean.TRUE.equals(r.getIsSuccess())) {
                log.warn("[DeviceEventAction] save failed (non-blocking) clientId={} action={} msg={}",
                    event.getClientId(), event.getActionType(), r.getMsg());
            }
        } catch (Exception e) {
            log.warn("[DeviceEventAction] save exception (non-blocking) clientId={} action={}",
                event.getClientId(), event.getActionType(), e);
        }
    }

}
