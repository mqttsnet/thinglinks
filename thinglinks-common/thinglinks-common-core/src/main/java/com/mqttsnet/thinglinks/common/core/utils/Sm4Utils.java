package com.mqttsnet.thinglinks.common.core.utils;

import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

/**
 * @Description: 国密SM4分组密码算法工具类（对称加密）
 * @Author: ShiHuan Sun
 * @CreateDate: 2021/8/25$ 15:27$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/8/25$ 15:27$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class Sm4Utils {
    /**
     * 加密
     *
     * @param key  密钥
     * @param iv   初始向量
     * @param data 明文
     * @return 密文
     */
    public static String encrypt(String key, String iv, String data) {
        try {
            // 创建SM4引擎
            SM4Engine sm4Engine = new SM4Engine();
            // 创建CBC模式的加密器
            CBCBlockCipher cbcBlockCipher = new CBCBlockCipher(sm4Engine);
            // 创建填充加密器
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcBlockCipher, new PKCS7Padding());
            // 创建密钥参数
            KeyParameter keyParameter = new KeyParameter(Hex.decode(key));
            // 创建带IV的参数
            ParametersWithIV parametersWithIV = new ParametersWithIV(keyParameter, Hex.decode(iv));
            // 初始化加密器
            cipher.init(true, parametersWithIV);

            byte[] encryptedData = new byte[cipher.getOutputSize(data.getBytes(StandardCharsets.UTF_8).length)];
            int length = cipher.processBytes(data.getBytes(StandardCharsets.UTF_8), 0, data.getBytes(StandardCharsets.UTF_8).length, encryptedData, 0);
            cipher.doFinal(encryptedData, length);

            return Hex.toHexString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("SM4加密失败", e);
        }
    }

    /**
     * 解密
     *
     * @param key  密钥
     * @param iv   初始向量
     * @param data 密文
     * @return 明文
     */
    public static String decrypt(String key, String iv, String data) {
        try {
            // 创建SM4引擎
            SM4Engine sm4Engine = new SM4Engine();
            // 创建CBC模式的解密器
            CBCBlockCipher cbcBlockCipher = new CBCBlockCipher(sm4Engine);
            // 创建填充解密器
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcBlockCipher, new PKCS7Padding());
            // 创建密钥参数
            KeyParameter keyParameter = new KeyParameter(Hex.decode(key));
            // 创建带IV的参数
            ParametersWithIV parametersWithIV = new ParametersWithIV(keyParameter, Hex.decode(iv));
            // 初始化解密器
            cipher.init(false, parametersWithIV);

            byte[] decryptedData = new byte[cipher.getOutputSize(Hex.decode(data).length)];
            int length = cipher.processBytes(Hex.decode(data), 0, Hex.decode(data).length, decryptedData, 0);
            int finalLength = cipher.doFinal(decryptedData, length);

            return new String(decryptedData, 0, length + finalLength, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4解密失败", e);
        }
    }

    public static void main(String[] args) {
        String key = "0123456789abcdef0123456789abcdef";
        String iv = "0123456789abcdef0123456789abcdef";
        String data = "Hello, SM4!";

        String encryptedData = Sm4Utils.encrypt(key, iv, data);
        System.out.println("加密后的数据: " + encryptedData);

        String decryptedData = Sm4Utils.decrypt(key, iv, encryptedData);
        System.out.println("解密后的数据: " + decryptedData);
    }

}
