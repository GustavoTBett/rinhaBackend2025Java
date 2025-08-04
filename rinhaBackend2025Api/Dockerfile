FROM ubuntu:jammy

RUN apt-get update && apt-get install -y curl

COPY target/rinhaBackend2025Api /rinhaBackend2025Api

CMD ["/rinhaBackend2025Api"]