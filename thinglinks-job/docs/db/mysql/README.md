# ThingLinks Job 数据库脚本

本目录保存 `thinglinks_job` 数据库的初始化脚本和后续增量脚本。

```text
mysql/
├── README.md
├── baseline/
│   └── thinglinks_job.sql
└── migration/
    └── README.md
```

## 初始化数据库

`baseline/thinglinks_job.sql` 是完整初始化脚本，适用于 MySQL 8.0 及以上版本。

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS thinglinks_job CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -u root -p thinglinks_job < "$REPO_ROOT/thinglinks-job/docs/db/mysql/baseline/thinglinks_job.sql"
```

## 增量升级

后续 DDL 或必要的数据调整统一放在 `migration/`，具体命名和注意事项见 [增量脚本说明](migration/README.md)。

## 需要注意

- 初始化脚本包含 `DROP TABLE IF EXISTS`，只能用于新建数据库或确认允许清空的数据库。
- 已有数据库升级前必须备份，只执行尚未执行的增量脚本。
- 基线包含初始化任务和管理员账号，上线前应核对任务启停状态、执行器地址并修改默认密码。
- SQL 文件中不要保存数据库地址、账号密码或环境数据。
