package com.mqttsnet.thinglinks.collection;

import com.mqttsnet.thinglinks.common.security.annotation.EnableRyFeignClients;
import com.mqttsnet.thinglinks.common.swagger.annotation.EnableCustomSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@EnableCustomSwagger2
@EnableRyFeignClients
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableCaching
@EnableScheduling
@CrossOrigin(origins = "*", maxAge = 3600)
public class ThingLinksCollectionApplication {
    public static void main(String[] args) {
        {
            SpringApplication.run(ThingLinksCollectionApplication.class, args);
            System.out.println("(♥◠‿◠)ﾉﾞ  服务采集系统启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                    " .-------.       ____     __        \n" +
                    " |  _ _   \\      \\   \\   /  /    \n" +
                    " | ( ' )  |       \\  _. /  '       \n" +
                    " |(_ o _) /        _( )_ .'         \n" +
                    " | (_,_).' __  ___(_ o _)'          \n" +
                    " |  |\\ \\  |  ||   |(_,_)'         \n" +
                    " |  | \\ `'   /|   `-'  /           \n" +
                    " |  |  \\    /  \\      /           \n" +
                    " ''-'   `'-'    `-..-'              ");
        }
    }


}
