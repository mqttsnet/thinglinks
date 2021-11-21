# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/link
ADD mqtts-modules-link-1.0.0.RELEASE.jar link.jar
EXPOSE 19305
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/link.jar"]