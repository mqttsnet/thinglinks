# ThingLinks 开发运维手册

> 本文档记录项目日常开发、构建、部署、运维中的关键操作，方便团队快速查阅。

---

## 目录

- [版本管理](#版本管理)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [构建与编译](#构建与编译)
- [微服务端口清单](#微服务端口清单)
- [Docker 部署](#docker-部署)
- [前端项目](#前端项目)
- [Nacos 配置管理](#nacos-配置管理)
- [数据库](#数据库)
- [日常运维命令](#日常运维命令)
- [项目规范文档](#项目规范文档)

---

## 版本管理

### 组件版本清单

| 组件 | 唯一版本源 | 产品配置工具 |
|------|------------|--------------|
| Cloud（含 SDK） | `thinglinks-cloud/.thinglinks-product.env` | `thinglinks-cloud/scripts/product-config.sh` |
| Job | `thinglinks-job/.thinglinks-product.env` | `thinglinks-job/scripts/product-config.sh` |
| BifroMQ 插件 | `bifromq-plugin/.thinglinks-product.env` | `bifromq-plugin/scripts/product-config.sh` |
| Web 管理端 | `thinglinks-web/.thinglinks-product.env` | `thinglinks-web/scripts/product-config.mjs` |
| Web 可视化大屏 | `thinglinks-web-visualize/.thinglinks-product.env` | `thinglinks-web-visualize/scripts/product-config.mjs` |

五份清单均通过 `THINGLINKS_COMPONENT_VERSION` 定义组件发布版本。POM 和 `package.json` 是产品配置工具生成并校验的派生文件，不应直接修改。

ThingLinks Util 采用独立发布周期，不随五个组件统一升级。Cloud、Job、BifroMQ 插件分别通过自身清单中的 `THINGLINKS_UTIL_VERSION` 声明所需依赖版本，各组件可按兼容性采用不同的 Util 版本。`thinglinks-web/tests/server/` 是本地 Mock 测试服务器，也不纳入统一版本。

### 一键升级版本

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
"$REPO_ROOT/scripts/bump-version.sh" 1.4.0
```

该命令可在仓库内任意目录执行。脚本先读取五份产品清单并完成全部只读检查，再依次调用各组件工具更新版本；任一更新或最终检查失败时，会按相反顺序恢复已尝试更新的组件。

只检查五个组件的产品配置：

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
"$REPO_ROOT/scripts/bump-version.sh" check
```

需要定位某个组件时，也可以单独检查：

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
"$REPO_ROOT/thinglinks-cloud/scripts/product-config.sh" check
"$REPO_ROOT/thinglinks-job/scripts/product-config.sh" check
"$REPO_ROOT/bifromq-plugin/scripts/product-config.sh" check
"$REPO_ROOT/thinglinks-web/scripts/product-config.mjs" check
"$REPO_ROOT/thinglinks-web-visualize/scripts/product-config.mjs" check
```

需要单独更新某个组件时，应调用该组件工具，不直接编辑派生文件：

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
"$REPO_ROOT/thinglinks-cloud/scripts/product-config.sh" set-component-version 1.4.0
"$REPO_ROOT/thinglinks-job/scripts/product-config.sh" set-version 1.4.0
"$REPO_ROOT/bifromq-plugin/scripts/product-config.sh" set-version 1.4.0
"$REPO_ROOT/thinglinks-web/scripts/product-config.mjs" set-version 1.4.0
"$REPO_ROOT/thinglinks-web-visualize/scripts/product-config.mjs" set-version 1.4.0
```

### 版本号规范

常用格式为 `X.Y.Z`，兼容 `X.Y.Z.N` 和预发布后缀（例如 `1.4.0-SNAPSHOT`）。

- **X** — 大版本（架构变更、不兼容升级）
- **Y** — 功能版本（新增功能、模块）
- **Z** — 补丁版本（Bug 修复、优化）

---

## 项目结构

```
thinglinks/
├── thinglinks-cloud/                    # 后端微服务主体
│   ├── thinglinks-dependencies-parent/  # 版本 & 依赖统一管理（pom）
│   ├── thinglinks-public/               # 公共模块（通用配置、模型、SDK）
│   ├── thinglinks-base/                 # 基础服务
│   ├── thinglinks-oauth/                # 认证授权服务
│   ├── thinglinks-system/               # 系统管理服务
│   ├── thinglinks-gateway/              # API 网关（含 SOP 网关）
│   ├── thinglinks-broker/               # MQTT Broker 服务
│   ├── thinglinks-link/                 # 设备连接管理服务
│   ├── thinglinks-mqs/                  # 消息队列服务
│   ├── thinglinks-rule/                 # 规则引擎服务
│   ├── thinglinks-tds/                  # 时序数据库服务（TDengine）
│   ├── thinglinks-card/                 # 物联卡服务
│   ├── thinglinks-video/                # 流媒体服务
│   ├── thinglinks-view/                 # 可视化大屏服务
│   ├── thinglinks-mobile/               # 移动端服务
│   ├── thinglinks-generator/            # 代码生成器
│   ├── thinglinks-openapi/              # 开放接口服务
│   ├── thinglinks-sop-admin/            # 运营管理服务
│   ├── thinglinks-support/              # 支撑服务（监控、执行器）
│   ├── thinglinks-sdk/                  # 设备接入 SDK
│   ├── docker/                          # Cloud 服务 Docker 编排
│   └── docs/                            # Cloud 配置、数据库与开发规范
├── thinglinks-job/                      # 分布式任务调度
│   └── docs/db/mysql/                   # Job 工程数据库脚本
├── bifromq-plugin/                      # BifroMQ MQTT Broker 插件库
│   ├── bifromq-auth-provider-plugin/    # 认证授权插件
│   ├── bifromq-event-collector-plugin/  # 事件采集插件
│   ├── bifromq-resource-throttler-plugin/ # 资源限流插件
│   └── bifromq-setting-provider-plugin/ # 动态配置插件
├── thinglinks-web/                      # 前端主应用（Vue 3）
├── thinglinks-web-visualize/            # 前端可视化应用
├── docs/                                # 整个社区工程的文档
│   └── images/                          # 项目架构图与产品截图
└── scripts/                             # 运维脚本
    └── bump-version.sh                  # 版本升级脚本
```

### 微服务模块标准结构

每个微服务遵循分层架构：

```
thinglinks-<service>/
├── thinglinks-<service>-server/         # 启动入口（Spring Boot Application）
├── thinglinks-<service>-controller/     # REST API 控制层
├── thinglinks-<service>-biz/            # 业务逻辑层
├── thinglinks-<service>-entity/         # 实体 & DTO
└── thinglinks-<service>-facade/         # 对外接口层
    ├── thinglinks-<service>-api/        # API 客户端接口
    ├── thinglinks-<service>-boot-impl/  # 单体模式实现
    └── thinglinks-<service>-cloud-impl/ # 微服务模式实现（Feign）
```

---

## 环境要求

| 依赖 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 17+ | OpenJDK 或 Oracle JDK |
| Maven | 3.8+ | 构建工具 |
| Node.js | 16+ | 前端构建 |
| pnpm | 8+ | 前端包管理器 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存 & 认证 |
| Nacos | 3.x | 注册中心 & 配置中心 |
| TDengine | 3.0+ | 时序数据库 |
| Docker | 20.10+ | 容器化部署（可选） |

---

## 构建与编译

### Maven Profile 说明

| Profile | 用途 | 激活方式 |
|---------|------|---------|
| `dev` | 本地开发（默认激活） | `-P dev` 或不指定 |
| `test` | 测试环境 | `-P test` |
| `prod` | 生产环境 | `-P prod` |
| `docker` | Docker 容器部署 | `-P docker` |

### 常用构建命令

```bash
mvn clean compile -f thinglinks-cloud/pom.xml
mvn clean package -P test -f thinglinks-cloud/pom.xml -DskipTests
mvn clean package -P prod -f thinglinks-cloud/pom.xml -DskipTests
mvn clean package -pl thinglinks-link/thinglinks-link-server -am -P test -DskipTests
mvn clean install -f thinglinks-cloud/pom.xml -DskipTests
mvn clean package -f thinglinks-job/pom.xml -DskipTests
```

### Maven 仓库

| 类型 | 地址 |
|------|------|
| Release | 环境变量 `THINGLINKS_MAVEN_RELEASE_URL` |
| Snapshot | 环境变量 `THINGLINKS_MAVEN_SNAPSHOT_URL` |

发布制品时由运行环境注入仓库地址；仓库不保存私有制品库 URL 或访问凭据。普通本地编译不需要设置这两个变量。

---

## 微服务端口清单

| 服务 | 端口 | 模块 |
|------|------|------|
| Gateway（API 网关） | 18760 | thinglinks-gateway |
| SOP Gateway | 18750 | thinglinks-gateway |
| Monitor（监控） | 18762 | thinglinks-support |
| Base（基础服务） | 18764 | thinglinks-base |
| SOP Admin（运营管理） | 18765 | thinglinks-sop-admin |
| OpenAPI（开放接口） | 18766 | thinglinks-openapi |
| Job Admin（任务调度） | 18767 | thinglinks-job |
| OAuth（认证授权） | 18773 | thinglinks-oauth |
| System（系统管理） | 18778 | thinglinks-system |
| Generator（代码生成） | 18780 | thinglinks-generator |
| Link（设备连接） | 18782 | thinglinks-link |
| MQS（消息队列） | 18784 | thinglinks-mqs |
| Rule（规则引擎） | 18786 | thinglinks-rule |
| TDS（时序数据库） | 18788 | thinglinks-tds |
| Broker（MQTT） | 18790 | thinglinks-broker |
| View（可视化大屏） | 18792 | thinglinks-view |
| Card（物联卡） | 18794 | thinglinks-card |
| Video（流媒体） | 18796 | thinglinks-video |
| Mobile（移动端） | 18798 | thinglinks-mobile |
| Base Executor | 18900-18901 | thinglinks-support |
| IoT Executor | 18910-18911 | thinglinks-support |

---

## Docker 部署

### 基础镜像

```
registry.cn-hangzhou.aliyuncs.com/mqttsnet-community/openjdk:17-jdk
```

### JVM 默认参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| JVM_XMS | 512m（compose）/ 4g（Dockerfile） | 初始堆大小 |
| JVM_XMX | 1024m（compose）/ 8g（Dockerfile） | 最大堆大小 |
| JVM_MAX_METASPACE | 1024m | 元空间大小 |
| JVM_RESERVED_CODE_CACHE | 512m | 代码缓存 |
| GC | ZGC | 垃圾回收器 |

### Docker Compose 快速启动

```bash
cd thinglinks-cloud/docker && docker-compose up -d
cd thinglinks-job/docker && docker-compose up -d
docker-compose logs -f thinglinks-gateway-server
docker-compose down
```

### 健康检查

所有服务均配置了 Actuator 健康检查：
- 端点：`/actuator/health`
- 认证：Basic Auth（`ACTUATOR_USERNAME` / `ACTUATOR_PASSWORD`）
- 检查间隔：30s | 超时：10s | 重试：5 次 | 启动等待：60s

---

## 前端项目

### 两个前端应用

| 项目 | 目录 | 端口 | 说明 |
|------|------|------|------|
| 主应用 | `thinglinks-web/` | 3100 | 管理后台（Vue 3 + Vite） |
| 可视化应用 | `thinglinks-web-visualize/` | — | 大屏可视化 |

### 前端启动命令

```bash
cd thinglinks-web
pnpm install
pnpm dev
cd thinglinks-web-visualize
pnpm install
pnpm dev
```

### 前端环境配置

| 文件 | 环境 |
|------|------|
| `.env` | 基础配置 |
| `.env.development` | 开发环境 |
| `.env.test` | 测试环境 |
| `.env.production` | 生产环境 |

关键配置项：
```properties
VITE_PORT=3100                                    # 开发服务端口
VITE_GLOB_APP_TITLE=ThingLinks                    # 应用标题
VITE_GLOB_MODE=cloud                              # 运行模式（cloud/boot）
VITE_GLOB_MULTI_TENANT_TYPE=DATASOURCE_COLUMN     # 多租户模式
VITE_GLOB_CLIENT_ID=thinglinks_web                # OAuth 客户端 ID
VITE_GLOB_AXIOS_TIMEOUT=30000                     # 请求超时（ms）
```

### 前端 CI/CD（Jenkins）

Jenkins Pipeline 位于 `thinglinks-web/Jenkinsfile`。

**Jenkins 动态参数：**

| 参数 | 说明 |
|------|------|
| `SERVER_SSH_HOST` | 目标服务器（格式：`name_profile`，如 `server01_test`） |
| `IS_INSTALL` | 是否执行 `pnpm install`（true/false） |
| `branch` | 拉取的 Git 分支 |

**构建流程：**
1. 从 `SERVER_SSH_HOST` 解析环境（test/prod）
2. 根据环境选择构建脚本：`build:test` 或 `build:prod`
3. `pnpm install`（可选） → `pnpm build` → 压缩 dist
4. SSH 推送 tgz 到目标服务器
5. 执行 `server_node_run.sh` 解压部署

> **注意：** 目标服务器需预先部署 `server_node_run.sh` 到 `${WORKSPACE_HOME}/bin/`，并配置 nginx。

---

## Nacos 配置管理

### 连接信息（默认测试环境）

| 参数 | 值 |
|------|-----|
| 地址 | `127.0.0.1:18848` |
| 命名空间 | `c0858b2e-960e-47b9-9b2d-17294dda787c` |
| 用户名 | `thinglinks-test` |
| 分组 | `DEFAULT_GROUP` |

### 共享配置文件

每个微服务通过 Nacos 导入以下共享配置：

| 配置文件 | 说明 |
|---------|------|
| `common.yml` | 公共配置（日志、序列化等） |
| `redis.yml` | Redis 连接配置 |
| `database.yml` | MySQL 数据库连接 |
| `rocketmq.yml` | RocketMQ 消息队列 |
| `<service-name>.yml` | 各服务独立配置 |

### 导入 Nacos 配置

Nacos 配置文件存放在 `thinglinks-cloud/docs/config/nacos/` 目录下，新环境部署时需手动导入。

---

## 数据库

### 数据库脚本位置

```
thinglinks-cloud/docs/db/mysql/        # Cloud MySQL 全量与增量脚本
thinglinks-job/docs/db/mysql/          # 独立 Job 工程初始化脚本
```

### 相关文档

- [数据库使用说明](../thinglinks-cloud/docs/db/数据库使用说明.md)
- [数据库设计规范](../thinglinks-cloud/docs/db/数据库设计规范.md)
- [达梦数据库适配](../thinglinks-cloud/docs/db/达梦适配.md)
- [Job 调度数据库脚本说明](../thinglinks-job/docs/db/mysql/README.md)

---

## 日常运维命令

### Git 操作

```bash
git branch -a
git pull origin feature/202603_v1.3
git status && git diff --stat
```

### 版本升级完整流程

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
"$REPO_ROOT/scripts/bump-version.sh" 1.4.0
git diff
mvn clean compile -f "$REPO_ROOT/thinglinks-cloud/pom.xml"
mvn clean compile -f "$REPO_ROOT/thinglinks-job/pom.xml"
(cd "$REPO_ROOT/thinglinks-web" && pnpm build)
(cd "$REPO_ROOT/thinglinks-web-visualize" && pnpm build)
```

### 单服务重新部署（Docker）

```bash
docker-compose up -d --build thinglinks-link-server
docker-compose logs -f --tail=200 thinglinks-link-server
docker-compose restart thinglinks-link-server
```

### 排查问题

```bash
curl -u actuator-admin:Secure@mqttsnet http://localhost:18782/actuator/health
curl -u actuator-admin:Secure@mqttsnet http://localhost:18782/actuator/info
docker-compose ps
```

---

## 项目规范文档

| 文档 | 路径 |
|------|------|
| Cloud 异步多线程使用规范 | [thinglinks-cloud/docs/异步多线程使用规范.md](../thinglinks-cloud/docs/异步多线程使用规范.md) |
| Cloud 接口权限配置规范 | [thinglinks-cloud/docs/接口权限配置规范.md](../thinglinks-cloud/docs/接口权限配置规范.md) |
| Cloud 项目分层架构规范 | [thinglinks-cloud/docs/项目分层架构规范.md](../thinglinks-cloud/docs/项目分层架构规范.md) |
| Cloud 数据库使用说明 | [thinglinks-cloud/docs/db/数据库使用说明.md](../thinglinks-cloud/docs/db/数据库使用说明.md) |
| Cloud 数据库设计规范 | [thinglinks-cloud/docs/db/数据库设计规范.md](../thinglinks-cloud/docs/db/数据库设计规范.md) |
| 贡献者指南 | [CONTRIBUTING.md](../CONTRIBUTING.md) |
