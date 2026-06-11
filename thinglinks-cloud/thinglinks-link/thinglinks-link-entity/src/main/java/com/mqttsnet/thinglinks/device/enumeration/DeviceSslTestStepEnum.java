package com.mqttsnet.thinglinks.device.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SSL 证书认证测试器分步枚举。前端按此顺序渲染步骤卡片。
 *
 * @author mqttsnet
 */
@Getter
@AllArgsConstructor
@Schema(title = "DeviceSslTestStepEnum", description = "SSL 测试分步")
public enum DeviceSslTestStepEnum {

    /** ① 解析 client 证书 */
    PARSE_CLIENT_CERT("PARSE_CLIENT_CERT", "证书解析"),
    /** ② 客户端证书有效期 */
    VALIDITY_CHECK("VALIDITY_CHECK", "有效期检查"),
    /** ③ 按序列号查找 CA */
    FIND_CA("FIND_CA", "CA 查找"),
    /** ④ 校验 CA 是否已颁发 */
    CA_STATE_CHECK("CA_STATE_CHECK", "CA 状态校验"),
    /** ⑤ 客户端 Issuer DN = CA Subject DN */
    ISSUER_MATCH("ISSUER_MATCH", "Issuer DN 匹配"),
    /** ⑥ 用 CA 公钥密码学验证客户端签名 */
    SIGNATURE_VERIFY("SIGNATURE_VERIFY", "密码学签名验证");

    private final String value;
    private final String desc;
}
