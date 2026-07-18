<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="160"></a>

# thinglinks-web

**ThingLinks 物联网平台 — 管理控制台**

[English](README.md) | 简体中文 | [日本語](README.ja.md) | [한국어](README.ko.md)

[![Vue 3](https://img.shields.io/badge/Vue-3.3-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)](https://vuejs.org/) [![TypeScript](https://img.shields.io/badge/TypeScript-4.6-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/) [![Vite](https://img.shields.io/badge/Vite-4.3-646CFF?style=flat-square&logo=vite&logoColor=white)](https://vitejs.dev/)

</div>

---

## 简介

`thinglinks-web` 是 [ThingLinks](https://mqttsnet.com) 多租户 SaaS 云物联网平台的管理控制台前端，基于 Vue 3 + TypeScript + Vite 构建。

产品发行版本与组件版本元数据见根目录 [`.thinglinks-product.env`](.thinglinks-product.env)。

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

授权范围以 [LICENSE](LICENSE) 及产品元数据清单引用的附加条款为准。

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
