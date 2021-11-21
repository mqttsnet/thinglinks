# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/gen
ADD mqtts-modules-gen-1.0.0.RELEASE.jar gen.jar
EXPOSE 19301
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/gen.jar"]