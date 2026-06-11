package com.mqttsnet.thinglinks.cacert.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CA 证书审计动作类型。
 *
 * @author mqttsnet
 */
@Getter
@AllArgsConstructor
@Schema(title = "CaCertAuditTypeEnum", description = "CA 证书审计动作类型")
public enum CaCertAuditTypeEnum {

    IMPORT("IMPORT", "导入证书"),
    ISSUE("ISSUE", "颁发证书"),
    REVOKE("REVOKE", "吊销证书"),
    DOWNLOAD_PACK("DOWNLOAD_PACK", "下载客户端证书包"),
    SSL_TEST("SSL_TEST", "SSL 认证测试");

    private final String value;
    private final String desc;
}
