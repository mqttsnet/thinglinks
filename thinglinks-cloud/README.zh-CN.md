<div align="center">

[![MQTTSNET Logo](./docs/images/logo.png)](https://mqttsnet.com)

</div>

## ThingLinks | [English Documentation](README.md)

# ThingLinks平台简介

[![OSCS Status](https://www.oscs1024.com/platform/badge/mqttsnet/thinglinks.svg?size=small)](https://www.oscs1024.com/project/mqttsnet/thinglinks?ref=badge_small)

## 项目结构

```
thinglinks-cloud
├── thinglinks-dependencies-parent     // 项目父POM，依赖管理
├── thinglinks-public                  // 业务相关公共模块
│   ├── thinglinks-common              // 公共工具类
│   ├── thinglinks-common-config       // 公共配置
│   ├── thinglinks-model               // 数据模型
│   ├── thinglinks-file-sdk            // 文件SDK
│   ├── thinglinks-data-scope-sdk      // 数据权限SDK
│   ├── thinglinks-tenant-datasource-init  // 租户数据源初始化
│   ├── thinglinks-sa-token-ext        // SA-Token扩展
│   └── thinglinks-login-user-facade   // 登录用户门面
├── thinglinks-gateway                 // 网关服务
│   ├── thinglinks-gateway-biz         // 业务逻辑
│   ├── thinglinks-gateway-server      // 服务启动模块
│   └── thinglinks-sop-gateway-server  // SOP网关服务
├── thinglinks-oauth                   // 认证服务
├── thinglinks-system                  // 系统服务（用户、角色、权限等）
├── thinglinks-base                    // 基础服务（产品、设备等）
├── thinglinks-link                    // 设备集成服务（协议适配）
├── thinglinks-broker                  // Broker服务（MQTT代理）
├── thinglinks-rule                    // 规则引擎服务
├── thinglinks-tds                     // 时序数据服务（设备数据存储）
├── thinglinks-mqs                     // 消息治理服务
├── thinglinks-openapi                 // 开放接口服务
├── thinglinks-video                   // 视频流服务
├── thinglinks-card                    // 物联卡业务服务
├── thinglinks-view                    // 可视化服务
├── thinglinks-mobile                  // 移动端服务
├── thinglinks-sop-admin               // SOP管理端服务
├── thinglinks-generator               // 在线代码生成器服务
├── thinglinks-sdk                     // SDK模块
│   ├── thinglinks-sdk-core            // 核心SDK
│   └── thinglinks-simple-sdk          // 简易SDK
└── thinglinks-support                 // 支撑模块
    ├── thinglinks-monitor-server      // 监控服务
    ├── thinglinks-base-executor       // 基础执行器
    └── thinglinks-iot-executor        // IoT执行器
```

### 模块结构说明

每个业务模块采用分层架构，通常包含：

- **facade**: 接口定义层，对外提供RPC接口
- **entity**: 实体层，定义数据模型
- **biz**: 业务逻辑层，实现核心业务逻辑
- **controller**: 控制器层，处理HTTP请求
- **server**: 服务启动层，Spring Boot应用入口
