package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 1:42$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 1:42$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Mapper
public interface DeviceMapper {
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
    int insert(Device record);

    int insertOrUpdate(Device record);

    int insertOrUpdateSelective(Device record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(Device record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    Device selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Device record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(Device record);

    int updateBatch(List<Device> list);

    int updateBatchSelective(List<Device> list);

    int batchInsert(@Param("list") List<Device> list);

    /**
     * @return
     * @Author: ShiHuan Sun
     * @E-mail: 13733918655@163.com
     * @Description: 更新设备在线状态
     * @CreateDate: 2021/12/26 1:01
     * @Version: V1.0
     * @Param: updatedConnect_status 设备状态值
     * client_id 客户端ID
     */
    int updateConnectStatusByClientId(@Param("updatedConnectStatus")String updatedConnectStatus,@Param("clientId")String clientId);

    Device findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(@Param("clientId")String clientId,@Param("userName")String userName,@Param("password")String password,@Param("deviceStatus")String deviceStatus,@Param("protocolType")String protocolType);


    List<Device> findByAll(Device device);

    Device findOneById(@Param("id")Long id);

    /**
     * 查询设备管理
     *
     * @param id 设备管理主键
     * @return 设备管理
     */
    public Device selectDeviceById(Long id);

    /**
     * 查询设备管理列表
     *
     * @param device 设备管理
     * @return 设备管理集合
     */
    public List<Device> selectDeviceList(Device device);

    /**
     * 新增设备管理
     *
     * @param device 设备管理
     * @return 结果
     */
    public int insertDevice(Device device);

    /**
     * 修改设备管理
     *
     * @param device 设备管理
     * @return 结果
     */
    public int updateDevice(Device device);

    /**
     * 删除设备管理
     *
     * @param id 设备管理主键
     * @return 结果
     */
    public int deleteDeviceById(Long id);

    /**
     * 批量删除设备管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);

    Device findOneByClientId(@Param("clientId")String clientId);

    Device findOneByClientIdAndDeviceIdentification(@Param("clientId")String clientId,@Param("deviceIdentification")String deviceIdentification);

    Device findOneByDeviceIdentification(@Param("deviceIdentification")String deviceIdentification);

    Device findOneByClientIdOrderByDeviceIdentification(@Param("clientId")String clientId);

	Device findOneByClientIdOrDeviceIdentification(@Param("clientId")String clientId,@Param("deviceIdentification")String deviceIdentification);






}