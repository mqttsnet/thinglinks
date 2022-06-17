package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @Description: 设备Topic数据Mapper接口
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:23$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:23$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
@Mapper
public interface DeviceTopicMapper {
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
    int insert(DeviceTopic record);

    int insertOrUpdate(DeviceTopic record);

    int insertOrUpdateSelective(DeviceTopic record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(DeviceTopic record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    DeviceTopic selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DeviceTopic record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DeviceTopic record);

    int updateBatch(List<DeviceTopic> list);

    int updateBatchSelective(List<DeviceTopic> list);

    int batchInsert(@Param("list") List<DeviceTopic> list);

    /**
     * 查询设备Topic数据
     *
     * @param id 设备Topic数据主键
     * @return 设备Topic数据
     */
    public DeviceTopic selectDeviceTopicById(Long id);

    /**
     * 查询设备Topic数据列表
     *
     * @param deviceTopic 设备Topic数据
     * @return 设备Topic数据集合
     */
    public List<DeviceTopic> selectDeviceTopicList(DeviceTopic deviceTopic);

    /**
     * 新增设备Topic数据
     *
     * @param deviceTopic 设备Topic数据
     * @return 结果
     */
    public int insertDeviceTopic(DeviceTopic deviceTopic);

    /**
     * 修改设备Topic数据
     *
     * @param deviceTopic 设备Topic数据
     * @return 结果
     */
    public int updateDeviceTopic(DeviceTopic deviceTopic);

    /**
     * 删除设备Topic数据
     *
     * @param id 设备Topic数据主键
     * @return 结果
     */
    public int deleteDeviceTopicById(Long id);

    /**
     * 批量删除设备Topic数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceTopicByIds(Long[] ids);
}