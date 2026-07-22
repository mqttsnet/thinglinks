# ThingLinks Job 数据库脚本

本目录保存 `thinglinks_job` 数据库的初始化脚本和后续增量脚本。

```text
mysql/
├── README.md
├── baseline/
│   └── thinglinks_job.sql
└── migration/
    ├── README.md
    └── V0001__sync_thinglinks_job_definitions.sql
```

## 初始化数据库

`baseline/thinglinks_job.sql` 是完整初始化脚本，适用于 MySQL 8.0 及以上版本。初始化任务默认保持停止，IoT 执行器使用自动注册模式。

基线中的 `admin` 账号不包含固定初始密码。下面的命令会交互读取两次新密码，只将密码的 MD5 值写入临时 SQL，然后完成数据库创建和导入：

```bash
(
  set -eu
  REPO_ROOT="$(git rev-parse --show-toplevel)"
  BASELINE_SQL="$REPO_ROOT/thinglinks-job/docs/db/mysql/baseline/thinglinks_job.sql"
  TEMP_SQL="$(mktemp "${TMPDIR:-/tmp}/thinglinks-job-baseline.XXXXXX")"
  cleanup_job_database_init() {
    stty echo 2>/dev/null || true
    rm -f "$TEMP_SQL"
  }
  trap cleanup_job_database_init EXIT HUP INT TERM
  printf '请输入 Job 管理员初始密码（至少 12 位）：'
  stty -echo
  IFS= read -r JOB_ADMIN_PASSWORD
  printf '\n请再次输入 Job 管理员初始密码：'
  IFS= read -r JOB_ADMIN_PASSWORD_CONFIRM
  stty echo
  printf '\n'
  if [ "${#JOB_ADMIN_PASSWORD}" -lt 12 ]; then
    printf '管理员初始密码不能少于 12 位。\n' >&2
    exit 1
  fi
  if [ "$JOB_ADMIN_PASSWORD" != "$JOB_ADMIN_PASSWORD_CONFIRM" ]; then
    printf '两次输入的管理员初始密码不一致。\n' >&2
    exit 1
  fi
  JOB_ADMIN_PASSWORD_MD5="$(printf '%s' "$JOB_ADMIN_PASSWORD" | openssl dgst -md5 -r | awk '{print $1}')"
  if ! printf '%s\n' "$JOB_ADMIN_PASSWORD_MD5" | grep -Eq '^[0-9a-fA-F]{32}$'; then
    printf '无法生成管理员初始密码摘要，请确认已安装 OpenSSL。\n' >&2
    exit 1
  fi
  PLACEHOLDER_COUNT="$(grep -c '__XXL_JOB_ADMIN_PASSWORD_MD5__' "$BASELINE_SQL" || true)"
  if [ "$PLACEHOLDER_COUNT" -ne 1 ]; then
    printf '数据库基线中的管理员密码占位符数量不正确。\n' >&2
    exit 1
  fi
  sed "s/__XXL_JOB_ADMIN_PASSWORD_MD5__/$JOB_ADMIN_PASSWORD_MD5/g" "$BASELINE_SQL" > "$TEMP_SQL"
  JOB_ADMIN_PASSWORD=''
  JOB_ADMIN_PASSWORD_CONFIRM=''
  mysql -u root -p -e "CREATE DATABASE thinglinks_job CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
  mysql -u root -p thinglinks_job < "$TEMP_SQL"
)
```

直接导入未替换占位符的基线不会产生可登录的默认密码。初始化完成后，在 Job 管理后台按实际环境核对执行器和任务，再逐项启用需要运行的任务。

## 增量升级

后续 DDL 或必要的数据调整统一放在 `migration/`，具体命名和注意事项见 [增量脚本说明](migration/README.md)。从旧版基线升级时先停止 Job Admin 服务并备份数据库，再执行：

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
mysql -u root -p thinglinks_job < "$REPO_ROOT/thinglinks-job/docs/db/mysql/migration/V0001__sync_thinglinks_job_definitions.sql"
```

`V0001` 只补齐缺失任务并让新增任务保持停止；已有合法任务的自定义启停状态、调度表达式和管理员密码不会被覆盖。

## 需要注意

- 初始化脚本包含 `DROP TABLE IF EXISTS`，只能用于新建数据库或确认允许清空的数据库。
- 已有数据库升级前必须备份，只执行尚未执行的增量脚本。
- 基线包含初始化任务和管理员账号，上线前应核对任务启停状态、执行器注册结果和管理员密码。
- SQL 文件中不要保存数据库地址、账号密码或环境数据。
