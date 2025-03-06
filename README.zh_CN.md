<div align="center">

[![MQTTSNET Logo](./doc/imgs/logo.png)](http://www.mqttsnet.com)

</div>

## ThingLinks | [English Documentation](README.md)

# ThingLinks平台简介

本系统采用Spring Cloud 微服务架构,一款高性、高吞吐量、高扩展性的物联网平台!
单机可以支持百万链接,同时支持自定义扩展功能多种协议交互，支持插件化开发!

[![OSCS Status](https://www.oscs1024.com/platform/badge/mqttsnet/thinglinks.svg?size=small)](https://www.oscs1024.com/project/mqttsnet/thinglinks?ref=badge_small)

## 技术栈

1、采用前后端分离的模式，前端框架VUE。

2、后端采用Spring Boot、Spring Cloud & Alibaba。

3、MqttBroker(支持集群化部署)基于Netty、Reactor3、Reactor-netty。

4、注册中心、配置中心选型Nacos，权限认证使用Redis。

5、流量控制框架选型Sentinel，分布式事务选型Seata。

6、时序数据库采用TDengine开源、高效的物联网大数据平台、处理物联网海量数据写入与负载查询。

## 核心特性

支持统一产品模型管理,多种设备,多种厂家,统一设备连接管理,多协议适配(MQTT,WebSocket,TCP,UDP,CoAP,HTTP等)。

灵活的规则引擎,设备告警,消息通知,数据转发。

设备地理位置可视化查看,可视化大屏。

TDengine时序数据库超级表设计概念：每个设备一张表,每类设备一个超级表。

## 文档

- [官方文档](https://mqttsnet.yuque.com/gt6zkc/thinglinks?# 《ThingLinks物联网一体化平台》)

## 平台总体架构

![](doc/imgs/overallArchitecture.png)

## 平台功能架构

![](doc/imgs/functionalArchitecture.png)

## 核心功能列表

系统管理：用户管理、角色管理、菜单管理、部门管理、岗位管理、字典管理、参数设置、通知公告、日志管理

系统监控：在线用户、定时任务、Sentinel控制台、Nacos控制台、Admin控制台、任务调度管理

系统工具：表单构建、代码生成、系统接口

设备集成：设备管理（支持MQTT协议、WebSocket协议、TCP-IP协议设备接入）、子设备管理、产品管理、协议管理、规则引擎（设备联动）

设备调试：实时日志、命令下发

规则引擎消息转发：支持KAFKA节点、HTTP节点、PREDICATE节点、ROCKET_MQ节点、RABBIT_MQ节点、MYSQL节点、MQTT节点、TOPIC节点、LOG节点

## 功能开发计划

近期在全力投入Pro版本研发，开源版本有些滞后，后面会逐步开源，敬请期待！

*、规则引擎-设备联动(开发完成，缺少告警信息的配置)

*、APP移动端-我的设备(实现中)

*、告警管理（钉钉推送、邮件推送等）(规划中)

*、大屏展示（客户端、消息发布订阅、告警）（规划中）

## 演示图

![](doc/imgs/deviceIntegration/img.png)

![](doc/imgs/deviceIntegration/img_0.png)

![](doc/imgs/deviceIntegration/img_1.png)

![](doc/imgs/deviceIntegration/img_2.png)

![](doc/imgs/deviceIntegration/img_3.png)

![](doc/imgs/deviceIntegration/img_4.png)

![](doc/imgs/deviceIntegration/img_5.png)

![](doc/imgs/deviceIntegration/img_6.png)

## 商业合作&贡献代码

如果你有兴趣参与项目开发或进行商务合作，请联系mqttsnet团队邮箱: mqttsnet@163.com

## Contributors

Thanks these wonderful people, welcome to join us:   
[贡献者指南](CONTRIBUTING.md)

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/xiaonannet"><img src="https://avatars.githubusercontent.com/u/57425893?s=400&u=7e19afcd215072e56e0168c6717342456b174b68&v=4" width="100px;" alt="xiaonannet"/><br /><sub><b>xiaonannet</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=xiaonannet" title="Code">💻</a> <a href="https://github.com/mqttsnet/thinglinks/commits?author=xiaonannet" title="Documentation">📖</a> <a href="#xiaonannet" title="Design">🎨</a><a href="https://github.com/mqttsnet/thinglinks/commits?author=lvwshuai" title="Bug reports">🐛</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/lovely-shisen"><img src="https://avatars.githubusercontent.com/u/32031196?s=400&u=7e19afcd215072e56e0168c6717342456b174b68&v=4" width="100px;" alt="lovely-shisen"/><br /><sub><b>lovely-shisen</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=lovely-shisen" title="Code">💻</a> <a href="https://github.com/mqttsnet/thinglinks/commits?author=lovely-shisen" title="Documentation">📖</a> <a href="#lovely-shisen" title="Design">🎨</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/whb-11"><img src="https://avatars.githubusercontent.com/u/95611581?v=4" width="100px;" alt="wanghoubang"/><br /><sub><b>whb-11</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=whb-11" title="Code">💻</a> <a href="https://github.com/mqttsnet/thinglinks/commits?author=whb-11" title="Documentation">📖</a><a href="#lovely-shisen" title="Design">🎨</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/kangkaime"><img src="https://avatars.githubusercontent.com/u/4422866?v=4" width="100px;" alt="kangkaime"/><br /><sub><b>kangkaime</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=kangkaime" title="Code">💻</a> <a href="https://github.com/mqttsnet/thinglinks/commits?author=kangkaime" title="Bug reports">🐛</a></td>      
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/lvwshuai"><img src="https://avatars.githubusercontent.com/u/2986900?v=4" width="100px;" alt="lvwshuai"/><br /><sub><b>lvwshuai</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=lvwshuai" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/andyz168"><img src="https://avatars.githubusercontent.com/u/22382971?v=4" width="100px;" alt="andyz168"/><br /><sub><b>andyz168</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=andyz168" title="Code">💻</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/llJam"><img src="https://avatars.githubusercontent.com/u/113015997?v=4" width="100px;" alt="llJam"/><br /><sub><b>llJam</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=llJam" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/qianmenfei"><img src="https://avatars.githubusercontent.com/u/39989430?v=4" width="100px;" alt="qianmenfei"/><br /><sub><b>qianmenfei</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=qianmenfei" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/wangfan1997"><img src="https://avatars.githubusercontent.com/u/108504684?v=4" width="100px;" alt="wangfan1997"/><br /><sub><b>wangfan1997</b></sub></a><br /><a href="https://github.com/mqttsnet/thinglinks/commits?author=wangfan1997" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- ALL-CONTRIBUTORS-LIST:END -->

## 友情链接

BifroMQ 是一个高性能的 MQTT 消息中间件，采用原生多租户架构。由百度开源。

- [官网](bifromq.io)
- [Github](https://github.com/baidu/bifromq)

高效MQTT Broker: SMQTTX

- [Github](https://github.com/quickmsg/smqttx)
- [Gitee](https://gitee.com/quickmsg/smqttx)
- [GitCode](https://gitcode.com/quickmsg/smqttx)

## issues(欢迎大家提出宝贵意见)

[issues](https://github.com/mqttsnet/thinglinks/issues)

## 欢迎提交 pr

[pr->feat/contrbute](https://github.com/mqttsnet/thinglinks/pulls)

## License(开源协议)

[Apache License, Version 2.0](LICENSE)

[感谢JetBrains官方提供免费 IDEA License](https://www.jetbrains.com)

## 版权使用说明

ThingLinks开源平台遵循 [Apache License, Version 2.0](LICENSE) 协议。 允许商业使用，但务必保留类作者、Copyright 信息。
