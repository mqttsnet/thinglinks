package com.mqttsnet.thinglinks.common.core.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;

/**
 * @Description: RSA加密工具类
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2022/09/15$ 19:03$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2022/09/15$ 19:03$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class EncrypRSA {
    private static final String PUBLIC = "PUBLIC";
    private static final String PRIVATE = "PRIVATE";


    /**
     * 根据公钥加密
     *
     * @param publicKey      公钥
     * @param cleartextBytes 明文
     */
    public byte[] encrypt(RSAPublicKey publicKey, byte[] cleartextBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (publicKey != null) {
            //Cipher负责完成加密或解密工作，基于RSA
            Cipher cipher = Cipher.getInstance("RSA");
            //根据公钥，对Cipher对象进行初始化
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(cleartextBytes);
        }
        return null;
    }

    /**
     * 根据公钥加密
     *
     * @param publicKey 公钥
     * @param cleartext 明文
     * @return
     * @throws Exception
     */
    public byte[] encrypt(RSAPublicKey publicKey, String cleartext) throws Exception {
        if (publicKey != null) {
            return encrypt(publicKey, cleartext.getBytes());
        }
        return null;
    }

    /**
     * 指定加密类型进行加密
     *
     * @param key       密钥
     * @param cleartext 明文
     * @param keyType   加密密钥类型，默认公钥加密
     * @return
     * @throws Exception
     */
    public byte[] encrypt(String key, String cleartext, String keyType) throws Exception {
        if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(keyType)) {
            switch (keyType) {
                case PUBLIC:
                    return encrypt(getPublicKey(key), cleartext.getBytes());
                case PRIVATE:
                    return encrypt(getPrivateKey(key), cleartext.getBytes());
            }
            return encrypt(getPublicKey(key), cleartext.getBytes());
        }
        return null;
    }

    /**
     * 根据私钥加密
     *
     * @param privateKey     私钥
     * @param cleartextBytes 明文
     */
    public byte[] encrypt(RSAPrivateKey privateKey, byte[] cleartextBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (privateKey != null) {
            //Cipher负责完成加密或解密工作，基于RSA
            Cipher cipher = Cipher.getInstance("RSA");
            //根据公钥，对Cipher对象进行初始化
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(cleartextBytes);
        }
        return null;
    }

    /**
     * 根据私钥加密
     *
     * @param privateKey 私钥
     * @param cleartext  明文
     */
    public byte[] encrypt(RSAPrivateKey privateKey, String cleartext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (StringUtils.isNotEmpty((Collection<?>) privateKey)) {
            return encrypt(privateKey, cleartext.getBytes());
        }
        return null;
    }

    /**
     * 根据公钥解密
     *
     * @param publicKey       公钥
     * @param ciphertextBytes 密文
     */
    public byte[] decrypt(RSAPublicKey publicKey, byte[] ciphertextBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (publicKey != null) {
            //Cipher负责完成加密或解密工作，基于RSA
            Cipher cipher = Cipher.getInstance("RSA");
            //根据公钥，对Cipher对象进行初始化
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(ciphertextBytes);
        }
        return null;
    }

    /**
     * 根据公钥解密
     *
     * @param publicKey  公钥
     * @param ciphertext 密文
     */
    public byte[] decrypt(RSAPublicKey publicKey, String ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (publicKey != null) {
            return decrypt(publicKey, Base64.getDecoder().decode(ciphertext));
        }
        return null;
    }

    /**
     * 根据私钥解密
     *
     * @param privateKey 私钥
     * @param srcBytes   密文
     */
    public byte[] decrypt(RSAPrivateKey privateKey, byte[] srcBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (privateKey != null) {
            //Cipher负责完成加密或解密工作，基于RSA
            Cipher cipher = Cipher.getInstance("RSA");
            //根据公钥，对Cipher对象进行初始化
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(srcBytes);
        }
        return null;
    }

    /**
     * 根据私钥解密
     *
     * @param privateKey 私钥
     * @param ciphertext 密文
     */
    public byte[] decrypt(RSAPrivateKey privateKey, String ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (privateKey != null) {
            return decrypt(privateKey, Base64.getDecoder().decode(ciphertext));
        }
        return null;
    }

    /**
     * 指定解密类型进行解密
     *
     * @param key        密钥
     * @param ciphertext 密文
     * @param keyType    密钥类型，默认私钥解密
     */
    public byte[] decrypt(String key, String ciphertext, String keyType) throws Exception {
        if (key != null) {
            byte[] decode = Base64.getDecoder().decode(ciphertext);
            switch (keyType){
                case PUBLIC:
                    return decrypt(getPublicKey(key), decode);
                case PRIVATE:
                    return decrypt(getPrivateKey(key), decode);
            }
            return decrypt(getPrivateKey(key), decode);
        }
        return null;
    }

    /**
     * 根据私钥解密为String
     *
     * @param privateKey      私钥
     * @param ciphertextBytes 密文
     */
    public String decryptToString(RSAPrivateKey privateKey, byte[] ciphertextBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return new String(decrypt(privateKey, ciphertextBytes));
    }

    /**
     * 根据私钥解密为String
     *
     * @param privateKey 私钥
     * @param ciphertext 密文
     */
    public String decryptToString(RSAPrivateKey privateKey, String ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return new String(decrypt(privateKey, ciphertext));
    }

    /**
     * 根据密钥与密钥类型解密为 String
     *
     * @param key        密钥
     * @param ciphertext 密文
     * @param keyType    密钥类型
     */
    public String decryptToString(String key, String ciphertext, String keyType) throws Exception {
        if (key != null && !StringUtils.isEmpty(ciphertext) && !StringUtils.isEmpty(keyType)) {
            return new String(decrypt(key, ciphertext, keyType));
        }
        return null;
    }

    /**
     * 根据公钥解密为String
     *
     * @param publicKey
     * @param srcBytes
     * @return
     */
    public String decryptToString(RSAPublicKey publicKey, byte[] srcBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return new String(decrypt(publicKey, srcBytes));
    }

    /**
     * String转公钥PublicKey
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static RSAPublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 公钥转 String
     *
     * @param publicKey
     * @return
     */
    private static String getString(RSAPublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * String转私钥PrivateKey
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static RSAPrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * 私钥转 String
     *
     * @param privateKey
     * @return
     */
    private static String getString(RSAPrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static void main(String[] args) throws Exception {
        EncrypRSA encrypRSA = new EncrypRSA();
        String msg = "qwertyio!@#$%^&*()_+1234567890-=/*-+|，。/、！@#￥%……&（（（）——+";
        //KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        //初始化密钥对生成器，密钥大小为1024位
        keyPairGen.initialize(1024);
        //生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        //得到私钥
        byte[] encrypt = encrypRSA.encrypt((RSAPublicKey) keyPair.getPublic(), "".getBytes());
        String privateStr = getString((RSAPrivateKey) keyPair.getPrivate());
        String publicStr = getString((RSAPublicKey) keyPair.getPublic());
        System.out.println("私钥是："+privateStr);
        System.out.println("公钥是："+publicStr);
        byte[] ciphertextPrivate = encrypRSA.encrypt(privateStr, msg, EncrypRSA.PRIVATE);
        byte[] ciphertextPublic = encrypRSA.encrypt(publicStr, msg, EncrypRSA.PUBLIC);
        System.out.println("原文是———————————————："+msg);
        String cleartextPrivate = encrypRSA.decryptToString(privateStr, new String(Base64.getEncoder().encode(ciphertextPublic)), EncrypRSA.PRIVATE);
        String cleartextPublic = encrypRSA.decryptToString(publicStr,  new String(Base64.getEncoder().encode(ciphertextPrivate)),EncrypRSA.PUBLIC);
        System.out.println("私钥加密公钥解密后明文为："+cleartextPrivate);
        System.out.println("公钥加密私钥解密后明文为："+cleartextPublic);
    }
}
