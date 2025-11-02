## 开发关注点

### 其他：

AbstractGlobalExceptionHandler
添加租户时，需要初始化网关

### Gateway网关服务：

不能有：
thinglinks-validator-starter
spring-webmvc
不能使用构造器注入
所有的Feign必须延迟注入： @Lazy
thinglinks-base-server 不能加入spring-boot-starter-tomcat依赖，否则websocket冲突

必须有：thinglinks-log-start

### Facade 注入

- 任意的微服务 a 调用b微服务的 api 层接口时， 都需要在 a-server/pom.xml ，加入b-cloud-impl 的依赖。

### traceId 传递

#### traceId 自动生成传递

- 请求必须经过gateway 和 HeaderThreadLocalInterceptor，不能是异步线程，并且不能是后台启动的线程
- 内部Fegin调用会经过 FeignAddHeaderRequestInterceptor 拦截器，在拦截器中会自动添加 traceId 到请求头中。

#### traceId 手动生成传递

- 在业务代码中，通过 `ContextUtil.setLogTraceId(IdUtil.fastSimpleUUID());` 的方式进行 traceId 设置。



