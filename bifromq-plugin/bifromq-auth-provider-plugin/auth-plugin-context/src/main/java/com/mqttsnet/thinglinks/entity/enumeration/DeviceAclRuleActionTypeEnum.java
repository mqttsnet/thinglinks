package com.mqttsnet.thinglinks.entity.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 设备Acl动作状态 枚举
 * </p>
 *
 * @author mqttsnet
 * @date 2025-06-04
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum DeviceAclRuleActionTypeEnum {

    /**
     * 全部
     */
    ALL(0, "全部"),

    /**
     * 发布操作
     */
    PUBLISH(1, "发布"),

    /**
     * 订阅操作
     */
    SUBSCRIBE(2, "订阅"),

    /**
     * 取消订阅操作
     */
    UNSUBSCRIBE(3, "取消订阅");;

    private Integer value;
    private String desc;


    /**
     * 根据key获取对应的枚举
     *
     * @param value 状态值
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<DeviceAclRuleActionTypeEnum> fromValue(Integer value) {
        return Stream.of(DeviceAclRuleActionTypeEnum.values())
            .filter(status -> status.getValue().equals(value))
            .findFirst();
    }


    public static Optional<DeviceAclRuleActionTypeEnum> fromClientType(ClientAclActionTypeEnum clientType) {
        if (clientType == null) return Optional.empty();

        switch (clientType) {
            case PUBLISH:
                return Optional.of(PUBLISH);
            case SUBSCRIBE:
                return Optional.of(SUBSCRIBE);
            case UNSUBSCRIBE:
                return Optional.of(UNSUBSCRIBE);
            default:
                return Optional.empty();
        }
    }


}
