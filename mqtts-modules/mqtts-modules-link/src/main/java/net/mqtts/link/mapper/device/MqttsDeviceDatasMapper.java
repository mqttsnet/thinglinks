package net.mqtts.link.mapper.device;

import java.util.List;

import net.mqtts.link.api.domain.MqttsDeviceDatas;
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
public interface MqttsDeviceDatasMapper {
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
    int insert(MqttsDeviceDatas record);

    int insertOrUpdate(MqttsDeviceDatas record);

    int insertOrUpdateSelective(MqttsDeviceDatas record);

    int insertOrUpdateWithBLOBs(MqttsDeviceDatas record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(MqttsDeviceDatas record);

    List<MqttsDeviceDatas> selectMqttsDeviceDatasList(MqttsDeviceDatas record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    MqttsDeviceDatas selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(MqttsDeviceDatas record);

    int updateByPrimaryKeyWithBLOBs(MqttsDeviceDatas record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(MqttsDeviceDatas record);

    int updateBatch(List<MqttsDeviceDatas> list);

    int updateBatchSelective(List<MqttsDeviceDatas> list);

    int batchInsert(@Param("list") List<MqttsDeviceDatas> list);

    int deleteMqttsDeviceDatasByIds(Long[] ids);
}