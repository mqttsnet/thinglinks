package com.mqttsnet.thinglinks.video.service.device.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelConfig;
import com.mqttsnet.thinglinks.video.dto.device.event.ChannelInfoOfflineEvent;
import com.mqttsnet.thinglinks.video.dto.device.event.ChannelInfoOnlineEvent;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.manager.device.VideoChannelManager;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务实现类 - 统一通道表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoChannelServiceImpl extends SuperServiceImpl<VideoChannelManager, Long, VideoChannel> implements VideoChannelService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 更新接口未提供口令或只提供空白字符时，将字段置为 {@code null}，由全局
     * MyBatis-Plus {@code NOT_NULL} 更新策略跳过该列，保留数据库中的原口令。
     */
    @Override
    protected <UpdateVO> VideoChannel updateBefore(UpdateVO updateVO) {
        VideoChannel entity = super.updateBefore(updateVO);
        if (StrUtil.isBlank(entity.getPassword())) {
            entity.setPassword(null);
        }
        return entity;
    }

    @Override
    public List<VideoChannelResultVO> listByDeviceIdentification(String deviceIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification不能为空");
        List<VideoChannel> channels = superManager.listByDeviceIdentification(deviceIdentification);
        return BeanPlusUtil.toBeanList(channels, VideoChannelResultVO.class);
    }

    @Override
    public VideoChannelResultVO getByChannelIdentification(String channelIdentification) {
        ArgumentAssert.notBlank(channelIdentification, "channelIdentification不能为空");
        VideoChannel channel = superManager.getOneByChannelIdentification(channelIdentification);
        return BeanPlusUtil.toBeanIgnoreError(channel, VideoChannelResultVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int syncChannelsFromCatalog(String deviceIdentification, List<VideoChannel> channels) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification不能为空");
        if (CollUtil.isEmpty(channels)) {
            return 0;
        }
        int count = 0;
        for (VideoChannel incoming : channels) {
            incoming.setDeviceIdentification(deviceIdentification);
            VideoChannel existing = superManager.getOneByDeviceAndChannel(
                    deviceIdentification, incoming.getChannelIdentification());
            if (existing != null) {
                incoming.setId(existing.getId());
                mergeChannelConfig(existing, incoming);
                superManager.updateById(incoming);
            } else {
                superManager.save(incoming);
            }
            publishChannelStatusEvent(incoming);
            count++;
        }
        log.info("[通道同步] 设备: {}, 同步通道数: {}", deviceIdentification, count);
        return count;
    }

    /**
     * Catalog 同步 channelConfig 合并策略。
     * <p>
     * 约定：{@link VideoChannelConfig#getInfo()} 节点由 GB28181 Catalog 全权维护，
     * 每次同步以 incoming 为准覆盖；其他顶层字段视为业务自定义配置，合并时从 existing
     * 原样保留，避免 catalog 同步抹掉业务侧写入的设置。
     * <p>未来若 {@link VideoChannelConfig} 新增业务自定义字段，需要在这里同步判断保留，
     * 以保证 catalog 只管自己的那一块。
     */
    private static void mergeChannelConfig(VideoChannel existing, VideoChannel incoming) {
        VideoChannelConfig existingCfg = existing.getChannelConfig();
        if (existingCfg == null) {
            return;
        }
        VideoChannelConfig incomingCfg = incoming.getChannelConfig();
        if (incomingCfg == null) {
            // incoming 没带 config（例如 catalog 没上报 Info），但要保留 existing 的全部内容
            incoming.setChannelConfig(existingCfg);
            return;
        }
        // 未来新增非 info 的业务字段时，在这里加 "if (incomingCfg.getXxx() == null) incomingCfg.setXxx(existingCfg.getXxx());"
        // 当前 VideoChannelConfig 只有 info 节点，catalog 同步直接以 incoming 为准，无需合并其他字段。
    }

    @Override
    public void updateOnlineStatus(String deviceIdentification, String channelIdentification, boolean online) {
        VideoChannel channel = superManager.getOneByDeviceAndChannel(deviceIdentification, channelIdentification);
        if (channel != null) {
            channel.setOnlineStatus(online);
            superManager.updateById(channel);
            publishChannelStatusEvent(channel);
        }
    }

    private void publishChannelStatusEvent(VideoChannel channel) {
        if (channel == null || channel.getChannelIdentification() == null) {
            return;
        }
        VideoChannelResultVO vo = BeanPlusUtil.toBeanIgnoreError(channel, VideoChannelResultVO.class);
        if (Boolean.TRUE.equals(channel.getOnlineStatus())) {
            eventPublisher.publishEvent(new ChannelInfoOnlineEvent(vo));
        } else {
            eventPublisher.publishEvent(new ChannelInfoOfflineEvent(vo));
        }
    }

    @Override
    public void removeByChannelIdentification(String deviceIdentification, String channelIdentification) {
        VideoChannel channel = superManager.getOneByDeviceAndChannel(deviceIdentification, channelIdentification);
        if (channel != null) {
            superManager.removeById(channel.getId());
        }
    }

    @Override
    public int countByDeviceIdentification(String deviceIdentification) {
        QueryWrap<VideoChannel> wrap = new QueryWrap<>();
        wrap.lambda().eq(VideoChannel::getDeviceIdentification, deviceIdentification);
        return (int) superManager.count(wrap);
    }
}
