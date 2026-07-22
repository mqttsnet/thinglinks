package com.mqttsnet.thinglinks.cache.device;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.CacheSuperAbstract;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.device.service.DeviceQueryService;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备缓存管理服务(设备信息)。
 *
 * @author ShiHuan Sun
 * @version 1.1
 */
@DS(DsConstant.BASE_TENANT)
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceCacheService extends CacheSuperAbstract {
    private final CachePlusOps cachePlusOps;
    /**
     * 所有 DB 读取统一走 leaf {@link DeviceQueryService},不依赖 DeviceService:DeviceService 间接持有
     * LinkCacheDataHelper,而本类又被 helper 的 read-through 回源调用,直接依赖 service 会成环;leaf
     * QueryService 零下游 Service 依赖断环,自带 @DS(BASE_TENANT) 切租户库。
     */
    private final DeviceQueryService deviceQueryService;
    private final ContextAwareExecutor contextAwareExecutor;


    private static final String BATCH_LOG_FORMAT =
            "[批次-开始] 租户ID={} | 操作类型={} | 总设备数={} | 分页大小={} | 预计批次={}";

    private static final String BATCH_ITEM_LOG =
            "[批次-进度] 租户ID={} | 当前批次={}/{} | 本页设备={} | 成功累计={} | 失败累计={} | 耗时={}ms";

    private static final String BATCH_SUMMARY =
            "[批次-完成] 租户ID={} | 总耗时={}ms | 成功总数={} | 失败总数={} | 平均耗时={}ms/设备";

    private static final String DEVICE_DETAIL_LOG =
            "[批次-设备] 租户ID={} | 设备标识={} | 状态={} | 耗时={}ms | 错误={}";

    /**
     * 刷新指定租户的设备缓存(全量)。缓存只放设备字段,不内嵌 productCacheVO ──
     * 消费方需要产品信息时走 linkCacheDataHelper.getProductCacheVO / resolveProductModelByVersionNo 取。
     *
     * @param tenantId 租户ID,不能为null
     */
    public void refreshDeviceCacheForTenant(Long tenantId) {
        long startTime = System.currentTimeMillis();
        AtomicInteger totalSuccess = new AtomicInteger();
        AtomicInteger totalFail = new AtomicInteger();

        // 批次元数据
        int totalDevices = deviceQueryService.findDeviceTotal().intValue();
        int totalPages = (int) Math.ceil((double) totalDevices / PAGE_SIZE);

        log.info(BATCH_LOG_FORMAT, tenantId, "设备缓存全量刷新", totalDevices, PAGE_SIZE, totalPages);

        // 按页顺序处理
        IntStream.rangeClosed(1, totalPages)
                .sequential() // 保持顺序处理分页
                .forEach(currentPage -> {
                    ContextUtil.setTenantId(tenantId);
                    long pageStartTime = System.currentTimeMillis();
                    // 查询当前页设备
                    List<DeviceResultVO> devices = fetchDevicePage(currentPage);
                    // 处理当前页设备（内部并行处理）
                    processDevicesBatch(tenantId, devices, totalSuccess, totalFail);
                    log.info(BATCH_ITEM_LOG, tenantId, currentPage, totalPages, devices.size(), totalSuccess.get(), totalFail.get(), System.currentTimeMillis() - pageStartTime);
                });

        long totalCost = System.currentTimeMillis() - startTime;
        log.info(BATCH_SUMMARY, tenantId, totalCost, totalSuccess.get(), totalFail.get(), totalDevices > 0 ? totalCost / totalDevices : 0);
    }

    /**
     * 获取分页设备数据,查询失败返回空列表。
     *
     * @param currentPage 当前页码(从1开始)
     * @see DeviceQueryService#getPage(PageParams)
     */
    private List<DeviceResultVO> fetchDevicePage(int currentPage) {
        ArgumentAssert.isTrue(currentPage >= 1, "currentPage must be greater than or equal to 1");
        PageParams<DevicePageQuery> params = new PageParams<>(currentPage, PAGE_SIZE);
        params.setModel(DevicePageQuery.builder().build());
        return Optional.ofNullable(deviceQueryService.getPage(params))
                .map(IPage::getRecords)
                .orElse(Collections.emptyList());
    }

    /**
     * 处理设备批次数据,只缓存设备本身字段;产品 / 物模型走各自独立缓存,消费方按需读取。
     */
    private void processDevicesBatch(Long tenantId,
                                     List<DeviceResultVO> devices,
                                     AtomicInteger totalSuccess,
                                     AtomicInteger totalFail) {
        AtomicInteger pageSuccess = new AtomicInteger();
        AtomicInteger pageFail = new AtomicInteger();

        List<CompletableFuture<Void>> deviceFutures = devices.stream()
                .map(device -> contextAwareExecutor.executeWithContext(() -> {
                    long deviceStart = System.currentTimeMillis();
                    try {
                        DeviceCacheVO cacheVO = BeanPlusUtil.toBeanIgnoreError(device, DeviceCacheVO.class);
                        cacheVO.setTenantId(tenantId);

                        // 更新缓存
                        this.cacheDeviceBasedOnIdentification(cacheVO);
                        this.cacheDeviceBasedOnClientId(cacheVO);
                        pageSuccess.incrementAndGet();
                    } catch (Exception e) {
                        pageFail.incrementAndGet();
                        log.error(DEVICE_DETAIL_LOG, tenantId, device.getDeviceIdentification(), "失败", System.currentTimeMillis() - deviceStart, e.getClass().getSimpleName() + ":" + e.getMessage(), e);
                    }
                    return null;
                }))
                .map(future -> future.thenApply(result -> (Void) null))
                .toList();

        CompletableFuture.allOf(deviceFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    totalSuccess.addAndGet(pageSuccess.get());
                    totalFail.addAndGet(pageFail.get());
                })
                .exceptionally(ex -> {
                    log.error("批次处理异常", ex);
                    return null;
                });
    }


    /**
     * 刷新单个设备的缓存。
     *
     * @param deviceIdentification 设备标识,不能为空
     * @return 刷新是否成功
     * @see DeviceQueryService#findDeviceCacheVO(String)
     */
    public boolean refreshDeviceCache(String deviceIdentification) {
        try {
            log.info("开始刷新{}设备缓存: {}", ContextUtil.getTenantId(), deviceIdentification);
            ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification is null");
            // 获取设备信息 ── DeviceService.findDeviceCacheVO 已下线,统一走 DeviceQueryService (leaf 服务)
            Optional<DeviceCacheVO> deviceCacheVOOptional = deviceQueryService.findDeviceCacheVO(deviceIdentification);
            if (deviceCacheVOOptional.isEmpty()) {
                log.warn("未找到设备信息: {}", deviceIdentification);
                return false;
            }
            // 更新缓存
            this.cacheDeviceBasedOnIdentification(deviceCacheVOOptional.get());
            this.cacheDeviceBasedOnClientId(deviceCacheVOOptional.get());
            log.info("设备缓存刷新成功: {}", deviceIdentification);
            return true;
        } catch (Exception e) {
            log.error("刷新设备缓存失败: {}", deviceIdentification, e);
            return false;
        }
    }

    /**
     * 仅从 DB 加载设备 VO,不写缓存 ── 供 LinkCacheDataHelper#getDeviceCacheVO 的 read-through 回源使用,
     * 写缓存由 CachePlusUtil 的 loader 链统一负责避免双写竞态。参数空 / DB 不存在 / 转换异常返 null
     * (由调用方决定是否缓存 null sentinel 防穿透)。上层 helper 不允许越级直接调 {@link DeviceQueryService},
     * 缓存策略集中在 CacheService 一层。
     *
     * @param deviceIdOrClientId 设备标识或客户端标识
     * @return 设备缓存 VO;失败返 null
     */
    public DeviceCacheVO loadDeviceCacheFromDb(String deviceIdOrClientId) {
        if (StrUtil.isBlank(deviceIdOrClientId)) {
            return null;
        }
        try {
            DeviceCacheVO vo = deviceQueryService.findDeviceCacheVO(deviceIdOrClientId).orElse(null);
            if (vo == null) {
                log.warn("[device-fallback] DB miss deviceIdOrClientId={} tenantId={}",
                        deviceIdOrClientId, ContextUtil.getTenantId());
                return null;
            }
            log.info("[device-fallback] resolved deviceIdOrClientId={} tenantId={} productIdentification={} boundVersionNo={}",
                    deviceIdOrClientId, ContextUtil.getTenantId(),
                    vo.getProductIdentification(), vo.getBoundProductVersionNo());
            return vo;
        } catch (Exception e) {
            log.error("[device-fallback] load failed deviceIdOrClientId={}", deviceIdOrClientId, e);
            return null;
        }
    }


    /**
     * 按 deviceIdentification 维度缓存 DeviceCacheVO。
     */
    public void cacheDeviceBasedOnIdentification(DeviceCacheVO deviceCacheVO) {
        CacheKey deviceIdentKey = DeviceCacheKeyBuilder.build(deviceCacheVO.getDeviceIdentification());
        cachePlusOps.del(deviceIdentKey);
        cachePlusOps.set(deviceIdentKey, deviceCacheVO);
    }

    /**
     * 按 clientId 维度缓存 DeviceCacheVO。
     */
    public void cacheDeviceBasedOnClientId(DeviceCacheVO deviceCacheVO) {
        CacheKey clientIdKey = DeviceCacheKeyBuilder.build(deviceCacheVO.getClientId());
        cachePlusOps.del(clientIdKey);
        cachePlusOps.set(clientIdKey, deviceCacheVO);
    }


}
