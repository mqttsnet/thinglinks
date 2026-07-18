# 默认库增量脚本

本目录保存 `thinglinks_ds_c_defaults` 的顺序增量脚本。

## 版本记录

版本来源为 [`thinglinks-cloud/.thinglinks-product.env`](../../../../../.thinglinks-product.env) 的 `THINGLINKS_COMPONENT_VERSION`。本目录当前记录版本为 `1.4.0`。

| 执行顺序 | 版本 | 文件 | 用途 |
| --- | --- | --- | --- |
| 1 | 1.4.0 | [`V1.4.0__register_rule_notification_apis.sql`](V1.4.0__register_rule_notification_apis.sql) | 注册规则通知变量与预览接口 |
| 2 | 1.4.0 | [`V1.4.0__rename_web_oauth_client.sql`](V1.4.0__rename_web_oauth_client.sql) | 统一 Web OAuth 客户端标识 |
| 3 | 1.4.0 | [`V1.4.0__sync_dictionary_and_resources.sql`](V1.4.0__sync_dictionary_and_resources.sql) | 同步字典和资源，清理已知废弃关系，中性化开发工具链接 |
| 4 | 1.4.0 | [`V1.4.0__sanitize_seed_data.sql`](V1.4.0__sanitize_seed_data.sql) | 清理历史演示种子、固定凭据和私有环境地址 |

新增脚本时读取产品清单中的当前组件版本，例如当前版本下可增加：

```text
V1.4.0__add_device_status_dictionary.sql
```

组件版本更新为 `1.4.1` 后，新脚本示例为 `V1.4.1__register_new_resource_api.sql`。历史脚本继续保留 `V1.4.0`，不随清单版本改名。同一版本存在多个脚本时，以本表记录的顺序执行。

## 需要注意

- 首次部署当前版本时只执行 `baseline/thinglinks_ds_c_defaults.sql`，不再执行本目录的同版本增量脚本。
- 每个脚本只处理一个逻辑变更，并在开头写明前置条件、结尾提供验证 SQL。
- 按版本和本页记录的顺序执行；已发布脚本不要修改、改名或删除，修正时增加新脚本。
- 规则通知接口已存在时只同步同方法、同 URI 的受管元数据并保留原主键；重复端点、预留主键占用或目标资源缺失会中止事务。
- OAuth 脚本只会迁移 ID、客户端标识、客户端密钥和名称均符合历史初始化值的记录；自定义客户端不会被改写，目标客户端标识已由其他记录占用时会中止事务。
- 迁移后的 `thinglinks_web` 客户端参数用于本地初始化和公开浏览器客户端兼容，不具备保密性；Cloud、Web 和 Visualize 必须保持一致，正式环境的安全边界不能依赖浏览器端客户端密钥。
- 字典与资源脚本先写入临时表，校验主键和业务唯一键身份后 UPSERT，所有真实表 DML 位于同一事务内，成功执行后可重复运行。
- 已废弃字典仅在 ID、父级 ID、父级键和字典键均符合历史基线时删除；相同 ID 已被调整为其他用途的记录保持不变。
- 该脚本只处理上一版基线中可确认身份的 37 条废弃资源；必须同时匹配 ID、应用 ID、资源编码和父资源 ID，才会清理其 `def_resource_api`、`def_tenant_resource_rel` 引用并将对应 `def_gen_table.menu_parent_id` 置空，不将旧关系猜测绑定到新资源。
- 开发工具链接只在命中三个确定的历史绝对地址时，分别更新为 `/api/doc.html`、`/druid/index.html` 和 `/monitor/thinglinks-monitor-server/applications`；其他自定义地址保持不变。
- 请使用遇错立即停止的客户端执行，不要使用 `--force`。任何失败门禁报错后立即执行 `ROLLBACK`、关闭当前连接，核对冲突或孤儿数据后再在新连接重试。
- 修改 `def_resource_api` 后，调用 `POST /system/defResource/clearCache?applicationId=3`，等待网关刷新或重启网关。
- 第 3 个脚本涉及应用 `1`、`2`、`3`、`403369958207127552`；执行后应分别刷新这些应用的资源缓存，或统一重启网关。
- 第 4 个脚本只在主键和历史身份同时匹配时处理记录；令牌、密码和应用密钥使用不可逆摘要识别，部署方已修改的同主键数据会保留并在末尾验证中显示。
- 第 4 个脚本中的 `pro-test`、`thinglinks.mqttsnet.com` 等地址只作为历史旧值的精确匹配条件，用于中性化存量数据，不是当前产品标识或默认配置。
- 能安全回退的变更可增加同版本 `U1.4.0__description.sql`；不能安全回退时写明人工恢复步骤。
- 执行前备份 `thinglinks_ds_c_defaults`，并记录执行时间、脚本编号和验证结果。
