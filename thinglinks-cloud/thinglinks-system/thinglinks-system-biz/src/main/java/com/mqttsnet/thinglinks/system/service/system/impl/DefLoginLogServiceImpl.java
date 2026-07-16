package com.mqttsnet.thinglinks.system.service.system.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.log.util.AddressUtil;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.system.entity.system.DefLoginLog;
import com.mqttsnet.thinglinks.system.entity.tenant.DefUser;
import com.mqttsnet.thinglinks.system.manager.system.DefLoginLogManager;
import com.mqttsnet.thinglinks.system.manager.tenant.DefUserManager;
import com.mqttsnet.thinglinks.system.service.system.DefLoginLogService;
import com.mqttsnet.thinglinks.system.vo.save.system.DefLoginLogSaveVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p>
 * 业务实现类
 * 登录日志
 * </p>
 *
 * @author mqttsnet
 * @date 2021-11-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@DS(DsConstant.DEFAULTS)
public class DefLoginLogServiceImpl extends SuperServiceImpl<DefLoginLogManager, Long, DefLoginLog> implements DefLoginLogService {
    private static final Supplier<Stream<String>> BROWSER = () -> Stream.of(
            "Chrome", "Firefox", "Microsoft Edge", "Safari", "Opera"
    );
    private static final Supplier<Stream<String>> OPERATING_SYSTEM = () -> Stream.of(
            "Android", "Linux", "Mac OS X", "Ubuntu", "Windows 10", "Windows 8", "Windows 7", "Windows XP", "Windows Vista"
    );
    private final DefUserManager defUserManager;

    private static String simplifyOperatingSystem(String operatingSystem) {
        return OPERATING_SYSTEM.get().parallel().filter(b -> StrUtil.containsIgnoreCase(operatingSystem, b)).findAny().orElse(operatingSystem);
    }

    private static String simplifyBrowser(String browser) {
        return BROWSER.get().parallel().filter(b -> StrUtil.containsIgnoreCase(browser, b)).findAny().orElse(browser);
    }

    @Override
    protected <SaveVO> DefLoginLog saveBefore(SaveVO saveVO) {
        DefLoginLogSaveVO defLoginLogSaveVO = (DefLoginLogSaveVO) saveVO;
        DefLoginLog defLoginLog = super.saveBefore(defLoginLogSaveVO);
        DefUser user;
        if (defLoginLog.getUserId() != null) {
            user = this.defUserManager.getByIdCache(defLoginLog.getUserId());
        } else if (StrUtil.isNotEmpty(defLoginLogSaveVO.getMobile())) {
            user = this.defUserManager.getUserByMobile(defLoginLogSaveVO.getMobile());
        } else {
            user = this.defUserManager.getUserByUsername(defLoginLog.getUsername());
        }

        defLoginLog.setLocation(resolveIpLocation(defLoginLogSaveVO.getRequestIp()));
        defLoginLog.setLoginDate(DateUtils.formatAsDate(LocalDateTime.now()));

        UserAgent userAgent = UserAgentUtil.parse(defLoginLog.getUa());
        Browser browser = userAgent.getBrowser();
        OS os = userAgent.getOs();
        String browserVersion = userAgent.getVersion();
        if (browser != null) {
            defLoginLog.setBrowser(simplifyBrowser(browser.getName()));
        }
        if (browserVersion != null) {
            defLoginLog.setBrowserVersion(browserVersion);
        }
        if (os != null) {
            defLoginLog.setOperatingSystem(simplifyOperatingSystem(os.getName()));
        }
        if (user != null) {
            defLoginLog.setUsername(user.getUsername()).setUserId(user.getId()).setNickName(user.getNickName())
                    .setCreatedBy(user.getId());
        }
        return defLoginLog;
    }

    private String resolveIpLocation(String requestIp) {
        if (StrUtil.isBlank(requestIp) || isLocalHostIp(requestIp)) {
            return "";
        }
        try {
            return AddressUtil.getRegion(requestIp);
        } catch (Exception e) {
            log.warn("解析ip失败 requestIp={}", requestIp, e);
            return "";
        }
    }

    /**
     * 判断是否为本地IP地址的方法
     */
    private boolean isLocalHostIp(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return inetAddress.isLoopbackAddress();
        } catch (UnknownHostException e) {
            // 处理异常情况，如果无法解析IP地址，则不视为本地地址
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearLog(LocalDateTime clearBeforeTime, Integer clearBeforeNum) {
        return superManager.clearLog(clearBeforeTime, clearBeforeNum) > 0;
    }
}
