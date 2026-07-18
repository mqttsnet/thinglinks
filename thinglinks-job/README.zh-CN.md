<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# thinglinks-job

**ThingLinks 物联网平台 — 分布式任务调度**

[English](README.md) | 简体中文 | [日本語](README.ja.md) | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)

</div>

---

## 简介

`thinglinks-job` 是 [ThingLinks](https://mqttsnet.com) 物联网平台的分布式任务调度服务，基于 XXL-Job 二次开发，并针对物联网任务场景进行适配。

产品身份、组件版本、依赖版本和发行信息统一维护在 [`.thinglinks-product.env`](.thinglinks-product.env)。

## 编译顺序

```bash
cd thinglinks-util && mvn clean install -DskipTests
cd ../thinglinks-cloud && mvn clean install -DskipTests
cd ../thinglinks-job && mvn clean package -DskipTests
```

数据库初始化脚本位于 [`docs/db/mysql/baseline/thinglinks_job.sql`](docs/db/mysql/baseline/thinglinks_job.sql)，增量升级注意事项见 [数据库脚本说明](docs/db/mysql/README.md)。

## 产品配置

快捷操作说明见 [产品配置快捷操作](docs/产品配置快捷操作.md)。

## 授权协议

本项目基于 [XXL-Job](https://github.com/xuxueli/xxl-job) 二次开发，并保留原始 [GPL v3 协议](LICENSE)。当前发行版本适用的附加条款由产品清单及其引用的授权文件声明。

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

</div>
