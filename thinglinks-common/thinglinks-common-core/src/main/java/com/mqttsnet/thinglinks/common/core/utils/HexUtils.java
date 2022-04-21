package com.mqttsnet.thinglinks.common.core.utils;

import com.mqttsnet.thinglinks.common.core.utils.bytes.ByteUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * @title 16进制相关工具类
 * @discribtion 本着依赖最少原则，封装常用工具类
 * @author thinglinks
 * @Date 2020年5月21日 下午4:31:17
 * @vision V1.0
 */
@Slf4j
public class HexUtils
{
    /**
     * @title byte数组转16进制字符串
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午4:36:49
     * @vision V1.0
     */
    public static String byteArr2HexStr(byte[] byteArr)
    {
        if (byteArr == null)
        {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArr.length * 2];
        for (int j = 0; j < byteArr.length; j++)
        {
            int v = byteArr[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * @title 16进制字符串转byte数组
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午4:39:00
     * @vision V1.0
     */
    public static byte[] hexStr2ByteArr(String hexStr)
    {
        if (hexStr == null)
        {
            return null;
        }
        if (hexStr.length() == 0)
        {
            return new byte[0];
        }
        byte[] byteArray = new byte[hexStr.length() / 2];
        for (int i = 0; i < byteArray.length; i++)
        {
            String subStr = hexStr.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte)Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }
    
    /**
     * @title 合并多个byte数组
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午4:41:36
     * @vision V1.0
     */
    public static byte[] mergeAllBytes(byte[]... values)
    {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++)
        {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++)
        {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }
    
    /**
     * @title 异或校验
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午4:47:40
     * @vision V1.0
     */
    public static byte getXor(byte[] datas)
    {
        byte temp = datas[0];
        for (int i = 1; i < datas.length; i++)
        {
            temp ^= datas[i];
        }
        return temp;
    }
    
    /**
     * @title 16进制字符串转整数
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午4:54:15
     * @vision V1.0
     */
    public static int hexStr2Int(String hexStr)
    {
        return Integer.parseInt(hexStr, 16);
    }
    
    /**
     * @title 整数转16进制字符串
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午4:54:54
     * @vision V1.0
     */
    public static String int2HexStr(int num)
    {
        return Integer.toHexString(num);
    }
    
    /**
     * @title 16进制字符串转2进制字符串
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午5:21:30
     * @vision V1.0
     */
    public static String hexStr2BinStr(String hexStr)
    {
        if (hexStr == null || hexStr.length() % 2 != 0)
        {
            return null;
        }
        String bString = "", tmp;
        for (int i = 0; i < hexStr.length(); i++)
        {
            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexStr.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }
    
    /**
     * @title 2进制字符串转16进制字符串
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午5:21:49
     * @vision V1.0
     */
    public static String binStr2HexStr(String binStr)
    {
        if (binStr == null || binStr.equals("") || binStr.length() % 8 != 0)
        {
            return null;
        }
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < binStr.length(); i += 4)
        {
            iTmp = 0;
            for (int j = 0; j < 4; j++)
            {
                iTmp += Integer.parseInt(binStr.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }
    
    /**
     * @title 字节转位
     * @discribtion
     * @author zch
     * @param
     * @Date 2020年5月21日 下午5:23:29
     * @vision V1.0
     */
    public static String byte2Bit(byte by)
    {
        // @formatter:off
        StringBuffer sb = new StringBuffer();
        sb.append((by >> 7) & 0x1)
          .append((by >> 6) & 0x1)
          .append((by >> 5) & 0x1)
          .append((by >> 4) & 0x1)
          .append((by >> 3) & 0x1)
          .append((by >> 2) & 0x1)
          .append((by >> 1) & 0x1)
          .append((by >> 0) & 0x1);
        // @formatter:on
        return sb.toString();
    }
    /**
     * @Description: 16进制字符串转10进制
     * @Param: [hex]
     * @return: java.lang.String
     * @Author: Liang Shan
     * @Date: 2019/9/24 0024
     */
    public static Integer hexStringToDecimal(String hex) {
        Integer result = Integer.valueOf(hex, 16);
        return result;
    }

    /**
     * @Description: 16进制字符串转2进制字符串
     * @Param: [hex]
     * @return: java.lang.String
     * @Author: Liang Shan
     * @Date: 2019/9/24 0024
     */
    public static String hexStringToBinaryString(String hex) {
        Integer temp = Integer.valueOf(hex, 16);
        String result = Integer.toBinaryString(temp);
        return result;
    }

    /**
     * @Description: 10进制转16进制字符串
     * @Param: []
     * @return: java.lang.String
     * @Author: Liang Shan
     * @Date: 2019/9/24 0024
     */
    public static String decimalToHexString(Integer decimal) {
        String result = Integer.toHexString(decimal);
        return result;
    }

    /**
     * @Description: 10进制转2进制字符串
     * @Param: []
     * @return: java.lang.String
     * @Author: Liang Shan
     * @Date: 2019/9/24 0024
     */
    public static String decimalToBinaryString(Integer decimal) {
        String result = Integer.toBinaryString(decimal);
        return result;
    }

    /**
     * @Description: 2进制字符串转10进制
     * @Param: [hex]
     * @return: java.lang.String
     * @Author: Liang Shan
     * @Date: 2019/9/24 0024
     */
    public static Integer binaryStringToDecimal(String binary) {
        Integer result = Integer.valueOf(binary, 2);
        return result;
    }

    /**
     * @Description: 2进制字符串转16进制字符串
     * @Param: []
     * @return: java.lang.String
     * @Author: Liang Shan
     * @Date: 2019/9/24 0024
     */
    public static String binaryStringToHexString(String binary) {
        Integer temp = Integer.valueOf(binary, 2);
        String result = Integer.toHexString(temp);
        return result;
    }

    /**
     * @Description: byte[]转字符串
     * @Param: [bytes]
     * @return: java.lang.String
     * @Author: Liang Shan
     * @Date: 2019/9/20 0020
     */
    public static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xff);
            if (temp.length() == 1) {
                // 得到的一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * @Description: byte[]转String[]
     * @Param: [bytes]
     * @return: java.lang.String[]
     * @Author: Liang Shan
     * @Date: 2019/9/20 0020
     */
    public static String[] bytesToStrings(byte[] bytes) {
        String[] strings = new String[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            String temp = Integer.toHexString(bytes[i] & 0xff);
            if (temp.length() == 1) {
                strings[i] = '0' + temp;
            } else {
                strings[i] = temp;
            }
        }
        return strings;
    }

    /**
     * 16进制转换为ASCII
     * @param hex
     * @return
     */
    public static String convertHexToString(String hex){

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }

        return sb.toString();
    }

    public static String getBCC(String hex) {
        int i = 0, j = 0;
        int len = hex.length();
        short inb[] = new short[len];
        for (i = 0; i < len; i++) {
            inb[i] = charToHex(hex.charAt(i));   //将String里的每一个char转换为Hex
        }

        for (i = 0; i < len; i++) {    //将每两个Hex合并成一个byte
            inb[j] = (byte) (((inb[i] << 4) & 0x00f0) | ((inb[i + 1]) & 0x000f));
            i++;
            j++;
        }
        byte temp = 0x00; //校验值
        for (i = 0; i < len / 2; i++) { //异或
            temp ^= inb[i];
        }
        byte[] bytes = new byte[1];
        bytes[0] = temp;
        return ByteUtil.byteToStr(bytes, bytes.length);
    }

    public static short charToHex(char x) {
        short result = 0;
        switch (x) {
            case 'a':
                result = 10;
                break;
            case 'b':
                result = 11;
                break;
            case 'c':
                result = 12;
                break;
            case 'd':
                result = 13;
                break;
            case 'e':
                result = 14;
                break;
            case 'f':
                result = 15;
                break;
            case 'A':
                result = 10;
                break;
            case 'B':
                result = 11;
                break;
            case 'C':
                result = 12;
                break;
            case 'D':
                result = 13;
                break;
            case 'E':
                result = 14;
                break;
            case 'F':
                result = 15;
                break;
            default:
                result = (short) Character.getNumericValue(x);
                break;
        }
        return result;
    }

    /**
     * 十六进制单精度浮点数，转BigDecimal，保留2为小数，截掉多余小数位
     *
     * @param hex 十六进制
     * @param scale 保留小数位
     * @return
     */
    public static Double hexFloat2BigDecimal(String hex,Integer scale) {
        float value = Float.intBitsToFloat((int) Long.parseLong(hex, 16));
        BigDecimal bd = new BigDecimal(Float.toString(value));
        Double num = bd.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        return num;
    }

    public static void main(String[] args) {
        System.out.println("\n***** 16进制转换为ASCII *****");
        System.out.println("Hex : " + "4c464341483935570344d33303130303936");
        System.out.println("ASCII : " + HexUtils.convertHexToString("4c464341483935570344d33303130303936"));
    }
}
