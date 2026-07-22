package com.mqttsnet.thinglinks.video.controller.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;
import com.mqttsnet.thinglinks.video.gb28181.cmd.QueryCommander;
import com.mqttsnet.thinglinks.video.gb28181.session.SsrcPoolService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.query.device.VideoDevicePageQuery;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceBatchResultVO;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoDeviceSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * 前端控制器 - 统一设备管理
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/videoDevice")
@Tag(name = "统一设备管理")
public class VideoDeviceController extends SuperController<VideoDeviceService, Long, VideoDevice,
        VideoDeviceSaveVO, VideoDeviceUpdateVO, VideoDevicePageQuery, VideoDeviceResultVO> {

    private final EchoService echoService;
    private final QueryCommander queryCommander;
    private final SsrcPoolService ssrcPoolService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoDevice> handlerWrapper(VideoDevice model, PageParams<VideoDevicePageQuery> params) {
        QueryWrap<VideoDevice> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_device");
        return queryWrap;
    }

    /**
     * 删除前校验：有活跃通道（正在推流/播放）时拒绝删除，否则级联删除通道。
     * 活跃会话校验通过 {@link SsrcPoolService#hasActiveSessions(String)} 走 Service 层。
     */
    @Override
    public R<Boolean> handlerDelete(List<Long> ids) {
        for (Long id : ids) {
            VideoDevice device = superService.getById(id);
            if (device != null) {
                if (ssrcPoolService.hasActiveSessions(device.getDeviceIdentification())) {
                    throw BizException.wrap("设备 [" + device.getDeviceIdentification() + "] 下有活跃通道(正在推流/播放)，不允许删除");
                }
                superService.deleteDeviceWithChannels(device.getDeviceIdentification());
            }
        }
        return R.success(true);
    }

    /**
     * 手动触发设备目录同步
     */
    @PostMapping("/syncCatalog")
    @Operation(summary = "同步目录", description = "向设备发送GB28181目录查询指令，触发通道信息同步")
    @ApiResponse(responseCode = "200", description = "指令已发送")
    public R<Boolean> syncCatalog(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification) {
        VideoDeviceResultVO device = Optional.ofNullable(superService.getByDeviceIdentification(deviceIdentification))
                .orElseThrow(() -> BizException.wrap("设备不存在: " + deviceIdentification));
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线，无法同步: " + deviceIdentification);
        }
        String transport = StrUtil.blankToDefault(device.getTransport(), "UDP");
        queryCommander.catalogQuery(deviceIdentification, device.getHost(), device.getPort(), transport, GbProtocolVersionEnum.GB2016);
        log.info("手动触发目录同步: deviceIdentification={}", deviceIdentification);
        return R.success(true);
    }

    /**
     * 强制设备下线
     */
    @PostMapping("/forceOffline")
    @Operation(summary = "强制下线", description = "强制将设备标记为离线，清理缓存和活跃流")
    @ApiResponse(responseCode = "200", description = "操作成功")
    public R<Boolean> forceOffline(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification) {
        VideoDeviceResultVO device = Optional.ofNullable(superService.getByDeviceIdentification(deviceIdentification))
                .orElseThrow(() -> BizException.wrap("设备不存在: " + deviceIdentification));
        superService.forceOffline(deviceIdentification);
        log.info("强制设备下线: deviceIdentification={}", deviceIdentification);
        return R.success(true);
    }

    /**
     * 批量目录同步
     */
    @PostMapping("/batch/syncCatalog")
    @Operation(summary = "批量同步目录", description = "向多台设备发送目录查询指令")
    @ApiResponse(responseCode = "200", description = "操作完成")
    public R<VideoDeviceBatchResultVO> batchSyncCatalog(@RequestBody List<String> deviceIdentifications) {
        VideoDeviceBatchResultVO result = new VideoDeviceBatchResultVO();
        for (String deviceId : CollUtil.emptyIfNull(deviceIdentifications)) {
            try {
                VideoDeviceResultVO device = superService.getByDeviceIdentification(deviceId);
                if (ObjectUtil.isNull(device) || !Boolean.TRUE.equals(device.getOnlineStatus())) {
                    result.addFailed(deviceId, "设备不存在或离线");
                    continue;
                }
                String transport = StrUtil.blankToDefault(device.getTransport(), "UDP");
                queryCommander.catalogQuery(deviceId, device.getHost(), device.getPort(), transport,
                        GbProtocolVersionEnum.GB2016);
                result.addSuccess(deviceId);
            } catch (Exception e) {
                result.addFailed(deviceId, e.getMessage());
            }
        }
        return R.success(result);
    }

    /**
     * 批量强制下线
     */
    @PostMapping("/batch/forceOffline")
    @Operation(summary = "批量强制下线", description = "批量强制多台设备下线")
    @ApiResponse(responseCode = "200", description = "操作完成")
    public R<VideoDeviceBatchResultVO> batchForceOffline(@RequestBody List<String> deviceIdentifications) {
        VideoDeviceBatchResultVO result = new VideoDeviceBatchResultVO();
        for (String deviceId : CollUtil.emptyIfNull(deviceIdentifications)) {
            try {
                superService.forceOffline(deviceId);
                result.addSuccess(deviceId);
            } catch (Exception e) {
                result.addFailed(deviceId, e.getMessage());
            }
        }
        return R.success(result);
    }
}
