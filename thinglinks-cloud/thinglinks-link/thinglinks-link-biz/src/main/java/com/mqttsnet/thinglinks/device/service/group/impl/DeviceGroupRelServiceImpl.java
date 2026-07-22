package com.mqttsnet.thinglinks.device.service.group.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.group.DeviceGroupRel;
import com.mqttsnet.thinglinks.device.manager.group.DeviceGroupRelManager;
import com.mqttsnet.thinglinks.device.service.group.DeviceGroupRelService;
import com.mqttsnet.thinglinks.device.vo.save.group.DeviceGroupRelSaveVO;
import com.mqttsnet.thinglinks.device.vo.update.group.DeviceGroupRelUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 业务实现类
 * 设备分组关系表
 * </p>
 *
 * @author mqttsnet
 * @since 2025-06-23 14:06:46
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceGroupRelServiceImpl extends SuperServiceImpl<DeviceGroupRelManager, Long, DeviceGroupRel> implements DeviceGroupRelService {


    @Override
    protected <UpdateVO> DeviceGroupRel updateBefore(UpdateVO vo) {
        DeviceGroupRelUpdateVO updateVO = (DeviceGroupRelUpdateVO) vo;

        if (updateVO.getGroupId() != null && StrUtil.isNotBlank(updateVO.getDeviceIdentification())) {
            if (superManager.count(Wraps.<DeviceGroupRel>lbQ()
                    .eq(DeviceGroupRel::getGroupId, updateVO.getGroupId())
                    .eq(DeviceGroupRel::getDeviceIdentification, updateVO.getDeviceIdentification())
                    .ne(DeviceGroupRel::getId, updateVO.getId())) > 0) {
                throw BizException.wrap("The device is already in this group");
            }
        }

        return super.updateBefore(updateVO);
    }

    @Override
    protected <SaveVO> DeviceGroupRel saveBefore(SaveVO vo) {
        DeviceGroupRelSaveVO saveVO = (DeviceGroupRelSaveVO) vo;

        if (superManager.count(Wraps.<DeviceGroupRel>lbQ()
                .eq(DeviceGroupRel::getGroupId, saveVO.getGroupId())
                .eq(DeviceGroupRel::getDeviceIdentification, saveVO.getDeviceIdentification())) > 0) {
            throw BizException.wrap("The device is already in this group");
        }

        return super.saveBefore(saveVO);
    }

    @Override
    public void removeByGroupIds(Collection<Long> groupIdList) {
        if (CollUtil.isEmpty(groupIdList)) {
            log.warn("GroupId list is empty, skip deletion");
            return;
        }
        superManager.remove(Wraps.<DeviceGroupRel>lbQ().in(DeviceGroupRel::getGroupId, groupIdList));
    }

    @Override
    public List<String> getDeviceIdentificationsByGroupIds(Collection<Long> groupIdList) {
        if (CollUtil.isEmpty(groupIdList)) {
            log.warn("GroupId list is empty, skip query");
            return List.of();
        }
        Optional<List<DeviceGroupRel>> deviceGroupRelListOptional = superManager.getDeviceGroupRelListByGroupIds(groupIdList);
        return deviceGroupRelListOptional.map(deviceGroupRels -> deviceGroupRels.stream().map(DeviceGroupRel::getDeviceIdentification).distinct().toList()).orElseGet(List::of);
    }

    @Override
    public void removeByDeviceIdentification(String deviceIdentification) {
        // 空值安全：deviceIdentification 缺失时直接跳过，避免空字符串条件匹配到全表数据
        Optional.ofNullable(deviceIdentification)
                .filter(StrUtil::isNotBlank)
                .ifPresent(identification -> {
                    boolean ok = superManager.remove(Wraps.<DeviceGroupRel>lbQ()
                            .eq(DeviceGroupRel::getDeviceIdentification, identification));
                    log.info("Clean device_group_rel for deleted device, deviceIdentification={}, ok={}",
                        identification, ok);
                });
    }
}


