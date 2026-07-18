<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# thinglinks-web-visualize

**ThingLinks 物联网平台 — 大屏可视化**

[English](README.md) | 简体中文 | [日本語](README.ja.md) | [한국어](README.ko.md)

</div>

---

## 简介

`thinglinks-web-visualize` 是 [ThingLinks](https://github.com/mqttsnet/thinglinks) 多租户 SaaS 云物联网平台的数据可视化大屏前端。基于 Vue 3 + TypeScript + Vite 构建，支持多种图表类型、3D 可视化、地图展示等，适用于物联网数据监控和业务大屏场景。

产品身份、发行信息和组件版本统一维护在 [`.thinglinks-product.env`](.thinglinks-product.env)，更新与验证命令见[产品配置快捷操作](docs/产品配置快捷操作.md)。

## 技术栈

![Vue 3](https://img.shields.io/badge/Vue-3.2-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-4.6-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-4.3-646CFF?style=flat-square&logo=vite&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-2.0-FFD859?style=flat-square)
![Naive UI](https://img.shields.io/badge/Naive%20UI-2.34-18A058?style=flat-square)
![ECharts](https://img.shields.io/badge/ECharts-5.3-AA344D?style=flat-square&logo=apacheecharts&logoColor=white)
![Three.js](https://img.shields.io/badge/Three.js-0.145-000000?style=flat-square&logo=threedotjs&logoColor=white)

## 特性

- 基于 ECharts 的丰富图表组件（柱状图、折线图、饼图、雷达图、仪表盘等）
- 高德地图可视化（热力图、散点图、区域图）
- 基于 Three.js 的 3D 场景渲染
- 可视化拖拽编辑器，所见即所得
- 响应式布局，多分辨率适配
- 主题定制与国际化（i18n）

## 项目结构

```
thinglinks-web-visualize/
├── src/
│   ├── api/              # API 接口
│   ├── components/       # 公共组件
│   ├── hooks/            # 组合式 API
│   ├── packages/         # 图表组件包
│   ├── router/           # 路由配置
│   ├── store/            # 状态管理（Pinia）
│   ├── views/            # 页面视图
│   └── main.ts           # 入口
├── .env                  # 基础配置
├── .env.development      # 开发环境
├── .env.production       # 生产环境
└── vite.config.ts        # Vite 配置
```

## 快速开始

### 环境要求

- **Node.js** >= 18.0.0
- **pnpm**（安装：`npm install -g pnpm`）

### 安装与运行

```bash
# 安装依赖
pnpm install

# 启动开发服务
pnpm dev

# 生产环境打包
pnpm build

# 本地预览生产包
pnpm preview
```

### 代码质量

```bash
pnpm lint          # 代码检查
pnpm lint:fix      # 代码检查并自动修复
```

## 浏览器支持

| 浏览器 | 支持 |
|--------|------|
| Chrome | 最新版 |
| Firefox | 最新版 |
| Safari | 最新版 |
| Edge | 最新版 |
| IE | 不支持 |

## 开源协议

授权内容见 [LICENSE](LICENSE) 及产品元数据清单所引用的附加条款。

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
