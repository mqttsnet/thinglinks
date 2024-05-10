package com.mqttsnet.thinglinks.link.service.device;

import com.mqttsnet.thinglinks.link.api.domain.deviceInfo.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.api.domain.deviceInfo.model.DeviceInfoParams;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.TopoAddSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.TopoUpdateSubDeviceStatusParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoDeviceOperationResultVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: 子设备档案接口
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/25$ 12:44$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/25$ 12:44$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
public interface DeviceInfoService {


    int deleteByPrimaryKey(Long id);

    int insert(DeviceInfo record);

    int insertOrUpdate(DeviceInfo record);

    int insertOrUpdateSelective(DeviceInfo record);

    int insertSelective(DeviceInfo record);

    DeviceInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceInfo record);

    int updateByPrimaryKey(DeviceInfo record);

    int updateBatch(List<DeviceInfo> list);

    int batchInsert(List<DeviceInfo> list);



	int deleteByDeviceId(String deviceId);



	DeviceInfo findOneByDeviceId(String deviceId);

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
     * @param deviceInfoParams
     * @return 结果
     */
    public int insertDeviceInfo(DeviceInfoParams deviceInfoParams);

    /**
     * 修改子设备管理
     *
     * @param deviceInfoParams 子设备管理
     * @return 结果
     */
    public int updateDeviceInfo(DeviceInfoParams deviceInfoParams);

    /**
     * 批量删除子设备管理
     *
     * @param ids 需要删除的子设备管理主键集合
     * @return 结果
     */
    public int deleteDeviceInfoByIds(Long[] ids);

    /**
     * 删除子设备管理信息
     *
     * @param id 子设备管理主键
     * @return 结果
     */
    public int deleteDeviceInfoById(Long id);

    /**
     * 查询子设备影子数据
     *
     * @param ids 需要查询的子设备id
     * @param startTime 开始时间 格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间 格式：yyyy-MM-dd HH:mm:ss
     * @return 子设备影子数据
     */
    public Map<String, List<Map<String, Object>>> getDeviceInfoShadow(String ids, String startTime, String endTime);



	List<DeviceInfo> findAllByIdInAndStatus(Collection<Long> idCollection, String status);



	List<DeviceInfo> findAllByIdIn(Collection<Long> idCollection);


    /**
     * 刷新子设备数据模型
     * @param idCollection
     * @return
     */
    public Boolean refreshDeviceInfoDataModel(Collection<Long> idCollection);



	List<DeviceInfo> findAllByStatus(String status);


    /**
     * MQTT协议下添加子设备
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 添加结果
     */
    TopoAddDeviceResultVO saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam);

    /**
     * HTTP协议下添加子设备
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 添加结果
     */
    TopoAddDeviceResultVO saveSubDeviceByHttp(TopoAddSubDeviceParam topoAddSubDeviceParam);


    /**
     * MQTT协议下更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新参数
     * @return {@link TopoDeviceOperationResultVO} 更新结果
     */
    TopoDeviceOperationResultVO updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);

    /**
     * Http协议下更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新参数
     * @return {@link TopoDeviceOperationResultVO} 更新结果
     */
    TopoDeviceOperationResultVO updateSubDeviceConnectStatusByHttp(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);

    /**
     * MQTT协议下删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 删除结果
     */
    TopoDeviceOperationResultVO deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);


    /**
     * Http协议下删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 删除结果
     */
    TopoDeviceOperationResultVO deleteSubDeviceByHttp(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);


}


