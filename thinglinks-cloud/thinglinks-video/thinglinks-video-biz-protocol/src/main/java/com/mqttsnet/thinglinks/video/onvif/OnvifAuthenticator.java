package com.mqttsnet.thinglinks.video.onvif;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * 生成 WS-Security UsernameToken（OASIS WSS Username Token Profile 1.0 + WS-Security 2004）。
 *
 * <p>ONVIF 设备认证遵循 WS-Security UsernameToken 规范，签名算法：
 * {@code passwordDigest = base64(sha1(nonce + created + password))}
 *
 * <p>客户端需要在每个 SOAP 请求的 Header 里塞入：
 * <pre>{@code
 * <Security xmlns="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
 *   <UsernameToken>
 *     <Username>admin</Username>
 *     <Password Type="...PasswordDigest">{passwordDigest}</Password>
 *     <Nonce EncodingType="...Base64Binary">{base64-nonce}</Nonce>
 *     <Created xmlns="...wsu">{ISO-8601 timestamp}</Created>
 *   </UsernameToken>
 * </Security>
 * }</pre>
 *
 * <p>此类不持有任何会话状态，每次 build() 都生成新 nonce + created，避免重放。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
public final class OnvifAuthenticator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OnvifAuthenticator() {
    }

    /**
     * 生成 SOAP Security Header XML 片段。
     * <p>调用方把它直接拼到 SOAP Envelope 的 {@code <s:Header>} 内即可。
     *
     * @param username 用户名（不为空）
     * @param password 密码（不为空，明文）
     * @return 完整 {@code <Security>...</Security>} XML 片段
     * @throws IllegalStateException 当 SHA-1 算法不可用时（理论上不会发生）
     */
    public static String buildSecurityHeader(String username, String password) {
        byte[] nonceBytes = new byte[16];
        RANDOM.nextBytes(nonceBytes);
        String nonceBase64 = Base64.getEncoder().encodeToString(nonceBytes);
        String createdIso = Instant.now().toString();

        String passwordDigest = computeDigest(nonceBytes, createdIso, password);

        return "<wsse:Security s:mustUnderstand=\"1\" "
                + "xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" "
                + "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" "
                + "xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">"
                + "<wsse:UsernameToken>"
                + "<wsse:Username>" + xmlEscape(username) + "</wsse:Username>"
                + "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">"
                + passwordDigest + "</wsse:Password>"
                + "<wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">"
                + nonceBase64 + "</wsse:Nonce>"
                + "<wsu:Created>" + createdIso + "</wsu:Created>"
                + "</wsse:UsernameToken>"
                + "</wsse:Security>";
    }

    /**
     * 计算 PasswordDigest = base64(sha1(nonceBytes + createdString + password))。
     */
    private static String computeDigest(byte[] nonceBytes, String created, String password) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] createdBytes = created.getBytes(StandardCharsets.UTF_8);
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buf = ByteBuffer.allocate(nonceBytes.length + createdBytes.length + passwordBytes.length);
            buf.put(nonceBytes);
            buf.put(createdBytes);
            buf.put(passwordBytes);
            byte[] digest = sha1.digest(buf.array());
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 是 JDK 必备算法，不可能抛
            throw new IllegalStateException("SHA-1 不可用", e);
        }
    }

    private static String xmlEscape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
