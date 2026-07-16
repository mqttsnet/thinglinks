# Nacos Configuration Management / Nacos 配置管理

## Overview / 概述

This directory contains the Nacos configuration templates for all ThingLinks microservices. These files are meant to be imported into Nacos as the initial configuration for new environments.

本目录包含所有 ThingLinks 微服务的 Nacos 配置模板。用于新环境部署时导入 Nacos 作为初始配置。

## Directory Structure / 目录结构

```
config/
└── nacos/
    └── DEFAULT_GROUP/
        ├── common.yml                    # Shared config (feign, OpenAI, security keys)
        ├── database.yml                  # MySQL / TDengine / Druid connection config
        ├── redis.yml                     # Redis connection config
        ├── rabbitmq.yml                  # RabbitMQ connection config
        ├── rocketmq.yml                  # RocketMQ connection config
        ├── kafka.yml                     # Kafka connection config
        ├── dynamictp.yml                 # Dynamic thread pool config
        └── thinglinks-*-server.yml       # Per-service config (28 files)
```

## Security Notice / 安全须知

All passwords and API keys in these template files are **placeholders**. Before importing into Nacos:

1. Replace all `your-*-password` and `your-*-key` values with actual credentials
2. Never commit real credentials to this repository
3. Use Nacos's built-in encryption or external secret management for production

所有模板文件中的密码和密钥均为**占位符**。导入 Nacos 前请替换为实际值，切勿将真实凭据提交到代码仓库。

## Change Log / 变更记录

When modifying Nacos configuration templates, please follow these guidelines:

修改 Nacos 配置模板时，请遵循以下规范：

### Rules / 规范

1. **Record every change** — Update the table below when adding, modifying, or removing configuration items
2. **Specify scope** — Clearly indicate which services are affected
3. **Backward compatibility** — Mark breaking changes with `[BREAKING]`
4. **Test first** — Verify in test environment before updating templates

| Date | File | Change | Affected Services | Author |
|------|------|--------|-------------------|--------|
| 2026-03-23 | common.yml | Replace hardcoded credentials with placeholders | All | mqttsnet |
| 2026-03-23 | database.yml | Replace MySQL password with placeholder | All | mqttsnet |
| 2026-03-23 | redis.yml | Replace Redis password with placeholder | All | mqttsnet |
| 2026-03-23 | rabbitmq.yml | Replace RabbitMQ password with placeholder | All | mqttsnet |

### How to Import into Nacos / 导入方法

1. Log in to Nacos Console (`http://<nacos-ip>:18848/nacos`)
2. Select target namespace
3. Go to **Configuration Management** > **Configurations**
4. Use **Import** feature to bulk import, or create configurations manually
5. Set Group to `DEFAULT_GROUP`
6. Data ID = filename without `.yml` extension (e.g., `common`, `database`)
