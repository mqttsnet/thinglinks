package com.mqttsnet.thinglinks.common.core.utils.tdengine;

public class TdUtils {
    /**
     * 创建子表名
     * @param supTbName 超级表名
     * @param deviceId 设备标识
     */
    public static String getSubTableName(String supTbName, String deviceId) {
        return supTbName + "_" + deviceId;
    }

    /**
     * 创建超级表名
     * @param productType 超级表名
     * @param productIdentification 设备标识
     * @param serviceName 服务名称
     */
    public static String getSuperTableName(String productType, String productIdentification, String serviceName) {
        return productType + "_" + productIdentification + "_" + serviceName;
    }
}
