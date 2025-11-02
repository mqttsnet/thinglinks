package com.xxl.job.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author xuxueli 2018-10-28 00:38:13
 */
@SpringBootApplication
public class XxlJobAdminApplication {
    private static final Logger log = LoggerFactory.getLogger(XxlJobAdminApplication.class);

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(XxlJobAdminApplication.class, args);
        Environment env = application.getEnvironment();
        String msg = """
                
                ----------------------------------------------------------
                应用 '{}' 启动成功， JDK版本号：{} ！
                xxl-job 调度器地址:   http://{}:{}{}
                当前环境变量：{}
                ----------------------------------------------------------
                """;

        log.info(msg,
                env.getProperty("spring.application.name"),
                env.getProperty("java.version"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                env.getProperty("spring.profiles.active")
        );
    }


}