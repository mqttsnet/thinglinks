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
     * link-> device_record:deviceIdentification
     */
    public static final String DEVICE_RECORD_KEY = "device_record:";

    /**
     * 全局产品信息 前缀
     * link-> def_product:productIdentification
     */
    public static final String PRODUCT = "def_product";

    /**
     * 全局产品模型 前缀
     * link-> def_product_model:productIdentification
     */
    public static final String PRODUCT_MODEL = "def_product_model";

    /**
     * 全局产品模型超级表 前缀
     * link-> def_product_model_super_table:productIdentification:serviceCode:deviceIdentification
     */
    public static final String PRODUCT_MODEL_SUPER_TABLE = "def_product_model_super_table";

    /**
     * TDengine superTableFields cache key
     * link-> tdengine_superTableFields:productIdentification:serviceCode:deviceIdentification
     */
    public static final String TDENGINE_SUPERTABLEFILELDS = "tdengine_superTableFields:";


    /**
     * 设备数据转换脚本 cache key
     * link-> device_data_reported_agreement_script:deviceIdentification
     */
    public static final String DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT = "device_data_reported_agreement_script";

}
