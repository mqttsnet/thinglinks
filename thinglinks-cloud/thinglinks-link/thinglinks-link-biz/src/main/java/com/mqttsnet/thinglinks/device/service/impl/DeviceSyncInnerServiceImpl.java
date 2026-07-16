package com.mqttsnet.thinglinks.device.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenInnerFacade;
import com.mqttsnet.thinglinks.cache.CacheSuperAbstract;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceNodeTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceStatusEnum;
import com.mqttsnet.thinglinks.device.service.DeviceActionService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.service.DeviceSyncInnerService;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备数据同步业务层接口实现。
 *
 * @author xiaonannet
 * @version 1.0
 * @date 2025/1/11 17:23
 */

@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceSyncInnerServiceImpl extends CacheSuperAbstract implements DeviceSyncInnerService {

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final DeviceService deviceService;
    private final DeviceActionService deviceActionService;
    private final MqttBrokerOpenInnerFacade mqttBrokerOpenInnerFacade;
    private final ContextAwareExecutor contextAwareExecutor;

    @Qualifier("linkDefaultExecutor")
    private final ThreadPoolExecutor linkDefaultExecutor;

    // 日志格式常量
    private static final String BATCH_LOG_FORMAT =
            "[设备状态同步-开始] 租户ID={} | 总设备数={} | 分页大小={} | 预计批次={}";

    private static final String BATCH_ITEM_LOG =
            "[设备状态同步-进度] 租户ID={} | 当前批次={}/{} | 本页设备={} | 成功累计={} | 失败累计={} | 耗时={}ms";

    private static final String BATCH_SUMMARY =
            "[设备状态同步-完成] 租户ID={} | 总耗时={}ms | 成功总数={} | 失败总数={} | 平均耗时={}ms/设备";

    private static final String DEVICE_DETAIL_LOG =
            "[设备状态同步-设备] 租户ID={} | 设备标识={} | 协议类型={} | 状态={} | 耗时={}ms | 错误={}";

    /**
     * 同步设备连接状态。
     *
     * @param tenantId 租户 ID
     */
    @Override
    public void syncDeviceConnectionStatus(Long tenantId) {
        long startTime = System.currentTimeMillis();
        AtomicInteger totalSuccess = new AtomicInteger();
        AtomicInteger totalFail = new AtomicInteger();

        log.info("Starting device connection status sync for tenantId: {}", tenantId);

        try {
            // 构建查询参数
            DevicePageQuery queryModel = DevicePageQuery.builder()
                    .deviceStatusList(Arrays.asList(
                            DeviceStatusEnum.ACTIVATED.getValue(),
                            DeviceStatusEnum.LOCKED.getValue()))
                    .nodeTypeList(Arrays.asList(
                            DeviceNodeTypeEnum.ORDINARY.getValue(),
                            DeviceNodeTypeEnum.GATEWAY.getValue()))
                    .build();

            // 先获取第一页数据获取总数
            PageParams<DevicePageQuery> firstPageParams = new PageParams<>(1, PAGE_SIZE);
            firstPageParams.setModel(queryModel);
            IPage<DeviceResultVO> firstPage = deviceService.getPage(firstPageParams);

            if (firstPage == null || firstPage.getTotal() == 0) {
                log.info("No device connection state to synchronize was found for tenantId: {}", tenantId);
                return;
            }

            // 批次元数据
            long totalDevices = firstPage.getTotal();
            int totalPages = (int) firstPage.getPages();

            log.info(BATCH_LOG_FORMAT, tenantId, totalDevices, PAGE_SIZE, totalPages);

            // 按页顺序处理
            IntStream.rangeClosed(1, totalPages)
                    .sequential()
                    .forEach(currentPage -> {
                        ContextUtil.setTenantId(tenantId);
                        long pageStartTime = System.currentTimeMillis();

                        // 查询当前页设备
                        List<DeviceResultVO> devices = fetchDevicePage(currentPage, queryModel);

                        // 处理当前页设备
                        processDevicesBatch(tenantId, devices, totalSuccess, totalFail);

                        log.info(BATCH_ITEM_LOG, tenantId, currentPage, totalPages, devices.size(), totalSuccess.get(), totalFail.get(), System.currentTimeMillis() - pageStartTime);
                    });

            long totalCost = System.currentTimeMillis() - startTime;
            log.info(BATCH_SUMMARY, tenantId, totalCost, totalSuccess.get(), totalFail.get(), totalDevices > 0 ? totalCost / totalDevices : 0);

        } catch (Exception e) {
            log.error("Error occurred during device status sync for tenantId: {}", tenantId, e);
        }
    }

    /**
     * 获取分页设备数据。
     *
     * @param currentPage 当前页码
     * @param queryModel  设备分页查询条件
     * @return 当前页设备列表;无数据返回空列表
     */
    private List<DeviceResultVO> fetchDevicePage(int currentPage, DevicePageQuery queryModel) {
        PageParams<DevicePageQuery> params = new PageParams<>(currentPage, PAGE_SIZE);
        params.setModel(queryModel);
        return Optional.ofNullable(deviceService.getPage(params))
                .map(IPage::getRecords)
                .orElse(Collections.emptyList());
    }

    /**
     * 处理设备批次数据。
     *
     * @param tenantId     租户 ID
     * @param devices      当前批次设备列表
     * @param totalSuccess 全局成功累计计数器
     * @param totalFail    全局失败累计计数器
     */
    private void processDevicesBatch(Long tenantId, List<DeviceResultVO> devices,
                                     AtomicInteger totalSuccess, AtomicInteger totalFail) {
        AtomicInteger pageSuccess = new AtomicInteger();
        AtomicInteger pageFail = new AtomicInteger();

        List<CompletableFuture<Void>> deviceFutures = devices.stream()
                .map(device -> contextAwareExecutor.executeWithContext(() -> {
                    long deviceStart = System.currentTimeMillis();
                    try {
                        syncDeviceStatus(device);
                        pageSuccess.incrementAndGet();
                    } catch (Exception e) {
                        pageFail.incrementAndGet();
                        log.error(DEVICE_DETAIL_LOG, tenantId, device.getDeviceIdentification(), "未知", "失败", System.currentTimeMillis() - deviceStart, e.getClass().getSimpleName() + ":" + e.getMessage());
                    }
                    return null;
                }, linkDefaultExecutor))
                .map(future -> future.thenApply(result -> (Void) null))
                .toList();

        // 等待当前页所有设备处理完成
        CompletableFuture.allOf(deviceFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    totalSuccess.addAndGet(pageSuccess.get());
                    totalFail.addAndGet(pageFail.get());
                })
                .exceptionally(ex -> {
                    log.error("批次处理异常", ex);
                    return null;
                })
                .join();
    }

    /**
     * 同步单个设备状态。
     *
     * <p>缓存 miss(分页查出来的设备 cache 还未构建,常见竞态)直接 skip,不计 failed ──
     * 由后续事件 / 下次 job 兜底纠正。
     *
     * @param device 待同步设备
     */
    private void syncDeviceStatus(DeviceResultVO device) {
        long startTime = System.currentTimeMillis();
        Optional<DeviceCacheVO> cacheOpt = linkCacheDataHelper.getDeviceCacheVO(device.getDeviceIdentification());
        if (cacheOpt.isEmpty()) {
            log.warn("[Sync] cache miss, skip deviceId={}", device.getDeviceIdentification());
            return;
        }
        DeviceCacheVO cache = cacheOpt.get();
        // 产品 protocolType 不再内嵌在 deviceCacheVO 上 ── 走 LinkCacheDataHelper 共享解析入口:
        // 优先按 (productIdentification, boundProductVersionNo) 取版本快照 protocolType,
        // 版本号缺失则回退到产品当前 activeVersionNo 维度的基础元数据。
        String protocolType = linkCacheDataHelper
                .resolveProtocolType(cache.getProductIdentification(), cache.getBoundProductVersionNo())
                .orElse(null);
        if (protocolType == null) {
            log.warn("[Sync] cannot resolve protocolType, skip deviceId={} productIdentification={} boundVer={}",
                    device.getDeviceIdentification(), cache.getProductIdentification(),
                    cache.getBoundProductVersionNo());
            return;
        }
        try {
            ProtocolTypeEnum.fromValue(protocolType)
                    .ifPresent(protocol -> dispatchByProtocol(protocol, device, cache));
            log.debug(DEVICE_DETAIL_LOG, ContextUtil.getTenantId(), device.getDeviceIdentification(),
                    protocolType, "成功", System.currentTimeMillis() - startTime, "无");
        } catch (Exception e) {
            log.error(DEVICE_DETAIL_LOG, ContextUtil.getTenantId(), device.getDeviceIdentification(),
                    protocolType, "失败", System.currentTimeMillis() - startTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 按设备协议类型分发到对应同步处理方法,未知协议走 handleUnknownProtocol 兜底不报错。
     *
     * @param protocol 设备协议类型
     * @param device   待同步设备
     * @param cache    设备缓存
     */
    private void dispatchByProtocol(ProtocolTypeEnum protocol, DeviceResultVO device, DeviceCacheVO cache) {
        switch (protocol) {
            case MQTT -> handleMqttProtocol(device, cache);
            case WEBSOCKET -> handleWebsocketProtocol(device, cache);
            case TCP -> handleTcpProtocol(device, cache);
            case HTTP -> handleHttpProtocol(device, cache);
            default -> handleUnknownProtocol(device, cache, protocol);
        }
    }

    /**
     * 处理 MQTT 协议设备 ── 以 BifroMQ session 为权威真相同步 connect_status。
     *
     * <p>不确定状态(broker 异常 / 临时不可达)不动 DB,等下次 job 或实时事件再纠正。
     *
     * @param device        待同步设备
     * @param deviceCacheVO 设备缓存
     */
    private void handleMqttProtocol(DeviceResultVO device, DeviceCacheVO deviceCacheVO) {
        resolveMqttSessionStatus(deviceCacheVO)
                .filter(target -> isStatusChanged(target, device.getConnectStatus()))
                .ifPresent(target -> updateDeviceStatus(
                        device,
                        DeviceConnectStatusEnum.fromValue(device.getConnectStatus())
                                .orElse(DeviceConnectStatusEnum.UNCONNECTED),
                        target));
    }

    /**
     * 判断 DB 当前 connect_status 与目标状态是否不同。
     * 当前值为 null 或非法枚举时一律返回 true,触发后续 update 把 DB 拉回合法值。
     *
     * @param target       目标连接状态
     * @param currentValue DB 当前连接状态值
     * @return 与目标状态不同返回 true
     */
    private boolean isStatusChanged(DeviceConnectStatusEnum target, Integer currentValue) {
        return DeviceConnectStatusEnum.fromValue(currentValue)
                .map(current -> !target.equals(current))
                .orElse(true);
    }

    /**
     * 处理 Websocket 协议设备。
     *
     * @param device        待同步设备
     * @param deviceCacheVO 设备缓存
     */
    private void handleWebsocketProtocol(DeviceResultVO device, DeviceCacheVO deviceCacheVO) {
        log.debug("Websocket protocol device {} - protocol specific handling needed",
                device.getDeviceIdentification());
        // Websocket协议特定的状态检查逻辑可以在这里实现
    }

    /**
     * 处理 TCP 协议设备。
     *
     * @param device        待同步设备
     * @param deviceCacheVO 设备缓存
     */
    private void handleTcpProtocol(DeviceResultVO device, DeviceCacheVO deviceCacheVO) {
        log.debug("TCP protocol device {} - protocol specific handling needed",
                device.getDeviceIdentification());
        // TCP连接状态检查逻辑可以在这里实现
    }

    /**
     * 处理 HTTP 协议设备。
     *
     * @param device        待同步设备
     * @param deviceCacheVO 设备缓存
     */
    private void handleHttpProtocol(DeviceResultVO device, DeviceCacheVO deviceCacheVO) {
        log.debug("HTTP protocol device {} - protocol specific handling needed",
                device.getDeviceIdentification());
        // HTTP心跳或状态查询接口检查逻辑可以在这里实现
    }


    /**
     * 处理未知协议设备。
     *
     * @param device        待同步设备
     * @param deviceCacheVO 设备缓存
     * @param protocol      未知协议类型
     */
    private void handleUnknownProtocol(DeviceResultVO device, DeviceCacheVO deviceCacheVO, ProtocolTypeEnum protocol) {
        log.debug("Unknown protocol {} for device {}, skipping sync",
                protocol.getDesc(), device.getDeviceIdentification());
    }

    /**
     * 查 BifroMQ session 实时状态,语义与 mqs 侧 SessionStatusResolver 对齐(三态):
     * ONLINE=明确在线;OFFLINE=明确离线(session not found);empty=不确定(broker 临时异常/超时),保留现状不动 DB。
     *
     * @param cache 设备缓存
     * @return 在线 / 离线状态;不确定时返回 empty
     */
    private Optional<DeviceConnectStatusEnum> resolveMqttSessionStatus(DeviceCacheVO cache) {
        R<Boolean> r = mqttBrokerOpenInnerFacade.isOnline(
                ContextUtil.getTenantIdStr(),
                cache.getDeviceIdentification(),
                cache.getClientId());
        if (r == null || !r.getIsSuccess()) {
            log.warn("[Sync] session unknown deviceId={} code={} msg={}",
                    cache.getDeviceIdentification(),
                    r == null ? "null" : r.getCode(),
                    r == null ? "null" : r.getMsg());
            return Optional.empty();
        }
        return Optional.ofNullable(r.getData())
                .map(online -> online ? DeviceConnectStatusEnum.ONLINE : DeviceConnectStatusEnum.OFFLINE);
    }


    /**
     * 更新设备状态,并记录设备动作。
     *
     * @param device        待更新设备
     * @param currentStatus 当前连接状态
     * @param targetStatus  目标连接状态
     */
    private void updateDeviceStatus(DeviceResultVO device, DeviceConnectStatusEnum currentStatus, DeviceConnectStatusEnum targetStatus) {
        try {
            // 更新设备连接状态
            deviceService.updateDeviceConnectionStatusById(device.getId(), targetStatus.getValue());
            log.info("Device {} status updated to {}", device.getDeviceIdentification(), targetStatus.getDesc());

            // 记录设备动作
            recordDeviceAction(device, currentStatus, targetStatus);
        } catch (Exception e) {
            log.error("Failed to update status for device {} to {}.", device.getDeviceIdentification(), targetStatus.getDesc(), e);
        }
    }


    /**
     * 记录设备动作数据。
     *
     * @param device        关联设备
     * @param currentStatus 当前连接状态
     * @param targetStatus  目标连接状态
     */
    private void recordDeviceAction(DeviceResultVO device, DeviceConnectStatusEnum currentStatus, DeviceConnectStatusEnum targetStatus) {
        // 构建设备动作描述和类型
        String describable = buildDeviceStatusDescription(device, currentStatus, targetStatus);
        DeviceActionSaveVO deviceActionSaveVO = getDeviceActionSaveVO(device, targetStatus, describable);
        try {
            // 保存设备动作记录
            DeviceAction deviceAction = deviceActionService.saveDeviceAction(deviceActionSaveVO);
            log.info("Device action saved: {}", deviceAction);
        } catch (Exception e) {
            log.error("Failed to save device action for device ID: {}", device.getDeviceIdentification(), e);
        }
    }


    private DeviceActionSaveVO getDeviceActionSaveVO(DeviceResultVO device, DeviceConnectStatusEnum targetStatus, String describable) {
        DeviceActionTypeEnum actionType = getActionTypeForStatus(targetStatus);

        // 构建并保存设备动作记录
        DeviceActionSaveVO deviceActionSaveVO = new DeviceActionSaveVO();
        deviceActionSaveVO.setDeviceIdentification(device.getDeviceIdentification());
        deviceActionSaveVO.setActionType(actionType.getValue());
        deviceActionSaveVO.setMessage(actionType.getDesc());
        deviceActionSaveVO.setStatus(DeviceActionStatusEnum.SUCCESSFUL.getValue());
        deviceActionSaveVO.setRemark(describable);
        return deviceActionSaveVO;
    }

    /**
     * 构建设备状态描述。
     *
     * @param device        关联设备
     * @param currentStatus 当前连接状态
     * @param targetStatus  目标连接状态
     * @return 设备状态描述文本
     */
    private String buildDeviceStatusDescription(DeviceResultVO device, DeviceConnectStatusEnum currentStatus, DeviceConnectStatusEnum targetStatus) {
        // 生成详细的描述
        StringBuilder description = new StringBuilder();
        description.append("Device ClientId: ").append(device.getClientId())
                .append(", DeviceIdentification: ").append(device.getDeviceIdentification())
                .append(", Current Status: ").append(currentStatus)
                .append(", Target Status: ").append(targetStatus)
                .append(", Timestamp: ").append(DateUtils.millisecondStampL())
                .append(", Source: System Sync");

        if (!currentStatus.equals(targetStatus)) {
            description.append(", Status Change: ")
                    .append(currentStatus)
                    .append(" -> ")
                    .append(targetStatus)
                    .append(" (Status change detected).");
        } else {
            description.append(", No change detected in status.");
        }
        return description.toString();
    }

    /**
     * 获取设备状态对应的动作类型。
     *
     * @param targetStatus 目标连接状态
     * @return 对应的设备动作类型
     */
    private DeviceActionTypeEnum getActionTypeForStatus(DeviceConnectStatusEnum targetStatus) {
        return switch (targetStatus) {
            case ONLINE -> DeviceActionTypeEnum.CONNECT;
            case OFFLINE -> DeviceActionTypeEnum.DISCONNECT;
            default -> DeviceActionTypeEnum.UNKNOWN;
        };
    }

}