package com.mqttsnet.thinglinks.device.manager;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.dto.DeviceOverviewResultDTO;
import com.mqttsnet.thinglinks.device.dto.DeviceVersionDTO;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.vo.query.DeviceDetailsPageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;

/**
 * <p>
 * 通用业务接口
 * 设备档案信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
public interface DeviceManager extends SuperManager<Device> {

    Long findDeviceTotal();

    /**
     * 根据设备ID查询设备信息。
     *
     * @param id 设备 ID
     * @return 设备信息;查不到返 null
     */
    Device findOneById(Long id);

    /**
     * 根据客户端ID查询设备信息。
     *
     * @param clientId 客户端 ID
     * @return 设备信息;查不到返 null
     */
    Device findOneByClientId(String clientId);

    /**
     * 根据条件查询设备信息。
     *
     * @param query 设备分页查询条件
     * @return 设备列表
     */
    List<Device> getDevicList(DevicePageQuery query);


    /**
     * 根据设备标识查询设备信息。
     *
     * @param deviceIdentification 设备标识
     * @return 设备信息;查不到返 null
     */
    Device findOneByDeviceIdentification(String deviceIdentification);


    /**
     * 查询设备详情分页信息。
     *
     * @param params 设备详情分页查询参数
     * @return 设备详情分页结果
     */
    IPage<Device> getDeviceDetailsPage(PageParams<DeviceDetailsPageQuery> params);

    /**
     * 分页查询设备档案信息。
     *
     * @param params 设备分页查询参数
     * @return 设备档案分页结果
     */
    IPage<Device> getPage(PageParams<DevicePageQuery> params);

    /**
     * 查询设备概览信息。
     *
     * @param params 设备分页查询参数
     * @return 设备概览结果
     */
    DeviceOverviewResultDTO selectDeviceOverview(PageParams<DevicePageQuery> params);

    /**
     * 根据产品标识查询设备版本信息。
     *
     * @param productIdentification 产品标识
     * @return {@link Optional} 包裹的设备版本信息
     */
    Optional<DeviceVersionDTO> selectDeviceVersionsByProduct(String productIdentification);


    /**
     * 根据设备标识或客户端ID查询设备。
     *
     * @param deviceIdOrClientId 设备标识或客户端 ID
     * @return 设备信息;查不到返 null
     */
    Device findOneByIdOrClientId(String deviceIdOrClientId);

    /**
     * 根据设备ID或客户端ID查询设备缓存信息实体(仅含设备字段,不含 productCacheVO)。
     *
     * <p>productCacheVO 由 Service 层补全:Manager 严禁跨域调 {@code ProductManager}(无 @DS 会 fallback
     * 默认库 + 违反禁止跨层级调用)。tenantId 从 {@code ContextUtil.getTenantId()} 取,调用方需保证上下文已设置。</p>
     *
     * @param deviceIdOrClientId 设备ID或客户端ID
     * @return 设备缓存信息实体(无 productCacheVO);未命中返 {@link Optional#empty()}
     */
    Optional<DeviceCacheVO> findDeviceCacheVO(String deviceIdOrClientId);

    /**
     * 把指定产品下"绑定了 fromVersion"的所有设备改绑到 toVersion(回滚 / 灰度晋升用)。
     * fromVersion=null 等价于匹配所有 boundProductVersionNo IS NULL。
     *
     * @param productIdentification 产品标识
     * @param fromVersion 源版本号(可空,表示匹配所有)
     * @param toVersion 目标版本号
     * @return 影响行数
     */
    int bulkRebindByProductAndVersion(String productIdentification, String fromVersion, String toVersion);

    /**
     * 按设备标识列表改绑版本号(灰度白名单命中 / 用户手动改绑用)。
     *
     * @param deviceIdentifications 设备标识列表
     * @param toVersion 目标版本号
     * @return 影响行数
     */
    int bulkRebindByDeviceIdentifications(List<String> deviceIdentifications, String toVersion);

    /**
     * 把指定产品下所有设备改绑到 toVersion(全量发布用)。
     *
     * @param productIdentification 产品标识
     * @param toVersion 目标版本号
     * @return 影响行数
     */
    int bulkRebindByProduct(String productIdentification, String toVersion);

    /**
     * 兜底回填:单设备 bound_product_version_no IS NULL 时把它写为 version(数据上报兜底用)。
     * 严格 CAS 语义(WHERE ... AND bound_product_version_no IS NULL),不覆盖已存在的版本,防止与并发发布事件竞争。
     *
     * @param deviceIdentification 设备标识
     * @param version 兜底版本号(产品当前 activeVersionNo)
     * @return 影响行数(0 表示已有版本无需回填或设备不存在)
     */
    int fillBoundProductVersionIfBlank(String deviceIdentification, String version);
}