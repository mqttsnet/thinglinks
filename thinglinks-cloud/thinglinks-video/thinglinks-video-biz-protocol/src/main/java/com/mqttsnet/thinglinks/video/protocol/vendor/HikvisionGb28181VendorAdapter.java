package com.mqttsnet.thinglinks.video.protocol.vendor;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.video.protocol.VendorProtocolAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 海康威视 GB28181 厂商适配器。
 *
 * <p>处理海康设备在 GB28181 协议实现中的特殊行为：</p>
 * <ul>
 *     <li>部分型号的 SDP 应答使用私有编码格式</li>
 *     <li>目录查询 XML 中可能携带私有扩展字段</li>
 *     <li>注册消息可能包含额外的设备能力描述</li>
 * </ul>
 *
 * <p>仅影响海康设备，不影响其他厂商的协议处理。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class HikvisionGb28181VendorAdapter implements VendorProtocolAdapter {

    @Override
    public String getManufacturer() {
        return "hikvision";
    }

    @Override
    public String getProtocolVersion() {
        return BizConstant.ALL; // 适用于海康所有版本
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean requiresCustomRegisterHandling() {
        // 海康部分设备注册时带有私有字段，需要特殊处理
        return true;
    }

    @Override
    public String adaptCatalogXml(String xmlContent) {
        // 海康部分设备的目录 XML 使用 GBK 编码声明但实际为 UTF-8
        if (xmlContent != null && xmlContent.contains("encoding=\"GB2312\"")) {
            log.debug("[海康适配器] 修正XML编码声明: GB2312 -> UTF-8");
            return xmlContent.replace("encoding=\"GB2312\"", "encoding=\"UTF-8\"");
        }
        return xmlContent;
    }
}
