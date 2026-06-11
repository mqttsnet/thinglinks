package com.mqttsnet.thinglinks.system.manager.tenant.impl;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.mqttsnet.basic.base.manager.impl.SuperCacheManagerImpl;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.basic.secure.EncryptDecryptUtils;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.CollHelper;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserEmailCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserIdCardCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserMobileCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserUserNameCacheKeyBuilder;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUser;
import com.mqttsnet.thinglinks.system.manager.tenant.DefUserManager;
import com.mqttsnet.thinglinks.system.mapper.tenant.DefUserMapper;
import com.mqttsnet.thinglinks.system.vo.query.tenant.DefUserPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用管理
 *
 * @author mqttsnet
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [mqttsnet] [初始创建]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DefUserManagerImpl extends SuperCacheManagerImpl<DefUserMapper, DefUser> implements DefUserManager {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new DefUserCacheKeyBuilder();
    }

    /**
     * Echo 字典回显批量接口,由 basic-echo-starter 框架 @EchoLoader 反射调用,
     * 框架契约要求 Manager 层直接暴露,不能下沉 Service(Echo 框架找不到 Service)。
     */
    @Transactional(readOnly = true)
    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        // ① null + 空集合兜底:Echo 框架可能传 null,父类 findByIds 对 null/空集合行为不确定
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        try {
            List<DefUser> list = findByIds(ids, null).stream().filter(Objects::nonNull).toList();
            return CollHelper.uniqueIndex(list, DefUser::getId, DefUser::getNickName);
        } catch (Exception e) {
            // ② 异常降级:Echo 失败不能拖垮调用方接口,前端 echoMapText 会自动回退到原 ID
            log.warn("[Echo] DefUser findByIds failed, ids={}, cause={}", ids, e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public IPage<DefUser> selectNotUserByTenantId(DefUserPageQuery pageQuery, IPage<DefUser> page) {
        return baseMapper.selectNotUserByTenantId(pageQuery, page);
    }

    @Override
    public IPage<DefUser> pageUserByTenant(DefUserPageQuery pageQuery, IPage<DefUser> page) {
        return baseMapper.pageUserByTenant(pageQuery, page);
    }

    @Override
    public int resetPassErrorNum(Long id) {
        return baseMapper.resetPassErrorNum(id, LocalDateTime.now());
    }

    @Override
    public void incrPasswordErrorNumById(Long id) {
        baseMapper.incrPasswordErrorNumById(id, LocalDateTime.now());
    }


    @Override
    public boolean checkUsername(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getUsername, value).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkEmail(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getEmail, value).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkMobile(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getMobile, value).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkIdCard(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getIdCard, value).ne(DefUser::getId, id)) > 0;
    }


    @Override
    public DefUser getUserByUsername(String username) {
        CacheKey key = DefUserUserNameCacheKeyBuilder.builder(username);
        return getDefUser(key, username, DefUser::getUsername);
    }

    @Override
    public DefUser getUserByMobile(String mobile) {
        CacheKey key = DefUserMobileCacheKeyBuilder.builder(mobile);
        return getDefUser(key, mobile, DefUser::getMobile);
    }

    @Override
    public DefUser getUserByEmail(String email) {
        CacheKey key = DefUserEmailCacheKeyBuilder.builder(email);
        return getDefUser(key, email, DefUser::getEmail);
    }

    @Override
    public DefUser getUserByIdCard(String idCard) {
        CacheKey key = DefUserIdCardCacheKeyBuilder.builder(idCard);
        return getDefUser(key, idCard, DefUser::getIdCard);
    }


    private DefUser getDefUser(CacheKey key, String value, SFunction<DefUser, ?> fun) {
        String encryptValue = EncryptDecryptUtils.encrypt(value);
        CacheResult<Long> result = cacheOps.get(key, k -> {
            DefUser defUser = getOne(Wrappers.<DefUser>lambdaQuery().eq(fun, encryptValue), false);
            return defUser != null ? defUser.getId() : null;
        });
        return getByIdCache(result.getValue());
    }

    @Override
    public boolean removeById(DefUser entity) {
        delUserCache(Collections.singletonList(entity.getId()));
        return super.removeById(entity);
    }


    @Override
    public boolean removeByIds(Collection<?> list, boolean useFill) {
        delUserCache(list);
        return super.removeByIds(list, useFill);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list) {
        delUserCache(list);
        return super.removeBatchByIds(list);
    }

    @Override
    public void delUserCache(Collection<?> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> idList = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof DefUser user) {
                idList.add(user.getId());
            } else {
                idList.add(Convert.toLong(o));
            }
        }
        List<DefUser> defUsers = listByIds(idList);
        ArgumentAssert.notEmpty(defUsers, "待删除数据不存在");
        List<CacheKey> keyList = new ArrayList<>();
        for (DefUser defUser : defUsers) {
            CacheKey idCardKey = DefUserIdCardCacheKeyBuilder.builder(defUser.getIdCard());
            CacheKey mobileKey = DefUserMobileCacheKeyBuilder.builder(defUser.getMobile());
            CacheKey emailKey = DefUserEmailCacheKeyBuilder.builder(defUser.getEmail());
            CacheKey usernameKey = DefUserUserNameCacheKeyBuilder.builder(defUser.getUsername());
            keyList.add(idCardKey);
            keyList.add(mobileKey);
            keyList.add(emailKey);
            keyList.add(usernameKey);
        }

        cacheOps.del(keyList);
    }
}
