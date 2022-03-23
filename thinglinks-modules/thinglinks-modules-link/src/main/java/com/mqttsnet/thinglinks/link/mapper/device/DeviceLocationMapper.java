package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/3/23$ 17:55$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/3/23$ 17:55$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Mapper
public interface DeviceLocationMapper {
    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(DeviceLocation record);

    int insertOrUpdate(DeviceLocation record);

    int insertOrUpdateSelective(DeviceLocation record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(DeviceLocation record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    DeviceLocation selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DeviceLocation record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DeviceLocation record);

    int updateBatch(List<DeviceLocation> list);

    int batchInsert(@Param("list") List<DeviceLocation> list);

    int updateBatchSelective(List<DeviceLocation> list);
}