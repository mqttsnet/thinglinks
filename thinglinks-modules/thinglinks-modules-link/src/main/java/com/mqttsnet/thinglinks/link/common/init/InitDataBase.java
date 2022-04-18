package com.mqttsnet.thinglinks.link.common.init;

import com.mqttsnet.thinglinks.link.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Description: 初始化基础数据
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/3/28$ 16:12$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/3/28$ 16:12$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Component
@Slf4j
@RefreshScope
public class InitDataBase {
    private static InitDataBase InitDataBase;

    @Autowired
    private ProductService productService;

    @PostConstruct
    public void init() throws Exception {
        InitDataBase = this;
        InitDataBase.productService=this.productService;
        StopWatch watch = new StopWatch();
        watch.start();
        //初始化产品模型数据
        this.productService.createSuperTableDataModel(null);
        watch.stop();
        log.info("初始化基础数据成功 ! Time Elapsed (millisecond): {}",watch.getTime());
    }

}
