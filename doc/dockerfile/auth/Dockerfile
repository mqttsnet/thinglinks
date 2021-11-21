# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/auth
ADD mqtts-auth-1.0.0.RELEASE.jar auth.jar
EXPOSE 19200
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/auth.jar"]