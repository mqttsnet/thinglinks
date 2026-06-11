package com.mqttsnet.thinglinks.device.service.impl;

import java.util.Collections;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceAclRuleCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.DeviceAclRule;
import com.mqttsnet.thinglinks.device.manager.DeviceAclRuleManager;
import com.mqttsnet.thinglinks.device.service.DeviceAclRuleQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备 ACL 规则只读查询 Service 实现。
 *
 * <p>仅持有 {@link DeviceAclRuleManager},零下游 Service 依赖,类图天然为 DAG。</p>
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceAclRuleQueryServiceImpl implements DeviceAclRuleQueryService {

    /**
     * 单设备生效 ACL 规则上限 ── SQL LIMIT 防御性截断,保护 broker 鉴权性能 + 缓存 hash field value 大小受控。
     * 触发截断时丢的是低优先级产品级规则(SQL 按"设备级在前 + priority asc"排序后取 top)。
     */
    private static final int MAX_RULES_PER_DEVICE = 200;

    private final DeviceAclRuleManager deviceAclRuleManager;

    @Override
    public List<DeviceAclRuleCacheVO> listTopEnabledRulesByDevice(String productIdentification, String deviceIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return Collections.emptyList();
        }
        // 产品级(deviceId IS NULL 或 '',兼容历史脏数据)∪ 该设备级(deviceId = ?);SQL ORDER BY 保证截断时丢低优先级产品级
        List<DeviceAclRule> rules = deviceAclRuleManager.list(
            Wraps.<DeviceAclRule>lbQ()
                .eq(DeviceAclRule::getProductIdentification, productIdentification)
                .eq(DeviceAclRule::getEnabled, Boolean.TRUE)
                .and(w -> w
                    .isNull(DeviceAclRule::getDeviceIdentification)
                    // LbQueryWrap.eq 对空串自动忽略 condition,只能用 apply 写裸 SQL 强制添加
                    .or().apply("device_identification = ''")
                    .or().eq(DeviceAclRule::getDeviceIdentification, deviceIdentification))
                .orderByDesc(DeviceAclRule::getRuleLevel)
                .orderByAsc(DeviceAclRule::getPriority)
                .last("LIMIT " + MAX_RULES_PER_DEVICE));
        if (rules.size() == MAX_RULES_PER_DEVICE) {
            log.warn("[acl] rules truncated to {} for productId={} deviceId={},low-priority product-level rules may be ignored — please reduce rule count",
                MAX_RULES_PER_DEVICE, productIdentification, deviceIdentification);
        }
        // SQL 已按"设备级 desc + priority asc"排好序,Java 不再 sort
        return rules.stream()
            .map(r -> BeanPlusUtil.toBeanIgnoreError(r, DeviceAclRuleCacheVO.class))
            .toList();
    }
}
