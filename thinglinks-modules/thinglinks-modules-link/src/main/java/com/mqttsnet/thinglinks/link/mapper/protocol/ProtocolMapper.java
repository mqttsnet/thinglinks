package com.mqttsnet.thinglinks.link.mapper.protocol;

import com.mqttsnet.thinglinks.link.api.domain.protocol.Protocol;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.link.mapper.protocol
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-07-01 17:56
**/
@Mapper
public interface ProtocolMapper {
    /**
     * delete by primary key
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(Protocol record);

    int insertOrUpdate(Protocol record);

    int insertOrUpdateSelective(Protocol record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(Protocol record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    Protocol selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Protocol record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(Protocol record);

    int updateBatch(List<Protocol> list);

    int updateBatchSelective(List<Protocol> list);

    int batchInsert(@Param("list") List<Protocol> list);
}