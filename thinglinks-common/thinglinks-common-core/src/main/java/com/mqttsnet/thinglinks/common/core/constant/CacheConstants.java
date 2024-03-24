package com.mqttsnet.thinglinks.common.core.constant;

/**
 * redis缓存的key 常量
 *
 * @author thinglinks
 */
public class CacheConstants {
    /**
     * 权限缓存前缀
     */
    public final static String LOGIN_TOKEN_KEY = "login_tokens:";


    /**
     * 设备信息 cache key
     * link-> def_device:deviceIdentification
     */
    public static final String DEF_DEVICE = "def_device:";

    /**
     * 全局产品信息 前缀
     * link-> def_product:productIdentification
     */
    public static final String DEF_PRODUCT = "def_product:";

    /**
     * 全局产品模型 前缀
     * link-> def_product_model:productIdentification
     */
    public static final String DEF_PRODUCT_MODEL = "def_product_model:";

    /**
     * 全局产品模型超级表 前缀
     * link-> def_product_model_super_table:productIdentification:serviceCode:deviceIdentification
     */
    public static final String DEF_PRODUCT_MODEL_SUPER_TABLE = "def_product_model_super_table:";

    /**
     * TDengine superTableFields cache key
     * link-> def_tdengine_superTableFields:productIdentification:serviceCode:deviceIdentification
     */
    public static final String DEF_TDENGINE_SUPERTABLEFILELDS = "def_tdengine_superTableFields:";


    /**
     * 设备数据转换脚本 cache key
     * link-> DEF_DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT:deviceIdentification
     */
    public static final String DEF_DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT = "def_DEF_DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT:";

}
