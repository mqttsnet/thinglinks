
###### mqtts平台简介
一款高性、高吞吐量、高扩展性的物联网mqttBroker！单机可以支持百万链接，同时支持自定义扩展功能多种协议交互，功能非常强大，采用netty作为通信层组件，支持插件化开发！

1、采用前后端分离的模式，前端(基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue))。

2、后端采用Spring Boot、Spring Cloud & Alibaba。

3、MqttBroker基于Netty、Reactor3、Reactor-netty。

4、注册中心、配置中心选型Nacos，权限认证使用Redis。

5、流量控制框架选型Sentinel，分布式事务选型Seata。


## 系统模块

~~~
net.mqtts     
├── mqtts-ui              // 前端框架 [80]
├── mqtts-gateway         // 网关模块 [8080]
├── mqtts-auth            // 认证中心 [9200]
├── mqtts-api             // 接口模块
│       └── mqtts-api-system                          // 系统接口
├── mqtts-common          // 通用模块
│       └── mqtts-common-core                         // 核心模块
│       └── mqtts-common-datascope                    // 权限范围
│       └── mqtts-common-datasource                   // 多数据源
│       └── mqtts-common-log                          // 日志记录
│       └── mqtts-common-redis                        // 缓存服务
│       └── mqtts-common-security                     // 安全模块
│       └── mqtts-common-swagger                      // 系统接口
├── mqtts-modules         // 业务模块
│       └── mqtts-system                              // 系统模块 [9201]
│       └── mqtts-gen                                 // 代码生成 [9202]
│       └── mqtts-job                                 // 定时任务 [9203]
│       └── mqtts-file                                // 文件服务 [9300]
├── mqtts-visual          // 图形化管理模块
│       └── mqtts-visual-monitor                      // 监控中心 [9100]
├──pom.xml                // 公共依赖
~~~

## 功能列表

待更新

## 开发计划
1、MQTTbroker集成时序数据库

2、规则引擎

