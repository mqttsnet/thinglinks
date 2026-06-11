package com.mqttsnet.thinglinks.cache.helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.cache.utils.CachePlusUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.cache.device.DeviceAclRuleCacheService;
import com.mqttsnet.thinglinks.cache.device.DeviceCacheService;
import com.mqttsnet.thinglinks.cache.product.ProductCacheService;
import com.mqttsnet.thinglinks.cache.product.ProductModelCacheService;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceAclRuleCacheVO;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceActionCacheVO;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.common.cache.link.collectionpool.DeviceActionCollectionPoolCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.collectionpool.DeviceDataCollectionPoolCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceAclRuleCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.ota.OtaTaskExecutorOffsetCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.product.ProductCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.product.ProductModelCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.product.ProductModelSuperTableCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

/**
 * 微服务级缓存聚合 / read-through 统一出口。调用层级:本类 → 各域 CacheService → 域内 Service / QueryService → Manager。
 * 凡走缓存的查询都从本类入口,纯 DB 直查走 Service。
 *
 * @author ShiHuan Sun
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LinkCacheDataHelper {

    private final CachePlusOps cachePlusOps;
    /**
     * read-through 统一入口 ── {@link CachePlusUtil#getOrLoad} 包装 cachePlusOps + 类型转换 + Optional 返值,
     * 所有需要回源 DB 的 get* 方法应使用此工具,而非手写 cachePlusOps.get(key, loader, ...).
     */
    private final CachePlusUtil cachePlusOpsUtil;

    /** 设备缓存 DB 回源依赖,调 {@link DeviceCacheService#loadDeviceCacheFromDb(String)}. */
    private final DeviceCacheService deviceCacheService;
    /** 物模型缓存 DB 回源依赖,调 {@link ProductModelCacheService#loadProductModelFromDbByVersionNo(String, String)}. */
    private final ProductModelCacheService productModelCacheService;

    /** 产品(基础信息)缓存 DB 回源依赖,调 {@link ProductCacheService#loadProductFromDb(String)}. */
    private final ProductCacheService productCacheService;

    /** ACL 规则缓存 DB 回源依赖,调 {@link DeviceAclRuleCacheService#loadEnabledAclRulesFromDb(String, String)}. */
    private final DeviceAclRuleCacheService deviceAclRuleCacheService;


    /**
     * 读设备缓存,cache miss 自动回源 DB(read-through,{@link CachePlusUtil#getOrLoad})。
     * 回源走 {@link DeviceCacheService#loadDeviceCacheFromDb(String)},不直接调 QueryService,保持调用层级。
     *
     * @param deviceIdOrClientId deviceIdentification 或 clientId,不能为空
     * @return 设备缓存;DB 也查不到时返空
     */
    @DS(DsConstant.BASE_TENANT)
    public Optional<DeviceCacheVO> getDeviceCacheVO(String deviceIdOrClientId) {
        CacheKey cacheKey = DeviceCacheKeyBuilder.build(deviceIdOrClientId);
        return cachePlusOpsUtil.getOrLoad(
            cacheKey,
            (k) -> deviceCacheService.loadDeviceCacheFromDb(deviceIdOrClientId),
            DeviceCacheVO.class,
            false);
    }

    /**
     * 从 Redis 删除设备缓存。
     *
     * @param deviceIdOrClientId deviceIdentification 或 clientId,不能为空
     */
    @DS(DsConstant.BASE_TENANT)
    public void deleteDeviceCacheVO(String deviceIdOrClientId) {
        CacheKey cacheKey = DeviceCacheKeyBuilder.build(deviceIdOrClientId);
        cachePlusOps.del(cacheKey);
        log.info("Device cache deleted for deviceIdOrClientId: {}", deviceIdOrClientId);
    }


    /**
     * 读产品(基础信息)缓存,cache miss 自动回源 DB(read-through)。
     * 取产品基础元数据(不含物模型结构);需要物模型快照走 resolveProductModelByVersionNo。
     *
     * @param productIdentification 产品标识,不能为空
     * @return 产品基础信息缓存;DB 也查不到时返空
     * @see ProductCacheService#loadProductFromDb(String)
     */
    public Optional<ProductCacheVO> getProductCacheVO(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return Optional.empty();
        }
        CacheKey cacheKey = ProductCacheKeyBuilder.build(productIdentification);
        return cachePlusOpsUtil.getOrLoad(
            cacheKey,
            (k) -> productCacheService.loadProductFromDb(productIdentification),
            ProductCacheVO.class,
            false);
    }

    /**
     * 失效产品(基础信息)缓存 ── 产品 CRUD 或发布事件后调用,确保下次读 read-through 回填新值。
     *
     * @param productIdentification 产品标识
     */
    public void deleteProductCacheVO(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return;
        }
        CacheKey cacheKey = ProductCacheKeyBuilder.build(productIdentification);
        cachePlusOps.del(cacheKey);
        log.info("[product-cache] cache deleted productIdentification={}", productIdentification);
    }


    /**
     * 读指定设备生效的 ACL 规则 ── Hash 模式 read-through。
     * 命中 → HGET 返;miss → DB 加载(产品级 + 设备级合并 + 排序)→ HSET 回填。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @return 生效的 ACL 规则列表;无规则返空列表
     */
    @DS(DsConstant.BASE_TENANT)
    public List<DeviceAclRuleCacheVO> getDeviceAclRules(String productIdentification, String deviceIdentification) {
        if (StrUtil.hasBlank(productIdentification, deviceIdentification)) {
            return Collections.emptyList();
        }
        CacheHashKey hashFieldKey = DeviceAclRuleCacheKeyBuilder.buildHashFieldKey(productIdentification, deviceIdentification);
        try {
            CacheResult<Object> cached = cachePlusOps.hGet(hashFieldKey);
            if (cached != null && !cached.isNullVal() && cached.getRawValue() instanceof List<?> rawList) {
                return rawList.stream()
                    .map(v -> v instanceof DeviceAclRuleCacheVO vo
                        ? vo
                        : BeanPlusUtil.toBeanIgnoreError(v, DeviceAclRuleCacheVO.class))
                    .toList();
            }
        } catch (Exception e) {
            // 反序列化异常兜底 ── 不挂请求,降级回 DB
            log.warn("[acl-cache] hGet deserialize failed key={} field={}, fallback to DB", hashFieldKey.getKey(), deviceIdentification, e);
        }
        // miss / 异常 → DB
        List<DeviceAclRuleCacheVO> rules = deviceAclRuleCacheService.loadEnabledAclRulesFromDb(productIdentification, deviceIdentification);
        // 非空回填(空 list 不写,避免占用 field + 让下次有规则时立即命中)
        if (!rules.isEmpty()) {
            try {
                cachePlusOps.hSet(hashFieldKey, rules);
            } catch (Exception e) {
                log.warn("[acl-cache] hSet failed key={} field={}", hashFieldKey.getKey(), deviceIdentification, e);
            }
        }
        return rules;
    }

    /**
     * 按产品维度失效 ── DEL 整个 hash,O(1) 清掉该产品所有设备的 field。
     *
     * @param productIdentification 产品标识
     */
    @DS(DsConstant.BASE_TENANT)
    public void evictAclCacheByProduct(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return;
        }
        CacheKey hashKey = DeviceAclRuleCacheKeyBuilder.buildHashKey(productIdentification);
        cachePlusOps.del(hashKey);
        log.info("[acl-cache] evict by product key={}", hashKey.getKey());
    }

    /**
     * 按设备维度失效 ── HDEL 单 field,O(1) 只清该设备。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     */
    @DS(DsConstant.BASE_TENANT)
    public void evictAclCacheByDevice(String productIdentification, String deviceIdentification) {
        if (StrUtil.hasBlank(productIdentification, deviceIdentification)) {
            return;
        }
        CacheHashKey hashFieldKey = DeviceAclRuleCacheKeyBuilder.buildHashFieldKey(productIdentification, deviceIdentification);
        cachePlusOps.hDel(hashFieldKey);
        log.info("[acl-cache] evict by device key={} field={}", hashFieldKey.getKey(), deviceIdentification);
    }

    /**
     * 按 (productIdentification, versionNo) 解析物模型缓存 ── 取产品物模型的唯一入口。
     * 版本快照不可变 + 长 TTL(7 天),旧版本设备永远读到旧版本快照,不存在"发布新版本后旧缓存脏";
     * cache miss 走 read-through {@link ProductModelCacheService#loadProductModelFromDbByVersionNo} 从 product_version 表回源。
     * versionNo 必须非空:传空值返 empty + warn,不做隐式 fallback ── 隐式 fallback 在灰度场景会让设备读到错误版本快照。
     *
     * @param productIdentification 产品标识,不能为空
     * @param versionNo             版本序号(设备绑定的或产品当前激活的),不能为空
     * @return 物模型缓存 VO;查不到时返空
     * @see ProductModelCacheService#loadProductModelFromDbByVersionNo(String, String)
     */
    public Optional<ProductModelCacheVO> resolveProductModelByVersionNo(String productIdentification, String versionNo) {
        if (StrUtil.isBlank(productIdentification)) {
            return Optional.empty();
        }
        if (StrUtil.isBlank(versionNo)) {
            log.warn("[product-model-version] versionNo is blank, return empty (caller should fallback by " +
                    "device.boundProductVersionNo or product.activeVersionNo) productIdentification={}",
                productIdentification);
            return Optional.empty();
        }
        CacheKey cacheKey = ProductModelCacheKeyBuilder.build(productIdentification, versionNo);
        return cachePlusOpsUtil.getOrLoad(
            cacheKey,
            (k) -> productModelCacheService.loadProductModelFromDbByVersionNo(productIdentification, versionNo),
            ProductModelCacheVO.class,
            false);
    }

    /**
     * 解析设备所属产品的 protocolType ── 共享入口,避免各业务模块自己手写。
     *
     * @param productIdentification 产品标识(必填;空值直接返空)
     * @param versionNo             设备绑定的版本序号(可空 ── 缺失时直接走基础元数据回退)
     * @return protocolType 字符串;两步都拿不到返空
     */
    public Optional<String> resolveProtocolType(String productIdentification, String versionNo) {
        if (StrUtil.isBlank(productIdentification)) {
            return Optional.empty();
        }
        Optional<String> fromSnapshot = (StrUtil.isBlank(versionNo))
            ? Optional.empty()
            : resolveProductModelByVersionNo(productIdentification, versionNo)
            .map(ProductModelCacheVO::getProtocolType);
        return fromSnapshot.or(() -> getProductCacheVO(productIdentification)
            .map(ProductCacheVO::getProtocolType));
    }

    /**
     * 精确删除某一版本的物模型缓存。正常不需调用(版本快照不可变,缓存值天然有效),
     * 仅在异常数据回滚(如手工改了 product_version.product_snapshot_json)或事件驱动主动刷新场景使用。
     *
     * @param productIdentification 产品标识
     * @param versionNo             版本序号
     */
    public void deleteProductModelCacheVOByVersionNo(String productIdentification, String versionNo) {
        if (StrUtil.hasBlank(productIdentification, versionNo)) {
            return;
        }
        CacheKey cacheKey = ProductModelCacheKeyBuilder.build(productIdentification, versionNo);
        cachePlusOps.del(cacheKey);
        log.info("[product-model-version] cache deleted productIdentification={} versionNo={}",
            productIdentification, versionNo);
    }


    /**
     * 读取设备子表 TDengine 表结构缓存(超级表 describe 结果),按 (pi, versionNo, serviceCode, deviceIdentification) 维度。
     * cache-only,故意不做 read-through 回源:数据真相在 TDengine 运行时表结构(非 RDBMS),回源需注入 TdsFacade,
     * 会让本 helper 跨域引用 tds feign client 拉低边界纯净度。cache miss 时由调用方主动查 TDengine 表结构
     * (不存在则触发子表初始化)再调 setProductModelSuperTableCacheVO 回填。
     *
     * @param productIdentification 产品标识
     * @param versionNo             版本序号(取设备 boundProductVersionNo),不能为空
     * @param serviceCode           服务码
     * @param deviceIdentification  设备标识
     * @return SuperTable 字段描述列表;cache miss 返空列表(由业务方触发 DDL + 回填)
     */
    public List<SuperTableDescribeVO> getProductModelSuperTableCacheVO(String productIdentification, String versionNo,
                                                                       String serviceCode, String deviceIdentification) {
        String cacheProductModelSuperTableKey = ProductModelSuperTableCacheKeyBuilder
            .build(productIdentification, versionNo, serviceCode, deviceIdentification).getKey();
        CacheResult<Object> objectCacheResult;

        try {
            objectCacheResult = cachePlusOps.get(cacheProductModelSuperTableKey);
        } catch (Exception e) {
            log.error("Error fetching from cache for key: {}", cacheProductModelSuperTableKey, e);
            return Collections.emptyList();
        }

        if (objectCacheResult == null || objectCacheResult.getRawValue() == null) {
            log.warn("The product model super table is not in the cache for key: {}", cacheProductModelSuperTableKey);
            return Collections.emptyList();
        }
        Object rawValue = objectCacheResult.getRawValue();
        // Check if rawValue is of the expected type before attempting conversion
        if (rawValue instanceof List<?> rawList) {
            if (rawList.isEmpty() || rawList.get(0) instanceof SuperTableDescribeVO) {
                return (List<SuperTableDescribeVO>) rawValue;
            } else {
                log.error("Unexpected type in cached value for key: {}", cacheProductModelSuperTableKey);
                return Collections.emptyList();
            }
        } else {
            log.error("Cached value is not a list for key: {}", cacheProductModelSuperTableKey);
            return Collections.emptyList();
        }
    }

    /**
     * 设置设备子表 TDengine 表结构缓存(对应 getProductModelSuperTableCacheVO)。
     *
     * @param productIdentification 产品标识
     * @param versionNo             版本序号(与 TD 表名拼接的版本一致)
     * @param serviceCode           服务码
     * @param deviceIdentification  设备标识
     * @param superTableDescribeOpt SuperTable 字段描述列表
     */
    public void setProductModelSuperTableCacheVO(String productIdentification, String versionNo,
                                                 String serviceCode, String deviceIdentification,
                                                 List<SuperTableDescribeVO> superTableDescribeOpt) {
        CacheKey cacheProductModelSuperTableKey = ProductModelSuperTableCacheKeyBuilder
            .build(productIdentification, versionNo, serviceCode, deviceIdentification);
        cachePlusOps.del(cacheProductModelSuperTableKey);
        cachePlusOps.set(cacheProductModelSuperTableKey, superTableDescribeOpt);
    }

    /**
     * 产品维度的超表结构缓存设置(预热场景:产品发布后,LinkJobHandler 调度任务刷所有版本的超表)。
     *
     * @param productIdentification 产品标识
     * @param versionNo             版本序号
     * @param serviceCode           服务码
     * @param superTableDescribeOpt SuperTable 字段描述列表
     */
    public void setProductModelSuperTableCacheVO(String productIdentification, String versionNo,
                                                 String serviceCode,
                                                 List<SuperTableDescribeVO> superTableDescribeOpt) {
        CacheKey cacheKey = ProductModelSuperTableCacheKeyBuilder
            .build(productIdentification, versionNo, serviceCode);
        cachePlusOps.del(cacheKey);
        cachePlusOps.set(cacheKey, superTableDescribeOpt);
    }

    /**
     * 往 Redis sorted set 写设备数据采集池缓存,超出最大条数时淘汰最旧数据。参数均不能为空。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param productResultVO       待写入的采集数据
     */
    public void setDeviceDataCollectionPoolCacheVO(String productIdentification, String deviceIdentification, ProductResultVO productResultVO) {
        log.info("Setting device data collection pool cache - Product ID: {}, Device ID: {}, Data: {}",
            productIdentification, deviceIdentification, productResultVO);

        try {
            CacheKey cacheKey = DeviceDataCollectionPoolCacheKeyBuilder.build(productIdentification, deviceIdentification);
            log.info("Generated CacheKey: {}", cacheKey);

            long timestamp = DateUtils.microsecondStampL();
            log.info("Current timestamp: {}", timestamp);

            // 获取最大缓存条数
            Long maxCacheSize = new DeviceDataCollectionPoolCacheKeyBuilder().getMaxCacheSize();

            cachePlusOps.zAdd(cacheKey, productResultVO, timestamp);
            log.info("Data added to cache - CacheKey: {}, ProductResult: {}", cacheKey, productResultVO);

            cachePlusOps.expire(cacheKey);
            log.info("Set cache expiration for CacheKey: {}", cacheKey);

            // 获取当前 Sorted Set 中的元素数量
            long size = cachePlusOps.zCard(cacheKey);
            log.info("Current cache size: {}", size);

            // 如果 Sorted Set 中的元素超过了最大条数，则删除最旧的数据
            if (size > maxCacheSize) {
                // 删除最旧的 excessCount 条数据
                long excessCount = size - maxCacheSize;
                // 删除最旧的 excessCount 条数据
                cachePlusOps.zRem(cacheKey, 0, excessCount - 1);
                log.info("Removed {} oldest data to maintain size limit of {}", excessCount, maxCacheSize);
            }

        } catch (Exception e) {
            log.error("Error setting device data collection pool cache - Product ID: {}, Device ID: {}, Data: {}",
                productIdentification, deviceIdentification, productResultVO, e);
            throw e;
        }
    }

    /**
     * 按 score 区间读设备数据采集池缓存,clear=true 时读后清掉该区间。返回 score → ProductResultVO 映射。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param startScore            score 区间起值
     * @param endScore              score 区间止值
     * @param clear                 是否读后清除
     * @return score → ProductResultVO 映射;无数据返空 map
     */
    public Map<Long, ProductResultVO> getDeviceDataCollectionPoolCacheVO(String productIdentification, String deviceIdentification, double startScore, double endScore, boolean clear) {
        if (startScore > endScore) {
            log.error("Start score is greater than end score for product: {} and device: {}, range: {} - {}", productIdentification, deviceIdentification, startScore, endScore);
            return Collections.emptyMap();
        }
        CacheKey cacheKey = DeviceDataCollectionPoolCacheKeyBuilder.build(productIdentification, deviceIdentification);
        Set<ZSetOperations.TypedTuple<Object>> resultSet;
        try {
            resultSet = cachePlusOps.zReverseRangeByScoreWithScores(cacheKey, startScore, endScore);
        } catch (Exception e) {
            log.error("Error retrieving from cache for product: {} and device: {}, range: {} - {}", productIdentification, deviceIdentification, startScore, endScore, e);
            return Collections.emptyMap();
        }

        if (resultSet.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, ProductResultVO> resultMap;
        try {
            resultMap = resultSet.stream()
                .filter(typedTuple -> typedTuple.getValue() instanceof ProductResultVO)
                .collect(Collectors.toMap(
                    // 将Double的二进制表示转换为Long
                    tuple -> Objects.requireNonNull(tuple.getScore()).longValue(),
                    tuple -> (ProductResultVO) tuple.getValue(),
                    (existing, replacement) -> existing));
        } catch (ClassCastException e) {
            log.error("Cache contains non-ProductResultVO objects for product: {} and device: {}, range: {} - {}",
                productIdentification, deviceIdentification, startScore, endScore, e);
            return Collections.emptyMap();
        }


        if (clear) {
            evictDeviceDataCollectionPoolCacheVO(productIdentification, deviceIdentification, startScore, endScore);
        }

        return resultMap;
    }


    /**
     * 删除指定 score 区间(闭区间)内的设备数据采集池缓存。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param startScore            score 区间起值
     * @param endScore              score 区间止值
     */
    public void evictDeviceDataCollectionPoolCacheVO(String productIdentification, String deviceIdentification, double startScore, double endScore) {
        CacheKey cacheKey = DeviceDataCollectionPoolCacheKeyBuilder.build(productIdentification, deviceIdentification);
        cachePlusOps.zRemRangeByScore(cacheKey, startScore, endScore);
    }


    /**
     * 往 Redis sorted set 写设备指令缓存。参数均不能为空。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param deviceActionCacheVO   待写入的设备指令
     */
    public void setDeviceActionCacheVO(String productIdentification, String deviceIdentification, DeviceActionCacheVO deviceActionCacheVO) {
        CacheKey cacheKey = DeviceActionCollectionPoolCacheKeyBuilder.build(productIdentification, deviceIdentification);
        cachePlusOps.zAdd(cacheKey, deviceActionCacheVO, DateUtils.microsecondStampL());
        cachePlusOps.expire(cacheKey);
    }

    /**
     * 读设备指令缓存全量,clear=true 时读后清空。返回 score → DeviceActionCacheVO 映射。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param clear                 是否读后清除
     * @return score → DeviceActionCacheVO 映射;无数据返空 map
     */
    public Map<Long, DeviceActionCacheVO> getDeviceActionCacheVO(String productIdentification, String deviceIdentification, boolean clear) {
        CacheKey cacheKey = DeviceActionCollectionPoolCacheKeyBuilder.build(productIdentification, deviceIdentification);
        Set<ZSetOperations.TypedTuple<Object>> resultSet;
        try {
            resultSet = cachePlusOps.zReverseRangeByScoreWithScores(cacheKey, Double.MIN_VALUE, Double.MAX_VALUE);
        } catch (Exception e) {
            log.error("Error retrieving from cache for product: {} and device: {}", productIdentification, deviceIdentification, e);
            return Collections.emptyMap();
        }

        if (resultSet.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, DeviceActionCacheVO> resultMap;
        try {
            resultMap = resultSet.stream()
                .filter(typedTuple -> typedTuple.getValue() instanceof DeviceActionCacheVO)
                .collect(Collectors.toMap(
                    tuple -> Objects.requireNonNull(tuple.getScore()).longValue(),
                    typedTuple -> (DeviceActionCacheVO) typedTuple.getValue(),
                    (existing, replacement) -> existing
                ));
        } catch (ClassCastException e) {
            log.error("Cache contains non-DeviceActionCacheVO objects for product: {} and device: {}", productIdentification, deviceIdentification, e);
            return Collections.emptyMap();
        }

        if (clear) {
            double startScore = Collections.min(resultMap.keySet());
            double endScore = Collections.max(resultMap.keySet());
            evictDeviceActionCacheVO(productIdentification, deviceIdentification, startScore, endScore);
        }

        return resultMap;
    }

    /**
     * 删除指定 score 区间(闭区间)内的设备指令缓存。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param startScore            score 区间起值
     * @param endScore              score 区间止值
     */
    public void evictDeviceActionCacheVO(String productIdentification, String deviceIdentification, double startScore, double endScore) {
        CacheKey cacheKey = DeviceActionCollectionPoolCacheKeyBuilder.build(productIdentification, deviceIdentification);
        cachePlusOps.zRemRangeByScore(cacheKey, startScore, endScore);
    }


    /**
     * 读 OTA 任务执行器的时间偏移量(格式 yyyy-MM-dd HH:mm:ss),缓存无值时返空。
     *
     * @return 时间偏移量;缓存无值返空
     */
    public Optional<String> getOtaTaskExecutorOffset() {
        CacheKey key = OtaTaskExecutorOffsetCacheKeyBuilder.build();
        CacheResult<Object> result = cachePlusOps.get(key);
        if (!result.isNullVal() && result.getRawValue() != null) {
            try {
                return Optional.of(result.getRawValue().toString());
            } catch (ClassCastException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * 写 OTA 任务执行器的时间偏移量(格式 yyyy-MM-dd HH:mm:ss)。
     *
     * @param offset 时间偏移量(格式 yyyy-MM-dd HH:mm:ss)
     */
    public void setOtaTaskExecutorOffset(String offset) {
        CacheKey cacheKey = OtaTaskExecutorOffsetCacheKeyBuilder.build();
        cachePlusOps.set(cacheKey, offset);
        cachePlusOps.expire(cacheKey);
    }

}
