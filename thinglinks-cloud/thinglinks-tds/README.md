# thinglinks-tds —— 时序数据库适配服务

> 物联网平台**时序数据(TDengine)代理 / 适配服务**。所有设备遥测数据(物模型解析后的 service / property)的**库 / 超级表 / 子表 DDL** 与**数据写入 / 时序查询**,统一通过本服务收口,屏蔽其他业务服务对 TDengine JDBC / REST 的直接依赖。
>
> **下游依赖**:TDengine(JDBC REST `TAOS-RS` 或 WebSocket `TAOS-WS`)。
> **主要调用方**:`thinglinks-mqs`(物模型解码后写时序)、`thinglinks-link`(产品物模型 DDL / 设备影子查询)。
>
> **本 README 是 tds 服务开发者唯一参考手册** ── 改 [TdsFacade](thinglinks-tds-facade/thinglinks-tds-api/src/main/java/com/mqttsnet/thinglinks/tds/facade/TdsFacade.java) / [TdsService](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/service/TdsService.java) 同步改本文。

---

## 🚀 快速导航

| 我想了解 / 改 | 跳到 | 关键入口文件 |
|---|---|---|
| **服务整体定位 / 5 子模块拆分** | [§1](#1-服务定位--5-子模块分工) | [pom.xml](pom.xml) |
| **TDengine 数据模型对照(Database / SuperTable / SubTable / Tag / Field)** | [§2](#2-tdengine-数据模型对照) | `basic.tds.utils.TdsUtils`(外部公共包) |
| **核心 Facade API 速查(DDL + DML + Query)** ⭐ | [§3](#3-核心-facade-api-速查) | [TdsFacade](thinglinks-tds-facade/thinglinks-tds-api/src/main/java/com/mqttsnet/thinglinks/tds/facade/TdsFacade.java) |
| **跨服务联动(mqs / link 怎么调 tds)** | [§4](#4-跨服务联动) | [DevicePayloadDecodeStage](../thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/stage/decode/DevicePayloadDecodeStage.java) |
| **TDengine 连接配置 / Nacos 配置** | [§5](#5-配置) | [database.yml](../docs/config/nacos/DEFAULT_GROUP/database.yml) |
| **常见排查路径(连接失败 / 表不存在 / 写入卡)** | [§6](#6-常见排查路径) | — |
| **文档维护索引** | [§7](#7-文档维护索引) | — |

---

## 1. 服务定位 / 5 子模块分工

| 维度 | 说明 |
|---|---|
| 服务职责 | 1) 屏蔽 TDengine 物理细节(库 / 超级表 / 子表 / 标签 / 字段 / 时序查询)<br>2) 通过 Facade 对外暴露**纯业务语义** API(`createSubTable` / `insertTableData` / `getDataInRangeOrLastRecord` 等)<br>3) 多租户:走 `EXTEND_TENANT` 动态数据源,按 `ContextUtil.getDataBase()` 切到当前租户的 TDengine 库 |
| **上游** | ① mqs 物模型解码后写时序(主流量)<br>② link 产品物模型 CRUD 时同步 DDL / 设备影子查询<br>③ Nacos 配置中心(连接串 / Druid 池) |
| **下游** | TDengine 集群(JDBC REST `TAOS-RS` 或 WebSocket `TAOS-WS`) |
| 启动入口 | [TdsServerApplication](thinglinks-tds-server/src/main/java/com/mqttsnet/thinglinks/TdsServerApplication.java) |
| 服务端口 | `18788`(见 [thinglinks-tds-server.yml](../docs/config/nacos/DEFAULT_GROUP/thinglinks-tds-server.yml)) |
| Spring app name | `thinglinks-tds-server` / 网关前缀 `/tds` |

### 1.1 5 个子模块分工

| 子模块 | 职责 | 包路径 / 关键类 |
|---|---|---|
| **[thinglinks-tds-entity](thinglinks-tds-entity/)** | DTO / 枚举 / VO(供 biz、controller、facade 共用) | [OperationEnum](thinglinks-tds-entity/src/main/java/com/mqttsnet/thinglinks/tds/enumeration/OperationEnum.java) `ADD/UPDATE/DELETE` ── 产品物模型属性变更类型<br>[SuperTableDescribeVO](thinglinks-tds-entity/src/main/java/com/mqttsnet/thinglinks/tds/vo/result/SuperTableDescribeVO.java) ── 超级表 / 子表结构描述(field / type / length / note) |
| **[thinglinks-tds-facade](thinglinks-tds-facade/)** ⭐ | 跨服务接口契约 + 两种调用实现(in-process boot / Feign cloud) | [TdsFacade](thinglinks-tds-facade/thinglinks-tds-api/src/main/java/com/mqttsnet/thinglinks/tds/facade/TdsFacade.java) ── API 接口<br>[boot-impl/TdsFacadeImpl](thinglinks-tds-facade/thinglinks-tds-boot-impl/src/main/java/com/mqttsnet/thinglinks/tds/facade/impl/TdsFacadeImpl.java) ── 进程内实现(直接调本地 `TdsService`)<br>[cloud-impl/TdsApi](thinglinks-tds-facade/thinglinks-tds-cloud-impl/src/main/java/com/mqttsnet/thinglinks/tds/api/TdsApi.java) ── Feign 客户端 + [TdsApiFallback](thinglinks-tds-facade/thinglinks-tds-cloud-impl/src/main/java/com/mqttsnet/thinglinks/tds/api/hystrix/TdsApiFallback.java) 熔断兜底 |
| **[thinglinks-tds-biz](thinglinks-tds-biz/)** | 业务实现 ── TDengine SQL 拼装 / 多租户切库 | [TdsService](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/service/TdsService.java) ── 服务接口(全 default 方法,子类按需覆写)<br>[TDengineServiceImpl](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/service/impl/TDengineServiceImpl.java) ── 注 `@DS(TDS_DEFAULTS)`,方法级 `@DS(EXTEND_TENANT)` 动态切租户库<br>[TdsSqlGuard](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/service/impl/TdsSqlGuard.java) ── TDengine 标识符 / 字段白名单校验,防止动态 SQL 注入<br>[TDengineMapper](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/mapper/TDengineMapper.java) ── MyBatis Mapper,直接发 TDengine SQL |
| **[thinglinks-tds-controller](thinglinks-tds-controller/)** | REST 控制器(本质是 Feign cloud-impl 的服务端) | [TDengineController](thinglinks-tds-controller/src/main/java/com/mqttsnet/thinglinks/tds/controller/TDengineController.java) ── `/tds/**` 鉴权接口<br>[TdsInnerController](thinglinks-tds-controller/src/main/java/com/mqttsnet/thinglinks/inner/controller/TdsInnerController.java) ── `/inner/tds/**` 服务间内部接口,供 Feign / Nacos 直连调用;经网关命中 `/inner/**` 会被拒绝 |
| **[thinglinks-tds-server](thinglinks-tds-server/)** | Spring Boot 启动入口 + Nacos 配置 + Feign 客户端注册 | [TdsServerApplication](thinglinks-tds-server/src/main/java/com/mqttsnet/thinglinks/TdsServerApplication.java) |

**依赖关系**:`server → controller → biz → entity`;`facade-api ← entity`;`boot-impl → biz`;`cloud-impl → 仅依赖 facade-api`。

> **Facade 双实现策略**:其他服务如果**和 tds 同进程**(单体部署)可直接装 [boot-impl/TdsFacadeImpl](thinglinks-tds-facade/thinglinks-tds-boot-impl/src/main/java/com/mqttsnet/thinglinks/tds/facade/impl/TdsFacadeImpl.java);**分布式部署**装 [cloud-impl/TdsApi](thinglinks-tds-facade/thinglinks-tds-cloud-impl/src/main/java/com/mqttsnet/thinglinks/tds/api/TdsApi.java) ── FeignClient path 为 `/inner/tds`,调用方只看到统一的 `TdsFacade` 接口,无感切换。

---

## 2. TDengine 数据模型对照

TDengine 是时序专用列存储数据库,其逻辑层次和关系库截然不同。tds 服务的所有 API 都围绕下表 5 个概念展开:

| TDengine 概念 | 物联网平台映射 | 在 tds 服务中的入口 |
|---|---|---|
| **Database** | 每个**租户**一个独立时序库(命名前缀 `thinglinks_extend_*`),按租户 schema 隔离 | `createDatabase(dataBaseName)` |
| **SuperTable**(STable) | 一类**同型设备**的模板(每个产品的某个 service 对应一张超级表),定义 `Field` schema + `Tag` schema | `createSuperTable` / `createSuperTableAndColumn` / `dropSuperTable` |
| **SubTable** | 单个**具体设备**的时序表(每个设备 + service 对应一张子表),继承自父超级表,标签值在创建时固化 | `createSubTable` |
| **Tag**(标签列) | 设备维度(如 `device_identification`),用于按设备过滤查询;改 Tag 不写新行 | `alterSuperTableTag` / `dropSuperTableTag` / `alterSuperTableTagRename` |
| **Field**(普通列) | 时序数值列(温度 / 湿度 / 状态等),每次写入都是一行新数据,首列必须是 `ts`(纳秒时间戳) | `alterSuperTableColumn` / `dropSuperTableColumn` / `insertTableData` |

### 2.1 命名约定(由 `basic.tds.utils.TdsUtils` 统一生成)

| 类型 | 命名规则 | 示例 |
|---|---|---|
| Database | `thinglinks_extend_{tenantCode}` | `thinglinks_extend_default` |
| SuperTable | `super_{productType}_{productIdentification}_{serviceCode}` | `super_directlyconnected_p1001_temperature` |
| SubTable | `{superTableName}_{deviceIdentification}`(超长时哈希) | `super_..._temperature_dev001` |
| Tag | `device_identification`(默认必带) | — |
| Field(首列) | `ts` 纳秒精度(`precision 'ns'`) | — |

### 2.2 设备数据写入路径

```
mqs DevicePayloadDecodeStage
   ├─ 解析物模型 service → 计算 superTableName / subTableName
   ├─ tdsApi.describeSuperOrSubTable(subTableName) ── 检查子表是否存在
   ├─ 不存在 → tdsApi.createSubTable(TableDTO{tagsFieldValues=device_identification})
   ├─ 组装 TableDTO{fieldValues = [ts=nano, field1=v1, field2=v2 ...]}
   └─ tdsApi.insertTableData(tableDTO) ── 落 TDengine
```

> 子表"按需懒建":首次写入某设备时由 mqs 触发,结构信息写 Redis ([LinkCacheDataHelper](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/helper/LinkCacheDataHelper.java) `getProductModelSuperTableCacheVO`),避免每次写入都查 TDengine schema。

---

## 3. 核心 Facade API 速查

所有方法定义见 [TdsFacade](thinglinks-tds-facade/thinglinks-tds-api/src/main/java/com/mqttsnet/thinglinks/tds/facade/TdsFacade.java),返回类型统一为 `R` / `R<T>`,失败自动 `R.fail(e.getMessage())`,超时走 [TdsApiFallback.timeout()](thinglinks-tds-facade/thinglinks-tds-cloud-impl/src/main/java/com/mqttsnet/thinglinks/tds/api/hystrix/TdsApiFallback.java)。

### 3.1 库管理(Database DDL)

| 方法 | 入参 | 用途 |
|---|---|---|
| `createDatabase(String dataBaseName)` | 库名 | 新租户开通时调用,创建独立时序库;建库选项见 `tdengineCreateDatabaseOptions`(默认 `vgroups 10 buffer 10 precision 'ns'`) |

### 3.2 超级表管理(SuperTable DDL)

| 方法 | 入参 | 用途 |
|---|---|---|
| `createSuperTable(String superTableName)` | 表名 | 仅建空骨架(不含字段),少用 |
| `createSuperTableAndColumn(SuperTableDTO)` ⭐ | 含 `dataBaseName` / `superTableName` / `fields` / `tagsFieldValues` | **主用** ── 产品物模型创建时由 link 调用 |
| `createSuperTableAndColumnTwo(JSONObject)` | 物模型原始 JSON | 兼容旧版 ── 内部通过 `TdsUtils.handleSuperTable` 解析后批量建表 |
| `dropSuperTable(String superTableName)` | 表名 | 产品删除时调用 |
| `alterSuperTableColumn(SuperTableDTO)` | 含新增字段列表 | 物模型新增属性 |
| `dropSuperTableColumn(SuperTableDTO)` | 含待删字段 | 物模型删除属性 |
| `alterSuperTableTag(SuperTableDTO)` | 含新增标签 | 加设备维度 |
| `dropSuperTableTag(SuperTableDTO)` | 含待删标签 | 减设备维度 |
| `alterSuperTableTagRename(superTableName, oldName, newName)` | 表名 + 标签新旧名 | 重命名标签 |

### 3.3 子表管理(SubTable DDL)

| 方法 | 入参 | 用途 |
|---|---|---|
| `createSubTable(TableDTO)` ⭐ | 含 `superTableName` / `tableName` / `tagsFieldValues` | **主用** ── 首次写入某设备数据时由 mqs 触发 |
| `createSubTableTwo(JSONObject)` | 子表原始 JSON | 兼容旧版 ── `TdsUtils.handleSubTable` 批量建子表 |

### 3.4 元数据查询(Describe)

| 方法 | 入参 | 返回 |
|---|---|---|
| `describeSuperOrSubTable(String tableName)` ⭐ | 表名(超级表或子表通用) | `R<List<SuperTableDescribeVO>>` ── 字段名 / 类型 / 长度 / note(`TAG` 标识标签列) |

> mqs 用这个方法判断子表是否存在(`getData()` 非空即视为存在),结果会被缓存到 Redis,避免高频 schema 查询。

### 3.5 数据写入与查询(DML / Query)

| 方法 | 入参 | 返回 / 用途 |
|---|---|---|
| `insertTableData(TableDTO)` ⭐ | 含 `tableName` / `fieldValues`(首字段 `ts` 纳秒) | **设备遥测主写入路径** ── 走子表 |
| `getDataInRangeOrLastRecord(tableName, startTime, endTime)` ⭐ | 表名 + 起止纳秒时间戳(可空) | `R<List<Map<String,Object>>>` ── 时间窗口内全部记录;**两个时间均空时回退查最后一条** ── 设备影子用这个查最新值 |

> 写入是同步阻塞(Mapper 单条 INSERT);如需批量请在调用方按 `subTable` 聚合后调多次,或扩展 Mapper 增加批量 SQL。

---

## 4. 跨服务联动

### 4.1 mqs ── 设备数据写入(主流量,在线路径)

| 触发点 | 调用 | 文件 |
|---|---|---|
| 协议总线 CORE 阶段,PUBLISH 报文物模型解码后 | `tdsApi.describeSuperOrSubTable` → `createSubTable`(按需) → `insertTableData` | [DevicePayloadDecodeStage](../thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/stage/decode/DevicePayloadDecodeStage.java)<br>[DeviceDataProcessingServiceImpl](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/service/impl/DeviceDataProcessingServiceImpl.java) `L102 / L119 / L194` |

mqs 注入 `private final TdsFacade tdsApi;` ── 看到的是统一 Facade,实际由 Spring 装配 cloud-impl 走 Feign。详细链路见 [`thinglinks-mqs/README.md §7`](../thinglinks-mqs/README.md)。

### 4.2 link ── 物模型 DDL(产品配置路径)

| 触发点 | 调用 | 文件 |
|---|---|---|
| 产品版本发布 / 回滚,需要同步建 / 改 / 删超级表 | `createSuperTableAndColumn` / `describeSuperOrSubTable` / `dropSuperTable` | [ProductVersionPublishOrchestrator](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productversion/publish/orchestrator/ProductVersionPublishOrchestrator.java) |
| 设备影子查询(最新值 / 时间范围) | `getDataInRangeOrLastRecord(subTableName, start, end)` | [DeviceShadowServiceImpl](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceShadowServiceImpl.java) `L125` |

详细的设备元数据流转见 [`thinglinks-link/README.md`](../thinglinks-link/README.md)。

### 4.3 调用方装配指引

```
其他服务 pom.xml ──┬── thinglinks-tds-api(契约,必装)
                  └── thinglinks-tds-cloud-impl(Feign,分布式部署装这个)
                      或 thinglinks-tds-boot-impl(同进程,单体部署装这个)

代码:    @Autowired private TdsFacade tdsApi;  // 接口不变,实现按部署形态切换
```

---

## 5. 配置

### 5.1 服务自身([application.yml](thinglinks-tds-server/src/main/resources/application.yml))

| 配置项 | 值 | 说明 |
|---|---|---|
| `spring.application.name` | `thinglinks-tds-server`(由 `@project.artifactId@` 注入) | Nacos 服务名 |
| `spring.application.path` | `/tds` | 网关前缀,需与网关路由保持一致 |
| `server.port` | `18788`(在 Nacos `thinglinks-tds-server.yml`) | — |
| Nacos imports | `common.yml` / `redis.yml` / `database.yml` / `rocketmq.yml` / `thinglinks-tds-server.yml` | 配置中心拉取 |

### 5.2 TDengine 连接([database.yml](../docs/config/nacos/DEFAULT_GROUP/database.yml))

```yaml
thinglinks:
  taosdata:
    username: ${TDENGINE_USERNAME:}
    password: ${TDENGINE_PASSWORD:}
    driverClassName: com.taosdata.jdbc.rs.RestfulDriver         # JDBC REST 模式(默认)
    url: jdbc:TAOS-RS://127.0.0.1:6041/thinglinks_ds_c_defaults # 默认库,租户库通过 EXTEND_TENANT 切换
    # 推荐 WebSocket 模式(切换 driver/url 注释行)
  database:
    extendSqlParameters:
      tdengineCreateDatabaseOptions: vgroups 10 buffer 10 precision 'ns'  # createDatabase 时附加
```

| 数据源标识 | 来源 | 说明 |
|---|---|---|
| `TDS_DEFAULTS = "1"` | [DsConstant](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/constant/DsConstant.java) | 类级 `@DS` 默认数据源 ── 公共 / 不切租户场景 |
| `EXTEND_TENANT = "#thread.xxx"` | 同上 | 方法级 `@DS` 表达式 ── 按当前线程上下文切到租户的 TDengine 库 |

> 所有写入 / 查询方法都标了 `@DS(EXTEND_TENANT)`,确保多租户场景下命中正确的 TDengine 库;切换由 `dynamic-datasource` + `ContextUtil.getDataBase()` 完成。

---

## 6. 常见排查路径

| 现象 | 排查点 |
|---|---|
| **服务启动报 `No DataSource registered for "1"`** | 检查 [database.yml](../docs/config/nacos/DEFAULT_GROUP/database.yml) `spring.datasource.dynamic.datasource."1"` 是否完整;检查 taosdata 驱动 jar 是否在 classpath |
| **TDengine 连接失败 / `Connection refused`** | 1) 测 `curl http://{tdHost}:6041/rest/sql -uroot:taosdata -d "show databases"`<br>2) 检查 `jdbc:TAOS-RS://...` 主机端口 与 taosadapter 实际监听一致<br>3) WebSocket 模式需 taosd ≥ 3.0 |
| **`Table does not exist` 写入失败** | 1) 检查 `subTableName` 命名是否一致(mqs 用 `TdsUtils.subTableName`,租户切换是否生效)<br>2) 调用 `describeSuperOrSubTable(superTableName)` 验证超级表已建<br>3) Redis 缓存的 `productModelSuperTableCacheVO` 可能过期,清后重试 |
| **写入很慢 / `insertTableData` 超时** | 1) Feign 熔断走 [TdsApiFallback](thinglinks-tds-facade/thinglinks-tds-cloud-impl/src/main/java/com/mqttsnet/thinglinks/tds/api/hystrix/TdsApiFallback.java) 返回 `R.timeout()`,日志会有 `tds-server` Hystrix 超时<br>2) Druid 池监控 `/druid/datasource.html`(REST 模式 `maxActive` 200 是否打满)<br>3) 单条 INSERT 性能瓶颈 → 评估改造批量 / 行协议(schemaless) |
| **多租户写到了错误的库** | 1) 检查上游 Feign 调用时 `TenantId` header 是否带<br>2) `ContextUtil.getDataBase()` 返回值是否正确 ── 在 [TDengineServiceImpl](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/service/impl/TDengineServiceImpl.java) 方法入口打印<br>3) `@DS(EXTEND_TENANT)` 是否被覆盖 |
| **`createSuperTable` 报字段已存在** | 时序库同名字段不可重复 alter,需先 `dropSuperTableColumn` 再 `alterSuperTableColumn`;或检查 [ProductVersionPublishOrchestrator](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productversion/publish/orchestrator/ProductVersionPublishOrchestrator.java) 中的建表 diff 逻辑 |

---

## 7. 文档维护索引

| 改了什么 | 必须同步 |
|---|---|
| 新增 / 修改 [TdsFacade](thinglinks-tds-facade/thinglinks-tds-api/src/main/java/com/mqttsnet/thinglinks/tds/facade/TdsFacade.java) 方法 | 1) §3 API 速查表<br>2) cloud-impl [TdsApi](thinglinks-tds-facade/thinglinks-tds-cloud-impl/src/main/java/com/mqttsnet/thinglinks/tds/api/TdsApi.java) 同步 Feign 签名<br>3) [TdsApiFallback](thinglinks-tds-facade/thinglinks-tds-cloud-impl/src/main/java/com/mqttsnet/thinglinks/tds/api/hystrix/TdsApiFallback.java) 加熔断兜底<br>4) [TdsFacadeImpl](thinglinks-tds-facade/thinglinks-tds-boot-impl/src/main/java/com/mqttsnet/thinglinks/tds/facade/impl/TdsFacadeImpl.java) 加 boot 实现<br>5) [TDengineController](thinglinks-tds-controller/src/main/java/com/mqttsnet/thinglinks/tds/controller/TDengineController.java) + [TdsInnerController](thinglinks-tds-controller/src/main/java/com/mqttsnet/thinglinks/inner/controller/TdsInnerController.java) 加 REST 端点 |
| 新增 / 修改 [TdsService](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/service/TdsService.java) 方法 | 同步加 default 方法 + [TDengineServiceImpl](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/service/impl/TDengineServiceImpl.java) 实现 + [TDengineMapper](thinglinks-tds-biz/src/main/java/com/mqttsnet/thinglinks/tds/mapper/TDengineMapper.java) SQL |
| 修改 TDengine 数据模型(库名 / 超级表 / 子表命名规则) | §2 表格 + `basic.tds.utils.TdsUtils` 命名工具 + mqs / link 调用方代码 |
| 修改 [database.yml](../docs/config/nacos/DEFAULT_GROUP/database.yml) TDengine 连接 | §5 配置表 |
| 调用方新增(新服务依赖 tds) | §4 跨服务联动表 + 该服务 README |

### 相关 README

- [thinglinks-mqs/README.md](../thinglinks-mqs/README.md) ── **主调用方**,物模型解码后通过 `TdsFacade` 写时序
- [thinglinks-link/README.md](../thinglinks-link/README.md) ── 设备元数据 / 产品物模型,DDL 与设备影子查询调用方
- [thinglinks-broker/README.md](../thinglinks-broker/README.md) / [thinglinks-rule/README.md](../thinglinks-rule/README.md) ── 同级微服务,不直接依赖 tds
