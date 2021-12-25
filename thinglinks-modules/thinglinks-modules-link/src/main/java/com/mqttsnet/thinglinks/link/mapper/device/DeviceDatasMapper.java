package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceDatas;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/26$ 0:27$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/26$ 0:27$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
@Mapper
public interface DeviceDatasMapper {
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
    int insert(DeviceDatas record);

    int insertOrUpdate(DeviceDatas record);

    int insertOrUpdateSelective(DeviceDatas record);

    int insertOrUpdateWithBLOBs(DeviceDatas record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(DeviceDatas record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    DeviceDatas selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DeviceDatas record);

    int updateByPrimaryKeyWithBLOBs(DeviceDatas record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DeviceDatas record);

    int updateBatch(List<DeviceDatas> list);

    int updateBatchSelective(List<DeviceDatas> list);

    int batchInsert(@Param("list") List<DeviceDatas> list);
}