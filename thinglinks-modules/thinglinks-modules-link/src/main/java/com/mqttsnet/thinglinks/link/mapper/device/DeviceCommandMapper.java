package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceCommand;
import org.apache.ibatis.annotations.Mapper;

/**
 * -----------------------------------------------------------------------------
 * File Name: DeviceCommandMapper
 * -----------------------------------------------------------------------------
 * Description:
 * ${DESCRIPTION}
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/4/17       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email ${EMAIL}
 * @date 2024/4/17 22:02
 */

@Mapper
public interface DeviceCommandMapper {
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
    int insert(DeviceCommand record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(DeviceCommand record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    DeviceCommand selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DeviceCommand record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DeviceCommand record);
}