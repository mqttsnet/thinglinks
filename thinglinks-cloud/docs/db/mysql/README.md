# ThingLinks Cloud MySQL 脚本

本目录保存 Cloud 默认库和租户库的全量基线，以及后续增量脚本的维护说明。当前版本 `1.4.0` 仅提供全量基线，不提供增量升级脚本。

```text
mysql/
├── README.md
├── default/
│   ├── baseline/
│   │   └── thinglinks_ds_c_defaults.sql
│   └── migration/
│       └── README.md
└── tenant/
    ├── baseline/
    │   └── thinglinks_base_1.sql
    └── migration/
        └── README.md
```

## 版本来源

Cloud 当前版本以 [`thinglinks-cloud/.thinglinks-product.env`](../../../.thinglinks-product.env) 中的 `THINGLINKS_COMPONENT_VERSION` 为准，当前值为 `1.4.0`。

后续版本如提供增量脚本，文件名使用 `V{THINGLINKS_COMPONENT_VERSION}__{英文小写下划线描述}.sql`。文件名记录脚本首次发布时的组件版本；组件升级后，历史脚本保留原版本，不改名。

| Cloud 版本 | 默认库全量基线 | 默认库增量脚本 | 租户库全量基线 | 租户库增量脚本 |
| --- | --- | --- | --- | --- |
| 1.4.0 | `default/baseline/thinglinks_ds_c_defaults.sql` | 不提供 | `tenant/baseline/thinglinks_base_1.sql` | 不提供 |

本表记录仓库当前数据库交付状态。全量基线对应当前 Cloud 版本。升级组件版本时，应同步更新全量基线和本表；仅在明确提供增量升级能力时，才在对应增量目录追加脚本，已有版本记录不修改。

## 本次版本交付说明

- `1.4.0` 仅提供默认库和租户库的全量基线。
- `default/migration/` 和 `tenant/migration/` 当前仅保留维护说明，没有可执行的增量 SQL。
- 新环境使用全量基线初始化；已有数据库不能直接使用本目录完成从旧版本到 `1.4.0` 的通用增量升级，应根据现有数据和目标基线单独制定迁移方案。

## 脚本分类

| 数据库 | 全量基线 | 增量目录 | 使用场景 |
| --- | --- | --- | --- |
| `thinglinks_ds_c_defaults` | `default/baseline/thinglinks_ds_c_defaults.sql` | `default/migration/` | 平台公共配置、权限、字典及租户元数据 |
| `thinglinks_base_{tenantId}` | `tenant/baseline/thinglinks_base_1.sql` | `tenant/migration/` | 各租户业务数据与 IoT 业务表 |

`thinglinks_base_1.sql` 对应内置租户 1。新租户由平台租户初始化流程创建。

后续如提供增量脚本，其命名及维护规则分别见[默认库增量说明](default/migration/README.md)和[租户库增量说明](tenant/migration/README.md)。
