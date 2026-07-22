# BifroMQ Setting Provider Plugin

BifroMQ 定义了一类租户级设置 (Settings)，允许在运行时修改，以实现针对单个租户动态调整 BifroMQ 的服务行为。Setting Provider
Plugin 的目的是在运行时为这些设置提供自定义值。

## 目录结构

```plaintext
bifromq-setting-provider-plugin/
├── setting-provider/ <-- setting provider module as a reference for other bifromq plugin, you can remove it if not needed
│   └── src/
│       └── main/
│           └── java/
│               └── com.yourcompany.newproject/
│                   └── YourPluginClassNameSettingProvider.java
├── plugin-build/  <-- plugin-build module to build the plugin zip file
│   ├── assembly/
│   │   └── assembly-zip.xml
│   ├── conf/      <-- folder to contain plugin configuration files
│   │   ├── config.yaml <-- plugin configuration file
│   │   └── logback.xml <-- logback configuration file for the plugin
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com.yourcompany.newproject/
│   │               └── YourPluginClassName.java <-- Your plugin main class
│   └── target/
│       └── pom.xml
├── plugin-context/  <-- plugin-context module to define the plugin context
│   └── src/
│       └── main/
│           └── java/
│               └── com.yourcompany.newproject/
│                   └── YourPluginContextClassName.java
└── pom.xml
```

## 功能特性

- **动态设置**：支持在运行时动态提供和修改租户级设置。
- **配置驱动**：支持通过 `config.yaml` 进行动态配置。
- **日志管理**：集成 Logback 日志框架，通过 `logback.xml` 进行日志级别和格式的配置。

## 快速开始

### 1. 配置文件设置

在 plugin-context 模块`conf/config.yaml` 中定义认证和授权配置，具体配置项如下：

```yaml
settingProviderConfig:
  # 自定义设置键值对
  customSettingKey: "customValue"
```

### 2. 构建和打包

通过 Maven 构建并打包插件：

```bash
mvn clean package
```

构建成功后，插件包将生成在 `target/` 目录中，例如 `target/bifromq-setting-provider-plugin-1.0.4.zip`。

### 3. 部署和加载插件

将插件包上传到 BifroMQ 插件目录（假设目录为 `/home/bifromq/plugins`）：

然后启动 BifroMQ 服务，插件将自动加载。

在 BifroMQ 中运行此插件时，需要通过配置文件指定 Setting Provider 的完全限定类名（FQN）。请注意，BifroMQ 一次只允许运行一个
Setting Provider 实例。

在 BifroMQ 配置文件 `standalone.yml` 中添加以下内容：

```yaml
settingProviderFQN: com.mqttsnet.thinglinks.BifromqSettingProviderPluginSettingProvider
```

### 4. ⚠️ 让插件配置生效(必做,否则等于没装)

**必须在 BifroMQ JVM 启动参数加 `-Dsetting_provide_init_value=true`。** 在 `bin/bifromq-start.sh`(约第 160 行,`EXTRA_JVM_OPTS` 定义之后)追加:

```bash
EXTRA_JVM_OPTS="$EXTRA_JVM_OPTS -Dsetting_provide_init_value=true"
```

#### 为什么必须加

| `setting_provide_init_value` | cache miss 时的行为 | 本插件配置 |
|------------------------------|--------------------|----------|
| `false`(BifroMQ 默认) | 直接用内核**初值**,**不调** `provide()` | 全部被内核默认值覆盖(等于没配) |
| `true` | 回调本插件 `provide()` 取值 | `debugModeEnabled / maxTopicFiltersPerSub / maxTopicFiltersPerInbox` 真正生效 |

不加这个参数最典型的坑:`DebugModeEnabled` 实际为 `false` → BifroMQ 内核不采集 `PingReq` 高频事件 → **设备心跳链路断在源头**(event-collector 收不到 `PING_REQ`,下游 mqs 写不了 `last_heartbeat_time`)。

#### 验证

```bash
# 设备连上并经过一个 keepalive 周期后,event-collector 日志应出现 PING_REQ
grep "Received event type=PING_REQ" logs/bifromq-event-collector-plugin/info.log
```

> 注意:启动日志里 `Setting: DebugModeEnabled=false` 打印的是**内核初值**,开 `setting_provide_init_value` 后该行仍可能显示 false(它不是 cache 实际值)。判断成败请直接看有没有 `PING_REQ` 事件,别看这行。

#### 单项硬覆盖(可选)

只想覆盖个别 Setting、不走插件时,可用同名 JVM 参数直接覆盖其初值,如 `-DMaxTopicLength=128`、`-DDebugModeEnabled=true`(官方机制,见[文档](https://bifromq.apache.org/docs/plugin/setting_provider/tenantsetting/))。但这只改单项且绕过插件,推荐仍以 `setting_provide_init_value=true` 让整套插件配置统一生效。

## 使用说明

### 代码示例

#### 初始化认证上下文

在插件初始化时，通过 `BifromqSettingProviderContext` 加载配置：

```java
public class BifromqSettingProviderContext {
    private final String customSetting;

    public BifromqSettingProviderContext(BifromqSettingProviderContext context) {
        this.customSetting = context.getPluginConfig().getSettingProviderConfig().getCustomSettingKey();
        // 初始化其他配置项
    }
}

```

## 常见问题

### 如何在不重启 BifroMQ 的情况下更新配置？

可以直接修改 BifroMQ 配置文件 `standalone.yml` 中的配置项，并重启插件实现动态更新。
程序会自动覆盖模块 `conf/config.yaml` 中定义的配置（standalone.yml 优先级高于 config.yaml）

### 如何启用详细日志？

修改 `logback.xml` 中的 `<level>` 标签，将日志级别设置为 `DEBUG`，然后重启插件。

### `provide` 方法里能查数据库 / 调远程吗？

**绝对不行。** 开启 `setting_provide_init_value=true` 后,`provide` 由 BifroMQ 工作线程在 cache miss 时**同步调用**,必须只读内存配置后立即返回。放 DB / RPC / 缓存查询会阻塞工作线程、拖垮整个 broker(官方明确警告)。无法快速判定时 `return null`,让该 Setting 维持当前值,把决策逻辑异步化。

## 4.0 升级备注

| 变更 | 说明 |
|------|------|
| `-Dsetting_provide_init_value=true` 机制 | 4.0.0-incubating **仍适用**,无替代方案 |
| `-D<SettingName>` 覆盖初值机制 | 4.0 **仍适用**(官方例:`-DMQTT3Enabled=false`) |
| 包路径 | `com.baidu.bifromq` → `org.apache.bifromq`,全局 import 替换 |
| Maven 依赖 | `groupId` 改 `org.apache.bifromq`,`version` → `4.0.0-incubating` |
| 兼容性 | 4.0 不向后兼容,按 major 升级对待 |

## 贡献

欢迎提出 issue 和 pull request 以改进插件功能。如有疑问，请联系项目维护社区 MQTTSNET。

--- 
