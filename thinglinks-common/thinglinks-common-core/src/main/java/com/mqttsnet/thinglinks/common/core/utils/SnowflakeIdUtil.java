package com.mqttsnet.thinglinks.common.core.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @program: thinglinks-util-pro
 * @description: SnowflakeIdUtil 是一个基于雪花算法的唯一 ID 生成工具类。 雪花算法可以保证生成的 ID 全局唯一且趋势递增。
 * @packagename: com.mqttsnet.basic.utils
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-03 13:15
 **/
public class SnowflakeIdUtil {

    private static final SnowflakeIdUtil INSTANCE = new SnowflakeIdUtil();

    // 开始时间戳 (2022-01-01)
    private static final long START_TIMESTAMP = 1672444800000L;

    // 工作器 ID 和数据中心 ID 的位数
    private static final int WORKER_ID_BITS = 5;
    private static final int DATACENTER_ID_BITS = 5;

    // 序列号的位数
    private static final int SEQUENCE_BITS = 12;

    // 工作器 ID 和数据中心 ID 的最大值
    private static final int MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);
    private static final int MAX_DATACENTER_ID = ~(-1 << DATACENTER_ID_BITS);

    // 工作器 ID、数据中心 ID 和序列号的位移
    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final int DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final int TIMESTAMP_SHIFT = DATACENTER_ID_SHIFT + DATACENTER_ID_BITS;

    // 序列号的掩码
    private static final int SEQUENCE_MASK = ~(-1 << SEQUENCE_BITS);

    // 工作器 ID 和数据中心 ID
    private final int workerId;
    private final int datacenterId;

    // 序列号和上次生成 ID 的时间戳
    private int sequence = 0;
    private long lastTimestamp = -1L;

    /**
     * 创建一个新的 SnowflakeIdUtil 实例。
     */
    public SnowflakeIdUtil() {
        this.workerId = generateWorkerId();
        this.datacenterId = generateDatacenterId();
    }


    /**
     * 生成一个新的唯一 ID，默认为 16 位。
     *
     * @return 一个不重复的十进制字符串
     */
    public static String nextId() {
        return INSTANCE.nextId(16);
    }

    /**
     * 生成一个新的唯一 ID。
     *
     * @param numDigits 生成的 ID 的位数，最少为 16 位
     * @return 一个不重复的十进制字符串
     */
    public synchronized String nextId(int numDigits) {
        if (numDigits < 16) {
            throw new IllegalArgumentException("numDigits must be at least 16");
        }

        long id = nextLongId();
        String idString = String.format("%0" + numDigits + "d", id % (long) Math.pow(10, numDigits));
        return idString;
    }

    /**
     * 生成一个新的唯一长整型 ID。
     *
     * @return 一个不重复的长整型值
     */
    public synchronized long nextLongId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id.");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitUntilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        long id = ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;

        return id;
    }

    /**
     * 等待直到下一个毫秒。
     *
     * @param lastTimestamp 上次生成 ID 的时间戳
     * @return 当前时间戳
     */
    private long waitUntilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 生成工作器 ID。
     *
     * @return 一个介于 0 和最大工作器 ID 之间的整数
     */
    private int generateWorkerId() {
        return ThreadLocalRandom.current().nextInt(MAX_WORKER_ID + 1);
    }

    /**
     * 生成数据中心 ID。
     *
     * @return 一个介于 0 和最大数据中心 ID 之间的整数
     */
    private int generateDatacenterId() {
        return ThreadLocalRandom.current().nextInt(MAX_DATACENTER_ID + 1);
    }
}
