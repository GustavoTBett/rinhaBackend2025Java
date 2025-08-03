# 🏗️ Rinha de Backend 2025 - Java + Spring Boot + Redis (sem fila externa)

Este projeto foi desenvolvido para o desafio **Rinha de Backend 2025**, com foco em entregar uma solução **performática, resiliente** e que respeita os limites de **CPU** e **memória** definidos pelo regulamento.

---

## ⚙️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3** com WebFlux (não-bloqueante)
- **Redis** (armazenamento e fila em memória)
- **Docker & Docker Compose**
- **NGINX** (load balancer)
---

## 🌀 Histórico do Projeto

### 💡 Versão Inicial (RabbitMQ + Processamento Distribuído)

A arquitetura inicial foi baseada em uma estrutura desacoplada com:

- Um serviço **Producer** responsável por receber requisições e publicar mensagens numa fila do **RabbitMQ**.
- Um ou mais serviços **Consumers** que processavam essas mensagens e interagiam com os serviços externos (Payment Processors).
- Armazenamento de dados e relatórios feito no **Redis**.

**Problema:**  
Apesar da arquitetura escalável, o stack completo com Java + Spring + RabbitMQ + Redis ultrapassava facilmente os **350MB de RAM**, mesmo com ajustes em JVM flags e tuning fino.

### 🔄 Mudança para uma Arquitetura Monolítica com Redis

Decidi então simplificar para uma arquitetura **monolítica** com os seguintes ajustes:

- O `Consumer` roda **dentro da própria API**, em uma thread separada, escutando os pagamentos salvos no Redis (simulando uma fila).
- **Redis ZSETs** são usados como mecanismo de fila e banco de dados ao mesmo tempo, com ordenação por data (`requestedAt`).
- **Fallback** entre Payment Processors foi mantido, com estratégia inteligente baseada em cache local dos `health-checks`.

Essa decisão permitiu reduzir significativamente o uso de memória, respeitando os **limites impostos pela Rinha**.

---

## 🧠 Arquitetura Atual

### 🧩 Componentes

- **API (Spring Boot)**  
  Responsável por:
  - Receber requisições `POST /payments` e salvar na fila (Redis).
  - Responder `GET /payments-summary` consultando agregações no Redis.
  - Rodar o `consumer` internamente que processa a fila.

- **Redis**  
  Usado como:
  - Fila com ZSET (pagamentos ordenados por tempo).
  - Base de dados para geração de relatório.

- **Payment Processor Default / Fallback**  
  Serviços externos simulando instabilidade e diferentes taxas de processamento.

- **NGINX**  
  Load balancer para as duas instâncias da API (obrigatório pelo desafio).

---

## 🔄 Fluxo de Processamento

### 📥 POST /payments

1. Recebido pela API (via NGINX).
2. Salvo no Redis (ZSET).
3. Consumidor interno lê da fila (polling) e tenta enviar para o serviço `Default`.
4. Em caso de falha, tenta o `Fallback`.
5. Se ambos falharem, o pagamento é reenfileirado.

### 📊 GET /payments-summary

1. Retorna totais agregados de requisições e valores por processador (`default` e `fallback`) a partir dos dados no Redis.

---

## 📌 Estratégias Técnicas

- **Fallback inteligente** com cache local dos health-checks (`5s TTL`), evitando chamadas excessivas (HTTP 429).
- **Consumer assíncrono** com delay configurável entre polls para balancear carga.
- **Redis como base de dados e fila** reduzindo dependências externas e memória.
- **Deploy com Docker Compose** respeitando:
  - `1.5 CPU`
  - `350MB RAM` somando todos os serviços

---

## 🚀 Como Rodar

```bash
git clone https://github.com/seu-usuario/rinha-backend-2025.git
cd rinha-backend-2025
docker compose up --build
```

Acesse: [http://localhost:9999](http://localhost:9999)

---

## 📁 Estrutura

```
rinha-backend-2025/
├── model/              # Módulo com entidades e DTOs
├── api/                # API com controller, redis e consumer interno
├── docker-compose.yml  # Orquestração
└── README.md
```

---

## 📚 Referências

- [Introduction to Spring Data Redis | Baeldung](https://www.baeldung.com/spring-data-redis-tutorial)
- [Diferença entre @RestController e @Controller Annotation no Spring MVC e REST | Medium](https://medium.com/@gcbrandao/diferen%C3%A7a-entre-restcontroller-e-controller-annotation-no-spring-mvc-e-rest-8533998a93eb)
- [Modify Request Body Before Reaching Controller in Spring Boot | Baeldung](https://www.baeldung.com/spring-boot-change-request-body-before-controller)
- [Spring @RequestParam Annotation | Baeldung](https://www.baeldung.com/spring-request-param)
- [WebClient :: Spring Framework Docs](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [How to get UTC+0 date in Java 8? | Stack Overflow](https://stackoverflow.com/questions/26142864/how-to-get-utc0-date-in-java-8)
- [Spring WebClient | Baeldung](https://www.baeldung.com/spring-5-webclient)
- [Messaging Using Spring AMQP | Baeldung](https://www.baeldung.com/spring-amqp)
- [How to Use the Redis Docker Official Image | Docker](https://www.docker.com/blog/how-to-use-the-redis-docker-official-image/)
- [Spring Data Redis's Property-Based Configuration | Baeldung](https://www.baeldung.com/spring-data-redis-properties)
- [Redis In Spring Boot: Exploring Redis As A Database | YouTube - JavaCodeEx](https://www.youtube.com/watch?v=IEJJ1tcAZTo)
- [Develop Multi Module Spring Boot Project Architecture | YouTube - EnggAdda](https://www.youtube.com/watch?v=QQ4oyr93B8k)
- [Multi-Module Project with Maven | Baeldung](https://www.baeldung.com/maven-multi-module)
- [Spring NoSuchBeanDefinitionException | Baeldung](https://www.baeldung.com/spring-nosuchbeandefinitionexception)
- [Spring Boot CRUD Operations Using Redis Database | GeeksforGeeks](https://www.geeksforgeeks.org/springboot/spring-boot-crud-operations-using-redis-database/)
- [CI/CD Pipeline with GitHub Actions and Docker | GitHub](https://github.com/OswaldAKs/CICD-Pipeline-with-GithubActions-Docker/tree/main)
- [Dockerizing a Spring Boot Application | Baeldung](https://www.baeldung.com/dockerizing-spring-boot-application)
- [Pub/Sub Messaging with Spring Data Redis | Baeldung](https://www.baeldung.com/spring-data-redis-pub-sub)
- [Pub/Sub Messaging :: Spring Data Redis Docs](https://docs.spring.io/spring-data/redis/reference/redis/pubsub.html)
- [Introduction to Java Serialization | Baeldung](https://www.baeldung.com/java-serialization)
- [Introduction to Lettuce - the Java Redis Client | Baeldung](https://www.baeldung.com/java-redis-lettuce)

---

> **Nota pessoal:** A experiência mostrou que, às vezes, menos é mais. Ao trocar complexidade por simplicidade, consegui entregar uma solução eficiente e dentro das regras – e é isso que importa numa competição com restrições reais.
