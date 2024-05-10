package com.mqttsnet.thinglinks.link.api.domain.ota.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * OTA升级包状态
 * </p>
 *
 * @author shihuan sun
 * @date 2023-04-14
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ProductStatusEnum", description = "OTA升级包状态，字典标识：LINK_OTA_PACKAGES_STATUS")
public enum OtaPackageStatusEnum {

    ENABLE(0, "启用"),

    DISABLE(1, "禁用"),

    ;

    private Integer value;
    private String desc;

    public static Optional<OtaPackageStatusEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
                .flatMap(val -> Stream.of(OtaPackageStatusEnum.values())
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
