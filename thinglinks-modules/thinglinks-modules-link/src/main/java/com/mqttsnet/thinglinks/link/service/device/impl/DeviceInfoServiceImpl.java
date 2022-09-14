package com.mqttsnet.thinglinks.link.service.device.impl;

import com.alibaba.fastjson.JSON;
import com.mqttsnet.thinglinks.broker.api.RemotePublishActorService;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatus;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.deviceInfo.DeviceInfo;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.deviceInfo.DeviceInfoParams;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.common.core.utils.tdengine.TdUtils;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceInfoMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductServicesService productServicesService;
    @Resource
    private RemoteTdEngineService remoteTdEngineService;

    @Resource
    private RemotePublishActorService remotePublishActorService;

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
        DeviceInfo deviceInfo = deviceInfoMapper.selectDeviceInfoById(id);
        if (StringUtils.isNotNull(deviceInfo)) {
            Device oneById = deviceService.findOneById(deviceInfo.getDid());
            deviceInfo.setEdgeDevicesIdentification(StringUtils.isNotNull(oneById)?oneById.getDeviceIdentification():"");
        }
        return deviceInfo;
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
            Device oneById = deviceService.findOneById(deviceInfo1.getDid());
            deviceInfo1.setEdgeDevicesIdentification(StringUtils.isNotNull(oneById)?oneById.getDeviceIdentification():"");
        });
        return deviceInfoList;
    }

    /**
     * 新增子设备管理
     *
     * @param deviceInfoParams 子设备管理
     * @return 结果
     */
    @Override
    public int insertDeviceInfo(DeviceInfoParams deviceInfoParams)
    {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.convertEntity(deviceInfoParams);
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        deviceInfo.setCreateBy(sysUser.getUserName());
        deviceInfo.setCreateTime(DateUtils.dateToLocalDateTime(DateUtils.getNowDate()));
        deviceInfo.setDeviceId(UUID.getUUID());
        deviceInfo.setConnectStatus(DeviceConnectStatus.INIT.getValue());
        deviceInfo.setShadowEnable(true);
        return deviceInfoMapper.insertDeviceInfo(deviceInfo);
    }

    /**
     * 修改子设备管理
     *
     * @param deviceInfoParams 子设备管理
     * @return 结果
     */
    @Override
    public int updateDeviceInfo(DeviceInfoParams deviceInfoParams)
    {
        DeviceInfo deviceInfo = deviceInfoMapper.selectByPrimaryKey(deviceInfoParams.getId());
        deviceInfo.convertEntity(deviceInfoParams);
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        deviceInfo.setUpdateBy(sysUser.getUserName());
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
    public int deleteDeviceInfoByIds(Long[] ids) {
        AtomicReference<Integer> deleteCount = new AtomicReference<>(0);
        deviceInfoMapper.findAllByIdIn(Arrays.asList(ids)).forEach(deviceInfo -> {
            Map responseMaps = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList();
            responseMaps.put("mid", 1);
            responseMaps.put("statusCode", 0);
            responseMaps.put("statusDesc", "successful");
            responseMaps.put("data", dataList);
            final int deleteByDeviceIdCount = this.deleteByDeviceId(deviceInfo.getDeviceId());
            Map responseMap = new HashMap<>();
            if (deleteByDeviceIdCount > 0) {
                responseMap.put("statusCode", 0);
                responseMap.put("statusDesc", "successful");
                deleteCount.getAndSet(deleteCount.get() + 1);
            } else {
                responseMap.put("statusCode", 1);
                responseMap.put("statusDesc", "abortive");
                log.error("Delete DeviceInfo Exception");
            }
            responseMap.put("deviceId", deviceInfo.getDeviceId());
            dataList.add(responseMap);
            Device device = deviceService.findOneById(deviceInfo.getDid());
            if (StringUtils.isNotNull(device)) {
                final Map<String, Object> param = new HashMap<>();
                param.put("topic", "/v1/devices/"+device.getDeviceIdentification()+"/topo/deleteResponse");
                param.put("qos", 2);
                param.put("retain", false);
                param.put("message", JSON.toJSONString(responseMaps));
                remotePublishActorService.sendMessage(param);
            }
            responseMaps.clear();
        });
        return deleteCount.get();
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
        List<DeviceInfo> deviceInfos = deviceInfoMapper.findAllByIdInAndStatus(idCollection, Constants.ENABLE);
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

	@Override
	public List<DeviceInfo> findAllByIdIn(Collection<Long> idCollection){
		 return deviceInfoMapper.findAllByIdIn(idCollection);
	}

    /**
     * 刷新子设备数据模型
     * @param idCollection
     * @return
     */
    @Override
    public Boolean refreshDeviceInfoDataModel(Collection<Long> idCollection) {
        List<DeviceInfo> allByIdInAndStatus = null;
        if (StringUtils.isNotEmpty(idCollection)) {
            allByIdInAndStatus = deviceInfoMapper.findAllByIdInAndStatus(idCollection, Constants.ENABLE);
        }else {
            allByIdInAndStatus = deviceInfoMapper.findAllByStatus(Constants.ENABLE);
        }
        allByIdInAndStatus.forEach(item->{
            final Device device = deviceService.findOneById(item.getDid());
            if (StringUtils.isNull(device)) {
                log.error("刷新子设备数据模型失败，子设备不存在");
                return;
            }
            final Product product = productService.findOneByProductIdentificationAndProtocolType(device.getProductIdentification(), device.getProtocolType());
            if (StringUtils.isNull(product)) {
                log.error("刷新子设备数据模型失败，子设备产品不存在");
                return;
            }
            StringBuilder shadowTableNameBuilder = new StringBuilder();
            // 新增设备管理成功后，创建TD普通表
            List<ProductServices> allByProductIdAndStatus = productServicesService.findAllByProductIdAndStatus(product.getId(), Constants.ENABLE);
            TableDto tableDto;
            for (ProductServices productServices : allByProductIdAndStatus) {
                tableDto = new TableDto();
                tableDto.setDataBaseName(dataBaseName);
                //超级表命名规则 : 产品类型_产品标识_服务名称
                String superTableName = TdUtils.getSuperTableName(product.getProductType(),product.getProductIdentification(),productServices.getServiceName());
                tableDto.setSuperTableName(superTableName);
                //子表命名规则 : 产品类型_产品标识_服务名称_设备标识（设备唯一标识）
                tableDto.setTableName(TdUtils.getSubTableName(superTableName,item.getDeviceId()));
                //Tag的处理
                List<Fields> tagsFieldValues = new ArrayList<>();
                Fields fields = new Fields();
                fields.setFieldValue(device.getDeviceIdentification());
                tagsFieldValues.add(fields);
                tableDto.setTagsFieldValues(tagsFieldValues);
                final R<?> ctResult = remoteTdEngineService.createTable(tableDto);
                if (ctResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                    shadowTableNameBuilder.append(tableDto.getTableName()).append(",");
                    log.info("Create SuperTable Success: " + ctResult.getMsg());
                } else {
                    log.error("Create SuperTable Exception: " + ctResult.getMsg());
                }
            }
            if (shadowTableNameBuilder.length() > 0) {
                item.setShadowTableName(shadowTableNameBuilder.substring(0, shadowTableNameBuilder.length() - 1));
            }
            shadowTableNameBuilder.replace(0, shadowTableNameBuilder.length(), "");
            item.setCreateBy(device.getCreateBy());
            deviceInfoMapper.updateByPrimaryKeySelective(item);
        });
        return true;
    }

	@Override
	public List<DeviceInfo> findAllByStatus(String status){
		 return deviceInfoMapper.findAllByStatus(status);
	}


}


