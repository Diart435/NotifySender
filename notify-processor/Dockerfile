FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . /build
RUN mvn clean package -pl notify-processor -am -Pprod -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/notify-processor/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]