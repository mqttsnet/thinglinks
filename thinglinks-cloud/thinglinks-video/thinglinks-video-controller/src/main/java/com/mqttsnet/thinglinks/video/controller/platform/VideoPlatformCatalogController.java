package com.mqttsnet.thinglinks.video.controller.platform;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformCatalog;
import com.mqttsnet.thinglinks.video.service.platform.VideoPlatformCatalogService;
import com.mqttsnet.thinglinks.video.vo.query.platform.VideoPlatformCatalogPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.platform.VideoPlatformCatalogResultVO;
import com.mqttsnet.thinglinks.video.vo.save.platform.VideoPlatformCatalogSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.platform.VideoPlatformCatalogUpdateVO;
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

/**
 * 前端控制器 - 级联目录管理
 * <p>
 * 继承 {@link SuperController} 提供标准 CRUD + 分页，
 * 额外提供按平台查询目录列表。
 *
 * @author mqttsnet
 * @date 2026-04-01
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/platformCatalog")
@Tag(name = "级联目录管理")
public class VideoPlatformCatalogController extends SuperController<VideoPlatformCatalogService, Long, VideoPlatformCatalog,
        VideoPlatformCatalogSaveVO, VideoPlatformCatalogUpdateVO, VideoPlatformCatalogPageQuery, VideoPlatformCatalogResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoPlatformCatalog> handlerWrapper(VideoPlatformCatalog model, PageParams<VideoPlatformCatalogPageQuery> params) {
        QueryWrap<VideoPlatformCatalog> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_platform_catalog");
        return queryWrap;
    }

    /**
     * 查询指定平台的所有目录
     */
    @GetMapping("/listByPlatform")
    @Operation(summary = "查询平台目录", description = "获取指定平台的所有目录")
    public R<List<VideoPlatformCatalog>> listByPlatformId(
            @Parameter(description = "平台ID", required = true) @RequestParam Long platformId) {
        return R.success(superService.listByPlatformId(platformId));
    }
}
