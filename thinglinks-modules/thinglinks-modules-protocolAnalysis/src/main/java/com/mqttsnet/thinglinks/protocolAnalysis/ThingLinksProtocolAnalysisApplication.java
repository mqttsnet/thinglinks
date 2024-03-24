package com.mqttsnet.thinglinks.protocolAnalysis;

import com.mqttsnet.thinglinks.common.security.annotation.EnableCustomConfig;
import com.mqttsnet.thinglinks.common.security.annotation.EnableRyFeignClients;
import com.mqttsnet.thinglinks.common.swagger.annotation.EnableCustomSwagger2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.net.InetAddress;

@Slf4j
@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
//实现跨域注解
//origin="*"代表所有域名都可访问
//maxAge飞行前响应的缓存持续时间的最大年龄，简单来说就是Cookie的有效期 单位为秒
//若maxAge是负数,则代表为临时Cookie,不会被持久化,Cookie信息保存在浏览器内存中,浏览器关闭Cookie就消失
@CrossOrigin(origins = "*", maxAge = 3600)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = {"com.mqttsnet.thinglinks"})
public class ThingLinksProtocolAnalysisApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext application = SpringApplication.run(ThingLinksProtocolAnalysisApplication.class, args);
        Environment env = application.getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" + "应用 '{}' 启动成功! 访问连接:\n\t" + "Swagger文档: \t\thttp://{}:{}/swagger-ui.html\n\t"
                        + "数据库监控: \t\thttp://{}:{}/druid\n" + "----------------------------------------------------------", env.getProperty("spring.application.name"),
                InetAddress.getLocalHost().getHostAddress(), env.getProperty("server.port", "8080"), "127.0.0.1", env.getProperty("server.port", "8080"));
    }
}