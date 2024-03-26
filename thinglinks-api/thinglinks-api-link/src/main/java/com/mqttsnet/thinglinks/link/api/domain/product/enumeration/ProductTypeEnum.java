package com.mqttsnet.thinglinks.link.api.domain.product.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 产品类型
 * </p>
 *
 * @author shihuan sun
 * @date 2023-04-14
 */
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ProductTypeEnum", description = "产品类型")
public enum ProductTypeEnum {

    /**
     * 普通产品，需直连设备
     */
    COMMON("COMMON", "COMMON"),

    /**
     * 网关产品，可挂载子设备
     */
    GATEWAY("GATEWAY", "GATEWAY"),

    /**
     * 未知产品
     */
    UNKNOWN("UNKNOWN", "UNKNOWN"),

    ;

    private String value;
    private String desc;

    /**
     * 可选值
     */
    public static final List<String> TYPE_COLLECTION = Arrays.asList(COMMON.value, GATEWAY.value, UNKNOWN.value);

    public static Optional<ProductTypeEnum> fromValue(String value) {
        return Optional.of(Stream.of(ProductTypeEnum.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElse(UNKNOWN));
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
