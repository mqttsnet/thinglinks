package com.mqttsnet.thinglinks.mqs.transform.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 前置转换脚本「在线调试」入参。
 *
 * <p>选一个真实设备 + 一条源原始报文,后端按运行时同一套逻辑组装绑定(device/product)后试跑脚本。
 *
 * @author mqttsnet
 */
@Data
public class TransformDebugParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 脚本内容(Groovy) */
    private String scriptContent;
    /** 调试设备标识 ── 后端据此取真实设备 + 绑定产品缓存组装 device/product */
    private String deviceIdentification;
    /** 源上行 topic(模拟设备实际上报的 topic) */
    private String originTopic;
    /** 源原始报文(模拟设备实际上报的 body) */
    private String originBody;
    /** 扩展参数 JSON ── 注入脚本的 {@code config} 绑定;调试时可填该脚本的 extend_params 一并试跑 */
    private String extendParams;
    /** 脚本唯一键(scriptType:channelCode:productIdentification:topicPattern)── 可选,调试也计入执行统计 */
    private String scriptUniqueKey;
    /** 调试使用的产品版本号 ── 据此解析物模型注入脚本(可调当前 / 下个版本);空则回退设备绑定版本 / 产品生效版本 */
    private String objectVersion;
}
