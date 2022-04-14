package com.mqttsnet.thinglinks.link.common.aop;

import com.mqttsnet.thinglinks.common.core.annotation.NoRepeatSubmit;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.utils.SecurityUtils;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;


/**
 * @author thinglinks
 * @功能描述 防止多次提交aop解析注解
 * @date 2022-02-15
 */
@Slf4j
@Aspect
@Component
public class NoRepeatSubmitAop {

    @Autowired
    private RedisService redisService;

    @Around("execution(* com.mqttsnet.thinglinks.link.controller..*.*(..)) && @annotation(nrs)")
    public Object arround(ProceedingJoinPoint pjp, NoRepeatSubmit nrs) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            log.info("请求地址：{}", request.getServletPath());
            String key = SecurityUtils.getToken() + "-" + request.getServletPath();
            log.info("newToken:{}", key);
            if (!redisService.hasKey(Constants.RESUBMIT_URL_KEY+key)) {// 如果缓存中有这个url视为重复提交
                redisService.setCacheObject(Constants.RESUBMIT_URL_KEY+key, pjp.toString(), 3000L, TimeUnit.MILLISECONDS);
                return pjp.proceed();
            } else {
                log.error("请勿重复提交");
                return AjaxResult.error("请勿重复提交");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            log.error("验证重复提交时出现未知异常!");
            return AjaxResult.error("内部服务异常!");
        }
    }

}