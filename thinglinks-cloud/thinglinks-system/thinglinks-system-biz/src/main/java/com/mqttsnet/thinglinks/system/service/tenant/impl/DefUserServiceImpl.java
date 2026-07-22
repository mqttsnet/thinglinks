package com.mqttsnet.thinglinks.system.service.tenant.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.base.service.impl.SuperCacheServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.secure.EncryptDecryptUtils;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserEmailCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserIdCardCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DefUserMobileCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.AppendixType;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.common.properties.SystemProperties;
import com.mqttsnet.thinglinks.file.service.AppendixService;
import com.mqttsnet.thinglinks.model.vo.result.AppendixResultVO;
import com.mqttsnet.thinglinks.model.vo.save.AppendixSaveVO;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUser;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUserTenantRel;
import com.mqttsnet.thinglinks.system.manager.tenant.DefUserManager;
import com.mqttsnet.thinglinks.system.manager.tenant.DefUserTenantRelManager;
import com.mqttsnet.thinglinks.system.service.tenant.DefUserService;
import com.mqttsnet.thinglinks.system.vo.query.tenant.DefUserPageQuery;
import com.mqttsnet.thinglinks.system.vo.result.tenant.DefUserDetailsResultVO;
import com.mqttsnet.thinglinks.system.vo.result.tenant.DefUserResultVO;
import com.mqttsnet.thinglinks.system.vo.save.tenant.DefUserSaveVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserAvatarUpdateVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserBaseInfoUpdateVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserEmailUpdateVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserMobileUpdateVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserPasswordResetVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserPasswordUpdateVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 业务实现类
 * 用户
 * </p>
 *
 * @author mqttsnet
 * @date 2021-10-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@DS(DsConstant.DEFAULTS)
public class DefUserServiceImpl extends SuperCacheServiceImpl<DefUserManager, Long, DefUser>
        implements DefUserService {

    private final AppendixService appendixService;
    private final DefUserTenantRelManager defUserTenantRelManager;
    private final SystemProperties systemProperties;

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return superManager.findByIds(ids.stream().map(Convert::toLong).collect(Collectors.toSet()));
    }

    @Override
    public boolean checkUsername(String value, Long id) {
        String encryptValue = EncryptDecryptUtils.encrypt(value);
        return superManager.count(Wraps.<DefUser>lbQ().eq(DefUser::getUsername, encryptValue).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkEmail(String value, Long id) {
        String encryptValue = EncryptDecryptUtils.encrypt(value);
        return superManager.count(Wraps.<DefUser>lbQ().eq(DefUser::getEmail, encryptValue).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkMobile(String value, Long id) {
        String encryptValue = EncryptDecryptUtils.encrypt(value);
        return superManager.count(Wraps.<DefUser>lbQ().eq(DefUser::getMobile, encryptValue).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkIdCard(String value, Long id) {
        String encryptValue = EncryptDecryptUtils.encrypt(value);
        return superManager.count(Wraps.<DefUser>lbQ().eq(DefUser::getIdCard, encryptValue).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public DefUser getUserByMobile(String mobile) {
        return superManager.getUserByMobile(mobile);
    }

    @Override
    public DefUser getUserByEmail(String email) {
        return superManager.getUserByEmail(email);
    }

    @Override
    public DefUser getUserByIdCard(String idCard) {
        return superManager.getUserByIdCard(idCard);
    }

    @Override
    public DefUser getUserByUsername(String username) {
        return superManager.getUserByUsername(username);
    }

    @Override
    protected <UpdateVO> DefUser updateBefore(UpdateVO vo) {
        DefUserUpdateVO updateVO = (DefUserUpdateVO) vo;
        ArgumentAssert.isFalse(checkUsername(updateVO.getUsername(), updateVO.getId()), "用户名：{}已经存在", updateVO.getUsername());
        if (StrUtil.isNotEmpty(updateVO.getEmail())) {
            ArgumentAssert.isFalse(checkEmail(updateVO.getEmail(), updateVO.getId()), "邮箱：{}已经存在", updateVO.getEmail());
        }
        if (StrUtil.isNotEmpty(updateVO.getMobile())) {
            ArgumentAssert.isFalse(checkMobile(updateVO.getMobile(), updateVO.getId()), "手机号：{}已经存在", updateVO.getMobile());
        }
        if (StrUtil.isNotEmpty(updateVO.getIdCard())) {
            ArgumentAssert.isFalse(checkIdCard(updateVO.getIdCard(), updateVO.getId()), "身份证号：{}已经存在", updateVO.getIdCard());
        }
        return super.updateBefore(updateVO);
    }

    @Override
    protected <SaveVO> DefUser saveBefore(SaveVO vo) {
        DefUserSaveVO saveVO = (DefUserSaveVO) vo;
        ArgumentAssert.isFalse(checkUsername(saveVO.getUsername(), null), "用户名：{}已经存在", saveVO.getUsername());
        if (StrUtil.isNotEmpty(saveVO.getEmail())) {
            ArgumentAssert.isFalse(checkEmail(saveVO.getEmail(), null), "邮箱：{}已经存在", saveVO.getEmail());
        }
        if (StrUtil.isNotEmpty(saveVO.getMobile())) {
            ArgumentAssert.isFalse(checkMobile(saveVO.getMobile(), null), "手机号：{}已经存在", saveVO.getMobile());
        }
        if (StrUtil.isNotEmpty(saveVO.getIdCard())) {
            ArgumentAssert.isFalse(checkIdCard(saveVO.getIdCard(), null), "身份证号：{}已经存在", saveVO.getIdCard());
        }
        DefUser defUser = BeanUtil.toBean(saveVO, DefUser.class);
        defUser.setSalt(RandomUtil.randomString(20));
        if (StrUtil.isEmpty(defUser.getPassword())) {
            defUser.setPassword(getConfiguredDefaultPassword());
        }
        defUser.setPassword(SecureUtil.sha256(defUser.getPassword() + defUser.getSalt()));
        defUser.setPasswordErrorNum(0);
        defUser.setReadonly(false);
        defUser.setState(true);
        return defUser;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String register(DefUser defUser) {
        ArgumentAssert.isFalse(checkMobile(defUser.getMobile(), null), "手机号：{}已经存在", defUser.getMobile());
        setDefUser(defUser);
        defUser.setNickName(defUser.getMobile());

        superManager.save(defUser);
        return defUser.getMobile();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String registerByEmail(DefUser defUser) {
        ArgumentAssert.isFalse(checkEmail(defUser.getEmail(), null), "邮箱：{}已经存在", defUser.getMobile());
        setDefUser(defUser);
        defUser.setNickName(defUser.getEmail());

        superManager.save(defUser);
        return defUser.getEmail();
    }

    @Override
    protected <SaveVO> void saveAfter(SaveVO saveVO, DefUser entity) {
        superManager.delUserCache(Collections.singletonList(entity));
    }

    @Override
    protected <UpdateVO> void updateAfter(UpdateVO updateVO, DefUser entity) {
        superManager.delUserCache(Collections.singletonList(entity));
    }

    private void setDefUser(DefUser defUser) {
        defUser.setSalt(RandomUtil.randomString(20));
        defUser.setPassword(SecureUtil.sha256(defUser.getPassword() + defUser.getSalt()));
        defUser.setPasswordErrorNum(0);
        defUser.setReadonly(false);
        defUser.setState(true);
        defUser.setUsername(UUID.fastUUID().toString(true));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DefUserTenantRel saveUserAndEmployee(Long tenantId, DefUserSaveVO defUserSaveVO) {
        DefUser entity = saveBefore(defUserSaveVO);
        entity.setState(true);
        log.info("entity={}", JsonUtil.toJson(entity));
        this.getSuperManager().save(entity);

        DefUserTenantRel defUserTenantRel = new DefUserTenantRel();
        defUserTenantRel.setUserId(entity.getId());
        defUserTenantRel.setTenantId(tenantId);
        defUserTenantRel.setState(defUserSaveVO.getState());
        defUserTenantRel.setIsDefault(true);
        log.info("defUserTenantRel={}", JsonUtil.toJson(defUserTenantRel));
        defUserTenantRelManager.save(defUserTenantRel);
        return defUserTenantRel;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(DefUserPasswordResetVO data) {
        if (data.getIsUseSystemPassword()) {
            data.setPassword(getConfiguredDefaultPassword());
        } else {
            ArgumentAssert.notEmpty(data.getConfirmPassword(), "请输入确认密码");
            ArgumentAssert.notEmpty(data.getPassword(), "请输入密码");
            ArgumentAssert.equals(data.getConfirmPassword(), data.getPassword(), "密码和确认密码不一致");
        }
        DefUser user = superManager.getById(data.getId());
        ArgumentAssert.notNull(user, "您要重置密码的用户不存在");

        return updateUserPassword(user.getId(), data.getPassword(), user.getSalt());
    }

    private String getConfiguredDefaultPassword() {
        ArgumentAssert.notEmpty(systemProperties.getDefPwd(), "系统默认密码未配置");
        return systemProperties.getDefPwd();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateState(Long id, Boolean state) {
        // 演示环境专用标识，用于WriteInterceptor拦截器判断演示环境需要禁止用户执行sql，若您无需搭建演示环境，可以删除下面一行代码
        ContextUtil.setStop();
        return superManager.updateById(DefUser.builder().state(state).id(id).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAvatar(DefUserAvatarUpdateVO data) {
        ArgumentAssert.isFalse(data.getAppendixAvatar() == null, "请上传或选择头像");
        boolean flag = appendixService.save(AppendixSaveVO.build(data.getId(), AppendixType.System.DEF__USER__AVATAR, data.getAppendixAvatar()));
        superManager.delCache(data.getId());
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(DefUserPasswordUpdateVO data) {
        ArgumentAssert.notEmpty(data.getOldPassword(), "请输入旧密码");
        DefUser user = superManager.getById(data.getId());
        ArgumentAssert.notNull(user, "用户不存在");
        ArgumentAssert.equals(user.getId(), ContextUtil.getUserId(), "只能修改自己的密码");
        String oldPassword = SecureUtil.sha256(data.getOldPassword() + user.getSalt());
        ArgumentAssert.equals(user.getPassword(), oldPassword, "旧密码错误");

        return updateUserPassword(user.getId(), data.getPassword(), user.getSalt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMobile(DefUserMobileUpdateVO data) {
        Long id = ContextUtil.getUserId();
        DefUser user = superManager.getById(id);
        ArgumentAssert.notNull(user, "用户不存在");
        user.setMobile(data.getMobile());
        superManager.updateById(user);

        // 淘汰旧手机缓存
        cacheOps.del(DefUserMobileCacheKeyBuilder.builder(user.getMobile()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEmail(DefUserEmailUpdateVO data) {
        Long id = ContextUtil.getUserId();
        DefUser user = superManager.getById(id);
        ArgumentAssert.notNull(user, "用户不存在");
        user.setEmail(data.getEmail());
        superManager.updateById(user);
        cacheOps.del(DefUserEmailCacheKeyBuilder.builder(user.getEmail()));
        return true;
    }

    private boolean updateUserPassword(Long id, String password, String salt) {
        if (StrUtil.isEmpty(salt)) {
            salt = RandomUtil.randomString(20);
        }
        String defPassword = SecureUtil.sha256(password + salt);

        boolean flag = superManager.update(Wrappers.<DefUser>lambdaUpdate()
                .set(DefUser::getPassword, defPassword)
                .set(DefUser::getPasswordErrorNum, 0L)
                .set(DefUser::getPasswordErrorLastTime, null)
                .set(DefUser::getPasswordExpireTime, null)
                .eq(DefUser::getId, id)
        );
        superManager.delCache(id);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBaseInfo(DefUserBaseInfoUpdateVO data) {
        DefUser old = getById(data.getId());
        DefUser defUser = BeanUtil.toBean(data, DefUser.class);

        if (data.getLogo() != null) {
            appendixService.save(AppendixSaveVO.build(data.getId(), AppendixType.System.DEF__USER__AVATAR, data.getLogo()));
        }

        boolean flag = superManager.updateById(defUser);
        if (StrUtil.isAllNotEmpty(data.getIdCard(), old.getIdCard()) && !StrUtil.equals(old.getIdCard(), data.getIdCard())) {
            cacheOps.del(DefUserIdCardCacheKeyBuilder.builder(old.getIdCard()));
        }
        return flag;
    }

    @Override
    public IPage<DefUserResultVO> findNotUserByTenantId(PageParams<DefUserPageQuery> params) {
        IPage<DefUser> page = params.buildPage(DefUser.class);
        DefUserPageQuery query = params.getModel();
        encryptSearchParams(query);
        return BeanPlusUtil.toBeanPage(superManager.selectNotUserByTenantId(params.getModel(), page), DefUserResultVO.class);
    }

    @Override
    public IPage<DefUserResultVO> pageUserByTenant(PageParams<DefUserPageQuery> params) {
        IPage<DefUser> page = params.buildPage(DefUser.class);
        DefUserPageQuery pageQuery = params.getModel();
        if (pageQuery.getTenantId() == null) {
            pageQuery.setTenantId(ContextUtil.getTenantId());
        }
        return BeanPlusUtil.toBeanPage(superManager.pageUserByTenant(pageQuery, page), DefUserResultVO.class);
    }

    @Override
    public List<Long> findUserIdList(DefUserPageQuery pageQuery) {
        if (pageQuery == null) {
            return superManager.listObjs(Wraps.<DefUser>lbQ().select(DefUser::getId), Convert::toLong);
        }
        return superManager.listObjs(Wraps.<DefUser>lbQ().select(DefUser::getId)
                .like(DefUser::getMobile, pageQuery.getMobile())
                .like(DefUser::getUsername, pageQuery.getUsername())
                .like(DefUser::getIdCard, pageQuery.getIdCard())
                .like(DefUser::getEmail, pageQuery.getEmail())
                .eq(DefUser::getSex, pageQuery.getSex()), Convert::toLong);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetPassErrorNum(Long id) {
        int count = superManager.resetPassErrorNum(id);
        superManager.delCache(id);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrPasswordErrorNumById(Long id) {
        superManager.incrPasswordErrorNumById(id);
        superManager.delCache(id);
    }

    @Override
    public List<DefUserResultVO> queryUser(DefUserPageQuery params) {
        LbQueryWrap<DefUser> wrap = Wraps.lbQ();
        if (StrUtil.isAllEmpty(params.getEmail(), params.getUsername(), params.getIdCard(), params.getMobile())) {
            throw BizException.wrap("请至少传递一个参数");
        }
        encryptSearchParams(params);
        wrap.eq(DefUser::getEmail, params.getEmail())
                .eq(DefUser::getUsername, params.getUsername())
                .eq(DefUser::getIdCard, params.getIdCard())
                .eq(DefUser::getMobile, params.getMobile());
        List<DefUser> list = superManager.list(wrap);
        return BeanPlusUtil.copyToList(list, DefUserResultVO.class);
    }

    @Override
    public List<DefUserDetailsResultVO> getDefUserByIds(List<Long> ids) {
        List<DefUser> defUserList = superManager.list(Wraps.<DefUser>lbQ().in(DefUser::getId, ids));
        List<DefUserDetailsResultVO> defUserDetailsResultVOList = BeanPlusUtil.toBeanList(defUserList, DefUserDetailsResultVO.class);

        List<Long> userIds = defUserDetailsResultVOList.stream()
                .map(DefUserDetailsResultVO::getId)
                .collect(Collectors.toList());
        List<AppendixResultVO> appendixList = appendixService.listByBizIdsAndBizType(userIds, AppendixType.System.DEF__USER__AVATAR);

        Map<Long, AppendixResultVO> appendixMap = appendixList.stream()
                .collect(Collectors.toMap(AppendixResultVO::getBizId, Function.identity()));

        defUserDetailsResultVOList.forEach(defUserDetailsResultVO -> {
            Optional.ofNullable(appendixMap.get(defUserDetailsResultVO.getId()))
                    .map(AppendixResultVO::getId)
                    .ifPresent(defUserDetailsResultVO::setAvatarId);
        });

        return defUserDetailsResultVOList;
    }

    /**
     * 加密查询参数
     * 与实体类的TypeHandler保持一致
     */
    private void encryptSearchParams(DefUserPageQuery query) {
        if (StrUtil.isNotBlank(query.getUsername())) {
            query.setUsername(EncryptDecryptUtils.encrypt(query.getUsername()));
        }
        if (StrUtil.isNotBlank(query.getNickName())) {
            query.setNickName(EncryptDecryptUtils.encrypt(query.getNickName()));
        }
        if (StrUtil.isNotBlank(query.getEmail())) {
            query.setEmail(EncryptDecryptUtils.encrypt(query.getEmail()));
        }
        if (StrUtil.isNotBlank(query.getMobile())) {
            query.setMobile(EncryptDecryptUtils.encrypt(query.getMobile()));
        }
        if (StrUtil.isNotBlank(query.getIdCard())) {
            query.setIdCard(EncryptDecryptUtils.encrypt(query.getIdCard()));
        }
    }
}
