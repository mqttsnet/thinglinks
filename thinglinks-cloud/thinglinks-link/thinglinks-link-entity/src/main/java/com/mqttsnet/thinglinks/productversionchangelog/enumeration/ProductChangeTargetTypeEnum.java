package com.mqttsnet.thinglinks.productversionchangelog.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产品变更维度枚举,与 product_version_change_log.target_type 字段对应。
 *
 * <p>标识一条变更记录属于产品树的哪一层 —— 命令请求 / 响应参数变更归入 {@link #COMMAND}。</p>
 *
 * @author mqttsnet
 */
@Getter
@AllArgsConstructor
@Schema(title = "ProductChangeTargetTypeEnum", description = "产品变更维度")
public enum ProductChangeTargetTypeEnum {

    /**
     * 产品基础信息。
     */
    PRODUCT_INFO(0, "产品信息"),
    /**
     * 服务。
     */
    SERVICE(1, "服务"),
    /**
     * 属性。
     */
    PROPERTY(2, "属性"),
    /**
     * 命令(含命令请求 / 响应参数)。
     */
    COMMAND(3, "命令"),
    ;

    /**
     * 维度数值。
     */
    private final Integer value;
    /**
     * 维度描述。
     */
    private final String desc;

    /**
     * 根据数值查找对应枚举。
     *
     * @param value 维度数值
     * @return {@link Optional} 包裹的枚举项
     */
    public static Optional<ProductChangeTargetTypeEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
            .flatMap(val -> Stream.of(ProductChangeTargetTypeEnum.values())
                .filter(e -> e.getValue().equals(val))
                .findFirst());
    }
}
