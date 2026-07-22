package com.mqttsnet.thinglinks.productpublishrecord.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 产品发布记录操作意图枚举。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ProductPublishRecordIntentEnum", description = "操作意图")
public enum ProductPublishRecordIntentEnum {

    /**
     * 发布新版本。
     */
    PUBLISH(0, "发布"),
    /**
     * 回滚到历史版本。
     */
    ROLLBACK(1, "回滚"),
    /**
     * 历史清理(删除版本对应的 TD 资源)。
     */
    PURGE_HISTORY(2, "历史清理"),
    ;

    private Integer value;
    private String desc;

    /**
     * 根据数值查找对应枚举。
     *
     * @param value 枚举数值
     * @return {@link Optional} 包裹的枚举项
     */
    public static Optional<ProductPublishRecordIntentEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
            .flatMap(val -> Stream.of(ProductPublishRecordIntentEnum.values())
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
