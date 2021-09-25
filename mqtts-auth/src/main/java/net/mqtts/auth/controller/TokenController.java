package net.mqtts.auth.controller;

import javax.servlet.http.HttpServletRequest;

import net.mqtts.common.core.domain.R;
import net.mqtts.common.core.utils.StringUtils;
import net.mqtts.common.security.service.TokenService;
import net.mqtts.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import net.mqtts.auth.form.LoginBody;
import net.mqtts.auth.form.RegisterBody;
import net.mqtts.auth.service.SysLoginService;

/**
 * token 控制
 * 
 * @author mqtts
 */
@RestController
public class TokenController
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysLoginService sysLoginService;

    @PostMapping("login")
    public R<?> login(@RequestBody LoginBody form)
    {
        // 用户登录
        LoginUser userInfo = sysLoginService.login(form.getUsername(), form.getPassword());
        // 获取登录token
        return R.ok(tokenService.createToken(userInfo));
    }

    @DeleteMapping("logout")
    public R<?> logout(HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            String username = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            sysLoginService.logout(username);
        }
        return R.ok();
    }

    @PostMapping("refresh")
    public R<?> refresh(HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            // 刷新令牌有效期
            tokenService.refreshToken(loginUser);
            return R.ok();
        }
        return R.ok();
    }

    @PostMapping("register")
    public R<?> register(@RequestBody RegisterBody registerBody)
    {
        // 用户注册
        sysLoginService.register(registerBody.getUsername(), registerBody.getPassword());
        return R.ok();
    }
}
