# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/system
ADD mqtts-modules-system-1.0.0.RELEASE.jar system.jar
EXPOSE 19303
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/system.jar"]