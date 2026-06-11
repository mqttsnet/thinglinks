package com.mqttsnet.thinglinks.service.impl;

import java.util.Optional;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.cache.utils.CacheUtil;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceCacheKeyBuilder;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import com.mqttsnet.thinglinks.service.DeviceEventActionService;
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
    private DeviceOpenAnyUserFacade deviceOpenAnyUserApi;

    @Autowired
    private CacheUtil cacheUtil;

    /**
     * 保存设备事件动作
     *
     * @param eventMessage 事件消息
     * @param actionType   动作类型
     * @param describable  描述
     */
    @Override
    public void saveDeviceEventAction(String eventMessage, DeviceActionTypeEnum actionType, String describable) {
        JSONObject map = JSONUtil.parseObj(eventMessage);
        String clientId = String.valueOf(map.get("clientId"));

        Optional<DeviceCacheVO> deviceCacheVOOptional = cacheUtil.getObjectFromCache(DeviceCacheKeyBuilder.build(clientId).getKey(), DeviceCacheVO.class);
        if (deviceCacheVOOptional.isEmpty()) {
            return;
        }

        // save device action
        DeviceActionSaveVO deviceActionSaveVO = new DeviceActionSaveVO();
        deviceActionSaveVO.setDeviceIdentification(deviceCacheVOOptional.get().getDeviceIdentification());
        deviceActionSaveVO.setActionType(actionType.getAction());
        deviceActionSaveVO.setMessage(eventMessage);
        deviceActionSaveVO.setStatus(DeviceActionStatusEnum.SUCCESSFUL.getValue());
        deviceActionSaveVO.setRemark(describable);
        R<DeviceAction> deviceActionR = deviceOpenAnyUserApi.saveDeviceAction(deviceActionSaveVO);
        if (Boolean.TRUE.equals(deviceActionR.getIsSuccess())) {
            log.info("Save device action success: deviceAction={}", deviceActionR.getData());
        } else {
            log.error("Save device action failed: deviceAction={}", deviceActionR.getData());
        }
    }

}
