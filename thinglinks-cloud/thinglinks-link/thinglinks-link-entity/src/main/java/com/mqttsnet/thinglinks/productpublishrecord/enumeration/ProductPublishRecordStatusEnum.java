package com.mqttsnet.thinglinks.productpublishrecord.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 产品发布记录执行状态枚举。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ProductPublishRecordStatusEnum", description = "执行状态")
public enum ProductPublishRecordStatusEnum {

    /**
     * 执行中。
     */
    RUNNING(0, "执行中"),
    /**
     * 执行成功。
     */
    SUCCESS(1, "成功"),
    /**
     * 执行失败。
     */
    FAILED(2, "失败"),
    ;

    private Integer value;
    private String desc;

    /**
     * 根据数值查找对应枚举。
     *
     * @param value 枚举数值
     * @return {@link Optional} 包裹的枚举项
     */
    public static Optional<ProductPublishRecordStatusEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
            .flatMap(val -> Stream.of(ProductPublishRecordStatusEnum.values())
                .filter(e -> e.getValue().equals(val))
                .findFirst());
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
