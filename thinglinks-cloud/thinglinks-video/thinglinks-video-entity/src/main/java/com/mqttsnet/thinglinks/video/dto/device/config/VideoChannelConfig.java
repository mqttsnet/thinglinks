package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code video_channel.channel_config} JSON 字段的类型安全视图。
 * <p>
 * 用于替代裸 {@link com.alibaba.fastjson2.JSONObject}，让 catalog 解析、业务读写、
 * 前端渲染三方通过同一套字段语义交互。{@code info} 节点由 GB28181 Catalog 解析器
 * 每次同步覆盖；业务自定义配置可以加到本类其他字段（或为其添加新的子节点类）。
 * <p>
 * <b>序列化说明</b>：MyBatis-Plus {@code FastjsonTypeHandler} 负责落库/读取，无需手动
 * 转换。若在非实体场景（如日志 / DTO 拼装）里需要裸字符串，直接用 FastJSON2 的
 * {@code JSON.toJSONString(obj)} 与 {@code JSON.parseObject(json, VideoChannelConfig.class)}。
 * <p>
 * <b>新增字段须知</b>：
 * <ul>
 *   <li>纯 GB28181 标准字段 → 加到 {@link Info} 并带 {@code @JSONField(name = "PascalCase")}</li>
 *   <li>平台业务自定义字段 → 直接加到本类，注释清楚用途</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "通道配置（channel_config 字段）")
public class VideoChannelConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** GB28181 Catalog {@code <Info>} 子元素字段（由 catalog 同步全权管理） */
    @Schema(description = "GB28181 Info 子元素字段（流/编解码能力）")
    private Info info;

    /**
     * GB/T 28181-2016 Catalog Item 中 {@code <Info>} 子元素的结构化表示。
     * <p>
     * 本类字段与 GB 标准 1:1 映射，字段名用驼峰，序列化时通过 {@code @JSONField}
     * 映射到 Pascal Case（如 {@code ptzType} ↔ {@code PTZType}）。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "GB28181 Info 节点")
    public static class Info implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** PTZ 类型：0-未知 / 1-球机 / 2-半球 / 3-固定枪机 / 4-遥控枪机 */
        @Schema(description = "PTZ类型（0未知/1球机/2半球/3固定枪机/4遥控枪机）")
        private String ptzType;

        /** 视频分辨率，形如 {@code "1920x1080"}，多分辨率用 {@code /} 分隔 */
        @Schema(description = "视频分辨率，如 1920x1080；多种分辨率用 / 分隔")
        private String resolution;

        /** 支持的下载速度倍率，逗号分隔，如 {@code "0.5,1,2,4"}（GB/T 28181-2016 §9.1.1） */
        @Schema(description = "下载倍速（逗号分隔），如 0.5,1,2,4")
        private String downloadSpeed;

        /** SVC 空间可伸缩编码模式（0-不支持，1-1/4，2-1/2，3-3/4） */
        @Schema(description = "SVC空间可伸缩模式")
        private String svcSpaceSupportMode;

        /** SVC 时域可伸缩编码模式（0-不支持，1-1/2，2-1/4，3-1/8） */
        @Schema(description = "SVC时域可伸缩模式")
        private String svcTimeSupportMode;
    }
}
