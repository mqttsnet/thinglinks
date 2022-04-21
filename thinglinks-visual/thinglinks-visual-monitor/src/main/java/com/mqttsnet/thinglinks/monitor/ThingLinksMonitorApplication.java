package com.mqttsnet.thinglinks.monitor;

import com.mqttsnet.thinglinks.common.swagger.annotation.EnableCustomSwagger2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@EnableCustomSwagger2
@SpringBootApplication(scanBasePackages = {"com.mqttsnet.thinglinks"})
@MapperScan("com.mqttsnet.thinglinks.monitor.mapper")
@EnableCaching
@EnableScheduling
@CrossOrigin(origins = "*",maxAge = 3600)
public class ThingLinksMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThingLinksMonitorApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  监控中心服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
