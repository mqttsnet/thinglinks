package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.service.DashboardService;
import com.mqttsnet.thinglinks.monitor.util.staticvar.StaticKeys;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:DashboardService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 主面板信息管理
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    /**
     * 获取从今天开始，往前倒数天数的日期集合
     *
     * @return
     */
    @Override
    public List<String> getDateList() {
        int days = 7;
        List<String> dateList = new ArrayList<String>();
        String nowTime = DateUtils.getCurrentDateTime();
        String sevenDayBefore = DateUtils.getDateBefore(nowTime, days);
        for (int i = 0; i < days; i++) {
            sevenDayBefore = DateUtils.getDateBefore(nowTime, i);
            dateList.add(sevenDayBefore.substring(0, 10));
        }
        return dateList;
    }

    /**
     * 查看详细信息监控时候，组装日期查询条件
     *
     * @param params
     * @param date
     */
    @Override
    public void setDateParam(String date, Map<String, Object> params) {
        params.put(StaticKeys.SEARCH_START_TIME, date + " 00:00:00");
        params.put(StaticKeys.SEARCH_END_TIME, date + " 23:59:59");
    }

}
