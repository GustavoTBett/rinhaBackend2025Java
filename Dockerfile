FROM openjdk:21-jdk-slim

WORKDIR /app

ADD /rinhaBackend2025Processor/target/rinhaBackend2025Processor-1.0-SNAPSHOT.jar /app/rinhaBackend2025Processor-docker.jar

EXPOSE 9999

ENTRYPOINT ["java", "-jar", "rinhaBackend2025Processor-docker.jar"]