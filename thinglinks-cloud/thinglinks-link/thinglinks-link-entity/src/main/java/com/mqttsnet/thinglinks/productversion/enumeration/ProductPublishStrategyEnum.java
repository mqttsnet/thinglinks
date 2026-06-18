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
     * 影子发布:仅预建影子版本的 TD 超表、不改绑设备、不切激活指针(预建表 + 外部切流)。
     * 影子表预建后空表属预期 ── 新版本启用靠外部把设备 bound_product_version_no 改到该版本,改绑后该设备上报
     * 自然落到预建表;发布时不旁路双写影子表(详见 {@code ShadowDeviceRebindStrategy})。
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
