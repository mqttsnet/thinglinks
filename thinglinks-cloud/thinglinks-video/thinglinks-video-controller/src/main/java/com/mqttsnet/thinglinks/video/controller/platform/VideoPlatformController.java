package com.mqttsnet.thinglinks.video.controller.platform;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;
import com.mqttsnet.thinglinks.video.gb28181.platform.PlatformRegisterService;
import com.mqttsnet.thinglinks.video.service.platform.VideoPlatformService;
import com.mqttsnet.thinglinks.video.vo.query.platform.VideoPlatformPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.platform.VideoPlatformResultVO;
import com.mqttsnet.thinglinks.video.vo.save.platform.VideoPlatformSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.platform.VideoPlatformUpdateVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器 - 级联平台管理
 * <p>
 * 继承 {@link SuperController} 提供标准 CRUD + 分页，
 * 额外提供国标编号查询和启用/禁用操作。
 *
 * @author mqttsnet
 * @date 2026-04-01
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/platform")
@Tag(name = "级联平台管理")
public class VideoPlatformController extends SuperController<VideoPlatformService, Long, VideoPlatform,
        VideoPlatformSaveVO, VideoPlatformUpdateVO, VideoPlatformPageQuery, VideoPlatformResultVO> {

    private final EchoService echoService;
    private final PlatformRegisterService platformRegisterService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoPlatform> handlerWrapper(VideoPlatform model, PageParams<VideoPlatformPageQuery> params) {
        QueryWrap<VideoPlatform> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_platform");
        return queryWrap;
    }

    /**
     * 根据平台国标编号查询
     */
    @GetMapping("/byServerGbId")
    @Operation(summary = "根据国标编号查询平台", description = "根据平台国标编号获取平台信息")
    public R<VideoPlatform> getByServerGbId(
            @Parameter(description = "平台国标编号", required = true) @RequestParam String serverGbId) {
        return R.success(superService.getByServerGbId(serverGbId));
    }

    /**
     * 启用或禁用级联平台
     */
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用/禁用平台", description = "设置级联平台启用或禁用状态")
    public R<Boolean> setEnable(
            @Parameter(description = "平台ID", required = true) @PathVariable Long id,
            @Parameter(description = "启用状态", required = true) @RequestParam Boolean enable) {
        log.info("设置平台启用状态: id={}, enable={}", id, enable);
        VideoPlatform platform = superService.getById(id);
        if (platform != null) {
            platform.setEnable(enable);
            superService.updateById(platform);
        }
        return R.success(true);
    }

    /**
     * 手动启动级联注册（带租户上下文调用，解决启动时无法访问租户库的问题）
     */
    @PostMapping("/{id}/startCascade")
    @Operation(summary = "启动级联注册", description = "向上级平台发起 SIP REGISTER 注册")
    public R<Boolean> startCascade(
            @Parameter(description = "平台ID", required = true) @PathVariable Long id) {
        VideoPlatform platform = superService.getById(id);
        if (platform == null) {
            return R.fail("平台不存在");
        }
        if (!Boolean.TRUE.equals(platform.getEnable())) {
            return R.fail("平台未启用");
        }
        platformRegisterService.startCascade(platform);
        return R.success(true);
    }

    /**
     * 停止级联注册
     */
    @PostMapping("/{id}/stopCascade")
    @Operation(summary = "停止级联注册", description = "向上级平台发送注销并停止心跳")
    public R<Boolean> stopCascade(
            @Parameter(description = "平台ID", required = true) @PathVariable Long id) {
        platformRegisterService.stopCascade(id);
        return R.success(true);
    }
}
