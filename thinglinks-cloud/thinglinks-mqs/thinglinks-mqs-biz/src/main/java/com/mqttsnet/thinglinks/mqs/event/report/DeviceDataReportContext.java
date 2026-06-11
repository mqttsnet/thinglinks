package com.mqttsnet.thinglinks.mqs.event.report;

import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 数据上报后置处理上下文 ── 承载物模型匹配后的结构化结果 + 设备身份,供后置处理器使用。
 *
 * @author mqttsnet
 * @since 2026-06-03
 */
@Getter
@Builder
@ToString
public class DeviceDataReportContext {

    /**
     * 应用 ID(多租户应用维度)。
     */
    private final String appId;
    /**
     * 产品标识。
     */
    private final String productIdentification;
    /**
     * 设备标识。
     */
    private final String deviceIdentification;
    /**
     * 设备绑定的产品发布版本号。
     */
    private final String boundProductVersionNo;
    /**
     * 上报物理时间(epoch ms)。
     */
    private final Long ts;
    /**
     * 物模型匹配后的结构化数据(产品 + 服务 + 属性值,来自 {@code ProductResultVO})。
     */
    private final ProductResultVO normalized;
}
