package com.mqttsnet.thinglinks.service;

import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;

/**
 * Description:
 * 设备数据处理服务接口
 * 负责处理设备数据上报的核心业务逻辑，包括数据验证、表结构初始化、数据存储等
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/10/27
 */
public interface DeviceDataProcessingService {

    /**
     * 处理设备数据上报消息
     * <p>
     * 该方法负责：
     * 1. 验证设备和产品信息
     * 2. 初始化表结构
     * 3. 处理服务数据并存储到时序数据库
     * 4. 构建数据收集池缓存
     *
     * @param dataReportParam 设备数据上报参数，包含设备列表和服务数据
     * @throws IllegalArgumentException 当输入参数无效时抛出
     * @throws RuntimeException         当数据处理过程中发生不可恢复错误时抛出
     */
    void processDeviceDataReport(TopoDeviceDataReportParam dataReportParam);
}
