# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/gateway
ADD mqtts-gateway-1.0.0.RELEASE.jar gateway.jar
EXPOSE 19100
EXPOSE 19101
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/gateway.jar"]