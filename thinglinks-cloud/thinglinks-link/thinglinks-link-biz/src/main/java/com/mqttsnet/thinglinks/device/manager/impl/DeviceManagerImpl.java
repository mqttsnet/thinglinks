package com.mqttsnet.thinglinks.device.manager.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StringUtils;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.dto.DeviceOverviewResultDTO;
import com.mqttsnet.thinglinks.device.dto.DeviceVersionDTO;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.manager.DeviceManager;
import com.mqttsnet.thinglinks.device.mapper.DeviceMapper;
import com.mqttsnet.thinglinks.device.vo.query.DeviceDetailsPageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用业务实现类 设备档案信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceManagerImpl extends SuperManagerImpl<DeviceMapper, Device> implements DeviceManager {

    private final DeviceMapper deviceMapper;


    @Override
    public Long findDeviceTotal() {
        return deviceMapper.selectCount(null);
    }

    @Override
    public Device findOneById(Long id) {
        if (id == null) {
            return null;
        }
        QueryWrap<Device> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(Device::getId, id);
        return deviceMapper.selectOne(queryWrap);
    }

    /**
     * 根据客户端ID查询设备信息
     *
     * @param clientId 客户端ID
     * @return 设备信息
     */
    @Override
    public Device findOneByClientId(String clientId) {
        QueryWrap<Device> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(clientId), Device::getClientId, clientId);
        return deviceMapper.selectOne(queryWrap);
    }

    /**
     * 根据条件查询设备信息
     *
     * @param query 查询条件
     * @return Device 设备信息
     */
    @Override
    public List<Device> getDevicList(DevicePageQuery query) {
        QueryWrap<Device> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(null != query.getId(), Device::getId, query.getId());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getIds()), Device::getId, query.getIds());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getClientId()), Device::getClientId, query.getClientId());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getDeviceName()), Device::getDeviceName, query.getDeviceName());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getPassword()), Device::getPassword, query.getPassword());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getDeviceIdentification()), Device::getDeviceIdentification, query.getDeviceIdentification());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getDeviceIdentificationList()), Device::getDeviceIdentification, query.getDeviceIdentificationList());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getProductIdentification()), Device::getProductIdentification, query.getProductIdentification());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getProductIdentificationList()), Device::getProductIdentification, query.getProductIdentificationList());
        queryWrap.lambda().eq(query.getDeviceStatus() != null, Device::getDeviceStatus, query.getDeviceStatus());
        queryWrap.lambda().eq(query.getConnectStatus() != null, Device::getConnectStatus, query.getConnectStatus());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getFwVersionList()), Device::getFwVersion, query.getFwVersionList());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getSwVersionList()), Device::getSwVersion, query.getSwVersionList());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getGatewayId()), Device::getGatewayId, query.getGatewayId());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getGatewayIdList()), Device::getGatewayId, query.getGatewayIdList());
        queryWrap.lambda().eq(query.getNodeType() != null, Device::getNodeType, query.getNodeType());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getNodeTypeList()), Device::getNodeType, query.getNodeTypeList());
        return deviceMapper.selectList(queryWrap);
    }

    /**
     * 根据设备标识查询设备信息
     *
     * @param deviceIdentification 设备标识
     * @return {@link Device} 设备信息
     */
    @Override
    public Device findOneByDeviceIdentification(String deviceIdentification) {
        QueryWrap<Device> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(deviceIdentification), Device::getDeviceIdentification, deviceIdentification);
        return deviceMapper.selectOne(queryWrap);
    }

    /**
     * 查询设备详情分页信息
     *
     * @param params 分页参数
     * @return {@link IPage<Device>} 设备详情分页信息
     */
    @Override
    public IPage<Device> getDeviceDetailsPage(PageParams<DeviceDetailsPageQuery> params) {
        IPage<Device> page = params.buildPage(Device.class);
        DeviceDetailsPageQuery paramsModel = params.getModel();

        // 构建查询条件
        LbQueryWrap<Device> wrap = Wraps.lbQ();
        wrap.eq(!StringUtils.isEmpty(paramsModel.getDeviceIdentification()), Device::getDeviceIdentification, paramsModel.getDeviceIdentification())
                .in(!CollUtil.isEmpty(paramsModel.getDeviceIdentificationList()), Device::getDeviceIdentification, paramsModel.getDeviceIdentificationList())
                .eq(!StringUtils.isEmpty(paramsModel.getDeviceName()), Device::getDeviceName, paramsModel.getDeviceName())
                .eq(!StringUtils.isEmpty(paramsModel.getProductIdentification()), Device::getProductIdentification, paramsModel.getProductIdentification())
                .eq(!StringUtils.isEmpty(paramsModel.getGatewayId()), Device::getGatewayId, paramsModel.getGatewayId())
                .in(!CollUtil.isEmpty(paramsModel.getGatewayIdList()), Device::getGatewayId, paramsModel.getGatewayIdList())
                .eq(paramsModel.getNodeType() != null, Device::getNodeType, paramsModel.getNodeType());

        return deviceMapper.selectPage(page, wrap);
    }


    @Override
    public IPage<Device> getPage(PageParams<DevicePageQuery> params) {
        IPage<Device> page = params.buildPage(Device.class);

        DevicePageQuery paramsModel = params.getModel();

        LbQueryWrap<Device> wrap = Wraps.lbQ();
        wrap.eq(StrUtil.isNotBlank(paramsModel.getDeviceIdentification()), Device::getDeviceIdentification, paramsModel.getDeviceIdentification())
                .in(!CollUtil.isEmpty(paramsModel.getDeviceIdentificationList()), Device::getDeviceIdentification, paramsModel.getDeviceIdentificationList())
                .eq(!StringUtils.isEmpty(paramsModel.getDeviceName()), Device::getDeviceName, paramsModel.getDeviceName())
                .eq(!StringUtils.isEmpty(paramsModel.getProductIdentification()), Device::getProductIdentification, paramsModel.getProductIdentification())
                .eq(!StringUtils.isEmpty(paramsModel.getGatewayId()), Device::getGatewayId, paramsModel.getGatewayId())
                .in(!CollUtil.isEmpty(paramsModel.getGatewayIdList()), Device::getGatewayId, paramsModel.getGatewayIdList())
                .in(CollUtil.isNotEmpty(paramsModel.getDeviceStatusList()), Device::getDeviceStatus, paramsModel.getDeviceStatusList())
                .eq(paramsModel.getDeviceStatus() != null, Device::getDeviceStatus, paramsModel.getDeviceStatus())
                .eq(paramsModel.getNodeType() != null, Device::getNodeType, paramsModel.getNodeType())
                .in(CollUtil.isNotEmpty(paramsModel.getNodeTypeList()), Device::getNodeType, paramsModel.getNodeTypeList());

        return deviceMapper.selectPage(page, wrap);
    }


    @Override
    public DeviceOverviewResultDTO selectDeviceOverview(PageParams<DevicePageQuery> params) {
        DevicePageQuery paramsModel = params.getModel();
        LambdaQueryWrapper<Device> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StrUtil.isNotBlank(paramsModel.getDeviceIdentification()), Device::getDeviceIdentification, paramsModel.getDeviceIdentification())
                .in(!CollUtil.isEmpty(paramsModel.getDeviceIdentificationList()), Device::getDeviceIdentification, paramsModel.getDeviceIdentificationList())
                .eq(!StringUtils.isEmpty(paramsModel.getDeviceName()), Device::getDeviceName, paramsModel.getDeviceName())
                .eq(!StringUtils.isEmpty(paramsModel.getProductIdentification()), Device::getProductIdentification, paramsModel.getProductIdentification())
                .eq(!StringUtils.isEmpty(paramsModel.getGatewayId()), Device::getGatewayId, paramsModel.getGatewayId())
                .in(!CollUtil.isEmpty(paramsModel.getGatewayIdList()), Device::getGatewayId, paramsModel.getGatewayIdList())
                .eq(paramsModel.getNodeType() != null, Device::getNodeType, paramsModel.getNodeType());

        return deviceMapper.selectDeviceOverview(wrapper);
    }

    /**
     * 根据设备标识或客户端ID查询设备
     *
     * @param deviceIdOrClientId 设备标识或客户端ID，为空时返回null
     * @return 设备实体对象，查询不到或参数为空时返回null，优先匹配设备标识
     */
    @Override
    public Device findOneByIdOrClientId(String deviceIdOrClientId) {
        if (StrUtil.isBlank(deviceIdOrClientId)) {
            return null;
        }
        LambdaQueryWrapper<Device> wrapper = Wrappers.lambdaQuery();
        wrapper.and(wq -> wq
                .eq(Device::getDeviceIdentification, deviceIdOrClientId)
                .or()
                .eq(Device::getClientId, deviceIdOrClientId)
        ).last("LIMIT 1");
        return getBaseMapper().selectOne(wrapper);
    }


    /**
     * 根据设备ID或客户端ID查询设备缓存信息实体(仅设备字段,不含 productCacheVO)。
     *
     * <p>productCacheVO 由 Service 层补全:Manager 严禁跨域调 {@code ProductManager}(无 @DS 会 fallback
     * 默认库 + 违反禁止跨层级调用)。tenantId 信任上游上下文,本方法不接收 tenantId 参数也不主动 set,消除隐藏副作用。</p>
     *
     * @param deviceIdOrClientId 设备ID或客户端ID
     * @return 设备缓存信息实体(无 productCacheVO);未命中返 {@link Optional#empty()}
     */
    @Override
    public Optional<DeviceCacheVO> findDeviceCacheVO(String deviceIdOrClientId) {
        Device device = this.findOneByIdOrClientId(deviceIdOrClientId);
        if (Objects.isNull(device)) {
            log.warn("设备档案信息不存在..deviceIdOrClientId:{}", deviceIdOrClientId);
            return Optional.empty();
        }
        DeviceCacheVO deviceCacheVO = BeanPlusUtil.toBeanIgnoreError(device, DeviceCacheVO.class);
        deviceCacheVO.setTenantId(ContextUtil.getTenantId());
        return Optional.of(deviceCacheVO);
    }

    /** 根据产品标识查询设备版本信息 */
    @Override
    public Optional<DeviceVersionDTO> selectDeviceVersionsByProduct(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return Optional.empty();
        }
        DeviceVersionDTO result = deviceMapper.selectDeviceVersionsByProduct(productIdentification);
        return Optional.ofNullable(result);
    }

    /**
     * {@inheritDoc}
     * <p>对应 SQL:UPDATE device SET bound_product_version_no = ? WHERE product_identification = ? AND bound_product_version_no = ?</p>
     */
    @Override
    public int bulkRebindByProductAndVersion(String productIdentification, String fromVersion, String toVersion) {
        if (StrUtil.isBlank(productIdentification) || StrUtil.isBlank(toVersion)) {
            return 0;
        }
        Device update = new Device();
        update.setBoundProductVersionNo(toVersion);

        LambdaQueryWrapper<Device> wrap = Wrappers.<Device>lambdaQuery()
                .eq(Device::getProductIdentification, productIdentification)
                .eq(StrUtil.isNotBlank(fromVersion), Device::getBoundProductVersionNo, fromVersion)
                // 无 fromVersion → 匹配"未绑定版本"的设备;字段默认值为空串,历史行可能为 NULL,两者都算未绑定
                .and(StrUtil.isBlank(fromVersion), w -> w
                        .isNull(Device::getBoundProductVersionNo)
                        .or().eq(Device::getBoundProductVersionNo, ""));
        return deviceMapper.update(update, wrap);
    }

    /**
     * {@inheritDoc}
     * <p>对应 SQL:UPDATE device SET bound_product_version_no = ? WHERE device_identification IN (...)</p>
     */
    @Override
    public int bulkRebindByDeviceIdentifications(List<String> deviceIdentifications, String toVersion) {
        if (CollUtil.isEmpty(deviceIdentifications) || StrUtil.isBlank(toVersion)) {
            return 0;
        }
        Device update = new Device();
        update.setBoundProductVersionNo(toVersion);

        LambdaQueryWrapper<Device> wrap = Wrappers.<Device>lambdaQuery()
                .in(Device::getDeviceIdentification, deviceIdentifications);
        return deviceMapper.update(update, wrap);
    }

    /**
     * {@inheritDoc}
     * <p>对应 SQL:UPDATE device SET bound_product_version_no = ? WHERE product_identification = ?</p>
     */
    @Override
    public int bulkRebindByProduct(String productIdentification, String toVersion) {
        if (StrUtil.isBlank(productIdentification) || StrUtil.isBlank(toVersion)) {
            return 0;
        }
        Device update = new Device();
        update.setBoundProductVersionNo(toVersion);

        LambdaQueryWrapper<Device> wrap = Wrappers.<Device>lambdaQuery()
                .eq(Device::getProductIdentification, productIdentification);
        return deviceMapper.update(update, wrap);
    }

    /**
     * {@inheritDoc}
     * <p>对应 SQL:UPDATE device SET bound_product_version_no = ? WHERE device_identification = ? AND (bound_product_version_no IS NULL OR bound_product_version_no = '')</p>
     * <p>CAS 语义 ── 只在当前值为空(NULL 或空串)时写入,避免覆盖并发发布事件刚刚改绑的版本号。
     * 字段默认值为空串,历史行可能为 NULL,两者都视为"未填充"。</p>
     */
    @Override
    public int fillBoundProductVersionIfBlank(String deviceIdentification, String version) {
        if (StrUtil.isBlank(deviceIdentification) || StrUtil.isBlank(version)) {
            return 0;
        }
        Device update = new Device();
        update.setBoundProductVersionNo(version);

        LambdaQueryWrapper<Device> wrap = Wrappers.<Device>lambdaQuery()
                .eq(Device::getDeviceIdentification, deviceIdentification)
                .and(w -> w.isNull(Device::getBoundProductVersionNo)
                        .or().eq(Device::getBoundProductVersionNo, ""));
        return deviceMapper.update(update, wrap);
    }

}
