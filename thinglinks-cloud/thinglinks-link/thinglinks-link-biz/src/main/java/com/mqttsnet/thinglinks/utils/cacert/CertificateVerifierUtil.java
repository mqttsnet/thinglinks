package com.mqttsnet.thinglinks.utils.cacert;

import java.io.ByteArrayInputStream;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import com.mqttsnet.thinglinks.utils.x509.CertSerialNumberUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * X.509 客户端证书与 CA 证书的链式校验工具(无状态,线程安全)。
 *
 * <p>核心 API:{@link #verify(String, String)} 在一次调用内完成 CA 解析 + 客户端证书解析 +
 * 有效期 / 签发者 DN / <b>密码学签名</b>四步校验,杜绝旧版静态字段并发污染。
 *
 * @author mqttsnet
 */
@Slf4j
public final class CertificateVerifierUtil {

    private CertificateVerifierUtil() {
    }

    /**
     * 验证客户端证书是否由指定 CA 签发且仍有效。
     *
     * @param caCertBase64     Base64 编码的 CA 证书(必填)
     * @param clientCertBase64 Base64 编码的客户端证书(必填)
     * @return true=通过;false=拒绝(原因走 {@code log.warn})
     */
    public static boolean verify(String caCertBase64, String clientCertBase64) {
        if (caCertBase64 == null || caCertBase64.isBlank()
                || clientCertBase64 == null || clientCertBase64.isBlank()) {
            log.warn("[Cert] verify skipped: blank input (caBlank={}, clientBlank={})",
                    caCertBase64 == null || caCertBase64.isBlank(),
                    clientCertBase64 == null || clientCertBase64.isBlank());
            return false;
        }
        try {
            X509Certificate ca = decode(caCertBase64);
            X509Certificate client = decode(clientCertBase64);

            // ① 有效期
            client.checkValidity();

            // ② 签发者 DN(快速短路)
            if (!client.getIssuerX500Principal().equals(ca.getSubjectX500Principal())) {
                log.warn("[Cert] issuer DN mismatch clientSerial={} clientIssuer={} caSubject={}",
                        CertSerialNumberUtil.getOpenSSLSerial(client),
                        client.getIssuerX500Principal(),
                        ca.getSubjectX500Principal());
                return false;
            }

            // ③ 密码学签名验证 ── PKI 灵魂,旧版被注释,本版恢复
            client.verify(ca.getPublicKey());

            log.debug("[Cert] verify ok clientSerial={} caSerial={}",
                    CertSerialNumberUtil.getOpenSSLSerial(client),
                    CertSerialNumberUtil.getOpenSSLSerial(ca));
            return true;
        } catch (CertificateExpiredException e) {
            log.warn("[Cert] client certificate expired: {}", e.getMessage());
        } catch (CertificateNotYetValidException e) {
            log.warn("[Cert] client certificate not yet valid: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("[Cert] signature verify failed (client not signed by this CA)");
        } catch (CertificateException e) {
            log.warn("[Cert] certificate decode failed: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("[Cert] verify unexpected error: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 解析 Base64 证书为 X.509 对象。
     *
     * @throws CertificateException Base64 / X.509 解码失败
     */
    public static X509Certificate decode(String base64Cert) throws CertificateException {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Cert);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(decoded));
        } catch (IllegalArgumentException e) {
            throw new CertificateException("Invalid Base64 input", e);
        }
    }
}
