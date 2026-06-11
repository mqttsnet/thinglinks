package com.mqttsnet.thinglinks.system.manager.application.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CacheOps;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.common.cache.tenant.tenant.TenantApplicationCacheKeyBuilder;
import com.mqttsnet.thinglinks.system.entity.application.DefApplication;
import com.mqttsnet.thinglinks.system.entity.application.DefResource;
import com.mqttsnet.thinglinks.system.entity.application.DefTenantApplicationRel;
import com.mqttsnet.thinglinks.system.entity.application.DefTenantResourceRel;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUserTenantRel;
import com.mqttsnet.thinglinks.system.manager.application.DefApplicationManager;
import com.mqttsnet.thinglinks.system.manager.application.DefResourceManager;
import com.mqttsnet.thinglinks.system.manager.application.DefTenantApplicationRelManager;
import com.mqttsnet.thinglinks.system.manager.application.DefTenantResourceRelManager;
import com.mqttsnet.thinglinks.system.manager.tenant.DefUserTenantRelManager;
import com.mqttsnet.thinglinks.system.mapper.application.DefTenantApplicationRelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用管理
 *
 * @author mqttsnet
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [mqttsnet] [初始创建]
 */
/**
 * <b>关系表 Manager 编排注 ── by design</b>:
 * <p>本类是租户-应用关联表的 Manager,职责本身就是编排同 domain(system)多表关系
 * (Application / Resource / TenantResourceRel / UserTenantRel).这种"关系表 Manager 跨表
 * 编排同域兄弟 Manager"是合理实现 ── 改成 Service 编排反而绕路且容易引发循环依赖.</p>
 */
@RequiredArgsConstructor
@Service
public class DefTenantApplicationRelManagerImpl extends SuperManagerImpl<DefTenantApplicationRelMapper, DefTenantApplicationRel> implements DefTenantApplicationRelManager {
    private final DefApplicationManager defApplicationManager;
    private final DefResourceManager defResourceManager;
    private final DefTenantResourceRelManager defTenantResourceRelManager;
    private final DefUserTenantRelManager defUserTenantRelManager;
    private final CacheOps cacheOps;

    @Override
    public void grantGeneralApplication(Long tenantId) {
        List<DefApplication> list = defApplicationManager.findGeneral();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> applicationIds = new ArrayList<>();
        List<DefTenantApplicationRel> tarList = list.stream().map(application -> {
            DefTenantApplicationRel tar = new DefTenantApplicationRel();
            tar.setTenantId(tenantId);
            tar.setApplicationId(application.getId());
            applicationIds.add(application.getId());
            return tar;
        }).toList();
        saveBatch(tarList);

        List<DefResource> resourceList = defResourceManager.findByApplicationId(applicationIds);
        if (CollUtil.isEmpty(resourceList)) {
            return;
        }
        List<DefTenantResourceRel> trrList = resourceList.stream().map(resource -> {
            DefTenantResourceRel trr = new DefTenantResourceRel();
            trr.setTenantId(tenantId);
            trr.setApplicationId(resource.getApplicationId());
            trr.setResourceId(resource.getId());
            return trr;
        }).toList();
        defTenantResourceRelManager.saveBatch(trrList);
    }


    @Override
    public List<Long> findApplicationByEmployeeId(Long employeeId) {
        DefUserTenantRel employee = defUserTenantRelManager.getByIdCache(employeeId);
        ArgumentAssert.notNull(employee, "用户不存在");
        Long tenantId = employee.getTenantId();
        CacheKey key = TenantApplicationCacheKeyBuilder.builder(tenantId);
        CacheResult<List<Long>> applicationIds = cacheOps.get(key, k -> listObjs(
                Wraps.<DefTenantApplicationRel>lbQ().select(DefTenantApplicationRel::getApplicationId)
                        .eq(DefTenantApplicationRel::getTenantId, tenantId)
                        .and(w ->
                                w.gt(DefTenantApplicationRel::getExpirationTime, LocalDateTime.now()).or().isNull(DefTenantApplicationRel::getExpirationTime)
                        ), Convert::toLong));
        return applicationIds.asList();
    }

    @Override
    public void deleteByTenantId(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        remove(Wraps.<DefTenantApplicationRel>lbQ().in(DefTenantApplicationRel::getTenantId, ids));

        cacheOps.del(ids.stream().map(TenantApplicationCacheKeyBuilder::builder).toList());
    }
}
