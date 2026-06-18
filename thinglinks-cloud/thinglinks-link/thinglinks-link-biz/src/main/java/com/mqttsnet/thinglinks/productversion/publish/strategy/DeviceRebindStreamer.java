package com.mqttsnet.thinglinks.productversion.publish.strategy;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.service.DeviceQueryService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备改绑流式分批执行器 ── 大设备量产品<b>全量</b>改绑的统一执行层,替代"全量载入 JVM + 单条巨型 UPDATE"。
 *
 * <p>做法:游标分页({@link DeviceQueryService#listRebindCursorPageByProduct} 按主键 id 翻页,恒定内存)
 * 逐页拉设备 → 按主键 IN 小事务改绑({@link DeviceService#bulkRebindByIds},IN 有界、事务小、缓存按本批标识
 * 精确失效)。彻底规避:全量 ID 入内存 OOM、几十万元素 IN 列表、单条锁全表的巨型 UPDATE 三类大设备量风险。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRebindStreamer {

    /** 单页行数 ── 同时约束:单次 SELECT 行数、单批 UPDATE 的 IN 元素数、单事务大小。 */
    private static final int PAGE_SIZE = 2000;
    /** 翻页次数硬上限 ── 防御游标异常(如 id 不前进)导致死循环;2000×500000=10 亿行,足够任何真实产品。 */
    private static final int MAX_PAGES = 500000;

    private final DeviceQueryService deviceQueryService;
    private final DeviceService deviceService;

    /**
     * 全量改绑:产品下所有设备改到 toVersion(游标流式分批,恒定内存 / 有界 IN / 小事务)。
     *
     * @param productIdentification 产品标识
     * @param toVersion             目标版本号
     * @return 改绑设备数
     */
    public int streamFull(String productIdentification, String toVersion) {
        if (StrUtil.isBlank(productIdentification) || StrUtil.isBlank(toVersion)) {
            return 0;
        }
        long afterId = 0L;
        int total = 0;
        int pages = 0;
        while (pages++ < MAX_PAGES) {
            List<Device> page = deviceQueryService.listRebindCursorPageByProduct(productIdentification, afterId, PAGE_SIZE);
            if (CollUtil.isEmpty(page)) {
                break;
            }
            afterId = page.get(page.size() - 1).getId();

            List<Long> hitIds = new ArrayList<>(page.size());
            List<String> hitIdentifications = new ArrayList<>(page.size());
            for (Device device : page) {
                if (device.getId() == null) {
                    continue;
                }
                hitIds.add(device.getId());
                // 缓存失效按标识精确下发,空标识不入列(避免无意义 evict)
                if (StrUtil.isNotBlank(device.getDeviceIdentification())) {
                    hitIdentifications.add(device.getDeviceIdentification());
                }
            }
            if (!hitIds.isEmpty()) {
                total += deviceService.bulkRebindByIds(hitIds, hitIdentifications, toVersion);
            }
            if (page.size() < PAGE_SIZE) {
                break;
            }
        }
        log.info("[device-rebind-stream:FULL] product={} toVersion={} pages={} devicesRebound={}",
            productIdentification, toVersion, pages - 1, total);
        return total;
    }
}
