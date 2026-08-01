FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY . .

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn clean package -Pprod -Dmaven.test.skip=true -T 1C


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG MODULE

COPY --from=builder /build/${MODULE}/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]