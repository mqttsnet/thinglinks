package com.mqttsnet.thinglinks.productversion.util;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.tds.utils.TdsUtils;

/**
 * 产品物模型版本化 TD 表命名工具。带版本拼 productType_productIdentification_versionNo_serviceCode;无版本
 * (向后兼容旧设备)落到公共 {@link TdsUtils#superTableName(String, String, String)}。versionNo 来源可能是
 * product.activeVersionNo 或 device.boundProductVersionNo,故参数名通用。
 *
 * @author mqttsnet
 */
public final class ProductTdsNamer {

    private ProductTdsNamer() {
    }

    /**
     * 拼 super table 名。versionNo 为空时走老命名(无版本)。
     *
     * @param productType           产品类型
     * @param productIdentification 产品标识
     * @param versionNo             版本号,为空时走无版本老命名
     * @param serviceCode           服务码
     * @return super table 表名
     */
    public static String superTableName(String productType,
                                        String productIdentification,
                                        String versionNo,
                                        String serviceCode) {
        if (StrUtil.isBlank(versionNo)) {
            return TdsUtils.superTableName(productType, productIdentification, serviceCode);
        }
        return productType + ContextConstants.UNDERLINE + productIdentification + ContextConstants.UNDERLINE + versionNo + ContextConstants.UNDERLINE + serviceCode;
    }
}
