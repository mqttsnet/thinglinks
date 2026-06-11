package com.mqttsnet.thinglinks;


import com.mqttsnet.thinglinks.common.ServerApplication;
import com.mqttsnet.thinglinks.common.config.ActuatorSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.UnknownHostException;

import static com.mqttsnet.thinglinks.common.constant.BizConstant.BUSINESS_PACKAGE;
import static com.mqttsnet.thinglinks.common.constant.BizConstant.UTIL_PACKAGE;

/**
 * @author mqttsnet
 * @date 2017-12-13 15:02
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
@EnableDiscoveryClient
@ComponentScan(value = {
        UTIL_PACKAGE, BUSINESS_PACKAGE
}, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = ActuatorSecurityConfig.class
))
@EnableFeignClients(value = {
        UTIL_PACKAGE, BUSINESS_PACKAGE
})
@EnableScheduling
@Slf4j
public class GatewayServerApplication extends ServerApplication {
    public static void main(String[] args) throws UnknownHostException {
        start(GatewayServerApplication.class, args);
    }

}
