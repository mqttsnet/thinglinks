package com.mqttsnet.thinglinks.video.controller.media;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.media.VideoStreamPush;
import com.mqttsnet.thinglinks.video.service.media.VideoStreamPushService;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoStreamPushPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoStreamPushResultVO;
import com.mqttsnet.thinglinks.video.vo.save.media.VideoStreamPushSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.media.VideoStreamPushUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * 视频推流信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-07 19:19:57
 * @create [2024-07-07 19:19:57] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/videoStreamPush")
@Tag(name = "视频推流信息")
public class VideoStreamPushController extends SuperController<VideoStreamPushService, Long, VideoStreamPush, VideoStreamPushSaveVO,
        VideoStreamPushUpdateVO, VideoStreamPushPageQuery, VideoStreamPushResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoStreamPush> handlerWrapper(VideoStreamPush model, PageParams<VideoStreamPushPageQuery> params) {
        QueryWrap<VideoStreamPush> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_stream_push");
        return queryWrap;
    }


    /**
     * 新增视频推流信息
     *
     * @param saveVO 保存参数
     * @return {@link VideoStreamPushSaveVO} 实体
     */
    @Operation(summary = "保存视频推流信息", description = "保存视频推流信息")
    @PostMapping("/saveStreamPush")
    @WebLog(value = "保存视频推流信息", request = false)
    public R<VideoStreamPushSaveVO> saveStreamPush(@RequestBody VideoStreamPushSaveVO saveVO) {
        log.info("saveStreamPush saveVO:{}", saveVO);
        return R.success(superService.saveStreamPush(saveVO));
    }

    /**
     * 修改视频推流信息
     *
     * @param updateVO 更新参数
     * @return {@link VideoStreamPushUpdateVO} 实体
     */
    @Operation(summary = "修改视频推流信息", description = "修改视频推流信息")
    @PutMapping("/updateStreamPush")
    @WebLog(value = "修改视频推流信息", request = false)
    public R<VideoStreamPushUpdateVO> updateStreamPush(@RequestBody VideoStreamPushUpdateVO updateVO) {
        log.info("updateStreamPush updateVO:{}", updateVO);

        return R.success(superService.updateStreamPush(updateVO));
    }

    /**
     * 删除视频推流信息
     *
     * @param id 视频推流信息ID
     * @return 删除结果
     */
    @Operation(summary = "删除视频推流信息", description = "根据视频推流信息ID删除视频推流信息")
    @Parameters({@Parameter(name = "id", description = "视频推流信息ID", required = true),})
    @DeleteMapping("/deleteStreamPush/{id}")
    @WebLog(value = "删除视频推流信息", request = false)
    public R<Boolean> deleteStreamPush(@PathVariable("id") Long id) {
        log.info("deleteStreamPush id:{}", id);
        return R.success(superService.deleteStreamPush(id));
    }

    /**
     * 批量删除视频推流信息
     *
     * @param ids 视频推流信息ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除视频推流信息", description = "根据视频推流信息ID列表批量删除视频推流信息")
    @DeleteMapping("/deleteStreamPushes")
    @WebLog(value = "批量删除视频推流信息", request = false)
    public R<Boolean> deleteStreamPushes(@RequestBody List<Long> ids) {
        log.info("deleteStreamPushes ids:{}", ids);
        boolean allDeleted = ids.stream().distinct().allMatch(id -> superService.deleteStreamPush(id));
        return R.success(allDeleted);
    }

    /**
     * 根据视频推流ID获取视频推流详情
     *
     * @param id 视频推流ID
     * @return 视频推流详情
     */
    @Operation(summary = "根据视频推流ID获取视频推流详情", description = "根据视频推流ID获取视频推流详情")
    @GetMapping("/getStreamPushDetails/{id}")
    @Parameters({@Parameter(name = "id", description = "视频推流ID", required = true),})
    public R<VideoStreamPushResultVO> getStreamPushDetails(@PathVariable("id") Long id) {
        log.info("getStreamPushDetails id:{}", id);
        VideoStreamPushResultVO result = superService.getStreamPushDetails(id);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 获取视频推流播放地址
     *
     * @param id 视频推流ID
     * @return 包含播放地址信息的视频推流详情
     */
    @Operation(summary = "获取视频推流播放地址", description = "根据视频推流ID获取播放地址")
    @GetMapping("/getPlayUrl/{id}")
    @Parameters({@Parameter(name = "id", description = "视频推流ID", required = true),})
    public R<VideoStreamPushResultVO> getPlayUrl(@PathVariable("id") Long id) {
        log.info("getPlayUrl id:{}", id);
        VideoStreamPushResultVO result = superService.getPlayUrl(id);
        echoService.action(result);
        return R.success(result);
    }

}


