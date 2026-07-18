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

Passwords, API keys, private endpoints, and storage locations are read from environment variables. Empty values disable the corresponding optional integration; local file storage and localhost endpoints are the safe development defaults. `THINGLINKS_SECURITY_AES_KEY` and `THINGLINKS_SECURITY_AES_IV` are required because they protect encrypted database fields.

密码、API 密钥、私有服务地址和存储位置均通过环境变量注入。空值表示对应的可选集成未配置；本地文件存储和 localhost 地址用于开发环境的安全默认配置。凭据适合保存在部署平台的密钥管理服务中，不写入本目录的模板文件。

`THINGLINKS_SECURITY_AES_KEY` 是数据库敏感字段的 AES 密钥，长度为 16、24 或 32 字节；`THINGLINKS_SECURITY_AES_IV` 是 CBC 模式初始化向量，长度为 16 字节。两项均为运行必需配置，Docker 部署可从 `docker/.env.example` 复制环境变量模板后填写。

文件存储类型由 `THINGLINKS_FILE_STORAGE_TYPE` 指定。各云存储使用对应的 `ALI_OSS_*`、`MINIO_*`、`HUAWEI_OSS_*` 或 `QINIU_*` 环境变量；本地存储使用 `THINGLINKS_FILE_LOCAL_STORAGE_PATH` 和 `THINGLINKS_FILE_LOCAL_URL_PREFIX`。

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
| 2026-07-19 | common.yml | Require AES key and IV for encrypted database fields | All | ThingLinks |
| 2026-07-19 | common.yml | Remove the default browser authorization value | API documentation | ThingLinks |
| 2026-07-19 | thinglinks-base-server.yml | Externalize file storage endpoints and use local storage by default | Base | ThingLinks |
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
