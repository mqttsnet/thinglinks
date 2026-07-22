package com.mqttsnet.thinglinks.video.service.device.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceExtendParams;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoUpdatedEvent;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;
import com.mqttsnet.thinglinks.video.manager.device.VideoDeviceManager;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoDeviceSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 业务实现类 - 统一设备表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceServiceImpl extends SuperServiceImpl<VideoDeviceManager, Long, VideoDevice> implements VideoDeviceService {

    private final VideoCacheDataHelper videoCacheDataHelper;
    private final ApplicationEventPublisher eventPublisher;
    @Lazy
    private final VideoChannelService videoChannelService;

    @Override
    public VideoDeviceResultVO getByDeviceIdentification(String deviceIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification不能为空");
        VideoDevice device = superManager.getOneByDeviceIdentification(deviceIdentification);
        return BeanPlusUtil.toBeanIgnoreError(device, VideoDeviceResultVO.class);
    }

    @Override
    public void saveDeviceInfo(VideoDeviceSaveVO saveVO) {
        ArgumentAssert.notNull(saveVO, "saveVO不能为空");
        VideoDevice entity = BeanPlusUtil.toBeanIgnoreError(saveVO, VideoDevice.class);
        superManager.save(entity);
    }

    @Override
    public void updateDeviceInfo(VideoDeviceUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO, "updateVO不能为空");
        // 如果没有 id，根据 deviceIdentification 查找
        if (updateVO.getId() == null && updateVO.getDeviceIdentification() != null) {
            VideoDevice existing = superManager.getOneByDeviceIdentification(updateVO.getDeviceIdentification());
            if (existing != null) {
                updateVO.setId(existing.getId());
            }
        }
        VideoDevice entity = BeanPlusUtil.toBeanIgnoreError(updateVO, VideoDevice.class);
        superManager.updateById(entity);
        // 写库成功后发"设备信息更新"事件：监听方负责按最新 DB 行刷缓存，
        // 让任何下游链路读到的都是最新值，避免缓存陈旧后又被写回 DB 形成覆盖循环。
        publishDeviceUpdated(updateVO.getDeviceIdentification(), updateVO.getId());
    }

    /**
     * 发布设备更新事件。优先用 deviceIdentification；只有 id 时用 id 反查。
     */
    private void publishDeviceUpdated(String deviceIdentification, Long id) {
        try {
            String key = deviceIdentification;
            if (StrUtil.isBlank(key) && id != null) {
                VideoDevice fresh = superManager.getById(id);
                key = fresh != null ? fresh.getDeviceIdentification() : null;
            }
            if (StrUtil.isNotBlank(key)) {
                eventPublisher.publishEvent(new DeviceInfoUpdatedEvent(key));
            }
        } catch (Exception e) {
            log.warn("[设备缓存] 更新事件发布失败，回退为直接清缓存: deviceIdentification={}, id={}", deviceIdentification, id, e);
            try {
                if (StrUtil.isNotBlank(deviceIdentification)) {
                    videoCacheDataHelper.removeDeviceInfo(deviceIdentification);
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    /**
     * SuperController 通用 save 入口的前置处理：
     * <ul>
     *   <li>对主动拉流类协议（RTSP / ONVIF）做必填字段校验</li>
     *   <li>校验通过后，确保设备有合理默认值（在线状态、传输模式等）</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <SaveVO> VideoDevice saveBefore(SaveVO saveVO) {
        VideoDevice entity = super.saveBefore(saveVO);
        if (saveVO instanceof VideoDeviceSaveVO vo) {
            applyActiveStreamProtocolDefaults(vo, entity);
        }
        return entity;
    }

    /**
     * SuperController 通用 save 入口的后置处理：
     * 对主动拉流类协议（RTSP / ONVIF）自动创建一条默认通道，
     * channelIdentification 复用 deviceIdentification，便于前端单通道点播。
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <SaveVO> void saveAfter(SaveVO saveVO, VideoDevice entity) {
        super.saveAfter(saveVO, entity);
        if (saveVO instanceof VideoDeviceSaveVO vo && isActiveStreamProtocol(vo.getAccessProtocol())) {
            ensureDefaultChannel(entity);
        }
    }

    /**
     * 主动拉流类协议（RTSP / ONVIF）：缺省字段补齐 + 必填校验。
     * 对 GB28181 / ISUP / JT1078 等被动注册协议保持原行为。
     */
    private void applyActiveStreamProtocolDefaults(VideoDeviceSaveVO saveVO, VideoDevice entity) {
        String protocol = saveVO.getAccessProtocol();
        if (!isActiveStreamProtocol(protocol)) {
            return;
        }
        VideoDeviceProtocolConfig.StreamSource src = Optional.ofNullable(entity.getProtocolConfig())
                .map(VideoDeviceProtocolConfig::getStreamSource).orElse(null);

        boolean hasUrl = src != null && StrUtil.isNotBlank(src.getUrl());
        boolean hasHostPort = StrUtil.isNotBlank(entity.getHost()) && entity.getPort() != null && entity.getPort() > 0;
        if (!hasUrl && !hasHostPort) {
            throw BizException.wrap(protocol + " 设备必须填写完整 URL 或 host+port");
        }
        // RTSP / ONVIF 设备不会自己 REGISTER，直接置在线，由后续点播时 ZLM 反馈纠正
        if (entity.getOnlineStatus() == null) {
            entity.setOnlineStatus(Boolean.TRUE);
        }
        if (StrUtil.isBlank(entity.getDeviceIdentification())) {
            // 主动拉流设备没有国标编号概念，缺省用 host:port 生成稳定 ID
            entity.setDeviceIdentification(generateActiveStreamDeviceId(protocol, entity.getHost(), entity.getPort()));
        }
    }

    private boolean isActiveStreamProtocol(String accessProtocol) {
        return AccessProtocolEnum.fromValue(accessProtocol)
                .map(p -> p == AccessProtocolEnum.RTSP || p == AccessProtocolEnum.ONVIF)
                .orElse(false);
    }

    /**
     * 主动拉流设备稳定标识：{@code RTSP_192.168.1.108_554}。
     * 同 host/port 重复创建会被通道唯一约束拦截，便于幂等。
     */
    private String generateActiveStreamDeviceId(String protocol, String host, Integer port) {
        return protocol + "_" + host + "_" + port;
    }

    /**
     * 为主动拉流设备创建默认通道（单通道场景），channelIdentification 复用 deviceIdentification。
     * 已存在则跳过，确保幂等。
     */
    private void ensureDefaultChannel(VideoDevice device) {
        String deviceId = device.getDeviceIdentification();
        if (StrUtil.isBlank(deviceId)) {
            return;
        }
        List<VideoChannelResultVO> existing = videoChannelService.listByDeviceIdentification(deviceId);
        if (existing != null && !existing.isEmpty()) {
            log.debug("[默认通道] 设备已存在通道，跳过自动创建: deviceIdentification={}, count={}", deviceId, existing.size());
            return;
        }
        VideoChannel channel = new VideoChannel();
        channel.setDeviceIdentification(deviceId);
        channel.setChannelIdentification(deviceId);
        channel.setChannelName(StrUtil.blankToDefault(device.getCustomName(), device.getDeviceName()));
        channel.setChannelType(0);
        channel.setOnlineStatus(Boolean.TRUE.equals(device.getOnlineStatus()));
        channel.setHost(device.getHost());
        channel.setPort(device.getPort());
        channel.setManufacturer(device.getManufacturer());
        channel.setModel(device.getModel());
        videoChannelService.save(channel);
        log.info("[默认通道] 自动创建: deviceIdentification={}, channelIdentification={}", deviceId, deviceId);
    }

    @Override
    public void patchProtocolConfig(String deviceIdentification, VideoDeviceProtocolConfig patch) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification不能为空");
        if (patch == null) {
            return;
        }
        VideoDevice device = superManager.getOneByDeviceIdentification(deviceIdentification);
        if (device == null) {
            log.warn("[patchProtocolConfig] 设备不存在，跳过: deviceIdentification={}", deviceIdentification);
            return;
        }
        VideoDeviceProtocolConfig current = device.getProtocolConfig();
        if (current == null) {
            current = new VideoDeviceProtocolConfig();
        }
        current.merge(patch);
        // 仅写 protocol_config 一列：构造空 entity，只 set id + protocolConfig，让 MyBatis-Plus
        // NOT_NULL 策略把 SET 子句裁剪到只剩这两个字段，其他字段保留 DB 原值。
        VideoDevice slim = new VideoDevice();
        slim.setId(device.getId());
        slim.setProtocolConfig(current);
        superManager.updateById(slim);
        log.debug("[patchProtocolConfig] 设备: {}, 已合并字段", deviceIdentification);
        // 主动刷缓存，下游读到的 protocol_config 始终是最新合并结果。
        publishDeviceUpdated(deviceIdentification, device.getId());
    }

    @Override
    public void patchExtendParams(String deviceIdentification, VideoDeviceExtendParams patch) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification不能为空");
        if (patch == null) {
            return;
        }
        VideoDevice device = superManager.getOneByDeviceIdentification(deviceIdentification);
        if (device == null) {
            log.warn("[patchExtendParams] 设备不存在，跳过: deviceIdentification={}", deviceIdentification);
            return;
        }
        VideoDeviceExtendParams current = VideoDeviceExtendParams.fromJson(device.getExtendParams());
        if (current == null) {
            current = new VideoDeviceExtendParams();
        }
        current.merge(patch);
        device.setExtendParams(current.toJsonString());
        superManager.updateById(device);
        log.debug("[patchExtendParams] 设备: {}, 已合并字段", deviceIdentification);
    }

    @Override
    public void forceOffline(String deviceIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification不能为空");
        VideoDevice device = superManager.getOneByDeviceIdentification(deviceIdentification);
        if (ObjectUtil.isNotNull(device)) {
            // 1. 更新 DB 在线状态
            device.setOnlineStatus(false);
            superManager.updateById(device);

            // 2. 清理 Redis 设备缓存
            videoCacheDataHelper.removeDeviceInfo(deviceIdentification);

            log.info("强制设备下线: deviceIdentification={}, DB已更新, Redis缓存已清理", deviceIdentification);
        }
    }

    @Override
    public long countOnline() {
        QueryWrap<VideoDevice> wrap = new QueryWrap<>();
        wrap.lambda().eq(VideoDevice::getOnlineStatus, true);
        return superManager.count(wrap);
    }

    @Override
    public long countOffline() {
        QueryWrap<VideoDevice> wrap = new QueryWrap<>();
        wrap.lambda().eq(VideoDevice::getOnlineStatus, false);
        return superManager.count(wrap);
    }

    @Override
    public long countTotal() {
        return superManager.count();
    }

    @Override
    public List<VideoDeviceResultVO> listOnlineDevices() {
        QueryWrap<VideoDevice> wrap = new QueryWrap<>();
        wrap.lambda().eq(VideoDevice::getOnlineStatus, true);
        List<VideoDevice> devices = superManager.list(wrap);
        return BeanPlusUtil.toBeanList(devices, VideoDeviceResultVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteDeviceWithChannels(String deviceIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification不能为空");

        VideoDevice device = superManager.getOneByDeviceIdentification(deviceIdentification);
        if (device == null) {
            return;
        }

        // 1. 删除该设备下所有通道
        List<VideoChannelResultVO> channels =
                videoChannelService.listByDeviceIdentification(deviceIdentification);
        if (channels != null && !channels.isEmpty()) {
            List<Long> channelIds = channels.stream()
                    .map(VideoChannelResultVO::getId)
                    .toList();
            videoChannelService.removeByIds(channelIds);
            log.info("[级联删除] 设备: {}, 删除通道 {} 个", deviceIdentification, channelIds.size());
        }

        // 2. 删除设备
        superManager.removeById(device.getId());

        // 3. 清理 Redis 缓存
        videoCacheDataHelper.removeDeviceInfo(deviceIdentification);

        log.info("[级联删除] 设备: {} 及其通道已删除", deviceIdentification);
    }
}
