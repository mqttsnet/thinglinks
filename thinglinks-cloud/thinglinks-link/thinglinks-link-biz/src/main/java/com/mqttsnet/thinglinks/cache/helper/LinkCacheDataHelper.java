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
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
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
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceAclRuleCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.ota.OtaTaskExecutorOffsetCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.product.ProductCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.product.ProductModelCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.product.ProductModelSuperTableCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
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

    /**
     * 设备缓存 DB 回源依赖,调 {@link DeviceCacheService#loadDeviceCacheFromDb(String)}.
     */
    private final DeviceCacheService deviceCacheService;
    /**
     * 物模型缓存 DB 回源依赖,调 {@link ProductModelCacheService#loadProductModelFromDbByVersionNo(String, String)}.
     */
    private final ProductModelCacheService productModelCacheService;

    /**
     * 产品(基础信息)缓存 DB 回源依赖,调 {@link ProductCacheService#loadProductFromDb(String)}.
     */
    private final ProductCacheService productCacheService;

    /**
     * ACL 规则缓存 DB 回源依赖,调 {@link DeviceAclRuleCacheService#loadEnabledAclRulesFromDb(String, String)}.
     */
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
        return cachePlusOpsUtil.hGetOrLoadList(hashFieldKey,
            k -> deviceAclRuleCacheService.loadEnabledAclRulesFromDb(productIdentification, deviceIdentification),
            DeviceAclRuleCacheVO.class, false);
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
        CacheKey cacheKey = ProductModelSuperTableCacheKeyBuilder
            .build(productIdentification, versionNo, serviceCode, deviceIdentification);
        return cachePlusOpsUtil.getOrLoadList(cacheKey, k -> Collections.emptyList(), SuperTableDescribeVO.class, false);
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
        cachePlusOps.set(cacheProductModelSuperTableKey, JsonUtil.toJson(superTableDescribeOpt));
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
        cachePlusOps.set(cacheKey, JsonUtil.toJson(superTableDescribeOpt));
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
