/*
 * Copyright 2006-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.mqttsnet.thinglinks.oauth.granter;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.boot.utils.WebUtils;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.exception.UnauthorizedException;
import com.mqttsnet.basic.exception.code.ExceptionCode;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.basic.utils.TreeUtil;
import com.mqttsnet.thinglinks.base.entity.user.BaseEmployee;
import com.mqttsnet.thinglinks.base.entity.user.BaseOrg;
import com.mqttsnet.thinglinks.base.service.user.BaseEmployeeService;
import com.mqttsnet.thinglinks.base.service.user.BaseOrgService;
import com.mqttsnet.thinglinks.common.properties.SystemProperties;
import com.mqttsnet.thinglinks.common.utils.Base64Util;
import com.mqttsnet.thinglinks.model.enumeration.StateEnum;
import com.mqttsnet.thinglinks.model.enumeration.base.OrgTypeEnum;
import com.mqttsnet.thinglinks.model.enumeration.base.UserStatusEnum;
import com.mqttsnet.thinglinks.oauth.event.LoginEvent;
import com.mqttsnet.thinglinks.oauth.event.model.LoginStatusDTO;
import com.mqttsnet.thinglinks.oauth.vo.param.LoginParamVO;
import com.mqttsnet.thinglinks.oauth.vo.result.LoginResultVO;
import com.mqttsnet.thinglinks.system.entity.system.DefClient;
import com.mqttsnet.thinglinks.system.entity.tenant.DefTenant;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUser;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUserTenantRel;
import com.mqttsnet.thinglinks.system.enumeration.system.LoginStatusEnum;
import com.mqttsnet.thinglinks.system.service.system.DefClientService;
import com.mqttsnet.thinglinks.system.service.tenant.DefTenantService;
import com.mqttsnet.thinglinks.system.service.tenant.DefUserService;
import com.mqttsnet.thinglinks.system.service.tenant.DefUserTenantRelService;
import com.mqttsnet.thinglinks.system.vo.result.tenant.DefUserTenantRelResultVO;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static com.mqttsnet.basic.context.ContextConstants.CLIENT_KEY;
import static com.mqttsnet.basic.context.ContextConstants.JWT_KEY_COMPANY_ID;
import static com.mqttsnet.basic.context.ContextConstants.JWT_KEY_DEPT_ID;
import static com.mqttsnet.basic.context.ContextConstants.JWT_KEY_EMPLOYEE_ID;
import static com.mqttsnet.basic.context.ContextConstants.JWT_KEY_TOP_COMPANY_ID;
import static com.mqttsnet.basic.context.ContextConstants.JWT_KEY_USER_ID;
import static com.mqttsnet.basic.context.ContextConstants.TENANT_ID_KEY;

/**
 * 验证码TokenGranter
 *
 * @author mqttsnet
 */
@Slf4j
public abstract class AbstractTokenGranter implements TokenGranter {

    @Autowired
    protected SystemProperties systemProperties;
    @Autowired
    protected DefClientService defClientService;
    @Resource
    protected DefTenantService defTenantService;
    @Autowired
    protected DefUserTenantRelService defUserTenantRelService;
    @Autowired
    protected DefUserService defUserService;
    @Autowired
    protected BaseEmployeeService baseEmployeeService;
    @Autowired
    protected BaseOrgService baseOrgService;
    @Autowired
    protected SaTokenConfig saTokenConfig;


    @Override
    public R<LoginResultVO> login(LoginParamVO loginParam) {
        // 0. 参数校验
        R<LoginResultVO> result = checkParam(loginParam);
        if (!result.getIsSuccess()) {
            return result;
        }
        result = checkClient();
        if (!result.getIsSuccess()) {
            return result;
        }

        // 1. 验证码
        result = checkCaptcha(loginParam);
        if (!result.getIsSuccess()) {
            return result;
        }


        // 2. 查找用户
        DefUser defUser = getUser(loginParam);

        // 账户不存在
        if (defUser == null) {
            SpringUtils.publishEvent(new LoginEvent(LoginStatusDTO.fail(null, loginParam.getUsername(), LoginStatusEnum.USER_ERROR, "用户不存在！")));
            return R.fail(ExceptionCode.JWT_USER_INVALID);
        }

        // 3. 获取员工和租户
        Employee employee = getEmployee(defUser);

        // 4. 判断密码
        result = checkUserPassword(loginParam, defUser, employee.getTenantId());
        if (!result.getIsSuccess()) {
            return result;
        }

        // 5. 检查用户状态
        result = checkUserState(defUser, employee.getTenantId());
        if (!result.getIsSuccess()) {
            return result;
        }

        // 6. 查询单位、部门
        Org org = findOrg(employee);

        // 7. 封装token
        LoginResultVO loginResultVO = buildResult(employee, org, defUser);

        LoginStatusDTO loginStatus = LoginStatusDTO.success(defUser.getId(), employee.getTenantId(), employee.getEmployeeId());
        SpringUtils.publishEvent(new LoginEvent(loginStatus));
        return R.success(loginResultVO);
    }

    /**
     * 检查参数
     *
     * @param loginParam 登录参数
     * @return com.mqttsnet.basic.base.R<com.mqttsnet.thinglinks.oauth.vo.result.LoginResultVO>
     * @author mqttsnet
     * @date 2022/10/5 12:38 PM
     * @create [2022/10/5 12:38 PM ] [mqttsnet] [初始创建]
     */
    protected abstract R<LoginResultVO> checkParam(LoginParamVO loginParam);

    /**
     * 检测客户端
     *
     * @return com.mqttsnet.basic.base.R<com.mqttsnet.thinglinks.oauth.vo.result.LoginResultVO>
     * @author mqttsnet
     * @date 2022/10/5 12:38 PM
     * @create [2022/10/5 12:38 PM ] [mqttsnet] [初始创建]
     */
    protected R<LoginResultVO> checkClient() {
        String basicHeader = JakartaServletUtil.getHeader(WebUtils.request(), CLIENT_KEY, StrPool.UTF_8);
        String[] client = Base64Util.getClient(basicHeader);
        DefClient defClient = defClientService.getClient(client[0], client[1]);

        if (defClient == null) {
            return R.fail("请在.env文件中配置正确的客户端ID或者客户端秘钥");
        }
        if (!defClient.getState()) {
            return R.fail("客户端[%s]已被禁用", defClient.getClientId());
        }
        return R.success(null);
    }


    /**
     * 检查验证码
     *
     * @param loginParam 登录参数
     * @return com.mqttsnet.basic.base.R<com.mqttsnet.thinglinks.oauth.vo.result.LoginResultVO>
     * @author mqttsnet
     * @date 2022/10/5 12:38 PM
     * @create [2022/10/5 12:38 PM ] [mqttsnet] [初始创建]
     */
    protected R<LoginResultVO> checkCaptcha(LoginParamVO loginParam) {
        return R.success(null);
    }

    /**
     * 查询用户
     *
     * @param loginParam 登录参数
     * @return com.mqttsnet.thinglinks.system.entity.tenant.DefUser
     * @author mqttsnet
     * @date 2022/10/5 12:38 PM
     * @create [2022/10/5 12:38 PM ] [mqttsnet] [初始创建]
     */
    protected abstract DefUser getUser(LoginParamVO loginParam);

    /**
     * 检查用户账号密码是否正确
     *
     * @param loginParam loginParam
     * @param user       user
     * @return com.mqttsnet.basic.base.R<com.mqttsnet.thinglinks.oauth.vo.result.LoginResultVO>
     * @author mqttsnet
     * @date 2022/10/5 12:38 PM
     * @create [2022/10/5 12:38 PM ] [mqttsnet] [初始创建]
     */

    protected R<LoginResultVO> checkUserPassword(LoginParamVO loginParam, DefUser user, Long tenantId) {
        return R.success(null);
    }

    /**
     * 检查用户状态是否正常
     *
     * @param user user
     * @return com.mqttsnet.basic.base.R<com.mqttsnet.thinglinks.oauth.vo.result.LoginResultVO>
     * @author mqttsnet
     * @date 2022/10/5 12:38 PM
     * @create [2022/10/5 12:38 PM ] [mqttsnet] [初始创建]
     */
    protected R<LoginResultVO> checkUserState(DefUser user, Long tenantId) {
        // 用户被禁用
        if (!user.getState()) {
            String msg = "您已被禁用，请联系管理员开通账号！";
            SpringUtils.publishEvent(new LoginEvent(LoginStatusDTO.fail(tenantId, user.getId(), LoginStatusEnum.USER_ERROR, msg)));
            return R.fail(msg);
        }
        return R.success(null);
    }

    /**
     * 查询员工信息
     *
     * @param defUser 用户信息
     * @return com.mqttsnet.thinglinks.oauth.granter.AbstractTokenGranter.Employee
     * @author mqttsnet
     * @date 2022/10/5 12:38 PM
     * @create [2022/10/5 12:38 PM ] [mqttsnet] [初始创建]
     */
    protected Employee getEmployee(DefUser defUser) {
        // 用户被禁用无法登陆， 员工被禁用无法访问当前企业的数据， 企业被禁用所有员工无法
        List<DefUserTenantRelResultVO> employeeList = defUserTenantRelService.listEmployeeByUserId(defUser.getId());
        Long employeeId = null;
        Long tenantId = null;
        Long userId = defUser.getId();
        UserStatusEnum userStatus = UserStatusEnum.NORMAL;
        if (CollUtil.isNotEmpty(employeeList)) {
            DefUserTenantRelResultVO defaultEmployee = employeeList.get(0);
            // 正常状态
            if (StateEnum.ENABLE.eq(defaultEmployee.getState()) && StateEnum.ENABLE.eq(defaultEmployee.getTenantState())) {
                employeeId = defaultEmployee.getId();
                tenantId = defaultEmployee.getTenantId();
            } else if (StateEnum.DISABLE.eq(defaultEmployee.getState()) && StateEnum.ENABLE.eq(defaultEmployee.getTenantState())) {
                // 员工在该企业被禁用
                tenantId = defaultEmployee.getTenantId();
                userStatus = UserStatusEnum.EMPLOYEE_DISABLE;
            } else {
                // 员工和企业都被禁用 或者 企业被禁用
                userStatus = UserStatusEnum.TENANT_DISABLE;
            }
        } else {
            // 您不属于任何企业，请联系管理员
            userStatus = UserStatusEnum.UNBIND_TENANT;
        }

        if (tenantId != null) {
            DefTenant defTenant = defTenantService.getByIdCache(tenantId);
            if (defTenant != null) {
                if (defTenant.getExpirationTime() != null) {
                    ArgumentAssert.checkGt(defTenant.getExpirationTime(), LocalDateTime.now(), "企业已过期");
                }
            }
        }

        log.info("userStatus={}, userId={}, employeeId={}, tenantId={}", userStatus, userId, employeeId, tenantId);
        return Employee.builder().employeeId(employeeId).tenantId(tenantId).build();
    }

    /**
     * 查询单位和部门信息
     *
     * @param employee 员工信息
     * @return com.mqttsnet.thinglinks.oauth.granter.AbstractTokenGranter.Org
     * @author mqttsnet
     * @date 2022/10/5 12:40 PM
     * @create [2022/10/5 12:40 PM ] [mqttsnet] [初始创建]
     */
    protected Org findOrg(Employee employee) {
        Long employeeId = employee.getEmployeeId();
        Long tenantId = employee.getTenantId();

        // 当前所属部门
        Long currentDeptId = null;
        // 当前所属单位
        Long currentCompanyId = null;
        // 当前所属顶级单位
        Long currentTopCompanyId = null;
        ContextUtil.setTenantId(tenantId);
        if (employeeId != null) {
            BaseEmployee baseEmployee = baseEmployeeService.getByIdCache(employeeId);

            // 当前用户尚不属于任意租户
            if (baseEmployee == null) {
                return Org.builder()
                        .currentTopCompanyId(null)
                        .currentCompanyId(null)
                        .currentDeptId(null).build();
            }

            boolean flag = false;
            // 上次登录的部门
            if (baseEmployee.getLastDeptId() != null) {
                currentDeptId = baseEmployee.getLastDeptId();
                // TODO 若用户变更了部门，就有问题
            } else {
                // 上次登录部门为空，则随机选择一个部门
                List<BaseOrg> deptList = baseOrgService.findDeptByEmployeeId(employeeId, null);
                BaseOrg defaultDept = baseOrgService.getDefaultOrg(deptList, null);

                currentDeptId = defaultDept != null ? defaultDept.getId() : null;
                baseEmployee.setLastDeptId(currentDeptId);

                flag = currentDeptId != null;
            }

            BaseOrg defaultCompany;
            if (baseEmployee.getLastCompanyId() != null) {
                currentCompanyId = baseEmployee.getLastCompanyId();

                defaultCompany = baseOrgService.getByIdCache(currentCompanyId);
            } else {
                if (currentDeptId != null) {
                    defaultCompany = baseOrgService.getCompanyByDeptId(currentDeptId);
                } else {
                    // currentDeptId 为空，员工可能直接挂在单位下、也可能挂不属于任何部门
                    List<BaseOrg> companyList = baseOrgService.findCompanyByEmployeeId(employeeId);
                    defaultCompany = baseOrgService.getDefaultOrg(companyList, baseEmployee.getLastCompanyId());
                }

                currentCompanyId = defaultCompany != null ? defaultCompany.getId() : null;
                baseEmployee.setLastCompanyId(currentCompanyId);
                flag = flag || currentCompanyId != null;

            }

            if (defaultCompany != null) {
                Long rootId = TreeUtil.getTopNodeId(defaultCompany.getTreePath());
                BaseOrg rootCompany;
                if (rootId != null) {
                    rootCompany = baseOrgService.getByIdCache(rootId);
                } else {
                    rootCompany = defaultCompany;
                }
                currentTopCompanyId = rootCompany != null ? rootCompany.getId() : null;
            }

            if (flag) {
                baseEmployeeService.updateById(baseEmployee);
            }
        }
        return Org.builder()
                .currentTopCompanyId(currentTopCompanyId)
                .currentCompanyId(currentCompanyId)
                .currentDeptId(currentDeptId).build();
    }

    /**
     * 构建返回值
     *
     * @param employee 员工信息
     * @param org      机构信息
     * @param defUser  用户信息
     * @return com.mqttsnet.thinglinks.oauth.vo.result.LoginResultVO
     * @author mqttsnet
     * @date 2022/10/5 12:41 PM
     * @create [2022/10/5 12:41 PM ] [mqttsnet] [初始创建]
     */
    protected LoginResultVO buildResult(Employee employee, Org org, DefUser defUser) {
        //此登录接口登录web端
        StpUtil.login(defUser.getId(), "PC");

        SaSession tokenSession = StpUtil.getTokenSession();
        tokenSession.setLoginId(defUser.getId());
        if (org.getCurrentTopCompanyId() != null) {
            tokenSession.set(JWT_KEY_TOP_COMPANY_ID, org.getCurrentTopCompanyId());
        } else {
            tokenSession.delete(JWT_KEY_TOP_COMPANY_ID);
        }
        if (org.getCurrentCompanyId() != null) {
            tokenSession.set(JWT_KEY_COMPANY_ID, org.getCurrentCompanyId());
        } else {
            tokenSession.delete(JWT_KEY_COMPANY_ID);
        }
        if (org.getCurrentDeptId() != null) {
            tokenSession.set(JWT_KEY_DEPT_ID, org.getCurrentDeptId());
        } else {
            tokenSession.delete(JWT_KEY_DEPT_ID);
        }
        if (employee.getEmployeeId() != null) {
            tokenSession.set(JWT_KEY_EMPLOYEE_ID, employee.getEmployeeId());
        } else {
            tokenSession.delete(JWT_KEY_EMPLOYEE_ID);
        }

        LoginResultVO resultVO = new LoginResultVO();
        resultVO.setToken(StpUtil.getTokenValue());
        resultVO.setExpire(StpUtil.getTokenTimeout());
        resultVO.setTenantId(employee.getTenantId());

        JSONObject obj = new JSONObject();
        obj.put(JWT_KEY_USER_ID, defUser.getId());
        obj.put(JWT_KEY_TOP_COMPANY_ID, tokenSession.get(JWT_KEY_TOP_COMPANY_ID));
        obj.put(JWT_KEY_COMPANY_ID, tokenSession.get(JWT_KEY_COMPANY_ID));
        obj.put(JWT_KEY_DEPT_ID, tokenSession.get(JWT_KEY_DEPT_ID));
        obj.put(JWT_KEY_EMPLOYEE_ID, tokenSession.get(JWT_KEY_EMPLOYEE_ID));
        obj.put(TENANT_ID_KEY, employee.getTenantId());

        resultVO.setRefreshToken(SaTempUtil.createToken(obj.toString(), 2 * saTokenConfig.getTimeout()));

        log.info("用户：{}  {} 登录成功", defUser.getUsername(), defUser.getNickName());
        return resultVO;
    }

    @Override
    public R<Boolean> logout() {
        try {
            StpUtil.logout();
        } catch (Exception e) {
            log.debug("token已经过期，无需清理缓存");
        }
        return R.success(true);
    }

    @Override
    public LoginResultVO switchTenantAndOrg(Long tenantId, Long orgId) {
        StpUtil.checkLogin();

        Long userId = ContextUtil.getUserId();
        DefUser defUser = defUserService.getByIdCache(userId);
        if (defUser == null) {
            throw UnauthorizedException.wrap(ExceptionCode.JWT_TOKEN_EXPIRED);
        }

        if (!Convert.toBool(defUser.getState(), true)) {
            throw UnauthorizedException.wrap(ExceptionCode.JWT_USER_DISABLE);
        }

        DefTenant defTenant = defTenantService.getByIdCache(tenantId);
        ArgumentAssert.notNull(defTenant, "您要切换的企业不存在！");
        if (!Convert.toBool(defTenant.getState(), true)) {
            throw BizException.wrap(ExceptionCode.JWT_TENANT_DISABLE);
        }

        DefUserTenantRel employee = defUserTenantRelService.getEmployeeByTenantAndUser(tenantId, userId);
        ArgumentAssert.notNull(employee, "您不属于该公司，无法切换");
        if (!Convert.toBool(employee.getState(), true)) {
            throw BizException.wrap(ExceptionCode.JWT_EMPLOYEE_DISABLE);
        }

        ContextUtil.setTenantId(tenantId);
        BaseEmployee baseEmployee = baseEmployeeService.getByIdCache(employee.getId());
        ArgumentAssert.notNull(employee, "您不属于该公司，无法切换");
        if (!Convert.toBool(employee.getState(), true)) {
            throw BizException.wrap(ExceptionCode.JWT_EMPLOYEE_DISABLE);
        }

        Long topCompanyId = null;
        Long companyId = null;
        Long deptId = null;
        if (orgId != null) {
            BaseOrg selectOrg = baseOrgService.getByIdCache(orgId);
            ArgumentAssert.notNull(selectOrg, "该部门不存在");

            if (OrgTypeEnum.COMPANY.eq(selectOrg.getType())) {
                companyId = selectOrg.getId();

                Long rootId = TreeUtil.getTopNodeId(selectOrg.getTreePath());
                if (rootId != null) {
                    BaseOrg rootCompany = baseOrgService.getByIdCache(rootId);
                    topCompanyId = rootCompany != null ? rootCompany.getId() : companyId;
                } else {
                    topCompanyId = companyId;
                }
            } else {
                deptId = selectOrg.getId();

                BaseOrg company = baseOrgService.getCompanyByDeptId(deptId);
                if (company != null) {
                    companyId = company.getId();

                    Long rootId = TreeUtil.getTopNodeId(company.getTreePath());
                    if (rootId != null) {
                        BaseOrg rootCompany = baseOrgService.getByIdCache(rootId);
                        topCompanyId = rootCompany != null ? rootCompany.getId() : companyId;
                    } else {
                        topCompanyId = companyId;
                    }
                }
            }

            baseEmployeeService.updateOrgInfo(baseEmployee.getId(), companyId, deptId);
        } else {
            baseEmployeeService.updateOrgInfo(baseEmployee.getId(), companyId, deptId);
        }


        Employee e = Employee.builder()
                .tenantId(tenantId).employeeId(employee.getId())
                .build();

        Org org = Org.builder()
                .currentTopCompanyId(topCompanyId)
                .currentCompanyId(companyId)
                .currentDeptId(deptId)
                .build();

        LoginResultVO loginResultVO = buildResult(e, org, defUser);

        LoginStatusDTO loginStatus = LoginStatusDTO.switchTenant(defUser.getId(), employee.getTenantId(), employee.getId());
        SpringUtils.publishEvent(new LoginEvent(loginStatus));
        return loginResultVO;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    private static class Employee {
        private Long employeeId;
        private Long tenantId;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    private static class Org {
        private Long currentCompanyId;
        private Long currentTopCompanyId;
        private Long currentDeptId;
    }

}
