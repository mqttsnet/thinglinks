package com.mqttsnet.thinglinks.device.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.cache.lock.LockRunResult;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.easyexcel.EasyExcelListener;
import com.mqttsnet.basic.easyexcel.EasyExcelUtils;
import com.mqttsnet.basic.easyexcel.ExcelCheckManager;
import com.mqttsnet.basic.easyexcel.ExcelImportErrDto;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.common.lock.link.LinkLockKeyBuilder;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.device.easyexcel.DeviceEasyExcelService;
import com.mqttsnet.thinglinks.device.easyexcel.DeviceExportData;
import com.mqttsnet.thinglinks.device.easyexcel.DeviceImportData;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceDetailsPageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DeviceSslTestQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceSslTestResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceOverviewResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceVersionResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceSaveVO;
import com.mqttsnet.thinglinks.device.vo.update.DeviceUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 前端控制器
 * 设备档案信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/device")
@Tag(name = "设备档案信息")
public class DeviceController extends SuperController<DeviceService, Long, Device, DeviceSaveVO, DeviceUpdateVO, DevicePageQuery, DeviceResultVO> {
    private final EchoService echoService;

    private final DeviceEasyExcelService deviceEasyExcelService;

    private final DistributedLock distributedLock;


    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<Device> handlerWrapper(Device model, PageParams<DevicePageQuery> params) {
        QueryWrap<Device> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("device");
        return queryWrap;
    }

    @Override
    public void handlerResult(IPage<DeviceResultVO> page) {
        super.handlerResult(page);
        // TODO 分页列表结果 产品信息填充处理


    }

    /**
     * 新增 设备档案信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存设备档案", description = "保存设备档案")
    @PostMapping("/saveDevice")
    @WebLog(value = "保存设备档案", request = false)
    public R<DeviceSaveVO> saveDevice(@RequestBody DeviceSaveVO saveVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forSaveDeviceByUserId(ContextUtil.getUserId());
            LockRunResult<DeviceSaveVO> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.saveDevice(saveVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("设备档案保存失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


    /**
     * 修改 设备档案信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改设备档案", description = "修改设备档案")
    @PutMapping("/updateDevice")
    @WebLog(value = "修改设备档案", request = false)
    public R<DeviceUpdateVO> updateDevice(@RequestBody DeviceUpdateVO updateVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forUpdateDeviceByUserId(ContextUtil.getUserId());
            LockRunResult<DeviceUpdateVO> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.updateDevice(updateVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("修改设备档案失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


    /**
     * 修改 设备状态
     *
     * @param id     对象ID
     * @param status 新状态值
     * @return 实体
     */
    @Operation(summary = "修改设备状态", description = "修改设备状态 ")
    @PutMapping("/updateDeviceStatus/{id}")
    @WebLog(value = "修改设备状态", request = false)
    @Parameters({@Parameter(name = "id", description = "对象ID", schema = @Schema(type = "long"), in = ParameterIn.QUERY, required = true), @Parameter(name = "status", description = "新状态值（0:未激活、1:已激活、2:已禁用）", in = ParameterIn.QUERY, required = true, example = "0,1,2"),})
    public R<Boolean> updateDeviceStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        log.info("updateDeviceStatus id:{},status:{}", id, status);
        try {
            return R.success(superService.updateDeviceStatus(id, status));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("修改设备状态失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 删除设备
     *
     * @param id 设备ID
     * @return 删除结果
     */
    @Operation(summary = "删除设备", description = "根据设备ID删除设备")
    @DeleteMapping("/deleteDevice/{id}")
    @WebLog(value = "删除设备", request = false)
    @Parameters({@Parameter(name = "id", description = "设备ID", required = true),})
    public R<Boolean> deleteDevice(@PathVariable("id") Long id) {
        log.info("deleteDevice id:{}", id);
        try {
            return R.success(superService.deleteDevice(id));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("删除设备失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * Deletes multiple devices.
     *
     * @param ids List of device IDs to delete.
     * @return Deletion result.
     */
    @Operation(summary = "批量删除设备", description = "根据设备ID列表删除多个设备,整批事务:任一失败回滚全部")
    @DeleteMapping("/deleteDevices")
    @WebLog(value = "批量删除设备", request = false)
    public R<Boolean> deleteDevices(@RequestBody List<Long> ids) {
        log.info("deleteDevices ids:{}", ids);
        try {
            // 走 service 层 deleteDevices ── 整批单事务,避免老 stream().allMatch
            // "N 个独立事务串行,失败后前 K-1 条已提交"造成的孤儿设备 / 孤儿分组关系
            return R.success(superService.deleteDevices(ids));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("批量删除设备失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


    /**
     * 修改设备连接状态
     *
     * @param id               设备ID
     * @param connectionStatus 新连接状态值
     * @return 更新结果
     */
    @Operation(summary = "修改设备连接状态", description = "根据设备ID修改设备连接状态")
    @PutMapping("/updateDeviceConnectionStatus/{id}")
    @WebLog(value = "修改设备连接状态", request = false)
    @Parameters({@Parameter(name = "id", description = "对象ID", schema = @Schema(type = "long"), in = ParameterIn.QUERY, required = true), @Parameter(name = "connectionStatus", description = "新连接状态值（0:未连接、1:在线、2:离线）", in = ParameterIn.QUERY, required = true, example = "0,1,2"),})
    public R<Boolean> updateDeviceConnectionStatus(@PathVariable("id") Long id, @RequestParam("connectionStatus") Integer connectionStatus) {
        log.info("updateDeviceConnectionStatus id:{}, connectionStatus:{}", id, connectionStatus);
        try {
            return R.success(superService.updateDeviceConnectionStatusById(id, connectionStatus));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("修改设备连接状态失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 获取设备概况统计信息
     *
     * @return 设备概况统计信息
     */
    @Operation(summary = "获取设备概况统计信息", description = "统计设备的概况信息")
    @GetMapping("/deviceOverview")
    public R<DeviceOverviewResultVO> getDeviceOverview() {
        try {
            DataScopeHelper.startDataScope("device");
            DeviceOverviewResultVO deviceOverview = superService.getDeviceOverview();
            return R.success(deviceOverview);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("获取设备概况统计信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 根据产品标识查询设备软固件版本信息
     *
     * @param productIdentification 产品标识
     * @return {@link DeviceVersionResultVO} 软固件版本信息
     */
    @Operation(summary = "查询设备软固件版本信息", description = "根据产品标识查询设备软件版本集合和固件版本")
    @GetMapping("/getDeviceVersionByProduct/{productIdentification}")
    @Parameters({@Parameter(name = "productIdentification", description = "产品标识", required = true),})
    public R<DeviceVersionResultVO> getDeviceVersionByProduct(@PathVariable("productIdentification") String productIdentification) {
        log.info("getDeviceVersionByProduct productIdentification:{}", productIdentification);
        try {
            // 开启数据权限
            DataScopeHelper.startDataScope("device");
            DeviceVersionResultVO result = superService.getDeviceVersionByProduct(productIdentification);
            echoService.action(result);
            return R.success(result);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("查询设备软固件版本集合信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 根据设备ID获取设备详情
     *
     * @param id 设备ID
     * @return 设备详情
     */
    @Operation(summary = "根据设备ID获取设备详情", description = "根据设备ID获取设备详情,包含设备位置、产品基本信息")
    @GetMapping("/getDeviceDetails/{id}")
    @Parameters({@Parameter(name = "id", description = "设备ID", required = true),})
    public R<DeviceDetailsResultVO> getDeviceDetails(@PathVariable("id") Long id) {
        log.info("getDeviceDetails...param..id:{}", id);
        try {
            DeviceDetailsResultVO deviceDetailsResultVO = superService.getDeviceDetails(id);
            echoService.action(deviceDetailsResultVO);
            return R.success(deviceDetailsResultVO);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("获取设备概况统计信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 根据设备标识获取设备详情
     *
     * @param deviceIdentification 设备标识
     * @return 设备详情
     */
    @Operation(summary = "根据设备标识获取设备详情", description = "根据设备标识获取设备详情")
    @GetMapping("/getDeviceDetailsByIdentification/{deviceIdentification}")
    @Parameters({@Parameter(name = "deviceIdentification", description = "设备标识", required = true),})
    public R<DeviceDetailsResultVO> getDeviceDetailsByIdentification(@PathVariable("deviceIdentification") String deviceIdentification) {
        log.info("getDeviceDetailsByIdentification deviceIdentification:{}", deviceIdentification);
        try {
            DeviceDetailsResultVO result = superService.findOneByDeviceIdentification(deviceIdentification);
            echoService.action(result);
            return R.success(result);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("根据设备标识获取设备详情失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 根据多个设备标识获取设备详情
     *
     * @param deviceIdentifications 设备标识列表
     * @return 设备详情列表
     */
    @Operation(summary = "根据多个设备标识获取设备详情", description = "根据多个设备标识获取设备详情(多个英文逗号分割)")
    @GetMapping("/getDeviceDetailsByIdentifications")
    @WebLog(value = "获取多个设备详情", request = false)
    @Parameters({@Parameter(name = "deviceIdentifications", description = "设备标识列表", required = true),})
    public R<List<DeviceDetailsResultVO>> getDeviceDetailsByIdentifications(@RequestParam List<String> deviceIdentifications) {
        log.info("getDeviceDetailsByIdentifications for deviceIdentifications: {}", JsonUtil.toJson(deviceIdentifications));
        try {
            List<DeviceDetailsResultVO> deviceDetailsList = deviceIdentifications.stream().filter(StrUtil::isNotBlank).distinct().map(id -> {
                try {
                    return Optional.ofNullable(superService.findOneByDeviceIdentification(id));
                } catch (Exception e) {
                    log.error("Error retrieving device details for identification: {}", id, e);
                    return Optional.<DeviceDetailsResultVO>empty();
                }
            }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

            echoService.action(deviceDetailsList);
            return R.success(deviceDetailsList);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("根据多个设备标识获取设备详情失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }

    }

    /**
     * 获取设备详情分页信息
     *
     * @param params 查询参数
     * @return {@link IPage<Device>} 设备详情分页信息
     */
    @Operation(summary = "获取设备详情分页信息", description = "获取设备详情(分页列表)")
    @PostMapping("/getDeviceDetailsPage")
    @WebLog(value = "获取设备详情分页信息", request = false)
    public R<IPage<DeviceDetailsResultVO>> getDeviceDetailsPage(@RequestBody PageParams<DeviceDetailsPageQuery> params) {
        log.info("getDeviceDetailsPage params:{}", params);
        try {
            IPage<DeviceDetailsResultVO> page = superService.getDeviceDetailsPage(params);
            echoService.action(page.getRecords());
            return R.success(page);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("获取设备详情分页信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * Imports Excel data and optionally exports error data if any.
     *
     * @param response the HTTP response
     * @param file     the Excel file to import
     * @return the result of the import operation
     */
    @Operation(summary = "导入设备Excel数据", description = "导入Excel数据，并在有错误数据时导出错误数据")
    @PostMapping("/importDeviceExcel")
    public void importDeviceExcel(HttpServletResponse response, @RequestParam("file") MultipartFile file) throws IOException {
        try {
            ExcelCheckManager<DeviceImportData> excelCheckManager = deviceEasyExcelService::checkImportExcel;

            EasyExcelListener<DeviceImportData> easyExcelListener = EasyExcelUtils.webImportExcel(response, file.getInputStream(), excelCheckManager, DeviceImportData.class, false);

            List<ExcelImportErrDto<DeviceImportData>> errorList = easyExcelListener.getErrList();
            List<DeviceImportData> successList = easyExcelListener.getSuccessList();
            log.info("importDeviceExcel successList size:{}, errorList size:{}", successList.size(), errorList.size());
            if (!errorList.isEmpty()) {
                EasyExcelUtils.exportErrorExcel(response, errorList, DeviceImportData.class);
            } else {
                response.setContentType(StrPool.CONTENT_TYPE);
                response.setCharacterEncoding(StrPool.UTF_8);
                response.getWriter().write(R.success().toString());
            }

        } catch (Exception e) {
            log.error("importDeviceExcel failed: {}", e.getMessage(), e);
            response.setContentType(StrPool.CONTENT_TYPE);
            response.setCharacterEncoding(StrPool.UTF_8);
            response.getWriter().write(R.fail("导入失败：" + e.getMessage()).toString());
        }
    }

    @Operation(summary = "批量导出设备数据", description = "根据设备ID列表导出多个设备数据为Excel文件")
    @PostMapping("/exportDevices")
    @WebLog(value = "批量导出设备数据", request = false)
    public void exportDevices(@RequestBody List<Long> ids, HttpServletResponse response) {
        log.info("exportDevices ids:{}", ids);
        try {
            DevicePageQuery query = new DevicePageQuery();
            query.setIds(ids);
            List<DeviceResultVO> deviceResultVOList = superService.getDeviceResultVOList(query);

            List<DeviceExportData> deviceExportDataList = deviceResultVOList.stream()
                    .distinct()
                    .filter(Objects::nonNull)
                    .map(device -> BeanPlusUtil.toBeanIgnoreError(device, DeviceExportData.class))
                    .collect(Collectors.toList());
            //注意：一次性使用输出流：我们确保 response.getOutputStream() 只被调用一次，并且不会与 getWriter() 冲突
            //错误处理：在捕获到异常时，重置响应并使用 PrintWriter 返回错误信息
            if (CollUtil.isNotEmpty(deviceExportDataList)) {
                EasyExcelUtils.webWriteExcel(response, deviceExportDataList, DeviceExportData.class, "Device_Data_Export");
            } else {
                response.setContentType(StrPool.CONTENT_TYPE);
                response.setCharacterEncoding(StrPool.UTF_8);
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(R.success().toString());
                }
            }
        } catch (IOException e) {
            log.error("导出设备数据时发生错误", e);
            response.reset();
            response.setContentType(StrPool.CONTENT_TYPE);
            response.setCharacterEncoding(StrPool.UTF_8);
            try (PrintWriter writer = response.getWriter()) {
                writer.write(R.fail("导出设备数据时发生错误：" + e.getMessage()).toString());
            } catch (IOException ioException) {
                log.error("返回错误信息时发生错误", ioException);
            }
        }
    }

    /**
     * SSL 证书认证测试器 ── 端到端模拟设备 SSL 认证流程,分步返回每步结果。
     * 仅给运维端测试器页面使用,不参与设备主认证主流程。
     */
    @Operation(summary = "SSL 证书认证测试", description = "端到端模拟设备 SSL 认证,分步返回结果")
    @PostMapping("/sslTest")
    public R<DeviceSslTestResultVO> sslTest(@Validated @RequestBody DeviceSslTestQuery query) {
        return R.success(superService.sslTest(query));
    }

}