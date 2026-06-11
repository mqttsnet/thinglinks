package com.mqttsnet.thinglinks.productversion.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 产品物模型版本发布策略枚举。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productversion.entity.ProductVersion
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ProductPublishStrategyEnum", description = "发布策略")
public enum ProductPublishStrategyEnum {

    /**
     * 全量发布。
     */
    FULL(0, "全量"),
    /**
     * 灰度发布。
     */
    CANARY(1, "灰度"),
    /**
     * 影子发布,旁路验证不影响生产。
     */
    SHADOW(2, "影子"),
    ;

    private Integer value;
    private String desc;

    /**
     * 根据数值查找对应枚举。
     *
     * @param value 枚举数值
     * @return {@link Optional} 包裹的枚举项
     */
    public static Optional<ProductPublishStrategyEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
            .flatMap(val -> Stream.of(ProductPublishStrategyEnum.values())
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
