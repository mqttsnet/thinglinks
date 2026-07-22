package com.mqttsnet.thinglinks.base.manager.user.impl;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.cache.repository.CacheOps;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.base.entity.system.BaseRole;
import com.mqttsnet.thinglinks.base.entity.user.BaseEmployeeRoleRel;
import com.mqttsnet.thinglinks.base.manager.system.BaseRoleManager;
import com.mqttsnet.thinglinks.base.manager.user.BaseEmployeeRoleRelManager;
import com.mqttsnet.thinglinks.base.mapper.user.BaseEmployeeRoleRelMapper;
import com.mqttsnet.thinglinks.common.cache.base.user.EmployeeRoleCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 员工的角色
 * </p>
 *
 * @author mqttsnet
 * @date 2021-10-18
 * @create [2021-10-18] [mqttsnet] 
 */
/**
 * <b>关系表 Manager 编排注 ── by design</b>:员工-角色关系表 Manager 联动同 base 域的 BaseRole Manager,
 * 是合理实现.改 Service 编排反而绕路.详见 system 域 {@code DefTenantApplicationRelManagerImpl} 的设计说明.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseEmployeeRoleRelManagerImpl extends SuperManagerImpl<BaseEmployeeRoleRelMapper, BaseEmployeeRoleRel> implements BaseEmployeeRoleRelManager {
    private final BaseRoleManager baseRoleManager;
    private final CacheOps cacheOps;

    @Override
    public boolean removeByEmployeeIds(Collection<Long> employeeIds) {
        ArgumentAssert.notEmpty(employeeIds, "员工ID不能为空");
        boolean remove = remove(Wraps.<BaseEmployeeRoleRel>lbQ().in(BaseEmployeeRoleRel::getEmployeeId, employeeIds));
        cacheOps.del(employeeIds.stream().map(EmployeeRoleCacheKeyBuilder::build).toList());
        return remove;
    }

    @Override
    public boolean bindRole(List<Long> employeeIdList, String code) {
        BaseRole role = baseRoleManager.getRoleByCode(code);
        ArgumentAssert.notNull(role, "请先配置{}管理员", code);
        List<BaseEmployeeRoleRel> erList = employeeIdList.stream().map(employeeId -> {
            BaseEmployeeRoleRel employeeRole = new BaseEmployeeRoleRel();
            employeeRole.setEmployeeId(employeeId);
            employeeRole.setRoleId(role.getId());
            return employeeRole;
        }).toList();

        boolean flag = saveBatch(erList);

        cacheOps.del(employeeIdList.stream().map(EmployeeRoleCacheKeyBuilder::build).toList());
        return flag;
    }

    @Override
    public boolean unBindRole(List<Long> employeeIdList, String code) {
        ArgumentAssert.notEmpty(employeeIdList, "请传递员工");
        BaseRole role = baseRoleManager.getRoleByCode(code);
        ArgumentAssert.notNull(role, "请先配置{}管理员", code);
        boolean flag = remove(Wraps.<BaseEmployeeRoleRel>lbQ().eq(BaseEmployeeRoleRel::getRoleId, role.getId())
                .in(BaseEmployeeRoleRel::getEmployeeId, employeeIdList));
        cacheOps.del(employeeIdList.stream().map(EmployeeRoleCacheKeyBuilder::build).toList());
        return flag;
    }

    @Override
    public void deleteByRole(Collection<Long> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        LbQueryWrap<BaseEmployeeRoleRel> wrap = Wraps.<BaseEmployeeRoleRel>lbQ().in(BaseEmployeeRoleRel::getRoleId, roleIdList);
        List<BaseEmployeeRoleRel> list = list(wrap);
        remove(wrap);
        cacheOps.del(list.stream().map(BaseEmployeeRoleRel::getEmployeeId).distinct().map(EmployeeRoleCacheKeyBuilder::build).toList());
    }
}
