package com.mqttsnet.thinglinks.video.controller.device;

import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.vo.query.device.VideoChannelPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoChannelSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoChannelUpdateVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器 - 统一通道管理
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/videoChannel")
@Tag(name = "统一通道管理")
public class VideoChannelController extends SuperController<VideoChannelService, Long, VideoChannel,
        VideoChannelSaveVO, VideoChannelUpdateVO, VideoChannelPageQuery, VideoChannelResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoChannel> handlerWrapper(VideoChannel model, PageParams<VideoChannelPageQuery> params) {
        QueryWrap<VideoChannel> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_channel");
        return queryWrap;
    }
}
