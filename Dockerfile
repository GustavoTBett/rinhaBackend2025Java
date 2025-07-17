# Etapa de build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia tudo (inclusive o pom pai e os módulos)
COPY . .

# Faz build do projeto pai (gera os .jar dos módulos também)
RUN mvn clean install -DskipTests

# Etapa final: apenas o JAR do processor
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/processor/target/processor.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
