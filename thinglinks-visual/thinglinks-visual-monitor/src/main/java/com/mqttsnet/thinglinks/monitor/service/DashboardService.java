package com.mqttsnet.thinglinks.monitor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:DashboardService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 主面板信息管理
 */
    public interface DashboardService {


        /**
     * 获取从今天开始，往前倒数天数的日期集合
     *
     * @return
     */
    List<String> getDateList();

    /**
     * 查看详细信息监控时候，组装日期查询条件
     *
     * @param params
     * @param date
     */
    void setDateParam(String date, Map<String, Object> params);

}
