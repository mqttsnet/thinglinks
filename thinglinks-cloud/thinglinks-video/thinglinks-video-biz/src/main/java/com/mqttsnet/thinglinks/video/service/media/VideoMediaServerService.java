package com.mqttsnet.thinglinks.video.service.media;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoMediaServerPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerResultVO;
import com.mqttsnet.thinglinks.video.vo.save.media.VideoMediaServerSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.media.VideoMediaServerUpdateVO;

import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 业务接口
 * 流媒体服务器信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-03 17:56:38
 * @create [2024-07-03 17:56:38] [mqttsnet]
 */
public interface VideoMediaServerService extends SuperService<Long, VideoMediaServer> {


    /**
     * 保存流媒体服务器信息
     *
     * @param saveVO 保存参数
     * @return {@link VideoMediaServerSaveVO} 实体
     */
    VideoMediaServerSaveVO saveMediaServer(VideoMediaServerSaveVO saveVO);

    /**
     * 更新流媒体服务器信息
     *
     * @param updateVO 更新参数
     * @return {@link VideoMediaServerUpdateVO} 实体
     */
    VideoMediaServerUpdateVO updateMediaServer(VideoMediaServerUpdateVO updateVO);


    /**
     * 删除流媒体服务器信息
     *
     * @param id 流媒体服务器ID
     * @return {@link Boolean} 删除结果
     */
    Boolean deleteMediaServer(Long id);

    /**
     * 获取流媒体服务器详情
     *
     * @param id id
     * @return {@link VideoMediaServerResultVO} 结果实体
     */
    VideoMediaServerResultVO getMediaServerDetails(Long id);


    /**
     * 根据媒体唯一标识获取流媒体服务器详情
     *
     * @param mediaIdentification 流媒体唯一标识
     * @return {@link VideoMediaServerResultVO} 结果实体
     */
    VideoMediaServerResultVO getOneByMediaIdentification(String mediaIdentification);

    /**
     * 根据媒体唯一标识获取流媒体服务器详情
     *
     * @param mediaIdentification 流媒体唯一标识
     * @return {@link VideoMediaServerResultDTO} 结果实体
     */
    VideoMediaServerResultDTO getVideoMediaServerResultDTO(String mediaIdentification);


    /**
     * 查询流媒体信息VO列表
     *
     * @param query 查询参数
     * @return {@link List<VideoMediaServerResultDTO>} 流媒体信息VO列表
     */
    List<VideoMediaServerResultDTO> getVideoMediaServerResultDTOList(VideoMediaServerPageQuery query);

    /**
     * 流媒体服务上线
     *
     * @param mediaServer 流媒体服务器信息
     */
    void serverOnline(VideoMediaServerResultDTO mediaServer);


    /**
     * 流媒体服务下线
     *
     * @param mediaServer 流媒体服务器信息
     */
    void serverOffline(VideoMediaServerResultDTO mediaServer);

    /**
     * 更新流媒体服务器性能指标（心跳上报时调用）
     *
     * @param mediaIdentification 媒体唯一标识
     * @param cpuUsage            CPU使用率
     * @param memoryUsage         内存使用率
     * @param currentStreams       当前流数量
     * @param networkInSpeed      入网速率bytes/s
     * @param networkOutSpeed     出网速率bytes/s
     */
    void updateServerMetrics(String mediaIdentification,
                             BigDecimal cpuUsage,
                             BigDecimal memoryUsage,
                             Integer currentStreams,
                             Long networkInSpeed,
                             Long networkOutSpeed);

    /**
     * 实时查询流媒体服务器性能指标（直接调用 ZLM HTTP API）
     *
     * @param id 流媒体服务器 ID
     * @return {@link VideoMediaServerMetricsResultVO} 实时性能指标
     */
    VideoMediaServerMetricsResultVO getRealTimeMetrics(Long id);

    /**
     * 测试流媒体服务器连接
     *
     * @param host     服务器地址
     * @param httpPort HTTP端口
     * @param secret   鉴权密钥
     * @return 连接是否成功
     */
    boolean testConnection(String host, Integer httpPort, String secret);
}
