package com.mqttsnet.thinglinks.common.core.utils;

/**
 * 比较工具类
 */
public class CompareUtil {

    /**
     * int数值获取区间
     *
     * @param current
     * @param min
     * @param max
     * @return
     */
    public static boolean rangeInDefinedInt(int current, int min, int max) {
        return Math.max(min, current) == Math.min(current, max);
    }


    /**
     * double数值获取区间
     *
     * @param current
     * @param min
     * @param max
     * @return
     */
    public static boolean rangeInDefinedDouble(double current, double min, double max) {
        return Math.max(min, current) == Math.min(current, max);
    }
}
