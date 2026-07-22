package com.mqttsnet.thinglinks.video.dto.device.config;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code video_device.protocol_config} JSON 字段的类型安全视图。
 * <p>
 * 存储 REGISTER / DeviceInfo / Catalog 等协议交互中与设备级相关的协议元数据，
 * 字段按写入来源分区，避免不同 handler 互相覆盖：
 * <ul>
 *   <li>{@link #sipUserAgent} / {@link #sipContact} - 由 REGISTER handler 写入（SIP 层特征）</li>
 *   <li>{@link #catalogSyncTime} / {@link #deviceInfoSyncTime} - 分别由 Catalog / DeviceInfo 同步写入</li>
 * </ul>
 * <p>
 * <b>按需更新</b>：通过 {@code VideoDeviceService.patchProtocolConfig(deviceId, patch)} 写入时，
 * patch 里为 null 的字段保持现有值不动；只有显式 set 的字段才覆盖。合并逻辑见
 * {@link #merge(VideoDeviceProtocolConfig)}。
 *
 * @author mqttsnet
 * @since 2026-04-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "设备协议配置（protocol_config 字段）")
public class VideoDeviceProtocolConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ---------------- REGISTER 阶段写入 ----------------

    /** SIP User-Agent 头（设备厂商/固件特征串，用于 DeviceAdapterFactory 指纹匹配） */
    @Schema(description = "SIP User-Agent 头")
    private String sipUserAgent;

    /** SIP Contact 头原文（设备回调地址，包含传输参数） */
    @Schema(description = "SIP Contact 头原文")
    private String sipContact;

    // ---------------- 同步时间戳（各 handler 各写各的） ----------------

    /**
     * 最后一次 Catalog 全量同步时间。
     * <p><b>类型用 String 而非 LocalDateTime 的原因</b>：fastjson2 对 {@code LocalDateTime} 的
     * 反序列化不一定走 {@code @JSONField(format)} 注解 —— 实测序列化输出 {@code "2026-04-19 20:07:45"}
     * 后，下一次 {@code patchProtocolConfig} 读 DB 反序列化时，LocalDateTime 字段会因解析失败变成
     * null，导致 merge 后时间戳"神秘丢失"。
     * <p>改成 String 后读写对称、不依赖反序列化配置；业务代码用 {@link #nowAsString()} 获得当前
     * 时间，前端拿到的就是 ISO-8601 字符串可以直接 {@code new Date(str)}。
     */
    @Schema(description = "最后一次 Catalog 全量同步时间（ISO-8601 字符串）")
    private String catalogSyncTime;

    /** 最后一次 DeviceInfo 查询应答时间，同 {@link #catalogSyncTime}，用 String 规避反序列化坑。 */
    @Schema(description = "最后一次 DeviceInfo 应答时间（标准时间字符串）")
    private String deviceInfoSyncTime;

    // ---------------- Keepalive 细节（1-3） ----------------

    /**
     * 最近一次 Keepalive 的 {@code <Status>} 字段原值。
     * <p>GB/T 28181-2016 §9.4 定义：{@code OK}（正常）/ {@code ERROR}（内部故障但仍在注册）。
     * 本项目对接非标设备可能还见到 {@code OFF}（下线自报）。
     */
    @Schema(description = "最近一次心跳的 Status")
    private String lastKeepaliveStatus;

    /**
     * 最近一次非 OK 心跳的 {@code <Info>} 子节点 JSON 序列化（常见为 DeviceErrorStatusList）。
     * <p>只在 Status != OK 时记录，正常心跳不存，避免 DB 里充满空 Info。
     */
    @Schema(description = "最近一次非OK心跳的 Info JSON")
    private String lastKeepaliveErrorDetail;

    /** 最近一次非 OK 心跳的时间戳，用于快速定位故障发生时间。 */
    @Schema(description = "最近一次非OK心跳时间")
    private String lastKeepaliveErrorTime;

    // ---------------- RTSP / ONVIF 等主动拉流设备 ----------------

    /**
     * 拉流源配置（RTSP、ONVIF 等 access_protocol 需要平台主动拉流时使用）。
     * <p>GB28181/ISUP/JT1078 等被动接入协议不需要填这块。
     */
    @Schema(description = "拉流源配置（RTSP/ONVIF 等主动拉流设备）")
    private StreamSource streamSource;

    /**
     * 将 patch 中有效的字段合并进当前对象（就地修改）。
     * <p>用于 {@code patchProtocolConfig} 场景：调用方只 set 关心的字段，其他字段保持现状。
     * <p>判空原则：
     * <ul>
     *   <li>String 字段用 {@link StrUtil#isNotBlank}（跳过 null / 空串 / 纯空白），防止被前端表单空值覆盖掉有效数据</li>
     *   <li>非 String 字段用 {@code != null}</li>
     * </ul>
     * 新增字段时在本方法内补一行同类判断即可。
     */
    public void merge(VideoDeviceProtocolConfig patch) {
        if (patch == null) {
            return;
        }
        if (StrUtil.isNotBlank(patch.sipUserAgent)) {
            this.sipUserAgent = patch.sipUserAgent;
        }
        if (StrUtil.isNotBlank(patch.sipContact)) {
            this.sipContact = patch.sipContact;
        }
        if (StrUtil.isNotBlank(patch.catalogSyncTime)) {
            this.catalogSyncTime = patch.catalogSyncTime;
        }
        if (StrUtil.isNotBlank(patch.deviceInfoSyncTime)) {
            this.deviceInfoSyncTime = patch.deviceInfoSyncTime;
        }
        if (StrUtil.isNotBlank(patch.lastKeepaliveStatus)) {
            this.lastKeepaliveStatus = patch.lastKeepaliveStatus;
        }
        if (StrUtil.isNotBlank(patch.lastKeepaliveErrorDetail)) {
            this.lastKeepaliveErrorDetail = patch.lastKeepaliveErrorDetail;
        }
        if (StrUtil.isNotBlank(patch.lastKeepaliveErrorTime)) {
            this.lastKeepaliveErrorTime = patch.lastKeepaliveErrorTime;
        }
        if (patch.streamSource != null) {
            if (this.streamSource == null) {
                this.streamSource = new StreamSource();
            }
            this.streamSource.merge(patch.streamSource);
        }
    }

    /**
     * 拉流源配置子结构。
     * <p>适用于 RTSP、ONVIF、RTMP 等"平台向设备主动拉流"的场景。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "拉流源配置")
    public static class StreamSource implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 完整拉流 URL（优先级最高）。
         * <p>若填了此字段，平台直接把它丢给 ZLM addStreamProxy，忽略下面的拼装字段。
         * <p>例：{@code rtsp://admin:pass@192.168.1.108:554/h264/ch1/main/av_stream}
         */
        @Schema(description = "完整拉流 URL（填了优先用，不用 host/port 拼）")
        private String url;

        /** 认证用户名（与 {@link #url} 二选一；或与 host/port/streamPath 搭配使用） */
        @Schema(description = "认证用户名")
        private String username;

        /**
         * RTSP / 其他协议的流路径，如 {@code /h264/ch1/main/av_stream}。
         * <p>仅在没填 {@link #url} 时由后端用 {@code protocol://user:pass@host:port/streamPath} 拼出完整 URL。
         */
        @Schema(description = "流路径，如 /h264/ch1/main/av_stream")
        private String streamPath;

        /**
         * ONVIF 设备的 Media Profile Token。
         * <p>一台 ONVIF 摄像头通常有多路（主码流/子码流），各自对应一个 profileToken。
         * 设备发现后由后端自动写入，RTSP 拉流 URL 由 {@code GetStreamUri(profileToken)} 获取。
         */
        @Schema(description = "ONVIF Media Profile Token")
        private String onvifProfileToken;

        /**
         * 传输模式（TCP / UDP），传给 ZLM addStreamProxy 的 rtp_type。
         * <p>null 时 ZLM 默认 TCP；公网拉流建议 TCP，局域网可用 UDP。
         */
        @Schema(description = "RTP 传输模式（TCP/UDP）")
        private String rtpType;

        public void merge(StreamSource patch) {
            if (patch == null) {
                return;
            }
            if (StrUtil.isNotBlank(patch.url)) {
                this.url = patch.url;
            }
            if (StrUtil.isNotBlank(patch.username)) {
                this.username = patch.username;
            }
            if (StrUtil.isNotBlank(patch.streamPath)) {
                this.streamPath = patch.streamPath;
            }
            if (StrUtil.isNotBlank(patch.onvifProfileToken)) {
                this.onvifProfileToken = patch.onvifProfileToken;
            }
            if (StrUtil.isNotBlank(patch.rtpType)) {
                this.rtpType = patch.rtpType;
            }
        }
    }
}
