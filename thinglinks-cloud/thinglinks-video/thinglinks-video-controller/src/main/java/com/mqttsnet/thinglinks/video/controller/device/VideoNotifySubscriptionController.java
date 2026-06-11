package com.mqttsnet.thinglinks.video.controller.device;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.device.VideoNotifySubscription;
import com.mqttsnet.thinglinks.video.notify.NotifyVariableEnum;
import com.mqttsnet.thinglinks.video.service.device.VideoNotifySubscriptionService;
import com.mqttsnet.thinglinks.video.vo.query.device.VideoNotifySubscriptionPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoNotifySubscriptionResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoNotifySubscriptionSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoNotifySubscriptionUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * 通知订阅控制器。
 * <p>
 * 继承 SuperController 提供标准 CRUD + 分页，
 * 额外提供模板变量列表查询。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/notifySubscription")
@Tag(name = "通知订阅管理")
public class VideoNotifySubscriptionController extends SuperController<VideoNotifySubscriptionService, Long, VideoNotifySubscription,
        VideoNotifySubscriptionSaveVO, VideoNotifySubscriptionUpdateVO, VideoNotifySubscriptionPageQuery, VideoNotifySubscriptionResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoNotifySubscription> handlerWrapper(VideoNotifySubscription model, PageParams<VideoNotifySubscriptionPageQuery> params) {
        QueryWrap<VideoNotifySubscription> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_notify_subscription");
        return queryWrap;
    }

    /**
     * 查询指定事件类型下可用的模板变量列表（前端渠道编辑页用）
     */
    @GetMapping("/variables")
    @Operation(summary = "模板变量列表", description = "根据事件类型返回可用的模板变量")
    public R<List<Map<String, String>>> variables(
            @Parameter(description = "事件类型: ALARM/DEVICE_ONLINE/DEVICE_OFFLINE/STREAM_CLOSE")
            @RequestParam(defaultValue = BizConstant.ALL) String eventType) {
        List<Map<String, String>> result = NotifyVariableEnum.getByEventType(eventType).stream()
                .map(v -> Map.of("key", v.getKey(), "label", v.getLabel()))
                .toList();
        return R.success(result);
    }
}
