
# mqtts平台简介

一款高性、高吞吐量、高扩展性的物联网平台！单机可以支持百万链接，同时支持自定义扩展功能多种协议交互，功能非常强大，采用netty作为通信层组件，支持插件化开发！

1、采用前后端分离的模式，前端框架(基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue))。

2、后端采用Spring Boot、Spring Cloud & Alibaba。

3、MqttBroker基于Netty、Reactor3、Reactor-netty(基于[mqtt-cluster](https://gitee.com/quickmsg/mqtt-cluster.git)))。

4、注册中心、配置中心选型Nacos，权限认证使用Redis。

5、流量控制框架选型Sentinel，分布式事务选型Seata。

6、时序数据库采用TDengine开源、高效的物联网大数据平台、处理物联网海量数据写入与负载查询。


## 系统模块

~~~
net.mqtts     
├── mqtts-ui              // 前端框架 [8088]
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
│       └── mqtts-tdengine                            // TDengine服务 [9204]
│       └── mqtts-file                                // 文件服务 [9300]
│       └── mqtts-broker                            // Mqtts Broker服务 [1883]
├── mqtts-visual          // 图形化管理模块
│       └── mqtts-visual-monitor                      // 监控中心 [9100]
├──pom.xml                // 公共依赖
~~~

## 功能列表

待更新

## 在线体验(暂未对外开放)

- admin/admin123

演示地址：http://mqtts.net
文档地址：http://doc.mqtts.net

## 开发计划

1、MQTTbroker集成时序数据库TDengine

2、客户端、主题列表

3、规则引擎

4、告警列表

5、大屏展示（客户端、消息发布订阅、告警）

## 演示图

待更新

## 配置使用

###启动命令（进入对应目录后逐一启动即可）
1、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-gateway-3.1.0.jar >/dev/null 2>&1 &

2、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-auth-3.1.0.jar >/dev/null 2>&1 &

3、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-modules-file-3.1.0.jar >/dev/null 2>&1 &

4、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-modules-gen-3.1.0.jar >/dev/null 2>&1 &

5、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-modules-job-3.1.0.jar >/dev/null 2>&1 &

6、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-modules-system-3.1.0.jar >/dev/null 2>&1 &

7、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-modules-tdengine-3.1.0.jar >/dev/null 2>&1 &

8、nohup java -Xms150m -Xmx150m -Xmn100m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -server -jar -Dfile.encoding=utf-8  ./mqtts-visual-monitor-3.1.0.jar >/dev/null 2>&1 &


##交流群

待更新