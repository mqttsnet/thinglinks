package com.mqttsnet.thinglinks.video.controller.record;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordPlan;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanService;
import com.mqttsnet.thinglinks.video.vo.query.record.VideoRecordPlanPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.record.VideoRecordPlanResultVO;
import com.mqttsnet.thinglinks.video.vo.save.record.VideoRecordPlanSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.record.VideoRecordPlanUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端控制器 - 录像计划管理
 * <p>
 * 继承 {@link SuperController} 提供标准 CRUD + 分页，
 * 额外提供激活/停用和查询启用计划。
 *
 * @author mqttsnet
 * @date 2026-04-01
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/recordPlan")
@Tag(name = "录像计划管理")
public class VideoRecordPlanController extends SuperController<VideoRecordPlanService, Long, VideoRecordPlan,
        VideoRecordPlanSaveVO, VideoRecordPlanUpdateVO, VideoRecordPlanPageQuery, VideoRecordPlanResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoRecordPlan> handlerWrapper(VideoRecordPlan model, PageParams<VideoRecordPlanPageQuery> params) {
        QueryWrap<VideoRecordPlan> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_record_plan");
        return queryWrap;
    }

    /**
     * 查询当前启用状态的录像计划
     */
    @GetMapping("/active")
    @Operation(summary = "查询启用的录像计划", description = "获取当前启用状态的录像计划")
    public R<List<VideoRecordPlan>> listActive() {
        return R.success(superService.listActivePlans());
    }

    /**
     * 激活（启用）录像计划
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "激活录像计划", description = "启用录像计划")
    public R<Boolean> activate(
            @Parameter(description = "计划ID", required = true) @PathVariable Long id) {
        superService.activatePlan(id);
        return R.success(true);
    }

    /**
     * 停用（禁用）录像计划
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "停用录像计划", description = "停用录像计划")
    public R<Boolean> deactivate(
            @Parameter(description = "计划ID", required = true) @PathVariable Long id) {
        superService.deactivatePlan(id);
        return R.success(true);
    }
}
