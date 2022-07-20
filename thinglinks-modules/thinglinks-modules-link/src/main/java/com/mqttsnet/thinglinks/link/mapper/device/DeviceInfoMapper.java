package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
* @Description: java类作用描述
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/4/25$ 12:48$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/4/25$ 12:48$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
@Mapper
public interface DeviceInfoMapper {
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
    int insert(DeviceInfo record);

    int insertOrUpdate(DeviceInfo record);

    int insertOrUpdateSelective(DeviceInfo record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(DeviceInfo record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    DeviceInfo selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DeviceInfo record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DeviceInfo record);

    int updateBatch(List<DeviceInfo> list);

    int batchInsert(@Param("list") List<DeviceInfo> list);

    int deleteByDeviceId(@Param("deviceId")String deviceId);

    DeviceInfo findOneByDeviceId(@Param("deviceId")String deviceId);


    /**
     * 查询子设备管理
     *
     * @param id 子设备管理主键
     * @return 子设备管理
     */
    public DeviceInfo selectDeviceInfoById(Long id);

    /**
     * 查询子设备管理列表
     *
     * @param deviceInfo 子设备管理
     * @return 子设备管理集合
     */
    public List<DeviceInfo> selectDeviceInfoList(DeviceInfo deviceInfo);

    /**
     * 新增子设备管理
     *
     * @param deviceInfo 子设备管理
     * @return 结果
     */
    public int insertDeviceInfo(DeviceInfo deviceInfo);

    /**
     * 修改子设备管理
     *
     * @param deviceInfo 子设备管理
     * @return 结果
     */
    public int updateDeviceInfo(DeviceInfo deviceInfo);

    /**
     * 删除子设备管理
     *
     * @param id 子设备管理主键
     * @return 结果
     */
    public int deleteDeviceInfoById(Long id);

    /**
     * 批量删除子设备管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceInfoByIds(Long[] ids);

    List<DeviceInfo> findAllByIdInAndStatus(@Param("idCollection")Collection<Long> idCollection,@Param("status")String status);


    List<DeviceInfo> findAllByIdIn(@Param("idCollection")Collection<Long> idCollection);





}