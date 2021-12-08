# mqttsnet=docker/dockerfile:1

FROM java:8
#FROM primetoninc/jdk:1.8
VOLUME /tmp/job
ADD thinglinks-modules-job-1.0.0.RELEASE.jar job.jar
EXPOSE 19302
ENTRYPOINT ["java", "-Xmx400m", "-Xms400m", "-Xmn150m", "-Xss1024k", "-jar", "/job.jar"]