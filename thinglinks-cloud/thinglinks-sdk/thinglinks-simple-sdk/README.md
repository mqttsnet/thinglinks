# sdk-java

开放平台把接口开发完毕后，一般需要开发对应的SDK，提供给ISV。SOP提供了一个基础的SDK开发包

开发者可以在此基础上做开发，就拿sdk-java来说，具体步骤如下：

## sdk-java

SDK依赖了三个jar包

+ okhttp.jar 用于网络请求
+ fastjson.jar 用于json处理
+ commons-logging.jar 日志处理

### 接口封装步骤

比如获取故事信息接口

+ 接口名：story.get
+ 版本号：1.0
+ 参数：id
+ 返回信息

```json
{
    "subCode": "",
    "subMsg": "",
    "code": "0",
    "msg": "success",
    "data": {
        "addTime": "2024-11-08 10:21:23",
        "name": "乌鸦喝水",
        "id": 1
    }
}
```

针对这个接口，封装步骤如下：

1.在`model`包下新建一个类，定义业务参数

```java
@Data
public class GetStoryModel {

    private Integer id;
}
```

2.在`response`包下新建一个返回类GetStoryResponse

里面填写返回的字段

```plain
@Data
public class GetStoryResponse {
    private Long id;
    private String name;
    private Date addTime;
}
```

3.在`request`包下新建一个请求类，继承`BaseRequest`

BaseRequest中有个泛型参数，填`GetStoryResponse`类，表示这个请求对应的返回类。  
重写`method()`方法，填接口名。

如果要指定版本号，可重写`version()`方法，或者后续使用`request.setVersion(version)`进行设置

```java
public class GetStoryRequest extends BaseRequest<GetStoryResponse> {
    @Override
    protected String method() {
        return "story.get";
    }

}
```

可重写`getRequestMethod()`方法指定HTTP请求method，默认是POST。

```java
@Override
protected RequestMethod getRequestMethod() {
    return RequestMethod.GET;
}
```

**建议读请求用GET，写请求用POST**，

### 使用方式

```java
String url = System.getenv("THINGLINKS_OPENAPI_URL");
String appId = System.getenv("THINGLINKS_OPENAPI_APP_ID");
String privateKey = System.getenv("THINGLINKS_OPENAPI_PRIVATE_KEY");

// 声明一个就行
OpenClient client = new OpenClient(url, appId, privateKey);

@Test
public void testGet() {
    // 创建请求对象
    GetStoryRequest request = new GetStoryRequest();
    // 请求参数
    GetStoryModel model = new GetStoryModel();
    model.setId(1);
    request.setBizModel(model);

    // 发送请求
    Result<GetStoryResponse> result = client.execute(request);

    if (result.isSuccess()) {
        GetStoryResponse response = result.getData();
        // 返回结果
        System.out.println(String.format("response:%s",
                JSON.toJSONString(response)));
    } else {
        System.out.println("错误，subCode:" + result.getSubCode() + ", subMsg:" + result.getSubMsg());
    }
}
```

运行联调程序前，通过环境变量提供开放平台配置：

| 环境变量 | 用途 |
| --- | --- |
| `THINGLINKS_OPENAPI_URL` | 开放平台 API 入口地址 |
| `THINGLINKS_OPENAPI_APP_ID` | 开放平台应用 ID |
| `THINGLINKS_OPENAPI_PRIVATE_KEY` | 应用签名私钥 |

联调配置仅存放在本机环境或密钥管理系统中，不写入源码、配置文件和日志。
