package com.mqttsnet.thinglinks.oauth.biz;

import cn.hutool.core.bean.BeanUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.UnauthorizedException;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.base.entity.user.BaseEmployee;
import com.mqttsnet.thinglinks.base.service.user.BaseEmployeeService;
import com.mqttsnet.thinglinks.base.vo.result.user.BaseEmployeeResultVO;
import com.mqttsnet.thinglinks.common.constant.AppendixType;
import com.mqttsnet.thinglinks.common.constant.DefValConstants;
import com.mqttsnet.thinglinks.file.service.AppendixService;
import com.mqttsnet.thinglinks.model.vo.result.AppendixResultVO;
import com.mqttsnet.thinglinks.oauth.vo.result.DefUserInfoResultVO;
import com.mqttsnet.thinglinks.system.entity.application.DefApplication;
import com.mqttsnet.thinglinks.system.entity.tenant.DefTenant;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUser;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUserTenantRel;
import com.mqttsnet.thinglinks.system.service.application.DefApplicationService;
import com.mqttsnet.thinglinks.system.service.tenant.DefTenantService;
import com.mqttsnet.thinglinks.system.service.tenant.DefUserService;
import com.mqttsnet.thinglinks.system.service.tenant.DefUserTenantRelService;
import com.mqttsnet.thinglinks.system.vo.result.application.DefApplicationResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户大业务
 *
 * @author mqttsnet
 * @date 2021/10/28 13:09
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OauthUserBiz {
    private final BaseEmployeeService baseEmployeeService;
    private final DefUserTenantRelService defUserTenantRelService;
    private final DefUserService defUserService;
    private final DefApplicationService defApplicationService;
    private final AppendixService appendixService;
    private final DefTenantService defTenantService;

    public DefUserInfoResultVO getUserById(Long id) {
        // 查默认库
        DefUser defUser = defUserService.getByIdCache(id);
        if (defUser == null) {
            log.warn("user not found, id:{}", id);
            return null;
        }

        // 用户信息
        DefUserInfoResultVO resultVO = new DefUserInfoResultVO();
        BeanUtil.copyProperties(defUser, resultVO);

        // 用户头像
        AppendixResultVO appendix = appendixService.getByBiz(defUser.getId(), AppendixType.System.DEF__USER__AVATAR);
        if (appendix != null) {
            resultVO.setAvatarId(appendix.getId());
        }

        DefTenant tenant = defTenantService.getById(ContextUtil.getTenantId());
        if (tenant != null && DefValConstants.DEF_TENANT_ID.equals(tenant.getId())) {
            // 演示环境专用标识，用于WriteInterceptor拦截器判断演示环境需要禁止用户执行sql，若您无需搭建演示环境，可以删除下面一行代码
            ContextUtil.setStop();
        }

        resultVO.setTenantId(ContextUtil.getTenantId());
        resultVO.setTenantName(Optional.ofNullable(tenant).map(DefTenant::getName).orElse(StrPool.EMPTY));

        if (!ContextUtil.isEmptyBasePoolNameHeader() && !ContextUtil.isEmptyEmployeeId()) {
            Long employeeId = ContextUtil.getEmployeeId();
            resultVO.setEmployeeId(employeeId);

            // 默认库的员工不存在说明该用户不属于该企业，就不能查 租户库的数据
            DefUserTenantRel defEmployee = defUserTenantRelService.getByIdCache(employeeId);
            if (defEmployee != null) {
                try {
                    //查 租户库
                    BaseEmployee employee = baseEmployeeService.getByIdCache(employeeId);
                    resultVO.setBaseEmployee(BeanUtil.toBean(employee, BaseEmployeeResultVO.class));
                } catch (Exception e) {
                    log.warn("查询您的身份信息失败，请重新登录", e);
                    // 报错说明 租户库没创建
                    throw UnauthorizedException.wrap("查询您的身份信息失败，请重新登录");
                }
            }
        }

        DefApplication defApplication = defApplicationService.getDefApp(id);
        resultVO.setDefApplication(BeanUtil.toBean(defApplication, DefApplicationResultVO.class));
        return resultVO;
    }
}
