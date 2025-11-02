<div align="center">
    <a href="https://github.com/mqttsnet">
        <img alt="MqttsNet Logo" width="200" height="200" src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4">
    </a>
    <br><br>
    <h1>thinglinks-web</h1>
</div>

[中文](./README-zh.md) | **English**

## Introduction

ThingLinks is a cloud-based Internet of Things (IoT) SaaS platform featuring high performance, high
throughput, and high scalability. `thinglinks-web-pro`, the web component of ThingLinks, is
developed using cutting - edge technologies such as `Vue 3`, `Vite 2`, and `TypeScript`.

## Features

- **Cutting - edge Technology Stack**: Leveraging front - end technologies like Vue 3 and Vite 2 to
  ensure excellent performance and development experience.
- **TypeScript Integration**: Utilizing TypeScript, an application - level JavaScript language, to
  enhance code maintainability and robustness.
- **Configurable Themes**: Allowing flexible theme configuration to meet diverse visual
  requirements.
- **Comprehensive Internationalization**: Built - in internationalization solution for easy global
  market expansion.
- **Mock Data Scheme**: An internal Mock data solution for convenient front - end development and
  testing.
- **Dynamic Permission Management**: A complete dynamic route permission generation scheme to ensure
  system security and controllable data access.
- **Component Re - encapsulation**: Re - encapsulating multiple commonly used components to improve
  development efficiency.

## Technology Stack

- **Vue 3.x**: A modern front - end framework offering an efficient reactive development experience.
- **vuex 4.x**: A state management library for managing application states.
- **TypeScript 4.x**: Static type checking to enhance code quality.
- **ant - design - vue 2.x**: A Vue implementation based on Ant Design, providing a rich set of UI
  components.
- **axios 0.21.x**: A powerful HTTP client for data interaction with the backend.
- **echarts 5.x**: An excellent data visualization library for presenting various charts.
- **Vite 2.x**: A fast build tool to speed up development and building processes.

## Environment Requirements

- **Node.js**: Version greater than `18.0.0`.
- **pnpm**: A package manager. Please download the appropriate pnpm version according to the version
  correspondence between Node, npm, and pnpm.

### Installation

```
npm install
```

```
npm install -g pnpm
！！！Download the appropriate pnpm version based on the compatibility between Node, npm, and pnpm versions.
```

### Development Environment

```bash
pnpm serve
```

### Build

```bash

pnpm build # Build the project

pnpm build:no-cache # Build the project after clearing the cache

pnpm report # Generate a build package report preview
```

### Formatting

```bash
pnpm lint:stylelint # Format styles

pnpm lint:prettier # Format JS/TS code
```

### Other Commands

```bash
pnpm reinstall      # Remove dependencies and reinstall (compatible with Windows)

pnpm preview        # Preview the build locally

pnpm log            # Generate CHANGELOG

pnpm clean:cache    # Clear the cache

pnpm clean:lib      # Remove node_modules (compatible with Windows)

```

## Copyright Usage Notice

ThingLinks is licensed under the Apache License, Version 2.0. This license permits commercial use, provided that all original copyright notices and attribution must be retained in all copies or substantial portions of the software.