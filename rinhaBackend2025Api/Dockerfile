FROM ubuntu:jammy

RUN apt-get update && apt-get install -y curl

COPY target/rinhaBackend2025Api /springboot-graalvm-docker

CMD ["/springboot-graalvm-docker"]