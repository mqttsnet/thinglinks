package com.mqttsnet.thinglinks.video.controller.anytenant;

import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.basic.utils.TenantUtil;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.HookResult;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.HookResultForOnPublish;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnPlayHookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnPublishHookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnRecordMp4HookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnRtpServerTimeoutHookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnSendRtpStoppedHookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnServerKeepaliveHookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnStreamChangedHookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnStreamNoneReaderHookParam;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.OnStreamNotFoundHookParam;
import com.mqttsnet.thinglinks.video.gb28181.session.SsrcPoolService;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaArrivalEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaDepartureEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaNotFoundEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaPublishEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaRecordMp4Event;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaRtpServerTimeoutEvent;
import com.mqttsnet.thinglinks.video.media.zlm.event.HookZlmServerKeepaliveEvent;
import com.mqttsnet.thinglinks.video.media.zlm.event.HookZlmServerStartEvent;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.service.stream.PlayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * ZLMediaKit HTTP Hook 回调控制器。
 * 接收 ZLM 流媒体服务器的各类 Hook 回调并发布对应事件。
 * <p>
 * 路径使用 anyTenant 权限：不需要 TenantID/登录/URI 权限验证。
 * Hook URL 格式：http://{gateway}:{port}/video/zlmHook/anyTenant/index/hook/{hookType}
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/zlmHook/anyTenant/index/hook")
@Tag(name = "ZLM Hook回调")
public class ZlmHookAnyTenantController {

    private final VideoMediaServerService videoMediaServerService;
    private final SsrcPoolService ssrcPoolService;
    private final PlayService playService;

    /**
     * ZLM hook 入口公共开场：从 mediaServerId（约定后缀 {@code @<tenantId>}）解出租户 ID
     * 立即写入 ThreadLocal，确保后续任何 {@code @DS(BASE_TENANT)} 调用都能正确路由到租户库。
     * 拿不到分隔符时退到内置默认租户，避免回退到 defaults DS。
     */
    private void setupTenantContext(String mediaServerId) {
        ContextUtil.setTenantId(TenantUtil.extractTenantIdWithDefault(mediaServerId));
    }

    /**
     * 流媒体服务器心跳
     */
    @PostMapping("/on_server_keepalive")
    @ResponseBody
    @Operation(summary = "服务器心跳", description = "ZLM服务器定时心跳回调")
    public HookResult onServerKeepalive(@RequestBody OnServerKeepaliveHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_server_keepalive: param={}", JSON.toJSONString(param));
        VideoMediaServerResultDTO mediaServer = getMediaServer(param.getMediaServerId());
        if (mediaServer != null) {
            HookZlmServerKeepaliveEvent event = new HookZlmServerKeepaliveEvent(this);
            event.setMediaServerItem(mediaServer);
            SpringUtils.publishEvent(event);
        }
        return HookResult.SUCCESS();
    }

    /**
     * 流媒体服务器启动通知
     */
    @PostMapping("/on_server_started")
    @ResponseBody
    @Operation(summary = "服务器启动", description = "ZLM服务器启动通知回调")
    public HookResult onServerStarted(@RequestBody OnServerKeepaliveHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_server_started: param={}", JSON.toJSONString(param));
        VideoMediaServerResultDTO mediaServer = getMediaServer(param.getMediaServerId());
        if (mediaServer != null) {
            HookZlmServerStartEvent event = new HookZlmServerStartEvent(this);
            event.setMediaServerItem(mediaServer);
            SpringUtils.publishEvent(event);
        }
        return HookResult.SUCCESS();
    }

    /**
     * 推流鉴权
     */
    @PostMapping("/on_publish")
    @ResponseBody
    @Operation(summary = "推流鉴权", description = "推流鉴权回调，返回是否允许推流")
    public HookResultForOnPublish onPublish(@RequestBody OnPublishHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_publish: param={}", JSON.toJSONString(param));
        VideoMediaServerResultDTO mediaServer = getMediaServer(param.getMediaServerId());
        if (mediaServer != null) {
            SpringUtils.publishEvent(MediaPublishEvent.getInstance(this, param, mediaServer));
        }
        return HookResultForOnPublish.SUCCESS();
    }

    /**
     * 播放鉴权
     */
    @PostMapping("/on_play")
    @ResponseBody
    @Operation(summary = "播放鉴权", description = "播放鉴权回调，返回是否允许播放")
    public HookResult onPlay(@RequestBody OnPlayHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_play: param={}", JSON.toJSONString(param));
        return HookResult.SUCCESS();
    }

    /**
     * 流变更（到达/离开）
     */
    @PostMapping("/on_stream_changed")
    @ResponseBody
    @Operation(summary = "流变更", description = "流到达或离开时回调")
    public HookResult onStreamChanged(@RequestBody OnStreamChangedHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_stream_changed: param={}", JSON.toJSONString(param));
        VideoMediaServerResultDTO mediaServer = getMediaServer(param.getMediaServerId());
        if (mediaServer == null) {
            log.warn("ZLM Hook on_stream_changed: 未找到流媒体服务器, mediaServerId={}", param.getMediaServerId());
            return HookResult.SUCCESS();
        }

        if (param.isRegist()) {
            SpringUtils.publishEvent(
                MediaArrivalEvent.getInstance(this, param, mediaServer, param.getMediaServerId()));
        } else {
            SpringUtils.publishEvent(
                MediaDepartureEvent.getInstance(this, param, mediaServer));
        }

        return HookResult.SUCCESS();
    }

    /**
     * 无人观看回调。
     * <p>
     * 用户关闭浏览器 / 网络断开后，ZLM 发现流已无任何观看者（超过配置阈值），
     * 通过本 hook 通知平台决定是否关闭流。
     * <p>
     * 我们的策略：**立即触发完整的 stop 流程**（发 BYE / 释放 SSRC、RTP 端口 /
     * 关 RTP server / 清缓存），并通过 HookResult 让 ZLM 同步关闭其端。
     * <p>
     * 关键反查：PlayService 生成的 streamId 直接取自 SSRC，所以从 SSRC 池
     * 按 streamId 即可查到 usedBy（deviceId:channelId），不需要额外反向索引。
     */
    @PostMapping("/on_stream_none_reader")
    @ResponseBody
    @Operation(summary = "无人观看", description = "流无人观看时回调，触发 stop 释放关联资源")
    public HookResult onStreamNoneReader(@RequestBody OnStreamNoneReaderHookParam param) {
        String mediaServerId = param.getMediaServerId();
        String stream = param.getStream();
        setupTenantContext(mediaServerId);
        log.info("ZLM Hook on_stream_none_reader: param={}", JSON.toJSONString(param));

        // close=true：让 ZLM 自行释放流资源；无论平台侧清理结果如何，都回复关闭
        HookResult result = new HookResult(0, "success");
        result.setClose(true);

        if (StrUtil.isBlank(mediaServerId) || StrUtil.isBlank(stream)) {
            return result;
        }

        try {
            Map<String, String> allocations = ssrcPoolService.getAllocationsByMediaIdentification(mediaServerId);
            String usedBy = allocations.get(stream);
            if (StrUtil.isBlank(usedBy)) {
                log.debug("on_stream_none_reader: 未找到对应 SSRC 分配，可能已清理: stream={}", stream);
                return result;
            }
            int colonIdx = usedBy.indexOf(':');
            if (colonIdx <= 0) {
                log.warn("on_stream_none_reader: usedBy 格式异常: {}", usedBy);
                return result;
            }
            String deviceIdentification = usedBy.substring(0, colonIdx);
            String channelIdentification = usedBy.substring(colonIdx + 1);
            log.info("on_stream_none_reader: 触发停止播放, deviceIdentification={}, channelIdentification={}",
                deviceIdentification, channelIdentification);
            playService.stop(deviceIdentification, channelIdentification);
        } catch (Exception e) {
            log.error("on_stream_none_reader 处理异常: stream={}, error={}", stream, e.getMessage(), e);
        }
        return result;
    }

    /**
     * 流未找到
     */
    @PostMapping("/on_stream_not_found")
    @ResponseBody
    @Operation(summary = "流未找到", description = "请求的流不存在时回调")
    public HookResult onStreamNotFound(@RequestBody OnStreamNotFoundHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_stream_not_found: param={}", JSON.toJSONString(param));
        VideoMediaServerResultDTO mediaServer = getMediaServer(param.getMediaServerId());
        if (mediaServer != null) {
            SpringUtils.publishEvent(MediaNotFoundEvent.getInstance(this, param, mediaServer));
        }
        return HookResult.SUCCESS();
    }

    /**
     * RTP 收流超时
     */
    @PostMapping("/on_rtp_server_timeout")
    @ResponseBody
    @Operation(summary = "RTP超时", description = "RTP服务器收流超时回调")
    public HookResult onRtpServerTimeout(@RequestBody OnRtpServerTimeoutHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_rtp_server_timeout: param={}", JSON.toJSONString(param));
        VideoMediaServerResultDTO mediaServer = getMediaServer(param.getMediaServerId());
        if (mediaServer != null) {
            OnStreamChangedHookParam hookParam = new OnStreamChangedHookParam();
            hookParam.setApp("rtp");
            hookParam.setStream(param.getStream_id());
            hookParam.setMediaServerId(param.getMediaServerId());
            SpringUtils.publishEvent(MediaRtpServerTimeoutEvent.getInstance(this, hookParam, mediaServer));
        }
        return HookResult.SUCCESS();
    }

    /**
     * 录像完成（MP4 文件生成）
     */
    @PostMapping("/on_record_mp4")
    @ResponseBody
    @Operation(summary = "录像完成", description = "MP4录像文件生成完成时回调")
    public HookResult onRecordMp4(@RequestBody OnRecordMp4HookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_record_mp4: param={}", JSON.toJSONString(param));
        VideoMediaServerResultDTO mediaServer = getMediaServer(param.getMediaServerId());
        if (mediaServer != null) {
            SpringUtils.publishEvent(MediaRecordMp4Event.getInstance(this, param, mediaServer));
        }
        return HookResult.SUCCESS();
    }

    /**
     * RTP 推流停止
     */
    @PostMapping("/on_send_rtp_stopped")
    @ResponseBody
    @Operation(summary = "推流停止", description = "RTP推流停止时回调")
    public HookResult onSendRtpStopped(@RequestBody OnSendRtpStoppedHookParam param) {
        setupTenantContext(param.getMediaServerId());
        log.info("ZLM Hook on_send_rtp_stopped: param={}", JSON.toJSONString(param));
        return HookResult.SUCCESS();
    }

    /**
     * 获取流媒体服务器信息
     */
    private VideoMediaServerResultDTO getMediaServer(String mediaServerId) {
        if (StrUtil.isBlank(mediaServerId)) {
            return null;
        }
        try {
            return videoMediaServerService.getVideoMediaServerResultDTO(mediaServerId);
        } catch (Exception e) {
            log.error("查询流媒体服务器失败: {}", mediaServerId, e);
            return null;
        }
    }
}
