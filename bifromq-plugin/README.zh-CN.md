<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# bifromq-plugin — 旗舰版

**ThingLinks 物联网平台 — BifroMQ MQTT Broker 插件库**

[English](README.md) | 简体中文 | [日本語](README.ja.md) | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![BifroMQ](https://img.shields.io/badge/BifroMQ-3.3.5-blue?style=flat-square)](https://github.com/baidu/bifromq)
[![Edition](https://img.shields.io/badge/版本-旗舰版-gold?style=flat-square)](LICENSE-COMMERCIAL)

</div>

---

## 简介

`bifromq-plugin` 是 [ThingLinks](https://mqttsnet.com) 物联网平台的 BifroMQ MQTT Broker 插件库（旗舰版），通过 BifroMQ 插件机制实现自定义业务逻辑与 ThingLinks IoT 系统的运行时集成。

## 插件列表

| 插件 | 说明 |
|------|------|
| `bifromq-auth-provider-plugin` | MQTT 客户端认证授权（用户名密码、证书、ACL） |
| `bifromq-event-collector-plugin` | BifroMQ 事件采集、过滤及性能监控 |
| `bifromq-resource-throttler-plugin` | 多租户资源分配与限流 |
| `bifromq-setting-provider-plugin` | 运行时动态租户级别配置 |

## 构建

```bash
mvn clean package -DskipTests
```

## 授权激活

购买旗舰版后，请将授权 ID 填入 [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL) 并通过 git commit 提交。

## 🤖 Agent Skills(AI 辅助开发)

本仓库配套官方 **Agent Skill**,让 AI Agent(Claude Code · Codex · Cursor)基于真实代码、按需加载地理解这套 BifroMQ 插件。隶属 **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** 合集(一仓一 skill)。

```bash
# 全局(-g);去掉 -g 仅装当前项目
npx skills add mqttsnet/thinglinks-skills@bifromq-plugin -g
```

涉及 BifroMQ 认证/ACL、事件采集(Kafka)、配置/限流 provider、`EventTypeEnum ↔ DeviceActionTypeEnum` 映射,或插件部署时自动触发。全部 skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)见 **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)**。

## 开源协议

基于 [Apache License 2.0](LICENSE)，附加条款详见 [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL)。

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
