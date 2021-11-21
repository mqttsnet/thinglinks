# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/broker
ADD mqtts-modules-broker-1.0.0.RELEASE.jar broker.jar
EXPOSE 19306
EXPOSE 11883
EXPOSE 18443
EXPOSE 18999
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/broker.jar"]