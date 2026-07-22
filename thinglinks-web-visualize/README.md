<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# thinglinks-web-visualize

**ThingLinks IoT Platform — Visualization Dashboard**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

</div>

---

## About

`thinglinks-web-visualize` is the data visualization and large-screen dashboard frontend for the [ThingLinks](https://github.com/mqttsnet/thinglinks) multi-tenant SaaS cloud IoT platform. Built with Vue 3 + TypeScript + Vite, it supports rich chart types, 3D visualization, and map displays for IoT monitoring scenarios.

Product identity, edition and component version are maintained in [`.thinglinks-product.env`](.thinglinks-product.env). See [Product Configuration Quick Operations](docs/产品配置快捷操作.md) for update and verification commands.

## Tech Stack

![Vue 3](https://img.shields.io/badge/Vue-3.2-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-4.6-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-4.3-646CFF?style=flat-square&logo=vite&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-2.0-FFD859?style=flat-square)
![Naive UI](https://img.shields.io/badge/Naive%20UI-2.34-18A058?style=flat-square)
![ECharts](https://img.shields.io/badge/ECharts-5.3-AA344D?style=flat-square&logo=apacheecharts&logoColor=white)
![Three.js](https://img.shields.io/badge/Three.js-0.145-000000?style=flat-square&logo=threedotjs&logoColor=white)

## Features

- Rich chart components based on ECharts (bar, line, pie, radar, gauge, etc.)
- Map visualization with Amap (heatmap, scatter, region)
- 3D scene rendering with Three.js
- Drag-and-drop dashboard editor (WYSIWYG)
- Responsive layout with multi-resolution support
- Theme customization and internationalization (i18n)

## Project Structure

```
thinglinks-web-visualize/
├── src/
│   ├── api/              # API layer
│   ├── components/       # Shared components
│   ├── hooks/            # Composables
│   ├── packages/         # Chart component packages
│   ├── router/           # Routes
│   ├── store/            # State management (Pinia)
│   ├── views/            # Pages
│   └── main.ts           # Entry
├── .env                  # Base config
├── .env.development      # Dev config
├── .env.production       # Prod config
└── vite.config.ts        # Vite config
```

## Quick Start

### Requirements

- **Node.js** >= 18.0.0
- **pnpm** (install: `npm install -g pnpm`)

### Install & Run

```bash
# Install dependencies
pnpm install

# Start dev server
pnpm dev

# Build for production
pnpm build

# Preview production build
pnpm preview
```

### Code Quality

```bash
pnpm lint          # Lint check
pnpm lint:fix      # Lint and auto-fix
```

## Browser Support

| Browser | Support |
|---------|---------|
| Chrome | Latest |
| Firefox | Latest |
| Safari | Latest |
| Edge | Latest |
| IE | Not supported |

## License

Licensed under the [Apache License 2.0](LICENSE).

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
