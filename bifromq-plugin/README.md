<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# bifromq-plugin — Enterprise Edition

**ThingLinks IoT Platform — BifroMQ MQTT Broker Plugin Library**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![BifroMQ](https://img.shields.io/badge/BifroMQ-3.3.5-blue?style=flat-square)](https://github.com/baidu/bifromq)
[![Edition](https://img.shields.io/badge/Edition-Enterprise-gold?style=flat-square)](LICENSE-COMMERCIAL)

</div>

---

## About

`bifromq-plugin` is the BifroMQ MQTT broker plugin library for the [ThingLinks](https://mqttsnet.com) IoT platform (Enterprise Edition). It enables runtime integration of custom business logic with the ThingLinks IoT system through BifroMQ's plugin mechanism.

## Plugins

| Plugin | Description |
|--------|-------------|
| `bifromq-auth-provider-plugin` | MQTT client authentication/authorization (username/password, certificate, ACL) |
| `bifromq-event-collector-plugin` | BifroMQ event collection, filtering, and performance monitoring |
| `bifromq-resource-throttler-plugin` | Multi-tenant resource allocation and rate limiting |
| `bifromq-setting-provider-plugin` | Dynamic tenant-level settings configuration at runtime |

## Build

```bash
# Build all plugins
mvn clean package -DskipTests

# Build single plugin
mvn clean package -pl bifromq-auth-provider-plugin -am -DskipTests
```

## Requirements

- JDK 17+
- Maven 3.6+
- BifroMQ 3.3.5

## 🤖 Agent Skills (AI-assisted development)

An official **Agent Skill** documents these BifroMQ plugins for AI agents (Claude Code · Codex · Cursor) — answers grounded in real code, loaded on demand. It's part of the **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** collection (one skill per repo).

```bash
# global (-g); drop -g to install into the current project only
npx skills add mqttsnet/thinglinks-skills@bifromq-plugin -g
```

Auto-triggers when you work on BifroMQ auth/ACL, the event collector (Kafka), setting/resource providers, the `EventTypeEnum ↔ DeviceActionTypeEnum` mapping, or plugin deployment. Browse all skills — `thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin` — at **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)**.

## License

Licensed under [Apache License 2.0](LICENSE) with additional terms — see [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL).

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

</div>
