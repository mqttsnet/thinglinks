<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="160"></a>

# thinglinks-web — 旗舰版

**ThingLinks 物联网平台 — 管理控制台（旗舰版）**

[English](README.md) | 简体中文 | [日本語](README.ja.md) | [한국어](README.ko.md)

[![Vue 3](https://img.shields.io/badge/Vue-3.3-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-4.6-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-4.3-646CFF?style=flat-square&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Edition](https://img.shields.io/badge/版本-旗舰版-gold?style=flat-square)](LICENSE-COMMERCIAL)

</div>

---

## 简介

`thinglinks-web` 是 [ThingLinks](https://mqttsnet.com) 多租户 SaaS 云物联网平台的管理控制台前端（旗舰版），基于 Vue 3 + TypeScript + Vite 构建。

### 授权激活

购买后，请将授权 ID 填入 [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL) 并通过 git commit 提交。git 提交记录作为授权生效的关键证明。通过 [mqttsnet.com](https://mqttsnet.com) 验证授权状态。

## 快速开始

```bash
pnpm install
pnpm dev            # 开发服务（http://localhost:3100）
pnpm build          # 生产环境打包
pnpm build:test     # 测试环境打包
```

## 🤖 Agent Skills(AI 辅助开发)

本仓库配套官方 **Agent Skill**,让 AI Agent(Claude Code · Codex · Cursor)基于真实代码、按需加载地理解本控制台。隶属 **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** 合集(一仓一 skill)。

```bash
# 全局(-g);去掉 -g 仅装当前项目
npx skills add mqttsnet/thinglinks-skills@thinglinks-web -g
```

涉及 IoT 页面、API 调用、共享组件 / 规则脚本调试面板,或文件放置与 **Flexy** 设计规范时自动触发。全部 skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)见 **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)**。

## 联系我们

- 商业合作：[mqttsnet@163.com](mailto:mqttsnet@163.com)
- 问题反馈：[GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)

> **声明：** Issue 唯一渠道为 [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)，其他平台不予处理。

## 开源协议

基于 [Apache License 2.0](LICENSE)，附加条款详见 [LICENSE-COMMERCIAL](LICENSE-COMMERCIAL)。

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
