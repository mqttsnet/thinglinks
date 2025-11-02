# 使用 Java 运行环境
FROM registry.cn-hangzhou.aliyuncs.com/mqttsnet-community/openjdk:17-jdk

# 维护者信息
LABEL maintainer="mqttsnet"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 环境配置（可根据实际情况调整）
ENV JVM_XMS=4g \
    JVM_XMX=8g \
    JVM_MAX_METASPACE=1g \
    JVM_RESERVED_CODE_CACHE=512m \
    JVM_INITIAL_CODE_CACHE=256m \
    SPRING_PROFILES_ACTIVE=prod

# 创建工作目录
WORKDIR /data01/mqttsnet/thinglinks/thinglinks-job-admin

# 从构建上下文（Dockerfile 所在目录的相对路径）复制 JAR 文件
COPY target/thinglinks-job-admin.jar /app.jar

# 暴露服务端口
EXPOSE 18767

# 添加健康检查，确保应用健康状态
HEALTHCHECK --interval=30s --timeout=30s --retries=3 \
  CMD curl -f http://localhost:18767/actuator/health || exit 1

# 启动 Java 应用
ENTRYPOINT ["sh", "-c", "exec java \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  -Xms${JVM_XMS} \
  -Xmx${JVM_XMX} \
  -XX:MaxMetaspaceSize=${JVM_MAX_METASPACE} \
  -XX:ReservedCodeCacheSize=${JVM_RESERVED_CODE_CACHE} \
  -XX:InitialCodeCacheSize=${JVM_INITIAL_CODE_CACHE} \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseZGC \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:-ZProactive \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=./heapdump.hprof \
  -XX:ErrorFile=./hs_err_pid%p.log \
  -Dfile.encoding=UTF-8 \
  -Djava.security.egd=file:/dev/./urandom \
  -Ddruid.mysql.usePingMethod=false \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
  -jar /app.jar \
  \"$@\""]

# 用于接收运行时参数
CMD []
