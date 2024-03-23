package com.mqttsnet.thinglinks.link.api.domain.ota.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * -----------------------------------------------------------------------------
 * File Name: OtaPackageTypeEnum
 * -----------------------------------------------------------------------------
 * Description:
 * PackageType
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/1/17       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024/1/17 14:34
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "OtaPackageTypeEnum", description = "OTA升级包类型，字典标识：LINK_OTA_PACKAGES_TYPE")
public enum OtaPackageTypeEnum {
    SOFTWARE(0, "software"),
    FIRMWARE(1, "firmware");

    private Integer value;
    private String desc;

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static Optional<OtaPackageTypeEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
                .flatMap(val -> Stream.of(OtaPackageTypeEnum.values())
                        .filter(e -> e.getValue().equals(val))
                        .findFirst());
    }

}
