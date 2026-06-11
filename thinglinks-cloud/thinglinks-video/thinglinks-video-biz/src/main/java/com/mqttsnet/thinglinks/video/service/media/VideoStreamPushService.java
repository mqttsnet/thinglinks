package com.mqttsnet.thinglinks.video.service.media;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.media.VideoStreamPush;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoStreamPushResultVO;
import com.mqttsnet.thinglinks.video.vo.save.media.VideoStreamPushSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.media.VideoStreamPushUpdateVO;


/**
 * <p>
 * 业务接口
 * 视频推流信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-07 19:19:57
 * @create [2024-07-07 19:19:57] [mqttsnet]
 */
public interface VideoStreamPushService extends SuperService<Long, VideoStreamPush> {

    /**
     * 保存视频推流信息
     *
     * @param saveVO 保存参数
     * @return {@link VideoStreamPushSaveVO} 实体
     */
    VideoStreamPushSaveVO saveStreamPush(VideoStreamPushSaveVO saveVO);

    /**
     * 更新视频推流信息
     *
     * @param updateVO 更新参数
     * @return {@link VideoStreamPushUpdateVO} 实体
     */
    VideoStreamPushUpdateVO updateStreamPush(VideoStreamPushUpdateVO updateVO);

    /**
     * 删除视频推流信息
     *
     * @param id 视频推流信息ID
     * @return {@link Boolean} 删除结果
     */
    boolean deleteStreamPush(Long id);

    /**
     * 获取视频推流详情
     *
     * @param id id
     * @return {@link VideoStreamPushResultVO} 详情结果实体
     */
    VideoStreamPushResultVO getStreamPushDetails(Long id);

    /**
     * 获取视频推流播放地址
     *
     * @param id 推流ID
     * @return {@link VideoStreamPushResultVO} 包含播放地址信息
     */
    VideoStreamPushResultVO getPlayUrl(Long id);
}


