# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/file
ADD mqtts-modules-file-1.0.0.RELEASE.jar file.jar
EXPOSE 19300
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/file.jar"]