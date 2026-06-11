package com.mqttsnet.thinglinks.video.controller.gateway;

import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.gateway.VideoGatewayMapping;
import com.mqttsnet.thinglinks.video.service.gateway.VideoGatewayMappingService;
import com.mqttsnet.thinglinks.video.vo.query.gateway.VideoGatewayMappingPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.gateway.VideoGatewayMappingResultVO;
import com.mqttsnet.thinglinks.video.vo.save.gateway.VideoGatewayMappingSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.gateway.VideoGatewayMappingUpdateVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器 - 网关协议映射
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/videoGatewayMapping")
@Tag(name = "网关协议映射")
public class VideoGatewayMappingController extends SuperController<VideoGatewayMappingService, Long, VideoGatewayMapping,
        VideoGatewayMappingSaveVO, VideoGatewayMappingUpdateVO, VideoGatewayMappingPageQuery, VideoGatewayMappingResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoGatewayMapping> handlerWrapper(VideoGatewayMapping model, PageParams<VideoGatewayMappingPageQuery> params) {
        QueryWrap<VideoGatewayMapping> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_gateway_mapping");
        return queryWrap;
    }
}
