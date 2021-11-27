package net.mqtts.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * 网关启动程序
 * 
 * @author mqtts
 */
//实现跨域注解
//origin="*"代表所有域名都可访问
//maxAge飞行前响应的缓存持续时间的最大年龄，简单来说就是Cookie的有效期 单位为秒
//若maxAge是负数,则代表为临时Cookie,不会被持久化,Cookie信息保存在浏览器内存中,浏览器关闭Cookie就消失
@CrossOrigin(origins = "*",maxAge = 3600)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class MqttsGatewayApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(MqttsGatewayApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  MQTTS网关启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
