package com.mqttsnet.thinglinks.video.service.group.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroupRelation;
import com.mqttsnet.thinglinks.video.manager.group.VideoDeviceGroupRelationManager;
import com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupRelationService;
import com.mqttsnet.thinglinks.video.service.support.VideoIdentityNormalizer;
import com.mqttsnet.thinglinks.video.vo.result.group.VideoDeviceGroupRelationResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 设备分组关联业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceGroupRelationServiceImpl extends SuperServiceImpl<VideoDeviceGroupRelationManager, Long, VideoDeviceGroupRelation> implements VideoDeviceGroupRelationService {

    @Override
    protected <SaveVO> VideoDeviceGroupRelation saveBefore(SaveVO saveVO) {
        return normalizeIdentity(super.saveBefore(saveVO));
    }

    @Override
    protected <UpdateVO> VideoDeviceGroupRelation updateBefore(UpdateVO updateVO) {
        return normalizeIdentity(super.updateBefore(updateVO));
    }

    @Override
    public List<VideoDeviceGroupRelation> listByGroupId(Long groupId) {
        return superManager.listByGroupId(groupId);
    }

    @Override
    public List<VideoDeviceGroupRelation> listByDeviceIdentification(String deviceIdentification) {
        return superManager.listByDeviceIdentification(deviceIdentification);
    }

    @Override
    public void bindDevice(Long groupId, String deviceIdentification, String channelIdentification) {
        VideoDeviceGroupRelation relation = VideoDeviceGroupRelation.builder()
                .groupId(groupId)
                .deviceIdentification(StrUtil.trim(deviceIdentification))
                .channelIdentification(VideoIdentityNormalizer.trimAsciiSpaceToNull(channelIdentification))
                .sortOrder(0)
                .build();
        superManager.save(relation);
        log.info("绑定设备到分组: groupId={}, device={}, channel={}", groupId,
                relation.getDeviceIdentification(), relation.getChannelIdentification());
    }

    @Override
    public void unbindDevice(Long id) {
        superManager.removeById(id);
        log.info("解绑设备分组关联: id={}", id);
    }

    @Override
    public void removeByGroupId(Long groupId) {
        superManager.removeByGroupId(groupId);
        log.info("删除分组下所有关联: groupId={}", groupId);
    }

    @Override
    public List<VideoDeviceGroupRelationResultVO> listResultVOByGroupId(Long groupId) {
        return BeanUtil.copyToList(listByGroupId(groupId), VideoDeviceGroupRelationResultVO.class);
    }

    @Override
    public List<VideoDeviceGroupRelationResultVO> listResultVOByDeviceIdentification(String deviceIdentification) {
        return BeanUtil.copyToList(listByDeviceIdentification(deviceIdentification), VideoDeviceGroupRelationResultVO.class);
    }

    private VideoDeviceGroupRelation normalizeIdentity(VideoDeviceGroupRelation relation) {
        relation.setDeviceIdentification(StrUtil.trim(relation.getDeviceIdentification()));
        relation.setChannelIdentification(
                VideoIdentityNormalizer.trimAsciiSpaceToNull(relation.getChannelIdentification()));
        return relation;
    }
}
