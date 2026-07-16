package com.mqttsnet.thinglinks.service.linkage.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.linkage.RuleInstance;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleInstanceStatusEnum;
import com.mqttsnet.thinglinks.manager.linkage.RuleInstanceManager;
import com.mqttsnet.thinglinks.service.linkage.RuleInstanceService;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleInstanceResultVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleInstanceSaveVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleInstanceFlowUpdateVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleInstanceUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 业务实现类
 * 规则实例表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-05 23:04:02
 * @create [2023-07-05 23:04:02] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class RuleInstanceServiceImpl extends SuperServiceImpl<RuleInstanceManager, Long, RuleInstance> implements RuleInstanceService {

    /**
     * 保存规则实例信息
     *
     * @param saveVO
     * @return
     */
    @Override
    public RuleInstanceResultVO saveRuleInstance(RuleInstanceSaveVO saveVO) {
        log.info("saveRuleInstance saveVO:{}", saveVO);
        //校验参数
        checkedRuleInstanceSaveVO(saveVO);
        //构建参数
        RuleInstance RuleInstance = builderRuleInstanceSaveVO(saveVO);
        //更新
        superManager.save(RuleInstance);
        return BeanPlusUtil.toBeanIgnoreError(RuleInstance, RuleInstanceResultVO.class);
    }

    /**
     * 修改规则实例信息
     *
     * @param updateVO
     * @return
     */
    @Override
    public RuleInstanceResultVO updateRuleInstance(RuleInstanceUpdateVO updateVO) {
        log.info("updateRuleInstance updateVO:{}", updateVO);
        //校验参数
        checkedRuleInstanceUpdateVO(updateVO);
        //构建参数
        RuleInstance RuleInstance = BeanPlusUtil.toBeanIgnoreError(updateVO, RuleInstance.class);
        //更新
        superManager.updateById(RuleInstance);
        return BeanPlusUtil.toBeanIgnoreError(RuleInstance, RuleInstanceResultVO.class);
    }

    /**
     * 删除规则实例信息
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteRuleInstance(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        RuleInstance RuleInstance = superManager.getById(id);
        if (null == RuleInstance) {
            throw BizException.wrap("The RuleInstance does not exist");
        }
        return superManager.removeById(id);
    }

    /**
     * 根据规则实例ID更新规则状态
     *
     * @param id     规则实例ID
     * @param status 规则实例状态
     * @return {@link Boolean} 更新结果
     */
    @Override
    public Boolean updateRuleInstanceStatus(Long id, Integer status) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        ArgumentAssert.notNull(status, "status Cannot be null");
        if (!RuleInstanceStatusEnum.STATE_COLLECTION.contains(status)) {
            throw BizException.wrap("Status is not exist");
        }
        RuleInstance ruleInstance = superManager.getById(id);
        if (null == ruleInstance) {
            throw BizException.wrap("The ruleInstance does not exist");
        }
        if (Objects.equals(status, ruleInstance.getStatus())) {
            return true;
        }
        ruleInstance.setStatus(status);
        return superManager.updateById(ruleInstance);
    }

    @Override
    public RuleInstanceResultVO updateRuleInstanceFlowData(RuleInstanceFlowUpdateVO updateVO) {
        log.info("updateRuleInstanceFlowData updateVO:{}", updateVO);
        // 检查流程ID是否存在
        RuleInstance existingRuleInstance = superManager.selectOneByFlowId(updateVO.getFlowId());
        if (existingRuleInstance == null) {
            throw BizException.wrap("The specified flowId does not exist");
        }
        // 更新流程数据
        existingRuleInstance.setFlowData(updateVO.getFlowData());
        superManager.updateById(existingRuleInstance);
        return BeanPlusUtil.toBeanIgnoreError(existingRuleInstance, RuleInstanceResultVO.class);
    }

    @Override
    public RuleInstanceResultVO getDetailsByFlowId(String flowId) {
        return Optional.ofNullable(superManager.selectOneByFlowId(flowId))
                .map(instance -> BeanPlusUtil.toBeanIgnoreError(instance, RuleInstanceResultVO.class))
                .orElseThrow(() -> BizException.wrap("Rule instance not found with flow ID: " + flowId));
    }

    /**
     * 新增 校验参数
     *
     * @param saveVO
     */
    private void checkedRuleInstanceSaveVO(RuleInstanceSaveVO saveVO) {

        ArgumentAssert.notBlank(saveVO.getAppId(), "AppId Cannot be null");
        //规则实例状态
        ArgumentAssert.notNull(saveVO.getStatus(), "Status Cannot be null");
        if (!RuleInstanceStatusEnum.STATE_COLLECTION.contains(saveVO.getStatus())) {
            throw BizException.wrap("Status is not exist");
        }

        //校验 规则实例地址是否存在
        RuleInstance existingRuleInstance = superManager.getOne(Wraps.<RuleInstance>lbQ().eq(RuleInstance::getInstanceAddress, saveVO.getInstanceAddress()));
        if (Objects.nonNull(existingRuleInstance)) {
            throw BizException.validFail("该实例地址已存在!");
        }
    }

    /**
     * 新增 构建参数
     *
     * @param saveVO
     * @return
     */
    private RuleInstance builderRuleInstanceSaveVO(RuleInstanceSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        saveVO.setFlowId(SnowflakeIdUtil.nextId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, RuleInstance.class);
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedRuleInstanceUpdateVO(RuleInstanceUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");

        ArgumentAssert.notNull(updateVO.getStatus(), "Status Cannot be null");
        if (!RuleInstanceStatusEnum.STATE_COLLECTION.contains(updateVO.getStatus())) {
            throw BizException.wrap("Status is not exist");
        }

        //校验 规则实例地址是否存在
        RuleInstance existingRuleInstance = superManager.getOne(Wraps.<RuleInstance>lbQ().eq(RuleInstance::getInstanceAddress, updateVO.getInstanceAddress())
                .ne(RuleInstance::getId, updateVO.getId()));
        if (Objects.nonNull(existingRuleInstance)) {
            throw BizException.validFail("该实例地址已存在!");
        }
    }

}

