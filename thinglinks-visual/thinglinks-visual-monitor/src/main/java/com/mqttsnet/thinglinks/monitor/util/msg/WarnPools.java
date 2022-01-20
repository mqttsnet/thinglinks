package com.mqttsnet.thinglinks.monitor.util.msg;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName:WarnPools.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: WarnPools.java
 */
public class WarnPools {

    /**
     * 存贮每天发送的内存告警信息map<用户ID+服务器IP，1>
     */
    public static Map<String, String> MEM_WARN_MAP = new HashMap<String, String>();

    public static void clearOldData() {
        MEM_WARN_MAP.clear();
    }

}
