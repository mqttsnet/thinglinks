<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="200"></a>

# ThingLinks IoT Platform

**Multi-tenant SaaS Cloud IoT Platform — High Performance · High Throughput · Highly Scalable**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025-green?style=flat-square&logo=spring)](https://spring.io/projects/spring-cloud)
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen?style=flat-square&logo=vuedotjs)](https://vuejs.org/)
[![TDengine](https://img.shields.io/badge/TDengine-3.0+-blue?style=flat-square)](https://tdengine.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/mqttsnet/thinglinks?style=flat-square)](https://github.com/mqttsnet/thinglinks/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/mqttsnet/thinglinks?style=flat-square)](https://github.com/mqttsnet/thinglinks/network/members)

<br>

[![Website](https://img.shields.io/badge/Website-mqttsnet.com-blue?style=for-the-badge)](https://mqttsnet.com)
[![GitHub](https://img.shields.io/badge/GitHub-mqttsnet/thinglinks-181717?style=for-the-badge&logo=github)](https://github.com/mqttsnet/thinglinks)
[![Docs](https://img.shields.io/badge/Docs-Documentation-green?style=for-the-badge)](https://mqttsnet.com)

</div>

---

## About

ThingLinks is an enterprise-grade **multi-tenant SaaS cloud IoT platform** built on Spring Cloud microservices architecture. It delivers **high-performance, high-throughput, and highly-scalable** device connectivity, supporting **millions of concurrent connections** on a single node with plugin-based extensibility and multi-protocol adaptation.

## Architecture

<details>
<summary><b>Architecture Diagram</b> (click to expand)</summary>
<br>
<a href="docs/images/architecture.png"><img src="docs/images/architecture.png" alt="ThingLinks Architecture" width="100%"></a>
</details>

## Core Features

| Feature | Description |
|---------|-------------|
| **Multi-tenant SaaS** | Enterprise-grade multi-tenant architecture with complete tenant isolation |
| **Million-level Connections** | Single node supports millions of concurrent device connections |
| **Multi-protocol** | MQTT, WebSocket, TCP, UDP, CoAP, HTTP, Modbus and more |
| **Device Management** | Unified product model, device lifecycle, device shadow, firmware OTA |
| **Rule Engine** | Chained rules, event orchestration, scene linkage (property/action/timer triggers) |
| **Alarm Center** | Multi-channel alarm notification, alarm records and tracking |
| **SCADA & Visualization** | Asset map, device geographic visualization, SCADA dashboards, large-screen displays |
| **Time-series Database** | TDengine — one table per device, one super-table per device type |
| **Plugin System** | Plugin-based development for custom protocol and feature extensions |
| **Message Bus** | Message governance: formatting, routing, filtering, queueing, security |
| **Protocol SDK** | Java-SDK, C-SDK, Python-SDK for protocol extensions |
| **IoT Card Management** | SIM card channel management, card lifecycle management |
| **Video Streaming** | Media server integration, video stream proxy |
| **AI Data Platform** | Big data platform, AI analytics, BI analysis, video center (Planned) |
| **Ecosystem** | Huawei IoT, Alibaba IoT, Apache BifroMQ integration |

## Tech Stack

![Java 17+](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Netty](https://img.shields.io/badge/Netty-4.x-0E83CD?style=flat-square)
![Vue 3](https://img.shields.io/badge/Vue.js-3.x-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)
![TDengine](https://img.shields.io/badge/TDengine-3.0+-0080FF?style=flat-square)
![Nacos](https://img.shields.io/badge/Nacos-3.x-1890FF?style=flat-square)
![Sentinel](https://img.shields.io/badge/Sentinel-1.x-00BFFF?style=flat-square)
![Seata](https://img.shields.io/badge/Seata-2.x-00CED1?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-Supported-231F20?style=flat-square&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Supported-2496ED?style=flat-square&logo=docker&logoColor=white)

## Quick Start

### Requirements

| Component | Version |
|-----------|---------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.x |
| TDengine | 3.x |
| Nacos | 3.x |

### Backend

```bash
# 1. Clone the repository
git clone https://github.com/mqttsnet/thinglinks.git

# 2. Import SQL scripts (see docs/sql/)

# 3. Configure Nacos, MySQL, Redis, TDengine connection info

# 4. Build
cd thinglinks/thinglinks-cloud
mvn clean install -DskipTests

# 5. Start services (gateway, oauth, link, etc.)
```

### Frontend

```bash
# Admin Console
cd thinglinks-web
pnpm install
pnpm run dev

# Visualization Dashboard
cd thinglinks-web-visualize
pnpm install
pnpm run dev
```

### Docker

```bash
# One-click deployment
docker compose -f thinglinks-cloud/docker/docker-compose.yml up -d
```

> For detailed deployment guide, visit [mqttsnet.com](https://mqttsnet.com).

## Project Structure

```
thinglinks/
├── thinglinks-cloud/                # Backend Microservices
│   ├── thinglinks-gateway/          # API Gateway
│   ├── thinglinks-oauth/            # Authentication & Authorization
│   ├── thinglinks-link/             # IoT Device Connectivity Core
│   ├── thinglinks-broker/           # MQTT Broker Integration (BifroMQ)
│   ├── thinglinks-rule/             # Rule Engine
│   ├── thinglinks-mqs/              # Message Queue Service
│   ├── thinglinks-card/             # IoT Card Management
│   ├── thinglinks-mobile/           # Mobile API
│   ├── thinglinks-support/          # Monitor & Admin Services
│   ├── thinglinks-sop-admin/        # DevOps Management
│   ├── thinglinks-generator/        # Code Generator
│   ├── thinglinks-openapi/          # Open API Service
│   ├── thinglinks-public/           # Public Service
│   ├── thinglinks-base/             # Base Platform Service
│   ├── thinglinks-sdk/              # SDK
│   └── docker/                      # Cloud Docker Compose Deployment
├── thinglinks-web/                  # Admin Console (Vue 3 + Vben)
├── thinglinks-web-visualize/        # Visualization Dashboard (Vue 3 + ECharts)
├── thinglinks-job/                  # Scheduled Task Service (XXL-JOB)
├── bifromq-plugin/                  # Apache BifroMQ Plugin
├── docs/                            # Documentation & Screenshots
└── scripts/                         # Build & Utility Scripts
```

## Documentation

For complete documentation including quick start guides, development guides, API references, and deployment instructions, visit the official website:

[![Docs](https://img.shields.io/badge/Documentation-mqttsnet.com-blue?style=for-the-badge)](https://mqttsnet.com)

## 🤖 Agent Skills (AI-assisted development)

Official **Agent Skills** document this project for AI agents (Claude Code · Codex · Cursor) — answers grounded in real code, loaded on demand. One skill per sub-project, from the **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** collection:

```bash
# global (-g); drop -g to install into the current project only
npx skills add mqttsnet/thinglinks-skills@thinglinks-cloud -g    # backend microservices
npx skills add mqttsnet/thinglinks-skills@thinglinks-web -g      # admin console (Vue3)
npx skills add mqttsnet/thinglinks-skills@bifromq-plugin -g      # BifroMQ broker plugins
```

Auto-triggers when you work on rule scripts, the protocol envelope, uplink/downlink, thing-model, ACL, console pages, or BifroMQ auth/event plugins. Browse all skills (incl. `thinglinks-util`) at **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)**.

## Screenshots

<details>
<summary><b>Basic Platform</b> (4 screenshots)</summary>
<br>
<p>
  <a href="docs/images/pc/login.png"><img src="docs/images/pc/login.png" width="270" alt="Login"></a>
  <a href="docs/images/pc/basic/myApplication.png"><img src="docs/images/pc/basic/myApplication.png" width="270" alt="My Application"></a>
  <a href="docs/images/pc/basic/openAccessKey.png"><img src="docs/images/pc/basic/openAccessKey.png" width="270" alt="Access Key"></a>
</p>
<p>
  <a href="docs/images/pc/basic/openApi.png"><img src="docs/images/pc/basic/openApi.png" width="270" alt="Open API"></a>
</p>
</details>

<details>
<summary><b>DevOps System</b> (7 screenshots)</summary>
<br>
<p>
  <a href="docs/images/pc/devOperation/tenant.png"><img src="docs/images/pc/devOperation/tenant.png" width="270" alt="Tenant Management"></a>
  <a href="docs/images/pc/devOperation/project.png"><img src="docs/images/pc/devOperation/project.png" width="270" alt="Project Management"></a>
  <a href="docs/images/pc/devOperation/application.png"><img src="docs/images/pc/devOperation/application.png" width="270" alt="Application Management"></a>
</p>
<p>
  <a href="docs/images/pc/devOperation/resource.png"><img src="docs/images/pc/devOperation/resource.png" width="270" alt="Resource Management"></a>
  <a href="docs/images/pc/devOperation/generator.png"><img src="docs/images/pc/devOperation/generator.png" width="270" alt="Code Generator"></a>
  <a href="docs/images/pc/devOperation/opsInterface.png"><img src="docs/images/pc/devOperation/opsInterface.png" width="270" alt="Ops Interface"></a>
</p>
<p>
  <a href="docs/images/pc/devOperation/sopIsvInfo.png"><img src="docs/images/pc/devOperation/sopIsvInfo.png" width="270" alt="ISV Info"></a>
</p>
</details>

<details>
<summary><b>IoT System</b> (15 screenshots)</summary>
<br>
<p>
  <a href="docs/images/pc/iotSystem/product.png"><img src="docs/images/pc/iotSystem/product.png" width="270" alt="Product Management"></a>
  <a href="docs/images/pc/iotSystem/productDetails.png"><img src="docs/images/pc/iotSystem/productDetails.png" width="270" alt="Product Details"></a>
  <a href="docs/images/pc/iotSystem/productService.png"><img src="docs/images/pc/iotSystem/productService.png" width="270" alt="Product Service"></a>
</p>
<p>
  <a href="docs/images/pc/iotSystem/device.png"><img src="docs/images/pc/iotSystem/device.png" width="270" alt="Device Management"></a>
  <a href="docs/images/pc/iotSystem/deviceDebug.png"><img src="docs/images/pc/iotSystem/deviceDebug.png" width="270" alt="Device Debug"></a>
  <a href="docs/images/pc/iotSystem/deviceShadow.png"><img src="docs/images/pc/iotSystem/deviceShadow.png" width="270" alt="Device Shadow"></a>
</p>
<p>
  <a href="docs/images/pc/iotSystem/deviceShadow_1.png"><img src="docs/images/pc/iotSystem/deviceShadow_1.png" width="270" alt="Device Shadow Detail"></a>
  <a href="docs/images/pc/iotSystem/assetStats.png"><img src="docs/images/pc/iotSystem/assetStats.png" width="270" alt="Asset Statistics"></a>
  <a href="docs/images/pc/iotSystem/assetmap.png"><img src="docs/images/pc/iotSystem/assetmap.png" width="270" alt="Asset Map"></a>
</p>
<p>
  <a href="docs/images/pc/iotSystem/pluginInfo.png"><img src="docs/images/pc/iotSystem/pluginInfo.png" width="270" alt="Plugin Info"></a>
  <a href="docs/images/pc/iotSystem/pluginInstance.png"><img src="docs/images/pc/iotSystem/pluginInstance.png" width="270" alt="Plugin Instance"></a>
  <a href="docs/images/pc/iotSystem/engineChained.png"><img src="docs/images/pc/iotSystem/engineChained.png" width="270" alt="Chained Rule Engine"></a>
</p>
<p>
  <a href="docs/images/pc/iotSystem/engineLinkage.png"><img src="docs/images/pc/iotSystem/engineLinkage.png" width="270" alt="Device Linkage"></a>
  <a href="docs/images/pc/iotSystem/ruleGroovyScript.png"><img src="docs/images/pc/iotSystem/ruleGroovyScript.png" width="270" alt="Groovy Script Rule"></a>
  <a href="docs/images/pc/iotSystem/scada.png"><img src="docs/images/pc/iotSystem/scada.png" width="270" alt="SCADA"></a>
</p>
</details>

<details>
<summary><b>IoT Card System</b> (2 screenshots)</summary>
<br>
<p>
  <a href="docs/images/pc/iotCard/cardChannelInfo.png"><img src="docs/images/pc/iotCard/cardChannelInfo.png" width="270" alt="Card Channel Info"></a>
  <a href="docs/images/pc/iotCard/cardSimInfo.png"><img src="docs/images/pc/iotCard/cardSimInfo.png" width="270" alt="SIM Card Info"></a>
</p>
</details>

<details>
<summary><b>Visualization Dashboard</b> (1 screenshot)</summary>
<br>
<p>
  <a href="docs/images/pc/view/visualization.png"><img src="docs/images/pc/view/visualization.png" width="540" alt="Visualization Dashboard"></a>
</p>
</details>

<details>
<summary><b>Video Streaming System</b> (2 screenshots)</summary>
<br>
<p>
  <a href="docs/images/pc/videoSystem/videoMediaServer.png"><img src="docs/images/pc/videoSystem/videoMediaServer.png" width="270" alt="Media Server"></a>
  <a href="docs/images/pc/videoSystem/videoStreamProxy.png"><img src="docs/images/pc/videoSystem/videoStreamProxy.png" width="270" alt="Stream Proxy"></a>
</p>
</details>

<details>
<summary><b>Mobile H5</b> (5 screenshots)</summary>
<br>
<p>
  <a href="docs/images/h5/login.jpg"><img src="docs/images/h5/login.jpg" width="160" alt="Login"></a>
  <a href="docs/images/h5/index.jpg"><img src="docs/images/h5/index.jpg" width="160" alt="Home"></a>
  <a href="docs/images/h5/dashboard.jpg"><img src="docs/images/h5/dashboard.jpg" width="160" alt="Dashboard"></a>
  <a href="docs/images/h5/myHome.jpg"><img src="docs/images/h5/myHome.jpg" width="160" alt="My Home"></a>
  <a href="docs/images/h5/scene.jpg"><img src="docs/images/h5/scene.jpg" width="160" alt="Scene"></a>
</p>
</details>

## Edition Comparison

| Feature | Community | Commercial | Enterprise |
|---------|:---------:|:----------:|:----------:|
| Business Layer Source Code | ✔ Full (GitHub/Gitee) | ✔ 100% Complete | ✔ Pro 100% Full |
| ThingLinks-util Core Library | ✕ JAR only | ✕ JAR only | ✔ Full source |
| Technical Documentation | Community docs | Community docs | Full technical + architecture docs |
| Private Repository Access | ✕ | ✔ | ✔ |
| Modify package name | ✕ Prohibited | ✔ Allowed | ✔ Unrestricted |
| Modify Maven groupId | ✕ Prohibited | ✔ Allowed | ✔ Unrestricted |
| Modify author info | ✕ Prohibited | ⚠ Allowed (keep copyright) | ✔ Unrestricted |
| Modify copyright info | ✕ Prohibited | ✕ Must retain | ✔ Unrestricted |

> **Community Edition Notice:** Per the Apache 2.0 License and ThingLinks Commercial License, modification or removal of package names, Maven groupId, author attributions, and copyright notices in community edition source code is prohibited. Upgrade to Commercial or Enterprise edition to modify these identifiers.

> **Commercial / Enterprise License Activation:** After purchasing, fill in the provided Authorization ID in the [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL) file and commit via git. The git commit history serves as proof of authorized activation. Verify your license at [mqttsnet.com](https://mqttsnet.com).

## Roadmap

See [GitHub Milestones](https://github.com/mqttsnet/thinglinks/milestones) for our planned features and upcoming releases.

## Star History

<a href="https://star-history.com/#mqttsnet/thinglinks&Date">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=mqttsnet/thinglinks&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=mqttsnet/thinglinks&type=Date" />
    <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=mqttsnet/thinglinks&type=Date" />
  </picture>
</a>

## Contributors

Thanks to all the wonderful people who contribute to this project!

<a href="https://github.com/mqttsnet/thinglinks/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=mqttsnet/thinglinks&max=100&columns=12" alt="Contributors" />
</a>

Want to contribute? Check out the [Contributor Guide](CONTRIBUTING.md).

## Contact

- Business Cooperation: [mqttsnet@163.com](mailto:mqttsnet@163.com)
- Issues: [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)
- Pull Requests: [GitHub PRs](https://github.com/mqttsnet/thinglinks/pulls)

> **Note:** This project is mirrored to multiple code hosting platforms. The **only official channel** for bug reports, feature requests, and discussions is [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues). Issues submitted on other platforms (Gitee, Gitea, etc.) will not be monitored or addressed.

<table>
  <tr>
    <td align="center">
      <img src="docs/images/wechat-mp.png" width="200" alt="WeChat: MqttsNet"><br>
      <sub>WeChat: MqttsNet</sub>
    </td>
  </tr>
</table>

## Acknowledgments

- [Apache BifroMQ](https://github.com/apache/bifromq) — High-performance MQTT broker

## License

ThingLinks Community Edition is licensed under the [Apache License 2.0](LICENSE) with additional commercial terms — see [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL) for details.

For commercial or enterprise licensing, contact [mqttsnet@163.com](mailto:mqttsnet@163.com).

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

[Thanks to JetBrains for providing free IDE licenses](https://www.jetbrains.com)

</div>
