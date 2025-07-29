FROM amazoncorretto:21-alpine-jdk

COPY target/rinhaBackend2025Consumer-1.0-SNAPSHOT.jar rinhaBackend2025Consumer-docker.jar

ENTRYPOINT ["java", "-jar", "/rinhaBackend2025Consumer-docker.jar"]