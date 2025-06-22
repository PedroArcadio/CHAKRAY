# Etapa 1: Build con Maven y JDK 11 (temurin)
FROM maven:3.9.6-eclipse-temurin-11 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Runtime con OpenJDK 11
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
