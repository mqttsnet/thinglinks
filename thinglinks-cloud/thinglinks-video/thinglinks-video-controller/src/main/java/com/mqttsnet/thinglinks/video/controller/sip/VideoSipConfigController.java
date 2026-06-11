package com.mqttsnet.thinglinks.video.controller.sip;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.entity.device.VideoSipConfig;
import com.mqttsnet.thinglinks.video.gb28181.SipLayer;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.service.device.VideoSipConfigService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.query.device.VideoSipConfigPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoSipConfigResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoSipConfigSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoSipConfigUpdateVO;
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
import java.util.Map;

/**
 * Description:
 * 租户 SIP 服务配置控制器。
 * <p>
 * 独立一级菜单，管理员专用。提供 SIP 接入配置 CRUD、缓存刷新、节点状态查询。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/sipConfig")
@Tag(name = "SIP服务配置")
public class VideoSipConfigController extends SuperController<VideoSipConfigService, Long, VideoSipConfig,
        VideoSipConfigSaveVO, VideoSipConfigUpdateVO, VideoSipConfigPageQuery, VideoSipConfigResultVO> {

    private final EchoService echoService;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final SipConfig sipConfig;
    private final SipLayer sipLayer;
    private final VideoDeviceService videoDeviceService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoSipConfig> handlerWrapper(VideoSipConfig model, PageParams<VideoSipConfigPageQuery> params) {
        QueryWrap<VideoSipConfig> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_sip_config");
        return queryWrap;
    }

    /**
     * 新增前校验 sipId 全局唯一 + 不与已接入设备的国标编号冲突。
     */
    @Override
    public R<VideoSipConfig> handlerSave(VideoSipConfigSaveVO saveVO) {
        validateSipId(saveVO.getSipId());
        R<VideoSipConfig> result = super.handlerSave(saveVO);
        // 保存后刷缓存
        superService.refreshTenantCache(com.mqttsnet.basic.context.ContextUtil.getTenantId());
        return result;
    }

    /**
     * 更新前校验 + 更新后刷缓存。
     */
    @Override
    public R<VideoSipConfig> handlerUpdate(VideoSipConfigUpdateVO updateVO) {
        validateSipId(updateVO.getSipId());
        R<VideoSipConfig> result = super.handlerUpdate(updateVO);
        superService.refreshTenantCache(com.mqttsnet.basic.context.ContextUtil.getTenantId());
        return result;
    }

    /**
     * 校验"SIP 服务器编号"：格式合法 + 全局唯一 + 不与本租户已接入设备的国标编号冲突。
     * <p>第三条是关键：摄像头默认会把"SIP 服务器编号"和"设备编号"都设成同一个值（比如 44010200492000000001），
     * 用户照搬填到平台 SIP 配置里，就会出现"INVITE From URI 用户名 = 设备自己的 deviceId"
     * 的自冲突，设备 200 OK 后立即 BYE，前端无限转圈。这里在保存前就拦掉，比让用户跑一遍 INVITE 再排错友好得多。
     */
    private void validateSipId(String sipId) {
        if (sipId == null) {
            return;
        }
        if (!sipId.matches("\\d{20}")) {
            throw BizException.wrap("SIP 服务器编号必须为 20 位数字");
        }
        if (tenantSipConfigProvider.isSipIdExists(sipId)) {
            // 全局唯一性校验：保存时前端可能没传 ID（新增），或者改成另一个已被占用的值（编辑）
            // 这里粗判一下，若该 sipId 在 Hash 中已存在，提示冲突
            throw BizException.wrap("SIP 服务器编号 [" + sipId + "] 已被其他租户使用");
        }
        VideoDeviceResultVO conflictDevice = videoDeviceService.getByDeviceIdentification(sipId);
        if (conflictDevice != null) {
            throw BizException.wrap("SIP 服务器编号 [" + sipId + "] 与本租户已接入的设备编号相同，"
                    + "GB28181 不允许这种配置（出站 INVITE 的 From URI 用户名会与设备 deviceId 重叠，"
                    + "设备会在 200 OK 后立即 BYE 导致拉不到流）。请改用其他不与任何设备国标编号冲突的 20 位数字。");
        }
    }

    /**
     * 删除后刷缓存
     */
    @Override
    public R<Boolean> handlerDelete(List<Long> ids) {
        R<Boolean> result = super.handlerDelete(ids);
        superService.refreshTenantCache(com.mqttsnet.basic.context.ContextUtil.getTenantId());
        return result;
    }

    /**
     * 设为默认配置
     */
    @PostMapping("/{id}/setDefault")
    @Operation(summary = "设为默认", description = "将指定配置设为当前租户的默认 SIP 配置")
    public R<Boolean> setDefault(@Parameter(description = "配置ID") @PathVariable Long id) {
        superService.setDefault(id);
        return R.success(true);
    }

    /**
     * 手动刷新当前租户 Redis 缓存
     */
    @PostMapping("/refreshCache")
    @Operation(summary = "刷新缓存", description = "将当前租户的 SIP 配置刷新到 Redis")
    public R<Boolean> refreshCache() {
        superService.refreshTenantCache(com.mqttsnet.basic.context.ContextUtil.getTenantId());
        return R.success(true);
    }

    /**
     * 获取 SIP 服务器物理节点信息（全局，只读）
     */
    @GetMapping("/serverInfo")
    @Operation(summary = "服务器信息", description = "获取 SIP 服务器物理节点信息（监听端口、网卡列表）")
    public R<Map<String, Object>> serverInfo() {
        return R.success(Map.of(
                "port", sipConfig.getPort(),
                "monitorIps", sipLayer.getMonitorIps(),
                "timeout", sipConfig.getTimeout()
        ));
    }
}
