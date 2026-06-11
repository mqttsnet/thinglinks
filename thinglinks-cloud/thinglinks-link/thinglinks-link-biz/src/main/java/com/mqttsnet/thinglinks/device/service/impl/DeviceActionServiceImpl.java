package com.mqttsnet.thinglinks.device.service.impl;

import java.util.List;
import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceActionCacheVO;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.manager.DeviceActionManager;
import com.mqttsnet.thinglinks.device.service.DeviceActionService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceActionPageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceActionResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 业务实现类
 * 设备动作数据
 * </p>
 *
 * @author mqttsnet
 * @date 2023-06-10 16:38:09
 * @create [2023-06-10 16:38:09] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceActionServiceImpl extends SuperServiceImpl<DeviceActionManager, Long, DeviceAction> implements DeviceActionService {

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyUserFacade;


    /**
     * 保存设备动作数据
     *
     * @param deviceActionSaveVO 设备动作数据
     * @return {@link DeviceAction} 保存完成的设备动作数据
     */
    @Override
    public DeviceAction saveDeviceAction(DeviceActionSaveVO deviceActionSaveVO) {
        // 校验参数
        checkedDeviceActionSaveVO(deviceActionSaveVO);

        // 构建参数
        DeviceAction deviceAction = builderDeviceActionSaveVO(deviceActionSaveVO);

        // 保存设备动作数据
        boolean saveSuccess = Optional.of(superManager.save(deviceAction)).orElse(false);

        if (saveSuccess) {
            // 从缓存中获取设备信息
            Optional<DeviceCacheVO> deviceCacheVOOptional = linkCacheDataHelper.getDeviceCacheVO(deviceAction.getDeviceIdentification());
            if (deviceCacheVOOptional.isPresent()) {
                DeviceActionCacheVO actionCacheVO = BeanPlusUtil.toBeanIgnoreError(deviceAction, DeviceActionCacheVO.class);
                linkCacheDataHelper.setDeviceActionCacheVO(deviceCacheVOOptional.get().getProductIdentification(), deviceCacheVOOptional.get().getDeviceIdentification(), actionCacheVO);
            }
        }

        return deviceAction;
    }

    /**
     * 查询设备动作数据VO列表
     *
     * @param query 查询参数
     * @return {@link List <DeviceActionResultVO>} 设备动作数据VO列表
     */
    @Override
    public List<DeviceActionResultVO> getDeviceActionResultVOList(DeviceActionPageQuery query) {
        return superManager.getDeviceActionResultVOList(query);
    }

    @Override
    public Boolean disconnectDevice(String deviceIdentification) {
        Optional<DeviceCacheVO> deviceCacheVOOptional = linkCacheDataHelper.getDeviceCacheVO(deviceIdentification);
        ArgumentAssert.isTrue(deviceCacheVOOptional.isPresent(), "Device does not exist!");
        DeviceCacheVO deviceCacheVO = deviceCacheVOOptional.get();
        KillClientRequestVO killClientRequestVO = new KillClientRequestVO().toBuilder()
                .clientId(deviceCacheVO.getClientId())
                .userId(deviceCacheVO.getDeviceIdentification())
                .tenantId(deviceCacheVO.getTenantId().toString())
                .clientType("web")
                .build();
        R<?> r = mqttBrokerOpenAnyUserFacade.closeConnection(killClientRequestVO);
        if (r.getIsSuccess()) {
            // 记录设备动作
            DeviceActionTypeEnum deviceActionTypeEnum = DeviceActionTypeEnum.DISCONNECT;
            DeviceActionSaveVO deviceActionSaveVO = new DeviceActionSaveVO();
            deviceActionSaveVO.setDeviceIdentification(deviceCacheVO.getDeviceIdentification());
            deviceActionSaveVO.setActionType(deviceActionTypeEnum.getValue());
            deviceActionSaveVO.setMessage(JSON.toJSONString(killClientRequestVO));
            deviceActionSaveVO.setStatus(DeviceActionStatusEnum.SUCCESSFUL.getValue());
            deviceActionSaveVO.setRemark("Manual Operation..." + deviceActionTypeEnum.getDesc());
            saveDeviceAction(deviceActionSaveVO);
        }
        return r.getIsSuccess();
    }


    /**
     * 构建 DeviceActionSaveVO 对象
     *
     * @param deviceActionSaveVO 要进行构建的对象
     * @return 构建好的 DeviceAction 对象
     */
    private DeviceAction builderDeviceActionSaveVO(DeviceActionSaveVO deviceActionSaveVO) {
        return BeanPlusUtil.toBeanIgnoreError(deviceActionSaveVO, DeviceAction.class);
    }

    /**
     * 检查 DeviceActionSaveVO 参数完整性
     *
     * @param deviceActionSaveVO 要进行检查的对象
     */
    private void checkedDeviceActionSaveVO(DeviceActionSaveVO deviceActionSaveVO) {
        ArgumentAssert.notNull(deviceActionSaveVO, "deviceActionSaveVO Cannot be null");
        ArgumentAssert.notBlank(deviceActionSaveVO.getDeviceIdentification(), "deviceIdentification Cannot be null");
        ArgumentAssert.notBlank(deviceActionSaveVO.getActionType(), "actionType Cannot be null");
        ArgumentAssert.notBlank(deviceActionSaveVO.getMessage(), "message Cannot be null");
        ArgumentAssert.notNull(deviceActionSaveVO.getStatus(), "status Cannot be null");
    }


}


