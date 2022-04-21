package com.mqttsnet.thinglinks.common.core.utils.bytes;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mqtts.net
 * @description:
 * @date 2018-12-27 17:27
 *
 */
public class ByteArrayUtil {

    /**
     * byte数组拼接
     * @param first
     * @param back
     * @return
     */
    public static byte[] append(byte[] first, byte[] back) {
        if(null == first || null == back){
            return null;
        }
        int length = first.length + back.length;
        byte[] res = new byte[length];
        System.arraycopy(first, 0, res, 0, first.length);
        System.arraycopy(back, 0, res, first.length, back.length);
        return res;

    }

    /**
     * sub byte
     * @param data
     * @param off
     * @param length
     * @return
     */
    public static byte[] subBytes(byte[] data,int off,int length){
        byte[] res = new byte[length];
        System.arraycopy(data, off, res, 0, length);
        return res;
    }


    /**
     * short转换为byte[]
     * @param number
     * @return byte[]
     */
    public static byte[] short2Bytes(short number) {
        byte[] b = new byte[2];
        b[0] = (byte) (number >> 8);
        b[1] = (byte) (number & 0xFF);
        return b;
    }

    /**
     * byte[]转换为short
     * @param bytes
     * @return short
     */
    public static short bytes2Short(byte[] bytes){
        short z = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
        return z;
    }

    /**
     * byte to int
     *
     * @param data
     * @return
     */
    public static int bytes2int(byte[] data) {
        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = 0; i < data.length; i++) {
            n <<= 8;
            temp = data[i] & mask;
            n |= temp;
        }
        return n;
    }


    /**
     * int to bytes
     *
     * @param n
     * @return
     */
    public static byte[] int2bytes(int n) {
        byte[] b = new byte[4];

        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));

        }
        return b;
    }

    /**
     * byte[] 转换为 GBK 编码的 字符串
     * @param byteArray
     * @return
     */
    public static String bytes2gbkString(byte[] byteArray) {
        return bytes2string(byteArray,"GBK");
    }

    /**
     * byte[] 转换为 string
     * @param byteArray
     * @param charset
     * @return
     */
    public static String bytes2string(byte[] byteArray, String charset) {
        if (byteArray.length == 0) {
            return "";
        }
        return new String(byteArray, Charset.forName(charset));
    }

    /**
     * byte[] 转换为 string
     * @param byteArray
     * @return
     */
    public static String bytes2string(byte[] byteArray) {
        if (byteArray.length == 0) {
            return "";
        }
        return new String(byteArray, Charset.defaultCharset());
    }


    /**
     * byte[] to float
     * @param floatArray
     * @return
     */
    public static float bytes2float(byte[] floatArray){
        int accum = 0;
        for(int i = 0,j = 0; i < floatArray.length; i ++){
            accum = accum|(floatArray[i] & 0xff) << j;
            j += 8;
        }
        return Float.intBitsToFloat(accum);
    }


    /**
     * byte[] to double
     * @param doubleArray
     * @return
     */
    public static double bytes2double(byte[] doubleArray) {
        long value = 0;
        for (int i = 0; i < doubleArray.length; i++) {
            value |= ((long) (doubleArray[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    /**
     * 数组转换成十六进制字符串
     * @param array
     * @return HexString
     */
    public static String bytes2HexStr(byte[] array) {
        StringBuffer sb = new StringBuffer(array.length);
        String sTemp;
        for (int i = 0; i < array.length; i++) {
            sTemp = Integer.toHexString(0xFF & array[i]);
            if (sTemp.length() < 2){
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 数组转换成带0x的十六进制字符串
     * @param array
     * @return HexString
     */
    public static String bytes2FullHexStr(byte[] array) {
        StringBuffer sb = new StringBuffer(array.length);
        sb.append("0x");
        String sTemp;
        for (int i = 0; i < array.length; i++) {
            sTemp = Integer.toHexString(0xFF & array[i]);
            if (sTemp.length() < 2){
                sb.append(0);
            }
            sb.append(sTemp);
            if(i < array.length-1){
                sb.append("0x");
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * short to 16进制字符串
     * @param num
     * @return
     */
    public static String short2HexStr(short num){
        return Integer.toHexString(num);
    }

    /**
     * 把16进制字符串转换成字节数组
     * @param hex
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * 把带0x的16进制字符串转换成字节数组
     * @param hex
     * @return byte[]
     */
    public static byte[] fullHexStr2Bytes(String hex){
        hex = hex.toLowerCase().replaceAll("0x","").trim().toUpperCase();
        return hexStr2Bytes(hex);
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * byte数组转时间字符串 格式 yyMMddHHmmss
     * @return
     */
    public static String bytes2timeStr(byte[] array){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < array.length; i ++){
            int timeUnit = byte2UnsignedInt(array[i]);
            if(timeUnit < 10){
                stringBuilder.append(0);
            }
            stringBuilder.append(timeUnit);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return  sdf.format(new Date()).substring(0,2) + stringBuilder.toString();
    }


    /**
     * byte转无符号整数
     * @param value
     * @return
     */
    public static int byte2UnsignedInt(byte value) {
        return Byte.toUnsignedInt(value);
    }

    /**
     *
     *
     * @描述 将一个long转换成8位的byte[]
     * @param num
     * 	long值
     * @return
     * 长度是8的byte[]
     * @throws Exception
     */
    public static byte[] longToBytes(long num) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (num >>> (56 - i * 8));
        }
        return b;
    }
    /**
     *
     *
     * @描述 将一个数组转换成一个long值
     * @param b
     *  长度是8的byte[]
     * @return
     * 	long值
     * @throws Exception
     */
    public static long bytesToLong(byte[] b) {
        int mask = 0xff;
        long temp = 0;
        long res = 0;
        for (int i = 0; i < 8; i++) {
            res <<= 8;
            temp = b[i] & mask;
            res |= temp;
        }
        return res;
    }

    /**
     *  将一个byte数组转换成二进制字符串
     * @param bytes
     * @return 二进制字符串
     */
    public static String bytes2bitStr(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(byte2bitStr(b));
        }
        return stringBuilder.toString();
    }

    /**
     *  将一个byte转换成二进制字符串
     * @param b
     * @return 二进制字符串
     */
    public static String byte2bitStr(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }






}
