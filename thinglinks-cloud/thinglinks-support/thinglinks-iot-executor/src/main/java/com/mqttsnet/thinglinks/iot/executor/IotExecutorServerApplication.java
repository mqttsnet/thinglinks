package com.mqttsnet.thinglinks.iot.executor;

import com.mqttsnet.basic.validator.annotation.EnableFormValidator;
import com.mqttsnet.thinglinks.common.ServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.UnknownHostException;

import static com.mqttsnet.thinglinks.common.constant.BizConstant.BUSINESS_PACKAGE;
import static com.mqttsnet.thinglinks.common.constant.BizConstant.UTIL_PACKAGE;

/**
 * 物联网业务系统执行器（XXL-Job）
 * <p>
 * 通过各业务模块的 boot-impl 本地调用 Facade 接口，
 * 依赖链：executor → boot-impl → biz（核心业务层）。
 * <p>
 * 各业务模块的协议层已独立为单独的 Maven 模块（如 video-biz-protocol），
 * executor 不引入协议模块，协议相关的 Bean 不会出现在 classpath 中，
 * 无需额外的 ComponentScan 排除配置。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.iot.executor.service.jobhandler.VideoJob
 * @see com.mqttsnet.thinglinks.iot.executor.service.jobhandler.LinkJob
 */
@SpringBootApplication
@EnableDiscoveryClient
@Configuration
@ComponentScan({
        UTIL_PACKAGE, BUSINESS_PACKAGE
})
@EnableFeignClients(value = {UTIL_PACKAGE, BUSINESS_PACKAGE})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Slf4j
@EnableAsync
@EnableFormValidator
@EnableDynamicTp
public class IotExecutorServerApplication extends ServerApplication {

    public static void main(String[] args) throws UnknownHostException {
        start(IotExecutorServerApplication.class, args);
    }

}
