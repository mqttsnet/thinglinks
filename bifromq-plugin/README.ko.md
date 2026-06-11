<div align="center">

# bifromq-plugin — Enterprise Edition

**ThingLinks IoT — BifroMQ 플러그인 라이브러리**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | 한국어

[![Edition](https://img.shields.io/badge/Edition-Enterprise-gold?style=flat-square)](LICENSE-COMMERCIAL)

</div>

---

BifroMQ MQTT Broker의 [ThingLinks](https://mqttsnet.com) IoT 플랫폼 플러그인 라이브러리. 인증, 이벤트 수집, 리소스 제어, 동적 설정 4개 플러그인.

## 🤖 Agent Skills(AI 지원 개발)

이 저장소에는 공식 **Agent Skill** 이 제공되어, AI 에이전트(Claude Code · Codex · Cursor)가 실제 코드를 기반으로 필요한 만큼만 로드하여 이해할 수 있습니다. **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** 컬렉션의 일부입니다(저장소 1개 = skill 1개).

```bash
# 전역(-g); -g 를 빼면 현재 프로젝트에만 설치
npx skills add mqttsnet/thinglinks-skills@bifromq-plugin -g
```

BifroMQ 인증/ACL, 이벤트 컬렉터(Kafka), 설정/리소스 provider, `EventTypeEnum ↔ DeviceActionTypeEnum` 매핑 또는 플러그인 배포 작업 시 자동으로 트리거됩니다. 모든 skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)은 **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)** 참조.

## 라이선스

[Apache License 2.0](LICENSE) + [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL)

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
