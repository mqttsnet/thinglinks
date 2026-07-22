<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="200"></a>

# ThingLinks Cloud

**多租户 SaaS 云物联网平台 — 高性能 · 高吞吐量 · 高扩展性**

[English](README.md) | 简体中文 | [日本語](README.ja.md) | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025-green?style=flat-square&logo=spring)](https://spring.io/projects/spring-cloud)
[![TDengine](https://img.shields.io/badge/TDengine-3.0+-blue?style=flat-square)](https://tdengine.com/)
<br>

[![Website](https://img.shields.io/badge/官网-mqttsnet.com-blue?style=for-the-badge)](https://mqttsnet.com)
[![Docs](https://img.shields.io/badge/文档-在线文档-green?style=for-the-badge)](https://mqttsnet.yuque.com/trgbro/thinglinks)

</div>

---

## 平台简介

ThingLinks Cloud 是 [ThingLinks](https://mqttsnet.com) 多租户 SaaS 云物联网平台的后端工程。基于 Spring Cloud 微服务架构，具备**高性能、高吞吐量、高扩展性**的设备接入能力，单机支持**百万级并发连接**。

产品发行版本与组件版本信息见根目录 [`.thinglinks-product.env`](.thinglinks-product.env)。

## 核心特性

| 特性 | 说明 |
|------|------|
| **多租户 SaaS** | 企业级多租户架构，完整租户隔离 |
| **百万级连接** | 单节点支持百万级设备并发连接 |
| **多协议支持** | MQTT、WebSocket、TCP、UDP、CoAP、HTTP 等 |
| **设备管理** | 统一产品模型、设备全生命周期管理、子设备管理 |
| **规则引擎** | 灵活的告警规则、消息通知、数据转发 |
| **SCADA 与可视化** | 设备地理位置可视化、SCADA 组态、大屏展示 |
| **时序数据库** | TDengine — 每个设备一张表，每类设备一个超级表 |
| **插件系统** | 插件化开发，支持自定义协议和功能扩展 |
| **物联卡管理** | SIM 卡通道管理、卡片生命周期管理 |
| **流媒体** | 流媒体服务集成、视频流代理 |
| **MCP 集成** | Model Context Protocol 支持 |

## 快速开始

编译所有模块：

```bash
mvn clean compile
```

测试环境打包：

```bash
mvn clean package -Ptest -DskipTests
```

生产环境打包：

```bash
mvn clean package -Pprod -DskipTests
```

将 `docker/.env.example` 复制为 `docker/.env`，填写环境配置后启动服务：

```bash
cp docker/.env.example docker/.env
docker compose --env-file docker/.env -f docker/docker-compose.yml up -d
```

更新组件版本：

```bash
printf '请输入 Cloud 新版本号：'
IFS= read -r CLOUD_NEW_VERSION
./scripts/bump-version.sh "$CLOUD_NEW_VERSION"
```

## 🤖 Agent Skills(AI 辅助开发)

本仓库配套官方 **Agent Skill**,让 AI Agent(Claude Code · Codex · Cursor)基于真实代码、按需加载地理解本仓库。隶属 **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** 合集(一仓一 skill)。

使用 `-g` 可全局安装；去掉 `-g` 则仅安装到当前项目：

```bash
npx skills add mqttsnet/thinglinks-skills@thinglinks-cloud -g
```

涉及规则脚本、协议信封、设备上下行、物模型、ACL,或 `broker` / `mqs` / `rule` / `link` 模块时自动触发。全部 skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)见 **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)**。

## 联系我们

- 商业合作：[mqttsnet@163.com](mailto:mqttsnet@163.com)
- 问题反馈：[GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)
- 提交 PR：[GitHub Pull Requests](https://github.com/mqttsnet/thinglinks/pulls)

> **声明：** Bug 反馈、功能建议、技术讨论的唯一官方渠道为 [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)，其他平台提交的 Issue 不予处理。

## 开源协议

授权范围见仓库级 [LICENSE](../LICENSE)。

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

</div>
