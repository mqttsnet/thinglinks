package net.mqtts.link.mapper.device;

import java.util.List;
import net.mqtts.link.domain.device.MqttsDeviceAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**

* @Description:    java类作用描述

* @Author:         ShiHuan Sun

* @E-mail:          13733918655@163.com

* @Website:         http://mqtts.net

* @CreateDate:     2021/11/18$ 9:41$

* @UpdateUser:     ShiHuan Sun

* @UpdateDate:     2021/11/18$ 9:41$

* @UpdateRemark:   修改内容

* @Version:        1.0

*/
@Mapper
public interface MqttsDeviceActionMapper {
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
    int insert(MqttsDeviceAction record);

    int insertOrUpdate(MqttsDeviceAction record);

    int insertOrUpdateSelective(MqttsDeviceAction record);

    int insertOrUpdateWithBLOBs(MqttsDeviceAction record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(MqttsDeviceAction record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    MqttsDeviceAction selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(MqttsDeviceAction record);

    int updateByPrimaryKeyWithBLOBs(MqttsDeviceAction record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(MqttsDeviceAction record);

    int updateBatch(List<MqttsDeviceAction> list);

    int updateBatchSelective(List<MqttsDeviceAction> list);

    int batchInsert(@Param("list") List<MqttsDeviceAction> list);
}