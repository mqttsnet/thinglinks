package com.mqttsnet.thinglinks.tdengine.mapper;

import com.mqttsnet.thinglinks.tdengine.api.domain.IotSequential;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IotSequentialMapper {

    List<IotSequential> getList(IotSequential iotSequential);

    IotSequential selectByTime(String startTime);

    int save(IotSequential iotSequential);

}
