FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/com.javarush.kagarmanov.taskManager-1.0-SNAPSHOT.jar taskmanager.jar
ENTRYPOINT ["java", "-jar", "taskmanager.jar"]
