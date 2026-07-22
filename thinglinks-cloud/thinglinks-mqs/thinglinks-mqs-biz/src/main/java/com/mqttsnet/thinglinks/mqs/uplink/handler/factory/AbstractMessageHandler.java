package com.mqttsnet.thinglinks.mqs.uplink.handler.factory;

import java.util.List;
import java.util.Optional;

import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.thinglinks.broker.DeviceDownlinkFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.entity.uplink.source.UplinkMessageEventSource;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: thinglinks-cloud
 * @description: 通用逻辑处理器
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-30 15:07
 **/
@Slf4j
public abstract class AbstractMessageHandler {

    protected final LinkCacheDataHelper linkCacheDataHelper;
    protected final DeviceOpenInnerFacade deviceOpenInnerApi;
    protected final ProtocolMessageAdapter protocolMessageAdapter;

    public AbstractMessageHandler(LinkCacheDataHelper linkCacheDataHelper,
                                  DeviceOpenInnerFacade deviceOpenInnerApi,
                                  ProtocolMessageAdapter protocolMessageAdapter) {
        this.linkCacheDataHelper = linkCacheDataHelper;
        this.deviceOpenInnerApi = deviceOpenInnerApi;
        this.protocolMessageAdapter = protocolMessageAdapter;
    }

    /**
     * 设备下行派发 Facade ── 字段注入(基类按协议分流下行;字段注入避免改动所有子类构造器)。
     */
    @Autowired
    protected DeviceDownlinkFacade deviceDownlinkFacade;

    /**
     * 取设备缓存 VO,内部对 helper 返回的 Optional 解包;缓存为空返 null,subclass 需做 null 防御。
     *
     * @param deviceIdentification 设备标识
     * @return 设备缓存 VO;缓存为空返 null
     */
    protected DeviceCacheVO getDeviceCacheVO(String deviceIdentification) {
        return linkCacheDataHelper.getDeviceCacheVO(deviceIdentification).orElse(null);
    }

    /**
     * 取目标设备缓存 ── 优先用上行 bus 透传到 {@link UplinkMessageEventSource} 的缓存(免重取);
     * 透传缺失或与目标 deviceId 不一致(子设备 / 网关)时回退按 deviceId 查缓存。
     *
     * @param eventSource 消息事件源(可能带 bus 透传的设备缓存)
     * @param deviceId    目标设备标识
     * @return 设备缓存 VO;查不到返 null
     */
    protected DeviceCacheVO resolveDeviceCache(UplinkMessageEventSource eventSource, String deviceId) {
        return Optional.ofNullable(eventSource)
            .map(UplinkMessageEventSource::getDeviceCacheVO)
            .filter(cached -> deviceId != null && deviceId.equals(cached.getDeviceIdentification()))
            .orElseGet(() -> getDeviceCacheVO(deviceId));
    }

    /**
     * 按设备绑定的产品版本号解析物模型(灰度感知)── mqs 子类拿物模型的唯一入口。
     * 调用方约束:必须先取 {@link DeviceCacheVO#getBoundProductVersionNo()};该值为空(老数据)时自行 fallback 到
     * productCacheVO.getActiveVersionNo() 再传入。不要传 null,空值会返 {@link Optional#empty()}。
     *
     * @param productIdentification 产品标识
     * @param boundProductVersionNo 设备绑定的产品版本号
     * @return 物模型缓存 VO;空值或查不到返 {@link Optional#empty()}
     */
    protected Optional<ProductModelCacheVO> resolveProductModelByVersionNo(String productIdentification,
                                                                           String boundProductVersionNo) {
        return linkCacheDataHelper.resolveProductModelByVersionNo(productIdentification, boundProductVersionNo);
    }

    /**
     * 取设备子表 TD 结构缓存 ── 按 (pi, versionNo, serviceCode, deviceIdentification) 维度。
     * 调用方必须传 device.boundProductVersionNo(灰度路由依据)。
     *
     * @param productIdentification 产品标识
     * @param versionNo             产品版本号
     * @param serviceCode           服务编码
     * @param deviceIdentification  设备标识
     * @return 子表 TD 结构描述列表
     */
    protected List<SuperTableDescribeVO> getProductModelSuperTableCacheVO(String productIdentification, String versionNo,
                                                                          String serviceCode, String deviceIdentification) {
        return linkCacheDataHelper.getProductModelSuperTableCacheVO(productIdentification, versionNo, serviceCode, deviceIdentification);
    }

    /**
     * 设置设备子表 TD 结构缓存(对应 getProductModelSuperTableCacheVO)。
     *
     * @param productIdentification 产品标识
     * @param versionNo             产品版本号
     * @param serviceCode           服务编码
     * @param deviceIdentification  设备标识
     * @param superTableDescribeOpt 子表 TD 结构描述列表
     */
    protected void setProductModelSuperTableCacheVO(String productIdentification, String versionNo,
                                                    String serviceCode, String deviceIdentification,
                                                    List<SuperTableDescribeVO> superTableDescribeOpt) {
        linkCacheDataHelper.setProductModelSuperTableCacheVO(productIdentification, versionNo, serviceCode, deviceIdentification, superTableDescribeOpt);
    }

    /**
     * 下行响应 ── 按设备产品的协议类型分流(MQTT / WebSocket / …),不再写死 MQTT。
     * 设备由上游(handler 已解析持有的 {@link DeviceCacheVO})直接传入,无需再从 topic 解析;协议解析不出由派发器兜底 MQTT。
     *
     * @param topic    下行 topic
     * @param qos      QoS(MQTT 用)
     * @param message  消息负载
     * @param tenantId 租户 ID
     * @param device   目标设备缓存(上游已解析);提供 clientId + 产品维度协议解析依据
     */
    protected void sendMessage(String topic, String qos, String message, String tenantId, DeviceCacheVO device) {
        String clientId = Optional.ofNullable(device).map(DeviceCacheVO::getClientId).orElse(null);
        String protocolType = Optional.ofNullable(device)
            .flatMap(d -> linkCacheDataHelper.resolveProtocolType(d.getProductIdentification(), d.getBoundProductVersionNo()))
            .orElse(null);
        deviceDownlinkFacade.dispatch(DownlinkCommand.builder()
            .protocolType(protocolType)
            .tenantId(tenantId)
            .clientId(clientId)
            .deviceIdentification(Optional.ofNullable(device).map(DeviceCacheVO::getDeviceIdentification).orElse(null))
            .topic(topic)
            .qos(qos)
            .payload(message)
            .forceBase64Decode(Boolean.FALSE)
            .clientType("web")
            .expirySeconds("3600")
            .build());
    }

    /**
     * 生成完整的响应主题字符串。
     *
     * @param version       协议版本
     * @param deviceId      设备 ID
     * @param responseTopic 响应主题后缀
     * @return 完整的响应主题字符串
     */
    protected String generateResponseTopic(String version, String deviceId, String responseTopic) {
        return String.format("/%s/devices/%s%s", version, deviceId, responseTopic);
    }

    protected abstract String processingTopicMessage(Object messageParam) throws Exception;
}
