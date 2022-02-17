# 基础镜像
FROM  openjdk:8-jre
# author
MAINTAINER thinglinks

# 挂载目录
VOLUME /www/wwwroot/thinglinks/thinglinks-server
# 创建目录
RUN mkdir -p /www/wwwroot/thinglinks/thinglinks-server
# 指定路径
WORKDIR /www/wwwroot/thinglinks/thinglinks-server
# 复制jar文件到路径
COPY ./jar/thinglinks-auth-1.0.0.RELEASE.jar /www/wwwroot/thinglinks/thinglinks-server/auth/auth.jar
# 启动认证服务
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/auth.jar"]