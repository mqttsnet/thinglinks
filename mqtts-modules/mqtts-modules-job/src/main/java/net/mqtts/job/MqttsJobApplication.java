package net.mqtts.job;

import net.mqtts.common.security.annotation.EnableCustomConfig;
import net.mqtts.common.security.annotation.EnableRyFeignClients;
import net.mqtts.common.swagger.annotation.EnableCustomSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * 定时任务
 * 
 * @author mqtts
 */
@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
//实现跨域注解
//origin="*"代表所有域名都可访问
//maxAge飞行前响应的缓存持续时间的最大年龄，简单来说就是Cookie的有效期 单位为秒
//若maxAge是负数,则代表为临时Cookie,不会被持久化,Cookie信息保存在浏览器内存中,浏览器关闭Cookie就消失
@CrossOrigin(origins = "*",maxAge = 3600)
@SpringBootApplication
public class MqttsJobApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(MqttsJobApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  定时任务模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
