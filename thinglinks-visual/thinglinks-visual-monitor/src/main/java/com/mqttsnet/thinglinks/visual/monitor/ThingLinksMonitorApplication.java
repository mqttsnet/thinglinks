package com.mqttsnet.thinglinks.visual.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 监控中心
 * 
 * @author thinglinks
 */
@EnableAdminServer
@SpringBootApplication
public class ThingLinksMonitorApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ThingLinksMonitorApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  监控中心启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
