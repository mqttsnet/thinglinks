package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 设备动作数据Mapper接口
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 13:20$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 13:20$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Mapper
public interface DeviceActionMapper {
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
    int insert(DeviceAction record);

    int insertOrUpdate(DeviceAction record);

    int insertOrUpdateSelective(DeviceAction record);

    int insertOrUpdateWithBLOBs(DeviceAction record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(DeviceAction record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    DeviceAction selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DeviceAction record);

    int updateByPrimaryKeyWithBLOBs(DeviceAction record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DeviceAction record);

    int updateBatch(List<DeviceAction> list);

    int updateBatchSelective(List<DeviceAction> list);

    int batchInsert(@Param("list") List<DeviceAction> list);

    /**
     * 查询设备动作数据
     *
     * @param id 设备动作数据主键
     * @return 设备动作数据
     */
    public DeviceAction selectDeviceActionById(Long id);

    /**
     * 查询设备动作数据列表
     *
     * @param deviceAction 设备动作数据
     * @return 设备动作数据集合
     */
    public List<DeviceAction> selectDeviceActionList(DeviceAction deviceAction);

    /**
     * 新增设备动作数据
     *
     * @param deviceAction 设备动作数据
     * @return 结果
     */
    public int insertDeviceAction(DeviceAction deviceAction);

    /**
     * 修改设备动作数据
     *
     * @param deviceAction 设备动作数据
     * @return 结果
     */
    public int updateDeviceAction(DeviceAction deviceAction);

    /**
     * 删除设备动作数据
     *
     * @param id 设备动作数据主键
     * @return 结果
     */
    public int deleteDeviceActionById(Long id);

    /**
     * 批量删除设备动作数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceActionByIds(Long[] ids);
}