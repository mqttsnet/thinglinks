package com.mqttsnet.thinglinks.common.core.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * @Description: AES加密
 * @Author: ShiHuan Sun
 * @CreateDate: 2021/9/8$ 14:49$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/9/8$ 14:49$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class AesUtils {
    public static void main(String[] args) throws Exception {

        String content =   " {\n" +
                "        \"batID\": \"120434305152\",\n" +
                "        \"chgTime\": 293,\n" +
                "        \"detailsList\": [\n" +
                "            {\n" +
                "                \"elect\": 7.23,\n" +
                "                \"rateType\": 3,\n" +
                "                \"startTime\": \"2021-06-30 12:43:10\",\n" +
                "                \"stopTime\": \"2021-06-30 12:48:03\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"electAmount\": 1,\n" +
                "        \"equipNo\": \"1000280001000003\",\n" +
                "        \"flatElect\": 7.23,\n" +
                "        \"gunNo\": 0,\n" +
                "        \"orderSn\": \"100028000100000320210630124232\",\n" +
                "        \"peakElect\": 0,\n" +
                "        \"plateNo\": \"京888888\",\n" +
                "        \"rateModelID\": \"1\",\n" +
                "        \"sharpElect\": 0,\n" +
                "        \"startElect\": 41.1,\n" +
                "        \"startSoc\": 97,\n" +
                "        \"startTime\": \"2021-06-30 12:43:10\",\n" +
                "        \"stopElect\": 48.33,\n" +
                "        \"stopReason\": 53,\n" +
                "        \"stopSoc\": 100,\n" +
                "        \"stopTime\": \"2021-06-30 12:48:03\",\n" +
                "        \"swapSn\": \"100028000200000120210630120022\",\n" +
                "        \"valleyElect\": 0,\n" +
                "        \"vin\": \"LZ5RB7D37LB013286\"\n" +
                "    }";
        System.out.println("加密内容：" + content);
        String key = "FA171555405706F73D7B973DB89F0B47";
        System.out.println("加密密钥和解密密钥：" + key);
        String encrypt = aesEncrypt(content, key);
        System.out.println("加密后：" +encrypt);
        String decrypt = aesDecrypt(encrypt, key);
        System.out.println("解密后：" + decrypt);
    }
    /**
     * 编码
     * @param bstr
     * @return String
     */
    public static String Base64encode(byte[] bstr) {
        return Base64.encodeBase64String(bstr);
    }

    /**
     * 解码
     * @param str
     * @return string
     */
    public static byte[] Base64decode(String str) {
        return Base64.decodeBase64(str);
    }

    /*
     * AES加密
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     * @throws Exception
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        /*防止linux下 随机生成key*/
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        kgen.init(128, random);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

        return cipher.doFinal(content.getBytes("UTF-8"));
    }

    /**
     * AES加密为base 64 code
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return Base64encode(aesEncryptToBytes(content, encryptKey));
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey 解密密钥
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) {
        byte[] decryptBytes = new byte[0];
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            /*防止linux下 随机生成key*/
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(decryptKey.getBytes());
            kgen.init(128, random);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
            decryptBytes = cipher.doFinal(encryptBytes);
            return new String(decryptBytes,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptKey;

    }

    /**
     * 将base 64 code AES解密
     * @param encryptStr 待解密的base 64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String decryptKey){
        return aesDecryptByBytes(Base64decode(encryptStr), decryptKey);
    }
}
