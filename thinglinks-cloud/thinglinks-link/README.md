# thinglinks-link —— IoT 核心域服务

> 物联网平台**核心元数据 + 业务域服务**。所有 IoT 实体的"事实真相"(devices / products / 物模型 / OTA / 证书 / ACL / 仪表板)都在本服务持久化与建模;5 个微服务里**域最多、表最多、被调用最频繁**。
>
> **本 README 是 link 服务开发者唯一参考手册** ── 改代码同步改 README,不要让文档脱节。

---

## 🚀 快速导航

| 我想了解 / 改 | 跳到 | 关键入口 |
|---|---|---|
| **服务整体定位 / 5 子模块分工** | [§1](#1-服务定位--5-子模块分工) | [pom.xml](pom.xml) / [LinkServerApplication](thinglinks-link-server/src/main/java/com/mqttsnet/thinglinks/LinkServerApplication.java) |
| **业务大维度俯瞰图(七大域)** | [§2](#2-业务大维度俯瞰图) | — |
| **设备域(Device / Action / ACL / Shadow / Location / Group / Sync)** | [§3](#3-设备域-device) | [DeviceService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceService.java) |
| **产品域(Product / 物模型 properties services commands / Topic / Cmd Req/Resp)** | [§4](#4-产品域-product--物模型) | [ProductService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/service/ProductService.java) |
| **OTA 升级(包/任务/记录/目标 + 状态机)** | [§5](#5-ota-升级) | [OtaUpgradeStateMachine](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/OtaUpgradeStateMachine.java) |
| **命令通路(平台 → 设备命令下发)** | [§6](#6-命令通路平台--设备命令下发) | [DeviceCommandService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceCommandService.java) |
| **证书(cacert)** | [§7](#7-证书-cacert) | [CaCertLicenseService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/service/license/CaCertLicenseService.java) |
| **缓存层(DeviceCacheVO / ProductModelCacheVO)** | [§8](#8-缓存层-被-mqs-重度使用) | [LinkCacheDataHelper](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/helper/LinkCacheDataHelper.java) |
| **Facade 三态部署 / Open vs Sync vs AnyTenant 接口分类** | [§9](#9-facade-三态部署--接口分类) | [thinglinks-link-api/](thinglinks-link-facade/thinglinks-link-api/) |
| **跨服务联动(broker / mqs / rule 谁调谁)** | [§10](#10-跨服务联动) | — |
| **HLC 单调写实现(配合 mqs §8 看)** | [§11](#11-hlc-单调写实现) | [DeviceServiceImpl#updateDeviceConnectionStatusByEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceServiceImpl.java) |
| **常见排查路径** | [§12](#12-排查路径) | — |
| **文档维护索引** | [§13](#13-文档维护索引) | — |

---

## 1. 服务定位 / 5 子模块分工

| 维度 | 说明 |
|---|---|
| 服务职责 | IoT 元数据中心 ── 设备 / 产品 / 物模型 / OTA / 证书 / ACL / 仪表板 的 CRUD + 业务语义 |
| **上游(谁调本服务)** | ① 控制台(用户 / 租户管理设备)<br>② [thinglinks-broker](../thinglinks-broker/README.md)(MQTT 客户端连接 / ACL 校验)<br>③ [thinglinks-mqs](../thinglinks-mqs/README.md)(设备事件入站后回查元数据 / 回写状态 / Facade `saveDeviceAction`)<br>④ [thinglinks-rule](../thinglinks-rule/README.md)(规则引擎条件求值、命令下发)<br>⑤ xxl-job(LinkJobHandler 定时任务) |
| **下游(本服务依赖)** | ① MySQL/PG(13 个 mapper 命名空间,详见 §2)<br>② Redis(DeviceCacheVO / ProductModelCacheVO / 集合池 / OTA offset)<br>③ [thinglinks-tds](../thinglinks-tds/README.md)(物模型超级表 schema 同步)<br>④ thinglinks-system(用户 / 字典 / 文件 / 短信 Feign) |
| 启动入口 | [LinkServerApplication](thinglinks-link-server/src/main/java/com/mqttsnet/thinglinks/LinkServerApplication.java) |
| 配置 | [application.yml](thinglinks-link-server/src/main/resources/application.yml) + Nacos `thinglinks-link-server.yml` / `common.yml` / `redis.yml` / `database.yml` |
| 服务路径前缀 | `/link`(网关层 + `spring.application.path`) |

### 1.1 5 个子模块分工

| 子模块 | 职责 | 关键路径 |
|---|---|---|
| **thinglinks-link-entity** | DTO / VO / Entity / Enum / Converter ── 跨模块 / 跨服务复用的纯数据 | [src/main/java/com/mqttsnet/thinglinks](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/) |
| **thinglinks-link-facade** | Feign 接口 + 三态实现(`api` / `boot-impl` / `cloud-impl`,详见 §9) | [thinglinks-link-facade/](thinglinks-link-facade/) |
| **thinglinks-link-biz** ⭐ | **业务核心** ── 15 个子域 service / manager / mapper / event / cache / utils / ws | [src/main/java/com/mqttsnet/thinglinks](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/) |
| **thinglinks-link-controller** | REST 控制器 ── 控制台 + 北向开放 + 跨租户 + 缓存运维 | [src/main/java/com/mqttsnet/thinglinks](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/) |
| **thinglinks-link-server** | Spring Boot 启动 + Nacos / Sentinel / Seata / WebSocketConfig 装配 | [LinkServerApplication](thinglinks-link-server/src/main/java/com/mqttsnet/thinglinks/LinkServerApplication.java) |

**依赖关系**:`server → controller → biz → entity ← facade`(facade-api 仅依赖 entity,facade-cloud-impl 由调用方引入)

### 1.2 biz 模块的 15 个业务子域(包级一览)

| 包 | 子域 | 重要程度 |
|---|---|---|
| [device/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/) | 设备 + 动作 + ACL + 影子 + 位置 + 二维码 + 网关同步 + 分组(8 个 service) | ⭐⭐⭐ |
| [product/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/) | 产品 CRUD | ⭐⭐⭐ |
| [productproperty/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productproperty/) | 物模型属性 | ⭐⭐ |
| [productservice/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productservice/) | 物模型服务 | ⭐⭐ |
| [productcommand/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommand/) | 产品命令模板 | ⭐⭐ |
| [producttopic/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/producttopic/) | 产品 topic 配置 | ⭐ |
| [productcommandrequest/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandrequest/) | 命令请求落表 | ⭐ |
| [productcommandresponse/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandresponse/) | 命令响应落表 | ⭐ |
| [ota/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/) | OTA 包 / 任务 / 记录 / 目标 + 状态机 | ⭐⭐⭐ |
| [cacert/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/) | CA 证书(license + audit 子模块) | ⭐⭐ |
| [dashboard/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/dashboard/) | 仪表板统计 | ⭐ |
| [cache/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/) | 缓存抽象 + helper(被 mqs 重度调用) | ⭐⭐⭐ |
| [utils/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/utils/) | ACL / cacert / ota / x509 工具 | ⭐ |
| [ws/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ws/) | link 端 WS subject/observer(mqs / rule 调过来时的链路) | ⭐ |

---

## 2. 业务大维度俯瞰图

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         thinglinks-link 七大业务域                            │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                                │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌─────────────┐  │
│  │ ① 设备域     │ ←  │ ② 产品域      │ ←  │ ③ 物模型     │ ←  │ ④ 命令通路   │  │
│  │ Device       │    │ Product       │    │ Property/    │    │ Cmd Req/    │  │
│  │ Action       │    │ Topic         │    │ Service/     │    │ Resp        │  │
│  │ ACL/Shadow   │    │               │    │ Command      │    │             │  │
│  │ Group/Loc    │    └──────────────┘    └──────────────┘    └─────────────┘  │
│  │ Qrcode/Sync  │                                                              │
│  └──────┬───────┘                                                              │
│         │                                                                       │
│         ▼                                                                       │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────┐                      │
│  │ ⑤ OTA 升级   │    │ ⑥ 证书       │    │ ⑦ 仪表板     │                      │
│  │ Pkg/Task/   │    │ Cacert        │    │ Dashboard    │                      │
│  │ Record/     │    │ License       │    │ Stats        │                      │
│  │ Target +SM  │    │ Audit         │    │              │                      │
│  └─────────────┘    └──────────────┘    └──────────────┘                      │
│                                                                                │
│  ─────────────────────── 横向能力 ───────────────────────                      │
│  缓存层(LinkCacheDataHelper) | 事件驱动(event/) | EasyExcel(导入导出)        │
│  Facade 三态部署(api/boot/cloud) | @Echo 字典回显 | 多租户切库(@DS)         │
└──────────────────────────────────────────────────────────────────────────────┘
```

**域间高频联动**:
- **设备 ↔ 产品**:device 表 `product_identification` 关联;DeviceCacheVO 内冗余产品标识。
- **产品 ↔ 物模型**:product → properties / services / commands 一对多,组合后产生 ProductModelCacheVO(给 mqs 解析 payload 用)。
- **设备 ↔ OTA**:OTA 任务按 `productIdentification` 圈设备,通过 commandRequest 下发到设备。
- **设备 ↔ 命令**:用户下发 → ProductCommandRequest → DeviceCommand → broker → 设备响应 → ProductCommandResponse。
- **设备 ↔ 证书**:SSL 模式下设备由 CaCertLicense 签发,撤销时联动设备状态(详见 §7)。

### 2.1 13 个 mapper 命名空间(MyBatis-Plus)

| mapper_xxx 目录 | 表数 | 主表 |
|---|---|---|
| [mapper_device/](thinglinks-link-biz/src/main/resources/mapper_device/) | 7 | device / device_action / device_acl_rule / device_command / device_location / device_group / device_group_rel |
| [mapper_product/](thinglinks-link-biz/src/main/resources/mapper_product/) | 1 | product |
| [mapper_productProperty/](thinglinks-link-biz/src/main/resources/mapper_productProperty/) | 1 | product_property |
| [mapper_productService/](thinglinks-link-biz/src/main/resources/mapper_productService/) | 1 | product_service |
| [mapper_productCommand/](thinglinks-link-biz/src/main/resources/mapper_productCommand/) | 1 | product_command |
| [mapper_productTopic/](thinglinks-link-biz/src/main/resources/mapper_productTopic/) | 1 | product_topic |
| [mapper_productCommandRequest/](thinglinks-link-biz/src/main/resources/mapper_productCommandRequest/) | 1 | product_command_request |
| [mapper_productCommandResponse/](thinglinks-link-biz/src/main/resources/mapper_productCommandResponse/) | 1 | product_command_response |
| [mapper_ota/](thinglinks-link-biz/src/main/resources/mapper_ota/) | 4 | ota_upgrades / ota_upgrade_tasks / ota_upgrade_records / ota_upgrade_targets |
| [mapper_cacert/](thinglinks-link-biz/src/main/resources/mapper_cacert/) | 2 | ca_cert_license / ca_cert_audit_log |

---

## 3. 设备域 (device)

设备域是 link 服务**最大的子域**,8 个 service + 5 个 manager + 1 个分组子域 + 事件驱动子包 + EasyExcel 导入导出。

### 3.1 8 个核心 service

| Service | 职责 | impl |
|---|---|---|
| [DeviceService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceService.java) ⭐ | 设备 CRUD / 状态变更 / **HLC CAS 单调写** / 子设备联动 | [DeviceServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceServiceImpl.java) |
| [DeviceActionService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceActionService.java) ⭐ | 设备动作审计(`device_action` 表 ── mqs 高频写入) | [DeviceActionServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceActionServiceImpl.java) |
| [DeviceAclRuleService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceAclRuleService.java) | 设备 ACL 规则(broker 鉴权拦截器调用) | [DeviceAclRuleServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceAclRuleServiceImpl.java) |
| [DeviceCommandService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceCommandService.java) | 设备命令落表 / 状态机变更(SENT/SUCC/FAIL) | [DeviceCommandServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceCommandServiceImpl.java) |
| [DeviceLocationService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceLocationService.java) | 设备位置(经纬度 / 地理围栏) | [DeviceLocationServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceLocationServiceImpl.java) |
| [DeviceQrcodeService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceQrcodeService.java) | 设备二维码绑定 / 扫码激活 | [DeviceQrcodeServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceQrcodeServiceImpl.java) |
| [DeviceShadowService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceShadowService.java) | 设备影子(期望值 / 实时值) | [DeviceShadowServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceShadowServiceImpl.java) |
| [DeviceSyncAnyUserService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceSyncAnyUserService.java) | 设备同步 ── broker session 探活 / 与 link 设备表对账 | [DeviceSyncAnyUserServiceImpl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceSyncAnyUserServiceImpl.java) |

### 3.2 分组子域 (group/)

| Service | 职责 |
|---|---|
| [DeviceGroupService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/group/DeviceGroupService.java) | 设备分组 CRUD |
| [DeviceGroupRelService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/group/DeviceGroupRelService.java) | 分组关系(设备 ↔ 组) |

### 3.3 Manager 层(简单缓存放本 domain)

按"简单缓存放本 domain Manager,跨 domain 才走 LinkCacheDataHelper"原则,Manager 提供 DB 直查接口:

- [DeviceManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/DeviceManager.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/impl/DeviceManagerImpl.java) ── `findDeviceCacheVO(tenantId, idOrClientId)` 给 LinkCacheDataHelper 回填用
- [DeviceActionManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/DeviceActionManager.java) / [DeviceAclRuleManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/DeviceAclRuleManager.java) / [DeviceCommandManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/DeviceCommandManager.java) / [DeviceLocationManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/DeviceLocationManager.java)
- [DeviceGroupManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/group/DeviceGroupManager.java) / [DeviceGroupRelManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/manager/group/DeviceGroupRelManager.java)

### 3.4 事件驱动子包 (event/)

DDD 风格,4 个子包配合:

| 子包 | 类 | 用途 |
|---|---|---|
| [event/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/) | [DeviceDeletedEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/DeviceDeletedEvent.java) / [DeviceInfoUpdatedEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/DeviceInfoUpdatedEvent.java) | 事件本体 |
| [event/source/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/source/) | DeviceDeletedEventSource / DeviceInfoUpdatedEventSource | 事件源(由 service 发布) |
| [event/publisher/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/publisher/) | [DeviceEventPublisher](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/publisher/DeviceEventPublisher.java) | 发布封装 |
| [event/listener/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/listener/) | DeviceInfoUpdatedEventListener / DeviceGroupRelDeviceDeletedListener | 监听器(异步消费) |
| [event/handler/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/handler/) | [DeviceInfoUpdatedCacheHandler](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/handler/DeviceInfoUpdatedCacheHandler.java) | 业务处理器(缓存失效等) |

**典型链路**:DeviceService.update → publisher 发 DeviceInfoUpdatedEvent → DeviceInfoUpdatedEventListener → DeviceInfoUpdatedCacheHandler → 失效 DeviceCacheVO

### 3.5 EasyExcel 导入导出

[DeviceEasyExcelService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/easyexcel/DeviceEasyExcelService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/easyexcel/impl/DeviceEasyExcelServiceImpl.java) ── 批量导入设备 Excel,模板下载 / 错误行回显。

### 3.6 ⭐ DeviceActionTypeEnum 13 项(跨服务字典契约)

[DeviceActionTypeEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/device/enumeration/DeviceActionTypeEnum.java) 是跨 broker / mqs / rule 共用的**设备动作类型核心字典**,13 项语义稳定(改它要走 [mqs README §5](../thinglinks-mqs/README.md#5--action-type-完整规范设备事件统一字典) 7 处同步流程)。

| 枚举 | 业务含义 | affectsConnectionStatus |
|---|---|---|
| PUBLISH | 设备数据上行 | false |
| CONNECT / DISCONNECT / CLOSE / KICKED / HEART_TIMEOUT / ERROR | 连接生命周期(6 个) | **true** |
| PING | 心跳上行 | false |
| SUBSCRIBE / UNSUBSCRIBE | 订阅 / 取消订阅 | false |
| DISPATCH_ERROR | broker 分发失败(主要下行失败) | false |
| INBOUND | 第三方订阅源入站 | false |
| UNKNOWN | 兜底 | false |

**字典对齐**:`LINK_DEVICE_ACTION_TYPE`(13 项)与枚举一一对齐;`RULE_CONDITION_DEVICE_ACTION_TIRGGER_TYPE`(11 项,不含 UNKNOWN/INBOUND)给规则 UI 下拉。

---

## 4. 产品域 (product + 物模型)

产品域围绕 `product` 主表 + 4 个物模型子表 + 3 个 topic/cmd 衍生表展开。

### 4.1 7 个产品子域 service

| 子域 / Service | 表 | 用途 |
|---|---|---|
| [ProductService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/service/ProductService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/service/impl/ProductServiceImpl.java) | product | 产品 CRUD ── productIdentification 是设备所属维度 |
| [ProductPropertyService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productproperty/service/ProductPropertyService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productproperty/service/impl/ProductPropertyServiceImpl.java) | product_property | 物模型**属性**(只读上报字段,如温度 / 湿度) |
| [ProductServiceService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productservice/service/ProductServiceService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productservice/service/impl/ProductServiceServiceImpl.java) | product_service | 物模型**服务**(可调用动作,如开灯 / 重启) |
| [ProductCommandService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommand/service/ProductCommandService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommand/service/impl/ProductCommandServiceImpl.java) | product_command | 产品命令模板(命令字段元信息) |
| [ProductTopicService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/producttopic/service/ProductTopicService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/producttopic/service/impl/ProductTopicServiceImpl.java) | product_topic | 产品 topic 模板(自定义 topic 配置) |
| [ProductCommandRequestService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandrequest/service/ProductCommandRequestService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandrequest/service/impl/ProductCommandRequestServiceImpl.java) | product_command_request | 命令请求落表(下行) |
| [ProductCommandResponseService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandresponse/service/ProductCommandResponseService.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandresponse/service/impl/ProductCommandResponseServiceImpl.java) | product_command_response | 命令响应落表(上行回执) |

### 4.2 物模型缓存视图

[ProductModelCacheVO](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cache/vo/product/ProductModelCacheVO.java) ── 将 product + property + service + command 组合后的"物模型快照",存 Redis;mqs `DevicePayloadDecodeStage` 用它解析 PUBLISH payload。

写入端:product / property / service / command CRUD 后触发 `ProductModelUpdatedEvent` → [ProductModelUpdatedCacheHandler](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/event/handler/ProductModelUpdatedCacheHandler.java) → 失效缓存。

### 4.3 产品事件子包

| 路径 | 用途 |
|---|---|
| [product/event/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/event/) | [ProductInfoUpdatedEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/event/ProductInfoUpdatedEvent.java) / [ProductModelUpdatedEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/product/event/ProductModelUpdatedEvent.java) |
| handler / listener / publisher / source | 同 §3.4 模式 |

---

## 5. OTA 升级

OTA 是 link 服务**最复杂的业务子域**,4 张表 + Spring StateMachine + 多种范围 / 执行策略。

### 5.1 4 个 Manager(对应 4 张表)

| Manager | 表 | 含义 |
|---|---|---|
| [OtaUpgradesManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/OtaUpgradesManager.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/impl/OtaUpgradesManagerImpl.java) | ota_upgrades | OTA 包(软件 / 固件 / 配置 / 资源) |
| [OtaUpgradeTasksManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/OtaUpgradeTasksManager.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/impl/OtaUpgradeTasksManagerImpl.java) | ota_upgrade_tasks | 升级任务(一次圈一批设备) |
| [OtaUpgradeRecordsManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/OtaUpgradeRecordsManager.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/impl/OtaUpgradeRecordsManagerImpl.java) | ota_upgrade_records | 升级记录(每设备一条,记录命令下发 / 结果) |
| [OtaUpgradeTargetsManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/OtaUpgradeTargetsManager.java) / [Impl](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/manager/impl/OtaUpgradeTargetsManagerImpl.java) | ota_upgrade_targets | 升级目标(任务圈定的具体设备清单) |

### 5.2 状态机 (statemachine/)

[OtaUpgradeStateMachine](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/OtaUpgradeStateMachine.java) ── Spring StateMachine 实现,核心 7 个组件:

| 组件 | 位置 |
|---|---|
| 配置 | [OtaUpgradeStateMachineConfig](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/config/OtaUpgradeStateMachineConfig.java) |
| 上下文 | [OtaUpgradeContext](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/context/OtaUpgradeContext.java) |
| Action(20+ 个) | [statemachine/action/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/action/) ── 静态 / 动态升级 + 取消 / 重试 / 超时 / 完成 + APP 确认 |
| Condition | [statemachine/condition/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/condition/) ── 静态 / 动态升级条件守卫 |
| Event | [OtaTaskExecutionEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/event/OtaTaskExecutionEvent.java) / [OtaTaskExecutionResultEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/event/OtaTaskExecutionResultEvent.java) |
| Strategy/scope(范围) | [statemachine/strategy/scope/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/strategy/scope/) ── All / Group / Regional / Targeted 4 种圈设备策略 |
| Strategy/executor(执行) | [statemachine/strategy/executor/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/strategy/executor/) ── 版本过滤 / 记录去重 / APP 确认 |

### 5.3 关键枚举

| 枚举 | 用途 |
|---|---|
| [OtaUpgradeTaskStatusEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaUpgradeTaskStatusEnum.java) | 任务总体状态 |
| [OtaUpgradeRecordStatusEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaUpgradeRecordStatusEnum.java) | 单设备升级记录状态 |
| [OtaUpgradeTargetStatusEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaUpgradeTargetStatusEnum.java) | 目标设备状态 |
| [OtaUpgradeEventEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaUpgradeEventEnum.java) | 状态机事件 |
| [OtaUpgradeMethodEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaUpgradeMethodEnum.java) | 升级方式(静态 / 动态) |
| [OtaUpgradeScopeEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaUpgradeScopeEnum.java) | 升级范围(All / Group / Regional / Targeted) |
| [OtaPackageTypeEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaPackageTypeEnum.java) | 包类型(软件 / 固件 / 配置 / 资源) |
| [OtaPackageSignMethodEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/ota/enumeration/OtaPackageSignMethodEnum.java) | 签名算法 |

### 5.4 OTA 任务执行链路(简版)

```
用户创建 OTA 任务(/ota/upgrades + /ota/upgradeTasks)
   │
   ├─→ OtaUpgradeStateMachine fire(START_UPGRADE)
   │      ├─ UpgradeScopeStrategy.resolve() → 圈定设备列表(ota_upgrade_targets)
   │      ├─ DeviceVersionFilterStrategy → 跳过已是目标版本的设备
   │      ├─ UpgradeRecordDeduplicationStrategy → 去重重复任务
   │      └─ AppConfirmationStrategy → 是否需要 APP 用户确认
   │
   ├─→ StaticBatchPushAction / DynamicStartUpgradeAction
   │      └─ 通过 productcommandrequest 下发命令到设备(走 broker)
   │
   ├─→ 设备回执 (mqs `MqttEventOtaReportService` 等)→ OtaOpenAnyUserFacade.otaReport*
   │
   └─→ OtaTaskExecutionResultEvent → 状态机 fire(COMPLETE/FAIL/TIMEOUT)
          └─ DynamicCompleteUpgradeAction / StaticFailUpgradeAction 等收尾
```

**定时探活**:[LinkJobHandlerFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/LinkJobHandlerFacade.java) 由 xxl-job 触发,扫描 ota offset(`LinkCacheDataHelper#getOtaTaskExecutorOffset`)推进未完成任务。

---

## 6. 命令通路(平台 → 设备命令下发)

平台命令下发是 link → broker → 设备的端到端链路,涉及 5 类组件。

### 6.1 端到端流向

```
控制台 / Facade caller
   │  DeviceCommandWrapperParam(可串行 / 并行)
   ▼
DeviceOpenAnyUserFacade.issueCommands(...)
   │  → DeviceOpenAnyUserController#issueCommands
   ▼
DeviceCommandService(校验设备 + 物模型 + 序列化 payload)
   │
   ├─→ ProductCommandRequestService.save (落 product_command_request 表)
   │
   └─→ broker(MQTT publish 到设备订阅 topic)
          │  设备响应
          ▼
       mqs `MqttEventCommandResponseService`
          ▼
       DeviceCommandFacade / ProductCommandResponseService
          ▼
       更新 DeviceCommand 状态(SENT → SUCC/FAIL)
       落 product_command_response 表
```

### 6.2 关键组件

| 组件 | 位置 |
|---|---|
| **Facade(对外)** | [DeviceCommandFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceCommandFacade.java) ── 命令查询 / 状态回写;[DeviceOpenAnyUserFacade#issueCommands](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceOpenAnyUserFacade.java) ── 命令下发入口 |
| **Service** | [DeviceCommandService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/DeviceCommandService.java) / [ProductCommandRequestService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandrequest/service/ProductCommandRequestService.java) / [ProductCommandResponseService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/productcommandresponse/service/ProductCommandResponseService.java) |
| **Controller** | [DeviceCommandController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/device/controller/DeviceCommandController.java) / [ProductCommandController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/productcommand/controller/ProductCommandController.java) |
| **关键枚举** | [DeviceCommandStatusEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/device/enumeration/DeviceCommandStatusEnum.java) / [DeviceCommandTypeEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/device/enumeration/DeviceCommandTypeEnum.java) |

---

## 7. 证书 (cacert)

### 7.1 cacert ── CA 证书生命周期管理(SSL 设备认证)

| 子模块 | 职责 | 位置 |
|---|---|---|
| **license** | 证书签发 / 撤销 / 影响面分析 / SSL 测试 | [cacert/service/license/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/service/license/) |
| **audit** | 证书操作审计 | [cacert/service/audit/](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/service/audit/) |
| Manager | [CaCertLicenseManager](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/manager/license/CaCertLicenseManager.java) | DB 直查 |
| Service | [CaCertLicenseService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/service/license/CaCertLicenseService.java) / [CaCertAuditLogService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/service/audit/CaCertAuditLogService.java) | 业务核心 |
| Controller | [CaCertLicenseController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/cacert/controller/license/CaCertLicenseController.java) | REST |
| 证书事件 | [CaRevokedEvent](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/event/CaRevokedEvent.java) + [CaRevokedCacheEvictListener](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/event/listener/CaRevokedCacheEvictListener.java) ── 撤销时失效相关设备缓存 |
| 工具 | [CertificateVerifierUtil](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/utils/cacert/CertificateVerifierUtil.java) / [X509Util](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/utils/x509/X509Util.java) / [MqttCertGenerator](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/utils/x509/MqttCertGenerator.java) / [CertSerialNumberUtil](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/utils/x509/CertSerialNumberUtil.java) |

**关键枚举**:[CaCertStatusEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cacert/enumeration/CaCertStatusEnum.java) / [CaCertAlgorithmEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cacert/enumeration/CaCertAlgorithmEnum.java) / [CaCertSignAlgorithmEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cacert/enumeration/CaCertSignAlgorithmEnum.java) / [CaCertAuditTypeEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cacert/enumeration/CaCertAuditTypeEnum.java)。

**SSL 测试 step 进度**:[DeviceSslTestStepEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/device/enumeration/DeviceSslTestStepEnum.java) + [DeviceSslTestStepStatusEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/device/enumeration/DeviceSslTestStepStatusEnum.java) 给前端实时进度条用。

---

## 8. 缓存层(被 mqs 重度使用)

缓存是 link ↔ mqs **唯一的高频读取通道**。mqs 每条设备消息都要查 DeviceCacheVO + ProductModelCacheVO,**不能查 DB**(吞吐瓶颈),所以这一层设计稳定性 = 平台稳定性。

### 8.1 LinkCacheDataHelper(总入口)

[LinkCacheDataHelper](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/helper/LinkCacheDataHelper.java) ── **统一缓存读写门面**,只负责 Redis CRUD,业务逻辑不进。

| 缓存 VO | 方法 | 用途 |
|---|---|---|
| [DeviceCacheVO](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cache/vo/device/DeviceCacheVO.java) ⭐ | `getDeviceCacheVO(idOrClientId)` / `deleteDeviceCacheVO(...)` | 设备元数据快照(被 mqs `DeviceCacheEnricher` 高频读) |
| [ProductModelCacheVO](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cache/vo/product/ProductModelCacheVO.java) ⭐ | `getProductModelCacheVO(productIdentification)` / `delete...` | 物模型快照(被 mqs `DevicePayloadDecodeStage` 高频读) |
| `SuperTableDescribeVO[]` | `getProductModelSuperTableCacheVO(...)` / `set...` | TDS 超级表 schema 缓存 |
| Sorted Set 设备数据池 | `setDeviceDataCollectionPoolCacheVO` / `get...` / `delete...` | 设备数据时序临时池(用于聚合) |
| Sorted Set 设备动作池 | `setDeviceActionCacheVO` / `get...` / `delete...` | 设备动作临时池([DeviceActionCacheVO](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cache/vo/device/DeviceActionCacheVO.java)) |
| 上下行计数器 | `incrementUpLinkCounter()` / `incrementDownLinkCounter()` | 按"日 + 分"Hash 计数(给仪表板) |
| OTA offset | `getOtaTaskExecutorOffset()` / `set...` | OTA 任务执行偏移(给 xxl-job) |

### 8.2 Domain 内缓存 service

按收敛原则,**只在涉及当前 domain 的简单 read-through** 时使用,跨 domain 才走 LinkCacheDataHelper:

| Service | 位置 |
|---|---|
| [DeviceCacheService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/device/DeviceCacheService.java) | 设备缓存抽象 |
| [ProductCacheService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/product/ProductCacheService.java) | 产品缓存抽象 |
| [ProductModelCacheService](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/product/ProductModelCacheService.java) | 物模型缓存抽象 |
| [CacheSuperAbstract](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/CacheSuperAbstract.java) | 共用抽象基类 |
| [CacheEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/cache/enumeration/CacheEnum.java) | 缓存键值定义 |

### 8.3 缓存运维接口

[CacheAnyUserController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/CacheAnyUserController.java) ── 给 mqs / 控制台调用,刷新 / 清除指定 device / product 缓存。

---

## 9. Facade 三态部署 + 接口分类

### 9.1 三态 module 拆分

| 模块 | 角色 | 选哪个 |
|---|---|---|
| [thinglinks-link-api](thinglinks-link-facade/thinglinks-link-api/) | **Facade 接口定义** ── 服务消费方 / 实现方都依赖此 | 必须 |
| [thinglinks-link-boot-impl](thinglinks-link-facade/thinglinks-link-boot-impl/) | 单体 / 进程内调用实现 ── 直接 `@Autowired` 本地 Service | 单体启动 |
| [thinglinks-link-cloud-impl](thinglinks-link-facade/thinglinks-link-cloud-impl/) | 微服务 Feign 远程调用实现 + Hystrix Fallback | 微服务部署 |

**核心设计**:Facade 接口在 `api` 模块定义,boot-impl / cloud-impl 各自提供实现 ── 消费方代码不变,部署形态切换无感知。

### 9.2 Facade 接口分类(8 个)

按访问语义分三类:

| 类别 | Facade | 路径(cloud-impl FeignClient path) | 用途 |
|---|---|---|---|
| **设备本体** | [DeviceFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceFacade.java) | `/device` | 内部设备查询(boot-impl: [DeviceFacadeImpl](thinglinks-link-facade/thinglinks-link-boot-impl/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceFacadeImpl.java) / cloud-impl Feign: [DeviceApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/device/DeviceApi.java))|
| **设备命令** | [DeviceCommandFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceCommandFacade.java) | `/device/command` | 命令查询 / 回写(cloud: [DeviceCommandApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/device/DeviceCommandApi.java)) |
| **anyUser 开放** | [DeviceOpenAnyUserFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceOpenAnyUserFacade.java) ⭐ | `/anyUser/deviceOpen` | 给 mqs / broker / rule 调用的设备开放接口(cloud: [DeviceOpenAnyUserApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/anyuser/DeviceOpenAnyUserApi.java)) |
| **anyUser 开放** | [ProductOpenAnyUserFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/ProductOpenAnyUserFacade.java) | `/anyUser/productOpen` | 产品 / 物模型查询(cloud: [ProductOpenAnyUserApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/anyuser/ProductOpenAnyUserApi.java)) |
| **anyUser 开放** | [ProductTopicOpenAnyUserFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/ProductTopicOpenAnyUserFacade.java) | `/anyUser/productTopicOpen` | Topic 配置查询(cloud: [ProductTopicOpenAnyUserApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/anyuser/ProductTopicOpenAnyUserApi.java)) |
| **anyUser 开放** | [OtaOpenAnyUserFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/OtaOpenAnyUserFacade.java) | `/anyUser/otaOpen` | OTA 上报 / 拉包 / 命令响应(cloud: [OtaOpenAnyUserApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/anyuser/OtaOpenAnyUserApi.java),mqs OTA 系列 service 调用) |
| **anyTenant 跨租户** | [DeviceOpenAnyTenantFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceOpenAnyTenantFacade.java) | `/anyTenant/deviceOpen` | 跨租户设备查询(cloud: [DeviceOpenAnyTenantApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/anytenant/DeviceOpenAnyTenantApi.java)) |
| **定时任务** | [LinkJobHandlerFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/LinkJobHandlerFacade.java) | `/job/linkHandler` | xxl-job 调用入口(OTA offset / 设备探活)(cloud: [LinkJobHandlerApi](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/job/LinkJobHandlerApi.java)) |

### 9.3 三类前缀语义

| 前缀 | 控制器目录 | 语义 |
|---|---|---|
| (业务前缀) | [device/](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/device/controller/) / [product*/](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/product/controller/) / [ota/](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/ota/controller/) / [cacert/](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/cacert/controller/) | 控制台用户态接口(走 oauth 鉴权) |
| `/anyUser/...` | [anyuser/controller/](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/) | **服务间调用** ── 跳过用户态认证,但仍按租户上下文走多租户切库 |
| `/anyTenant/...` | [anytenant/controller/](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anytenant/controller/) | **跨租户调用** ── 显式带 tenantId 参数访问其他租户数据,运维 / 平台级 |

### 9.4 anyuser controllers 一览

| Controller | 用途 |
|---|---|
| [DeviceOpenAnyUserController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/DeviceOpenAnyUserController.java) ⭐ | 设备状态 CAS 写、子设备维护、设备命令下发、动作落表 |
| [DeviceSyncAnyUserController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/DeviceSyncAnyUserController.java) | 设备同步 / broker session 探活对账 |
| [ProductOpenAnyUserController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/ProductOpenAnyUserController.java) | 产品 / 物模型查询 |
| [ProductTopicOpenAnyUserController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/ProductTopicOpenAnyUserController.java) | Topic 配置查询 |
| [OtaOpenAnyUserController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/OtaOpenAnyUserController.java) | OTA 上报 / 拉包 / 命令响应 |
| [CacheAnyUserController](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/CacheAnyUserController.java) | 缓存运维(刷新 / 清除) |

---

## 10. 跨服务联动

```
            ┌──────────────────────────────────────────┐
            │            thinglinks-link                │
            │  (元数据中心 + Redis 缓存)               │
            └─▲──────────────▲────────────▲────────────┘
              │              │            │
   ┌──────────┴──┐   ┌───────┴─────┐  ┌──┴─────────────┐
   │  broker     │   │   mqs       │  │   rule          │
   │ (MQTT 接入) │   │ (消息处理)  │  │ (规则 / 桥接)   │
   └─────────────┘   └─────────────┘  └─────────────────┘
```

### 10.1 broker → link

| broker 场景 | 调用 link Facade | 用途 |
|---|---|---|
| MQTT 连接鉴权 / ACL | DeviceFacade + DeviceAclRule 查询 | 通过 device + acl 表 + CaCertLicense 校验 |
| Session 设备探活 | [DeviceOpenAnyUserFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceOpenAnyUserFacade.java)(老路径) | broker session 状态对账 |

### 10.2 mqs → link(高频)

mqs 是 link 的**最重要的消费方**:

| mqs 类 | 调用 link | 用途 |
|---|---|---|
| `DeviceCacheEnricher` | [LinkCacheDataHelper#getDeviceCacheVO](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cache/helper/LinkCacheDataHelper.java) | 每条设备事件查 DeviceCacheVO |
| `DevicePayloadDecodeStage` | LinkCacheDataHelper#getProductModelCacheVO | 物模型解析 |
| `DeviceLifecycleStage` | [DeviceOpenAnyUserFacade#updateDeviceConnectionStatusByEvent](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceOpenAnyUserFacade.java) ⭐ | 设备状态 CAS 单调写(详见 §11) |
| `DeviceActionPersistStage` | DeviceOpenAnyUserFacade#saveDeviceAction | 设备动作审计落 `device_action` |
| `BridgeIngressRocketmqConsumerHandler` | DeviceOpenAnyUserFacade#saveDeviceAction | 桥接入站设备动作落表 |
| `MqttEventOtaReportService` 等 4 个 | [OtaOpenAnyUserFacade](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/OtaOpenAnyUserFacade.java) `otaReport*` / `otaPull*` / `saveOtaUpgradeRecord*` | OTA 上报 / 拉包 / 命令响应回 link |

### 10.3 rule → link

| rule 场景 | 调用 link | 用途 |
|---|---|---|
| INBOUND 桥接 | DeviceOpenAnyUserFacade#saveDeviceAction | 第三方订阅源入站时落 `device_action` |
| 命令下发 action | DeviceCommandFacade / DeviceOpenAnyUserFacade#issueCommands | 规则触发时下发命令 |

---

## 11. HLC 单调写实现

设备连接状态(`connect_status` 字段)在事件驱动场景下必须**event-time LWW(Last-Writer-Wins)** ── 否则乱序 / 抖动重连会导致状态回退(老 OFFLINE 事件覆盖新 ONLINE)。

### 11.1 入口方法签名

```java
// DeviceServiceImpl#updateDeviceConnectionStatusByEvent(...)  ── biz 内 CAS 实现
boolean updateDeviceConnectionStatusByEvent(String clientId, Integer status, Long eventHlc);
```

源码:[DeviceServiceImpl L812](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceServiceImpl.java)

### 11.2 SQL 单调写语义

```sql
UPDATE device
   SET connect_status = ?,
       last_status_event_hlc = ?
 WHERE client_id = ?
   AND last_status_event_hlc < ?     -- CAS 关键
```

- 字段 `last_status_event_hlc` NOT NULL DEFAULT 0 ── 存量行首次事件总能写入。
- 严格 `<` 比较 ── 同 hlc 重试不会改写。
- 仅依赖单语句 SQL,**不需要 SELECT-then-UPDATE 串行化**,Postgres / MySQL 默认隔离都安全。

### 11.3 调用链与协作

| 角色 | 行为 |
|---|---|
| mqs `DeviceLifecycleStage` | 拿到 actionType + 上游事件 hlc,通过 Facade 调入 |
| Facade(cloud-impl Feign)| [DeviceOpenAnyUserApi#updateDeviceConnectionStatusByEvent](thinglinks-link-facade/thinglinks-link-cloud-impl/src/main/java/com/mqttsnet/thinglinks/link/api/anyuser/DeviceOpenAnyUserApi.java) (HTTP PUT `/anyUser/deviceOpen/updateDeviceConnectionStatusByEvent/{clientId}`) |
| Controller | [DeviceOpenAnyUserController#updateDeviceConnectionStatusByEvent](thinglinks-link-controller/src/main/java/com/mqttsnet/thinglinks/anyuser/controller/DeviceOpenAnyUserController.java) (L124) |
| Service CAS | [DeviceServiceImpl L812](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceServiceImpl.java) |
| 旧路径(无 HLC)| Facade [#updateDeviceConnectionStatus(clientId, status)](thinglinks-link-facade/thinglinks-link-api/src/main/java/com/mqttsnet/thinglinks/link/facade/DeviceOpenAnyUserFacade.java) ── **无条件覆盖**,仅控制台 / xxl-job / 老监听器用 |

### 11.4 日志关键词(排查必查)

```
[Device.updateByEvent] applied clientId=xxx status=1 hlc=...  → CAS 成功
[Device.updateByEvent] CAS rejected (stale event) clientId=...→ 老事件被拒
```

mqs 端配套日志关键字见 [mqs README §8](../thinglinks-mqs/README.md#8-设备状态-hlc-单调写--eventutc-物理时间) / `[bus.lifecycle]`。

### 11.5 网关联动

CAS 命中后,如果设备节点类型 = GATEWAY 且新状态 = OFFLINE,**联动把子设备一并 OFFLINE**(在 `DeviceServiceImpl.updateSubDevicesConnectionStatus`)。

---

## 12. 排查路径

### 12.1 设备元数据异常

| 现象 | 排查路径 |
|---|---|
| mqs 报"设备不存在" / 物模型解析失败 | ① 控制台查设备是否存在 → ② curl `CacheAnyUserController` 清缓存 → ③ 检查 `LinkCacheDataHelper#getDeviceCacheVO` 日志看是否回查 DB 失败 |
| 设备状态总是 OFFLINE | 看 link 日志 `[Device.updateByEvent] CAS rejected` ── 上游 hlc 比 DB 老;或 mqs `DeviceLifecycleStage` 未投递 |
| 字典回显错乱 | `LINK_DEVICE_ACTION_TYPE` 字典对齐 [DeviceActionTypeEnum](thinglinks-link-entity/src/main/java/com/mqttsnet/thinglinks/device/enumeration/DeviceActionTypeEnum.java) |

### 12.2 OTA 卡住

| 现象 | 排查路径 |
|---|---|
| 任务一直 PENDING | ① 检查 `LinkCacheDataHelper#getOtaTaskExecutorOffset` Redis 值 → ② xxl-job 调度日志 → ③ 状态机 [OtaUpgradeStateMachine](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/OtaUpgradeStateMachine.java) action 失败堆栈 |
| 部分设备 SKIP | [DeviceVersionFilterStrategy](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/strategy/executor/DeviceVersionFilterStrategy.java) 命中(已是目标版本)|
| APP 不弹确认 | [AppConfirmationStrategy](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/strategy/executor/AppConfirmationStrategy.java) 是否启用 + APP 端是否拉取 |

### 12.3 命令下发失败

| 现象 | 排查路径 |
|---|---|
| Command 一直 SENT,无回执 | ① 设备订阅 topic 是否正确 → ② mqs `MqttEventCommandResponseService` 日志 → ③ broker plugin DISPATCH_ERROR 计数(Redis stats) |
| 命令请求落表但 broker 没投出 | DeviceCommandService → broker 链路;查 broker `MqttBrokerService` 日志 |

### 12.4 证书签发 / 撤销

| 现象 | 排查路径 |
|---|---|
| SSL 测试卡住 | 看 `device.last_status_event_hlc` + DeviceSslTestStep 表 |
| 撤销后设备仍能连 | [CaRevokedCacheEvictListener](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/cacert/event/listener/CaRevokedCacheEvictListener.java) 是否成功失效;broker plugin 是否拉到新 CRL |

### 12.5 缓存不一致

| 现象 | 排查路径 |
|---|---|
| 控制台改设备后 mqs 还是老数据 | ① DeviceInfoUpdatedEventListener 是否触发 → ② [DeviceInfoUpdatedCacheHandler](thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/event/handler/DeviceInfoUpdatedCacheHandler.java) 日志 → ③ Redis key 是否真删 |

---

## 13. 文档维护索引

### 13.1 改动联动清单

| 改这里 | 必须同步改 |
|---|---|
| `DeviceActionTypeEnum` 新增 / 删除 | [mqs README §5.5 7 处流程](../thinglinks-mqs/README.md#55-新增-action-type-流程开发约定) + 字典 SQL + 本 README §3.6 |
| 任何 entity / 表结构 | `docs/db/thinglinks_base.sql`(参考用户记忆 schema-sync 规则) |
| Facade 接口签名 | api + boot-impl + cloud-impl(Feign + Fallback)三处 |
| DeviceCacheVO / ProductModelCacheVO 字段 | mqs Enricher / Stage 同步;清 Redis 旧数据 |
| OTA 状态机 / 枚举 | 状态机 config + action + condition 三处 |

### 13.2 兄弟服务 README 索引

| 服务 | README |
|---|---|
| broker | [thinglinks-broker/README.md](../thinglinks-broker/README.md) |
| **link** | 本文件 |
| mqs | [thinglinks-mqs/README.md](../thinglinks-mqs/README.md) ── HLC 上游驱动方 |
| rule | [thinglinks-rule/README.md](../thinglinks-rule/README.md) |
| tds | [thinglinks-tds/README.md](../thinglinks-tds/README.md) ── 物模型时序入库 |
| 顶级 | [README.md](../README.md) |

### 13.3 PR Review 红线

- 任何 entity / mapper 改动必带 `docs/db/thinglinks_base.sql` 同步 commit
- 新建 Java 类必带 `@author mqttsnet` + `@since YYYY-MM-DD` class javadoc
- 缓存读取走 LinkCacheDataHelper(跨 domain)或 domain 内 Manager(本 domain 简单缓存)── **不要在 Service 直接调 cachePlusOps**
- HLC 路径(`updateDeviceConnectionStatusByEvent`)只走事件驱动 ── 控制台 / xxl-job 用无条件覆盖路径
- OTA 状态机变更必跑状态机集成测试

---

> **最后维护**:2026-05-18 ── 改 README 与改代码同步,过期文档比没文档更糟糕。
