# 使用 Java 运行环境
FROM openjdk:8-jre

# 维护者信息
LABEL maintainer="mqttsnet mqttsnet@163.com"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建工作目录
WORKDIR /home/www/mqttsnet/thinglinks-modules-job

# 从构建上下文（Dockerfile 所在目录的相对路径）复制 JAR 文件
COPY target/thinglinks-modules-job.jar /app.jar

# 暴露服务端口
EXPOSE 19302

# 添加健康检查，确保应用健康状态
HEALTHCHECK --interval=30s --timeout=30s --retries=3 \
  CMD curl -f http://localhost:19302/actuator/health || exit 1

# 启动 Java 应用
ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
