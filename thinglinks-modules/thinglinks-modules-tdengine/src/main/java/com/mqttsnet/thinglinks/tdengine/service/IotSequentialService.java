package com.mqttsnet.thinglinks.tdengine.service;

import com.mqttsnet.thinglinks.tdengine.api.domain.IotSequential;

import java.util.List;

/**
 * @program: thinglinks
 * @description:
 * @packagename: com.mqttsnet.thinglinks.tdengine.service.impl
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-11-04 10:50
 **/
public interface IotSequentialService {

    public IotSequential selectByTime(String startTime);

    public List<IotSequential> getList(IotSequential iotSequential);


    public int save(IotSequential iotSequential);
}
