package com.mqttsnet.thinglinks.video.controller.media;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.media.VideoStreamProxy;
import com.mqttsnet.thinglinks.video.service.media.VideoStreamProxyService;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoStreamProxyPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoStreamProxyResultVO;
import com.mqttsnet.thinglinks.video.vo.save.media.VideoStreamProxySaveVO;
import com.mqttsnet.thinglinks.video.vo.update.media.VideoStreamProxyUpdateVO;
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
 * 视频拉流代理信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-05 22:32:06
 * @create [2024-07-05 22:32:06] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/videoStreamProxy")
@Tag(name = "视频拉流代理信息")
public class VideoStreamProxyController extends SuperController<VideoStreamProxyService, Long, VideoStreamProxy, VideoStreamProxySaveVO,
        VideoStreamProxyUpdateVO, VideoStreamProxyPageQuery, VideoStreamProxyResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoStreamProxy> handlerWrapper(VideoStreamProxy model, PageParams<VideoStreamProxyPageQuery> params) {
        QueryWrap<VideoStreamProxy> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_stream_proxy");
        return queryWrap;
    }

    /**
     * 新增拉流代理信息
     *
     * @param saveVO 保存参数
     * @return {@link VideoStreamProxySaveVO} 实体
     */
    @Operation(summary = "保存拉流代理", description = "保存拉流代理信息")
    @PostMapping("/saveStreamProxy")
    @WebLog(value = "保存拉流代理", request = false)
    public R<VideoStreamProxySaveVO> saveStreamProxy(@RequestBody VideoStreamProxySaveVO saveVO) {
        return R.success(superService.saveStreamProxy(saveVO));
    }

    /**
     * 修改拉流代理信息
     *
     * @param updateVO 更新参数
     * @return {@link VideoStreamProxyUpdateVO} 实体
     */
    @Operation(summary = "修改拉流代理", description = "修改拉流代理信息")
    @PutMapping("/updateStreamProxy")
    @WebLog(value = "修改拉流代理", request = false)
    public R<VideoStreamProxyUpdateVO> updateStreamProxy(@RequestBody VideoStreamProxyUpdateVO updateVO) {
        return R.success(superService.updateStreamProxy(updateVO));
    }


    /**
     * 删除拉流代理信息
     *
     * @param id 拉流代理信息ID
     * @return 删除结果
     */
    @Operation(summary = "删除拉流代理信息", description = "根据拉流代理信息ID删除拉流代理信息")
    @Parameters({@Parameter(name = "id", description = "拉流代理信息ID", required = true),})
    @DeleteMapping("/deleteStreamProxy/{id}")
    @WebLog(value = "删除拉流代理信息", request = false)
    public R<Boolean> deleteStreamProxy(@PathVariable("id") Long id) {
        log.info("deleteStreamProxy id:{}", id);
        return R.success(superService.deleteStreamProxy(id));
    }

    /**
     * 批量删除拉流代理信息
     *
     * @param ids 拉流代理信息ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除拉流代理信息", description = "根据拉流代理信息ID列表批量删除拉流代理信息")
    @DeleteMapping("/deleteStreamProxies")
    @WebLog(value = "批量删除拉流代理信息", request = false)
    public R<Boolean> deleteStreamProxies(@RequestBody List<Long> ids) {
        log.info("deleteStreamProxies ids:{}", ids);
        boolean allDeleted = ids.stream().distinct().allMatch(id -> superService.deleteStreamProxy(id));
        return R.success(allDeleted);
    }

    /**
     * 根据流代理ID获取流代理详情
     *
     * @param id 流代理ID
     * @return 流代理详情
     */
    @Operation(summary = "根据流代理ID获取流代理详情", description = "根据流代理ID获取流代理详情")
    @GetMapping("/getStreamProxyDetails/{id}")
    @Parameters({@Parameter(name = "id", description = "流代理ID", required = true),})
    public R<VideoStreamProxyResultVO> getStreamProxyDetails(@PathVariable("id") Long id) {
        log.info("getStreamProxyDetails id:{}", id);
        VideoStreamProxyResultVO result = superService.getStreamProxyDetails(id);
        echoService.action(result);
        return R.success(result);
    }


    /**
     * 根据流代理ID获取流播放地址
     *
     * @param id 流代理ID
     * @return 流代理详情
     */
    @Operation(summary = "根据流代理ID获取流播放地址", description = "根据流代理ID获取流播放地址")
    @GetMapping("/getPlayUrl/{id}")
    @Parameters({@Parameter(name = "id", description = "流代理ID", required = true),})
    public R<VideoStreamProxyResultVO> getPlayUrl(@PathVariable("id") Long id) {
        log.info("getPlayUrl id:{}", id);
        VideoStreamProxyResultVO result = superService.getPlayUrl(id);
        echoService.action(result);
        return R.success(result);
    }


}


