FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace/notification-service

COPY notification-service/pom.xml pom.xml
RUN mvn -q -DskipTests dependency:go-offline

COPY notification-service/src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/notification-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
