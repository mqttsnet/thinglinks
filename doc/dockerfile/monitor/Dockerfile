# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/monitor
ADD mqtts-visual-monitor-1.0.0.RELEASE.jar monitor.jar
EXPOSE 19400
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/monitor.jar"]