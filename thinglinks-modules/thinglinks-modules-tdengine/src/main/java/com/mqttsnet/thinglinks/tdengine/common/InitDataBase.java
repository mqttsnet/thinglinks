//package com.mqttsnet.thinglinks.tdengine.common;
//
//import com.mqttsnet.thinglinks.tdengine.service.TdEngineService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//
///**
// * @Description: 初始化数据库
// * @Author: ShiHuan SUN
// * @E-mail: 13733918655@163.com
// * @Website: http://thinglinks.mqttsnet.com
// * @CreateDate: 2022/3/28$ 16:12$
// * @UpdateUser: ShiHuan SUN
// * @UpdateDate: 2022/3/28$ 16:12$
// * @UpdateRemark: 修改内容
// * @Version: V1.0
// */
//@Component
//@Slf4j
//@RefreshScope
//public class InitDataBase {
//    private static InitDataBase InitDataBase;
//
//    @Autowired
//    private TdEngineService tdEngineService;
//
//    /**
//     * 数据库名称
//     */
//    @Value("${spring.datasource.dynamic.datasource.master.dbName:thinglinks}")
//    private String databaseName;
//
//    @PostConstruct
//    public void init() {
//        /*InitDataBase = this;
//        InitDataBase.tdEngineService=this.tdEngineService;
//        //创建数据库
//        this.tdEngineService.createDateBase(databaseName);
//        log.info("初始化数据库:{}成功!",databaseName);*/
//    }
//
//}
