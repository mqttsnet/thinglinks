# ThingLinks Video - 视频流服务

支持浏览器无插件播放摄像头视频。
支持国标设备（摄像机、平台、NVR 等）设备接入。
支持 RTSP、RTMP 直播设备接入，充分利旧。
支持国标级联、多平台级联、跨网视频预览。
支持跨网网闸平台互联。

---

## 模块结构

```
thinglinks-video/
├── thinglinks-video-entity/            # 实体层：VO、Entity、DTO、枚举、缓存对象
├── thinglinks-video-biz/               # 核心业务层：Service、Manager、Mapper、流媒体管理、缓存、事件监听
├── thinglinks-video-biz-protocol/      # 协议层：GB28181/JT1078/ISUP 协议栈及依赖协议的播放/控制服务
├── thinglinks-video-controller/        # 控制器层：REST 接口
├── thinglinks-video-facade/            # Facade 层
│   ├── thinglinks-video-api/           #   Facade 接口定义
│   ├── thinglinks-video-boot-impl/     #   单体版实现（executor 本地调用）
│   └── thinglinks-video-cloud-impl/    #   微服务版实现（Feign 远程调用）
└── thinglinks-video-server/            # 启动模块：SpringBoot Application
```

## 模块职责

### video-entity

实体层，包含所有数据模型，不含任何业务逻辑。

- `entity/` — 数据库实体（MyBatis-Plus）
- `vo/` — 请求/响应视图对象
- `dto/` — 数据传输对象
- `enumeration/` — 枚举（字典）
- `cache/` — Redis 缓存对象（CacheVO）

### video-biz

核心业务层，包含与协议无关的纯业务逻辑。**executor 通过 boot-impl 依赖此模块。**

- `service/` — 业务服务（ServiceImpl，标注 `@DS(DsConstant.BASE_TENANT)` 实现多租户数据源路由）
- `manager/` — 数据访问层（ManagerImpl → Mapper）
- `mapper/` — MyBatis Mapper 接口
- `media/` — 流媒体节点管理（ZLM/ABL 心跳、指标采集、上下线事件）
- `config/` — 配置类（SipConfig、UserSetting 等）
- `cache/` — 缓存管理（SSRC、StreamInfo、RTP 端口）
- `listener/` — 事件监听器（录像 Hook、设备上下线）
- `common/` — 通用工具

### video-biz-protocol

协议层，包含所有**进程绑定**的协议栈和依赖协议的上层服务。仅 video-server 依赖此模块，**executor 不依赖**，因此协议类不会出现在 executor 的 classpath 中。

- `gb28181/` — GB/T 28181 SIP 协议栈
  - `SipLayer` — SIP 服务初始化（CommandLineRunner，监听端口）
  - `transmit/` — SIP 消息收发（Request/Response 处理器）
  - `cmd/` — SIP 指令构建（SipMessageBuilder、各 Commander）
  - `session/` — SIP 会话管理（SSRC、Invite Session）
  - `auth/` — SIP 摘要认证
  - `device/` — 厂商设备适配（海康、大华、宇视等）
  - `event/` — SIP 生命周期事件（注册、播放、PTZ、告警）
- `jt1078/` — JT/T 1078 协议栈（车载视频）
- `isup/` — ISUP 协议栈（海康 SDK 接入）
- `protocol/` — 多协议适配层（DeviceAccessProtocol 接口 + 各协议实现）
- `service/stream/` — 播放/回放/下载/广播服务（依赖 SIPSender）
- `service/device/` — 设备控制/PTZ 服务（依赖 Commander）

### video-controller

REST 控制器层，对外暴露 HTTP 接口。

### video-facade

Facade 层，为其他模块提供调用入口。

- **video-api** — Facade 接口定义（如 `VideoJobHandlerFacade`）
- **video-boot-impl** — 单体版实现，依赖 video-biz（不依赖 video-biz-protocol），供 iot-executor 本地调用
- **video-cloud-impl** — 微服务版实现，通过 Feign 远程调用

### video-server

SpringBoot 启动模块，聚合所有子模块。

## 依赖关系

```
video-server
  └─ video-controller     → video-biz + video-biz-protocol
  └─ video-biz-protocol   → video-biz
  └─ video-biz            → video-entity

iot-executor（XXL-Job 执行器）
  └─ video-boot-impl      → video-biz（不含 protocol，协议类不在 classpath）
```

## 运行时链路拓扑

以 GB/T 28181 + ZLMediaKit 为参考拓扑，四个角色之间是**单向可达**关系——只要下列方向通即可，不要求互通：

```
摄像头 ── ① RTP (UDP/TCP) ─────▶ ZLM ── ② Hook (HTTP) ──▶ 后端 video
  ▲                               ▲                          │
  │                               │                          │ WebSocket 推送
  └─── ③-a SIP (UDP/TCP) ─────────┴─── ③-b openAPI (HTTP) ───┤
                                                              ▼
                                                            前端
                                                              │  ④ HTTP-FLV / WS-FLV / HLS / WebRTC
                                                              ▼
                                                             ZLM
```

| # | 方向 | 协议 | 作用 |
|---|------|------|------|
| ① | 摄像头 → ZLM | RTP (UDP/TCP) | 音视频推流 |
| ② | ZLM → 后端 | HTTP 回调 | 流上下线、录像完成等事件 |
| ③ | 后端 → 摄像头 / ZLM | SIP + HTTP openAPI | INVITE/BYE/PTZ 信令 + 流代理/查询等控制 |
| ④ | 浏览器 → ZLM | HTTP-FLV / WS-FLV / HLS / WebRTC | 前端拉流，不经过后端 |

### 部署要点

- **端口最小集**：SIP 5060（UDP+TCP）、ZLM HTTP API（默认 8080）、ZLM RTP 端口段（默认 30000–30500 UDP）、后端 Gateway 接收 Hook 的 HTTP 端口
- **NAT / 跨网段**：摄像头与 ZLM 跨 NAT 时需放通 UDP 端口段；必要时在"媒体服务器 → SDP地址 / 流播放地址"填设备/浏览器实际可达的 IP
- **Hook 回调地址**：ZLM 必须能访问后端 Gateway；集群可 per-ZLM 在"媒体服务器 → Hook 回调地址"覆盖（留空走 Nacos 全局）

### 三种典型拓扑

| 拓扑 | 适用 | Hook 回调地址 | 注意事项 |
|------|------|--------------|---------|
| **全内网** | 开发 / 内部部署 | 后端内网 IP，如 `http://192.168.1.84:8080/video/zlmHook/index/hook` | 三方同网段，直接跑 |
| **ZLM + 后端同机云端** | 生产推荐 | `http://127.0.0.1:<gateway-port>/video/zlmHook/index/hook` | 摄像头出公网推 ZLM，Hook 走 loopback；带宽全部走云端 |
| **ZLM 公网 / 后端内网** | 本地开发跨公网 ZLM | 内网穿透公网 URL（frp/ngrok/cpolar 等） | Mac 内网 IP 公网访问不到，必须穿透；下面给 frp 模板 |

#### frp 内网穿透样板（ZLM 公网 → 后端 Mac）

`frpc.toml`（运行在本地 Mac）：

```toml
serverAddr = "your-frps-host.com"
serverPort = 7000
auth.method = "token"
auth.token = "your-token"

[[proxies]]
name = "thinglinks-gateway"
type = "tcp"
localIP = "127.0.0.1"
localPort = 8080      # 后端 Gateway 端口
remotePort = 18080    # 公网端口
```

启动：`frpc -c frpc.toml`，然后在"媒体服务器 → Hook 回调地址"填：

```
http://your-frps-host.com:18080/video/zlmHook/index/hook
```

ZLM 用此公网 URL 回调，链路打通。

### 常见故障定位

| 现象 | 可能原因 | 快速排查 |
|------|---------|---------|
| INVITE 10s 超时 | SIP 链路不通 / 设备不在线 | 抓 SIP 端口看有没有 200 OK |
| INVITE 200 OK 后前端持续转圈 | 摄像头没推 RTP，或 Hook 回不到后端 | ZLM 抓 `udp port <rtp>` 看有无包；查后端日志是否收到 `on_stream_changed` |
| RTSP/ONVIF 设备添加成功但点播失败 | 设备 URL 不可达 / 凭据错误 / ZLM `addStreamProxy` 失败 | 用 ffplay 直接拉一次 RTSP URL 验证；后端日志搜 `[RTSP拉流]` |
| 前端拿到流地址但播放失败 | 浏览器 → ZLM 端口不通 / 协议不匹配 | 浏览器 Network 看 FLV/HLS/WS 请求状态码 |

## 多协议接入

平台通过 `DeviceAccessProtocol` 抽象统一不同接入协议，业务层（PlayController 等）只看 `accessProtocol` 字段路由到对应实现，新增协议无需改任何上层代码。

| 协议 | 模型 | 状态 | 主要落点 |
|------|------|------|---------|
| **GB/T 28181** | 设备 REGISTER 推流 → SIP INVITE 协商 | ✅ 完整 | `gb28181/`, `protocol/Gb28181AccessProtocol` |
| **RTSP** | 平台主动 ZLM `addStreamProxy` 拉流 | ✅ 完整 | `protocol/RtspAccessProtocol`, `service/stream/RtspPlayService` |
| **ONVIF** | WS-Discovery 发现 → SOAP 取 StreamUri → 走 RTSP | ✅ 完整 | `onvif/`, `protocol/OnvifAccessProtocol` |
| **JT/T 1078** | TCP 信令 + RTP-PS 特殊封装 | 🟡 Mock 骨架 | `jt1078/`（待实现协议栈） |
| **ISUP** | 海康 EHome SDK 私有协议 | 🟡 Mock 骨架 | `isup/`（待对接 SDK） |

### RTSP 接入

1. **设备表达**：`access_protocol = "RTSP"` + `host:port` + `protocol_config.streamSource`（`url` / `username` / `streamPath` / `rtpType`）。完整 URL 优先，否则后端用 `rtsp://user:pass@host:port/path` 拼装。
2. **拉流时序**：
   ```
   前端 POST /play/start ──▶ PlayController（按 accessProtocol 路由）
                              ▼
                          RtspAccessProtocol → RtspPlayService
                              ▼
                          ZLM addStreamProxy(rtsp_url, app=rtp, stream=<md5>)
                              ▼
                          构建 StreamInfo（含多协议 URL）返回前端
                              ▼
                          浏览器 → ZLM HTTP-FLV/HLS/WebRTC 拉流
   ```
3. **Stop 行为**：`closeStreams` + `delStreamProxy`，避免无人观看时占用 ZLM 资源。
4. **streamId 设计**：MD5(deviceIdentification + channelIdentification) 前 16 位前缀 `rtsp_`，同设备同通道多次点播复用同一路流。
5. **字典前置**：在系统字典 `VIDEO_DEVICE_ACCESS_PROTOCOL` 中确保有 `RTSP` 选项；前端表单按 `accessProtocol` 动态显示 `streamSourceUrl/Username/Path/RtpType`。

### ONVIF 接入

实现思路：发现层 + 控制层（SOAP）+ 取流层（RTSP），三层串起来。

1. **发现**（`OnvifDiscoveryService`）：UDP 组播 `239.255.255.250:3702` 发送 WS-Discovery Probe，监听 ProbeMatch；典型 3-5 秒收齐局域网内所有 ONVIF 摄像头，按 EndpointReference 去重。
2. **SOAP 控制**（`OnvifSoapClient`）：手写 SOAP envelope（不引入 CXF），核心调用 `GetDeviceInformation` / `GetProfiles` / `GetStreamUri`，认证用 `OnvifAuthenticator` 生成 WS-Security UsernameToken（`base64(sha1(nonce + created + password))`）。
3. **导入**（`OnvifService.importDevice`）：取 RTSP URL → 注入凭据 → 落到 `protocol_config.streamSource.url`，通用 save 入口走完 RTSP 设备校验和默认通道；后续点播自动复用 `RtspPlayService` 流程。
4. **REST**：
   - `POST /video/onvif/discover` 扫描
   - `POST /video/onvif/profiles?xaddr=...&username=...&password=...` 取主/子码流
   - `POST /video/onvif/import?xaddr=...&profileToken=...&mediaIdentification=...` 导入为设备

### 后续扩展（TODO）

- JT/T 1078：完整 JT808 信令栈（鉴权/心跳）+ JT1078 RTP-PS 解封 → ZLM 推流（当前仅 Mock 骨架）
- ISUP（EHome 5.0）：对接海康 SDK（JNA） → RTP 转 ZLM（当前仅 Mock 骨架）
- ONVIF PTZ / 录像回放（Profile G）：SOAP 通路已有，业务侧未对接
- 国标级联（下级 → 上级平台互联）
- 跨网闸平台互联

## 核心架构约定

### 分层调用规范

```
Controller / Listener / Commander
        ↓
    ServiceImpl（@DS 多租户数据源路由）
        ↓
    ManagerImpl
        ↓
      Mapper
```

所有数据库操作必须经过 ServiceImpl 层（标注 `@DS(DsConstant.BASE_TENANT)`），禁止跨层直接调用 Manager 或 Mapper。

### 协议层隔离原则

- **进程绑定的组件**（端口监听、协议栈、SIP 指令收发）→ `video-biz-protocol`
- **纯业务逻辑**（CRUD、缓存管理、流媒体节点管理）→ `video-biz`
- 判断标准：如果一个类需要 `SIPSender`、`Commander`、`SipLayer` 等协议 Bean，它属于 `video-biz-protocol`

### 事件架构

项目采用 Spring Event 进行模块间解耦通信，事件分布遵循分层隔离原则：

#### 事件定义层次

| 层级 | 事件类型 | 示例 | 说明 |
|------|---------|------|------|
| **entity** | 通用领域事件 | `MediaServerOnlineEvent`、`DeviceInfoOnlineEvent` | 协议无关的抽象事件，供 biz 和 protocol 共用 |
| **biz** | 流媒体 Hook 事件 | `MediaArrivalEvent`、`MediaRecordMp4Event` | ZLM/ABL Hook 回调触发的流媒体事件 |
| **biz-protocol** | 协议专属事件 | `SipCommandSentEvent`、`InviteSessionCreatedEvent`、`IsupDeviceOnlineEvent` | GB28181/ISUP/JT1078 协议栈内部事件 |

#### 事件隔离规则

- **协议事件严禁往下渗透**：所有 SIP、GB28181、ISUP、JT1078 相关的事件（定义、发布、监听）必须在 `video-biz-protocol` 内闭环，不得出现在 `video-biz` 中
- **Biz 层事件必须协议无关**：`video-biz` 中的事件和监听器不得依赖任何协议 Bean（`SIPSender`、`Commander`、`SipLayer` 等）
- **跨层通信通过 entity 事件**：协议层需要通知业务层时，使用 entity 层定义的通用事件（如 `DeviceInfoOnlineEvent`），而非协议专属事件

#### executor 中的事件安全性

`iot-executor` 通过 `boot-impl` 依赖 `video-biz`，因此 biz 层的事件监听器会被加载到 executor 进程中。安全性保证：

- **`MediaServerStatusEventListener`**（监听 Online/Offline 事件）：StatusManager 在 Job 执行时会触发，监听器调用 `videoMediaServerService.serverOnline/Offline()` 做 DB 状态更新，有 `@DS` 路由，**安全**
- **Hook 类监听器**（`HookZlmServerKeepaliveEventListener` 等）：executor 中没有 ZLM Hook 回调源，**不会被触发**
- **`MediaRecordMp4EventListener`**：executor 中没有录像 Hook 回调源，**不会被触发**
- **`HookSubscribe`**：内存订阅管理，executor 中无订阅注册，**空转无副作用**

### 数据源路由规范

所有数据库操作必须经过 `ServiceImpl` 层，并标注 `@DS(DsConstant.BASE_TENANT)` 实现多租户数据源路由。

- `ServiceImpl` 是数据源切换的唯一入口，**禁止跨层直接调用 Manager 或 Mapper**
- `ManagerImpl` 不标注 `@DS`（全模块统一规范，与 Link 模块一致）
- `StatusManager`（如 `ZLMMediaServerStatusManager`）作为 executor Job 调用链的首个入口 Bean，也需要标注 `@DS`，确保 ThreadLocal 数据源在调用 ServiceImpl 前已正确设置

### 新增协议接入指引

1. 在 `video-biz-protocol` 中创建协议包（如 `onvif/`）
2. 实现 `DeviceAccessProtocol` 接口
3. 在 `protocol/` 包中注册协议适配器
4. 协议相关的事件（定义、发布、监听）全部放在协议包内，严禁渗透到 `video-biz`
5. 无需修改 executor 或其他模块 — 协议代码天然隔离在 protocol 模块中

### 注意事项

1. **禁止在 executor 引入协议模块**：`iot-executor` 只依赖 `video-boot-impl` → `video-biz`，绝不能引入 `video-biz-protocol`，否则 SipLayer 等 `CommandLineRunner` 会在 executor 中启动导致 NPE
2. **新增 @Scheduled 任务需评估归属**：如果定时任务是纯业务逻辑（CRUD、缓存刷新），应迁移到 `iot-executor` 通过 XXL-Job 调度；如果是进程绑定的协议逻辑（SIP 消息队列、Hook 超时清理），保留在 `video-server` 本地
3. **事件监听器不得依赖协议 Bean**：`video-biz` 中的 `@EventListener` 方法只能注入 Service/Manager/Factory 等业务 Bean，不得注入 `SIPSender`、`Commander` 等协议组件
4. **Controller 引用协议层服务**：`video-controller` 直接依赖 `video-biz-protocol`（PlayService、PtzService 等），这是合理的 — Controller 运行在 `video-server` 进程中，协议 Bean 可用
5. **测试依赖**：`spring-boot-starter-test` 仅在 `video-server` 引入，子模块通过 Maven 依赖传递获取，无需重复声明
