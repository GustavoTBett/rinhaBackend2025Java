FROM amazoncorretto:21-alpine-jdk

RUN apk add --no-cache curl

COPY target/rinhaBackend2025Api-1.0-SNAPSHOT.jar rinhaBackend2025Api-docker.jar

ENTRYPOINT ["java", "-jar", "/rinhaBackend2025Api-docker.jar"]