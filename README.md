
# 🏗️ Rinha de Backend 2025 - Java + Spring + Redis + RabbitMQ

Este projeto foi desenvolvido como parte do desafio **Rinha de Backend 2025** utilizando **Java 21**, **Spring Boot 3**, **Redis**, **RabbitMQ**, **Docker** e arquitetura resiliente com fallback.

## 📐 Arquitetura

A arquitetura foi desenhada com foco em **resiliência**, **desempenho**, **tolerância a falhas** e **escalabilidade horizontal**.

### Componentes


- **Producer (API Spring Boot)**  
  Responsável por receber as requisições e publicar mensagens na fila do RabbitMQ.

- **RabbitMQ**  
  Controla a comunicação assíncrona entre Producer e Consumers.

- **Consumers**  
  Serviços responsáveis por consumir mensagens, aplicar lógica de negócio e persistir dados no Redis.

- **Redis**  
  Utilizado como banco de dados principal com uso de ZSETs para otimizar queries por data.

- **Serviço Externo (Default & Fallback)**  
  O processamento principal ocorre via chamadas HTTP. Em caso de falha, o consumer reencaminha a mensagem para fallback automaticamente.

## ⚙️ Tecnologias

- Java 21
- Spring Boot 3
- Redis (como banco principal)
- RabbitMQ
- Docker & Docker Compose
- Observabilidade com HealthCheck

## 🚀 Execução local

1. Clone o repositório:

   ```bash
   git clone https://github.com/seu-usuario/rinha-backend-2025.git
   cd rinha-backend-2025
   ```

2. Construa e suba os containers:

   ```bash
   docker compose up --build
   ```

3. Acesse a API:

   ```
   http://localhost:9999
   ```

## 📊 Metas de desempenho

- CPU máxima: `1.5`
- Memória máxima: `350MB`

## 📁 Estrutura do Projeto

```
rinha-backend-2025/
├── model/          # Módulo comum com entidades e enums
├── processor/      # Módulo da API Producer
├── consumer/       # Módulo que consome da fila e processa
├── docker-compose.yaml
├── README.md
└── ...
```

## 📌 Observações

- Todas as comunicações são feitas via HTTP REST.
- Fallback é automático caso o serviço padrão esteja fora ou responda com erro.
- Persistência é baseada em Redis com ZSet para ordenação por data (`createdAt`).

## 📚 Referências

- [Vídeo do Francisco Zanfranceschi](https://www.youtube.com/@zanfranceschi)
- https://www.baeldung.com/spring-data-redis-tutorial
- https://medium.com/@gcbrandao/diferen%C3%A7a-entre-restcontroller-e-controller-annotation-no-spring-mvc-e-rest-8533998a93eb
- https://www.baeldung.com/spring-boot-change-request-body-before-controller
- https://www.baeldung.com/spring-request-param
- https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html
- https://stackoverflow.com/questions/26142864/how-to-get-utc0-date-in-java-8
- https://www.baeldung.com/spring-5-webclient
- https://www.baeldung.com/spring-amqp
- https://www.docker.com/blog/how-to-use-the-redis-docker-official-image/
- https://www.baeldung.com/spring-data-redis-properties
- https://www.youtube.com/watch?v=IEJJ1tcAZTo
- https://www.baeldung.com/maven-multi-module
- https://www.baeldung.com/spring-nosuchbeandefinitionexception
- https://www.geeksforgeeks.org/springboot/spring-boot-crud-operations-using-redis-database/

