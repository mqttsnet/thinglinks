package com.mqttsnet.thinglinks.device.easyexcel.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.annotation.ExcelProperty;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.easyexcel.ExcelCheckResult;
import com.mqttsnet.basic.easyexcel.ExcelImportErrDto;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.basic.utils.TenantUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.easyexcel.DeviceEasyExcelService;
import com.mqttsnet.thinglinks.device.easyexcel.DeviceImportData;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.enumeration.DeviceAuthModeEnum;
import com.mqttsnet.thinglinks.device.event.publisher.DeviceEventPublisher;
import com.mqttsnet.thinglinks.device.event.source.DeviceInfoUpdatedEventSource;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.product.service.ProductQueryService;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * -----------------------------------------------------------------------------
 * File Name: DeviceEasyExcelServiceImpl
 * -----------------------------------------------------------------------------
 * Description:
 * 设备档案导入导出业务接口实现类
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/6/20       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/6/20 18:50
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceEasyExcelServiceImpl implements DeviceEasyExcelService {

    private final DeviceService deviceService;
    private final ProductQueryService productQueryService;
    private final DeviceEventPublisher deviceEventPublisher;


    /**
     * Checks the imported Excel data for devices.
     *
     * @param deviceImportDataList the list of imported device data
     * @return the result of the Excel check, including any errors found
     */
    @Override
    public ExcelCheckResult<DeviceImportData> checkImportExcel(List<DeviceImportData> deviceImportDataList) {
        List<DeviceImportData> successList = new ArrayList<>();
        List<ExcelImportErrDto<DeviceImportData>> errList = new ArrayList<>();

        // Collect device and product identifiers
        List<String> deviceIdentificationCollect = deviceImportDataList.stream()
                .map(DeviceImportData::getDeviceIdentification)
                .collect(Collectors.toList());
        List<String> productIdentificationCollect = deviceImportDataList.stream()
                .map(DeviceImportData::getProductIdentification)
                .collect(Collectors.toList());

        // Retrieve existing devices and products
        Map<String, DeviceResultVO> existenceDeviceResultVOMap = getExistingDevices(deviceIdentificationCollect);
        Map<String, ProductResultVO> existenceProductResultVOMap = getExistingProducts(productIdentificationCollect);

        for (DeviceImportData item : deviceImportDataList) {
            Map<Integer, String> cellErrColMap = new HashMap<>();

            // Validate device identification
            validateDeviceIdentification(item, existenceDeviceResultVOMap, cellErrColMap);

            // Validate product identification
            validateProductIdentification(item, existenceProductResultVOMap, cellErrColMap);

            if (cellErrColMap.isEmpty()) {
                successList.add(item);
            } else {
                errList.add(new ExcelImportErrDto<>(item, cellErrColMap));
            }
        }

        // 入库 ── 严格"全有或全无"原子语义:
        //   - errList 非空 → 本批存在错行,即使 successList 部分通过校验也不写入 DB,
        //     与 Controller 错误下载链路 + 前端 "导入失败" 提示对齐,用户修订错行后整批重传
        //   - 否则 successList 全量入库;同时带入产品索引,导入时按 activeVersionNo 默认绑定版本快照,
        //     避免之后 mqs 主流程为每条数据再次回填造成额外 UPDATE
        // 历史实现只判断 successList 非空就入库,造成"前端报失败但部分行已落库",
        // 用户修完整批重传 → 之前成功行第二次入库 → 重复设备 / 绑错产品。
        if (!errList.isEmpty()) {
            log.warn("Importing aborted ── batch has {} error rows, skip persisting the {} valid rows",
                    errList.size(), successList.size());
        } else if (CollUtil.isNotEmpty(successList)) {
            log.info("Saving {} successful items to the database", successList.size());
            saveSuccessList(successList, existenceProductResultVOMap);
        }

        return new ExcelCheckResult<>(successList, errList);
    }

    private Map<String, DeviceResultVO> getExistingDevices(List<String> deviceIdentificationCollect) {
        DevicePageQuery query = new DevicePageQuery();
        query.setDeviceIdentificationList(deviceIdentificationCollect);

        List<DeviceResultVO> deviceResultVOList = Optional.ofNullable(deviceService.getDeviceResultVOList(query))
                .orElse(Collections.emptyList());

        return deviceResultVOList.stream()
                .filter(d -> d.getDeviceIdentification() != null)
                .collect(Collectors.toMap(DeviceResultVO::getDeviceIdentification,
                        Function.identity(), (existing, replacement) -> existing));
    }

    private Map<String, ProductResultVO> getExistingProducts(List<String> productIdentificationCollect) {
        ProductPageQuery productQuery = new ProductPageQuery();
        productQuery.setProductIdentificationList(productIdentificationCollect);

        List<ProductResultVO> productResultVOList = Optional.ofNullable(productQueryService.getProductResultVOList(productQuery))
                .orElse(Collections.emptyList());

        return productResultVOList.stream()
                .filter(p -> p.getProductIdentification() != null)
                .collect(Collectors.toMap(ProductResultVO::getProductIdentification,
                        Function.identity(), (existing, replacement) -> existing));
    }

    private void validateDeviceIdentification(DeviceImportData item, Map<String, DeviceResultVO> existenceDeviceResultVOMap, Map<Integer, String> cellErrColMap) {
        if (CollUtil.isNotEmpty(existenceDeviceResultVOMap) && existenceDeviceResultVOMap.containsKey(item.getDeviceIdentification())) {
            addValidationError(item, "deviceIdentification", "该设备标识符已存在", cellErrColMap);
        }
    }

    private void validateProductIdentification(DeviceImportData item, Map<String, ProductResultVO> existenceProductResultVOMap, Map<Integer, String> cellErrColMap) {
        if (CollUtil.isNotEmpty(existenceProductResultVOMap) && !existenceProductResultVOMap.containsKey(item.getProductIdentification())) {
            addValidationError(item, "productIdentification", "该产品标识符不存在", cellErrColMap);
        }
    }

    private void addValidationError(DeviceImportData item, String fieldName, String errorMessage, Map<Integer, String> cellErrColMap) {
        try {
            Field field = DeviceImportData.class.getDeclaredField(fieldName);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            int index = annotation.index();
            cellErrColMap.put(index, errorMessage);
        } catch (NoSuchFieldException e) {
            log.error("NoSuchFieldException during validation for field: {}", fieldName, e);
        }
    }

    private void saveSuccessList(List<DeviceImportData> successList,
                                 Map<String, ProductResultVO> existenceProductResultVOMap) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        List<Device> devicesToSave = successList.stream()
                .map(data -> convertToDeviceEntity(data, existenceProductResultVOMap))
                .collect(Collectors.toList());
        log.info("Importing device files ,Saving {} count devices to the database", devicesToSave.size());
        boolean saveFlag = deviceService.saveBatch(devicesToSave);

        if (saveFlag) {
            log.info("Saving {} count devices to the database success", devicesToSave.size());
            deviceEventPublisher.publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource.builder()
                    .deviceIdentificationList(devicesToSave.stream().map(Device::getDeviceIdentification).distinct().collect(Collectors.toList()))
                    .build());
        } else {
            log.error("Saving {} count devices to the database failed", devicesToSave.size());
        }

    }

    /**
     * Excel 行 → Device 实体。
     *
     * <p>boundProductVersionNo 默认按导入时所属产品的 activeVersionNo 绑定 ──
     * 跟单设备新增保持同一语义,避免后续物模型解析按产品当前版本回填造成多余 UPDATE。
     * 若 Excel 行显式带了版本号(灰度白名单批量导入场景),则保留入参不覆盖。</p>
     */
    private Device convertToDeviceEntity(DeviceImportData data,
                                         Map<String, ProductResultVO> existenceProductResultVOMap) {
        Device device = new Device();
        device.setAppId(data.getAppId());
        device.setDeviceIdentification(StrUtil.isNotBlank(data.getDeviceIdentification()) ? data.getDeviceIdentification() : SnowflakeIdUtil.nextId());
        device.setProductIdentification(data.getProductIdentification());
        device.setDeviceName(StrUtil.isNotBlank(data.getDeviceName()) ? data.getDeviceName() : RandomUtil.randomString(16));
        device.setConnector(data.getConnector());
        device.setUserName(StrUtil.isNotBlank(data.getUserName()) ? data.getUserName() : RandomUtil.randomString(32));
        String password = StrUtil.isNotBlank(data.getPassword()) ? data.getPassword() : RandomUtil.randomString(32);
        device.setPassword(password);
        device.setClientId(TenantUtil.buildOptionalItem(StrUtil.isNotBlank(data.getClientId()) ? data.getClientId() : SnowflakeIdUtil.nextId(), ContextUtil.getTenantIdStr()));
        device.setAuthMode(DeviceAuthModeEnum.ACCOUNT_MODE.getValue());
        device.setEncryptMethod(Integer.valueOf(data.getEncryptMethod()));
        device.setEncryptKey(data.getEncryptKey());
        device.setEncryptVector(data.getEncryptVector());
        device.setSignKey(data.getSignKey());
        device.setDeviceTags(data.getDeviceTags());
        device.setSwVersion(data.getSwVersion());
        device.setFwVersion(data.getFwVersion());
        device.setDeviceSdkVersion(data.getDeviceSdkVersion());
        device.setDeviceStatus(Integer.valueOf(data.getDeviceStatus()));
        device.setNodeType(Integer.valueOf(data.getNodeType()));

        // 默认绑定版本:灰度态(previousFullVersionNo 非空)绑稳定版,否则绑当前生效版本 activeVersionNo ——
        // 避免导入设备自动进未验证的灰度组,与新建设备口径一致(见 DeviceServiceImpl#resolveBindVersionForNewDevice);
        // Excel 显式带值时保留不覆盖。
        if (StrUtil.isBlank(device.getBoundProductVersionNo()) && existenceProductResultVOMap != null) {
            ProductResultVO product = existenceProductResultVOMap.get(data.getProductIdentification());
            if (product != null) {
                String bindVersion = StrUtil.blankToDefault(
                        product.getPreviousFullVersionNo(), product.getActiveVersionNo());
                if (StrUtil.isNotBlank(bindVersion)) {
                    device.setBoundProductVersionNo(bindVersion);
                }
            }
        }
        return device;
    }


}
