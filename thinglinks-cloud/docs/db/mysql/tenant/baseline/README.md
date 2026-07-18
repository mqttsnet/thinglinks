# 租户库全量基线

本目录保存 `thinglinks_base_{tenantId}` 的当前全量初始化脚本，仓库中的脚本以内置租户 1 为基准。

## 版本记录

版本来源为 [`thinglinks-cloud/.thinglinks-product.env`](../../../../../.thinglinks-product.env) 的 `THINGLINKS_COMPONENT_VERSION`。

| 基线版本 | 文件 | 数据库 | 用途 |
| --- | --- | --- | --- |
| 1.4.0 | [`thinglinks_base_1.sql`](thinglinks_base_1.sql) | `thinglinks_base_1` | 初始化租户业务数据及 IoT 业务表 |

此处的 `1.4.0` 表示该全量脚本与 Cloud `1.4.0` 的最终租户库状态一致。Cloud 发布新版本时，应将全量脚本更新到新版本的最终状态，并同步修改本表和 SQL 文件头部的基线版本；旧租户库仍按 `migration/README.md` 记录的历史增量顺序逐库升级。

## 需要注意

- 脚本包含 `DROP TABLE IF EXISTS`，只用于新租户初始化或明确允许清空的租户库。
- 先导入 `default/baseline/thinglinks_ds_c_defaults.sql`，再导入本基线；末尾的角色资源完整性查询需要读取默认库 `def_resource`。
- 初始化当前版本时只执行本全量基线，不再执行同版本及更早的增量脚本。
- 租户创建流程使用 `thinglinks-public/thinglinks-tenant-datasource-init` 内置的 MySQL schema；该运行时副本与本基线同步维护，并由增量目录的静态校验脚本共同核对。
- 其他租户库应由租户初始化流程按对应 `tenantId` 创建，不要直接把租户 1 的业务数据复制到已有租户。
- 地图密钥使用占位值，启动相关功能前应替换为目标环境配置。
- 视频通道口令、通知渠道凭证和级联平台密码由应用加密写入，查询接口不回显原值。
- 视频设备分组关联和网关映射使用内部生成列归一可空通道标识，空通道仍表示设备级关联；逻辑删除记录按主键隔离，删除后可重新建立同一关系。生成列无需由应用赋值。
- 桥接执行轨迹使用内部生成列归一可空规则 ID，入站订阅轨迹的空规则 ID 仍保持原业务语义。
- 已有租户升级时，对每个租户库分别执行尚未执行的增量脚本并记录结果。
- 导入结束后确认 `base_role_resource_orphan_count_should_be_0 = 0`；非零表示角色引用了不存在或应用不匹配的默认库资源，不能直接启用该租户。
