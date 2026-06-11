package com.mqttsnet.thinglinks.product.constant;

/**
 * 物模型编码(服务 / 属性 / 命令 / 参数)命名规范 —— 平台统一标准。
 * <p>
 * 编码将作为底层时序库的表名 / 列名等数据标识,统一采用小写字母开头的 snake_case:
 * 仅允许小写字母、数字、下划线,长度 2-50。系统仅在入口做校验拦截、不做任何大小写转换,
 * 以保证存储值与用户输入原值完全一致。
 *
 * @author mqttsnet
 */
public interface ThingModelCodeRule {

    /**
     * 编码格式:小写字母开头,仅含小写字母、数字、下划线,长度 2-50。
     */
    String PATTERN = "^[a-z][a-z0-9_]{1,49}$";

    /**
     * 编码格式校验失败提示。
     */
    String PATTERN_MSG = "编码须以小写字母开头,仅含小写字母、数字、下划线,长度2-50(示例:device_id)";
}
