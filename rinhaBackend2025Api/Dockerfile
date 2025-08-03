FROM amazoncorretto:21-alpine-jdk

RUN apk add --no-cache curl

COPY target/rinhaBackend2025Api app

ENTRYPOINT ["/app"]