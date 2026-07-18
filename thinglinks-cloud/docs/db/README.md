# 数据库管理

本目录保存 ThingLinks Cloud 数据库初始化脚本、增量脚本和数据库规范。

```text
db/
├── README.md
├── mysql/
│   ├── README.md
│   ├── default/
│   │   ├── baseline/
│   │   └── migration/
│   └── tenant/
│       ├── baseline/
│       └── migration/
├── 数据库使用说明.md
├── 数据库设计规范.md
└── 达梦适配.md
```

MySQL 全量初始化、增量升级命令和脚本维护规则见 [MySQL 脚本说明](mysql/README.md)。

## 支持的数据库

| 数据库 | 版本 | 用途 |
| --- | --- | --- |
| MySQL | 8.0 及以上 | 默认库与租户业务库 |
| TDengine | 3.0 及以上 | 设备时序数据 |
| 达梦 | 8.x | MySQL 的可选替代方案 |

## 相关说明

- [数据库初始化指南](数据库使用说明.md)
- [数据库设计规范](数据库设计规范.md)
- [达梦适配](达梦适配.md)
- [Job 调度数据库](../../../thinglinks-job/docs/db/mysql/README.md)
