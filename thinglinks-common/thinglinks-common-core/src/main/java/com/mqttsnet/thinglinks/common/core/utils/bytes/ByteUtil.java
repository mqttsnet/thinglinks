package com.mqttsnet.thinglinks.common.core.utils.bytes;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
 * @CreateDate: 2021/11/23$ 11:37$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/23$ 11:37$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class ByteUtil {

    private static ObjectMapper objectMapper=new ObjectMapper();
    private static List<String> datePattern=new ArrayList<String>();
    static {
        datePattern.add("yyyy-MM-dd hh/mm/ss");
        datePattern.add("yyyy-MM-dd hh:mm:ss");
        datePattern.add("yyyy/MM/dd hh:mm:dd");
    }
    static {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        DeserializationConfig config=objectMapper.getDeserializationConfig();
        for (String s : datePattern) {
            objectMapper.setDateFormat(new SimpleDateFormat(s));
        }
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }
    public  static ObjectMapper InstanceObjectMapper(){
        return  objectMapper;
    }
    public List<String> getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(List<String> datePattern) {
        this.datePattern = datePattern;
    }

    /**
     * 向串口发送数据转为字节数组
     */
    public static byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        String hex1 = hex.replace(" ", "");
        char[] hex2char = hex1.toCharArray();
        byte[] bytes = new byte[hex1.length() / 2];
        byte temp;
        for (int p = 0; p < bytes.length; p++) {
            temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
            temp += digital.indexOf(hex2char[2 * p + 1]);
            bytes[p] = (byte) (temp & 0xff);
        }
        return bytes;
    }

    /**
     * 接收到的字节数组转换16进制字符串
     */
    public static String byteToStr(byte[] b, int size) {
        String ret = "";
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * 计算CRC16校验码
     * 逐个求和
     *
     * @param bytes 字节数组
     * @return {@link String} 校验码
     * @since 1.0
     */
    public static String getCRC_16(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        if (Integer.toHexString(CRC).toUpperCase().length() == 2) {
            return byteToStr(bytes, bytes.length) + "00" + Integer.toHexString(CRC).toUpperCase();
        } else if (Integer.toHexString(CRC).toUpperCase().length() == 3) {
            return byteToStr(bytes, bytes.length) + "0" + Integer.toHexString(CRC).toUpperCase();
        }
        return byteToStr(bytes, bytes.length) + Integer.toHexString(CRC).toUpperCase();
    }

    /**
     * 指令校验和,并取出后两位字节
     * */
    public static String getSum16(byte[] msg, int length) {
        long mSum = 0;
        byte[] mByte = new byte[length];

        /** 逐Byte添加位数和 */
        for (byte byteMsg : msg) {
            long mNum = ((long) byteMsg >= 0) ? (long) byteMsg : ((long) byteMsg + 256);
            mSum += mNum;
        } /** end of for (byte byteMsg : msg) */

        /** 位数和转化为Byte数组 */
        for (int liv_Count = 0; liv_Count < length; liv_Count++) {
            mByte[length - liv_Count - 1] = (byte) (mSum >> (liv_Count * 8) & 0xff);
        } /** end of for (int liv_Count = 0; liv_Count < length; liv_Count++) */
        return byteToStr(msg, length) + byteToStr(mByte, mByte.length).substring(byteToStr(mByte, mByte.length).length() - 4, byteToStr(mByte, mByte.length).length());
    }

    /**
     * 计算校验值
     */
    public static String getCRC_16CheckSum(String hexdata) {
        if (hexdata == null || hexdata.equals("")) {
            return "00";
        }
        hexdata = hexdata.replaceAll(" ", "");
        int total = 0;
        int len = hexdata.length();
        if (len % 2 != 0) {
            return "00";
        }
        int num = 0;
        while (num < len) {
            String s = hexdata.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        String data = hexInt(total);
        return data.substring(data.length() - 2, data.length());
    }

    public static String hexInt(int total) {
        int a = total / 256;
        int b = total % 256;
        if (a > 255) {
            return hexInt(a) + format(b);
        }
        return format(a) + format(b);
    }

    public static String format(int hex) {
        String hexa = Integer.toHexString(hex);
        int len = hexa.length();
        if (len < 2) {
            hexa = "0" + hexa;
        }
        return hexa;
    }

    public static String convertByteBufToString(ByteBuf buf) {
        String str;
        if (buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }

}
