package com.mqttsnet.thinglinks.link.service.device.impl;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceInfoMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 子设备档案接口实现
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/25$ 12:44$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/25$ 12:44$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Service
@Slf4j
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Resource
    private DeviceInfoMapper deviceInfoMapper;
    @Autowired
    private DeviceService deviceService;
    @Resource
    private RemoteTdEngineService remoteTdEngineService;

    @Value("${spring.datasource.dynamic.datasource.master.dbName:thinglinks}")
    private String dataBaseName;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceInfo record) {
        return deviceInfoMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(DeviceInfo record) {
        return deviceInfoMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(DeviceInfo record) {
        return deviceInfoMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(DeviceInfo record) {
        return deviceInfoMapper.insertSelective(record);
    }

    @Override
    public DeviceInfo selectByPrimaryKey(Long id) {
        return deviceInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceInfo record) {
        return deviceInfoMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceInfo record) {
        return deviceInfoMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<DeviceInfo> list) {
        return deviceInfoMapper.updateBatch(list);
    }

    @Override
    public int batchInsert(List<DeviceInfo> list) {
        return deviceInfoMapper.batchInsert(list);
    }

	@Override
	public int deleteByDeviceId(String deviceId){
		 return deviceInfoMapper.deleteByDeviceId(deviceId);
	}

	@Override
	public DeviceInfo findOneByDeviceId(String deviceId){
		 return deviceInfoMapper.findOneByDeviceId(deviceId);
	}

    /**
     * 查询子设备管理
     *
     * @param id 子设备管理主键
     * @return 子设备管理
     */
    @Override
    public DeviceInfo selectDeviceInfoById(Long id)
    {
        return deviceInfoMapper.selectDeviceInfoById(id);
    }

    /**
     * 查询子设备管理列表
     *
     * @param deviceInfo 子设备管理
     * @return 子设备管理
     */
    @Override
    public List<DeviceInfo> selectDeviceInfoList(DeviceInfo deviceInfo)
    {
        List<DeviceInfo> deviceInfoList = deviceInfoMapper.selectDeviceInfoList(deviceInfo);
        deviceInfoList.forEach(deviceInfo1 -> {
            Device oneById = deviceService.findOneById(deviceInfo1.getDId());
            deviceInfo1.setEdgeDevicesIdentification(StringUtils.isNotNull(oneById)?oneById.getDeviceIdentification():"");
        });
        return deviceInfoList;
    }

    /**
     * 新增子设备管理
     *
     * @param deviceInfo 子设备管理
     * @return 结果
     */
    @Override
    public int insertDeviceInfo(DeviceInfo deviceInfo)
    {
        deviceInfo.setCreateTime(DateUtils.dateToLocalDateTime(DateUtils.getNowDate()));
        return deviceInfoMapper.insertDeviceInfo(deviceInfo);
    }

    /**
     * 修改子设备管理
     *
     * @param deviceInfo 子设备管理
     * @return 结果
     */
    @Override
    public int updateDeviceInfo(DeviceInfo deviceInfo)
    {
        deviceInfo.setUpdateTime(DateUtils.dateToLocalDateTime(DateUtils.getNowDate()));
        return deviceInfoMapper.updateDeviceInfo(deviceInfo);
    }

    /**
     * 批量删除子设备管理
     *
     * @param ids 需要删除的子设备管理主键
     * @return 结果
     */
    @Override
    public int deleteDeviceInfoByIds(Long[] ids)
    {
        return deviceInfoMapper.deleteDeviceInfoByIds(ids);
    }

    /**
     * 删除子设备管理信息
     *
     * @param id 子设备管理主键
     * @return 结果
     */
    @Override
    public int deleteDeviceInfoById(Long id)
    {
        return deviceInfoMapper.deleteDeviceInfoById(id);
    }

    /**
     * 查询子设备影子数据
     *
     * @param ids 需要查询的子设备id
     * @param startTime 开始时间 格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间 格式：yyyy-MM-dd HH:mm:ss
     * @return 子设备影子数据
     */
    @Override
    public Map<String, List<Map<String, Object>>> getDeviceInfoShadow(String ids,String startTime,String endTime) {
        List<Long> idCollection = Arrays.stream(ids.split(",")).mapToLong(Long::parseLong).boxed().collect(Collectors.toList());
        List<DeviceInfo> deviceInfos = deviceInfoMapper.findAllByIdInAndStatus(idCollection, "0");
        if (StringUtils.isNull(deviceInfos)) {
            log.error("查询子设备影子数据失败，子设备不存在");
            return null;
        }
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        deviceInfos.forEach(deviceInfo -> {
            if (StringUtils.isNull(deviceInfo.getShadowTableName())) {
                log.error("查询子设备影子数据失败，子设备影子表名为空");
                return;
            }
            List<String> shadowTableNameCollect = Stream.of(deviceInfo.getShadowTableName().split(",")).collect(Collectors.toList());
            shadowTableNameCollect.forEach(shadowTableName -> {
                SelectDto selectDto = new SelectDto();
                selectDto.setDataBaseName(dataBaseName);
                selectDto.setTableName(shadowTableName);
                if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
                    selectDto.setFieldName("ts");
                    selectDto.setStartTime(DateUtils.localDateTime2Millis(DateUtils.dateToLocalDateTime(DateUtils.strToDate(startTime))));
                    selectDto.setEndTime(DateUtils.localDateTime2Millis(DateUtils.dateToLocalDateTime(DateUtils.strToDate(endTime))));
                    R<?> dataByTimestamp = remoteTdEngineService.getDataByTimestamp(selectDto);
                    if (StringUtils.isNull(dataByTimestamp)) {
                        log.error("查询子设备影子数据失败，子设备影子数据不存在");
                    }else {
                        map.put(shadowTableName, (List<Map<String, Object>>) dataByTimestamp.getData());
                        log.info("查询子设备影子数据成功，子设备影子数据：{}", dataByTimestamp.getData());

                    }
                }else{
                    R<?> lastData = remoteTdEngineService.getLastData(selectDto);
                    if (StringUtils.isNull(lastData)) {
                        log.error("查询子设备影子数据失败，子设备影子数据不存在");
                    }else {
                        map.put(shadowTableName, (List<Map<String, Object>>) lastData.getData());
                        log.info("查询子设备影子数据成功，子设备影子数据：{}", lastData.getData());

                    }
                }

            });
        });
        return map;
    }

	@Override
	public List<DeviceInfo> findAllByIdInAndStatus(Collection<Long> idCollection, String status){
		 return deviceInfoMapper.findAllByIdInAndStatus(idCollection,status);
	}





}


