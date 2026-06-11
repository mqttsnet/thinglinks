package com.mqttsnet.thinglinks.video.manager.media;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoMediaServerPageQuery;

import java.util.List;

/**
 * 流媒体服务器 Manager（DB + 在线 Hook 缓存原始操作）。
 *
 * @author mqttsnet
 */
public interface VideoMediaServerManager extends SuperManager<VideoMediaServer> {


    /**
     * 根据媒体唯一标识获取流媒体服务器详情
     *
     * @param mediaIdentification 流媒体唯一标识
     * @return {@link VideoMediaServer} 结果实体
     */
    VideoMediaServer getOneByMediaIdentification(String mediaIdentification);

    /**
     * 分页查询流媒体服务器列表
     * @param query 分页查询参数
     * @return {@link List<VideoMediaServer>} 流媒体服务器列表
     */
    List<VideoMediaServer> getVideoMediaServerList(VideoMediaServerPageQuery query);

    /**
     * 心跳 / Hook 在线缓存写入（按 mediaServerType + mediaIdentification 存储）。
     */
    void putOnlineHookCache(String mediaServerType, String mediaIdentification, VideoMediaServerResultDTO server);

    /**
     * 节点离线时清理 Hook 在线缓存。
     */
    void removeOnlineHookCache(String mediaServerType, String mediaIdentification);
}


