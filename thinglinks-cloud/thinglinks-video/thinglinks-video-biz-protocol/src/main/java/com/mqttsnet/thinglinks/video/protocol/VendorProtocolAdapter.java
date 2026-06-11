package com.mqttsnet.thinglinks.video.protocol;

/**
 * Description:
 * 厂商协议适配器接口。
 *
 * <p>同一协议（如 GB28181）不同厂商的实现可能存在差异：
 * <ul>
 *     <li>同一协议不同版本（2016 vs 2022）的 SDP 编码、XML 字段差异</li>
 *     <li>同一协议不同厂商（海康 vs 大华 vs 宇视）的私有扩展</li>
 *     <li>部分厂商协议实现不规范，需要做特殊兼容</li>
 * </ul>
 *
 * <p>设计模式：策略 + 责任链。</p>
 * <ul>
 *     <li>每个厂商/版本组合注册为一个 {@code VendorProtocolAdapter}</li>
 *     <li>{@link VendorProtocolAdapterFactory} 根据设备的厂商+版本匹配最佳适配器</li>
 *     <li>找不到精确匹配时回退到默认适配器，保证兼容性</li>
 *     <li>修改某一厂商的适配器不影响其他厂商</li>
 * </ul>
 *
 * <p>扩展指南：新增厂商适配器只需：</p>
 * <ol>
 *     <li>实现此接口</li>
 *     <li>标注 {@code @Component}</li>
 *     <li>在 {@link #getManufacturer()} 和 {@link #getProtocolVersion()} 中返回匹配条件</li>
 * </ol>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VendorProtocolAdapter {

    /**
     * 适配器适用的厂商标识。
     * 返回 {@code BizConstant.ALL} 表示通配所有厂商（作为默认回退）。
     *
     * @return 厂商名称（如 "hikvision"、"dahua"、"uniview"、BizConstant.ALL）
     */
    String getManufacturer();

    /**
     * 适配器适用的协议版本。
     * 返回 {@code BizConstant.ALL} 表示通配所有版本。
     *
     * @return 协议版本（如 "2016"、"2022"、BizConstant.ALL）
     */
    String getProtocolVersion();

    /**
     * 优先级（数值越小优先级越高）。
     * 当多个适配器匹配同一厂商+版本时，取优先级最高的。
     *
     * @return 优先级值，默认 100
     */
    default int getPriority() {
        return 100;
    }

    // ========== SDP 相关适配 ==========

    /**
     * 构建 INVITE SDP 内容。
     * 不同厂商/版本的 SDP 格式可能有差异。
     *
     * @param context SDP 构建上下文
     * @return 适配后的 SDP 字符串
     */
    default String buildInviteSdp(SdpBuildContext context) {
        return context.getDefaultSdp();
    }

    /**
     * 解析设备返回的 SDP 应答。
     * 部分厂商的 SDP 应答格式不标准，需要特殊解析。
     *
     * @param sdpContent SDP 原始内容
     * @return 解析后的标准化 SDP 信息
     */
    default SdpParseResult parseSdpResponse(String sdpContent) {
        return SdpParseResult.defaultParse(sdpContent);
    }

    // ========== XML 消息适配 ==========

    /**
     * 适配目录查询结果 XML。
     * 部分厂商的 Catalog XML 格式或编码与标准不一致。
     *
     * @param xmlContent 原始 XML
     * @return 标准化后的 XML
     */
    default String adaptCatalogXml(String xmlContent) {
        return xmlContent;
    }

    /**
     * 适配告警通知 XML。
     *
     * @param xmlContent 原始 XML
     * @return 标准化后的 XML
     */
    default String adaptAlarmXml(String xmlContent) {
        return xmlContent;
    }

    /**
     * 是否需要对 REGISTER 做特殊处理。
     * 部分厂商设备注册时携带非标字段。
     *
     * @return true 需要特殊处理
     */
    default boolean requiresCustomRegisterHandling() {
        return false;
    }

    /**
     * SDP 构建上下文
     */
    record SdpBuildContext(
            String deviceIdentification,
            String channelIdentification,
            String sdpIp,
            int port,
            String ssrc,
            String sessionName,
            String defaultSdp
    ) {
        public String getDefaultSdp() {
            return defaultSdp;
        }
    }

    /**
     * SDP 解析结果
     */
    record SdpParseResult(
            String ip,
            int port,
            String ssrc,
            String codec,
            boolean valid
    ) {
        public static SdpParseResult defaultParse(String sdpContent) {
            // 默认解析逻辑由调用方提供，此处返回占位
            return new SdpParseResult("", 0, "", "", false);
        }
    }
}
