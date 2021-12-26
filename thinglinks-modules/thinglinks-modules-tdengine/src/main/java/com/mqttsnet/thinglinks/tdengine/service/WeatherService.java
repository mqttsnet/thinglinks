package com.mqttsnet.thinglinks.tdengine.service;

import com.mqttsnet.thinglinks.tdengine.domain.Weather;

import java.util.List;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 21:52$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 21:52$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public interface WeatherService {

    int init();

    int count();

    List<Weather> query(Long limit, Long offset);

    int save(float temperature, float humidity);

    List<String> getSubTables();

    List<Weather> avg();

    Weather lastOne();
}
