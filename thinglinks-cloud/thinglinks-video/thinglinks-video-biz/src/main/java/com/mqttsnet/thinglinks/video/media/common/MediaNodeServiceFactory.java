package com.mqttsnet.thinglinks.video.media.common;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * 流媒体节点服务工厂类。
 * 根据 {@link VideoMediaServer#getType()} 自动选择对应的 {@link MediaNodeService} 实现，
 * 支持 ZLM 和 ABL 两种流媒体服务器类型。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MediaNodeServiceFactory {

    /**
     * 所有 MediaNodeService 实现（Spring 自动注入）
     */
    private final List<MediaNodeService> mediaNodeServices;

    /**
     * 类型 -> 服务实例映射（懒加载）
     */
    private volatile Map<String, MediaNodeService> serviceMap;

    /**
     * 根据流媒体服务器信息获取对应的节点服务
     *
     * @param mediaServer 流媒体服务器信息
     * @return 对应的 MediaNodeService 实现
     * @throws BizException 不支持的服务器类型
     */
    public MediaNodeService getService(VideoMediaServer mediaServer) {
        Objects.requireNonNull(mediaServer, "mediaServer不能为空");
        return getServiceByType(mediaServer.getType());
    }

    /**
     * 根据服务器类型标识获取对应的节点服务
     *
     * @param serverType 服务器类型（如 "zlm"、"abl"）
     * @return 对应的 MediaNodeService 实现
     * @throws BizException 不支持的服务器类型
     */
    public MediaNodeService getServiceByType(String serverType) {
        if (StrUtil.isBlank(serverType)) {
            throw BizException.wrap("流媒体服务器类型不能为空");
        }

        var map = getServiceMap();
        var service = map.get(serverType);
        if (service == null) {
            throw BizException.wrap("不支持的流媒体服务器类型: {}", serverType);
        }
        return service;
    }

    /**
     * 获取默认类型（ZLM）的节点服务
     *
     * @return ZLM 的 MediaNodeService 实现
     */
    public MediaNodeService getDefaultService() {
        return getServiceByType(VideoMediaServerTypeEnum.ZLM.getValue());
    }

    /**
     * 获取类型到服务实例的映射（双重检查锁懒加载）
     *
     * @return 服务映射
     */
    private Map<String, MediaNodeService> getServiceMap() {
        if (serviceMap == null) {
            synchronized (this) {
                if (serviceMap == null) {
                    serviceMap = mediaNodeServices.stream()
                            .collect(Collectors.toMap(
                                    MediaNodeService::getServerType,
                                    Function.identity(),
                                    (existing, replacement) -> {
                                        log.warn("发现重复的 MediaNodeService 类型: {}，使用后注册的实现",
                                                existing.getServerType());
                                        return replacement;
                                    }
                            ));
                    log.info("MediaNodeServiceFactory 初始化完成，已注册服务类型: {}", serviceMap.keySet());
                }
            }
        }
        return serviceMap;
    }
}
