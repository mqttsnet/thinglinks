# 使用 Java 运行环境
FROM registry.cn-hangzhou.aliyuncs.com/mqttsnet-community/openjdk:17-jdk

# 维护者信息
LABEL maintainer="mqttsnet mqttsnet@163.com"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建工作目录
WORKDIR /home/www/mqttsnet/thinglinks-modules-system

# 从构建上下文（Dockerfile 所在目录的相对路径）复制 JAR 文件
COPY target/thinglinks-modules-system.jar /app.jar

# 暴露服务端口
EXPOSE 19303

# 添加健康检查，确保应用健康状态
HEALTHCHECK --interval=30s --timeout=30s --retries=3 \
  CMD curl -f http://localhost:19303/actuator/health || exit 1

# 启动 Java 应用
ENTRYPOINT ["java", \
  "--add-opens=java.base/java.lang=ALL-UNNAMED", \
  "-Xms512m", "-Xmx1024m", \
  "-XX:ReservedCodeCacheSize=256m", \
  "-XX:InitialCodeCacheSize=256m", \
  "-XX:+UnlockExperimentalVMOptions", \
  "-XX:+UseZGC", \
  "-XX:ConcGCThreads=1", "-XX:ParallelGCThreads=2", \
  "-XX:ZCollectionInterval=30", "-XX:ZAllocationSpikeTolerance=5", \
  "-XX:+UnlockDiagnosticVMOptions", "-XX:-ZProactive", \
  "-Dfile.encoding=UTF-8", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app.jar"]
