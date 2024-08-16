package com.mqttsnet.thinglinks.link.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * -----------------------------------------------------------------------------
 * File Name: CertificateVerifierUtil
 * -----------------------------------------------------------------------------
 * Description:
 * CA证书验证工具类
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/8/16       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/8/16 16:37
 */
@Slf4j
public class CertificateVerifierUtil {

    private static X509Certificate trustedCertificate;

    /**
     * 设置受信任的CA证书
     *
     * @param base64Cert Base64编码的CA证书
     * @throws CertificateException 证书解析异常
     */
    public static void setTrustedCertificate(String base64Cert) throws CertificateException {
        try {
            trustedCertificate = decodeCertificate(base64Cert);
            log.info("Trusted CA certificate set successfully.");
        } catch (CertificateException e) {
            log.error("Failed to decode trusted CA certificate: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证客户端证书
     *
     * @param base64Cert Base64编码的客户端证书
     * @return 证书是否有效
     */
    public static boolean verifyCertificate(String base64Cert) {
        try {
            X509Certificate clientCert = decodeCertificate(base64Cert);
            // 检查证书是否在有效期内
            clientCert.checkValidity();
            // 验证证书是否由受信任的CA签署
            clientCert.verify(trustedCertificate.getPublicKey());
            log.info("Client certificate is valid.");
            return true;
        } catch (CertificateException e) {
            log.error("Failed to decode client certificate: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Certificate validation failed: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 解码Base64编码的证书
     *
     * @param base64Cert Base64编码的证书
     * @return X509Certificate对象
     * @throws CertificateException 证书解析异常
     */
    private static X509Certificate decodeCertificate(String base64Cert) throws CertificateException {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Cert);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(decoded));
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 input: {}", e.getMessage(), e);
            throw new CertificateException("Invalid Base64 input", e);
        } catch (CertificateException e) {
            log.error("Failed to generate certificate from input: {}", e.getMessage(), e);
            throw e;
        }
    }
}
