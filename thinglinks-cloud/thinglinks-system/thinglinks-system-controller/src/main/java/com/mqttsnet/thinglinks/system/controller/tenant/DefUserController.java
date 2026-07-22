package com.mqttsnet.thinglinks.system.controller.tenant;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperExcelController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUser;
import com.mqttsnet.thinglinks.system.service.tenant.DefUserService;
import com.mqttsnet.thinglinks.system.vo.query.system.OnlineUsersPageQuery;
import com.mqttsnet.thinglinks.system.vo.query.tenant.DefUserPageQuery;
import com.mqttsnet.thinglinks.system.vo.result.system.OnlineTokenResultVO;
import com.mqttsnet.thinglinks.system.vo.result.system.OnlineUsersResultVO;
import com.mqttsnet.thinglinks.system.vo.result.tenant.DefUserDetailsResultVO;
import com.mqttsnet.thinglinks.system.vo.result.tenant.DefUserExcelVO;
import com.mqttsnet.thinglinks.system.vo.result.tenant.DefUserResultVO;
import com.mqttsnet.thinglinks.system.vo.save.tenant.DefUserSaveVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserPasswordResetVO;
import com.mqttsnet.thinglinks.system.vo.update.tenant.DefUserUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 * 前端控制器
 * 用户
 * </p>
 *
 * @author mqttsnet
 * @date 2021-10-09
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/defUser")
@Tag(name = "用户")
public class DefUserController extends SuperExcelController<DefUserService, Long, DefUser, DefUserSaveVO, DefUserUpdateVO, DefUserPageQuery, DefUserResultVO> {

    private final EchoService echoService;

    public static <T> IPage<T> buildPager(long pageSize, long pageIndex, List<T> list) {
        //使用list 中的sublist方法分页
        List<T> dataList = new ArrayList<>();
        IPage<T> pageInfoVo = new Page<>(pageIndex, pageSize);
        //当前第几页数据
        long currentPage;
        // 一共多少条记录
        long totalRecord = list.size();
        // 一共多少页
        long totalPage = totalRecord % pageSize;
        if (totalPage > 0) {
            totalPage = totalRecord / pageSize + 1;
        } else {
            totalPage = totalRecord / pageSize;
        }
        pageInfoVo.setTotal(totalRecord);
        // 当前第几页数据
        currentPage = Math.min(totalPage, pageIndex);
        // 起始索引
        int fromIndex = (int) (pageSize * (currentPage - 1));
        // 结束索引
        int toIndex = (int) (Math.min(pageSize * currentPage, totalRecord));
        try {
            if (!list.isEmpty()) {
                dataList = list.subList(fromIndex, toIndex);
            }
        } catch (IndexOutOfBoundsException e) {
            log.error("e", e);
        }
        pageInfoVo.setRecords(dataList);
        return pageInfoVo;
    }

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Operation(summary = "检测用户名是否存在")
    @GetMapping("/checkUsername")
    @WebLog("'检测用户名是否存在, username=' + #username + ', id=' + #id")
    public R<Boolean> checkUsername(@RequestParam String username, @RequestParam(required = false) Long id) {
        return success(superService.checkUsername(username, id));
    }

    @Operation(summary = "检测邮箱是否存在")
    @GetMapping("/checkEmail")
    @WebLog("'检测邮箱是否存在, email=' + #email + ', id=' + #id")
    public R<Boolean> checkEmail(@RequestParam String email, @RequestParam(required = false) Long id) {
        return success(superService.checkEmail(email, id));
    }

    @Operation(summary = "检测身份证是否存在")
    @GetMapping("/checkIdCard")
    @WebLog("'检测身份证是否存在, idCard=' + #idCard + ', id=' + #id")
    public R<Boolean> checkIdCard(@RequestParam String idCard, @RequestParam(required = false) Long id) {
        return success(superService.checkIdCard(idCard, id));
    }

    @Operation(summary = "检测手机号是否存在")
    @GetMapping("/checkMobile")
    @WebLog("'检测手机号是否存在, mobile=' + #mobile + ', id=' + #id")
    public R<Boolean> checkMobile(@RequestParam String mobile, @RequestParam(required = false) Long id) {
        return success(superService.checkMobile(mobile, id));
    }

    /**
     * 重置密码
     *
     * @param data 修改实体
     * @return 是否成功
     */
    @Operation(summary = "重置密码", description = "重置密码")
    @PutMapping("/resetPassword")
    @WebLog("'修改密码:' + #data.id")
    public R<Boolean> resetPassword(@RequestBody @Validated DefUserPasswordResetVO data) {
        return success(superService.resetPassword(data));
    }

    /**
     * 修改状态
     *
     * @param id    用户id
     * @param state 用户状态
     * @return 是否成功
     */
    @Operation(summary = "修改状态", description = "修改状态")
    @PutMapping("/updateState")
    @WebLog("'修改状态:id=' + #id + ', state=' + #state")
    public R<Boolean> updateState(
            @NotNull(message = "请选择用户") @RequestParam Long id,
            @NotNull(message = "请设置正确的状态值") @RequestParam Boolean state) {
        return success(superService.updateState(id, state));
    }

    @Operation(summary = "查询未绑定到该企业的用户")
    @PostMapping("/findNotUserByTenantId")
    @WebLog("查询未绑定到该企业的用户")
    public R<IPage<DefUserResultVO>> findNotUserByTenantId(@RequestBody @Validated PageParams<DefUserPageQuery> params) {
        IPage<DefUserResultVO> page = superService.findNotUserByTenantId(params);
        echoService.action(page.getRecords());
        return success(page);
    }

    @Operation(summary = "查询所有的用户id", description = "查询所有的用户id")
    @PostMapping(value = "/findAllUserId")
    @WebLog("查询所有的用户id")
    public R<List<Long>> findAllUserId(@RequestParam Long tenantId) {
        return R.success(superService.findUserIdList(null));
    }

    @Operation(summary = "查找同一企业下的用户", description = "查找同一企业下的用户")
    @PostMapping(value = "/pageUser")
    @WebLog("查找同一企业下的用户")
    public R<IPage<DefUserResultVO>> pageUserByTenant(@RequestBody @Validated PageParams<DefUserPageQuery> params) {
        IPage<DefUserResultVO> page = superService.pageUserByTenant(params);
        echoService.action(page);
        return R.success(page);
    }

    @Operation(summary = "邀请员工进入企业前精确查询用户", description = "邀请员工进入企业前精确查询用户")
    @PostMapping(value = "/queryUser")
    @WebLog("邀请员工进入企业前精确查询用户")
    public R<List<DefUserResultVO>> queryUser(@RequestBody DefUserPageQuery params) {
        List<DefUserResultVO> result = superService.queryUser(params);
        echoService.action(result);
        return R.success(result);
    }

    @Override
    public Class<?> getExcelClass() {
        return DefUserExcelVO.class;
    }

    @PostMapping("/onlineUsers/logout")
    @Operation(summary = "强制注销")
    @WebLog("强制注销")
    public R<Boolean> logout(@RequestParam(required = false) Long userId, @RequestParam(required = false) String token) {
        if (userId != null) {
            StpUtil.logout(userId);
        }
        if (StrUtil.isNotEmpty(token)) {
            StpUtil.logoutByTokenValue(token);
        }
        return R.success(true);
    }

    @PostMapping("/onlineUsers/kickout")
    @Operation(summary = "踢人下线")
    @WebLog("踢人下线")
    public R<Boolean> kickout(@RequestParam(required = false) Long userId, @RequestParam(required = false) String token) {
        if (userId != null) {
            StpUtil.kickout(userId);
        }
        if (StrUtil.isNotEmpty(token)) {
            StpUtil.kickoutByTokenValue(token);
        }
        return R.success(true);
    }

    @PostMapping("/onlineUsers/page")
    @Operation(summary = "获取在线人员")
    @WebLog("获取在线人员")
    public R<IPage<OnlineUsersResultVO>> onlineUsersPage(@RequestBody @Validated PageParams<OnlineUsersPageQuery> params) {
        OnlineUsersPageQuery model = params.getModel();

        List<String> sessionIdList = StpUtil.searchSessionId(StringPool.EMPTY, 0, -1, false);
        List<OnlineUsersResultVO> loginUserList = new ArrayList<>(sessionIdList.size());
        LocalDateTime currentTime = LocalDateTime.now();
        for (String sessionId : sessionIdList) {
            // 根据会话id，查询对应的 SaSession 对象，此处一个 SaSession 对象即代表一个登录的账号
            SaSession session = StpUtil.getSessionBySessionId(sessionId);

            DefUser defUser = superService.getByIdCache(Convert.toLong(session.getLoginId()));

            OnlineUsersResultVO bean = BeanUtil.toBean(session, OnlineUsersResultVO.class);
            if (defUser != null) {
                if (StrUtil.isNotEmpty(model.getUsername()) && !StrUtil.containsIgnoreCase(defUser.getUsername(), model.getUsername())) {
                    continue;
                }
                if (StrUtil.isNotEmpty(model.getNickName()) && !StrUtil.containsIgnoreCase(defUser.getNickName(), model.getNickName())) {
                    continue;
                }
                bean.setNickName(defUser.getNickName());
                bean.setUsername(defUser.getUsername());
            }
            bean.setSessionTime(DateUtils.getDateTimeOfTimestamp(bean.getCreateTime()));
            bean.setExpireTime(DateUtils.getDateTimeOfTimestamp(System.currentTimeMillis() + bean.timeout() * 1000));
            Duration duration = Duration.between(bean.getSessionTime(), currentTime);
            bean.setSessionStr(DateUtils.tranDurationToShow(duration));

            Duration expireDuration = Duration.between(bean.getExpireTime(), currentTime);
            bean.setExpireStr(DateUtils.tranDurationToShow(expireDuration));

            loginUserList.add(bean);
        }
        List<OnlineUsersResultVO> sortedList = loginUserList.stream().sorted(((o1, o2) -> o2.getSessionTime().compareTo(o1.getSessionTime()))).collect(Collectors.toList());
        return R.success(buildPager(params.getSize(), params.getCurrent(), sortedList));
    }

    @PostMapping("/onlineUsers/getTokenSignList")
    @Operation(summary = "获取此 Session 绑定的 Token 签名列表 ")
    @WebLog("获取 Token 签名列表 ")
    public R<IPage<OnlineTokenResultVO>> getTokenSignList(@RequestBody @Validated PageParams<OnlineUsersPageQuery> params) {
        SaSession session = StpUtil.getSessionBySessionId(params.getModel().getSessionId());
        if (session == null) {
            return R.success(new Page<>());
        }
        List<SaTerminalInfo> tokenSignList = session.getTerminalList();

        List<OnlineTokenResultVO> loginUserList = new ArrayList<>(tokenSignList.size());
        LocalDateTime currentTime = LocalDateTime.now();
        for (SaTerminalInfo tokenSign : tokenSignList) {
            OnlineTokenResultVO bean = BeanUtil.toBean(tokenSign, OnlineTokenResultVO.class);
            try {
                SaSession tokenSession = StpUtil.getTokenSessionByToken(tokenSign.getTokenValue());

                bean.setSessionTime(DateUtils.getDateTimeOfTimestamp(tokenSession.getCreateTime()));
                bean.setExpireTime(DateUtils.getDateTimeOfTimestamp(System.currentTimeMillis() + tokenSession.timeout() * 1000));

                Duration duration = Duration.between(bean.getSessionTime(), currentTime);
                bean.setSessionStr(DateUtils.tranDurationToShow(duration));

                Duration expireDuration = Duration.between(bean.getExpireTime(), currentTime);
                bean.setExpireStr(DateUtils.tranDurationToShow(expireDuration));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            loginUserList.add(bean);
        }
        IPage<OnlineTokenResultVO> page = new Page<>(params.getCurrent(), params.getSize(), loginUserList.size());
        page.setRecords(loginUserList);
        return R.success(page);
    }

    @PostMapping(value = "/getDefUserByIds")
    @Operation(summary = "根据ID集合获取默认用户详情列表")
    public R<List<DefUserDetailsResultVO>> getDefUserByIds(@RequestBody @NotEmpty(message = "ID集合不能为空") List<Long> ids) {
        List<DefUserDetailsResultVO> result = superService.getDefUserByIds(ids);
        echoService.action(result);
        return R.success(result);
    }
}
