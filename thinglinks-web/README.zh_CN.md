<div align="center">
    <a href="https://github.com/mqttsnet">
        <img alt="MqttsNet Logo" width="200" height="200" src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4">
    </a>
    <br><br>
    <h1>thinglinks-web-pro</h1>
</div>

**中文** | [English](./README.md)

## 简介
ThingLinks 是一款具备高性能、高吞吐量和高扩展性的云物联网 SaaS 平台。`thinglinks-web` 作为 ThingLinks 的 Web 端，采用了 `Vue 3`、`Vite 2`、`TypeScript` 等当下主流技术进行开发。

## 特性
- **前沿技术栈**：运用 Vue 3、Vite 2 等前端前沿技术，确保项目具备优秀的性能和开发体验。
- **TypeScript 加持**：使用应用程序级 JavaScript 语言 TypeScript，增强代码的可维护性和健壮性。
- **主题可配置**：支持灵活的主题配置，满足多样化的视觉需求。
- **完善国际化**：内置完备的国际化方案，方便拓展全球市场。
- **Mock 数据方案**：提供内置的 Mock 数据方案，便于前端开发和测试。
- **动态权限管理**：具备完善的动态路由权限生成方案，保障系统的安全性和数据访问的可控性。
- **组件二次封装**：对多个常用组件进行二次封装，提高开发效率。

## 技术栈
- **Vue 3.x**：先进的前端框架，带来高效的响应式开发体验。
- **vuex 4.x**：状态管理库，用于管理应用的状态。
- **TypeScript 4.x**：静态类型检查，提升代码质量。
- **ant-design-vue 2.x**：基于 Ant Design 的 Vue 实现，提供丰富的 UI 组件。
- **axios 0.21.x**：强大的 HTTP 客户端，用于与后端进行数据交互。
- **echarts 5.x**：优秀的数据可视化库，用于呈现各种图表。
- **Vite 2.x**：快速的构建工具，提升开发和构建速度。

## 环境要求
- **Node.js**：版本需大于 `18.0.0`。
- **pnpm**：包管理工具，请根据 Node、npm 和 pnpm 的版本对应关系下载合适的 pnpm 版本。

### 安装

```
npm install
```

```
npm install -g pnpm
！！！根据node npm pnpm版本对应关系下载pnpm对应版本
```

### 开发环境

```bash
pnpm serve
```

### 打包

```bash

pnpm build # 打包

pnpm build:no-cache # 打包，执行之前会先删除缓存

pnpm report # 生成构建包报表预览
```

### 格式化

```bash
pnpm lint:stylelint # 样式格式化

pnpm lint:prettier # js/ts代码格式化
```

### 其他

```bash
pnpm reinstall # 删除依赖重新装，兼容window

pnpm preview # 本地进行打包预览

pnpm log # 生成CHANGELOG

pnpm clean:cache # 删除缓存

pnpm clean:lib # 删除node_modules，兼容window系统
```

