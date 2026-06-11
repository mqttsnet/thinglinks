package com.mqttsnet.thinglinks.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;

import java.util.List;
import java.util.Optional;

/**
 * 设备基础信息只读查询 Service,仅持有 DeviceManager、零下游 Service 依赖、类图为 DAG。
 * 专供 cache helper / 跨域 Service 反向查询,规避 device ↔ cache helper / action 之间的构造期循环依赖。
 *
 * @author mqttsnet
 * @since 2026-05-18
 */
public interface DeviceQueryService {

    /**
     * 根据设备 ID 或客户端 ID 查询设备缓存信息,未命中返回 {@link Optional#empty()}。
     *
     * @param deviceIdOrClientId 设备 ID 或客户端 ID
     * @return {@link Optional} 包裹的设备缓存信息;未命中返回 {@link Optional#empty()}
     */
    Optional<DeviceCacheVO> findDeviceCacheVO(String deviceIdOrClientId);

    /**
     * 统计绑定指定 CA 证书序列号的设备总数。
     *
     * @param certSerialNumber CA 证书序列号
     * @return 设备总数
     */
    Long countByCertSerialNumber(String certSerialNumber);

    /**
     * 统计绑定指定 CA 证书序列号且当前在线的设备数。
     *
     * @param certSerialNumber CA 证书序列号
     * @return 在线设备数
     */
    Long countOnlineByCertSerialNumber(String certSerialNumber);

    /**
     * 查询绑定指定 CA 证书序列号的设备 Top N(按最后心跳时间倒序)。
     *
     * @param certSerialNumber CA 证书序列号
     * @param limit 返回条数上限
     * @return 设备列表(按最后心跳时间倒序)
     */
    List<Device> listTopBoundDevicesByCertSerialNumber(String certSerialNumber, int limit);

    /**
     * 查询绑定指定 CA 证书序列号的全量设备列表(不限条数),专供 CA 吊销失效缓存场景(需清全部关联设备)。
     * 跨域调用走 Service 层触发 {@code @DS(BASE_TENANT)} 切租户库,禁止直接调 {@code DeviceManager}(无 @DS 会 fallback 默认库)。
     *
     * @param certSerialNumber CA 证书序列号
     * @return 设备列表(空字符串返空列表)
     */
    List<Device> listByCertSerialNumber(String certSerialNumber);

    /**
     * 设备总数(全租户内,跨产品),供缓存预热 / 批次刷新计算分页总数。
     *
     * @return 设备总数
     */
    Long findDeviceTotal();

    /**
     * 设备分页查询(返 {@link DeviceResultVO}),供缓存预热 / 批次刷新拉取。
     * 与 DeviceService.getPage 等价但在 leaf 层,允许下游 cache 类不引入 DeviceService(避免 cache → service → linkCacheHelper → cache 成环)。
     *
     * @param params 设备分页查询参数
     * @return 设备分页结果(元素为 {@link DeviceResultVO})
     */
    IPage<DeviceResultVO> getPage(PageParams<DevicePageQuery> params);

    /**
     * 拉某产品下全量设备的 deviceIdentification 清单(去重,过滤空值),专供灰度 percent 模式做一致性哈希取子集。
     * 只查 identification 列避免整表入内存;跨域调用走 Service 层触发 {@code @DS(BASE_TENANT)} 切租户库,不直接调 {@code DeviceManager}。
     *
     * @param productIdentification 产品标识
     * @return 设备识别码列表(去重,过滤 blank);产品下无设备返空列表
     */
    List<String> listDeviceIdentificationsByProduct(String productIdentification);
}
