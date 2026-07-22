package com.mqttsnet.thinglinks.device.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceAclRuleCacheVO;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.DeviceAclRule;
import com.mqttsnet.thinglinks.device.enumeration.ClientAclActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceAclRuleActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceAclRuleLevelEnum;
import com.mqttsnet.thinglinks.device.event.publisher.DeviceAclRuleEventPublisher;
import com.mqttsnet.thinglinks.device.event.source.DeviceAclRuleChangedEventSource;
import com.mqttsnet.thinglinks.device.manager.DeviceAclRuleManager;
import com.mqttsnet.thinglinks.device.service.DeviceAclRuleService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceAclCheckQuery;
import com.mqttsnet.thinglinks.device.vo.save.DeviceAclRuleSaveVO;
import com.mqttsnet.thinglinks.device.vo.update.DeviceAclRuleUpdateVO;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceAclCheckResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceInfoResultVO;
import com.mqttsnet.thinglinks.utils.acl.AclMatcherUtil;
import com.mqttsnet.thinglinks.utils.acl.AclTopicPatternPlaceholderReplacer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 业务实现类
 * 设备访问控制(ACL)规则表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-06-11 19:57:46
 * @create [2025-06-11 19:57:46] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceAclRuleServiceImpl extends SuperServiceImpl<DeviceAclRuleManager, Long, DeviceAclRule> implements DeviceAclRuleService {

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final DeviceAclRuleEventPublisher deviceAclRuleEventPublisher;

    @Override
    protected <UpdateVO> DeviceAclRule updateBefore(UpdateVO vo) {
        DeviceAclRuleUpdateVO updateVO = (DeviceAclRuleUpdateVO) vo;
        DeviceAclRuleLevelEnum level = validateAndNormalize(updateVO);
        requireNoPriorityConflict(level, updateVO);
        return super.updateBefore(updateVO);
    }

    @Override
    protected <SaveVO> DeviceAclRule saveBefore(SaveVO vo) {
        DeviceAclRuleSaveVO saveVO = (DeviceAclRuleSaveVO) vo;
        DeviceAclRuleLevelEnum level = validateAndNormalize(saveVO);
        requireNoPriorityConflict(level, saveVO);
        return super.saveBefore(saveVO);
    }

    @Override
    protected <SaveVO> void saveAfter(SaveVO saveVO, DeviceAclRule entity) {
        publishChanged(entity);
    }

    @Override
    protected <UpdateVO> void updateAfter(UpdateVO updateVO, DeviceAclRule entity) {
        publishChanged(entity);
    }

    /**
     * SuperServiceImpl.removeByIds 没有 after 钩子,显式 override 发变更事件,
     * 让 {@code DeviceAclRuleCacheEvictListener} 在事务提交后失效缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<Long> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return false;
        }
        List<DeviceAclRule> deleted = superManager.listByIds(idList);
        boolean ok = super.removeByIds(idList);
        if (ok && !deleted.isEmpty()) {
            deleted.forEach(this::publishChanged);
        }
        return ok;
    }

    /**
     * 发 ACL 规则变更事件 ── 由 {@code @TransactionalEventListener(AFTER_COMMIT)}
     * 异步消费触发缓存失效,事务回滚则不触发,保证缓存与 DB 一致。
     */
    private void publishChanged(DeviceAclRule rule) {
        if (rule == null || StrUtil.isBlank(rule.getProductIdentification())) {
            return;
        }
        // 非法 ruleLevel 转 null,listener fallback 走"产品级 evict" 兜底
        DeviceAclRuleLevelEnum level = DeviceAclRuleLevelEnum.fromValue(rule.getRuleLevel()).orElse(null);
        deviceAclRuleEventPublisher.publishDeviceAclRuleChangedEvent(
            DeviceAclRuleChangedEventSource.builder()
                .ruleId(rule.getId())
                .ruleLevel(level)
                .productIdentification(rule.getProductIdentification())
                .deviceIdentification(rule.getDeviceIdentification())
                .build());
    }

    // ============================== 内部:校验 / 规范化 ==============================

    /** save VO 重载 ── 调用方一行搞定。 */
    private DeviceAclRuleLevelEnum validateAndNormalize(DeviceAclRuleSaveVO vo) {
        return validateAndNormalize(vo.getRuleLevel(), vo.getProductIdentification(),
            vo::setDeviceIdentification, vo.getDeviceIdentification());
    }

    /** update VO 重载 ── 调用方一行搞定。 */
    private DeviceAclRuleLevelEnum validateAndNormalize(DeviceAclRuleUpdateVO vo) {
        return validateAndNormalize(vo.getRuleLevel(), vo.getProductIdentification(),
            vo::setDeviceIdentification, vo.getDeviceIdentification());
    }

    private DeviceAclRuleLevelEnum validateAndNormalize(Integer ruleLevel, String productIdentification,
                                                        Consumer<String> deviceIdSetter,
                                                        String currentDeviceId) {
        if (StrUtil.isBlank(productIdentification)) {
            throw BizException.wrap("产品标识不能为空");
        }
        DeviceAclRuleLevelEnum level = DeviceAclRuleLevelEnum.fromValue(ruleLevel)
            .orElseThrow(() -> BizException.wrap("规则级别非法:仅支持 0(产品级)或 1(设备级)"));
        if (level == DeviceAclRuleLevelEnum.PRODUCT_LEVEL) {
            // 产品级:强制 null,避免与设备级"空 deviceId"混淆 + 兼容 IS NULL 唯一性查询
            deviceIdSetter.accept(null);
        } else if (level == DeviceAclRuleLevelEnum.DEVICE_LEVEL) {
            if (StrUtil.isBlank(currentDeviceId)) {
                throw BizException.wrap("设备级规则必须填写设备标识");
            }
        }
        return level;
    }

    /**
     * 检查同 (level, productId, deviceId, priority) 维度是否已有规则。
     *
     * <p>产品级 deviceIdentification 兼容 NULL + 空字符串(防历史脏数据漏判);设备级走 .eq 精确匹配。
     */
    private boolean existsSamePriorityRule(DeviceAclRuleLevelEnum level, String productId, String deviceId,
                                           Integer priority, Long excludeId) {
        var wrap = Wraps.<DeviceAclRule>lbQ()
            .eq(DeviceAclRule::getRuleLevel, level.getValue())
            .eq(DeviceAclRule::getProductIdentification, productId)
            .eq(DeviceAclRule::getPriority, priority);
        if (StrUtil.isBlank(deviceId)) {
            // LbQueryWrap.eq 对空串自动忽略 condition,只能用 apply 写裸 SQL 强制添加
            wrap.and(w -> w
                .isNull(DeviceAclRule::getDeviceIdentification)
                .or().apply("device_identification = ''"));
        } else {
            wrap.eq(DeviceAclRule::getDeviceIdentification, deviceId);
        }
        if (excludeId != null) {
            wrap.ne(DeviceAclRule::getId, excludeId);
        }
        return superManager.count(wrap) > 0;
    }

    /**
     * 抛"同优先级冲突"错误,消息带维度上下文。
     */
    private void throwSamePriorityConflict(DeviceAclRuleLevelEnum level, String productId, String deviceId, Integer priority) {
        String dimension = level == DeviceAclRuleLevelEnum.DEVICE_LEVEL
            ? StrUtil.format("产品 [{}] 设备 [{}]", productId, deviceId)
            : StrUtil.format("产品 [{}] (产品级)", productId);
        throw BizException.wrap("{} 下已存在优先级 {} 的规则,请调整 priority 或编辑现有规则",
            dimension, priority);
    }

    /** save VO 重载 ── excludeId 默认 null(新增无需排除自身)。 */
    private void requireNoPriorityConflict(DeviceAclRuleLevelEnum level, DeviceAclRuleSaveVO vo) {
        if (existsSamePriorityRule(level, vo.getProductIdentification(),
            vo.getDeviceIdentification(), vo.getPriority(), null)) {
            throwSamePriorityConflict(level, vo.getProductIdentification(),
                vo.getDeviceIdentification(), vo.getPriority());
        }
    }

    /** update VO 重载 ── 排除自身 id 防把自己当冲突。 */
    private void requireNoPriorityConflict(DeviceAclRuleLevelEnum level, DeviceAclRuleUpdateVO vo) {
        if (existsSamePriorityRule(level, vo.getProductIdentification(),
            vo.getDeviceIdentification(), vo.getPriority(), vo.getId())) {
            throwSamePriorityConflict(level, vo.getProductIdentification(),
                vo.getDeviceIdentification(), vo.getPriority());
        }
    }


    @Override
    public DeviceAclCheckResultVO checkAclPermission(DeviceAclCheckQuery deviceAclCheckQuery) {
        Optional<DeviceCacheVO> deviceCacheVO = linkCacheDataHelper.getDeviceCacheVO(deviceAclCheckQuery.getClientIdentifier());
        if (deviceCacheVO.isEmpty()) {
            return denied("Device Not Found");
        }
        DeviceInfoResultVO deviceInfoResultVO = BeanPlusUtil.toBean(deviceCacheVO.get(), DeviceInfoResultVO.class);
        // 直接走 helper,不 self-call(self-call 绕过 AOP,以后加切面会失效)
        List<DeviceAclRuleCacheVO> rules = linkCacheDataHelper.getDeviceAclRules(
            deviceInfoResultVO.getProductIdentification(), deviceInfoResultVO.getDeviceIdentification());
        if (CollectionUtil.isEmpty(rules)) {
            return denied("Not ACL Rule");
        }

        // client action → rule action 映射;映射不存在(如 disconnect)走默认 deny
        return ClientAclActionTypeEnum.fromValue(deviceAclCheckQuery.getActionType())
            .flatMap(DeviceAclRuleActionTypeEnum::fromClientType)
            .map(targetAction -> decideTopicAccess(targetAction, deviceAclCheckQuery.getTopic(), deviceInfoResultVO, rules))
            .orElseGet(() -> denied("Unsupported action type for ACL: " + deviceAclCheckQuery.getActionType()));
    }

    /**
     * 按 actionType 过滤规则(targetAction 或 ALL 命中) + 占位符替换 + matcher 决策。
     * <p>enabled filter 已由缓存 loader 保证;ruleAction 为 null 时显式拒绝防 DB 脏数据误命中。
     */
    private DeviceAclCheckResultVO decideTopicAccess(DeviceAclRuleActionTypeEnum targetAction,
                                                     String topic,
                                                     DeviceInfoResultVO deviceInfo,
                                                     List<DeviceAclRuleCacheVO> rules) {
        List<DeviceAclRuleCacheVO> filteredRules = rules.stream()
            .filter(rule -> {
                DeviceAclRuleActionTypeEnum ruleAction = DeviceAclRuleActionTypeEnum
                    .fromValue(rule.getActionType()).orElse(null);
                return ruleAction != null && (ruleAction == targetAction || ruleAction == DeviceAclRuleActionTypeEnum.ALL);
            })
            .collect(Collectors.toList());

        AclTopicPatternPlaceholderReplacer.replacePlaceholders(filteredRules, Optional.of(deviceInfo));
        boolean allowed = AclMatcherUtil.isTopicAllowed(topic, filteredRules);
        return DeviceAclCheckResultVO.builder()
            .allowed(allowed)
            .echoMap(MapUtil.newHashMap())
            .build();
    }

    private DeviceAclCheckResultVO denied(String errorMessage) {
        return DeviceAclCheckResultVO.builder()
            .allowed(false)
            .errorMessage(errorMessage)
            .echoMap(MapUtil.newHashMap())
            .build();
    }

    @Override
    public List<DeviceAclRuleCacheVO> getDeviceAclRuleCacheVOList(String productIdentification, String deviceIdentification) {
        return linkCacheDataHelper.getDeviceAclRules(productIdentification, deviceIdentification);
    }

}


