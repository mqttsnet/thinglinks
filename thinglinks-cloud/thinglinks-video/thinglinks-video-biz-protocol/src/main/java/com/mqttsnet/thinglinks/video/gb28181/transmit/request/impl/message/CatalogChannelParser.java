package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelConfig;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelExtendParams;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * GB/T 28181 Catalog Item XML 通道解析器。
 * <p>
 * 统一 {@code CatalogNotifyMessageHandler} 与 {@code CatalogResponseMessageHandler}
 * 对 {@code <Item>} 节点的字段映射逻辑，保证两处解析对称、覆盖 GB 标准字段。
 * <p>
 * 字段归属约定：
 * <ul>
 *   <li>核心列（name/manufacturer/model/host/port/longitude/latitude 等） → VideoChannel 独立列</li>
 *   <li>{@code <Info>} 子元素（流/编解码能力） → {@link VideoChannelConfig}（channel_config）</li>
 *   <li>其他 GB 扩展字段（Owner/Parental/SafetyWay/PositionType 等） → {@link VideoChannelExtendParams}（extend_params JSON 字符串）</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-17
 */
@Slf4j
public final class CatalogChannelParser {

    private CatalogChannelParser() {
    }

    /**
     * 从 GB28181 Catalog Item XML 构建 {@link VideoChannel}。
     * 单字段解析失败不中断，会吞异常并继续其它字段。
     *
     * @param deviceIdentification 上级设备国标编号
     * @param itemElement          Catalog 中的 Item 元素
     * @return 已填充字段的 VideoChannel 实体（未入库）
     */
    public static VideoChannel parseItem(String deviceIdentification, Element itemElement) {
        var channel = new VideoChannel();
        channel.setDeviceIdentification(deviceIdentification);

        var channelIdentification = XmlUtil.getText(itemElement, "DeviceID");
        channel.setChannelIdentification(channelIdentification);

        channel.setChannelName(XmlUtil.getText(itemElement, "Name"));
        channel.setManufacturer(XmlUtil.getText(itemElement, "Manufacturer"));
        channel.setModel(XmlUtil.getText(itemElement, "Model"));
        channel.setOnlineStatus("ON".equalsIgnoreCase(XmlUtil.getText(itemElement, "Status")));

        tryParseDecimal(XmlUtil.getText(itemElement, "Longitude")).ifPresent(channel::setLongitude);
        tryParseDecimal(XmlUtil.getText(itemElement, "Latitude")).ifPresent(channel::setLatitude);

        channel.setFullAddress(XmlUtil.getText(itemElement, "Address"));

        var civilCode = XmlUtil.getText(itemElement, "CivilCode");
        channel.setRegionCode(civilCode);
        fillAdministrativeCodes(channel, civilCode);

        channel.setHost(XmlUtil.getText(itemElement, "IPAddress"));
        tryParseInt(XmlUtil.getText(itemElement, "Port")).ifPresent(channel::setPort);
        channel.setPassword(XmlUtil.getText(itemElement, "Password"));

        resolvePtzType(itemElement).ifPresent(pt -> {
            channel.setPtzType(pt);
            channel.setPtzCapability(pt > 0);
        });

        tryParseInt(XmlUtil.getText(itemElement, "Secrecy")).ifPresent(channel::setSecrecy);

        deriveFromDeviceId(channelIdentification, channel::setChannelType, channel::setChannelNo);

        buildChannelConfig(itemElement).ifPresent(channel::setChannelConfig);
        buildExtendParams(itemElement).ifPresent(channel::setExtendParams);
        return channel;
    }

    /** `<PTZType>` 可能出现在 Item 直接子元素或 `<Info>` 内部 */
    private static Optional<Integer> resolvePtzType(Element itemElement) {
        return Optional.ofNullable(XmlUtil.getText(itemElement, "PTZType"))
                .filter(StrUtil::isNotBlank)
                .or(() -> Optional.ofNullable(itemElement.element("Info"))
                        .map(info -> XmlUtil.getText(info, "PTZType")))
                .flatMap(CatalogChannelParser::tryParseInt);
    }

    /** 解析经纬度：非空且非 0 才返回（0,0 是无效坐标，GB 习惯把"未知"上报为 0） */
    private static Optional<BigDecimal> tryParseDecimal(String text) {
        if (StrUtil.isBlank(text) || !NumberUtil.isNumber(text.trim())) {
            return Optional.empty();
        }
        BigDecimal value = new BigDecimal(text.trim());
        return value.compareTo(BigDecimal.ZERO) == 0 ? Optional.empty() : Optional.of(value);
    }

    /** 解析整型（GB28181 各种类型码）：非数字返回空 Optional */
    private static Optional<Integer> tryParseInt(String text) {
        return Optional.ofNullable(NumberUtil.parseInt(text, null));
    }

    /** 从 CivilCode 派生 provinceCode（前 2 位）、cityCode（前 4 位），补 0 凑 6 位。 */
    private static void fillAdministrativeCodes(VideoChannel channel, String civilCode) {
        if (StrUtil.isBlank(civilCode)) {
            return;
        }
        if (civilCode.length() >= 2) {
            channel.setProvinceCode(civilCode.substring(0, 2) + "0000");
        }
        if (civilCode.length() >= 4) {
            channel.setCityCode(civilCode.substring(0, 4) + "00");
        }
    }

    /**
     * 从 DeviceID 派生 channelType（11-13 位）与 channelNo（14-20 位）。
     * 参见 GB/T 28181-2016 Section 7.3.3 统一编码规则。
     */
    private static void deriveFromDeviceId(String deviceId, Consumer<Integer> typeSetter, Consumer<Integer> noSetter) {
        if (StrUtil.isBlank(deviceId)) {
            return;
        }
        Optional.of(deviceId)
                .filter(id -> id.length() >= 13)
                .map(id -> id.substring(10, 13))
                .flatMap(CatalogChannelParser::tryParseInt)
                .ifPresent(typeSetter);
        Optional.of(deviceId)
                .filter(id -> id.length() >= 20)
                .map(id -> id.substring(13, 20))
                .flatMap(CatalogChannelParser::tryParseInt)
                .ifPresent(noSetter);
    }

    /**
     * 构建 {@link VideoChannelConfig}：{@code <Info>} 子元素里的流/编解码能力字段。
     * <p>前端可直接读取 {@code channel_config.info.*}；业务自定义字段加在 VideoChannelConfig 顶层
     * 其他字段里，{@code syncChannelsFromCatalog} 的 merge 策略只覆盖 {@code info} 节点。
     */
    private static Optional<VideoChannelConfig> buildChannelConfig(Element itemElement) {
        var infoElement = itemElement.element("Info");
        if (infoElement == null) {
            return Optional.empty();
        }
        var info = VideoChannelConfig.Info.builder()
                .ptzType(nullIfBlank(XmlUtil.getText(infoElement, "PTZType")))
                .resolution(nullIfBlank(XmlUtil.getText(infoElement, "Resolution")))
                .downloadSpeed(nullIfBlank(XmlUtil.getText(infoElement, "DownloadSpeed")))
                .svcSpaceSupportMode(nullIfBlank(XmlUtil.getText(infoElement, "SVCSpaceSupportMode")))
                .svcTimeSupportMode(nullIfBlank(XmlUtil.getText(infoElement, "SVCTimeSupportMode")))
                .build();
        if (isAllBlank(info)) {
            return Optional.empty();
        }
        return Optional.of(VideoChannelConfig.builder().info(info).build());
    }

    /**
     * 构建 {@link VideoChannelExtendParams} 并序列化为 JSON 字符串（协议兜底）。
     * <p>收录 GB28181 Catalog Item 里未映射到实体列、也不在 {@code <Info>} 里的字段。
     * <p>{@code Info} 子元素已独立走 {@link #buildChannelConfig}，不再重复。
     */
    private static Optional<String> buildExtendParams(Element itemElement) {
        var params = VideoChannelExtendParams.builder()
                .owner(nullIfBlank(XmlUtil.getText(itemElement, "Owner")))
                .parental(nullIfBlank(XmlUtil.getText(itemElement, "Parental")))
                .parentId(nullIfBlank(XmlUtil.getText(itemElement, "ParentID")))
                .safetyWay(nullIfBlank(XmlUtil.getText(itemElement, "SafetyWay")))
                .registerWay(nullIfBlank(XmlUtil.getText(itemElement, "RegisterWay")))
                .certNum(nullIfBlank(XmlUtil.getText(itemElement, "CertNum")))
                .certifiable(nullIfBlank(XmlUtil.getText(itemElement, "Certifiable")))
                .errCode(nullIfBlank(XmlUtil.getText(itemElement, "ErrCode")))
                .endTime(nullIfBlank(XmlUtil.getText(itemElement, "EndTime")))
                .block(nullIfBlank(XmlUtil.getText(itemElement, "Block")))
                .supplyLightType(nullIfBlank(XmlUtil.getText(itemElement, "SupplyLightType")))
                .directionType(nullIfBlank(XmlUtil.getText(itemElement, "DirectionType")))
                .businessGroupId(nullIfBlank(XmlUtil.getText(itemElement, "BusinessGroupID")))
                // 1-2 新增：Catalog Item 里之前漏采的 GB 字段
                .positionType(nullIfBlank(XmlUtil.getText(itemElement, "PositionType")))
                .roomType(nullIfBlank(XmlUtil.getText(itemElement, "RoomType")))
                .useType(nullIfBlank(XmlUtil.getText(itemElement, "UseType")))
                .build();
        return Optional.ofNullable(params.toJsonString());
    }

    private static String nullIfBlank(String text) {
        return StrUtil.isBlank(text) ? null : text;
    }

    private static boolean isAllBlank(VideoChannelConfig.Info info) {
        return StrUtil.isAllBlank(info.getPtzType(), info.getResolution(), info.getDownloadSpeed(),
                info.getSvcSpaceSupportMode(), info.getSvcTimeSupportMode());
    }
}
