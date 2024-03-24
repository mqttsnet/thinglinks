package com.mqttsnet.thinglinks.link.service.device.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.common.core.enums.DeviceType;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.tdengine.TdUtils;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.MqttProtocolTopoStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.deviceInfo.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.api.domain.deviceInfo.model.DeviceInfoParams;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoAddSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoUpdateSubDeviceStatusParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceInfoMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceActionService;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TableDTO;
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

    @Autowired
    private DeviceActionService deviceActionService;

    @Resource
    private RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi;

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
    public int deleteByDeviceId(String deviceId) {
        return deviceInfoMapper.deleteByDeviceId(deviceId);
    }

    @Override
    public DeviceInfo findOneByDeviceId(String deviceId) {
        return deviceInfoMapper.findOneByDeviceId(deviceId);
    }

    /**
     * 查询子设备管理
     *
     * @param id 子设备管理主键
     * @return 子设备管理
     */
    @Override
    public DeviceInfo selectDeviceInfoById(Long id) {
        DeviceInfo deviceInfo = deviceInfoMapper.selectDeviceInfoById(id);
        if (StringUtils.isNotNull(deviceInfo)) {
            Device oneById = deviceService.findOneById(deviceInfo.getDid());
            deviceInfo.setEdgeDevicesIdentification(StringUtils.isNotNull(oneById) ? oneById.getDeviceIdentification() : "");
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
    public List<DeviceInfo> selectDeviceInfoList(DeviceInfo deviceInfo) {
        List<DeviceInfo> deviceInfoList = deviceInfoMapper.selectDeviceInfoList(deviceInfo);
        deviceInfoList.forEach(deviceInfo1 -> {
            Device oneById = deviceService.findOneById(deviceInfo1.getDid());
            deviceInfo1.setEdgeDevicesIdentification(StringUtils.isNotNull(oneById) ? oneById.getDeviceIdentification() : "");
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
    public int insertDeviceInfo(DeviceInfoParams deviceInfoParams) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.convertEntity(deviceInfoParams);
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        deviceInfo.setCreateBy(sysUser.getUserName());
        deviceInfo.setDeviceId(UUID.getUUID());
        deviceInfo.setConnectStatus(DeviceConnectStatusEnum.INIT.getValue());
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
    public int updateDeviceInfo(DeviceInfoParams deviceInfoParams) {
        DeviceInfo deviceInfo = deviceInfoMapper.selectByPrimaryKey(deviceInfoParams.getId());
        deviceInfo.convertEntity(deviceInfoParams);
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        deviceInfo.setUpdateBy(sysUser.getUserName());
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
                param.put("topic", "/v1/devices/" + device.getDeviceIdentification() + "/topo/deleteResponse");
                param.put("qos", 2);
                param.put("retain", false);
                param.put("message", JSON.toJSONString(responseMaps));
//                remoteMqttBrokerOpenApi.sendMessage(param);
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
    public int deleteDeviceInfoById(Long id) {
        return deviceInfoMapper.deleteDeviceInfoById(id);
    }

    /**
     * 查询子设备影子数据
     *
     * @param ids       需要查询的子设备id
     * @param startTime 开始时间 格式：yyyy-MM-dd HH:mm:ss
     * @param endTime   结束时间 格式：yyyy-MM-dd HH:mm:ss
     * @return 子设备影子数据
     */
    @Override
    public Map<String, List<Map<String, Object>>> getDeviceInfoShadow(String ids, String startTime, String endTime) {
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
                    } else {
                        map.put(shadowTableName, (List<Map<String, Object>>) dataByTimestamp.getData());
                        log.info("查询子设备影子数据成功，子设备影子数据：{}", dataByTimestamp.getData());

                    }
                } else {
                    R<?> lastData = remoteTdEngineService.getLastData(selectDto);
                    if (StringUtils.isNull(lastData)) {
                        log.error("查询子设备影子数据失败，子设备影子数据不存在");
                    } else {
                        map.put(shadowTableName, (List<Map<String, Object>>) lastData.getData());
                        log.info("查询子设备影子数据成功，子设备影子数据：{}", lastData.getData());

                    }
                }

            });
        });
        return map;
    }

    @Override
    public List<DeviceInfo> findAllByIdInAndStatus(Collection<Long> idCollection, String status) {
        return deviceInfoMapper.findAllByIdInAndStatus(idCollection, status);
    }

    @Override
    public List<DeviceInfo> findAllByIdIn(Collection<Long> idCollection) {
        return deviceInfoMapper.findAllByIdIn(idCollection);
    }

    /**
     * 刷新子设备数据模型
     *
     * @param idCollection
     * @return
     */
    @Override
    public Boolean refreshDeviceInfoDataModel(Collection<Long> idCollection) {
        List<DeviceInfo> allByIdInAndStatus = null;
        if (StringUtils.isNotEmpty(idCollection)) {
            allByIdInAndStatus = deviceInfoMapper.findAllByIdInAndStatus(idCollection, Constants.ENABLE);
        } else {
            allByIdInAndStatus = deviceInfoMapper.findAllByStatus(Constants.ENABLE);
        }
        allByIdInAndStatus.forEach(item -> {
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
            List<ProductServices> allByProductIdAndStatus = productServicesService.findAllByProductIdentificationIdAndStatus(product.getProductIdentification(), Constants.ENABLE);
            TableDTO tableDto;
            for (ProductServices productServices : allByProductIdAndStatus) {
                tableDto = new TableDTO();
                tableDto.setDataBaseName(dataBaseName);
                //超级表命名规则 : 产品类型_产品标识_服务名称
                String superTableName = TdUtils.getSuperTableName(product.getProductType(), product.getProductIdentification(), productServices.getServiceName());
                tableDto.setSuperTableName(superTableName);
                //子表命名规则 : 产品类型_产品标识_服务名称_设备标识（设备唯一标识）
                tableDto.setTableName(TdUtils.getSubTableName(superTableName, item.getDeviceId()));
                //Tag的处理
                List<Fields> tagsFieldValues = new ArrayList<>();
                Fields fields = new Fields();
                fields.setFieldValue(device.getDeviceIdentification());
                tagsFieldValues.add(fields);
                tableDto.setTagsFieldValues(tagsFieldValues);
                final R<?> ctResult = remoteTdEngineService.createSubTable(tableDto);
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
    public List<DeviceInfo> findAllByStatus(String status) {
        return deviceInfoMapper.findAllByStatus(status);
    }


    /**
     * MQTT协议下添加子设备
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 添加结果
     */
    @Override
    public TopoAddDeviceResultVO saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return saveSubDevice(topoAddSubDeviceParam);
    }

    /**
     * HTTP协议下添加子设备
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 添加结果
     */
    @Override
    public TopoAddDeviceResultVO saveSubDeviceByHttp(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return saveSubDevice(topoAddSubDeviceParam);
    }


    /**
     * 添加网关子设备
     *
     * @param topoAddSubDeviceParam TopoAddDeviceParam，添加设备的参数信息
     * @return {@link TopoAddDeviceResultVO} 添加设备的结果信息
     */
    private TopoAddDeviceResultVO saveSubDevice(TopoAddSubDeviceParam topoAddSubDeviceParam) {

        // 根据网关ID查找网关设备
        Device gatewayDevice = deviceService.findOneByDeviceIdentification(topoAddSubDeviceParam.getGatewayIdentification());

        // 假设 gatewayDevice.getType() 方法返回设备类型，且 DeviceType.GATEWAY 代表网关设备类型
        MqttProtocolTopoStatusEnum statusEnum = MqttProtocolTopoStatusEnum.FAILURE;

        if (DeviceType.GATEWAY.getValue().equals(gatewayDevice.getDeviceType())) {
            statusEnum = MqttProtocolTopoStatusEnum.SUCCESS;
        } else {
            statusEnum = MqttProtocolTopoStatusEnum.FAILURE;
        }

        // 创建返回结果实例并设置状态码和状态描述
        TopoAddDeviceResultVO mqttTopoAddDeviceResultVO = TopoAddDeviceResultVO.builder()
                .statusCode(statusEnum.getValue())
                .statusDesc(statusEnum.getDesc())
                .build();

        // 创建一个设备列表用于存储处理结果
        List<TopoAddDeviceResultVO.DataItem> deviceList = new ArrayList<>();

        // 检查设备信息列表是否为空
        List<TopoAddSubDeviceParam.DeviceInfos> deviceInfos = topoAddSubDeviceParam.getDeviceInfos();
        if (!deviceInfos.isEmpty() && MqttProtocolTopoStatusEnum.SUCCESS.equals(statusEnum)) {
            // 遍历添加设备的参数信息列表
            for (TopoAddSubDeviceParam.DeviceInfos item : deviceInfos) {
                try {
                    // 创建数据项实例并验证设备参数
                    TopoAddDeviceResultVO.DataItem dataItem = new TopoAddDeviceResultVO.DataItem();
                    checkedTopoAddDeviceParam(item, dataItem);
                    // 将参数对象转换为设备信息对象并设置到数据项中
                    dataItem.setDeviceInfo(BeanUtil.toBean(item, TopoAddDeviceResultVO.DataItem.DeviceInfo.class, CopyOptions.create().ignoreError()));

                    // 如果设备参数验证不通过，添加到设备列表并继续下一次循环
                    if (!MqttProtocolTopoStatusEnum.SUCCESS.getValue().equals(dataItem.getStatusCode())) {
                        deviceList.add(dataItem);
                        continue;
                    }

                    // 转换并保存子设备信息
                    DeviceInfoParams deviceInfoParams = conversionDeviceBySaveSubDevice(gatewayDevice, item);
                    int insertCount = this.insertDeviceInfo(deviceInfoParams);

                    // TODO 存储子设备经纬度信息
                    /*DeviceLocationPageQuery deviceLocationPageQuery = new DeviceLocationPageQuery();
                    deviceLocationPageQuery.setDeviceIdentification(gatewayDevice.getDeviceIdentification());

                    List<DeviceLocationResultVO> deviceLocationResultVOList = deviceLocationService.getDeviceLocationResultVOList(deviceLocationPageQuery);

                    Optional.ofNullable(deviceLocationResultVOList)
                            .filter(list -> !list.isEmpty())
                            .map(list -> list.get(0))
                            .map(deviceLocationResultVO -> BeanPlusUtil.toBeanIgnoreError(deviceLocationResultVO, DeviceLocationSaveVO.class))
                            .ifPresent(deviceLocationSaveVO -> {
                                deviceLocationSaveVO.setDeviceIdentification(subDeviceDO.getDeviceIdentification());
                                deviceLocationService.saveDeviceLocation(deviceLocationSaveVO);
                            });*/

                    // 设置平台生成的设备标识
                    dataItem.getDeviceInfo().setDeviceId(deviceInfoParams.getDeviceId());

                    // 根据保存结果设置状态码和状态描述
                    MqttProtocolTopoStatusEnum saveStatusEnum = insertCount > 0 ? MqttProtocolTopoStatusEnum.SUCCESS : MqttProtocolTopoStatusEnum.FAILURE;
                    dataItem.setStatusCode(saveStatusEnum.getValue())
                            .setStatusDesc(saveStatusEnum.getDesc());

                    // 添加数据项到设备列表
                    deviceList.add(dataItem);
                } catch (Exception e) {
                    // 处理异常情况，将异常信息设置到数据项中
                    TopoAddDeviceResultVO.DataItem dataItem = new TopoAddDeviceResultVO.DataItem();
                    dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                            .setStatusDesc(e.getMessage());
                    deviceList.add(dataItem);
                }
            }
        }

        // 将设备列表设置到返回结果实例中
        mqttTopoAddDeviceResultVO.setData(deviceList);
        return mqttTopoAddDeviceResultVO;
    }

    /**
     * 添加网关子设备转换DO Device
     *
     * @param deviceDO 网关设备信息
     * @param item     子设备信息
     * @return {@link DeviceInfoParams} 子设备信息
     */
    private DeviceInfoParams conversionDeviceBySaveSubDevice(Device deviceDO, TopoAddSubDeviceParam.DeviceInfos item) {
        DeviceInfoParams deviceInfoParams = new DeviceInfoParams();
        deviceInfoParams.setAppId(deviceDO.getAppId());
        deviceInfoParams.setDid(deviceDO.getId());
        deviceInfoParams.setDeviceId(SnowflakeIdUtil.nextId());
        deviceInfoParams.setNodeId(item.getNodeId());
        deviceInfoParams.setNodeName(item.getName());
        deviceInfoParams.setManufacturerId(item.getManufacturerId());
        deviceInfoParams.setModel(item.getModel());
        deviceInfoParams.setDescription(item.getDescription());
        deviceInfoParams.setConnectStatus(DeviceConnectStatusEnum.INIT.getValue());
        deviceInfoParams.setShadowEnable(true);
        return deviceInfoParams;
    }

    /**
     * 验证Topo添加设备参数，并设置对应的状态码和状态描述。
     *
     * @param item     TopoAddDeviceParam.DeviceInfos，添加设备的参数信息
     * @param dataItem MqttTopoAddDeviceResultVO.DataItem，用于存储设备添加结果的数据项
     */
    private void checkedTopoAddDeviceParam(TopoAddSubDeviceParam.DeviceInfos item,
                                           TopoAddDeviceResultVO.DataItem dataItem) {
        // 根据设备标识查找子设备
        Device subDevice = deviceService.findOneByDeviceIdentification(item.getNodeId());
        // 用于拼接错误消息的StringBuilder
        StringBuilder errorMessage = new StringBuilder();

        // 检查各参数是否为空，并将错误消息追加到StringBuilder中
        appendErrorMessageIfEmpty(errorMessage, item.getName(), "name is null; ");
        appendErrorMessageIfEmpty(errorMessage, item.getNodeId(), "nodeId is null; ");
        appendErrorMessageIfEmpty(errorMessage, item.getManufacturerId(), "manufacturerId is null; ");
        appendErrorMessageIfEmpty(errorMessage, item.getModel(), "model is null; ");

        // 检查设备节点ID是否已经存在
        if (subDevice != null) {
            errorMessage.append("nodeId is exist; ");
        }

        // 根据错误消息长度判断是否有错误，并设置相应的状态码和状态描述
        if (errorMessage.length() > 0) {
            dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                    .setStatusDesc(errorMessage.toString());
        } else {
            dataItem.setStatusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                    .setStatusDesc(MqttProtocolTopoStatusEnum.SUCCESS.getDesc());
        }
    }

    /**
     * 检查参数值是否为空，如果为空，将错误消息追加到StringBuilder中。
     *
     * @param errorMessage StringBuilder，用于拼接错误消息
     * @param value        CharSequence，待检查的参数值
     * @param message      String，错误消息
     */
    private void appendErrorMessageIfEmpty(StringBuilder errorMessage, CharSequence value, String message) {
        if (CharSequenceUtil.isEmpty(value)) {
            errorMessage.append(message);
        }
    }


    /**
     * MQTT协议下更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新参数
     * @return {@link TopoDeviceOperationResultVO} 更新结果
     */
    @Override
    public TopoDeviceOperationResultVO updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return updateSubDeviceConnectStatus(topoUpdateSubDeviceStatusParam);
    }

    /**
     * Http协议下更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新参数
     * @return {@link TopoDeviceOperationResultVO} 更新结果
     */
    @Override
    public TopoDeviceOperationResultVO updateSubDeviceConnectStatusByHttp(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return updateSubDeviceConnectStatus(topoUpdateSubDeviceStatusParam);
    }

    /**
     * Updates the connection status of sub-devices and logs their actions.
     *
     * @param topoUpdateSubDeviceStatusParam the parameters containing device statuses to be updated
     * @return an object containing the operation results
     */
    private TopoDeviceOperationResultVO updateSubDeviceConnectStatus(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        List<TopoDeviceOperationResultVO.OperationRsp> operationRsps = topoUpdateSubDeviceStatusParam.getDeviceStatuses().stream()
                .map(this::processSubDeviceStatus)
                .collect(Collectors.toList());

        return TopoDeviceOperationResultVO.builder()
                .statusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                .statusDesc(MqttProtocolTopoStatusEnum.SUCCESS.getDesc())
                .data(operationRsps)
                .build();
    }

    /**
     * Processes the status of a single sub-device, updates it, and logs the action.
     *
     * @param subDeviceStatus the status of the sub-device to be processed
     * @return an operation response object for the sub-device
     */
    private TopoDeviceOperationResultVO.OperationRsp processSubDeviceStatus(TopoUpdateSubDeviceStatusParam.DeviceStatus subDeviceStatus) {
        DeviceInfo deviceInfo = this.findOneByDeviceId(subDeviceStatus.getDeviceId());
        TopoDeviceOperationResultVO.OperationRsp dataItem = new TopoDeviceOperationResultVO.OperationRsp()
                .setDeviceId(subDeviceStatus.getDeviceId());

        if (deviceInfo != null) {
            deviceInfo.setConnectStatus(subDeviceStatus.getStatus().getValue());
            int updateCount = deviceInfoMapper.updateByPrimaryKey(deviceInfo);
            recordDeviceAction(deviceInfo, subDeviceStatus.getStatus());

            MqttProtocolTopoStatusEnum updateStatusEnum = updateCount > 0 ? MqttProtocolTopoStatusEnum.SUCCESS : MqttProtocolTopoStatusEnum.FAILURE;
            dataItem.setStatusCode(updateStatusEnum.getValue())
                    .setStatusDesc(updateStatusEnum.getDesc());
        } else {
            dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                    .setStatusDesc(MqttProtocolTopoStatusEnum.FAILURE.getDesc());
        }

        return dataItem;
    }

    /**
     * Records an action taken on a device and saves it to the database.
     *
     * @param deviceInfo    the device information
     * @param connectStatus the new connection status of the device
     */
    private void recordDeviceAction(DeviceInfo deviceInfo, DeviceConnectStatusEnum connectStatus) {
        // 构建设备动作描述和类型
        String describable = getDescriptionForStatus(connectStatus);
        DeviceActionTypeEnum actionType = getActionTypeForStatus(connectStatus);

        // 构建并保存设备动作记录
        DeviceAction deviceActionSaveVO = new DeviceAction();
        deviceActionSaveVO.setDeviceIdentification(deviceInfo.getDeviceId());
        deviceActionSaveVO.setActionType(actionType.getAction());
        deviceActionSaveVO.setMessage(describable);
        deviceActionSaveVO.setStatus(DeviceActionStatusEnum.SUCCESSFUL.getValue());

        try {
            int insertDeviceActionCount = deviceActionService.insertDeviceAction(deviceActionSaveVO);
            log.info("Saved device action for device ID: {}, insert count: {}", deviceInfo.getDeviceId(), insertDeviceActionCount);
        } catch (Exception e) {
            log.error("Failed to save device action for device ID: {}", deviceInfo.getDeviceId(), e);
        }
    }

    /**
     * Determines the action type based on the device connection status.
     *
     * @param status the device connection status
     * @return the corresponding action type
     */
    private DeviceActionTypeEnum getActionTypeForStatus(DeviceConnectStatusEnum status) {
        if (DeviceConnectStatusEnum.OFFLINE.equals(status)) {
            return DeviceActionTypeEnum.CLOSE;
        } else if (DeviceConnectStatusEnum.ONLINE.equals(status)) {
            return DeviceActionTypeEnum.CONNECT;
        } else {
            // Handle unexpected status here
            log.warn("Unexpected status: {}", status);
            return DeviceActionTypeEnum.UNKNOWN;
        }
    }

    /**
     * Gets a descriptive text based on the device connection status.
     *
     * @param status the device connection status
     * @return a string description for the status
     */
    private String getDescriptionForStatus(DeviceConnectStatusEnum status) {
        String desc = Optional.ofNullable(status)
                .map(DeviceConnectStatusEnum::getValue)
                .orElse("Unknown Status");

        return "The device connection status is updated to " + desc;
    }

    /**
     * MQTT协议下删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 删除结果
     */
    @Override
    public TopoDeviceOperationResultVO deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deleteSubDevice(topoDeleteSubDeviceParam);
    }

    /**
     * Http协议下删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 删除结果
     */
    @Override
    public TopoDeviceOperationResultVO deleteSubDeviceByHttp(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deleteSubDevice(topoDeleteSubDeviceParam);
    }

    private TopoDeviceOperationResultVO deleteSubDevice(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        // 创建一个操作结果列表用于存储处理结果
        List<TopoDeviceOperationResultVO.OperationRsp> operationResultList = new ArrayList<>();

        // 遍历子设备标识集合
        for (String deviceId : topoDeleteSubDeviceParam.getDeviceIds()) {
            // 根据子设备唯一标识查找设备
            DeviceInfo deviceInfo = this.findOneByDeviceId(deviceId);

            // 创建操作结果实例
            TopoDeviceOperationResultVO.OperationRsp operationRsp = new TopoDeviceOperationResultVO.OperationRsp()
                    .setDeviceId(deviceId);

            // 判断设备是否存在
            if (deviceInfo != null) {
                // 删除设备
                int deleteCount = deviceInfoMapper.deleteByDeviceId(deviceInfo.getDeviceId());

                // 根据删除结果设置状态码和状态描述
                MqttProtocolTopoStatusEnum deleteStatusEnum = deleteCount > 0 ? MqttProtocolTopoStatusEnum.SUCCESS : MqttProtocolTopoStatusEnum.FAILURE;
                operationRsp.setStatusCode(deleteStatusEnum.getValue())
                        .setStatusDesc(deleteStatusEnum.getDesc());
            } else {
                // 如果设备不存在，设置状态码和状态描述为FAILURE
                operationRsp.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                        .setStatusDesc(MqttProtocolTopoStatusEnum.FAILURE.getDesc());
            }

            // 添加操作结果到操作结果列表
            operationResultList.add(operationRsp);
        }

        // 创建返回结果实例并设置状态码、状态描述和操作结果列表
        return TopoDeviceOperationResultVO.builder()
                .statusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                .statusDesc(MqttProtocolTopoStatusEnum.SUCCESS.getDesc())
                .data(operationResultList)
                .build();
    }


}


