package com.mqttsnet.thinglinks.utils.x509;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * 证书生成工具类
 * @author mqttsnet
 */

public class MqttCertGenerator {
    private static final String KEY_ALG = "RSA";
    private static final String SIG_ALG = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;
    private static final int VALIDITY_DAYS = 36500;

    private static final String CA_DN = "CN=CA,O=mqttsnet,OU=www.mqttsnet.com,C=CN,ST=BJ,L=BJ";
    private static final String SERVER_DN = "CN=SERVER,O=mqttsnet,OU=www.mqttsnet.com,C=CN,ST=BJ,L=BJ";
    private static final String CLIENT_DN = "CN=CLIENT,O=mqttsnet,OU=www.mqttsnet.com,C=CN,ST=BJ,L=BJ";

    /** PKCS12 客户端证书口令的环境变量名称。 */
    private static final String P12_PASSWORD_ENV = "THINGLINKS_MQTT_P12_PASSWORD";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        char[] p12Password = requirePkcs12Password(System.getenv(P12_PASSWORD_ENV));
        try {
            String outputDir = "./ssl";
            createDirectories(outputDir);

            // Generate CA
            KeyPair caKeyPair = generateKeyPair();
            X509Certificate caCert = generateCACert(caKeyPair);
            savePEM(outputDir + "/ca/ca.key", caKeyPair.getPrivate().getEncoded(), "PRIVATE KEY");
            savePEM(outputDir + "/ca/ca.cer", caCert.getEncoded(), "CERTIFICATE");

            // Generate Server
            KeyPair serverKeyPair = generateKeyPair();
            X509Certificate serverCert = generateCert(serverKeyPair.getPublic(), caKeyPair, SERVER_DN);
            savePEM(outputDir + "/server/server.key", serverKeyPair.getPrivate().getEncoded(), "PRIVATE KEY");
            savePEM(outputDir + "/server/server.cer", serverCert.getEncoded(), "CERTIFICATE");
            convertToPKCS8(outputDir + "/server/server.key", outputDir + "/server/server_pkcs8.key");

            // Generate Client
            KeyPair clientKeyPair = generateKeyPair();
            X509Certificate clientCert = generateCert(clientKeyPair.getPublic(), caKeyPair, CLIENT_DN);
            savePEM(outputDir + "/client/client.key", clientKeyPair.getPrivate().getEncoded(), "PRIVATE KEY");
            savePEM(outputDir + "/client/client.cer", clientCert.getEncoded(), "CERTIFICATE");
            convertToPKCS8(outputDir + "/client/client.key", outputDir + "/client/client_pkcs8.key");
            createPKCS12(clientCert, clientKeyPair.getPrivate(), caCert,
                    outputDir + "/client/client.p12", p12Password);

            System.out.println("All certificates generated successfully in: " + outputDir);
        } finally {
            Arrays.fill(p12Password, '\0');
        }
    }

    static char[] requirePkcs12Password(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("PKCS12 证书口令未配置，请设置环境变量 " + P12_PASSWORD_ENV);
        }
        return password.toCharArray();
    }

    private static void createDirectories(String baseDir) throws Exception {
        Files.createDirectories(Paths.get(baseDir, "ca"));
        Files.createDirectories(Paths.get(baseDir, "server"));
        Files.createDirectories(Paths.get(baseDir, "client"));
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALG);
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    private static X509Certificate generateCACert(KeyPair keyPair) throws Exception {
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + VALIDITY_DAYS * 24L * 60 * 60 * 1000);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                new X500Name(CA_DN),
                BigInteger.ONE,
                notBefore,
                notAfter,
                new X500Name(CA_DN),
                keyPair.getPublic())
                .addExtension(Extension.keyUsage, true,
                        new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign))
                .addExtension(Extension.basicConstraints, true,
                        new BasicConstraints(true));

        ContentSigner signer = new JcaContentSignerBuilder(SIG_ALG).build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));
    }

    private static X509Certificate generateCert(PublicKey publicKey, KeyPair caKeyPair, String subjectDN) throws Exception {
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + VALIDITY_DAYS * 24L * 60 * 60 * 1000);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                new X500Name(CA_DN),
                BigInteger.valueOf(System.currentTimeMillis()),
                notBefore,
                notAfter,
                new X500Name(subjectDN),
                publicKey)
                .addExtension(Extension.keyUsage, true,
                        new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment))
                .addExtension(Extension.basicConstraints, false,
                        new BasicConstraints(false));

        ContentSigner signer = new JcaContentSignerBuilder(SIG_ALG).build(caKeyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));
    }

    private static void savePEM(String path, byte[] content, String type) throws Exception {
        String pem = "-----BEGIN " + type + "-----\n" +
                Base64.getEncoder().encodeToString(content) + "\n" +
                "-----END " + type + "-----\n";
        Files.write(Paths.get(path), pem.getBytes());
    }

    private static void convertToPKCS8(String inputPath, String outputPath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(inputPath));
        String keyPem = new String(keyBytes);
        String base64 = keyPem.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        savePEM(outputPath, Base64.getDecoder().decode(base64), "PRIVATE KEY");
    }

    private static void createPKCS12(X509Certificate cert, PrivateKey privateKey,
                                     X509Certificate caCert, String filename,
                                     char[] password) throws Exception {
        KeyStore pkcs12 = KeyStore.getInstance("PKCS12", "BC");
        pkcs12.load(null, null);

        // 创建证书链
        Certificate[] chain = new Certificate[]{cert, caCert};

        // 设置私钥条目
        pkcs12.setKeyEntry("client", privateKey, password, chain);

        // 保存文件
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            pkcs12.store(fos, password);
        }
    }


}
