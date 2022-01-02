package com.mqttsnet.thinglinks.monitor.util.staticvar;

import com.mqttsnet.thinglinks.monitor.api.domain.MailSet;

/**
 * @ClassName:StaticKeys.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: StaticKeys.java
 */
public class StaticKeys {

    public static final String SEARCH_START_TIME = "startTime";//日期查询开始时间条件

    public static final String SEARCH_END_TIME = "endTime";//日期查询结束时间条件

    // 日志失败标记
    public static final String LOG_ERROR = "1";

    //不发送报警短信邮件标识
    public static final String NO_SEND_WARN = "false";

    public static String SPLIT_BR = "</br>";//换行标识

    public static String DOWN_STATE = "2";

    public static MailSet mailSet = null;

}
