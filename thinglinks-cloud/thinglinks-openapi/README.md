# thinglinks-openapi 模块

## 模块概述

`thinglinks-openapi` 是 ThingLinks**开放 API 模块**，负责对外提供标准化、安全可靠的 OpenAPI 接口服务。该模块基于 SOP（Simple
Open Platform）网关框架构建，支持多租户、签名验签、限流熔断等企业级特性。

## 模块定位

| 定位         | 说明                                                        |
|------------|-----------------------------------------------------------|
| **对外开放**   | 面向第三方应用、合作伙伴提供标准化 API                                     |
| **北向接口**   | 提供设备管理、产品管理、OTA 升级等物联网核心能力的北向 API                         |
| **API 网关** | 统一的 API 入口，支持签名验签、限流、熔断、监控等                               |
| **文档自动化**  | 基于 smart-doc 自动生成 API 文档并推送至 Torna,供ThingLinks 开放平台进行文档同步 |

## 目录结构

```
thinglinks-openapi/
├── README.md                          # 模块说明文档
├── pom.xml                            # 父 POM
├── thinglinks-openapi-biz/            # 业务逻辑层
│   └── src/main/java/.../openapi/open/
│       ├── AbstractBaseApiImpl.java   # API 基类
│       └── iot/                       # 物联网北向 API
│           ├── device/                # 设备管理 API
│           │   ├── IotNorthboundDeviceManagerApi.java      # 接口定义
│           │   ├── impl/              # 接口实现
│           │   ├── req/               # 请求实体
│           │   ├── resp/              # 响应实体
│           │   └── converter/         # 响应转换器
│           ├── product/               # 产品管理 API
│           └── ota/                   # OTA 升级 API
├── thinglinks-openapi-controller/     # 控制器层（回调等）
├── thinglinks-openapi-entity/         # 公共实体和枚举
│   └── enumeration/
│       └── ErrorStoryMessageEnum.java # 错误码枚举
└── thinglinks-openapi-server/         # 启动模块
    └── src/main/resources/
        ├── application.yml            # 配置文件
        ├── smart-doc.json             # 文档生成配置
        └── i18n/                      # 国际化资源
```

## 使用规范

### 1. API 命名规范

| 类型  | 命名规则                                    | 示例                                  |
|-----|-----------------------------------------|-------------------------------------|
| 接口类 | `IotNorthbound{Domain}{Api}`            | `IotNorthboundDeviceManagerApi`     |
| 实现类 | `IotNorthbound{Domain}{Api}Impl`        | `IotNorthboundDeviceManagerApiImpl` |
| 请求类 | `IotNorthbound{Domain}{Action}Request`  | `IotNorthboundDeviceCreateRequest`  |
| 响应类 | `IotNorthbound{Domain}{Action}Response` | `IotNorthboundDeviceCreateResponse` |
| 转换器 | `{Domain}ResponseConverter`             | `DeviceResponseConverter`           |

### 2. 包结构规范

每个业务领域应包含以下子包：

```
iot/{domain}/
├── IotNorthbound{Domain}Api.java     # 接口定义（使用 @Open 注解）
├── impl/                              # 实现类（使用 @DubboService 注解）
├── req/                               # 请求 DTO
├── resp/                              # 响应 DTO
└── converter/                         # 响应转换器
```

### 3. 接口定义规范

```java

@Open(value = "iot.northbound.device.create", bizCode = "iot_device")
IotNorthboundDeviceCreateResponse createDevice(
        IotNorthboundDeviceCreateRequest request,
        OpenContext context
) throws OpenException;
```

- `value`: API 唯一标识，格式为 `{模块}.{领域}.{操作}`
- `bizCode`: 业务编码，用于分类管理
- `OpenContext`: 包含 appId、tenantId 等上下文信息

### 4. 请求/响应实体规范

**请求实体要求：**

- 使用 `@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor` 注解
- 字段添加校验注解：`@NotBlank`、`@NotNull`、`@Size`、`@Min`、`@Max` 等
- 字段添加 Javadoc 注释和 `@mock` 标签（用于文档生成）

```java
/**
 * 设备标识（设备的唯一标识）
 * @mock DEV_001
 */
@NotBlank(message = "设备标识不能为空")
@Size(max = 64, message = "设备标识长度不能超过64位")
private String deviceIdentification;
```

**响应实体要求：**

- 不添加 Swagger 注解，仅使用 Javadoc 和 `@mock`
- 复杂响应结构使用 Converter 进行转换

### 5. 响应转换器规范

```java

@UtilityClass
public class DeviceResponseConverter {

    public static IotNorthboundDeviceCreateResponse convertToCreateDeviceResponse(
            DeviceResultVO deviceResultVO) {
        return Optional.ofNullable(deviceResultVO)
                .map(vo -> IotNorthboundDeviceCreateResponse.builder()
                        .deviceIdentification(vo.getDeviceIdentification())
                        .deviceName(vo.getDeviceName())
                        .build())
                .orElseGet(() -> IotNorthboundDeviceCreateResponse.builder().build());
    }
}
```

### 6. 错误码规范

在 `ErrorStoryMessageEnum` 中定义错误码：

```java
DEVICE_CREATE_FAILED("isp.device-create-failed","设备创建失败","请检查设备信息是否完整"),
```

- `subCode`: 错误码（格式：`isp.{domain}-{action}-{result}`）
- `subMsg`: 错误信息
- `solution`: 解决方案

### 7. 实现类规范

```java

@DubboService
@Slf4j
public class IotNorthboundDeviceManagerApiImpl implements IotNorthboundDeviceManagerApi {

    @Resource
    private DeviceOpenInnerFacade deviceOpenInnerFacade;

    @Override
    public IotNorthboundDeviceCreateResponse createDevice(
            IotNorthboundDeviceCreateRequest request, OpenContext context) {
        // 1. 日志记录
        log.info("createDevice...params: appId={}, tenantId={}, deviceIdentification={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification());

        // 2. 参数校验
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");

        // 3. 设置租户上下文
        ContextUtil.setTenantId(context.getTenantId());

        // 4. 调用 Facade 层
        var result = deviceOpenInnerFacade.saveDeviceByNorthbound(deviceSaveVO);

        // 5. 处理错误
        if (!result.getIsSuccess()) {
            throw new OpenException(
                    ErrorStoryMessageEnum.DEVICE_CREATE_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_CREATE_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_CREATE_FAILED.getSolution());
        }

        // 6. 使用 Converter 转换响应
        return DeviceResponseConverter.convertToCreateDeviceResponse(result.getData());
    }
}
```

## 开发流程

### 新增 API 开发步骤

1. **定义接口** - 在 `IotNorthbound{Domain}Api` 中定义方法，添加 `@Open` 注解
2. **创建请求/响应实体** - 在 `req/` 和 `resp/` 目录下创建 DTO
3. **实现接口** - 在 `impl/` 目录下实现接口逻辑
4. **添加转换器** - 在 `converter/` 目录下添加响应转换方法
5. **定义错误码** - 在 `ErrorStoryMessageEnum` 中添加业务错误码
6. **推送文档** - 执行文档推送命令

---
