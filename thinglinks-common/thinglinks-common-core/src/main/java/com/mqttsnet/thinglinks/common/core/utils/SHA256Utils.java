package com.mqttsnet.thinglinks.common.core.utils;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Component
public class SHA256Utils {
    public static void main(String[] args) {
        String appId="123";
        String appKey="123";
        String timestamp="1568383635245";
        String sha256String = getSHA256String(appId + appKey + timestamp);
        System.out.println(sha256String);
    }

    /**
     * 用java原生的摘要实现SHA256加密
     *
     * @param str 加密前的报文
     * @return
     */
    public static String getSHA256String(String str) {
        String encodeStr = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * byte[]转为16进制
     *
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    //随机密码 ： 6位数字
    public static String randompassword(){
        char[] chars = new char[6];
        Random rnd = new Random();
        for(int i=0;i < 6 ; i++){
            chars[i] = (char)('0'+rnd.nextInt(10));
        }
        return new String(chars);
    }
}
