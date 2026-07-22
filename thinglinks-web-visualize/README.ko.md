<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# thinglinks-web-visualize

**ThingLinks IoT 플랫폼 — 시각화 대시보드**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | 한국어

</div>

---

## 소개

`thinglinks-web-visualize`는 [ThingLinks](https://github.com/mqttsnet/thinglinks) 멀티테넌트 SaaS 클라우드 IoT 플랫폼의 데이터 시각화 및 대형 스크린 대시보드 프론트엔드입니다. Vue 3 + TypeScript + Vite로 구축되었으며, 다양한 차트 유형, 3D 시각화, 지도 디스플레이를 지원합니다.

제품 정보, 에디션 및 컴포넌트 버전은 [`.thinglinks-product.env`](.thinglinks-product.env)에서 통합 관리합니다. 업데이트와 검증 명령은 [제품 설정 빠른 작업](docs/产品配置快捷操作.md)을 참조하세요.

## 기술 스택

![Vue 3](https://img.shields.io/badge/Vue-3.2-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-4.6-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-4.3-646CFF?style=flat-square&logo=vite&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-2.0-FFD859?style=flat-square)
![Naive UI](https://img.shields.io/badge/Naive%20UI-2.34-18A058?style=flat-square)
![ECharts](https://img.shields.io/badge/ECharts-5.3-AA344D?style=flat-square&logo=apacheecharts&logoColor=white)
![Three.js](https://img.shields.io/badge/Three.js-0.145-000000?style=flat-square&logo=threedotjs&logoColor=white)

## 주요 기능

- ECharts 기반 풍부한 차트 컴포넌트
- 가오더 지도 시각화 (히트맵, 산점도)
- Three.js 기반 3D 씬 렌더링
- 드래그 앤 드롭 대시보드 에디터
- 반응형 레이아웃 (멀티 해상도 지원)
- 테마 커스터마이징 및 국제화 (i18n)

## 빠른 시작

### 환경 요구사항

- **Node.js** >= 18.0.0
- **pnpm** (설치: `npm install -g pnpm`)

### 설치 및 실행

```bash
# 의존성 설치
pnpm install

# 개발 서버 시작
pnpm dev

# 프로덕션 빌드
pnpm build
```

## 라이선스

[Apache License 2.0](LICENSE)에 따라 배포됩니다.

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
