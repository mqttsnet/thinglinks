# ThingLinks Cloud MySQL 脚本

本目录保存 Cloud 默认库和租户库的全量基线、历史增量脚本。

```text
mysql/
├── README.md
├── default/
│   ├── baseline/
│   │   ├── README.md
│   │   └── thinglinks_ds_c_defaults.sql
│   └── migration/
│       ├── README.md
│       ├── V1.4.0__register_rule_notification_apis.sql
│       ├── V1.4.0__rename_web_oauth_client.sql
│       ├── V1.4.0__sync_dictionary_and_resources.sql
│       ├── V1.4.0__sanitize_seed_data.sql
│       └── verify-default-database.sh
└── tenant/
    ├── baseline/
    │   ├── README.md
    │   └── thinglinks_base_1.sql
    └── migration/
        ├── README.md
        ├── V1.4.0__sync_tenant_schema.sql
        └── verify-tenant-migration.sh
```

## 版本来源

Cloud 当前版本以 [`thinglinks-cloud/.thinglinks-product.env`](../../../.thinglinks-product.env) 中的 `THINGLINKS_COMPONENT_VERSION` 为准，当前值为 `1.4.0`。

增量脚本使用 `V{THINGLINKS_COMPONENT_VERSION}__{英文小写下划线描述}.sql`。文件名记录脚本首次发布时的组件版本；组件升级后，历史脚本保留原版本，不改名。

| Cloud 版本 | 默认库全量基线 | 默认库增量记录 | 租户库全量基线 | 租户库增量记录 |
| --- | --- | --- | --- | --- |
| 1.4.0 | `default/baseline/thinglinks_ds_c_defaults.sql` | 4 个 `V1.4.0` 脚本 | `tenant/baseline/thinglinks_base_1.sql` | 1 个 `V1.4.0` 脚本 |

本表记录仓库当前数据库交付状态。全量基线对应当前 Cloud 版本；每个增量脚本记录它首次随哪个 Cloud 版本发布。升级组件版本时，应同步更新全量基线和本表，并在对应增量目录追加新版本脚本，不修改已有版本记录。

## 脚本分类

| 数据库 | 全量基线 | 增量目录 | 使用场景 |
| --- | --- | --- | --- |
| `thinglinks_ds_c_defaults` | `default/baseline/thinglinks_ds_c_defaults.sql` | `default/migration/` | 平台公共配置、权限、字典及租户元数据 |
| `thinglinks_base_{tenantId}` | `tenant/baseline/thinglinks_base_1.sql` | `tenant/migration/` | 各租户业务数据与 IoT 业务表 |

`thinglinks_base_1.sql` 对应内置租户 1。新租户由平台租户初始化流程创建；已有租户升级时，对每个租户库分别执行尚未执行的增量脚本。

## 新环境初始化

以下命令可在仓库内任意目录执行：

```bash
GIT_ROOT="$(git rev-parse --show-toplevel)"
CLOUD_ROOT="$GIT_ROOT"
if [ -d "$GIT_ROOT/thinglinks-cloud/docs/db/mysql" ]; then CLOUD_ROOT="$GIT_ROOT/thinglinks-cloud"; fi
"$CLOUD_ROOT/docs/db/mysql/default/migration/verify-default-database.sh"
"$CLOUD_ROOT/docs/db/mysql/tenant/migration/verify-tenant-migration.sh"
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS thinglinks_ds_c_defaults CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -u root -p thinglinks_ds_c_defaults < "$CLOUD_ROOT/docs/db/mysql/default/baseline/thinglinks_ds_c_defaults.sql"
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS thinglinks_base_1 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -u root -p thinglinks_base_1 < "$CLOUD_ROOT/docs/db/mysql/tenant/baseline/thinglinks_base_1.sql"
```

全量基线已包含当前结构和初始化数据，新环境不需要再执行历史增量脚本。

默认库基线中的管理员密码、7 个内置应用密钥、数据源密码、地图配置、通知渠道和外部控制台地址均使用用途明确的占位值。启动服务前应按[默认库基线说明](default/baseline/README.md)设置管理员密码并替换所有必需占位值；租户 1 基线也需要配置地图占位值。完成后核对连接地址：

```sql
SELECT id, name, username, url
FROM def_datasource_config
ORDER BY id;
```

## 已有环境升级

默认库按顺序执行尚未执行的脚本：

```bash
GIT_ROOT="$(git rev-parse --show-toplevel)"
CLOUD_ROOT="$GIT_ROOT"
if [ -d "$GIT_ROOT/thinglinks-cloud/docs/db/mysql" ]; then CLOUD_ROOT="$GIT_ROOT/thinglinks-cloud"; fi
mysql -u root -p thinglinks_ds_c_defaults < "$CLOUD_ROOT/docs/db/mysql/default/migration/V1.4.0__register_rule_notification_apis.sql"
mysql -u root -p thinglinks_ds_c_defaults < "$CLOUD_ROOT/docs/db/mysql/default/migration/V1.4.0__rename_web_oauth_client.sql"
mysql -u root -p thinglinks_ds_c_defaults < "$CLOUD_ROOT/docs/db/mysql/default/migration/V1.4.0__sync_dictionary_and_resources.sql"
mysql -u root -p thinglinks_ds_c_defaults < "$CLOUD_ROOT/docs/db/mysql/default/migration/V1.4.0__sanitize_seed_data.sql"
```

每个已有租户库都要单独执行租户迁移。下面示例只升级租户 1：

```bash
GIT_ROOT="$(git rev-parse --show-toplevel)"
CLOUD_ROOT="$GIT_ROOT"
if [ -d "$GIT_ROOT/thinglinks-cloud/docs/db/mysql" ]; then CLOUD_ROOT="$GIT_ROOT/thinglinks-cloud"; fi
TENANT_DATABASE="thinglinks_base_1"
mysql -u root -p "$TENANT_DATABASE" < "$CLOUD_ROOT/docs/db/mysql/tenant/migration/V1.4.0__sync_tenant_schema.sql"
```

## 需要注意

- 全量基线包含 `DROP TABLE IF EXISTS`，仅用于新建或明确允许清空的数据库。
- 增量脚本只能按编号顺序执行；已经执行过的脚本不要重复运行。
- 默认库 `V1.4.0__sync_dictionary_and_resources.sql` 会删除并重建指定字典和资源数据，执行前应核对已有自定义配置。
- 默认库 `V1.4.0__sanitize_seed_data.sql` 只处理主键和历史身份同时匹配的演示种子、固定凭据与私有地址；已修改的同主键数据会跳过，执行后要确认脚本末尾校验均为 `0`。
- 租户库 `V1.4.0__sync_tenant_schema.sql` 保留旧字段与旧业务表，不自动删除或合并业务数据；其中 DDL 仍会隐式提交，必须逐库备份并安排停机窗口。
- 脚本统一使用 `utf8mb4` 和 `utf8mb4_general_ci`。
- 初始化后应检查数据源地址、账号、密码占位值、地图密钥、应用/客户端密钥和管理员账号，替换为当前环境配置。
- 全量基线不保存登录日志、通知发送记录、作业节点以及 SOP 私钥；SOP 签名密钥需在目标环境初始化并妥善保管。
- SQL 文件不得保存新的数据库地址、真实账号密码、运行日志或环境临时数据。

后续增量命名及维护规则分别见[默认库增量说明](default/migration/README.md)和[租户库增量说明](tenant/migration/README.md)。
