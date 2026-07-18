# 租户库增量脚本

本目录保存 `thinglinks_base_{tenantId}` 的顺序增量脚本。版本来源为 [`thinglinks-cloud/.thinglinks-product.env`](../../../../../.thinglinks-product.env) 的 `THINGLINKS_COMPONENT_VERSION`，当前记录版本为 `1.4.0`。

| 执行顺序 | 版本 | 文件 | 用途 |
| --- | --- | --- | --- |
| 1 | 1.4.0 | [`V1.4.0__sync_tenant_schema.sql`](V1.4.0__sync_tenant_schema.sql) | 同步产品版本、规则引擎、CA 证书和视频业务结构，并保留旧结构供核对 |

## 执行前

1. 先完成默认库的同版本增量脚本，再逐个处理租户库。
2. 停止会写入目标租户库的 Cloud 服务，记录当前应用版本和租户数据库名。
3. 对目标租户库做全量备份并验证备份文件可读取。DDL 会隐式提交，不能依靠事务整体回滚。
4. 确认目标为已有租户库；新环境只执行 `tenant/baseline/thinglinks_base_1.sql`，不要再执行本增量。
5. 执行账号需具备目标库的 `SELECT`、`UPDATE`、`CREATE`、`ALTER`、`INDEX`、`CREATE ROUTINE` 和 `ALTER ROUTINE` 权限。
6. 不要给 MySQL 客户端增加 `--force`，发生 SQL 错误时应立即停止。

下面以租户 1 为例，命令可从仓库内任意目录执行：

```bash
GIT_ROOT="$(git rev-parse --show-toplevel)"
if [ -d "$GIT_ROOT/thinglinks-cloud" ]; then
  CLOUD_ROOT="$GIT_ROOT/thinglinks-cloud"
else
  CLOUD_ROOT="$GIT_ROOT"
fi
TENANT_DATABASE="thinglinks_base_1"
BACKUP_FILE="${TENANT_DATABASE}_before_1.4.0_$(date +%Y%m%d%H%M%S).sql"

"$CLOUD_ROOT/docs/db/mysql/tenant/migration/verify-tenant-migration.sh"
mysqldump -u root -p --routines --triggers --events --single-transaction "$TENANT_DATABASE" > "$BACKUP_FILE"
mysql -u root -p --show-warnings "$TENANT_DATABASE" < "$CLOUD_ROOT/docs/db/mysql/tenant/migration/V1.4.0__sync_tenant_schema.sql"
```

停服务后使用 `--single-transaction` 仍可获得一致的 InnoDB 快照；如果库中还有非 InnoDB 表，应按现场备份规范额外锁表或使用物理备份。

## 回填与旧结构保留

- `product.active_version_no` 从旧 `product.product_version` 回填。长度超过 64 的旧值不会截断，留在旧列中供人工确认；`previous_full_version_no` 没有可靠历史来源，保持空值。
- `rule_groovy_script` 只自动识别旧内置 MQTT/WS 数据协议组合。旧 `business_identification` 在该组合中明确表示产品标识，因此回填到 `product_identification`；旧结构没有主题模式，`topic_pattern` 不做猜测。`namespace`、`platform_code`、`product_code`、`business_code`、`business_identification` 和旧唯一索引都保留。
- `video_media_server.host`、`hook_host`、`sdp_host`、`stream_host` 分别从 `ip`、`hook_ip`、`sdp_ip`、`stream_ip` 回填。已有新值不会被覆盖，四个旧地址列和旧唯一索引继续保留。
- `empowerment_record`、`video_device_channel`、`video_device_info` 没有完整、可证明的新表映射，因此不删除、不搬迁，也不改写其逻辑删除状态或租户定制数据。待业务方完成数据归档方案后，再新增独立迁移脚本。

数据回填使用短事务；结构变更通过 `information_schema` 探测后执行。新增表使用 `CREATE TABLE IF NOT EXISTS`，新增列和索引只在不存在时创建，因此同一脚本可用于完成未结束的迁移。除三个视频凭据字段会按加密存储所需容量自动扩容外，脚本不会自动修正其他已经存在但定义不一致的新表，遇到这种情况应先人工比对结构。

## 视频凭据加密迁移

`video_channel.password`、`video_notify_subscription.channel_config` 和 `video_platform.password` 由应用的 `EncryptTypeHandler` 加密落盘。处理器使用 `ENC@` 标记识别密文，读取旧明文时保持兼容，写入已带合法标记的密文时不会重复加密。

增量脚本只扩充字段容量并统计旧明文，不在 SQL 中持有或复制应用加密密钥。执行脚本后查看 `video_plaintext_credential_count_manual_migration`：

1. 结果为 `0` 时无需处理历史凭据。
2. 结果大于 `0` 时，先部署包含本次加密处理器配置的 Cloud 版本。
3. 在视频通道、通知订阅和级联平台的编辑入口中，为对应记录重新输入已知凭据并保存；不知道旧值时重置为新的安全凭据。
4. 更新请求不携带凭据或只携带空白字符时会保留旧值，因此仅修改其他字段不会完成明文迁移。
5. 重跑本增量脚本或单独执行脚本末尾的统计查询，直到结果为 `0`。

不要在数据库中手工拼接 `ENC@` 前缀；该前缀只表示处理器生成的完整密文格式。迁移期间不要从查询接口读取旧凭据，接口响应不会回显这些字段。

## 唯一索引预检

脚本不会为建立唯一索引而删除或合并数据。它会先输出以下计数：

- `rule_script_identity_blank_count_should_be_0`
- `rule_script_duplicate_group_count_should_be_0`
- `media_server_empty_unique_key_count_should_be_0`
- `media_server_duplicate_group_count_should_be_0`
- `video_group_relation_normalized_duplicate_group_count_should_be_0`
- `video_gateway_mapping_normalized_duplicate_group_count_should_be_0`
- `rule_trace_normalized_duplicate_group_count_should_be_0`

前四项对应计数为 `0` 时，脚本创建：

- `rule_groovy_script.uk_rule_groovy_script_identity`
- `video_media_server.uk_media_server_unique_host_http_port`

第五、六项用于检查可空通道标识。`video_device_group_relation.channel_identification` 的空值表示设备级分组关联，`video_gateway_mapping.src_channel_identification` 的空值表示设备级网关映射；两个字段继续保持可空。活动记录的生成列将空值或仅由普通半角空格组成的值归一为 `N:`，将去除首尾普通半角空格后的非空值归一为带 `V:` 前缀的内部键；制表符、全角空格等其他字符保留为标识内容。逻辑删除记录使用 `D:` 加主键形成隔离键，因此解绑或删除后仍可重新建立同一业务关系。应用无需读取或写入生成列。

第五、六项均为 `0` 时，脚本创建：

- `video_device_group_relation.uk_group_device_channel_key`
- `video_gateway_mapping.uk_src_device_channel_key`

新索引存在后，脚本才移除会被 `NULL` 绕过的旧索引 `uk_group_device_channel` 和 `uk_src_device_channel`。任一重复预检非 `0` 时，对应生成列仍会保留，新索引暂不创建，旧索引也不会移除。先根据记录 ID、创建时间和业务归属人工确认保留项，再重跑同一脚本；不要使用通用去重语句直接删除业务记录。

第七项用于检查桥接执行轨迹。`rule_bridge_execution_trace.bridge_rule_id` 在入站订阅轨迹中允许为空，生成列将空值归一为 `N:`，将任意非空规则 ID 转为带 `V:` 前缀的内部键，因此不会占用或碰撞任何合法 `BIGINT` 规则 ID。计数为 `0` 时创建 `uk_trace_rule_key(trace_id, bridge_rule_id_key)`，并在新索引定义确认正确后移除旧索引 `uk_trace_rule`。生成列只用于唯一约束，应用无需赋值。

如果库中已经存在同名但定义不同的生成列或索引，脚本不会覆盖该结构，也不会移除旧索引；执行后验证计数会显示未达到期望值。此时先用 `SHOW CREATE TABLE` 与当前基线比对，再由数据库管理员确认修正方式。

下面的查询可列出需要人工确认的重复组及记录 ID：

```sql
SELECT `group_id`, `device_identification`,
       CASE WHEN NULLIF(TRIM(`channel_identification`), '') IS NULL
            THEN 'N:' ELSE CONCAT('V:', TRIM(`channel_identification`)) END AS `channel_key`,
       GROUP_CONCAT(`id` ORDER BY `id`) AS `relation_ids`
FROM `video_device_group_relation`
WHERE `deleted` = 0
GROUP BY `group_id`, `device_identification`, `channel_key`
HAVING COUNT(*) > 1;

SELECT `src_protocol`, `src_device_identification`,
       CASE WHEN NULLIF(TRIM(`src_channel_identification`), '') IS NULL
            THEN 'N:' ELSE CONCAT('V:', TRIM(`src_channel_identification`)) END AS `channel_key`,
       GROUP_CONCAT(`id` ORDER BY `id`) AS `mapping_ids`
FROM `video_gateway_mapping`
WHERE `deleted` = 0
GROUP BY `src_protocol`, `src_device_identification`, `channel_key`
HAVING COUNT(*) > 1;

SELECT `trace_id`,
       CASE WHEN `bridge_rule_id` IS NULL THEN 'N:' ELSE CONCAT('V:', `bridge_rule_id`) END AS `bridge_rule_key`,
       GROUP_CONCAT(`id` ORDER BY `id`) AS `trace_record_ids`
FROM `rule_bridge_execution_trace`
GROUP BY `trace_id`, `bridge_rule_key`
HAVING COUNT(*) > 1;
```

如果其他唯一索引预检计数非 `0`，结构和无歧义回填仍会保留，但对应新唯一索引暂不创建。先依据旧字段和业务配置补齐空值、人工决定重复记录的保留方式，再重跑同一脚本。不要用删除重复行的通用语句代替业务确认。

## 执行后验证

保留完整终端输出，并确认以下结果：

- `new_table_count_should_be_23 = 23`
- `device_version_column_count_should_be_2 = 2`
- `product_version_backfill_mismatch_count_should_be_0 = 0`
- `legacy_column_count_should_be_10 = 10`
- `legacy_table_count_should_be_3 = 3`
- `video_encrypted_credential_column_count_should_be_3 = 3`
- `video_nullable_identity_key_column_count_should_be_2 = 2`
- `rule_trace_identity_key_column_count_should_be_1 = 1`
- `media_server_address_backfill_mismatch_count_should_be_0 = 0`
- `rule_script_known_mapping_mismatch_count_should_be_0 = 0`
- 前四个唯一键预检计数均为 `0` 后，`new_unique_index_count_should_be_2_when_prechecks_pass = 2`
- 两个视频归一重复预检均为 `0` 后，`video_normalized_unique_index_count_should_be_2_when_prechecks_pass = 2`、`video_legacy_nullable_unique_index_count_should_be_0_when_prechecks_pass = 0`
- 规则轨迹归一重复预检为 `0` 后，`rule_trace_normalized_unique_index_count_should_be_1_when_precheck_passes = 1`、`rule_trace_legacy_nullable_unique_index_count_should_be_0_when_precheck_passes = 0`

`product_version_too_long_manual_review_count` 不是固定值；大于 `0` 时逐条确认新版本编号。`video_plaintext_credential_count_manual_migration` 按“视频凭据加密迁移”一节处理并最终归零。随后核对产品发布、规则脚本加载、视频服务器连接和旧视频业务查询，再恢复服务。

## 回退

旧字段和三张旧业务表仍在，可用于应用版本回退时读取旧数据，但已新增的表、列和索引不会自动撤销。若验证失败，应保持服务停止，保存执行日志，使用执行前备份恢复整个租户库；不要只回滚部分 DDL。

新增脚本时读取产品清单中的当前组件版本，例如 `V1.4.1__extend_product_version_snapshot.sql`。历史脚本不随清单版本改名；同一版本存在多个脚本时，在本表中明确执行顺序。脚本不得写死某个租户库名，也不得加入租户账号、密码、地址、密钥或运行日志。
