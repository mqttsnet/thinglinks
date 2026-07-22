package com.mqttsnet.thinglinks.video.protocol;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * 设备接入协议工厂。
 *
 * <p>根据设备的 accessProtocol 字段自动选择对应的协议实现，
 * 实现 GB28181 / ISUP / JT1078 协议的完全隔离。
 *
 * <p>Spring 容器启动时自动发现所有 {@link DeviceAccessProtocol} 实现并注册到本工厂。
 * 业务层通过本工厂获取协议实现，无需关心具体类型。
 *
 * <p>使用示例：
 * <pre>{@code
 * // 根据枚举获取
 * DeviceAccessProtocol protocol = factory.getProtocol(AccessProtocolEnum.GB28181);
 *
 * // 根据字符串获取（容错，未知值默认 GB28181）
 * DeviceAccessProtocol protocol = factory.getProtocol("ISUP");
 * }</pre>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see DeviceAccessProtocol
 * @see AccessProtocolEnum
 * @see Gb28181AccessProtocol
 */
@Slf4j
@Component
public class DeviceAccessProtocolFactory {

    /**
     * 协议类型 → 协议实现 的映射表
     */
    private final Map<AccessProtocolEnum, DeviceAccessProtocol> protocolMap;

    /**
     * 构造工厂，由 Spring 自动注入所有 {@link DeviceAccessProtocol} 实现。
     *
     * @param protocols Spring 容器中所有 {@link DeviceAccessProtocol} 实现的列表
     */
    public DeviceAccessProtocolFactory(List<DeviceAccessProtocol> protocols) {
        this.protocolMap = protocols.stream()
                .collect(Collectors.toMap(DeviceAccessProtocol::getProtocolType, Function.identity()));
        log.info("注册设备接入协议 {} 种: {}", protocolMap.size(),
                protocolMap.keySet().stream().map(AccessProtocolEnum::getValue).toList());
    }

    /**
     * 根据协议类型枚举获取对应的协议实现。
     *
     * @param protocolType 协议类型枚举
     * @return 对应的协议实现
     * @throws BizException 当协议类型未注册时抛出
     */
    public DeviceAccessProtocol getProtocol(AccessProtocolEnum protocolType) {
        var protocol = protocolMap.get(protocolType);
        if (protocol == null) {
            throw BizException.wrap("不支持的设备接入协议: " + protocolType.getValue());
        }
        return protocol;
    }

    /**
     * 根据协议类型字符串获取对应的协议实现。
     * 未匹配到时默认返回 {@link AccessProtocolEnum#GB28181} 对应的实现。
     *
     * @param protocolValue 协议标识字符串（如 "GB28181"、"ISUP"、"JT1078"）
     * @return 对应的协议实现
     * @throws BizException 当默认协议也未注册时抛出
     */
    public DeviceAccessProtocol getProtocol(String protocolValue) {
        var protocolEnum = AccessProtocolEnum.fromValue(protocolValue)
                .orElse(AccessProtocolEnum.GB28181);
        return getProtocol(protocolEnum);
    }

    /**
     * 根据协议类型字符串获取对应的协议实现，未匹配时默认返回 GB28181。
     * 语义等同于 {@link #getProtocol(String)}，提供更明确的方法名。
     *
     * @param protocolValue 协议标识字符串
     * @return 对应的协议实现
     */
    public DeviceAccessProtocol getProtocolOrDefault(String protocolValue) {
        return getProtocol(protocolValue);
    }
}
