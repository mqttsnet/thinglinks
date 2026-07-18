# 默认库全量基线

本目录保存 `thinglinks_ds_c_defaults` 的当前全量初始化脚本。

## 版本记录

版本来源为 [`thinglinks-cloud/.thinglinks-product.env`](../../../../../.thinglinks-product.env) 的 `THINGLINKS_COMPONENT_VERSION`。

| 基线版本 | 文件 | 数据库 | 用途 |
| --- | --- | --- | --- |
| 1.4.0 | [`thinglinks_ds_c_defaults.sql`](thinglinks_ds_c_defaults.sql) | `thinglinks_ds_c_defaults` | 初始化平台公共配置、权限、字典及租户元数据 |

此处的 `1.4.0` 表示该全量脚本与 Cloud `1.4.0` 的最终数据库状态一致。Cloud 发布新版本时，应将全量脚本更新到新版本的最终状态，并同步修改本表和 SQL 文件头部的基线版本；旧环境仍按 `migration/README.md` 记录的历史增量顺序升级。

## 需要注意

- 脚本包含 `DROP TABLE IF EXISTS`，只用于新环境初始化或明确允许清空的数据库。
- 初始化当前版本时只执行本全量基线，不再执行同版本及更早的增量脚本。
- 管理员密码、应用密钥、数据源密码、地图密钥及外部通知渠道凭据均使用占位值，启动服务前应替换为目标环境配置。
- 高德地图保留 4 个独立占位符：`__AMAP_JS_API_KEY__`、`__AMAP_SECURITY_JS_CODE__`、`__AMAP_WEB_SERVICE_API_KEY__`、`__AMAP_WEB_SERVICE_SIGN_KEY__`。
- 基线末尾的三项关系完整性查询应全部返回 `0`；非 `0` 时不应启动服务。
- 内置租户的联系电话、邮箱和地址保持为空，由部署方初始化后维护。
- 登录日志、通知发送记录、作业节点和 SOP 私钥不属于初始化数据，不写入全量基线。
- SOP 签名密钥应在目标环境生成或导入，不得提交真实私钥到仓库。

## 外部配置占位符

以下值只说明配置用途，不是可直接使用的账号或密钥：

| 配置维度 | 占位符 |
| --- | --- |
| 管理员登录 | `__ADMIN_PASSWORD_SHA256__`、`__ADMIN_SALT__` |
| 内置应用密钥 | `__BASIC_PLATFORM_APP_SECRET__`、`__DEV_OPERATION_APP_SECRET__`、`__IOT_SYSTEM_APP_SECRET__`、`__IOT_CARD_SYSTEM_APP_SECRET__`、`__VISUALIZATION_SYSTEM_APP_SECRET__`、`__VIDEO_SYSTEM_APP_SECRET__`、`__IOT_MOBILE_APP_SECRET__` |
| MySQL / TDengine | `__THINGLINKS_MYSQL_PASSWORD__`、`__THINGLINKS_TDENGINE_PASSWORD__` |
| 高德地图 | `__AMAP_JS_API_KEY__`、`__AMAP_SECURITY_JS_CODE__`、`__AMAP_WEB_SERVICE_API_KEY__`、`__AMAP_WEB_SERVICE_SIGN_KEY__` |
| 253 短信 | `__SMS_253_ACCOUNT__`、`__SMS_253_PASSWORD__` |
| 阿里云短信 | `__ALIYUN_SMS_ACCESS_KEY_ID__`、`__ALIYUN_SMS_ACCESS_KEY_SECRET__` |
| 百度云短信 | `__BAIDU_SMS_ACCESS_KEY_ID__`、`__BAIDU_SMS_SECRET_KEY__` |
| 腾讯云短信 | `__TENCENT_SMS_SDK_APP_ID__`、`__TENCENT_CLOUD_SECRET_ID__`、`__TENCENT_CLOUD_SECRET_KEY__` |
| 钉钉机器人 | `__DINGTALK_BOT_TOKEN__`、`__DINGTALK_BOT_SECRET__` |
| 企业微信机器人 | `__WECHAT_WORK_BOT_TOKEN__` |
| 飞书机器人及应用 | `__FEISHU_BOT_TOKEN__`、`__FEISHU_APP_ID__`、`__FEISHU_APP_SECRET__` |
| SMTP 邮件 | `__SMTP_USERNAME__`、`__SMTP_PASSWORD__`、`__SMTP_FROM_EMAIL__` |
| 开放平台 | `__SOP_OPEN_PRODUCTION_URL__`、`__SOP_OPEN_SANDBOX_URL__`、`__TORNA_SERVER_URL__` |
| 运维控制台 | `__NACOS_CONSOLE_URL__`、`__SKYWALKING_CONSOLE_URL__`、`__MINIO_CONSOLE_URL__`、`__JOB_ADMIN_URL__`、`__FILE_PREVIEW_URL__`、`__JENKINS_URL__`、`__SCADA_URL__`、`__NODERED_MANAGER_URL__`、`__FUXA_URL__` |

导入后可在平台对应的接口配置中替换这些值。正式环境应使用独立账号，并按服务商要求限制权限和来源地址。

## 初始化管理员密码

导入基线后，在启动 Cloud 前为管理员设置实际密码。下面语句在数据库会话中生成随机盐并写入 SHA-256 密码摘要；先把 `CHANGE_ME_STRONG_PASSWORD` 改成目标环境的强密码：

```sql
SET @admin_password = 'CHANGE_ME_STRONG_PASSWORD';
SET @admin_salt = LEFT(REPLACE(UUID(), '-', ''), 20);
UPDATE def_user
SET password = SHA2(CONCAT(@admin_password, @admin_salt), 256),
    salt = @admin_salt
WHERE id = 1452186486253289472;
SET @admin_password = NULL;
SET @admin_salt = NULL;
```

同时为 7 个内置应用生成各自独立的随机密钥，不要让占位值进入运行环境。配置完成后执行：

```bash
GIT_ROOT="$(git rev-parse --show-toplevel)"
CLOUD_ROOT="$GIT_ROOT"
if [ -d "$GIT_ROOT/thinglinks-cloud/docs/db/mysql" ]; then CLOUD_ROOT="$GIT_ROOT/thinglinks-cloud"; fi
"$CLOUD_ROOT/docs/db/mysql/default/migration/verify-default-database.sh"
```
