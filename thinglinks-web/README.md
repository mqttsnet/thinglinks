<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="160"></a>

# thinglinks-web

**ThingLinks IoT Platform — Admin Console**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

[![Vue 3](https://img.shields.io/badge/Vue-3.3-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)](https://vuejs.org/) [![TypeScript](https://img.shields.io/badge/TypeScript-4.6-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/) [![Vite](https://img.shields.io/badge/Vite-4.3-646CFF?style=flat-square&logo=vite&logoColor=white)](https://vitejs.dev/)

</div>

---

## About

`thinglinks-web` is the admin console frontend for the [ThingLinks](https://mqttsnet.com) multi-tenant SaaS cloud IoT platform. Built with Vue 3 + TypeScript + Vite.

Product edition and component version metadata are available in [`.thinglinks-product.env`](.thinglinks-product.env).

## Quick Start

```bash
pnpm install
pnpm dev            # Dev server (http://localhost:3100)
pnpm build          # Production build
pnpm build:test     # Test environment build
```

## 🤖 Agent Skills (AI-assisted development)

An official **Agent Skill** documents this console for AI agents (Claude Code · Codex · Cursor) — answers grounded in real code, loaded on demand. It's part of the **[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** collection (one skill per repo).

```bash
# global (-g); drop -g to install into the current project only
npx skills add mqttsnet/thinglinks-skills@thinglinks-web -g
```

Auto-triggers when you build IoT pages, define API calls, use shared components / the rule-script debug panel, or follow the file-placement & **Flexy** design conventions. Browse all skills — `thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin` — at **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)**.

## Contact

- Business: [mqttsnet@163.com](mailto:mqttsnet@163.com)
- Issues: [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)

> **Note:** Issues should only be submitted via [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues).

## License

See [LICENSE](LICENSE) and the additional terms referenced by the product metadata manifest.

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
