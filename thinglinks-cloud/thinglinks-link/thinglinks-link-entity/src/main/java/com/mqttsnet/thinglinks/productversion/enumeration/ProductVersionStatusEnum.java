package com.mqttsnet.thinglinks.productversion.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 产品物模型版本状态枚举。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productversion.entity.ProductVersion
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ProductVersionStatusEnum", description = "产品物模型版本状态")
public enum ProductVersionStatusEnum {

    /**
     * 草稿。
     */
    DRAFT(0, "草稿"),
    /**
     * 已发布。
     */
    PUBLISHED(1, "已发布"),
    /**
     * 灰度中。
     */
    CANARY(2, "灰度中"),
    /**
     * 影子(旁路验证)。
     */
    SHADOW(3, "影子"),
    /**
     * 已回滚。
     */
    ROLLED_BACK(4, "已回滚"),
    /**
     * 已归档。
     */
    ARCHIVED(5, "已归档"),
    ;

    private Integer value;
    private String desc;

    /**
     * 根据数值查找对应枚举,找不到返回 {@link Optional#empty()}。
     *
     * @param value 枚举数值
     * @return {@link Optional} 包裹的枚举项
     */
    public static Optional<ProductVersionStatusEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
            .flatMap(val -> Stream.of(ProductVersionStatusEnum.values())
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
