<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="200"></a>

# ThingLinks IoT 플랫폼

**멀티테넌트 SaaS 클라우드 IoT 플랫폼 — 고성능 · 고처리량 · 고확장성**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | 한국어

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

## 플랫폼 소개

ThingLinks는 Spring Cloud 마이크로서비스 아키텍처 기반의 엔터프라이즈급 **멀티테넌트 SaaS 클라우드 IoT 플랫폼**입니다. **고성능, 고처리량, 고확장성**의 디바이스 연결 기능을 제공하며, 단일 노드에서 **백만 레벨의 동시 접속**을 지원합니다. 플러그인 기반 확장 개발과 다중 프로토콜 적응이 가능합니다.

## 시스템 아키텍처

<details>
<summary><b>아키텍처 다이어그램</b> (클릭하여 확장)</summary>
<br>
<a href="docs/images/architecture.png"><img src="docs/images/architecture.png" alt="ThingLinks Architecture" width="100%"></a>
</details>

## 핵심 기능

| 기능 | 설명 |
|------|------|
| **멀티테넌트 SaaS** | 엔터프라이즈급 멀티테넌트 아키텍처, 완전한 테넌트 격리 |
| **백만 레벨 연결** | 단일 노드에서 백만 레벨 디바이스 동시 연결 지원 |
| **다중 프로토콜** | MQTT, WebSocket, TCP, UDP, CoAP, HTTP, Modbus 등 지원 |
| **디바이스 관리** | 통합 제품 모델, 디바이스 라이프사이클 관리, 디바이스 섀도, 펌웨어 OTA |
| **룰 엔진** | 체인 룰, 이벤트 오케스트레이션, 씬 연동 (속성/액션/타이머 트리거) |
| **알람 센터** | 다중 채널 알람 알림, 알람 기록 및 추적 |
| **SCADA & 시각화** | 자산 지도, 디바이스 지리적 위치 시각화, SCADA 대시보드, 대형 스크린 디스플레이 |
| **시계열 데이터베이스** | TDengine — 디바이스당 1테이블, 디바이스 유형당 1슈퍼테이블 |
| **플러그인 시스템** | 플러그인 기반 개발, 커스텀 프로토콜 및 기능 확장 지원 |
| **메시지 버스** | 메시지 거버넌스: 포맷팅, 라우팅, 필터링, 큐잉, 보안 |
| **프로토콜 SDK** | Java-SDK, C-SDK, Python-SDK를 통한 프로토콜 확장 |
| **IoT 카드 관리** | SIM 카드 채널 관리, 카드 라이프사이클 관리 |
| **비디오 스트리밍** | 미디어 서버 통합, 비디오 스트림 프록시 |
| **AI 데이터 플랫폼** | 빅데이터 플랫폼, AI 분석, BI 분석, 비디오 센터 (계획 중) |
| **에코시스템** | Huawei IoT, Alibaba IoT, Apache BifroMQ 통합 |

## 기술 스택

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

## 빠른 시작

### 필수 조건

| 컴포넌트 | 버전 |
|----------|------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.x |
| TDengine | 3.x |
| Nacos | 3.x |

### 백엔드

```bash
# 1. 리포지토리 클론
git clone https://github.com/mqttsnet/thinglinks.git

# 2. SQL 스크립트 임포트 (docs/sql/ 참조)

# 3. Nacos, MySQL, Redis, TDengine 연결 정보 설정

# 4. 빌드
cd thinglinks/thinglinks-cloud
mvn clean install -DskipTests

# 5. 서비스 시작 (gateway, oauth, link 등)
```

### 프론트엔드

```bash
# 관리 콘솔
cd thinglinks-web
pnpm install
pnpm run dev

# 시각화 대시보드
cd thinglinks-web-visualize
pnpm install
pnpm run dev
```

### Docker

```bash
# 원클릭 배포
docker-compose up -d
```

> 상세 배포 가이드는 [mqttsnet.com](https://mqttsnet.com)을 방문하세요.

## 프로젝트 구조

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
│   └── thinglinks-sdk/              # SDK
├── thinglinks-web/                  # Admin Console (Vue 3 + Vben)
├── thinglinks-web-visualize/        # Visualization Dashboard (Vue 3 + ECharts)
├── thinglinks-job/                  # Scheduled Task Service (XXL-JOB)
├── bifromq-plugin/                  # Apache BifroMQ Plugin
├── docker/                          # Docker Compose Deployment
├── docs/                            # Documentation & Screenshots
└── scripts/                         # Build & Utility Scripts
```

## 문서

빠른 시작 가이드, 개발 가이드, API 레퍼런스, 배포 지침을 포함한 전체 문서는 공식 웹사이트를 방문하세요:

[![Docs](https://img.shields.io/badge/Documentation-mqttsnet.com-blue?style=for-the-badge)](https://mqttsnet.com)

## 🤖 Agent Skills(AI 지원 개발)

공식 **Agent Skills**는 본 프로젝트의 실제 코드 기반 지식을 AI 에이전트(Claude Code · Codex · Cursor)가 온디맨드로 로드하는 스킬로 제공합니다. 서브 프로젝트별 1개 스킬, **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** 컬렉션:

```bash
# global (-g); drop -g to install into the current project only
npx skills add mqttsnet/thinglinks-skills@thinglinks-cloud -g    # backend microservices
npx skills add mqttsnet/thinglinks-skills@thinglinks-web -g      # admin console (Vue3)
npx skills add mqttsnet/thinglinks-skills@bifromq-plugin -g      # BifroMQ broker plugins
```

룰 스크립트, 프로토콜 envelope, 업/다운링크, 사물모델, ACL, 콘솔 페이지, BifroMQ 인증/이벤트 플러그인 작업 시 자동 트리거됩니다. 전체 스킬(`thinglinks-util` 포함)은 **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)** 참조。

## 스크린샷

<details>
<summary><b>기본 플랫폼</b> (4장)</summary>
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
<summary><b>DevOps 시스템</b> (7장)</summary>
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
<summary><b>IoT 시스템</b> (15장)</summary>
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
<summary><b>IoT 카드 시스템</b> (2장)</summary>
<br>
<p>
  <a href="docs/images/pc/iotCard/cardChannelInfo.png"><img src="docs/images/pc/iotCard/cardChannelInfo.png" width="270" alt="Card Channel Info"></a>
  <a href="docs/images/pc/iotCard/cardSimInfo.png"><img src="docs/images/pc/iotCard/cardSimInfo.png" width="270" alt="SIM Card Info"></a>
</p>
</details>

<details>
<summary><b>시각화 대시보드</b> (1장)</summary>
<br>
<p>
  <a href="docs/images/pc/view/visualization.png"><img src="docs/images/pc/view/visualization.png" width="540" alt="Visualization Dashboard"></a>
</p>
</details>

<details>
<summary><b>비디오 스트리밍 시스템</b> (2장)</summary>
<br>
<p>
  <a href="docs/images/pc/videoSystem/videoMediaServer.png"><img src="docs/images/pc/videoSystem/videoMediaServer.png" width="270" alt="Media Server"></a>
  <a href="docs/images/pc/videoSystem/videoStreamProxy.png"><img src="docs/images/pc/videoSystem/videoStreamProxy.png" width="270" alt="Stream Proxy"></a>
</p>
</details>

<details>
<summary><b>모바일 H5</b> (5장)</summary>
<br>
<p>
  <a href="docs/images/h5/login.jpg"><img src="docs/images/h5/login.jpg" width="160" alt="Login"></a>
  <a href="docs/images/h5/index.jpg"><img src="docs/images/h5/index.jpg" width="160" alt="Home"></a>
  <a href="docs/images/h5/dashboard.jpg"><img src="docs/images/h5/dashboard.jpg" width="160" alt="Dashboard"></a>
  <a href="docs/images/h5/myHome.jpg"><img src="docs/images/h5/myHome.jpg" width="160" alt="My Home"></a>
  <a href="docs/images/h5/scene.jpg"><img src="docs/images/h5/scene.jpg" width="160" alt="Scene"></a>
</p>
</details>

## 에디션 비교

| 기능 | 커뮤니티 | 상용 | 엔터프라이즈 |
|------|:-------:|:----:|:-----------:|
| 비즈니스 레이어 소스코드 | ✔ 전체 (GitHub/Gitee 공개) | ✔ 100% 완전 | ✔ Pro 버전 100% 전체 |
| ThingLinks-util 코어 라이브러리 | ✕ JAR 참조 | ✕ JAR 참조 | ✔ 전체 소스코드 |
| 기술 문서 | 커뮤니티 문서 | 커뮤니티 문서 | 전체 기술 + 아키텍처 문서 |
| 프라이빗 리포지토리 접근 | ✕ | ✔ | ✔ |
| 패키지명 변경 | ✕ 금지 | ✔ 허용 | ✔ 제한 없음 |
| Maven groupId 변경 | ✕ 금지 | ✔ 허용 | ✔ 제한 없음 |
| 작성자 정보 변경 | ✕ 금지 | ⚠ 가능 (저작권 유지) | ✔ 제한 없음 |
| 저작권 정보 변경 | ✕ 금지 | ✕ 유지 필수 | ✔ 제한 없음 |

> **커뮤니티 에디션 사용자 주의:** Apache 2.0 라이선스 및 ThingLinks 상용 라이선스에 따라 커뮤니티 에디션 소스코드의 패키지명, Maven groupId, 저작자 표시, 저작권 표시의 수정 또는 제거는 금지됩니다. 이러한 식별자를 수정하려면 상용 또는 엔터프라이즈 에디션으로 업그레이드하세요.

> **상용 / 엔터프라이즈 라이선스 활성화:** 구매 후 제공된 라이선스 ID를 [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL) 파일에 입력하고 git commit으로 기록하세요. git 커밋 이력이 라이선스 인증의 핵심 증거가 됩니다. [mqttsnet.com](https://mqttsnet.com)에서 인증 상태를 확인할 수 있습니다.

## 로드맵

계획된 기능 및 향후 릴리스에 대해서는 [GitHub Milestones](https://github.com/mqttsnet/thinglinks/milestones)를 확인하세요.

## Star 히스토리

<a href="https://star-history.com/#mqttsnet/thinglinks&Date">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=mqttsnet/thinglinks&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=mqttsnet/thinglinks&type=Date" />
    <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=mqttsnet/thinglinks&type=Date" />
  </picture>
</a>

## 기여자

이 프로젝트에 기여해주신 모든 분들께 감사드립니다!

<a href="https://github.com/mqttsnet/thinglinks/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=mqttsnet/thinglinks&max=100&columns=12" alt="Contributors" />
</a>

기여에 참여하고 싶으시다면 [기여자 가이드](CONTRIBUTING.md)를 확인해주세요.

## 연락처

- 비즈니스 협력: [mqttsnet@163.com](mailto:mqttsnet@163.com)
- 이슈 보고: [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)
- 풀 리퀘스트: [GitHub PRs](https://github.com/mqttsnet/thinglinks/pulls)

> **안내:** 본 프로젝트는 여러 코드 호스팅 플랫폼에 미러링됩니다. 버그 보고, 기능 요청, 기술 토론의 **유일한 공식 채널**은 [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)입니다. 다른 플랫폼(Gitee, Gitea 등)에서 제출된 이슈는 처리되지 않습니다.

<table>
  <tr>
    <td align="center">
      <img src="docs/images/wechat-mp.png" width="200" alt="WeChat: MqttsNet"><br>
      <sub>WeChat: MqttsNet</sub>
    </td>
  </tr>
</table>

## 감사의 글

- [Apache BifroMQ](https://github.com/apache/bifromq) — 고성능 MQTT Broker

## 라이선스

ThingLinks 커뮤니티 에디션은 [Apache License 2.0](LICENSE)에 따라 오픈소스로 제공됩니다. 추가 상용 조건은 [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL)을 참조하십시오.

상용 / 엔터프라이즈 라이선스는 [mqttsnet@163.com](mailto:mqttsnet@163.com)으로 문의하십시오.

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

[무료 IDE 라이선스를 제공해주신 JetBrains에 감사드립니다](https://www.jetbrains.com)

</div>
