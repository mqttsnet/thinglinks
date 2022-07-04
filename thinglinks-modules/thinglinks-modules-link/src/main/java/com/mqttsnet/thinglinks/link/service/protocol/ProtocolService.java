package com.mqttsnet.thinglinks.link.service.protocol;

import com.mqttsnet.thinglinks.link.api.domain.protocol.Protocol;

import java.util.List;
    /**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.link.service.protocol
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-07-01 17:56
**/
public interface ProtocolService{


    int deleteByPrimaryKey(Long id);

    int insert(Protocol record);

    int insertOrUpdate(Protocol record);

    int insertOrUpdateSelective(Protocol record);

    int insertSelective(Protocol record);

    Protocol selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Protocol record);

    int updateByPrimaryKey(Protocol record);

    int updateBatch(List<Protocol> list);

    int updateBatchSelective(List<Protocol> list);

    int batchInsert(List<Protocol> list);

}
