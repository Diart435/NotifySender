FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . /build
RUN mvn clean package -pl notify-api -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/notify-api/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]