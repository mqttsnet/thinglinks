package com.mqttsnet.thinglinks.productversionchangelog.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产品物模型版本变更类型。
 *
 * <p>一条 product_version_change_log 记录的主操作类型,由本次草稿刷新的字段级 diff
 * 反推:全为新增 → CREATE,全为删除 → DELETE,含修改或混合 → UPDATE。</p>
 *
 * @author mqttsnet
 */
@Getter
@AllArgsConstructor
public enum ProductVersionChangeTypeEnum {

    /**
     * 新增(本次变更全是新增字段 / 服务 / 属性 / 命令)。
     */
    CREATE(0, "新增"),

    /**
     * 编辑(本次变更含字段修改,或新增 + 删除混合)。
     */
    UPDATE(1, "编辑"),

    /**
     * 删除(本次变更全是删除)。
     */
    DELETE(2, "删除");

    private final Integer value;
    private final String desc;

    /**
     * 根据数值查找对应枚举。
     *
     * @param value 类型值
     * @return {@link Optional} 包裹的枚举项
     */
    public static Optional<ProductVersionChangeTypeEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
            .flatMap(val -> Stream.of(ProductVersionChangeTypeEnum.values())
                .filter(e -> e.getValue().equals(val))
                .findFirst());
    }
}
