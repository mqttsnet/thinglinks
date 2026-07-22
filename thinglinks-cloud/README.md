<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="200"></a>

# ThingLinks Cloud

**Multi-tenant SaaS Cloud IoT Platform — High Performance · High Throughput · Highly Scalable**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025-green?style=flat-square&logo=spring)](https://spring.io/projects/spring-cloud)
[![TDengine](https://img.shields.io/badge/TDengine-3.0+-blue?style=flat-square)](https://tdengine.com/)
<br>

[![Website](https://img.shields.io/badge/Website-mqttsnet.com-blue?style=for-the-badge)](https://mqttsnet.com)
[![Docs](https://img.shields.io/badge/Docs-Documentation-green?style=for-the-badge)](https://mqttsnet.yuque.com/trgbro/thinglinks)

</div>

---

## About

ThingLinks Cloud is the backend of the [ThingLinks](https://mqttsnet.com) multi-tenant SaaS cloud IoT platform. Built on Spring Cloud microservices, it delivers **high-performance, high-throughput, and highly-scalable** device connectivity with **millions of concurrent connections** per node.

Product edition and component version metadata are available in [`.thinglinks-product.env`](.thinglinks-product.env).

## Core Features

| Feature | Description |
|---------|-------------|
| **Multi-tenant SaaS** | Enterprise-grade multi-tenant architecture with complete tenant isolation |
| **Million-level Connections** | Single node supports millions of concurrent device connections |
| **Multi-protocol** | MQTT, WebSocket, TCP, UDP, CoAP, HTTP and more |
| **Device Management** | Unified product model, device lifecycle, sub-device management |
| **Rule Engine** | Flexible rules for alarm, notification, data forwarding |
| **SCADA & Visualization** | Device geographic visualization, SCADA dashboards, large-screen displays |
| **Time-series Database** | TDengine — one table per device, one super-table per device type |
| **Plugin System** | Plugin-based development for custom protocol and feature extensions |
| **IoT Card Management** | SIM card channel management, card lifecycle management |
| **Video Streaming** | Media server integration, video stream proxy |
| **MCP Integration** | Model Context Protocol support |

## Tech Stack

![Java 17+](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Netty](https://img.shields.io/badge/Netty-4.x-0E83CD?style=flat-square)
![TDengine](https://img.shields.io/badge/TDengine-3.0+-0080FF?style=flat-square)
![Nacos](https://img.shields.io/badge/Nacos-3.x-1890FF?style=flat-square)
![Seata](https://img.shields.io/badge/Seata-2.x-00CED1?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Supported-2496ED?style=flat-square&logo=docker&logoColor=white)

## Quick Start

Build all modules:

```bash
mvn clean compile
```

Build for the test environment:

```bash
mvn clean package -Ptest -DskipTests
```

Build for production:

```bash
mvn clean package -Pprod -DskipTests
```

Copy `docker/.env.example` to `docker/.env`, fill in the environment values, and start the services:

```bash
cp docker/.env.example docker/.env
docker compose --env-file docker/.env -f docker/docker-compose.yml up -d
```

Update the component version:

```bash
printf 'Enter the new Cloud version: '
IFS= read -r CLOUD_NEW_VERSION
./scripts/bump-version.sh "$CLOUD_NEW_VERSION"
```

## 🤖 Agent Skills (AI-assisted development)

An official **Agent Skill** documents this repo for AI agents (Claude Code · Codex · Cursor) — answers grounded in real code, loaded on demand. It's part of the **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** collection (one skill per repo).

Install it globally with `-g`; remove `-g` to install it only for the current project:

```bash
npx skills add mqttsnet/thinglinks-skills@thinglinks-cloud -g
```

Auto-triggers when you work on rule scripts, the protocol envelope, uplink/downlink, thing-model, ACL, or the `broker` / `mqs` / `rule` / `link` modules. Browse all skills — `thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin` — at **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)**.

## Contact

- Business Cooperation: [mqttsnet@163.com](mailto:mqttsnet@163.com)
- Issues: [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)
- Pull Requests: [GitHub PRs](https://github.com/mqttsnet/thinglinks/pulls)

> **Note:** Bug reports, feature requests, and discussions should be submitted via [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues). Issues on other platforms will not be addressed.

## License

See the repository-level [LICENSE](../LICENSE).

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

</div>
