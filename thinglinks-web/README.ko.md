<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="160"></a>

# thinglinks-web — Enterprise Edition

**ThingLinks IoT — 관리 콘솔 (Enterprise)**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | 한국어

[![Edition](https://img.shields.io/badge/Edition-Enterprise-gold?style=flat-square)](LICENSE-COMMERCIAL)

</div>

---

## 소개

`thinglinks-web`은 [ThingLinks](https://mqttsnet.com) 멀티테넌트 SaaS 클라우드 IoT 플랫폼의 관리 콘솔 (Enterprise Edition)입니다.

### 라이선스 활성화

구매 후 라이선스 ID를 [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL)에 입력하고 git commit으로 기록하세요.

## 🤖 Agent Skills(AI 지원 개발)

이 저장소에는 공식 **Agent Skill** 이 제공되어, AI 에이전트(Claude Code · Codex · Cursor)가 실제 코드를 기반으로 필요한 만큼만 로드하여 이해할 수 있습니다. **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** 컬렉션의 일부입니다(저장소 1개 = skill 1개).

```bash
# 전역(-g); -g 를 빼면 현재 프로젝트에만 설치
npx skills add mqttsnet/thinglinks-skills@thinglinks-web -g
```

IoT 페이지, API 호출, 공유 컴포넌트 / 룰 스크립트 디버그 패널 또는 파일 배치와 **Flexy** 디자인 규약 작업 시 자동으로 트리거됩니다. 모든 skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)은 **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)** 참조.

## 라이선스

[Apache License 2.0](LICENSE) + [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL)

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
