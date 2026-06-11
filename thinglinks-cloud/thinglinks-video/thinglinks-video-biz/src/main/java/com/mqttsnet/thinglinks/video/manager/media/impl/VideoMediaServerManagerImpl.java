package com.mqttsnet.thinglinks.video.manager.media.impl;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.video.media.MediaServerHookCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.manager.media.VideoMediaServerManager;
import com.mqttsnet.thinglinks.video.mapper.media.VideoMediaServerMapper;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoMediaServerPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 流媒体服务器信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-03 17:56:38
 * @create [2024-07-03 17:56:38] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoMediaServerManagerImpl extends SuperManagerImpl<VideoMediaServerMapper, VideoMediaServer> implements VideoMediaServerManager {

    private final VideoMediaServerMapper videoMediaServerMapper;
    private final CachePlusOps cachePlusOps;


    @Override
    public VideoMediaServer getOneByMediaIdentification(String mediaIdentification) {
        QueryWrap<VideoMediaServer> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(StrUtil.isNotBlank(mediaIdentification), VideoMediaServer::getMediaIdentification, mediaIdentification);
        return videoMediaServerMapper.selectOne(queryWrap);
    }

    @Override
    public List<VideoMediaServer> getVideoMediaServerList(VideoMediaServerPageQuery query) {
        QueryWrap<VideoMediaServer> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(StrUtil.isNotBlank(query.getAppId()), VideoMediaServer::getAppId, query.getAppId());
        queryWrap.lambda().eq(StrUtil.isNotBlank(query.getMediaIdentification()), VideoMediaServer::getMediaIdentification, query.getMediaIdentification());
        queryWrap.lambda().eq(StrUtil.isNotBlank(query.getType()), VideoMediaServer::getType, query.getType());
        queryWrap.lambda().eq(query.getOnlineStatus() != null, VideoMediaServer::getOnlineStatus, query.getOnlineStatus());
        queryWrap.lambda().like(StrUtil.isNotBlank(query.getName()), VideoMediaServer::getName, query.getName());
        return videoMediaServerMapper.selectList(queryWrap);
    }

    @Override
    public void putOnlineHookCache(String mediaServerType, String mediaIdentification, VideoMediaServerResultDTO server) {
        CacheKey cacheKey = MediaServerHookCacheKeyBuilder.build(mediaServerType, mediaIdentification);
        cachePlusOps.set(cacheKey, server);
    }

    @Override
    public void removeOnlineHookCache(String mediaServerType, String mediaIdentification) {
        try {
            CacheKey cacheKey = MediaServerHookCacheKeyBuilder.build(mediaServerType, mediaIdentification);
            cachePlusOps.del(cacheKey);
        } catch (Exception ex) {
            log.warn("[MediaServer] 清理离线缓存失败（忽略）: type={}, mediaId={}, error={}", mediaServerType, mediaIdentification, ex.getMessage());
        }
    }

}


