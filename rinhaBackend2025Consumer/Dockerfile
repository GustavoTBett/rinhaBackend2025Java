FROM openjdk:21-jdk-slim

WORKDIR /app

ADD /target/rinhaBackend2025Consumer-1.0-SNAPSHOT.jar /app/rinhaBackend2025Consumer-docker.jar

EXPOSE 9999

ENTRYPOINT ["java", "-jar", "rinhaBackend2025Consumer-docker.jar"]