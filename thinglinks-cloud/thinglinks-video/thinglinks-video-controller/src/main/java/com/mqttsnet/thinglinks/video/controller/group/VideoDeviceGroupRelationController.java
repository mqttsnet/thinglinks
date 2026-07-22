package com.mqttsnet.thinglinks.video.controller.group;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupRelationService;
import com.mqttsnet.thinglinks.video.vo.result.group.VideoDeviceGroupRelationResultVO;
import com.mqttsnet.thinglinks.video.vo.save.group.VideoDeviceGroupRelationSaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端控制器 - 设备分组关联
 * <p>
 * 提供设备/通道与分组的绑定、解绑及查询 REST API。
 * 绑定操作为专有业务逻辑，不使用 SuperController。
 *
 * @author mqttsnet
 * @date 2026-04-01
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/deviceGroupRelation")
@Tag(name = "设备分组关联")
public class VideoDeviceGroupRelationController {

    private final VideoDeviceGroupRelationService videoDeviceGroupRelationService;
    private final EchoService echoService;

    /**
     * 查询指定分组下的所有设备/通道关联记录
     */
    @GetMapping("/listByGroup")
    @Operation(summary = "查询分组设备列表", description = "获取指定分组下的所有设备/通道关联")
    public R<List<VideoDeviceGroupRelationResultVO>> listByGroupId(
            @Parameter(description = "分组ID", required = true) @RequestParam Long groupId) {
        List<VideoDeviceGroupRelationResultVO> list = videoDeviceGroupRelationService.listResultVOByGroupId(groupId);
        echoService.action(list);
        return R.success(list);
    }

    /**
     * 查询指定设备关联的所有分组记录
     */
    @GetMapping("/listByDevice")
    @Operation(summary = "查询设备所属分组", description = "查询设备关联的所有分组")
    public R<List<VideoDeviceGroupRelationResultVO>> listByDevice(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification) {
        List<VideoDeviceGroupRelationResultVO> list = videoDeviceGroupRelationService.listResultVOByDeviceIdentification(deviceIdentification);
        echoService.action(list);
        return R.success(list);
    }

    /**
     * 将设备/通道绑定到指定分组
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定设备到分组", description = "将设备/通道绑定到指定分组")
    public R<Boolean> bind(@Valid @RequestBody VideoDeviceGroupRelationSaveVO saveVO) {
        videoDeviceGroupRelationService.bindDevice(saveVO.getGroupId(), saveVO.getDeviceIdentification(), saveVO.getChannelIdentification());
        return R.success(true);
    }

    /**
     * 解绑设备/通道与分组的关系
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "解绑设备", description = "从分组中解绑设备/通道")
    public R<Boolean> unbind(
            @Parameter(description = "关联ID", required = true) @PathVariable Long id) {
        videoDeviceGroupRelationService.unbindDevice(id);
        return R.success(true);
    }
}
