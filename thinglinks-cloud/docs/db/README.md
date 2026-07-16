# Database Management / 数据库管理

## Overview / 概述

This directory contains database initialization scripts, migration files, and related documentation for the ThingLinks platform.

本目录包含 ThingLinks 平台的数据库初始化脚本、迁移文件及相关文档。

## Directory Structure / 目录结构

```
db/
├── README.md                    # This file / 本文件
├── mysql/
│   └── job/
│       └── thinglinks_job.sql   # Job scheduler database init script
├── 数据库使用说明.md              # Database usage guide
├── 数据库设计规范.md              # Database design standards
└── 达梦适配.md                   # DM (Dameng) database adaptation guide
```

## Database Change Management / 数据库变更管理

### Rules / 规范

1. **Script naming convention** — Use `V{version}__{description}.sql` format (e.g., `V1.3.0__add_device_shadow_table.sql`)
2. **One change per script** — Each script should contain a single logical change
3. **Idempotent** — Scripts should be safe to run multiple times (use `IF NOT EXISTS`, `IF EXISTS`)
4. **Record every change** — Update the changelog below when adding or modifying scripts
5. **Backward compatible** — Avoid dropping columns/tables in use; use deprecation first
6. **Include rollback** — For critical changes, provide a rollback script

### Changelog / 变更记录

| Date | Script | Database | Change Description | Author |
|------|--------|----------|--------------------|--------|
| 2026-03-23 | thinglinks_job.sql | thinglinks_job | Job scheduler initial schema | mqttsnet |

### Supported Databases / 支持的数据库

| Database | Version | Usage |
|----------|---------|-------|
| MySQL | 8.0+ | Primary relational database |
| TDengine | 3.0+ | Time-series data (device metrics) |
| DM (Dameng) | 8.x | Alternative (see 达梦适配.md) |

### Related Documentation / 相关文档

- [Database Usage Guide / 数据库使用说明](数据库使用说明.md)
- [Database Design Standards / 数据库设计规范](数据库设计规范.md)
- [DM Database Adaptation / 达梦适配](达梦适配.md)
