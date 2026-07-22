package com.mqttsnet.thinglinks.video.controller.platform;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.video.service.platform.VideoPlatformChannelService;
import com.mqttsnet.thinglinks.video.vo.result.platform.VideoPlatformChannelResultVO;
import com.mqttsnet.thinglinks.video.vo.save.platform.VideoPlatformChannelSaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端控制器 - 级联平台通道管理
 * <p>
 * 提供级联平台通道的查询、绑定与解绑 REST API。
 * 绑定操作为专有业务逻辑，不使用 SuperController。
 *
 * @author mqttsnet
 * @date 2026-04-01
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/platformChannel")
@Tag(name = "级联平台通道管理")
public class VideoPlatformChannelController {

    private final VideoPlatformChannelService videoPlatformChannelService;
    private final EchoService echoService;

    /**
     * 查询指定平台已绑定的通道列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询平台通道列表", description = "获取指定平台已绑定的所有通道")
    public R<List<VideoPlatformChannelResultVO>> listByPlatformId(
            @Parameter(description = "平台ID", required = true) @RequestParam Long platformId) {
        List<VideoPlatformChannelResultVO> list = videoPlatformChannelService.listResultVOByPlatformId(platformId);
        echoService.action(list);
        return R.success(list);
    }

    /**
     * 将设备通道绑定到级联平台
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定通道", description = "将设备通道绑定到级联平台")
    public R<Boolean> bindChannel(@Valid @RequestBody VideoPlatformChannelSaveVO saveVO) {
        log.info("绑定平台通道: platformId={}, deviceIdentification={}, channelIdentification={}",
                saveVO.getPlatformId(), saveVO.getDeviceIdentification(), saveVO.getChannelIdentification());
        videoPlatformChannelService.bindChannel(saveVO.getPlatformId(), saveVO.getDeviceChannelId(),
                saveVO.getCatalogId(), saveVO.getDeviceIdentification(), saveVO.getChannelIdentification());
        return R.success(true);
    }

    /**
     * 从级联平台解绑设备通道
     */
    @DeleteMapping("/unbind")
    @Operation(summary = "解绑通道", description = "从级联平台解绑设备通道")
    public R<Boolean> unbindChannel(
            @Parameter(description = "平台ID", required = true) @RequestParam Long platformId,
            @Parameter(description = "设备通道ID", required = true) @RequestParam Long deviceChannelId) {
        log.info("解绑平台通道: platformId={}, deviceChannelId={}", platformId, deviceChannelId);
        videoPlatformChannelService.unbindChannel(platformId, deviceChannelId);
        return R.success(true);
    }
}
