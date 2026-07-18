<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="200"></a>

# ThingLinks Cloud

**멀티테넌트 SaaS 클라우드 IoT 플랫폼 — 고성능 · 고처리량 · 고확장성**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | 한국어

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025-green?style=flat-square&logo=spring)](https://spring.io/projects/spring-cloud)
<br>

[![Website](https://img.shields.io/badge/공식사이트-mqttsnet.com-blue?style=for-the-badge)](https://mqttsnet.com)
[![Docs](https://img.shields.io/badge/문서-온라인문서-green?style=for-the-badge)](https://mqttsnet.yuque.com/trgbro/thinglinks)

</div>

---

## 소개

ThingLinks Cloud는 [ThingLinks](https://mqttsnet.com) 멀티테넌트 SaaS 클라우드 IoT 플랫폼의 백엔드입니다. Spring Cloud 마이크로서비스로 구축되어 **고성능, 고처리량, 고확장성**의 디바이스 연결을 제공합니다.

제품 에디션과 컴포넌트 버전 메타데이터는 루트의 [`.thinglinks-product.env`](.thinglinks-product.env)에 있습니다.

## 🤖 Agent Skills(AI 지원 개발)

이 저장소에는 공식 **Agent Skill** 이 제공되어, AI 에이전트(Claude Code · Codex · Cursor)가 실제 코드를 기반으로 필요한 만큼만 로드하여 이해할 수 있습니다. **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** 컬렉션의 일부입니다(저장소 1개 = skill 1개).

`-g`를 사용하면 전역으로 설치되며, 빼면 현재 프로젝트에만 설치됩니다.

```bash
npx skills add mqttsnet/thinglinks-skills@thinglinks-cloud -g
```

룰 스크립트, 프로토콜 봉투, 업/다운링크, 사물 모델, ACL 또는 `broker` / `mqs` / `rule` / `link` 모듈 작업 시 자동으로 트리거됩니다. 모든 skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)은 **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)** 참조.

## 문의

- 비즈니스: [mqttsnet@163.com](mailto:mqttsnet@163.com)
- Issues: [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)

> **안내:** Issue는 [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)에서만 접수합니다.

## 라이선스

저장소 루트의 [LICENSE](../LICENSE)를 확인하세요.

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

</div>
